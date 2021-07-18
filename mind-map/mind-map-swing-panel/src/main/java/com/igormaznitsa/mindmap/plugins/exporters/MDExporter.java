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
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

public class MDExporter extends AbstractExporter {

  private static final int STARTING_INDEX_FOR_NUMERATION = 5;
  private static final Icon ICO = ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_EXPORT_MARKDOWN);

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

  private void writeTopic(@Nonnull final Topic topic, @Nonnull final String listPosition,
                          @Nonnull final State state) throws IOException {
    final int level = topic.getTopicLevel();

    String prefix = "";//NOI18N

    final String topicUid = getTopicUid(topic);
    if (topicUid != null) {
      state.append("<a name=\"").append(topicUid).append("\">").nextLine();//NOI18N
    }

    if (level < STARTING_INDEX_FOR_NUMERATION) {
      final String headerPrefix = generateString('#', topic.getTopicLevel() + 1);//NOI18N
      state.append(headerPrefix).append(' ').append(ModelUtils.escapeMarkdownStr(topic.getText()))
          .nextLine();
    } else {
      final String headerPrefix = generateString('#', STARTING_INDEX_FOR_NUMERATION + 1);//NOI18N
      state.append(prefix).append(headerPrefix).append(' ').append(listPosition).append(' ')
          .append(ModelUtils.escapeMarkdownStr(topic.getText())).nextLine();
    }

    final ExtraFile file = (ExtraFile) this.findExtra(topic, Extra.ExtraType.FILE);
    final ExtraLink link = (ExtraLink) this.findExtra(topic, Extra.ExtraType.LINK);
    final ExtraNote note = (ExtraNote) this.findExtra(topic, Extra.ExtraType.NOTE);
    final ExtraTopic transition = (ExtraTopic) this.findExtra(topic, Extra.ExtraType.TOPIC);

    boolean extrasPrinted = false;

    if (transition != null) {
      final Topic linkedTopic = topic.getMap().findTopicForLink(transition);
      if (linkedTopic != null) {
        state.append(prefix).append("*Related to: ")//NOI18N
            .append('[')//NOI18N
            .append(ModelUtils.escapeMarkdownStr(makeLineFromString(linkedTopic.getText())))
            .append("](")//NOI18N
            .append("#")//NOI18N
            .append(assertNotNull(getTopicUid(linkedTopic)))
            .append(")*")//NOI18N
            .nextStringMarker()
            .nextLine();
        extrasPrinted = true;
        if (file != null || link != null || note != null) {
          state.nextStringMarker().nextLine();
        }
      }
    }

    if (file != null) {
      final MMapURI fileURI = file.getValue();
      state.append(prefix)
          .append("> File: ")//NOI18N
          .append(ModelUtils.escapeMarkdownStr(fileURI.isAbsolute() ? fileURI.asFile(null).getAbsolutePath() : fileURI.toString())).nextStringMarker().nextLine();
      extrasPrinted = true;
    }

    if (link != null) {
      final String url = link.getValue().toString();
      final String ascurl = link.getValue().asString(true, true);
      state.append(prefix)
          .append("> Url: ")//NOI18N
          .append('[')//NOI18N
          .append(ModelUtils.escapeMarkdownStr(url))
          .append("](")//NOI18N
          .append(ascurl)
          .append(')')//NOI18N
          .nextStringMarker()
          .nextLine();
      extrasPrinted = true;
    }

    if (note != null) {
      if (extrasPrinted) {
        state.nextLine();
      }
      state.append(prefix)
          .append("<pre>")//NOI18N
          .append(StringEscapeUtils.escapeHtml3(note.getValue()))
          .append("</pre>")//NOI18N
          .nextLine();
    }

    final Map<String, String> codeSnippets = topic.getCodeSnippets();
    if (!codeSnippets.isEmpty()) {
      for (final Map.Entry<String, String> e : codeSnippets.entrySet()) {
        final String lang = e.getKey();

        state.append("```").append(lang).nextLine();

        final String body = e.getValue();
        for (final String s : StringUtils.split(body, '\n')) {
          state.append(Utils.removeAllISOControlsButTabs(s)).nextLine();
        }

        state.append("```").nextLine();
      }
    }
  }

  private void writeInterTopicLine(@Nonnull final State state) {
    state.nextLine();
  }

  private void writeOtherTopicRecursively(@Nonnull final Topic t, @Nonnull final String topicListNumStr, final int topicIndex, @Nonnull final State state) throws IOException {
    writeInterTopicLine(state);
    final String prefix;
    if (t.getTopicLevel() >= STARTING_INDEX_FOR_NUMERATION) {
      prefix = topicListNumStr + (topicIndex + 1) + '.';//NOI18N
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

    state.append("<!--")//NOI18N
        .nextLine()//NOI18N
        .append("Generated by NB Mind Map Plugin (https://github.com/raydac/netbeans-mmd-plugin)")//NOI18N
        .nextLine();//NOI18N
    state.append(DATE_FORMAT.format(new java.util.Date().getTime())).nextLine().append("-->").nextLine();//NOI18N

    final Topic root = panel.getModel().getRoot();
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
      fileToSaveMap = MindMapUtils.selectFileToSaveForFileFilter(
          context.getPanel(),
          context,
          this.getClass().getName(),
          Texts.getString("MDExporter.saveDialogTitle"),
          null,
          ".MD",
          Texts.getString("MDExporter.filterDescription"),
          Texts.getString("MDExporter.approveButtonText"));
      fileToSaveMap = MindMapUtils.checkFileAndExtension(context.getPanel(), fileToSaveMap, ".MD");//NOI18N
      theOut = fileToSaveMap == null ? null : new BufferedOutputStream(new FileOutputStream(fileToSaveMap, false));
    }
    if (theOut != null) {
      try {
        IOUtils.write(text, theOut, "UTF-8");
      } finally {
        if (fileToSaveMap != null) {
          IOUtils.closeQuietly(theOut);
        }
      }
    }
  }

  @Override
  @Nullable
  public String getMnemonic() {
    return "markdown";
  }

  @Override
  @Nonnull
  public String getName(@Nonnull final PluginContext context, @Nullable Topic actionTopic) {
    return Texts.getString("MDExporter.exporterName");
  }

  @Override
  @Nonnull
  public String getReference(@Nonnull final PluginContext context, @Nullable Topic actionTopic) {
    return Texts.getString("MDExporter.exporterReference");
  }

  @Override
  @Nonnull
  public Icon getIcon(@Nonnull final PluginContext context, @Nullable Topic actionTopic) {
    return ICO;
  }

  @Override
  public int getOrder() {
    return 3;
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
    public State nextStringMarker() {
      this.buffer.append("  ");//NOI18N
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
