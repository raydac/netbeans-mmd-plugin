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

import static com.igormaznitsa.mindmap.exporters.AbstractMindMapExporter.BUNDLE;
import static com.igormaznitsa.mindmap.exporters.AbstractMindMapExporter.selectFileForFileFilter;
import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.ModelUtils;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.utils.Icons;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.sql.Timestamp;
import java.util.Locale;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;

public class MDExporter extends AbstractMindMapExporter {

  private static final int STARTING_INDEX_FOR_NUMERATION = 5;
  
  private static class State {

    private static final String NEXT_LINE = System.getProperty("line.separator", "\n");//NOI18N
    private final StringBuilder buffer = new StringBuilder(16384);

    public State append(final char ch) {
      this.buffer.append(ch);
      return this;
    }

    public State nextStringMarker(){
      this.buffer.append("  ");//NOI18N
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

  private static String getTopicUid(final Topic topic){
    return topic.getAttribute(ExtraTopic.TOPIC_UID_ATTR);
  }
  
  private static void writeTopic(final Topic topic, final String listPosition, final State state) throws IOException {
    final int level = topic.getTopicLevel();
    
    String prefix = "";//NOI18N
    
    final String topicUid = getTopicUid(topic);
    if (topicUid!=null){
      state.append("<a name=\"").append(topicUid).append("\">").nextLine();//NOI18N
    }
    
    if (level<STARTING_INDEX_FOR_NUMERATION){
      final String headerPrefix = generateString('#', topic.getTopicLevel() + 1);//NOI18N
      state.append(headerPrefix).append(' ').append(ModelUtils.escapeMarkdownStr(topic.getText())).nextLine();
    }else{
      final String headerPrefix = generateString('#', STARTING_INDEX_FOR_NUMERATION+1);//NOI18N
      state.append(prefix).append(headerPrefix).append(' ').append(listPosition).append(' ').append(ModelUtils.escapeMarkdownStr(topic.getText())).nextLine();
    }

    final ExtraFile file = (ExtraFile) topic.getExtras().get(Extra.ExtraType.FILE);
    final ExtraLink link = (ExtraLink) topic.getExtras().get(Extra.ExtraType.LINK);
    final ExtraNote note = (ExtraNote) topic.getExtras().get(Extra.ExtraType.NOTE);
    final ExtraTopic transition = (ExtraTopic) topic.getExtras().get(Extra.ExtraType.TOPIC);

    boolean extrasPrinted = false;

    if (transition != null) {
      final Topic linkedTopic = topic.getMap().findTopicForLink(transition);
      state.append(prefix)
              .append("*Related to: ")//NOI18N
              .append('[')//NOI18N
              .append(ModelUtils.escapeMarkdownStr(makeLineFromString(linkedTopic.getText())))
              .append("](")//NOI18N
              .append("#")//NOI18N
              .append(getTopicUid(linkedTopic))
              .append(")*")//NOI18N
              .nextStringMarker()
              .nextLine();
      extrasPrinted = true;
      if (file != null || link != null || note != null){
        state.nextStringMarker().nextLine();
      }
    }

    if (file != null) {
      final URI fileURI = file.getValue();
      state.append(prefix)
              .append("> File: ")//NOI18N
              .append(ModelUtils.escapeMarkdownStr(fileURI.isAbsolute() ? Utils.toFile(fileURI).getAbsolutePath() : fileURI.toString())).nextStringMarker().nextLine();
      extrasPrinted = true;
    }

    if (link != null) {
      final String url = link.getValue().toString();
      final String ascurl = link.getValue().toASCIIString();
      state.append(prefix)
              .append("> Url: ")//NOI18N
              .append('[')//NOI18N
              .append(ModelUtils.escapeMarkdownStr(url))
              .append("](")//NOI18N
              .append(ascurl)
              .append(')')//NOI18N
              .nextStringMarker()
              .nextLine();
      extrasPrinted = true;
    }

    if (note != null) {
      if (extrasPrinted) {
        state.nextLine();
      }
      state.append(prefix)
              .append("<pre>")//NOI18N
              .append(StringEscapeUtils.escapeHtml(note.getValue()))
              .append("</pre>")//NOI18N
              .nextLine();
      extrasPrinted = true;
    }
  }

  private void writeInterTopicLine(final State state) {
    state.nextLine();
  }

  private void writeOtherTopicRecursively(final Topic t, final String topicListNumStr, final int topicIndex, final State state) throws IOException {
    writeInterTopicLine(state);
    final String prefix;
    if (t.getTopicLevel()>=STARTING_INDEX_FOR_NUMERATION){
      prefix = topicListNumStr + Integer.toString(topicIndex+1) + '.';//NOI18N
    }else{
      prefix = "";//NOI18N
    }
    writeTopic(t, prefix, state);
    int index = 0;
    for (final Topic ch : t.getChildren()) {
      writeOtherTopicRecursively(ch, prefix, index++, state);
    }
  }

  @Override
  public void doExport(final MindMapPanel panel, final OutputStream out) throws IOException {
    final State state = new State();

    state.append("<!--")//NOI18N
            .nextLine()//NOI18N
            .append("Generated by NB Mind Map Plugin (https://github.com/raydac/netbeans-mmd-plugin)")//NOI18N
            .nextLine();//NOI18N
    state.append(new Timestamp(new java.util.Date().getTime()).toString()).nextLine().append("-->").nextLine();//NOI18N

    final Topic root = panel.getModel().getRoot();
    if (root != null) {
      writeTopic(root, "", state);//NOI18N

      final Topic[] children = Utils.getLeftToRightOrderedChildrens(root);
      for (final Topic t : children) {
        writeInterTopicLine(state);
        writeTopic(t, "", state);//NOI18N
        int indexChild = 0;
        for (final Topic tt : t.getChildren()) {
          writeOtherTopicRecursively(tt, "", indexChild++, state);//NOI18N
        }
      }
    }

    final String text = state.toString();

    File fileToSaveMap = null;
    OutputStream theOut = out;
    if (theOut == null) {
      fileToSaveMap = selectFileForFileFilter(panel, BUNDLE.getString("MDExporter.saveDialogTitle"), ".MD", BUNDLE.getString("MDExporter.filterDescription"), BUNDLE.getString("MDExporter.approveButtonText"));
      fileToSaveMap = checkFileAndExtension(panel, fileToSaveMap, ".MD");//NOI18N
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
  public String getName() {
    return BUNDLE.getString("MDExporter.exporterName");
  }

  @Override
  public String getReference() {
    return BUNDLE.getString("MDExporter.exporterReference");
  }

  @Override
  public ImageIcon getIcon() {
    return Icons.ICO_MARKDOWN.getIcon();
  }

}
