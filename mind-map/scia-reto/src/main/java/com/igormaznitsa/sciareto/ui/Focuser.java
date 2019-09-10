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
package com.igormaznitsa.sciareto.ui;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
    if (event.getID() == AncestorEvent.ANCESTOR_ADDED) {
      if (event.getAncestor() instanceof Window) {
        ((Window) event.getAncestor()).addWindowListener(new WindowAdapter() {
          @Override
          public void windowOpened(@Nonnull final WindowEvent e) {
            try {
              SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                  component.requestFocus();
                }
              });
            } finally {
              ((Window) e.getComponent()).removeWindowListener(this);
            }
          }
        });
        this.component.removeAncestorListener(this);
      }
    }
  }

  @Override
  public void ancestorRemoved(@Nonnull final AncestorEvent event) {
  }

  @Override
  public void ancestorMoved(@Nonnull final AncestorEvent event) {

  }
}
