/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.mindmap.swing.panel.utils;

import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.MindMapPluginRegistry;
import com.igormaznitsa.mindmap.plugins.PopUpSection;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.plugins.api.PopUpMenuItemPlugin;
import com.igormaznitsa.mindmap.swing.i18n.MmdI18n;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractCollapsableElement;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractElement;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.MMGraphics;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.MMGraphics2DWrapper;
import com.igormaznitsa.mindmap.swing.services.IconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconService;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.parser.Parser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public final class Utils {

  public static final UIComponentFactory UI_COMPO_FACTORY =
      UIComponentFactoryProvider.findInstance();
  public static final ImageIconService ICON_SERVICE = ImageIconServiceProvider.findInstance();
  public static final String PROPERTY_MAX_EMBEDDED_IMAGE_SIDE_SIZE = "mmap.max.image.side.size";
  public static final boolean LTR_LANGUAGE =
      ComponentOrientation.getOrientation(new Locale(System.getProperty("user.language")))
          .isLeftToRight();
  private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
  private static final Pattern URI_PATTERN = Pattern.compile(
      "^(?:([^:\\s]+):)(?://(?:[^?/@\\s]*@)?([^/?\\s]*)/?)?([^?\\s]+)?(?:\\?([^#\\s]*))?(?:#\\S*)?$");
  private static final int MAX_IMAGE_SIDE_SIZE_IN_PIXELS = 350;
  private static final Pattern STRIP_PATTERN = Pattern.compile("^(\\s*)(.*[^\\s])(\\s*)$");

  private Utils() {
  }

  /**
   * Get input stream for resource in zip file.
   *
   * @param zipFile      zip file
   * @param resourcePath path to resource
   * @return input stream for resource or null if not found or directory
   * @throws IOException if there is any transport error
   */
  public static InputStream findInputStreamForResource(final ZipFile zipFile,
                                                       final String resourcePath)
      throws IOException {
    final ZipEntry entry = zipFile.getEntry(resourcePath);

    InputStream result = null;

    if (entry != null && !entry.isDirectory()) {
      result = zipFile.getInputStream(entry);
    }

    return result;
  }

  /**
   * Read who;e zip item into byte array.
   *
   * @param zipFile zip file
   * @param path    path to resource
   * @return byte array or null if not found
   * @throws IOException thrown if there is any transport error
   */
  public static byte[] toByteArray(final ZipFile zipFile, final String path)
      throws IOException {
    final InputStream in = findInputStreamForResource(zipFile, path);

    byte[] result = null;

    if (in != null) {
      try {
        result = IOUtils.toByteArray(in);
      } finally {
        IOUtils.closeQuietly(in);
      }
    }

    return result;
  }

  public static Document load(
      final InputStream inStream,
      final String charset,
      final Parser parser,
      final boolean autoClose) throws ParserConfigurationException, IOException {
    try {
      final org.jsoup.nodes.Document result =
          Jsoup.parse(IOUtils.toString(inStream, charset), parser);
      return new W3CDom().namespaceAware(false).fromJsoup(result);
    } finally {
      if (autoClose) {
        IOUtils.closeQuietly(inStream);
      }
    }
  }

  /**
   * Load and parse XML document from input stream.
   *
   * @param inStream  stream to read document
   * @param charset   charset to be used for loading, can be null
   * @param autoClose true if stream must be closed, false otherwise
   * @return parsed document
   * @throws IOException                  will be thrown if transport error
   * @throws ParserConfigurationException will be thrown if parsing error
   * @throws SAXException                 will be thrown if SAX error
   * @since 1.4.0
   */
  public static Document loadXmlDocument(final InputStream inStream,
                                         final String charset, final boolean autoClose)
      throws SAXException, IOException, ParserConfigurationException {
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    try {
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    } catch (final ParserConfigurationException ex) {
      LOGGER.error("Can't set feature for XML parser : " + ex.getMessage(), ex);
      throw new SAXException("Can't set flag to use security processing of XML file");
    }

    try {
      factory.setFeature("http://apache.org/xml/features/validation/schema", false);
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    } catch (final ParserConfigurationException ex) {
      LOGGER.warn("Can't set some features for XML parser : " + ex.getMessage());
    }

    factory.setIgnoringComments(true);
    factory.setValidating(false);

    final DocumentBuilder builder = factory.newDocumentBuilder();

    final Document document;
    try {
      final InputStream stream;
      if (charset == null) {
        stream = inStream;
      } else {
        stream = new ByteArrayInputStream(
            IOUtils.toString(inStream, charset).getBytes(StandardCharsets.UTF_8));
      }
      document = builder.parse(stream);
    } finally {
      if (autoClose) {
        IOUtils.closeQuietly(inStream);
      }
    }

    return document;
  }

  /**
   * Get first direct child for name.
   *
   * @param node        element to find children
   * @param elementName name of child element
   * @return found first child or null if not found
   * @since 1.4.0
   */
  public static Element findFirstElement(final Element node,
                                         final String elementName) {
    Element result = null;
    for (final Element l : Utils.findDirectChildrenForName(node, elementName)) {
      result = l;
      break;
    }
    return result;
  }

  /**
   * Find all direct children with defined name.
   *
   * @param element   parent element
   * @param childName child element name
   * @return list of found elements
   * @since 1.4.0
   */
  public static List<Element> findDirectChildrenForName(final Element element,
                                                        final String childName) {
    final List<Element> resultList = new ArrayList<>();

    final NodeList list = element.getChildNodes();
    for (int i = 0; i < list.getLength(); i++) {
      final Node node = list.item(i);
      if (element.equals(node.getParentNode()) && node instanceof Element &&
          childName.equals(node.getNodeName())) {
        resultList.add((Element) node);
      }
    }
    return resultList;
  }

  /**
   * Allows to check that some string has URI in appropriate format.
   *
   * @param uri string to be checked, must not be null
   * @return true if the string contains correct uri, false otherwise
   */
  public static boolean isUriCorrect(final String uri) {
    return URI_PATTERN.matcher(uri).matches();
  }

  /**
   * Get max image size.
   *
   * @return max image size
   * @see #MAX_IMAGE_SIDE_SIZE_IN_PIXELS
   * @see #PROPERTY_MAX_EMBEDDED_IMAGE_SIDE_SIZE
   */
  public static int getMaxImageSize() {
    int result = MAX_IMAGE_SIDE_SIZE_IN_PIXELS;
    try {
      final String defined = System.getProperty(PROPERTY_MAX_EMBEDDED_IMAGE_SIDE_SIZE);
      if (defined != null) {
        LOGGER.info("Detected redefined max size for embedded image side : " + defined);
        result = Math.max(8, Integer.parseInt(defined.trim()));
      }
    } catch (NumberFormatException ex) {
      LOGGER.error("Error during image size decoding : ", ex);
    }
    return result;
  }

  /**
   * Load and encode image into Base64.
   *
   * @param in      stream to read image
   * @param maxSize max size of image, if less or zero then don't rescale
   * @return null if it was impossible to load image for its format, loaded
   * prepared image
   * @throws IOException if any error during conversion or loading
   * @since 1.4.0
   */
  public static String rescaleImageAndEncodeAsBase64(final InputStream in,
                                                     final int maxSize) throws IOException {
    final Image image = ImageIO.read(in);
    String result = null;
    if (image != null) {
      result = rescaleImageAndEncodeAsBase64(image, maxSize);
    }
    return result;
  }

  /**
   * Load and encode image into Base64 from file.
   *
   * @param file    image file
   * @param maxSize max size of image, if less or zero then don't rescale
   * @return image
   * @throws IOException if any error during conversion or loading
   * @since 1.4.0
   */
  public static String rescaleImageAndEncodeAsBase64(final File file, final int maxSize)
      throws IOException {
    final Image image = ImageIO.read(file);
    if (image == null) {
      throw new IllegalArgumentException("Can't load image file : " + file);
    }
    return rescaleImageAndEncodeAsBase64(image, maxSize);
  }

  /**
   * Get default render quality for host OS.
   *
   * @return the render quality for host OS, must not be null
   * @since 1.4.5
   */
  public static RenderQuality getDefaultRenderQialityForOs() {
    RenderQuality result = RenderQuality.DEFAULT;
    if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_WINDOWS) {
      result = RenderQuality.QUALITY;
    }
    return result;
  }

  /**
   * Rescale image and encode into Base64.
   *
   * @param image   image to rescale and encode
   * @param maxSize max size of image, if less or zero then don't rescale
   * @return scaled and encoded image
   * @throws IOException if it was impossible to encode image
   * @since 1.4.0
   */
  public static String rescaleImageAndEncodeAsBase64(Image image, final int maxSize)
      throws IOException {
    final int width = image.getWidth(null);
    final int height = image.getHeight(null);

    final int maxImageSideSize = maxSize > 0 ? maxSize : Math.max(width, height);

    final float imageScale = width > maxImageSideSize || height > maxImageSideSize ?
        (float) maxImageSideSize / (float) Math.max(width, height) : 1.0f;

    if (!(image instanceof RenderedImage) || Float.compare(imageScale, 1.0f) != 0) {
      final int swidth;
      final int sheight;

      if (Float.compare(imageScale, 1.0f) == 0) {
        swidth = width;
        sheight = height;
      } else {
        swidth = Math.round(imageScale * width);
        sheight = Math.round(imageScale * height);
      }

      final BufferedImage buffer = new BufferedImage(swidth, sheight, BufferedImage.TYPE_INT_ARGB);
      final Graphics2D gfx = buffer.createGraphics();

      gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      gfx.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      gfx.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
          RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      gfx.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
      gfx.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
          RenderingHints.VALUE_COLOR_RENDER_QUALITY);

      gfx.drawImage(image, AffineTransform.getScaleInstance(imageScale, imageScale), null);
      gfx.dispose();
      image = buffer;
    }

    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      if (!ImageIO.write((RenderedImage) image, "png", bos)) {
        throw new IOException("Can't encode image as PNG");
      }
    } finally {
      IOUtils.closeQuietly(bos);
    }
    return Utils.base64encode(bos.toByteArray());
  }

  public static int calculateColorBrightness(final Color color) {
    return (int) Math.sqrt(
        color.getRed() * color.getRed() * .241d + color.getGreen() * color.getGreen() * .691d +
            color.getBlue() * color.getBlue() * .068d);
  }

  public static boolean isDarkTheme() {
    final Color panelBack = UIManager.getColor("Panel.background");
    if (panelBack == null) {
      return false;
    } else {
      return calculateColorBrightness(panelBack) < 150;
    }
  }

  public static String convertCamelCasedToHumanForm(final String camelCasedString,
                                                    final boolean capitalizeFirstChar) {
    final StringBuilder result = new StringBuilder();

    boolean notFirst = false;

    for (final char c : camelCasedString.toCharArray()) {
      if (notFirst) {
        if (Character.isUpperCase(c)) {
          result.append(' ');
          result.append(Character.toLowerCase(c));
        } else {
          result.append(c);
        }
      } else {
        notFirst = true;
        if (capitalizeFirstChar) {
          result.append(Character.toUpperCase(c));
        } else {
          result.append(c);
        }
      }
    }
    return result.toString();
  }

  public static Topic[] getLeftToRightOrderedChildrens(final Topic topic) {
    final List<Topic> result = new ArrayList<>();
    if (topic.getTopicLevel() == 0) {
      for (final Topic t : topic.getChildren()) {
        if (AbstractCollapsableElement.isLeftSidedTopic(t)) {
          result.add(t);
        }
      }
      for (final Topic t : topic.getChildren()) {
        if (!AbstractCollapsableElement.isLeftSidedTopic(t)) {
          result.add(t);
        }
      }
    } else {
      result.addAll(topic.getChildren());
    }
    return result.toArray(new Topic[0]);
  }

  public static boolean safeObjectEquals(final Object obj1, final Object obj2) {
    if (obj1 == obj2) {
      return true;
    }
    if (obj1 == null || obj2 == null) {
      return false;
    }
    return obj1.equals(obj2);
  }

  public static void setAttribute(final String name, final String value,
                                  final Topic[] topics) {
    for (final Topic t : topics) {
      t.putAttribute(name, value);
    }
  }

  public static Color html2color(final String str, final boolean hasAlpha) {
    Color result = null;
    if (str != null && !str.isEmpty() && str.charAt(0) == '#') {
      try {
        String color = str.substring(1);
        if (color.length() > 6) {
          color = color.substring(color.length() - 6);
        }

        if (color.length() == 6) {
          result = new Color(Integer.parseInt(color, 16), hasAlpha);
        } else if (color.length() == 3) {
          final int r = Integer.parseInt(color.charAt(0) + "0", 16);
          final int g = Integer.parseInt(color.charAt(1) + "0", 16);
          final int b = Integer.parseInt(color.charAt(2) + "0", 16);
          result = new Color(r, g, b);
        }
      } catch (NumberFormatException ex) {
        LOGGER.warn(String.format("Can't convert %s to color", str));
      }
    }
    return result;
  }

  public static String color2html(final Color color, final boolean hasAlpha) {
    String result = null;
    if (color != null) {
      final StringBuilder buffer = new StringBuilder();

      buffer.append('#');

      final int[] components;

      if (hasAlpha) {
        components =
            new int[] {color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue()};
      } else {
        components = new int[] {color.getRed(), color.getGreen(), color.getBlue()};
      }

      for (final int c : components) {
        final String str = Integer.toHexString(c & 0xFF).toUpperCase(Locale.ENGLISH);
        if (str.length() < 2) {
          buffer.append('0');
        }
        buffer.append(str);
      }

      result = buffer.toString();
    }
    return result;
  }

  public static String getFirstLine(final String text) {
    return text.replace("\r", "").split("\\n")[0];
  }

  public static String makeShortTextVersion(String text, final int maxLength) {
    if (text.length() > maxLength) {
      text = text.substring(0, maxLength) + "...";
    }
    return text;
  }

  public static void safeSwingCall(final Runnable runnable) {
    if (SwingUtilities.isEventDispatchThread()) {
      runnable.run();
    } else {
      SwingUtilities.invokeLater(runnable);
    }
  }

  public static void safeSwingBlockingCall(final Runnable runnable) {
    if (SwingUtilities.isEventDispatchThread()) {
      runnable.run();
    } else {
      try {
        SwingUtilities.invokeAndWait(runnable);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      } catch (InvocationTargetException ex) {
        throw new RuntimeException("Detected exception during SwingUtilities.invokeAndWait", ex);
      }
    }
  }

  public static String[] breakToLines(final String text) {
    final int lineNum = numberOfLines(text);
    final String[] result = new String[lineNum];
    final StringBuilder line = new StringBuilder();

    int index = 0;

    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == '\n') {
        result[index++] = line.toString();
        line.setLength(0);
      } else {
        line.append(text.charAt(i));
      }
    }
    result[index] = line.toString();
    return result;
  }

  public static int numberOfLines(final String text) {
    int result = 1;
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == '\n') {
        result++;
      }
    }
    return result;
  }

  public static void foldUnfoldTree(final JTree tree, final boolean unfold) {
    if (!SwingUtilities.isEventDispatchThread()) {
      throw new IllegalStateException("Must be called from Swing thread");
    }
    final TreeModel model = tree.getModel();
    if (model != null) {
      final Object root = model.getRoot();
      if (root != null) {
        final TreePath thePath = new TreePath(root);
        setTreeState(tree, thePath, true, unfold);
        if (!unfold) {
          setTreeState(tree, thePath, false, true);
        }
      }
    }
  }

  private static void setTreeState(final JTree tree, final TreePath path,
                                   final boolean recursively, final boolean unfold) {
    final Object lastNode = path.getLastPathComponent();
    for (int i = 0; i < tree.getModel().getChildCount(lastNode); i++) {
      final Object child = tree.getModel().getChild(lastNode, i);
      final TreePath pathToChild = path.pathByAddingChild(child);
      if (recursively) {
        setTreeState(tree, pathToChild, recursively, unfold);
      }
    }
    if (unfold) {
      tree.expandPath(path);
    } else {
      tree.collapsePath(path);
    }
  }

  public static String removeAllISOControlsButTabs(final String str) {
    final StringBuilder result = new StringBuilder(str.length());
    for (final char c : str.toCharArray()) {
      if (c != '\t' && Character.isISOControl(c)) {
        continue;
      }
      result.append(c);
    }
    return result.toString();
  }

  public static Point2D findRectEdgeIntersection(final Rectangle2D rect,
                                                 final double outboundX, final double outboundY) {
    final int detectedSide = rect.outcode(outboundX, outboundY);

    if ((detectedSide & (Rectangle2D.OUT_TOP | Rectangle2D.OUT_BOTTOM)) != 0) {
      final boolean top = (detectedSide & Rectangle2D.OUT_BOTTOM) == 0;

      final double dx = outboundX - rect.getCenterX();
      if (dx == 0.0d) {
        return new Point2D.Double(rect.getCenterX(), top ? rect.getMinY() : rect.getMaxY());
      } else {
        final double halfy = top ? rect.getHeight() / 2 : -rect.getHeight() / 2;
        final double coeff = (outboundY - rect.getCenterY()) / dx;
        final double calculatedX = rect.getCenterX() - (halfy / coeff);
        if (calculatedX >= rect.getMinX() && calculatedX <= rect.getMaxX()) {
          return new Point2D.Double(calculatedX, top ? rect.getMinY() : rect.getMaxY());
        }
      }
    }

    if ((detectedSide & (Rectangle2D.OUT_LEFT | Rectangle2D.OUT_RIGHT)) != 0) {
      final boolean left = (detectedSide & Rectangle2D.OUT_RIGHT) == 0;

      final double dy = outboundY - rect.getCenterY();
      if (dy == 0.0d) {
        return new Point2D.Double(left ? rect.getMinX() : rect.getMaxX(), rect.getCenterY());
      } else {
        final double halfx = left ? rect.getWidth() / 2 : -rect.getWidth() / 2;
        final double coeff = (outboundX - rect.getCenterX()) / dy;
        final double calculatedY = rect.getCenterY() - (halfx / coeff);
        if (calculatedY >= rect.getMinY() && calculatedY <= rect.getMaxY()) {
          return new Point2D.Double(left ? rect.getMinX() : rect.getMaxX(), calculatedY);
        }
      }
    }

    return null;
  }

  public static boolean isPlantUmlFileExtension(final String lowerCasedTrimmedExtension) {
    boolean result = false;
    if (lowerCasedTrimmedExtension.length() > 1 && lowerCasedTrimmedExtension.charAt(0) == 'p') {
      result =
          "pu".equals(lowerCasedTrimmedExtension) || "puml".equals(lowerCasedTrimmedExtension) ||
              "plantuml".equals(lowerCasedTrimmedExtension);
    }
    return result;
  }

  public static Image scaleImage(final Image src, final double baseScaleX,
                                 final double baseScaleY, final double scale) {
    final int imgw = src.getWidth(null);
    final int imgh = src.getHeight(null);

    final int scaledW = (int) Math.round(imgw * baseScaleX * scale);
    final int scaledH = (int) Math.round(imgh * baseScaleY * scale);

    BufferedImage result = null;
    if (scaledH > 0 && scaledW > 0) {
      try {
        result = new BufferedImage(scaledW, scaledH, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = (Graphics2D) result.getGraphics();

        RenderQuality.QUALITY.prepare(g);

        g.drawImage(src, 0, 0, scaledW, scaledH, null);
        g.dispose();
      } catch (OutOfMemoryError e) {
        LOGGER.error(
            "OutOfmemoryError in scaleImage (" + baseScaleX + ',' + baseScaleY + ',' + scale + ')',
            e);
        throw e;
      }

    }
    return result;
  }

  public static Image renderWithTransparency(final float opacity,
                                             final AbstractElement element,
                                             final MindMapPanelConfig config,
                                             final RenderQuality quality) {
    final AbstractElement cloned = element.makeCopy();
    final Rectangle2D bounds = cloned.getBounds();

    final float increase =
        config.safeScaleFloatValue(config.getElementBorderWidth() + config.getShadowOffset(), 0.0f);
    final int imageWidth = (int) Math.round(bounds.getWidth() + increase);
    final int imageHeight = (int) Math.round(bounds.getHeight() + increase);

    bounds.setRect(0.0d, 0.0d, bounds.getWidth(), bounds.getHeight());

    final BufferedImage result =
        new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);

    for (int y = 0; y < imageHeight; y++) {
      for (int x = 0; x < imageWidth; x++) {
        result.setRGB(x, y, 0);
      }
    }

    final Graphics2D g = result.createGraphics();
    final MMGraphics gfx = new MMGraphics2DWrapper(g);
    try {
      quality.prepare(g);
      cloned.doPaint(gfx, config, false);
    } finally {
      gfx.dispose();
    }

    int alpha;
    if (opacity <= 0.0f) {
      alpha = 0x00;
    } else if (opacity >= 1.0f) {
      alpha = 0xFF;
    } else {
      alpha = Math.round(0xFF * opacity);
    }

    alpha <<= 24;

    for (int y = 0; y < imageHeight; y++) {
      for (int x = 0; x < imageWidth; x++) {
        final int curAlpha = result.getRGB(x, y) >>> 24;
        if (curAlpha == 0xFF) {
          result.setRGB(x, y, (result.getRGB(x, y) & 0xFFFFFF) | alpha);
        } else if (curAlpha != 0x00) {
          final int calculated = Math.round(curAlpha * opacity) << 24;
          result.setRGB(x, y, (result.getRGB(x, y) & 0xFFFFFF) | calculated);
        }
      }
    }

    return result;
  }

  public static Color makeContrastColor(final Color color) {
    return new Color(color.getRed() ^ 0xFF, color.getGreen() ^ 0xFF, color.getBlue() ^ 0xFF);
  }

  private static List<JMenuItem> findPopupMenuItems(
      final PluginContext context,
      final PopUpSection section,
      final boolean fullScreenModeActive,
      final List<JMenuItem> list,
      final Topic topicUnderMouse,
      final List<PopUpMenuItemPlugin> pluginMenuItems
  ) {
    list.clear();

    for (final PopUpMenuItemPlugin p : pluginMenuItems) {
      if (fullScreenModeActive && !p.isCompatibleWithFullScreenMode()) {
        continue;
      }
      if (p.getSection() == section) {
        if (!(p.needsTopicUnderMouse() || p.needsSelectedTopics())
            || (p.needsTopicUnderMouse() && topicUnderMouse != null)
            || (p.needsSelectedTopics() && context.getSelectedTopics().length > 0)) {

          final JMenuItem item = p.makeMenuItem(context, topicUnderMouse);
          if (item != null) {
            item.setEnabled(p.isEnabled(context, topicUnderMouse));
            list.add(item);
          }
        }
      }
    }
    return list;
  }

  public static void assertSwingDispatchThread() {
    if (!SwingUtilities.isEventDispatchThread()) {
      throw new Error("Must be called in Swing dispatch thread");
    }
  }

  private static List<JMenuItem> putAllItemsAsSection(final JPopupMenu menu,
                                                      final JMenu subMenu,
                                                      final List<JMenuItem> items) {
    if (!items.isEmpty()) {
      if (menu.getComponentCount() > 0) {
        menu.add(UI_COMPO_FACTORY.makeMenuSeparator());
      }
      for (final JMenuItem i : items) {
        if (subMenu == null) {
          menu.add(i);
        } else {
          subMenu.add(i);
        }
      }

      if (subMenu != null) {
        menu.add(subMenu);
      }
    }
    return items;
  }

  public static boolean isDataFlavorAvailable(final Clipboard clipboard,
                                              final DataFlavor flavor) {
    boolean result = false;
    try {
      result = clipboard.isDataFlavorAvailable(flavor);
    } catch (final IllegalStateException ex) {
      LOGGER.warn("Can't get access to clipboard : " + ex.getMessage());
    }
    return result;
  }

  public static JPopupMenu makePopUp(
      final PluginContext context,
      final boolean fullScreenModeActive,
      final Topic topicUnderMouse
  ) {
    final JPopupMenu result = UI_COMPO_FACTORY.makePopupMenu();
    final List<PopUpMenuItemPlugin> pluginMenuItems =
        MindMapPluginRegistry.getInstance().findFor(PopUpMenuItemPlugin.class);
    final List<JMenuItem> tmpList = new ArrayList<>();

    final boolean isModelNotEmpty = context.getModel().getRoot() != null;

    putAllItemsAsSection(result, null,
        findPopupMenuItems(context, PopUpSection.MAIN, fullScreenModeActive, tmpList,
            topicUnderMouse, pluginMenuItems));
    putAllItemsAsSection(result, null,
        findPopupMenuItems(context, PopUpSection.MANIPULATORS, fullScreenModeActive, tmpList,
            topicUnderMouse, pluginMenuItems));
    putAllItemsAsSection(result, null,
        findPopupMenuItems(context, PopUpSection.EXTRAS, fullScreenModeActive, tmpList,
            topicUnderMouse, pluginMenuItems));

    final JMenu exportMenu =
        UI_COMPO_FACTORY.makeMenu(MmdI18n.getInstance().findBundle().getString("MMDExporters.SubmenuName"));
    exportMenu.setIcon(ICON_SERVICE.getIconForId(IconID.POPUP_EXPORT));

    final JMenu importMenu =
        UI_COMPO_FACTORY.makeMenu(MmdI18n.getInstance().findBundle().getString("MMDImporters.SubmenuName"));
    importMenu.setIcon(ICON_SERVICE.getIconForId(IconID.POPUP_IMPORT));

    putAllItemsAsSection(result, importMenu,
        findPopupMenuItems(context, PopUpSection.IMPORT, fullScreenModeActive, tmpList,
            topicUnderMouse, pluginMenuItems));
    if (isModelNotEmpty) {
      putAllItemsAsSection(result, exportMenu,
          findPopupMenuItems(context, PopUpSection.EXPORT, fullScreenModeActive, tmpList,
              topicUnderMouse, pluginMenuItems));
    }

    putAllItemsAsSection(result, null,
        findPopupMenuItems(context, PopUpSection.TOOLS, fullScreenModeActive, tmpList,
            topicUnderMouse, pluginMenuItems));
    putAllItemsAsSection(result, null,
        findPopupMenuItems(context, PopUpSection.MISC, fullScreenModeActive, tmpList,
            topicUnderMouse, pluginMenuItems));

    return result;
  }

  public static boolean isPopupEvent(final MouseEvent mouseEvent) {
    return mouseEvent != null && mouseEvent.getButton() != 0 && mouseEvent.isPopupTrigger();
  }

  public static boolean isKeyStrokeEvent(final KeyStroke keyStroke,
                                         final int keyEventType, final KeyEvent event) {
    boolean result = false;
    if (keyStroke != null && event != null) {
      if (keyEventType == keyStroke.getKeyEventType()) {
        result = ((keyStroke.getModifiers() & event.getModifiers()) == keyStroke.getModifiers()) &&
            (keyStroke.getKeyCode() == event.getKeyCode());
      }
    }
    return result;
  }

  private static List<JButton> findAllOptionPaneButtons(final JComponent component) {
    final List<JButton> result = new ArrayList<>();
    Arrays.stream(component.getComponents())
        .filter(x -> x instanceof JComponent)
        .map(x -> (JComponent) x)
        .forEach(x -> {
          if (x instanceof JButton) {
            if ("OptionPane.button".equals(x.getName())) {
              result.add((JButton) x);
            }
          } else {
            result.addAll(findAllOptionPaneButtons(x));
          }
        });
    return result;
  }

  private static void replaceActionListenerForButton(final JButton button,
                                                     final ActionListener listener) {
    final ActionListener[] currentListeners = button.getActionListeners();
    for (final ActionListener l : currentListeners) {
      button.removeActionListener(l);
    }
    button.addActionListener(listener);
  }

  @SafeVarargs
  public static JComponent catchEscInParentDialog(
      final JComponent component,
      final DialogProvider dialogProvider,
      final Predicate<JDialog> doClose,
      final Consumer<JDialog>... beforeClose) {
    component.addHierarchyListener(new HierarchyListener() {

      final Consumer<JDialog> processor = dialog -> {
        final boolean close;
        if (doClose == null) {
          close = true;
        } else {
          if (doClose.test(dialog)) {
            close = dialogProvider
                .msgConfirmOkCancel(dialog, MmdI18n.getInstance().findBundle().getString("Utils.confirmActionTitle"),
                    MmdI18n.getInstance().findBundle().getString("Utils.closeForContentChange"));
          } else {
            close = true;
          }
        }
        if (close) {
          try {
            for (final Consumer<JDialog> c : beforeClose) {
              try {
                c.accept(dialog);
              } catch (Exception ex) {
                LOGGER.error("Error during before close call", ex);
              }
            }
          } finally {
            dialog.dispose();
          }
        }
      };
      private final KeyStroke escapeKeyStroke =
          KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);

      private final List<WindowListener> foundWindowListeners = new ArrayList<>();

      @Override
      public void hierarchyChanged(final HierarchyEvent e) {
        final Window window = SwingUtilities.getWindowAncestor(component);
        if (window instanceof JDialog &&
            (e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) != 0) {
          final JDialog dialog = (JDialog) window;

          final List<JButton> dialogButtons = findAllOptionPaneButtons(dialog.getRootPane());
          dialogButtons.stream()
              .filter(x -> "cancel".equalsIgnoreCase(x.getText()))
              .forEach(x -> replaceActionListenerForButton(x, be -> processor.accept(dialog)));

          dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

          final WindowListener windowListener = new WindowListener() {
            @Override
            public void windowClosing(final WindowEvent e) {
              processor.accept(dialog);
            }

            @Override
            public void windowOpened(final WindowEvent e) {
              foundWindowListeners.forEach(x -> x.windowOpened(e));
            }

            @Override
            public void windowClosed(final WindowEvent e) {
              foundWindowListeners.forEach(x -> x.windowClosed(e));
            }

            @Override
            public void windowIconified(final WindowEvent e) {
              foundWindowListeners.forEach(x -> x.windowIconified(e));
            }

            @Override
            public void windowDeiconified(final WindowEvent e) {
              foundWindowListeners.forEach(x -> x.windowDeiconified(e));
            }

            @Override
            public void windowActivated(final WindowEvent e) {
              foundWindowListeners.forEach(x -> x.windowActivated(e));
            }

            @Override
            public void windowDeactivated(final WindowEvent e) {
              foundWindowListeners.forEach(x -> x.windowDeactivated(e));
            }
          };

          if (this.foundWindowListeners.isEmpty()) {
            final WindowListener[] windowListeners = dialog.getWindowListeners();
            for (final WindowListener w : windowListeners) {
              dialog.removeWindowListener(w);
              this.foundWindowListeners.add(w);
            }
            dialog.addWindowListener(windowListener);
          }

          final InputMap inputMap =
              dialog.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
          inputMap.put(escapeKeyStroke, "PRESSING_ESCAPE");
          final ActionMap actionMap = dialog.getRootPane().getActionMap();
          actionMap.put("PRESSING_ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
              processor.accept(dialog);
            }
          });
        }
      }
    });
    return component;
  }

  public static byte[] base64decode(final String text) {
    return Base64.getDecoder().decode(text);
  }

  public static String strip(final String str, final boolean leading) {
    if (str.trim().isEmpty()) {
      return "";
    }
    final Matcher matcher = STRIP_PATTERN.matcher(str);
    if (!matcher.find()) {
      throw new Error("Unexpected error in strip(String): " + str);
    }
    return leading ? matcher.group(2) + matcher.group(3) : matcher.group(1) + matcher.group(2);
  }


  public static String base64encode(final byte[] data) {
    return Base64.getEncoder().encodeToString(data);
  }

}
