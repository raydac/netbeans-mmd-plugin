/*
 * Copyright 2017 Igor Maznitsa.
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

import com.igormaznitsa.mindmap.plugins.api.AbstractExporter;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.Date;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JComponent;

import org.apache.commons.io.IOUtils;
import com.igormaznitsa.mindmap.swing.panel.utils.MindMapUtils;
import com.igormaznitsa.mindmap.swing.panel.Texts;
import com.igormaznitsa.mindmap.swing.services.IconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import javax.swing.Icon;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.ModelUtils;

public class ASCIIDocExporter extends AbstractExporter {

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

  private static final Icon ICO = ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_EXPORT_ASCIIDOC);

  @Nonnull
  private static String escapeAsciiDoc(@Nonnull final String text, final boolean head) {
    String result = text;
    if (head) {
      result = text.replace("\n", " pass:[<br>]");
    } else {
      result = text;
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

  private static void writeTopic(@Nonnull final Topic topic, @Nonnull final State state) throws IOException {
    final int level = topic.getTopicLevel();
    final String uid = getTopicUid(topic);
    
    if (uid!=null) {
      state.append("anchor:").append(uid).append("[]").appendNextLine().appendNextLine();
    }
    final String prefix = generateString('=', level + 1);
    state.append(prefix).append(' ').appendHead(topic.getText()).appendNextLine();

    if (level == 0) {
      state.append(":encoding: UTF-8").appendNextLine();
      state.append(":Date: ").append(DATE_FORMAT.format(new Date())).appendNextLine();
    }
    state.appendNextLine();

    final ExtraFile file = (ExtraFile) topic.getExtras().get(Extra.ExtraType.FILE);
    final ExtraLink link = (ExtraLink) topic.getExtras().get(Extra.ExtraType.LINK);
    final ExtraNote note = (ExtraNote) topic.getExtras().get(Extra.ExtraType.NOTE);
    final ExtraTopic transition = (ExtraTopic) topic.getExtras().get(Extra.ExtraType.TOPIC);

    if (note != null) {
      state.appendParagraphText(note.getValue());
      state.appendNextLine();
    }

    if (file != null) {
      final MMapURI fileURI = file.getValue();
      final String filePathAsText = fileURI.isAbsolute() ? fileURI.asFile(null).getAbsolutePath() : fileURI.toString();
      state.append("link:++").append(filePathAsText).append("++[File]").appendNextLine().appendNextLine();
    }

    if (link != null) {
      final String url = link.getValue().toString();
      final String ascurl = link.getValue().asString(true, true);
      state.append("link:").append(ascurl).append("[Link]").appendNextLine().appendNextLine();
    }

    if (transition != null) {
      final Topic linkedTopic = topic.getMap().findTopicForLink(transition);
      state.append("<<").append(Assertions.assertNotNull(getTopicUid(linkedTopic))).append(",Go to>>").appendNextLine().appendNextLine();
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

  @Override
  public void doExport(@Nonnull final MindMapPanel panel, @Nonnull final JComponent options, @Nullable final OutputStream out) throws IOException {
    final State state = new State();
    state.append("// Generated by NB-MindMap AsciiDoc exporter https://github.com/raydac/netbeans-mmd-plugin").appendNextLine();
    
    final Topic root = panel.getModel().getRoot();
    if (root != null) {
      writeTopic(root, state);//NOI18N
    }

    final String text = state.toString();
    
    File fileToSaveMap = null;
    OutputStream theOut = out;
    if (theOut == null) {
      fileToSaveMap = MindMapUtils.selectFileToSaveForFileFilter(panel, Texts.getString("ASCIIDOCExporter.saveDialogTitle"), ".asciidoc", Texts.getString("ASCIIDOCExporter.filterDescription"), Texts.getString("ASCIIDOCExporter.approveButtonText"));
      fileToSaveMap = MindMapUtils.checkFileAndExtension(panel, fileToSaveMap, ".asciidoc");//NOI18N
      theOut = fileToSaveMap == null ? null : new BufferedOutputStream(new FileOutputStream(fileToSaveMap, false));
    }
    if (theOut != null) {
      try {
        IOUtils.write(text, theOut, "UTF-8");
      }
      finally {
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
  public String getName(@Nonnull final MindMapPanel panel, @Nullable Topic actionTopic, @Nonnull @MustNotContainNull Topic[] selectedTopics) {
    return Texts.getString("ASCIIDOCExporter.exporterName");
  }

  @Override
  @Nonnull
  public String getReference(@Nonnull final MindMapPanel panel, @Nullable Topic actionTopic, @Nonnull @MustNotContainNull Topic[] selectedTopics) {
    return Texts.getString("ASCIIDOCExporter.exporterReference");
  }

  @Override
  @Nonnull
  public Icon getIcon(@Nonnull final MindMapPanel panel, @Nullable Topic actionTopic, @Nonnull @MustNotContainNull Topic[] selectedTopics) {
    return ICO;
  }

  @Override
  public int getOrder() {
    return 8;
  }

}
