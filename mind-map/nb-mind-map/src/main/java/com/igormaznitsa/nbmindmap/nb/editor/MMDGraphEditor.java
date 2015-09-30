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
package com.igormaznitsa.nbmindmap.nb.editor;

import com.igormaznitsa.mindmap.exporters.AbstractMindMapExporter;
import com.igormaznitsa.mindmap.exporters.Exporters;
import com.igormaznitsa.mindmap.model.*;
import com.igormaznitsa.nbmindmap.utils.NbUtils;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapListener;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelController;
import static com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute.*;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractElement;
import com.igormaznitsa.mindmap.swing.panel.ui.ElementPart;
import com.igormaznitsa.mindmap.swing.panel.utils.MindMapUtils;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.nbmindmap.nb.swing.ColorAttributePanel;
import com.igormaznitsa.nbmindmap.nb.print.MMDPrint;
import com.igormaznitsa.nbmindmap.nb.swing.AboutPanel;
import com.igormaznitsa.nbmindmap.nb.swing.ColorChooserButton;
import com.igormaznitsa.nbmindmap.nb.swing.FileEditPanel;
import com.igormaznitsa.nbmindmap.utils.Icons;
import com.igormaznitsa.nbmindmap.nb.swing.MindMapTreePanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
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
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.WeakSet;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;
import static org.openide.windows.TopComponent.PERSISTENCE_NEVER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MultiViewElement.Registration(
        displayName = "#MMDGraphEditor.displayName",
        mimeType = MMDDataObject.MIME,
        persistenceType = PERSISTENCE_NEVER,
        iconBase = "com/igormaznitsa/nbmindmap/icons/logo/logo16.png",
        preferredID = MMDGraphEditor.ID,
        position = 1
)
public final class MMDGraphEditor extends CloneableEditor implements MindMapController, PrintProvider, MultiViewElement, MindMapListener, DropTargetListener, MindMapPanelController, DialogProvider {

  private static final long serialVersionUID = -8776707243607267446L;

  private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle");

  private static final Logger logger = LoggerFactory.getLogger(MMDGraphEditor.class);

  private static final String FILELINK_ATTR_OPEN_IN_SYSTEM = "useSystem"; //NOI18N

  public static final String ID = "mmd-graph-editor"; //NOI18N

  private volatile boolean rootToCentre = true;

  private MultiViewElementCallback callback;
  private final MMDEditorSupport editorSupport;

  private final JScrollPane mainScrollPane;
  private final MindMapPanel mindMapPanel;

  private boolean dragAcceptableType = false;

  private final JToolBar toolBar = new JToolBar();

  private static final WeakSet<MMDGraphEditor> allEditors = new WeakSet<>();

  public MMDGraphEditor() {
    this(Lookup.getDefault().lookup(MMDEditorSupport.class));
  }

