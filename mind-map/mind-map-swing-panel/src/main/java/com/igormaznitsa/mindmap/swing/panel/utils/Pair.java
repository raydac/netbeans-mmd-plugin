/*
 * Copyright 2019 Igor Maznitsa.
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

package com.igormaznitsa.mindmap.swing.panel.utils;

import com.igormaznitsa.meta.common.utils.Assertions;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Auxiliary class to keep some pair of objects in single container.
 *
 * @param <L> left object type
 * @param <R> right object type
 * @since 1.4.7
 */
public class Pair<L, R> {

  private final L left;
  private final R right;

  public Pair(@Nonnull final L left, @Nonnull final R right) {
    this.left = Assertions.assertNotNull(left);
    this.right = Assertions.assertNotNull(right);
  }

  @Override
  public int hashCode() {
    return this.left.hashCode() ^ this.right.hashCode();
  }

  @Override
  public boolean equals(@Nullable final Object that) {
    if (that == null) {
      return false;
    }
    if (this == that) {
      return true;
    }
    if (that instanceof Pair<?, ?>) {
      final Pair<?, ?> thatPair = (Pair<?, ?>) that;
      return this.left.equals(thatPair.left) && this.right.equals(thatPair.right);
    }
    return false;
  }

  @Nonnull
  public L getLeft() {
    return this.left;
  }

  @Nonnull
  public R getRight() {
    return this.right;
  }

  @Override
  @Nonnull
  public String toString() {
    return "Pair(" + this.left + ":" + this.right + ")";
  }
}
