package com.igormaznitsa.sciareto.ui.platform;

import com.igormaznitsa.meta.annotation.MayContainNull;
import com.igormaznitsa.meta.annotation.Warning;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.io.File;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Warning("It is accessible through Class.forName(), don't rename it!")
public class DesktopAppHandler implements Platform {

  private final Logger LOGGER = LoggerFactory.getLogger(DesktopAppHandler.class);

  private final Map<PlatformMenuEvent, PlatformMenuAction> actions = Collections.synchronizedMap(new EnumMap<>(PlatformMenuEvent.class));

  public DesktopAppHandler() {
    try {
      final Desktop desktop = Desktop.getDesktop();

      desktop.setAboutHandler(ae -> processMenuEvent(PlatformMenuEvent.ABOUT));
      desktop.setPreferencesHandler(pe -> processMenuEvent(PlatformMenuEvent.PREFERENCES));

      desktop.setOpenFileHandler(ofe -> {
        final List<File> files = ofe.getFiles();
        if (files != null) {
          for (final File f : files) {
            processMenuEvent(PlatformMenuEvent.OPEN_FILE, f.getAbsolutePath());
          }
        }
      });

      desktop.setPrintFileHandler(pfe -> {
        final List<File> files = pfe.getFiles();
        if (files != null) {
          for (final File f : files) {
            processMenuEvent(PlatformMenuEvent.PRINT_FILE, f.getAbsolutePath());
          }
        }
      });

      desktop.setQuitHandler((qe, qr) -> {
        if (processMenuEvent(PlatformMenuEvent.QUIT)) {
          qr.performQuit();
        }
      });
    }catch (Throwable ex) {
      LOGGER.error("Detected error during platform event handler init", ex);
    }
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
