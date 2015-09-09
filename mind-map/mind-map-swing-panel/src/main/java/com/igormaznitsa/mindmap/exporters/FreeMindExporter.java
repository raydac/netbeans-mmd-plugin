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
package com.igormaznitsa.mindmap.exporters;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraLinkable;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractCollapsableElement;
import com.igormaznitsa.mindmap.swing.panel.utils.Icons;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;

public class FreeMindExporter extends AbstractMindMapExporter {

  private static class State {

    private static final String NEXT_LINE = "\r\n";//NOI18N
    private final StringBuilder buffer = new StringBuilder(16384);

    public State append(final char ch) {
      this.buffer.append(ch);
      return this;
    }

    public State append(final long val) {
      this.buffer.append(val);
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

  private static String generateString(final char chr, final int length) {
    final StringBuilder buffer = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      buffer.append(chr);
    }
    return buffer.toString();
  }

  private static String makeUID(final Topic t) {
    final int[] path = t.getPositionPath();
    final StringBuilder buffer = new StringBuilder("mmlink");//NOI18N
    for (final int i : path) {
      buffer.append('A' + i);
    }
    return buffer.toString();
  }

  private static void writeTopicRecursively(final Topic topic, final MindMapPanelConfig cfg, int shift, final State state) {
    final String mainShiftStr = generateString(' ', shift);

    final Color edge = cfg.getConnectorColor();
    final Color color;
    final Color backcolor;
    String position = ""; //NOI18N
    switch (topic.getTopicLevel()) {
      case 0: {
        color = cfg.getRootTextColor();
        backcolor = cfg.getRootBackgroundColor();
      }
      break;
      case 1: {
        color = cfg.getFirstLevelTextColor();
        backcolor = cfg.getFirstLevelBackgroundColor();
        position = AbstractCollapsableElement.isLeftSidedTopic(topic) ? "left" : "right";//NOI18N
      }
      break;
      default: {
        color = cfg.getOtherLevelTextColor();
        backcolor = cfg.getOtherLevelBackgroundColor();
      }
      break;
    }

    state.append(mainShiftStr)
            .append("<node CREATED=\"") //NOI18N
            .append(System.currentTimeMillis()) //NOI18N
            .append("\" MODIFIED=\"") //NOI18N
            .append(System.currentTimeMillis()) //NOI18N
            .append("\" COLOR=\"") //NOI18N
            .append(Utils.color2html(color)) //NOI18N
            .append("\" BACKGROUND_COLOR=\"") //NOI18N
            .append(Utils.color2html(backcolor)) //NOI18N
            .append("\" ") //NOI18N
            .append(position.isEmpty() ? " " : String.format("POSITION=\"%s\"", position)) //NOI18N
            .append(" ID=\"") //NOI18N
            .append(makeUID(topic)) //NOI18N
            .append("\" ") //NOI18N
            .append("TEXT=\"") //NOI18N
            .append(escapeXML(topic.getText()))
            .append("\" "); //NOI18N

    final ExtraFile file = (ExtraFile) topic.getExtras().get(Extra.ExtraType.FILE);
    final ExtraLink link = (ExtraLink) topic.getExtras().get(Extra.ExtraType.LINK);
    final ExtraTopic transition = (ExtraTopic) topic.getExtras().get(Extra.ExtraType.TOPIC);

    final String thelink;

    final List<Extra<?>> extrasToSaveInText = new ArrayList<Extra<?>>();

    // make some prioritization for only attribute
    if (transition != null) {
      thelink = '#' + makeUID(topic.getMap().findTopicForLink(transition));//NOI18N
      if (file != null) {
        extrasToSaveInText.add(file);
      }
      if (link != null) {
        extrasToSaveInText.add(link);
      }
    }
    else if (file != null) {
      thelink = file.getValue().toString();
      if (link != null) {
        extrasToSaveInText.add(link);
      }
    }
    else if (link != null) {
      thelink = link.getValue().toString();
    }
    else {
      thelink = "";//NOI18N
    }

    if (!thelink.isEmpty()) {
      state.append(" LINK=\"").append(escapeXML(thelink)).append("\"");//NOI18N
    }
    state.append(">").nextLine();//NOI18N

    shift++;
    final String childShift = generateString(' ', shift);//NOI18N

    state.append(childShift).append("<edge COLOR=\"").append(Utils.color2html(edge)).append("\"/>").nextLine();//NOI18N

    final ExtraNote note = (ExtraNote) topic.getExtras().get(Extra.ExtraType.NOTE);

    final StringBuilder htmlTextForNode = new StringBuilder();
    if (!extrasToSaveInText.isEmpty()) {
      htmlTextForNode.append("<ul>"); //NOI18N
      for (final Extra<?> e : extrasToSaveInText) {
        htmlTextForNode.append("<li>"); //NOI18N
        if (e instanceof ExtraLinkable) {
          final String linkAsText = ((ExtraLinkable) e).getAsURI().toASCIIString();
          htmlTextForNode.append("<b>").append(StringEscapeUtils.escapeHtml(e.getType().name())).append(": </b>").append("<a href=\"").append(linkAsText).append("\">").append(linkAsText).append("</a>"); //NOI18N
        }
        else {
          htmlTextForNode.append("<b>").append(StringEscapeUtils.escapeHtml(e.getType().name())).append(": </b>").append(StringEscapeUtils.escapeHtml(e.getAsString())); //NOI18N
        }
        htmlTextForNode.append("</li>"); //NOI18N
      }
      htmlTextForNode.append("</ul>"); //NOI18N
    }

    if (note != null) {
      htmlTextForNode.append("<p><pre>").append(StringEscapeUtils.escapeHtml(note.getValue())).append("</pre></p>"); //NOI18N
    }

    if (htmlTextForNode.length() > 0) {
      state.append(childShift).append("<richcontent TYPE=\"NOTE\">").append("<html><head></head><body>" + htmlTextForNode.toString() + "</body></html>").append("</richcontent>").nextLine();//NOI18N //NOI18N
    }

    for (final Topic ch : topic.getChildren()) {
      writeTopicRecursively(ch, cfg, shift, state);
    }

    state.append(mainShiftStr).append("</node>").nextLine();//NOI18N
  }

