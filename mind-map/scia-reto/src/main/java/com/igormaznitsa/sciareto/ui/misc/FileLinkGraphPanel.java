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
package com.igormaznitsa.sciareto.ui.misc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import com.google.common.base.Function;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.sciareto.ui.MapUtils;
import com.igormaznitsa.sciareto.ui.UiUtils;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.LayoutScalingControl;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ViewScalingControl;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;

public final class FileLinkGraphPanel extends javax.swing.JPanel {

  private static final long serialVersionUID = -5145163577941732908L;

  private static final Logger LOGGER = LoggerFactory.getLogger(FileLinkGraphPanel.class);

  private FileVertex selectedVertex;

  private static final Color COLOR_BACKGROUND = Color.WHITE;
  private static final Color COLOR_ARROW = Color.ORANGE.darker();
  private static final Color COLOR_LABELS = Color.BLACK;
  
  private static final Icon RELAYOUT_ICON = new ImageIcon(UiUtils.loadIcon("graph16.png")); //NOI18N
  
  public enum FileVertexType {
    FOLDER("folder.png","Folder"), DOCUMENT("document.png","Document"), MINDMAP("mindmap.png","Mind Map"), UNKNOWN("unknown.png","Unknown"), NOTFOUND("notfound.png","Not found"); //NOI18N

    private final Icon icon;
    private final String text;

    private FileVertexType(@Nonnull final String icon, @Nonnull final String text) {
      this.icon = new ImageIcon(UiUtils.loadIcon("graph/" + icon)); //NOI18N
      this.text = text;
    }

    @Override
    public String toString(){
      return text;
    }
    
    @Nonnull
    public Icon getIcon() {
      return this.icon;
    }
  }

  public static final class FileVertex {

    private final String text;
    private final String tooltip;
    private final FileVertexType type;
    private final File file;

    public FileVertex(@Nonnull final File file, @Nonnull final FileVertexType type) {
      this.type = type;
      this.text = file.getName();
      this.tooltip = "<html><b>"+type.toString()+"</b><br>"+StringEscapeUtils.unescapeHtml(FilenameUtils.normalizeNoEndSeparator(file.getAbsolutePath()))+"</html>"; //NOI18N
      this.file = file;
    }

    @Override
    public int hashCode() {
      return this.file.hashCode();
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
      if (obj == null) {
        return false;
      }
      if (obj == this) {
        return true;
      }
      return obj instanceof FileVertex && ((FileVertex) obj).file.equals(this.file);
    }

    @Nonnull
    @Override
    public String toString() {
      return this.text;
    }

    @Nonnull
    public String getTooltip() {
      return this.tooltip;
    }

    @Nonnull
    public File getFile() {
      return this.file;
    }

    @Nonnull
    public FileVertexType getType() {
      return this.type;
    }
  }

  @Nonnull
  private Graph<FileVertex, Number> makeGraph(@Nullable final File projectFolder, @Nullable final File startMindMap) {
    final DirectedSparseGraph<FileVertex, Number> result = new DirectedSparseGraph<FileVertex, Number>();

    final AtomicInteger edgeCounter = new AtomicInteger();

    final Set<File> mapFilesInProcessing = new HashSet<File>();

    if (startMindMap != null) {
      addMindMapAndFillByItsLinks(null, result, projectFolder, startMindMap, edgeCounter, mapFilesInProcessing);
    } else if (projectFolder != null) {
      final Iterator<File> iterator = FileUtils.iterateFiles(projectFolder, new String[]{"mmd"}, true); //NOI18N
      while (iterator.hasNext()) {
        final File mmdFile = iterator.next();
        if (mmdFile.isFile()) {
          addMindMapAndFillByItsLinks(null, result, projectFolder, mmdFile, edgeCounter, mapFilesInProcessing);
        }
      }
    }

    return result;
  }

  @Nullable
  private static FileVertex addMindMapAndFillByItsLinks(@Nullable final FileVertex parent, @Nonnull final @Nullable Graph<FileVertex, Number> graph, @Nullable final File projectFolder, @Nonnull final File mindMapFile, @Nonnull final AtomicInteger edgeCounter, @Nonnull Set<File> mapFilesInProcessing) {

    MindMap map;

    FileVertex thisVertex;

    try {

      thisVertex = new FileVertex(mindMapFile, FileVertexType.MINDMAP);
      map = new MindMap(null, new StringReader(FileUtils.readFileToString(mindMapFile, "UTF-8"))); //NOI18N

      if (parent != null) {
        for (final MMapURI fileUri : MapUtils.extractAllFileLinks(map)) {
          if (parent.getFile().equals(fileUri.asFile(projectFolder))) {
            graph.addEdge(edgeCounter.getAndIncrement(), thisVertex, parent, EdgeType.DIRECTED);
            break;
          }
        }
        if (mapFilesInProcessing.contains(mindMapFile)) {
          return null;
        }
      }
    }
    catch (final Exception ex) {
      LOGGER.error("Can't load mind map : " + mindMapFile, ex); //NOI18N
      thisVertex = new FileVertex(mindMapFile, FileVertexType.UNKNOWN);
      map = null;
    }

    mapFilesInProcessing.add(mindMapFile);

    graph.addVertex(thisVertex);

    if (map != null) {
      for (final MMapURI fileUri : MapUtils.extractAllFileLinks(map)) {
        final FileVertex that;

        final File convertedFile = convertUriInFile(mindMapFile, projectFolder, fileUri);

        if (convertedFile == null) {
          that = new FileVertex(fileUri.asFile(projectFolder), FileVertexType.NOTFOUND);
        } else if (convertedFile.isDirectory()) {
          that = new FileVertex(convertedFile, FileVertexType.FOLDER);
        } else if (convertedFile.isFile()) {

          if (convertedFile.getName().endsWith(".mmd")) { //NOI18N
            if (convertedFile.equals(mindMapFile)) {
              that = thisVertex;
            } else {
              that = addMindMapAndFillByItsLinks(thisVertex, graph, projectFolder, convertedFile, edgeCounter, mapFilesInProcessing);
            }
          } else {
            that = new FileVertex(convertedFile, FileVertexType.DOCUMENT);
          }

        } else {
          that = new FileVertex(convertedFile, convertedFile.exists() ? FileVertexType.UNKNOWN : FileVertexType.NOTFOUND);
        }

        if (that != null) {
          graph.addEdge(edgeCounter.getAndIncrement(), thisVertex, that, EdgeType.DIRECTED);
        }
      }
    }

    return thisVertex;
  }

