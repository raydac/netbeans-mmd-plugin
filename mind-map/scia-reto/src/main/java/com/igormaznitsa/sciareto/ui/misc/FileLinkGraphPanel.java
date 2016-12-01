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
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.StringReader;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import com.google.common.base.Function;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.sciareto.ui.MapUtils;
import com.igormaznitsa.sciareto.ui.UiUtils;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ViewScalingControl;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;

public class FileLinkGraphPanel extends javax.swing.JPanel {

  private static final long serialVersionUID = -5145163577941732908L;

  private static final Logger LOGGER = LoggerFactory.getLogger(FileLinkGraphPanel.class);

  private FileVertex selectedVertex;

  private enum FileVertexType {
    FOLDER("folder.png"), DOCUMENT("document.png"), MINDMAP("mindmap.png"), UNKNOWN("unknown.png");

    private final Icon icon;

    private FileVertexType(@Nonnull final String icon) {
      this.icon = new ImageIcon(UiUtils.loadIcon("graph/" + icon));
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

      final String name = FilenameUtils.getBaseName(file.getName());
      this.text = name.isEmpty() ? file.getName() : name;

      this.tooltip = FilenameUtils.normalizeNoEndSeparator(file.getAbsolutePath());
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

    if (startMindMap != null) {
      addMindMapAndFillByItsLinks(result, projectFolder, startMindMap, new AtomicInteger());
    } else if (projectFolder != null) {
      final Iterator<File> iterator = FileUtils.iterateFiles(projectFolder, new String[]{"mmd"}, true);
      while (iterator.hasNext()) {
        final File mmdFile = iterator.next();
        if (mmdFile.isFile()) {
          addMindMapAndFillByItsLinks(result, projectFolder, mmdFile, new AtomicInteger());
        }
      }
    }

    return result;
  }

  @Nullable
  private static FileVertex addMindMapAndFillByItsLinks(@Nonnull final @Nullable Graph<FileVertex, Number> graph, @Nullable final File projectFolder, @Nonnull final File mindMapFile, @Nonnull final AtomicInteger edgeCounter) {

    MindMap map;

    FileVertex thisVertex;

    try {
      thisVertex = new FileVertex(mindMapFile, FileVertexType.MINDMAP);
      if (graph.containsVertex(thisVertex)) {
        return null;
      }
      map = new MindMap(null, new StringReader(FileUtils.readFileToString(mindMapFile, "UTF-8")));
    }
    catch (final Exception ex) {
      LOGGER.error("Can't load mind map : " + mindMapFile, ex);
      thisVertex = new FileVertex(mindMapFile, FileVertexType.UNKNOWN);
      map = null;
    }

    graph.addVertex(thisVertex);

    if (map != null) {
      for (final File f : MapUtils.extractAllFileLinks(projectFolder, map)) {
        final FileVertex that;
        if (f.isDirectory()) {
          that = new FileVertex(f, FileVertexType.FOLDER);
        } else if (f.isFile()) {
          if (f.getName().endsWith(".mmd")) {
            that = addMindMapAndFillByItsLinks(graph, projectFolder, f, edgeCounter);
          } else {
            that = new FileVertex(f, FileVertexType.DOCUMENT);
          }
        } else {
          that = new FileVertex(f, FileVertexType.UNKNOWN);
        }

        if (that != null) {
          graph.addEdge(edgeCounter.getAndIncrement(), thisVertex, that, EdgeType.DIRECTED);
        }
      }
    }

    return thisVertex;
  }

  public FileLinkGraphPanel(@Nullable final File projectFolder, @Nullable final File startMindMap) {
    initComponents();

    final Dimension SCROLL_COMPONENT_SIZE = new Dimension(600, 400);

    final Graph<FileVertex, Number> graph = makeGraph(projectFolder, startMindMap);
    final FRLayout<FileVertex, Number> graphLayout = new FRLayout<FileVertex, Number>(graph, new Dimension(500, 500));
    final VisualizationModel<FileVertex, Number> viewModel = new DefaultVisualizationModel<FileVertex, Number>(graphLayout, new Dimension(2000, 2000));
    final VisualizationViewer<FileVertex, Number> graphViewer = new VisualizationViewer<FileVertex, Number>(viewModel);

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

    graphViewer.setBackground(Color.WHITE);
    graphViewer.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());

    final DefaultVertexLabelRenderer labelRenderer = new DefaultVertexLabelRenderer(Color.BLACK);

    graphViewer.getRenderContext().setVertexLabelRenderer(labelRenderer);
    graphViewer.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.S);

    final Function<Number, Paint> edgePaintTransformer = new Function<Number, Paint>() {
      @Override
      public Paint apply(@Nonnull final Number input) {
        return Color.lightGray;
      }
    };

    graphViewer.getRenderContext().setEdgeDrawPaintTransformer(edgePaintTransformer);

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
        if (!me.isPopupTrigger() && me.getClickCount() > 1) {
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

    this.add(scroll, BorderLayout.CENTER);

    final FileLinkGraphPanel theIntance = this;
    this.addHierarchyListener(new HierarchyListener() {
      @Override
      public void hierarchyChanged(@Nonnull final HierarchyEvent e) {
        Window window = SwingUtilities.getWindowAncestor(theIntance);
        if (window instanceof Dialog) {
          final Dialog dialog = (Dialog) window;
          if (!dialog.isResizable()) {
            dialog.setResizable(true);
            dialog.addComponentListener(new ComponentAdapter() {
              @Override
              public void componentResized(ComponentEvent e) {
                scroll.invalidate();
                scroll.doLayout();
              }
            });
          }
        }
      }
    });
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
