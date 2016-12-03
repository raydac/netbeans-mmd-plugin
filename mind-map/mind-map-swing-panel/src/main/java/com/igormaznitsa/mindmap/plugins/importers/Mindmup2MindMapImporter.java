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

import static com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute.ATTR_FILL_COLOR;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import com.igormaznitsa.mindmap.plugins.api.AbstractImporter;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.io.FileUtils;
import com.grack.nanojson.JsonArray;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.Texts;
import com.igormaznitsa.mindmap.swing.services.IconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.attributes.images.ImageVisualAttributePlugin;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractCollapsableElement;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;

public class Mindmup2MindMapImporter extends AbstractImporter {

  private static final Icon ICO = ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_EXPORT_MINDMUP);

  private static final Logger LOGGER = LoggerFactory.getLogger(Mindmup2MindMapImporter.class);

  private static final class OrderableIdea implements Comparable<OrderableIdea> {

    private final double order;
    private final JsonObject idea;

    private OrderableIdea(final double order, @Nonnull final JsonObject idea) {
      this.order = order;
      this.idea = idea;
    }

    private boolean isLeftBranch() {
      return this.order < 0.0d;
    }

    @Nonnull
    private JsonObject getIdea() {
      return this.idea;
    }

    @Override
    public int compareTo(@Nonnull final OrderableIdea that) {
      return Double.compare(this.order, that.order);
    }

  }

  @Override
  @Nullable
  public MindMap doImport(@Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nullable final Topic actionTopic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) throws Exception {
    final File file = this.selectFileForExtension(panel, Texts.getString("MMDImporters.Mindmup2MindMap.openDialogTitle"), "mup", "Mindmup files (.MUP)", Texts.getString("MMDImporters.ApproveImport"));

    final JsonObject parsedJson;
    try {
      parsedJson = JsonParser.object().from(FileUtils.readFileToString(file, "UTF-8"));
    }
    catch (JsonParserException ex) {
      dialogProvider.msgError(Texts.getString("MMDImporters.Mindmup2MindMap.Error.WrongFormat"));
      return null;
    }

    MindMap resultedMap = null;

    final Number formatVersion = parsedJson.getNumber("formatVersion");
    if (formatVersion == null) {
      dialogProvider.msgError(Texts.getString("MMDImporters.Mindmup2MindMap.Error.WrongFormat"));
    } else {
      if (formatVersion.intValue() < 2) {
        dialogProvider.msgError(Texts.getString("MMDImporters.Mindmup2MindMap.Error.UnsupportedVersion"));
      } else {
        resultedMap = new MindMap(null, true);
        resultedMap.setAttribute(MindMapPanel.ATTR_SHOW_JUMPS, "true");

        final Topic mindMapRoot = Assertions.assertNotNull(resultedMap.getRoot());
        final Map<Long, Topic> mapTopicId = new HashMap<Long, Topic>();

        parseTopic(resultedMap, null, mindMapRoot, parsedJson, mapTopicId);

        if (!mindMapRoot.getExtras().containsKey(Extra.ExtraType.FILE)) {
          mindMapRoot.setExtra(new ExtraFile(new MMapURI(null, file, null)));
        }

        final JsonArray links = parsedJson.getArray("links");
        if (links != null) {
          processLinks(resultedMap, links, mapTopicId);
        }
      }
    }
    return resultedMap;
  }

  private void processLinks(@Nonnull final MindMap map, @Nonnull final JsonArray links, @Nonnull final Map<Long, Topic> topics) {
    for (int i = 0; i < links.size(); i++) {
      try {
        final JsonObject linkObject = links.getObject(i);

        final Topic fromTopic = topics.get(linkObject.getNumber("ideaIdFrom", Long.MIN_VALUE).longValue());
        final Topic toTopic = topics.get(linkObject.getNumber("ideaIdTo", Long.MIN_VALUE).longValue());

        if (fromTopic != null && toTopic != null) {
          fromTopic.setExtra(ExtraTopic.makeLinkTo(map, toTopic));
        }
      }
      catch (final Exception ex) {
        LOGGER.error("Can't parse link", ex);
      }
    }
  }

  private void parseTopic(@Nonnull MindMap map, @Nullable final Topic parentTopic, @Nullable final Topic pregeneratedTopic, @Nonnull final JsonObject json, @Nonnull final Map<Long, Topic> idTopicMap) {
    final JsonObject ideas = json.getObject("ideas");
    if (ideas != null) {

      final List<OrderableIdea> sortedIdeas = new ArrayList<OrderableIdea>();
      for (final Map.Entry<String, Object> i : ideas.entrySet()) {
        if (i.getValue() instanceof JsonObject) {
          final JsonObject idea = (JsonObject) i.getValue();
          double order = 0.0d;
          try {
            order = Double.parseDouble(i.getKey().trim());
          }
          catch (final NumberFormatException ex) {
            LOGGER.error("Detected unexpected number format in order", ex);
          }
          sortedIdeas.add(new OrderableIdea(order, idea));
        }
      }
      Collections.sort(sortedIdeas);

      for (final OrderableIdea i : sortedIdeas) {
        final JsonObject ideaObject = i.getIdea();

        final String title = ideaObject.getString("title", "");
        final long id = ideaObject.getNumber("id", Long.MIN_VALUE).longValue();

        final Topic topicToProcess;

        if (pregeneratedTopic == null) {
          topicToProcess = Assertions.assertNotNull(parentTopic).makeChild(title.trim(), parentTopic);
          if (Assertions.assertNotNull(topicToProcess.getParent()).isRoot()) {
            if (i.isLeftBranch()) {
              AbstractCollapsableElement.makeTopicLeftSided(topicToProcess, true);
              final Topic firstSibling = parentTopic.getFirst();
              if (firstSibling != null && firstSibling != topicToProcess) {
                topicToProcess.moveBefore(firstSibling);
              }
            }
          }
        } else {
          topicToProcess = pregeneratedTopic;
          topicToProcess.setText(title.trim());
        }

        if (id != Long.MIN_VALUE) {
          idTopicMap.put(id, topicToProcess);
        }

        final JsonObject attributes = ideaObject.getObject("attr");

        if (attributes != null) {
          for (final Map.Entry<String, Object> a : attributes.entrySet()) {
            final String name = a.getKey();
            final Object attrJson = a.getValue();
            if ("note".equals(name)) {
              processAttrNote((JsonObject) attrJson, topicToProcess);
            } else if ("icon".equals(name)) {
              processAttrIcon((JsonObject) attrJson, topicToProcess);
            } else if ("style".equals(name)) {
              processAttrStyle((JsonObject) attrJson, topicToProcess);
            } else {
              LOGGER.warn("Detected unsupported attribute '" + name + '\'');
            }
          }
        }

        if (id >= 0L) {
          idTopicMap.put(id, topicToProcess);
        }

        parseTopic(map, topicToProcess, null, ideaObject, idTopicMap);
      }
    }
  }

  private void processAttrNote(@Nonnull final JsonObject note, @Nonnull final Topic topic) {
    topic.setExtra(new ExtraNote(note.getString("text", "")));
  }

  private void processAttrIcon(@Nonnull final JsonObject icon, @Nonnull final Topic topic) {
    final String iconUrl = icon.getString("url");
    if (iconUrl.startsWith("data:")) {
      final String[] data = iconUrl.split("\\,");
      if (data.length == 2 && data[0].startsWith("data:image/") && data[0].endsWith("base64")) {
        try {
          final Image image = ImageIO.read(new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(data[1].trim())));
          if (image != null) {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write((RenderedImage) image, "png", bos); //NOI18N
            bos.close();
            final String encoded = Utils.base64encode(bos.toByteArray());
            topic.setAttribute(ImageVisualAttributePlugin.ATTR_KEY, encoded);
          }
        }
        catch (final Exception ex) {
          LOGGER.error("Can't decode image", ex);
        }
      }
    } else {
      try {
        topic.setExtra(new ExtraLink(iconUrl));
      }
      catch (final URISyntaxException ex) {
        LOGGER.error("Can't parse URI : " + iconUrl);
      }
    }
  }

  private void processAttrStyle(@Nonnull final JsonObject style, @Nonnull final Topic topic) {
    final String background = style.getString("background");
    if (background != null) {
      final Color color = Utils.html2color(background, false);
      if (color != null) {
        topic.setAttribute(ATTR_FILL_COLOR.getText(), Utils.color2html(color, false));
      }
    }
  }

  @Override
  @Nullable
  public String getMnemonic() {
    return "mindmup";
  }

  @Override
  @Nonnull
  public String getName(@Nonnull final MindMapPanel panel, @Nullable final Topic actionTopic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
    return Texts.getString("MMDImporters.Mindmup2MindMap.Name");
  }

  @Override
  @Nonnull
  public String getReference(@Nonnull final MindMapPanel panel, @Nullable final Topic actionTopic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
    return Texts.getString("MMDImporters.Mindmup2MindMap.Reference");
  }

  @Override
  @Nonnull
  public Icon getIcon(@Nonnull final MindMapPanel panel, @Nullable final Topic actionTopic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
    return ICO;
  }

  @Override
  public int getOrder() {
    return 2;
  }
}
