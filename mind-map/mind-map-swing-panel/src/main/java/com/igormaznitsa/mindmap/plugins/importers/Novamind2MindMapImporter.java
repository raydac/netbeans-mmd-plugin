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

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.MiscUtils;
import com.igormaznitsa.mindmap.model.StandardMmdAttributes;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.api.AbstractImporter;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.plugins.attributes.images.ImageVisualAttributePlugin;
import com.igormaznitsa.mindmap.swing.i18n.MmdI18n;
import com.igormaznitsa.mindmap.swing.ide.IDEBridgeFactory;
import com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.mindmap.swing.services.IconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;
import javax.swing.Icon;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Novamind2MindMapImporter extends AbstractImporter {

  private static final Icon ICO = ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_IMPORT_NOVAMIND2MM);

  private static final Logger LOGGER = LoggerFactory.getLogger(Novamind2MindMapImporter.class);

  private static void processURLLinks(final MindMap map, final ParsedContent model,
                                      final ParsedContent.TopicReference topicRef,
                                      final Map<String, Topic> mapTopicRefToTopics) {
    final Topic topic = mapTopicRefToTopics.get(topicRef.getId());
    if (topic != null) {
      final ParsedContent.ContentTopic ctopic = requireNonNull(topicRef.getContentTopic());

      final List<String> urls = ctopic.getLinkUrls();

      if (!urls.isEmpty()) {
        final List<Topic> insideLinksToTopics = new ArrayList<>();
        final List<MMapURI> insideLinksToURLs = new ArrayList<>();
        final List<MMapURI> insideLinksToFiles = new ArrayList<>();

        for (final String s : urls) {
          if (s.startsWith("novamind://topic/")) {
            final String targetTopicId = s.substring(17);
            final ParsedContent.TopicReference reference =
                model.findForTopicId(requireNonNull(model.getRootTopic()), targetTopicId);
            if (reference != null) {
              final Topic destTopic = mapTopicRefToTopics.get(reference.getId());
              if (destTopic != null) {
                insideLinksToTopics.add(destTopic);
              }
            }
          } else {
            MMapURI uri;
            try {
              uri = new MMapURI(s);
              if (!uri.isAbsolute()) {
                uri = null;
              }
            } catch (final URISyntaxException ex) {
              uri = null;
            }

            if (uri == null) {
              try {
                insideLinksToFiles.add(new MMapURI(new File(s).toURI()));
              } catch (Exception ex) {
                LOGGER.warn("Can't convert file link : " + s);
              }
            } else {
              insideLinksToURLs.add(uri);
            }
          }
        }

        if (insideLinksToTopics.size() == 1 && !topic.getExtras().containsKey(Extra.ExtraType.TOPIC)) {
          topic.setExtra(ExtraTopic.makeLinkTo(map, insideLinksToTopics.get(0)));
        } else {
          for (final Topic linkTo : insideLinksToTopics) {
            final Topic local = topic.makeChild("Linked to topic", null);
            local.setExtra(ExtraTopic.makeLinkTo(map, linkTo));
          }
        }

        if (insideLinksToURLs.size() == 1 && !topic.getExtras().containsKey(Extra.ExtraType.LINK)) {
          topic.setExtra(new ExtraLink(insideLinksToURLs.get(0)));
        } else {
          for (final MMapURI uri : insideLinksToURLs) {
            final Topic local = topic.makeChild("URL link", null);
            local.setExtra(new ExtraLink(uri));
          }
        }

        if (insideLinksToFiles.size() == 1 && !topic.getExtras().containsKey(Extra.ExtraType.FILE)) {
          topic.setExtra(new ExtraFile(insideLinksToFiles.get(0)));
        } else {
          for (final MMapURI file : insideLinksToFiles) {
            final Topic local = topic.makeChild("File link", null);
            local.setExtra(new ExtraFile(file));
          }
        }
      }

      for (final ParsedContent.TopicReference c : topicRef.getChildren()) {
        processURLLinks(map, model, c, mapTopicRefToTopics);
      }
    }
  }

  private static void convertContentTopicIntoMMTopic(final MindMap map, final Topic parent,
                                                     final ParsedContent.TopicReference node,
                                                     final Manifest manifest,
                                                     final Map<String, Topic> mapRefToTopic) {
    final Topic processing;
    if (parent == null) {
      processing = requireNonNull(map.getRoot());
    } else {
      processing = parent.makeChild("<ID not found>", null);
    }

    if (node.getColorBackground() != null) {
      processing.putAttribute(StandardTopicAttribute.ATTR_FILL_COLOR.getText(),
          Utils.color2html(node.getColorBackground(), false));
    }

    if (node.getColorBorder() != null) {
      processing.putAttribute(StandardTopicAttribute.ATTR_BORDER_COLOR.getText(),
          Utils.color2html(node.getColorBorder(), false));
    }

    if (node.getColorText() != null) {
      processing.putAttribute(StandardTopicAttribute.ATTR_TEXT_COLOR.getText(),
          Utils.color2html(node.getColorText(), false));
    }

    final ParsedContent.ContentTopic data = node.getContentTopic();
    if (data != null) {
      mapRefToTopic.put(node.getId(), processing);
      processing.setText(MiscUtils.ensureNotNull(data.getRichText(), ""));

      final String imageResourceId = data.getImageResourceId();
      if (imageResourceId != null) {
        final String imageBody = manifest.findResourceImage(imageResourceId);
        if (imageBody != null) {
          processing.putAttribute(ImageVisualAttributePlugin.ATTR_KEY, imageBody);
        }
      }

      if (data.getNotes() != null) {
        processing.setExtra(new ExtraNote(data.getNotes()));
      }

      for (final ParsedContent.TopicReference c : node.getChildren()) {
        convertContentTopicIntoMMTopic(map, processing, c, manifest, mapRefToTopic);
      }
    }
  }

  @Override
  public MindMap doImport(final PluginContext context) throws Exception {
    final File file = this.selectFileForExtension(context,
        MmdI18n.getInstance().findBundle().getString("MMDImporters.Novamind2MindMap.openDialogTitle"), null, "nm5",
        "Novamind files (.NM5)", MmdI18n.getInstance().findBundle().getString("MMDImporters.ApproveImport"));

    if (file == null) {
      return null;
    }

    return this.doImportFile(file);
  }

  MindMap doImportFile(final File file) throws IOException {
    final ZipFile zipFile = new ZipFile(file);
    final Manifest manifest = new Manifest(zipFile, "manifest.xml");
    final ParsedContent content = new ParsedContent(zipFile, "content.xml");

    final MindMap result = new MindMap(true);
    result.putAttribute(StandardMmdAttributes.MMD_ATTRIBUTE_GENERATOR_ID, IDEBridgeFactory.findInstance()
        .getIDEGeneratorId());
    result.putAttribute(StandardMmdAttributes.MMD_ATTRIBUTE_SHOW_JUMPS, "true");

    requireNonNull(result.getRoot()).setText("Empty map");

    final ParsedContent.TopicReference rootRef = content.getRootTopic();
    if (rootRef != null) {
      final Map<String, Topic> mapIdToTopic = new HashMap<>();
      convertContentTopicIntoMMTopic(result, null, rootRef, manifest, mapIdToTopic);

      for (final Map.Entry<String, String> link : content.getLinksBetweenTopics().entrySet()) {
        final Topic from = mapIdToTopic.get(link.getKey());
        final Topic to = mapIdToTopic.get(link.getValue());

        if (from != null && to != null) {
          from.setExtra(ExtraTopic.makeLinkTo(result, to));
        }
      }

      processURLLinks(result, content, rootRef, mapIdToTopic);
    }
    return result;
  }

  @Override
  public String getMnemonic() {
    return "novamind";
  }

  @Override
  public String getName(final PluginContext context) {
    return MmdI18n.getInstance().findBundle().getString("MMDImporters.Novamind2MindMap.Name");
  }

  @Override
  public String getReference(final PluginContext context) {
    return MmdI18n.getInstance().findBundle().getString("MMDImporters.Novamind2MindMap.Reference");
  }

  @Override
  public Icon getIcon(final PluginContext context) {
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

  private static final class Manifest {

    private final ZipFile zipFile;
    private final Map<String, Resource> resourceMap = new HashMap<>();

    private Manifest(final ZipFile zipFile, final String manifestPath) {
      this.zipFile = zipFile;
      try {
        final InputStream resourceIn = Utils.findInputStreamForResource(zipFile, manifestPath);
        if (resourceIn != null) {
          final Document document = Utils.loadXmlDocument(resourceIn, null, true);
          final Element main = document.getDocumentElement();
          if ("manifest".equals(main.getTagName())) {
            for (final Element e : Utils.findDirectChildrenForName(main, "resources")) {
              for (final Element r : Utils.findDirectChildrenForName(e, "resource")) {
                final String id = r.getAttribute("id");
                final String url = r.getAttribute("url");
                if (!id.isEmpty() && !url.isEmpty()) {
                  resourceMap.put(id, new Resource(url));
                }
              }
            }
          } else {
            LOGGER.warn("Can't find manifest tag, looks like that format changed");
          }
        }
      } catch (final Exception ex) {
        LOGGER.error("Can't parse resources list", ex);
      }
    }

    private String findResourceImage(final String id) {
      String result = null;

      final Resource resource = findResource(id);
      if (resource != null) {
        final byte[] imageFile = resource.extractResourceBody();
        if (imageFile != null) {
          try {
            result = Utils.rescaleImageAndEncodeAsBase64(new ByteArrayInputStream(imageFile), -1);
            if (result == null) {
              LOGGER.warn("Impossible to read image: " + resource.getUrl());
            }
          } catch (Exception ex) {
            LOGGER.error("Can't find or convert image resource : " + resource.getUrl(), ex);
          }
        }
      }

      return result;
    }

    private Resource findResource(final String id) {
      return this.resourceMap.get(id);
    }

    private final class Resource {

      private final String url;

      Resource(final String url) {
        this.url = url;
      }

      String getUrl() {
        return this.url;
      }

      byte[] extractResourceBody() {
        byte[] result = null;
        final String path = "Resources/" + url;
        try {
          result = Utils.toByteArray(zipFile, path);
        } catch (Exception ex) {
          LOGGER.error("Can't extract resource data : " + path, ex);
        }
        return result;
      }
    }
  }

  private static final class ParsedContent {

    private final Map<String, ContentTopic> topicsMap = new HashMap<>();
    private final Map<String, String> linksBetweenTopics = new HashMap<>();
    private final TopicReference rootRef;

    ParsedContent(final ZipFile file, final String path) {
      TopicReference mapRoot = null;

      try {
        final InputStream resourceIn = Utils.findInputStreamForResource(file, path);
        if (resourceIn != null) {
          final Document document = Utils.loadXmlDocument(resourceIn, null, true);
          final Element main = document.getDocumentElement();
          if ("document".equals(main.getTagName())) {
            for (final Element e : Utils.findDirectChildrenForName(main, "topics")) {
              for (final Element r : Utils.findDirectChildrenForName(e, "topic")) {
                final String id = r.getAttribute("id");
                this.topicsMap.put(id, new ContentTopic(id, r));
              }
            }

            final Element maps = Utils.findFirstElement(main, "maps");
            if (maps != null) {
              final Element firstMap = Utils.findFirstElement(maps, "map");
              if (firstMap != null) {
                final Element rootTopicNode = Utils.findFirstElement(firstMap, "topic-node");
                if (rootTopicNode != null) {
                  mapRoot = new TopicReference(rootTopicNode, this.topicsMap);
                }

                for (final Element l : Utils.findDirectChildrenForName(firstMap, "link-lines")) {
                  for (final Element tn : Utils.findDirectChildrenForName(l, "topic-node")) {
                    for (final Element lld : Utils.findDirectChildrenForName(tn, "link-line-data")) {
                      this.linksBetweenTopics.put(lld.getAttribute("start-topic-node-ref"), lld.getAttribute("end-topic-node-ref"));
                    }
                  }
                }

              } else {
                mapRoot = null;
              }
            } else {
              mapRoot = null;
            }

          } else {
            LOGGER.warn("Can't find document, looks like that format changed");
          }
        }
      } catch (final Exception ex) {
        LOGGER.error("Can't parse resources list", ex);
      }

      this.rootRef = mapRoot;
    }

    TopicReference findForTopicId(final TopicReference startTopicRef, final String contentTopicId) {
      TopicReference result = null;

      if (contentTopicId.equals(requireNonNull(startTopicRef.getContentTopic()).getId())) {
        result = startTopicRef;
      } else {
        for (final ParsedContent.TopicReference c : startTopicRef.getChildren()) {
          result = findForTopicId(c, contentTopicId);
          if (result != null) {
            break;
          }
        }
      }

      return result;
    }

    TopicReference getRootTopic() {
      return this.rootRef;
    }

    Map<String, String> getLinksBetweenTopics() {
      return this.linksBetweenTopics;
    }

    private static final class TopicReference {

      private final String id;
      private final ContentTopic linkedTopic;

      private final Color colorBorder;
      private final Color colorText;
      private final Color colorFill;

      private final List<TopicReference> children = new ArrayList<>();

      private TopicReference(final Element topicNode, final Map<String, ContentTopic> topicMap) {
        this.id = topicNode.getAttribute("id");
        this.linkedTopic = topicMap.get(topicNode.getAttribute("topic-ref"));

        final Element subTopics = Utils.findFirstElement(topicNode, "sub-topics");
        if (subTopics != null) {
          for (final Element t : Utils.findDirectChildrenForName(subTopics, "topic-node")) {
            this.children.add(new TopicReference(t, topicMap));
          }
        }

        Color tmpColorBackground = null;
        Color tmpColorText = null;
        Color tmpColorBorder = null;

        final Element topicNodeView = Utils.findFirstElement(topicNode, "topic-node-view");
        if (topicNodeView != null) {
          final Element style = Utils.findFirstElement(topicNodeView, "topic-node-style");
          if (style != null) {
            final Element fillStyle = Utils.findFirstElement(style, "fill-style");
            final Element lineStyle = Utils.findFirstElement(style, "line-style");

            if (fillStyle != null) {
              final Element solidColor = Utils.findFirstElement(fillStyle, "solid-color");
              if (solidColor != null) {
                tmpColorBackground = Utils.html2color(solidColor.getAttribute("color"), false);
                if (tmpColorBackground != null) {
                  tmpColorText = Utils.makeContrastColor(tmpColorBackground);
                }
              }
            }

            if (lineStyle != null) {
              tmpColorBorder = Utils.html2color(lineStyle.getAttribute("color"), false);
            }

          }
        }

        this.colorBorder = tmpColorBorder;
        this.colorText = tmpColorText;
        this.colorFill = tmpColorBackground;
      }

      Color getColorBorder() {
        return this.colorBorder;
      }

      Color getColorBackground() {
        return this.colorFill;
      }

      Color getColorText() {
        return this.colorText;
      }

      String getId() {
        return this.id;
      }

      ContentTopic getContentTopic() {
        return this.linkedTopic;
      }

      public List<TopicReference> getChildren() {
        return this.children;
      }
    }

    private static final class ContentTopic {

      private final String id;
      private final String richText;
      private final String notes;
      private final List<String> linkUrls;
      private final String imageResourceId;

      private ContentTopic(final String id, final Element nodeElement) {
        this.id = id;
        this.imageResourceId = extractImageId(nodeElement);
        this.notes = extractNotes(nodeElement);
        this.linkUrls = extractLinkUrls(nodeElement);
        this.richText = extractRichTextBlock(nodeElement);
      }

      private static String extractRichText(final Element richText) {
        final StringBuilder result = new StringBuilder();

        for (final Element r : Utils.findDirectChildrenForName(richText, "text-run")) {
          NodeList list = r.getChildNodes();
          for (int i = 0; i < list.getLength(); i++) {
            final Node n = list.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE) {
              if (n.getNodeName().equals("br")) {
                result.append('\n');
              } else {
                result.append(n.getTextContent());
              }
            } else if (n.getNodeType() == Node.TEXT_NODE) {
              result.append(n.getTextContent());
            }
          }
        }

        return result.toString();
      }

      private static String extractImageId(final Element node) {
        final Element imageElement = Utils.findFirstElement(node, "top-image");
        final String resourceRef =
            imageElement == null ? "" : imageElement.getAttribute("resource-ref");
        return resourceRef.isEmpty() ? null : resourceRef;
      }

      private static String extractNotes(final Element node) {
        final StringBuilder result = new StringBuilder();
        for (final Element e : Utils.findDirectChildrenForName(node, "notes")) {
          final String rtext = extractRichTextBlock(e);
          if (rtext != null) {
            result.append(rtext);
          }
        }
        return result.length() == 0 ? null : result.toString();
      }

      private static String extractRichTextBlock(final Element element) {
        final StringBuilder result = new StringBuilder();
        for (final Element rc : Utils.findDirectChildrenForName(element, "rich-text")) {
          result.append(extractRichText(rc));
        }
        return result.length() == 0 ? null : result.toString();
      }

      private static List<String> extractLinkUrls(final Element node) {
        final List<String> result = new ArrayList<>();
        for (final Element links : Utils.findDirectChildrenForName(node, "links")) {
          for (final Element l : Utils.findDirectChildrenForName(links, "link")) {
            final String url = l.getAttribute("url");
            if (!url.isEmpty()) {
              result.add(url);
            }
          }
        }
        return result;
      }

      String getId() {
        return this.id;
      }

      String getImageResourceId() {
        return this.imageResourceId;
      }

      String getNotes() {
        return this.notes;
      }

      String getRichText() {
        return this.richText;
      }

      List<String> getLinkUrls() {
        return this.linkUrls;
      }

    }

  }
}
