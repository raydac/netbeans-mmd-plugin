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

import static com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute.ATTR_FILL_COLOR;
import static com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute.ATTR_TEXT_COLOR;
import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.api.AbstractImporter;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.plugins.attributes.images.ImageVisualAttributePlugin;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.Texts;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractCollapsableElement;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.mindmap.swing.services.IconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.jsoup.parser.Parser;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Freemind2MindMapImporter extends AbstractImporter {

  private static final Icon ICO =
      ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_EXPORT_FREEMIND);

  private static final Logger LOGGER = LoggerFactory.getLogger(Freemind2MindMapImporter.class);

  private static final Set<String> TOKEN_NEEDS_NEXT_LINE =
      new HashSet<>(Arrays.asList("br", "div", "p", "li"));

  private static String findArrowlinkDestination(final Element element) {
    final List<Element> arrows = Utils.findDirectChildrenForName(element, "arrowlink");
    return arrows.isEmpty() ? "" : findAttribute(arrows.get(0), "destination");
  }

  private static void processImageLinkForTopic(final File rootFolder,
                                               final Topic topic,
                                               final String[] imageUrls) {
    for (final String s : imageUrls) {
      try {
        URI imageUri = URI.create(s);

        final File file;
        if (imageUri.isAbsolute()) {
          file = new File(imageUri);
        } else {
          file = new File(rootFolder.toURI().resolve(imageUri));
        }

        if (file.isFile()) {
          topic.putAttribute(ImageVisualAttributePlugin.ATTR_KEY,
              Utils.rescaleImageAndEncodeAsBase64(file, -1));
          break;
        }
      } catch (Exception ex) {
        LOGGER.warn("Can't decode or load image for URI : " + s);
      }
    }
  }

  private static StringBuilder processHtmlElement(final NodeList list,
                                                  final StringBuilder builder,
                                                  final List<String> imageURLs) {
    for (int i = 0; i < list.getLength(); i++) {
      final Node n = list.item(i);
      switch (n.getNodeType()) {
        case Node.TEXT_NODE: {
          builder.append(n.getTextContent());
        }
        break;
        case Node.ELEMENT_NODE: {
          final String tag = n.getNodeName();
          if ("img".equals(tag)) {
            final String source = findAttribute((Element) n, "src");
            if (!source.isEmpty()) {
              imageURLs.add(source);
            }
          }

          if (TOKEN_NEEDS_NEXT_LINE.contains(tag)) {
            builder.append('\n');
          }
          processHtmlElement(n.getChildNodes(), builder, imageURLs);
        }
        break;
        default: {
          // just ignoring  other elements
        }
        break;
      }
    }
    return builder;
  }

  private static StringBuilder extractTextFromHtmlElement(final Element element,
                                                          final StringBuilder buffer,
                                                          final List<String> imageURLs) {
    processHtmlElement(element.getChildNodes(), buffer, imageURLs);
    return buffer;
  }

  private static List<RichContent> extractRichContent(final Element richContentElement) {
    final List<Element> richContents =
        Utils.findDirectChildrenForName(richContentElement, "richcontent");

    final List<RichContent> result = new ArrayList<>();

    final List<String> foundImageUrls = new ArrayList<>();

    for (final Element e : richContents) {
      final String textType = findAttribute(e, "type");
      try {
        foundImageUrls.clear();
        final RichContentType type = RichContentType.valueOf(textType);
        final String text =
            extractTextFromHtmlElement(e, new StringBuilder(), foundImageUrls).toString()
                .replace("\r", "");
        result.add(new RichContent(type, text, foundImageUrls));
      } catch (IllegalArgumentException ex) {
        LOGGER.warn("Unknown node type : " + textType);
      }
    }

    return result;
  }

  private static String findAttribute(final Element element,
                                      final String attribute) {
    final NamedNodeMap map = element.getAttributes();
    for (int i = 0; i < map.getLength(); i++) {
      final Attr attr = (Attr) map.item(i);
      if (attribute.equalsIgnoreCase(attr.getName())) {
        return attr.getValue();
      }
    }
    return "";
  }

  @Override
  public MindMap doImport(final PluginContext context) throws Exception {
    final File file = this.selectFileForExtension(context,
        Texts.getString("MMDImporters.Freemind2MindMap.openDialogTitle"), null, "mm",
        "Freemind files (.MM)", Texts.getString("MMDImporters.ApproveImport"));

    if (file == null) {
      return null;
    }

    try (final FileInputStream in = new FileInputStream(file)) {
      final File rootFolder = file.getParentFile();
      return extractTopics(rootFolder == null ? file : rootFolder, in);
    }
  }

  MindMap extractTopics(final File rootFolder,
                        final FileInputStream inputStream)
      throws ParserConfigurationException, IOException, XPathExpressionException {
    final Document document = Utils.load(inputStream, "UTF-8", Parser.xmlParser(), true);

    final XPath xpath = XPathFactory.newInstance().newXPath();
    final Element rootElement =
        (Element) xpath.evaluate("/map", document, XPathConstants.NODE);

    if (rootElement == null) {
      throw new IllegalArgumentException("Can't parse freemind file as xhtml");
    }

    final Map<String, Topic> idTopicMap = new HashMap<>();
    final Map<String, String> linksMap = new HashMap<>();
    final MindMap resultedMap = new MindMap(true);
    resultedMap.putAttribute(MindMapPanel.ATTR_SHOW_JUMPS, "true");

    final List<Element> list = Utils.findDirectChildrenForName(rootElement, "node");
    if (list.isEmpty()) {
      requireNonNull(resultedMap.getRoot()).setText("Empty");
    } else {
      parseTopic(rootFolder, resultedMap, null, resultedMap.getRoot(), list.get(0), idTopicMap,
          linksMap);
    }

    for (final Map.Entry<String, String> l : linksMap.entrySet()) {
      final Topic start = idTopicMap.get(l.getKey());
      final Topic end = idTopicMap.get(l.getValue());
      if (start != null && end != null) {
        start.setExtra(ExtraTopic.makeLinkTo(resultedMap, end));
      }
    }

    return resultedMap;
  }

  private void parseTopic(final File rootFolder, final MindMap map,
                          final Topic parent, final Topic preGeneratedTopic,
                          final Element element, final Map<String, Topic> idTopicMap,
                          final Map<String, String> linksMap) {

    final String text = findAttribute(element, "text");
    final String id = findAttribute(element, "id");
    final String position = findAttribute(element, "position");
    final String arrowDestination = findArrowlinkDestination(element);
    final String backgroundColor = findAttribute(element, "background_color");
    final String color = findAttribute(element, "color");
    final String link = findAttribute(element, "link");

    final List<RichContent> foundRichContent = extractRichContent(element);

    final Topic topicToProcess;
    if (preGeneratedTopic == null) {
      topicToProcess = requireNonNull(parent).makeChild(text, null);
      if (requireNonNull(parent).isRoot()) {
        if ("left".equalsIgnoreCase(position)) {
          AbstractCollapsableElement.makeTopicLeftSided(topicToProcess, true);
        }
      }
    } else {
      topicToProcess = preGeneratedTopic;
    }

    if (!color.isEmpty()) {
      final Color colorConverted = Utils.html2color(color, false);
      final Color backgroundColorConverted = Utils.html2color(backgroundColor, false);

      if (colorConverted != null) {
        topicToProcess.putAttribute(ATTR_TEXT_COLOR.getText(),
            Utils.color2html(colorConverted, false));
      }

      if (backgroundColorConverted != null) {
        topicToProcess.putAttribute(ATTR_FILL_COLOR.getText(),
            Utils.color2html(backgroundColorConverted, false));
      } else {
        if (colorConverted != null) {
          topicToProcess.putAttribute(ATTR_FILL_COLOR.getText(),
              Utils.color2html(Utils.makeContrastColor(colorConverted), false));
        }
      }
    }

    topicToProcess.setText(text);

    for (final RichContent r : foundRichContent) {
      switch (r.getType()) {
        case NODE: {
          if (!r.getText().isEmpty()) {
            topicToProcess.setText(r.getText().trim());
          }
        }
        break;
        case NOTE: {
          if (!r.getText().isEmpty()) {
            topicToProcess.setExtra(new ExtraNote(r.getText().trim()));
          }
        }
        break;
      }
      processImageLinkForTopic(rootFolder, topicToProcess, r.getFoundImageURLs());
    }

    if (!link.isEmpty()) {
      if (link.startsWith("#")) {
        if (!id.isEmpty()) {
          linksMap.put(id, link.substring(1));
        }
      } else {
        try {
          topicToProcess.setExtra(new ExtraLink(link));
        } catch (URISyntaxException ex) {
          LOGGER.warn("Can't convert link: " + link);
        }
      }
    }

    if (!id.isEmpty()) {
      idTopicMap.put(id, topicToProcess);
      if (!arrowDestination.isEmpty()) {
        linksMap.put(id, arrowDestination);
      }
    }

    for (final Element e : Utils.findDirectChildrenForName(element, "node")) {
      parseTopic(rootFolder, map, topicToProcess, null, e, idTopicMap, linksMap);
    }
  }

  @Override
  public String getMnemonic() {
    return "freemind";
  }

  @Override
  public String getName(final PluginContext context) {
    return Texts.getString("MMDImporters.Freemind2MindMap.Name");
  }

  @Override
  public String getReference(final PluginContext context) {
    return Texts.getString("MMDImporters.Freemind2MindMap.Reference");
  }

  @Override
  public Icon getIcon(final PluginContext context) {
    return ICO;
  }

  @Override
  public int getOrder() {
    return 3;
  }

  @Override
  public boolean isCompatibleWithFullScreenMode() {
    return false;
  }

  private enum RichContentType {
    NODE, NOTE
  }

  private static final class RichContent {

    private final RichContentType type;
    private final String text;
    private final String[] imageUrls;

    private RichContent(final RichContentType type,
                        final String text,
                        final List<String> foundImageUrls) {
      this.type = type;
      this.text = text;
      this.imageUrls = foundImageUrls.toArray(new String[0]);
    }

    private String[] getFoundImageURLs() {
      return this.imageUrls;
    }

    private RichContentType getType() {
      return this.type;
    }

    private String getText() {
      return this.text;
    }
  }
}
