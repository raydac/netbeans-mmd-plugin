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

package com.igormaznitsa.mindmap.plugins.importers;

import static com.igormaznitsa.meta.common.utils.Assertions.assertNotNull;


import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.api.AbstractImporter;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.plugins.attributes.images.ImageVisualAttributePlugin;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute;
import com.igormaznitsa.mindmap.swing.panel.Texts;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.mindmap.swing.services.IconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;
import java.awt.Color;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMind2MindMapImporter extends AbstractImporter {

  private static final Icon ICO =
      ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_IMPORT_XMIND2MM);

  private static final Logger LOGGER = LoggerFactory.getLogger(XMind2MindMapImporter.class);

  @Nonnull
  private static RuntimeException makeWrongFormatException() {
    return new IllegalArgumentException("Wrong or unsupported XMind file format");
  }

  @Nonnull
  private static String extractTopicTitle(@Nonnull final Element topic) {
    final List<Element> title = Utils.findDirectChildrenForName(topic, "title");
    return title.isEmpty() ? "" : title.get(0).getTextContent();
  }

  @Nonnull
  @MustNotContainNull
  private static List<Element> getChildTopics(@Nonnull final Element topic) {
    List<Element> result = new ArrayList<Element>();

    for (final Element c : Utils.findDirectChildrenForName(topic, "children")) {
      for (Element t : Utils.findDirectChildrenForName(c, "topics")) {
        result.addAll(Utils.findDirectChildrenForName(t, "topic"));
      }
    }

    return result;
  }

  private static void convertTopic(@Nonnull ZipFile zipFile, @Nonnull final XMindStyles styles,
                                   @Nonnull final MindMap map, @Nullable final Topic parent,
                                   @Nullable Topic pregeneratedOne,
                                   @Nonnull final Element topicElement,
                                   @Nonnull Map<String, Topic> idTopicMap,
                                   @Nonnull final Map<String, String> linksBetweenTopics)
      throws Exception {
    final Topic topicToProcess;

    if (pregeneratedOne == null) {
      topicToProcess = assertNotNull(parent).makeChild("", null);
    } else {
      topicToProcess = pregeneratedOne;
    }

    topicToProcess.setText(extractTopicTitle(topicElement));

    final String theTopicId = topicElement.getAttribute("id");

    idTopicMap.put(theTopicId, topicToProcess);

    final String styleId = topicElement.getAttribute("style-id");
    if (!styleId.isEmpty()) {
      styles.setStyle(styleId, topicToProcess);
    }

    final String attachedImage = extractFirstAttachedImageAsBase64(zipFile, topicElement);
    if (attachedImage != null && !attachedImage.isEmpty()) {
      topicToProcess.setAttribute(ImageVisualAttributePlugin.ATTR_KEY, attachedImage);
    }

    final String xlink = topicElement.getAttribute("xlink:href");
    if (!xlink.isEmpty()) {
      if (xlink.startsWith("file:")) {
        try {
          topicToProcess.setExtra(new ExtraFile(new MMapURI(new File(xlink.substring(5)).toURI())));
        } catch (Exception ex) {
          LOGGER.error("Can't convert file link : " + xlink, ex);
        }
      } else if (xlink.startsWith("xmind:#")) {
        linksBetweenTopics.put(theTopicId, xlink.substring(7));
      } else {
        try {
          topicToProcess.setExtra(new ExtraLink(new MMapURI(URI.create(xlink))));
        } catch (Exception ex) {
          LOGGER.error("Can't convert link : " + xlink, ex);
        }
      }
    }

    final String extractedNote = extractNote(topicElement);

    if (!extractedNote.isEmpty()) {
      topicToProcess.setExtra(new ExtraNote(extractedNote));
    }

    for (final Element c : getChildTopics(topicElement)) {
      convertTopic(zipFile, styles, map, topicToProcess, null, c, idTopicMap, linksBetweenTopics);
    }
  }

  @Nullable
  private static String extractFirstAttachedImageAsBase64(@Nonnull final ZipFile file,
                                                          @Nonnull final Element topic) {
    String result = null;

    for (final Element e : Utils.findDirectChildrenForName(topic, "xhtml:img")) {
      final String link = e.getAttribute("xhtml:src");
      if (!link.isEmpty()) {
        if (link.startsWith("xap:")) {
          InputStream inStream = null;
          try {
            inStream = Utils.findInputStreamForResource(file, link.substring(4));
            if (inStream != null) {
              result = Utils.rescaleImageAndEncodeAsBase64(inStream, -1);
              if (result != null) {
                break;
              }
            }
          } catch (final Exception ex) {
            LOGGER.error("Can't decode attached image : " + link, ex);
          } finally {
            IOUtils.closeQuietly(inStream);
          }
        }
      }
    }
    return result;
  }

  @Nullable
  private static String extractFirstAttachedImageAsBase64(@Nonnull final ZipFile file,
                                                          @Nonnull final JSONObject topic) {

    String result = null;
    JSONObject image = topic.has("image") ? topic.getJSONObject("image") : null;
    if (image != null) {
      final String link = assertNotNull(image.getString("src"));
      if (link.startsWith("xap:")) {
        InputStream inStream = null;
        try {
          inStream = Utils.findInputStreamForResource(file, link.substring(4));
          if (inStream != null) {
            result = Utils.rescaleImageAndEncodeAsBase64(inStream, -1);
          }
        } catch (final Exception ex) {
          LOGGER.error("Can't decode attached image : " + link, ex);
        } finally {
          IOUtils.closeQuietly(inStream);
        }
      }
    }
    return result;
  }

  @Nonnull
  private static String extractNote(@Nonnull final Element topic) {
    final StringBuilder result = new StringBuilder();

    for (final Element note : Utils.findDirectChildrenForName(topic, "notes")) {
      final String plain = extractTextContentFrom(note, "plain");
      final String html = extractTextContentFrom(note, "html");

      if (result.length() > 0) {
        result.append('\n');
      }

      if (!plain.isEmpty()) {
        result.append(plain);
      } else if (!html.isEmpty()) {
        result.append(html);
      }
    }

    return result.toString();
  }

  @Nonnull
  private static String extractNote(@Nonnull final JSONObject topic) {
    final StringBuilder result = new StringBuilder();

    final JSONObject notes = topic.has("notes") ? topic.getJSONObject("notes") : null;
    if (notes != null) {
      final String plain = extractTextContentFrom(notes, "plain");

      if (result.length() > 0) {
        result.append('\n');
      }

      if (!plain.isEmpty()) {
        result.append(plain);
      }
    }


    return result.toString();
  }

  @Nonnull
  private static String extractTextContentFrom(@Nonnull final Element element,
                                               @Nonnull final String tag) {
    final StringBuilder result = new StringBuilder();

    for (final Element c : Utils.findDirectChildrenForName(element, tag)) {
      final String found = c.getTextContent();
      if (found != null && !found.isEmpty()) {
        result.append(found.replace("\r", ""));
      }
    }

    return result.toString();
  }

  @Nonnull
  private static String extractTextContentFrom(@Nonnull final JSONObject element,
                                               @Nonnull final String tag) {
    final StringBuilder result = new StringBuilder();

    if (element.has(tag)) {
      final JSONObject object = element.getJSONObject(tag);
      final String found = object.getString("content");
      if (found != null && !found.isEmpty()) {
        result.append(found.replace("\r", ""));
      }
    }

    return result.toString();
  }

  @Override
  @Nullable
  public MindMap doImport(@Nonnull final PluginContext context) throws Exception {
    final File file = this.selectFileForExtension(context,
        Texts.getString("MMDImporters.XMind2MindMap.openDialogTitle"), null, "xmind",
        "XMind files (.XMIND)", Texts.getString("MMDImporters.ApproveImport"));

    if (file == null) {
      return null;
    }

    final ZipFile zipFile = new ZipFile(file);
    final XMindStyles styles = new XMindStyles(zipFile);

    return this.parseZipFile(zipFile);
  }

  @Nonnull
  MindMap parseZipFile(@Nonnull final ZipFile zipFile) throws Exception {
    InputStream contentStream = Utils.findInputStreamForResource(zipFile, "content.json");
    MindMap result;
    if (contentStream == null) {
      final XMindStyles styles = new XMindStyles(zipFile);
      contentStream = Utils.findInputStreamForResource(zipFile, "content.xml");
      if (contentStream == null) {
        throw makeWrongFormatException();
      } else {
        result = convertXmlContent(styles, zipFile, contentStream);
      }
    } else {
      result = convertJsonContent(zipFile, contentStream);
    }
    return result;
  }

  @Nonnull
  private MindMap convertJsonContent(
      @Nonnull final ZipFile zipFile,
      @Nonnull final InputStream content
  ) throws Exception {
    final JSONArray parsed = new JSONArray(IOUtils.toString(content, "UTF-8"));

    final List<JSONObject> sheets = new ArrayList<>();

    if (parsed.length() > 0) {
      for (int i = 0; i < parsed.length(); i++) {
        final JSONObject object = parsed.getJSONObject(i);
        if ("sheet".equals(object.getString("class"))) {
          sheets.add(object);
        }
      }
    }

    final MindMap result;

    if (sheets.isEmpty()) {
      result = new MindMap(true);
      assertNotNull(result.getRoot()).setText("Empty");
    } else {
      result = convertJsonSheet(zipFile, sheets.get(0));
    }

    return result;
  }

  @Nullable
  private static String convertTextAlign(@Nullable final String align) {
    return align;
  }

  private static void convertTopic(@Nonnull ZipFile zipFile,
                                   @Nonnull final MindMap map,
                                   @Nullable final Topic parent,
                                   @Nullable Topic pregeneratedOne,
                                   @Nonnull final JSONObject topicElement,
                                   @Nonnull Map<String, Topic> idTopicMap,
                                   @Nonnull final Map<String, String> linksBetweenTopics)
      throws Exception {
    final Topic topicToProcess;

    if (pregeneratedOne == null) {
      topicToProcess = assertNotNull(parent).makeChild("", null);
    } else {
      topicToProcess = pregeneratedOne;
    }

    topicToProcess.setText(assertNotNull(topicElement.getString("title")));

    final String theTopicId = assertNotNull(topicElement.getString("id"));

    idTopicMap.put(theTopicId, topicToProcess);

    final JSONObject style = topicElement.has("style") ? topicElement.getJSONObject("style") : null;
    if (style != null) {
      final String styleId = assertNotNull(style.getString("id"));
      final JSONObject properties =
          style.has("properties") ? style.getJSONObject("properties") : null;
      if (properties != null) {
        final String fillColor =
            properties.has("svg:fill") ? properties.getString("svg:fill") : null;
        final String fontColor =
            properties.has("fo:color") ? properties.getString("fo:color") : null;
        final String textAlign =
            properties.has("fo:text-align") ? properties.getString("fo:text-align") : null;
        final String borderLineColor =
            properties.has("border-line-color") ? properties.getString("border-line-color") : null;

        if (fillColor != null) {
          topicToProcess.setAttribute(StandardTopicAttribute.ATTR_FILL_COLOR.getText(),
              fillColor);
        }
        if (fontColor != null) {
          topicToProcess.setAttribute(StandardTopicAttribute.ATTR_TEXT_COLOR.getText(),
              fontColor);
        }
        if (borderLineColor != null) {
          topicToProcess.setAttribute(StandardTopicAttribute.ATTR_BORDER_COLOR.getText(),
              borderLineColor);
        }
        if (textAlign != null) {
          topicToProcess.setAttribute("align", convertTextAlign(textAlign));
        }
      }
    }

    final String attachedImage = extractFirstAttachedImageAsBase64(zipFile, topicElement);
    if (attachedImage != null && !attachedImage.isEmpty()) {
      topicToProcess.setAttribute(ImageVisualAttributePlugin.ATTR_KEY, attachedImage);
    }

    final String xlink = topicElement.has("href") ? topicElement.getString("href") : null;
    if (xlink != null && !xlink.isEmpty()) {
      if (xlink.startsWith("file:")) {
        try {
          topicToProcess.setExtra(new ExtraFile(new MMapURI(new File(xlink.substring(5)).toURI())));
        } catch (Exception ex) {
          LOGGER.error("Can't convert file link : " + xlink, ex);
        }
      } else if (xlink.startsWith("xmind:#")) {
        linksBetweenTopics.put(theTopicId, xlink.substring(7));
      } else {
        try {
          topicToProcess.setExtra(new ExtraLink(new MMapURI(URI.create(xlink))));
        } catch (Exception ex) {
          LOGGER.error("Can't convert link : " + xlink, ex);
        }
      }
    }

    final String extractedNote = extractNote(topicElement);

    if (!extractedNote.isEmpty()) {
      topicToProcess.setExtra(new ExtraNote(extractedNote));
    }

    final JSONObject children =
        topicElement.has("children") ? topicElement.getJSONObject("children") : null;
    if (children != null) {
      final JSONArray attached =
          children.has("attached") ? children.getJSONArray("attached") : null;
      if (attached != null) {
        for (final Object c : attached) {
          final JSONObject child = (JSONObject) c;
          convertTopic(zipFile, map, topicToProcess, null, child, idTopicMap, linksBetweenTopics);
        }
      }
      final JSONArray detached =
          children.has("detached") ? children.getJSONArray("detached") : null;
      if (detached != null) {
        for (final Object c : detached) {
          final JSONObject child = (JSONObject) c;
          convertTopic(zipFile, map, topicToProcess, null, child, idTopicMap, linksBetweenTopics);
        }
      }
    }
  }

  @Nonnull
  private MindMap convertJsonSheet(@Nonnull final ZipFile file,
                                   @Nonnull final JSONObject sheet) throws Exception {
    final MindMap resultedMap = new MindMap(true);
    resultedMap.setAttribute(MindMapPanel.ATTR_SHOW_JUMPS, "true");

    final Topic rootTopic = assertNotNull(resultedMap.getRoot());
    rootTopic.setText("Empty sheet");

    final Map<String, Topic> topicIdMap = new HashMap<String, Topic>();
    final Map<String, String> linksBetweenTopics = new HashMap<String, String>();

    final JSONObject rootTopicObj = sheet.getJSONObject("rootTopic");
    if (rootTopicObj != null) {
      convertTopic(file, resultedMap, null, rootTopic, rootTopicObj, topicIdMap,
          linksBetweenTopics);
    }

    if (sheet.has("relationships")) {
      for (final Object l : sheet.getJSONArray("relationships")) {
        final JSONObject relationship = (JSONObject) l;
        final String end1 = relationship.getString("end1Id");
        final String end2 = relationship.getString("end2Id");
        if (!linksBetweenTopics.containsKey(end1)) {
          final Topic startTopic = topicIdMap.get(end1);
          final Topic endTopic = topicIdMap.get(end2);
          if (startTopic != null && endTopic != null) {
            startTopic.setExtra(ExtraTopic.makeLinkTo(resultedMap, endTopic));
          }
        }
      }
    }

    for (final Map.Entry<String, String> e : linksBetweenTopics.entrySet()) {
      final Topic startTopic = topicIdMap.get(e.getKey());
      final Topic endTopic = topicIdMap.get(e.getValue());
      if (startTopic != null && endTopic != null) {
        startTopic.setExtra(ExtraTopic.makeLinkTo(resultedMap, endTopic));
      }
    }

    return resultedMap;
  }

  @Nonnull
  private MindMap convertXmlContent(
      @Nonnull final XMindStyles style,
      @Nonnull final ZipFile zipFile,
      @Nonnull final InputStream content
  ) throws Exception {
    final Document document =
        Utils.loadXmlDocument(assertNotNull(content), null, true);

    final Element rootElement = document.getDocumentElement();
    if (!rootElement.getTagName().equals("xmap-content")) {
      makeWrongFormatException();
    }

    final List<Element> xmlSheets =
        Utils.findDirectChildrenForName(document.getDocumentElement(), "sheet");

    final MindMap result;

    if (xmlSheets.isEmpty()) {
      result = new MindMap(true);
      assertNotNull(result.getRoot()).setText("Empty");
    } else {
      result = convertXmlSheet(style, zipFile, xmlSheets.get(0));
    }

    return result;
  }

  @Nonnull
  private MindMap convertXmlSheet(@Nonnull final XMindStyles styles, @Nonnull final ZipFile file,
                                  @Nonnull final Element sheet) throws Exception {
    final MindMap resultedMap = new MindMap(true);
    resultedMap.setAttribute(MindMapPanel.ATTR_SHOW_JUMPS, "true");

    final Topic rootTopic = assertNotNull(resultedMap.getRoot());
    rootTopic.setText("Empty sheet");

    final Map<String, Topic> topicIdMap = new HashMap<String, Topic>();
    final Map<String, String> linksBetweenTopics = new HashMap<String, String>();

    final List<Element> rootTopics = Utils.findDirectChildrenForName(sheet, "topic");
    if (!rootTopics.isEmpty()) {
      convertTopic(file, styles, resultedMap, null, rootTopic, rootTopics.get(0), topicIdMap,
          linksBetweenTopics);
    }

    for (final Element l : Utils.findDirectChildrenForName(sheet, "relationships")) {
      for (final Element r : Utils.findDirectChildrenForName(l, "relationship")) {
        final String end1 = r.getAttribute("end1");
        final String end2 = r.getAttribute("end2");
        if (!linksBetweenTopics.containsKey(end1)) {
          final Topic startTopic = topicIdMap.get(end1);
          final Topic endTopic = topicIdMap.get(end2);
          if (startTopic != null && endTopic != null) {
            startTopic.setExtra(ExtraTopic.makeLinkTo(resultedMap, endTopic));
          }
        }
      }
    }

    for (final Map.Entry<String, String> e : linksBetweenTopics.entrySet()) {
      final Topic startTopic = topicIdMap.get(e.getKey());
      final Topic endTopic = topicIdMap.get(e.getValue());
      if (startTopic != null && endTopic != null) {
        startTopic.setExtra(ExtraTopic.makeLinkTo(resultedMap, endTopic));
      }
    }

    return resultedMap;
  }

  @Override
  @Nullable
  public String getMnemonic() {
    return "xmind";
  }

  @Override
  @Nonnull
  public String getName(@Nonnull final PluginContext context) {
    return Texts.getString("MMDImporters.XMind2MindMap.Name");
  }

  @Override
  @Nonnull
  public String getReference(@Nonnull final PluginContext context) {
    return Texts.getString("MMDImporters.XMind2MindMap.Reference");
  }

  @Override
  @Nonnull
  public Icon getIcon(@Nonnull final PluginContext context) {
    return ICO;
  }

  @Override
  public int getOrder() {
    return 4;
  }

  private static final class XMindStyle {

    private final Color foreground;
    private final Color background;
    private final Color border;
    private final String textAlign;

    private XMindStyle(@Nullable final Color background,
                       @Nullable final Color front,
                       @Nullable final Color border,
                       @Nullable final String align) {
      this.background = background;
      this.foreground = front;
      this.border = border;
      this.textAlign = align;
    }

    private XMindStyle(@Nonnull final Element style) {
      Color back = null;
      Color front = null;
      Color bord = null;
      String align = null;

      for (final Element t : Utils.findDirectChildrenForName(style, "topic-properties")) {
        final String colorFill = t.getAttribute("svg:fill");
        final String colorText = t.getAttribute("fo:color");
        final String textAlign = t.getAttribute("fo:text-align");

        final String colorBorder = t.getAttribute("border-line-color");
        back = Utils.html2color(colorFill, false);
        front = Utils.html2color(colorText, false);
        bord = Utils.html2color(colorBorder, false);
        align = convertTextAlign(textAlign);
      }

      this.foreground = front;
      this.background = back;
      this.border = bord;
      this.textAlign = align;
    }

    private void attachTo(@Nonnull final Topic topic) {
      if (this.background != null) {
        topic.setAttribute(StandardTopicAttribute.ATTR_FILL_COLOR.getText(),
            Utils.color2html(this.background, false));
      }
      if (this.foreground != null) {
        topic.setAttribute(StandardTopicAttribute.ATTR_TEXT_COLOR.getText(),
            Utils.color2html(this.foreground, false));
      }
      if (this.border != null) {
        topic.setAttribute(StandardTopicAttribute.ATTR_BORDER_COLOR.getText(),
            Utils.color2html(this.border, false));
      }
      if (this.textAlign != null) {
        topic.setAttribute("align", this.textAlign);
      }
    }

  }

  private static final class XMindStyles {

    private final Map<String, XMindStyle> stylesMap = new HashMap<String, XMindStyle>();

    private XMindStyles(@Nonnull final ZipFile zipFile) {
      try {
        final InputStream stylesXml = Utils.findInputStreamForResource(zipFile, "styles.xml");
        if (stylesXml != null) {
          final Document parsedStyles = Utils.loadXmlDocument(stylesXml, null, true);

          final Element root = parsedStyles.getDocumentElement();

          if ("xmap-styles".equals(root.getTagName())) {
            for (final Element styles : Utils.findDirectChildrenForName(root, "styles")) {
              for (final Element style : Utils.findDirectChildrenForName(styles, "style")) {
                final String id = style.getAttribute("id");
                if (!id.isEmpty() && "topic".equals(style.getAttribute("type"))) {
                  this.stylesMap.put(id, new XMindStyle(style));
                }
              }
            }
          }
        }
      } catch (Exception ex) {
        LOGGER.error("Can't extract XMIND styles", ex);
      }
    }

    private void setStyle(@Nonnull final String styleId, @Nonnull final Topic topic) {
      final XMindStyle foundStyle = this.stylesMap.get(styleId);
      if (foundStyle != null) {
        foundStyle.attachTo(topic);
      }
    }
  }
}
