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

public interface FindTextScopeProvider {
  
  public enum SearchTextScope {
    IN_TOPIC_TEXT,
    IN_TOPIC_NOTES,
    IN_TOPIC_FILES,
    IN_TOPIC_URI,
    CASE_INSENSETIVE;
  }
  
  boolean toSearchIn(@Nonnull SearchTextScope scope);
}
