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
package com.igormaznitsa.nbmindmap.exporters;

import com.igormaznitsa.nbmindmap.mmgui.AbstractCollapsableElement;
import com.igormaznitsa.nbmindmap.mmgui.Configuration;
import com.igormaznitsa.nbmindmap.mmgui.MindMapPanel;
import com.igormaznitsa.nbmindmap.model.Extra;
import com.igormaznitsa.nbmindmap.model.ExtraFile;
import com.igormaznitsa.nbmindmap.model.ExtraLink;
import com.igormaznitsa.nbmindmap.model.ExtraNote;
import com.igormaznitsa.nbmindmap.model.ExtraTopic;
import com.igormaznitsa.nbmindmap.model.Topic;
import com.igormaznitsa.nbmindmap.utils.Icons;
import static com.igormaznitsa.nbmindmap.utils.Utils.color2html;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Locale;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.openide.filesystems.FileChooserBuilder;

public class FreeMindExporter extends AbstractMindMapExporter {

  private static class State {

    private static final String NEXT_LINE = "\r\n";
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
    final StringBuilder buffer = new StringBuilder("mmlink");
    for (final int i : path) {
      buffer.append('A'+i);
    }
    return buffer.toString();
  }

  private static void writeTopicRecursively(final Topic topic, final Configuration cfg, int shift, final State state) {
    final String mainShiftStr = generateString(' ', shift);

    final Color edge = cfg.getConnectorColor();
    final Color color;
    final Color backcolor;
    String position = "";
    switch (topic.getTopicLevel()) {
      case 0: {
        color = cfg.getRootTextColor();
        backcolor = cfg.getRootBackgroundColor();
      }
      break;
      case 1: {
        color = cfg.getFirstLevelTextColor();
        backcolor = cfg.getFirstLevelBackgroundColor();
        position = AbstractCollapsableElement.isLeftSidedTopic(topic) ? "left" : "right";
      }
      break;
      default: {
        color = cfg.getOtherLevelTextColor();
        backcolor = cfg.getOtherLevelBackgroundColor();
      }
      break;
    }

    state.append(mainShiftStr).append("<node CREATED=\"").append(System.currentTimeMillis()).append("\" MODIFIED=\"").append(System.currentTimeMillis())
            .append("\" COLOR=\"").append(color2html(color)).append("\" BACKGROUND_COLOR=\"").append(color2html(backcolor))
            .append("\" ").append(position.isEmpty() ? " " : "POSITION=\"" + position + "\" ")
            .append("ID=\"").append(makeUID(topic)).append("\" ")
            .append("TEXT=\"").append(escapeXML(topic.getText())).append("\" ");

    final ExtraFile file = (ExtraFile) topic.getExtras().get(Extra.ExtraType.FILE);
    final ExtraLink link = (ExtraLink) topic.getExtras().get(Extra.ExtraType.LINK);
    final ExtraTopic transition = (ExtraTopic) topic.getExtras().get(Extra.ExtraType.TOPIC);

    final String thelink;

    // make some prioritization for only attribute
    if (file != null) {
      thelink = file.getValue().toString();
    }
    else if (link != null) {
      thelink = link.getValue().toString();
    }
    else if (transition != null) {
      thelink = '#'+makeUID(topic.getMap().findTopicForLink(transition));
    }
    else {
      thelink = "";
    }

    if (!thelink.isEmpty()) {
      state.append(" LINK=\"").append(escapeXML(thelink)).append("\"");
    }
    state.append(">").nextLine();

    shift++;
    final String childShift = generateString(' ', shift);

    state.append(childShift).append("<edge COLOR=\"").append(color2html(edge)).append("\"/>").nextLine();

    final ExtraNote note = (ExtraNote) topic.getExtras().get(Extra.ExtraType.NOTE);

    if (note != null) {
      state.append(childShift).append("<hook NAME=\"accessories/plugins/NodeNote.properties\">").nextLine()
              .append(childShift).append(" <text>").append(escapeXML(note.getValue())).append("</text>").nextLine()
              .append(childShift).append("</hook>").nextLine();
    }

    for(final Topic ch : topic.getChildren()){
      writeTopicRecursively(ch, cfg, shift, state);
    }
    
    shift--;
    state.append(mainShiftStr).append("</node>").nextLine();
  }

  @Override
  public void doExport(final MindMapPanel currentPanel) throws IOException {
    final State state = new State();

    state.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>").nextLine();
    state.append("<!--").nextLine().append("Generated by NB Mind Map Plugin (https://github.com/raydac/netbeans-mmd-plugin)").nextLine();
    state.append(new Timestamp(new java.util.Date().getTime()).toString()).nextLine().append("-->").nextLine();
    state.append("<map version=\"0.8.1\" background_color=\"").append(color2html(currentPanel.getConfiguration().getPaperColor())).append("\">").nextLine();

    final Topic root = currentPanel.getModel().getRoot();
    if (root != null) {
      writeTopicRecursively(root, currentPanel.getConfiguration(), 1, state);
    }

    state.append("</map>");

    final String text = state.toString();

    final File home = new File(System.getProperty("user.home"));
    File fileToSaveImage = new FileChooserBuilder("user-dir").setTitle("Export as FreeMind file").setDefaultWorkingDirectory(home).setFilesOnly(true).setFileFilter(new FileFilter() {

      @Override
      public boolean accept(File f) {
        return f.isDirectory() || (f.isFile() && f.getName().toLowerCase(Locale.ENGLISH).endsWith(".mm"));
      }

      @Override
      public String getDescription() {
        return "FreeMind file (*.mm)";
      }
    }).setApproveText("Save").showSaveDialog();

    fileToSaveImage = checkFile(fileToSaveImage, ".mm");

    if (fileToSaveImage != null) {
      FileUtils.writeStringToFile(fileToSaveImage, text, "UTF-8");
    }
  }

  private static String escapeXML(final String text){
    return StringEscapeUtils.escapeXml(text).replace("\n", "&#10;");
  }
  
  @Override
  public String getName() {
    return "FreeMind file";
  }

  @Override
  public String getReference() {
    return "Export the mind map content as a FreeMind XML file.";
  }

  @Override
  public ImageIcon getIcon() {
    return Icons.FREEMIND.getIcon();
  }

}
