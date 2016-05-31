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
package com.igormaznitsa.nbmindmap.nb.services;

import javax.annotation.Nonnull;
import javax.swing.ImageIcon;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import com.igormaznitsa.commons.version.Version;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.ide.IDEBridge;
import com.igormaznitsa.mindmap.swing.ide.NotificationType;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;

public class NbIDEBridge implements IDEBridge {

  private final Version ideVersion;

  private static final Logger LOGGER = LoggerFactory.getLogger(NbIDEBridge.class);

  public NbIDEBridge() {
    final String versionInfo = System.getProperty("netbeans.productversion");
    if (versionInfo == null) {
      this.ideVersion = new Version("netbeans");
    } else if (versionInfo.equalsIgnoreCase("dev")) {
      this.ideVersion = new Version("netbeans-8.1-dev");
    } else {
      this.ideVersion = new Version(versionInfo.replace(' ', '-')).changePrefix("netbeans").changePostfix("");
    }
  }

  @Override
  @Nonnull
  public Version getIDEVersion() {
    return this.ideVersion;
  }

  @Override
  public void showIDENotification(@Nonnull final String title, @Nonnull final String message, @Nonnull final NotificationType type) {
    final NotificationDisplayer.Priority priority;
    final NotificationDisplayer.Category category;
    final ImageIcon icon;
    switch (type) {
      case INFO: {
        priority = NotificationDisplayer.Priority.NORMAL;
        category = NotificationDisplayer.Category.INFO;
        icon = ImageUtilities.loadImageIcon("org/netbeans/core/windows/resources/info.png", false);
        LOGGER.info("IDENotification : (" + title + ") " + message);
      }
      break;
      case WARNING: {
        priority = NotificationDisplayer.Priority.HIGH;
        category = NotificationDisplayer.Category.WARNING;
        icon = ImageUtilities.loadImageIcon("org/netbeans/core/windows/resources/warning.png", false);
        LOGGER.warn("IDENotification : (" + title + ") " + message);
      }
      break;
      case ERROR: {
        priority = NotificationDisplayer.Priority.HIGH;
        category = NotificationDisplayer.Category.ERROR;
        icon = ImageUtilities.loadImageIcon("org/netbeans/core/windows/resources/error.png", false);
        LOGGER.error("IDENotification : (" + title + ") " + message);
      }
      break;
      default: {
        priority = NotificationDisplayer.Priority.NORMAL;
        category = NotificationDisplayer.Category.WARNING;
        icon = ImageUtilities.loadImageIcon("org/netbeans/core/windows/resources/warning.png", false);
        LOGGER.warn("*IDENotification : (" + title + ") " + message);
      }
      break;
    }

    Utils.safeSwingCall(new Runnable() {
      @Override
      public void run() {
        NotificationDisplayer.getDefault().notify(title, icon, message, null, priority, category);
      }
    });

  }

}
