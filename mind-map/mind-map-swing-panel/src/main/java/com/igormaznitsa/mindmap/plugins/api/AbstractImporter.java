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

import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.PopUpSection;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.Texts;
import com.igormaznitsa.mindmap.swing.panel.utils.MindMapUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Locale;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

/**
 * Abstract auxiliary class automates way to implement an abstract importer.
 *
 * @since 1.2
 */
public abstract class AbstractImporter extends AbstractPopupMenuItem implements HasMnemonic {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractImporter.class);

  private static String normalizeExtension(final String extension) {
    String result = extension.toUpperCase(Locale.ENGLISH);
    if (!result.startsWith(".")) {
      result = '.' + result;
    }
    return result;
  }

  @Override
  public JMenuItem makeMenuItem(
      final PluginContext context,
      final Topic activeTopic
  ) {
    final JMenuItem result = UI_COMPO_FACTORY.makeMenuItem(getName(context), getIcon(context));
    result.setToolTipText(getReference(context));

    result.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        try {
          if (AbstractImporter.this instanceof ExternallyExecutedPlugin) {
            context.processPluginActivation((ExternallyExecutedPlugin) AbstractImporter.this,
                activeTopic);
          } else {
            context.getPanel().removeAllSelection();
            final MindMap map = doImport(context);
            if (map != null) {
              SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                  context.getPanel().setModel(map, true);
                }
              });
              SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                  final Topic root = map.getRoot();
                  if (root != null) {
                    final MindMapPanel panel = context.getPanel();
                    panel.doLayout();
                    panel.revalidate();
                    panel.focusTo(root);
                    panel.repaint();
                  }
                }
              });
            }
          }
        } catch (Exception ex) {
          LOGGER.error("Error during map import", ex); //NOI18N
          context.getDialogProvider().msgError(null, Texts.getString("MMDGraphEditor.makePopUp.errMsgCantImport"));
        }
      }
    });
    return result;
  }

  @Override
  public PopUpSection getSection() {
    return PopUpSection.IMPORT;
  }

  @Override
  public boolean needsTopicUnderMouse() {
    return false;
  }

  @Override
  public boolean needsSelectedTopics() {
    return false;
  }

  protected File selectFileForExtension(final PluginContext context,
                                        final String dialogTitle,
                                        final File defaultFolder,
                                        final String fileExtension,
                                        final String fileFilterDescription,
                                        final String approveButtonText) {
    return MindMapUtils.selectFileToOpenForFileFilter(
        context.getPanel(),
        context,
        this.getClass().getName(),
        dialogTitle,
        defaultFolder,
        normalizeExtension(fileExtension), fileFilterDescription, approveButtonText);
  }

  @Override
  public String getMnemonic() {
    return null;
  }

  public abstract MindMap doImport(PluginContext context) throws Exception;

  public abstract String getName(PluginContext context);

  public abstract String getReference(PluginContext context);

  public abstract Icon getIcon(PluginContext context);


}
