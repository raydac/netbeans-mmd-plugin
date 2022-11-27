/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.ideamindmap.utils;

import static com.intellij.openapi.ui.playback.PlaybackRunner.StatusCallback.Type.message;


import com.igormaznitsa.commons.version.Version;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.ide.IDEBridge;
import com.igormaznitsa.mindmap.swing.ide.NotificationType;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.IconLoader;
import javax.annotation.Nonnull;
import javax.swing.Icon;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.Nullable;

public class IdeaIDEBridge implements IDEBridge {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdeaIDEBridge.class);
  private static final NotificationGroup MMD_GROUP = new NotificationGroup("IDEA MindMap", NotificationDisplayType.BALLOON, true);
  private final Version ideVersion;

  public IdeaIDEBridge() {
    final ApplicationInfo info = ApplicationInfo.getInstance();
    final long major = safeNumberExtraction(info.getMajorVersion());
    final long minor = safeNumberExtraction(info.getMinorVersion());
    final long micro = safeNumberExtraction(IdeaUtils.safeInvokeMethodForResult(info, "0", "getMicroVersion", new Class<?>[0], new Object[0]));
    this.ideVersion = new Version("intellij", new long[] {major, minor, micro}, info.getVersionName());
  }

  private static long safeNumberExtraction(final String data) {
    if (data == null) {
      return 0L;
    }
    final StringBuilder buffer = new StringBuilder();
    for (final char c : data.toCharArray()) {
      if (Character.isDigit(c)) {
        buffer.append(c);
      }
    }
    try {
      return Long.parseLong(buffer.toString());
    } catch (NumberFormatException ex) {
      return 0L;
    }
  }

  @Nonnull
  @Override
  public Version getIDEVersion() {
    return this.ideVersion;
  }

  @Override
  @Nonnull
  public String getIDEGeneratorId() {
    return "com.igormaznitsa:idea-mindmap:" + this.getIDEVersion();
  }

  @Override
  public void showIDENotification(@Nonnull final String title, @Nonnull final String text, @Nonnull final NotificationType type) {
    final com.intellij.notification.NotificationType ideType;
    switch (type) {
      case INFO: {
        ideType = com.intellij.notification.NotificationType.INFORMATION;
        LOGGER.info("{INFO}IDENotification : (" + title + ") " + message);
      }
      break;
      case WARNING: {
        ideType = com.intellij.notification.NotificationType.WARNING;
        LOGGER.warn("{WARN}IDENotification : (" + title + ") " + message);
      }
      break;
      case ERROR: {
        ideType = com.intellij.notification.NotificationType.ERROR;
        LOGGER.warn("{ERROR}IDENotification : (" + title + ") " + message);
      }
      break;
      default: {
        ideType = com.intellij.notification.NotificationType.WARNING;
        LOGGER.warn("{*****}IDENotification : (" + title + ") " + message);
      }
      break;
    }

    ApplicationManager.getApplication().invokeLater(() -> {
      final long timestamp = System.currentTimeMillis();
      final Notification notification = new Notification(MMD_GROUP.getDisplayId(), StringEscapeUtils.escapeHtml3(title), StringEscapeUtils.escapeHtml3(text), ideType) {
        @Nullable
        @Override
        public Icon getIcon() {
          return AllIcons.Logo.MINDMAP;
        }

        public long getTimestamp() {
          return timestamp;
        }
      };
      Notifications.Bus.notify(notification);
    });
  }

  @Override
  public void notifyRestart() {
    LOGGER.info("Request to restart IDE");
    final Application application = ApplicationManager.getApplication();
    application.restart();
  }

  @Nonnull
  @Override
  public Icon loadIcon(@Nonnull final String path, @Nonnull final Class<?> klazz) {
    return IconLoader.getIcon(path, klazz);
  }
}
