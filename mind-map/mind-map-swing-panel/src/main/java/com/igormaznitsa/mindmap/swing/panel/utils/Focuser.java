/* 
 * Copyright (C) 2018 Igor Maznitsa.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
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
