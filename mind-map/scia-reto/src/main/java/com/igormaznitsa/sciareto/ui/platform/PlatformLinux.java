/*
 * Copyright (C) 2019 Igor Maznitsa.
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
package com.igormaznitsa.sciareto.ui.platform;

import com.igormaznitsa.sciareto.Main;
import java.awt.Toolkit;
import java.lang.reflect.Field;
import javax.annotation.Nonnull;

public class PlatformLinux extends PlatformDefault {

  public PlatformLinux() {
    super();
  }

  private static void setGnomeAppTitle() {
    try {
      final Toolkit toolkit = Toolkit.getDefaultToolkit();
      final Field awtAppClassNameField = toolkit.getClass().getDeclaredField("awtAppClassName");
      awtAppClassNameField.setAccessible(true);
      awtAppClassNameField.set(toolkit, Main.APP_TITLE);
    } catch (Exception ex) {
      //Do nothing
    }
  }

  @Override
  public void init() {
    setGnomeAppTitle();
  }

  @Override
  @Nonnull
  public String getName() {
    return "Linux"; //NOI18N
  }

}
