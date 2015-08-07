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

import com.igormaznitsa.nbmindmap.utils.NbUtils;
import com.igormaznitsa.nbmindmap.mmgui.MindMapListener;
import com.igormaznitsa.nbmindmap.mmgui.MindMapPanel;
import com.igormaznitsa.nbmindmap.mmgui.AbstractElement;
import com.igormaznitsa.nbmindmap.model.Extra;
import com.igormaznitsa.nbmindmap.model.ExtraFile;
import com.igormaznitsa.nbmindmap.model.MindMap;
import com.igormaznitsa.nbmindmap.model.Topic;
import com.igormaznitsa.nbmindmap.utils.Logger;
import java.awt.BorderLayout;
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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import org.netbeans.api.actions.Openable;
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
import org.openide.windows.TopComponent;
import static org.openide.windows.TopComponent.PERSISTENCE_NEVER;

@MultiViewElement.Registration(
        displayName = "Graph",
        mimeType = MMDDataObject.MIME,
        persistenceType = PERSISTENCE_NEVER,
        iconBase = "com/igormaznitsa/nbmindmap/icons/nbmm16.png",
        preferredID = MMDGraphEditor.ID, 
        position = 100
)
public final class MMDGraphEditor extends CloneableEditor implements MultiViewElement, MindMapListener, DropTargetListener {
  private static final long serialVersionUID = -8776707243607267446L;

  public static final String ID = "mmd-graph-editor";
  
  private MultiViewElementCallback callback;
  private final MMDEditorSupport editorSupport;

  private final JScrollPane mainScrollPane;
  private final MindMapPanel mindMapPanel;

  private boolean dragAcceptableType = false;

  private final JToolBar toolBar;

