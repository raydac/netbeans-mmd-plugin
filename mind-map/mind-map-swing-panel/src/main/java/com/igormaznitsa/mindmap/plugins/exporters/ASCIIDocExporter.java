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

import static java.util.Objects.requireNonNull;

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
import com.igormaznitsa.mindmap.plugins.api.parameters.AbstractParameter;
import com.igormaznitsa.mindmap.swing.ide.IDEBridgeFactory;
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
import java.util.Set;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.apache.commons.io.IOUtils;

public class ASCIIDocExporter extends AbstractExporter {

  private static final Icon ICO = ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_EXPORT_ASCIIDOC);

  private static String escapeAsciiDoc(final String text, final boolean head) {
    String result = text;
    if (head) {
      result = text.replace("\n", " pass:[<br>]");
    }
    return result;
  }

  private static final ExtrasToStringConverter ASCII_STRING_CONVERTER =
      new ExtrasToStringConverter() {
        @Override
        public String apply(PluginContext pluginContext, Extra<?> extra) {
          switch (extra.getType()) {
            case FILE: {
              final MMapURI fileURI = ((ExtraFile) extra).getValue();
              return fileURI.isAbsolute() ? fileURI.asFile(null).getAbsolutePath() :
                  fileURI.toString();
            }
            case LINK: {
              return ((ExtraLink) extra).getValue().asString(true, true);
            }
            case TOPIC: {
              return ((ExtraTopic) extra).getValue();
            }
            case NOTE: {
              return ((ExtraNote) extra).getValue();
            }
            default:
              throw new IllegalArgumentException("Unsupported extra: " + extra);
          }
        }
      };

  private static String getTopicUid(final Topic topic) {
    return topic.getAttribute(ExtraTopic.TOPIC_UID_ATTR);
  }

  private static String generateString(final char symbol, final int length) {
    final StringBuilder buffer = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      buffer.append(symbol);
    }
    return buffer.toString();
  }

  @Override
  public ExtrasToStringConverter getDefaultStringConverter() {
    return ASCII_STRING_CONVERTER;
  }

  private void writeTopic(
      final PluginContext pluginContext,
      final Topic topic,
      final State state,
      final ExtrasToStringConverter stringConverter) {
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
      state.appendParagraphText(stringConverter.apply(pluginContext, note));
      state.appendNextLine();
    }

    if (file != null) {
      final String filePathAsText = stringConverter.apply(pluginContext, file);
      state.append("link:++").append(filePathAsText).append("++[File]").appendNextLine()
          .appendNextLine();
    }

    if (link != null) {
      state.append("link:").append(stringConverter.apply(pluginContext, link)).append("[Link]")
          .appendNextLine().appendNextLine();
    }

    if (transition != null) {
      final Topic linkedTopic = topic.getMap().findTopicForLink(transition);
      if (linkedTopic != null) {
        final String topicUid = stringConverter.apply(pluginContext, transition);
        state.append("<<").append(requireNonNull(topicUid)).append(",Go to>>")
            .appendNextLine().appendNextLine();
      }
    }

    for (final Map.Entry<String, String> s : topic.getCodeSnippets().entrySet()) {
      state.append("[source,").append(s.getKey()).append("]").appendNextLine();
      state.append("----").appendNextLine();
      state.append(s.getValue());
      state.appendConditionalNextLine();
      state.append("----").appendNextLine().appendNextLine();
    }

    for (final Topic childTopic : topic.getChildren()) {
      writeTopic(pluginContext, childTopic, state, stringConverter);
    }
  }

  private String makeContent(final PluginContext context,
                             final ExtrasToStringConverter stringConverter) {
    final State state = new State();
    state.append("// Generated by ")
        .append(IDEBridgeFactory.findInstance().getIDEGeneratorId())
        .append(' ')
        .append(IDEBridgeFactory.findInstance().getIDEVersion().toString())
        .append(" (https://sciareto.org)")
        .appendNextLine();

    final Topic root = context.getModel().getRoot();
    if (root != null) {
      this.writeTopic(context, root, state, stringConverter);
    }
    return state.toString();
  }

  @Override
  public void doExportToClipboard(
      final PluginContext context,
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
  public void doExport(final PluginContext context, final Set<AbstractParameter<?>> options,
                       final OutputStream out, final ExtrasToStringConverter stringConverter)
      throws IOException {
    final String text = makeContent(context, stringConverter);

    File fileToSaveMap = null;
    OutputStream theOut = out;
    if (theOut == null) {
      fileToSaveMap = MindMapUtils.selectFileToSaveForFileFilter(
          context.getPanel(),
          context,
          this.getClass().getName(),
          this.getResourceBundle().getString("ASCIIDOCExporter.saveDialogTitle"),
          null,
          ".asciidoc",
          this.getResourceBundle().getString("ASCIIDOCExporter.filterDescription"),
          this.getResourceBundle().getString("ASCIIDOCExporter.approveButtonText"));
      fileToSaveMap =
          MindMapUtils.checkFileAndExtension(context.getPanel(), fileToSaveMap, ".asciidoc");
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
    return "asciidoc";
  }

  @Override
  public String getName(final PluginContext context, final Topic actionTopic) {
    return this.getResourceBundle().getString("ASCIIDOCExporter.exporterName");
  }

  @Override
  public String getReference(final PluginContext context, final Topic actionTopic) {
    return this.getResourceBundle().getString("ASCIIDOCExporter.exporterReference");
  }

  @Override
  public Icon getIcon(final PluginContext context, final Topic actionTopic) {
    return ICO;
  }

  @Override
  public int getOrder() {
    return 9;
  }

  private static class State {

    private static final String NEXT_LINE = System.getProperty("line.separator", "\n");
    private final StringBuilder buffer = new StringBuilder(16384);

    public State append(final char ch) {
      this.buffer.append(ch);
      return this;
    }

    public State appendHead(final String str) {
      this.buffer.append(escapeAsciiDoc(str, true));
      return this;
    }

    public State appendParagraphText(final String str) {
      for (final String s : ModelUtils.breakToLines(str)) {
        this.buffer.append(escapeAsciiDoc(s, false)).append(" +");
        appendNextLine();
      }
      return this;
    }

    public State append(final String str) {
      this.buffer.append(str);
      return this;
    }

    public State appendNextLine() {
      this.buffer.append(NEXT_LINE);
      return this;
    }

    public State appendConditionalNextLine() {
      if (this.buffer.charAt(this.buffer.length() - 1) != '\n') {
        this.buffer.append(NEXT_LINE);
      }
      return this;
    }

    @Override
    public String toString() {
      return this.buffer.toString();
    }

  }

}
