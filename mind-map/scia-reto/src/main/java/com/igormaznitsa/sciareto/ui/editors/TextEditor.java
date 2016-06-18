/*
 * Copyright 2016 Igor Maznitsa.
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
package com.igormaznitsa.sciareto.ui.editors;

import java.io.File;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.io.FileUtils;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.ui.DialogProviderManager;
import com.igormaznitsa.sciareto.ui.tabs.TabProvider;
import com.igormaznitsa.sciareto.ui.tabs.TabTitle;

public class TextEditor extends JScrollPane implements TabProvider {

  private static final long serialVersionUID = -8551212562825517869L;

  private static final Logger LOGGER = LoggerFactory.getLogger(TextEditor.class);
  
  private final JTextArea editor;
  private final TabTitle title;
  
  public static class TextFileFilter extends FileFilter {

    @Override
    public boolean accept(@Nonnull final File f) {
      return f.isDirectory() || f.getName().endsWith(".txt");
    }

    @Override
    public String getDescription() {
      return "Text document (*.txt)";
    }

  }
  
  public TextEditor(@Nonnull final Context context, @Nullable File file) throws IOException {
    super();
    this.editor = new JTextArea();
    if (file!=null){
      this.editor.setText(FileUtils.readFileToString(file, "UTF-8"));
      this.editor.setCaretPosition(0);
    }
    this.title = new TabTitle(context, this, file);
    this.editor.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(@Nonnull final DocumentEvent e) {
        title.setChanged(true);
      }

      @Override
      public void removeUpdate(@Nonnull final DocumentEvent e) {
        title.setChanged(true);
      }

      @Override
      public void changedUpdate(@Nonnull final DocumentEvent e) {
        title.setChanged(true);
      }
    });
    
    setViewportView(this.editor);
  }
  
  @Override
  public boolean saveDocument() {
    boolean result = false;
    if (this.title.isChanged()) {
      File file = this.title.getAssociatedFile();
      if (file == null) {
        file = DialogProviderManager.getInstance().getDialogProvider().msgSaveFileDialog("text-editor", "Save Text document", null, true, new TextFileFilter(), "Save");
        if (file == null) {
          return result;
        }
      }
      try {
        FileUtils.write(file, this.editor.getText(), "UTF-8", false);
        this.title.setChanged(false);
        result = true;
      } catch (IOException ex) {
        LOGGER.error("Can't write file : " + file, ex);
      }
    } else {
      result = true;
    }
    return result;
  }

  
  @Override
  @Nonnull
  public TabTitle getTabTitle() {
    return this.title;
  }

  @Override
  @Nonnull
  public JComponent getMainComponent() {
    return this;
  }
  
}
