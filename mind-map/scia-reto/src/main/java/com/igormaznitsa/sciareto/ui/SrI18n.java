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

import com.igormaznitsa.mindmap.swing.ide.IDEBridgeFactory;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;

public final class SrI18n {

  private static final String RESOURCE_PATH = "com/igormaznitsa/nbmindmap/i18n/Bundle";

  private static final SrI18n INSTANCE = new SrI18n();

  private SrI18n() {

  }

  @Nonnull
  public static SrI18n getInstance() {
    return INSTANCE;
  }

  @Nonnull
  public ResourceBundle findBundle() {
    return this.findBundle(IDEBridgeFactory.findInstance().getIDELocale());
  }

  @Nonnull
  public ResourceBundle findBundle(@Nonnull final Locale locale) {
    return ResourceBundle.getBundle(RESOURCE_PATH, locale);
  }
}
