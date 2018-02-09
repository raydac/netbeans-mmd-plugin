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

import com.apple.eawt.Application;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;

class PlatformMacOSX implements Platform {

  private final Application application;

  private final Logger LOGGER = LoggerFactory.getLogger(PlatformMacOSX.class);
  private final Object macOsxAppListener;

  public PlatformMacOSX() {
    this.application = Application.getApplication();
    Object macListener = null;
    try {
      final Class<?> klazz = Class.forName("com.igormaznitsa.sciareto.ui.platform.MacOSXAppListener");//NOI18N
      macListener = klazz.getConstructor(Application.class).newInstance(this.application);
    } catch (Exception ex) {
      LOGGER.error("Can't register application listener, may be newest JDK with removed deprecated methods", ex);//NOI18N
    } finally {
      this.macOsxAppListener = macListener;
    }
  }

  @Override
  public boolean registerPlatformMenuEvent(@Nonnull final PlatformMenuEvent event, @Nonnull final PlatformMenuAction action) {
    if (this.macOsxAppListener != null) {
      try {
        return (Boolean) this.macOsxAppListener.getClass().getMethod("registerPlatformMenuEvent", PlatformMenuEvent.class, PlatformMenuAction.class).invoke(this.macOsxAppListener, event, action);
      } catch (Exception ex) {
        LOGGER.error("Error during call com.igormaznitsa.sciareto.ui.platform.MacOSXAppListener#registerPlatformMenuEvent", ex);//NOI18N
      }
    } else {
      LOGGER.warn("Can't register platform menu event " + event + " because listener is not provided");//NOI18N
    }
    return false;
  }

  @Override
  public void init() {
    System.setProperty("apple.awt.fileDialogForDirectories", "true"); //NOI18N
    System.setProperty("apple.laf.useScreenMenuBar", "true"); //NOI18N
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "SciaReto"); //NOI18N
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
    return "MAC OSX"; //NOI18N
  }
}
