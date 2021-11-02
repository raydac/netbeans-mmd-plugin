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

import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.sciareto.SciaRetoStarter;
import org.apache.commons.lang3.StringEscapeUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;

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
        SciaRetoStarter.getApplicationFrame().getStackPanel().removeAll();
        SciaRetoStarter.getApplicationFrame().getGlassPane().setVisible(false);
      }
    });
  }

  public void showNotification(@Nullable final Image icon, @Nullable final String title, @Nonnull final Type type, @Nonnull final String message) {
    final JLabel label = new JLabel(String.format("<html>%s</html>", StringEscapeUtils.escapeHtml3(message))); //NOI18N
    label.setForeground(Color.black);
    this.showNotification(icon, title, type, label);
  }

  public void showNotification(@Nullable final Image icon, @Nullable final String title, @Nonnull final Type type, @Nonnull final JComponent component) {
    Utils.safeSwingCall(new Runnable() {
      @Override
      public void run() {
        final JPanel stack = SciaRetoStarter.getApplicationFrame().getStackPanel();

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
        SciaRetoStarter.getApplicationFrame().getGlassPane().setVisible(true);
      }
    });
  }
}
