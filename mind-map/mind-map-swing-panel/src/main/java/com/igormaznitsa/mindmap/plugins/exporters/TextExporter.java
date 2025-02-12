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

import static com.igormaznitsa.mindmap.model.StandardTopicAttributes.MMD_TOPIC_ATTRIBUTE_LINK_UID;

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
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class TextExporter extends AbstractExporter {

  private static final int SHIFT_STEP = 1;
  private static final Icon ICO =
      ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_EXPORT_TEXT);

  private static String[] split(final String text) {
    return text.replace("\r", "").split("\\n");
  }

  private static String replaceAllNextLineSeq(final String text, final String newNextLine) {
    String result = text.replace("\r", "");
    if (!"\n".equals(newNextLine)) {
      result = result.replace("\n", newNextLine);
    }
    return result;
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

  private static final ExtrasToStringConverter DEFAULT_TEXT_EXTRAS_CONVERTER =
      new ExtrasToStringConverter() {
        @Override
        public String apply(final PluginContext pluginContext, final Extra<?> extra) {
          switch (extra.getType()) {
            case FILE:
              return ((ExtraFile) extra).getValue().asString(false, false);
            case LINK:
              return ((ExtraLink) extra).getValue().asString(false, true);
            case NOTE:
              return ((ExtraNote) extra).getValue();
            case TOPIC:
              return ((ExtraTopic) extra).getValue();
            default:
              throw new IllegalArgumentException("Unknown extras: " + extra);
          }
        }
      };

  @Override
  public ExtrasToStringConverter getDefaultExtrasStringConverter() {
    return DEFAULT_TEXT_EXTRAS_CONVERTER;
  }

  private void writeTopic(
      final PluginContext pluginContext,
      final Topic topic, final char ch, final int shift,
      final State state, final ExtrasToStringConverter stringConverter) {
    final int maxLen = getMaxLineWidth(topic.getText());
    final String uid = topic.getAttribute(MMD_TOPIC_ATTRIBUTE_LINK_UID);
    state.append(shiftString(topic.getText(), ' ', shift))
        .append(uid == null ? "" : " [" + uid + ']')
        .nextLine()
        .append(shiftString(generateString(ch, maxLen + 2), ' ', shift)).nextLine();

    final ExtraFile file = (ExtraFile) this.findExtra(topic, Extra.ExtraType.FILE);
    final ExtraLink link = (ExtraLink) this.findExtra(topic, Extra.ExtraType.LINK);
    final ExtraNote note = (ExtraNote) this.findExtra(topic, Extra.ExtraType.NOTE);
    final ExtraTopic transition = (ExtraTopic) this.findExtra(topic, Extra.ExtraType.TOPIC);

    boolean extrasPrinted = false;

    if (file != null) {
      final String line = file.getValue().getParameters().getProperty("line");
      state.append(shiftString("FILE: ", ' ', shift))
          .append(stringConverter.apply(pluginContext, file))
          .append(line == null ? "" : ':' + line)
          .nextLine();
      extrasPrinted = true;
    }

    if (link != null) {
      state.append(shiftString("URL: ", ' ', shift))
          .append(stringConverter.apply(pluginContext, link)).nextLine();
      extrasPrinted = true;
    }

    if (transition != null) {
      final Topic linkedTopic = topic.getMap().findTopicForLink(transition);
      if (linkedTopic != null) {
        state.append(shiftString("Related to: ", ' ', shift))
            .append('\"')
            .append(makeLineFromString(linkedTopic.getText()))
            .append("\" [")
            .append(stringConverter.apply(pluginContext, transition))
            .append(']')
            .nextLine();

        extrasPrinted = true;
      }
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

  private void writeOtherTopicRecursively(
      final PluginContext context,
      final Topic topic,
      int shift,
      final State state,
      final ExtrasToStringConverter stringConverter) {
    writeInterTopicLine(state);
    writeTopic(context, topic, '.', shift, state, stringConverter);
    shift += SHIFT_STEP;
    for (final Topic ch : topic.getChildren()) {
      writeOtherTopicRecursively(context, ch, shift, state, stringConverter);
    }
  }

  private String makeContent(final PluginContext context,
                             final ExtrasToStringConverter stringConverter) {
    final State state = new State();

    state.append(
            "# ").append("Generated by " + IDEBridgeFactory.findInstance().getIDEGeneratorId() + ' ' +
            IDEBridgeFactory.findInstance().getIDEVersion() + " (https://sciareto.org)")
        .nextLine();
    state.append("# ").append(
            DateTimeFormatter.ISO_DATE_TIME.format(Instant.now().atZone(ZoneId.systemDefault())))
        .nextLine()
        .nextLine();

    int shift = 0;

    final Topic root = context.getModel().getRoot();
    if (root != null) {
      writeTopic(context, root, '=', shift, state, stringConverter);

      shift += SHIFT_STEP;

      final Topic[] children = Utils.getLeftToRightOrderedChildrens(root);
      for (final Topic t : children) {
        writeInterTopicLine(state);
        writeTopic(context, t, '-', shift, state, stringConverter);
        shift += SHIFT_STEP;
        for (final Topic tt : t.getChildren()) {
          writeOtherTopicRecursively(context, tt, shift, state, stringConverter);
        }
        shift -= SHIFT_STEP;
      }
    }

    return state.toString();
  }

  @Override
  public void doExportToClipboard(final PluginContext context,
                                  final Set<AbstractParameter<?>> options,
                                  final ExtrasToStringConverter stringConverter)
      throws IOException {
    final String text = makeContent(context, stringConverter);
    SwingUtilities.invokeLater(() -> {
      final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      if (clipboard != null) {
        clipboard.setContents(new StringSelection(text), null);
      }
    });
  }

  @Override
  public void doExport(
      final PluginContext context,
      final Set<AbstractParameter<?>> options,
      final OutputStream out,
      final ExtrasToStringConverter stringConverter
  ) throws IOException {
    final String text = makeContent(context, stringConverter);

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
    return 7;
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
