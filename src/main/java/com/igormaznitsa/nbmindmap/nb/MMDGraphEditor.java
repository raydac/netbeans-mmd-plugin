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
package com.igormaznitsa.nbmindmap.nb;

import com.igormaznitsa.nbmindmap.exporters.Exporters;
import com.igormaznitsa.nbmindmap.exporters.AbstractMindMapExporter;
import com.igormaznitsa.nbmindmap.utils.NbUtils;
import com.igormaznitsa.nbmindmap.mmgui.MindMapListener;
import com.igormaznitsa.nbmindmap.mmgui.MindMapPanel;
import com.igormaznitsa.nbmindmap.mmgui.AbstractElement;
import com.igormaznitsa.nbmindmap.mmgui.ElementPart;
import com.igormaznitsa.nbmindmap.model.Extra;
import com.igormaznitsa.nbmindmap.model.ExtraFile;
import com.igormaznitsa.nbmindmap.model.ExtraLink;
import com.igormaznitsa.nbmindmap.model.ExtraNote;
import com.igormaznitsa.nbmindmap.model.ExtraTopic;
import com.igormaznitsa.nbmindmap.model.MindMap;
import com.igormaznitsa.nbmindmap.model.Topic;
import com.igormaznitsa.nbmindmap.utils.AboutPanel;
import com.igormaznitsa.nbmindmap.utils.Icons;
import com.igormaznitsa.nbmindmap.utils.Logger;
import com.igormaznitsa.nbmindmap.utils.MindMapTreePanel;
import com.igormaznitsa.nbmindmap.utils.Utils;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
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
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.*;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.Project;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditor;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import static org.openide.windows.TopComponent.PERSISTENCE_NEVER;

@MultiViewElement.Registration(
        displayName = "Graph",
        mimeType = MMDDataObject.MIME,
        persistenceType = PERSISTENCE_NEVER,
        iconBase = "com/igormaznitsa/nbmindmap/icons/logo/logo16.png",
        preferredID = MMDGraphEditor.ID,
        position = 1
)
public final class MMDGraphEditor extends CloneableEditor implements MultiViewElement, MindMapListener, DropTargetListener, MindMapPanel.PopUpProvider {

  private static final long serialVersionUID = -8776707243607267446L;

  public static final String ID = "mmd-graph-editor"; //NOI18N

  private MultiViewElementCallback callback;
  private final MMDEditorSupport editorSupport;

  private final JScrollPane mainScrollPane;
  private final MindMapPanel mindMapPanel;

  private boolean dragAcceptableType = false;

  private final JToolBar toolBar = new JToolBar();

  public MMDGraphEditor(){
    this(Lookup.getDefault().lookup(MMDEditorSupport.class));
  }
  
