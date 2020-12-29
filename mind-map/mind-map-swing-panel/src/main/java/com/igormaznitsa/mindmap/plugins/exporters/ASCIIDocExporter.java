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

import com.igormaznitsa.meta.common.utils.Assertions;
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
import java.util.Date;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.apache.commons.io.IOUtils;

public class ASCIIDocExporter extends AbstractExporter {

  private static final Icon ICO = ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_EXPORT_ASCIIDOC);

  @Nonnull
  private static String escapeAsciiDoc(@Nonnull final String text, final boolean head) {
    String result = text;
    if (head) {
      result = text.replace("\n", " pass:[<br>]");
    }
    return result;
  }

  @Nonnull
  private static String generateString(final char chr, final int length) {
    final StringBuilder buffer = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      buffer.append(chr);
    }
    return buffer.toString();
  }

  @Nullable
  private static String getTopicUid(@Nonnull final Topic topic) {
    return topic.getAttribute(ExtraTopic.TOPIC_UID_ATTR);
  }

  private void writeTopic(@Nonnull final Topic topic, @Nonnull final State state)
      throws IOException {
    final int level = topic.getTopicLevel();
    final String uid = getTopicUid(topic);

    if (uid != null) {
      state.append("anchor:").append(uid).append("[]").appendNextLine().appendNextLine();
    }
    final String prefix = generateString('=', level + 1);
    state.append(prefix).append(' ').appendHead(topic.getText()).appendNextLine();

    if (level == 0) {
      state.append(":encoding: UTF-8").appendNextLine();
      state.append(":Date: ").append(DATE_FORMAT.format(new Date())).appendNextLine();
    }
    state.appendNextLine();

    final ExtraFile file = (ExtraFile) this.findExtra(topic, Extra.ExtraType.FILE);
    final ExtraLink link = (ExtraLink) this.findExtra(topic, Extra.ExtraType.LINK);
    final ExtraNote note = (ExtraNote) this.findExtra(topic, Extra.ExtraType.NOTE);
    final ExtraTopic transition = (ExtraTopic) this.findExtra(topic, Extra.ExtraType.TOPIC);

    if (note != null) {
      state.appendParagraphText(note.getValue());
      state.appendNextLine();
    }

    if (file != null) {
      final MMapURI fileURI = file.getValue();
      final String filePathAsText =
          fileURI.isAbsolute() ? fileURI.asFile(null).getAbsolutePath() : fileURI.toString();
      state.append("link:++").append(filePathAsText).append("++[File]").appendNextLine()
          .appendNextLine();
    }

    if (link != null) {
      final String url = link.getValue().toString();
      final String ascurl = link.getValue().asString(true, true);
      state.append("link:").append(ascurl).append("[Link]").appendNextLine().appendNextLine();
    }

    if (transition != null) {
      final Topic linkedTopic = topic.getMap().findTopicForLink(transition);
      if (linkedTopic != null) {
        state.append("<<").append(Assertions.assertNotNull(getTopicUid(linkedTopic))).append(",Go to>>").appendNextLine().appendNextLine();
      }
    }

    for (final Map.Entry<String, String> s : topic.getCodeSnippets().entrySet()) {
      state.append("[source,").append(s.getKey()).append("]").appendNextLine();
      state.append("----").appendNextLine();
      state.append(s.getValue());
      state.appendConditionalNextLine();
      state.append("----").appendNextLine().appendNextLine();
    }

    for (final Topic t : topic.getChildren()) {
      writeTopic(t, state);
    }
  }

  @Nonnull
  private String makeContent(@Nonnull final MindMapPanel panel) throws IOException {
    final State state = new State();
    state.append("// Generated by NB-MindMap AsciiDoc exporter https://github.com/raydac/netbeans-mmd-plugin").appendNextLine();
    final Topic root = panel.getModel().getRoot();
    if (root != null) {
      writeTopic(root, state);//NOI18N
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
          Texts.getString("ASCIIDOCExporter.saveDialogTitle"),
          null,
          ".asciidoc",
          Texts.getString("ASCIIDOCExporter.filterDescription"),
          Texts.getString("ASCIIDOCExporter.approveButtonText"));
      fileToSaveMap = MindMapUtils.checkFileAndExtension(context.getPanel(), fileToSaveMap, ".asciidoc");//NOI18N
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
    return "asciidoc";
  }

  @Override
  @Nonnull
  public String getName(@Nonnull final PluginContext context, @Nullable final Topic actionTopic) {
    return Texts.getString("ASCIIDOCExporter.exporterName");
  }

  @Override
  @Nonnull
  public String getReference(@Nonnull final PluginContext context, @Nullable Topic actionTopic) {
    return Texts.getString("ASCIIDOCExporter.exporterReference");
  }

  @Override
  @Nonnull
  public Icon getIcon(@Nonnull final PluginContext context, @Nullable Topic actionTopic) {
    return ICO;
  }

  @Override
  public int getOrder() {
    return 8;
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
    public State appendHead(@Nonnull final String str) {
      this.buffer.append(escapeAsciiDoc(str, true));
      return this;
    }

    @Nonnull
    public State appendParagraphText(@Nonnull final String str) {
      for (final String s : ModelUtils.breakToLines(str)) {
        this.buffer.append(escapeAsciiDoc(s, false)).append(" +");
        appendNextLine();
      }
      return this;
    }

    @Nonnull
    public State append(@Nonnull final String str) {
      this.buffer.append(str);
      return this;
    }

    @Nonnull
    public State appendNextLine() {
      this.buffer.append(NEXT_LINE);
      return this;
    }

    @Nonnull
    public State appendConditionalNextLine() {
      if (this.buffer.charAt(this.buffer.length() - 1) != '\n') {
        this.buffer.append(NEXT_LINE);
      }
      return this;
    }

    @Override
    @Nonnull
    public String toString() {
      return this.buffer.toString();
    }

  }

}