  public MMDGraphEditor(final MMDEditorSupport support) {
    super(support);
    
    this.toolBar = makeToolBar();
    
    this.editorSupport = support;

    this.mainScrollPane = new JScrollPane();
    this.mindMapPanel = new MindMapPanel();
    this.mindMapPanel.addMindMapListener(this);

    this.mindMapPanel.setDropTarget(new DropTarget(this.mindMapPanel, this));

    this.mainScrollPane.setViewportView(this.mindMapPanel);

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

  private JToolBar makeToolBar(){
    return new JToolBar();
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
    final MMDEditorSupport ces = this.editorSupport;
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
      this.mindMapPanel.setErrorText("Can't load document");
    }
    else {
      try {
        this.mindMapPanel.setModel(new MindMap(new StringReader(text)));
      }
      catch (IllegalArgumentException ex) {
        Logger.warn("Can't detect mind map");
        this.mindMapPanel.setErrorText("Text doesn't contain mind map description");
      }
      catch (IOException ex) {
        Logger.error("Can't parse mind map text", ex);
        this.mindMapPanel.setErrorText("Can't parse document");
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
  public void onMindMapModelChanged(final MindMapPanel source) {
    try {
      final StringWriter writer = new StringWriter(16384);
      this.mindMapPanel.getModel().write(writer);
      this.editorSupport.replaceDocumentText(writer.toString());
      this.editorSupport.notifyModified();
    }
    catch (Exception ex) {
      Logger.error("Can't get document text", ex);
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
  }

  @Override
  public void onClickOnExtra(final MindMapPanel source, final Topic topic, final Extra<?> extra) {
    switch (extra.getType()) {
      case FILE: {
        NbUtils.editText("Test edit", "kjflksd sdf;sdf; sdfsd sdfpwe]r\n sdf ;dfsdl;kfjlsdjf weiopr \njf sdf lsdkjf lsdjf ower oweij \nladjf ldjflasdj dflasdj foeuropiwe \nrowejr lej \nflsdjk flsdf ");

        final FileObject fileObj;
        try {
          final URI uri = (URI) extra.getValue();
          if (uri.isAbsolute()) {
            fileObj = FileUtil.toFileObject(new File(uri));
          }
          else {
            fileObj = this.editorSupport.makeRelativePathToProjectRoot(uri.getPath());
            if (fileObj == null) {
              NbUtils.msgError("Can't find file at project: " + uri.getPath());
              return;
            }
          }
        }
        catch (Exception ex) {
          NbUtils.msgError("Wrong file path : " + extra.getValue().toString());
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
          Logger.error("Cant't find data object", ex);
        }
      }
      break;
      case LINK: {
      }
      break;
      case NOTE: {
      }
      break;
      case TOPIC: {
        final Topic theTopic = this.mindMapPanel.getModel().findTopicForUID((String) extra.getValue());
        if (theTopic == null) {
          // not presented
          NbUtils.msgWarn("Can't find the topic, may be it was removed");
        }
        else {
          // detected
          this.mindMapPanel.focusTo(theTopic);
        }
      }
      break;
    }
  }

  @Override
  public void onChangedSelection(final MindMapPanel source, final Topic[] currentSelectedTopics) {
  }

  private static void processEditorResizing(final MindMapPanel panel) {
    panel.endEdit(false);
    panel.revalidate();
    panel.repaint();
  }

  private static void moveVisibleRectToElement(final JScrollPane pane, final MindMapPanel mmPanel, final AbstractElement e) {
    if (e != null) {
      final Rectangle componentRect = e.getBounds().getBounds();

      final Rectangle visibleRect = pane.getViewport().getViewRect();

      final int xoffset = (visibleRect.width - componentRect.width) / 2;
      final int yoffset = (visibleRect.height - componentRect.height) / 2;

      int px = Math.max(0, componentRect.x - xoffset);
      int py = Math.max(0, componentRect.y - yoffset);

      final Dimension preferredSize = mmPanel.getPreferredSize();
      pane.getViewport().setViewPosition(new Point(px, py));
    }
  }

//  public static void main(String... args) throws Exception {
//    final JFrame frame = new JFrame("Test");
//    frame.setSize(500, 500);
//    frame.setLocationRelativeTo(null);
//
//    final JScrollPane panel = new JScrollPane();
//
//    final MindMapPanel pp = new MindMapPanel();
//    pp.addComponentListener(new ComponentAdapter() {
//
//      @Override
//      public void componentResized(ComponentEvent e) {
//        processEditorResizing(pp);
//      }
//
//    });
//
////    final MindMap map = new MindMap(new StringReader("some\n------\n# HelloWorld\n##rrr\n##rrr\n###GGG\n####HHH\n#####JJKKLL\n## leve1.1\n> leftSide=\"true\"\n## leve1.1\n> leftSide=\"true\"\n"));
////    final MindMap map = new MindMap(new StringReader("some\n------\n# HelloWorld\n## Some\n## Some\n## Some\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n"));
//    final MindMap map = new MindMap(new StringReader("some\n------\n# HelloWorld\n- LINK\n```http://www.color.com```\n- NOTE\n```Hello world note```\n## Some\n- NOTE\n```Hello world note```\n- LINK\n```http://www.color.com```\n### SSS\n- NOTE\n```hhh```\n"));
//    pp.setModel(map);
//
//    pp.addMindMapListener(new MindMapListener() {
//
//      @Override
//      public void onEnsureVisibilityOfTopic(MindMapPanel source, Topic topic) {
//        moveVisibleRectToElement(panel, source, (AbstractElement) topic.getPayload());
//      }
//
//      @Override
//      public void onMindMapModelChanged(MindMapPanel source) {
//      }
//
//      @Override
//      public void onMindMapModelRealigned(MindMapPanel source, Dimension coveredAreaSize) {
//        panel.getViewport().revalidate();
//      }
//
//      @Override
//      public void onClickOnExtra(MindMapPanel panel, Topic topic, Extra<?> extra) {
//      }
//
//      @Override
//      public void onChangedSelection(MindMapPanel source, Topic[] currentSelectedTopics) {
//      }
//
//    });
//
//    panel.setViewportView(pp);
//
//    frame.setContentPane(panel);
//
//    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//    frame.setVisible(true);
//  }

  public void updateView() {
    this.updateModel();
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
    return ImageUtilities.loadImage("com/igormaznitsa/nbmindmap/icons/nbmm16.png");
  }

  @Override
  public void dropActionChanged(DropTargetDragEvent dtde) {
  }

  @Override
  public void dragExit(DropTargetEvent dte) {
  }

  @Override
  public void drop(final DropTargetDropEvent dtde) {
    DataFlavor data = null;
    for (final DataFlavor df : dtde.getCurrentDataFlavors()) {
      if (DataObject.class.isAssignableFrom(df.getRepresentationClass())) {
        data = df;
        break;
      }
    }

    if (data != null) {
      try {
        final DataObject dataObj = (DataObject) dtde.getTransferable().getTransferData(data);
        final AbstractElement destination = this.mindMapPanel.findTopicUnderPoint(dtde.getLocation());
        if (dataObj != null && destination != null) {

          final FileObject fileObj = dataObj.getPrimaryFile();
          final String relativePath = getRelativePathToProjectIfPossible(fileObj);

          final URI uri;
          if (relativePath != null) {
            uri = new URI(relativePath.replace('\\', '/'));
          }
          else {
            uri = fileObj.toURI();
          }

          final Topic topic = destination.getModel();

          if (topic.getExtras().containsKey(Extra.ExtraType.FILE)) {
            if (!NbUtils.msgConfirmOkCancel("Mind Map File link", "Replace existing file link in the topic?")) {
              return;
            }
          }

          destination.getModel().setExtra(new ExtraFile(uri));
          this.mindMapPanel.invalidate();
          this.mindMapPanel.repaint();
          onMindMapModelChanged(this.mindMapPanel);
        }
      }
      catch (Exception ex) {
        Logger.error("Can't extract data from dragged object", ex);
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

}
