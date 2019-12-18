/*
 * Copyright 2015-2018 Igor Maznitsa.
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

import static com.igormaznitsa.mindmap.ide.commons.Misc.FILELINK_ATTR_LINE;
import static com.igormaznitsa.mindmap.ide.commons.Misc.FILELINK_ATTR_OPEN_IN_SYSTEM;
import static com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute.doesContainOnlyStandardAttributes;


import com.igormaznitsa.ideamindmap.facet.MindMapFacet;
import com.igormaznitsa.ideamindmap.settings.MindMapApplicationSettings;
import com.igormaznitsa.ideamindmap.settings.MindMapSettingsComponent;
import com.igormaznitsa.ideamindmap.swing.AboutForm;
import com.igormaznitsa.ideamindmap.swing.ColorAttributePanel;
import com.igormaznitsa.ideamindmap.swing.ColorChooserButton;
import com.igormaznitsa.ideamindmap.swing.FileEditPanel;
import com.igormaznitsa.ideamindmap.swing.MindMapTreePanel;
import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.igormaznitsa.mindmap.ide.commons.FilePathWithLine;
import com.igormaznitsa.mindmap.ide.commons.Misc;
import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.api.ExternallyExecutedPlugin;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.plugins.misc.AboutPlugin;
import com.igormaznitsa.mindmap.plugins.misc.OptionsPlugin;
import com.igormaznitsa.mindmap.plugins.processors.ExtraFilePlugin;
import com.igormaznitsa.mindmap.plugins.processors.ExtraJumpPlugin;
import com.igormaznitsa.mindmap.plugins.processors.ExtraNotePlugin;
import com.igormaznitsa.mindmap.plugins.processors.ExtraURIPlugin;
import com.igormaznitsa.mindmap.plugins.tools.ChangeColorPlugin;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapConfigListener;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelController;
import com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractElement;
import com.igormaznitsa.mindmap.swing.panel.ui.ElementPart;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.intellij.openapi.options.ShowSettingsUtil;
import java.awt.Color;
import java.awt.Point;
import java.io.File;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

public class MindMapPanelControllerImpl implements MindMapPanelController, MindMapConfigListener, PluginContext {
  private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("/i18n/Bundle");
  private static final Logger LOGGER = LoggerFactory.getLogger(MindMapPanelControllerImpl.class);

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
  public boolean canTopicBeDeleted(@Nonnull MindMapPanel source, @Nonnull Topic topic) {
    return topic.getText().isEmpty()
        && topic.getExtras().isEmpty()
        && doesContainOnlyStandardAttributes(topic);
  }

  @Nonnull
  @Override
  public PluginContext makePluginContext(@Nonnull MindMapPanel source) {
    return this;
  }

  @Override
  public boolean isUnfoldCollapsedTopicDropTarget(@Nonnull final MindMapPanel mindMapPanel) {
    final MindMapFacet facet = this.editor.findFacet();
    return facet == null || facet.getConfiguration().isUnfoldTopicWhenItIsDropTarget();
  }

  @Override
  public boolean isTrimTopicTextBeforeSet(MindMapPanel source) {
    final MindMapFacet facet = this.editor.findFacet();
    return facet != null && facet.getConfiguration().isTrimTopicTextBeforeSet();
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
  public boolean isElementDragAllowed(@Nonnull MindMapPanel mindMapPanel) {
    return true;
  }

  @Override
  public boolean isMouseMoveProcessingAllowed(@Nonnull MindMapPanel mindMapPanel) {
    return true;
  }

  @Override
  public boolean isMouseWheelProcessingAllowed(@Nonnull MindMapPanel mindMapPanel) {
    return true;
  }

  @Override
  public boolean isMouseClickProcessingAllowed(@Nonnull MindMapPanel mindMapPanel) {
    return true;
  }

  @Override
  @Nonnull
  public MindMapPanelConfig provideConfigForMindMapPanel(@Nonnull MindMapPanel mindMapPanel) {
    return MindMapApplicationSettings.findInstance().getConfig();
  }

  @Override
  public void processPluginActivation(@Nonnull ExternallyExecutedPlugin plugin, @Nullable Topic activeTopic) {
    if (plugin instanceof ExtraNotePlugin) {
      editTextForTopic(activeTopic);
      this.editor.getMindMapPanel().requestFocus();
    } else if (plugin instanceof ExtraFilePlugin) {
      editFileLinkForTopic(activeTopic);
      this.editor.getMindMapPanel().requestFocus();
    } else if (plugin instanceof ExtraURIPlugin) {
      editLinkForTopic(activeTopic);
      this.editor.getMindMapPanel().requestFocus();
    } else if (plugin instanceof ExtraJumpPlugin) {
      editTopicLinkForTopic(activeTopic);
      this.editor.getMindMapPanel().requestFocus();
    } else if (plugin instanceof ChangeColorPlugin) {
      final Topic[] selectedTopics = this.getSelectedTopics();
      processColorDialogForTopics(this.editor.getMindMapPanel(), selectedTopics.length > 0 ? selectedTopics : new Topic[] {activeTopic});
    } else if (plugin instanceof AboutPlugin) {
      showAbout();
    } else if (plugin instanceof OptionsPlugin) {
      startOptionsEdit();
    } else {
      throw new Error("Unexpected plugin execution request: " + plugin.getClass().getName());
    }
  }

  @Nonnull
  @Override
  public MindMapPanelConfig getPanelConfig() {
    return this.editor.getMindMapPanel().getConfiguration();
  }

  @Nonnull
  @Override
  public MindMapPanel getPanel() {
    return this.editor.getMindMapPanel();
  }

  @Nullable
  @Override
  public Topic[] getSelectedTopics() {
    return this.editor.getMindMapPanel().getSelectedTopics();
  }

  @Override
  public JPopupMenu makePopUpForMindMapPanel(@Nonnull final MindMapPanel source, @Nonnull final Point point, @Nullable final AbstractElement element, @Nullable final ElementPart partUnderMouse) {
    return Utils.makePopUp(this, false, element == null ? null : element.getModel());
  }

  private void startOptionsEdit() {
    final Runnable action = new Runnable() {
      @Override
      public void run() {
        ShowSettingsUtil.getInstance().showSettingsDialog(editor.getProject(), MindMapSettingsComponent.DISPLAY_NAME);
      }
    };

    if (!IdeaUtils.submitTransactionLater(action)) {
      SwingUtilities.invokeLater(action);
    }
  }

  private void editLinkForTopic(final Topic topic) {
    final ExtraLink link = (ExtraLink) topic.getExtras().get(Extra.ExtraType.LINK);
    final MMapURI result;
    if (link == null) {
      // create new
      result = IdeaUtils.editURI(this.editor, String.format(BUNDLE.getString("MMDGraphEditor.editLinkForTopic.dlgAddURITitle"), Utils.makeShortTextVersion(topic.getText(), 16)), null);
    } else {
      // edit
      result = IdeaUtils.editURI(this.editor, String.format(BUNDLE.getString("MMDGraphEditor.editLinkForTopic.dlgEditURITitle"), Utils.makeShortTextVersion(topic.getText(), 16)), link.getValue());
    }
    if (result != null) {
      boolean changed = false;
      if (result == IdeaUtils.EMPTY_URI) {
        if (link != null) {
          changed = true;
          topic.removeExtra(Extra.ExtraType.LINK);
        }
      } else {
        final ExtraLink newLink = new ExtraLink(result);
        if (link == null || !link.equals(newLink)) {
          changed = true;
          topic.setExtra(newLink);
        }
      }

      if (changed) {
        final MindMapPanel mindMapPanel = this.editor.getMindMapPanel();
        mindMapPanel.invalidate();
        mindMapPanel.repaint();
        this.editor.onMindMapModelChanged(mindMapPanel, true);
      }
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
        } else {
          result = remove;
        }
      }
    } else {
      final MindMapTreePanel panel = new MindMapTreePanel(mindMapPanel.getModel(), link, true, null);
      if (IdeaUtils.plainMessageOkCancel(this.editor.getProject(), BUNDLE.getString("MMDGraphEditor.editTopicLinkForTopic.dlgEditSelectedTitle"), panel)) {
        final Topic selected = panel.getSelectedTopic();
        if (selected != null) {
          result = ExtraTopic.makeLinkTo(mindMapPanel.getModel(), selected);
        } else {
          result = remove;
        }
      }
    }

    if (result != null) {
      boolean changed = false;

      if (result == remove) {
        if (link != null) {
          changed = true;
          topic.removeExtra(Extra.ExtraType.TOPIC);
        }
      } else {
        if (link == null || !link.equals(result)) {
          changed = true;
          topic.setExtra(result);
        }
      }

      if (changed) {
        mindMapPanel.invalidate();
        mindMapPanel.repaint();
        this.editor.onMindMapModelChanged(mindMapPanel, true);
      }
    }
  }

  private void editFileLinkForTopic(@Nullable final Topic topic) {
    if (topic != null) {
      final ExtraFile currentFilePath = (ExtraFile) topic.getExtras().get(Extra.ExtraType.FILE);

      final FileEditPanel.DataContainer dataContainer;

      final File projectFolder = IdeaUtils.vfile2iofile(this.editor.findRootFolderForEditedFile());

      if (projectFolder == null) {
        LOGGER.error("Can't find root folder for project or module!");
        dialogProvider.msgError(null, "Can't find the project or module root folder!");
        return;
      }

      if (currentFilePath == null) {
        final FileEditPanel.DataContainer prefilled = new FileEditPanel.DataContainer(null, this.editor.getMindMapPanel().getSessionObject(Misc.SESSIONKEY_ADD_FILE_OPEN_IN_SYSTEM, Boolean.class, false));

        dataContainer = IdeaUtils.editFilePath(this.editor,
            BUNDLE.getString("MMDGraphEditor.editFileLinkForTopic.dlgTitle"),
            this.editor.getMindMapPanel().getSessionObject(Misc.SESSIONKEY_ADD_FILE_LAST_FOLDER, File.class, projectFolder),
            prefilled);
        if (dataContainer != null) {
          this.editor.getMindMapPanel().putSessionObject(Misc.SESSIONKEY_ADD_FILE_OPEN_IN_SYSTEM, dataContainer.isShowWithSystemTool());
        }
      } else {
        final MMapURI uri = currentFilePath.getValue();
        final boolean flagOpenInSystem = Boolean.parseBoolean(uri.getParameters().getProperty(FILELINK_ATTR_OPEN_IN_SYSTEM, "false")); //NOI18N
        final int line = FilePathWithLine.strToLine(uri.getParameters().getProperty(FILELINK_ATTR_LINE, null));

        final FileEditPanel.DataContainer origPath;
        origPath = new FileEditPanel.DataContainer(uri.asFile(projectFolder).getAbsolutePath() + (line < 0 ? "" : ":" + Integer.toString(line)), flagOpenInSystem);
        dataContainer = IdeaUtils.editFilePath(this.editor, BUNDLE.getString("MMDGraphEditor.editFileLinkForTopic.addPathTitle"), projectFolder, origPath);
      }

      if (dataContainer != null) {
        boolean changed = false;
        if (dataContainer.getPathWithLine().isEmptyOrOnlySpaces()) {
          changed = topic.removeExtra(Extra.ExtraType.FILE);
        } else {
          final Properties props = new Properties();
          if (dataContainer.isShowWithSystemTool()) {
            props.put(FILELINK_ATTR_OPEN_IN_SYSTEM, "true"); //NOI18N
          }
          if (dataContainer.getPathWithLine().getLine() >= 0) {
            props.put(FILELINK_ATTR_LINE, Integer.toString(dataContainer.getPathWithLine().getLine()));
          }

          final MMapURI fileUri = MMapURI.makeFromFilePath(this.editor.isMakeRelativePath() ? projectFolder : null, dataContainer.getPathWithLine().getPath(), props); //NOI18N
          final File theFile = fileUri.asFile(projectFolder);
          LOGGER.info(String.format("Path %s converted to uri: %s", dataContainer.getPathWithLine(), fileUri.asString(false, true))); //NOI18N

          if (theFile.exists()) {
            if (currentFilePath == null) {
              this.editor.getMindMapPanel().putSessionObject(Misc.SESSIONKEY_ADD_FILE_LAST_FOLDER, theFile.getParentFile());
            }

            final ExtraFile newFile = new ExtraFile(fileUri);
            if (currentFilePath == null || !currentFilePath.equals(newFile)) {
              topic.setExtra(newFile);
              changed = true;
            }
          } else {
            dialogProvider.msgError(null, String.format(BUNDLE.getString("MMDGraphEditor.editFileLinkForTopic.errorCantFindFile"), dataContainer.getPathWithLine().getPath()));
            changed = false;
          }
        }

        if (changed) {
          final MindMapPanel mindMapPanel = this.editor.getMindMapPanel();
          mindMapPanel.invalidate();
          mindMapPanel.repaint();
          this.editor.onMindMapModelChanged(mindMapPanel, true);
        }
      }
    }
  }

  private void processColorDialogForTopics(final MindMapPanel source, final Topic[] topics) {
    final Color borderColor = IdeaUtils.extractCommonColorForColorChooserButton(StandardTopicAttribute.ATTR_BORDER_COLOR.getText(), topics);
    final Color fillColor = IdeaUtils.extractCommonColorForColorChooserButton(StandardTopicAttribute.ATTR_FILL_COLOR.getText(), topics);
    final Color textColor = IdeaUtils.extractCommonColorForColorChooserButton(StandardTopicAttribute.ATTR_TEXT_COLOR.getText(), topics);

    final ColorAttributePanel panel = new ColorAttributePanel(source.getModel(), getDialogProvider(), borderColor, fillColor, textColor);
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

      source.doLayout();
      source.requestFocus();
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
    } else {
      // edit
      result = IdeaUtils
          .editText(this.editor.getProject(), String.format(BUNDLE.getString("MMDGraphEditor.editTextForTopic.dlgEditNoteTitle"), Utils.makeShortTextVersion(topic.getText(), 16)),
              note.getValue());
    }
    if (result != null) {
      boolean changed = false;

      if (result.isEmpty()) {
        if (note != null) {
          topic.removeExtra(Extra.ExtraType.NOTE);
          changed = true;
        }
      } else {
        final ExtraNote newNote = new ExtraNote(result);
        if (note == null || !note.equals(newNote)) {
          topic.setExtra(newNote);
          changed = true;
        }
      }

      if (changed) {
        this.editor.getMindMapPanel().invalidate();
        this.editor.getMindMapPanel().repaint();
        this.editor.onMindMapModelChanged(this.editor.getMindMapPanel(), true);
      }
    }
  }

  public void showAbout() {
    AboutForm.show(this.editor.getProject());
  }

  @Override
  @Nonnull
  public DialogProvider getDialogProvider(@Nonnull final MindMapPanel mindMapPanel) {
    return this.dialogProvider;
  }

  @Override
  public boolean processDropTopicToAnotherTopic(@Nonnull final MindMapPanel source, @Nonnull final Point dropPoint, @Nonnull final Topic draggedTopic, @Nullable final Topic destinationTopic) {
    boolean result = false;
    if (draggedTopic != null && destinationTopic != null && draggedTopic != destinationTopic) {
      if (destinationTopic.getExtras().containsKey(Extra.ExtraType.TOPIC)) {
        if (!getDialogProvider()
            .msgConfirmOkCancel(null, BUNDLE.getString("MMDGraphEditor.addTopicToElement.confirmTitle"), BUNDLE.getString("MMDGraphEditor.addTopicToElement.confirmMsg"))) {
          return result;
        }
      }

      final ExtraTopic topicLink = ExtraTopic.makeLinkTo(this.editor.getMindMapPanel().getModel(), draggedTopic);
      destinationTopic.setExtra(topicLink);

      result = true;
    }
    return result;

  }

  @Override
  public void onConfigurationPropertyChanged(@Nonnull final MindMapPanelConfig mindMapPanelConfig) {
    this.editor.refreshConfiguration();
  }
}
