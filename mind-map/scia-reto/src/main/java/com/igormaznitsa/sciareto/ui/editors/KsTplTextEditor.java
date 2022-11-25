/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.igormaznitsa.sciareto.ui.editors;

import static com.igormaznitsa.sciareto.ui.UiUtils.findTextBundle;
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
import java.util.Properties;
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
  public static final String NEW_TEMPLATE = "Topology\n"
      + "Sub-topologies:\n"
      + "Sub-topology: 0\n"
      + "	Source:  KSTREAM-SOURCE-0000000000 (topics: [conversation-meta])\n";
  public final FileFilter sourceFileFilter = makeFileFilter();
  private static final String MIME = "text/kstpl";
  private static final String PROPERTY_ORTHOGONAL = "edge.ortho";
  private static final String PROPERTY_TOPICS_GROUP = "group.topics";
  private static final String PROPERTY_STORE_GROUP = "group.stores";
  private static final Icon ICON_PLANTUML = new ImageIcon(loadIcon("clipboard_plantuml16.png"));
  private static final String PROPERTY_LAYOUT_HORIZ = "layout.horiz";
  private static final Pattern GLOBAL_STORAGE_SUBTOPOLOGY =
      Pattern.compile(".*global.*store.*", Pattern.CASE_INSENSITIVE);
  private volatile boolean modeOrtho;
  private volatile boolean modeHoriz;
  private volatile boolean modeGroupTopics;
  private volatile boolean modeGroupStores;
  private JCheckBox checkBoxGroupTopics;
  private JCheckBox checkBoxGroupStores;
  private JCheckBox checkBoxOrtho;
  private JCheckBox checkBoxHorizontal;

  public KsTplTextEditor(@Nonnull final Context context, @Nonnull File file) throws IOException {
    super(context, file);
  }

  @Nonnull
  private static String makePumlMultiline(@Nonnull final String text,
                                          final int maxNonSplittedLength) {
    if (text.length() < maxNonSplittedLength) {
      return text;
    }
    return text.replace("-", "-\\n")
        .replace(" ", "\\n")
        .replace("_", "_\\n");
  }

  public static FileFilter makeFileFilter() {
    return new FileFilter() {

      @Override
      public boolean accept(@Nonnull final File f) {
        if (f.isDirectory()) {
          return true;
        }
        return SUPPORTED_EXTENSIONS
            .contains(FilenameUtils.getExtension(f.getName()).toLowerCase(Locale.ENGLISH));
      }

      @Override
      @Nonnull
      public String getDescription() {
        return findTextBundle().getString("editorAbstractPlUml.fileFilter.kstpl.description");
      }
    };
  }

  @Override
  protected boolean isPageAllowed() {
    return false;
  }

  @Override
  protected void onEditorInitialSetText(@Nonnull final String editorText) {
    final KStreamsTopologyDescriptionParser parser =
        new KStreamsTopologyDescriptionParser(editorText);
    this.modeGroupTopics = Boolean.parseBoolean(parser.getProperty(PROPERTY_TOPICS_GROUP, "true"));
    this.modeGroupStores = Boolean.parseBoolean(parser.getProperty(PROPERTY_STORE_GROUP, "true"));
    this.modeHoriz = Boolean.parseBoolean(parser.getProperty(PROPERTY_LAYOUT_HORIZ, "false"));
    this.modeOrtho = Boolean.parseBoolean(parser.getProperty(PROPERTY_ORTHOGONAL, "true"));
    this.checkBoxGroupStores.setSelected(this.modeGroupStores);
    this.checkBoxGroupTopics.setSelected(this.modeGroupTopics);
    this.checkBoxHorizontal.setSelected(this.modeHoriz);
    this.checkBoxOrtho.setSelected(this.modeOrtho);
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
  private Map<String, String> generateKeyMap(
      @Nonnull final KStreamsTopologyDescriptionParser parser) {
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
            e.dataItems.forEach((k, v) -> v.forEach(z -> {
              if (!result.containsKey(z)) {
                result.put(z,
                    "__dta_" + (k.hashCode() & 0x7FFFFFFF) + "_" + counter.incrementAndGet());
              }
            }));
          });
      x.orphans.forEach(a -> {
        if (!result.containsKey(a.id)) {
          result.put(a.id, "__tel_" + counter.incrementAndGet());
        }
      });
    });
    return result;
  }

  @Override
  protected boolean isCopyAsAscIIImageInClipboardAllowed() {
    return false;
  }

  @Nonnull
  private String makeCommentNote(@Nullable String componentId, @Nonnull
  final KStreamsTopologyDescriptionParser.TopologyElement element) {
    if (element.comment == null || element.comment.isEmpty()) {
      return "";
    }
    final String noteId = "nte_" + Integer.toHexString(element.id.hashCode()) + '_' +
        Long.toHexString(System.nanoTime());
    return format("note \"%s\" as %s%n%s%n", unicode(element.comment), noteId,
        componentId == null ? "" : componentId + " --> " + noteId);
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

  @Override
  @Nonnull
  protected String getEditorTextForSave() {
    final String textInEditor = super.getEditorTextForSave();
    final Properties properties = new Properties();
    properties.setProperty(PROPERTY_LAYOUT_HORIZ, Boolean.toString(this.modeHoriz));
    properties.setProperty(PROPERTY_ORTHOGONAL, Boolean.toString(this.modeOrtho));
    properties.setProperty(PROPERTY_STORE_GROUP, Boolean.toString(this.modeGroupStores));
    properties.setProperty(PROPERTY_TOPICS_GROUP, Boolean.toString(this.modeGroupTopics));
    return KStreamsTopologyDescriptionParser.replaceProperties(textInEditor, properties);
  }

  private boolean isGlobalStorageSubTopology(
      @Nonnull final KStreamsTopologyDescriptionParser.SubTopology topology) {
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
              + (this.getTabTitle().getAssociatedFile() == null ? "none" :
              this.getTabTitle().getAssociatedFile().getName()) + '\"')).append('\n');
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
                    bufferBroker.append(
                        format("%s \"%s\" as %s%n", type, makePumlMultiline(unicode(elem), 32),
                            keys.get(elem)));
                  }
                  break;
                  case TYPE_STORES: {
                    bufferStores.append(
                        format("%s \"%s\" as %s%n", type, makePumlMultiline(unicode(elem), 10),
                            keys.get(elem)));
                  }
                  break;
                  default: {
                    bufferOthers.append(
                        format("%s \"%s\" as %s%n", type, makePumlMultiline(unicode(elem), 10),
                            keys.get(elem)));
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
          builder.append(bufferBroker);
          if (groupTopics) {
            builder.append("}\n");
          }
        }

        if (bufferStores.length() > 0) {
          final boolean groupStores = this.modeGroupStores;
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

      parser.getTopologies().forEach(topology -> Stream.concat(topology.subTopologies.stream().flatMap(st -> st.children.values().stream()),
              topology.orphans.stream())
          .forEach(element -> {
            final String elemKey = keys.get(element.id);
            element.to.forEach(dst -> builder.append(format("%s -->> %s%n", elemKey, keys.get(dst.id))));
            element.from.stream()
                .filter(src -> src.to.stream().noneMatch(tl -> tl.id.equals(element.id)))
                .forEach(src -> builder.append(format("%s -->> %s%n", keys.get(src.id), elemKey)));
            element.dataItems.values().stream().flatMap(Collection::stream)
                .forEach(dataItemName -> {
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

          }));

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

  private void onConfigCheckboxChange(@Nonnull final JCheckBox source) {
    switch (source.getName()) {
      case PROPERTY_LAYOUT_HORIZ: {
        this.modeHoriz = source.isSelected();
      }
      break;
      case PROPERTY_ORTHOGONAL: {
        this.modeOrtho = source.isSelected();
      }
      break;
      case PROPERTY_STORE_GROUP: {
        this.modeGroupStores = source.isSelected();
      }
      break;
      case PROPERTY_TOPICS_GROUP: {
        this.modeGroupTopics = source.isSelected();
      }
      break;
    }
    this.getTabTitle().setChanged(true);
    resetLastRendered();
    startRenderScript();
  }

  @Override
  protected void addCustomComponents(@Nonnull final JPanel panel,
                                     @Nonnull final GridBagConstraints gbdata) {
    final JButton buttonClipboardText = new JButton(ICON_PLANTUML);
    buttonClipboardText.setName("BUTTON.PLANTUML");
    buttonClipboardText.setToolTipText(
        this.bundle.getString("editorKsTpl.buttonClipboardTtext.tooltip"));
    buttonClipboardText.addActionListener((ActionEvent e) -> Toolkit.getDefaultToolkit().getSystemClipboard()
        .setContents(new StringSelection(preprocessEditorText(editor.getText())), null));

    checkBoxGroupTopics =
        new JCheckBox(this.bundle.getString("editorKsTpl.checkBoxGroupTopics.title"),
            this.modeGroupTopics);
    checkBoxGroupTopics.setName(PROPERTY_TOPICS_GROUP);
    checkBoxGroupTopics.setToolTipText(
        this.bundle.getString("editorKsTpl.checkBoxGroupTopics.tooltip"));
    checkBoxGroupTopics.addActionListener((x) -> this.onConfigCheckboxChange(checkBoxGroupTopics));

    checkBoxGroupStores =
        new JCheckBox(this.bundle.getString("editorKsTpl.checkBoxGroupStores.title"),
            this.modeGroupStores);
    checkBoxGroupStores.setName(PROPERTY_STORE_GROUP);
    checkBoxGroupStores.setToolTipText(
        this.bundle.getString("editorKsTpl.checkBoxGroupStores.tooltip"));
    checkBoxGroupStores.addActionListener((x) -> this.onConfigCheckboxChange(checkBoxGroupStores));

    checkBoxOrtho =
        new JCheckBox(this.bundle.getString("editorKsTpl.checkBoxOrtho.title"), this.modeOrtho);
    checkBoxOrtho.setName(PROPERTY_ORTHOGONAL);
    checkBoxOrtho.setToolTipText(this.bundle.getString("editorKsTpl.checkBoxOrtho.tooltip"));
    checkBoxOrtho.addActionListener((x) -> this.onConfigCheckboxChange(checkBoxOrtho));

    checkBoxHorizontal =
        new JCheckBox(this.bundle.getString("editorKsTpl.checkBoxHorizontal.title"),
            this.modeHoriz);
    checkBoxHorizontal.setName(PROPERTY_LAYOUT_HORIZ);
    checkBoxHorizontal.setToolTipText(
        this.bundle.getString("editorKsTpl.checkBoxHorizontal.tooltip"));
    checkBoxHorizontal.addActionListener((x) -> this.onConfigCheckboxChange(checkBoxHorizontal));

    panel.add(buttonClipboardText, gbdata);
    panel.add(checkBoxGroupTopics, gbdata);
    panel.add(checkBoxGroupStores, gbdata);
    panel.add(checkBoxOrtho, gbdata);
    panel.add(checkBoxHorizontal, gbdata);
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
    return sourceFileFilter;
  }

  private enum PartitioningFlag {
    INHERITED,
    MAY_ON,
    ON
  }

  private enum KStreamType {
    SOURCE(".*source.*", ".*kstream.*source.*", "node \"%s\" as %s %s", PartitioningFlag.INHERITED),
    PRINT("", ".*kstream.*printer.*", "file \"%s\" as %s", PartitioningFlag.INHERITED),
    SINK(".*sink.*", ".*kstream.*sink.*", "node \"%s\" as %s %s", PartitioningFlag.INHERITED),
    PEEK("", ".*kstream.*peek.*", "file \"%s\" as %s %s", PartitioningFlag.INHERITED),
    FOREACH("", ".*kstream.*foreach.*", "node \"%s\" as %s %s", PartitioningFlag.INHERITED),

    PROCESSOR("", ".*kstream.*processor.*", "node \"%s\" as %s %s", PartitioningFlag.INHERITED),
    TRANSFORMVALUES("", ".*kstream.*transformvalues.*", "cloud \"%s\" as %s %s",
        PartitioningFlag.INHERITED),
    TRANSFORM("", ".*kstream.*transform.*", "cloud \"%s\" as %s %s", PartitioningFlag.ON),

    KEY_SELECT("", ".*kstream.*key.*select.*", "rectangle \"%s\" as %s %s", PartitioningFlag.ON),

    FLATMAPVALUES("", ".*kstream.*flatmapvalues.*", "rectangle \"%s\" as %s %s",
        PartitioningFlag.INHERITED),
    FLATMAP("", ".*kstream.*flatmap.*", "rectangle \"%s\" as %s %s", PartitioningFlag.ON),
    FILTER("", ".*kstream.*filter.*", "rectangle \"%s\" as %s %s", PartitioningFlag.INHERITED),

    MAPVALUES("", ".*kstream.*mapvalues.*", "rectangle \"%s\" as %s %s",
        PartitioningFlag.INHERITED),
    MAP("", ".*kstream.*map.*", "rectangle \"%s\" as %s %s", PartitioningFlag.ON),
    MERGE("", ".*kstream.*merge.*", "usecase \"%s\" as %s %s", PartitioningFlag.MAY_ON),

    WINDOWED("", ".*kstream.*windowed.*", "frame \"%s\" as %s %s", PartitioningFlag.INHERITED),
    JOINTHIS("", ".*kstream.*jointhis.*", "usecase \"%s\" as %s %s", PartitioningFlag.MAY_ON),
    JOINOTHER("", ".*kstream.*joinother.*", "usecase \"%s\" as %s %s", PartitioningFlag.MAY_ON),
    JOIN("", ".*kstream.*join.*", "usecase \"%s\" as %s %s", PartitioningFlag.MAY_ON),

    OUTERTHIS("", ".*kstream.*outerthis.*", "usecase \"%s\" as %s %s", PartitioningFlag.MAY_ON),
    OUTEROTHER("", ".*kstream.*outerother.*", "usecase \"%s\" as %s %s", PartitioningFlag.MAY_ON),

    BRANCHCHILD("", ".*kstream.*branchchild.*", "usecase \"%s\" as %s %s",
        PartitioningFlag.INHERITED),
    BRANCH("", ".*kstream.*branch.*", "usecase \"%s\" as %s %s", PartitioningFlag.INHERITED);

    private final Pattern patternType;
    private final Pattern patternId;
    private final String pumlPattern;
    private final PartitioningFlag partitioning;

    KStreamType(final String patternType,
                final String patternId,
                final String pumlPattern,
                final PartitioningFlag partitioning
    ) {
      this.partitioning = partitioning;
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
      return FILTER;
    }

    PartitioningFlag getPartitioning() {
      return this.partitioning;
    }

    String makePuml(final KStreamsTopologyDescriptionParser.TopologyElement element,
                    @Nonnull final String alias) {
      final String color;
      switch (this.partitioning) {
        case ON:
          color = "#FFDEDE";
          break;
        case MAY_ON:
          color = "#FFFFBB";
          break;
        case INHERITED:
          color = "#BBFFBB";
          break;
        default:
          color = "#BBBBFF";
          break;
      }
      return format(this.pumlPattern, makePumlMultiline(unicode(element.id), 0), alias, color);
    }
  }
}
