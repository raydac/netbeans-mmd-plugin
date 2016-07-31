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
package com.igormaznitsa.sciareto;

import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.SplashScreen;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.prefs.Preferences;
import com.igormaznitsa.sciareto.ui.MainFrame;
import javax.annotation.Nonnull;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import com.igormaznitsa.commons.version.Version;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.MindMapPluginRegistry;
import com.igormaznitsa.mindmap.plugins.external.ExternalPlugins;
import com.igormaznitsa.mindmap.plugins.misc.AboutPlugin;
import com.igormaznitsa.mindmap.plugins.misc.OptionsPlugin;
import com.igormaznitsa.sciareto.metrics.MetricsService;
import com.igormaznitsa.sciareto.notifications.MessagesService;
import com.igormaznitsa.sciareto.plugins.PrinterPlugin;
import com.igormaznitsa.sciareto.preferences.PreferencesManager;
import com.igormaznitsa.sciareto.ui.UiUtils;
import com.igormaznitsa.sciareto.ui.misc.JHtmlLabel;
import com.igormaznitsa.sciareto.ui.platform.PlatformProvider;

public class Main {

  public static final long UPSTART = System.currentTimeMillis();

  private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

  private static MainFrame MAIN_FRAME;

  public static final Version IDE_VERSION = new Version("sciareto", new long[]{1L, 0L, 0L}, null);

  public static final Random RND = new Random();

  private static final String PROPERTY = "nbmmd.plugin.folder";
  public static final String PROPERTY_LOOKANDFEEL = "selected.look.and.feel";
  public static final String PROPERTY_TOTAL_UPSTART = "time.total.upstart";

  private static final long STATISTICS_DELAY = 7L * 24L * 3600L * 1000L; 
  
  @Nonnull
  public static MainFrame getApplicationFrame() {
    return MAIN_FRAME;
  }

  private static void loadPlugins() {
    final String pluginFolder = System.getProperty(PROPERTY);
    if (pluginFolder != null) {
      final File folder = new File(pluginFolder);
      if (folder.isDirectory()) {
        LOGGER.info("Loading plugins from folder : " + folder);
        new ExternalPlugins(folder).init();
      } else {
        LOGGER.error("Can't find plugin folder : " + folder);
      }
    } else {
      LOGGER.info("Property " + PROPERTY + " is not defined");
    }
  }

  public static void main(@Nonnull @MustNotContainNull final String... args) {
    PlatformProvider.getPlatform().init();

    final String selectedLookAndFeel = PreferencesManager.getInstance().getPreferences().get(PROPERTY_LOOKANDFEEL, PlatformProvider.getPlatform().getDefaultLFClassName());

    LOGGER.info("java.vendor = "+System.getProperty("java.vendor","unknown"));
    LOGGER.info("java.version = "+System.getProperty("java.version","unknown"));
    LOGGER.info("os.name = "+System.getProperty("os.name","unknown"));
    LOGGER.info("os.arch = "+System.getProperty("os.arch","unknown"));
    LOGGER.info("os.version = "+System.getProperty("os.version","unknown"));
    
    final SplashScreen splash = SplashScreen.getSplashScreen();
    if (splash == null) {
      LOGGER.warn("There is no splash screen");
    } else {
      final Graphics2D gfx = splash.createGraphics();
      gfx.dispose();
      splash.update();
    }

    if ((System.currentTimeMillis() - PreferencesManager.getInstance().getPreferences().getLong(MetricsService.PROPERTY_METRICS_SENDING_LAST_TIME, System.currentTimeMillis()+STATISTICS_DELAY)) >= STATISTICS_DELAY) {
      LOGGER.info("Statistics scheduled");
      
      final Timer timer = new Timer(45000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          MetricsService.getInstance().sendStatistics();
        }
      });
      timer.setRepeats(false);
      timer.start();
    }

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        try{
        final Preferences prefs = PreferencesManager.getInstance().getPreferences();
        prefs.putLong(PROPERTY_TOTAL_UPSTART, prefs.getLong(PROPERTY_TOTAL_UPSTART, 0L) + (System.currentTimeMillis() - UPSTART));
        PreferencesManager.getInstance().flush();
        }finally{
          PlatformProvider.getPlatform().dispose();
        }
      }
    });

    try {
      for (final LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
        if (selectedLookAndFeel.equals(info.getClassName())) {
          UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
    } catch (Exception e) {
      LOGGER.error("Can't set L&F", e);
    }

    loadPlugins();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {

        final GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        final int width = gd.getDisplayMode().getWidth();
        final int height = gd.getDisplayMode().getHeight();

        MindMapPluginRegistry.getInstance().registerPlugin(new PrinterPlugin());
        MindMapPluginRegistry.getInstance().unregisterPluginForClass(AboutPlugin.class);
        MindMapPluginRegistry.getInstance().unregisterPluginForClass(OptionsPlugin.class);
        
        MAIN_FRAME = new MainFrame(args);
        MAIN_FRAME.setSize(Math.round(width * 0.75f), Math.round(height * 0.75f));

        if (args.length == 0 && splash != null) {
          splash.update();
          final long delay = 2000L - (System.currentTimeMillis() - UPSTART);
          if (delay > 0L) {
            try {
              Thread.sleep(delay);
            } catch (InterruptedException ex) {
              return;
            }
          }
        }

        MAIN_FRAME.setVisible(true);

        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            MAIN_FRAME.setExtendedState(MAIN_FRAME.getExtendedState() | JFrame.MAXIMIZED_BOTH);
            final JHtmlLabel label = new JHtmlLabel("<html>You use the application already for some time. If you like it then you could support its author and <a href=\"http://www.google.com\"><b>make some donation</b></a>.</html>");
            label.addLinkListener(new JHtmlLabel.LinkListener() {
              @Override
              public void onLinkActivated(@Nonnull final JHtmlLabel source, @Nonnull final String link) {
                try{
                  UiUtils.browseURI(new URI(link), false);
                }catch(URISyntaxException ex){
                  LOGGER.error("Can't make URI",ex);
                }
              }
            });
 
          }
        });
      }
    });
    
    new MessagesService().execute();
  }
}
