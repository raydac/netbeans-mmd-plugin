/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.mindmap.plugins.exporters;

import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.api.AbstractExporter;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.plugins.api.parameters.AbstractParameter;
import com.igormaznitsa.mindmap.plugins.api.parameters.BooleanParameter;
import com.igormaznitsa.mindmap.plugins.api.parameters.FileParameter;
import com.igormaznitsa.mindmap.swing.i18n.MmdI18n;
import com.igormaznitsa.mindmap.swing.ide.IDEBridgeFactory;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.utils.ImageSelection;
import com.igormaznitsa.mindmap.swing.panel.utils.MindMapUtils;
import com.igormaznitsa.mindmap.swing.panel.utils.RenderQuality;
import com.igormaznitsa.mindmap.swing.services.IconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.apache.commons.io.IOUtils;

public final class PNGImageExporter extends AbstractExporter {

  private static final String KEY_PARAMETER_UNFOLD_ALL = "mmd.exporter.png.unfold.all";
  private static final String KEY_PARAMETER_DRAW_BACKGROUND = "mmd.exporter.png.background.draw";
  private static final String KEY_PARAMETER_CUSTOM_CONFIG_FILE =
      "mmd.exporter.png.custom.config.file";

  private static final Logger LOGGER = LoggerFactory.getLogger(PNGImageExporter.class);
  private static final Icon ICO =
      ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_EXPORT_PNG);

  public PNGImageExporter() {
    super();
  }

  @Override
  public Set<AbstractParameter<?>> makeDefaultParameters() {
    return new HashSet<AbstractParameter<?>>() {{
      add(new BooleanParameter(KEY_PARAMETER_UNFOLD_ALL,
          getResourceBundle().getString("PNGImageExporter.optionUnfoldAll"),
          getResourceBundle().getString("PNGImageExporter.optionUnfoldAll.comment"),
          true, 1));
      add(new BooleanParameter(KEY_PARAMETER_DRAW_BACKGROUND,
          getResourceBundle().getString("PNGImageExporter.optionDrawBackground"),
          getResourceBundle().getString("PNGImageExporter.optionDrawBackground.comment"),
          true, 2));
      add(new FileParameter(KEY_PARAMETER_CUSTOM_CONFIG_FILE,
          getResourceBundle().getString("PNGImageExporter.customConfigFile"),
          getResourceBundle().getString("PNGImageExporter.optionCustomConfigFile.comment"),
          null, new FileParameter.FileChooserParamsProvider() {
        @Override
        public String getTitle() {
          return MmdI18n.getInstance().findBundle()
              .getString("PNGImageExporter.preferences.fileChooser.title");
        }

        @Override
        public FileFilter[] getFileFilters() {
          return new FileFilter[] {
              new FileFilter() {
                @Override
                public String toString() {
                  return MmdI18n.getInstance().findBundle()
                      .getString("PNGImageExporter.preferences.fileChooser.filter");
                }

                @Override
                public boolean accept(File file) {
                  return file.isDirectory() ||
                      file.getName().toLowerCase(Locale.ENGLISH).endsWith(".properties");
                }
              }
          };
        }

        @Override
        public String getApproveText() {
          return MmdI18n.getInstance().findBundle()
              .getString("PNGImageExporter.preferences.fileChooser.approve");
        }

        @Override
        public boolean isFilesOnly() {
          return true;
        }
      }, 0));
    }};
  }

  private BufferedImage makeImage(final PluginContext context,
                                  final Set<AbstractParameter<?>> options) throws IOException {
    final boolean flagExpandAllNodes = options.stream()
        .filter(x -> KEY_PARAMETER_UNFOLD_ALL.equals(x.getId()))
        .findFirst()
        .map(x -> ((BooleanParameter) x).getValue())
        .orElse(true);

    final boolean flagDrawBackground = options.stream()
        .filter(x -> KEY_PARAMETER_DRAW_BACKGROUND.equals(x.getId()))
        .findFirst()
        .map(x -> ((BooleanParameter) x).getValue())
        .orElse(true);

    final File customPreferencesFile = options.stream()
        .filter(x -> KEY_PARAMETER_CUSTOM_CONFIG_FILE.equals(x.getId()))
        .findFirst()
        .map(x -> ((FileParameter) x).getValue())
        .orElse(null);


    final MindMapPanelConfig panelConfig;
    if (customPreferencesFile == null) {
      panelConfig = new MindMapPanelConfig(context.getPanelConfig(), false);
    } else {
      LOGGER.info("Loading custom preferences file: " + customPreferencesFile);
      panelConfig = this.loadPreferencesFile(customPreferencesFile);
    }

    panelConfig.setDrawBackground(flagDrawBackground);
    panelConfig.setScale(1.0f);

    return MindMapPanel.renderMindMapAsImage(context.getPanel().getModel(), panelConfig,
        flagExpandAllNodes, RenderQuality.QUALITY);
  }

  @Override
  public void doExportToClipboard(final PluginContext context,
                                  final Set<AbstractParameter<?>> options)
      throws IOException {
    final BufferedImage image = makeImage(context, options);
    if (image != null) {
      SwingUtilities.invokeLater(() -> {
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        if (clipboard != null) {
          clipboard.setContents(new ImageSelection(image), null);
        }
      });
    }
  }

  @Override
  public void doExport(final PluginContext context, final Set<AbstractParameter<?>> options,
                       final OutputStream out) throws IOException {
    final RenderedImage image = makeImage(context, options);

    if (image == null) {
      if (out == null) {
        LOGGER.error("Can't render map as image");
        context.getDialogProvider()
            .msgError(IDEBridgeFactory.findInstance().findApplicationComponent(),
                this.getResourceBundle().getString("PNGImageExporter.msgErrorDuringRendering"));
        return;
      } else {
        throw new IOException("Can't render image");
      }
    }

    final ByteArrayOutputStream buff = new ByteArrayOutputStream(128000);
    ImageIO.write(image, "png", buff);

    final byte[] imageData = buff.toByteArray();

    File fileToSaveMap = null;
    OutputStream theOut = out;
    if (theOut == null) {
      fileToSaveMap = MindMapUtils.selectFileToSaveForFileFilter(
          context.getPanel(),
          context,
          this.getClass().getName(),
          this.getResourceBundle().getString("PNGImageExporter.saveDialogTitle"),
          null,
          ".png",
          this.getResourceBundle().getString("PNGImageExporter.filterDescription"),
          this.getResourceBundle().getString("PNGImageExporter.approveButtonText"));
      fileToSaveMap =
          MindMapUtils.checkFileAndExtension(context.getPanel(), fileToSaveMap, ".png");
      theOut = fileToSaveMap == null ? null :
          new BufferedOutputStream(new FileOutputStream(fileToSaveMap, false));
    }
    if (theOut != null) {
      try {
        IOUtils.write(imageData, theOut);
      } finally {
        if (fileToSaveMap != null) {
          IOUtils.closeQuietly(theOut);
        }
      }
    }
  }

  @Override
  public String getMnemonic() {
    return "png";
  }

  @Override
  public String getName(final PluginContext context, Topic actionTopic) {
    return this.getResourceBundle().getString("PNGImageExporter.exporterName");
  }

  @Override
  public String getReference(final PluginContext context, Topic actionTopic) {
    return this.getResourceBundle().getString("PNGImageExporter.exporterReference");
  }

  @Override
  public Icon getIcon(final PluginContext context, Topic actionTopic) {
    return ICO;
  }

  @Override
  public int getOrder() {
    return 5;
  }
}
