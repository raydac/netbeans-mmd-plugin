package com.igormaznitsa.mindmap.annotations.processor.builder.elements;

import com.igormaznitsa.mindmap.annotations.processor.FoundMmdAnnotation;
import com.igormaznitsa.mindmap.model.annotations.MmdTopic;
import java.util.Optional;
import javax.lang.model.element.Element;
import org.apache.commons.lang3.StringUtils;

public class TopicItem extends AbstractItem {
  public TopicItem(final FoundMmdAnnotation base) {
    super(base);
    if (!(base.asAnnotation() instanceof MmdTopic)) {
      throw new IllegalArgumentException("Expected annotation " + MmdTopic.class.getName());
    }
  }

  private static Optional<String> findFileUidAmongParentTopics(final Element element) {
    if (element == null) {
      return Optional.empty();
    }
    final MmdTopic topicAnnotation = element.getAnnotation(MmdTopic.class);
    if (topicAnnotation != null && StringUtils.isNotBlank(topicAnnotation.mmdFileUid())) {
      return Optional.of(topicAnnotation.mmdFileUid());
    } else {
      return findFileUidAmongParentTopics(element.getEnclosingElement());
    }
  }

  public MmdTopic asMmdTopicAnnotation() {
    return this.asAnnotation();
  }

  public Optional<String> findFileUidAttribute() {
    return findFileUidAmongParentTopics(this.getElement());
  }

}
