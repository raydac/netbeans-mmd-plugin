package com.igormaznitsa.mindmap.annotation.processor.creator.elements;

import static com.igormaznitsa.mindmap.annotation.processor.creator.elements.AbstractMmdAnnotationItem.MmdAttribute.COLOR_BORDER;
import static com.igormaznitsa.mindmap.annotation.processor.creator.elements.AbstractMmdAnnotationItem.MmdAttribute.COLOR_FILL;
import static com.igormaznitsa.mindmap.annotation.processor.creator.elements.AbstractMmdAnnotationItem.MmdAttribute.COLOR_TEXT;
import static com.igormaznitsa.mindmap.annotation.processor.creator.elements.AbstractMmdAnnotationItem.MmdAttribute.EMOTICON;
import static com.igormaznitsa.mindmap.annotation.processor.creator.elements.AbstractMmdAnnotationItem.MmdAttribute.LEFT_SIDE;
import static com.igormaznitsa.mindmap.annotation.processor.creator.elements.AbstractMmdAnnotationItem.MmdAttribute.TOPIC_LINK_UID;
import static com.igormaznitsa.mindmap.annotation.processor.creator.elements.AbstractMmdAnnotationItem.fillAttributesWithoutFileAndTopicLinks;
import static com.igormaznitsa.mindmap.annotation.processor.creator.elements.AbstractMmdAnnotationItem.setTopicDirection;

import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.annotations.Direction;
import com.igormaznitsa.mindmap.model.annotations.MmdColor;
import com.igormaznitsa.mindmap.model.annotations.MmdTopic;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.lang.model.element.Element;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

final class TopicLayoutItem {
  private final MmdAnnotationTopicItem annotationItem;
  private final boolean autoCreated;
  private final String forceTitle;
  private TopicLayoutItem parent;
  private Topic topic;

  TopicLayoutItem(final MmdAnnotationTopicItem annotationItem) {
    this(annotationItem, null, false);
  }

  TopicLayoutItem(
      final MmdAnnotationTopicItem annotationItem,
      final String forceTitle,
      final boolean autoCreated
  ) {
    this.annotationItem = annotationItem;
    this.forceTitle = forceTitle;
    this.autoCreated = autoCreated;
  }

  public boolean isAutocreated() {
    return this.autoCreated;
  }

  boolean isLinked() {
    return this.parent != null;
  }

  public String findAnyPossibleUid() {
    if (this.autoCreated) {
      return this.forceTitle;
    } else {
      if (StringUtils.isBlank(this.annotationItem.getTopicAnnotation().uid())) {
        final String uid = this.annotationItem.getTopicAnnotation().uid();
        final String title = this.annotationItem.getTopicAnnotation().title();
        final String elementName =
            this.annotationItem.annotation.getElement().getSimpleName().toString();

        if (StringUtils.isNotBlank(uid)) {
          return uid;
        }
        if (StringUtils.isNotBlank(title)) {
          return uid;
        }
        return elementName;
      } else {
        return this.annotationItem.getTopicAnnotation().uid();
      }
    }
  }

  public Optional<TopicLayoutItem> findCloseParentByElements(final List<TopicLayoutItem> items) {
    return this.findParentByElements(items, this.annotationItem.annotation.getElement());
  }

  private Optional<TopicLayoutItem> findParentByElements(final List<TopicLayoutItem> items,
                                                         final Element element) {
    final Element enclosing = element.getEnclosingElement();
    if (enclosing == null) {
      return Optional.empty();
    }
    final MmdTopic[] topicAnnotations = enclosing.getAnnotationsByType(MmdTopic.class);
    if (topicAnnotations.length != 0) {
      final TopicLayoutItem found = items.stream()
          .filter(x -> x.annotationItem.getAnnotation().getElement().equals(enclosing))
          .findFirst().orElse(null);
      if (found != null) {
        return Optional.of(found);
      }
    }
    return this.findParentByElements(items, enclosing);
  }

  public TopicLayoutItem getParent() {
    return this.parent;
  }

  public void setParent(final TopicLayoutItem parent) {
    this.parent = parent;
  }

