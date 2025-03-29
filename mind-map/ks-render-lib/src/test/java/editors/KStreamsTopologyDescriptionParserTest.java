/*
 * Copyright (C) 2015-2024 Igor A. Maznitsa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package editors;

import static java.time.Duration.ofMinutes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.igormaznitsa.ksrender.KStreamsTopologyDescriptionParser;
import java.time.Instant;
import java.util.Optional;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Repartitioned;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.apache.kafka.streams.processor.api.ContextualProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.junit.Test;

public class KStreamsTopologyDescriptionParserTest {

  @Test
  public void testFromTopology() {
    final Topology topology = new Topology();
    topology.addSource("SomeSource", "topic1", "topic2");
    topology.addProcessor("Processor1", FakeProcessor::new, "SomeSource");
    topology.addProcessor("Processor2", FakeProcessor::new, "Processor1");
    topology.addProcessor("Processor3", FakeProcessor::new, "Processor2");
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
    assertEquals(1,
        parsed.findForId("KSTREAM-SINK-0000000003").get().dataItems.get("topic").size());
    assertEquals("c",
        parsed.findForId("KSTREAM-SINK-0000000003").get().dataItems.get("topic").stream()
            .findFirst().get());
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
    Optional<KStreamsTopologyDescriptionParser.TopologyElement> source =
        graph.findForId("sensor-a");
    assertTrue(source.isPresent());
    assertEquals(1, source.get().dataItems.get("topics").size());
    assertTrue(source.get().from.isEmpty());
    assertEquals(1, source.get().to.size());
    assertEquals("to-the-world", source.get().to.get(0).id);

    Optional<KStreamsTopologyDescriptionParser.TopologyElement> sink =
        graph.findForId("to-the-world");
    assertTrue(sink.isPresent());
    assertEquals(2, sink.get().from.size());
  }

  @Test
  public void testKsDsl1() {
    final StreamsBuilder builder = new StreamsBuilder();
    final KStream<String, String> streamOne = builder.stream("input-topic-one");
    final KStream<String, String> streamTwo = builder.stream("input-topic-two");
    final KStream<String, String> streamOneNewKey =
        streamOne.selectKey((k, v) -> v.substring(0, 5));
    final KStream<String, String> streamTwoNewKey =
        streamTwo.selectKey((k, v) -> v.substring(4, 9));
    streamOneNewKey.join(streamTwoNewKey, (v1, v2) -> v1 + ":" + v2, JoinWindows.of(ofMinutes(5)))
        .to("joined-output");

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
    final StoreBuilder<KeyValueStore<String, String>> globalStoreBuilder =
        Stores.keyValueStoreBuilder(
            Stores.persistentKeyValueStore(globalStoreName),
            Serdes.String(),
            Serdes.String());
    builder.addGlobalStore(
        globalStoreBuilder,
        "some-global-topic",
        Consumed.with(Serdes.String(), Serdes.String())
            .withTimestampExtractor(new WallclockTimestampExtractor())
            .withOffsetResetPolicy(Topology.AutoOffsetReset.EARLIEST),
        () -> new FakeProcessor()
    );
    builder.addStateStore(storeBuilder);
    builder.<String, String>stream("input")
        .filter((k, v) -> v.endsWith("FOO"))
        .repartition(Repartitioned.as("some-through-topic"))
        .processValues(() -> new SimpleValueTransformer(storeName))
        .processValues(() -> new SimpleValueTransformer(storeName), storeName)
        .to("output");

    final Topology topology = builder.build();
    final String text = topology.describe().toString();
    System.out.println(text);

    final KStreamsTopologyDescriptionParser parsed = new KStreamsTopologyDescriptionParser(text);
    assertEquals(10, parsed.size());
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

  static class SimpleValueTransformer implements FixedKeyProcessor<String, String, String> {

    private String storeName;
    private KeyValueStore<String, String> store;

    public SimpleValueTransformer(String storeName) {
      this.storeName = storeName;
    }

    @Override
    public void process(FixedKeyRecord<String, String> record) {
      String persistedValue = store.get(record.key());
      final String updatedValue = record.value() + "_" + Instant.now().toString();

      if (persistedValue == null) {
        persistedValue = updatedValue;
      }

      store.put(record.key(), updatedValue);
    }

    @Override
    public void close() {

    }
  }

  private static class FakeProcessor extends ContextualProcessor<String, String, Void, Void> {
    public FakeProcessor() {
      super();
    }

    @Override
    public void process(Record record) {

    }

  }

}
