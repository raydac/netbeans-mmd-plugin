/*
 * Copyright (C) 2018 Igor Maznitsa.
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

import com.apple.eawt.Application;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;

public class PlatformMacOSX extends PlatformDefault {

  private final Application application;

  private final Logger LOGGER = LoggerFactory.getLogger(PlatformMacOSX.class);
  private final Object macOsxAppListener;

  public PlatformMacOSX() {
    super();
    this.application = Application.getApplication();
    Object macListener = null;
    try {
      final Class<?> klazz = Class.forName("com.igormaznitsa.sciareto.ui.platform.MacOSXAppHandlerOld");//NOI18N
      macListener = klazz.getConstructor(Application.class).newInstance(this.application);
      LOGGER.info("Legacy version of MACOSX AWT has been detected and in use");
    } catch (Throwable ex) {
      LOGGER.error("Can't register application listener, may be newest JDK with removed deprecated methods", ex);//NOI18N
      try {
        final Class<?> klazz = Class.forName("com.igormaznitsa.sciareto.ui.platform.MacOSXAppHandler");//NOI18N
        macListener = klazz.getConstructor(Application.class).newInstance(this.application);
        LOGGER.info("Newer version of MACOSX AWT has been detected and in use");
      } catch (Throwable exx) {
        LOGGER.error("Can't register newer MACOSX handler", exx);//NOI18N
        try {
          final Class<?> klazz = Class.forName("com.igormaznitsa.sciareto.ui.platform.DesktopAppHandler");//NOI18N
          macListener = klazz.getConstructor().newInstance();
          LOGGER.info("Desktop handler detected and inited for MACOSX");
        } catch (Throwable exxx) {
          LOGGER.error("Can't register desktop handler, contact dveloper!", exxx);//NOI18N
        }
      }
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
  @Nonnull
  public String getDefaultLFClassName() {
    return UIManager.getSystemLookAndFeelClassName();
  }

  @Override
  @Nonnull
  public String getName() {
    return "MAC OSX"; //NOI18N
  }
}
