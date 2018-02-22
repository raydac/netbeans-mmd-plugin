/*
 * Copyright 2018 Igor Maznitsa.
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
    application.setAboutHandler(new AboutHandler() {
        @Override
        public void handleAbout(AppEvent.AboutEvent ae) {
            processMenuEvent(PlatformMenuEvent.ABOUT);
        }
    });

    application.setPreferencesHandler(new PreferencesHandler() {
        @Override
        public void handlePreferences(AppEvent.PreferencesEvent pe) {
            processMenuEvent(PlatformMenuEvent.PREFERENCES);
        }
    });

    application.setOpenFileHandler(new OpenFilesHandler() {
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
    
    application.setPrintFileHandler(new PrintFilesHandler() {
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

    application.setQuitHandler(new QuitHandler() {
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
