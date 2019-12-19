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

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.api.AbstractImporter;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.plugins.attributes.images.ImageVisualAttributePlugin;
import com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute;
import com.igormaznitsa.mindmap.swing.panel.Texts;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractCollapsableElement;
import com.igormaznitsa.mindmap.swing.panel.utils.MindMapUtils;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.mindmap.swing.services.IconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;
import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CoggleMM2MindMapImporter extends AbstractImporter {

  private static final Icon ICO = ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_IMPORT_COGGLE2MM);

  private static final Logger LOGGER = LoggerFactory.getLogger(CoggleMM2MindMapImporter.class);
  private static final Pattern MD_IMAGE_LINK = Pattern.compile("\\!\\[(.*?)\\]\\((.*?)\\)", Pattern.MULTILINE | Pattern.UNICODE_CASE);
  private static final Pattern MD_URL_LINK = Pattern.compile("(?<!\\!)\\[(.*?)\\]\\((.*?)\\)", Pattern.MULTILINE | Pattern.UNICODE_CASE);

  @Nullable
  private static String loadImageForURLAndEncode(@Nonnull final String imageUrl) {
    String result = null;

    final Image loadedImage;
    try {
      loadedImage = ImageIO.read(new URL(imageUrl));
    } catch (final Exception ex) {
      LOGGER.error("Can't load image for URL : " + imageUrl, ex);
      return null;
    }

    if (loadedImage != null) {
      try {
        result = Utils.rescaleImageAndEncodeAsBase64(loadedImage, -1);
      } catch (final Exception ex) {
        LOGGER.error("Can't decode image", ex);
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
      } catch (final Exception ex) {
        LOGGER.error("Can't recognize URI : " + url, ex);
      }
      if (result != null) {
        break;
      }
    }
    return result;
  }

  @Override
  @Nullable
  public MindMap doImport(@Nonnull final PluginContext context) throws Exception {
    final File file = this.selectFileForExtension(context, Texts.getString("MMDImporters.CoggleMM2MindMap.openDialogTitle"), null, "mm", "Coggle MM files (.MM)", Texts.getString("MMDImporters.ApproveImport"));

    if (file == null) {
      return null;
    }

    final Document document = Utils.loadXmlDocument(new FileInputStream(file), "UTF-8", true);

    final MindMap result = new MindMap(true);
    Assertions.assertNotNull(result.getRoot()).setText("Empty");

    final Element root = document.getDocumentElement();
    if ("map".equals(root.getTagName())) {
      final List<Element> nodes = Utils.findDirectChildrenForName(root, "node");
      if (!nodes.isEmpty()) {
        parseTopic(result, null, result.getRoot(), nodes.get(0));
      }
    } else {
      throw new IllegalArgumentException("File is not Coggle mind map");
    }

    return result;
  }

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
    for (final Element e : Utils.findDirectChildrenForName(element, "edge")) {
      try {
        edgeColor = Utils.html2color(e.getAttribute("COLOR"), false);
      } catch (final Exception ex) {
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
      topicToProcess.setAttribute(StandardTopicAttribute.ATTR_FILL_COLOR.getText(), Utils.color2html(edgeColor, false));
      topicToProcess.setAttribute(StandardTopicAttribute.ATTR_TEXT_COLOR.getText(), Utils.color2html(Utils.makeContrastColor(edgeColor), false));
    }

    if (note.length() > 0) {
      topicToProcess.setExtra(new ExtraNote(note.toString()));
    }

    for (final Element c : Utils.findDirectChildrenForName(element, "node")) {
      parseTopic(map, topicToProcess, null, c);
    }
  }

  @Override
  @Nullable
  public String getMnemonic() {
    return "cogglemm";
  }

  @Override
  @Nonnull
  public String getName(@Nonnull final PluginContext context) {
    return Texts.getString("MMDImporters.CoggleMM2MindMap.Name");
  }

  @Override
  @Nonnull
  public String getReference(@Nonnull final PluginContext context) {
    return Texts.getString("MMDImporters.CoggleMM2MindMap.Reference");
  }

  @Override
  @Nonnull
  public Icon getIcon(@Nonnull final PluginContext context) {
    return ICO;
  }

  @Override
  public int getOrder() {
    return 5;
  }

  @Override
  public boolean isCompatibleWithFullScreenMode() {
    return false;
  }
}
