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

package com.igormaznitsa.mindmap.plugins.api;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.PopUpSection;
import com.igormaznitsa.mindmap.swing.panel.Texts;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;

/**
 * Abstract auxiliary class automates way to implement an abstract exporter.
 *
 * @since 1.2
 */
public abstract class AbstractExporter extends AbstractPopupMenuItem implements HasMnemonic {

  protected static final Format DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  protected static final Format TIME_FORMAT = new SimpleDateFormat("HH:mm:ss z");
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractExporter.class);

  @Override
  @Nullable
  public JMenuItem makeMenuItem(@Nonnull final PluginContext context, @Nullable final Topic activeTopic) {
    final JMenuItem result = UI_COMPO_FACTORY.makeMenuItem(getName(context, activeTopic), getIcon(context, activeTopic));
    result.setToolTipText(getReference(context, activeTopic));

    final AbstractPopupMenuItem theInstance = this;

    result.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(@Nonnull final ActionEvent e) {
        try {
          if (theInstance instanceof ExternallyExecutedPlugin) {
            context.processPluginActivation((ExternallyExecutedPlugin) theInstance, activeTopic);
          } else {
            final JComponent options = makeOptions(context);
            if (options != null && !context.getDialogProvider().msgOkCancel(null, getName(context, activeTopic), options)) {
              return;
            }
            if ((e.getModifiers() & ActionEvent.CTRL_MASK) == 0) {
              LOGGER.info("Export map into file: " + AbstractExporter.this);
              doExport(context, options, null);
            } else {
              LOGGER.info("Export map into clipboard:" + AbstractExporter.this);
              doExportToClipboard(context, options);
            }
          }
        } catch (Exception ex) {
          LOGGER.error("Error during map export", ex); //NOI18N
          context.getDialogProvider().msgError(null, Texts.getString("MMDGraphEditor.makePopUp.errMsgCantExport"));
        }
      }
    });
    return result;
  }

  @Override
  @Nonnull
  public PopUpSection getSection() {
    return PopUpSection.EXPORT;
  }

  @Nullable
  protected Extra<?> findExtra(@Nonnull final Topic topic, @Nonnull final Extra.ExtraType type) {
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

  @Nullable
  public JComponent makeOptions(@Nonnull final PluginContext context) {
    return null;
  }

  @Nullable
  @Override
  public String getMnemonic() {
    return null;
  }

  public abstract void doExport(@Nonnull final PluginContext context, @Nullable final JComponent options, @Nullable final OutputStream out) throws IOException;

  /**
   * Export data into clipboard.
   *
   * @param context plugin context, must not be null
   * @param options component containing extra options, can be null
   * @throws IOException it will be thrown if any error
   */
  public abstract void doExportToClipboard(@Nonnull final PluginContext context, @Nullable final JComponent options) throws IOException;

  @Nonnull
  public abstract String getName(@Nonnull final PluginContext context, @Nullable final Topic activeTopic);

  @Nonnull
  public abstract String getReference(@Nonnull final PluginContext context, @Nullable final Topic activeTopic);

  @Nonnull
  public abstract Icon getIcon(@Nonnull final PluginContext context, @Nullable final Topic activeTopic);

}
