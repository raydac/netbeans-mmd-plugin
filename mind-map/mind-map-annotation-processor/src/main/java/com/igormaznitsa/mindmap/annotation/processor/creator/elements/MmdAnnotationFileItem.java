package com.igormaznitsa.mindmap.annotation.processor.creator.elements;

import com.igormaznitsa.mindmap.annotation.processor.MmdAnnotation;
import com.igormaznitsa.mindmap.model.annotations.MmdFile;
import java.util.UUID;

public class MmdAnnotationFileItem extends AbstractMmdAnnotationItem {
  private final MmdFile mmdFileAnnotation;
  private final String uid;

  public MmdAnnotationFileItem(final MmdAnnotation annotation) {
    super(annotation);
    if (!(annotation.getAnnotation() instanceof MmdFile)) {
      throw new IllegalArgumentException("Expected annotation " + MmdFile.class.getName());
    }
    this.mmdFileAnnotation = (MmdFile) annotation.getAnnotation();
    if (this.mmdFileAnnotation.uid().trim().isEmpty()) {
      this.uid = UUID.randomUUID().toString();
    } else {
      this.uid = this.mmdFileAnnotation.uid().trim();
    }
  }

  public MmdFile getFileAnnotation() {
    return this.mmdFileAnnotation;
  }

  public String getUid() {
    return this.uid;
  }
}
