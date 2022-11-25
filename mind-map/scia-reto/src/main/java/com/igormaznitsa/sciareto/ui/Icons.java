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
package com.igormaznitsa.sciareto.ui;

import javax.annotation.Nonnull;
import javax.swing.ImageIcon;

public enum Icons {
  MMDBADGE("mmdbadge.png"), //NOI18N
  DOCUMENT("document16.png"), //NOI18N
  EXPANDALL("toggle_expand16.png"), //NOI18N
  COLLAPSEALL("toggle16.png"), //NOI18N
  SOURCE("source16.png"), //NOI18N
  BLUEBALL("blueball16.png"), //NOI18N
  GOLDBALL("goldball16.png"), //NOI18N
  COINS("coins_in_hand16.png"); //NOI18N

  private final ImageIcon icon;
  
  @Nonnull
  public ImageIcon getIcon(){
    return this.icon;
  }
  
  private Icons(@Nonnull final String name) {
    this.icon = new ImageIcon(UiUtils.loadIcon(name));
  }
}
