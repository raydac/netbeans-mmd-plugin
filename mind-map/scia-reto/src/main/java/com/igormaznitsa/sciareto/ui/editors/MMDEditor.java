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

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.ide.commons.DnDUtils;
import com.igormaznitsa.mindmap.ide.commons.FilePathWithLine;
import com.igormaznitsa.mindmap.ide.commons.Misc;
import static com.igormaznitsa.mindmap.ide.commons.Misc.FILELINK_ATTR_LINE;
import static com.igormaznitsa.mindmap.ide.commons.Misc.FILELINK_ATTR_OPEN_IN_SYSTEM;
import com.igormaznitsa.mindmap.model.*;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.api.CustomJob;
import com.igormaznitsa.mindmap.plugins.api.PopUpMenuItemPlugin;
import com.igormaznitsa.mindmap.plugins.processors.ExtraFilePlugin;
import com.igormaznitsa.mindmap.plugins.processors.ExtraJumpPlugin;
import com.igormaznitsa.mindmap.plugins.processors.ExtraNotePlugin;
import com.igormaznitsa.mindmap.plugins.processors.ExtraURIPlugin;
import com.igormaznitsa.mindmap.plugins.tools.ChangeColorPlugin;
import com.igormaznitsa.mindmap.swing.panel.*;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractElement;
import com.igormaznitsa.mindmap.swing.panel.ui.ElementPart;
import com.igormaznitsa.mindmap.swing.panel.utils.KeyEventType;
import com.igormaznitsa.mindmap.swing.panel.utils.MindMapUtils;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.Main;
import com.igormaznitsa.sciareto.preferences.PreferencesManager;
import com.igormaznitsa.sciareto.ui.DialogProviderManager;
import com.igormaznitsa.sciareto.ui.FindTextScopeProvider;
import com.igormaznitsa.sciareto.ui.UiUtils;
import com.igormaznitsa.sciareto.ui.editors.mmeditors.ColorAttributePanel;
import com.igormaznitsa.sciareto.ui.editors.mmeditors.FileEditPanel;
import com.igormaznitsa.sciareto.ui.editors.mmeditors.MindMapTreePanel;
import com.igormaznitsa.sciareto.ui.misc.ColorChooserButton;
import com.igormaznitsa.sciareto.ui.tabs.TabTitle;
import com.igormaznitsa.sciareto.ui.tree.FileTransferable;
import com.igormaznitsa.sciareto.ui.tree.NodeProject;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

import static com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute.*;
import static com.igormaznitsa.mindmap.swing.panel.utils.Utils.assertSwingDispatchThread;
import com.igormaznitsa.sciareto.preferences.SystemFileExtensionManager;
import static com.igormaznitsa.sciareto.ui.UiUtils.BUNDLE;
import org.apache.commons.io.FilenameUtils;

public final class MMDEditor extends AbstractEditor implements MindMapPanelController, MindMapController, MindMapListener, DropTargetListener {

  private static final long serialVersionUID = -1011638261448046208L;

  private static final Logger LOGGER = LoggerFactory.getLogger(MMDEditor.class);

  private final MindMapPanel mindMapPanel;

  private final TabTitle title;

  private final Context context;

  private boolean dragAcceptableType;
  private final transient UndoRedoStorage<String> undoStorage = new UndoRedoStorage<>(5);

  private boolean preventAddUndo = false;
  private String currentModelState;

  private boolean firstLayouting = true;

  private final JScrollPane scrollPane;

  public void refreshConfig() {
    this.mindMapPanel.refreshConfiguration();
  }

