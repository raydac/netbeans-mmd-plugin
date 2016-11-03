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

import com.igormaznitsa.mindmap.plugins.api.AbstractImporter;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;
import org.apache.commons.io.FileUtils;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.MindMapController;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.Texts;
import com.igormaznitsa.mindmap.swing.services.IconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;
import java.util.ArrayList;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractCollapsableElement;

public class Text2MindMapImporter extends AbstractImporter {

  private static final Icon ICO = ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_IMPORT_TXT2MM);

  private static final int TAB_POSITIONS = 16;

  private static final class TopicData {

    private final int offset;
    private final Topic topic;

    public TopicData(final int offset, @Nonnull final Topic topic) {
      this.offset = offset;
      this.topic = topic;
    }

  }

  @Override
  @Nullable
  public MindMap doImport(@Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nullable final Topic actionTopic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) throws Exception {
    final File file = this.selectFileForExtension(panel, Texts.getString("MMDImporters.Text2MindMap.openDialogTitle"), "txt", "text files (.TXT)", Texts.getString("MMDImporters.ApproveImport"));
    MindMap result = null;
    if (file != null) {
      final List<String> lines = FileUtils.readLines(file);
      result = makeFromLines(lines, panel.getModel().getController());
    }
    return result;
  }

  @Nonnull
  MindMap makeFromLines(@Nonnull @MustNotContainNull final List<String> lines, @Nullable final MindMapController controller) {
    final MindMap result = new MindMap(controller, false);
    final Iterator<String> iterator = lines.iterator();
    final List<TopicData> topicStack = new ArrayList<TopicData>();
    while (true) {
      final Topic topic = decodeLine(result, iterator, topicStack);
      if (topic == null) {
        break;
      }
    }

    final Topic root = result.getRoot();

    final int size = root == null ? 0 : root.getChildren().size();
    if (root != null && size != 0) {
      final List<Topic> topics = root.getChildren();
      final int left = (topics.size() + 1) / 2;
      for (int i = 0; i < left; i++) {
        AbstractCollapsableElement.makeTopicLeftSided(topics.get(i), true);
      }
    }

    return result;
  }

  @Nullable
  private String nextNonEmptyString(@Nonnull final Iterator<String> iterator) {
    String result = null;
    while (iterator.hasNext()) {
      result = iterator.next();
      if (result.trim().isEmpty()) {
        result = null;
      } else {
        break;
      }
    }
    return result;
  }

  private int calcDataOffset(@Nonnull final String text) {
    int result = 0;
    for (final char c : text.toCharArray()) {
      if (c == '\t') {
        result += TAB_POSITIONS - (result % TAB_POSITIONS);
      } else if (Character.isWhitespace(c)) {
        result++;
      } else {
        break;
      }
    }
    return result;
  }

  @Nullable
  private Topic findPrevTopicForOffset(@Nonnull @MustNotContainNull final List<TopicData> topicStack, final int detectedOffset) {
    for (final TopicData d : topicStack) {
      if (d.offset < detectedOffset) {
        return d.topic;
      }
    }

    TopicData result = null;
    if (!topicStack.isEmpty()) {
      for (final TopicData d : topicStack) {
        if (result == null) {
          result = d;
        } else if (result.offset > d.offset) {
          result = d;
        }
      }
    }

    return result == null ? null : result.topic;
  }

  @Nullable
  private Topic decodeLine(@Nonnull final MindMap map, @Nonnull final Iterator<String> lines, @Nonnull @MustNotContainNull final List<TopicData> topicStack) {
    Topic result = null;
    final String line = nextNonEmptyString(lines);
    if (line != null) {
      final int currentOffset = calcDataOffset(line);
      final String trimmed = line.trim();
      final Topic parentTopic = findPrevTopicForOffset(topicStack, currentOffset);

      if (parentTopic == null) {
        result = new Topic(map, null, trimmed);
        map.setRoot(result, false);
      } else {
        result = new Topic(map, parentTopic, trimmed);
      }

      topicStack.add(0, new TopicData(currentOffset, result));
    }

    return result;
  }

  @Override
  @Nullable
  public String getMnemonic() {
    return "tabtext";
  }
  
  @Override
  @Nonnull
  public String getName(@Nonnull final MindMapPanel panel, @Nullable final Topic actionTopic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
    return Texts.getString("MMDImporters.Text2MindMap.Name");
  }

  @Override
  @Nonnull
  public String getReference(@Nonnull final MindMapPanel panel, @Nullable final Topic actionTopic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
    return Texts.getString("MMDImporters.Text2MindMap.Reference");
  }

  @Override
  @Nonnull
  public Icon getIcon(@Nonnull final MindMapPanel panel, @Nullable final Topic actionTopic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
    return ICO;
  }

  @Override
  public int getOrder() {
    return 1;
  }
}
