/*
 * Copyright 2016 Igor Maznitsa.
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
package com.igormaznitsa.sciareto.ui.platform;

import javax.annotation.Nonnull;
import javax.swing.UIManager;

class PlatformWindows implements Platform {

  @Override
  public void init() {
    
  }

  @Override
  public boolean registerPlatformMenuEvent(@Nonnull final PlatformMenuEvent event, @Nonnull final PlatformMenuAction listener) {
    return false;
  }

  @Override
  @Nonnull
  public String getDefaultLFClassName() {
    return UIManager.getSystemLookAndFeelClassName();
  }

  @Override
  public void dispose() {
    
  }

  @Override
  @Nonnull
  public String getName() {
    return "Windows"; //NOI18N
  }
}
