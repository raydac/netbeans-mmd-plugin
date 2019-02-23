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
package com.igormaznitsa.mindmap.plugins.exporters;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.api.AbstractExporter;
import com.igormaznitsa.mindmap.plugins.api.HasOptions;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.Texts;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.MMGraphics;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.StrokeType;
import com.igormaznitsa.mindmap.swing.panel.utils.MindMapUtils;
import com.igormaznitsa.mindmap.swing.panel.utils.RenderQuality;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.mindmap.swing.services.IconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.text.DecimalFormat;

import static com.igormaznitsa.mindmap.swing.panel.MindMapPanel.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.SystemFlavorMap;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SVGImageExporter extends AbstractExporter {

  public static class SvgClip implements Transferable {

    private static final DataFlavor SVG_FLAVOR = new DataFlavor("image/svg+xml; class=java.io.InputStream", "Scalable Vector Graphic");
    final private String svgContent;

    private final DataFlavor[] supportedFlavors;

    public SvgClip(@Nonnull final String str) {
      this.supportedFlavors = new DataFlavor[]{
        SVG_FLAVOR,};

      this.svgContent = str;
      SystemFlavorMap systemFlavorMap = (SystemFlavorMap) SystemFlavorMap.getDefaultFlavorMap();
      DataFlavor dataFlavor = SVG_FLAVOR;
      systemFlavorMap.addUnencodedNativeForFlavor(dataFlavor, "image/svg+xml");
    }

    @Nonnull
    static DataFlavor getSVGFlavor() {
      return SvgClip.SVG_FLAVOR;
    }

    @Override
    public boolean isDataFlavorSupported(@Nonnull final DataFlavor flavor) {
      for (DataFlavor supported : this.supportedFlavors) {
        if (flavor.equals(supported)) {
          return true;
        }
      }
      return false;
    }

    @Override
    @Nonnull
    @MustNotContainNull
    public DataFlavor[] getTransferDataFlavors() {
      return this.supportedFlavors;
    }

    @Override
    @Nonnull
    public Object getTransferData(@Nonnull final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
      if (isDataFlavorSupported(flavor) && flavor.equals(SVG_FLAVOR)) {
        InputStream stream = new ByteArrayInputStream(this.svgContent.getBytes("UTF-8"));
        return stream;
      }
      throw new UnsupportedFlavorException(flavor);
    }

    public void lostOwnership(@Nonnull final Clipboard clipboard, @Nonnull final Transferable tr) {
    }
  }

  protected static final String FONT_CLASS_NAME = "mindMapTitleFont";

  private static final Map<String, String[]> LOCAL_FONT_MAP = new HashMap<String, String[]>() {
    {
      put("dialog", new String[]{"sans-serif", "SansSerif"});
      put("dialoginput", new String[]{"monospace", "Monospace"});
      put("monospaced", new String[]{"monospace", "Monospace"});
      put("serif", new String[]{"serif", "Serif"});
      put("sansserif", new String[]{"sans-serif", "SansSerif"});
      put("symbol", new String[]{"'WingDings'", "WingDings"});
    }
  };

  private static final Logger LOGGER = LoggerFactory.getLogger(SVGImageExporter.class);
  private static final UIComponentFactory UI_FACTORY = UIComponentFactoryProvider.findInstance();
  private static final Icon ICO = ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_EXPORT_SVG);
  private static final String SVG_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<!-- Generated by SVG Image Exporter plugin of NB Mind Map Swing panel -->\n<svg version=\"1.1\" baseProfile=\"tiny\" id=\"svg-root\" width=\"%d%%\" height=\"%d%%\" viewBox=\"0 0 %s %s\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">";
  private static final String NEXT_LINE = "\n";
  private static final DecimalFormat DOUBLE = new DecimalFormat("#.###");
  private boolean flagExpandAllNodes = false;
  private boolean flagDrawBackground = true;

  @Nonnull
  private static String dbl2str(final double value) {
    return DOUBLE.format(value);
  }

  @Override
  @Nullable
  public String getMnemonic() {
    return "svg";
  }

  @Override
  @Nullable
  public JComponent makeOptions() {
    final Options options = new Options(flagExpandAllNodes, flagDrawBackground);
    final JPanel panel = UI_FACTORY.makePanelWithOptions(options);
    final JCheckBox checkBoxExpandAll = UI_FACTORY.makeCheckBox();
    checkBoxExpandAll.setSelected(flagExpandAllNodes);
    checkBoxExpandAll.setText(Texts.getString("SvgExporter.optionUnfoldAll"));
    checkBoxExpandAll.setActionCommand("unfold");

    final JCheckBox checkBoxDrawBackground = UI_FACTORY.makeCheckBox();
    checkBoxDrawBackground.setSelected(flagDrawBackground);
    checkBoxDrawBackground.setText(Texts.getString("SvgExporter.optionDrawBackground"));
    checkBoxDrawBackground.setActionCommand("back");

    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    panel.add(checkBoxExpandAll);
    panel.add(checkBoxDrawBackground);

    panel.setBorder(BorderFactory.createEmptyBorder(16, 32, 16, 32));

    final ActionListener actionListener = new ActionListener() {
      @Override
      public void actionPerformed(@Nonnull final ActionEvent e) {
        if (e.getSource() == checkBoxExpandAll) {
          options.setOption(Options.KEY_EXPAND_ALL, Boolean.toString(checkBoxExpandAll.isSelected()));
        }
        if (e.getSource() == checkBoxDrawBackground) {
          options.setOption(Options.KEY_DRAW_BACK, Boolean.toString(checkBoxDrawBackground.isSelected()));
        }
      }
    };

    checkBoxExpandAll.addActionListener(actionListener);
    checkBoxDrawBackground.addActionListener(actionListener);

    return panel;
  }

  @Nonnull
  private String makeContent(@Nonnull final MindMapPanel panel, @Nullable final JComponent options) throws IOException {
    if (options instanceof HasOptions) {
      final HasOptions opts = (HasOptions) options;
      this.flagExpandAllNodes = Boolean.parseBoolean(opts.getOption(Options.KEY_EXPAND_ALL));
      this.flagDrawBackground = Boolean.parseBoolean(opts.getOption(Options.KEY_DRAW_BACK));
    } else {
      for (final Component compo : Assertions.assertNotNull(options).getComponents()) {
        if (compo instanceof JCheckBox) {
          final JCheckBox cb = (JCheckBox) compo;
          if ("unfold".equalsIgnoreCase(cb.getActionCommand())) {
            this.flagExpandAllNodes = cb.isSelected();
          } else if ("back".equalsIgnoreCase(cb.getActionCommand())) {
            this.flagDrawBackground = cb.isSelected();
          }
        }
      }
    }

    final MindMap workMap = new MindMap(panel.getModel(), null);
    workMap.resetPayload();

    if (this.flagExpandAllNodes) {
      MindMapUtils.removeCollapseAttr(workMap);
    }

    final MindMapPanelConfig newConfig = new MindMapPanelConfig(panel.getConfiguration(), false);
    final String[] mappedFont = LOCAL_FONT_MAP.get(newConfig.getFont().getFamily().toLowerCase(Locale.ENGLISH));
    if (mappedFont != null) {
      final Font adaptedFont = new Font(mappedFont[1], newConfig.getFont().getStyle(), newConfig.getFont().getSize());
      newConfig.setFont(adaptedFont);
    }

    newConfig.setDrawBackground(this.flagDrawBackground);
    newConfig.setScale(1.0f);

    final Dimension2D blockSize = calculateSizeOfMapInPixels(workMap, null, newConfig, flagExpandAllNodes, RenderQuality.DEFAULT);
    if (blockSize == null) {
      return SVG_HEADER + "</svg>";
    }

    final StringBuilder buffer = new StringBuilder(16384);
    buffer.append(String.format(SVG_HEADER, 100, 100, dbl2str(blockSize.getWidth()), dbl2str(blockSize.getHeight()))).append(NEXT_LINE);
    buffer.append(prepareStylePart(buffer, newConfig)).append(NEXT_LINE);

    final BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
    final Graphics2D g = image.createGraphics();
    final MMGraphics gfx = new SVGMMGraphics(buffer, g);

    gfx.setClip(0, 0, (int) Math.round(blockSize.getWidth()), (int) Math.round(blockSize.getHeight()));
    try {
      layoutFullDiagramWithCenteringToPaper(gfx, workMap, newConfig, blockSize);
      drawOnGraphicsForConfiguration(gfx, newConfig, workMap, false, null);
    } finally {
      gfx.dispose();
    }
    buffer.append("</svg>");

    return buffer.toString();
  }

  @Override
  public void doExportToClipboard(@Nonnull final MindMapPanel panel, @Nonnull final JComponent options) throws IOException {
    final String text = makeContent(panel, options);
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        if (clipboard != null) {
          clipboard.setContents(new SvgClip(text), null);
        }
      }
    });
  }

  @Override
  public void doExport(@Nonnull final MindMapPanel panel, @Nullable final JComponent options, @Nullable final OutputStream out) throws IOException {
    final String text = makeContent(panel, options);

    File fileToSaveMap = null;
    OutputStream theOut = out;
    if (theOut == null) {
      fileToSaveMap = MindMapUtils.selectFileToSaveForFileFilter(panel, Texts.getString("SvgExporter.saveDialogTitle"), ".svg", Texts.getString("SvgExporter.filterDescription"), Texts.getString("SvgExporter.approveButtonText"));
      fileToSaveMap = MindMapUtils.checkFileAndExtension(panel, fileToSaveMap, ".svg");//NOI18N
      theOut = fileToSaveMap == null ? null : new BufferedOutputStream(new FileOutputStream(fileToSaveMap, false));
    }
    if (theOut != null) {
      try {
        IOUtils.write(text, theOut, "UTF-8");
      } finally {
        if (fileToSaveMap != null) {
          IOUtils.closeQuietly(theOut);
        }
      }
    }
  }

  @Nonnull
  private static String fontFamilyToSVG(@Nonnull Font font) {
    String fontFamilyStr = font.getFamily();
    final String[] logicalFontFamily = LOCAL_FONT_MAP.get(font.getName().toLowerCase());
    if (logicalFontFamily != null) {
      fontFamilyStr = logicalFontFamily[0];
    } else {
      fontFamilyStr = String.format("'%s'", fontFamilyStr);
    }
    return fontFamilyStr;
  }

  @Nonnull
  private static String font2style(@Nonnull final Font font) {
    final StringBuilder result = new StringBuilder();

    final String fontStyle = font.isItalic() ? "italic" : "normal";
    final String fontWeight = font.isBold() ? "bold" : "normal";
    final String fontSize = DOUBLE.format(font.getSize2D()) + "px";
    final String fontFamily = fontFamilyToSVG(font);

    result.append("font-family: ").append(fontFamily).append(';').append(NEXT_LINE);
    result.append("font-size: ").append(fontSize).append(';').append(NEXT_LINE);
    result.append("font-style: ").append(fontStyle).append(';').append(NEXT_LINE);
    result.append("font-weight: ").append(fontWeight).append(';').append(NEXT_LINE);

    return result.toString();
  }

  @Nonnull
  private String prepareStylePart(@Nonnull final StringBuilder buffer, @Nonnull final MindMapPanelConfig config) {
    final StringBuilder result = new StringBuilder();
    result.append("<style>").append(NEXT_LINE);
    result.append('.' + FONT_CLASS_NAME).append(" {").append(NEXT_LINE).append(font2style(config.getFont())).append("}").append(NEXT_LINE);
    result.append("</style>");
    return result.toString();
  }

  @Override
  @Nonnull
  public String getName(@Nonnull final MindMapPanel panel, @Nullable Topic actionTopic, @Nonnull @MustNotContainNull Topic[] selectedTopics) {
    return Texts.getString("SvgExporter.exporterName");
  }

  @Override
  @Nonnull
  public String getReference(@Nonnull final MindMapPanel panel, @Nullable Topic actionTopic, @Nonnull @MustNotContainNull Topic[] selectedTopics) {
    return Texts.getString("SvgExporter.exporterReference");
  }

  @Override
  @Nonnull
  public Icon getIcon(@Nonnull final MindMapPanel panel, @Nullable Topic actionTopic, @Nonnull @MustNotContainNull Topic[] selectedTopics) {
    return ICO;
  }

  @Override
  public int getOrder() {
    return 5;
  }

  private static class Options implements HasOptions {

    private static final String KEY_EXPAND_ALL = "expand.all";
    private static final String KEY_DRAW_BACK = "draw.back";
    private boolean expandAll;
    private boolean drawBack;

    private Options(final boolean expandAllNodes, final boolean drawBackground) {
      this.expandAll = expandAllNodes;
      this.drawBack = drawBackground;
    }

    @Override
    public boolean doesSupportKey(@Nonnull final String key) {
      return KEY_DRAW_BACK.equals(key) || KEY_EXPAND_ALL.equals(key);
    }

    @Override
    @Nonnull
    @MustNotContainNull
    public String[] getOptionKeys() {
      return new String[]{KEY_EXPAND_ALL, KEY_DRAW_BACK};
    }

    @Override
    @Nonnull
    public String getOptionKeyDescription(@Nonnull final String key) {
      if (KEY_DRAW_BACK.equals(key)) {
        return "Draw background";
      }
      if (KEY_EXPAND_ALL.equals(key)) {
        return "Unfold all topics";
      }
      return "";
    }

    @Override
    public void setOption(@Nonnull final String key, @Nullable final String value) {
      if (KEY_DRAW_BACK.equals(key)) {
        this.drawBack = Boolean.parseBoolean(value);
      } else if (KEY_EXPAND_ALL.equals(key)) {
        this.expandAll = Boolean.parseBoolean(value);
      }
    }

    @Override
    @Nullable
    public String getOption(@Nonnull final String key) {
      if (KEY_DRAW_BACK.equals(key)) {
        return Boolean.toString(this.drawBack);
      }
      if (KEY_EXPAND_ALL.equals(key)) {
        return Boolean.toString(this.expandAll);
      }
      return null;
    }

  }

  private static final class SVGMMGraphics implements MMGraphics {

    private static final DecimalFormat ALPHA = new DecimalFormat("#.##");
    private final StringBuilder buffer;
    private final Graphics2D context;
    private double translateX;
    private double translateY;
    private float strokeWidth = 1.0f;
    private StrokeType strokeType = StrokeType.SOLID;

    private SVGMMGraphics(@Nonnull final StringBuilder buffer, @Nonnull final Graphics2D context) {
      this.buffer = buffer;
      this.context = (Graphics2D) context.create();
    }

    @Nonnull
    private static String svgRgb(@Nonnull final Color color) {
      return "rgb(" + color.getRed() + ',' + color.getGreen() + ',' + color.getBlue() + ')';
    }

    private void printFillOpacity(@Nonnull final Color color) {
      if (color.getAlpha() < 255) {
        this.buffer.append(" fill-opacity=\"").append(ALPHA.format(color.getAlpha() / 255.0f)).append("\" ");
      }
    }

    private void printFontData() {
      this.buffer.append("class=\"" + FONT_CLASS_NAME + '\"');
    }

    private void printStrokeData(@Nonnull final Color color) {
      this.buffer.append(" stroke=\"").append(svgRgb(color))
              .append("\" stroke-width=\"").append(dbl2str(this.strokeWidth)).append("\"");

      switch (this.strokeType) {
        case SOLID:
          this.buffer.append(" stroke-linecap=\"round\"");
          break;
        case DASHES:
          this.buffer.append(" stroke-linecap=\"butt\" stroke-dasharray=\"").append(dbl2str(this.strokeWidth * 3.0f)).append(',').append(dbl2str(this.strokeWidth)).append("\"");
          break;
        case DOTS:
          this.buffer.append(" stroke-linecap=\"butt\" stroke-dasharray=\"").append(dbl2str(this.strokeWidth)).append(',').append(dbl2str(this.strokeWidth * 2.0f)).append("\"");
          break;
      }
    }

    @Override
    public float getFontMaxAscent() {
      return this.context.getFontMetrics().getMaxAscent();
    }

    @Override
    @Nonnull
    public Rectangle2D getStringBounds(@Nonnull final String s) {
      return this.context.getFontMetrics().getStringBounds(s, this.context);
    }

    @Override
    public void setClip(final int x, final int y, final int w, final int h) {
      this.context.setClip(x, y, w, h);
    }

    @Override
    @Nonnull
    public MMGraphics copy() {
      final SVGMMGraphics result = new SVGMMGraphics(this.buffer, this.context);
      result.translateX = this.translateX;
      result.translateY = this.translateY;
      result.strokeType = this.strokeType;
      result.strokeWidth = this.strokeWidth;
      return result;
    }

    @Override
    public void dispose() {
      this.context.dispose();
    }

    @Override
    public void translate(final double x, final double y) {
      this.translateX += x;
      this.translateY += y;
      this.context.translate(x, y);
    }

    @Override
    @Nullable
    public Rectangle getClipBounds() {
      return this.context.getClipBounds();
    }

    @Override
    public void setStroke(final float width, @Nonnull final StrokeType type) {
      if (type != this.strokeType || Float.compare(this.strokeWidth, width) != 0) {
        this.strokeType = type;
        this.strokeWidth = width;
        if (type != this.strokeType || Float.compare(this.strokeWidth, width) != 0) {
          this.strokeType = type;
          this.strokeWidth = width;

          final Stroke stroke;

          switch (type) {
            case SOLID:
              stroke = new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
              break;
            case DASHES:
              stroke = new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, new float[]{width * 2.0f, width}, 0.0f);
              break;
            case DOTS:
              stroke = new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{width, width * 2.0f}, 0.0f);
              break;
            default:
              throw new Error("Unexpected stroke type : " + type);
          }
          this.context.setStroke(stroke);
        }
      }
    }

    @Override
    public void drawLine(final int startX, final int startY, final int endX, final int endY, @Nullable final Color color) {
      this.buffer.append("<line x1=\"").append(dbl2str(startX + this.translateX))
              .append("\" y1=\"").append(dbl2str(startY + this.translateY))
              .append("\" x2=\"").append(dbl2str(endX + this.translateX))
              .append("\" y2=\"").append(dbl2str(endY + this.translateY)).append("\" ");
      if (color != null) {
        printStrokeData(color);
        printFillOpacity(color);
      }
      this.buffer.append("/>").append(NEXT_LINE);
    }

    @Override
    public void drawString(@Nonnull final String text, final int x, final int y, @Nullable final Color color) {
      this.buffer.append("<text x=\"").append(dbl2str(this.translateX + x)).append("\" y=\"").append(dbl2str(this.translateY + y)).append('\"');
      if (color != null) {
        this.buffer.append(" fill=\"").append(svgRgb(color)).append("\"");
        printFillOpacity(color);
      }
      this.buffer.append(' ');
      printFontData();
      this.buffer.append('>').append(StringEscapeUtils.escapeXml(text)).append("</text>").append(NEXT_LINE);
    }

    @Override
    public void drawRect(final int x, final int y, final int width, final int height, final @Nullable Color border, final @Nullable Color fill) {
      this.buffer.append("<rect x=\"").append(dbl2str(this.translateX + x))
              .append("\" y=\"").append(dbl2str(translateY + y))
              .append("\" width=\"").append(dbl2str(width))
              .append("\" height=\"").append(dbl2str(height))
              .append("\" ");
      if (border != null) {
        printStrokeData(border);
      }

      if (fill == null) {
        this.buffer.append(" fill=\"none\"");
      } else {
        this.buffer.append(" fill=\"").append(svgRgb(fill)).append("\"");
        printFillOpacity(fill);
      }

      this.buffer.append("/>").append(NEXT_LINE);
    }

    @Override
    public void draw(@Nonnull final Shape shape, @Nullable final Color border, @Nullable final Color fill) {
      if (shape instanceof RoundRectangle2D) {
        final RoundRectangle2D rect = (RoundRectangle2D) shape;

        this.buffer.append("<rect x=\"").append(dbl2str(this.translateX + rect.getX()))
                .append("\" y=\"").append(dbl2str(translateY + rect.getY()))
                .append("\" width=\"").append(dbl2str(rect.getWidth()))
                .append("\" height=\"").append(dbl2str(rect.getHeight()))
                .append("\" rx=\"").append(dbl2str(rect.getArcWidth() / 2.0d))
                .append("\" ry=\"").append(dbl2str(rect.getArcHeight() / 2.0d))
                .append("\" ");

      } else if (shape instanceof Rectangle2D) {

        final Rectangle2D rect = (Rectangle2D) shape;
        this.buffer.append("<rect x=\"").append(dbl2str(this.translateX + rect.getX()))
                .append("\" y=\"").append(dbl2str(translateY + rect.getY()))
                .append("\" width=\"").append(dbl2str(rect.getWidth()))
                .append("\" height=\"").append(dbl2str(rect.getHeight()))
                .append("\" ");

      } else if (shape instanceof Path2D) {
        final Path2D path = (Path2D) shape;
        final double[] data = new double[6];

        this.buffer.append("<path d=\"");

        boolean nofirst = false;

        for (final PathIterator pi = path.getPathIterator(null); !pi.isDone(); pi.next()) {
          if (nofirst) {
            this.buffer.append(' ');
          }
          switch (pi.currentSegment(data)) {
            case PathIterator.SEG_MOVETO: {
              this.buffer.append("M ").append(dbl2str(this.translateX + data[0])).append(' ').append(dbl2str(this.translateY + data[1]));
            }
            break;
            case PathIterator.SEG_LINETO: {
              this.buffer.append("L ").append(dbl2str(this.translateX + data[0])).append(' ').append(dbl2str(this.translateY + data[1]));
            }
            break;
            case PathIterator.SEG_CUBICTO: {
              this.buffer.append("C ")
                      .append(dbl2str(this.translateX + data[0])).append(' ').append(dbl2str(this.translateY + data[1])).append(',')
                      .append(dbl2str(this.translateX + data[2])).append(' ').append(dbl2str(this.translateY + data[3])).append(',')
                      .append(dbl2str(this.translateX + data[4])).append(' ').append(dbl2str(this.translateY + data[5]));
            }
            break;
            case PathIterator.SEG_QUADTO: {
              this.buffer.append("Q ")
                      .append(dbl2str(this.translateX + data[0])).append(' ').append(dbl2str(this.translateY + data[1])).append(',')
                      .append(dbl2str(this.translateX + data[2])).append(' ').append(dbl2str(this.translateY + data[3]));
            }
            break;
            case PathIterator.SEG_CLOSE: {
              this.buffer.append("Z");
            }
            break;
            default:
              LOGGER.warn("Unexpected path segment type");
          }
          nofirst = true;
        }
        this.buffer.append("\" ");
      } else {
        LOGGER.warn("Detected unexpected shape : " + shape.getClass().getName());
      }

      if (border != null) {
        printStrokeData(border);
      }

      if (fill == null) {
        this.buffer.append(" fill=\"none\"");
      } else {
        this.buffer.append(" fill=\"").append(svgRgb(fill)).append("\"");
        printFillOpacity(fill);
      }

      this.buffer.append("/>").append(NEXT_LINE);
    }

    @Override
    public void drawCurve(final double startX, final double startY, final double endX, final double endY, @Nullable final Color color) {
      this.buffer.append("<path d=\"M").append(dbl2str(startX + this.translateX)).append(',').append(startY + this.translateY)
              .append(" C").append(dbl2str(startX))
              .append(',').append(dbl2str(endY))
              .append(' ').append(dbl2str(startX))
              .append(',').append(dbl2str(endY))
              .append(' ').append(dbl2str(endX))
              .append(',').append(dbl2str(endY))
              .append("\" fill=\"none\"");

      if (color != null) {
        printStrokeData(color);
      }
      this.buffer.append(" />").append(NEXT_LINE);
    }

    @Override
    public void drawOval(final int x, final int y, final int w, final int h, @Nullable final Color border, @Nullable final Color fill) {
      final double rx = (double) w / 2.0d;
      final double ry = (double) h / 2.0d;
      final double cx = (double) x + this.translateX + rx;
      final double cy = (double) y + this.translateY + ry;

      this.buffer.append("<ellipse cx=\"").append(dbl2str(cx))
              .append("\" cy=\"").append(dbl2str(cy))
              .append("\" rx=\"").append(dbl2str(rx))
              .append("\" ry=\"").append(dbl2str(ry))
              .append("\" ");

      if (border != null) {
        printStrokeData(border);
      }

      if (fill == null) {
        this.buffer.append(" fill=\"none\"");
      } else {
        this.buffer.append(" fill=\"").append(svgRgb(fill)).append("\"");
        printFillOpacity(fill);
      }

      this.buffer.append("/>").append(NEXT_LINE);
    }

    @Override
    public void drawImage(@Nullable final Image image, final int x, final int y) {
      if (image != null) {
        if (image instanceof RenderedImage) {
          final RenderedImage ri = (RenderedImage) image;
          final ByteArrayOutputStream imageBuffer = new ByteArrayOutputStream(1024);
          try {
            if (ImageIO.write(ri, "png", imageBuffer)) {
              this.buffer.append("<image width=\"").append(ri.getWidth()).append("\" height=\"").append(ri.getHeight()).append("\" x=\"").append(dbl2str(this.translateX + x)).append("\" y=\"").append(dbl2str(this.translateY + y)).append("\" xlink:href=\"data:image/png;base64,");
              this.buffer.append(Utils.base64encode(imageBuffer.toByteArray()));
              this.buffer.append("\"/>").append(NEXT_LINE);
            } else {
              LOGGER.warn("Can't place image because PNG writer is not found");
            }
          } catch (IOException ex) {
            LOGGER.error("Can't place image for error", ex);
          }
        } else {
          LOGGER.warn("Can't place image because it is not rendered one : " + image.getClass().getName());
        }
      }
    }

    @Override
    public void setFont(@Nonnull final Font font) {
      this.context.setFont(font);
    }

  }
}
