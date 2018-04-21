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
import com.igormaznitsa.mindmap.print.PrintableObject;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.sciareto.Main;
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
        Main.getApplicationFrame().endFullScreenIfActive();
        final MMDPrintPanel panel = new MMDPrintPanel(dialogProvider, adaptor, PrintableObject.newBuild().mmdpanel(mindMapPanel).build());
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
