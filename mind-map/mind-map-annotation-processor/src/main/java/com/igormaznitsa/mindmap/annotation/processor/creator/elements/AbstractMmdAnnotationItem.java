package com.igormaznitsa.mindmap.annotation.processor.creator.elements;

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.annotation.processor.MmdAnnotation;
import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.annotations.Direction;
import com.igormaznitsa.mindmap.model.annotations.MmdColor;
import com.igormaznitsa.mindmap.model.annotations.MmdEmoticon;
import com.igormaznitsa.mindmap.model.annotations.MmdTopic;
import java.net.URISyntaxException;
import java.util.Objects;
import javax.lang.model.element.Element;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractMmdAnnotationItem {
  protected final MmdAnnotation annotation;

  protected static void setTopicDirection(final Topic topic, final Direction direction) {
    if (direction != Direction.AUTO) {
      if (direction == Direction.LEFT) {
        topic.putAttribute(MmdAttribute.LEFT_SIDE.getId(), "true");
      }
    }
  }

  protected static void fillAttributesWithoutFileAndTopicLinks(
      final Topic topic,
      final Element element,
      final MmdTopic topicAnnotation
  ) throws URISyntaxException {
    topic.setText(
        StringUtils.isBlank(topicAnnotation.title()) ? element.getSimpleName().toString() :
            topicAnnotation.title());

    if (StringUtils.isNotBlank(topicAnnotation.note())) {
      topic.setExtra(new ExtraNote(topicAnnotation.note()));
    }

    if (StringUtils.isNotBlank(topicAnnotation.uri())) {
      topic.setExtra(new ExtraLink(topicAnnotation.uri()));
    }

    if (topicAnnotation.colorBorder() != MmdColor.DEFAULT) {
      topic.putAttribute(MmdAttribute.COLOR_BORDER.getId(),
          topicAnnotation.colorBorder().getHtmlColor());
    }

    if (topicAnnotation.colorFill() != MmdColor.DEFAULT) {
      topic.putAttribute(MmdAttribute.COLOR_FILL.getId(),
          topicAnnotation.colorFill().getHtmlColor());
    }

    if (topicAnnotation.colorText() != MmdColor.DEFAULT) {
      topic.putAttribute(MmdAttribute.COLOR_TEXT.getId(),
          topicAnnotation.colorText().getHtmlColor());
    }

    if (topicAnnotation.collapse()) {
      topic.putAttribute(MmdAttribute.COLLAPSED.getId(), "true");
    }

    if (topicAnnotation.emoticon() != MmdEmoticon.EMPTY) {
      topic.putAttribute(MmdAttribute.EMOTICON.getId(), topicAnnotation.emoticon().getId());
    }
  }

  public enum MmdAttribute {
    LEFT_SIDE("leftSide"),
    TOPIC_LINK_UID("topicLinkUID"),
    EMOTICON("mmd.emoticon"),
    COLOR_FILL("fillColor"),
    COLOR_BORDER("borderColor"),
    COLOR_TEXT("textColor"),
    COLLAPSED("collapsed");

    private final String id;

    MmdAttribute(final String id) {
      this.id = id;
    }

    public String getId() {
      return this.id;
    }
  }

  public AbstractMmdAnnotationItem(final MmdAnnotation annotation) {
    this.annotation = requireNonNull(annotation);
  }

  public MmdAnnotation getAnnotation() {
    return this.annotation;
  }

  @Override
  public boolean equals(final Object that) {
    if (this == that) {
      return true;
    }
    if (that == null || getClass() != that.getClass()) {
      return false;
    }
    return Objects.equals(this.annotation, ((AbstractMmdAnnotationItem) that).annotation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.annotation);
  }

  @Override
  public String toString() {
    return "AbstractMmdAnnotationItem{" +
        "annotation=" + annotation +
        '}';
  }
}
