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
package com.igormaznitsa.mindmap.plugins.api;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import com.igormaznitsa.meta.annotation.MayContainNull;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.PopUpSection;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.Texts;
import com.igormaznitsa.mindmap.swing.panel.utils.MindMapUtils;

/**
 * Abstract auxiliary class automates way to implement an abstract importer.
 *
 * @since 1.2
 */
public abstract class AbstractImporter extends AbstractPopupMenuItem implements HasMnemonic {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractImporter.class);

  @Override
  @Nullable
  public JMenuItem makeMenuItem(
      @Nonnull final MindMapPanel panel,
      @Nonnull final DialogProvider dialogProvider,
      @Nullable final Topic actionTopic,
      @Nonnull @MayContainNull final Topic[] selectedTopics,
      @Nullable final CustomJob processor) {
      final JMenuItem result = UI_COMPO_FACTORY.makeMenuItem(getName(panel, actionTopic, selectedTopics), getIcon(panel, actionTopic, selectedTopics));
      result.setToolTipText(getReference(panel, actionTopic, selectedTopics));

      final AbstractPopupMenuItem theInstance = this;

      result.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@Nonnull final ActionEvent e) {
          try {
            if (processor == null) {
              panel.removeAllSelection();
              final MindMap map = doImport(panel, dialogProvider, actionTopic, selectedTopics);
              if (map != null) {
                SwingUtilities.invokeLater(new Runnable() {
                  @Override
                  public void run() {
                    panel.setModel(map,false);
                    panel.updateView(true);
                    final Topic root = map.getRoot();
                    if (root!=null){
                      panel.focusTo(root);
                    }
                  }
                });
              }
            } else {
              processor.doJob(theInstance, panel, dialogProvider, actionTopic, selectedTopics);
            }
          } catch (Exception ex) {
            LOGGER.error("Error during map import", ex); //NOI18N
            dialogProvider.msgError(Texts.getString("MMDGraphEditor.makePopUp.errMsgCantImport"));
          }
        }
      });
    return result;
  }

  @Override
  @Nonnull
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

  @Nonnull
  private static String normalizeExtension(@Nonnull final String extension) {
    String result = extension.toUpperCase(Locale.ENGLISH);
    if (!result.startsWith(".")) {
      result = '.'+result;
    }
    return result;
  }
  
  @Nullable
  protected File selectFileForExtension(@Nonnull final MindMapPanel panel, @Nonnull final String dialogTitle, @Nonnull final String fileExtension, @Nonnull final String fileFilterDescription, @Nonnull final String approveButtonText){
    return  MindMapUtils.selectFileToOpenForFileFilter(panel, dialogTitle, normalizeExtension(fileExtension), fileFilterDescription, approveButtonText);
  }
  
  @Nullable
  public String getMnemonic(){
    return null;
  }
  
  @Nullable
  public abstract MindMap doImport(@Nonnull final MindMapPanel panel,
      @Nonnull final DialogProvider dialogProvider,
      @Nullable final Topic actionTopic,
      @Nonnull @MayContainNull final Topic[] selectedTopics) throws Exception;

  @Nonnull
  public abstract String getName(@Nonnull final MindMapPanel panel, @Nullable Topic actionTopic, @Nonnull @MustNotContainNull Topic[] selectedTopics);

  @Nonnull
  public abstract String getReference(@Nonnull final MindMapPanel panel, @Nullable Topic actionTopic, @Nonnull @MustNotContainNull Topic[] selectedTopics);

  @Nonnull
  public abstract Icon getIcon(@Nonnull final MindMapPanel panel, @Nullable Topic actionTopic, @Nonnull @MustNotContainNull Topic[] selectedTopics);


}
