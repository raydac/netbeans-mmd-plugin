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
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;

@Warning("It is accessible through Class.forName(), don't rename it!")
public class PlatformMacOSX extends PlatformDefault {

  private final Logger LOGGER = LoggerFactory.getLogger(PlatformMacOSX.class);
  private final DesktopAppHandler macOsxAppListener;

  public PlatformMacOSX() {
    super();
    LOGGER.info("Creating desktop application handler");//NOI18N
    this.macOsxAppListener = new DesktopAppHandler();
  }

  @Override
  public boolean registerPlatformMenuEvent(@Nonnull final PlatformMenuEvent event, @Nonnull final PlatformMenuAction action) {
    if (this.macOsxAppListener != null) {
      return this.macOsxAppListener.registerPlatformMenuEvent(event, action);
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
