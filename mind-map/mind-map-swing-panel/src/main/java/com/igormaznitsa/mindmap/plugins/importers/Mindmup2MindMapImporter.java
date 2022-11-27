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

package com.igormaznitsa.mindmap.plugins.importers;

import static com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute.ATTR_FILL_COLOR;
import static com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute.ATTR_TEXT_COLOR;
import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.StandardMmdAttributes;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.api.AbstractImporter;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.plugins.attributes.images.ImageVisualAttributePlugin;
import com.igormaznitsa.mindmap.swing.i18n.MmdI18n;
import com.igormaznitsa.mindmap.swing.ide.IDEBridgeFactory;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractCollapsableElement;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.mindmap.swing.services.IconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class Mindmup2MindMapImporter extends AbstractImporter {

  private static final Icon ICO = ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_EXPORT_MINDMUP);

  private static final Logger LOGGER = LoggerFactory.getLogger(Mindmup2MindMapImporter.class);

  @Override
  public MindMap doImport(final PluginContext context) throws Exception {
    final File file = this.selectFileForExtension(context,
        MmdI18n.getInstance().findBundle().getString("MMDImporters.Mindmup2MindMap.openDialogTitle"), null, "mup",
        "Mindmup files (.MUP)", MmdI18n.getInstance().findBundle().getString("MMDImporters.ApproveImport"));

    if (file == null) {
      return null;
    }

    try {
      return this.doImportFile(file);
    } catch (final IllegalArgumentException ex) {
      LOGGER.error("Can't parse", ex);
      context.getDialogProvider().msgError(null, MmdI18n.getInstance().findBundle().getString("MMDImporters.Mindmup2MindMap.Error.WrongFormat"));
      return null;
    }
  }

  MindMap doImportFile(final File file) throws IOException {
    final JSONObject parsedJson = new JSONObject(FileUtils.readFileToString(file, "UTF-8"));
    final Number formatVersion = parsedJson.getNumber("formatVersion");
    if (formatVersion == null) {
      throw new IllegalArgumentException("Can't find formatVersion");
    }
    final MindMap resultedMap = new MindMap(true);
    resultedMap.putAttribute(StandardMmdAttributes.MMD_ATTRIBUTE_GENERATOR_ID, IDEBridgeFactory.findInstance()
        .getIDEGeneratorId());
    resultedMap.putAttribute(StandardMmdAttributes.MMD_ATTRIBUTE_SHOW_JUMPS, "true");

    final Topic mindMapRoot = requireNonNull(resultedMap.getRoot());
    final Map<Long, Topic> mapTopicId = new HashMap<>();

    parseTopic(resultedMap, null, mindMapRoot, parsedJson, mapTopicId);

    if (!mindMapRoot.getExtras().containsKey(Extra.ExtraType.FILE)) {
      mindMapRoot.setExtra(new ExtraFile(new MMapURI(null, file, null)));
    }

    if (parsedJson.has("links")) {
      final JSONArray links = parsedJson.getJSONArray("links");
      processLinks(resultedMap, links, mapTopicId);
    }

    return resultedMap;
  }

  private void processLinks(final MindMap map, final JSONArray links,
                            final Map<Long, Topic> topics) {
    for (int i = 0; i < links.length(); i++) {
      try {
        final JSONObject linkObject = links.getJSONObject(i);

        final Topic fromTopic = topics.get(linkObject.optLong("ideaIdFrom", Long.MIN_VALUE));
        final Topic toTopic = topics.get(linkObject.optLong("ideaIdTo", Long.MIN_VALUE));

        if (fromTopic != null && toTopic != null) {
          fromTopic.setExtra(ExtraTopic.makeLinkTo(map, toTopic));
        }
      } catch (final Exception ex) {
        LOGGER.error("Can't parse link", ex);
      }
    }
  }

  private void parseTopic(
      final MindMap map,
      final Topic parentTopic,
      final Topic pregeneratedTopic,
      final JSONObject json,
      final Map<Long, Topic> idTopicMap
  ) {
    final JSONObject ideas = json.optJSONObject("ideas");
    if (ideas != null) {

      final List<OrderableIdea> sortedIdeas = new ArrayList<>();
      for (final String key : ideas.keySet()) {
        final JSONObject idea = ideas.optJSONObject(key);
        if (idea == null) {
          continue;
        }
        double order = 0.0d;
        try {
          order = Double.parseDouble(key.trim());
        } catch (final NumberFormatException ex) {
          LOGGER.error("Detected unexpected number format in order", ex);
        }
        sortedIdeas.add(new OrderableIdea(order, idea));
      }
      Collections.sort(sortedIdeas);

      for (final OrderableIdea i : sortedIdeas) {
        final JSONObject ideaObject = i.getIdea();

        final String title = ideaObject.optString("title", "");
        final long id = ideaObject.optLong("id", Long.MIN_VALUE);

        final Topic topicToProcess;

        if (pregeneratedTopic == null) {
          topicToProcess = requireNonNull(parentTopic).makeChild(title.trim(), parentTopic);
          if (requireNonNull(topicToProcess.getParent()).isRoot()) {
            if (i.isLeftBranch()) {
              AbstractCollapsableElement.makeTopicLeftSided(topicToProcess, true);
              final Topic firstSibling = requireNonNull(parentTopic).getFirst();
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

        final JSONObject attributes = ideaObject.optJSONObject("attr");

        if (attributes != null) {
          for (final String key : attributes.keySet()) {
            final JSONObject attrJson = attributes.optJSONObject(key);
            if (attrJson != null) {
              if ("note".equals(key)) {
                processAttrNote(attrJson, topicToProcess);
              } else if ("icon".equals(key)) {
                processAttrIcon(attrJson, topicToProcess);
              } else if ("style".equals(key)) {
                processAttrStyle(attrJson, topicToProcess);
              } else {
                LOGGER.warn("Detected unsupported attribute '" + key + '\'');
              }
            }
          }
        }

        if (id >= 0L) {
          idTopicMap.put(id, topicToProcess);
        }

        parseTopic(map, topicToProcess, null, ideaObject, idTopicMap);

        if (parentTopic == null && pregeneratedTopic != null) {
          // process only root
          break;
        }
      }
    }
  }

  private void processAttrNote(final JSONObject note, final Topic topic) {
    topic.setExtra(new ExtraNote(note.optString("text", "")));
  }

  private void processAttrIcon(final JSONObject icon, final Topic topic) {
    final String iconUrl = icon.getString("url");
    if (iconUrl.startsWith("data:")) {
      final String[] data = iconUrl.split("\\,");
      if (data.length == 2 && data[0].startsWith("data:image/") && data[0].endsWith("base64")) {
        try {
          final String encoded = Utils.rescaleImageAndEncodeAsBase64(
              new ByteArrayInputStream(Utils.base64decode(data[1].trim())), -1);
          if (encoded == null) {
            LOGGER.warn("Can't convert image : " + iconUrl);
          } else {
            topic.putAttribute(ImageVisualAttributePlugin.MMD_TOPIC_ATTRIBUTE_IMAGE_DATA, encoded);
          }
        } catch (final Exception ex) {
          LOGGER.error("Can't load image : " + iconUrl, ex);
        }
      }
    } else {
      try {
        topic.setExtra(new ExtraLink(iconUrl));
      } catch (final URISyntaxException ex) {
        LOGGER.error("Can't parse URI : " + iconUrl);
      }
    }
  }

  private void processAttrStyle(final JSONObject style, final Topic topic) {
    final String background = style.getString("background");
    if (background != null) {
      final Color color = Utils.html2color(background, false);
      if (color != null) {
        topic.putAttribute(ATTR_FILL_COLOR.getText(), Utils.color2html(color, false));
        topic.putAttribute(ATTR_TEXT_COLOR.getText(),
            Utils.color2html(Utils.makeContrastColor(color), false));
      }
    }
  }

  @Override
  public String getMnemonic() {
    return "mindmup";
  }

  @Override
  public String getName(final PluginContext context) {
    return MmdI18n.getInstance().findBundle().getString("MMDImporters.Mindmup2MindMap.Name");
  }

  @Override
  public String getReference(final PluginContext context) {
    return MmdI18n.getInstance().findBundle().getString("MMDImporters.Mindmup2MindMap.Reference");
  }

  @Override
  public Icon getIcon(final PluginContext context) {
    return ICO;
  }

  @Override
  public int getOrder() {
    return 2;
  }

  @Override
  public boolean isCompatibleWithFullScreenMode() {
    return false;
  }

  private static final class OrderableIdea implements Comparable<OrderableIdea> {

    private final double order;
    private final JSONObject idea;

    private OrderableIdea(final double order, final JSONObject idea) {
      this.order = order;
      this.idea = idea;
    }

    private boolean isLeftBranch() {
      return this.order < 0.0d;
    }

    private JSONObject getIdea() {
      return this.idea;
    }

    @Override
    public int compareTo(final OrderableIdea that) {
      return Double.compare(this.order, that.order);
    }

  }
}
