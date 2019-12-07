/*
 * Copyright (C) 2018 Igor Maznitsa.
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

import static java.lang.String.format;
import static net.sourceforge.plantuml.StringUtils.unicode;


import com.igormaznitsa.sciareto.Context;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.io.FilenameUtils;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;

public final class KsTplTextEditor extends AbstractPlUmlEditor {

  public static final Set<String> SUPPORTED_EXTENSIONS = Collections.singleton("kstpl");
  private static final String MIME = "text/kstpl";


  @Override
  protected boolean isPageAllowed() {
    return false;
  }

  public static final FileFilter SRC_FILE_FILTER = new FileFilter() {

    @Override
    public boolean accept(@Nonnull final File f) {
      if (f.isDirectory()) {
        return true;
      }
      return SUPPORTED_EXTENSIONS.contains(FilenameUtils.getExtension(f.getName()).toLowerCase(Locale.ENGLISH));
    }

    @Override
    @Nonnull
    public String getDescription() {
      return "KStream topology files (*.kstpl)";
    }
  };

  public KsTplTextEditor(@Nonnull final Context context, @Nullable File file) throws IOException {
    super(context, file);
  }

  @Nonnull
  private Map<String, String> generateKeyMap(KStreamsTopologyDescriptionParser parser) {
    final AtomicInteger counter = new AtomicInteger(0);
    final Map<String, String> result = new HashMap<>();
    parser.getTopologies().forEach(x -> {
      x.orphans.forEach(z -> {
        if (!result.containsKey(z.id)) {
          result.put(z.id, "__orph_" + counter.incrementAndGet());
        }
      });

      x.subTopologies.stream().flatMap(a -> a.children.values().stream())
          .forEach(e -> {
            if (!result.containsKey(e.id)) {
              result.put(e.id, "__tel_" + counter.incrementAndGet());
            }
            e.dataItems.forEach((k, v) -> {
              v.forEach(z -> {
                if (!result.containsKey(z)) {
                  result.put(z, "__dta_" + (k.hashCode() & 0x7FFFFFFF) + "_" + counter.incrementAndGet());
                }
              });
            });
          });
      x.orphans.stream().forEach(a -> {
        if (!result.containsKey(a.id)) {
          result.put(a.id, "__tel_" + counter.incrementAndGet());
        }
      });
    });
    return result;
  }

  @Override
  @Nonnull
  protected String preprocessEditorText(@Nonnull final String text) {
    try {
      final KStreamsTopologyDescriptionParser parser = new KStreamsTopologyDescriptionParser(text);
      final StringBuilder builder = new StringBuilder();
      builder.append("@startuml\ntop to bottom direction\ntitle ").append(unicode("KafkaStreams topology")).append('\n');
      final Map<String, String> keys = generateKeyMap(parser);

      for (final KStreamsTopologyDescriptionParser.Topologies t : parser.getTopologies()) {
        t.getSubTopologies().stream().sorted().forEach(subTopology -> {
          builder.append(format("package \"Sub-topology %s\" {%n", unicode(subTopology.id)));
          subTopology.children.values().forEach(elem -> {
            builder.append(KStreamType.find(elem.id).makePuml(elem, keys.get(elem.id))).append('\n');
          });
          builder.append("}\n");
        });

        t.orphans.forEach(elem -> {
          builder.append(KStreamType.find(elem.id).makePuml(elem, keys.get(elem.id))).append('\n');
        });

        final StringBuilder bufferBroker = new StringBuilder();
        final StringBuilder bufferStores = new StringBuilder();
        final StringBuilder bufferOthers = new StringBuilder();

        final int TYPE_BROKER = 0;
        final int TYPE_STORES = 1;
        final int TYPE_OTHER = 2;

        t.subTopologies.stream().flatMap(st -> st.children.values().stream())
            .flatMap(el -> el.dataItems.entrySet().stream())
            .forEach(es -> {
              final String keyLowCase = es.getKey().trim().toLowerCase(Locale.ENGLISH);
              es.getValue().forEach(elem -> {
                final String type;
                int storeType = TYPE_OTHER;
                if (keyLowCase.startsWith("topic")) {
                  storeType = TYPE_BROKER;
                  type = "queue";
                } else if (keyLowCase.startsWith("store")) {
                  storeType = TYPE_STORES;
                  type = "database";
                } else {
                  type = "file";
                }

                switch (storeType) {
                  case TYPE_BROKER: {
                    bufferBroker.append(format("%s \"%s\" as %s%n", type, unicode(elem), keys.get(elem)));
                  }
                  break;
                  case TYPE_STORES: {
                    bufferStores.append(format("%s \"%s\" as %s%n", type, unicode(elem), keys.get(elem)));
                  }
                  break;
                  default: {
                    bufferOthers.append(format("%s \"%s\" as %s%n", type, unicode(elem), keys.get(elem)));
                  }
                  break;
                }
              });
            });

        if (bufferBroker.length() > 0) {
          builder.append("package \"Kafka broker\" {\n");
          builder.append(bufferBroker.toString());
          builder.append("}\n");
        }

        if (bufferStores.length() > 0) {
          builder.append("package \"Stores\" {\n");
          builder.append(bufferStores.toString());
          builder.append("}\n");
        }

        if (bufferOthers.length() > 0) {
          builder.append("package \"Others\" {\n");
          builder.append(bufferOthers.toString());
          builder.append("}\n");
        }
      }

      parser.getTopologies().stream().forEach(topology -> {
        Stream.concat(topology.subTopologies.stream().flatMap(st -> st.children.values().stream()), topology.orphans.stream())
            .forEach(element -> {
              final String elemKey = keys.get(element.id);
              element.to.forEach(dst -> {
                builder.append(format("%s --> %s%n", elemKey, keys.get(dst.id)));
              });
              element.from.stream()
                  .filter(src -> src.to.stream().noneMatch(tl -> tl.id.equals(element.id)))
                  .forEach(src -> {
                    builder.append(format("%s --> %s%n", keys.get(src.id), elemKey));
                  });
              element.dataItems.values().stream().flatMap(di -> di.stream()).forEach(dataItemName -> {
                final String link;
                switch (KStreamType.find(element.id)) {
                  case SOURCE:
                    link = "<==";
                    break;
                  case SINK:
                    link = "==>";
                    break;
                  default:
                    link = "==";
                    break;
                }
                builder.append(format("%s %s %s%n", elemKey, link, keys.get(dataItemName)));
              });

            });
      });

      builder.append("@enduml\n");

      System.out.println(builder.toString());

      return builder.toString();
    } catch (Exception ex) {
      final String errorText = ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage();
      return "@startuml\n"
          + "skinparam shadowing false\n"
          + "scale 3\n"
          + "rectangle \"<&circle-x><b>" + unicode(errorText) + "</b>\" #FF6666\n"
          + "@enduml";
    }
  }

  private enum KStreamType {
    SOURCE(".*kstream.*source.*", "rectangle \"%s\" as %s"),
    TRANSFORM(".*kstream.*transform.*", "rectangle \"%s\" as %s"),
    KEY_SELECT(".*kstream.*key.*select.*", "rectangle \"%s\" as %s"),
    FILTER(".*kstream.*filter.*", "rectangle \"%s\" as %s"),
    SINK(".*kstream.*sink.*", "rectangle \"%s\" as %s"),
    AGGREGATE(".*kstream.*aggregate.*", "rectangle \"%s\" as %s"),
    TABLE_TOSTREAM(".*ktable.*to.*stream.*", "rectangle \"%s\" as %s"),
    MERGE(".*kstrean.*merge.*", "rectangle \"%s\" as %s"),
    PROCESSOR(".*kstream.*processor.*", "rectangle \"%s\" as %s");

    private final Pattern pattern;
    private final String pumlPattern;

    KStreamType(final String pattern, final String pumlPattern) {
      this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
      this.pumlPattern = pumlPattern;
    }

    static KStreamType find(final String text) {
      for (final KStreamType t : KStreamType.values()) {
        if (t.pattern.matcher(text).matches()) {
          return t;
        }
      }
      return PROCESSOR;
    }

    String makePuml(final KStreamsTopologyDescriptionParser.TopologyElement element, @Nonnull final String alias) {
      return String.format(this.pumlPattern, unicode(element.id), alias);
    }
  }

  @Override
  protected void doPutMapping(@Nonnull final AbstractTokenMakerFactory f) {
    f.putMapping(MIME, "com.igormaznitsa.sciareto.ui.editors.KsTplTokenMaker");
  }

  @Override
  @Nonnull
  protected String getSyntaxEditingStyle() {
    return MIME;
  }

  @Nonnull
  @Override
  public String getDefaultExtension() {
    return "kstpl";
  }

  @Override
  @Nonnull
  public FileFilter getFileFilter() {
    return SRC_FILE_FILTER;
  }
}
