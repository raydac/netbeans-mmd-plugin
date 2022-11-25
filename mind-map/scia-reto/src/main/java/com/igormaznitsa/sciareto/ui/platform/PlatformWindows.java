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
package com.igormaznitsa.sciareto.ui.platform;

import com.igormaznitsa.meta.annotation.Warning;

import javax.annotation.Nonnull;
import javax.swing.UIManager;

@Warning("It is accessible through Class.forName(), don't rename it!")
public class PlatformWindows extends PlatformDefault {

  public PlatformWindows(){
    super();
  }
  
  @Override
  @Nonnull
  public String getDefaultLFClassName() {
    return UIManager.getSystemLookAndFeelClassName();
  }

  @Override
  @Nonnull
  public String getName() {
    return "Windows"; //NOI18N
  }
}
