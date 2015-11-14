package com.igormaznitsa.ideamindmap.editor;

import com.igormaznitsa.ideamindmap.facet.MindMapFacet;
import com.igormaznitsa.ideamindmap.settings.MindMapApplicationSettings;
import com.igormaznitsa.ideamindmap.settings.MindMapSettingsComponent;
import com.igormaznitsa.ideamindmap.swing.AboutForm;
import com.igormaznitsa.ideamindmap.swing.ColorAttributePanel;
import com.igormaznitsa.ideamindmap.swing.ColorChooserButton;
import com.igormaznitsa.ideamindmap.swing.FileEditPanel;
import com.igormaznitsa.ideamindmap.swing.MindMapTreePanel;
import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.igormaznitsa.mindmap.exporters.AbstractMindMapExporter;
import com.igormaznitsa.mindmap.exporters.Exporters;
import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapConfigListener;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelController;
import com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractElement;
import com.igormaznitsa.mindmap.swing.panel.ui.ElementPart;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import org.jetbrains.annotations.NotNull;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Properties;
import java.util.ResourceBundle;

public class MindMapPanelControllerImpl implements MindMapPanelController, MindMapConfigListener {
  private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("/i18n/Bundle");
  private static final Logger LOGGER = Logger.getInstance(MindMapPanelControllerImpl.class);
  private static final String FILELINK_ATTR_OPEN_IN_SYSTEM = "useSystem"; //NOI18N

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
  public boolean isUnfoldCollapsedTopicDropTarget(@NotNull final MindMapPanel mindMapPanel) {
    final MindMapFacet facet = this.editor.findFacet();
    return facet == null ? true : facet.getConfiguration().isUnfoldTopicWhenItIsDropTarget();
  }

  @Override
  public boolean isCopyColorInfoFromParentToNewChildAllowed(@NotNull final MindMapPanel mindMapPanel) {
    final MindMapFacet facet = this.editor.findFacet();
    return facet == null ? true : facet.getConfiguration().isCopyColorInformationFromParent();
  }

  @Override
  public boolean isSelectionAllowed(@NotNull final MindMapPanel mindMapPanel) {
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

  @Override
  public JPopupMenu makePopUpForMindMapPanel(final MindMapPanel source, final Point point, final AbstractElement element, final ElementPart partUnderMouse) {
    final JBPopupMenu result = new JBPopupMenu();

    if (element != null) {
      final JBMenuItem editText = new JBMenuItem(BUNDLE.getString("MMDGraphEditor.makePopUp.miEditText"), AllIcons.PopUp.EDITTEXT);
      editText.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          editor.getMindMapPanel().startEdit(element);
        }
      });

      result.add(editText);

