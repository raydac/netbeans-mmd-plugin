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

package com.igormaznitsa.mindmap.plugins.api;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.PopUpSection;
import com.igormaznitsa.mindmap.plugins.api.parameters.AbstractParameter;
import com.igormaznitsa.mindmap.swing.ide.IDEBridgeFactory;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.utils.PropertiesPreferences;
import com.igormaznitsa.mindmap.swing.services.DefaultParametersPanelFactory;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiFunction;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import org.apache.commons.io.FileUtils;

/**
 * Abstract auxiliary class automates way to implement an abstract exporter.
 *
 * @since 1.2
 */
public abstract class AbstractExporter extends AbstractPopupMenuItem implements HasMnemonic {

  protected static final Format DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  protected static final Format TIME_FORMAT = new SimpleDateFormat("HH:mm:ss z");
  protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractExporter.class);
  private static final ExtrasToStringConverter DEFAULT_STRING_CONVERTER =
      new ExtrasToStringConverter() {
      };

  public Set<AbstractParameter<?>> makeDefaultParameters() {
    return Collections.emptySet();
  }

  protected MindMapPanelConfig loadPreferencesFile(final File file) throws IOException {
    final MindMapPanelConfig result = new MindMapPanelConfig();
    try {
      result.loadFrom(new PropertiesPreferences("SciaReto",
          FileUtils.readFileToString(file, StandardCharsets.UTF_8)));
      return result;
    } catch (IOException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new IOException(ex.getMessage(), ex);
    }
  }

  public ExtrasToStringConverter getDefaultExtrasStringConverter() {
    return DEFAULT_STRING_CONVERTER;
  }

  @Override
  public JMenuItem makeMenuItem(final PluginContext context, final Topic activeTopic) {
    final JMenuItem result =
        UI_COMPO_FACTORY.makeMenuItem(getName(context, activeTopic), getIcon(context, activeTopic));
    result.setToolTipText(getReference(context, activeTopic));

    final AbstractPopupMenuItem theInstance = this;

    result.addActionListener(e -> {
      try {
        if (theInstance instanceof ExternallyExecutedPlugin) {
          context.processPluginActivation((ExternallyExecutedPlugin) theInstance, activeTopic);
        } else {
          final Set<AbstractParameter<?>> parameters = this.makeDefaultParameters();
          if (!parameters.isEmpty()) {
            final JComponent options = DefaultParametersPanelFactory.getInstance()
                .make(context.getDialogProvider(), parameters);
            if (options != null && !context.getDialogProvider()
                .msgOkCancel(IDEBridgeFactory.findInstance().findApplicationComponent(),
                    getName(context, activeTopic), options)) {
              return;
            }
          }
          if ((e.getModifiers() & ActionEvent.CTRL_MASK) == 0) {
            LOGGER.info("Export map into file: " + AbstractExporter.this);
            doExport(context, parameters, null);
          } else {
            LOGGER.info("Export map into clipboard:" + AbstractExporter.this);
            doExportToClipboard(context, parameters);
          }
        }
      } catch (Exception ex) {
        LOGGER.error("Error during map export", ex);
        context.getDialogProvider()
            .msgError(IDEBridgeFactory.findInstance().findApplicationComponent(),
                String.format(
                    this.getResourceBundle().getString("MMDGraphEditor.makePopUp.errMsgCantExport"),
                    ex.getMessage()));
      }
    });
    return result;
  }

  @Override
  public PopUpSection getSection() {
    return PopUpSection.EXPORT;
  }

  protected Extra<?> findExtra(final Topic topic, final Extra.ExtraType type) {
    final Extra<?> result = topic.getExtras().get(type);
    return result == null ? null : (result.isExportable() ? result : null);
  }

  @Override
  public boolean needsTopicUnderMouse() {
    return false;
  }

  @Override
  public boolean needsSelectedTopics() {
    return false;
  }

  @Override
  public String getMnemonic() {
    return null;
  }

  /**
   * Export into output stream.
   *
   * @param context                 plugin context, must not be null
   * @param options                 set of parameters to be used during export, must not be null
   * @param out                     target output stream, can be null
   * @param extrasToStringConverter converter to get strings from extras
   * @throws IOException thrown if any error
   * @see ExtrasToStringConverter
   * @since 1.6.8
   */
  public abstract void doExport(final PluginContext context,
                                final Set<AbstractParameter<?>> options,
                                final OutputStream out,
                                final ExtrasToStringConverter extrasToStringConverter)
      throws IOException;

  /**
   * Export into output stream with default extra string converter.
   *
   * @param context plugin context, must not be null
   * @param options set of parameters to be used during export, must not be null
   * @param out     target output stream, can be null
   * @throws IOException thrown if any error
   * @see ExtrasToStringConverter
   * @since 1.6.0
   */
  public void doExport(final PluginContext context,
                       final Set<AbstractParameter<?>> options,
                       final OutputStream out) throws IOException {
    this.doExport(context, options, out, this.getDefaultExtrasStringConverter());
  }

  /**
   * Export data into clipboard with default extras string converter.
   *
   * @param context plugin context, must not be null
   * @param options set of parameters to be used during export, must not be null
   * @throws IOException it will be thrown if any error
   * @since 1.6.0
   */
  public void doExportToClipboard(final PluginContext context,
                                  final Set<AbstractParameter<?>> options) throws IOException {
    this.doExportToClipboard(context, options, this.getDefaultExtrasStringConverter());
  }


  /**
   * Export data into clipboard.
   *
   * @param context                 plugin context, must not be null
   * @param options                 set of parameters to be used during export, must not be null
   * @param extrasToStringConverter converter to get strings from extras
   * @throws IOException it will be thrown if any error
   * @since 1.6.8
   */
  public abstract void doExportToClipboard(final PluginContext context,
                                           final Set<AbstractParameter<?>> options,
                                           final ExtrasToStringConverter extrasToStringConverter)
      throws IOException;

  public abstract String getName(final PluginContext context, final Topic activeTopic);

  public abstract String getReference(final PluginContext context, final Topic activeTopic);

  public abstract Icon getIcon(final PluginContext context, final Topic activeTopic);

  public interface ExtrasToStringConverter extends BiFunction<PluginContext, Extra<?>, String> {
    @Override
    default String apply(final PluginContext pluginContext, final Extra<?> extra) {
      if (extra == null) {
        return null;
      }
      switch (extra.getType()) {
        case FILE:
          return ((ExtraFile) extra).getAsURI().asString(true, false);
        case NOTE:
          return ((ExtraNote) extra).getValue();
        case LINK:
          return ((ExtraLink) extra).getValue().asString(true, true);
        case TOPIC:
          return ((ExtraTopic) extra).getValue();
        default:
          throw new IllegalArgumentException(
              "Unknown extra type: " + extra.getClass().getCanonicalName());
      }
    }
  }

}
