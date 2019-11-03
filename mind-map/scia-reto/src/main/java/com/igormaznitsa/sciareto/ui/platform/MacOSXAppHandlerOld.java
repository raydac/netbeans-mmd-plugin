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
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationListener;
import com.igormaznitsa.meta.annotation.MayContainNull;
import com.igormaznitsa.meta.annotation.Warning;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.sciareto.ui.UiUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

@Warning("It is accessible through Class.forName(), don't rename it!")
public final class MacOSXAppHandlerOld implements ApplicationListener, Platform {

  private final Logger LOGGER = LoggerFactory.getLogger(MacOSXAppHandlerOld.class);
  private final Map<PlatformMenuEvent, PlatformMenuAction> actions = Collections.synchronizedMap(new EnumMap<PlatformMenuEvent, PlatformMenuAction>(PlatformMenuEvent.class));
  private final Application application;

  public MacOSXAppHandlerOld(@Nonnull final Application application) {
    this.application = application;
    this.application.addApplicationListener(this);
    this.application.setDockIconBadge("SciaReto");
    this.application.setDockIconImage(UiUtils.loadIcon("logo256x256.png"));
  }

  private boolean processMenuEvent(@Nonnull final PlatformMenuEvent event, @Nullable @MayContainNull final Object... args) {
    final PlatformMenuAction action = this.actions.get(event);
    boolean handled = false;
    if (action == null) {
      LOGGER.info("No registered menu event handler : " + event);//NOI18N
    } else {
      handled = action.doPlatformMenuAction(event, args);
      LOGGER.info("Processed menu event : " + event); //NOI18N
    }
    return handled;
  }

  @Override
  public void init() {
  }

  @Override
  public void dispose() {
  }

  @Nonnull
  @Override
  public String getDefaultLFClassName() {
    return "";
  }

  @Override
  public boolean registerPlatformMenuEvent(@Nonnull final PlatformMenuEvent event, @Nonnull final PlatformMenuAction action) {
    this.actions.put(event, Assertions.assertNotNull(action));

    switch (event) {
      case ABOUT: {
        this.application.setEnabledAboutMenu(true);
      }
      break;
      case PREFERENCES: {
        this.application.setEnabledPreferencesMenu(true);
      }
      break;
    }

    return true;
  }

  @Nonnull
  @Override
  public String getName() {
    return "";
  }

  @Override
  public void handleAbout(@Nonnull final ApplicationEvent ae) {
    ae.setHandled(processMenuEvent(PlatformMenuEvent.ABOUT));
  }

  @Override
  public void handleOpenApplication(@Nonnull final ApplicationEvent ae) {
  }

  @Override
  public void handleOpenFile(@Nonnull final ApplicationEvent ae) {
    ae.setHandled(processMenuEvent(PlatformMenuEvent.OPEN_FILE, ae.getFilename()));
  }

  @Override
  public void handlePreferences(@Nonnull final ApplicationEvent ae) {
    ae.setHandled(processMenuEvent(PlatformMenuEvent.PREFERENCES));
  }

  @Override
  public void handlePrintFile(@Nonnull final ApplicationEvent ae) {
    ae.setHandled(processMenuEvent(PlatformMenuEvent.PRINT_FILE, ae.getFilename()));
  }

  @Override
  public void handleQuit(@Nonnull final ApplicationEvent ae) {
    ae.setHandled(processMenuEvent(PlatformMenuEvent.QUIT));
  }

  @Override
  public void handleReOpenApplication(@Nonnull final ApplicationEvent ae) {
    ae.setHandled(processMenuEvent(PlatformMenuEvent.REOPEN_APPLICATION));
  }

}
