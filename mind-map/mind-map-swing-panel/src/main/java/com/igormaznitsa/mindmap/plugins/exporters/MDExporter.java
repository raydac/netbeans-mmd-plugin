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
import com.igormaznitsa.mindmap.model.MindMap;
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
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

public class MDExporter extends AbstractExporter {

  private static final int STARTING_INDEX_FOR_NUMERATION = 5;
  private static final Icon ICO =
      ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_EXPORT_MARKDOWN);
  private static final ExtrasToStringConverter DEFAULT_MD_EXTRAS_STRING_CONVERTER =
      new ExtrasToStringConverter() {
        @Override
        public String apply(final PluginContext pluginContext, final Extra<?> extra) {
          switch (extra.getType()) {
            case FILE:
              return ((ExtraFile) extra).getValue().asFile(pluginContext.getProjectFolder())
                  .getAbsolutePath();
            case LINK: {
              final String url = ((ExtraLink) extra).getValue().toString();
              final String urlAsAscII = ((ExtraLink) extra).getValue().asString(true, true);
              return '[' + url + "](" + urlAsAscII + ')';
            }
            case NOTE:
              return StringEscapeUtils.escapeHtml3(((ExtraNote) extra).getValue());
            case TOPIC:
              return ((ExtraTopic) extra).getValue();
            default:
              throw new IllegalArgumentException("Unknown extras: " + extra);
          }
        }
      };

  private static String generateString(final char symbol, final int length) {
    final StringBuilder buffer = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      buffer.append(symbol);
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

  @Override
  public ExtrasToStringConverter getDefaultExtrasStringConverter() {
    return DEFAULT_MD_EXTRAS_STRING_CONVERTER;
  }

  private void writeTopic(
      final PluginContext pluginContext,
      final Topic topic,
      final String listPosition,
      final MdWriter state,
      final ExtrasToStringConverter stringConverter) {
    final int level = topic.getTopicLevel();

    String prefix = "";

    final String topicUid = getTopicUid(topic);
    if (topicUid != null) {
      state.append("<a name=\"").append(topicUid).append("\"></a>").nextLine();
    }

    if (level < STARTING_INDEX_FOR_NUMERATION) {
      final String headerPrefix = generateString('#', topic.getTopicLevel() + 1);
      state.append(headerPrefix).append(' ').escape(topic.getText())
          .nextLine();
    } else {
      final String headerPrefix = generateString('#', STARTING_INDEX_FOR_NUMERATION + 1);
      state.append(prefix).append(headerPrefix).append(' ').append(listPosition).append(' ')
          .escape(topic.getText()).nextLine();
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
            .escape(makeLineFromString(linkedTopic.getText()))
            .append("](")
            .append("#")
            .append(requireNonNull(stringConverter.apply(pluginContext, transition)))
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
          .append("> File: [")
          .escape(asLineInfo(fileURI))
          .append("](")
          .escape(stringConverter.apply(pluginContext, file))
          .append(')')
          .nextStringMarker()
          .nextLine();
      extrasPrinted = true;
    }

    if (link != null) {
      state.append(prefix)
          .append("> Url: ")
          .escape(stringConverter.apply(pluginContext, link))
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
          .append(stringConverter.apply(pluginContext, note))
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

  private String asLineInfo(final MMapURI uri) {
    final String line = uri.getParameters().getProperty("line");
    final String resourceName = uri.getResourceName();
    return line == null ? resourceName : resourceName + ':' + line;
  }

  private void writeInterTopicLine(final MdWriter state) {
    state.nextLine();
  }

  private void writeOtherTopicRecursively(final PluginContext pluginContext, final Topic t,
                                          final String topicListNumStr,
                                          final int topicIndex,
                                          final MdWriter state,
                                          final ExtrasToStringConverter stringConverter) {
    writeInterTopicLine(state);
    final String prefix;
    if (t.getTopicLevel() >= STARTING_INDEX_FOR_NUMERATION) {
      prefix = topicListNumStr + (topicIndex + 1) + '.';
    } else {
      prefix = "";
    }
    writeTopic(pluginContext, t, prefix, state, stringConverter);
    int index = 0;
    for (final Topic ch : t.getChildren()) {
      writeOtherTopicRecursively(pluginContext, ch, prefix, index++, state, stringConverter);
    }
  }

  private String makeContent(final PluginContext pluginContext, final MindMap model,
                             final ExtrasToStringConverter stringConverter) {
    final MdWriter state = new MdWriter();

    state.append("<!--")
        .nextLine()
        .append(
            "Generated by " + IDEBridgeFactory.findInstance().getIDEGeneratorId() + ' ' +
                IDEBridgeFactory.findInstance().getIDEVersion() + " (https://sciareto.org)")
        .nextLine();
    state.append(DATE_FORMAT.format(new java.util.Date().getTime())).nextLine().append("-->")
        .nextLine();

    final Topic root = model.getRoot();
    if (root != null) {
      writeTopic(pluginContext, root, "", state, stringConverter);

      final Topic[] children = Utils.getLeftToRightOrderedChildrens(root);
      for (final Topic t : children) {
        writeInterTopicLine(state);
        writeTopic(pluginContext, t, "", state, stringConverter);
        int indexChild = 0;
        for (final Topic tt : t.getChildren()) {
          writeOtherTopicRecursively(pluginContext, tt, "", indexChild++, state, stringConverter);
        }
      }
    }

    return state.toString();
  }

  @Override
  public void doExportToClipboard(
      final PluginContext context,
      final Set<AbstractParameter<?>> options,
      final ExtrasToStringConverter stringConverter)
      throws IOException {
    final String text = makeContent(context, context.getModel(), stringConverter);
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
      final ExtrasToStringConverter stringConverter) throws IOException {
    final String text = makeContent(context, context.getModel(), stringConverter);

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

  private static final class MdWriter {

    private static final String NEXT_LINE = "\n";
    private final StringBuilder buffer = new StringBuilder(16384);

    public MdWriter append(final char ch) {
      this.buffer.append(ch);
      return this;
    }

    public MdWriter nextStringMarker() {
      this.buffer.append("  ");
      return this;
    }

    public MdWriter escape(final String str) {
      this.buffer.append(escapeMarkdown(str));
      return this;
    }

    public MdWriter append(final String str) {
      this.buffer.append(str);
      return this;
    }

    public MdWriter nextLine() {
      this.buffer.append(NEXT_LINE);
      return this;
    }

    @Override
    public String toString() {
      return this.buffer.toString();
    }

  }

}
