/*
 * Copyright 2015-2018 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
