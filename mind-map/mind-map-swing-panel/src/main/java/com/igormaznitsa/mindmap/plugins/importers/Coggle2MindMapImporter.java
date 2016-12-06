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
package com.igormaznitsa.mindmap.plugins.importers;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import com.igormaznitsa.mindmap.plugins.api.AbstractImporter;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.Texts;
import com.igormaznitsa.mindmap.swing.services.IconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.attributes.images.ImageVisualAttributePlugin;
import com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractCollapsableElement;
import com.igormaznitsa.mindmap.swing.panel.utils.MindMapUtils;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;

public class Coggle2MindMapImporter extends AbstractImporter {

  private static final Icon ICO = ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_IMPORT_COGGLE2MM);

  private static final Logger LOGGER = LoggerFactory.getLogger(Coggle2MindMapImporter.class);

  @Nonnull
  private static Document extractDocument(@Nonnull final InputStream xmlStream) throws Exception {
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setIgnoringComments(true);
    factory.setValidating(false);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

    final DocumentBuilder builder = factory.newDocumentBuilder();

    final Document document;
    try {
      document = builder.parse(xmlStream);
    }
    finally {
      IOUtils.closeQuietly(xmlStream);
    }

    return document;
  }

  @Override
  @Nullable
  public MindMap doImport(@Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nullable final Topic actionTopic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) throws Exception {
    final File file = this.selectFileForExtension(panel, Texts.getString("MMDImporters.Coggle2MindMap.openDialogTitle"), "mm", "Coggle files (.MM)", Texts.getString("MMDImporters.ApproveImport"));

    if (file == null) {
      return null;
    }

    final String content = FileUtils.readFileToString(file, "UTF-8");

    final MindMap result = new MindMap(null, true);
    Assertions.assertNotNull(result.getRoot()).setText("Empty");

    final Document document = extractDocument(new ByteArrayInputStream(content.getBytes("UTF-8")));

    final Element root = document.getDocumentElement();
    if ("map".equals(root.getTagName())) {
      final List<Element> nodes = getDirectChildren(root, "node");
      if (!nodes.isEmpty()) {
        parseTopic(result, null, result.getRoot(), nodes.get(0));
      }
    } else {
      throw new IllegalArgumentException("File is not Coggle mind map");
    }

    return result;
  }

  private static final Pattern MD_IMAGE_LINK = Pattern.compile("\\!\\[(.*?)\\]\\((.*?)\\)", Pattern.MULTILINE | Pattern.UNICODE_CASE);
  private static final Pattern MD_URL_LINK = Pattern.compile("(?<!\\!)\\[(.*?)\\]\\((.*?)\\)", Pattern.MULTILINE | Pattern.UNICODE_CASE);

  @Nonnull
  @MustNotContainNull
  private List<String> extractImageURLs(@Nonnull final String mdText, @Nonnull final StringBuilder resultText) {
    final List<String> result = new ArrayList<String>();
    final Matcher matcher = MD_IMAGE_LINK.matcher(mdText);
    int lastFoundEnd = 0;
    while (matcher.find()) {
      final String text = matcher.group(1);
      result.add(matcher.group(2));
      resultText.append(mdText, lastFoundEnd, matcher.start()).append(text);
      lastFoundEnd = matcher.end();
    }

    if (lastFoundEnd < mdText.length()) {
      resultText.append(mdText, lastFoundEnd, mdText.length());
    }

    return result;
  }

  @Nonnull
  @MustNotContainNull
  private List<String> extractURLs(@Nonnull final String mdText, @Nonnull final StringBuilder resultText) {
    final List<String> result = new ArrayList<String>();
    final Matcher matcher = MD_URL_LINK.matcher(mdText);
    int lastFoundEnd = 0;
    while (matcher.find()) {
      final String text = matcher.group(1);
      result.add(matcher.group(2));
      resultText.append(mdText, lastFoundEnd, matcher.start()).append(text);
      lastFoundEnd = matcher.end();
    }

    if (lastFoundEnd < mdText.length()) {
      resultText.append(mdText, lastFoundEnd, mdText.length());
    }

    return result;
  }

  @Nullable
  private static String loadImageForURLAndEncode(@Nonnull final String imageUrl) {
    String result = null;

    final Image loadedImage;
    try {
      loadedImage = ImageIO.read(new URL(imageUrl));
    }
    catch (final Exception ex) {
      LOGGER.error("Can't load image for URL : " + imageUrl, ex);
      return null;
    }

    if (loadedImage != null) {
      try {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write((RenderedImage) loadedImage, "png", bos); //NOI18N
        bos.close();
        result = Utils.base64encode(bos.toByteArray());
      }
      catch (final Exception ex) {
        LOGGER.error("Can't encode image into png", ex);
      }
    }

    return result;
  }

  @Nullable
  private static String loadFirstSuccessfulImage(@Nonnull @MustNotContainNull final List<String> urls) {
    String result = null;
    for (final String url : urls) {
      result = loadImageForURLAndEncode(url);
      if (result != null) {
        break;
      }
    }
    return result;
  }

  @Nullable
  private static MMapURI getFirstSuccessfulURL(@Nonnull @MustNotContainNull final List<String> urls) {
    MMapURI result = null;
    for (final String url : urls) {
      try {
        result = new MMapURI(url);
      }
      catch (final Exception ex) {
        LOGGER.error("Can't recognize URI : " + url, ex);
      }
      if (result != null) {
        break;
      }
    }
    return result;
  }

  private void parseTopic(@Nonnull final MindMap map, @Nullable final Topic parent, @Nullable final Topic preGeneratedOne, @Nonnull final Element element) {
    final Topic topicToProcess;
    if (preGeneratedOne == null) {
      topicToProcess = Assertions.assertNotNull(parent).makeChild("", null);
    } else {
      topicToProcess = preGeneratedOne;
    }

    final StringBuilder resultTextBuffer = new StringBuilder();
    final List<String> foundImageURLs = extractImageURLs(element.getAttribute("TEXT"), resultTextBuffer);
    String nodeText = resultTextBuffer.toString();
    resultTextBuffer.setLength(0);

    final List<String> foundLinkURLs = extractURLs(nodeText, resultTextBuffer);
    final MMapURI succesfullDecodedUrl = getFirstSuccessfulURL(foundLinkURLs);

    nodeText = resultTextBuffer.toString();

    final String encodedImage = loadFirstSuccessfulImage(foundImageURLs);
    if (encodedImage != null) {
      topicToProcess.setAttribute(ImageVisualAttributePlugin.ATTR_KEY, encodedImage);
    }

    if (succesfullDecodedUrl != null) {
      topicToProcess.setExtra(new ExtraLink(succesfullDecodedUrl));
    }

    final StringBuilder note = new StringBuilder();

    if (!foundLinkURLs.isEmpty() && (succesfullDecodedUrl == null || foundLinkURLs.size() > 1)) {
      if (note.length() > 0) {
        note.append("\n\n");
      }
      note.append("Detected URLs\n---------------");
      for (final String u : foundLinkURLs) {
        note.append('\n').append(u);
      }
    }

    if (!foundImageURLs.isEmpty() && (encodedImage == null || foundImageURLs.size() > 1)) {
      if (note.length() > 0) {
        note.append("\n\n");
      }
      note.append("Detected image links\n---------------");
      for (final String u : foundImageURLs) {
        note.append('\n').append(u);
      }
    }

    final String text = nodeText.replace("\r", "");
    final String position = element.getAttribute("POSITION");
    final String folded = element.getAttribute("FOLDED");

    Color edgeColor = null;
    for (final Element e : getDirectChildren(element, "edge")) {
      try {
        edgeColor = Utils.html2color(e.getAttribute("COLOR"), false);
      }
      catch (final Exception ex) {
        LOGGER.error("Can't parse color value", ex);
      }
    }

    topicToProcess.setText(text);

    if (parent != null && parent.isRoot() && "left".equalsIgnoreCase(position)) {
      AbstractCollapsableElement.makeTopicLeftSided(topicToProcess, true);
    }

    if ("true".equalsIgnoreCase(folded)) {
      MindMapUtils.setCollapsed(topicToProcess, true);
    }

    if (edgeColor != null) {
      topicToProcess.setAttribute(StandardTopicAttribute.ATTR_BORDER_COLOR.getText(), Utils.color2html(edgeColor, false));
    }

    if (note.length() > 0) {
      topicToProcess.setExtra(new ExtraNote(note.toString()));
    }

    for (final Element c : getDirectChildren(element, "node")) {
      parseTopic(map, topicToProcess, null, c);
    }
  }

  @Nonnull
  @MustNotContainNull
  public static List<Element> getDirectChildren(@Nonnull final Element element, @Nonnull final String name) {
    final NodeList found = element.getElementsByTagName(name);
    final List<Element> resultList = new ArrayList<Element>();

    for (int i = 0; i < found.getLength(); i++) {
      if (found.item(i).getParentNode().equals(element) && found.item(i) instanceof Element) {
        resultList.add((Element) found.item(i));
      }
    }

    return resultList;
  }

  @Override
  @Nullable
  public String getMnemonic() {
    return "coggle";
  }

  @Override
  @Nonnull
  public String getName(@Nonnull final MindMapPanel panel, @Nullable final Topic actionTopic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
    return Texts.getString("MMDImporters.Coggle2MindMap.Name");
  }

  @Override
  @Nonnull
  public String getReference(@Nonnull final MindMapPanel panel, @Nullable final Topic actionTopic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
    return Texts.getString("MMDImporters.Coggle2MindMap.Reference");
  }

  @Override
  @Nonnull
  public Icon getIcon(@Nonnull final MindMapPanel panel, @Nullable final Topic actionTopic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
    return ICO;
  }

  @Override
  public int getOrder() {
    return 5;
  }
}
