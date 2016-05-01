/*
 * Copyright 2015 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.igormaznitsa.ideamindmap.editor;

import com.igormaznitsa.ideamindmap.facet.MindMapFacet;
import com.igormaznitsa.ideamindmap.print.IdeaMMDPrintPanelAdaptor;
import com.igormaznitsa.ideamindmap.settings.MindMapApplicationSettings;
import com.igormaznitsa.ideamindmap.settings.MindMapSettingsComponent;
import com.igormaznitsa.ideamindmap.swing.AboutForm;
import com.igormaznitsa.ideamindmap.swing.ColorAttributePanel;
import com.igormaznitsa.ideamindmap.swing.ColorChooserButton;
import com.igormaznitsa.ideamindmap.swing.FileEditPanel;
import com.igormaznitsa.ideamindmap.swing.MindMapTreePanel;
import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.MindMapPluginRegistry;
import com.igormaznitsa.mindmap.plugins.MindMapPopUpItemCustomProcessor;
import com.igormaznitsa.mindmap.plugins.MindMapPopUpItemPlugin;
import com.igormaznitsa.mindmap.plugins.PopUpSection;
import com.igormaznitsa.mindmap.plugins.focused.ExtraFilePlugin;
import com.igormaznitsa.mindmap.plugins.focused.ExtraJumpPlugin;
import com.igormaznitsa.mindmap.plugins.focused.ExtraNotePlugin;
import com.igormaznitsa.mindmap.plugins.focused.ExtraURIPlugin;
import com.igormaznitsa.mindmap.plugins.misc.AboutPlugin;
import com.igormaznitsa.mindmap.plugins.misc.OptionsPlugin;
import com.igormaznitsa.mindmap.plugins.tools.ChangeColorPlugin;
import com.igormaznitsa.mindmap.print.MMDPrintPanel;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapConfigListener;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelController;
import com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractElement;
import com.igormaznitsa.mindmap.swing.panel.ui.ElementPart;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.mindmap.swing.services.IconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconService;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import com.intellij.openapi.options.ShowSettingsUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

public class MindMapPanelControllerImpl implements MindMapPanelController, MindMapConfigListener {
  private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("/i18n/Bundle");
  private static final Logger LOGGER = LoggerFactory.getLogger(MindMapPanelControllerImpl.class);
  private static final String FILELINK_ATTR_OPEN_IN_SYSTEM = "useSystem"; //NOI18N
  private static final UIComponentFactory UI_COMPO_FACTORY = UIComponentFactoryProvider.findInstance();
  private static final ImageIconService ICON_SERVICE = ImageIconServiceProvider.findInstance();

  private final MindMapDocumentEditor editor;
  private final MindMapDialogProvider dialogProvider;

  public MindMapPanelControllerImpl(final MindMapDocumentEditor editor) {
    this.editor = editor;
    this.dialogProvider = new MindMapDialogProvider(editor.getProject());
    MindMapApplicationSettings.findInstance().getConfig().addConfigurationListener(this);
  }

  public MindMapDialogProvider getDialogProvider() {
    return this.dialogProvider;
  }

  public MindMapDocumentEditor getEditor() {
    return this.editor;
  }

  @Override
  public boolean isUnfoldCollapsedTopicDropTarget(@Nonnull final MindMapPanel mindMapPanel) {
    final MindMapFacet facet = this.editor.findFacet();
    return facet == null || facet.getConfiguration().isUnfoldTopicWhenItIsDropTarget();
  }

  @Override
  public boolean isCopyColorInfoFromParentToNewChildAllowed(@Nonnull final MindMapPanel mindMapPanel) {
    final MindMapFacet facet = this.editor.findFacet();
    return facet == null || facet.getConfiguration().isCopyColorInformationFromParent();
  }

  @Override
  public boolean isSelectionAllowed(@Nonnull final MindMapPanel mindMapPanel) {
    return true;
  }

  @Override
  public boolean isElementDragAllowed(MindMapPanel mindMapPanel) {
    return true;
  }

  @Override
  public boolean isMouseMoveProcessingAllowed(MindMapPanel mindMapPanel) {
    return true;
  }

  @Override
  public boolean isMouseWheelProcessingAllowed(MindMapPanel mindMapPanel) {
    return true;
  }

  @Override
  public boolean isMouseClickProcessingAllowed(MindMapPanel mindMapPanel) {
    return true;
  }

  @Override
  public MindMapPanelConfig provideConfigForMindMapPanel(MindMapPanel mindMapPanel) {
    return MindMapApplicationSettings.findInstance().getConfig();
  }

  private static final List<JMenuItem> putAllItemsAsSection(final JPopupMenu menu, final List<JMenuItem> items) {
    if (!items.isEmpty()) {
      if (menu.getComponentCount() > 0) {
        menu.add(UI_COMPO_FACTORY.makeMenuSeparator());
      }
      for (final JMenuItem i : items) {
        menu.add(i);
      }
    }
    return items;
  }

  @Override
  public JPopupMenu makePopUpForMindMapPanel(final MindMapPanel source, final Point point, final AbstractElement element, final ElementPart partUnderMouse) {
    final List<JMenuItem> tmpList = new ArrayList<JMenuItem>();

    final JPopupMenu result = UI_COMPO_FACTORY.makePopupMenu();
    final Topic elementTopic = element == null ? null : element.getModel();

    final Topic[] selectedTopics = source.getSelectedTopics();
    final List<MindMapPopUpItemPlugin> pluginMenuItems = MindMapPluginRegistry.getInstance().findFor(MindMapPopUpItemPlugin.class);

    final boolean isModelNotEmpty = source.getModel().getRoot() != null;

    for (final MindMapPopUpItemPlugin p : pluginMenuItems) {
      final JMenuItem menuItem = p.getPluginMenuItem(source, dialogProvider, PopUpSection.MAIN, elementTopic, selectedTopics, null);
      if (menuItem != null) {
        tmpList.add(menuItem);
      }
    }

    putAllItemsAsSection(result, tmpList).clear();

    for (final MindMapPopUpItemPlugin p : pluginMenuItems) {
      final JMenuItem menuItem;
      if (p instanceof ExtraNotePlugin) {
        menuItem = p.getPluginMenuItem(source, dialogProvider, PopUpSection.EXTRAS, elementTopic, selectedTopics, new MindMapPopUpItemCustomProcessor() {
          @Override
          public void doJobInsteadOfPlugin(@Nonnull final MindMapPopUpItemPlugin plugin, @Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nonnull final PopUpSection section, @Nullable final Topic topic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
            editTextForTopic(topic);
            panel.requestFocus();
          }
        });
      } else
      if (p instanceof ExtraFilePlugin) {
        menuItem = p.getPluginMenuItem(source, dialogProvider, PopUpSection.EXTRAS, elementTopic, selectedTopics, new MindMapPopUpItemCustomProcessor() {
          @Override
          public void doJobInsteadOfPlugin(@Nonnull final MindMapPopUpItemPlugin plugin, @Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nonnull final PopUpSection section, @Nullable final Topic topic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
            editFileLinkForTopic(topic);
            panel.requestFocus();
          }
        });
      } else if (p instanceof ExtraURIPlugin) {
        menuItem = p.getPluginMenuItem(source, dialogProvider, PopUpSection.EXTRAS, elementTopic, selectedTopics, new MindMapPopUpItemCustomProcessor() {
          @Override
          public void doJobInsteadOfPlugin(@Nonnull final MindMapPopUpItemPlugin plugin, @Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nonnull final PopUpSection section, @Nullable final Topic topic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
            editLinkForTopic(topic);
            panel.requestFocus();
          }
        });
      } else if (p instanceof ExtraJumpPlugin) {
        menuItem = p.getPluginMenuItem(source, dialogProvider, PopUpSection.EXTRAS, elementTopic, selectedTopics, new MindMapPopUpItemCustomProcessor() {
          @Override
          public void doJobInsteadOfPlugin(@Nonnull final MindMapPopUpItemPlugin plugin, @Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nonnull final PopUpSection section, @Nullable final Topic topic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
            editTopicLinkForTopic(topic);
            panel.requestFocus();
          }
        });
      } else {
        menuItem = p.getPluginMenuItem(source, dialogProvider, PopUpSection.EXTRAS, elementTopic, selectedTopics, null);
      }
      if (menuItem != null) {
        tmpList.add(menuItem);
      }
    }

    putAllItemsAsSection(result, tmpList).clear();

    for (final MindMapPopUpItemPlugin p : pluginMenuItems) {

      final JMenuItem item;
      if (p instanceof ChangeColorPlugin) {
        item = p.getPluginMenuItem(source, dialogProvider, PopUpSection.TOOLS, elementTopic, selectedTopics, new MindMapPopUpItemCustomProcessor() {
          @Override
          public void doJobInsteadOfPlugin(@Nonnull final MindMapPopUpItemPlugin plugin, @Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nonnull final PopUpSection section, @Nullable final Topic topic, @Nonnull @MustNotContainNull Topic[] selectedTopics) {
            processColorDialogForTopics(source, selectedTopics.length > 0 ? selectedTopics : new Topic[]{topic});
          }
        });
      } else {
        item = p.getPluginMenuItem(source, dialogProvider, PopUpSection.TOOLS, elementTopic, selectedTopics, null);
      }
      if (item != null) {
        tmpList.add(item);
      }
    }


    putAllItemsAsSection(result, tmpList).clear();

    final JMenu exportMenu = UI_COMPO_FACTORY.makeMenu(BUNDLE.getString("MMDGraphEditor.makePopUp.miExportMapAs"));
    exportMenu.setIcon(ICON_SERVICE.getIconForId(IconID.POPUP_EXPORT));

    for (final MindMapPopUpItemPlugin plugin : pluginMenuItems) {
      final JMenuItem exporterMenuItem = plugin.getPluginMenuItem(source, dialogProvider, PopUpSection.EXPORT, elementTopic, selectedTopics, null);
      if (exporterMenuItem != null) {
        exportMenu.add(exporterMenuItem);
      }
    }
    tmpList.add(exportMenu);

    final JMenuItem printAction = UI_COMPO_FACTORY.makeMenuItem(BUNDLE.getString("MMDGraphEditor.makePopUp.miPrintPreview"), AllIcons.PopUp.PRINTER);
    printAction.addActionListener(new ActionListener() {
      @Override public void actionPerformed(ActionEvent e) {
        final MMDPrintPanel panel = new MMDPrintPanel(new IdeaMMDPrintPanelAdaptor(getEditor().getProject()), getEditor().getMindMapPanel());
        IdeaUtils.plainMessageClose(getEditor().getProject(),"Print mind map",panel);
      }
    });
    tmpList.add(printAction);

    putAllItemsAsSection(result, tmpList).clear();

    exportMenu.setEnabled(isModelNotEmpty);

    for (final MindMapPopUpItemPlugin p : pluginMenuItems) {
      final JMenuItem item;
      if (p instanceof AboutPlugin) {
        item = p.getPluginMenuItem(source, dialogProvider, PopUpSection.MISC, elementTopic, selectedTopics, new MindMapPopUpItemCustomProcessor() {
          @Override
          public void doJobInsteadOfPlugin(@Nonnull final MindMapPopUpItemPlugin plugin, @Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nonnull final PopUpSection section, @Nullable final Topic topic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
            showAbout();
          }
        });
      } else if (p instanceof OptionsPlugin) {
        item = p.getPluginMenuItem(source, dialogProvider, PopUpSection.MISC, elementTopic, selectedTopics, new MindMapPopUpItemCustomProcessor() {
          @Override
          public void doJobInsteadOfPlugin(@Nonnull final MindMapPopUpItemPlugin plugin, @Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nonnull final PopUpSection section, @Nullable final Topic topic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
            startOptionsEdit();
          }
        });
      } else {
        item = p.getPluginMenuItem(source, dialogProvider, PopUpSection.MISC, elementTopic, selectedTopics, null);
      }

      if (item != null) {
        tmpList.add(item);
      }
    }

    putAllItemsAsSection(result, tmpList);

    return result;
  }

  private void startOptionsEdit() {
    ShowSettingsUtil.getInstance().showSettingsDialog(this.editor.getProject(),MindMapSettingsComponent.ID);
  }

  private void editLinkForTopic(final Topic topic) {
    final ExtraLink link = (ExtraLink) topic.getExtras().get(Extra.ExtraType.LINK);
    final MMapURI result;
    if (link == null) {
      // create new
      result = IdeaUtils.editURI(this.editor, String.format(BUNDLE.getString("MMDGraphEditor.editLinkForTopic.dlgAddURITitle"), Utils.makeShortTextVersion(topic.getText(), 16)), null);
    }
    else {
      // edit
      result = IdeaUtils.editURI(this.editor, String.format(BUNDLE.getString("MMDGraphEditor.editLinkForTopic.dlgEditURITitle"), Utils.makeShortTextVersion(topic.getText(), 16)), link.getValue());
    }
    if (result != null) {
      if (result == IdeaUtils.EMPTY_URI) {
        topic.removeExtra(Extra.ExtraType.LINK);
      }
      else {
        topic.setExtra(new ExtraLink(result));
      }

      final MindMapPanel mindMapPanel = this.editor.getMindMapPanel();
      mindMapPanel.invalidate();
      mindMapPanel.repaint();
      this.editor.onMindMapModelChanged(mindMapPanel);
    }
  }

  private void editTopicLinkForTopic(final Topic topic) {
    final MindMapPanel mindMapPanel = this.editor.getMindMapPanel();

    final ExtraTopic link = (ExtraTopic) topic.getExtras().get(Extra.ExtraType.TOPIC);

    ExtraTopic result = null;

    final ExtraTopic remove = new ExtraTopic("_______"); //NOI18N

    if (link == null) {
      final MindMapTreePanel treePanel = new MindMapTreePanel(mindMapPanel.getModel(), null, true, null);
      if (IdeaUtils.plainMessageOkCancel(this.editor.getProject(), BUNDLE.getString("MMDGraphEditor.editTopicLinkForTopic.dlgSelectTopicTitle"), treePanel)) {
        final Topic selected = treePanel.getSelectedTopic();
        treePanel.dispose();
        if (selected != null) {
          result = ExtraTopic.makeLinkTo(mindMapPanel.getModel(), selected);
        }
        else {
          result = remove;
        }
      }
    }
    else {
      final MindMapTreePanel panel = new MindMapTreePanel(mindMapPanel.getModel(), link, true, null);
      if (IdeaUtils.plainMessageOkCancel(this.editor.getProject(), BUNDLE.getString("MMDGraphEditor.editTopicLinkForTopic.dlgEditSelectedTitle"), panel)) {
        final Topic selected = panel.getSelectedTopic();
        if (selected != null) {
          result = ExtraTopic.makeLinkTo(mindMapPanel.getModel(), selected);
        }
        else {
          result = remove;
        }
      }
    }

    if (result != null) {
      if (result == remove) {
        topic.removeExtra(Extra.ExtraType.TOPIC);
      }
      else {
        topic.setExtra(result);
      }
      mindMapPanel.invalidate();
      mindMapPanel.repaint();
      this.editor.onMindMapModelChanged(mindMapPanel);
    }
  }

  private void editFileLinkForTopic(final Topic topic) {
    final ExtraFile file = (ExtraFile) topic.getExtras().get(Extra.ExtraType.FILE);

    final FileEditPanel.DataContainer path;

    final File projectFolder = IdeaUtils.vfile2iofile(this.editor.findRootFolderForEditedFile());

    if (projectFolder == null){
      LOGGER.error("Can't find root folder for project or module!");
      dialogProvider.msgError("Can't find the project or module root folder!");
      return;
    }

    if (file == null) {
      path = IdeaUtils.editFilePath(this.editor, BUNDLE.getString("MMDGraphEditor.editFileLinkForTopic.dlgTitle"), projectFolder, null);
    }
    else {
      final MMapURI uri = file.getValue();
      final boolean flagOpenInSystem = Boolean.parseBoolean(uri.getParameters().getProperty(FILELINK_ATTR_OPEN_IN_SYSTEM, "false")); //NOI18N

      final FileEditPanel.DataContainer origPath;
      origPath = new FileEditPanel.DataContainer(uri.asFile(projectFolder).getAbsolutePath(), flagOpenInSystem);
      path = IdeaUtils.editFilePath(this.editor, BUNDLE.getString("MMDGraphEditor.editFileLinkForTopic.addPathTitle"), projectFolder, origPath);
    }

    if (path != null) {
      final boolean changed;
      if (path.isEmpty()) {
        changed = topic.removeExtra(Extra.ExtraType.FILE);
      }
      else {
        final Properties props = new Properties();
        if (path.isShowWithSystemTool()) {
          props.put(FILELINK_ATTR_OPEN_IN_SYSTEM, "true"); //NOI18N
        }
        final MMapURI fileUri = MMapURI.makeFromFilePath(this.editor.isMakeRelativePath() ? projectFolder : null, path.getPath(), props); //NOI18N
        final File theFile = fileUri.asFile(projectFolder);
        LOGGER.info(String.format("Path %s converted to uri: %s", path.getPath(), fileUri.asString(false, true))); //NOI18N

        if (theFile.exists()) {
          topic.setExtra(new ExtraFile(fileUri));
          changed = true;
        }
        else {
          dialogProvider.msgError(String.format(BUNDLE.getString("MMDGraphEditor.editFileLinkForTopic.errorCantFindFile"), path.getPath()));
          changed = false;
        }
      }

      if (changed) {
        final MindMapPanel mindMapPanel = this.editor.getMindMapPanel();
        mindMapPanel.invalidate();
        mindMapPanel.repaint();
        this.editor.onMindMapModelChanged(mindMapPanel);
      }
    }
  }

  private void processColorDialogForTopics(final MindMapPanel source, final Topic[] topics) {
    final Color borderColor = IdeaUtils.extractCommonColorForColorChooserButton(StandardTopicAttribute.ATTR_BORDER_COLOR.getText(), topics);
    final Color fillColor = IdeaUtils.extractCommonColorForColorChooserButton(StandardTopicAttribute.ATTR_FILL_COLOR.getText(), topics);
    final Color textColor = IdeaUtils.extractCommonColorForColorChooserButton(StandardTopicAttribute.ATTR_TEXT_COLOR.getText(), topics);

    final ColorAttributePanel panel = new ColorAttributePanel(getDialogProvider(), borderColor, fillColor, textColor);
    if (IdeaUtils.plainMessageOkCancel(this.editor.getProject(), String.format(BUNDLE.getString("MMDGraphEditor.colorEditDialogTitle"), topics.length), panel)) {
      ColorAttributePanel.Result result = panel.getResult();

      if (result.getBorderColor() != ColorChooserButton.DIFF_COLORS) {
        Utils.setAttribute(StandardTopicAttribute.ATTR_BORDER_COLOR.getText(), Utils.color2html(result.getBorderColor(), false), topics);
      }

      if (result.getTextColor() != ColorChooserButton.DIFF_COLORS) {
        Utils.setAttribute(StandardTopicAttribute.ATTR_TEXT_COLOR.getText(), Utils.color2html(result.getTextColor(), false), topics);
      }

      if (result.getFillColor() != ColorChooserButton.DIFF_COLORS) {
        Utils.setAttribute(StandardTopicAttribute.ATTR_FILL_COLOR.getText(), Utils.color2html(result.getFillColor(), false), topics);
      }

      source.updateView(true);
    }
  }

  private void editTextForTopic(final Topic topic) {
    final ExtraNote note = (ExtraNote) topic.getExtras().get(Extra.ExtraType.NOTE);
    final String result;
    if (note == null) {
      // create new
      result = IdeaUtils
        .editText(this.editor.getProject(), String.format(BUNDLE.getString("MMDGraphEditor.editTextForTopic.dlfAddNoteTitle"), Utils.makeShortTextVersion(topic.getText(), 16)),
          ""); //NOI18N
    }
    else {
      // edit
      result = IdeaUtils
        .editText(this.editor.getProject(), String.format(BUNDLE.getString("MMDGraphEditor.editTextForTopic.dlgEditNoteTitle"), Utils.makeShortTextVersion(topic.getText(), 16)),
          note.getValue());
    }
    if (result != null) {
      if (result.isEmpty()) {
        topic.removeExtra(Extra.ExtraType.NOTE);
      }
      else {
        topic.setExtra(new ExtraNote(result));
      }
      this.editor.getMindMapPanel().invalidate();
      this.editor.getMindMapPanel().repaint();
      this.editor.onMindMapModelChanged(this.editor.getMindMapPanel());
    }
  }

  public void showAbout() {
    AboutForm.show(this.editor.getProject());
  }

  @Override
  public DialogProvider getDialogProvider(final MindMapPanel mindMapPanel) {
    return this.dialogProvider;
  }

  @Override
  public boolean processDropTopicToAnotherTopic(final MindMapPanel source, final Point dropPoint, final Topic draggedTopic, final Topic destinationTopic) {
    boolean result = false;
    if (draggedTopic != null && destinationTopic != null && draggedTopic != destinationTopic) {
      if (destinationTopic.getExtras().containsKey(Extra.ExtraType.TOPIC)) {
        if (!getDialogProvider()
          .msgConfirmOkCancel(BUNDLE.getString("MMDGraphEditor.addTopicToElement.confirmTitle"), BUNDLE.getString("MMDGraphEditor.addTopicToElement.confirmMsg"))) {
          return result;
        }
      }

      final ExtraTopic topicLink = ExtraTopic.makeLinkTo(this.editor.getMindMapPanel().getModel(), draggedTopic);
      destinationTopic.setExtra(topicLink);

      result = true;
    }
    return result;

  }

  @Override public void onConfigurationPropertyChanged(final MindMapPanelConfig mindMapPanelConfig) {
    this.editor.refreshConfiguration();
  }
}
