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

package com.igormaznitsa.sciareto;

import static java.lang.System.currentTimeMillis;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatArcDarkOrangeIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatArcIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatArcOrangeIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatCarbonIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatCobalt2IJTheme;
import com.formdev.flatlaf.intellijthemes.FlatCyanLightIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatDraculaIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatMaterialDesignDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatSolarizedDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatArcDarkContrastIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneDarkContrastIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneLightContrastIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneLightIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatGitHubDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatGitHubIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDarkerIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDeepOceanContrastIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialPalenightIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatSolarizedLightIJTheme;
import com.igormaznitsa.commons.version.Version;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.MindMapPluginRegistry;
import com.igormaznitsa.mindmap.plugins.api.AbstractExporter;
import com.igormaznitsa.mindmap.plugins.api.AbstractImporter;
import com.igormaznitsa.mindmap.plugins.api.ExternallyExecutedPlugin;
import com.igormaznitsa.mindmap.plugins.api.HasMnemonic;
import com.igormaznitsa.mindmap.plugins.api.HasOptions;
import com.igormaznitsa.mindmap.plugins.api.MindMapPlugin;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.plugins.external.ExternalPlugins;
import com.igormaznitsa.mindmap.plugins.misc.OptionsPlugin;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelController;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractElement;
import com.igormaznitsa.mindmap.swing.panel.ui.ElementPart;
import com.igormaznitsa.mindmap.swing.panel.utils.PropertiesPreferences;
import com.igormaznitsa.sciareto.metrics.MetricsService;
import com.igormaznitsa.sciareto.notifications.MessagesService;
import com.igormaznitsa.sciareto.plugins.services.PrinterPlugin;
import com.igormaznitsa.sciareto.preferences.PreferencesManager;
import com.igormaznitsa.sciareto.ui.MainFrame;
import com.igormaznitsa.sciareto.ui.SystemUtils;
import com.igormaznitsa.sciareto.ui.UiUtils;
import com.igormaznitsa.sciareto.ui.UiUtils.SplashScreen;
import com.igormaznitsa.sciareto.ui.misc.JHtmlLabel;
import com.igormaznitsa.sciareto.ui.platform.PlatformProvider;
import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class SciaRetoStarter {

  public static final String APP_TITLE = "Scia Reto";
  public static final Image APP_ICON = UiUtils.loadIcon("logo256x256.png");
  public static final long UPSTART = currentTimeMillis();
  public static final Version IDE_VERSION = new Version("sciareto", new long[] {1L, 6L, 0L}, null);
  public static final Random RND = new Random();
  public static final String PROPERTY_LOOKANDFEEL = "selected.look.and.feel"; //NOI18N
  public static final String PROPERTY_SCALE_GUI = "general.gui.scale"; //NOI18N
  //NOI18N
  public static final String PROPERTY_TOTAL_UPSTART = "time.total.upstart"; //NOI18N
  private static final AtomicReference<SplashScreen> splash = new AtomicReference<>();
  private static final Logger LOGGER = LoggerFactory.getLogger(SciaRetoStarter.class);
  private static final String PROPERTY = "nbmmd.plugin.folder"; //NOI18N
  private static final long STATISTICS_DELAY = 7L * 24L * 3600L * 1000L;
  private static MainFrame MAIN_FRAME;

  public static void disposeSplash() {
    final SplashScreen splashScr = splash.getAndSet(null);
    if (splashScr != null) {
      if (SwingUtilities.isEventDispatchThread()) {
        splashScr.dispose();
      } else {
        SwingUtilities.invokeLater(splashScr::dispose);
      }
    }
  }

  @Nonnull
  public static MainFrame getApplicationFrame() {
    return MAIN_FRAME;
  }

  private static void loadPlugins() {
    final String pluginFolder = System.getProperty(PROPERTY);
    if (pluginFolder != null) {
      final File folder = new File(pluginFolder);
      if (folder.isDirectory()) {
        LOGGER.info("Loading plugins from folder : " + folder); //NOI18N
        new ExternalPlugins(folder).init();
      } else {
        LOGGER.error("Can't find plugin folder : " + folder); //NOI18N
      }
    } else {
      LOGGER.info("Property " + PROPERTY + " is not defined"); //NOI18N
    }
  }

  private static boolean trySetTaskBarValues() {
    try {
      final Object taskbar = Class.forName("java.awt.Taskbar").getMethod("getTaskbar").invoke(null);
      try {
        taskbar.getClass().getMethod("setIconImage", Image.class).invoke(taskbar, APP_ICON);
      } catch (InvocationTargetException ex) {
        LOGGER.warn("Can't set icon through Taskbar: " + ex.getCause());
      }
      return true;
    } catch (InvocationTargetException exx) {
      LOGGER.error("trySetTaskBarValues: " + exx.getCause().toString());
      return false;
    } catch (Exception exx) {
      LOGGER.error("trySetTaskBarValues: " + exx.toString());
      return false;
    }
  }

  @Nullable
  private static GraphicsConfiguration findPrimaryScreen() {
    final GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsConfiguration result = null;
    if (environment != null) {
      result = environment.getDefaultScreenDevice().getDefaultConfiguration();
    }
    return result;
  }

  @Nonnull
  private static Optional<LookAndFeelInfo> findLookAndFeelForClassName(
      @Nullable final String className) {
    if (className == null) {
      return Optional.empty();
    }
    return Stream.of(UIManager.getInstalledLookAndFeels())
        .filter(x -> className.equals(x.getClassName()))
        .findFirst();
  }

  public static void main(@Nonnull @MustNotContainNull final String... args) {
    final GraphicsConfiguration primaryScreen = findPrimaryScreen();

    // -- Properties for MAC OSX --
    System.setProperty("apple.awt.fileDialogForDirectories", "true"); //NOI18N
    System.setProperty("apple.laf.useScreenMenuBar", "true"); //NOI18N
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "SciaReto"); //NOI18N
    // ----------------------------

    if (trySetTaskBarValues()) {
      LOGGER.info("Taskbar values set completed");
    } else {
      LOGGER.warn("Taskbar values set failed");
    }

    SystemUtils.setDebugLevelForJavaLogger(Level.WARNING);

    final double screenScale =
        UiUtils.findDeviceScale(primaryScreen == null ? null : primaryScreen.getDevice());

    PlatformProvider.getPlatform().init();
    final boolean foundScaling = Math.abs(screenScale - 1.0d) >
        0.00001d;

    if (foundScaling) {
      LOGGER.info("Detected screen scale: " + screenScale);
    }

    final String selectedLookAndFeel = PreferencesManager.getInstance().getPreferences()
        .get(PROPERTY_LOOKANDFEEL,
            !IS_OS_WINDOWS || !foundScaling ? PlatformProvider.getPlatform().getDefaultLFClassName()
                : UIManager.getCrossPlatformLookAndFeelClassName());

    LOGGER.info("java.vendor = " + System.getProperty("java.vendor", "unknown")); //NOI18N
    LOGGER.info("java.version = " + System.getProperty("java.version", "unknown")); //NOI18N
    LOGGER.info("os.name = " + System.getProperty("os.name", "unknown")); //NOI18N
    LOGGER.info("os.arch = " + System.getProperty("os.arch", "unknown")); //NOI18N
    LOGGER.info("os.version = " + System.getProperty("os.version", "unknown")); //NOI18N

    final long timeTakenBySplashStart;

    if (args.length == 0) {
      final long splashTimerStart = currentTimeMillis();
      final CountDownLatch latch = new CountDownLatch(1);
      try {
        SwingUtilities.invokeLater(() -> {
          try {
            final Image splashImage = Assertions.assertNotNull(UiUtils.loadIcon("splash.png")); //NOI18N
            final SplashScreen splashScreen =new SplashScreen(primaryScreen, splashImage);
            splashScreen.addWindowListener(new WindowAdapter() {
              @Override
              public void windowActivated(WindowEvent e) {
                latch.countDown();
              }
            });
            splash.set(splashScreen);
            splashScreen.setVisible(true);
            splashScreen.repaint();
          } catch (Exception ex) {
            LOGGER.error("Splash can't be shown", ex); //NOI18N
            if (splash.get() != null) {
              splash.get().dispose();
              splash.set(null);
            }
          }
        });
      } catch (final Exception ex) {
        LOGGER.error("Error during splash processing", ex); //NOI18N
      }

      try {
        if (!latch.await(10, TimeUnit.SECONDS)){
          LOGGER.warn("Splash latch as not decremented!");
        }
      }catch (InterruptedException ex){
        Thread.currentThread().interrupt();
        return;
      }

      timeTakenBySplashStart = currentTimeMillis() - splashTimerStart;
    } else {
      timeTakenBySplashStart = 0L;
    }

    if ((currentTimeMillis() - PreferencesManager.getInstance().getPreferences()
        .getLong(MetricsService.PROPERTY_METRICS_SENDING_LAST_TIME,
            currentTimeMillis() + STATISTICS_DELAY)) >= STATISTICS_DELAY) {
      LOGGER.info("Statistics scheduled"); //NOI18N

      final Timer timer = new Timer(45000, e -> MetricsService.getInstance().sendStatistics());
      timer.setRepeats(false);
      timer.start();
    }

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        final Preferences prefs = PreferencesManager.getInstance().getPreferences();
        prefs.putLong(PROPERTY_TOTAL_UPSTART,
            prefs.getLong(PROPERTY_TOTAL_UPSTART, 0L) + (currentTimeMillis() - UPSTART));
        PreferencesManager.getInstance().flush();
      } finally {
        PlatformProvider.getPlatform().dispose();
      }
    }));

    loadPlugins();

    boolean doShowGUI = true;

    if (args.length > 0) {
      if ("--help".equalsIgnoreCase(args[0]) || "--?".equals(args[0])) { //NOI18N
        doShowGUI = false;

        MindMapPluginRegistry.getInstance().registerPlugin(new LocalMMDExporter());
        MindMapPluginRegistry.getInstance().registerPlugin(new LocalMMDImporter());

        printCliHelp(System.out);
        System.exit(0);
      } else if ("--importsettings".equalsIgnoreCase(args[0])) { //NOI18N
        doShowGUI = false;
        final File file = args.length > 1 ? new File(args[1]) : null;

        if (file == null) {
          LOGGER.error("Settings file not provided"); //NOI18N
          System.exit(1);
        }
        if (!importSettings(file)) {
          System.exit(1);
        }
      } else if ("--exportsettings".equalsIgnoreCase(args[0])) { //NOI18N
        doShowGUI = false;

        final File file =
            args.length > 1 ? new File(args[1]) : new File("sciaretosettings.properties"); //NOI18N

        if (!exportSettings(file)) {
          System.exit(1);
        }
      } else if ("--convert".equalsIgnoreCase(args[0])) { //NOI18N
        doShowGUI = false;
        if (!convertData(args)) {
          LOGGER.error("Conversion failed for error"); //NOI18N
          printConversionHelp(System.out);
          System.exit(1);
        }
      }
    }

    if (doShowGUI) {
      SwingUtilities.invokeLater(() -> {
        registerExtraLF();
        findLookAndFeelForClassName(selectedLookAndFeel)
            .ifPresent(info -> {
              try {
                UIManager.setLookAndFeel(info.getClassName());
              } catch (Exception ex) {
                LOGGER.error("Can't set L&F", ex); //NOI18N
              }
            });
      });

      SystemUtils.setDebugLevelForJavaLogger(Level.INFO);

      MindMapPluginRegistry.getInstance().registerPlugin(new PrinterPlugin());
      MindMapPluginRegistry.getInstance().unregisterPluginForClass(OptionsPlugin.class);

      SwingUtilities.invokeLater(() -> {
        final GraphicsDevice gd =
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        final int width = gd.getDisplayMode().getWidth();
        final int height = gd.getDisplayMode().getHeight();

        if (PlatformProvider.isErrorDetected()) {
          disposeSplash();
          JOptionPane.showMessageDialog(null,
              "Can't init the Platform dependent layer, the default one will be used instead.\n"
                  + "Check that you have installed Java correctly to avoid the warning", "Warning",
              JOptionPane.WARNING_MESSAGE);
        }

        try {
          MAIN_FRAME = new MainFrame(primaryScreen, args);
        } catch (IOException ex) {
          LOGGER.error("Can't create frame", ex);
          JOptionPane.showMessageDialog(null, "Can't create frame : " + ex.getMessage(), "Error",
              JOptionPane.ERROR_MESSAGE);
          System.exit(1);
        }
        MAIN_FRAME.setSize(Math.round(width * 0.75f), Math.round(height * 0.75f));

        if (splash.get() != null) {
          final long delay =
              (2000L + timeTakenBySplashStart) - (currentTimeMillis() - UPSTART);
          if (delay > 0L) {
            final Timer timer = new Timer((int) delay, e -> {
              disposeSplash();
            });
            timer.setRepeats(false);
            timer.start();
          } else {
            disposeSplash();
          }
        }

        MAIN_FRAME.setVisible(true);

        MAIN_FRAME.setExtendedState(MAIN_FRAME.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        final JHtmlLabel label = new JHtmlLabel(
            "<html>You use the application already for some time. If you like it then you could support its author and <a href=\"http://www.google.com\"><b>make some donation</b></a>.</html>");
        label.addLinkListener((source, link) -> {
          try {
            UiUtils.browseURI(new URI(link), false);
          } catch (URISyntaxException ex) {
            LOGGER.error("Can't make URI", ex); //NOI18N
          }
        });
      });

      new MessagesService().execute();
    }
  }

  private static void registerExtraLF() {
    FlatLightLaf.installLafInfo();
    FlatDarculaLaf.installLafInfo();

    FlatArcOrangeIJTheme.installLafInfo();
    FlatArcDarkContrastIJTheme.installLafInfo();
    FlatArcDarkIJTheme.installLafInfo();
    FlatArcIJTheme.installLafInfo();
    FlatArcDarkOrangeIJTheme.installLafInfo();

    FlatAtomOneDarkIJTheme.installLafInfo();
    FlatAtomOneLightContrastIJTheme.installLafInfo();
    FlatAtomOneDarkContrastIJTheme.installLafInfo();
    FlatAtomOneLightIJTheme.installLafInfo();

    FlatMaterialDarkerIJTheme.installLafInfo();
    FlatMaterialPalenightIJTheme.installLafInfo();
    FlatMaterialDesignDarkIJTheme.installLafInfo();
    FlatMaterialDeepOceanContrastIJTheme.installLafInfo();

    FlatIntelliJLaf.installLafInfo();
    FlatCarbonIJTheme.installLafInfo();
    FlatCobalt2IJTheme.installLafInfo();
    FlatCyanLightIJTheme.installLafInfo();
    FlatDraculaIJTheme.installLafInfo();

    FlatSolarizedLightIJTheme.installLafInfo();
    FlatSolarizedDarkIJTheme.installLafInfo();

    FlatGitHubIJTheme.installLafInfo();
    FlatGitHubDarkIJTheme.installLafInfo();
  }

  private static boolean convertData(@Nonnull @MustNotContainNull final String[] args) {
    MindMapPluginRegistry.getInstance().registerPlugin(new LocalMMDExporter());
    MindMapPluginRegistry.getInstance().registerPlugin(new LocalMMDImporter());

    final String[] params = new String[5];

    final Properties options = new Properties();

    final int IN_FILE = 0;
    final int OUT_FILE = 1;
    final int IN_TYPE = 2;
    final int OUT_TYPE = 3;
    final int SETTINGS = 4;
    final int OPTION = 5;

    params[IN_TYPE] = "mmd"; //NOI18N
    params[OUT_TYPE] = "mmd"; //NOI18N
    params[SETTINGS] = ""; //NOI18N

    int detected = -1;

    boolean allOk = true;
    for (int i = 1; i < args.length; i++) {
      if (detected >= 0) {
        if (detected == OPTION) {
          final String text = args[i];
          final String[] splitted = text.split("\\="); //NOI18N
          if (splitted.length < 2) {
            options.put(splitted[0], "true"); //NOI18N
          } else {
            options.put(splitted[0], splitted[1]);
          }
        } else {
          params[detected] = args[i];
        }
        detected = -1;
      } else {
        if ("--in".equalsIgnoreCase(args[i])) { //NOI18N
          detected = IN_FILE;
        } else if ("--out".equalsIgnoreCase(args[i])) { //NOI18N
          detected = OUT_FILE;
        } else if ("--from".equalsIgnoreCase(args[i])) { //NOI18N
          detected = IN_TYPE;
        } else if ("--to".equalsIgnoreCase(args[i])) { //NOI18N
          detected = OUT_TYPE;
        } else if ("--settings".equalsIgnoreCase(args[i])) { //NOI18N
          detected = SETTINGS;
        } else if ("--option".equalsIgnoreCase(args[i])) { //NOI18N
          detected = OPTION;
        } else {
          LOGGER.error("Unexpected argument : " + args[i]); //NOI18N
          allOk = false;
          break;
        }
      }
    }

    if (allOk) {
      for (final String s : params) {
        if (s == null) {
          LOGGER.error("Not provided required parameter"); //NOI18N
          allOk = true;
          break;
        }
      }

      if (allOk) {
        final File inFile = new File(params[IN_FILE]);
        final File outFile = new File(params[OUT_FILE]);
        final AbstractImporter importer =
            MindMapPluginRegistry.getInstance().findImporterForMnemonic(params[IN_TYPE]);
        final AbstractExporter exporter =
            MindMapPluginRegistry.getInstance().findExporterForMnemonic(params[OUT_TYPE]);

        if (importer == null) {
          LOGGER.error("Unknown importer : " + params[IN_TYPE]); //NOI18N
          allOk = false;
        } else if (exporter == null) {
          LOGGER.error("Unknown exporter : " + params[OUT_TYPE]); //NOI18N
          allOk = false;
        }

        if (allOk) {
          final File settingsFile = params[SETTINGS].isEmpty() ? null : new File(params[SETTINGS]);
          final MindMapPanelConfig config = new MindMapPanelConfig();
          if (settingsFile != null) {
            try {
              config.loadFrom(
                  new PropertiesPreferences(FileUtils.readFileToString(settingsFile, "UTF-8")));
            } catch (IOException ex) {
              LOGGER.error("Can't load settings file : " + settingsFile, ex); //NOI18N
              allOk = false;
            }
          }
          if (allOk && importer != null && exporter != null) {
            try {
              makeConversion(inFile, importer, outFile, exporter, config, options);
            } catch (final Exception ex) {
              if (ex instanceof IllegalArgumentException) {
                LOGGER.error(ex.getMessage());
              } else {
                LOGGER.error("Unexpected error during conversion", ex); //NOI18N
              }
              allOk = false;
            }
          }
        }
      }
    }

    return allOk;
  }

  private static boolean exportSettings(@Nonnull final File settingsFile) {
    boolean result = true;

    final MindMapPanelConfig config = new MindMapPanelConfig();
    config.loadFrom(PreferencesManager.getInstance().getPreferences());

    final PropertiesPreferences prefs = new PropertiesPreferences(
        "Exported configuration for SciaReto editor https://github.com/raydac/netbeans-mmd-plugin"); //NOI18N
    config.saveTo(prefs);

    try {
      FileUtils.write(settingsFile, prefs.toString(), "UTF-8");
    } catch (final Exception ex) {
      LOGGER.error("Can't export settings for error", ex); //NOI18N
      result = false;
    }

    return result;
  }

  private static boolean importSettings(@Nonnull final File settingsFile) {
    boolean result = true;
    try {
      final PropertiesPreferences prefs =
          new PropertiesPreferences(FileUtils.readFileToString(settingsFile, "UTF-8"));
      final MindMapPanelConfig config = new MindMapPanelConfig();
      config.loadFrom(prefs);
      config.saveTo(PreferencesManager.getInstance().getPreferences());
      PreferencesManager.getInstance().flush();
      LOGGER.info("Settings imported from file : " + settingsFile); //NOI18N
    } catch (final Exception ex) {
      LOGGER.error("Error during import from file : " + settingsFile, ex); //NOI18N
      result = false;
    }
    return result;
  }

  private static void makeConversion(@Nonnull final File from,
                                     @Nonnull final AbstractImporter fromFormat,
                                     @Nonnull final File to,
                                     @Nonnull final AbstractExporter toFormat,
                                     @Nonnull final MindMapPanelConfig config,
                                     @Nonnull final Properties options) throws Exception {
    final AtomicReference<Exception> error = new AtomicReference<>();
    SwingUtilities.invokeAndWait(new Runnable() {
      @Override
      public void run() {
        try {
          final DialogProvider dialog = new DialogProvider() {
            @Override
            public void msgError(@Nullable final Component parentComponent,
                                 @Nonnull final String text) {
              LOGGER.error(text);
            }

            @Override
            public void msgInfo(@Nullable final Component parentComponent,
                                @Nonnull final String text) {
              LOGGER.info(text);
            }

            @Override
            public void msgWarn(@Nullable final Component parentComponent,
                                @Nonnull final String text) {
              LOGGER.warn(text);
            }

            @Override
            public boolean msgConfirmOkCancel(@Nullable final Component parentComponent,
                                              @Nonnull final String title,
                                              @Nonnull final String question) {
              throw new UnsupportedOperationException("Not supported yet."); //NOI18N
            }

            @Override
            public boolean msgOkCancel(@Nullable final Component parentComponent,
                                       @Nonnull final String title,
                                       @Nonnull final JComponent component) {
              throw new UnsupportedOperationException("Not supported yet."); //NOI18N
            }

            @Override
            public boolean msgConfirmYesNo(@Nullable final Component parentComponent,
                                           @Nonnull final String title,
                                           @Nonnull final String question) {
              throw new UnsupportedOperationException("Not supported yet."); //NOI18N
            }

            @Override
            public Boolean msgConfirmYesNoCancel(@Nullable final Component parentComponent,
                                                 @Nonnull final String title,
                                                 @Nonnull final String question) {
              throw new UnsupportedOperationException("Not supported yet."); //NOI18N
            }

            @Override
            public File msgSaveFileDialog(@Nullable final Component parentComponent,
                                          @Nullable final PluginContext pluginContext,
                                          @Nonnull final String id, @Nonnull final String title,
                                          @Nullable final File defaultFolder,
                                          final boolean filesOnly, @Nonnull @MustNotContainNull
                                          final FileFilter[] fileFilter,
                                          @Nonnull final String approveButtonText) {
              return to;
            }

            @Override
            public File msgOpenFileDialog(@Nullable final Component parentComponent,
                                          @Nullable final PluginContext pluginContext,
                                          @Nonnull final String id, @Nonnull final String title,
                                          @Nullable final File defaultFolder,
                                          final boolean filesOnly, @Nonnull @MustNotContainNull
                                          final FileFilter[] fileFilter,
                                          @Nonnull final String approveButtonText) {
              return from;
            }
          };

          final MindMapPanel panel = new MindMapPanel(new MindMapPanelController() {

            @Override
            public boolean canTopicBeDeleted(@Nonnull MindMapPanel source, @Nonnull Topic topic) {
              return true;
            }

            @Nonnull
            @Override
            public PluginContext makePluginContext(@Nonnull MindMapPanel source) {
              return new PluginContext() {
                @Nonnull
                @Override
                public MindMapPanelConfig getPanelConfig() {
                  return config;
                }

                @Nonnull
                @Override
                public MindMapPanel getPanel() {
                  return source;
                }

                @Nonnull
                @Override
                public DialogProvider getDialogProvider() {
                  return dialog;
                }

                @Nullable
                @Override
                public File getMindMapFile() {
                  return from;
                }

                @Override
                public void openFile(@Nonnull File file, boolean preferSystemBrowser) {

                }

                @Nullable
                @Override
                public File getProjectFolder() {
                  return new File(".");
                }

                @Nullable
                @Override
                public Topic[] getSelectedTopics() {
                  return new Topic[0];
                }

                @Override
                public void processPluginActivation(@Nonnull ExternallyExecutedPlugin plugin,
                                                    @Nullable Topic activeTopic) {

                }
              };
            }

            @Override
            public boolean isUnfoldCollapsedTopicDropTarget(@Nonnull final MindMapPanel source) {
              return false;
            }

            @Override
            public boolean isCopyColorInfoFromParentToNewChildAllowed(
                @Nonnull final MindMapPanel source) {
              return false;
            }

            @Override
            public boolean isTrimTopicTextBeforeSet(@Nonnull final MindMapPanel source) {
              return false;
            }

            @Override
            public boolean isSelectionAllowed(@Nonnull final MindMapPanel source) {
              return false;
            }

            @Override
            public boolean isElementDragAllowed(@Nonnull final MindMapPanel source) {
              return false;
            }

            @Override
            public boolean isMouseMoveProcessingAllowed(@Nonnull final MindMapPanel source) {
              return false;
            }

            @Override
            public boolean isMouseWheelProcessingAllowed(@Nonnull final MindMapPanel source) {
              return false;
            }

            @Override
            public boolean isMouseClickProcessingAllowed(@Nonnull final MindMapPanel source) {
              return false;
            }

            @Override
            @Nonnull
            public MindMapPanelConfig provideConfigForMindMapPanel(
                @Nonnull final MindMapPanel source) {
              return config;
            }

            @Override
            @Nullable
            public JPopupMenu makePopUpForMindMapPanel(@Nonnull final MindMapPanel source,
                                                       @Nonnull final Point point, @Nullable
                                                       final AbstractElement elementUnderMouse,
                                                       @Nullable
                                                       final ElementPart elementPartUnderMouse) {
              return null;
            }

            @Override
            @Nonnull
            public DialogProvider getDialogProvider(@Nonnull final MindMapPanel source) {
              return dialog;
            }

            @Override
            public boolean processDropTopicToAnotherTopic(@Nonnull final MindMapPanel source,
                                                          @Nonnull final Point dropPoint,
                                                          @Nonnull final Topic draggedTopic,
                                                          @Nonnull final Topic destinationTopic) {
              return false;
            }

          });

          MindMap map = new MindMap(false);
          panel.setModel(map);

          map = fromFormat.doImport(panel.getController().makePluginContext(panel));
          if (map != null) {
            panel.setModel(map);
          } else {
            dialog.msgError(MAIN_FRAME, "Can't import map");
          }

          final JComponent optionsComponent =
              toFormat.makeOptions(panel.getController().makePluginContext(panel));

          if (!options.isEmpty()) {
            if (optionsComponent instanceof HasOptions) {
              final HasOptions optionable = (HasOptions) optionsComponent;
              for (final String k : options.stringPropertyNames()) {
                if (optionable.doesSupportKey(k)) {
                  optionable.setOption(k, options.getProperty(k));
                } else {
                  throw new IllegalArgumentException(
                      "Exporter " + toFormat.getMnemonic() + " doesn't support option '" + k +
                          "\', it provides options " +
                          Arrays.toString(optionable.getOptionKeys())); //NOI18N
                }
              }
            } else {
              throw new IllegalArgumentException(
                  "Exporter " + toFormat.getMnemonic() + " doesn't support options"); //NOI18N
            }
          }

          final FileOutputStream result = new FileOutputStream(to, false);
          try {
            toFormat
                .doExport(panel.getController().makePluginContext(panel), optionsComponent, result);
            result.flush();
          } finally {
            IOUtils.closeQuietly(result);
          }
        } catch (Exception ex) {
          error.set(ex);
        }
      }
    });
    if (error.get() != null) {
      throw error.get();
    }
  }

  @Nonnull
  private static String makeMnemonicList(
      @Nonnull @MustNotContainNull final List<? extends MindMapPlugin> plugins) {
    final StringBuilder result = new StringBuilder();
    for (final MindMapPlugin p : plugins) {
      if (p instanceof HasMnemonic) {
        final String mnemo = ((HasMnemonic) p).getMnemonic();
        if (mnemo != null) {
          if (result.length() > 0) {
            result.append('|');
          }
          result.append(mnemo);
        }
      }
    }
    return result.toString();
  }

  private static void printCliHelp(@Nonnull final PrintStream out) {
    out.println(IDE_VERSION.toString());
    out.println("Project page : https://github.com/raydac/netbeans-mmd-plugin"); //NOI18N
    out.println();
    out.println("Usage from command line:"); //NOI18N
    out.println(
        "   java -jar sciareto.jar [--help|--importsettings FILE|--exportsettings FILE|--convert <>]|[FILE FILE ... FILE]"); //NOI18N
    out.println();
    printConversionHelp(out);
  }

  private static void printConversionHelp(@Nonnull final PrintStream out) {
    final String allowedFormatsFrom =
        makeMnemonicList(MindMapPluginRegistry.getInstance().findFor(AbstractImporter.class));
    final String allowedFormatsTo =
        makeMnemonicList(MindMapPluginRegistry.getInstance().findFor(AbstractExporter.class));
    out.println();
    out.println("Usage in converter mode:"); //NOI18N
    out.println(String.format(
        " --convert --in IN_FILE [--from (%s)] --out OUT_FILE [--to (%s)] [--settings FILE] [--option NAME=VALUE...]",
        allowedFormatsFrom, allowedFormatsTo)); //NOI18N
    out.println();
    out.println("   --convert - command to make conversion, must be the first argument"); //NOI18N
    out.println("   --in FILE - file to be converted"); //NOI18N
    out.println("   --from FORMAT - type of source format, be default 'mmd' (allowed " +
        allowedFormatsFrom + ')'); //NOI18N
    out.println("   --out FILE - destination file, if file exists it will be overrided"); //NOI18N
    out.println("   --to FORMAT - type of destination format, bye default 'mmd' (allowed " +
        allowedFormatsTo + ')'); //NOI18N
    out.println("   --settings FILE - use graphic settings defined in Java property file"); //NOI18N
    out.println(
        "   --option NAME=VALUE - an option to tune export process, specific for each exporter, see documentation"); //NOI18N
    out.println();
  }

  private static final class LocalMMDImporter extends AbstractImporter {

    @Nullable
    @Override
    public MindMap doImport(@Nonnull PluginContext context) throws Exception {
      final File fileToImport = context.getDialogProvider()
          .msgOpenFileDialog(null, context, "", "", null, true, new FileFilter[0], ""); //NOI18N
      return new MindMap(
          new StringReader(FileUtils.readFileToString(fileToImport, "UTF-8"))); //NOI18N
    }

    @Nonnull
    @Override
    public String getName(@Nonnull PluginContext context) {
      return "MMDImporter"; //NOI18N
    }

    @Nonnull
    @Override
    public String getReference(@Nonnull PluginContext context) {
      return "MMDImporter"; //NOI18N
    }

    @Override
    public String getMnemonic() {
      return "mmd"; //NOI18N
    }

    @Nonnull
    @Override
    public Icon getIcon(@Nonnull PluginContext context) {
      return new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_BYTE_INDEXED));
    }

    @Override
    public int getOrder() {
      return 0;
    }

  }

  private static final class LocalMMDExporter extends AbstractExporter {

    @Override
    public void doExport(@Nonnull PluginContext context, @Nullable JComponent options,
                         @Nullable OutputStream out) throws IOException {
      final MindMap map = context.getPanel().getModel();
      IOUtils.write(map.write(new StringWriter()).toString(), out, "UTF-8"); //NOI18N
    }

    @Override
    public void doExportToClipboard(@Nonnull PluginContext context, @Nullable JComponent options)
        throws IOException {
      final MindMap map = context.getPanel().getModel();
      final StringWriter writer = map.write(new StringWriter());
      final String text = writer.toString();
      SwingUtilities.invokeLater(() -> {
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        if (clipboard != null) {
          clipboard.setContents(new StringSelection(text), context.getPanel());
        }
      });
    }

    @Nonnull
    @Override
    public String getName(@Nonnull PluginContext context, @Nullable Topic activeTopic) {
      return "MMDExporter"; //NOI18N
    }

    @Nonnull
    @Override
    public String getReference(@Nonnull PluginContext context, @Nullable Topic activeTopic) {
      return "MMDExporter"; //NOI18N
    }

    @Nonnull
    @Override
    public Icon getIcon(@Nonnull PluginContext context, @Nullable Topic activeTopic) {
      return new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_BYTE_INDEXED));
    }

    @Override
    @Nullable
    public String getMnemonic() {
      return "mmd"; //NOI18N
    }

    @Override
    public int getOrder() {
      return 0;
    }
  }
}
