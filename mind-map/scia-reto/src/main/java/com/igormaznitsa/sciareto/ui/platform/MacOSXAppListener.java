package com.igormaznitsa.sciareto.ui.platform;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationListener;
import com.igormaznitsa.meta.annotation.MayContainNull;
import com.igormaznitsa.meta.annotation.Warning;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

@Warning("It is accessible through Class.forName(), don't rename it!")
public final class MacOSXAppListener implements ApplicationListener, Platform {

  private final Logger LOGGER = LoggerFactory.getLogger(MacOSXAppListener.class);
  private final Map<PlatformMenuEvent, PlatformMenuAction> actions = Collections.synchronizedMap(new EnumMap<PlatformMenuEvent, PlatformMenuAction>(PlatformMenuEvent.class));
  private final Application application;

  public MacOSXAppListener(@Nonnull final Application application) {
    this.application = application;
    application.addApplicationListener(this);
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
