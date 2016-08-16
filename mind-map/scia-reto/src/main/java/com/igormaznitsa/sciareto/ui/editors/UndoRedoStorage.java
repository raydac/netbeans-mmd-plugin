/*
 * Copyright 2016 Igor Maznitsa.
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
package com.igormaznitsa.sciareto.ui.editors;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.undo.UndoManager;

public final class UndoRedoStorage<T> {
  
  private final List<T> undoItems = new ArrayList<>();
  private final List<T> redoItems = new ArrayList<>();
  private final int maxSize;
  
  public UndoRedoStorage(final int max) {
    UndoManager manager;
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
  public T fromRedo(){
    return this.redoItems.isEmpty() ? null : this.redoItems.remove(this.redoItems.size() - 1);
  }
  
  public void addToRedo(@Nonnull final T val) {
    this.redoItems.add(val);
    while (this.redoItems.size() > maxSize) {
      this.redoItems.remove(0);
    }
  }

  public void clearRedo(){
    this.redoItems.clear();
  }

  public void clearUndo() {
    this.undoItems.clear();
  }
  
  public void addToUndo(@Nonnull final T val) {
    this.undoItems.add(val);
    while(this.undoItems.size()>maxSize) this.undoItems.remove(0);
  }
}
