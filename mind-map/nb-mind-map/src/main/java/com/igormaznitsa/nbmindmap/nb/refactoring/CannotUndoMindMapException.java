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
package com.igormaznitsa.nbmindmap.nb.refactoring;

import java.io.File;
import java.util.ResourceBundle;
import javax.swing.undo.CannotUndoException;

public class CannotUndoMindMapException extends CannotUndoException {
  protected static final ResourceBundle BUNDLE = ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle");

  public static final long serialVersionUID = 12312439213L;
  
  private final String filePath;
  
  public CannotUndoMindMapException(final File file){
    this.filePath = file == null ? "<NULL>" : file.getAbsolutePath(); //NOI18N
  }
  
  @Override
  public String getMessage(){
    return String.format(BUNDLE.getString("CannotUndoMindMapException.getMessage"),this.filePath);
  }
}
