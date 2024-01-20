/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.igormaznitsa.sciareto.ui.editors;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class UndoRedoStorage<T> {

  private final List<T> undoItems = new ArrayList<>();
  private final List<T> redoItems = new ArrayList<>();
  private final int maxSize;

  private boolean hasUndoStateRemovedForFullBuffer = false;

  @Nonnull
  @MustNotContainNull
  public List<byte[]> historyAsBytes(final int limit,
                                     @Nonnull final Function<T, byte[]> converter) {
    final List<byte[]> result = new ArrayList<>();
    for (int i = this.undoItems.size() - 1; i >= 0 && limit > 0; i--) {
      result.add(0, converter.apply(this.undoItems.get(i)));
    }
    return result;
  }

  public void loadFromBytes(@Nonnull @MustNotContainNull final List<byte[]> items,
                            @Nonnull final Function<byte[], T> converter) {
    this.undoItems.clear();
    this.redoItems.clear();
    for (final byte[] item : items) {
      if (this.undoItems.size() >= this.maxSize) {
        break;
      }
      this.undoItems.add(converter.apply(item));
    }
  }

  public UndoRedoStorage(final int max) {
    this.maxSize = max;
  }

  public boolean hasUndo() {
    return !this.undoItems.isEmpty();
  }

  public boolean hasRedo() {
    return !this.redoItems.isEmpty();
  }

  @Nullable
  public T fromUndo() {
    return this.undoItems.isEmpty() ? null : this.undoItems.remove(this.undoItems.size() - 1);
  }

  @Nullable
  public T fromRedo() {
    return this.redoItems.isEmpty() ? null : this.redoItems.remove(this.redoItems.size() - 1);
  }

  public void addToRedo(@Nonnull final T val) {
    this.redoItems.add(val);
    while (this.redoItems.size() > maxSize) {
      this.redoItems.remove(0);
    }
  }

  public void clearRedo() {
    this.redoItems.clear();
  }

  public void clearUndo() {
    this.hasUndoStateRemovedForFullBuffer = false;
    this.undoItems.clear();
  }

  public void setFlagThatSomeStateLost() {
    this.hasUndoStateRemovedForFullBuffer = true;
  }

  public boolean hasRemovedUndoStateForFullBuffer() {
    return this.hasUndoStateRemovedForFullBuffer;
  }

  public void addToUndo(@Nonnull final T val) {
    this.undoItems.add(val);
    while (this.undoItems.size() > maxSize) {
      this.hasUndoStateRemovedForFullBuffer = true;
      this.undoItems.remove(0);
    }
  }
}
