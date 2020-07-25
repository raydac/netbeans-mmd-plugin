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
package com.igormaznitsa.sciareto.metrics;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;
import org.json.JSONObject;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.sciareto.Main;
import com.igormaznitsa.sciareto.preferences.PreferencesManager;

public class MetricsService {

  public static final String PROPERTY_METRICS_SENDING_FLAG = "metrics.sending"; //NOI18N
  public static final String PROPERTY_METRICS_SENDING_LAST_TIME = "metrics.sending.last.time"; //NOI18N

  private static final MetricsService INSTANCE = new MetricsService();
  private static final String PROJECT_TOKEN = "3b0f869a336fd27dc5c2fbd73c7bd3ee"; //NOI18N

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricsService.class);

  private final AtomicBoolean enabled = new AtomicBoolean();

  private MetricsService() {
    this.enabled.set(PreferencesManager.getInstance().getPreferences().getBoolean(PROPERTY_METRICS_SENDING_FLAG, true));
  }

  @Nonnull
  public static final MetricsService getInstance() {
    return INSTANCE;
  }

  public void onFirstStart() {
      LOGGER.info("Starting statistics send"); //NOI18N
      final Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            doFirstStartAction();
          } catch (Exception ex) {
            LOGGER.error("Can't send statistics", ex); //NOI18N
          }
        }
      }, "SCIARETO_STATISTIC_SEND"); //NOI18N
      thread.setDaemon(true);
      thread.start();
  }
  
  public void sendStatistics() {
    if (this.enabled.get()) {
      LOGGER.info("Starting statistics send"); //NOI18N
      final Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            doAction();
          } catch (Exception ex) {
            LOGGER.error("Can't send statistics", ex); //NOI18N
          }
        }
      }, "SCIARETO_STATISTIC_SEND"); //NOI18N
      thread.setDaemon(true);
      thread.start();
    } else {
      LOGGER.info("Ignored statistics because disabled"); //NOI18N
    }
  }

  private void doFirstStartAction() throws Exception {
    try {
      final String installationUUID = PreferencesManager.getInstance().getInstallationUUID().toString();
    } finally {
      PreferencesManager.getInstance().getPreferences().putLong(PROPERTY_METRICS_SENDING_LAST_TIME, System.currentTimeMillis());
      PreferencesManager.getInstance().flush();
    }
  }
  
  private void doAction() throws Exception {
    try {
      final String installationUUID = PreferencesManager.getInstance().getInstallationUUID().toString();
    } finally {
      PreferencesManager.getInstance().getPreferences().putLong(PROPERTY_METRICS_SENDING_LAST_TIME, System.currentTimeMillis());
      PreferencesManager.getInstance().flush();
    }
  }

  public boolean isEnabled() {
    return this.enabled.get();
  }

  public void setEnabled(final boolean flag) {
    if (this.enabled.compareAndSet(!flag, flag)) {
      PreferencesManager.getInstance().getPreferences().putBoolean(PROPERTY_METRICS_SENDING_FLAG, flag);
      PreferencesManager.getInstance().flush();
    }
  }
}
