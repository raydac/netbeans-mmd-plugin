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

import static com.igormaznitsa.sciareto.ui.UiUtils.loadIcon;
import static java.lang.String.format;
import static net.sourceforge.plantuml.StringUtils.unicode;


import com.igormaznitsa.sciareto.Context;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
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
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.io.FilenameUtils;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;

public final class KsTplTextEditor extends AbstractPlUmlEditor {

  public static final Set<String> SUPPORTED_EXTENSIONS = Collections.singleton("kstpl");
  private static final String MIME = "text/kstpl";
  public static final String NEW_TEMPLATE = "Topology\n"
          + "Sub-topologies:\n"
          + "Sub-topology: 0\n"
          + "	Source:  KSTREAM-SOURCE-0000000000 (topics: [conversation-meta])\n";

  private volatile boolean modeOrtho;
  private volatile boolean modeHoriz;
  private volatile boolean modeGroupTopics;
  private volatile boolean modeGroupStores;

  private static final Icon ICON_PLANTUML = new ImageIcon(loadIcon("clipboard_plantuml16.png"));

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

  public KsTplTextEditor(@Nonnull final Context context, @Nonnull File file) throws IOException {
    super(context, file);
  }

  @Nonnull
  private Map<String, String> generateKeyMap(@Nonnull final KStreamsTopologyDescriptionParser parser) {
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

  @Nonnull
  private String makeCommentNote(@Nullable String componentId, @Nonnull final KStreamsTopologyDescriptionParser.TopologyElement element) {
    if (element.comment == null || element.comment.isEmpty()) {
      return "";
    }
    final String noteId = "nte_" + Integer.toHexString(element.id.hashCode()) + '_' + Long.toHexString(System.nanoTime());
    return format("note \"%s\" as %s%n%s%n", unicode(element.comment), noteId, componentId == null ? "" : componentId + " --> " + noteId);
  }

  @Nonnull
  private String makeOptions() {
    final StringBuilder result = new StringBuilder();

    if (this.modeHoriz) {
      result.append("left to right direction\n");
    } else {
      result.append("top to bottom direction\n");
    }

    if (this.modeOrtho) {
      result.append("skinparam linetype ortho\n");
    }

    return result.toString();
  }

  @Nonnull
  private static String makePumlMultiline(@Nonnull final String text, final int maxNonSplittedLength) {
    if (text.length() < maxNonSplittedLength) {
      return text;
    }
    return text.replace("-", "-\\n")
            .replace(" ", "\\n")
            .replace("_", "_\\n");
  }

  @Override
  protected boolean isCopyAsAscIIImageInClipboardAllowed() {
    return false;
  }

  @Override
  public boolean isSyntaxCorrect(@Nonnull final String text) {
    try {
      new KStreamsTopologyDescriptionParser(text);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  private static final Pattern GLOBAL_STORAGE_SUBTOPOLOGY = Pattern.compile(".*global.*store.*", Pattern.CASE_INSENSITIVE);

  private boolean isGlobalStorageSubTopology(@Nonnull final KStreamsTopologyDescriptionParser.SubTopology topology) {
    final String comment = topology.comment;
    if (comment == null || comment.isEmpty()) {
      return false;
    }
    return GLOBAL_STORAGE_SUBTOPOLOGY.matcher(comment).matches();
  }

  @Override
  @Nonnull
  protected String preprocessEditorText(@Nonnull final String text) {
    try {
      final KStreamsTopologyDescriptionParser parser = new KStreamsTopologyDescriptionParser(text);
      final StringBuilder builder = new StringBuilder();

      builder.append("@startuml\n")
              .append(makeOptions()).append('\n')
              .append("hide stereotype\n")
              .append("skinparam ArrowThickness 3\n")
              .append("skinparam rectangle {\n")
              .append("borderStyle<<Sub-Topologies>> dotted\n")
              .append("borderColor<<Sub-Topologies>> Gray\n")
              .append("borderThickness<<Sub-Topologies>> 2\n")
              .append("roundCorner<<Sub-Topologies>> 25\n")
              .append("shadowing<<Sub-Topologies>> false\n")
              .append("}\n")
              .append("title ").append(unicode("KStreams topology \""
              + (this.getTabTitle().getAssociatedFile() == null ? "none" : this.getTabTitle().getAssociatedFile().getName()) + '\"')).append('\n');
      final Map<String, String> keys = generateKeyMap(parser);

      for (final KStreamsTopologyDescriptionParser.Topologies t : parser.getTopologies()) {
        builder.append("rectangle \"Sub-topologies\" <<Sub-Topologies>> {\n");
        t.getSubTopologies().stream().sorted().forEach(subTopology -> {
          builder.append(format("package \"Sub-topology %s\" %s {%n", unicode(subTopology.id),
                  isGlobalStorageSubTopology(subTopology) ? "#FFDFFF" : "#DFDFFF"));
          builder.append(makeCommentNote(null, subTopology));
          subTopology.children.values().forEach(elem -> {
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

        t.orphans.forEach(elem -> {
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
                        bufferBroker.append(format("%s \"%s\" as %s%n", type, makePumlMultiline(unicode(elem), 32), keys.get(elem)));
                      }
                      break;
                      case TYPE_STORES: {
                        bufferStores.append(format("%s \"%s\" as %s%n", type, makePumlMultiline(unicode(elem), 10), keys.get(elem)));
                      }
                      break;
                      default: {
                        bufferOthers.append(format("%s \"%s\" as %s%n", type, makePumlMultiline(unicode(elem), 10), keys.get(elem)));
                      }
                      break;
                    }
                  });
                });

        if (bufferBroker.length() > 0) {
          final boolean groupTopics = this.modeGroupTopics;
          if (groupTopics) {
            builder.append("package \"Topics\" #DFFFDF {\n");
          }
          builder.append(bufferBroker.toString());
          if (groupTopics) {
            builder.append("}\n");
          }
        }

        if (bufferStores.length() > 0) {
          final boolean groupStores = this.modeGroupStores;
          if (groupStores) {
            builder.append("package \"Stores\" #FFDFDF {\n");
          }
          builder.append(bufferStores.toString());
          if (groupStores) {
            builder.append("}\n");
          }
        }

        if (bufferOthers.length() > 0) {
          builder.append("package \"Others\" #DDDDDD {\n");
          builder.append(bufferOthers.toString());
          builder.append("}\n");
        }
      }

      parser.getTopologies().forEach(topology -> {
        Stream.concat(topology.subTopologies.stream().flatMap(st -> st.children.values().stream()), topology.orphans.stream())
                .forEach(element -> {
                  final String elemKey = keys.get(element.id);
                  element.to.forEach(dst -> {
                    builder.append(format("%s -->> %s%n", elemKey, keys.get(dst.id)));
                  });
                  element.from.stream()
                          .filter(src -> src.to.stream().noneMatch(tl -> tl.id.equals(element.id)))
                          .forEach(src -> {
                            builder.append(format("%s -->> %s%n", keys.get(src.id), elemKey));
                          });
                  element.dataItems.values().stream().flatMap(Collection::stream).forEach(dataItemName -> {
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
                    builder.append(format("%s %s %s%n", elemKey, link, keys.get(dataItemName)));
                  });

                });
      });

      builder.append("@enduml\n");

//      System.out.println(builder.toString());
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

  @Override
  protected void addCustomComponents(@Nonnull final JPanel panel, @Nonnull final GridBagConstraints gbdata) {
    this.modeGroupTopics = true;
    this.modeGroupStores = true;
    this.modeHoriz = false;
    this.modeOrtho = true;

    final JButton buttonClipboardText = new JButton(ICON_PLANTUML);
    buttonClipboardText.setToolTipText("Copy formed PlantUML script to clipboard");
    buttonClipboardText.addActionListener((ActionEvent e) -> {
      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(preprocessEditorText(editor.getText())), null);
    });

    final JCheckBox checkBoxGroupTopics = new JCheckBox("Group topics  ", this.modeGroupTopics);
    checkBoxGroupTopics.setToolTipText("Group all topics on scheme together");
    checkBoxGroupTopics.addActionListener((x) -> {
      this.modeGroupTopics = checkBoxGroupTopics.isSelected();
      resetLastRendered();
      startRenderScript();
    });

    final JCheckBox checkBoxGroupStores = new JCheckBox("Group stores  ", this.modeGroupStores);
    checkBoxGroupStores.setToolTipText("Group all stores on scheme together");
    checkBoxGroupStores.addActionListener((x) -> {
      this.modeGroupStores = checkBoxGroupStores.isSelected();
      resetLastRendered();
      startRenderScript();
    });

    final JCheckBox checkBoxOrtho = new JCheckBox("Orthogonal lines  ", this.modeOrtho);
    checkBoxOrtho.setToolTipText("Orthogonal connector lines");
    checkBoxOrtho.addActionListener((x) -> {
      this.modeOrtho = checkBoxOrtho.isSelected();
      resetLastRendered();
      startRenderScript();
    });

    final JCheckBox checkBoxHorizontal = new JCheckBox("Horizontal layout  ", this.modeHoriz);
    checkBoxHorizontal.setToolTipText("Horizontal layouting of components");
    checkBoxHorizontal.addActionListener((x) -> {
      this.modeHoriz = checkBoxHorizontal.isSelected();
      resetLastRendered();
      startRenderScript();
    });

    panel.add(buttonClipboardText, gbdata);
    panel.add(checkBoxGroupTopics, gbdata);
    panel.add(checkBoxGroupStores, gbdata);
    panel.add(checkBoxOrtho, gbdata);
    panel.add(checkBoxHorizontal, gbdata);
  }

  private enum KStreamType {
    SOURCE(".*source.*", ".*kstream.*source.*", "node \"%s\" as %s"),
    TRANSFORM("", ".*kstream.*transform.*", "cloud \"%s\" as %s"),
    KEY_SELECT("", ".*kstream.*key.*select.*", "usecase \"%s\" as %s"),
    FILTER("", ".*kstream.*filter.*", "rectangle \"%s\" as %s"),
    SINK(".*sink.*", ".*kstream.*sink.*", "node \"%s\" as %s"),
    AGGREGATE("", ".*kstream.*aggregate.*", "usecase \"%s\" as %s"),
    TABLE_TOSTREAM("", ".*ktable.*to.*stream.*", "usecase \"%s\" as %s"),
    MERGE("", ".*kstrean.*merge.*", "usecase \"%s\" as %s"),
    PROCESSOR("", ".*kstream.*processor.*", "cloud \"%s\" as %s");

    private final Pattern patternType;
    private final Pattern patternId;
    private final String pumlPattern;

    KStreamType(final String patternType, final String patternId, final String pumlPattern) {
      this.patternId = Pattern.compile(patternId, Pattern.CASE_INSENSITIVE);
      this.patternType = Pattern.compile(patternType, Pattern.CASE_INSENSITIVE);
      this.pumlPattern = pumlPattern;
    }

    static KStreamType find(final KStreamsTopologyDescriptionParser.TopologyElement element) {
      for (final KStreamType t : KStreamType.values()) {
        if (t.patternType.matcher(element.type).matches()) {
          return t;
        }
        if (t.patternId.matcher(element.id).matches()) {
          return t;
        }
      }
      return PROCESSOR;
    }

    String makePuml(final KStreamsTopologyDescriptionParser.TopologyElement element, @Nonnull final String alias) {
      return format(this.pumlPattern, makePumlMultiline(unicode(element.id), 0), alias);
    }
  }

  @Override
  protected void doPutMapping(@Nonnull final AbstractTokenMakerFactory f) {
    f.putMapping(MIME, "com.igormaznitsa.sciareto.ui.editors.KStreamsTopologyTokenMaker");
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
