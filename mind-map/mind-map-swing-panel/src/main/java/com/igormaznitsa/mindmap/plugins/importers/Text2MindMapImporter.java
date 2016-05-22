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
import static com.igormaznitsa.meta.common.utils.Assertions.assertNotNull;
import static com.igormaznitsa.meta.common.utils.Assertions.assertNotNull;

public class Text2MindMapImporter extends AbstractImporter {

  private static final Icon ICO = ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_IMPORT_TXT2MM);

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
    Topic topic = decodeLine(result, null, iterator);
    if (topic != null) {
      while (true) {
        topic = decodeLine(result, topic, iterator);
        if (topic == null) {
          break;
        }
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

  private int calcLevel(@Nonnull final String text) {
    int result = 0;
    for (final char c : text.toCharArray()) {
      if (c == '\t') {
        result++;
      } else if (!Character.isWhitespace(c)) {
        break;
      }
    }
    return result;
  }

  @Nonnull
  private Topic findParentForLevel(@Nonnull final Topic start, final int neededLevel) {
    if (neededLevel <= 0) {
      return assertNotNull(start.getMap().getRoot());
    } else {
      Topic thetopic = start;
      while (thetopic.getTopicLevel() >= neededLevel) {
        thetopic = assertNotNull(thetopic.getParent());
      }
      return thetopic;
    }
  }

  @Nullable
  private Topic decodeLine(@Nonnull final MindMap map, @Nullable final Topic parent, @Nonnull final Iterator<String> lines) {
    Topic result = null;
    final String line = nextNonEmptyString(lines);
    if (line != null) {
      final int level = calcLevel(line);
      final String trimmed = line.trim();
      int parentLevel = 0;
      if (parent != null) {
        parentLevel = parent.getTopicLevel();
      }
      if (parent != null) {
        if (parentLevel < level) {
          result = new Topic(map, parent, trimmed);
        } else if (level == parentLevel) {
          result = new Topic(map, parent.getParent(), trimmed);
        } else {
          result = new Topic(map, findParentForLevel(parent, level), trimmed);
        }
      } else {
        result = new Topic(map, null, trimmed);
        map.setRoot(result, false);
      }
    }
    return result;
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
