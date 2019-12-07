/*
 * Copyright (C) 2019 Igor Maznitsa.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package com.igormaznitsa.sciareto.ui.editors;

import static java.time.Duration.ofMinutes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


import java.time.Instant;
import java.util.Optional;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.ValueTransformerWithKey;
import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.junit.Test;

public class KStreamsTopologyDescriptionParserTest {

  @Test
  public void testFromTopology() {
    final Topology topology = new Topology();
    topology.addSource("SomeSource", "topic1", "topic2");
    topology.addProcessor("Processor1", () -> new FakeProcessor(), "SomeSource");
    topology.addProcessor("Processor2", () -> new FakeProcessor(), "Processor1");
    topology.addProcessor("Processor3", () -> new FakeProcessor(), "Processor2");
    topology.addSink("TheSink", "FinalTopic", "Processor3");

    final String src = topology.describe().toString();
    System.out.println(src);

    final KStreamsTopologyDescriptionParser graph = new KStreamsTopologyDescriptionParser(src);
    System.out.println(graph);

    assertEquals(5, graph.size());
  }

  @Test
  public void testSimple() {
    final String text = "Topologies:\n"
        + "   Sub-topology: 0\n"
        + "    Source: KSTREAM-SOURCE-0000000000 (topics: [a])\n"
        + "      --> KSTREAM-MERGE-0000000001\n"
        + "\n"
        + "    Processor: KSTREAM-MERGE-0000000001 (stores: [])\n"
        + "      --> KSTREAM-SINK-0000000002\n"
        + "      <-- KSTREAM-SOURCE-0000000000\n"
        + "\n"
        + "    Sink: KSTREAM-SINK-0000000002 (topic: c)\n"
        + "      <-- KSTREAM-MERGE-0000000001";

    final KStreamsTopologyDescriptionParser parser = new KStreamsTopologyDescriptionParser(text);
    assertEquals(3, parser.size());
  }

  @Test
  public void testTwoSources() {
    final String text = "Topologies:\n"
        + "   Sub-topology: 0\n"
        + "    Source: KSTREAM-SOURCE-0000000000 (topics: [a])\n"
        + "      --> KSTREAM-MERGE-0000000002\n"
        + "\n"
        + "    Source: KSTREAM-SOURCE-0000000001 (topics: [b])\n"
        + "      --> KSTREAM-MERGE-0000000002\n"
        + "\n"
        + "    Processor: KSTREAM-MERGE-0000000002 (stores: [])\n"
        + "      --> KSTREAM-SINK-0000000003\n"
        + "      <-- KSTREAM-SOURCE-0000000000, KSTREAM-SOURCE-0000000001\n"
        + "\n"
        + "    Sink: KSTREAM-SINK-0000000003 (topic: c)\n"
        + "      <-- KSTREAM-MERGE-0000000002";

    final KStreamsTopologyDescriptionParser parsed = new KStreamsTopologyDescriptionParser(text);

    assertEquals(4, parsed.size());
    assertEquals(1, parsed.findForId("KSTREAM-SINK-0000000003").get().dataItems.get("topic").size());
    assertEquals("c", parsed.findForId("KSTREAM-SINK-0000000003").get().dataItems.get("topic").stream().findFirst().get());
  }

  @Test
  public void testManualText() {
    final String text = "Topologies:\n"
        + "   Sub-topology: 0\n"
        + "    Source: sensor-a (topics: [topic-a])\n"
        + "      --> to-the-world\n"
        + "    Source: sensor-b (topics: [topic-b])\n"
        + "      --> to-the-world\n"
        + "    Sink: to-the-world (topic: output-topic)\n"
        + "      <-- sensor-a, sensor-b";

    final KStreamsTopologyDescriptionParser graph = new KStreamsTopologyDescriptionParser(text);

    assertEquals(3, graph.size());
    Optional<KStreamsTopologyDescriptionParser.TopologyElement> source = graph.findForId("sensor-a");
    assertTrue(source.isPresent());
    assertEquals(1, source.get().dataItems.get("topics").size());
    assertTrue(source.get().from.isEmpty());
    assertEquals(1, source.get().to.size());
    assertEquals("to-the-world", source.get().to.get(0).id);

    Optional<KStreamsTopologyDescriptionParser.TopologyElement> sink = graph.findForId("to-the-world");
    assertTrue(sink.isPresent());
    assertEquals(2, sink.get().from.size());
  }

  @Test
  public void testKsDsl1() {
    final StreamsBuilder builder = new StreamsBuilder();
    final KStream<String, String> streamOne = builder.stream("input-topic-one");
    final KStream<String, String> streamTwo = builder.stream("input-topic-two");
    final KStream<String, String> streamOneNewKey = streamOne.selectKey((k, v) -> v.substring(0, 5));
    final KStream<String, String> streamTwoNewKey = streamTwo.selectKey((k, v) -> v.substring(4, 9));
    streamOneNewKey.join(streamTwoNewKey, (v1, v2) -> v1 + ":" + v2, JoinWindows.of(ofMinutes(5).toMillis())).to("joined-output");

    final Topology topology = builder.build();
    final String text = topology.describe().toString();
    System.out.println(text);

    final KStreamsTopologyDescriptionParser parsed = new KStreamsTopologyDescriptionParser(text);
    assertEquals(16, parsed.size());
    System.out.println(parsed.toString());
  }

  @Test
  public void testKsDsl2() {

    final String storeName = "stateStore";
    final String globalStoreName = "glob-stateStore";
    final StreamsBuilder builder = new StreamsBuilder();
    final StoreBuilder<KeyValueStore<String, String>> storeBuilder = Stores.keyValueStoreBuilder(
        Stores.persistentKeyValueStore(storeName),
        Serdes.String(),
        Serdes.String());
    final StoreBuilder<KeyValueStore<String, String>> globalStoreBuilder = Stores.keyValueStoreBuilder(
        Stores.persistentKeyValueStore(globalStoreName),
        Serdes.String(),
        Serdes.String());
    builder.addGlobalStore(globalStoreBuilder, "some-global-topic", Consumed.with(Serdes.Short(), Serdes.String(), new WallclockTimestampExtractor(), Topology.AutoOffsetReset.EARLIEST), () -> new FakeProcessor());
    builder.addStateStore(storeBuilder);
    builder.<String, String>stream("input")
        .filter((k, v) -> v.endsWith("FOO"))
        .transformValues(() -> new SimpleValueTransformer(storeName), storeName)
        .to("output");

    final Topology topology = builder.build();
    final String text = topology.describe().toString();
    System.out.println(text);

    final KStreamsTopologyDescriptionParser parsed = new KStreamsTopologyDescriptionParser(text);
    assertEquals(6, parsed.size());
  }

  @Test
  public void testFromGitTopologyVisualizer() {
    final String text = "Topology\n"
        + "Sub-topologies:\n"
        + "Sub-topology: 0\n"
        + "\tSource:  KSTREAM-SOURCE-0000000000 (topics: [conversation-meta])\n"
        + "\t--> KSTREAM-TRANSFORM-0000000001\n"
        + "\tProcessor: KSTREAM-TRANSFORM-0000000001 (stores: [conversation-meta-state])\n"
        + "\t--> KSTREAM-KEY-SELECT-0000000002\n"
        + "\t<-- KSTREAM-SOURCE-0000000000\n"
        + "\tProcessor: KSTREAM-KEY-SELECT-0000000002 (stores: [])\n"
        + "\t--> KSTREAM-FILTER-0000000005\n"
        + "\t<-- KSTREAM-TRANSFORM-0000000001\n"
        + "\tProcessor: KSTREAM-FILTER-0000000005 (stores: [])\n"
        + "\t--> KSTREAM-SINK-0000000004\n"
        + "\t<-- KSTREAM-KEY-SELECT-0000000002\n"
        + "\tSink: KSTREAM-SINK-0000000004 (topic: count-resolved-repartition)\n"
        + "\t<-- KSTREAM-FILTER-0000000005\n"
        + "Sub-topology: 1\n"
        + "\tSource: KSTREAM-SOURCE-0000000006 (topics: [count-resolved-repartition])\n"
        + "\t--> KSTREAM-AGGREGATE-0000000003\n"
        + "\tProcessor: KSTREAM-AGGREGATE-0000000003 (stores: [count-resolved])\n"
        + "\t--> KTABLE-TOSTREAM-0000000007\n"
        + "\t<-- KSTREAM-SOURCE-0000000006\n"
        + "\tProcessor: KTABLE-TOSTREAM-0000000007 (stores: [])\n"
        + "\t--> KSTREAM-SINK-0000000008\n"
        + "\t<-- KSTREAM-AGGREGATE-0000000003\n"
        + "\tSink: KSTREAM-SINK-0000000008 (topic: streams-count-resolved)\n"
        + "\t<-- KTABLE-TOSTREAM-0000000007\n"
        + "\t\t\t";

    final KStreamsTopologyDescriptionParser graph = new KStreamsTopologyDescriptionParser(text);
    System.out.println(graph);
    assertEquals(9, graph.size());
  }

  static class SimpleValueTransformer implements ValueTransformerWithKey<String, String, String> {

    private String storeName;
    private KeyValueStore<String, String> store;

    public SimpleValueTransformer(String storeName) {
      this.storeName = storeName;
    }

    @Override
    public void init(final ProcessorContext context) {
      store = (KeyValueStore) context.getStateStore(storeName);
    }

    @Override
    public String transform(final String key, final String value) {
      String persistedValue = store.get(key);
      final String updatedValue = value + "_" + Instant.now().toString();

      if (persistedValue == null) {
        persistedValue = updatedValue;
      }

      store.put(key, updatedValue);
      return persistedValue;
    }

    @Override
    public void close() {

    }
  }

  private class FakeProcessor extends AbstractProcessor<Object, Object> {
    public FakeProcessor() {
      super();
    }

    @Override
    public void process(Object k, Object v) {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }

}
