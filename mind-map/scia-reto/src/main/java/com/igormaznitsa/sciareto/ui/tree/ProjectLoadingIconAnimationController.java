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
package com.igormaznitsa.sciareto.ui.tree;

import com.igormaznitsa.sciareto.ui.UiUtils;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;
import static java.awt.image.ImageObserver.ABORT;
import static java.awt.image.ImageObserver.ALLBITS;
import static java.awt.image.ImageObserver.FRAMEBITS;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nonnull;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

public final class ProjectLoadingIconAnimationController {
  
  public static final ImageIcon LOADING = new ImageIcon(UiUtils.class.getClassLoader().getResource("icons/loading.gif")); //NOI18N

  private static final class LoadingIconRedrawer {

    final JTree tree;
    final TreePath path;

    LoadingIconRedrawer(@Nonnull final JTree tree, @Nonnull final TreePath path) {
      this.tree = tree;
      this.path = path;
    }

    public void redraw() {
      final Rectangle rect = this.tree.getPathBounds(this.path);
      if (rect != null) {
        this.tree.repaint(rect);
      }
    }
  }

  private final List<LoadingIconRedrawer> registeredRedrawers = new CopyOnWriteArrayList<>();
 
  private static final ProjectLoadingIconAnimationController INSTANCE = new ProjectLoadingIconAnimationController();
 
  private ProjectLoadingIconAnimationController(){
    LOADING.setImageObserver(new ImageObserver() {
      @Override
      public boolean imageUpdate(Image img, int flags, int x, int y, int width, int height) {
        if ((flags & (FRAMEBITS | ALLBITS)) != 0) {
          for (final LoadingIconRedrawer redrawer : registeredRedrawers) {
            redrawer.redraw();
          }
        }
        return (flags & (ALLBITS | ABORT)) == 0;
      }
    });

  }
  
  @Nonnull
  public static ProjectLoadingIconAnimationController getInstance() {
    return INSTANCE;
  }
  
  public void registerLoadingProject(@Nonnull final JTree tree, @Nonnull final NodeProject project) {
    this.registeredRedrawers.add(new LoadingIconRedrawer(tree, new TreePath(new Object[]{project.getGroup(), project})));
  }

  public void unregisterLoadingProject(@Nonnull final NodeProject project) {
    registeredRedrawers.removeIf(r -> r.path.getLastPathComponent() == project);
  }

 
}
