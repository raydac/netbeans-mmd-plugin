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
package com.igormaznitsa.sciareto.plugins.services;

import static com.igormaznitsa.sciareto.ui.UiUtils.BUNDLE;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.PopUpSection;
import com.igormaznitsa.mindmap.plugins.api.AbstractPopupMenuItem;
import com.igormaznitsa.mindmap.plugins.api.CustomJob;
import com.igormaznitsa.mindmap.print.MMDPrintPanel;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.sciareto.ui.UiUtils;

public class PrinterPlugin extends AbstractPopupMenuItem implements MMDPrintPanel.Adaptor {

  private static final Image ICON_PRINTER = UiUtils.loadIcon("printer.png"); //NOI18N

  private static final Logger LOGGER = LoggerFactory.getLogger(PrinterPlugin.class);

  @Nullable
  @Override
  public JMenuItem makeMenuItem(@Nonnull final MindMapPanel mindMapPanel, @Nonnull final DialogProvider dialogProvider, @Nullable final Topic topic,
      @Nullable @MustNotContainNull final Topic[] topics, @Nullable final CustomJob mindMapPopUpItemCustomProcessor) {

    final MMDPrintPanel.Adaptor adaptor = this;

    final JMenuItem printAction = UI_COMPO_FACTORY.makeMenuItem(BUNDLE.getString("MMDGraphEditor.makePopUp.miPrintPreview"), new ImageIcon(ICON_PRINTER));
    printAction.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(@Nonnull final ActionEvent e) {
        final MMDPrintPanel panel = new MMDPrintPanel(dialogProvider, adaptor, mindMapPanel);
        UiUtils.makeOwningDialogResizable(panel);
        JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(mindMapPanel), panel, "Print mind map", JOptionPane.PLAIN_MESSAGE);
      }
    });
    return printAction;
  }

  @Override
  public boolean isEnabled(@Nonnull MindMapPanel panel, @Nullable Topic topic, @Nonnull @MustNotContainNull Topic[] selectedTopics) {
    return !panel.getModel().isEmpty();
  }

  @Nonnull
  @Override
  public PopUpSection getSection() {
    return PopUpSection.MISC;
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
  public int getOrder() {
    return CUSTOM_PLUGIN_START + 100;
  }

  @Override
  public void startBackgroundTask(@Nonnull final MMDPrintPanel source, @Nonnull final String name, @Nonnull final Runnable task) {
    LOGGER.info("Starting print task : " + name); //NOI18N
    final Thread thread = new Thread(task, name);
    thread.setDaemon(true);
    thread.start();
  }

  @Override
  public boolean isDarkTheme(@Nonnull final MMDPrintPanel source) {
    return false;
  }

  @Override
  public void onPrintTaskStarted(@Nonnull final MMDPrintPanel source) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        final Window wnd = SwingUtilities.windowForComponent(source);
        if (wnd != null) {
          wnd.dispose();
        }
      }
    });
  }

  @Override
  @Nonnull
  public Dimension getPreferredSizeOfPanel(@Nonnull final MMDPrintPanel source) {
    return new Dimension(600, 450);
  }
}
