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
package com.igormaznitsa.sciareto.notifications;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import javax.annotation.Nonnull;
import javax.swing.Timer;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.sciareto.Main;
import com.igormaznitsa.sciareto.preferences.PreferencesManager;
import com.igormaznitsa.sciareto.ui.misc.DonateButton;
import com.igormaznitsa.sciareto.ui.misc.JHtmlLabel;

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
      final long totalUpstartTime = prefs.getLong(Main.PROPERTY_TOTAL_UPSTART, 0L);
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
