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

package com.igormaznitsa.mindmap.model.parser;

import java.io.Serializable;

/**
 * Parser token position.
 */
public final class TokenPosition implements Serializable {

  private static final long serialVersionUID = 4029368174558764315L;

  private final int startOffset;
  private final int endOffset;

  /**
   * Constructor
   *
   * @param startOffset token start offset
   * @param endOffset   token end offset
   * @throws IllegalArgumentException if end position is less than start position
   */
  public TokenPosition(final int startOffset, final int endOffset) {
    if (endOffset < startOffset) {
      throw new IllegalArgumentException("End offset is less that start offset");
    } else {
      this.startOffset = startOffset;
      this.endOffset = endOffset;
    }
  }

  /**
   * Get start offset
   *
   * @return start offset
   */
  public int getStartOffset() {
    return this.startOffset;
  }

  /**
   * Get end offset
   *
   * @return end offset
   */
  public int getEndOffset() {
    return this.endOffset;
  }
}
