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
package com.igormaznitsa.sciareto.metrics;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.sciareto.preferences.AdditionalPreferences;
import com.igormaznitsa.sciareto.preferences.PreferencesManager;

import javax.annotation.Nonnull;

public class MetricsService implements AdditionalPreferences {

  public static final String PROPERTY_METRICS_SENDING_LAST_TIME = "metrics.sending.last.time";

  private static final MetricsService INSTANCE = new MetricsService();
  private static final String PROJECT_TOKEN = "3b0f869a336fd27dc5c2fbd73c7bd3ee";

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricsService.class);
  private volatile boolean sendMetricsDataEnabled = true;

  private MetricsService() {
    this.refreshConfig();
  }

  @Nonnull
  public static MetricsService getInstance() {
    return INSTANCE;
  }
  
  public void onFirstStart() {
      LOGGER.info("Starting statistics send"); //NOI18N
      final Thread thread = new Thread(() -> {
        try {
          doFirstStartAction();
        } catch (Exception ex) {
          LOGGER.error("Can't send statistics", ex); //NOI18N
        }
      }, "sciareto-statistics-send-thread"); //NOI18N
      thread.setDaemon(true);
      thread.start();
  }

  public void sendStatistics() {
    if (this.sendMetricsDataEnabled) {
      LOGGER.info("Starting statistics send");
      final Thread thread = new Thread(() -> {
        try {
          doAction();
        } catch (Exception ex) {
          LOGGER.error("Can't send statistics", ex);
        }
      }, "sciareto-statistics-send-thread");
      thread.setDaemon(true);
      thread.start();
    } else {
      LOGGER.info("Ignored statistics because disabled");
    }
  }
  
  private void doFirstStartAction() {
    try {
      final String installationUUID = PreferencesManager.getInstance().getInstallationUUID().toString();
    } finally {
      PreferencesManager.getInstance().getPreferences().putLong(PROPERTY_METRICS_SENDING_LAST_TIME, System.currentTimeMillis());
      PreferencesManager.getInstance().flush();
    }
  }

  private void doAction() {
    try {
      final String installationUUID = PreferencesManager.getInstance().getInstallationUUID().toString();
    } finally {
      PreferencesManager.getInstance().getPreferences().putLong(PROPERTY_METRICS_SENDING_LAST_TIME, System.currentTimeMillis());
      PreferencesManager.getInstance().flush();
    }
  }

  public void refreshConfig() {
    final MindMapPanelConfig config = new MindMapPanelConfig();
    config.loadFrom(PreferencesManager.getInstance().getPreferences());
    this.sendMetricsDataEnabled = config.getOptionalProperty(PROPERTY_METRICS_SENDING_FLAG, true);
  }

  public boolean isEnabled() {
    return this.sendMetricsDataEnabled;
  }

}