  @Nullable
  private static File convertUriInFile(@Nonnull final File containingMindMap, @Nullable final File baseFolder, @Nonnull final MMapURI uri) {
    File result = uri.asFile(baseFolder);

    if (!uri.isAbsolute() && !result.exists()) {
      File basePath =  com.igormaznitsa.sciareto.ui.FileUtils.removeLastElementInPath(containingMindMap);
      do {
        result = uri.asFile(basePath);
        if (result.exists()) {
          break;
        }
        result = null;
        basePath = com.igormaznitsa.sciareto.ui.FileUtils.removeLastElementInPath(basePath);
      }while(!com.igormaznitsa.sciareto.ui.FileUtils.isRootFile(basePath));
    }

    return result;
  }

  public FileLinkGraphPanel(@Nullable final File projectFolder, @Nullable final File startMindMap) {
    initComponents();

    final Dimension SCROLL_COMPONENT_SIZE = new Dimension(600, 450);

    final Graph<FileVertex, Number> graph = makeGraph(projectFolder, startMindMap);
    
    final ISOMLayout<FileVertex, Number> graphLayout = new ISOMLayout<FileVertex, Number>(graph);
    
    final VisualizationModel<FileVertex, Number> viewModel = new DefaultVisualizationModel<FileVertex, Number>(graphLayout, new Dimension(2000, 2000));
    final VisualizationViewer<FileVertex, Number> graphViewer = new VisualizationViewer<FileVertex, Number>(viewModel,new Dimension(800, 800));

    final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse() {
      @Override
      protected void loadPlugins() {
        this.scalingPlugin = new ScalingGraphMousePlugin(new ViewScalingControl(), 0);
        this.pickingPlugin = new PickingGraphMousePlugin();
        add(this.scalingPlugin);
        add(this.pickingPlugin);
        setMode(Mode.PICKING);
      }

    };
    graphViewer.setGraphMouse(graphMouse);

    graphViewer.getRenderContext().setVertexIconTransformer(new Function<FileVertex, Icon>() {
      @Override
      public Icon apply(@Nonnull final FileVertex f) {
        return f.getType().getIcon();
      }
    });

    graphViewer.setBackground(COLOR_BACKGROUND);
    graphViewer.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());

    final DefaultVertexLabelRenderer labelRenderer = new DefaultVertexLabelRenderer(COLOR_LABELS);

    graphViewer.getRenderContext().setVertexLabelRenderer(labelRenderer);
    graphViewer.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.S);

    final Function<Number, Paint> edgePaintTransformer = new Function<Number, Paint>() {
      @Override
      public Paint apply(@Nonnull final Number input) {
        return COLOR_ARROW;
      }
    };

    graphViewer.getRenderContext().setEdgeDrawPaintTransformer(edgePaintTransformer);
    graphViewer.getRenderContext().setArrowFillPaintTransformer(edgePaintTransformer);
    graphViewer.getRenderContext().setArrowDrawPaintTransformer(edgePaintTransformer);

    graphViewer.setVertexToolTipTransformer(new Function<FileVertex, String>() {
      @Override
      @Nonnull
      public String apply(@Nonnull final FileVertex f) {
        return f.getTooltip();
      }
    });

    graphViewer.addGraphMouseListener(new GraphMouseListener<FileVertex>() {
      @Override
      public void graphClicked(@Nonnull final FileVertex v, @Nonnull final MouseEvent me) {
        if (!me.isPopupTrigger() && me.getClickCount() > 1 && v.getType() != FileVertexType.NOTFOUND) {
          selectedVertex = v;
          final Window window = SwingUtilities.getWindowAncestor(graphViewer);
          if (window != null) {
            window.setVisible(false);
          }
        }
      }

      @Override
      public void graphPressed(@Nonnull final FileVertex v, @Nonnull final MouseEvent me) {
      }

      @Override
      public void graphReleased(@Nonnull final FileVertex v, @Nonnull final MouseEvent me) {
      }
    });

    final GraphZoomScrollPane scroll = new GraphZoomScrollPane(graphViewer);
    scroll.setPreferredSize(SCROLL_COMPONENT_SIZE);

    UiUtils.makeOwningDialogResizable(this);

    graphViewer.scaleToLayout(new LayoutScalingControl());

    final JButton layoutButton = new JButton(RELAYOUT_ICON);
    layoutButton.setToolTipText("Relayout graph");
    layoutButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    layoutButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(@Nonnull final ActionEvent e) {
       final Rectangle visible = scroll.getVisibleRect();
       graphViewer.getGraphLayout().reset();
       graphViewer.getGraphLayout().setSize(new Dimension(visible.width,visible.height));
       graphViewer.repaint();
      }
    });
    
    scroll.setCorner(layoutButton);
    
    this.add(scroll, BorderLayout.CENTER);
  }

  @Nullable
  public FileVertex getSelectedFile() {
    return this.selectedVertex;
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    setLayout(new java.awt.BorderLayout());
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  // End of variables declaration//GEN-END:variables
}
