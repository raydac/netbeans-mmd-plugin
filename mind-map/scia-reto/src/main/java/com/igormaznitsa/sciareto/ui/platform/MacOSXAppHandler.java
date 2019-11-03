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

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent;
import com.apple.eawt.Application;
import com.apple.eawt.OpenFilesHandler;
import com.apple.eawt.PreferencesHandler;
import com.apple.eawt.PrintFilesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
import com.igormaznitsa.meta.annotation.MayContainNull;
import com.igormaznitsa.meta.annotation.Warning;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.sciareto.Main;
import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Warning("It is accessible through Class.forName(), don't rename it!")
public final class MacOSXAppHandler implements Platform {

  private final Logger LOGGER = LoggerFactory.getLogger(MacOSXAppHandler.class);
  private final Map<PlatformMenuEvent, PlatformMenuAction> actions = Collections.synchronizedMap(new EnumMap<PlatformMenuEvent, PlatformMenuAction>(PlatformMenuEvent.class));
  private final Application application;

  public MacOSXAppHandler(@Nonnull final Application application) {
    this.application = application;
    this.application.setDockIconBadge(Main.APP_TITLE);
    this.application.setDockIconImage(Main.APP_ICON);
    
    this.application.setAboutHandler(new AboutHandler() {
        @Override
        public void handleAbout(AppEvent.AboutEvent ae) {
            processMenuEvent(PlatformMenuEvent.ABOUT);
        }
    });

    this.application.setPreferencesHandler(new PreferencesHandler() {
        @Override
        public void handlePreferences(AppEvent.PreferencesEvent pe) {
            processMenuEvent(PlatformMenuEvent.PREFERENCES);
        }
    });

    this.application.setOpenFileHandler(new OpenFilesHandler() {
        @Override
        public void openFiles(AppEvent.OpenFilesEvent ofe) {
            final List<File> files = ofe.getFiles();
            if (files!=null) {
                for(final File f : files) {
                    processMenuEvent(PlatformMenuEvent.OPEN_FILE, f.getAbsolutePath());
                }
            }
        }
    });
    
    this.application.setPrintFileHandler(new PrintFilesHandler() {
        @Override
        public void printFiles(AppEvent.PrintFilesEvent pfe) {
            final List<File> files = pfe.getFiles();
            if (files != null) {
                for (final File f : files) {
                    processMenuEvent(PlatformMenuEvent.PRINT_FILE, f.getAbsolutePath());
                }
            }
        }
    });

    this.application.setQuitHandler(new QuitHandler() {
        @Override
        public void handleQuitRequestWith(AppEvent.QuitEvent qe, QuitResponse qr) {
            if (processMenuEvent(PlatformMenuEvent.QUIT)){
                qr.performQuit();
            }
        }
    });
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
    return true;
  }

  @Nonnull
  @Override
  public String getName() {
    return "";
  }

}
