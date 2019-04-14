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
package com.igormaznitsa.nbmindmap.nb.editor;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.ide.commons.DnDUtils;
import com.igormaznitsa.mindmap.ide.commons.FilePathWithLine;
import static com.igormaznitsa.mindmap.ide.commons.FilePathWithLine.strToLine;
import com.igormaznitsa.mindmap.ide.commons.Misc;
import static com.igormaznitsa.mindmap.ide.commons.Misc.FILELINK_ATTR_LINE;
import static com.igormaznitsa.mindmap.ide.commons.Misc.FILELINK_ATTR_OPEN_IN_SYSTEM;
import com.igormaznitsa.mindmap.model.*;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.api.CustomJob;
import com.igormaznitsa.mindmap.plugins.api.PopUpMenuItemPlugin;
import com.igormaznitsa.mindmap.plugins.misc.AboutPlugin;
import com.igormaznitsa.mindmap.plugins.misc.OptionsPlugin;
import com.igormaznitsa.mindmap.plugins.processors.ExtraFilePlugin;
import com.igormaznitsa.mindmap.plugins.processors.ExtraJumpPlugin;
import com.igormaznitsa.mindmap.plugins.processors.ExtraNotePlugin;
import com.igormaznitsa.mindmap.plugins.processors.ExtraURIPlugin;
import com.igormaznitsa.mindmap.plugins.tools.ChangeColorPlugin;
import com.igormaznitsa.mindmap.print.MMDPrint;
import com.igormaznitsa.mindmap.print.MMDPrintOptions;
import com.igormaznitsa.mindmap.print.PrintableObject;
import com.igormaznitsa.mindmap.swing.panel.*;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractElement;
import com.igormaznitsa.mindmap.swing.panel.ui.ElementPart;
import com.igormaznitsa.mindmap.swing.panel.utils.KeyEventType;
import com.igormaznitsa.mindmap.swing.panel.utils.MindMapUtils;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import com.igormaznitsa.nbmindmap.nb.print.PrintPageAdapter;
import com.igormaznitsa.nbmindmap.nb.swing.*;
import com.igormaznitsa.nbmindmap.utils.DialogProviderManager;
import com.igormaznitsa.nbmindmap.utils.NbUtils;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.spi.print.PrintPage;
import org.netbeans.spi.print.PrintProvider;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.PasteAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.WeakSet;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.ClipboardEvent;
import org.openide.util.datatransfer.ClipboardListener;
import org.openide.util.datatransfer.ExClipboard;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

import static com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute.*;
import static com.igormaznitsa.mindmap.swing.panel.utils.Utils.assertSwingDispatchThread;
import java.util.regex.Pattern;
import org.openide.actions.FindAction;
import org.openide.cookies.LineCookie;
import org.openide.text.Line;
import org.openide.windows.Mode;
import static org.openide.windows.TopComponent.PERSISTENCE_NEVER;
import org.openide.windows.WindowManager;

@MultiViewElement.Registration(
        displayName = "#MMDGraphEditor.displayName",
        mimeType = MMDDataObject.MIME,
        persistenceType = PERSISTENCE_NEVER,
        iconBase = "com/igormaznitsa/nbmindmap/icons/logo/logo16.png",
        preferredID = MMDGraphEditor.ID,
        position = 1
)
public final class MMDGraphEditor extends CloneableEditor implements AdjustmentListener, MindMapController, PrintProvider, MultiViewElement, MindMapListener, DropTargetListener, MindMapPanelController, FlavorListener, ClipboardListener {

  private static final long serialVersionUID = -8776707243607267446L;

  private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle");

  private static final Logger LOGGER = LoggerFactory.getLogger(MMDGraphEditor.class);
  private static final UIComponentFactory UI_COMPO_FACTORY = UIComponentFactoryProvider.findInstance();

  public static final String ID = "mmd-graph-editor"; //NOI18N

  private volatile boolean rootToCentre = true;

  private MultiViewElementCallback callback;
  private final MMDEditorSupport editorSupport;

  private final JScrollPane mainScrollPane;
  private final MindMapPanel mindMapPanel;
  private final FindTextPanel findTextPanel;

  private boolean dragAcceptableType = false;

  private final JToolBar toolBar = UI_COMPO_FACTORY.makeToolBar();

  private static final WeakSet<MMDGraphEditor> ALL_EDITORS = new WeakSet<MMDGraphEditor>();

  private static final FindAction findAction = new FindAction() {
    @Override
    public void actionPerformed(ActionEvent ae) {
      final Mode editor = WindowManager.getDefault().findMode("editor");
      if (editor != null) {
        final TopComponent component = editor.getSelectedTopComponent();
        if (component instanceof MMDGraphEditor) {
          ((MMDGraphEditor) component).findTextPanel.activate();
        }
      }
    }

    @Override
    public boolean isEnabled() {
      return true;
    }
  };

  private final Action actionCopy = new AbstractAction() {
    private static final long serialVersionUID = 935382113400815225L;

    @Override
    public void actionPerformed(@Nonnull final ActionEvent e) {
      mindMapPanel.copyTopicsToClipboard(false, MindMapUtils.removeSuccessorsAndDuplications(mindMapPanel.getSelectedTopics()));
    }

    @Override
    public boolean isEnabled() {
      return mindMapPanel.hasSelectedTopics();
    }
  };

  private final Action actionCut = new AbstractAction() {
    private static final long serialVersionUID = 935382113400815225L;

    @Override
    public void actionPerformed(@Nonnull final ActionEvent e) {
      mindMapPanel.copyTopicsToClipboard(true, MindMapUtils.removeSuccessorsAndDuplications(mindMapPanel.getSelectedTopics()));
    }

    @Override
    public boolean isEnabled() {
      return mindMapPanel.hasSelectedTopics();
    }
  };

  private final Action actionPaste = new AbstractAction() {
    private static final long serialVersionUID = -5644390861803492172L;

    @Override
    public void actionPerformed(@Nonnull final ActionEvent e) {
      mindMapPanel.pasteTopicsFromClipboard();
    }
  };

  public MMDGraphEditor() {
    this(Lookup.getDefault().lookup(MMDEditorSupport.class));
  }

