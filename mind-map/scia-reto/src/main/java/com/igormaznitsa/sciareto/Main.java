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

import java.awt.Component;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.prefs.Preferences;

import com.igormaznitsa.sciareto.ui.MainFrame;

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
import com.igormaznitsa.commons.version.Version;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.MindMapController;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.MindMapPluginRegistry;
import com.igormaznitsa.mindmap.plugins.api.AbstractExporter;
import com.igormaznitsa.mindmap.plugins.api.AbstractImporter;
import com.igormaznitsa.mindmap.plugins.api.HasMnemonic;
import com.igormaznitsa.mindmap.plugins.api.MindMapPlugin;
import com.igormaznitsa.mindmap.plugins.external.ExternalPlugins;
import com.igormaznitsa.mindmap.plugins.misc.AboutPlugin;
import com.igormaznitsa.mindmap.plugins.misc.OptionsPlugin;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelController;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractElement;
import com.igormaznitsa.mindmap.swing.panel.ui.ElementPart;
import com.igormaznitsa.sciareto.metrics.MetricsService;
import com.igormaznitsa.sciareto.notifications.MessagesService;
import com.igormaznitsa.sciareto.plugins.services.PrinterPlugin;
import com.igormaznitsa.sciareto.preferences.PreferencesManager;
import com.igormaznitsa.mindmap.swing.panel.utils.PropertiesPreferences;
import com.igormaznitsa.sciareto.ui.SystemUtils;
import com.igormaznitsa.sciareto.ui.UiUtils;
import com.igormaznitsa.sciareto.ui.UiUtils.SplashScreen;
import com.igormaznitsa.sciareto.ui.misc.JHtmlLabel;
import com.igormaznitsa.sciareto.ui.platform.PlatformProvider;
import com.igormaznitsa.mindmap.plugins.api.HasOptions;
import com.igormaznitsa.meta.common.utils.Assertions;

public class Main {

  public static final long UPSTART = System.currentTimeMillis();

  private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

  private static MainFrame MAIN_FRAME;

  public static final Version IDE_VERSION = new Version("sciareto", new long[] {1L, 4L, 3L}, null); //NOI18N

  public static final Random RND = new Random();

  private static final String PROPERTY = "nbmmd.plugin.folder"; //NOI18N
  public static final String PROPERTY_LOOKANDFEEL = "selected.look.and.feel"; //NOI18N
  public static final String PROPERTY_TOTAL_UPSTART = "time.total.upstart"; //NOI18N

  private static final long STATISTICS_DELAY = 7L * 24L * 3600L * 1000L;

  private static final class FakeFileFilter extends FileFilter {

    @Override
    public boolean accept(@Nonnull final File f) {
      return false;
    }

    @Override
    @Nonnull
    public String getDescription() {
      return ""; //NOI18N
    }

  }

  private static final class LocalMMDImporter extends AbstractImporter {

    @Override
    @Nullable
    public MindMap doImport(@Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nullable final Topic actionTopic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) throws Exception {
      final File fileToImport = dialogProvider.msgOpenFileDialog(null, "", "", null, true, new FakeFileFilter(), ""); //NOI18N
      return new MindMap(null, new StringReader(FileUtils.readFileToString(fileToImport, "UTF-8"))); //NOI18N
    }

    @Override
    @Nonnull
    public String getName(@Nonnull final MindMapPanel panel, @Nullable final Topic actionTopic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
      return "MMDImporter"; //NOI18N
    }

    @Override
    @Nonnull
    public String getReference(final MindMapPanel panel, @Nullable final Topic actionTopic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
      return "MMDImporter"; //NOI18N
    }

    @Override
    public String getMnemonic() {
      return "mmd"; //NOI18N
    }