  public MMDGraphEditor(final MMDEditorSupport support) {
    super(support);

    this.editorSupport = support;

    this.mainScrollPane = new JScrollPane();
    this.mindMapPanel = new MindMapPanel(this);
    this.mindMapPanel.addMindMapListener(this);

    this.mindMapPanel.setDropTarget(new DropTarget(this.mindMapPanel, this));

    this.mainScrollPane.setViewportView(this.mindMapPanel);
    this.mainScrollPane.setWheelScrollingEnabled(true);
    this.mainScrollPane.setAutoscrolls(true);

    this.setLayout(new BorderLayout(0, 0));
    this.add(this.mainScrollPane, BorderLayout.CENTER);

    this.mindMapPanel.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(final ComponentEvent e) {
        processEditorResizing(mindMapPanel);
      }
    });
    updateName();

    synchronized (allEditors) {
      allEditors.add(this);
    }
  }

  @Override
  public JComponent getVisualRepresentation() {
    return this;
  }

  @Override
  public void componentActivated() {
    super.componentActivated();
    this.editorSupport.onEditorActivated();

    if (this.rootToCentre) {
      this.rootToCentre = false;
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          final Topic root = mindMapPanel.getModel() == null ? null : mindMapPanel.getModel().getRoot();
          if (mindMapPanel.hasSelectedTopics()) {
            topicToCentre(mindMapPanel.getFirstSelected());
          }
          else if (root != null) {
            mindMapPanel.select(root, false);
            topicToCentre(root);
          }
        }
      });
    }
  }

  @Override
  public void componentClosed() {
    super.componentClosed();
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
    super.componentDeactivated();
  }

  @Override
  public void componentHidden() {
    super.componentHidden();
  }

  private void updateModel() {
    this.mindMapPanel.hideEditor();

    final String text = this.editorSupport.getDocumentText();
    if (text == null) {
      this.mindMapPanel.setErrorText(BUNDLE.getString("MMDGraphEditor.updateModel.cantLoadDocument"));
    }
    else {
      try {
        this.mindMapPanel.setModel(new MindMap(this, new StringReader(text)));
      }
      catch (IllegalArgumentException ex) {
        logger.warn("Can't detect mind map"); //NOI18N
        this.mindMapPanel.setErrorText(BUNDLE.getString("MMDGraphEditor.updateModel.cantDetectMMap"));
      }
      catch (IOException ex) {
        logger.error("Can't parse mind map text", ex); //NOI18N
        this.mindMapPanel.setErrorText(BUNDLE.getString("MMDGraphEditor.updateModel.cantParseDoc"));
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
    }
    else {
      result = NbUtils.msgConfirmYesNo(BUNDLE.getString("MMDGraphEditor.allowedRemovingOfTopics,title"), String.format(BUNDLE.getString("MMDGraphEditor.allowedRemovingOfTopics.message"), topics.length));
    }
    return result;
  }

  @Override
  public void onMindMapModelChanged(final MindMapPanel source) {
    try {
      final StringWriter writer = new StringWriter(16384);

      final MindMap theMap = this.mindMapPanel.getModel();
      MindMapUtils.removeCollapseAttributeFromTopicsWithoutChildren(theMap);
      theMap.write(writer);
      this.editorSupport.replaceDocumentText(writer.toString());
      this.editorSupport.getDataObject().setModified(true);
    }
    catch (Exception ex) {
      logger.error("Can't get document text", ex); //NOI18N
    }
    finally {
      copyNameToCallbackTopComponent();
    }
  }

  @Override
  public void onMindMapModelRealigned(final MindMapPanel source, final Dimension coveredAreaSize) {
    this.mainScrollPane.getViewport().revalidate();
  }

  public void topicToCentre(final Topic topic) {
    if (topic != null) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          final AbstractElement element = (AbstractElement) topic.getPayload();
          if (element != null) {
            final Rectangle2D bounds = element.getBounds();
            final Dimension viewPortSize = mainScrollPane.getViewport().getExtentSize();

            final int x = Math.max(0, (int) Math.round(bounds.getX() - (viewPortSize.getWidth() - bounds.getWidth()) / 2));
            final int y = Math.max(0, (int) Math.round(bounds.getY() - (viewPortSize.getHeight() - bounds.getHeight()) / 2));

            mainScrollPane.getViewport().setViewPosition(new Point(x, y));
          }
        }
      });
    }
  }

  @Override
  public void onEnsureVisibilityOfTopic(final MindMapPanel source, final Topic topic) {
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

  @Override
  @SuppressWarnings("unchecked")
  public void onClickOnExtra(final MindMapPanel source, final int clicks, final Topic topic, final Extra<?> extra) {
    if (clicks > 1) {
      switch (extra.getType()) {
        case FILE: {
          final FileObject fileObj;
          final MMapURI uri = (MMapURI) extra.getValue();
          try {
            fileObj = FileUtil.toFileObject(uri.asFile(this.editorSupport.getProjectDirectory()));
            if (fileObj == null) {
              NbUtils.msgError(String.format(BUNDLE.getString("MMDGraphEditor.onClickExtra.errorCanfFindFile"), uri.toString()));
              return;
            }
          }
          catch (Exception ex) {
            logger.error("onClickExtra#FILE", ex); //NOI18N
            NbUtils.msgError(String.format(BUNDLE.getString("MMDGraphEditor.onClickOnExtra.msgWrongFilePath"), uri.toString()));
            return;
          }

          try {
            final File theFile = uri.asFile(this.editorSupport.getProjectDirectory());

            if (Boolean.parseBoolean(uri.getParameters().getProperty(FILELINK_ATTR_OPEN_IN_SYSTEM, "false"))) { //NOI18N
              NbUtils.openInExternalEditor(theFile);
            }
            else {
              if (fileObj.isFolder()) {
                if (FileOwnerQuery.getOwner(fileObj) != null) {
                  final DataObject dobj = DataObject.find(fileObj);
                  if (dobj instanceof DataFolder) {
                    if (NbUtils.SelectIn.PROJECTS.select(this, dobj)) {
                      return;
                    }
                  }
                }

                final ProjectManager manager = ProjectManager.getDefault();
                if (manager.isProject(fileObj)) {
                  final Project project = manager.findProject(fileObj);
                  if (project == null) {
                    if (!NbUtils.SelectIn.FAVORITES.select(this, fileObj)) {
                      NbUtils.openInExternalEditor(theFile);
                    }
                  }
                  else {
                    final OpenProjects openManager = OpenProjects.getDefault();
                    if (!openManager.isProjectOpen(project)) {
                      openManager.open(new Project[]{project}, false, true);
                    }
                  }
                }
                else {
                  if (!NbUtils.SelectIn.FAVORITES.select(this, fileObj)) {
                    NbUtils.openInExternalEditor(theFile);
                  }
                }
              }
              else {
                final DataObject dobj = DataObject.find(fileObj);
                final Openable openable = dobj.getLookup().lookup(Openable.class);
                if (openable != null) {
                  openable.open();
                }
              }
            }
          }
          catch (Exception ex) {
            logger.error("Cant't find or open data object", ex); //NOI18N
            NbUtils.msgError(String.format(BUNDLE.getString("MMDGraphEditor.onClickExtra.cantFindFileObj"), uri.toString()));
          }
        }
        break;
        case LINK: {
          final MMapURI uri = ((ExtraLink) extra).getValue();
          if (!NbUtils.browseURI(uri.asURI(), NbUtils.getPreferences().getBoolean("useInsideBrowser", false))) { //NOI18N
            NbUtils.msgError(String.format(BUNDLE.getString("MMDGraphEditor.onClickOnExtra.msgCantBrowse"), uri.toString()));
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
            NbUtils.msgWarn(BUNDLE.getString("MMDGraphEditor.onClickOnExtra.msgCantFindTopic"));
          }
          else {
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
  }

  private static void processEditorResizing(final MindMapPanel panel) {
    panel.updateView(false);
    panel.updateEditorAfterResizing();
  }

  public void updateView() {
    if (SwingUtilities.isEventDispatchThread()) {
      this.updateModel();
    }
    else {
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
    }
    else {
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
  public boolean processDropTopicToAnotherTopic(final MindMapPanel source, final Point dropPoint, final Topic draggedTopic, final Topic destinationTopic) {
    boolean result = false;
    if (draggedTopic != null && destinationTopic != null && draggedTopic != destinationTopic) {
      if (destinationTopic.getExtras().containsKey(Extra.ExtraType.TOPIC)) {
        if (!NbUtils.msgConfirmOkCancel(BUNDLE.getString("MMDGraphEditor.addTopicToElement.confirmTitle"), BUNDLE.getString("MMDGraphEditor.addTopicToElement.confirmMsg"))) {
          return result;
        }
      }

      final ExtraTopic topicLink = ExtraTopic.makeLinkTo(this.mindMapPanel.getModel(), draggedTopic);
      destinationTopic.setExtra(topicLink);

      result = true;
    }
    return result;
  }

  private void addDataObjectToElement(final DataObject dataObject, final AbstractElement element) {
    if (element != null) {
      final Topic topic = element.getModel();

      final File theFile = FileUtil.toFile(dataObject.getPrimaryFile());

      final MMapURI theURI = NbUtils.getPreferences().getBoolean("makeRelativePathToProject", true) ? new MMapURI(getProjectFolder(), theFile, null) : new MMapURI(null, theFile, null); //NOI18N

      if (topic.getExtras().containsKey(Extra.ExtraType.FILE)) {
        if (!NbUtils.msgConfirmOkCancel(BUNDLE.getString("MMDGraphEditor.addDataObjectToElement.confirmTitle"), BUNDLE.getString("MMDGraphEditor.addDataObjectToElement.confirmMsg"))) {
          return;
        }
      }

      topic.setExtra(new ExtraFile(theURI));
      this.mindMapPanel.invalidate();
      this.mindMapPanel.repaint();
      onMindMapModelChanged(this.mindMapPanel);
    }
  }

  @Override
  public void drop(final DropTargetDropEvent dtde) {
    DataFlavor dataObjectFlavor = null;
    DataFlavor nodeObjectFlavor = null;
    for (final DataFlavor df : dtde.getCurrentDataFlavors()) {
      final Class<?> representation = df.getRepresentationClass();
      if (Node.class.isAssignableFrom(representation)) {
        nodeObjectFlavor = df;
        break;
      }
      else if (DataObject.class.isAssignableFrom(representation)) {
        dataObjectFlavor = df;
        break;
      }
    }

    final DataObject dataObject;

    if (nodeObjectFlavor != null) {
      try {
        final Node theNode = (Node) dtde.getTransferable().getTransferData(nodeObjectFlavor);
        dataObject = theNode.getLookup().lookup(DataObject.class);
      }
      catch (Exception ex) {
        logger.error("Can't extract node from dragged element", ex);
        dtde.rejectDrop();
        return;
      }
    }
    else if (dataObjectFlavor != null) {
      try {
        dataObject = (DataObject) dtde.getTransferable().getTransferData(dataObjectFlavor);
      }
      catch (Exception ex) {
        logger.error("Can't extract data object from dragged element", ex);
        dtde.rejectDrop();
        return;
      }
    }
    else {
      dataObject = null;
    }

    if (dataObject != null) {
      addDataObjectToElement(dataObject, this.mindMapPanel.findTopicUnderPoint(dtde.getLocation()));
    }
    else {
      logger.error("There is not any DataObject in the dragged element");
      dtde.rejectDrop();
    }
  }

  public File getProjectFolder() {
    File result = null;
    final Project proj = this.editorSupport.getProject();
    if (proj != null) {
      result = FileUtil.toFile(proj.getProjectDirectory());
    }
    return result;
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
    boolean result = false;
    for (final DataFlavor fl1 : dtde.getCurrentDataFlavors()) {
      final Class dataClass = fl1.getRepresentationClass();
      if (Node.class.isAssignableFrom(dataClass) || DataObject.class.isAssignableFrom(dataClass)) {
        result = true;
        break;
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

  private void editFileLinkForTopic(final Topic topic) {
    final ExtraFile file = (ExtraFile) topic.getExtras().get(Extra.ExtraType.FILE);

    final FileEditPanel.DataContainer path;

    final File projectFolder = getProjectFolder();
    if (file == null) {
      path = NbUtils.editFilePath(BUNDLE.getString("MMDGraphEditor.editFileLinkForTopic.dlgTitle"), projectFolder, null);
    }
    else {
      final MMapURI uri = file.getValue();
      final boolean flagOpenInSystem = Boolean.parseBoolean(uri.getParameters().getProperty(FILELINK_ATTR_OPEN_IN_SYSTEM, "false")); //NOI18N

      final FileEditPanel.DataContainer origPath;
      if (uri.isAbsolute()) {
        origPath = new FileEditPanel.DataContainer(uri.asFile(getProjectFolder()).getAbsolutePath(), flagOpenInSystem);
      }
      else {
        if (projectFolder == null) {
          NbUtils.msgWarn(BUNDLE.getString("MMDGraphEditor.editFileLinkForTopic.warnText"));
          origPath = new FileEditPanel.DataContainer(uri.asFile(getProjectFolder()).getPath(), flagOpenInSystem);
        }
        else {
          origPath = new FileEditPanel.DataContainer(uri.asFile(projectFolder).getAbsolutePath(), flagOpenInSystem);
        }
      }
      path = NbUtils.editFilePath(BUNDLE.getString("MMDGraphEditor.editFileLinkForTopic.addPathTitle"), projectFolder, origPath);
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
        final MMapURI fileUri = MMapURI.makeFromFilePath(NbUtils.getPreferences().getBoolean("makeRelativePathToProject", true) ? projectFolder : null, path.getPath(), props); //NOI18N
        final File theFile = fileUri.asFile(projectFolder);
        logger.info(String.format("Path %s converted to uri: %s", path.getPath(), fileUri.asString(false, true))); //NOI18N

        if (theFile.exists()) {
          topic.setExtra(new ExtraFile(fileUri));
          changed = true;
        }
        else {
          NbUtils.msgError(String.format(BUNDLE.getString("MMDGraphEditor.editFileLinkForTopic.errorCantFindFile"), path.getPath()));
          changed = false;
        }
      }

      if (changed) {
        this.mindMapPanel.invalidate();
        this.mindMapPanel.repaint();
        onMindMapModelChanged(this.mindMapPanel);
      }
    }
  }

  private void editTopicLinkForTopic(final Topic topic) {
    final ExtraTopic link = (ExtraTopic) topic.getExtras().get(Extra.ExtraType.TOPIC);

    ExtraTopic result = null;

    final ExtraTopic remove = new ExtraTopic("_______"); //NOI18N

    if (link == null) {
      final MindMapTreePanel treePanel = new MindMapTreePanel(this.mindMapPanel.getModel(), null, true, null);
      if (NbUtils.plainMessageOkCancel(BUNDLE.getString("MMDGraphEditor.editTopicLinkForTopic.dlgSelectTopicTitle"), treePanel)) {
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
      if (NbUtils.plainMessageOkCancel(BUNDLE.getString("MMDGraphEditor.editTopicLinkForTopic.dlgEditSelectedTitle"), panel)) {
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

  private void editLinkForTopic(final Topic topic) {
    final ExtraLink link = (ExtraLink) topic.getExtras().get(Extra.ExtraType.LINK);
    final MMapURI result;
    if (link == null) {
      // create new
      result = NbUtils.editURI(String.format(BUNDLE.getString("MMDGraphEditor.editLinkForTopic.dlgAddURITitle"), Utils.makeShortTextVersion(topic.getText(), 16)), null);
    }
    else {
      // edit
      result = NbUtils.editURI(String.format(BUNDLE.getString("MMDGraphEditor.editLinkForTopic.dlgEditURITitle"), Utils.makeShortTextVersion(topic.getText(), 16)), link.getValue());
    }
    if (result != null) {
      if (result == NbUtils.EMPTY_URI) {
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

  private void editTextForTopic(final Topic topic) {
    final ExtraNote note = (ExtraNote) topic.getExtras().get(Extra.ExtraType.NOTE);
    final String result;
    if (note == null) {
      // create new
      result = NbUtils.editText(String.format(BUNDLE.getString("MMDGraphEditor.editTextForTopic.dlfAddNoteTitle"), Utils.makeShortTextVersion(topic.getText(), 16)), ""); //NOI18N
    }
    else {
      // edit
      result = NbUtils.editText(String.format(BUNDLE.getString("MMDGraphEditor.editTextForTopic.dlgEditNoteTitle"), Utils.makeShortTextVersion(topic.getText(), 16)), note.getValue());
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
      }
      else {
        topic = MindMapUtils.findFirstVisibleAncestor(topic);
      }
      if (mapChanged) {
        this.mindMapPanel.updateView(true);
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
    return new MMDPrint(this.mindMapPanel, paperWidthInPixels, paperHeightInPixels, pageZoomFactor).getPages();
  }

  @Override
  public Date lastModified() {
    if (this.editorSupport.isModified()) {
      return new Date();
    }
    else {
      return this.editorSupport.getDataObject().getPrimaryFile().lastModified();
    }
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

  @Override
  public JPopupMenu makePopUpForMindMapPanel(final MindMapPanel source, final Point point, final AbstractElement element, final ElementPart partUnderMouse) {
    final JPopupMenu result = new JPopupMenu();

    if (element != null) {
      final JMenuItem editText = new JMenuItem(BUNDLE.getString("MMDGraphEditor.makePopUp.miEditText"), Icons.EDITTEXT.getIcon());
      editText.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          mindMapPanel.startEdit(element);
        }
      });

      result.add(editText);

      final JMenuItem addChild = new JMenuItem(BUNDLE.getString("MMDGraphEditor.makePopUp.miAddChild"), Icons.ADD.getIcon());
      addChild.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          mindMapPanel.makeNewChildAndStartEdit(element.getModel(), null);
        }
      });

      result.add(addChild);
    }

    if (element != null || this.mindMapPanel.hasSelectedTopics()) {
      final JMenuItem deleteItem = new JMenuItem(this.mindMapPanel.hasSelectedTopics() ? BUNDLE.getString("MMDGraphEditor.makePopUp.miRemoveSelectedTopics") : BUNDLE.getString("MMDGraphEditor.makePopUp.miRemoveTheTopic"), Icons.DELETE.getIcon());
      deleteItem.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          if (mindMapPanel.hasSelectedTopics()) {
            mindMapPanel.deleteSelectedTopics();
          }
          else {
            mindMapPanel.deleteTopics(element.getModel());
          }
        }
      });

      result.add(deleteItem);
    }

    if (element != null || this.mindMapPanel.hasOnlyTopicSelected()) {
      final Topic theTopic = this.mindMapPanel.getFirstSelected() == null ? (element != null ? element.getModel() : null) : this.mindMapPanel.getFirstSelected();
      if (theTopic != null && theTopic.getParent() != null) {
        final JMenuItem cloneItem = new JMenuItem(this.mindMapPanel.hasSelectedTopics() ? BUNDLE.getString("MMDGraphEditor.makePopUp.miCloneSelectedTopic") : BUNDLE.getString("MMDGraphEditor.makePopUp.miCloneTheTopic"), Icons.CLONE.getIcon());
        cloneItem.addActionListener(new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            mindMapPanel.cloneTopic(theTopic);
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

      final JMenuItem editText = new JMenuItem(topic.getExtras().containsKey(Extra.ExtraType.NOTE) ? BUNDLE.getString("MMDGraphEditor.makePopUp.miEditNote") : BUNDLE.getString("MMDGraphEditor.makePopUp.miAddNote"), Icons.NOTE.getIcon());
      editText.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
          editTextForTopic(topic);
          mindMapPanel.requestFocus();
        }
      });

      result.add(editText);

      final JMenuItem editLink = new JMenuItem(topic.getExtras().containsKey(Extra.ExtraType.LINK) ? BUNDLE.getString("MMDGraphEditor.makePopUp.miEditURI") : BUNDLE.getString("MMDGraphEditor.makePopUp.miAddURI"), Icons.URL.getIcon());
      editLink.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          editLinkForTopic(topic);
          mindMapPanel.requestFocus();
        }
      });

      result.add(editLink);

      final JMenuItem editTopicLink = new JMenuItem(topic.getExtras().containsKey(Extra.ExtraType.TOPIC) ? BUNDLE.getString("MMDGraphEditor.makePopUp.miEditTransition") : BUNDLE.getString("MMDGraphEditor.makePopUp.miAddTransition"), Icons.TOPIC.getIcon());
      editTopicLink.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          editTopicLinkForTopic(topic);
          mindMapPanel.requestFocus();
        }
      });

      result.add(editTopicLink);

      final JMenuItem editFileLink = new JMenuItem(topic.getExtras().containsKey(Extra.ExtraType.FILE) ? BUNDLE.getString("MMDGraphEditor.makePopUp.miEditFile") : BUNDLE.getString("MMDGraphEditor.makePopUp.miAddFile"), Icons.FILE.getIcon());
      editFileLink.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          editFileLinkForTopic(topic);
          mindMapPanel.requestFocus();
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
        topics = new Topic[]{element.getModel()};
        name = BUNDLE.getString("MMDGraphEditor.makePopUp.miColorsForTopic");
      }

      final JMenuItem colors = new JMenuItem(name, Icons.COLORS.getIcon());
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

    final JMenuItem expandAll = new JMenuItem(BUNDLE.getString("MMDGraphEditor.makePopUp.miExpandAll"), Icons.EXPANDALL.getIcon());
    expandAll.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        mindMapPanel.collapseOrExpandAll(false);
      }

    });

    final JMenuItem collapseAll = new JMenuItem(BUNDLE.getString("MMDGraphEditor.makePopUp.miCollapseAll"), Icons.COLLAPSEALL.getIcon());
    collapseAll.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        mindMapPanel.collapseOrExpandAll(true);
      }

    });

    final JCheckBoxMenuItem showJumps = new JCheckBoxMenuItem(BUNDLE.getString("MMDGraphEditor.makePopUp.miShowJumps"), Icons.SHOWJUMPS.getIcon(), source.isShowJumps());
    showJumps.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        mindMapPanel.setShowJumps(showJumps.isSelected());
      }
    });

    result.add(showJumps);
    result.add(expandAll);
    result.add(collapseAll);

    if (result.getComponentCount() > 0) {
      result.add(new JSeparator());
    }
    final JMenu exportMenu = new JMenu(BUNDLE.getString("MMDGraphEditor.makePopUp.miExportMapAs"));
    exportMenu.setIcon(Icons.EXPORT.getIcon());
    for (final Exporters e : Exporters.values()) {
      final AbstractMindMapExporter exp = e.getExporter();
      final JMenuItem item = new JMenuItem(exp.getName());
      item.setToolTipText(exp.getReference());
      item.setIcon(exp.getIcon());
      item.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
          try {
            exp.doExport(mindMapPanel, null);
          }
          catch (Exception ex) {
            logger.error("Error during map export", ex); //NOI18N
            NbUtils.msgError(BUNDLE.getString("MMDGraphEditor.makePopUp.errMsgCantExport"));
          }
        }
      });
      exportMenu.add(item);
    }
    result.add(exportMenu);

    result.add(new JSeparator());

    JMenuItem optionsMenu = new JMenuItem(BUNDLE.getString("MMDGraphEditor.makePopUp.miOptions"), Icons.OPTIONS.getIcon());
    optionsMenu.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        OptionsDisplayer.getDefault().open("nb-mmd-config-main"); //NOI18N
      }
    });

    result.add(optionsMenu);

    JMenuItem infoMenu = new JMenuItem(BUNDLE.getString("MMDGraphEditor.makePopUp.miAbout"), Icons.INFO.getIcon());
    infoMenu.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        NbUtils.plainMessageOk(BUNDLE.getString("MMDGraphEditor.makePopUp.msgAboutTitle"), new AboutPanel());
      }
    });

    result.add(infoMenu);

    return result;
  }

  private void processColorDialogForTopics(final MindMapPanel source, final Topic[] topics) {
    final Color borderColor = NbUtils.extractCommonColorForColorChooserButton(ATTR_BORDER_COLOR.getText(), topics);
    final Color fillColor = NbUtils.extractCommonColorForColorChooserButton(ATTR_FILL_COLOR.getText(), topics);
    final Color textColor = NbUtils.extractCommonColorForColorChooserButton(ATTR_TEXT_COLOR.getText(), topics);

    final ColorAttributePanel panel = new ColorAttributePanel(borderColor, fillColor, textColor);
    if (NbUtils.plainMessageOkCancel(String.format(BUNDLE.getString("MMDGraphEditor.colorEditDialogTitle"), topics.length), panel)) {
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

      source.updateView(true);
    }
  }

  @Override
  public DialogProvider getDialogProvider(final MindMapPanel source) {
    return this;
  }

  @Override
  public void msgError(final String text) {
    NbUtils.msgError(text);
  }

  @Override
  public void msgInfo(final String text) {
    NbUtils.msgInfo(text);
  }

  @Override
  public void msgWarn(final String text) {
    NbUtils.msgWarn(text);
  }

  @Override
  public boolean msgConfirmOkCancel(String title, String question) {
    return NbUtils.msgConfirmOkCancel(title, question);
  }

  @Override
  public boolean msgConfirmYesNo(String title, String question) {
    return NbUtils.msgConfirmYesNo(title, question);
  }

  @Override
  public Boolean msgConfirmYesNoCancel(String title, String question) {
    return NbUtils.msgConfirmYesNoCancel(title, question);
  }

  @Override
  public File msgSaveFileDialog(final String id, final String title, final File defaultFolder, final boolean fileOnly, final FileFilter fileFilter, final String approveButtonText) {
    return new FileChooserBuilder(id).setTitle(title).setDefaultWorkingDirectory(defaultFolder).setFilesOnly(fileOnly).setFileFilter(fileFilter).setApproveText(approveButtonText).showSaveDialog();
  }

  private void updateConfigFromPreferences() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        mindMapPanel.refreshConfiguration();
        mindMapPanel.invalidate();
        mindMapPanel.revalidate();
        mindMapPanel.repaint();
      }
    });
  }

  public static void notifyReloadConfig() {
    synchronized (allEditors) {
      for (final MMDGraphEditor e : allEditors) {
        e.updateConfigFromPreferences();
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
