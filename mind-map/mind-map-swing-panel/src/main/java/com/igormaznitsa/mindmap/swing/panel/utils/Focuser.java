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
package com.igormaznitsa.mindmap.swing.panel.utils;

import static java.util.Objects.requireNonNull;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public final class Focuser implements AncestorListener {

  private final JComponent component;
  private final Consumer<Window>[] extraActions;

  @SafeVarargs
  public Focuser(final JComponent component, final Consumer<Window>... extraActions) {
    this.component = component;
    this.component.addAncestorListener(this);
    this.extraActions = requireNonNull(extraActions);
  }

  @Override
  public void ancestorAdded(final AncestorEvent event) {
    if (event.getID() == AncestorEvent.ANCESTOR_ADDED) {
      if (event.getAncestor() instanceof Window) {
        final Window window = (Window) event.getAncestor();
        window.addWindowListener(new WindowAdapter() {
          private void doBusiness() {
            SwingUtilities.invokeLater(() -> {
              component.requestFocus();
              for (final Consumer<Window> r : extraActions) {
                r.accept(window);
              }
            });

          }

          @Override
          public void windowGainedFocus(final WindowEvent e) {
            this.doBusiness();
          }

          @Override
          public void windowActivated(final WindowEvent e) {
            this.doBusiness();
          }

          @Override
          public void windowOpened(final WindowEvent e) {
            this.doBusiness();
          }

          @Override
          public void windowClosing(final WindowEvent e) {
            ((Window) e.getComponent()).removeWindowListener(this);
          }
        });
        this.component.removeAncestorListener(this);
      }
    }
  }

  @Override
  public void ancestorRemoved(final AncestorEvent event) {
  }

  @Override
  public void ancestorMoved(final AncestorEvent event) {

  }
}
