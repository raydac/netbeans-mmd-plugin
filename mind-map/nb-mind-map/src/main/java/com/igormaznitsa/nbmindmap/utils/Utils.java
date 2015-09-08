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
package com.igormaznitsa.nbmindmap.utils;

import com.igormaznitsa.nbmindmap.mmgui.AbstractCollapsableElement;
import com.igormaznitsa.mindmap.model.Topic;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Utils {

  ;
  
  private static final Logger logger = LoggerFactory.getLogger(Utils.class);
        
        
  private static String normalizeURI(final String s){
    final int schemePosition = s.indexOf(':');
    final String scheme =  s.substring(0,schemePosition);
    final String chars = " :<>?";
    String result = s.substring(schemePosition+1);
    for(final char ch : chars.toCharArray()){
      result = result.replace(Character.toString(ch), "%"+Integer.toHexString(ch).toUpperCase(Locale.ENGLISH));
    }
    return scheme+':'+result;
  }
        
  public static File makeFileForPath(final String str){
    if (str == null || str.isEmpty()) return null;
    if (str.startsWith("file:")){
      try {
        return new File(new URI(normalizeURI(str)));
      }
      catch (URISyntaxException ex) {
        logger.error("URISyntaxException for "+str, ex);
        return null;
      }
    }else{
      return new File(str);
    }
  }
        
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

  public static boolean equals(final Map<?, ?> map1, final Map<?, ?> map2) {
    if (map1 == map2) {
      return true;
    }
    if (map1.size() != map2.size()) {
      return false;
    }
    for (final Map.Entry<?, ?> e : map1.entrySet()) {
      final Object value = e.getValue();
      final Object thatValue = map2.get(e.getKey());

      if (value != thatValue) {
        if (value == null || thatValue == null) {
          return false;
        }
        else if (!value.equals(thatValue)) {
          return false;
        }
      }
    }
    return true;
  }

  public static String toStringAndReset(final StringBuilder buffer) {
    final String result = buffer.toString();
    buffer.setLength(0);
    return result;
  }

  public static int nextLine(final Reader in, final Appendable buffer) throws IOException {
    int num = 0;
    while (true) {
      final int code = in.read();
      if (code < 0) {
        num = num == 0 ? -1 : num;
        break;
      }
      else {
        if (code == '\n') {
          break;
        }
        buffer.append((char) code);
        num++;
      }
    }
    return num;
  }

  public static Topic[] getLeftToRightOrderedChildrens(final Topic topic) {
    final List<Topic> result = new ArrayList<Topic>();
    if (topic.getTopicLevel() == 0) {
      for (final Topic t : topic.getChildren()) {
        if (AbstractCollapsableElement.isLeftSidedTopic(t)) {
          result.add(t);
        }
      }
      for (final Topic t : topic.getChildren()) {
        if (!AbstractCollapsableElement.isLeftSidedTopic(t)) {
          result.add(t);
        }
      }
    }
    else {
      result.addAll(topic.getChildren());
    }
    return result.toArray(new Topic[result.size()]);
  }

  public static String color2html(final Color color) {
    final StringBuilder buffer = new StringBuilder();

    buffer.append('#');
    for (final int c : new int[]{color.getRed(), color.getGreen(), color.getBlue()}) {
      final String str = Integer.toHexString(c & 0xFF).toUpperCase(Locale.ENGLISH);
      if (str.length() < 2) {
        buffer.append('0');
      }
      buffer.append(str);
    }

    return buffer.toString();
  }

  public static String getFirstLine(final String text) {
    return text.replace("\r", "").split("\\n")[0]; //NOI18N
  }

  public static String makeShortTextVersion(String text, final int maxLength) {
    if (text.length() > maxLength) {
      text = text.substring(0, maxLength) + "..."; //NOI18N
    }
    return text;
  }

}