    @Override
    @Nonnull
    public Icon getIcon(@Nonnull final MindMapPanel panel, @Nullable final Topic actionTopic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
      return new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_BYTE_INDEXED));
    }

    @Override
    public int getOrder() {
      return 0;
    }

  }

  private static final class LocalMMDExporter extends AbstractExporter {

    @Override
    public void doExport(@Nonnull final MindMapPanel panel, @Nullable final JComponent options, @Nullable final OutputStream out) throws IOException {
      final MindMap map = panel.getModel();
      IOUtils.write(map.write(new StringWriter()).toString(), out, "UTF-8"); //NOI18N
    }

    @Nonnull
    @Override
    public String getName(@Nonnull final MindMapPanel panel, @Nullable Topic actionTopic, @Nonnull @MustNotContainNull Topic[] selectedTopics) {
      return "MMDExporter"; //NOI18N
    }

    @Nonnull
    @Override
    public String getReference(@Nonnull final MindMapPanel panel, @Nullable Topic actionTopic, @Nonnull @MustNotContainNull Topic[] selectedTopics) {
      return "MMDExporter"; //NOI18N
    }

    @Nonnull
    @Override
    public Icon getIcon(@Nonnull final MindMapPanel panel, @Nullable Topic actionTopic, @Nonnull @MustNotContainNull Topic[] selectedTopics) {
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

  public static void main(@Nonnull @MustNotContainNull final String... args) {
    // -- Properties for MAC OSX --  
    System.setProperty("apple.awt.fileDialogForDirectories", "true"); //NOI18N
    System.setProperty("apple.laf.useScreenMenuBar", "true"); //NOI18N
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "SciaReto"); //NOI18N
    // ----------------------------
    
    SystemUtils.setDebugLevelForJavaLogger(Level.WARNING);

    PlatformProvider.getPlatform().init();

    final String selectedLookAndFeel = PreferencesManager.getInstance().getPreferences().get(PROPERTY_LOOKANDFEEL, PlatformProvider.getPlatform().getDefaultLFClassName());

    LOGGER.info("java.vendor = " + System.getProperty("java.vendor", "unknown")); //NOI18N
    LOGGER.info("java.version = " + System.getProperty("java.version", "unknown")); //NOI18N
    LOGGER.info("os.name = " + System.getProperty("os.name", "unknown")); //NOI18N
    LOGGER.info("os.arch = " + System.getProperty("os.arch", "unknown")); //NOI18N
    LOGGER.info("os.version = " + System.getProperty("os.version", "unknown")); //NOI18N

    final AtomicReference<SplashScreen> splash = new AtomicReference<>();

    final long timeTakenBySplashStart;

    if (args.length == 0) {
      final long splashTimerStart = System.currentTimeMillis();
      try {
        final Image splashImage = Assertions.assertNotNull(UiUtils.loadIcon("splash.png")); //NOI18N

        SwingUtilities.invokeAndWait(new Runnable() {
          @Override
          public void run() {
            try {
              splash.set(new SplashScreen(splashImage));
              splash.get().setVisible(true);
            } catch (Exception ex) {
              LOGGER.error("Splash can't be shown", ex); //NOI18N
              if (splash.get() != null) {
                splash.get().dispose();
                splash.set(null);
              }
            }
          }
        });
      } catch (final Exception ex) {
        LOGGER.error("Error during splash processing", ex); //NOI18N
      }
      timeTakenBySplashStart = System.currentTimeMillis() - splashTimerStart;
    } else {
      timeTakenBySplashStart = 0L;
    }

    if ((System.currentTimeMillis() - PreferencesManager.getInstance().getPreferences().getLong(MetricsService.PROPERTY_METRICS_SENDING_LAST_TIME, System.currentTimeMillis() + STATISTICS_DELAY)) >= STATISTICS_DELAY) {
      LOGGER.info("Statistics scheduled"); //NOI18N

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
        try {
          final Preferences prefs = PreferencesManager.getInstance().getPreferences();
          prefs.putLong(PROPERTY_TOTAL_UPSTART, prefs.getLong(PROPERTY_TOTAL_UPSTART, 0L) + (System.currentTimeMillis() - UPSTART));
          PreferencesManager.getInstance().flush();
        } finally {
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
      LOGGER.error("Can't set L&F", e); //NOI18N
    }

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

        final File file = args.length > 1 ? new File(args[1]) : new File("sciaretosettings.properties"); //NOI18N

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
      SystemUtils.setDebugLevelForJavaLogger(Level.INFO);

      MindMapPluginRegistry.getInstance().registerPlugin(new PrinterPlugin());
      MindMapPluginRegistry.getInstance().unregisterPluginForClass(AboutPlugin.class);
      MindMapPluginRegistry.getInstance().unregisterPluginForClass(OptionsPlugin.class);

      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          final GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
          final int width = gd.getDisplayMode().getWidth();
          final int height = gd.getDisplayMode().getHeight();

          if (PlatformProvider.isErrorDetected()) {
            JOptionPane.showMessageDialog(null, "Can't init the Platform dependent layer, the default one will be used instead.\n"
                + "Check that you have installed Java correctly to avoid the warning", "Warning", JOptionPane.WARNING_MESSAGE);
          }

          MAIN_FRAME = new MainFrame(args);
          MAIN_FRAME.setSize(Math.round(width * 0.75f), Math.round(height * 0.75f));

          if (splash.get() != null) {
            final long delay = (2000L + timeTakenBySplashStart) - (System.currentTimeMillis() - UPSTART);
            if (delay > 0L) {
              final Timer timer = new Timer((int) delay, new ActionListener() {
                @Override
                public void actionPerformed(@Nonnull final ActionEvent e) {
                  splash.get().dispose();
                  splash.set(null);
                }
              });
              timer.setRepeats(false);
              timer.start();
            } else {
              splash.get().dispose();
              splash.set(null);
            }
          }

          MAIN_FRAME.setVisible(true);

          MAIN_FRAME.setExtendedState(MAIN_FRAME.getExtendedState() | JFrame.MAXIMIZED_BOTH);
          final JHtmlLabel label = new JHtmlLabel("<html>You use the application already for some time. If you like it then you could support its author and <a href=\"http://www.google.com\"><b>make some donation</b></a>.</html>");
          label.addLinkListener(new JHtmlLabel.LinkListener() {
            @Override
            public void onLinkActivated(@Nonnull final JHtmlLabel source, @Nonnull final String link) {
              try {
                UiUtils.browseURI(new URI(link), false);
              } catch (URISyntaxException ex) {
                LOGGER.error("Can't make URI", ex); //NOI18N
              }
            }
          });
        }
      });

      new MessagesService().execute();
    }
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
        final AbstractImporter importer = MindMapPluginRegistry.getInstance().findImporterForMnemonic(params[IN_TYPE]);
        final AbstractExporter exporter = MindMapPluginRegistry.getInstance().findExporterForMnemonic(params[OUT_TYPE]);

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
              config.loadFrom(new PropertiesPreferences(FileUtils.readFileToString(settingsFile, "UTF-8")));
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

    final PropertiesPreferences prefs = new PropertiesPreferences("Exported configuration for SciaReto editor https://github.com/raydac/netbeans-mmd-plugin"); //NOI18N
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
      final PropertiesPreferences prefs = new PropertiesPreferences(FileUtils.readFileToString(settingsFile, "UTF-8"));
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

  private static void makeConversion(@Nonnull final File from, @Nonnull final AbstractImporter fromFormat, @Nonnull final File to, @Nonnull final AbstractExporter toFormat, @Nonnull final MindMapPanelConfig config, @Nonnull final Properties options) throws Exception {
    final AtomicReference<Exception> error = new AtomicReference<>();
    SwingUtilities.invokeAndWait(new Runnable() {
      @Override
      public void run() {
        try {
          final DialogProvider dialog = new DialogProvider() {
            @Override
            public void msgError(@Nullable final Component parentComponent, @Nonnull final String text) {
              LOGGER.error(text);
            }

            @Override
            public void msgInfo(@Nullable final Component parentComponent, @Nonnull final String text) {
              LOGGER.info(text);
            }

            @Override
            public void msgWarn(@Nullable final Component parentComponent, @Nonnull final String text) {
              LOGGER.warn(text);
            }

            @Override
            public boolean msgConfirmOkCancel(@Nullable final Component parentComponent, @Nonnull final String title, @Nonnull final String question) {
              throw new UnsupportedOperationException("Not supported yet."); //NOI18N
            }

            @Override
            public boolean msgOkCancel(@Nullable final Component parentComponent, @Nonnull final String title, @Nonnull final JComponent component) {
              throw new UnsupportedOperationException("Not supported yet."); //NOI18N
            }

            @Override
            public boolean msgConfirmYesNo(@Nullable final Component parentComponent, @Nonnull final String title, @Nonnull final String question) {
              throw new UnsupportedOperationException("Not supported yet."); //NOI18N
            }

            @Override
            public Boolean msgConfirmYesNoCancel(@Nullable final Component parentComponent, @Nonnull final String title, @Nonnull final String question) {
              throw new UnsupportedOperationException("Not supported yet."); //NOI18N
            }

            @Override
            public File msgSaveFileDialog(@Nullable final Component parentComponent, @Nonnull final String id, @Nonnull final String title, @Nullable final File defaultFolder, final boolean filesOnly, @Nonnull final FileFilter fileFilter, @Nonnull final String approveButtonText) {
              return to;
            }

            @Override
            public File msgOpenFileDialog(@Nullable final Component parentComponent, @Nonnull final String id, @Nonnull final String title, @Nullable final File defaultFolder, final boolean filesOnly, @Nonnull final FileFilter fileFilter, @Nonnull final String approveButtonText) {
              return from;
            }
          };

          final MindMapPanel panel = new MindMapPanel(new MindMapPanelController() {
            @Override
            public boolean isUnfoldCollapsedTopicDropTarget(@Nonnull final MindMapPanel source) {
              return false;
            }

            @Override
            public boolean isCopyColorInfoFromParentToNewChildAllowed(@Nonnull final MindMapPanel source) {
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
            public MindMapPanelConfig provideConfigForMindMapPanel(@Nonnull final MindMapPanel source) {
              return config;
            }

            @Override
            @Nullable
            public JPopupMenu makePopUpForMindMapPanel(@Nonnull final MindMapPanel source, @Nonnull final Point point, @Nullable final AbstractElement elementUnderMouse, @Nullable final ElementPart elementPartUnderMouse) {
              return null;
            }

            @Override
            @Nonnull
            public DialogProvider getDialogProvider(@Nonnull final MindMapPanel source) {
              return dialog;
            }

            @Override
            public boolean processDropTopicToAnotherTopic(@Nonnull final MindMapPanel source, @Nonnull final Point dropPoint, @Nonnull final Topic draggedTopic, @Nonnull final Topic destinationTopic) {
              return false;
            }

          });

          MindMap map = new MindMap(new MindMapController() {
            private static final long serialVersionUID = -5276000656494506314L;

            @Override
            public boolean canBeDeletedSilently(@Nonnull final MindMap map, @Nonnull final Topic topic) {
              return true;
            }
          }, false);
          panel.setModel(map);

          map = fromFormat.doImport(panel, dialog, null, new Topic[0]);
          if (map != null) {
            panel.setModel(map);
          } else {
            dialog.msgError(MAIN_FRAME, "Can't import map");
          }

          final JComponent optionsComponent = toFormat.makeOptions();

          if (!options.isEmpty()) {
            if (optionsComponent instanceof HasOptions) {
              final HasOptions optionable = (HasOptions) optionsComponent;
              for (final String k : options.stringPropertyNames()) {
                if (optionable.doesSupportKey(k)) {
                  optionable.setOption(k, options.getProperty(k));
                } else {
                  throw new IllegalArgumentException("Exporter " + toFormat.getMnemonic() + " doesn't support option '" + k + "\', it provides options " + Arrays.toString(optionable.getOptionKeys())); //NOI18N
                }
              }
            } else {
              throw new IllegalArgumentException("Exporter " + toFormat.getMnemonic() + " doesn't support options"); //NOI18N
            }
          }

          final FileOutputStream result = new FileOutputStream(to, false);
          try {
            toFormat.doExport(panel, optionsComponent, result);
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
  private static String makeMnemonicList(@Nonnull @MustNotContainNull final List<? extends MindMapPlugin> plugins) {
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
    out.println("   java -jar sciareto.jar [--help|--importsettings FILE|--exportsettings FILE|--convert <>]|[FILE FILE ... FILE]"); //NOI18N
    out.println();
    printConversionHelp(out);
  }

  private static void printConversionHelp(@Nonnull final PrintStream out) {
    final String allowedFormatsFrom = makeMnemonicList(MindMapPluginRegistry.getInstance().findFor(AbstractImporter.class));
    final String allowedFormatsTo = makeMnemonicList(MindMapPluginRegistry.getInstance().findFor(AbstractExporter.class));
    out.println();
    out.println("Usage in converter mode:"); //NOI18N
    out.println(String.format(" --convert --in IN_FILE [--from (%s)] --out OUT_FILE [--to (%s)] [--settings FILE] [--option NAME=VALUE...]", allowedFormatsFrom, allowedFormatsTo)); //NOI18N
    out.println();
    out.println("   --convert - command to make conversion, must be the first argument"); //NOI18N
    out.println("   --in FILE - file to be converted"); //NOI18N
    out.println("   --from FORMAT - type of source format, be default 'mmd' (allowed " + allowedFormatsFrom + ')'); //NOI18N
    out.println("   --out FILE - destination file, if file exists it will be overrided"); //NOI18N
    out.println("   --to FORMAT - type of destination format, bye default 'mmd' (allowed " + allowedFormatsTo + ')'); //NOI18N
    out.println("   --settings FILE - use graphic settings defined in Java property file"); //NOI18N
    out.println("   --option NAME=VALUE - an option to tune export process, specific for each exporter, see documentation"); //NOI18N
    out.println();
  }
}
