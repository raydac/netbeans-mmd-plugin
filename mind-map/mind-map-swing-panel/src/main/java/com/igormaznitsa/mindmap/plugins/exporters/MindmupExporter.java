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

package com.igormaznitsa.mindmap.plugins.exporters;

import static com.igormaznitsa.mindmap.plugins.attributes.images.ImageVisualAttributePlugin.MMD_TOPIC_ATTRIBUTE_IMAGE_DATA;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.MiscUtils;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.api.AbstractExporter;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.plugins.api.parameters.AbstractParameter;
import com.igormaznitsa.mindmap.swing.i18n.MmdI18n;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractCollapsableElement;
import com.igormaznitsa.mindmap.swing.panel.utils.MindMapUtils;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.mindmap.swing.services.IconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.apache.commons.io.IOUtils;
import org.json.JSONStringer;

public class MindmupExporter extends AbstractExporter {

  private static final Icon ICO =
      ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_EXPORT_MINDMUP);
  private static final Logger LOGGER = LoggerFactory.getLogger(MindmupExporter.class);

  private static String makeHtmlFromExtras(final ExtraLink link, final ExtraFile file) {
    final StringBuilder result = new StringBuilder();

    if (file != null) {
      final String uri = file.getValue().asString(true, false);
      result.append("FILE: <a href=\"").append(uri).append("\">").append(uri)
          .append("</a><br>");
    }
    if (link != null) {
      final String uri = link.getValue().asString(true, true);
      result.append("LINK: <a href=\"").append(uri).append("\">").append(uri)
          .append("</a><br>");
    }
    return result.toString();
  }

  @Override
  public String getMnemonic() {
    return "mindmup";
  }

  private void writeTopic(
      final JSONStringer stringer,
      final MindMapPanelConfig cfg,
      final AtomicInteger idCounter,
      final Topic topic,
      final Map<String, String> linkMap,
      final Map<String, TopicId> uuidMap
  ) {
    stringer.key("title").value(MiscUtils.ensureNotNull(topic.getText(), ""));
    final int topicId = idCounter.getAndIncrement();
    stringer.key("id").value(topicId);

    final String uuid =
        MiscUtils.ensureNotNull(topic.getAttribute(ExtraTopic.TOPIC_UID_ATTR),
            "genlink_" + topicId);
    uuidMap.put(uuid, new TopicId(topicId, uuid, topic));

    final ExtraNote note = (ExtraNote) this.findExtra(topic, Extra.ExtraType.NOTE);
    final ExtraTopic jump = (ExtraTopic) this.findExtra(topic, Extra.ExtraType.TOPIC);
    final ExtraLink link = (ExtraLink) this.findExtra(topic, Extra.ExtraType.LINK);
    final ExtraFile file = (ExtraFile) this.findExtra(topic, Extra.ExtraType.FILE);

    final String encodedImage = topic.getAttribute(MMD_TOPIC_ATTRIBUTE_IMAGE_DATA);

    if (jump != null) {
      linkMap.put(uuid, jump.getValue());
    }

    stringer.key("attr").object();

    stringer.key("style").object();
    stringer.key("background")
        .value(Utils.color2html(MindMapUtils.getBackgroundColor(cfg, topic), false));
    stringer.endObject();

    if (note != null) {
      stringer.key("note").object();
      stringer.key("index").value(3);
      stringer.key("text").value(note.getValue());
      stringer.endObject();
    }

    if (encodedImage != null) {
      BufferedImage renderedImage;
      try {
        renderedImage = ImageIO.read(new ByteArrayInputStream(Utils.base64decode(encodedImage)));
      } catch (Exception ex) {
        LOGGER.error("Can't render image for topic:" + topic);
        renderedImage = null;
      }

      stringer.key("icon").object();
      stringer.key("url").value("data:image/png;base64," + encodedImage);
      stringer.key("position").value("left");

      if (renderedImage != null) {
        stringer.key("width").value(renderedImage.getWidth());
        stringer.key("height").value(renderedImage.getHeight());
      }

      stringer.endObject();
    }

    stringer.endObject();

    if (link != null || file != null) {
      stringer.key("attachment").object();
      stringer.key("contentType").value("text/html");
      stringer.key("content").value(makeHtmlFromExtras(link, file));
      stringer.endObject();
    }

    stringer.key("ideas").object();
    int childIdCounter = 1;
    for (final Topic child : topic.getChildren()) {
      final boolean left = AbstractCollapsableElement.isLeftSidedTopic(child);
      stringer.key(Integer.toString(left ? -childIdCounter : childIdCounter)).object();
      childIdCounter++;
      writeTopic(stringer, cfg, idCounter, child, linkMap, uuidMap);
      stringer.endObject();
    }
    stringer.endObject();
  }

  private void writeRoot(final JSONStringer stringer, final MindMapPanelConfig cfg,
                         final Topic root) {
    stringer.object();

    stringer.key("formatVersion").value(3L);
    stringer.key("id").value("root");
    stringer.key("ideas").object();

    final Map<String, String> linkMap = new HashMap<>();
    final Map<String, TopicId> uuidTopicMap = new HashMap<>();

    if (root != null) {
      stringer.key("1").object();
      writeTopic(stringer, cfg, new AtomicInteger(1), root, linkMap, uuidTopicMap);
      stringer.endObject();
      stringer.key("title").value(Objects.requireNonNull(root.getText(), "[Root]"));
    } else {
      stringer.key("title").value("Empty map");
    }

    stringer.endObject();

    if (!linkMap.isEmpty()) {
      stringer.key("links").array();

      for (final Map.Entry<String, String> entry : linkMap.entrySet()) {
        final TopicId from = uuidTopicMap.get(entry.getKey());
        final TopicId to = uuidTopicMap.get(entry.getValue());

        if (from != null && to != null) {
          stringer.object();

          stringer.key("ideaIdFrom").value(from.id);
          stringer.key("ideaIdTo").value(to.id);

          stringer.key("attr").object();
          stringer.key("style").object();

          stringer.key("arrow").value("to");
          stringer.key("color").value(Utils.color2html(cfg.getJumpLinkColor(), false));
          stringer.key("lineStyle").value("dashed");

          stringer.endObject();
          stringer.endObject();

          stringer.endObject();
        }
      }

      stringer.endArray();
    }

    stringer.endObject();
  }

  private String makeContent(final MindMapPanel panel) {
    final JSONStringer stringer = new JSONStringer();
    writeRoot(stringer, panel.getConfiguration(), panel.getModel().getRoot());
    return stringer.toString();
  }

  @Override
  public void doExportToClipboard(final PluginContext context, final Set<AbstractParameter<?>> options)
      throws IOException {
    final String text = makeContent(context.getPanel());
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        if (clipboard != null) {
          clipboard.setContents(new StringSelection(text), null);
        }
      }
    });
  }

  @Override
  public void doExport(final PluginContext context, final Set<AbstractParameter<?>> options,
                       final OutputStream out) throws IOException {
    final String text = makeContent(context.getPanel());

    File fileToSaveMap = null;
    OutputStream theOut = out;
    if (theOut == null) {
      fileToSaveMap = MindMapUtils.selectFileToSaveForFileFilter(
          context.getPanel(),
          context,
          this.getClass().getName(),
          MmdI18n.getInstance().findBundle().getString("MindmupExporter.saveDialogTitle"),
          null,
          ".mup",
          MmdI18n.getInstance().findBundle().getString("MindmupExporter.filterDescription"),
          MmdI18n.getInstance().findBundle().getString("MindmupExporter.approveButtonText"));
      fileToSaveMap =
          MindMapUtils.checkFileAndExtension(context.getPanel(), fileToSaveMap, ".mup");
      theOut = fileToSaveMap == null ? null :
          new BufferedOutputStream(new FileOutputStream(fileToSaveMap, false));
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

  @Override
  public String getName(final PluginContext context, final Topic actionTopic) {
    return MmdI18n.getInstance().findBundle().getString("MindmupExporter.exporterName");
  }

  @Override
  public String getReference(final PluginContext context, final Topic actionTopic) {
    return MmdI18n.getInstance().findBundle().getString("MindmupExporter.exporterReference");
  }

  @Override
  public Icon getIcon(final PluginContext context, Topic actionTopic) {
    return ICO;
  }

  @Override
  public int getOrder() {
    return 2;
  }

  private static final class TopicId {
    private final int id;
    private final Topic topic;
    private final String uuid;

    private TopicId(final int id, final String uuid, final Topic topic) {
      this.id = id;
      this.topic = topic;
      this.uuid = uuid;
    }
  }

}
