/*
 * Copyright 2015-2018 Igor Maznitsa.
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
package com.igormaznitsa.mindmap.swing.panel;

import java.io.Serializable;
import javax.annotation.Nonnull;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.Topic;

/**
 * Auxiliary container to transfer topics through clipboard.
 * @since 1.3.1
 */
public final class NBMindMapTopicsContainer implements Serializable {

  private static final long serialVersionUID = -2749724232423031881L;
  private final Topic[] topics;

  public NBMindMapTopicsContainer(@Nonnull @MustNotContainNull final Topic[] topics) {
    this.topics = topics.clone();
  }

  public boolean isEmpty(){
    return this.topics == null || this.topics.length == 0;
  }
  
  @Nonnull
  @MustNotContainNull
  public Topic[] getTopics() {
    return this.topics.clone();
  }

}