  @Override
  public void doExport(final MindMapPanel panel, final OutputStream out) throws IOException {
    final State state = new State();

    state.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>").nextLine();//NOI18N
    state.append("<!--").nextLine().append("Generated by NB Mind Map Plugin (https://github.com/raydac/netbeans-mmd-plugin)").nextLine();//NOI18N
    state.append(new Timestamp(new java.util.Date().getTime()).toString()).nextLine().append("-->").nextLine();//NOI18N
    state.append("<map version=\"1.0.1\" background_color=\"").append(Utils.color2html(panel.getConfiguration().getPaperColor())).append("\">").nextLine();//NOI18N

    final Topic root = panel.getModel().getRoot();
    if (root != null) {
      writeTopicRecursively(root, panel.getConfiguration(), 1, state);
    }

    state.append("</map>");//NOI18N

    final String text = state.toString();

    File fileToSaveMap = null;
    OutputStream theOut = out;
    if (theOut == null) {
      fileToSaveMap = selectFileForFileFilter(panel, BUNDLE.getString("FreeMindExporter.saveDialogTitle"), ".mm", BUNDLE.getString("FreeMindExporter.filterDescription"), BUNDLE.getString("FreeMindExporter.approveButtonText"));
      fileToSaveMap = checkFileAndExtension(panel, fileToSaveMap, ".mm");//NOI18N
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

    if (fileToSaveMap != null) {
      FileUtils.writeStringToFile(fileToSaveMap, text, "UTF-8");//NOI18N
    }
  }

  private static String escapeXML(final String text) {
    return StringEscapeUtils.escapeXml(text).replace("\n", "&#10;"); //NOI18N
  }

  @Override
  public String getName() {
    return BUNDLE.getString("FreeMindExporter.exporterName");
  }

  @Override
  public String getReference() {
    return BUNDLE.getString("FreeMindExporter.exporterReference");
  }

  @Override
  public ImageIcon getIcon() {
    return Icons.ICO_FREEMIND.getIcon();
  }

}
