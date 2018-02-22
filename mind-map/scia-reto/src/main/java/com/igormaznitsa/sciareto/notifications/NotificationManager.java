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

import java.awt.Color;
import java.awt.Image;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.commons.lang.StringEscapeUtils;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.sciareto.Main;

public class NotificationManager {

  private static final NotificationManager INSTANCE = new NotificationManager();

  public enum Type {
    INFO, WARN, ERROR
  }

  private NotificationManager() {
  }

  @Nonnull
  public static NotificationManager getInstance() {
    return INSTANCE;
  }

  public void clearAll() {
    Utils.safeSwingCall(new Runnable() {
      @Override
      public void run() {
        Main.getApplicationFrame().getStackPanel().removeAll();
        Main.getApplicationFrame().getGlassPane().setVisible(false);
      }
    });
  }

  public void showNotification(@Nullable final Image icon, @Nullable final String title, @Nonnull final Type type, @Nonnull final String message) {
    final JLabel label = new JLabel(String.format("<html>%s</html>", StringEscapeUtils.escapeHtml(message))); //NOI18N
    label.setForeground(Color.black);
    this.showNotification(icon, title, type, label);
  }

  public void showNotification(@Nullable final Image icon, @Nullable final String title, @Nonnull final Type type, @Nonnull final JComponent component) {
    Utils.safeSwingCall(new Runnable() {
      @Override
      public void run() {
        final JPanel stack = Main.getApplicationFrame().getStackPanel();

        Color color = Color.WHITE;
        switch (type) {
          case ERROR:
            color = new Color(0xFFCCCC, false);
            break;
          case WARN:
            color = new Color(0xFFFF99, false);
            break;
          case INFO:
            color = new Color(0xCCFFCC, false);
            break;
        }

        stack.add(new MessagePanel(icon, title, color, component), 0);
        stack.revalidate();
        Main.getApplicationFrame().getGlassPane().setVisible(true);
      }
    });
  }
}
