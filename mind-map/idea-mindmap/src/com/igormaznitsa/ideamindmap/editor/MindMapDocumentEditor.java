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

import static com.igormaznitsa.ideamindmap.utils.SwingUtils.safeSwing;
import static com.igormaznitsa.mindmap.ide.commons.Misc.FILELINK_ATTR_LINE;
import static com.igormaznitsa.mindmap.ide.commons.Misc.FILELINK_ATTR_OPEN_IN_SYSTEM;
import static com.igormaznitsa.mindmap.swing.panel.utils.Utils.assertSwingDispatchThread;


import com.intellij.openapi.module.Module;
import com.igormaznitsa.ideamindmap.facet.MindMapFacet;
import com.igormaznitsa.ideamindmap.findtext.FindTextPanel;
import com.igormaznitsa.ideamindmap.findtext.FindTextScopeProvider;
import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.igormaznitsa.ideamindmap.utils.SelectIn;
import com.igormaznitsa.ideamindmap.utils.SwingUtils;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.ide.commons.DnDUtils;
import com.igormaznitsa.mindmap.ide.commons.FilePathWithLine;
import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.Extra.ExtraType;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.TopicFinder;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.MindMapPluginRegistry;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MMDTopicsTransferable;
import com.igormaznitsa.mindmap.swing.panel.MindMapListener;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractElement;
import com.igormaznitsa.mindmap.swing.panel.utils.KeyEventType;
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
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.actionSystem.ShortcutSet;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.DocumentsEditor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.xml.ui.Committable;
import com.intellij.util.xml.ui.UndoHelper;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class MindMapDocumentEditor implements AdjustmentListener, DocumentsEditor, MindMapListener, DropTargetListener, Committable, DataProvider, CopyProvider, CutProvider, PasteProvider {
  private static final long serialVersionUID = -8185230144865144686L;

  private static final Logger LOGGER = LoggerFactory.getLogger(MindMapDocumentEditor.class);
  private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("i18n/Bundle");

  private final JPanel mainPanel;
  private final JScrollPane mainScrollPane;
  private final MindMapPanel mindMapPanel;
  private final Project project;
  private final VirtualFile file;
  private final Document[] documents;
  private final MindMapPanelControllerImpl panelController;
  private final UndoHelper undoHelper;
  private final DocumentListener documentListener;
  private final FindTextPanel findTextPanel;
  private boolean dragAcceptableType = false;

  private static final Set<TopicFinder> TOPIC_FINDERS = MindMapPluginRegistry.getInstance()
      .findAllTopicFinders();

  public MindMapDocumentEditor(
      final Project project,
      final VirtualFile file
  ) {
    this.project = project;
    this.file = file;

    this.panelController = new MindMapPanelControllerImpl(this);

    this.mindMapPanel = new MindMapPanel(panelController);
    this.mindMapPanel.putTmpObject("project", project);
    this.mindMapPanel.putTmpObject("editor", this);

    this.mindMapPanel.addMindMapListener(this);
    this.mainScrollPane = new JBScrollPane(this.mindMapPanel, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); // NB! JBScrollPane sometime doesn't show scrollbars so that it replaced by swing panel

    this.mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
    this.mainScrollPane.getVerticalScrollBar().setBlockIncrement(128);
    this.mainScrollPane.getHorizontalScrollBar().setUnitIncrement(16);
    this.mainScrollPane.getHorizontalScrollBar().setBlockIncrement(128);

    this.mainScrollPane.setWheelScrollingEnabled(true);
    this.mainScrollPane.setAutoscrolls(true);

    final Document document = FileDocumentManager.getInstance().getDocument(this.file);

    this.documents = new Document[] {document};

    this.mindMapPanel.setDropTarget(new DropTarget(this.mindMapPanel, this));

    loadMindMapFromDocument();

    this.undoHelper = new UndoHelper(this.project, this);
    this.undoHelper.addWatchedDocument(getDocument());

    this.documentListener = new DocumentAdapter() {
      @Override
      public void documentChanged(DocumentEvent e) {
        loadMindMapFromDocument();
      }
    };
    this.getDocument().addDocumentListener(this.documentListener);

    DataManager.registerDataProvider(this.mainScrollPane, this);

    this.findTextPanel = new FindTextPanel(this);

    this.mainScrollPane.setColumnHeaderView(this.findTextPanel);

    this.mainScrollPane.getHorizontalScrollBar().addAdjustmentListener(this);
    this.mainScrollPane.getVerticalScrollBar().addAdjustmentListener(this);

    this.mainPanel = new JBPanel(new BorderLayout());
    this.mainPanel.add(this.mainScrollPane, BorderLayout.CENTER);
    this.mainPanel.add(this.findTextPanel, BorderLayout.NORTH);
  }

  @Nullable
  private ShortcutSet getFindAtMindMapShortcutSet() {
    ShortcutSet result = null;
    final AnAction findAction = ActionManager.getInstance().getAction("FindTextAtMindMap");
    if (findAction != null) {
      result = findAction.getShortcutSet();
    }
    return result;
  }

  @Override
  public void adjustmentValueChanged(AdjustmentEvent e) {
    this.mindMapPanel.repaint();
  }

  @Override
  public void onComponentElementsLayouted(@Nonnull final MindMapPanel mindMapPanel, @Nonnull final Graphics2D graphics2D) {
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
    if (!this.mindMapPanel.isDisposed()) {
      final MindMap model = this.mindMapPanel.getModel();
      final Document document = getDocument();
      if (document != null && model != null) {
        IdeaUtils.executeWriteAction(getProject(), document, () -> document.setText(model.packToString()));
      }
    }
  }

  private Runnable makeRunnableSetDocumenttextAction(@Nonnull final MindMapDocumentEditor editorIstance, @Nonnull final String documentText) {
    return () -> safeSwing(() -> {
      if (!mindMapPanel.isDisposed()) {
        try {
          if (documentText.isEmpty()) {
            LOGGER.warn("Detected empty text document, default mind-map will be created");
            mindMapPanel.setModel(new MindMap(true));
          } else {
            mindMapPanel.setModel(new MindMap(new StringReader(documentText)));
          }
        } catch (Exception ex) {
          LOGGER.error("Can't parse MindMap text", ex);
          editorIstance.mindMapPanel.setErrorText("Can't parse mind map content");
        }
      }
    });
  }

  private void loadMindMapFromDocument() {
    final MindMapDocumentEditor editorIstance = this;
    SwingUtils.safeSwing(() -> {
      final Document document = getDocument();

      if (document != null) {

        IdeaUtils.executeReadAction(getProject(), getDocument(), () -> safeSwing(makeRunnableSetDocumenttextAction(editorIstance, document.getText())));

        CommandProcessor.getInstance().executeCommand(getProject(), () -> ApplicationManager.getApplication().runReadAction(() -> safeSwing(makeRunnableSetDocumenttextAction(editorIstance, document.getText()))), null, null, document);
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
    return this.mainPanel;
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

  @Nonnull
  public VirtualFile getFile() {
    return this.file;
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
    try {
      this.mindMapPanel.dispose();
    } finally {
      this.getDocument().removeDocumentListener(this.documentListener);
      DataManager.removeDataProvider(this.mainScrollPane);
    }
  }

  @Nullable
  @Override
  public <T> T getUserData(@Nonnull Key<T> key) {
    return null;
  }

  @Override
  public <T> void putUserData(@Nonnull Key<T> key, @Nullable T t) {

  }

  public boolean findNext(@Nonnull final Pattern pattern, @Nonnull final FindTextScopeProvider provider) {
    Topic startTopic = null;
    if (this.mindMapPanel.hasSelectedTopics()) {
      final Topic[] selected = this.mindMapPanel.getSelectedTopics();
      startTopic = selected[selected.length - 1];
    }

    final VirtualFile baseFolder = findRootFolderForEditedFile();
    final File projectBaseFolder = baseFolder == null ? null : VfsUtil.virtualToIoFile(baseFolder);

    final Set<ExtraType> extras = EnumSet.noneOf(ExtraType.class);
    if (provider.toSearchIn(FindTextScopeProvider.SearchTextScope.IN_TOPIC_NOTES)) {
      extras.add(ExtraType.NOTE);
    }
    if (provider.toSearchIn(FindTextScopeProvider.SearchTextScope.IN_TOPIC_FILES)) {
      extras.add(ExtraType.FILE);
    }
    if (provider.toSearchIn(FindTextScopeProvider.SearchTextScope.IN_TOPIC_URI)) {
      extras.add(ExtraType.LINK);
    }
    final boolean inTopicText = provider.toSearchIn(FindTextScopeProvider.SearchTextScope.IN_TOPIC_TEXT);

    Topic found = this.mindMapPanel.getModel()
        .findNext(projectBaseFolder, startTopic, pattern, inTopicText, extras, TOPIC_FINDERS);
    if (found == null && startTopic != null) {
      found = this.mindMapPanel.getModel()
          .findNext(projectBaseFolder, null, pattern, inTopicText, extras, TOPIC_FINDERS);
    }

    if (found != null) {
      this.mindMapPanel.removeAllSelection();
      this.mindMapPanel.focusTo(found);
    }

    return found != null;
  }

  public boolean findPrev(@Nonnull final Pattern pattern, @Nonnull final FindTextScopeProvider provider) {
    Topic startTopic = null;
    if (this.mindMapPanel.hasSelectedTopics()) {
      final Topic[] selected = this.mindMapPanel.getSelectedTopics();
      startTopic = selected[0];
    }

    final VirtualFile baseFolder = findRootFolderForEditedFile();
    final File projectBaseFolder = baseFolder == null ? null : VfsUtil.virtualToIoFile(baseFolder);

    final Set<ExtraType> extras = new HashSet<>();
    if (provider.toSearchIn(FindTextScopeProvider.SearchTextScope.IN_TOPIC_NOTES)) {
      extras.add(ExtraType.NOTE);
    }
    if (provider.toSearchIn(FindTextScopeProvider.SearchTextScope.IN_TOPIC_FILES)) {
      extras.add(ExtraType.FILE);
    }
    if (provider.toSearchIn(FindTextScopeProvider.SearchTextScope.IN_TOPIC_URI)) {
      extras.add(ExtraType.LINK);
    }
    final boolean inTopicText = provider.toSearchIn(FindTextScopeProvider.SearchTextScope.IN_TOPIC_TEXT);

    Topic found = this.mindMapPanel.getModel()
        .findPrev(projectBaseFolder, startTopic, pattern, inTopicText, extras, TOPIC_FINDERS);
    if (found == null && startTopic != null) {
      found = this.mindMapPanel.getModel()
          .findPrev(projectBaseFolder, null, pattern, inTopicText, extras, TOPIC_FINDERS);
    }

    if (found != null) {
      this.mindMapPanel.removeAllSelection();
      this.mindMapPanel.focusTo(found);
    }

    return found != null;
  }


  @Override
  public void onNonConsumedKeyEvent(@Nonnull final MindMapPanel source, @Nonnull final KeyEvent e, @Nonnull final KeyEventType type) {
    if (type == KeyEventType.PRESSED) {
      if (e.getModifiersEx() == 0) {
        switch (e.getKeyCode()) {
          case KeyEvent.VK_UP:
          case KeyEvent.VK_LEFT:
          case KeyEvent.VK_RIGHT:
          case KeyEvent.VK_DOWN: {
            e.consume();
          }
          break;
        }
      }
    }

    boolean activated = false;
    final ShortcutSet findAtMindMap = getFindAtMindMapShortcutSet();
    if (findAtMindMap != null) {
      final KeyStroke eventStroke = KeyStroke.getKeyStrokeForEvent(e);
      for (final Shortcut c : findAtMindMap.getShortcuts()) {
        if (c instanceof KeyboardShortcut) {
          final KeyboardShortcut keyboardShortcut = (KeyboardShortcut) c;
          final KeyStroke firstKeyStroke = keyboardShortcut.getFirstKeyStroke();
          final KeyStroke secondKeyStroke = keyboardShortcut.getSecondKeyStroke();

          if (firstKeyStroke != null && firstKeyStroke.getModifiers() == eventStroke.getModifiers() && firstKeyStroke.getKeyCode() == eventStroke.getKeyCode() && firstKeyStroke.getKeyChar() == eventStroke.getKeyChar()) {
            activated = true;
            break;
          }
          if (secondKeyStroke != null && secondKeyStroke.getModifiers() == eventStroke.getModifiers() && secondKeyStroke.getKeyCode() == eventStroke.getKeyCode() && secondKeyStroke.getKeyChar() == eventStroke.getKeyChar()) {
            activated = true;
            break;
          }
        }
      }
    }

    if (activated) {
      e.consume();
      activateTextSearchPanel();
    }

    if (!e.isConsumed() && e.getModifiersEx() == 0 && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
      ApplicationManager.getApplication().invokeLater(findTextPanel::deactivate);
    }
  }

  public void activateTextSearchPanel() {
    ApplicationManager.getApplication().invokeLater(findTextPanel::activate);
  }

  @Override
  public void onMindMapModelChanged(@Nonnull final MindMapPanel mindMapPanel, final boolean saveToHistory) {
    if (saveToHistory) {
      saveMindMapToDocument();
    }
  }

  @Override
  public void onMindMapModelRealigned(@Nonnull MindMapPanel mindMapPanel, @Nonnull Dimension dimension) {
    this.mainScrollPane.revalidate();
    this.mainScrollPane.repaint();
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
      this.mainScrollPane.setViewportView(source);

      final JViewport viewport = this.mainScrollPane.getViewport();

      final Rectangle viewPos = viewport.getViewRect();

      final Dimension size = source.getSize();
      final Dimension extentSize = viewport.getExtentSize();

      if (extentSize.width < size.width || extentSize.height < size.height) {

        final int dx = mousePoint.x - viewPos.x;
        final int dy = mousePoint.y - viewPos.y;

        final double scaleX = newSize.getWidth() / oldSize.getWidth();
        final double scaleY = newSize.getHeight() / oldSize.getHeight();

        final int newMouseX = (int) (Math.round(mousePoint.x * scaleX));
        final int newMouseY = (int) (Math.round(mousePoint.y * scaleY));

        viewPos.x = Math.max(0, newMouseX - dx);
        viewPos.y = Math.max(0, newMouseY - dy);

      } else {
        viewPos.x = 0;
        viewPos.y = 0;
      }
      source.scrollRectToVisible(viewPos);

      this.mainScrollPane.repaint();
    }
  }

  @Override
  public void onEnsureVisibilityOfTopic(@Nonnull final MindMapPanel mindMapPanel, @Nonnull final Topic topic) {
    SwingUtilities.invokeLater(() -> {

      mindMapPanel.doLayout();

      final AbstractElement element = (AbstractElement) topic.getPayload();
      if (element == null) {
        return;
      }

      final Rectangle2D orig = element.getBounds();
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

      mainScrollPane.revalidate();
      SwingUtilities.invokeLater(() -> {
        viewport.scrollRectToVisible(bounds);
        mainScrollPane.repaint();
      });
    });

  }

  @Nullable
  public VirtualFile findRootFolderForEditedFile() {
    final Module module = IdeaUtils.findModuleForFile(this.project, this.file);
    final VirtualFile rootFolder;

    if (module == null || isUseProjectBaseFolderAsRoot()) {
      final VirtualFile baseDir = this.project.getBaseDir();
      if (module == null) {
        rootFolder = baseDir;
      } else {
        final VirtualFile mavenProjectRoot = IdeaUtils.findMavenProjectRootForFile(this.project, this.file);
        rootFolder = mavenProjectRoot == null ? baseDir : mavenProjectRoot;
      }
    } else {
      rootFolder = IdeaUtils.findPotentialRootFolderForModule(module);
    }

    return rootFolder;
  }

  private void processClicksExtraFile(@Nonnull final Topic topic, @Nonnull final ExtraFile extra) {
    final MMapURI fileURI = extra.getValue();
    final boolean flagOpenFileLinkInSystemViewer = Boolean.parseBoolean(fileURI.getParameters().getProperty(FILELINK_ATTR_OPEN_IN_SYSTEM, "false"));
    final int lineNumber = FilePathWithLine.strToLine(fileURI.getParameters().getProperty(FILELINK_ATTR_LINE, null));

    final VirtualFile rootFolder = findRootFolderForEditedFile();
    final VirtualFile theFile = LocalFileSystem.getInstance().findFileByIoFile(fileURI.asFile(IdeaUtils.vfile2iofile(rootFolder)));

    if (theFile == null) {
      // file not found
      LOGGER.warn("Can't find FileObject for " + fileURI);
      getDialogProvider().msgError(null, String.format(BUNDLE.getString("MMDGraphEditor.onClickExtra.errorCanfFindFile"), fileURI));
    } else if (VfsUtilCore.isAncestor(rootFolder, theFile, false)) {
      // inside project
      if (flagOpenFileLinkInSystemViewer) {
        SelectIn.SYSTEM.open(this, theFile, -1);
      } else {
        SelectIn.IDE.open(this, theFile, lineNumber);
      }
    } else {
      // outside project
      if (flagOpenFileLinkInSystemViewer) {
        SelectIn.SYSTEM.open(this, theFile, -1);
      } else {
        SelectIn.IDE.open(this, theFile, lineNumber);
      }
    }
  }
  
  private void processClicksExtraLink(@Nonnull final Topic topic, @Nonnull final ExtraLink extra) {
      final MMapURI uri = extra.getValue();
      if (!IdeaUtils.browseURI(uri.asURI(), isUseInsideBrowser())) { //NOI18N
          getDialogProvider().msgError(null, String.format(BUNDLE.getString("MMDGraphEditor.onClickOnExtra.msgCantBrowse"), uri));
      }
  }
  
  @Override
  public void onClickOnExtra(@Nonnull final MindMapPanel source, final int modifiers, final int clicks, @Nonnull final Topic topic, @Nonnull final Extra<?> extra) {
    if (clicks > 1) {
      switch (extra.getType()) {
        case FILE: {
          processClicksExtraFile(topic, (ExtraFile)extra);
        }
        break;
        case LINK: {
          processClicksExtraLink(topic, (ExtraLink) extra);
        }
        break;
        case NOTE: {
            this.panelController.editTextForTopic(topic);
        }
        break;
        case TOPIC: {
          final Topic theTopic = this.mindMapPanel.getModel().findTopicForLink((ExtraTopic) extra);
          if (theTopic == null) {
            // not presented
            getDialogProvider().msgWarn(null, BUNDLE.getString("MMDGraphEditor.onClickOnExtra.msgCantFindTopic"));
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

  boolean isUseProjectBaseFolderAsRoot() {
    final MindMapFacet facet = findFacet();
    return facet == null || facet.getConfiguration().isUseProjectBaseFolderAsRoot();
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
  public void onChangedSelection(@Nonnull final MindMapPanel mindMapPanel, @Nonnull @MustNotContainNull final Topic[] topics) {
  }

  public DialogProvider getDialogProvider() {
    return this.panelController.getDialogProvider();
  }

  @Override
  public boolean allowedRemovingOfTopics(@Nonnull final MindMapPanel mindMapPanel, @Nonnull @MustNotContainNull final Topic[] topics) {
    boolean topicsNotImportant = true;

    for (final Topic t : topics) {
      topicsNotImportant &= this.panelController.canTopicBeDeleted(this.mindMapPanel, t);
      if (!topicsNotImportant) {
        break;
      }
    }

    final boolean result;

    if (topicsNotImportant) {
      result = true;
    } else {
      result = this.getDialogProvider().msgConfirmYesNo(null, BUNDLE.getString("MMDGraphEditor.allowedRemovingOfTopics,title"),
          String.format(BUNDLE.getString("MMDGraphEditor.allowedRemovingOfTopics.message"), topics.length));
    }
    return result;
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

  @SuppressWarnings("unchecked")
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

  @Override
  public void drop(final DropTargetDropEvent dtde) {
    dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

    File dropFile = null;
    String extractedLink;
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
      if (DnDUtils.isUriString(extractedText)) {
        try {
          final URI uri = new URI(extractedText);
          addURItoElement(uri, element);
        } catch (URISyntaxException exx) {
          addNoteToElement(extractedText, element);
        }
      } else {
        addNoteToElement(extractedText, element);
      }
    }
  }

  private void addURItoElement(@Nonnull final URI uri, @Nullable final AbstractElement element) {
    if (element != null) {
      final Topic topic = element.getModel();
      final MMapURI mmapUri = new MMapURI(uri);
      if (topic.getExtras().containsKey(ExtraType.LINK)) {
        if (!getDialogProvider().msgConfirmOkCancel(null, BUNDLE.getString("MMDGraphEditor.addDataObjectLinkToElement.confirmTitle"), BUNDLE.getString("MMDGraphEditor.addDataObjectLinkToElement.confirmMsg"))) {
          return;
        }
      }
      topic.setExtra(new ExtraLink(mmapUri));
      this.mindMapPanel.doLayout();
      onMindMapModelChanged(this.mindMapPanel, true);
    }
  }

  private void addNoteToElement(@Nonnull final String text, @Nullable final AbstractElement element) {
    if (element != null) {
      final Topic topic = element.getModel();
      if (topic.getExtras().containsKey(ExtraType.NOTE)) {
        if (!getDialogProvider().msgConfirmOkCancel(null, BUNDLE.getString("MMDGraphEditor.addDataObjectTextToElement.confirmTitle"), BUNDLE.getString("MMDGraphEditor.addDataObjectTextToElement.confirmMsg"))) {
          return;
        }
      }
      topic.setExtra(new ExtraNote(text));
      this.mindMapPanel.doLayout();
      onMindMapModelChanged(this.mindMapPanel, true);
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

        if (topic.getExtras().containsKey(ExtraType.FILE)) {
          if (!getDialogProvider()
              .msgConfirmOkCancel(null, BUNDLE.getString("MMDGraphEditor.addDataObjectToElement.confirmTitle"), BUNDLE.getString("MMDGraphEditor.addDataObjectToElement.confirmMsg"))) {
            return;
          }
        }

        topic.setExtra(new ExtraFile(theURI));
        this.mindMapPanel.doLayout();
        onMindMapModelChanged(this.mindMapPanel, true);
      } else {
        LOGGER.warn("Can't find VirtualFile for " + file);
      }
    }
  }


  protected boolean acceptOrRejectDragging(final DropTargetDragEvent dtde) {
    final int dropAction = dtde.getDropAction();

    boolean result = this.dragAcceptableType && (dropAction & DnDConstants.ACTION_COPY_OR_MOVE) != 0 && this.mindMapPanel.findTopicUnderPoint(dtde.getLocation()) != null;

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
    this.mindMapPanel.doLayout();
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
      result = Utils.isDataFlavorAvailable(Toolkit.getDefaultToolkit().getSystemClipboard(), MMDTopicsTransferable.MMD_DATA_FLAVOR);
    }
    return result;
  }
}
