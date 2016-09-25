/*
 * Copyright 2016 Igor Maznitsa.
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
package com.igormaznitsa.sciareto.ui.editors;

import static com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute.*;
import static com.igormaznitsa.sciareto.ui.UiUtils.BUNDLE;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FileUtils;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.MindMapController;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.api.CustomJob;
import com.igormaznitsa.mindmap.plugins.api.PopUpMenuItemPlugin;
import com.igormaznitsa.mindmap.plugins.processors.ExtraFilePlugin;
import com.igormaznitsa.mindmap.plugins.processors.ExtraJumpPlugin;
import com.igormaznitsa.mindmap.plugins.processors.ExtraNotePlugin;
import com.igormaznitsa.mindmap.plugins.processors.ExtraURIPlugin;
import com.igormaznitsa.mindmap.plugins.tools.ChangeColorPlugin;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapListener;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelController;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractElement;
import com.igormaznitsa.mindmap.swing.panel.ui.ElementPart;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.Main;
import com.igormaznitsa.sciareto.preferences.PreferencesManager;
import com.igormaznitsa.sciareto.ui.tree.NodeProject;
import com.igormaznitsa.sciareto.ui.editors.mmeditors.ColorAttributePanel;
import com.igormaznitsa.sciareto.ui.misc.ColorChooserButton;
import com.igormaznitsa.sciareto.ui.DialogProviderManager;
import com.igormaznitsa.sciareto.ui.editors.mmeditors.FileEditPanel;
import com.igormaznitsa.sciareto.ui.editors.mmeditors.MindMapTreePanel;
import com.igormaznitsa.sciareto.ui.tabs.TabTitle;
import com.igormaznitsa.sciareto.ui.UiUtils;
import com.igormaznitsa.sciareto.ui.tree.FileTransferable;
import com.igormaznitsa.sciareto.ui.FindTextScopeProvider;

public final class MMDEditor extends AbstractScrollPane implements MindMapPanelController, MindMapController, MindMapListener, DropTargetListener {

  private static final long serialVersionUID = -1011638261448046208L;

  private static final Logger LOGGER = LoggerFactory.getLogger(MMDEditor.class);

  private final MindMapPanel mindMapPanel;

  private final TabTitle title;

  private static final String FILELINK_ATTR_OPEN_IN_SYSTEM = "useSystem"; //NOI18N
  private final Context context;

  private boolean dragAcceptableType;
  private final UndoRedoStorage<String> undoStorage = new UndoRedoStorage<>(5);

  private boolean preventAddUndo = false;
  private String currentModelState;

  public void refreshConfig() {
    this.mindMapPanel.refreshConfiguration();
  }

  public static final FileFilter MMD_FILE_FILTER = new FileFilter() {

    @Override
    public boolean accept(@Nonnull final File f) {
      return f.isDirectory() || f.getName().endsWith(".mmd");
    }

    @Override
    @Nonnull
    public String getDescription() {
      return "Mind Map document (*.mmd)";
    }
  };

  @Override
  @Nonnull
  public FileFilter getFileFilter() {
    return MMD_FILE_FILTER;
  }

  public MMDEditor(@Nonnull final Context context, @Nullable File file) throws IOException {
    super();
    this.context = context;
    this.title = new TabTitle(context, this, file);
    this.mindMapPanel = new MindMapPanel(this);
    this.mindMapPanel.addMindMapListener(this);
    this.setViewportView(this.mindMapPanel);
    this.mindMapPanel.setDropTarget(new DropTarget(this.mindMapPanel, this));

    final MindMap map;
    if (file == null) {
      map = new MindMap(this, true);
    }
    else {
      map = new MindMap(this, new StringReader(FileUtils.readFileToString(file, "UTF-8")));
    }

    this.mindMapPanel.setModel(Assertions.assertNotNull(map), false);

    loadContent(file);
    this.currentModelState = this.mindMapPanel.getModel().packToString();
  }

  public void topicToCentre(@Nullable final Topic topic) {
    if (topic != null) {
      final Runnable runnable = new Runnable() {
        @Override
        public void run() {
          AbstractElement element = (AbstractElement) topic.getPayload();

          if (element == null) {
            return;
          }

          final Rectangle2D bounds = element.getBounds();
          final Dimension viewPortSize = getViewport().getExtentSize();

          final int x = Math.max(0, (int) Math.round(bounds.getX() - (viewPortSize.getWidth() - bounds.getWidth()) / 2));
          final int y = Math.max(0, (int) Math.round(bounds.getY() - (viewPortSize.getHeight() - bounds.getHeight()) / 2));

          getViewport().setViewPosition(new Point(x, y));
        }
      };
      SwingUtilities.invokeLater(runnable);
    }
  }

  @Override
  @Nonnull
  public EditorType getContentType() {
    return EditorType.MINDMAP;
  }

  @Override
  public void focusToEditor() {
    this.mindMapPanel.requestFocus();
  }

  @Override
  public boolean isEditable() {
    return true;
  }

  @Override
  public boolean isSaveable() {
    return true;
  }

  @Override
  public boolean isRedo() {
    return this.undoStorage.hasRedo();
  }

  @Override
  public boolean isUndo() {
    return this.undoStorage.hasUndo();
  }

  @Override
  public void loadContent(@Nullable File file) throws IOException {
    final MindMap map;
    if (file == null) {
      map = new MindMap(this, true);
    }
    else {
      map = new MindMap(this, new StringReader(FileUtils.readFileToString(file, "UTF-8")));
    }
    this.mindMapPanel.setModel(Assertions.assertNotNull(map), false);

    this.undoStorage.clearRedo();
    this.undoStorage.clearUndo();

    this.title.setChanged(false);

    this.revalidate();
  }

  @Override
  public boolean saveDocument() throws IOException {
    boolean result = false;
    if (this.title.isChanged()) {
      File file = this.title.getAssociatedFile();
      if (file == null) {
        file = DialogProviderManager.getInstance().getDialogProvider().msgSaveFileDialog("mmd-editor-document", "Save Mind Map", null, true, getFileFilter(), "Save");
        if (file == null) {
          return result;
        }
      }
      FileUtils.write(file, this.mindMapPanel.getModel().write(new StringWriter(16384)).toString(), "UTF-8", false);
      this.title.setChanged(false);
      result = true;
    }
    else {
      result = true;
    }
    return result;
  }

  @Override
  public boolean isUnfoldCollapsedTopicDropTarget(@Nonnull final MindMapPanel source) {
    return PreferencesManager.getInstance().getPreferences().getBoolean("unfoldCollapsedTarget", true);
  }

  @Override
  public boolean isCopyColorInfoFromParentToNewChildAllowed(@Nonnull final MindMapPanel source) {
    return PreferencesManager.getInstance().getPreferences().getBoolean("copyColorInfoToNewChildAllowed", true);
  }

  @Override
  public boolean isSelectionAllowed(@Nonnull final MindMapPanel source) {
    return true;
  }

  @Override
  public boolean isElementDragAllowed(@Nonnull final MindMapPanel source) {
    return true;
  }

  @Override
  public boolean isMouseMoveProcessingAllowed(@Nonnull final MindMapPanel source) {
    return true;
  }

  @Override
  public boolean isMouseWheelProcessingAllowed(@Nonnull final MindMapPanel source) {
    return true;
  }

  @Override
  public boolean isMouseClickProcessingAllowed(@Nonnull final MindMapPanel source) {
    return true;
  }

  @Override
  @Nonnull
  public MindMapPanelConfig provideConfigForMindMapPanel(@Nonnull final MindMapPanel source) {
    final MindMapPanelConfig config = new MindMapPanelConfig();
    config.loadFrom(PreferencesManager.getInstance().getPreferences());
    return config;
  }

  private Map<Class<? extends PopUpMenuItemPlugin>, CustomJob> customProcessors = null;

  @Nonnull
  public MindMapPanel getMindMapPanel() {
    return this.mindMapPanel;
  }

  @Override
  @Nonnull
  public TabTitle getTabTitle() {
    return this.title;
  }

  @Override
  @Nonnull
  public JComponent getMainComponent() {
    return this;
  }

  @Override
  public void requestFocus() {
    this.mindMapPanel.requestFocus();
  }

  @Override
  public void onMindMapModelChanged(@Nonnull final MindMapPanel source) {
    if (!this.preventAddUndo && this.currentModelState != null) {
      this.undoStorage.addToUndo(this.currentModelState);
      this.undoStorage.clearRedo();
      this.currentModelState = source.getModel().packToString();
    }

    try {
      this.title.setChanged(true);
      this.getViewport().revalidate();
      this.repaint();
    }
    finally {
      this.context.notifyUpdateRedoUndo();
    }
  }

  @Override
  public boolean redo() {
    if (!this.mindMapPanel.endEdit(false)) {
      if (this.undoStorage.hasRedo()) {
        this.undoStorage.addToUndo(this.currentModelState);
        this.currentModelState = this.undoStorage.fromRedo();
        this.preventAddUndo = true;
        try {
          this.mindMapPanel.setModel(new MindMap(null, new StringReader(this.currentModelState)), true);
          this.title.setChanged(this.undoStorage.hasUndo() || this.undoStorage.hasRemovedUndoStateForFullBuffer());
        }
        catch (IOException ex) {
          LOGGER.error("Can't redo mind map", ex);
        }
        finally {
          this.preventAddUndo = false;
        }
      }
    }
    return this.undoStorage.hasRedo();
  }

  @Override
  public boolean undo() {
    if (!this.mindMapPanel.endEdit(false)) {
      if (this.undoStorage.hasUndo()) {
        this.undoStorage.addToRedo(this.currentModelState);
        this.currentModelState = this.undoStorage.fromUndo();
        this.preventAddUndo = true;
        try {
          this.mindMapPanel.setModel(new MindMap(null, new StringReader(this.currentModelState)), true);
          this.title.setChanged(this.undoStorage.hasUndo() || this.undoStorage.hasRemovedUndoStateForFullBuffer());
        }
        catch (IOException ex) {
          LOGGER.error("Can't redo mind map", ex);
        }
        finally {
          this.preventAddUndo = false;
        }
      }
    }
    return this.undoStorage.hasUndo();
  }

  @Override
  public void onMindMapModelRealigned(@Nonnull final MindMapPanel source, @Nonnull final Dimension coveredAreaSize) {
    this.getViewport().revalidate();
    this.repaint();
  }

  @Override
  public void onEnsureVisibilityOfTopic(@Nonnull final MindMapPanel source, @Nullable final Topic topic) {
    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        if (topic == null) {
          return;
        }

        final AbstractElement element = (AbstractElement) topic.getPayload();
        if (element == null) {
          return;
        }

        final Rectangle2D orig = element.getBounds();
        final int GAP = 30;

        final Rectangle bounds = orig.getBounds();
        bounds.setLocation(Math.max(0, bounds.x - GAP), Math.max(0, bounds.y - GAP));
        bounds.setSize(bounds.width + GAP * 2, bounds.height + GAP * 2);

        final JViewport viewport = getViewport();
        final Rectangle visible = viewport.getViewRect();

        if (visible.contains(bounds)) {
          return;
        }

        bounds.setLocation(bounds.x - visible.x, bounds.y - visible.y);

        viewport.scrollRectToVisible(bounds);
      }

    });
  }

  @Override
  public void onClickOnExtra(@Nonnull final MindMapPanel source, final int clicks, @Nonnull final Topic topic, @Nonnull final Extra<?> extra) {
    if (clicks > 1) {
      switch (extra.getType()) {
        case FILE: {
          Main.getApplicationFrame().endFullScreenIfActive();
          final MMapURI uri = (MMapURI) extra.getValue();
          final File theFile = uri.asFile(getProjectFolder());
          if (Boolean.parseBoolean(uri.getParameters().getProperty(FILELINK_ATTR_OPEN_IN_SYSTEM, "false"))) { //NOI18N
            UiUtils.openInSystemViewer(theFile);
          }
          else if (theFile.isDirectory()) {
            this.context.openProject(theFile, false);
          }
          else if (!this.context.openFileAsTab(theFile)) {
            UiUtils.openInSystemViewer(theFile);
          }
        }
        break;
        case LINK: {
          Main.getApplicationFrame().endFullScreenIfActive();
          final MMapURI uri = ((ExtraLink) extra).getValue();
          if (!UiUtils.browseURI(uri.asURI(), PreferencesManager.getInstance().getPreferences().getBoolean("useInsideBrowser", false))) { //NOI18N
            DialogProviderManager.getInstance().getDialogProvider().msgError(String.format(BUNDLE.getString("MMDGraphEditor.onClickOnExtra.msgCantBrowse"), uri.toString()));
          }
        }
        break;
        case NOTE: {
          editTextForTopic(topic);
        }
        break;
        case TOPIC: {
          final Topic theTopic = source.getModel().findTopicForLink((ExtraTopic) extra);
          if (theTopic == null) {
            // not presented
            DialogProviderManager.getInstance().getDialogProvider().msgWarn(BUNDLE.getString("MMDGraphEditor.onClickOnExtra.msgCantFindTopic"));
          }
          else {
            // detected
            this.mindMapPanel.focusTo(theTopic);
          }
        }
        break;
      }
    }
  }

  @Override
  public void onChangedSelection(@Nonnull final MindMapPanel source, @Nonnull @MustNotContainNull final Topic[] currentSelectedTopics) {
  }

  @Override
  public boolean allowedRemovingOfTopics(@Nonnull final MindMapPanel source, @Nonnull @MustNotContainNull final Topic[] topics) {
    boolean topicsNotImportant = true;

    for (final Topic t : topics) {
      topicsNotImportant &= t.canBeLost();
      if (!topicsNotImportant) {
        break;
      }
    }

    final boolean result;

    if (topicsNotImportant) {
      result = true;
    }
    else {
      result = DialogProviderManager.getInstance().getDialogProvider().msgConfirmYesNo(BUNDLE.getString("MMDGraphEditor.allowedRemovingOfTopics,title"), String.format(BUNDLE.getString("MMDGraphEditor.allowedRemovingOfTopics.message"), topics.length));
    }
    return result;
  }

  @Override
  public void dragEnter(@Nonnull final DropTargetDragEvent dtde) {
    this.dragAcceptableType = checkDragType(dtde);
    if (!this.dragAcceptableType) {
      dtde.rejectDrag();
    }
    else {
      dtde.acceptDrag(DnDConstants.ACTION_MOVE);
    }
    repaint();
  }

  @Override
  public void dragOver(@Nonnull final DropTargetDragEvent dtde) {
    if (acceptOrRejectDragging(dtde)) {
      dtde.acceptDrag(DnDConstants.ACTION_MOVE);
    }
    else {
      dtde.rejectDrag();
    }
    repaint();
  }

  @Override
  public void dropActionChanged(@Nonnull final DropTargetDragEvent dtde) {
  }

  @Override
  public void dragExit(@Nonnull final DropTargetEvent dte) {
  }

  @Override
  public void drop(@Nonnull final DropTargetDropEvent dtde) {

    dtde.acceptDrop(DnDConstants.ACTION_MOVE);
    File detectedFileObject = null;

    for (final DataFlavor df : dtde.getCurrentDataFlavors()) {
      final Class<?> representation = df.getRepresentationClass();
      if (FileTransferable.class.isAssignableFrom(representation)) {
        final FileTransferable t = (FileTransferable) dtde.getTransferable();
        final List<File> listOfFiles = t.getFiles();
        detectedFileObject = listOfFiles.isEmpty() ? null : listOfFiles.get(0);
        break;
      }
      else if (df.isFlavorJavaFileListType()) {
        try {
          final List list = (List) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
          if (list != null && !list.isEmpty()) {
            detectedFileObject = (File) list.get(0);
          }
          break;
        }
        catch (Exception ex) {
          LOGGER.error("Can't extract file from DnD", ex);
        }
      }
    }

    if (detectedFileObject != null) {
      addFileToElement(detectedFileObject, this.mindMapPanel.findTopicUnderPoint(dtde.getLocation()));
    }
  }

  private void addFileToElement(@Nonnull final File theFile, @Nullable final AbstractElement element) {
    if (element != null) {
      final Topic topic = element.getModel();
      final MMapURI theURI;

      if (PreferencesManager.getInstance().getPreferences().getBoolean("makeRelativePathToProject", true)) { //NOI18N
        final File projectFolder = getProjectFolder();
        if (theFile.equals(projectFolder)) {
          theURI = new MMapURI(projectFolder, new File("."), null);
        }
        else {
          theURI = new MMapURI(projectFolder, theFile, null);
        }
      }
      else {
        theURI = new MMapURI(null, theFile, null);
      }

      if (topic.getExtras().containsKey(Extra.ExtraType.FILE)) {
        if (!DialogProviderManager.getInstance().getDialogProvider().msgConfirmOkCancel(BUNDLE.getString("MMDGraphEditor.addDataObjectToElement.confirmTitle"), BUNDLE.getString("MMDGraphEditor.addDataObjectToElement.confirmMsg"))) {
          return;
        }
      }

      topic.setExtra(new ExtraFile(theURI));
      this.mindMapPanel.invalidate();
      this.mindMapPanel.repaint();
      onMindMapModelChanged(this.mindMapPanel);
    }
  }

  protected boolean acceptOrRejectDragging(@Nonnull final DropTargetDragEvent dtde) {
    final int dropAction = dtde.getDropAction();

    boolean result = false;

    if (this.dragAcceptableType && (dropAction & DnDConstants.ACTION_COPY_OR_MOVE) != 0 && this.mindMapPanel.findTopicUnderPoint(dtde.getLocation()) != null) {
      result = true;
    }

    return result;
  }

  protected static boolean checkDragType(@Nonnull final DropTargetDragEvent dtde) {
    boolean result = false;
    for (final DataFlavor flavor : dtde.getCurrentDataFlavors()) {
      final Class dataClass = flavor.getRepresentationClass();
      if (FileTransferable.class.isAssignableFrom(dataClass) || flavor.isFlavorJavaFileListType()) {
        result = true;
        break;
      }
    }
    return result;
  }

  @Nonnull
  private Map<Class<? extends PopUpMenuItemPlugin>, CustomJob> getCustomProcessors() {
    if (this.customProcessors == null) {
      this.customProcessors = new HashMap<>();
      this.customProcessors.put(ExtraNotePlugin.class, new CustomJob() {
        @Override
        public void doJob(@Nonnull final PopUpMenuItemPlugin plugin, @Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nullable final Topic topic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
          if (topic != null) {
            editTextForTopic(topic);
            panel.requestFocus();
          }
        }
      });
      this.customProcessors.put(ExtraFilePlugin.class, new CustomJob() {
        @Override
        public void doJob(@Nonnull final PopUpMenuItemPlugin plugin, @Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nullable final Topic topic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
          editFileLinkForTopic(topic);
          panel.requestFocus();
        }
      });
      this.customProcessors.put(ExtraURIPlugin.class, new CustomJob() {
        @Override
        public void doJob(@Nonnull final PopUpMenuItemPlugin plugin, @Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nullable final Topic topic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
          editLinkForTopic(topic);
          panel.requestFocus();
        }
      });
      this.customProcessors.put(ExtraJumpPlugin.class, new CustomJob() {
        @Override
        public void doJob(@Nonnull final PopUpMenuItemPlugin plugin, @Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nullable final Topic topic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
          editTopicLinkForTopic(topic);
          panel.requestFocus();
        }
      });
      this.customProcessors.put(ChangeColorPlugin.class, new CustomJob() {
        @Override
        public void doJob(@Nonnull final PopUpMenuItemPlugin plugin, @Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nullable final Topic topic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
          processColorDialogForTopics(panel, selectedTopics.length > 0 ? selectedTopics : new Topic[]{topic});
        }
      });
    }
    return this.customProcessors;
  }

  private void editTextForTopic(@Nonnull final Topic topic) {
    final ExtraNote note = (ExtraNote) topic.getExtras().get(Extra.ExtraType.NOTE);
    final String result;
    if (note == null) {
      // create new
      result = UiUtils.editText(String.format(BUNDLE.getString("MMDGraphEditor.editTextForTopic.dlfAddNoteTitle"), Utils.makeShortTextVersion(topic.getText(), 16)), ""); //NOI18N
    }
    else {
      // edit
      result = UiUtils.editText(String.format(BUNDLE.getString("MMDGraphEditor.editTextForTopic.dlgEditNoteTitle"), Utils.makeShortTextVersion(topic.getText(), 16)), note.getValue());
    }
    if (result != null) {
      if (result.isEmpty()) {
        topic.removeExtra(Extra.ExtraType.NOTE);
      }
      else {
        topic.setExtra(new ExtraNote(result));
      }
      this.mindMapPanel.invalidate();
      this.mindMapPanel.repaint();
      onMindMapModelChanged(this.mindMapPanel);
    }
  }

  @Nullable
  private File getProjectFolder() {
    File result = null;
    final File associatedFile = this.title.getAssociatedFile();
    if (associatedFile != null) {
      final NodeProject project = context.findProjectForFile(associatedFile);
      result = project == null ? null : project.getFolder();
    }
    return result;
  }

  private void editFileLinkForTopic(@Nullable final Topic topic) {
    if (topic != null) {
      final ExtraFile file = (ExtraFile) topic.getExtras().get(Extra.ExtraType.FILE);

      final FileEditPanel.DataContainer path;

      final File projectFolder = getProjectFolder();
      if (file == null) {
        path = UiUtils.editFilePath(BUNDLE.getString("MMDGraphEditor.editFileLinkForTopic.dlgTitle"), projectFolder, null);
      }
      else {
        final MMapURI uri = file.getValue();
        final boolean flagOpenInSystem = Boolean.parseBoolean(uri.getParameters().getProperty(FILELINK_ATTR_OPEN_IN_SYSTEM, "false")); //NOI18N

        final FileEditPanel.DataContainer origPath;
        origPath = new FileEditPanel.DataContainer(uri.asFile(projectFolder).getAbsolutePath(), flagOpenInSystem);
        path = UiUtils.editFilePath(BUNDLE.getString("MMDGraphEditor.editFileLinkForTopic.addPathTitle"), projectFolder, origPath);
      }

      if (path != null) {
        final boolean valueChanged;
        if (path.isEmpty()) {
          valueChanged = topic.removeExtra(Extra.ExtraType.FILE);
        }
        else {
          final Properties props = new Properties();
          if (path.isShowWithSystemTool()) {
            props.put(FILELINK_ATTR_OPEN_IN_SYSTEM, "true"); //NOI18N
          }
          final MMapURI fileUri = MMapURI.makeFromFilePath(PreferencesManager.getInstance().getPreferences().getBoolean("makeRelativePathToProject", true) ? projectFolder : null, path.getPath(), props); //NOI18N
          final File theFile = fileUri.asFile(projectFolder);
          LOGGER.info(String.format("Path %s converted to uri: %s", path.getPath(), fileUri.asString(false, true))); //NOI18N

          if (theFile.exists()) {
            topic.setExtra(new ExtraFile(fileUri));
            valueChanged = true;
          }
          else {
            DialogProviderManager.getInstance().getDialogProvider().msgError(String.format(BUNDLE.getString("MMDGraphEditor.editFileLinkForTopic.errorCantFindFile"), path.getPath()));
            valueChanged = false;
          }
        }

        if (valueChanged) {
          this.mindMapPanel.invalidate();
          this.mindMapPanel.repaint();
          onMindMapModelChanged(this.mindMapPanel);
        }
      }
    }
  }

  private void editTopicLinkForTopic(@Nullable final Topic topic) {
    if (topic != null) {
      final ExtraTopic link = (ExtraTopic) topic.getExtras().get(Extra.ExtraType.TOPIC);

      ExtraTopic result = null;

      final ExtraTopic remove = new ExtraTopic("_______"); //NOI18N

      if (link == null) {
        final MindMapTreePanel treePanel = new MindMapTreePanel(this.mindMapPanel.getModel(), null, true, null);
        if (DialogProviderManager.getInstance().getDialogProvider().msgOkCancel(BUNDLE.getString("MMDGraphEditor.editTopicLinkForTopic.dlgSelectTopicTitle"), treePanel)) {
          final Topic selected = treePanel.getSelectedTopic();
          treePanel.dispose();
          if (selected != null) {
            result = ExtraTopic.makeLinkTo(this.mindMapPanel.getModel(), selected);
          }
          else {
            result = remove;
          }
        }
      }
      else {
        final MindMapTreePanel panel = new MindMapTreePanel(this.mindMapPanel.getModel(), link, true, null);
        if (DialogProviderManager.getInstance().getDialogProvider().msgOkCancel(BUNDLE.getString("MMDGraphEditor.editTopicLinkForTopic.dlgEditSelectedTitle"), panel)) {
          final Topic selected = panel.getSelectedTopic();
          if (selected != null) {
            result = ExtraTopic.makeLinkTo(this.mindMapPanel.getModel(), selected);
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
        this.mindMapPanel.invalidate();
        this.mindMapPanel.repaint();
        onMindMapModelChanged(this.mindMapPanel);
      }
    }
  }

  private void editLinkForTopic(@Nullable final Topic topic) {
    if (topic != null) {
      final ExtraLink link = (ExtraLink) topic.getExtras().get(Extra.ExtraType.LINK);
      final MMapURI result;
      if (link == null) {
        // create new
        result = UiUtils.editURI(String.format(BUNDLE.getString("MMDGraphEditor.editLinkForTopic.dlgAddURITitle"), Utils.makeShortTextVersion(topic.getText(), 16)), null);
      }
      else {
        // edit
        result = UiUtils.editURI(String.format(BUNDLE.getString("MMDGraphEditor.editLinkForTopic.dlgEditURITitle"), Utils.makeShortTextVersion(topic.getText(), 16)), link.getValue());
      }
      if (result != null) {
        if (result == UiUtils.EMPTY_URI) {
          topic.removeExtra(Extra.ExtraType.LINK);
        }
        else {
          topic.setExtra(new ExtraLink(result));
        }
        this.mindMapPanel.invalidate();
        this.mindMapPanel.repaint();
        onMindMapModelChanged(this.mindMapPanel);
      }
    }
  }

  private void processColorDialogForTopics(@Nonnull final MindMapPanel source, @Nonnull @MustNotContainNull final Topic[] topics) {
    final Color borderColor = UiUtils.extractCommonColorForColorChooserButton(ATTR_BORDER_COLOR.getText(), topics);
    final Color fillColor = UiUtils.extractCommonColorForColorChooserButton(ATTR_FILL_COLOR.getText(), topics);
    final Color textColor = UiUtils.extractCommonColorForColorChooserButton(ATTR_TEXT_COLOR.getText(), topics);

    final ColorAttributePanel panel = new ColorAttributePanel(borderColor, fillColor, textColor);
    if (DialogProviderManager.getInstance().getDialogProvider().msgOkCancel(String.format(BUNDLE.getString("MMDGraphEditor.colorEditDialogTitle"), topics.length), panel)) {
      ColorAttributePanel.Result result = panel.getResult();

      if (result.getBorderColor() != ColorChooserButton.DIFF_COLORS) {
        Utils.setAttribute(ATTR_BORDER_COLOR.getText(), Utils.color2html(result.getBorderColor(), false), topics);
      }

      if (result.getTextColor() != ColorChooserButton.DIFF_COLORS) {
        Utils.setAttribute(ATTR_TEXT_COLOR.getText(), Utils.color2html(result.getTextColor(), false), topics);
      }

      if (result.getFillColor() != ColorChooserButton.DIFF_COLORS) {
        Utils.setAttribute(ATTR_FILL_COLOR.getText(), Utils.color2html(result.getFillColor(), false), topics);
      }

      onMindMapModelChanged(source);

      source.updateView(true);
    }
  }

  @Override
  @Nonnull
  public JPopupMenu makePopUpForMindMapPanel(@Nonnull final MindMapPanel source, @Nonnull final Point point, @Nullable final AbstractElement elementUnderMouse, @Nullable final ElementPart elementPartUnderMouse) {
    return Utils.makePopUp(source, DialogProviderManager.getInstance().getDialogProvider(), elementUnderMouse == null ? null : elementUnderMouse.getModel(), source.getSelectedTopics(), getCustomProcessors());
  }

  @Override
  @Nonnull
  public DialogProvider getDialogProvider(@Nonnull final MindMapPanel source) {
    return DialogProviderManager.getInstance().getDialogProvider();
  }

  @Override
  public boolean processDropTopicToAnotherTopic(@Nonnull final MindMapPanel source, @Nonnull final Point dropPoint, @Nullable final Topic draggedTopic, @Nullable final Topic destinationTopic) {
    boolean result = false;
    if (draggedTopic != null && destinationTopic != null && draggedTopic != destinationTopic) {
      if (destinationTopic.getExtras().containsKey(Extra.ExtraType.TOPIC)) {
        if (!DialogProviderManager.getInstance().getDialogProvider().msgConfirmOkCancel(BUNDLE.getString("MMDGraphEditor.addTopicToElement.confirmTitle"), BUNDLE.getString("MMDGraphEditor.addTopicToElement.confirmMsg"))) {
          return result;
        }
      }

      final ExtraTopic topicLink = ExtraTopic.makeLinkTo(this.mindMapPanel.getModel(), draggedTopic);
      destinationTopic.setExtra(topicLink);

      result = true;
    }
    return result;
  }

  @Override
  public boolean canBeDeletedSilently(@Nonnull final MindMap map, @Nonnull final Topic topic) {
    return topic.getText().isEmpty() && topic.getExtras().isEmpty() && doesContainOnlyStandardAttributes(topic);
  }

  @Override
  public boolean findNext(@Nonnull final Pattern pattern, @Nonnull final FindTextScopeProvider provider) {
    Topic startTopic = null;
    if (this.mindMapPanel.hasSelectedTopics()) {
      final Topic[] selected = this.mindMapPanel.getSelectedTopics();
      startTopic = selected[selected.length - 1];
    }

    final File projectBaseFolder = this.getProjectFolder();

    final Set<Extra.ExtraType> extras = EnumSet.noneOf(Extra.ExtraType.class);
    if (provider.toSearchIn(FindTextScopeProvider.SearchTextScope.IN_TOPIC_NOTES)) {
      extras.add(Extra.ExtraType.NOTE);
    }
    if (provider.toSearchIn(FindTextScopeProvider.SearchTextScope.IN_TOPIC_FILES)) {
      extras.add(Extra.ExtraType.FILE);
    }
    if (provider.toSearchIn(FindTextScopeProvider.SearchTextScope.IN_TOPIC_URI)) {
      extras.add(Extra.ExtraType.LINK);
    }
    final boolean inTopicText = provider.toSearchIn(FindTextScopeProvider.SearchTextScope.IN_TOPIC_TEXT);

    Topic found = this.mindMapPanel.getModel().findNext(projectBaseFolder, startTopic, pattern, inTopicText, extras);
    if (found == null && startTopic != null) {
      found = this.mindMapPanel.getModel().findNext(projectBaseFolder, null, pattern, inTopicText, extras);
    }

    if (found != null) {
      this.mindMapPanel.removeAllSelection();
      this.mindMapPanel.focusTo(found);
    }

    return found != null;
  }

  @Override
  public boolean findPrev(@Nonnull final Pattern pattern, @Nonnull final FindTextScopeProvider provider) {
    Topic startTopic = null;
    if (this.mindMapPanel.hasSelectedTopics()) {
      final Topic[] selected = this.mindMapPanel.getSelectedTopics();
      startTopic = selected[0];
    }

    final File projectBaseFolder = this.getProjectFolder();

    final Set<Extra.ExtraType> extras = new HashSet<>();
    if (provider.toSearchIn(FindTextScopeProvider.SearchTextScope.IN_TOPIC_NOTES)) {
      extras.add(Extra.ExtraType.NOTE);
    }
    if (provider.toSearchIn(FindTextScopeProvider.SearchTextScope.IN_TOPIC_FILES)) {
      extras.add(Extra.ExtraType.FILE);
    }
    if (provider.toSearchIn(FindTextScopeProvider.SearchTextScope.IN_TOPIC_URI)) {
      extras.add(Extra.ExtraType.LINK);
    }
    final boolean inTopicText = provider.toSearchIn(FindTextScopeProvider.SearchTextScope.IN_TOPIC_TEXT);

    Topic found = this.mindMapPanel.getModel().findPrev(projectBaseFolder, startTopic, pattern, inTopicText, extras);
    if (found == null && startTopic != null) {
      found = this.mindMapPanel.getModel().findPrev(projectBaseFolder, null, pattern, inTopicText, extras);
    }

    if (found != null) {
      this.mindMapPanel.removeAllSelection();
      this.mindMapPanel.focusTo(found);
    }

    return found != null;
  }

  @Override
  public boolean doesSupportPatternSearch() {
    return true;
  }

}