      final JBMenuItem addChild = new JBMenuItem(BUNDLE.getString("MMDGraphEditor.makePopUp.miAddChild"), AllIcons.PopUp.ADD);
      addChild.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          editor.getMindMapPanel().makeNewChildAndStartEdit(element.getModel(), null);
        }
      });

      result.add(addChild);
    }

    if (element != null || this.editor.getMindMapPanel().hasSelectedTopics()) {
      final JBMenuItem deleteItem = new JBMenuItem(this.editor.getMindMapPanel().hasSelectedTopics() ?
        BUNDLE.getString("MMDGraphEditor.makePopUp.miRemoveSelectedTopics") :
        BUNDLE.getString("MMDGraphEditor.makePopUp.miRemoveTheTopic"), AllIcons.PopUp.DELETE);
      deleteItem.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          if (editor.getMindMapPanel().hasSelectedTopics()) {
            editor.getMindMapPanel().deleteSelectedTopics();
          }
          else {
            editor.getMindMapPanel().deleteTopics(element.getModel());
          }
        }
      });

      result.add(deleteItem);
    }

    if (element != null || this.editor.getMindMapPanel().hasOnlyTopicSelected()) {
      final Topic theTopic =
        this.editor.getMindMapPanel().getFirstSelected() == null ? (element != null ? element.getModel() : null) : this.editor.getMindMapPanel().getFirstSelected();
      if (theTopic != null && theTopic.getParent() != null) {
        final JBMenuItem cloneItem = new JBMenuItem(this.editor.getMindMapPanel().hasSelectedTopics() ?
          BUNDLE.getString("MMDGraphEditor.makePopUp.miCloneSelectedTopic") :
          BUNDLE.getString("MMDGraphEditor.makePopUp.miCloneTheTopic"), AllIcons.PopUp.CLONE);
        cloneItem.addActionListener(new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            editor.getMindMapPanel().cloneTopic(theTopic);
          }
        });

        result.add(cloneItem);
      }
    }

    if (element != null) {
      if (result.getComponentCount() > 0) {
        result.add(new JSeparator());
      }

      final Topic topic = element.getModel();

      final JBMenuItem editText = new JBMenuItem(
        topic.getExtras().containsKey(Extra.ExtraType.NOTE) ? BUNDLE.getString("MMDGraphEditor.makePopUp.miEditNote") : BUNDLE.getString("MMDGraphEditor.makePopUp.miAddNote"),
        AllIcons.PopUp.NOTE);
      editText.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
          editTextForTopic(topic);
          editor.getMindMapPanel().requestFocus();
        }
      });

      result.add(editText);

      final JBMenuItem editLink = new JBMenuItem(
        topic.getExtras().containsKey(Extra.ExtraType.LINK) ? BUNDLE.getString("MMDGraphEditor.makePopUp.miEditURI") : BUNDLE.getString("MMDGraphEditor.makePopUp.miAddURI"),
        AllIcons.PopUp.URL);
      editLink.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          editLinkForTopic(topic);
          editor.getMindMapPanel().requestFocus();
        }
      });

      result.add(editLink);

      final JBMenuItem editTopicLink = new JBMenuItem(topic.getExtras().containsKey(Extra.ExtraType.TOPIC) ?
        BUNDLE.getString("MMDGraphEditor.makePopUp.miEditTransition") :
        BUNDLE.getString("MMDGraphEditor.makePopUp.miAddTransition"), AllIcons.PopUp.TOPIC);
      editTopicLink.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          editTopicLinkForTopic(topic);
          editor.getMindMapPanel().requestFocus();
        }
      });

      result.add(editTopicLink);

      final JBMenuItem editFileLink = new JBMenuItem(
        topic.getExtras().containsKey(Extra.ExtraType.FILE) ? BUNDLE.getString("MMDGraphEditor.makePopUp.miEditFile") : BUNDLE.getString("MMDGraphEditor.makePopUp.miAddFile"),
        AllIcons.PopUp.FILE);
      editFileLink.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          editFileLinkForTopic(topic);
          editor.getMindMapPanel().requestFocus();
        }
      });

      result.add(editFileLink);
    }

    if (element != null || source.hasSelectedTopics()) {
      if (result.getComponentCount() > 0) {
        result.add(new JSeparator());
      }

      final Topic[] topics;
      final String name;
      if (source.hasSelectedTopics()) {
        topics = source.getSelectedTopics();
        name = String.format(BUNDLE.getString("MMDGraphEditor.makePopUp.miColorsForSelected"), topics.length);
      }
      else {
        topics = new Topic[] { element.getModel() };
        name = BUNDLE.getString("MMDGraphEditor.makePopUp.miColorsForTopic");
      }

      final JBMenuItem colors = new JBMenuItem(name, AllIcons.PopUp.COLORS);
      colors.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          processColorDialogForTopics(source, topics);
        }
      });

      result.add(colors);
    }

    if (result.getComponentCount() > 0) {
      result.add(new JSeparator());
    }

    final JBMenuItem expandAll = new JBMenuItem(BUNDLE.getString("MMDGraphEditor.makePopUp.miExpandAll"), AllIcons.PopUp.EXPANDALL);
    expandAll.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        editor.getMindMapPanel().collapseOrExpandAll(false);
      }

    });

    final JBMenuItem collapseAll = new JBMenuItem(BUNDLE.getString("MMDGraphEditor.makePopUp.miCollapseAll"), AllIcons.PopUp.COLLAPSEALL);
    collapseAll.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        editor.getMindMapPanel().collapseOrExpandAll(true);
      }

    });

    final JCheckBoxMenuItem showJumps = new JCheckBoxMenuItem(BUNDLE.getString("MMDGraphEditor.makePopUp.miShowJumps"), AllIcons.PopUp.SHOWJUMPS, source.isShowJumps());
    showJumps.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        editor.getMindMapPanel().setShowJumps(showJumps.isSelected());
      }
    });

    result.add(showJumps);
    result.add(expandAll);
    result.add(collapseAll);

    if (result.getComponentCount() > 0) {
      result.add(new JSeparator());
    }
    final JMenu exportMenu = new JMenu(BUNDLE.getString("MMDGraphEditor.makePopUp.miExportMapAs"));
    exportMenu.setIcon(AllIcons.PopUp.EXPORT);
    for (final Exporters e : Exporters.values()) {
      final AbstractMindMapExporter exp = e.getExporter();
      final JBMenuItem item = new JBMenuItem(exp.getName());
      item.setToolTipText(exp.getReference());
      item.setIcon(exp.getIcon());
      item.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
          try {
            exp.doExport(editor.getMindMapPanel(), null);
          }
          catch (Exception ex) {
            LOGGER.error("Error during map export", ex); //NOI18N
            getDialogProvider().msgError(BUNDLE.getString("MMDGraphEditor.makePopUp.errMsgCantExport"));
          }
        }
      });
      exportMenu.add(item);
    }
    result.add(exportMenu);

    result.add(new JSeparator());

    JBMenuItem optionsMenu = new JBMenuItem(BUNDLE.getString("MMDGraphEditor.makePopUp.miOptions"), AllIcons.PopUp.OPTIONS);
    optionsMenu.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        startOptionsEdit();
      }
    });

    result.add(optionsMenu);

    JBMenuItem infoMenu = new JBMenuItem(BUNDLE.getString("MMDGraphEditor.makePopUp.miAbout"), AllIcons.PopUp.INFO);
    infoMenu.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        showAbout();
      }
    });

    result.add(infoMenu);

    return result;
  }

  private void startOptionsEdit() {
    ShowSettingsUtil.getInstance().showSettingsDialog(this.editor.getProject(),MindMapSettingsComponent.class);
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
      if (uri.isAbsolute()) {
        origPath = new FileEditPanel.DataContainer(uri.asFile(projectFolder).getAbsolutePath(), flagOpenInSystem);
      }
      else {
        if (projectFolder == null) {
          dialogProvider.msgWarn(BUNDLE.getString("MMDGraphEditor.editFileLinkForTopic.warnText"));
          origPath = new FileEditPanel.DataContainer(uri.asFile(projectFolder).getPath(), flagOpenInSystem);
        }
        else {
          origPath = new FileEditPanel.DataContainer(uri.asFile(projectFolder).getAbsolutePath(), flagOpenInSystem);
        }
      }
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
