/*
 * Copyright 2015 Igor Maznitsa.
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

import com.grack.nanojson.JsonStringWriter;
import com.grack.nanojson.JsonWriter;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractCollapsableElement;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.swing.panel.Texts;
import com.igormaznitsa.mindmap.swing.panel.utils.MindMapUtils;
import com.igormaznitsa.mindmap.swing.services.ImageIconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;

public class MindmupExporter extends AbstractExportingPlugin {

  private static int idCounter = 1;

  private static class TopicData {

    private final int uid;
    private final int id;
    private final Topic topic;

    public TopicData (final int uid, final int id, @Nonnull final Topic topic) {
      this.uid = uid;
      this.id = id;
      this.topic = topic;
    }

    public int getUID () {
      return this.uid;
    }

    public int getID () {
      return id;
    }

    @Nonnull
    public Topic getTopic () {
      return this.topic;
    }
  }

  private static class State {

    private JsonStringWriter json = JsonWriter.string();
    private final Map<String, TopicData> topicsWithId = new HashMap<String, TopicData>();
    private final List<TopicData> topicsContainsJump = new ArrayList<TopicData>();

    public State () {
    }

    public void processTopic (final int uid, final int id, @Nonnull final Topic topic) {
      final String topicUID = getTopicUid(topic);
      if (topicUID != null) {
        topicsWithId.put(topicUID, new TopicData(uid, id, topic));
      }

      final ExtraTopic linkto = (ExtraTopic) topic.getExtras().get(Extra.ExtraType.TOPIC);
      if (linkto != null) {
        this.topicsContainsJump.add(new TopicData(uid, id, topic));
      }
    }

    @Nonnull
    @MustNotContainNull
    public List<TopicData> getTopicsContainingJump () {
      return this.topicsContainsJump;
    }

    @Nullable
    public TopicData findTopic (@Nonnull final ExtraTopic link) {
      return topicsWithId.get(link.getValue());
    }

    @Nonnull
    public State startObj (@Nonnull final String key) {
      this.json = this.json.object(key);
      return this;
    }

    @Nonnull
    public State startObj () {
      this.json = this.json.object();
      return this;
    }

    @Nonnull
    public State startArray (@Nonnull final String key) {
      this.json = this.json.array(key);
      return this;
    }

    @Nonnull
    public State set (@Nonnull final String key, @Nonnull final String value) {
      this.json.value(key, value);
      return this;
    }

    @Nonnull
    public State set (@Nonnull final String key, final int value) {
      this.json.value(key, value);
      return this;
    }

    @Nonnull
    public State end () {
      this.json = json.end();
      return this;
    }

    @Override
    @Nonnull
    public String toString () {
      return json.done();
    }
  }

  private static final ImageIcon ICO = ImageIconServiceProvider.findInstance().getIconForId(ImageIconID.POPUP_EXPORT_MINDMUP);
  
  @Nullable
  private static String getTopicUid (@Nonnull final Topic topic) {
    return topic.getAttribute(ExtraTopic.TOPIC_UID_ATTR);
  }

  private int writeTopic (@Nonnull final State state, int id, @Nonnull final MindMapPanelConfig cfg, @Nonnull final Topic topic) {
    state.startObj(Integer.toString(idCounter));

    state.processTopic(idCounter, id, topic);

    idCounter++;

    state.set("title", topic.getText()); //NOI18N
    state.set("id", id); //NOI18N

    id = Math.abs(id);

    state.startObj("ideas"); //NOI18N
    for (final Topic t : topic.getChildren()) {
      id = writeTopic(state, id + 1, cfg, t);
    }
    state.end();

    state.startObj("attr"); //NOI18N
    state.startObj("style").set("background", Utils.color2html(MindMapUtils.getBackgroundColor(cfg, topic), false)).set("color", Utils.color2html(MindMapUtils.getTextColor(cfg, topic), false)).end(); //NOI18N

    final String attachment = makeHtmlFromExtras(topic);
    if (attachment != null) {
      state.startObj("attachment"); //NOI18N
      state.set("contentType", "text/html"); //NOI18N
      state.set("content", attachment); //NOI18N
      state.end();
    }

    state.end();
    state.end();

    return id;
  }

  @Nullable
  private static String makeHtmlFromExtras (@Nonnull final Topic topic) {
    final ExtraFile file = (ExtraFile) topic.getExtras().get(Extra.ExtraType.FILE);
    final ExtraNote note = (ExtraNote) topic.getExtras().get(Extra.ExtraType.NOTE);
    final ExtraLink link = (ExtraLink) topic.getExtras().get(Extra.ExtraType.LINK);

    if (file == null && link == null && note == null) {
      return null;
    }

    final StringBuilder result = new StringBuilder();

    if (file != null) {
      final String uri = file.getValue().asString(true, false);
      result.append("FILE: <a href=\"").append(uri).append("\">").append(uri).append("</a><br>"); //NOI18N
    }
    if (link != null) {
      final String uri = link.getValue().asString(true, true);
      result.append("LINK: <a href=\"").append(uri).append("\">").append(uri).append("</a><br>"); //NOI18N
    }
    if (note != null) {
      if (file != null || link != null) {
        result.append("<br>"); //NOI18N
      }
      result.append("<pre>").append(StringEscapeUtils.escapeHtml(note.getValue())).append("</pre>"); //NOI18N
    }
    return result.toString();
  }

  private void writeRoot (@Nonnull final State state, @Nonnull final MindMapPanelConfig cfg, @Nullable final Topic root) {
    state.startObj();

    if (root == null) {
      state.set("title", ""); //NOI18N
    }
    else {
      state.set("title", root.getText()); //NOI18N
    }
    state.set("id", 1); //NOI18N
    state.set("formatVersion", 2); //NOI18N

    final List<Topic> leftChildren = new ArrayList<Topic>();
    final List<Topic> rightChildren = new ArrayList<Topic>();

    if (root != null) {
      for (final Topic t : root.getChildren()) {
        if (AbstractCollapsableElement.isLeftSidedTopic(t)) {
          leftChildren.add(t);
        }
        else {
          rightChildren.add(t);
        }
      }
    }
    state.startObj("ideas"); //NOI18N

    if (root != null) {
      state.processTopic(0, 1, root);
    }

    int id = 2;
    for (final Topic right : rightChildren) {
      id = writeTopic(state, id + 1, cfg, right);
    }

    for (final Topic left : leftChildren) {
      id = writeTopic(state, -(id + 1), cfg, left);
    }

    state.end();

    if (root != null) {
    state.startObj("attr"); //NOI18N
    state.startObj("style")
        .set("background", Utils.color2html(MindMapUtils.getBackgroundColor(cfg, root), false))//NOI18N
        .set("color", Utils.color2html(MindMapUtils.getTextColor(cfg, root), false))//NOI18N
        .end(); //NOI18N
    }

    final String attachment = root == null ? null : makeHtmlFromExtras(root);
    if (attachment != null) {
      state.startObj("attachment"); //NOI18N
      state.set("contentType", "text/html"); //NOI18N
      state.set("content", attachment); //NOI18N
      state.end();
    }
    state.end();

    final List<TopicData> topicsWithJumps = state.getTopicsContainingJump();
    if (!topicsWithJumps.isEmpty()) {
      state.startArray("links"); //NOI18N
      for (final TopicData src : topicsWithJumps) {
        final TopicData dest = state.findTopic((ExtraTopic) src.getTopic().getExtras().get(Extra.ExtraType.TOPIC));
        if (dest != null) {
          state.startObj(); //NOI18N
          state.set("ideaIdFrom", src.getID()); //NOI18N
          state.set("ideaIdTo", dest.getID()); //NOI18N
          state.startObj("attr")
              .startObj("style")
              .set("color", "#FF0000")
              .set("lineStyle", "dashed")
              .end()
              .end(); //NOI18N
          state.end();
        }
      }
      state.end();
    }
    
    if (root!=null) state.end();
  }

  @Override
  public void doExport (@Nonnull final MindMapPanel panel, @Nullable final JComponent options, @Nullable final OutputStream out) throws IOException {
    final State state = new State();
    
    writeRoot(state, panel.getConfiguration(), panel.getModel().getRoot());

    final String text = state.toString();

    File fileToSaveMap = null;
    OutputStream theOut = out;
    if (theOut == null) {
      fileToSaveMap = MindMapUtils.selectFileForFileFilter(panel, Texts.getString("MindmupExporter.saveDialogTitle"), ".mup", Texts.getString("MindmupExporter.filterDescription"), Texts.getString("MindmupExporter.approveButtonText"));
      fileToSaveMap = MindMapUtils.checkFileAndExtension(panel, fileToSaveMap, ".mup");//NOI18N
      theOut = fileToSaveMap == null ? null : new BufferedOutputStream(new FileOutputStream(fileToSaveMap, false));
    }
    if (theOut != null) {
      try {
        IOUtils.write(text, theOut, "UTF-8");
      }
      finally {
        if (fileToSaveMap != null) {
          IOUtils.closeQuietly(theOut);
        }
      }
    }
  }

  @Override
  @Nonnull
  public String getName (@Nonnull final MindMapPanel panel, @Nullable Topic actionTopic, @Nonnull @MustNotContainNull Topic[] selectedTopics) {
    return Texts.getString("MindmupExporter.exporterName");
  }

  @Override
  @Nonnull
  public String getReference (@Nonnull final MindMapPanel panel, @Nullable Topic actionTopic, @Nonnull @MustNotContainNull Topic[] selectedTopics) {
    return Texts.getString("MindmupExporter.exporterReference");
  }

  @Override
  @Nonnull
  public ImageIcon getIcon (@Nonnull final MindMapPanel panel, @Nullable Topic actionTopic, @Nonnull @MustNotContainNull Topic[] selectedTopics) {
    return ICO;
  }

  @Override
  public int getOrder() {
    return 3;
  }

}
