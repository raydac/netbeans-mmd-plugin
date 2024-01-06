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

package com.igormaznitsa.mindmap.annotations.processor.builder.elements;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.igormaznitsa.mindmap.annotations.Direction;
import com.igormaznitsa.mindmap.annotations.MmdColor;
import com.igormaznitsa.mindmap.annotations.MmdEmoticon;
import com.igormaznitsa.mindmap.annotations.MmdTopic;
import com.igormaznitsa.mindmap.annotations.processor.MmdAnnotationWrapper;
import com.igormaznitsa.mindmap.annotations.processor.builder.exceptions.MmdAnnotationProcessorException;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.StandardTopicAttributes;
import com.igormaznitsa.mindmap.model.Topic;
import java.lang.annotation.Annotation;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractItem {
  private static final Pattern PATTERN_FILEPATH_LINE_NUMBER =
      Pattern.compile("^(.+)(?:\\:([0-9]+))|(.+)$");
  protected final MmdAnnotationWrapper annotationContainer;

  protected AbstractItem(final MmdAnnotationWrapper annotationContainer) {
    this.annotationContainer = requireNonNull(annotationContainer);
  }

  public MmdAnnotationWrapper getAnnotationContainer() {
    return this.annotationContainer;
  }

  @SuppressWarnings("SameParameterValue")
  protected static void setTopicDirection(final Topic topic, final Direction direction) {
    if (direction != Direction.AUTO) {
      if (direction == Direction.LEFT) {
        topic.putAttribute(MmdAttribute.LEFT_SIDE.getId(), "true");
      }
    }
  }

  protected static void fillAnchorOrFileLink(
      final Topic topic,
      final AbstractItem topicItem,
      final MmdTopic topicAnnotation,
      final Path baseFolder)
      throws MmdAnnotationProcessorException {
    final Properties properties = new Properties();

    final String filePath;
    if (isNotBlank(topicAnnotation.fileLink())) {
      final Matcher matcher = PATTERN_FILEPATH_LINE_NUMBER.matcher(topicAnnotation.fileLink());
      if (matcher.find()) {
        if (matcher.group(1) == null) {
          filePath = matcher.group(3);
        } else {
          filePath = matcher.group(1);
          try {
            properties.put("line", Long.valueOf(matcher.group(2)));
          } catch (NumberFormatException ex) {
            throw new MmdAnnotationProcessorException(
                topicItem, "Can't process line number in file path: " + topicAnnotation.fileLink());
          }
        }
      } else {
        throw new MmdAnnotationProcessorException(
            topicItem, "Can't extract file and line from file path: " + topicAnnotation.fileLink());
      }
    } else if (topicAnnotation.anchor()) {
      filePath = topicItem.getPath().toString();
      properties.put("line", Long.toString(topicItem.getLine()));
    } else {
      return;
    }
    final MMapURI fileUri;
    try {
      fileUri = MMapURI.makeFromFilePath(baseFolder.toFile(), filePath, properties);
    } catch (Exception ex) {
      throw new MmdAnnotationProcessorException(
          topicItem, "Can't create topic file path for error", ex);
    }
    topic.setExtra(new ExtraFile(fileUri));
  }

  protected static void fillAttributesWithoutFileAndTopicLinks(
      final Topic topic, final Element element, final MmdTopic topicAnnotation)
      throws URISyntaxException {
    topic.setText(
        StringUtils.isBlank(topicAnnotation.title())
            ? element.getSimpleName().toString()
            : topicAnnotation.title());

    if (isNotBlank(topicAnnotation.note())) {
      topic.setExtra(new ExtraNote(topicAnnotation.note()));
    }

    if (isNotBlank(topicAnnotation.uri())) {
      topic.setExtra(new ExtraLink(topicAnnotation.uri()));
    }

    if (isNotBlank(topicAnnotation.uid())) {
      topic.putAttribute(MmdAttribute.TOPIC_LINK_UID.getId(), topicAnnotation.uid());
    }

    if (topicAnnotation.colorBorder() != MmdColor.Default) {
      topic.putAttribute(
          MmdAttribute.COLOR_BORDER.getId(), topicAnnotation.colorBorder().getHtmlColor());
    }

    if (topicAnnotation.colorFill() != MmdColor.Default) {
      topic.putAttribute(
          MmdAttribute.COLOR_FILL.getId(), topicAnnotation.colorFill().getHtmlColor());
    }

    if (topicAnnotation.colorText() != MmdColor.Default) {
      topic.putAttribute(
          MmdAttribute.COLOR_TEXT.getId(), topicAnnotation.colorText().getHtmlColor());
    }

    if (topicAnnotation.collapse()) {
      topic.putAttribute(MmdAttribute.COLLAPSED.getId(), "true");
    }

    if (topicAnnotation.emoticon() != MmdEmoticon.EMPTY) {
      topic.putAttribute(MmdAttribute.EMOTICON.getId(), topicAnnotation.emoticon().getId());
    }
  }

  public <A extends Annotation> A asAnnotation() {
    return this.annotationContainer.asAnnotation();
  }

  public long getLine() {
    return this.annotationContainer.getLine();
  }

  public long getStartPosition() {
    return this.annotationContainer.getStartPositionInsideFile();
  }

  public Path getPath() {
    return this.annotationContainer.getPath();
  }

  public Element getElement() {
    return this.annotationContainer.getElement();
  }

  @Override
  public boolean equals(final Object that) {
    if (this == that) {
      return true;
    }
    if (that == null || getClass() != that.getClass()) {
      return false;
    }
    return Objects.equals(this.annotationContainer, ((AbstractItem) that).annotationContainer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.annotationContainer);
  }

  @Override
  public String toString() {
    return "AbstractItem{" + "annotationContainer=" + this.annotationContainer + '}';
  }

  public enum MmdAttribute implements StandardTopicAttributes {
    LEFT_SIDE(MMD_TOPIC_ATTRIBUTE_SIDE_LEFT),
    TOPIC_LINK_UID(MMD_TOPIC_ATTRIBUTE_LINK_UID),
    EMOTICON(MMD_TOPIC_ATTRIBUTE_EMOTICON),
    COLOR_FILL(MMD_TOPIC_ATTRIBUTE_COLOR_FILL),
    COLOR_BORDER(MMD_TOPIC_ATTRIBUTE_COLOR_BORDER),
    COLOR_TEXT(MMD_TOPIC_ATTRIBUTE_COLOR_TEXT),
    COLLAPSED(MMD_TOPIC_ATTRIBUTE_COLLAPSED);

    private final String id;

    MmdAttribute(final String id) {
      this.id = id;
    }

    public String getId() {
      return this.id;
    }
  }
}
