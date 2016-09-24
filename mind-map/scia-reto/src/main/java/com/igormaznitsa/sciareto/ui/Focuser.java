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
package com.igormaznitsa.sciareto.ui;

import javax.annotation.Nonnull;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public final class Focuser implements AncestorListener {

  private final JComponent component;

  public Focuser(@Nonnull final JComponent component) {
    this.component = component;
    this.component.addAncestorListener(this);
  }

  @Override
  public void ancestorAdded(@Nonnull final AncestorEvent event) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        component.grabFocus();
      }
    });
    this.component.removeAncestorListener(this);
  }

  @Override
  public void ancestorRemoved(@Nonnull final AncestorEvent event) {
  }

  @Override
  public void ancestorMoved(@Nonnull final AncestorEvent event) {

  }
}
