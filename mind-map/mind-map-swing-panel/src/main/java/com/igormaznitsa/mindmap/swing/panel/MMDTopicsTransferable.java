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

package com.igormaznitsa.mindmap.swing.panel;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.StandardMmdAttributes;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.ide.IDEBridgeFactory;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Map;

/**
 * Transferable object to represent topic list in clipboard.
 *
 * @since 1.3.1
 */
public class MMDTopicsTransferable implements Transferable {

  public static final DataFlavor MMD_DATA_FLAVOR;

  static {
    try {
      MMD_DATA_FLAVOR = new DataFlavor(DataFlavor.javaSerializedObjectMimeType + ";class=\"" +
          NBMindMapTopicsContainer.class.getName() + "\"", "nb-mindmap-topic-list",
          NBMindMapTopicsContainer.class.getClassLoader());
    } catch (ClassNotFoundException ex) {
      throw new Error("Can't find class", ex);
    }
  }

  private static final DataFlavor[] FLAVORS
      = new DataFlavor[] {DataFlavor.stringFlavor, MMD_DATA_FLAVOR};
  private static final String END_OF_LINE = System.getProperty("line.separator", "\n");

  private final Topic[] topics;

  public MMDTopicsTransferable(final Topic... topics) {
    this.topics = new Topic[topics.length];

    final MindMap fakeMap = new MindMap(false);
    fakeMap.putAttribute(StandardMmdAttributes.MMD_ATTRIBUTE_GENERATOR_ID, IDEBridgeFactory.findInstance()
        .getIDEGeneratorId());

    for (int i = 0; i < topics.length; i++) {
      this.topics[i] = new Topic(fakeMap, topics[i], true);
    }
  }

  private static String oneLineTitle(final Topic topic) {
    return topic.getText().replace("\n", " ").trim();
  }

  private static String convertTopicToText(final Topic topic, final int level) {
    final StringBuilder result = new StringBuilder();

    for (int i = 0; i < level; i++) {
      if (i == level - 1) {
        result.append("+");
      } else {
        result.append('|');
      }
    }
    final String firstIndentString = result.toString();
    result.setLength(0);

    for (int i = 0; i < level; i++) {
      result.append('|');
    }
    final String otherIndentString = result.toString();
    result.setLength(0);

    result.append(firstIndentString)
        .append('[').append(oneLineTitle(topic)).append(']')
        .append(END_OF_LINE);

    boolean hasExtras = false;
    Topic linkedTopic = null;
    for (final Map.Entry<Extra.ExtraType, Extra<?>> e : topic.getExtras().entrySet()) {
      if (e.getKey() == Extra.ExtraType.TOPIC) {
        final ExtraTopic topicLink = ((ExtraTopic) e.getValue());
        final Topic root = topic.findRoot();
        linkedTopic =
            root.findForAttribute(ExtraTopic.TOPIC_UID_ATTR, topicLink.getValue());
      } else {
        hasExtras = true;
      }
    }

    if (hasExtras || linkedTopic != null) {
      result.append(otherIndentString).append("--------------------").append(END_OF_LINE);
    }

    if (!topic.getExtras().isEmpty()) {
      for (final Map.Entry<Extra.ExtraType, Extra<?>> e : topic.getExtras().entrySet()) {
        switch (e.getKey()) {
          case NOTE: {
            if (Boolean.parseBoolean(topic.getAttributes().get(ExtraNote.ATTR_ENCRYPTED))) {
              result.append(otherIndentString).append("<ENCRYPTED NOTE>").append(END_OF_LINE);
            } else {
              for (final String s : e.getValue().getAsString().split("\\n")) {
                result.append(otherIndentString).append(s.trim()).append(END_OF_LINE);
              }
            }
          }
          break;
          case TOPIC: {
            if (linkedTopic != null) {
              result.append(otherIndentString).append("#(").append(oneLineTitle(linkedTopic))
                  .append(')')
                  .append(END_OF_LINE);
            }
          }
          break;
          case FILE: {
            result.append(otherIndentString).append("FILE=").append(e.getValue().getAsString())
                .append(END_OF_LINE);
          }
          break;
          case LINK: {
            result.append(otherIndentString).append(e.getValue().getAsString())
                .append(END_OF_LINE);
          }
          break;
        }
      }
    }
    if (hasExtras) {
      result.append(otherIndentString).append("--------------------").append(END_OF_LINE);
    }
    result.append(otherIndentString).append(END_OF_LINE);
    for (final Topic c : topic.getChildren()) {
      result.append(convertTopicToText(c, level + 1));
    }

    return result.toString();
  }

  @Override
  public DataFlavor[] getTransferDataFlavors() {
    return FLAVORS;
  }

  @Override
  public boolean isDataFlavorSupported(final DataFlavor flavor) {
    return flavor.isFlavorTextType() || flavor.isMimeTypeEqual(MMD_DATA_FLAVOR);
  }

  @Override
  public Object getTransferData(final DataFlavor flavor)
      throws UnsupportedFlavorException, IOException {
    if (flavor.isFlavorTextType()) {
      final StringBuilder result = new StringBuilder();

      for (final Topic t : this.topics) {
        if (result.length() > 0) {
          result.append("...").append(END_OF_LINE);
        }
        result.append(convertTopicToText(t, 1));
      }

      return result.toString();
    } else if (flavor.isMimeTypeEqual(MMD_DATA_FLAVOR)) {
      return new NBMindMapTopicsContainer(this.topics);
    } else {
      throw new UnsupportedFlavorException(flavor);
    }
  }

}
