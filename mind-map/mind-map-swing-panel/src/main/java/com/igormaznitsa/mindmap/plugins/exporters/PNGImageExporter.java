/*
 * Copyright 2015-2018 Igor Maznitsa.
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

package com.igormaznitsa.mindmap.plugins.exporters;

import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.api.AbstractExporter;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.plugins.api.parameters.AbstractParameter;
import com.igormaznitsa.mindmap.plugins.api.parameters.BooleanParameter;
import com.igormaznitsa.mindmap.swing.i18n.MmdI18n;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.apache.commons.io.IOUtils;

public final class PNGImageExporter extends AbstractExporter {

  private static final String KEY_PARAMETER_UNFOLD_ALL = "mmd.exporter.png.unfold.all";
  private static final String KEY_PARAMETER_DRAW_BACKGROUND = "mmd.exporter.png.background.draw";

  private static final Logger LOGGER = LoggerFactory.getLogger(PNGImageExporter.class);
  private static final Icon ICO =
      ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_EXPORT_PNG);

  public PNGImageExporter() {
    super();
  }

  @Override
  public Set<AbstractParameter<?>> makeDefaultParameters() {
    return new HashSet<AbstractParameter<?>>() {{
      add(new BooleanParameter(KEY_PARAMETER_UNFOLD_ALL, MmdI18n.getInstance().findBundle().getString("PNGImageExporter.optionUnfoldAll"),
          MmdI18n.getInstance().findBundle().getString("PNGImageExporter.optionUnfoldAll.comment"),
          true));
      add(new BooleanParameter(KEY_PARAMETER_DRAW_BACKGROUND,
          MmdI18n.getInstance().findBundle().getString("PNGImageExporter.optionDrawBackground"), MmdI18n.getInstance().findBundle().getString("PNGImageExporter.optionDrawBackground.comment"),
          true));
    }};
  }

  private BufferedImage makeImage(final PluginContext context,
                                  final Set<AbstractParameter<?>> options) {
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

    final MindMapPanelConfig newConfig = new MindMapPanelConfig(context.getPanelConfig(), false);
    newConfig.setDrawBackground(flagDrawBackground);
    newConfig.setScale(1.0f);

    return MindMapPanel.renderMindMapAsImage(context.getPanel().getModel(), newConfig,
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
            .msgError(null, MmdI18n.getInstance().findBundle().getString("PNGImageExporter.msgErrorDuringRendering"));
        return;
      } else {
        throw new IOException("Can't render image");
      }
    }

    final ByteArrayOutputStream buff = new ByteArrayOutputStream(128000);
    ImageIO.write(image, "png", buff);//NOI18N

    final byte[] imageData = buff.toByteArray();

    File fileToSaveMap = null;
    OutputStream theOut = out;
    if (theOut == null) {
      fileToSaveMap = MindMapUtils.selectFileToSaveForFileFilter(
          context.getPanel(),
          context,
          this.getClass().getName(),
          MmdI18n.getInstance().findBundle().getString("PNGImageExporter.saveDialogTitle"),
          null,
          ".png",
          MmdI18n.getInstance().findBundle().getString("PNGImageExporter.filterDescription"),
          MmdI18n.getInstance().findBundle().getString("PNGImageExporter.approveButtonText"));
      fileToSaveMap =
          MindMapUtils.checkFileAndExtension(context.getPanel(), fileToSaveMap, ".png");//NOI18N
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
    return MmdI18n.getInstance().findBundle().getString("PNGImageExporter.exporterName");
  }

  @Override
  public String getReference(final PluginContext context, Topic actionTopic) {
    return MmdI18n.getInstance().findBundle().getString("PNGImageExporter.exporterReference");
  }

  @Override
  public Icon getIcon(final PluginContext context, Topic actionTopic) {
    return ICO;
  }

  @Override
  public int getOrder() {
    return 4;
  }
}
