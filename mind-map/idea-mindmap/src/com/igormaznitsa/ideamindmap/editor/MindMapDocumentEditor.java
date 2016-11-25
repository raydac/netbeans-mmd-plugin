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
import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.igormaznitsa.ideamindmap.utils.SelectIn;
import com.igormaznitsa.ideamindmap.utils.SwingUtils;
import com.igormaznitsa.mindmap.ide.commons.DnDUtils;
import com.igormaznitsa.mindmap.model.*;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MMDTopicsTransferable;
import com.igormaznitsa.mindmap.swing.panel.MindMapListener;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractElement;
import com.igormaznitsa.mindmap.swing.panel.utils.MindMapUtils;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.CopyProvider;
import com.intellij.ide.CutProvider;
import com.intellij.ide.DataManager;
import com.intellij.ide.PasteProvider;
import com.intellij.ide.dnd.DnDDragStartBean;
import com.intellij.ide.dnd.TransferableWrapper;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.xml.ui.Committable;
import com.intellij.util.xml.ui.UndoHelper;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;

import static com.igormaznitsa.ideamindmap.utils.SwingUtils.safeSwing;
import static com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute.doesContainOnlyStandardAttributes;
import static com.igormaznitsa.mindmap.swing.panel.utils.Utils.assertSwingDispatchThread;

