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
package com.igormaznitsa.nbmindmap.nb.gui;

import com.igormaznitsa.nbmindmap.gui.MindMapListener;
import com.igormaznitsa.nbmindmap.gui.MindMapPanel;
import com.igormaznitsa.nbmindmap.gui.mmview.AbstractElement;
import com.igormaznitsa.nbmindmap.model.Extra;
import com.igormaznitsa.nbmindmap.model.MindMap;
import com.igormaznitsa.nbmindmap.model.MindMapTopic;
import com.igormaznitsa.nbmindmap.nb.dataobj.MMDDataObject;
import com.igormaznitsa.nbmindmap.nb.dataobj.MMDEditorSupport;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.text.StyledDocument;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

public class MMDGraphEditor extends CloneableTopComponent implements MultiViewElement, MindMapListener, Runnable {

  private static final long serialVersionUID = -8776707243607267446L;

  private JToolBar toolbar;
  private MultiViewElementCallback callback;
  private MMDEditorSupport editorSupport;

  private final JScrollPane mainScrollPane;
  private final MindMapPanel mindMapPanel;

  public MMDGraphEditor(final MMDEditorSupport support) {
    super();
    this.editorSupport = support;

    this.mainScrollPane = new JScrollPane();
    this.mindMapPanel = new MindMapPanel();
    this.mindMapPanel.addMindMapListener(this);

    this.mainScrollPane.setViewportView(this.mindMapPanel);

    this.setLayout(new BorderLayout(0, 0));
    this.add(this.mainScrollPane, BorderLayout.CENTER);
  }

  @Override
  public JComponent getVisualRepresentation() {
    return this;
  }

  @Override
  public void componentActivated() {
  }

  @Override
  public void componentClosed() {
  }

  @Override
  public void componentOpened() {
  }

  @Override
  public void componentShowing() {
    updateModel();
  }

  @Override
  public void componentDeactivated() {
  }

  @Override
  public void componentHidden() {
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
      catch (IOException ex) {
        this.mindMapPanel.setErrorText("Can't parse document");
      }
    }
  }

  @Override
  public JComponent getToolbarRepresentation() {
    if (this.toolbar == null) {
      this.toolbar = new JToolBar();
    }
    return this.toolbar;
  }

  @Override
  public void setMultiViewCallback(final MultiViewElementCallback cllback) {
    this.callback = cllback;
    updateName();
  }

  public void updateName() {
    final MMDEditorSupport ces = this.editorSupport;

    if (ces != null) {
      Mutex.EVENT.writeAccess(
              new Runnable() {
                @Override
                public void run() {
                  String name = ces.messageHtmlName();
                  setHtmlDisplayName(name);
                  name = ces.messageName();
                  setDisplayName(name);
                  setName(name);
                  setToolTipText(ces.messageToolTip());
                }
              }
      );
    }
  }

  @Override
  public int getPersistenceType() {
    return PERSISTENCE_NEVER;
  }

  @Override
  public CloseOperationState canCloseElement() {
    return CloseOperationState.STATE_OK;
  }

  @Override
  public void run() {
    final MultiViewElementCallback c = this.callback;
    if (c == null) {
      return;
    }
    TopComponent tc = c.getTopComponent();
    if (tc == null) {
      return;
    }
    updateName();
    tc.setName(this.getName());
    tc.setDisplayName(this.getDisplayName());
    tc.setHtmlDisplayName(this.getHtmlDisplayName());
  }

  @Override
  public Lookup getLookup() {
    return ((MMDDataObject) (this.editorSupport).getDataObject()).getNodeDelegate().getLookup();
  }

  @Override
  public void onMindMapModelChanged(final MindMapPanel source) {
    try {
      final StringWriter writer = new StringWriter(16384);
      this.mindMapPanel.getModel().write(writer);
      final String text = writer.toString();
      
      final StyledDocument doc = this.editorSupport.getDocument();
      doc.remove(0, doc.getLength());
      doc.insertString(0, text, null);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void onMindMapModelRealigned(final MindMapPanel source, final Dimension coveredAreaSize) {
  }

  @Override
  public void onEnsureVisibilityOfTopic(final MindMapPanel source, final MindMapTopic topic) {
  }

  @Override
  public void onClickOnExtra(final MindMapPanel source, final MindMapTopic topic, final Extra<?> extra) {
  }

  @Override
  public void onChangedSelection(final MindMapPanel source, final MindMapTopic[] currentSelectedTopics) {
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

  public static void main(String... args) throws Exception {
    final JFrame frame = new JFrame("Test");
    frame.setSize(500, 500);
    frame.setLocationRelativeTo(null);

    final JScrollPane panel = new JScrollPane();

    final MindMapPanel pp = new MindMapPanel();
    pp.addComponentListener(new ComponentAdapter() {

      @Override
      public void componentResized(ComponentEvent e) {
        processEditorResizing(pp);
      }

    });

//    final MindMap map = new MindMap(new StringReader("some\n------\n# HelloWorld\n##rrr\n##rrr\n###GGG\n####HHH\n#####JJKKLL\n## leve1.1\n> leftSide=\"true\"\n## leve1.1\n> leftSide=\"true\"\n"));
//    final MindMap map = new MindMap(new StringReader("some\n------\n# HelloWorld\n## Some\n## Some\n## Some\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n### AAAA\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n## Some\n"));
    final MindMap map = new MindMap(new StringReader("some\n------\n# HelloWorld\n- LINK\n```http://www.color.com```\n- NOTE\n```Hello world note```\n## Some\n- NOTE\n```Hello world note```\n- LINK\n```http://www.color.com```\n### SSS\n- NOTE\n```hhh```\n"));
    pp.setModel(map);

    pp.addMindMapListener(new MindMapListener() {

      @Override
      public void onEnsureVisibilityOfTopic(MindMapPanel source, MindMapTopic topic) {
        moveVisibleRectToElement(panel, source, (AbstractElement) topic.getPayload());
      }

      @Override
      public void onMindMapModelChanged(MindMapPanel source) {
      }

      @Override
      public void onMindMapModelRealigned(MindMapPanel source, Dimension coveredAreaSize) {
        panel.getViewport().revalidate();
      }

      @Override
      public void onClickOnExtra(MindMapPanel panel, MindMapTopic topic, Extra<?> extra) {
        System.out.println("EXTRAS: " + extra);
      }

      @Override
      public void onChangedSelection(MindMapPanel source, MindMapTopic[] currentSelectedTopics) {
      }

    });

    panel.setViewportView(pp);

    frame.setContentPane(panel);

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }

}
