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
package com.igormaznitsa.sciareto.metrics;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;
import org.json.JSONObject;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.sciareto.preferences.PreferencesManager;
import com.mixpanel.mixpanelapi.ClientDelivery;
import com.mixpanel.mixpanelapi.MessageBuilder;
import com.mixpanel.mixpanelapi.MixpanelAPI;

public class MetricsService {

  public static final String PROPERTY_METRICS_SENDING_FLAG = "metrics.sending";

  private static final MetricsService INSTANCE = new MetricsService();
  private static final String PROJECT_TOKEN = "3b0f869a336fd27dc5c2fbd73c7bd3ee";

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricsService.class);
  
  private final AtomicBoolean enabled = new AtomicBoolean();

  private MetricsService() {
    this.enabled.set(PreferencesManager.getInstance().getPreferences().getBoolean(PROPERTY_METRICS_SENDING_FLAG, true));
  }

  @Nonnull
  public static final MetricsService getInstance() {
    return INSTANCE;
  }

  public void sendStatistics() {
    if (this.enabled.get()) {
     LOGGER.info("Starting statistics send");
     final Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
          try{
            doAction();
          }catch(Exception ex){
            LOGGER.error("Can't send statistics", ex);
          }
        }
      }, "SCIARETO_STATISTIC_SEND");
      thread.setDaemon(true);
      thread.start();
    } else {
      LOGGER.info("Ignored statistics because disabled");
    }
  }

  private void doAction() throws Exception {
    final String installationUUID = PreferencesManager.getInstance().getInstallationUUID().toString();

    final MessageBuilder messageBuilder = new MessageBuilder(PROJECT_TOKEN);
    final MixpanelAPI mixpanel = new MixpanelAPI();
    
    final JSONObject sentEvent = messageBuilder.event(installationUUID, "Sent Message", null);
    
    JSONObject props = new JSONObject();
    props.put("Gender", "Female");
    props.put("Plan", "Premium");

    final JSONObject planEvent = messageBuilder.event(installationUUID, "Plan Selected", props);
    
    final ClientDelivery delivery = new ClientDelivery();
    delivery.addMessage(sentEvent);
    delivery.addMessage(planEvent);
    
//    mixpanel.deliver(delivery);
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