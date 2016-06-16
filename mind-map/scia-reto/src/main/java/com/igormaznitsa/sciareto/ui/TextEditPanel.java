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
package com.igormaznitsa.sciareto.ui;

import java.io.File;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.commons.io.FileUtils;
import com.igormaznitsa.sciareto.Context;

public class TextEditPanel extends JScrollPane implements TabProvider {

  private static final long serialVersionUID = -8551212562825517869L;

  private final JTextArea editor;
  private final TabTitle title;
  
  public TextEditPanel(@Nonnull final Context context, @Nullable File file) throws IOException {
    super();
    this.editor = new JTextArea();
    if (file!=null){
      this.editor.setText(FileUtils.readFileToString(file, "UTF-8"));
    }
    this.title = new TabTitle(context, file);
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
  public TabTitle getTabTitle() {
    return this.title;
  }

  @Override
  public JComponent getMainComponent() {
    return this;
  }
  
}
