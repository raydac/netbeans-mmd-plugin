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

package com.igormaznitsa.mindmap.annotations.processor.builder.exceptions;

import com.igormaznitsa.mindmap.annotations.processor.builder.elements.FileItem;
import com.igormaznitsa.mindmap.annotations.processor.builder.elements.TopicItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultipleFileVariantsForTopicException extends MmdAnnotationProcessorException {

  private final List<FileItem> variants;

  public MultipleFileVariantsForTopicException(
      final TopicItem source, final List<FileItem> variants) {
    super(source, "Detected multiple target MMD file variants for a topic annotation");
    this.variants = Collections.unmodifiableList(new ArrayList<>(variants));
  }

  public List<FileItem> getVariants() {
    return this.variants;
  }

  @Override
  public String toString() {
    return "MultipleFIleVariantsForTopicException{"
        + "source="
        + this.getSource()
        + "variants="
        + this.getVariants()
        + "message="
        + this.getMessage()
        + '}';
  }
}
