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

package com.igormaznitsa.mindmap.plugins.exporters;

import static com.igormaznitsa.mindmap.model.ModelUtils.escapeMarkdown;
import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.plugins.api.AbstractExporter;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.plugins.api.parameters.AbstractParameter;
import com.igormaznitsa.mindmap.swing.ide.IDEBridgeFactory;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
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
import java.util.Set;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

public class MDExporter extends AbstractExporter {

  private static final int STARTING_INDEX_FOR_NUMERATION = 5;
  private static final Icon ICO = ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_EXPORT_MARKDOWN);

  private static String generateString(final char chr, final int length) {
    final StringBuilder buffer = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      buffer.append(chr);
    }
    return buffer.toString();
  }

  private static String makeLineFromString(final String text) {
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

  private static String getTopicUid(final Topic topic) {
    return topic.getAttribute(ExtraTopic.TOPIC_UID_ATTR);
  }

  private void writeTopic(final Topic topic, final String listPosition,
                          final State state) {
    final int level = topic.getTopicLevel();

    String prefix = "";

    final String topicUid = getTopicUid(topic);
    if (topicUid != null) {
      state.append("<a name=\"").append(topicUid).append("\">").nextLine();
    }

    if (level < STARTING_INDEX_FOR_NUMERATION) {
      final String headerPrefix = generateString('#', topic.getTopicLevel() + 1);
      state.append(headerPrefix).append(' ').append(escapeMarkdown(topic.getText()))
          .nextLine();
    } else {
      final String headerPrefix = generateString('#', STARTING_INDEX_FOR_NUMERATION + 1);
      state.append(prefix).append(headerPrefix).append(' ').append(listPosition).append(' ')
          .append(escapeMarkdown(topic.getText())).nextLine();
    }

    final ExtraFile file = (ExtraFile) this.findExtra(topic, Extra.ExtraType.FILE);
    final ExtraLink link = (ExtraLink) this.findExtra(topic, Extra.ExtraType.LINK);
    final ExtraNote note = (ExtraNote) this.findExtra(topic, Extra.ExtraType.NOTE);
    final ExtraTopic transition = (ExtraTopic) this.findExtra(topic, Extra.ExtraType.TOPIC);

    boolean extrasPrinted = false;

    if (transition != null) {
      final Topic linkedTopic = topic.getMap().findTopicForLink(transition);
      if (linkedTopic != null) {
        state.append(prefix).append("*Related to: ")
            .append('[')
            .append(escapeMarkdown(makeLineFromString(linkedTopic.getText())))
            .append("](")
            .append("#")
            .append(requireNonNull(getTopicUid(linkedTopic)))
            .append(")*")
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
          .append("> File: ")
          .append(escapeMarkdown(
              fileURI.isAbsolute() ? fileURI.asFile(null).getAbsolutePath() : fileURI.toString()))
          .nextStringMarker().nextLine();
      extrasPrinted = true;
    }

    if (link != null) {
      final String url = link.getValue().toString();
      final String ascurl = link.getValue().asString(true, true);
      state.append(prefix)
          .append("> Url: ")
          .append('[')
          .append(escapeMarkdown(url))
          .append("](")
          .append(ascurl)
          .append(')')
          .nextStringMarker()
          .nextLine();
      extrasPrinted = true;
    }

    if (note != null) {
      if (extrasPrinted) {
        state.nextLine();
      }
      state.append(prefix)
          .append("<pre>")
          .append(StringEscapeUtils.escapeHtml3(note.getValue()))
          .append("</pre>")
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

  private void writeInterTopicLine(final State state) {
    state.nextLine();
  }

  private void writeOtherTopicRecursively(final Topic t, final String topicListNumStr,
                                          final int topicIndex, final State state) {
    writeInterTopicLine(state);
    final String prefix;
    if (t.getTopicLevel() >= STARTING_INDEX_FOR_NUMERATION) {
      prefix = topicListNumStr + (topicIndex + 1) + '.';
    } else {
      prefix = "";
    }
    writeTopic(t, prefix, state);
    int index = 0;
    for (final Topic ch : t.getChildren()) {
      writeOtherTopicRecursively(ch, prefix, index++, state);
    }
  }

  private String makeContent(final MindMapPanel panel) {
    final State state = new State();

    state.append("<!--")
        .nextLine()
        .append(
            "Generated by " + IDEBridgeFactory.findInstance().getIDEGeneratorId() + ' ' +
                IDEBridgeFactory.findInstance().getIDEVersion() + " (https://sciareto.org)")
        .nextLine();
    state.append(DATE_FORMAT.format(new java.util.Date().getTime())).nextLine().append("-->")
        .nextLine();

    final Topic root = panel.getModel().getRoot();
    if (root != null) {
      writeTopic(root, "", state);

      final Topic[] children = Utils.getLeftToRightOrderedChildrens(root);
      for (final Topic t : children) {
        writeInterTopicLine(state);
        writeTopic(t, "", state);
        int indexChild = 0;
        for (final Topic tt : t.getChildren()) {
          writeOtherTopicRecursively(tt, "", indexChild++, state);
        }
      }
    }

    return state.toString();
  }

  @Override
  public void doExportToClipboard(final PluginContext context, final Set<AbstractParameter<?>> options)
      throws IOException {
    final String text = makeContent(context.getPanel());
    SwingUtilities.invokeLater(() -> {
      final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      if (clipboard != null) {
        clipboard.setContents(new StringSelection(text), null);
      }
    });
  }

  @Override
  public void doExport(final PluginContext context, final Set<AbstractParameter<?>> options,
                       final OutputStream out) throws IOException {
    final String text = makeContent(context.getPanel());

    File fileToSaveMap = null;
    OutputStream theOut = out;
    if (theOut == null) {
      fileToSaveMap = MindMapUtils.selectFileToSaveForFileFilter(
          context.getPanel(),
          context,
          this.getClass().getName(),
          this.getResourceBundle().getString("MDExporter.saveDialogTitle"),
          null,
          ".MD",
          this.getResourceBundle().getString("MDExporter.filterDescription"),
          this.getResourceBundle().getString("MDExporter.approveButtonText"));
      fileToSaveMap = MindMapUtils.checkFileAndExtension(context.getPanel(), fileToSaveMap, ".MD");
      theOut = fileToSaveMap == null ? null :
          new BufferedOutputStream(new FileOutputStream(fileToSaveMap, false));
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
  public String getMnemonic() {
    return "markdown";
  }

  @Override
  public String getName(final PluginContext context, final Topic actionTopic) {
    return this.getResourceBundle().getString("MDExporter.exporterName");
  }

  @Override
  public String getReference(final PluginContext context, final Topic actionTopic) {
    return this.getResourceBundle().getString("MDExporter.exporterReference");
  }

  @Override
  public Icon getIcon(final PluginContext context, final Topic actionTopic) {
    return ICO;
  }

  @Override
  public int getOrder() {
    return 4;
  }

  private static class State {

    private static final String NEXT_LINE = System.getProperty("line.separator", "\n");
    private final StringBuilder buffer = new StringBuilder(16384);

    public State append(final char ch) {
      this.buffer.append(ch);
      return this;
    }

    public State nextStringMarker() {
      this.buffer.append("  ");
      return this;
    }

    public State append(final String str) {
      this.buffer.append(str);
      return this;
    }

    public State nextLine() {
      this.buffer.append(NEXT_LINE);
      return this;
    }

    @Override
    public String toString() {
      return this.buffer.toString();
    }

  }

}
