/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.igormaznitsa.sciareto.notifications;

import static com.igormaznitsa.sciareto.ui.UiUtils.findTextBundle;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.sciareto.SciaRetoStarter;
import com.igormaznitsa.sciareto.preferences.PreferencesManager;
import com.igormaznitsa.sciareto.ui.misc.DonateButton;
import com.igormaznitsa.sciareto.ui.misc.JHtmlLabel;
import java.util.prefs.Preferences;
import javax.swing.Timer;

public class MessagesService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessagesService.class);

  private static final String PROPERTY_OFFER_TO_DONATE_WAS_SHOWN = "message.shown.offer.donate"; //NOI18N

  public MessagesService() {
  }

  public void execute() {
    final Thread thread = new Thread(this::doAction, "SR_MessageService"); //NOI18N
    thread.setDaemon(true);
    thread.start();
  }

  protected void doAction() {
    final Preferences prefs = PreferencesManager.getInstance().getPreferences();
    if (!prefs.getBoolean(PROPERTY_OFFER_TO_DONATE_WAS_SHOWN, false)) {
      final long totalUpstartTime = prefs.getLong(SciaRetoStarter.PROPERTY_TOTAL_UPSTART, 0L);
      if (totalUpstartTime >= (1000L * 3600L * 24L)) {
        final Timer timer = new Timer(60000, e -> {
          final String text = findTextBundle().getString("messageService.donation.text");
          final JHtmlLabel label = new JHtmlLabel(text);
          label.addLinkListener((source, link) -> new DonateButton().doClick());
          NotificationManager.getInstance().showNotification(null,
              findTextBundle().getString("messageService.donation.title"), NotificationManager.Type.INFO, label);
          LOGGER.info("Shown offer to make donation");
          prefs.putBoolean(PROPERTY_OFFER_TO_DONATE_WAS_SHOWN, true);
          PreferencesManager.getInstance().flush();
        });
        timer.setRepeats(false);
        timer.start();
      }
    }
  }

}
