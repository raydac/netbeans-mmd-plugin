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

package com.igormaznitsa.sciareto.ui.editors;

import java.util.Locale;
import javax.annotation.Nullable;

public enum PlantUmlSecurityProfile {
  LEGACY,
  UNSECURE,
  INTERNET,
  //ALLOWLIST,
  SANDBOX;

  @Nullable
  public static PlantUmlSecurityProfile findForText(@Nullable String text, @Nullable
  final PlantUmlSecurityProfile defaultProfile) {
    PlantUmlSecurityProfile result = defaultProfile;
    if (text != null) {
      final String normalized = text.trim().toUpperCase(Locale.ENGLISH);
      for (final PlantUmlSecurityProfile p : PlantUmlSecurityProfile.values()) {
        if (p.name().equals(normalized)) {
          result = p;
          break;
        }
      }
    }
    return result;
  }
}