  public MMDGraphEditor(final MMDEditorSupport support) {
    super(support);

    this.editorSupport = support;

    this.mainScrollPane = new JScrollPane();
    this.mindMapPanel = new MindMapPanel();
    this.mindMapPanel.addMindMapListener(this);
    this.mindMapPanel.setPopUpProvider(this);

    this.mindMapPanel.setDropTarget(new DropTarget(this.mindMapPanel, this));

    this.mainScrollPane.setViewportView(this.mindMapPanel);
    this.mainScrollPane.setWheelScrollingEnabled(true);

    this.setLayout(new BorderLayout(0, 0));
    this.add(this.mainScrollPane, BorderLayout.CENTER);

    this.mindMapPanel.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        processEditorResizing(mindMapPanel);
      }
    });
    updateName();
  }

  @Override
  public JComponent getVisualRepresentation() {
    return this;
  }

  @Override
  public void componentActivated() {
    super.componentActivated();
    this.editorSupport.onEditorActivated();
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
    this.editorSupport.updateTitles();
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
    final String text = this.editorSupport.getDocumentText();
    if (text == null) {
      this.mindMapPanel.setErrorText(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.updateModel.cantLoadDocument"));
    }
    else {
      try {
        this.mindMapPanel.setModel(new MindMap(new StringReader(text)));
      }
      catch (IllegalArgumentException ex) {
        Logger.warn("Can't detect mind map"); //NOI18N
        this.mindMapPanel.setErrorText(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.updateModel.cantDetectMMap"));
      }
      catch (IOException ex) {
        Logger.error("Can't parse mind map text", ex); //NOI18N
        this.mindMapPanel.setErrorText(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.updateModel.cantParseDoc"));
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
    
    for(final Topic t : topics){
      topicsNotImportant &= t.canBeLost();
      if (!topicsNotImportant) break;
    }
    
    final boolean result;
    
    if (topicsNotImportant){
      result = true;
    }else{
      result = NbUtils.msgConfirmYesNo(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.allowedRemovingOfTopics,title"), String.format(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.allowedRemovingOfTopics.message"), topics.length));
    }
    return result;
  }

  @Override
  public void onMindMapModelChanged(final MindMapPanel source) {
    try {
      final StringWriter writer = new StringWriter(16384);
      this.mindMapPanel.getModel().write(writer);
      this.editorSupport.replaceDocumentText(writer.toString());
      this.editorSupport.notifyModified();
    }
    catch (Exception ex) {
      Logger.error("Can't get document text", ex); //NOI18N
    }
    finally {
      copyNameToCallbackTopComponent();
    }
  }

  @Override
  public void onMindMapModelRealigned(final MindMapPanel source, final Dimension coveredAreaSize) {
    this.mainScrollPane.getViewport().revalidate();
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
          try {
            final URI uri = (URI) extra.getValue();
            if (uri.isAbsolute()) {
              fileObj = FileUtil.toFileObject(new File(uri));
            }
            else {
              fileObj = this.editorSupport.makeRelativePathToProjectRoot(uri.getPath());
              if (fileObj == null) {
                NbUtils.msgError(String.format(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.onClickExtra.errorCanfFindFile"), uri.getPath()));
                return;
              }
            }
          }
          catch (Exception ex) {
            Logger.error("onClickExtra#FILE", ex);
            NbUtils.msgError(String.format(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.onClickOnExtra.msgWrongFilePath"), extra.getValue().toString()));
            return;
          }

          try {
            final DataObject dobj = DataObject.find(fileObj);
            final Openable openable = dobj.getLookup().lookup(Openable.class);
            if (openable != null) {
              openable.open();
            }
          }
          catch (DataObjectNotFoundException ex) {
            Logger.error("Cant't find data object", ex); //NOI18N
          }
        }
        break;
        case LINK: {
          final URI uri = ((ExtraLink) extra).getValue();
          if (!NbUtils.browseURI(uri, NbUtils.getPreferences().getBoolean("useInsideBrowser", false))) { //NOI18N
            NbUtils.msgError(String.format(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.onClickOnExtra.msgCantBrowse"), uri.toString()));
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
            NbUtils.msgWarn(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.onClickOnExtra.msgCantFindTopic"));
          }
          else {
            // detected
            this.mindMapPanel.focusTo(theTopic);
          }
        }
        break;
        default:
          throw new Error("Unexpected type " + extra.getType());
      }
    }
  }

  @Override
  public void onChangedSelection(final MindMapPanel source, final Topic[] currentSelectedTopics) {
  }

  private static void processEditorResizing(final MindMapPanel panel) {
    panel.revalidate();
    panel.repaint();
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

  private void addDataObjectToElement(final DataObject dataObject, final AbstractElement element) {
    try {
      if (element != null) {
        final Topic topic = element.getModel();

        final FileObject fileObj = dataObject.getPrimaryFile();
        final String relativePath = NbUtils.getPreferences().getBoolean("makeRelativePathToProject", true) ? getRelativePathToProjectIfPossible(fileObj) : null; //NOI18N

        final URI uri;
        if (relativePath != null) {
          uri = new URI(relativePath.replace('\\', '/'));
        }
        else {
          uri = fileObj.toURI();
        }

        if (topic.getExtras().containsKey(Extra.ExtraType.FILE)) {
          if (!NbUtils.msgConfirmOkCancel(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.addDataObjectToElement.confirmTitle"), java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.addDataObjectToElement.confirmMsg"))) {
            return;
          }
        }

        topic.setExtra(new ExtraFile(uri));
        this.mindMapPanel.invalidate();
        this.mindMapPanel.repaint();
        onMindMapModelChanged(this.mindMapPanel);
      }
    }
    catch (URISyntaxException ex) {
      Logger.error("Can't make URI to the file", ex); //NOI18N
    }
  }

  @Override
  public void drop(final DropTargetDropEvent dtde) {
    DataFlavor dataObject = null;
    for (final DataFlavor df : dtde.getCurrentDataFlavors()) {
      final Class<?> representation = df.getRepresentationClass();
      if (DataObject.class.isAssignableFrom(representation)) {
        dataObject = df;
        break;
      }
    }

    if (dataObject != null) {
      try {
        addDataObjectToElement((DataObject) dtde.getTransferable().getTransferData(dataObject), this.mindMapPanel.findTopicUnderPoint(dtde.getLocation()));
      }
      catch (UnsupportedFlavorException ex) {
        Logger.error("Can't get DataObject flavor", ex); //NOI18N
      }
      catch (IOException ex) {
        Logger.error("Can't extract DataObject", ex); //NOI18N
      }
    }
  }

  private String getRelativePathToProjectIfPossible(final FileObject obj) {
    String result = null;

    final Project proj = this.editorSupport.getProject();
    if (proj != null) {
      result = FileUtil.getRelativePath(proj.getProjectDirectory(), obj);
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
      if (DataObject.class.isAssignableFrom(dataClass)) {
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

    File projectDir = this.editorSupport.getProjectDirectory();

    final String path;

    if (file == null) {
      path = NbUtils.editFilePath(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.editFileLinkForTopic.dlgTitle"), projectDir, null);
    }
    else {
      final URI uri = file.getValue();
      final String origPath;
      if (uri.isAbsolute()) {
        origPath = Utilities.toFile(uri).getAbsolutePath();
      }
      else {
        if (projectDir == null) {
          NbUtils.msgWarn(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.editFileLinkForTopic.warnText"));
          origPath = Utilities.toFile(uri).getAbsolutePath();
        }
        else {
          origPath = Utilities.toFile(Utilities.toURI(projectDir).resolve(uri)).getAbsolutePath();
        }
      }
      path = NbUtils.editFilePath(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.editFileLinkForTopic.addPathTitle"), projectDir, origPath);
    }

    if (path != null) {
      if (path.isEmpty()) {
        topic.removeExtra(Extra.ExtraType.FILE);
        if (file != null) {
          this.mindMapPanel.invalidate();
          this.mindMapPanel.repaint();
          onMindMapModelChanged(this.mindMapPanel);
        }
      }
      else {
        final File filePath = new File(path);
        if (filePath.isFile()) {
          final FileObject fileObj = FileUtil.toFileObject(filePath);
          final String relativePath = NbUtils.getPreferences().getBoolean("makeRelativePathToProject", true) ? getRelativePathToProjectIfPossible(fileObj) : null; //NOI18N

          URI value = null;
          if (relativePath == null) {
            value = fileObj.toURI();
          }
          else {
            try {
              value = new URI(relativePath);
            }
            catch (URISyntaxException ex) {
              Logger.error("Can't convert file path to URI", ex); //NOI18N
              NbUtils.msgError(String.format(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.editFileLinkForTopic.errorCantConvertFilePath"), relativePath));
            }
          }

          if (value != null) {
            topic.setExtra(new ExtraFile(value));
            this.mindMapPanel.invalidate();
            this.mindMapPanel.repaint();
            onMindMapModelChanged(this.mindMapPanel);
          }
        }
        else {
          NbUtils.msgError(String.format(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.editFileLinkForTopic.errorCantFindFile"), path));
        }
      }
    }

  }

  private void editTopicLinkForTopic(final Topic topic) {
    final ExtraTopic link = (ExtraTopic) topic.getExtras().get(Extra.ExtraType.TOPIC);

    ExtraTopic result = null;

    final ExtraTopic remove = new ExtraTopic("_______"); //NOI18N

    if (link == null) {
      final MindMapTreePanel panel = new MindMapTreePanel(this.mindMapPanel.getModel(), null, true, null);
      if (NbUtils.plainMessageOkCancel(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.editTopicLinkForTopic.dlgSelectTopicTitle"), panel)) {
        final Topic selected = panel.getSelectedTopic();
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
      if (NbUtils.plainMessageOkCancel(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.editTopicLinkForTopic.dlgEditSelectedTitle"), panel)) {
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
    final URI result;
    if (link == null) {
      // create new
      result = NbUtils.editURI(String.format(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.editLinkForTopic.dlgAddURITitle"), Utils.makeShortTextVersion(topic.getText(), 16)), null);
    }
    else {
      // edit
      result = NbUtils.editURI(String.format(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.editLinkForTopic.dlgEditURITitle"), Utils.makeShortTextVersion(topic.getText(), 16)), link.getValue());
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
      result = NbUtils.editText(String.format(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.editTextForTopic.dlfAddNoteTitle"), Utils.makeShortTextVersion(topic.getText(), 16)), ""); //NOI18N
    }
    else {
      // edit
      result = NbUtils.editText(String.format(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.editTextForTopic.dlgEditNoteTitle"), Utils.makeShortTextVersion(topic.getText(), 16)), note.getValue());
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
  public JPopupMenu makePopUp(final Point point, final AbstractElement element, final ElementPart partUnderMouse) {
    final JPopupMenu result = new JPopupMenu();

    if (element != null) {
      final JMenuItem editText = new JMenuItem(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.makePopUp.miEditText"), Icons.EDITTEXT.getIcon());
      editText.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          mindMapPanel.startEdit(element);
        }
      });

      result.add(editText);

      final JMenuItem addChild = new JMenuItem(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.makePopUp.miAddChild"), Icons.ADD.getIcon());
      addChild.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          mindMapPanel.makeNewChildAndStartEdit(element.getModel(), null);
        }
      });

      result.add(addChild);
    }

    if (element != null || this.mindMapPanel.hasSelectedTopics()) {
      final JMenuItem deleteItem = new JMenuItem(this.mindMapPanel.hasSelectedTopics() ? java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.makePopUp.miRemoveSelectedTopics") : java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.makePopUp.miRemoveTheTopic"), Icons.DELETE.getIcon());
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
      final Topic theTopic = this.mindMapPanel.getFirstSelected() == null ? element.getModel() : this.mindMapPanel.getFirstSelected();
      if (theTopic.getParent() != null) {
        final JMenuItem cloneItem = new JMenuItem(this.mindMapPanel.hasSelectedTopics() ? java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.makePopUp.miCloneSelectedTopic") : java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.makePopUp.miCloneTheTopic"), Icons.CLONE.getIcon());
        cloneItem.addActionListener(new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            mindMapPanel.cloneTopic(theTopic);
          }
        });

        result.add(cloneItem);
      }
    }

    if (result.getComponentCount() > 0) {
      result.add(new JSeparator());
    }

    if (element != null) {
      final Topic topic = element.getModel();

      final JMenuItem editText = new JMenuItem(topic.getExtras().containsKey(Extra.ExtraType.NOTE) ? java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.makePopUp.miEditNote") : java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.makePopUp.miAddNote"), Icons.NOTE.getIcon());
      editText.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
          editTextForTopic(topic);
        }
      });

      result.add(editText);

      final JMenuItem editLink = new JMenuItem(topic.getExtras().containsKey(Extra.ExtraType.LINK) ? java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.makePopUp.miEditURI") : java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.makePopUp.miAddURI"), Icons.URL.getIcon());
      editLink.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          editLinkForTopic(topic);
        }
      });

      result.add(editLink);

      final JMenuItem editTopicLink = new JMenuItem(topic.getExtras().containsKey(Extra.ExtraType.TOPIC) ? java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.makePopUp.miEditTransition") : java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.makePopUp.miAddTransition"), Icons.TOPIC.getIcon());
      editTopicLink.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          editTopicLinkForTopic(topic);
        }
      });

      result.add(editTopicLink);

      final JMenuItem editFileLink = new JMenuItem(topic.getExtras().containsKey(Extra.ExtraType.FILE) ? java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.makePopUp.miEditFile") : java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.makePopUp.miAddFile"), Icons.FILE.getIcon());
      editFileLink.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          editFileLinkForTopic(topic);
        }
      });

      result.add(editFileLink);
    }

    if (result.getComponentCount() > 0) {
      result.add(new JSeparator());
    }

    final JMenuItem expandAll = new JMenuItem(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.makePopUp.miExpandAll"), Icons.EXPANDALL.getIcon());
    expandAll.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        mindMapPanel.collapseOrExpandAll(false);
      }

    });

    final JMenuItem collapseAll = new JMenuItem(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.makePopUp.miCollapseAll"), Icons.COLLAPSEALL.getIcon());
    collapseAll.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        mindMapPanel.collapseOrExpandAll(true);
      }

    });

    result.add(expandAll);
    result.add(collapseAll);

    if (result.getComponentCount() > 0) {
      result.add(new JSeparator());
    }
    final JMenu exportMenu = new JMenu(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.makePopUp.miExportMapAs"));
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
            exp.doExport(mindMapPanel);
          }
          catch (Exception ex) {
            Logger.error("Error during map export", ex); //NOI18N
            NbUtils.msgError(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.makePopUp.errMsgCantExport"));
          }
        }
      });
      exportMenu.add(item);
    }
    result.add(exportMenu);

    result.add(new JSeparator());

    JMenuItem optionsMenu = new JMenuItem(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.makePopUp.miOptions"), Icons.OPTIONS.getIcon());
    optionsMenu.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        OptionsDisplayer.getDefault().open("mmd-config-main"); //NOI18N
      }
    });

    result.add(optionsMenu);

    JMenuItem infoMenu = new JMenuItem(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.makePopUp.miAbout"), Icons.INFO.getIcon());
    infoMenu.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        NbUtils.plainMessageOk(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("MMDGraphEditor.makePopUp.msgAboutTitle"), new AboutPanel());
      }
    });

    result.add(infoMenu);

    return result;
  }

  @Override
  public int getPersistenceType() {
    return TopComponent.PERSISTENCE_NEVER;
  }

  public void focusToPath(final int[] positionPath) {
    this.mindMapPanel.removeAllSelection();
    final Topic topic = this.mindMapPanel.getModel().findForPositionPath(positionPath);
    if (topic != null) {
      this.mindMapPanel.select(topic, false);
    }
  }
}
