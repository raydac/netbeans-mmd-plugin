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
package com.igormaznitsa.sciareto.notifications;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.sciareto.SciaRetoStarter;
import com.igormaznitsa.sciareto.preferences.PreferencesManager;
import com.igormaznitsa.sciareto.ui.misc.DonateButton;
import com.igormaznitsa.sciareto.ui.misc.JHtmlLabel;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

public class MessagesService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessagesService.class);

  private static final String PROPERTY_OFFER_TO_DONATE_WAS_SHOWN = "message.shown.offer.donate"; //NOI18N

  public MessagesService() {
  }

  public void execute() {
    final Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        doAction();
      }
    }, "SR_MessageService"); //NOI18N
    thread.setDaemon(true);
    thread.start();
  }

  protected void doAction() {
    final Preferences prefs = PreferencesManager.getInstance().getPreferences();
    if (!prefs.getBoolean(PROPERTY_OFFER_TO_DONATE_WAS_SHOWN, false)) {
      final long totalUpstartTime = prefs.getLong(SciaRetoStarter.PROPERTY_TOTAL_UPSTART, 0L);
      if (totalUpstartTime >= (1000L * 3600L * 24L)) {
        final Timer timer = new Timer(60000,new ActionListener() {
          @Override
          public void actionPerformed(@Nonnull final ActionEvent e) {
            final String text = "<html>You have been using the application for long time!<br>If you like it then you could support us and <a href=\"#\">make a donation</a>!</html>";
            final JHtmlLabel label = new JHtmlLabel(text);
            label.addLinkListener(new JHtmlLabel.LinkListener() {
              @Override
              public void onLinkActivated(@Nonnull final JHtmlLabel source, @Nonnull final String link) {
                new DonateButton().doClick();
              }
            });
            NotificationManager.getInstance().showNotification(null, "Do you like the application?", NotificationManager.Type.INFO, label);
            prefs.putBoolean(PROPERTY_OFFER_TO_DONATE_WAS_SHOWN, true);
            PreferencesManager.getInstance().flush();
          }
        });
        timer.setRepeats(false);
        timer.start();
      }
    }
  }

}
