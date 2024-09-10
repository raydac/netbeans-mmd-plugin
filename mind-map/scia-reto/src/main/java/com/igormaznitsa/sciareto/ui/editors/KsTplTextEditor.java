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

import static com.igormaznitsa.sciareto.ui.UiUtils.loadIcon;
import static java.util.Objects.requireNonNull;

import com.igormaznitsa.ksrender.KStreamsTopologyDescriptionParser;
import com.igormaznitsa.ksrender.PlantUmlFlag;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.ui.SrI18n;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import javax.annotation.Nonnull;
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
  private static final String MIME = "text/kstpl";
  private static final String PROPERTY_ORTHOGONAL = "edge.ortho";
  private static final String PROPERTY_TOPICS_GROUP = "group.topics";
  private static final String PROPERTY_STORE_GROUP = "group.stores";
  private static final Icon ICON_PLANTUML =
      new ImageIcon(requireNonNull(loadIcon("clipboard_plantuml16.png")));
  private static final String PROPERTY_LAYOUT_HORIZ = "layout.horiz";
  public final FileFilter sourceFileFilter = makeFileFilter();
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
        return SrI18n.getInstance().findBundle()
            .getString("editorAbstractPlUml.fileFilter.kstpl.description");
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

  @Override
  @Nonnull
  protected String preprocessEditorText(@Nonnull final String text) {
    final Set<PlantUmlFlag> flags = new HashSet<>();
    if (this.modeOrtho) {
      flags.add(PlantUmlFlag.ORTHOGONAL);
    }
    if (this.modeHoriz) {
      flags.add(PlantUmlFlag.HORIZONTAL);
    }
    if (this.modeGroupStores) {
      flags.add(PlantUmlFlag.GROUP_STORES);
    }
    if (this.modeGroupTopics) {
      flags.add(PlantUmlFlag.GROUP_TOPICS);
    }

    final String title = "KStreams topology \""
        + (this.getTabTitle().getAssociatedFile() == null ? "none" :
        this.getTabTitle().getAssociatedFile().getName()) + '\"';
    try {
      return new KStreamsTopologyDescriptionParser(text).asPlantUml(title, flags);
    } catch (Exception ex) {
      return ex.getMessage();
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
    buttonClipboardText.addActionListener(
        (ActionEvent e) -> Toolkit.getDefaultToolkit().getSystemClipboard()
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

}
