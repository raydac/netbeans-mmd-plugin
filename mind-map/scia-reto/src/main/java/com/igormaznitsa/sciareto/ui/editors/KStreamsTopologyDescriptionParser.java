package com.igormaznitsa.sciareto.ui.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class KStreamsTopologyDescriptionParser {

  private static final Pattern MAIN_PATTERN = Pattern.compile("^(?:(<-+|-+>)|(?:([\\w\\-\\s]+?):))(.*)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern ID_TAIL_PATTERN = Pattern.compile("^\\s*([\\S]+)(?:\\s+(.+?))?\\s*$");
  private static final Pattern DATA_COMMENT_PATTERN = Pattern.compile("^\\s*(?:\\(([^)]*)\\))?(.*)\\s*$");
  private static final Pattern DATA_FINDER = Pattern.compile(",?\\s*([a-z_\\-]+?)\\s*:\\s*\\[(.*?)]");

  private static final String NONE = "none";
  private final List<Topologies> topologies = new ArrayList<>();

  public KStreamsTopologyDescriptionParser(@Nonnull final String script) {
    final List<ParsedItem> foundItems = new ArrayList<>();
    for (final String s : script.split("\\n")) {
      String lineText = s.trim();
      if ("topology".equalsIgnoreCase(lineText)) {
        lineText = "Topologies:";
      }
      if ("sub-topologies:".equalsIgnoreCase(lineText)) {
        lineText = "";
      }

      if (lineText.isEmpty()) {
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
    final Map<String, TopologyElement> topologyElementMaps = new HashMap<>();
    for (final ParsedItem i : foundItems) {
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
          curTop.subTopologies.get(curTop.subTopologies.size() - 1).children.put(newElement.id, newElement);
        }
      }
      if (newElement != null) {
        topologyElementMaps.put(newElement.id, newElement);
      }
    }

    this.topologies.forEach(x -> x.link(topologyElementMaps));
  }

  public int size() {
    return this.topologies.stream().mapToInt(x -> x.size()).sum();
  }

  @Override
  @Nonnull
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    this.topologies.forEach(x -> builder.append(x).append("\n"));
    return builder.toString();
  }

  @Nonnull
  public Optional<TopologyElement> findForId(@Nonnull final String id) {
    TopologyElement result = null;
    for (final Topologies t : this.topologies) {
      result = t.findForId(id);
      if (result != null) {
        break;
      }
    }
    return Optional.ofNullable(result);
  }

  private static final class ParsedItem {

    final String name;
    final String tail;
    final List<String> from;
    final List<String> to;
    final String orig;

    public ParsedItem(@Nonnull final String orig, @Nonnull final String name, @Nonnull final String tail) {
      this.orig = orig;
      this.name = name;
      this.tail = tail;
      this.from = new ArrayList<>();
      this.to = new ArrayList<>();
    }
  }

  public static class TopologyElement {

    final String type;
    final String id;
    final String comment;
    final Map<String, List<String>> dataItems = new HashMap<>();
    final List<TopologyElement> to = new ArrayList<>();
    final List<TopologyElement> from = new ArrayList<>();
    final ParsedItem parsedItem;

    public TopologyElement(@Nonnull final ParsedItem parsedItem) {
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
                final List<String> foundItems = Arrays.stream(foundDataList.split(",")).map(x -> x.trim()).filter(x -> !x.isEmpty()).collect(Collectors.toList());
                this.dataItems.put(foundType, foundItems);
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

    void link(@Nonnull final Map<String, TopologyElement> map) {
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
      final StringBuilder builder = new StringBuilder();
      builder.append(String.format("%s: %s '%s data: %s", this.id, this.type, this.comment, this.dataItems));
      return builder.toString();
    }
  }

  public static final class SubTopology extends TopologyElement {

    final Map<String, TopologyElement> children = new TreeMap<>();

    SubTopology(@Nonnull final ParsedItem parsedItem) {
      super(parsedItem);
    }

    @Override
    void link(@Nonnull final Map<String, TopologyElement> map) {
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

    @Nullable
    public TopologyElement findForId(@Nonnull final String id) {
      return this.children.get(id);
    }
  }

  public static final class Topologies {

    final String comment;
    final List<SubTopology> subTopologies = new ArrayList<>();
    final List<TopologyElement> orphans = new ArrayList<>();

    public Topologies(@Nullable final String comment) {
      this.comment = comment == null ? "" : comment;
    }

    void link(@Nonnull final Map<String, TopologyElement> map) {
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

    @Nullable
    public TopologyElement findForId(@Nonnull String id) {
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
