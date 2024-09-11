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

package com.igormaznitsa.ksrender;

import static com.igormaznitsa.ksrender.KsRenderUtils.isGlobalStorageSubTopology;
import static com.igormaznitsa.ksrender.KsRenderUtils.makeCommentNote;
import static com.igormaznitsa.ksrender.KsRenderUtils.makePumlMultiline;
import static com.igormaznitsa.ksrender.KsRenderUtils.preprocessId;
import static com.igormaznitsa.ksrender.KsRenderUtils.unicodeString;
import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KStreamsTopologyDescriptionParser {

  private static final Pattern MAIN_PATTERN =
      Pattern.compile("^(?:(<-+|-+>)|(?:([\\w\\-\\s]+?):))(.*)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern ID_TAIL_PATTERN =
      Pattern.compile("^\\s*([\\S]+)(?:\\s+(.+?))?\\s*$");
  private static final Pattern DATA_COMMENT_PATTERN =
      Pattern.compile("^\\s*(?:\\(([^)]*)\\))?(.*)\\s*$");
  private static final Pattern DATA_FINDER =
      Pattern.compile(",?\\s*([a-z_\\-]+?)\\s*:\\s*(?:\\[(.*?)]|(.*))");
  private static final String NONE = "none";
  private static final String PROPERTIES_PREFIX = "//properties ";
  private final List<Topologies> topologies = new ArrayList<>();
  private final Properties properties = new Properties();

  public KStreamsTopologyDescriptionParser(final String script) {
    final List<ParsedItem> foundItems = new ArrayList<>();
    for (final String s : script.split("\\n")) {
      String lineText = s.trim();

      if (lineText.startsWith(PROPERTIES_PREFIX)) {
        final String propertiesStr = lineText.substring(PROPERTIES_PREFIX.length()).trim();
        for (final String p : propertiesStr.split(";")) {
          final String[] parsed = p.split("=");
          if (parsed.length == 2) {
            this.properties.setProperty(parsed[0].trim(), parsed[1].trim());
          }
        }
        lineText = "";
      }

      if ("topology".equalsIgnoreCase(lineText) || "topology:".equalsIgnoreCase(lineText)) {
        lineText = "Topologies:";
      }
      if ("sub-topologies:".equalsIgnoreCase(lineText)) {
        lineText = "";
      }

      if (lineText.isEmpty() || NONE.equalsIgnoreCase(lineText)) {
        continue;
      }
      final Matcher matcher = MAIN_PATTERN.matcher(lineText);
      if (matcher.find()) {
        final String srcDst = matcher.group(1);
        final String itemName = matcher.group(2);
        final String tail = matcher.group(3);

        if (srcDst != null) {
          if (foundItems.isEmpty()) {
            throw new IllegalArgumentException(String.format("Found '%s' without element", srcDst));
          } else {
            final List<String> identifiers = Arrays.stream(tail.split(","))
                .map(String::trim)
                .filter(x -> !x.isEmpty())
                .collect(Collectors.toList());

            if (srcDst.endsWith(">")) {
              foundItems.get(foundItems.size() - 1).to.addAll(identifiers);
            } else {
              foundItems.get(foundItems.size() - 1).from.addAll(identifiers);
            }
          }
        } else {
          foundItems.add(new ParsedItem(lineText, itemName, tail));
        }
      } else {
        throw new IllegalArgumentException("Can't parse line: " + lineText);
      }
    }
    final Map<String, TopologyElement> topologyElementMaps = new TreeMap<>();
    foundItems.stream().map(i -> {
          final String lcName = i.name.toLowerCase(Locale.ENGLISH);
          final TopologyElement newElement;
          if ("topologies".equals(lcName)) {
            this.topologies.add(new Topologies(i.tail));
            newElement = null;
          } else if (lcName.startsWith("sub") && lcName.endsWith("topology")) {
            if (this.topologies.isEmpty()) {
              this.topologies.add(new Topologies(null));
            }
            final Topologies curTop = this.topologies.get(this.topologies.size() - 1);
            curTop.subTopologies.add(new SubTopology(i));
            newElement = null;
          } else {
            if (this.topologies.isEmpty()) {
              this.topologies.add(new Topologies(null));
            }
            newElement = new TopologyElement(i);
            final Topologies curTop = this.topologies.get(this.topologies.size() - 1);
            if (curTop.subTopologies.isEmpty()) {
              curTop.orphans.add(newElement);
            } else {
              curTop.subTopologies.get(curTop.subTopologies.size() - 1).children
                  .put(newElement.id, newElement);
            }
          }
          return newElement;
        }).filter(Objects::nonNull)
        .forEachOrdered(newElement -> topologyElementMaps.put(newElement.id, newElement));

    this.topologies.forEach(x -> x.link(topologyElementMaps));
  }

  public static String replaceProperties(
      final String script,
      final Properties properties
  ) {
    final StringBuilder result = new StringBuilder();
    result.append(properties2str(properties));
    for (final String s : script.split("\\r?\\n")) {
      if (!s.trim().startsWith(PROPERTIES_PREFIX)) {
        result.append(System.lineSeparator()).append(s);
      }
    }
    return result.toString();
  }

  public static String properties2str(final Properties properties) {
    return PROPERTIES_PREFIX +
        properties.stringPropertyNames().stream()
            .map(key -> String.format("%s=%s", key, properties.getProperty(key)))
            .collect(Collectors.joining(";"));
  }

  public String getProperty(final String key, final String dflt) {
    return this.properties.getProperty(key, dflt);
  }

  public int size() {
    return this.topologies.stream().mapToInt(Topologies::size).sum();
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    this.topologies.forEach(x -> builder.append(x).append("\n"));
    return builder.toString();
  }

  public Optional<TopologyElement> findForId(final String id) {
    TopologyElement result = null;
    for (final Topologies t : this.topologies) {
      result = t.findForId(id);
      if (result != null) {
        break;
      }
    }
    return Optional.ofNullable(result);
  }

  public List<Topologies> getTopologies() {
    return this.topologies;
  }

  public Map<String, String> generateKeyMap() {
    final AtomicInteger counter = new AtomicInteger(0);
    final Map<String, String> result = new HashMap<>();
    this.getTopologies().forEach(x -> {
      x.getOrphans().forEach(z -> {
        if (!result.containsKey(z.id)) {
          result.put(z.id, "__orph_" + counter.incrementAndGet());
        }
      });

      x.getSubTopologies().stream().flatMap(a -> a.getChildren().values().stream())
          .forEach(e -> {
            if (!result.containsKey(e.id)) {
              result.put(e.id, "__tel_" + counter.incrementAndGet());
            }
            e.dataItems.forEach((k, v) -> v.forEach(z -> {
              final String processedId = preprocessId(IdType.find(k), z);
              if (!result.containsKey(processedId)) {
                result.put(processedId,
                    "__dta_" + (k.hashCode() & 0x7FFFFFFF) + "_" + counter.incrementAndGet());
              }
            }));
          });
      x.getOrphans().forEach(a -> {
        if (!result.containsKey(a.id)) {
          result.put(a.id, "__tel_" + counter.incrementAndGet());
        }
      });
    });
    return result;
  }

  private String makeOptions(final Set<PlantUmlFlag> flags) {
    if (flags == null || flags.isEmpty()) {
      return "top to bottom direction\n";
    }

    final StringBuilder result = new StringBuilder();

    if (flags.contains(PlantUmlFlag.HORIZONTAL)) {
      result.append("left to right direction\n");
    } else {
      result.append("top to bottom direction\n");
    }

    if (flags.contains(PlantUmlFlag.ORTHOGONAL)) {
      result.append("skinparam linetype ortho\n");
    }

    return result.toString();
  }

  public String asPlantUml(final String title, final Set<PlantUmlFlag> flags) {
    try {
      final StringBuilder builder = new StringBuilder();

      builder.append("@startuml\n")
          .append(makeOptions(flags)).append('\n')
          .append("hide stereotype\n")
          .append("skinparam ArrowThickness 3\n")
          .append("skinparam rectangle {\n")
          .append("borderStyle<<Sub-Topologies>> dotted\n")
          .append("borderColor<<Sub-Topologies>> Gray\n")
          .append("borderThickness<<Sub-Topologies>> 2\n")
          .append("roundCorner<<Sub-Topologies>> 25\n")
          .append("shadowing<<Sub-Topologies>> false\n")
          .append("}\n")
          .append("title ").append(unicodeString(title)).append('\n');
      final Map<String, String> keys = this.generateKeyMap();

      for (final KStreamsTopologyDescriptionParser.Topologies t : this.getTopologies()) {
        builder.append("rectangle \"Sub-topologies\" <<Sub-Topologies>> {\n");
        t.getSubTopologies().stream().sorted().forEach(subTopology -> {
          builder.append(format("package \"Sub-topology %s\" %s {%n", unicodeString(subTopology.id),
              isGlobalStorageSubTopology(subTopology) ? "#FFDFFF" : "#DFDFFF"));
          builder.append(makeCommentNote(null, subTopology));
          subTopology.getChildren().values().forEach(elem -> {
            final String elemKey = keys.get(elem.id);
            final String element = KStreamType.find(elem).makePuml(elem, elemKey);
            final String elementComment = makeCommentNote(elemKey, elem);
            builder.append(element).append('\n');
            if (!elementComment.isEmpty()) {
              builder.append(elementComment).append('\n');
            }
          });
          builder.append("}\n");
        });
        builder.append("}\n");

        t.getOrphans().forEach(elem -> {
          final String elementKey = keys.get(elem.id);
          final String element = KStreamType.find(elem).makePuml(elem, elementKey);
          final String elementComment = makeCommentNote(elementKey, elem);
          builder.append(element).append('\n');
          if (!elementComment.isEmpty()) {
            builder.append(elementComment).append('\n');
          }
        });

        final StringBuilder bufferBroker = new StringBuilder();
        final StringBuilder bufferStores = new StringBuilder();
        final StringBuilder bufferOthers = new StringBuilder();

        t.getSubTopologies().stream().flatMap(st -> st.getChildren().values().stream())
            .flatMap(el -> el.dataItems.entrySet().stream())
            .forEach(es -> {
              final IdType idType = IdType.find(es.getKey());
              es.getValue().forEach(elem -> {
                switch (idType) {
                  case TOPICS: {
                    bufferBroker.append(
                        format("%s \"%s\" as %s%n", "queue",
                            makePumlMultiline(unicodeString(elem), 32),
                            keys.get(preprocessId(IdType.TOPICS, elem))));
                  }
                  break;
                  case STORES: {
                    bufferStores.append(
                        format("%s \"%s\" as %s%n", "database",
                            makePumlMultiline(unicodeString(elem), 10),
                            keys.get(preprocessId(IdType.STORES, elem))));
                  }
                  break;
                  default: {
                    bufferOthers.append(
                        format("%s \"%s\" as %s%n", "file",
                            makePumlMultiline(unicodeString(elem), 10),
                            keys.get(preprocessId(IdType.OTHERS, elem))));
                  }
                  break;
                }
              });
            });

        if (bufferBroker.length() > 0) {
          final boolean groupTopics = flags.contains(PlantUmlFlag.GROUP_TOPICS);
          if (groupTopics) {
            builder.append("package \"Topics\" #DFFFDF {\n");
          }
          builder.append(bufferBroker);
          if (groupTopics) {
            builder.append("}\n");
          }
        }

        if (bufferStores.length() > 0) {
          final boolean groupStores = flags.contains(PlantUmlFlag.GROUP_STORES);
          if (groupStores) {
            builder.append("package \"Stores\" #FED8B1 {\n");
          }
          builder.append(bufferStores);
          if (groupStores) {
            builder.append("}\n");
          }
        }

        if (bufferOthers.length() > 0) {
          builder.append("package \"Others\" #DDDDDD {\n");
          builder.append(bufferOthers);
          builder.append("}\n");
        }
      }

      this.getTopologies().forEach(topology -> Stream.concat(
              topology.getSubTopologies().stream().flatMap(st -> st.getChildren().values().stream()),
              topology.getOrphans().stream())
          .forEach(element -> {
            final String elemKey = keys.get(element.id);
            element.to.forEach(
                dst -> builder.append(format("%s -->> %s%n", elemKey, keys.get(dst.id))));
            element.from.stream()
                .filter(src -> src.to.stream().noneMatch(tl -> tl.id.equals(element.id)))
                .forEach(src -> builder.append(format("%s -->> %s%n", keys.get(src.id), elemKey)));

            element.dataItems.forEach((key, value) -> {
              final IdType idType = IdType.find(key);
              value.forEach(dataItemName -> {
                final String link;
                switch (KStreamType.find(element)) {
                  case SOURCE:
                    link = "<<=.=";
                    break;
                  case SINK:
                    link = "=.=>>";
                    break;
                  default:
                    link = "=.=";
                    break;
                }
                builder.append(format("%s %s %s%n", elemKey, link,
                    keys.get(preprocessId(idType, dataItemName))));
              });
            });
          }));

      builder.append("@enduml\n");
      return builder.toString();
    } catch (Exception ex) {
      final String errorText = ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage();
      throw new RuntimeException("@startuml\n"
          + "skinparam shadowing false\n"
          + "scale 3\n"
          + "rectangle \"<&circle-x><b>" + unicodeString(errorText) + "</b>\" #FF6666\n"
          + "@enduml", ex);
    }
  }

  public static final class ParsedItem {

    final String name;
    final String tail;
    final List<String> from;
    final List<String> to;
    final String orig;

    public ParsedItem(final String orig, final String name, final String tail) {
      this.orig = orig;
      this.name = name;
      this.tail = tail;
      this.from = new ArrayList<>();
      this.to = new ArrayList<>();
    }
  }

  public static class TopologyElement {

    public final String type;
    public final String id;
    public final String comment;
    public final Map<String, List<String>> dataItems = new TreeMap<>();
    public final List<TopologyElement> to = new ArrayList<>();
    public final List<TopologyElement> from = new ArrayList<>();
    public final ParsedItem parsedItem;

    public TopologyElement(final ParsedItem parsedItem) {
      this.parsedItem = parsedItem;
      this.type = parsedItem.name;
      final Matcher idtail = ID_TAIL_PATTERN.matcher(parsedItem.tail);
      if (idtail.find()) {
        this.id = idtail.group(1);
        final String tail = idtail.group(2);
        if (tail == null) {
          this.comment = "";
        } else {
          final Matcher dataComment = DATA_COMMENT_PATTERN.matcher(tail);
          if (dataComment.find()) {
            final String data = dataComment.group(1);
            final String foundComment = dataComment.group(2);
            if (data != null) {
              final Matcher dataFinder = DATA_FINDER.matcher(data);
              while (dataFinder.find()) {
                final String foundType = dataFinder.group(1);
                final String foundDataList = dataFinder.group(2);
                final String foundSingleData = dataFinder.group(3);
                if (foundDataList != null) {
                  final List<String> foundItems =
                      Arrays.stream(foundDataList.split(",")).map(String::trim)
                          .filter(x -> !x.isEmpty()).collect(Collectors.toList());
                  this.dataItems.put(foundType, foundItems);
                }
                if (foundSingleData != null) {
                  this.dataItems.computeIfAbsent(foundType, (x) -> new ArrayList<>())
                      .add(foundSingleData.trim());
                }
              }
            }
            this.comment = foundComment == null ? "" : foundComment;
          } else {
            this.comment = tail;
          }
        }
      } else {
        throw new IllegalArgumentException("Can't extract ID from line: " + parsedItem.orig);
      }
    }

    void link(final Map<String, TopologyElement> map) {
      this.from.clear();
      this.to.clear();

      this.parsedItem.to.forEach(x -> {
        if (!NONE.equalsIgnoreCase(x)) {
          final TopologyElement element = map.get(x);
          if (element == null) {
            throw new IllegalArgumentException("Can't find any element named " + x);
          }
          this.to.add(element);
        }
      });

      this.parsedItem.from.forEach(x -> {
        if (!NONE.equalsIgnoreCase(x)) {
          final TopologyElement element = map.get(x);
          if (element == null) {
            throw new IllegalArgumentException("Can't find any element named " + x);
          }
          this.from.add(element);
        }
      });
    }

    @Override
    public String toString() {
      return String.format("%s: %s '%s data: %s", this.id, this.type, this.comment, this.dataItems);
    }
  }

  public static final class SubTopology extends TopologyElement implements Comparable<SubTopology> {

    final Map<String, TopologyElement> children = new TreeMap<>();

    SubTopology(final ParsedItem parsedItem) {
      super(parsedItem);
    }

    @Override
    void link(final Map<String, TopologyElement> map) {
      this.children.forEach((k, v) -> v.link(map));
    }

    @Override
    public String toString() {
      final StringBuilder builder = new StringBuilder();
      builder.append(super.toString());
      this.children.forEach((k, v) -> {
        builder.append('\n').append(v);
      });
      return builder.toString();
    }

    public int size() {
      return this.children.size();
    }

    public Map<String, TopologyElement> getChildren() {
      return this.children;
    }

    public TopologyElement findForId(final String id) {
      return this.children.get(id);
    }

    @Override
    public int compareTo(final SubTopology arg0) {
      return this.id.compareTo(arg0.id);
    }
  }

  public static final class Topologies {

    private final String comment;
    private final List<SubTopology> subTopologies = new ArrayList<>();
    private final List<TopologyElement> orphans = new ArrayList<>();

    public Topologies(final String comment) {
      this.comment = comment == null ? "" : comment;
    }

    public String getComment() {
      return this.comment;
    }

    public List<TopologyElement> getOrphans() {
      return this.orphans;
    }

    void link(final Map<String, TopologyElement> map) {
      this.subTopologies.forEach(v -> v.link(map));
    }

    @Override
    public String toString() {
      final StringBuilder builder = new StringBuilder();
      builder.append("Topologies: ").append(this.comment);
      this.subTopologies.forEach(x -> {
        builder.append('\n').append(x.toString());
      });
      builder.append("\nOrphans:");
      this.orphans.forEach(x -> {
        builder.append('\n').append(x.toString());
      });
      return builder.toString();
    }

    public int size() {
      return this.subTopologies.stream().mapToInt(SubTopology::size).sum() + this.orphans.size();
    }

    public List<SubTopology> getSubTopologies() {
      return this.subTopologies;
    }

    public TopologyElement findForId(final String id) {
      TopologyElement result = null;
      for (final SubTopology t : this.subTopologies) {
        result = t.findForId(id);
        if (result != null) {
          break;
        }
      }

      if (result == null) {
        for (final TopologyElement e : this.orphans) {
          if (id.equals(e.id)) {
            result = e;
            break;
          }
        }
      }

      return result;
    }
  }
}


