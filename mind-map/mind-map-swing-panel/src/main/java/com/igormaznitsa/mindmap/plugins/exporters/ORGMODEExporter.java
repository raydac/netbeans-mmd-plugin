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

package com.igormaznitsa.mindmap.plugins.exporters;

import static com.igormaznitsa.meta.common.utils.Assertions.assertNotNull;


import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.ModelUtils;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.plugins.api.AbstractExporter;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.Texts;
import com.igormaznitsa.mindmap.swing.panel.utils.MindMapUtils;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.mindmap.swing.services.IconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

public class ORGMODEExporter extends AbstractExporter {

  private static final int STARTING_INDEX_FOR_NUMERATION = 5;
  private static final Icon ICO = ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_EXPORT_ORGMODE);

  @Nonnull
  private static String generateString(final char chr, final int length) {
    final StringBuilder buffer = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      buffer.append(chr);
    }
    return buffer.toString();
  }

  @Nonnull
  private static String makeLineFromString(@Nonnull final String text) {
    final StringBuilder result = new StringBuilder(text.length());

    for (final char c : text.toCharArray()) {
      if (Character.isISOControl(c)) {
        result.append(' ');
      } else {
        result.append(c);
      }
    }

    return result.toString();
  }

  @Nullable
  private static String getTopicUid(@Nonnull final Topic topic) {
    return topic.getAttribute(ExtraTopic.TOPIC_UID_ATTR);
  }

  private static void writeTopic(@Nonnull final Topic topic, @Nonnull final String listPosition, @Nonnull final State state) throws IOException {
    final int level = topic.getTopicLevel();

    String prefix = "";//NOI18N

    if (level < STARTING_INDEX_FOR_NUMERATION) {
      final String headerPrefix = generateString('*', topic.getTopicLevel() + 1);//NOI18N
      state.append(headerPrefix).append(' ').append(escapeStr(topic.getText(), true)).nextLine();
    } else {
      final String headerPrefix = generateString('*', STARTING_INDEX_FOR_NUMERATION + 1);//NOI18N
      state.append(prefix).append(headerPrefix).append(' ').append(listPosition).append(' ').append(escapeStr(topic.getText(), true)).nextLine();
    }

    final String topicUid = getTopicUid(topic);
    if (topicUid != null) {
      state.append(":PROPERTIES:").nextLine();//NOI18N
      state.append(":CUSTOM_ID: sec:").append(topicUid).nextLine();//NOI18N
      state.append(":END:").nextLine();//NOI18N
    }

    final ExtraFile file = (ExtraFile) topic.getExtras().get(Extra.ExtraType.FILE);
    final ExtraLink link = (ExtraLink) topic.getExtras().get(Extra.ExtraType.LINK);
    final ExtraNote note = (ExtraNote) topic.getExtras().get(Extra.ExtraType.NOTE);
    final ExtraTopic jump = (ExtraTopic) topic.getExtras().get(Extra.ExtraType.TOPIC);

    boolean extrasPrinted = false;

    if (jump != null) {
      final Topic linkedTopic = topic.getMap().findTopicForLink(jump);
      if (linkedTopic != null) {
        state.append(prefix).append("RELATED TO: ")//NOI18N
            .append("[[#sec:")//NOI18N
            .append(assertNotNull(getTopicUid(linkedTopic)))
            .append("][")//NOI18N
            .append(escapeStr(makeLineFromString(linkedTopic.getText()), true))
            .append("]]")//NOI18N
            .append("  \\\\")//NOI18N
            .nextLine();
        extrasPrinted = true;
      }
    }

    if (file != null) {
      final MMapURI fileURI = file.getValue();
      state.append(prefix).append("FILE: [[");//NOI18N
      if (fileURI.isAbsolute()) {
        state.append(fileURI.asURI().toASCIIString());
      } else {
        state.append("file://./").append(fileURI.asURI().toASCIIString());//NOI18N
      }
      state.append("]] \\\\").nextLine();//NOI18N
      extrasPrinted = true;
    }

    if (link != null) {
      final String ascurl = link.getValue().asString(true, true);
      state.append(prefix).append("URL: [[")//NOI18N
          .append(ascurl)
          .append("]] \\\\")//NOI18N
          .nextLine();
      extrasPrinted = true;
    }

    if (note != null) {
      if (extrasPrinted) {
        state.nextLine();
      }
      printTextBlock(state, prefix, note.getValue());
    }

    final Map<String, String> codeSnippets = topic.getCodeSnippets();
    if (!codeSnippets.isEmpty()) {
      for (final Map.Entry<String, String> e : codeSnippets.entrySet()) {
        final String lang = e.getKey();

        state.append(prefix).append("#+BEGIN_SRC ").append(lang).nextLine();

        final String body = e.getValue();
        for (final String s : StringUtils.split(body, '\n')) {
          state.append(prefix).append(Utils.removeAllISOControlsButTabs(s)).nextLine();
        }

        state.append(prefix).append("#+END_SRC").nextLine();
      }
    }

  }

  private static void printTextBlock(@Nonnull final State state, @Nonnull final String prefix, @Nonnull final String text) {
    final String[] lines = ModelUtils.breakToLines(text);
    for (final String s : lines) {
      state.append(prefix).append(": ").append(s).nextLine();//NOI18N
    }
  }

  @Nonnull
  private static String escapeStr(@Nonnull final String value, final boolean makeOneLine) {
    final StringBuilder result = new StringBuilder();

    for (final char c : value.toCharArray()) {
      boolean processed = false;
      if (makeOneLine) {
        if (c == '\n') {
          result.append(' ');
          processed = true;
        }
      }

      if (!processed) {
        if (!Character.isISOControl(c)) {
          result.append(c);
        }
      }
    }

    return result.toString();
  }

  @Nonnull
  private static String ensureNumberFormatting(final int desiredLength, final int number) {
    final String numAsText = Integer.toString(number);
    final StringBuilder result = new StringBuilder();

    for (int i = 0; i < desiredLength - numAsText.length(); i++) {
      result.append('0');
    }
    result.append(numAsText);

    return result.toString();
  }

  @Nonnull
  private static String formatTimestamp(final long time) {
    final StringBuilder result = new StringBuilder();

    final Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(time);

    result.append(calendar.get(Calendar.YEAR)).append('-').append(ensureNumberFormatting(2, calendar.get(Calendar.MONTH) + 1)).append('-').append(ensureNumberFormatting(2, calendar.get(Calendar.DAY_OF_MONTH)));
    result.append(' ');
    switch (calendar.get(Calendar.DAY_OF_WEEK)) {
      case Calendar.MONDAY:
        result.append("Mon");//NOI18N
        break;
      case Calendar.TUESDAY:
        result.append("Tue");//NOI18N
        break;
      case Calendar.WEDNESDAY:
        result.append("Wed");//NOI18N
        break;
      case Calendar.THURSDAY:
        result.append("Thu");//NOI18N
        break;
      case Calendar.FRIDAY:
        result.append("Fri");//NOI18N
        break;
      case Calendar.SATURDAY:
        result.append("Sat");//NOI18N
        break;
      case Calendar.SUNDAY:
        result.append("Sun");//NOI18N
        break;
      default:
        throw new Error("Unexpected week day");//NOI18N
    }
    result.append(' ');

    result.append(ensureNumberFormatting(2, calendar.get(Calendar.HOUR_OF_DAY))).append(':').append(ensureNumberFormatting(2, calendar.get(Calendar.MINUTE))).append(':').append(ensureNumberFormatting(2, Calendar.SECOND));

    return result.toString();
  }

  @Override
  @Nullable
  public String getMnemonic() {
    return "orgmode";
  }

  private void writeInterTopicLine(@Nonnull final State state) {
    state.nextLine();
  }

  private void writeOtherTopicRecursively(@Nonnull final Topic t, @Nonnull final String topicListNumStr, final int topicIndex, @Nonnull final State state) throws IOException {
    writeInterTopicLine(state);
    final String prefix;
    if (t.getTopicLevel() >= STARTING_INDEX_FOR_NUMERATION) {
      prefix = topicListNumStr + Integer.toString(topicIndex + 1) + '.';//NOI18N
    } else {
      prefix = "";//NOI18N
    }
    writeTopic(t, prefix, state);
    int index = 0;
    for (final Topic ch : t.getChildren()) {
      writeOtherTopicRecursively(ch, prefix, index++, state);
    }
  }

  @Nonnull
  private String makeContent(@Nonnull final MindMapPanel panel) throws IOException {
    final State state = new State();

    final Topic root = panel.getModel().getRoot();

    state.append("#+TITLE: ").append(escapeStr(root == null ? "" : root.getText(), true)).nextLine();//NOI18N
    state.append("#+AUTHOR: ").append(escapeStr(System.getProperty("user.name"), true)).nextLine();//NOI18N
    state.append("#+DATE: ").append(formatTimestamp(System.currentTimeMillis())).nextLine();//NOI18N
    state.append("#+CREATOR: ").append("Generated by [[https://github.com/raydac/netbeans-mmd-plugin][NB Mind Map Plugin]").nextLine();//NOI18N

    state.nextLine();

    if (root != null) {
      writeTopic(root, "", state);//NOI18N

      final Topic[] children = Utils.getLeftToRightOrderedChildrens(root);
      for (final Topic t : children) {
        writeInterTopicLine(state);
        writeTopic(t, "", state);//NOI18N
        int indexChild = 0;
        for (final Topic tt : t.getChildren()) {
          writeOtherTopicRecursively(tt, "", indexChild++, state);//NOI18N
        }
      }
    }

    return state.toString();
  }

  @Override
  public void doExportToClipboard(@Nonnull final PluginContext context, @Nonnull final JComponent options) throws IOException {
    final String text = makeContent(context.getPanel());
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        if (clipboard != null) {
          clipboard.setContents(new StringSelection(text), null);
        }
      }
    });
  }

  @Override
  public void doExport(@Nonnull final PluginContext context, @Nonnull final JComponent options, @Nullable final OutputStream out) throws IOException {
    final String text = makeContent(context.getPanel());

    File fileToSaveMap = null;
    OutputStream theOut = out;
    if (theOut == null) {
      fileToSaveMap = MindMapUtils.selectFileToSaveForFileFilter(context.getPanel(), Texts.getString("ORGMODEExporter.saveDialogTitle"), ".org", Texts.getString("ORGMODEExporter.filterDescription"), Texts.getString("ORGMODEExporter.approveButtonText"));//NOI18N
      fileToSaveMap = MindMapUtils.checkFileAndExtension(context.getPanel(), fileToSaveMap, ".org");//NOI18N
      theOut = fileToSaveMap == null ? null : new BufferedOutputStream(new FileOutputStream(fileToSaveMap, false));
    }
    if (theOut != null) {
      try {
        IOUtils.write(text, theOut, "UTF-8");//NOI18N
      } finally {
        if (fileToSaveMap != null) {
          IOUtils.closeQuietly(theOut);
        }
      }
    }
  }

  @Override
  @Nonnull
  public String getName(@Nonnull final PluginContext context, @Nullable Topic actionTopic) {
    return Texts.getString("ORGMODEExporter.exporterName");//NOI18N
  }

  @Override
  @Nonnull
  public String getReference(@Nonnull final PluginContext context, @Nullable Topic actionTopic) {
    return Texts.getString("ORGMODEExporter.exporterReference");//NOI18N
  }

  @Override
  @Nonnull
  public Icon getIcon(@Nonnull final PluginContext context, @Nullable Topic actionTopic) {
    return ICO;
  }

  @Override
  public int getOrder() {
    return 7;
  }

  private static class State {

    private static final String NEXT_LINE = System.getProperty("line.separator", "\n");//NOI18N
    private final StringBuilder buffer = new StringBuilder(16384);

    @Nonnull
    public State append(final char ch) {
      this.buffer.append(ch);
      return this;
    }

    @Nonnull
    public State append(@Nonnull final String str) {
      this.buffer.append(str);
      return this;
    }

    @Nonnull
    public State nextLine() {
      this.buffer.append(NEXT_LINE);
      return this;
    }

    @Override
    @Nonnull
    public String toString() {
      return this.buffer.toString();
    }

  }

}