  public MMDGraphEditor(final MMDEditorSupport support) {
    super(support);

    this.putClientProperty("print.size", new Dimension(500, 500));

    this.editorSupport = support;

    this.mainScrollPane = UI_COMPO_FACTORY.makeScrollPane();

    this.mindMapPanel = new MindMapPanel(this);
    this.mindMapPanel.addMindMapListener(this);

    this.mindMapPanel.setDropTarget(new DropTarget(this.mindMapPanel, this));

    this.mainScrollPane.getHorizontalScrollBar().setBlockIncrement(128);
    this.mainScrollPane.getHorizontalScrollBar().setUnitIncrement(16);
    this.mainScrollPane.getVerticalScrollBar().setBlockIncrement(128);
    this.mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);

    this.mainScrollPane.setViewportView(this.mindMapPanel);
    this.mainScrollPane.setWheelScrollingEnabled(true);
    this.mainScrollPane.setAutoscrolls(true);

    this.setLayout(new BorderLayout(0, 0));
    this.add(this.mainScrollPane, BorderLayout.CENTER);

    this.findTextPanel = new FindTextPanel(this, "");
    this.add(this.findTextPanel, BorderLayout.SOUTH);

    this.mindMapPanel.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(final ComponentEvent e) {
        processEditorResizing();
      }
    });
    updateName();

    synchronized (ALL_EDITORS) {
      ALL_EDITORS.add(this);
    }

    this.actionCut.setEnabled(false);
    this.actionCopy.setEnabled(false);
    this.actionPaste.setEnabled(false);

    this.mainScrollPane.getHorizontalScrollBar().addAdjustmentListener(this);
    this.mainScrollPane.getVerticalScrollBar().addAdjustmentListener(this);

  }

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

  public boolean findPrev(@Nonnull final Pattern pattern, @Nonnull final FindTextScopeProvider provider) {
    Topic startTopic = null;
    if (this.mindMapPanel.hasSelectedTopics()) {
      final Topic[] selected = this.mindMapPanel.getSelectedTopics();
      startTopic = selected[0];
    }

    final File projectBaseFolder = this.getProjectFolder();

    final Set<Extra.ExtraType> extras = new HashSet<Extra.ExtraType>();
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
  public void adjustmentValueChanged(@Nonnull final AdjustmentEvent e) {
    this.mindMapPanel.repaint();
  }

  private void registerCustomCCPActions(@Nonnull final JComponent component) {
    final ActionMap actionMap = component.getActionMap();
    actionMap.put(DefaultEditorKit.cutAction, this.actionCut);
    actionMap.put(DefaultEditorKit.copyAction, this.actionCopy);
    actionMap.put(DefaultEditorKit.pasteAction, this.actionPaste);
    actionMap.put(FindAction.class.getName(), findAction);
  }

  private void registerAsClipboardListener() {
    final Clipboard clipboard = NbUtils.findClipboard();
    if (clipboard instanceof ExClipboard) {
      ((ExClipboard) clipboard).addClipboardListener(this);
    } else {
      clipboard.addFlavorListener(this);
    }
    processClipboardChange(clipboard);
  }

  private void unregisterFromClipboardListeners() {
    final Clipboard clipboard = NbUtils.findClipboard();
    if (clipboard instanceof ExClipboard) {
      ((ExClipboard) clipboard).removeClipboardListener(this);
    } else {
      clipboard.removeFlavorListener(this);
    }
  }

  private void restoreSystemCCPActions(@Nonnull final JComponent component) {
    final ActionMap actionMap = component.getActionMap();
    actionMap.put(DefaultEditorKit.copyAction, SystemAction.get(CopyAction.class));
    actionMap.put(DefaultEditorKit.cutAction, SystemAction.get(CutAction.class));
    actionMap.put(DefaultEditorKit.pasteAction, SystemAction.get(PasteAction.class));
  }

  @Override
  public void flavorsChanged(@Nonnull final FlavorEvent e) {
    processClipboardChange((Clipboard) e.getSource());
  }

  @Override
  public void clipboardChanged(@Nonnull final ClipboardEvent ev) {
    processClipboardChange(ev.getClipboard());
  }

  private boolean processClipboardChange(@Nonnull final Clipboard clipboard) {
    final boolean result = Utils.isDataFlavorAvailable(clipboard, MMDTopicsTransferable.MMD_DATA_FLAVOR);
    Utils.safeSwingCall(new Runnable() {
      @Override
      public void run() {
        actionPaste.setEnabled(mindMapPanel.hasSelectedTopics() && result);
      }
    });
    return result;
  }

  public boolean isPanelDisposed() {
    return this.mindMapPanel.isDisposed();
  }

  @Override
  public JComponent getVisualRepresentation() {
    return this;
  }

  @Override
  public void componentActivated() {
    this.editorSupport.onEditorActivated();

    if (this.rootToCentre) {
      this.rootToCentre = false;
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          final Topic root = mindMapPanel.getModel().getRoot();
          if (mindMapPanel.hasSelectedTopics()) {
            topicToCentre(mindMapPanel.getFirstSelected());
          } else if (root != null) {
            mindMapPanel.select(root, false);
            mindMapPanel.getModel().resetPayload();
            topicToCentre(root);
          }
        }
      });
    }

    registerCustomCCPActions(this);
    registerAsClipboardListener();

    super.componentActivated();
  }

  @Override
  public void componentClosed() {
    try {
      this.mindMapPanel.dispose();
      LOGGER.info("MMD Editor is disposed : " + this.mindMapPanel.toString());
    } finally {

      final Clipboard clipboard = NbUtils.findClipboard();
      if (clipboard instanceof ExClipboard) {
        ((ExClipboard) clipboard).removeClipboardListener(this);
      } else {
        clipboard.removeFlavorListener(this);
      }

      super.componentClosed();
    }
  }

  @Override
  public void componentOpened() {
    super.componentOpened();
  }

  @Override
  public void componentShowing() {
    updateModel();
  }

  private void copyNameToCallbackTopComponent() {
    final MultiViewElementCallback c = this.callback;
    if (c != null) {
      final TopComponent tc = c.getTopComponent();
      if (tc != null) {
        tc.setHtmlDisplayName(this.getHtmlDisplayName());
        tc.setDisplayName(this.getDisplayName());
        tc.setName(this.getName());
        tc.setToolTipText(this.getToolTipText());
      }
    }
    if (this.editorSupport != null) {
      this.editorSupport.updateTitles();
    }
  }

  @Override
  public void componentDeactivated() {
    restoreSystemCCPActions(this);
    unregisterFromClipboardListeners();
    super.componentDeactivated();
  }

  @Override
  public void componentHidden() {
    super.componentHidden();
  }

  private void updateModel() {
    if (!this.mindMapPanel.isDisposed()) {
      this.mindMapPanel.hideEditor();

      final String text = this.editorSupport.getDocumentText();
      if (text == null) {
        this.mindMapPanel.setErrorText(BUNDLE.getString("MMDGraphEditor.updateModel.cantLoadDocument"));
      } else {
        try {
          if (text.isEmpty()) {
            LOGGER.warn("Detected empty text document as mind map, the default mind map will be created");
            this.mindMapPanel.setModel(new MindMap(this, true), false);
          } else {
            this.mindMapPanel.setModel(new MindMap(this, new StringReader(text)), false);
          }
        } catch (IllegalArgumentException ex) {
          LOGGER.warn("Can't detect mind map"); //NOI18N
          this.mindMapPanel.setErrorText(BUNDLE.getString("MMDGraphEditor.updateModel.cantDetectMMap"));
        } catch (IOException ex) {
          LOGGER.error("Can't parse mind map text", ex); //NOI18N
          this.mindMapPanel.setErrorText(BUNDLE.getString("MMDGraphEditor.updateModel.cantParseDoc"));
        }
      }
    }
  }

  @Override
  public void setMultiViewCallback(final MultiViewElementCallback callback) {
    this.callback = callback;
    updateName();
    copyNameToCallbackTopComponent();
  }

  @Override
  public boolean allowedRemovingOfTopics(final MindMapPanel source, final Topic[] topics) {
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
      result = NbUtils.msgConfirmYesNo(null, BUNDLE.getString("MMDGraphEditor.allowedRemovingOfTopics,title"), String.format(BUNDLE.getString("MMDGraphEditor.allowedRemovingOfTopics.message"), topics.length));
    }
    return result;
  }

  @Override
  public void onMindMapModelChanged(final MindMapPanel source, final boolean saveToHistory) {
    try {
      final StringWriter writer = new StringWriter(16384);

      final MindMap theMap = this.mindMapPanel.getModel();
      MindMapUtils.removeCollapseAttributeFromTopicsWithoutChildren(theMap);
      theMap.write(writer);
      if (saveToHistory) {
        this.editorSupport.replaceDocumentText(writer.toString());
        this.editorSupport.getDataObject().setModified(true);
      }
    } catch (Exception ex) {
      LOGGER.error("Can't get document text", ex); //NOI18N
    } finally {
      copyNameToCallbackTopComponent();
    }
  }

  @Override
  public void onComponentElementsLayouted(@Nonnull final MindMapPanel source, @Nonnull final Graphics2D g) {
  }

  @Override
  public void onMindMapModelRealigned(final MindMapPanel source, final Dimension coveredAreaSize) {
    this.mainScrollPane.getViewport().revalidate();
  }

  public boolean topicToCentre(@Nullable Topic topic) {
    boolean result = false;

    assertSwingDispatchThread();

    if (topic != null) {
      // to make it sure that topic is from the same model
      topic = this.mindMapPanel.getModel().findForPositionPath(topic.getPositionPath());
      if (topic != null) {
        AbstractElement element = (AbstractElement) topic.getPayload();

        if (element == null && this.mindMapPanel.updateElementsAndSizeForCurrentGraphics(true, true)) {
          element = (AbstractElement) topic.getPayload();
          this.mainScrollPane.getViewport().doLayout();
        }

        if (element != null) {
          final Rectangle2D bounds = element.getBounds();
          final Dimension viewPortSize = mainScrollPane.getViewport().getExtentSize();

          final int x = Math.max(0, (int) Math.round(bounds.getX() - (viewPortSize.getWidth() - bounds.getWidth()) / 2));
          final int y = Math.max(0, (int) Math.round(bounds.getY() - (viewPortSize.getHeight() - bounds.getHeight()) / 2));

          this.mainScrollPane.getViewport().setViewPosition(new Point(x, y));

          result = true;
        }
      }
    }

    return result;
  }

  @Override
  public void onTopicCollapsatorClick(@Nonnull final MindMapPanel source, @Nonnull final Topic topic, final boolean beforeAction) {
    if (!beforeAction) {
      this.mindMapPanel.getModel().resetPayload();
      topicToCentre(topic);
    }
  }

  @Override
  public void onNonConsumedKeyEvent(@Nonnull final MindMapPanel source, @Nonnull final KeyEvent e, @Nonnull final KeyEventType type) {
    if (type == KeyEventType.PRESSED) {
      if (e.getModifiers() == 0) {
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

    if (!e.isConsumed() && e.getModifiers() == 0 && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
      this.findTextPanel.deactivate();
    }
  }

  @Override
  public void onEnsureVisibilityOfTopic(@Nonnull final MindMapPanel source, @Nonnull final Topic topic) {
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
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        viewport.scrollRectToVisible(bounds);
        mainScrollPane.repaint();
      }
    });
  }

  @Override
  @SuppressWarnings("unchecked")
  public void onClickOnExtra(final MindMapPanel source, final int modifiers, final int clicks, final Topic topic, final Extra<?> extra) {
    if (clicks > 1) {
      switch (extra.getType()) {
        case FILE: {
          final FileObject fileObj;
          final MMapURI uri = (MMapURI) extra.getValue();
          final File theFile = FileUtil.normalizeFile(uri.asFile(this.editorSupport.getProjectDirectory()));
          try {
            fileObj = FileUtil.toFileObject(theFile);
            if (fileObj == null) {
              LOGGER.warn("Can't find FileObject for " + theFile);
              if (theFile.exists()) {
                NbUtils.openInSystemViewer(null, theFile);
              } else {
                NbUtils.msgError(null, String.format(BUNDLE.getString("MMDGraphEditor.onClickExtra.errorCanfFindFile"), theFile.getAbsolutePath()));
              }
              return;
            }
          } catch (Exception ex) {
            LOGGER.error("onClickExtra#FILE", ex); //NOI18N
            NbUtils.msgError(null, String.format(BUNDLE.getString("MMDGraphEditor.onClickOnExtra.msgWrongFilePath"), uri.toString()));
            return;
          }

          try {
            final int line = FilePathWithLine.strToLine(uri.getParameters().getProperty(FILELINK_ATTR_LINE, null));
            if (Boolean.parseBoolean(uri.getParameters().getProperty(FILELINK_ATTR_OPEN_IN_SYSTEM, "false"))) { //NOI18N
              NbUtils.openInSystemViewer(null, theFile);
            } else {
              Project projectOwner;
              if (fileObj.isFolder()) {
                projectOwner = FileOwnerQuery.getOwner(fileObj);
                if (projectOwner != null) {
                  final DataObject dataObj = DataObject.find(fileObj);
                  if (dataObj instanceof DataFolder) {
                    final ProjectManager manager = ProjectManager.getDefault();

                    if (manager.isProject(fileObj)) {
                      final Project project = manager.findProject(fileObj);
                      final OpenProjects openProjects = OpenProjects.getDefault();

                      NbUtils.SelectIn browserType = NbUtils.SelectIn.PROJECTS;

                      if (!openProjects.isProjectOpen(project)) {
                        openProjects.open(new Project[]{project}, false);
                      } else {
                        final FileObject mapProjectFolder = getProjectFolderAsFileObject();
                        if (mapProjectFolder != null && mapProjectFolder.equals(fileObj)) {
                          browserType = NbUtils.SelectIn.FAVORITES;
                        }
                      }

                      if (browserType.select(this, project.getProjectDirectory())) {
                        return;
                      }
                    }
                  }
                }

                NbUtils.SelectIn selector = NbUtils.SelectIn.FAVORITES;

                final ProjectManager manager = ProjectManager.getDefault();
                final OpenProjects openManager = OpenProjects.getDefault();
                if (manager.isProject(fileObj)) {
                  final Project project = manager.findProject(fileObj);
                  if (project != null && !openManager.isProjectOpen(project)) {
                    openManager.open(new Project[]{project}, false, true);
                  }
                } else if (projectOwner != null) {
                  openManager.open(new Project[]{projectOwner}, false);

                  if (!NbUtils.isInProjectKnowledgeFolder(projectOwner, fileObj)) {
                    if (NbUtils.isFileInProjectScope(projectOwner, fileObj)) {
                      selector = NbUtils.SelectIn.PROJECTS;
                    } else if (FileUtil.isParentOf(projectOwner.getProjectDirectory(), fileObj)) {
                      selector = NbUtils.SelectIn.FILES;
                    }
                  }
                }

                if (!selector.select(this, fileObj)) {
                  NbUtils.openInSystemViewer(null, theFile);
                }
              } else {
                final DataObject dobj = DataObject.find(fileObj);
                final Openable openable = dobj.getLookup().lookup(Openable.class);
                if (openable != null) {
                  openable.open();
                }

                if (line > 0) {
                  final LineCookie lineCookie = dobj.getCookie(LineCookie.class);
                  if (lineCookie != null) {
                    final Line theLine = lineCookie.getLineSet().getOriginal(line - 1);
                    if (theLine != null) {
                      theLine.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                    }
                  }
                }

                NbUtils.SelectIn.PROJECTS.select(this, fileObj);
              }
            }
          } catch (Exception ex) {
            LOGGER.error("Cant't find or open data object", ex); //NOI18N
            NbUtils.msgError(null, String.format(BUNDLE.getString("MMDGraphEditor.onClickExtra.cantFindFileObj"), uri.toString()));
          }
        }
        break;
        case LINK: {
          final MMapURI uri = ((ExtraLink) extra).getValue();
          if (!NbUtils.browseURI(uri.asURI(), NbUtils.getPreferences().getBoolean("useInsideBrowser", false))) { //NOI18N
            NbUtils.msgError(null, String.format(BUNDLE.getString("MMDGraphEditor.onClickOnExtra.msgCantBrowse"), uri.toString()));
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
            NbUtils.msgWarn(null, BUNDLE.getString("MMDGraphEditor.onClickOnExtra.msgCantFindTopic"));
          } else {
            // detected
            this.mindMapPanel.focusTo(theTopic);
          }
        }
        break;
        default:
          throw new Error("Unexpected type " + extra.getType()); //NOI18N
      }
    }
  }

  @Override
  public void onChangedSelection(final MindMapPanel source, final Topic[] currentSelectedTopics) {
    this.actionCopy.setEnabled(currentSelectedTopics.length != 0);
    this.actionCut.setEnabled(currentSelectedTopics.length != 0);
    this.actionPaste.setEnabled(currentSelectedTopics.length != 0 && Utils.isDataFlavorAvailable(NbUtils.findClipboard(), MMDTopicsTransferable.MMD_DATA_FLAVOR));
  }

  private void processEditorResizing() {
    this.mindMapPanel.doLayout();
    this.mindMapPanel.updateEditorAfterResizing();
  }

  public void updateView() {
    if (SwingUtilities.isEventDispatchThread()) {
      this.updateModel();
    } else {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          updateModel();
        }
      });
    }
  }

  @Override
  public void dragEnter(DropTargetDragEvent dtde) {
    this.dragAcceptableType = checkDragType(dtde);
    if (!this.dragAcceptableType) {
      dtde.rejectDrag();
    }
  }

  @Override
  public void dragOver(final DropTargetDragEvent dtde) {
    if (acceptOrRejectDragging(dtde)) {
      dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
    } else {
      dtde.rejectDrag();
    }
  }

  @Override
  public Image getIcon() {
    return ImageUtilities.loadImage("com/igormaznitsa/nbmindmap/icons/logo/logo16.png"); //NOI18N
  }

  @Override
  public void dropActionChanged(DropTargetDragEvent dtde) {
  }

  @Override
  public void dragExit(DropTargetEvent dte) {
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
        viewport.setView(source);

        source.scrollRectToVisible(viewPos);
      } else {
        viewPos.x = 0;
        viewPos.y = 0;
        source.scrollRectToVisible(viewPos);
      }

      this.mainScrollPane.revalidate();
      this.mainScrollPane.repaint();
    }
  }

  @Override
  public boolean processDropTopicToAnotherTopic(final MindMapPanel source, final Point dropPoint, final Topic draggedTopic, final Topic destinationTopic) {
    boolean result = false;
    if (draggedTopic != null && destinationTopic != null && draggedTopic != destinationTopic) {
      if (destinationTopic.getExtras().containsKey(Extra.ExtraType.TOPIC)) {
        if (!NbUtils.msgConfirmOkCancel(null, BUNDLE.getString("MMDGraphEditor.addTopicToElement.confirmTitle"), BUNDLE.getString("MMDGraphEditor.addTopicToElement.confirmMsg"))) {
          return result;
        }
      }

      final ExtraTopic topicLink = ExtraTopic.makeLinkTo(this.mindMapPanel.getModel(), draggedTopic);
      destinationTopic.setExtra(topicLink);

      result = true;
    }
    return result;
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
      onMindMapModelChanged(this.mindMapPanel, true);
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
      onMindMapModelChanged(this.mindMapPanel, true);
    }
  }

  private void addFileToElement(final File theFile, final AbstractElement element) {
    if (element != null) {
      final Topic topic = element.getModel();
      final MMapURI theURI;

      if (NbUtils.getPreferences().getBoolean("makeRelativePathToProject", true)) { //NOI18N
        final File projectFolder = getProjectFolder();
        if (theFile.equals(projectFolder)) {
          theURI = new MMapURI(projectFolder, new File("."), null);
        } else {
          theURI = new MMapURI(projectFolder, theFile, null);
        }
      } else {
        theURI = new MMapURI(null, theFile, null);
      }

      if (topic.getExtras().containsKey(Extra.ExtraType.FILE)) {
        if (!NbUtils.msgConfirmOkCancel(null, BUNDLE.getString("MMDGraphEditor.addDataObjectToElement.confirmTitle"), BUNDLE.getString("MMDGraphEditor.addDataObjectToElement.confirmMsg"))) {
          return;
        }
      }

      topic.setExtra(new ExtraFile(theURI));
      this.mindMapPanel.invalidate();
      this.mindMapPanel.repaint();
      onMindMapModelChanged(this.mindMapPanel, true);
    }
  }

  private void addDataObjectToElement(final DataObject dataObject, final AbstractElement element) {
    addFileToElement(FileUtil.toFile(dataObject.getPrimaryFile()), element);
  }

  @Nullable
  private Object extractDataObjectOrFileFromDnD(@Nonnull final DropTargetDropEvent dtde) throws Exception {
    DataFlavor dataObjectFlavor = null;
    DataFlavor nodeObjectFlavor = null;
    DataFlavor projectObjectFlavor = null;

    File detectedFileObject = null;

    for (final DataFlavor df : dtde.getCurrentDataFlavors()) {
      final Class<?> representation = df.getRepresentationClass();
      if (Node.class.isAssignableFrom(representation)) {
        nodeObjectFlavor = df;
        break;
      } else if (DataObject.class.isAssignableFrom(representation)) {
        dataObjectFlavor = df;
        break;
      } else if (Project.class.isAssignableFrom(representation)) {
        projectObjectFlavor = df;
        break;
      } else if (df.isFlavorJavaFileListType()) {
        try {
          final List list = (List) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
          if (list != null && !list.isEmpty()) {
            detectedFileObject = (File) list.get(0);
          }
        } catch (Exception ex) {
          LOGGER.error("Can't extract file from DnD", ex);
          throw ex;
        }
      }
    }

    DataObject dataObject;

    if (nodeObjectFlavor != null) {
      try {
        final Node theNode = (Node) dtde.getTransferable().getTransferData(nodeObjectFlavor);
        dataObject = theNode.getLookup().lookup(DataObject.class);
        if (dataObject == null) {
          final Project proj = theNode.getLookup().lookup(Project.class);
          if (proj != null) {
            dataObject = DataObject.find(proj.getProjectDirectory());
          }
        }
      } catch (Exception ex) {
        LOGGER.error("Can't extract node from dragged element", ex);
        throw ex;
      }
    } else if (dataObjectFlavor != null) {
      try {
        dataObject = (DataObject) dtde.getTransferable().getTransferData(dataObjectFlavor);
      } catch (Exception ex) {
        LOGGER.error("Can't extract data object from dragged element", ex);
        throw ex;
      }
    } else if (projectObjectFlavor != null) {
      try {
        dataObject = DataObject.find(((Project) dtde.getTransferable().getTransferData(projectObjectFlavor)).getProjectDirectory());
      } catch (Exception ex) {
        LOGGER.error("Can't extract data object from project", ex);
        throw ex;
      }
    } else {
      dataObject = null;
    }

    return dataObject == null ? detectedFileObject : dataObject;
  }

  @Override
  public void drop(final DropTargetDropEvent dtde) {

    DataObject detectedDataObject = null;
    File detectedFileObject = null;
    String detectedNote = null;
    URI decodedLink = null;

    dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

    try {
      final Object dataObjOrFile = extractDataObjectOrFileFromDnD(dtde);
      if (dataObjOrFile instanceof DataObject) {
        detectedDataObject = (DataObject) dataObjOrFile;
      } else {
        detectedFileObject = (File) dataObjOrFile;
      }

      final String detectedLink = DnDUtils.extractDropLink(dtde);
      detectedNote = DnDUtils.extractDropNote(dtde);

      if (detectedLink != null) {
        try {
          decodedLink = new URI(detectedLink);
        } catch (final URISyntaxException ex) {
          decodedLink = null;
        }
      }

      dtde.dropComplete(true);
    } catch (final Exception ex) {
      LOGGER.error("Can't extract data from DnD", ex);
      dtde.dropComplete(false);
    }

    final AbstractElement element = this.mindMapPanel.findTopicUnderPoint(dtde.getLocation());

    if (detectedDataObject != null) {
      addDataObjectToElement(detectedDataObject, element);
    } else if (detectedFileObject != null) {
      decodedLink = DnDUtils.extractUrlLinkFromFile(detectedFileObject);
      if (decodedLink != null) {
        addURItoElement(decodedLink, element);
      } else {
        addFileToElement(detectedFileObject, element);
      }
    } else if (decodedLink != null) {
      addURItoElement(decodedLink, element);
    } else if (detectedNote != null) {
      addNoteToElement(detectedNote, element);
    }
  }

  @Nullable
  public FileObject getProjectFolderAsFileObject() {
    final Project proj = this.editorSupport.getProject();
    FileObject result = null;
    if (proj != null) {
      result = proj.getProjectDirectory();
    }
    return result;
  }

  @Nullable
  public File getProjectFolder() {
    final FileObject projectFolder = getProjectFolderAsFileObject();
    return projectFolder == null ? null : FileUtil.toFile(projectFolder);
  }

  protected boolean acceptOrRejectDragging(final DropTargetDragEvent dtde) {
    final int dropAction = dtde.getDropAction();

    boolean result = false;

    if (this.dragAcceptableType && (dropAction & DnDConstants.ACTION_COPY_OR_MOVE) != 0 && this.mindMapPanel.findTopicUnderPoint(dtde.getLocation()) != null) {
      result = true;
    }

    return result;
  }

  protected static boolean checkDragType(final DropTargetDragEvent dtde) {
    boolean result = DnDUtils.isFileOrLinkOrText(dtde);
    if (!result) {
      for (final DataFlavor flavor : dtde.getCurrentDataFlavors()) {
        final Class dataClass = flavor.getRepresentationClass();
        if (Node.class.isAssignableFrom(dataClass) || DataObject.class.isAssignableFrom(dataClass)) {
          result = true;
          break;
        }
      }
    }
    return result;
  }

  @Override
  public CloseOperationState canCloseElement() {
    return CloseOperationState.STATE_OK;
  }

  @Override
  public JComponent getToolbarRepresentation() {
    return this.toolBar;
  }

  private void editFileLinkForTopic(@Nullable final Topic topic) {
    if (topic != null) {
      final ExtraFile currentFilePath = (ExtraFile) topic.getExtras().get(Extra.ExtraType.FILE);
      final FileEditPanel.DataContainer dataContainer;

      final File projectFolder = getProjectFolder();
      if (currentFilePath == null) {
        final FileEditPanel.DataContainer prefilled = new FileEditPanel.DataContainer(null, this.mindMapPanel.getSessionObject(Misc.SESSIONKEY_ADD_FILE_OPEN_IN_SYSTEM, Boolean.class, false));
        dataContainer = NbUtils.editFilePath(null, BUNDLE.getString("MMDGraphEditor.editFileLinkForTopic.dlgTitle"),
                this.mindMapPanel.getSessionObject(Misc.SESSIONKEY_ADD_FILE_LAST_FOLDER, File.class, projectFolder),
                prefilled);
        if (dataContainer != null) {
          this.mindMapPanel.putSessionObject(Misc.SESSIONKEY_ADD_FILE_OPEN_IN_SYSTEM, dataContainer.isShowWithSystemTool());
        }
      } else {
        final MMapURI uri = currentFilePath.getValue();
        final boolean flagOpenInSystem = Boolean.parseBoolean(uri.getParameters().getProperty(FILELINK_ATTR_OPEN_IN_SYSTEM, "false")); //NOI18N
        int lineToOpen = strToLine(uri.getParameters().getProperty(FILELINK_ATTR_LINE, null));
        final String lineAsStr = lineToOpen < 0 ? "" : ":" + lineToOpen;

        final FileEditPanel.DataContainer origPath;
        if (uri.isAbsolute()) {
          origPath = new FileEditPanel.DataContainer(uri.asFile(getProjectFolder()).getAbsolutePath() + lineAsStr, flagOpenInSystem);
        } else if (projectFolder == null) {
          NbUtils.msgWarn(null, BUNDLE.getString("MMDGraphEditor.editFileLinkForTopic.warnText"));
          origPath = new FileEditPanel.DataContainer(uri.asFile(getProjectFolder()).getPath() + lineAsStr, flagOpenInSystem);
        } else {
          origPath = new FileEditPanel.DataContainer(uri.asFile(projectFolder).getAbsolutePath() + lineAsStr, flagOpenInSystem);
        }
        dataContainer = NbUtils.editFilePath(null, BUNDLE.getString("MMDGraphEditor.editFileLinkForTopic.addPathTitle"), projectFolder, origPath);
      }

      if (dataContainer != null) {
        boolean changed = false;
        if (dataContainer.isEmptyOrOnlySpaces()) {
          changed = topic.removeExtra(Extra.ExtraType.FILE);
        } else {
          final Properties props = new Properties();
          if (dataContainer.isShowWithSystemTool()) {
            props.put(FILELINK_ATTR_OPEN_IN_SYSTEM, "true"); //NOI18N
          }
          if (dataContainer.getFilePathWithLine().getLine() >= 0) {
            props.put(FILELINK_ATTR_LINE, Integer.toString(dataContainer.getFilePathWithLine().getLine()));
          }
          final MMapURI fileUri = MMapURI.makeFromFilePath(NbUtils.getPreferences().getBoolean("makeRelativePathToProject", true) ? projectFolder : null, dataContainer.getFilePathWithLine().getPath(), props); //NOI18N
          final File theFile = fileUri.asFile(projectFolder);
          LOGGER.info(String.format("Path %s converted to uri: %s", dataContainer.getFilePathWithLine(), fileUri.asString(false, true))); //NOI18N

          if (theFile.exists()) {
            if (currentFilePath == null) {
              this.mindMapPanel.putSessionObject(Misc.SESSIONKEY_ADD_FILE_LAST_FOLDER, theFile.getParentFile());
            }

            final ExtraFile newFile = new ExtraFile(fileUri);

            if (currentFilePath == null || !currentFilePath.equals(newFile)) {
              topic.setExtra(newFile);
              changed = true;
            }
          } else {
            NbUtils.msgError(null, String.format(BUNDLE.getString("MMDGraphEditor.editFileLinkForTopic.errorCantFindFile"), dataContainer.getFilePathWithLine()));
            changed = false;
          }
        }

        if (changed) {
          this.mindMapPanel.invalidate();
          this.mindMapPanel.repaint();
          onMindMapModelChanged(this.mindMapPanel, true);
        }
      }
    }
  }

  private void editTopicLinkForTopic(final Topic topic) {
    final ExtraTopic link = (ExtraTopic) topic.getExtras().get(Extra.ExtraType.TOPIC);

    ExtraTopic result = null;

    final ExtraTopic remove = new ExtraTopic("_______"); //NOI18N

    if (link == null) {
      final MindMapTreePanel treePanel = new MindMapTreePanel(this.mindMapPanel.getModel(), null, true, null);
      if (NbUtils.plainMessageOkCancel(null, BUNDLE.getString("MMDGraphEditor.editTopicLinkForTopic.dlgSelectTopicTitle"), treePanel)) {
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
      if (NbUtils.plainMessageOkCancel(null, BUNDLE.getString("MMDGraphEditor.editTopicLinkForTopic.dlgEditSelectedTitle"), panel)) {
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
        if (link != null) {
          topic.removeExtra(Extra.ExtraType.TOPIC);
          changed = true;
        }
      } else {
        if (link == null || !link.equals(result)) {
          changed = true;
          topic.setExtra(result);
        }
      }

      if (changed) {
        this.mindMapPanel.invalidate();
        this.mindMapPanel.repaint();
        onMindMapModelChanged(this.mindMapPanel, true);
      }
    }
  }

  private void editLinkForTopic(final Topic topic) {
    final ExtraLink link = (ExtraLink) topic.getExtras().get(Extra.ExtraType.LINK);
    final MMapURI result;
    if (link == null) {
      // create new
      result = NbUtils.editURI(null, String.format(BUNDLE.getString("MMDGraphEditor.editLinkForTopic.dlgAddURITitle"), Utils.makeShortTextVersion(topic.getText(), 16)), null);
    } else {
      // edit
      result = NbUtils.editURI(null, String.format(BUNDLE.getString("MMDGraphEditor.editLinkForTopic.dlgEditURITitle"), Utils.makeShortTextVersion(topic.getText(), 16)), link.getValue());
    }
    if (result != null) {
      boolean changed = false;

      if (result == NbUtils.EMPTY_URI) {
        if (link != null) {
          changed = true;
          topic.removeExtra(Extra.ExtraType.LINK);
        }
      } else {
        final ExtraLink newLink = new ExtraLink(result);
        if (link == null || !link.equals(newLink)) {
          topic.setExtra(newLink);
          changed = true;
        }
      }

      if (changed) {
        this.mindMapPanel.invalidate();
        this.mindMapPanel.repaint();
        onMindMapModelChanged(this.mindMapPanel, true);
      }
    }
  }

  private void editTextForTopic(final Topic topic) {
    final ExtraNote note = (ExtraNote) topic.getExtras().get(Extra.ExtraType.NOTE);
    final String result;
    if (note == null) {
      // create new
      result = NbUtils.editText(null, String.format(BUNDLE.getString("MMDGraphEditor.editTextForTopic.dlfAddNoteTitle"), Utils.makeShortTextVersion(topic.getText(), 16)), ""); //NOI18N
    } else {
      // edit
      result = NbUtils.editText(null, String.format(BUNDLE.getString("MMDGraphEditor.editTextForTopic.dlgEditNoteTitle"), Utils.makeShortTextVersion(topic.getText(), 16)), note.getValue());
    }
    if (result != null) {
      boolean changed = false;

      if (result.isEmpty()) {
        if (note != null) {
          changed = true;
          topic.removeExtra(Extra.ExtraType.NOTE);
        }
      } else {
        final ExtraNote newNote = new ExtraNote(result);
        if (note == null || !note.equals(newNote)) {
          topic.setExtra(newNote);
          changed = true;
        }
      }

      if (changed) {
        this.mindMapPanel.invalidate();
        this.mindMapPanel.repaint();
        onMindMapModelChanged(this.mindMapPanel, true);
      }
    }
  }

  @Override
  public int getPersistenceType() {
    return TopComponent.PERSISTENCE_NEVER;
  }

  public boolean focusToPath(final boolean enforceVisibility, final int[] positionPath) {
    this.mindMapPanel.removeAllSelection();
    Topic topic = this.mindMapPanel.getModel().findForPositionPath(positionPath);
    boolean mapChanged = false;
    if (topic != null) {
      if (enforceVisibility) {
        mapChanged = MindMapUtils.ensureVisibility(topic);
      } else {
        topic = MindMapUtils.findFirstVisibleAncestor(topic);
      }
      if (mapChanged) {
        this.mindMapPanel.doLayout();
        topic = this.mindMapPanel.getModel().findForPositionPath(positionPath);
      }
      this.mindMapPanel.select(topic, false);
    }
    return mapChanged;
  }

  @Override
  public Lookup getLookup() {
    return new ProxyLookup(Lookups.singleton(this), super.getLookup());
  }

  @Override
  public PrintPage[][] getPages(final int paperWidthInPixels, final int paperHeightInPixels, final double pageZoomFactor) {
    final MMDPrintOptions printOptions = new MMDPrintOptions();

    if (pageZoomFactor < 0.1d) {
      printOptions.setScaleType(MMDPrintOptions.ScaleType.FIT_TO_SINGLE_PAGE);
    } else if (pageZoomFactor > 20.0d) {
      printOptions.setScaleType(MMDPrintOptions.ScaleType.ZOOM);
      printOptions.setScale(1.0d);
    } else {
      printOptions.setScaleType(MMDPrintOptions.ScaleType.ZOOM);
      printOptions.setScale(pageZoomFactor);
    }

    final com.igormaznitsa.mindmap.print.PrintPage[][] pages = new MMDPrint(PrintableObject.newBuild().mmdpanel(this.mindMapPanel).build(), paperWidthInPixels, paperHeightInPixels, printOptions).getPages();

    final PrintPage[][] result = new PrintPage[pages.length][];

    for (int i = 0; i < pages.length; i++) {
      result[i] = new PrintPage[pages[i].length];
      for (int p = 0; p < pages[i].length; p++) {
        result[i][p] = new PrintPageAdapter(pages[i][p]);
      }
    }
    return result;
  }

  @Override
  public Date lastModified() {
    if (this.editorSupport.isModified()) {
      return new Date();
    } else {
      return this.editorSupport.getDataObject().getPrimaryFile().lastModified();
    }
  }

  @Override
  public boolean isTrimTopicTextBeforeSet(MindMapPanel source) {
    return NbUtils.getPreferences().getBoolean("trimTopicText", false);
  }

  @Override
  public boolean isCopyColorInfoFromParentToNewChildAllowed(MindMapPanel source) {
    return NbUtils.getPreferences().getBoolean("copyColorInfoToNewChildAllowed", true); //NOI18N
  }

  @Override
  public boolean isUnfoldCollapsedTopicDropTarget(final MindMapPanel source) {
    return NbUtils.getPreferences().getBoolean("unfoldCollapsedTarget", true); //NOI18N
  }

  @Override
  public MindMapPanelConfig provideConfigForMindMapPanel(final MindMapPanel source) {
    final MindMapPanelConfig config = new MindMapPanelConfig();
    config.loadFrom(NbUtils.getPreferences());
    return config;
  }

  private Map<Class<? extends PopUpMenuItemPlugin>, CustomJob> customProcessors = null;

  private Map<Class<? extends PopUpMenuItemPlugin>, CustomJob> getCustomProcessors() {
    if (this.customProcessors == null) {
      this.customProcessors = new HashMap<Class<? extends PopUpMenuItemPlugin>, CustomJob>();
      this.customProcessors.put(ExtraNotePlugin.class, new CustomJob() {
        @Override
        public void doJob(@Nonnull final PopUpMenuItemPlugin plugin, @Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nullable final Topic topic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
          editTextForTopic(topic);
          panel.requestFocus();
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
      this.customProcessors.put(AboutPlugin.class, new CustomJob() {
        @Override
        public void doJob(@Nonnull final PopUpMenuItemPlugin plugin, @Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nullable final Topic topic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
          NbUtils.plainMessageOk(null, BUNDLE.getString("MMDGraphEditor.makePopUp.msgAboutTitle"), new AboutPanel());//NOI18N
        }
      });
      this.customProcessors.put(OptionsPlugin.class, new CustomJob() {
        @Override
        public void doJob(@Nonnull final PopUpMenuItemPlugin plugin, @Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nullable final Topic topic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
          OptionsDisplayer.getDefault().open("nb-mmd-config-main"); //NOI18N
        }
      });
    }
    return this.customProcessors;
  }

  @Override
  public JPopupMenu makePopUpForMindMapPanel(@Nonnull final MindMapPanel source, @Nonnull final Point point, @Nullable final AbstractElement element, @Nullable final ElementPart partUnderMouse) {
    return Utils.makePopUp(source, false, DialogProviderManager.getInstance().getDialogProvider(), element == null ? null : element.getModel(), source.getSelectedTopics(), getCustomProcessors());
  }

  private void processColorDialogForTopics(final MindMapPanel source, final Topic[] topics) {
    final Color borderColor = NbUtils.extractCommonColorForColorChooserButton(ATTR_BORDER_COLOR.getText(), topics);
    final Color fillColor = NbUtils.extractCommonColorForColorChooserButton(ATTR_FILL_COLOR.getText(), topics);
    final Color textColor = NbUtils.extractCommonColorForColorChooserButton(ATTR_TEXT_COLOR.getText(), topics);

    final ColorAttributePanel panel = new ColorAttributePanel(source.getModel(), borderColor, fillColor, textColor);
    if (NbUtils.plainMessageOkCancel(null, String.format(BUNDLE.getString("MMDGraphEditor.colorEditDialogTitle"), topics.length), panel)) {
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

      source.doLayout();
      source.requestFocus();
      onMindMapModelChanged(source, true);
    }
  }

  @Override
  public DialogProvider getDialogProvider(final MindMapPanel source) {
    return DialogProviderManager.getInstance().getDialogProvider();
  }

  private void updateConfigFromPreferences() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (mindMapPanel.lockIfNotDisposed()) {
          try {
            mindMapPanel.refreshConfiguration();
            mindMapPanel.invalidate();
            mindMapPanel.revalidate();
            mindMapPanel.repaint();
          } finally {
            mindMapPanel.unlock();
          }
        } else {
          LOGGER.warn("Attempt tp update disposed panel : " + mindMapPanel);
        }
      }
    });
  }

  public static void notifyReloadConfig() {
    synchronized (ALL_EDITORS) {
      final Iterator<MMDGraphEditor> iterator = ALL_EDITORS.iterator();
      while (iterator.hasNext()) {
        final MMDGraphEditor next = iterator.next();
        if (next.isPanelDisposed()) {
          LOGGER.warn("Detected disposed mind map panel among active editors set : " + next);
          iterator.remove();
        } else {
          next.updateConfigFromPreferences();
        }
      }
    }
  }

  @Override
  public boolean isSelectionAllowed(MindMapPanel source) {
    return true;
  }

  @Override
  public boolean isElementDragAllowed(MindMapPanel source) {
    return true;
  }

  @Override
  public boolean isMouseMoveProcessingAllowed(MindMapPanel source) {
    return true;
  }

  @Override
  public boolean isMouseWheelProcessingAllowed(MindMapPanel source) {
    return true;
  }

  @Override
  public boolean isMouseClickProcessingAllowed(final MindMapPanel source) {
    return true;
  }

  @Override
  public boolean canBeDeletedSilently(final MindMap map, final Topic topic) {
    return topic.getText().isEmpty() && topic.getExtras().isEmpty() && doesContainOnlyStandardAttributes(topic);
  }

}