  private String findTitle() {
    if (this.forceTitle != null) {
      return this.forceTitle;
    }
    if (this.getAnnotation().title().length() == 0) {
      return this.annotationItem.getAnnotation().getElement().getSimpleName().toString();
    } else {
      return this.getAnnotation().title();
    }
  }

  public Topic findOrCreateTopic(final MindMap mindMap) {
    if (this.topic == null) {
      Topic parentTopic = null;
      TopicLayoutItem current = this.parent;
      while (current != null) {
        final Topic found = current.findOrCreateTopic(mindMap);
        if (parentTopic == null) {
          parentTopic = found;
        }
        current = current.parent;
      }
      this.topic = new Topic(mindMap,
          parentTopic == null ? mindMap.getRoot() : parentTopic,
          this.findTitle());
      if (this.autoCreated) {
        this.topic.setText(this.forceTitle);
      }
    }
    return this.topic;
  }

  public MmdTopic getAnnotation() {
    return (MmdTopic) this.annotationItem.getAnnotation().getAnnotation();
  }

  public MmdAnnotationTopicItem getAnnotationItem() {
    return this.annotationItem;
  }

  public TopicLayoutItem[] findPath() {
    if (!(this.parent == null)) {
      return ArrayUtils.add(this.parent.findPath(), this);
    } else {
      return new TopicLayoutItem[] {this};
    }
  }

  @Override
  public String toString() {
    return "TopicLayoutItem{" +
        "autoCreated=" + this.autoCreated +
        ", title='" + this.findTitle() + '\'' +
        '}';
  }

  @Override
  public int hashCode() {
    return this.annotationItem.hashCode();
  }

  @Override
  public boolean equals(final Object that) {
    if (that == null) {
      return false;
    }
    if (this == that) {
      return true;
    }
    if (that instanceof TopicLayoutItem) {
      return Objects.equals(this.annotationItem, ((TopicLayoutItem) that).annotationItem);
    }
    return false;
  }

  public void processTopicAttributes() throws URISyntaxException {
    if (this.annotationItem.getTopicAnnotation().direction() == Direction.LEFT) {
      TopicLayoutItem root = this;
      while (root.getParent() != null) {
        root = root.getParent();
      }
      setTopicDirection(root.topic, Direction.LEFT);
    }

    this.fillAttributesWithParentAwareness(this.topic);
  }

  private void fillAttributesWithParentAwareness(final Topic topic) throws URISyntaxException {
    fillAttributesWithoutFileAndTopicLinks(topic, this.annotationItem.annotation.getElement(),
        this.annotationItem.getTopicAnnotation());

    if (this.autoCreated) {
      topic.setText(this.forceTitle);
      topic.removeAttributes(false,
          TOPIC_LINK_UID.getId(),
          EMOTICON.getId(),
          LEFT_SIDE.getId()
      );
    }

    final TopicLayoutItem[] topicPath = this.findPath();

    MmdColor colorText = this.annotationItem.getTopicAnnotation().colorText();
    MmdColor colorBorder = this.annotationItem.getTopicAnnotation().colorBorder();
    MmdColor colorFill = this.annotationItem.getTopicAnnotation().colorFill();

    for (int i = topicPath.length - 1; i >= 0; i--) {
      final TopicLayoutItem next = topicPath[i];
      if (colorText == MmdColor.DEFAULT) {
        colorText = next.getAnnotationItem().getTopicAnnotation().colorText();
      }
      if (colorFill == MmdColor.DEFAULT) {
        colorFill = next.getAnnotationItem().getTopicAnnotation().colorFill();
      }
      if (colorBorder == MmdColor.DEFAULT) {
        colorBorder = next.getAnnotationItem().getTopicAnnotation().colorBorder();
      }
    }

    if (colorText != MmdColor.DEFAULT) {
      topic.putAttribute(COLOR_TEXT.getId(), colorText.getHtmlColor());
    }
    if (colorFill != MmdColor.DEFAULT) {
      topic.putAttribute(COLOR_FILL.getId(), colorFill.getHtmlColor());
    }
    if (colorBorder != MmdColor.DEFAULT) {
      topic.putAttribute(COLOR_BORDER.getId(), colorBorder.getHtmlColor());
    }
  }
}
