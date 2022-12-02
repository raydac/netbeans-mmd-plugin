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

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.plugins.api.AbstractExporter;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.plugins.api.parameters.AbstractParameter;
import com.igormaznitsa.mindmap.swing.ide.IDEBridgeFactory;
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
import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class TextExporter extends AbstractExporter {

  private static final int SHIFT_STEP = 1;
  private static final Icon ICO = ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_EXPORT_TEXT);

  private static String[] split(final String text) {
    return text.replace("\r", "").split("\\n");
  }

  private static String replaceAllNextLineSeq(final String text, final String newNextLine) {
    return text.replace("\r", "").replace("\n", newNextLine);
  }

  private static String shiftString(final String text, final char fill, final int shift) {
    final String[] lines = split(text);
    final StringBuilder builder = new StringBuilder();
    final String line = generateString(fill, shift);
    boolean nofirst = false;
    for (final String s : lines) {
      if (nofirst) {
        builder.append(State.NEXT_LINE);
      } else {
        nofirst = true;
      }
      builder.append(line).append(s);
    }
    return builder.toString();
  }

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

  private static int getMaxLineWidth(final String text) {
    final String[] lines = replaceAllNextLineSeq(text, "\n").split("\\n");
    int max = 0;
    for (final String s : lines) {
      max = Math.max(s.length(), max);
    }
    return max;
  }

  private void writeTopic(final Topic topic, final char ch, final int shift,
                          final State state) {
    final int maxLen = getMaxLineWidth(topic.getText());
    state.append(shiftString(topic.getText(), ' ', shift)).nextLine()
        .append(shiftString(generateString(ch, maxLen + 2), ' ', shift)).nextLine();

    final ExtraFile file = (ExtraFile) this.findExtra(topic, Extra.ExtraType.FILE);
    final ExtraLink link = (ExtraLink) this.findExtra(topic, Extra.ExtraType.LINK);
    final ExtraNote note = (ExtraNote) this.findExtra(topic, Extra.ExtraType.NOTE);
    final ExtraTopic transition = (ExtraTopic) this.findExtra(topic, Extra.ExtraType.TOPIC);

    boolean hasExtras = false;
    boolean extrasPrinted = false;

    if (file != null || link != null || note != null || transition != null) {
      hasExtras = true;
    }

    if (file != null) {
      final String uri = file.getValue().asString(false, false);
      state.append(shiftString("FILE: ", ' ', shift)).append(uri).nextLine();
      extrasPrinted = true;
    }

    if (link != null) {
      final String uri = link.getValue().asString(false, false);
      state.append(shiftString("URL: ", ' ', shift)).append(uri).nextLine();
      extrasPrinted = true;
    }

    if (transition != null) {
      final Topic linkedTopic = topic.getMap().findTopicForLink(transition);
      state.append(shiftString("Related to: ", ' ', shift)).append(
          linkedTopic == null ? "<UNKNOWN>" :
              '\"' + makeLineFromString(linkedTopic.getText()) + "\"").nextLine();
      extrasPrinted = true;
    }

    if (note != null) {
      if (extrasPrinted) {
        state.nextLine();
      }
      state.append(shiftString(note.getValue(), ' ', shift)).nextLine();
    }

    final Map<String, String> codeSnippets = topic.getCodeSnippets();
    if (!codeSnippets.isEmpty()) {
      boolean first = true;
      for (final Map.Entry<String, String> e : codeSnippets.entrySet()) {
        final String lang = e.getKey();

        if (!first) {
          state.nextLine();
        } else {
          first = false;
        }

        state.append(shiftString("====BEGIN SOURCE (" + lang + ')', ' ', shift)).nextLine();

        final String body = e.getValue();
        for (final String s : StringUtils.split(body, '\n')) {
          state.append(shiftString(Utils.removeAllISOControlsButTabs(s), ' ', shift)).nextLine();
        }

        state.append(shiftString("====END SOURCE", ' ', shift)).nextLine();
      }
    }

  }

  private void writeInterTopicLine(final State state) {
    state.nextLine();
  }

  private void writeOtherTopicRecursively(final Topic t, int shift, final State state) {
    writeInterTopicLine(state);
    writeTopic(t, '.', shift, state);
    shift += SHIFT_STEP;
    for (final Topic ch : t.getChildren()) {
      writeOtherTopicRecursively(ch, shift, state);
    }
  }

  private String makeContent(final PluginContext context) {
    final State state = new State();

    state.append(
            "# ").append("Generated by " + IDEBridgeFactory.findInstance().getIDEGeneratorId() + ' ' +
            IDEBridgeFactory.findInstance().getIDEVersion() + " (https://sciareto.org)")
        .nextLine();
    state.append("# ").append(new Timestamp(new java.util.Date().getTime()).toString()).nextLine()
        .nextLine();

    int shift = 0;

    final Topic root = context.getPanel().getModel().getRoot();
    if (root != null) {
      writeTopic(root, '=', shift, state);

      shift += SHIFT_STEP;

      final Topic[] children = Utils.getLeftToRightOrderedChildrens(root);
      for (final Topic t : children) {
        writeInterTopicLine(state);
        writeTopic(t, '-', shift, state);
        shift += SHIFT_STEP;
        for (final Topic tt : t.getChildren()) {
          writeOtherTopicRecursively(tt, shift, state);
        }
        shift -= SHIFT_STEP;
      }
    }

    return state.toString();
  }

  @Override
  public void doExportToClipboard(final PluginContext context, final Set<AbstractParameter<?>> options)
      throws IOException {
    final String text = makeContent(context);
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
    final String text = makeContent(context);

    File fileToSaveMap = null;
    OutputStream theOut = out;
    if (theOut == null) {
      fileToSaveMap = MindMapUtils.selectFileToSaveForFileFilter(
          context.getPanel(),
          context,
          this.getClass().getName(),
          this.getResourceBundle().getString("TextExporter.saveDialogTitle"),
          null,
          ".txt",
          this.getResourceBundle().getString("TextExporter.filterDescription"),
          this.getResourceBundle().getString("TextExporter.approveButtonText"));
      fileToSaveMap = MindMapUtils.checkFileAndExtension(context.getPanel(), fileToSaveMap, ".txt");
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
    return "text";
  }

  @Override
  public String getName(final PluginContext context, final Topic actionTopic) {
    return this.getResourceBundle().getString("TextExporter.exporterName");
  }

  @Override
  public String getReference(final PluginContext context, final Topic actionTopic) {
    return this.getResourceBundle().getString("TextExporter.exporterReference");
  }

  @Override
  public Icon getIcon(final PluginContext context, final Topic actionTopic) {
    return ICO;
  }

  @Override
  public int getOrder() {
    return 6;
  }

  private static class State {

    private static final String NEXT_LINE = System.getProperty("line.separator", "\n");
    private final StringBuilder buffer = new StringBuilder(16384);

    public State append(final char ch) {
      this.buffer.append(ch);
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