public class MindMapDocumentEditor implements DocumentsEditor, MindMapController, MindMapListener, DropTargetListener, Committable, DataProvider, CopyProvider, CutProvider, PasteProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(MindMapDocumentEditor.class);
  private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("/i18n/Bundle");

  private static final String FILELINK_ATTR_OPEN_IN_SYSTEM = "useSystem"; //NOI18N

  private final JBScrollPane mainScrollPane;
  private final MindMapPanel mindMapPanel;
  private final Project project;
  private final VirtualFile file;
  private final Document[] documents;
  private boolean dragAcceptableType = false;
  private final MindMapPanelControllerImpl panelController;
  private final UndoHelper undoHelper;
  private boolean firstLayouting = true;


  public MindMapDocumentEditor(final Project project, final VirtualFile file) {
    this.project = project;
    this.file = file;

    this.panelController = new MindMapPanelControllerImpl(this);

    this.mindMapPanel = new MindMapPanel(panelController);
    this.mindMapPanel.putTmpObject("project", project);
    this.mindMapPanel.putTmpObject("editor", this);

    this.mindMapPanel.addMindMapListener(this);
    this.mainScrollPane = new JBScrollPane();
    this.mainScrollPane.setViewportView(this.mindMapPanel);
    this.mainScrollPane.setWheelScrollingEnabled(true);
    this.mainScrollPane.setAutoscrolls(true);

    this.mindMapPanel.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        mainScrollPane.getViewport().revalidate();
      }
    });

    final Document document = FileDocumentManager.getInstance().getDocument(this.file);

    this.documents = new Document[]{document};

    this.mindMapPanel.setDropTarget(new DropTarget(this.mindMapPanel, this));

    loadMindMapFromDocument();

    this.undoHelper = new UndoHelper(this.project, this);
    this.undoHelper.addWatchedDocument(getDocument());

    this.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      public void documentChanged(DocumentEvent e) {
        loadMindMapFromDocument();
      }
    });

    DataManager.registerDataProvider(this.mainScrollPane,this);
  }

  @Override
  public void onComponentElementsLayouted(@Nonnull final MindMapPanel mindMapPanel, @Nonnull final Graphics2D graphics2D) {
    if (this.firstLayouting) {
      this.firstLayouting = false;
      centreToRoot();
    }
  }

  public void centreToRoot() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        final Topic root = mindMapPanel.getModel().getRoot();
        if (root != null) {
          mindMapPanel.updateElementsAndSizeForCurrentGraphics(true, false);
          topicToCentre(root);
        }
      }
    });
  }

  @Override
  public void onTopicCollapsatorClick(@Nonnull final MindMapPanel source, @Nonnull final Topic topic, final boolean beforeAction) {
    if (!beforeAction) {
      this.mindMapPanel.getModel().resetPayload();
      topicToCentre(topic);
    }
  }

  public void topicToCentre(@Nullable Topic topic) {
    assertSwingDispatchThread();

    if (topic != null) {
      AbstractElement element = (AbstractElement) topic.getPayload();

      if (element == null && this.mindMapPanel.updateElementsAndSizeForCurrentGraphics(true, true)) {
        topic = this.mindMapPanel.getModel().findForPositionPath(topic.getPositionPath());
        if (topic != null) {
          element = (AbstractElement) topic.getPayload();
          this.mainScrollPane.getViewport().doLayout();
        }
      }

      if (element != null) {
        final Rectangle2D bounds = element.getBounds();
        final Dimension viewPortSize = mainScrollPane.getViewport().getExtentSize();

        final int x = Math.max(0, (int) Math.round(bounds.getX() - (viewPortSize.getWidth() - bounds.getWidth()) / 2));
        final int y = Math.max(0, (int) Math.round(bounds.getY() - (viewPortSize.getHeight() - bounds.getHeight()) / 2));

        mainScrollPane.getViewport().setViewPosition(new Point(x, y));
      }
    }
  }


  @Nullable
  public Document getDocument() {
    return this.documents[0];
  }

  private void saveMindMapToDocument() {
    final Document document = getDocument();
    final MindMap model = this.mindMapPanel.getModel();
    if (document != null && model != null) {
      CommandProcessor.getInstance().executeCommand(getProject(), new Runnable() {
        @Override
        public void run() {
          ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
              final String packedMindMap = model.packToString();
              document.setText(packedMindMap);
            }
          });
        }
      }, null, null, document);
    }
  }

  private void loadMindMapFromDocument() {
    final MindMapDocumentEditor editorIstance = this;
    SwingUtils.safeSwing(new Runnable() {
      @Override
      public void run() {
        final Document document = getDocument();
        if (document != null) {
          CommandProcessor.getInstance().executeCommand(getProject(), new Runnable() {
            @Override
            public void run() {
              ApplicationManager.getApplication().runReadAction(new Runnable() {
                @Override
                public void run() {
                  final String documentText = document.getText();
                  safeSwing(new Runnable() {
                    @Override
                    public void run() {
                      try {
                        mindMapPanel.setModel(new MindMap(editorIstance, new StringReader(documentText)));
                      } catch (Exception ex) {
                        LOGGER.error("Can't parse MindMap text", ex);
                        editorIstance.mindMapPanel.setErrorText("Can't parse mind map content");
                      }
                    }
                  });
                }
              });
            }
          }, null, null, document);
        }
      }
    });
  }

  @Override
  public Document[] getDocuments() {
    return this.documents;
  }

  @Nonnull
  @Override
  public JComponent getComponent() {
    return this.mainScrollPane;
  }

  @Nullable
  @Override
  public JComponent getPreferredFocusedComponent() {
    return this.mainScrollPane;
  }

  @Nonnull
  @Override
  public String getName() {
    return "nb-mm-editor";
  }

  @Nonnull
  @Override
  public FileEditorState getState(@Nonnull FileEditorStateLevel fileEditorStateLevel) {
    return MindMapFileEditorState.DUMMY;
  }

  @Override
  public void setState(@Nonnull FileEditorState fileEditorState) {
  }

  @Override
  public boolean isModified() {
    return FileDocumentManager.getInstance().isFileModified(this.file);
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public void selectNotify() {

  }

  @Override
  public void deselectNotify() {

  }

  @Override
  public void addPropertyChangeListener(@Nonnull PropertyChangeListener propertyChangeListener) {

  }

  @Override
  public void removePropertyChangeListener(@Nonnull PropertyChangeListener propertyChangeListener) {

  }

  @Nullable
  @Override
  public BackgroundEditorHighlighter getBackgroundHighlighter() {
    return null;
  }

  @Nullable
  @Override
  public FileEditorLocation getCurrentLocation() {
    return null;
  }

  @Nullable
  @Override
  public StructureViewBuilder getStructureViewBuilder() {
    return null;
  }

  @Override
  public void dispose() {
      this.mindMapPanel.dispose();
  }

  @Nullable
  @Override
  public <T> T getUserData(@Nonnull Key<T> key) {
    return null;
  }

  @Override
  public <T> void putUserData(@Nonnull Key<T> key, @Nullable T t) {

  }

  @Override
  public void onNonConsumedKeyEvent(@Nonnull final MindMapPanel mindMapPanel, @Nonnull final KeyEvent keyEvent) {

  }

  @Override
  public boolean canBeDeletedSilently(@Nonnull MindMap mindMap, @Nonnull Topic topic) {
    return topic.getText().isEmpty() && topic.getExtras().isEmpty() && doesContainOnlyStandardAttributes(topic);
  }

  @Override
  public void onMindMapModelChanged(final MindMapPanel mindMapPanel) {
    saveMindMapToDocument();
  }

  @Override
  public void onMindMapModelRealigned(MindMapPanel mindMapPanel, Dimension dimension) {
    this.mainScrollPane.getViewport().revalidate();
  }

  @Override
  public void onScaledByMouse(@Nonnull final MindMapPanel source, @Nonnull final Point mousePoint, final double oldScale, final double newScale, final boolean beforeAction) {
    if (!beforeAction && Double.compare(oldScale, newScale) != 0) {
      final JViewport viewport = this.mainScrollPane.getViewport();

      final Point viewPos = viewport.getViewPosition();
      final int dx = mousePoint.x - viewPos.x;
      final int dy = mousePoint.y - viewPos.y;

      final double scaleRelation = newScale / oldScale;

      final int newMouseX = (int) (Math.round(mousePoint.x * scaleRelation));
      final int newMouseY = (int) (Math.round(mousePoint.y * scaleRelation));

      viewport.doLayout();
      viewport.setViewPosition(new Point(Math.max(0, newMouseX - dx), Math.max(0, newMouseY - dy)));
    }
  }

  @Override
  public void onEnsureVisibilityOfTopic(MindMapPanel mindMapPanel, final Topic topic) {
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
        if (orig == null) {
          return;
        }

        final int GAP = 30;

        final Rectangle bounds = orig.getBounds();
        bounds.setLocation(Math.max(0, bounds.x - GAP), Math.max(0, bounds.y - GAP));
        bounds.setSize(bounds.width + GAP * 2, bounds.height + GAP * 2);

        final JViewport viewport = mainScrollPane.getViewport();
        final Rectangle visible = viewport.getViewRect();

        if (visible.contains(bounds)) {
          return;
        }

        bounds.setLocation(bounds.x - visible.x, bounds.y - visible.y);

        viewport.scrollRectToVisible(bounds);
      }

    });

  }

  @Nullable
  public VirtualFile findRootFolderForEditedFile() {
    final Module module = IdeaUtils.findModuleForFile(this.project, this.file);

    final VirtualFile rootFolder;
    if (module == null) {
      rootFolder = this.project.getBaseDir();
    } else {
      rootFolder = IdeaUtils.findPotentialRootFolderForModule(module);
    }

    return rootFolder;
  }

  @Override
  public void onClickOnExtra(final MindMapPanel source, final int clicks, final Topic topic, final Extra<?> extra) {
    if (clicks > 1) {
      switch (extra.getType()) {
        case FILE: {
          final MMapURI fileURI = (MMapURI) extra.getValue();
          final boolean flagOpenFileLinkInSystemViewer = Boolean.parseBoolean(fileURI.getParameters().getProperty(FILELINK_ATTR_OPEN_IN_SYSTEM, "false"));

          final VirtualFile rootFolder = findRootFolderForEditedFile();
          final VirtualFile theFile = LocalFileSystem.getInstance().findFileByIoFile(fileURI.asFile(IdeaUtils.vfile2iofile(rootFolder)));

          if (theFile == null) {
            // file not found
            LOGGER.warn("Can't find FileObject for " + fileURI);
            getDialogProvider().msgError(String.format(BUNDLE.getString("MMDGraphEditor.onClickExtra.errorCanfFindFile"), fileURI.toString()));
          } else if (VfsUtilCore.isAncestor(rootFolder, theFile, false)) {
            // inside project
            if (flagOpenFileLinkInSystemViewer) {
              SelectIn.SYSTEM.open(this, theFile);
            } else {
              SelectIn.IDE.open(this, theFile);
            }
          } else {
            // outside project
            if (flagOpenFileLinkInSystemViewer) {
              SelectIn.SYSTEM.open(this, theFile);
            } else {
              SelectIn.IDE.open(this, theFile);
            }
          }
        }
        break;
        case LINK: {
          final MMapURI uri = ((ExtraLink) extra).getValue();
          if (!IdeaUtils.browseURI(uri.asURI(), isUseInsideBrowser())) { //NOI18N
            getDialogProvider().msgError(String.format(BUNDLE.getString("MMDGraphEditor.onClickOnExtra.msgCantBrowse"), uri.toString()));
          }
        }
        break;
        case NOTE: {
          editTextForTopic(topic);
        }
        break;
        case TOPIC: {
          final Topic theTopic = this.mindMapPanel.getModel().findTopicForLink((ExtraTopic) extra);
          if (theTopic == null) {
            // not presented
            getDialogProvider().msgWarn(BUNDLE.getString("MMDGraphEditor.onClickOnExtra.msgCantFindTopic"));
          } else {
            // detected
            this.mindMapPanel.focusTo(theTopic);
          }
        }
        break;
        default:
          throw new Error("Unexpected type " + extra);
      }
    }
  }

  @Nullable
  public final MindMapFacet findFacet() {
    return MindMapFacet.getInstance(IdeaUtils.findModuleForFile(this.project, this.file));
  }

  boolean isUseInsideBrowser() {
    final MindMapFacet facet = findFacet();
    return facet != null && facet.getConfiguration().isUseInsideBrowser();
  }

  boolean isMakeRelativePath() {
    final MindMapFacet facet = findFacet();
    return facet == null || facet.getConfiguration().isMakeRelativePath();
  }

  @Override
  public void onChangedSelection(final MindMapPanel mindMapPanel, final Topic[] topics) {
  }

  public DialogProvider getDialogProvider() {
    return this.panelController.getDialogProvider();
  }

  @Override
  public boolean allowedRemovingOfTopics(final MindMapPanel mindMapPanel, final Topic[] topics) {
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
      result = this.getDialogProvider().msgConfirmYesNo(BUNDLE.getString("MMDGraphEditor.allowedRemovingOfTopics,title"),
              String.format(BUNDLE.getString("MMDGraphEditor.allowedRemovingOfTopics.message"), topics.length));
    }
    return result;
  }

  private void editTextForTopic(final Topic topic) {
    final ExtraNote note = (ExtraNote) topic.getExtras().get(Extra.ExtraType.NOTE);
    final String result;
    if (note == null) {
      // create new
      result = IdeaUtils
              .editText(this.project, String.format(BUNDLE.getString("MMDGraphEditor.editTextForTopic.dlfAddNoteTitle"), Utils.makeShortTextVersion(topic.getText(), 16)), ""); //NOI18N
    } else {
      // edit
      result = IdeaUtils
              .editText(this.project, String.format(BUNDLE.getString("MMDGraphEditor.editTextForTopic.dlgEditNoteTitle"), Utils.makeShortTextVersion(topic.getText(), 16)),
                      note.getValue());
    }
    if (result != null) {
      if (result.isEmpty()) {
        topic.removeExtra(Extra.ExtraType.NOTE);
      } else {
        topic.setExtra(new ExtraNote(result));
      }
      this.mindMapPanel.invalidate();
      this.mindMapPanel.repaint();
      onMindMapModelChanged(this.mindMapPanel);
    }

    this.mainScrollPane.requestFocus();
  }

  @Override
  public void commit() {
    loadMindMapFromDocument();
  }

  @Override
  public void reset() {
    loadMindMapFromDocument();
  }

  @Override
  public void dragEnter(DropTargetDragEvent dtde) {
    this.dragAcceptableType = DnDUtils.isFileOrLinkOrText(dtde);
    if (!this.dragAcceptableType) {
      dtde.rejectDrag();
    }
  }

  @Override
  public void dragOver(DropTargetDragEvent dtde) {
    if (acceptOrRejectDragging(dtde)) {
      dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
    } else {
      dtde.rejectDrag();
    }
  }

  @Override
  public void dropActionChanged(DropTargetDragEvent dtde) {

  }

  @Override
  public void dragExit(DropTargetEvent dte) {

  }

  @Nullable
  private File extractDropFile(@Nonnull final DropTargetDropEvent dtde) throws Exception {
    try {
      java.util.List<File> files = null;
      final Object objectToDrop = dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
      if (objectToDrop instanceof DnDDragStartBean) {
        final Object wrapper = ((DnDDragStartBean) objectToDrop).getAttachedObject();
        if (wrapper instanceof TransferableWrapper) {
          files = ((TransferableWrapper) wrapper).asFileList();
        }
      } else if (objectToDrop instanceof java.util.List) {
        files = (java.util.List<File>) objectToDrop;
      }

      return files == null || files.isEmpty() ? null : files.get(0);
    } catch (UnsupportedFlavorException ex) {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void drop(final DropTargetDropEvent dtde) {
    dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

    File dropFile = null;
    String extractedLink = null;
    String extractedText = null;
    URI decodedLink = null;

    try {
      dropFile = extractDropFile(dtde);
      extractedLink = DnDUtils.extractDropLink(dtde);
      extractedText = DnDUtils.extractDropNote(dtde);
      decodedLink = null;

      if (extractedLink != null) {
        try {
          decodedLink = new URI(extractedLink);
        } catch (final URISyntaxException ex) {
          decodedLink = null;
        }
      }

      dtde.dropComplete(true);

    } catch (Exception ex) {
      LOGGER.error("Can't complete DnD operation for error", ex);
      dtde.dropComplete(false);
    }

    final AbstractElement element = this.mindMapPanel.findTopicUnderPoint(dtde.getLocation());


    if (dropFile != null) {
      decodedLink = DnDUtils.extractUrlLinkFromFile(dropFile);
      if (decodedLink == null) {
        addFileToElement(dropFile, element);
      } else {
        addURItoElement(decodedLink, element);
      }
    } else if (decodedLink != null) {
      addURItoElement(decodedLink, element);
    } else if (extractedText != null) {
      addNoteToElement(extractedText, element);
    }

  }

  private void addURItoElement(@Nonnull final URI uri, @Nullable final AbstractElement element) {
    if (element != null) {
      final Topic topic = element.getModel();
      final MMapURI mmapUri = new MMapURI(uri);
      if (topic.getExtras().containsKey(Extra.ExtraType.LINK)) {
        if (!getDialogProvider().msgConfirmOkCancel(BUNDLE.getString("MMDGraphEditor.addDataObjectLinkToElement.confirmTitle"), BUNDLE.getString("MMDGraphEditor.addDataObjectLinkToElement.confirmMsg"))) {
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
        if (!getDialogProvider().msgConfirmOkCancel(BUNDLE.getString("MMDGraphEditor.addDataObjectTextToElement.confirmTitle"), BUNDLE.getString("MMDGraphEditor.addDataObjectTextToElement.confirmMsg"))) {
          return;
        }
      }
      topic.setExtra(new ExtraNote(text));
      this.mindMapPanel.invalidate();
      this.mindMapPanel.repaint();
      onMindMapModelChanged(this.mindMapPanel);
    }
  }


  private void addFileToElement(final File file, final AbstractElement element) {
    if (element != null) {
      final Topic topic = element.getModel();

      final VirtualFile theFile = VfsUtil.findFileByIoFile(file, true);
      if (theFile != null) {
        final File rootFolder = IdeaUtils.vfile2iofile(findRootFolderForEditedFile());
        final File theFileIo = IdeaUtils.vfile2iofile(theFile);

        final MMapURI theURI = isMakeRelativePath() ?
                new MMapURI(rootFolder, theFileIo, null) :
                new MMapURI(null, theFileIo, null); //NOI18N

        if (topic.getExtras().containsKey(Extra.ExtraType.FILE)) {
          if (!getDialogProvider()
                  .msgConfirmOkCancel(BUNDLE.getString("MMDGraphEditor.addDataObjectToElement.confirmTitle"), BUNDLE.getString("MMDGraphEditor.addDataObjectToElement.confirmMsg"))) {
            return;
          }
        }

        topic.setExtra(new ExtraFile(theURI));
        this.mindMapPanel.invalidate();
        this.mindMapPanel.repaint();
        onMindMapModelChanged(this.mindMapPanel);
      } else {
        LOGGER.warn("Can't find VirtualFile for " + file);
      }
    }
  }


  protected boolean acceptOrRejectDragging(final DropTargetDragEvent dtde) {
    final int dropAction = dtde.getDropAction();

    boolean result = false;

    if (this.dragAcceptableType && (dropAction & DnDConstants.ACTION_COPY_OR_MOVE) != 0 && this.mindMapPanel.findTopicUnderPoint(dtde.getLocation()) != null) {
      result = true;
    }

    return result;
  }

  public MindMapPanel getMindMapPanel() {
    return this.mindMapPanel;
  }

  public Project getProject() {
    return this.project;
  }

  public void refreshConfiguration() {
    this.mindMapPanel.refreshConfiguration();
    this.mindMapPanel.revalidate();
    this.mindMapPanel.repaint();
  }

  @org.jetbrains.annotations.Nullable
  @Override
  public Object getData(@NonNls String s) {
    if (PlatformDataKeys.COPY_PROVIDER.is(s) || PlatformDataKeys.CUT_PROVIDER.is(s) || PlatformDataKeys.PASTE_PROVIDER.is(s)) {
      return this;
    }
    return null;
  }

  @Override
  public void performCopy(@NotNull DataContext dataContext) {
    this.mindMapPanel.copyTopicsToClipboard(false, MindMapUtils.removeSuccessorsAndDuplications(this.mindMapPanel.getSelectedTopics()));
  }

  @Override
  public boolean isCopyEnabled(@NotNull DataContext dataContext) {
    return this.mindMapPanel.hasSelectedTopics();
  }

  @Override
  public boolean isCopyVisible(@NotNull DataContext dataContext) {
    return true;
  }

  @Override
  public void performCut(@NotNull DataContext dataContext) {
    this.mindMapPanel.copyTopicsToClipboard(true, MindMapUtils.removeSuccessorsAndDuplications(this.mindMapPanel.getSelectedTopics()));
  }

  @Override
  public boolean isCutEnabled(@NotNull DataContext dataContext) {
    return this.mindMapPanel.hasSelectedTopics();
  }

  @Override
  public boolean isCutVisible(@NotNull DataContext dataContext) {
    return true;
  }

  @Override
  public void performPaste(@NotNull DataContext dataContext) {
    this.mindMapPanel.pasteTopicsFromClipboard();
  }

  @Override
  public boolean isPastePossible(@NotNull DataContext dataContext) {
    return isPasteEnabled(dataContext);
  }

  @Override
  public boolean isPasteEnabled(@NotNull DataContext dataContext) {
    boolean result = false;
    if (this.mindMapPanel.hasSelectedTopics()) {
      result = Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(MMDTopicsTransferable.MMD_DATA_FLAVOR);
    }
    return result;
  }
}
