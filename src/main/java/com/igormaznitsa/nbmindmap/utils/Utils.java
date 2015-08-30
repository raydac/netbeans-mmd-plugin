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
import com.igormaznitsa.nbmindmap.model.Topic;
import java.awt.Color;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import org.apache.commons.lang.StringEscapeUtils;
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

  private static final String MD_ESCAPED_CHARS = "\\`*_{}[]()#<>+-.!";
  private static final Pattern MD_ESCAPED_PATTERN = Pattern.compile("(\\\\[\\\\`*_{}\\[\\]()#<>+-.!])");
  private static final Pattern UNESCAPE_BR = Pattern.compile("(?i)\\<\\s*?br\\s*?\\/?\\>");

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
      if (value == null && thatValue == null) {
        continue;
      }
      if (value == null && thatValue != null) {
        return false;
      }
      if (!value.equals(thatValue)) {
        return false;
      }
    }
    return true;
  }

  public static boolean onlyFromChar(final String line, final char chr) {
    if (line.isEmpty()) {
      return false;
    }
    for (int i = 0; i < line.length(); i++) {
      if (line.charAt(i) != chr) {
        return false;
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

  public static void writeChar(final Appendable out, final char chr, final int times) throws IOException {
    for (int i = 0; i < times; i++) {
      out.append(chr);
    }
  }

  public static String escapeMarkdownStr(final String text) {
    final StringBuilder buffer = new StringBuilder(text.length() * 2);
    for (final char c : text.toCharArray()) {
      if (c == '\n') {
        buffer.append("<br/>");
        continue;
      }
      else if (Character.isISOControl(c)) {
        continue;
      }
      else if (MD_ESCAPED_CHARS.indexOf(c) >= 0) {
        buffer.append('\\');
      }

      buffer.append(c);
    }
    return buffer.toString();
  }

  public static String unescapeMarkdownStr(final String text) {
    String unescaped = UNESCAPE_BR.matcher(text).replaceAll("\n");
    final StringBuffer result = new StringBuffer(text.length());
    final Matcher escaped = MD_ESCAPED_PATTERN.matcher(unescaped);
    while (escaped.find()) {
      final String group = escaped.group(1);
      escaped.appendReplacement(result, Matcher.quoteReplacement(group.substring(1)));
    }
    escaped.appendTail(result);
    return result.toString();
  }

  public static int calcMaxLengthOfBacktickQuotesSubstr(final String text) {
    int result = 0;
    if (text != null) {
      int pos = 0;
      while (pos >= 0) {
        pos = text.indexOf('`', pos);
        if (pos >= 0) {
          int found = 0;
          while (pos < text.length() && text.charAt(pos) == '`') {
            found++;
            pos++;
          }
          result = Math.max(result, found);
        }
      }
    }
    return result;
  }

  public static String makePreBlock(final String text) {
    return "<pre>"+StringEscapeUtils.escapeHtml(text)+"</pre>";
  }
  
  public static String makeMDCodeBlock(final String text) throws IOException {
    final int maxQuotes = Utils.calcMaxLengthOfBacktickQuotesSubstr(text) + 1;
    final StringBuilder result = new StringBuilder(text.length() + 16);
    Utils.writeChar(result, '`', maxQuotes);
    result.append(text);
    Utils.writeChar(result, '`', maxQuotes);
    return result.toString();
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

  public static String getFirstLine(final String text){
    return text.replace("\r", "").split("\\n")[0];
  }
  
  public static String makeShortTextVersion(String text, final int maxLength){
    if (text.length() > maxLength) {
      text = text.substring(0, maxLength) + "...";
    }
    return text;
  }
  
}
