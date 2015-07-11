/*
 * Copyright 2015 Igor Maznitsa.
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
package com.igormaznitsa.nbmindmap.gui.mmview;

import javax.swing.SwingUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

public enum Utils {
  ;
        
  public static String[] breakToLines(final String text) {
    final int lineNum = numberOfLines(text);
    final String[] result = new String[lineNum];
    final StringBuilder line = new StringBuilder();

    int index = 0;

    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == '\n') {
        result[index++] = line.toString();
        line.setLength(0);
      }
      else {
        line.append(text.charAt(i));
      }
    }
    result[index] = line.toString();
    return result;
  }

  public static int numberOfLines(final String text) {
    int result = 1;
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == '\n') {
        result++;
      }
    }
    return result;
  }

  public static void assertNotNull(final String message, final Object value) {
    if (value == null) {
      throw new NullPointerException(message);
    }
  }

  public static <T> T defaultable(T value, T defaultValue) {
    return value == null ? defaultValue : value;
  }

  public static void safeSwingAsync(final Runnable run) {
    if (SwingUtilities.isEventDispatchThread()) {
      run.run();
    }
    else {
      SwingUtilities.invokeLater(run);
    }
  }

  public static void showInfo(final String message) {
    safeSwingAsync(new Runnable() {
      @Override
      public void run() {
        final NotifyDescriptor descriptor = new NotifyDescriptor.Message(message, NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notify(descriptor);
      }
    });
  }

  public static void showWarn(final String message) {
    safeSwingAsync(new Runnable() {
      @Override
      public void run() {
        final NotifyDescriptor descriptor = new NotifyDescriptor.Message(message, NotifyDescriptor.WARNING_MESSAGE);
        DialogDisplayer.getDefault().notify(descriptor);
      }
    });
  }

  public static void showError(final String message) {
    safeSwingAsync(new Runnable() {
      @Override
      public void run() {
        final NotifyDescriptor descriptor = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(descriptor);
      }
    });
  }

  public static boolean showConfirmationOkCancel(final String title, final String message) {
    final NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(message, title, NotifyDescriptor.OK_CANCEL_OPTION);
    return DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.OK_OPTION;
  }

  public static void delay(final long millseconds) {
    try {
      Thread.sleep(millseconds);
    }
    catch (InterruptedException ex) {
    }
  }

}
