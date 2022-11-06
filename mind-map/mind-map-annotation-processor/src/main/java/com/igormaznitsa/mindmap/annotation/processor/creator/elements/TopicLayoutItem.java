package com.igormaznitsa.mindmap.annotation.processor.creator.elements;

import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.annotations.MmdTopic;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.lang.model.element.Element;
import org.apache.commons.lang3.ArrayUtils;

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

  boolean isLinked() {
    return this.parent != null;
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
}
