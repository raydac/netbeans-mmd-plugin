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

import com.igormaznitsa.nbmindmap.mmgui.MindMapPanel;
import com.igormaznitsa.nbmindmap.model.Extra;
import com.igormaznitsa.nbmindmap.model.ExtraFile;
import com.igormaznitsa.nbmindmap.model.ExtraLink;
import com.igormaznitsa.nbmindmap.model.ExtraNote;
import com.igormaznitsa.nbmindmap.model.ExtraTopic;
import com.igormaznitsa.nbmindmap.model.Topic;
import com.igormaznitsa.nbmindmap.utils.Icons;
import com.igormaznitsa.nbmindmap.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.sql.Timestamp;
import java.util.Locale;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.Utilities;

public class MDExporter extends AbstractMindMapExporter {

  private static class State {

    private static final String NEXT_LINE = System.getProperty("line.separator", "\n");
    private final StringBuilder buffer = new StringBuilder(16384);

    public State append(final char ch) {
      this.buffer.append(ch);
      return this;
    }

    public State nextStringMarker(){
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
      }
      else {
        result.append(c);
      }
    }

    return result.toString();
  }

  private static String makeLinkToTopic(final Topic topic){
    final int [] path = topic.getPositionPath();
    
    final String listPosition;
    
    if (path.length<3){
      listPosition = "";
    }else{
      final StringBuilder b = new StringBuilder();
      for(int i=2;i<path.length;i++){
        final int pos = path[i];
        if (pos<0) throw new IllegalStateException("Unexpected state! Topic position is -1!");
        b.append(pos+1).append('.');
      }
      listPosition = b.toString();
    }
    
    final String text = topic.getText().trim().replace("\r", "").replace("\n", " ");
    final String [] splitted = text.split("\\s");
    final StringBuilder buffer = new StringBuilder(listPosition);
    
    for(final String s : splitted){
      if (buffer.length()>0) buffer.append('-');
      buffer.append(s.toLowerCase(Locale.ENGLISH));
    }
    
    return buffer.toString();
  }
  
  private static void writeTopic(final Topic topic, final String listPosition, final State state) throws IOException {
    final int level = topic.getTopicLevel();
    
    String prefix = "";
    
    if (level<2){
      final String headerPrefix = generateString('#', topic.getTopicLevel() + 1);
      state.append(headerPrefix).append(' ').append(Utils.escapeMarkdownStr(topic.getText())).nextLine();
    }else{
      final String headerPrefix = generateString('#', 3);
      state.append(prefix).append(headerPrefix).append(' ').append(listPosition).append(' ').append(Utils.escapeMarkdownStr(topic.getText())).nextLine();
    }

    final ExtraFile file = (ExtraFile) topic.getExtras().get(Extra.ExtraType.FILE);
    final ExtraLink link = (ExtraLink) topic.getExtras().get(Extra.ExtraType.LINK);
    final ExtraNote note = (ExtraNote) topic.getExtras().get(Extra.ExtraType.NOTE);
    final ExtraTopic transition = (ExtraTopic) topic.getExtras().get(Extra.ExtraType.TOPIC);

    boolean hasExtras = false;
    boolean extrasPrinted = false;

    if (file != null || link != null || note != null || transition != null) {
      hasExtras = true;
    }

    if (transition != null) {
      final Topic linkedTopic = topic.getMap().findTopicForLink(transition);
      state.append(prefix).append("*Related to: ").append('[').append(Utils.escapeMarkdownStr(makeLineFromString(linkedTopic.getText()))).append("](").append("#").append(makeLinkToTopic(linkedTopic)).append(")*").nextStringMarker().nextLine();
      extrasPrinted = true;
      if (file != null || link != null || note != null){
        state.nextStringMarker().nextLine();
      }
    }

    if (file != null) {
      final URI fileURI = file.getValue();
      state.append(prefix).append("> File: ").append(Utils.escapeMarkdownStr(fileURI.isAbsolute() ? Utilities.toFile(file.getValue()).getAbsolutePath() : fileURI.toString())).nextStringMarker().nextLine();
      extrasPrinted = true;
    }

    if (link != null) {
      final String url = link.getValue().toString();
      final String ascurl = link.getValue().toASCIIString();
      state.append(prefix).append("> Url: ").append('[').append(Utils.escapeMarkdownStr(url)).append("](").append(ascurl).append(')').nextStringMarker().nextLine();
      extrasPrinted = true;
    }

    if (note != null) {
      if (extrasPrinted) {
        state.nextLine();
      }
      state.append(prefix).append("<pre>").append(StringEscapeUtils.escapeHtml(note.getValue())).append("</pre>").nextLine();
      extrasPrinted = true;
    }
  }

  private void writeInterTopicLine(final State state) {
    state.nextLine();
  }

  private void writeOtherTopicRecursively(final Topic t, final String topicListNumStr, final int topicIndex, final State state) throws IOException {
    writeInterTopicLine(state);
    final String prefix = topicListNumStr + Integer.toString(topicIndex+1) + '.';
    writeTopic(t, prefix, state);
    int index = 0;
    for (final Topic ch : t.getChildren()) {
      writeOtherTopicRecursively(ch, prefix, index++, state);
    }
  }

  @Override
  public void doExport(final MindMapPanel currentPanel) throws IOException {
    final State state = new State();

    state.append("<!--").nextLine().append("Generated by NB Mind Map Plugin (https://github.com/raydac/netbeans-mmd-plugin)").nextLine();
    state.append(new Timestamp(new java.util.Date().getTime()).toString()).nextLine().append("-->").nextLine();

    final Topic root = currentPanel.getModel().getRoot();
    if (root != null) {
      writeTopic(root, "", state);

      final Topic[] children = Utils.getLeftToRightOrderedChildrens(root);
      int index = 0;
      for (final Topic t : children) {
        writeInterTopicLine(state);
        writeTopic(t, "", state);
        int indexChild = 0;
        for (final Topic tt : t.getChildren()) {
          writeOtherTopicRecursively(tt, "", indexChild++, state);
        }
      }
    }

    final String text = state.toString();

    final File home = new File(System.getProperty("user.home"));
    File fileToSaveImage = new FileChooserBuilder("user-dir").setTitle("Export as MD file").setDefaultWorkingDirectory(home).setFilesOnly(true).setFileFilter(new FileFilter() {

      @Override
      public boolean accept(File f) {
        return f.isDirectory() || (f.isFile() && f.getName().toLowerCase(Locale.ENGLISH).endsWith(".md"));
      }

      @Override
      public String getDescription() {
        return "MD file (*.MD)";
      }
    }).setApproveText("Save").showSaveDialog();

    fileToSaveImage = checkFile(fileToSaveImage, ".MD");

    if (fileToSaveImage != null) {
      FileUtils.writeStringToFile(fileToSaveImage, text, "UTF-8");
    }
  }

  @Override
  public String getName() {
    return "MD file";
  }

  @Override
  public String getReference() {
    return "Export the mind map content as a plain UTF8 encoded GitHub compatible MarkDown file.";
  }

  @Override
  public ImageIcon getIcon() {
    return Icons.FILE_EXTENSION_RTF.getIcon();
  }

}