  public static final FileFilter MMD_FILE_FILTER = new FileFilter() {

    @Override
    public boolean accept(@Nonnull final File f) {
      return f.isDirectory() || f.getName().endsWith(".mmd"); //NOI18N
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

    this.scrollPane = new JScrollPane(this.mindMapPanel);

    this.scrollPane.getHorizontalScrollBar().setBlockIncrement(128);
    this.scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
    this.scrollPane.getVerticalScrollBar().setBlockIncrement(128);
    this.scrollPane.getVerticalScrollBar().setUnitIncrement(16);

    this.scrollPane.setWheelScrollingEnabled(true);
    this.scrollPane.setAutoscrolls(true);

    final AdjustmentListener listener = new AdjustmentListener() {
      @Override
      public void adjustmentValueChanged(@Nonnull final AdjustmentEvent e) {
        mindMapPanel.repaint();
      }
    };

    this.scrollPane.getHorizontalScrollBar().addAdjustmentListener(listener);
    this.scrollPane.getVerticalScrollBar().addAdjustmentListener(listener);

    this.mindMapPanel.setDropTarget(new DropTarget(this.mindMapPanel, this));

    final MindMap map;
    if (file == null || file.length() == 0L) {
      map = new MindMap(this, true);
    } else {
      map = new MindMap(this, new StringReader(FileUtils.readFileToString(file, "UTF-8"))); //NOI18N
    }

    this.mindMapPanel.setModel(Assertions.assertNotNull(map), false);

    loadContent(file);
    this.currentModelState = this.mindMapPanel.getModel().packToString();
  }

  public void rootToCentre() {
    final Topic root = this.mindMapPanel.getModel().getRoot();
    if (root != null) {
      topicToCentre(root);
    }
  }

  public boolean topicToCentre(@Nullable final Topic topic) {
    boolean result = false;

    assertSwingDispatchThread();
    if (topic != null) {
      AbstractElement element = (AbstractElement) topic.getPayload();

      if (element == null && this.mindMapPanel.updateElementsAndSizeForCurrentGraphics(true, true)) {
        element = (AbstractElement) topic.getPayload();
        this.scrollPane.getViewport().doLayout();
      }

      if (element != null) {
        final Rectangle2D bounds = element.getBounds();
        final Dimension viewPortSize = this.scrollPane.getViewport().getExtentSize();

        final int x = Math.max(0, (int) Math.round(bounds.getX() - (viewPortSize.getWidth() - bounds.getWidth()) / 2));
        final int y = Math.max(0, (int) Math.round(bounds.getY() - (viewPortSize.getHeight() - bounds.getHeight()) / 2));

        this.scrollPane.getViewport().setViewPosition(new Point(x, y));
        result = true;
      }
    }

    return result;
  }

  @Override
  public void onNonConsumedKeyEvent(@Nonnull final MindMapPanel source, @Nonnull final KeyEvent e, @Nonnull final KeyEventType type) {
    if (type == KeyEventType.PRESSED && e.getModifiers() == 0 && (e.getKeyCode() == KeyEvent.VK_UP
            || e.getKeyCode() == KeyEvent.VK_LEFT
            || e.getKeyCode() == KeyEvent.VK_RIGHT
            || e.getKeyCode() == KeyEvent.VK_DOWN)) {
      e.consume();
    }
  }

  @Override
  public void onTopicCollapsatorClick(@Nonnull final MindMapPanel source, @Nonnull final Topic topic, final boolean beforeAction) {
    if (!beforeAction) {
      topicToCentre(topic);
    }
  }

  @Override
  @Nonnull
  public JComponent getMainComponent() {
    return this.mindMapPanel;
  }

  @Override
  @Nonnull
  public EditorContentType getEditorContentType() {
    return EditorContentType.MINDMAP;
  }

  @Override
  public void focusToEditor(final int line) {
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

  @Nonnull
  @Override
  public String getDefaultExtension() {
    return "mmd";
  }

  @Override
  public void loadContent(@Nullable File file) throws IOException {
    final MindMap map;
    if (file == null || file.length() == 0L) {
      map = new MindMap(this, true);
    } else {
      map = new MindMap(this, new StringReader(FileUtils.readFileToString(file, "UTF-8"))); //NOI18N
    }
    this.mindMapPanel.setModel(Assertions.assertNotNull(map), false);

    this.undoStorage.clearRedo();
    this.undoStorage.clearUndo();

    this.title.setChanged(false);

    this.scrollPane.revalidate();
  }

  @Override
  public boolean saveDocument() throws IOException {
    boolean result = false;
    if (this.title.isChanged()) {
      File file = this.title.getAssociatedFile();
      if (file == null) {
        file = DialogProviderManager.getInstance().getDialogProvider().msgSaveFileDialog(null, "mmd-editor-document", "Save Mind Map", null, true, getFileFilter(), "Save");
        if (file == null) {
          return result;
        }
      }
      FileUtils.write(file, this.mindMapPanel.getModel().write(new StringWriter(16384)).toString(), "UTF-8", false); //NOI18N
      this.title.setChanged(false);
      result = true;
      this.undoStorage.setFlagThatSomeStateLost();
    } else {
      result = true;
    }
    return result;
  }

  @Override
  public void onComponentElementsLayouted(@Nonnull final MindMapPanel source, @Nonnull final Graphics2D g) {
    if (this.firstLayouting) {
      this.firstLayouting = false;
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          topicToCentre(mindMapPanel.getModel().getRoot());
          scrollPane.revalidate();
          scrollPane.repaint();
        }
      });
    }
  }

  @Override
  public boolean isTrimTopicTextBeforeSet(@Nonnull final MindMapPanel source) {
    return PreferencesManager.getInstance().getPreferences().getBoolean("trimTopicText", false); //NOI18N
  }

  @Override
  public boolean isUnfoldCollapsedTopicDropTarget(@Nonnull final MindMapPanel source) {
    return PreferencesManager.getInstance().getPreferences().getBoolean("unfoldCollapsedTarget", true); //NOI18N
  }

  @Override
  public boolean isCopyColorInfoFromParentToNewChildAllowed(@Nonnull final MindMapPanel source) {
    return PreferencesManager.getInstance().getPreferences().getBoolean("copyColorInfoToNewChildAllowed", true); //NOI18N
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

  private transient Map<Class<? extends PopUpMenuItemPlugin>, CustomJob> customProcessors = null;

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
  public JComponent getContainerToShow() {
    return this.scrollPane;
  }

  @Override
  @Nonnull
  public AbstractEditor getEditor() {
    return this;
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
      this.scrollPane.revalidate();
      this.scrollPane.repaint();
    } finally {
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
        } catch (IOException ex) {
          LOGGER.error("Can't redo mind map", ex); //NOI18N
        } finally {
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
        } catch (IOException ex) {
          LOGGER.error("Can't redo mind map", ex); //NOI18N
        } finally {
          this.preventAddUndo = false;
        }
      }
    }
    return this.undoStorage.hasUndo();
  }

  @Override
  public void onMindMapModelRealigned(@Nonnull final MindMapPanel source, @Nonnull final Dimension coveredAreaSize) {
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

        final JViewport viewport = scrollPane.getViewport();
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
  public void onScaledByMouse(
          @Nonnull final MindMapPanel source, 
          @Nonnull final Point mousePoint, 
          final double oldScale, 
          final double newScale,
          @Nonnull final Dimension oldSize,
          @Nonnull final Dimension newSize
  ) {
    if (Double.compare(oldScale, newScale) != 0) {
      final JViewport viewport = this.scrollPane.getViewport();

      final Rectangle viewPos = viewport.getViewRect();

      final Dimension size = source.getSize();
      final Dimension extentSize = viewport.getExtentSize();

      if (extentSize.width < size.width || extentSize.height < size.height) {

        final int dx = mousePoint.x - viewPos.x;
        final int dy = mousePoint.y - viewPos.y;

        final double scaleX = newSize.getWidth() / oldSize.getWidth();
        final double scaleY = newSize.getHeight()/ oldSize.getHeight();
        
        final int newMouseX = (int) (Math.round(mousePoint.x * scaleX));
        final int newMouseY = (int) (Math.round(mousePoint.y * scaleY));

        viewPos.x = Math.max(0, newMouseX - dx);
        viewPos.y = Math.max(0, newMouseY - dy);
        viewport.setView(source);

        source.scrollRectToVisible(viewPos);
      } else {
        viewPos.x = 0;
        viewPos.y = 0;
        source.scrollRectToVisible(viewPos);
      }

      this.scrollPane.revalidate();
      this.scrollPane.repaint();
    }
  }

  @Override
  public void onClickOnExtra(@Nonnull final MindMapPanel source, final int modifiers, final int clicks, @Nonnull final Topic topic, @Nonnull final Extra<?> extra) {
    if (clicks > 1) {
      switch (extra.getType()) {
        case FILE: {
          Main.getApplicationFrame().endFullScreenIfActive();
          final MMapURI uri = (MMapURI) extra.getValue();
          final File theFile = uri.asFile(getProjectFolder());
          if (Boolean.parseBoolean(uri.getParameters().getProperty(FILELINK_ATTR_OPEN_IN_SYSTEM, "false"))) { //NOI18N
            UiUtils.openInSystemViewer(theFile);
          } else if (theFile.isDirectory()) {
            this.context.openProject(theFile, false);
          } else if (!this.context.openFileAsTab(theFile, FilePathWithLine.strToLine(uri.getParameters().getProperty(FILELINK_ATTR_LINE, null)))) {
            UiUtils.openInSystemViewer(theFile);
          }
        }
        break;
        case LINK: {
          Main.getApplicationFrame().endFullScreenIfActive();
          final MMapURI uri = ((ExtraLink) extra).getValue();
          if (!UiUtils.browseURI(uri.asURI(), PreferencesManager.getInstance().getPreferences().getBoolean("useInsideBrowser", false))) { //NOI18N
            DialogProviderManager.getInstance().getDialogProvider().msgError(null, String.format(BUNDLE.getString("MMDGraphEditor.onClickOnExtra.msgCantBrowse"), uri.toString()));
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
            DialogProviderManager.getInstance().getDialogProvider().msgWarn(null, BUNDLE.getString("MMDGraphEditor.onClickOnExtra.msgCantFindTopic"));
          } else {
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
    } else {
      result = DialogProviderManager.getInstance().getDialogProvider().msgConfirmYesNo(null, BUNDLE.getString("MMDGraphEditor.allowedRemovingOfTopics,title"), String.format(BUNDLE.getString("MMDGraphEditor.allowedRemovingOfTopics.message"), topics.length));
    }
    return result;
  }

  @Override
  public void dragEnter(@Nonnull final DropTargetDragEvent dtde) {
    this.dragAcceptableType = checkDragType(dtde);
    if (!this.dragAcceptableType) {
      dtde.rejectDrag();
    } else {
      dtde.acceptDrag(DnDConstants.ACTION_MOVE);
    }
    this.scrollPane.repaint();
  }

  @Override
  public void dragOver(@Nonnull final DropTargetDragEvent dtde) {
    if (acceptOrRejectDragging(dtde)) {
      dtde.acceptDrag(DnDConstants.ACTION_MOVE);
    } else {
      dtde.rejectDrag();
    }
    this.scrollPane.repaint();
  }

  @Override
  public void dropActionChanged(@Nonnull final DropTargetDragEvent dtde) {
  }

  @Override
  public void dragExit(@Nonnull final DropTargetEvent dte) {
  }

  @Nullable
  private File extractDropFile(@Nonnull final DropTargetDropEvent dtde) throws Exception {
    File result = null;
    for (final DataFlavor df : dtde.getCurrentDataFlavors()) {
      final Class<?> representation = df.getRepresentationClass();
      if (FileTransferable.class.isAssignableFrom(representation)) {
        final FileTransferable t = (FileTransferable) dtde.getTransferable();
        final List<File> listOfFiles = t.getFiles();
        result = listOfFiles.isEmpty() ? null : listOfFiles.get(0);
        break;
      } else if (df.isFlavorJavaFileListType()) {
        try {
          final List list = (List) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
          if (list != null && !list.isEmpty()) {
            result = (File) list.get(0);
          }
          break;
        } catch (final Exception ex) {
          LOGGER.error("Can't extract file from DnD", ex); //NOI18N
        }
      }
    }
    return result;
  }

  @Override
  public void drop(@Nonnull final DropTargetDropEvent dtde) {
    dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

    File detectedFile;
    String detectedLink;
    String detectedNote;
    URI decodedLink;

    try {
      detectedFile = extractDropFile(dtde);
      detectedLink = DnDUtils.extractDropLink(dtde);
      detectedNote = DnDUtils.extractDropNote(dtde);

      decodedLink = null;
      if (detectedLink != null) {
        try {
          decodedLink = new URI(detectedLink);
        } catch (final URISyntaxException ex) {
          decodedLink = null;
        }
      }

      dtde.dropComplete(true);

    } catch (final Exception ex) {
      LOGGER.error("Error during DnD processing", ex); //NOI18N
      dtde.dropComplete(false);
      return;
    }

    final AbstractElement element = this.mindMapPanel.findTopicUnderPoint(dtde.getLocation());

    if (detectedFile != null) {
      decodedLink = DnDUtils.extractUrlLinkFromFile(detectedFile);
      if (decodedLink != null) {
        addURItoElement(decodedLink, element);
      } else {
        addFileToElement(detectedFile, element, -1, SystemFileExtensionManager.getInstance().isSystemFileExtension(FilenameUtils.getExtension(detectedFile.getName())));
      }
      dtde.dropComplete(true);
    } else if (decodedLink != null) {
      addURItoElement(decodedLink, element);
      dtde.dropComplete(true);
    } else if (detectedNote != null) {
      addNoteToElement(detectedNote, element);
      dtde.dropComplete(true);
    } else {
      dtde.dropComplete(false);
    }
  }

  private void addURItoElement(@Nonnull final URI uri, @Nullable final AbstractElement element) {
    if (element != null) {
      final Topic topic = element.getModel();
      final MMapURI mmapUri = new MMapURI(uri);
      if (topic.getExtras().containsKey(Extra.ExtraType.LINK)) {
        if (!DialogProviderManager.getInstance().getDialogProvider().msgConfirmOkCancel(null, BUNDLE.getString("MMDGraphEditor.addDataObjectLinkToElement.confirmTitle"), BUNDLE.getString("MMDGraphEditor.addDataObjectLinkToElement.confirmMsg"))) {
          return;
        }
      }
      topic.setExtra(new ExtraLink(mmapUri));
      this.mindMapPanel.invalidate();
      this.mindMapPanel.repaint();
      onMindMapModelChanged(this.mindMapPanel);
    }
  }

  private void addNoteToElement(@Nonnull final String text, @Nullable final AbstractElement element) {
    if (element != null) {
      final Topic topic = element.getModel();
      if (topic.getExtras().containsKey(Extra.ExtraType.NOTE)) {
        if (!DialogProviderManager.getInstance().getDialogProvider().msgConfirmOkCancel(null, BUNDLE.getString("MMDGraphEditor.addDataObjectTextToElement.confirmTitle"), BUNDLE.getString("MMDGraphEditor.addDataObjectTextToElement.confirmMsg"))) {
          return;
        }
      }
      topic.setExtra(new ExtraNote(text));
      this.mindMapPanel.invalidate();
      this.mindMapPanel.repaint();
      onMindMapModelChanged(this.mindMapPanel);
    }
  }

  private void addFileToElement(@Nonnull final File theFile, @Nullable final AbstractElement element, final int lineNumber, final boolean openInSystemBrowser) {
    if (element != null) {
      final Topic topic = element.getModel();
      final MMapURI theURI;

      final Properties properties = new Properties();

      if (openInSystemBrowser) {
        properties.setProperty(FILELINK_ATTR_OPEN_IN_SYSTEM, "true");
      }

      if (lineNumber >= 0) {
        properties.setProperty(FILELINK_ATTR_LINE, Integer.toString(lineNumber));
      }

      if (PreferencesManager.getInstance().getPreferences().getBoolean("makeRelativePathToProject", true)) { //NOI18N
        final File projectFolder = getProjectFolder();
        if (theFile.equals(projectFolder)) {
          theURI = new MMapURI(projectFolder, new File("."), properties); //NOI18N
        } else {
          theURI = new MMapURI(projectFolder, theFile, properties);
        }
      } else {
        theURI = new MMapURI(null, theFile, properties);
      }

      if (topic.getExtras().containsKey(Extra.ExtraType.FILE)) {
        if (!DialogProviderManager.getInstance().getDialogProvider().msgConfirmOkCancel(null, BUNDLE.getString("MMDGraphEditor.addDataObjectToElement.confirmTitle"), BUNDLE.getString("MMDGraphEditor.addDataObjectToElement.confirmMsg"))) {
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
    boolean result = DnDUtils.isFileOrLinkOrText(dtde);
    if (!result) {
      for (final DataFlavor flavor : dtde.getCurrentDataFlavors()) {
        final Class dataClass = flavor.getRepresentationClass();
        if (FileTransferable.class.isAssignableFrom(dataClass)) {
          result = true;
          break;
        }
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
    } else {
      // edit
      result = UiUtils.editText(String.format(BUNDLE.getString("MMDGraphEditor.editTextForTopic.dlgEditNoteTitle"), Utils.makeShortTextVersion(topic.getText(), 16)), note.getValue());
    }
    if (result != null) {
      boolean changed = false;

      if (result.isEmpty()) {
        if (note != null) {
          topic.removeExtra(Extra.ExtraType.NOTE);
          changed = true;
        }
      } else {
        if (note == null || !note.getValue().equals(result)) {
          topic.setExtra(new ExtraNote(result));
          changed = true;
        }
      }
      if (changed) {
        this.mindMapPanel.invalidate();
        this.mindMapPanel.repaint();
        onMindMapModelChanged(this.mindMapPanel);
      }
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
      final ExtraFile currentFilePath = (ExtraFile) topic.getExtras().get(Extra.ExtraType.FILE);

      final FileEditPanel.DataContainer dataContainer;

      final File projectFolder = getProjectFolder();
      if (currentFilePath == null) {
        final FileEditPanel.DataContainer prefilled = new FileEditPanel.DataContainer(null, this.mindMapPanel.getSessionObject(Misc.SESSIONKEY_ADD_FILE_OPEN_IN_SYSTEM, Boolean.class, false));
        dataContainer = UiUtils.editFilePath(BUNDLE.getString("MMDGraphEditor.editFileLinkForTopic.dlgTitle"),
                this.mindMapPanel.getSessionObject(Misc.SESSIONKEY_ADD_FILE_LAST_FOLDER, File.class, projectFolder),
                prefilled);
        if (dataContainer != null) {
          this.mindMapPanel.putSessionObject(Misc.SESSIONKEY_ADD_FILE_OPEN_IN_SYSTEM, dataContainer.isShowWithSystemTool());
        }
      } else {
        final MMapURI uri = currentFilePath.getValue();
        final boolean flagOpenInSystem = Boolean.parseBoolean(uri.getParameters().getProperty(FILELINK_ATTR_OPEN_IN_SYSTEM, "false")); //NOI18N
        final int line = FilePathWithLine.strToLine(uri.getParameters().getProperty(FILELINK_ATTR_LINE, null));

        final FileEditPanel.DataContainer origPath;
        origPath = new FileEditPanel.DataContainer(uri.asFile(projectFolder).getAbsolutePath() + (line < 0 ? "" : ":" + line), flagOpenInSystem);
        dataContainer = UiUtils.editFilePath(BUNDLE.getString("MMDGraphEditor.editFileLinkForTopic.addPathTitle"), projectFolder, origPath);
      }

      if (dataContainer != null) {
        final boolean valueChanged;
        if (dataContainer.getFilePathWithLine().isEmptyOrOnlySpaces()) {
          valueChanged = topic.removeExtra(Extra.ExtraType.FILE);
        } else {
          final Properties props = new Properties();
          if (dataContainer.isShowWithSystemTool()) {
            props.put(FILELINK_ATTR_OPEN_IN_SYSTEM, "true"); //NOI18N
          }
          if (dataContainer.getFilePathWithLine().getLine() >= 0) {
            props.put(FILELINK_ATTR_LINE, Integer.toString(dataContainer.getFilePathWithLine().getLine()));
          }
          final MMapURI fileUri = MMapURI.makeFromFilePath(PreferencesManager.getInstance().getPreferences().getBoolean("makeRelativePathToProject", true) ? projectFolder : null, dataContainer.getFilePathWithLine().getPath(), props); //NOI18N
          final File theFile = fileUri.asFile(projectFolder);
          LOGGER.info(String.format("Path %s converted to uri: %s", dataContainer.getFilePathWithLine(), fileUri.asString(false, true))); //NOI18N

          if (theFile.exists()) {
            if (currentFilePath == null) {
              this.mindMapPanel.putSessionObject(Misc.SESSIONKEY_ADD_FILE_LAST_FOLDER, theFile.getParentFile());
            }
            topic.setExtra(new ExtraFile(fileUri));
            valueChanged = true;
          } else {
            DialogProviderManager.getInstance().getDialogProvider().msgError(null, String.format(BUNDLE.getString("MMDGraphEditor.editFileLinkForTopic.errorCantFindFile"), dataContainer.getFilePathWithLine().getPath()));
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
        if (DialogProviderManager.getInstance().getDialogProvider().msgOkCancel(null, BUNDLE.getString("MMDGraphEditor.editTopicLinkForTopic.dlgSelectTopicTitle"), treePanel)) {
          final Topic selected = treePanel.getSelectedTopic();
          treePanel.dispose();
          if (selected != null) {
            result = ExtraTopic.makeLinkTo(this.mindMapPanel.getModel(), selected);
          } else {
            result = remove;
          }
        }
      } else {
        final MindMapTreePanel panel = new MindMapTreePanel(this.mindMapPanel.getModel(), link, true, null);
        if (DialogProviderManager.getInstance().getDialogProvider().msgOkCancel(null, BUNDLE.getString("MMDGraphEditor.editTopicLinkForTopic.dlgEditSelectedTitle"), panel)) {
          final Topic selected = panel.getSelectedTopic();
          if (selected != null) {
            result = ExtraTopic.makeLinkTo(this.mindMapPanel.getModel(), selected);
          } else {
            result = remove;
          }
        }
      }

      if (result != null) {
        boolean changed = false;
        if (result == remove) {
          if (topic.getExtras().get(Extra.ExtraType.TOPIC) != null) {
            topic.removeExtra(Extra.ExtraType.TOPIC);
            changed = true;
          }
        } else {
          final Object prev = topic.getExtras().get(Extra.ExtraType.TOPIC);
          if (prev == null) {
            topic.setExtra(result);
            changed = true;
          } else {
            if (!result.equals(prev)) {
              topic.setExtra(result);
              changed = true;
            }
          }
        }
        if (changed) {
          this.mindMapPanel.invalidate();
          this.mindMapPanel.repaint();
          onMindMapModelChanged(this.mindMapPanel);
        }
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
      } else {
        // edit
        result = UiUtils.editURI(String.format(BUNDLE.getString("MMDGraphEditor.editLinkForTopic.dlgEditURITitle"), Utils.makeShortTextVersion(topic.getText(), 16)), link.getValue());
      }
      if (result != null) {

        boolean changed = false;

        if (result == UiUtils.EMPTY_URI) {
          if (link != null) {
            changed = true;
            topic.removeExtra(Extra.ExtraType.LINK);
          }
        } else {
          if (link == null || !result.equals(link.getAsURI())) {
            changed = true;
            topic.setExtra(new ExtraLink(result));
          }
        }

        if (changed) {
          this.mindMapPanel.invalidate();
          this.mindMapPanel.repaint();
          onMindMapModelChanged(this.mindMapPanel);
        }
      }
    }
  }

  private void processColorDialogForTopics(@Nonnull final MindMapPanel source, @Nonnull @MustNotContainNull final Topic[] topics) {
    final Color borderColor = UiUtils.extractCommonColorForColorChooserButton(ATTR_BORDER_COLOR.getText(), topics);
    final Color fillColor = UiUtils.extractCommonColorForColorChooserButton(ATTR_FILL_COLOR.getText(), topics);
    final Color textColor = UiUtils.extractCommonColorForColorChooserButton(ATTR_TEXT_COLOR.getText(), topics);

    final ColorAttributePanel panel = new ColorAttributePanel(borderColor, fillColor, textColor);
    if (DialogProviderManager.getInstance().getDialogProvider().msgOkCancel(null, String.format(BUNDLE.getString("MMDGraphEditor.colorEditDialogTitle"), topics.length), panel)) {
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
    return Utils.makePopUp(source, Main.getApplicationFrame().isFullScreenActive(), DialogProviderManager.getInstance().getDialogProvider(), elementUnderMouse == null ? null : elementUnderMouse.getModel(), source.getSelectedTopics(), getCustomProcessors());
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
        if (!DialogProviderManager.getInstance().getDialogProvider().msgConfirmOkCancel(null, BUNDLE.getString("MMDGraphEditor.addTopicToElement.confirmTitle"), BUNDLE.getString("MMDGraphEditor.addTopicToElement.confirmMsg"))) {
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

  @Override
  public boolean doesSupportCutCopyPaste() {
    return true;
  }

  @Override
  public boolean isCutAllowed() {
    return this.mindMapPanel.getSelectedTopics().length > 0;
  }

  @Override
  public boolean doCut() {
    assertSwingDispatchThread();
    return this.mindMapPanel.copyTopicsToClipboard(true, MindMapUtils.removeSuccessorsAndDuplications(this.mindMapPanel.getSelectedTopics()));
  }

  @Override
  public boolean isCopyAllowed() {
    return this.mindMapPanel.getSelectedTopics().length > 0;
  }

  @Override
  public boolean isPasteAllowed() {
    final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    return this.mindMapPanel.hasSelectedTopics() && Utils.isDataFlavorAvailable(clipboard, MMDTopicsTransferable.MMD_DATA_FLAVOR);
  }

  @Override
  public boolean doCopy() {
    assertSwingDispatchThread();
    return this.mindMapPanel.copyTopicsToClipboard(false, MindMapUtils.removeSuccessorsAndDuplications(this.mindMapPanel.getSelectedTopics()));
  }

  @Override
  public boolean doPaste() {
    assertSwingDispatchThread();
    return this.mindMapPanel.pasteTopicsFromClipboard();
  }

}
