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
package com.igormaznitsa.nbmindmap.nb;

import com.igormaznitsa.nbmindmap.gui.mmview.Utils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JEditorPane;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Source", id = "com.igormaznitsa.nbmindmap.nb.ActionCopySrcPosToMMap")
@ActionRegistration(iconBase = "com/igormaznitsa/nbmindmap/icons/nbmm16.png", displayName = "#CTL_ActionCopySrcPosToMMap")
@ActionReferences({
  @ActionReference(path = "Menu/Source", position = 200, separatorBefore = 150),
  @ActionReference(path = "Editors/Popup", position = 200, separatorBefore = 150),})
@Messages("CTL_ActionCopySrcPosToMMap=Position to Mind map")
public final class ActionCopySrcPosToMMap implements ActionListener {

  private final EditorCookie context;

  public ActionCopySrcPosToMMap(final EditorCookie context) {
    this.context = context;
  }

  @Override
  public void actionPerformed(ActionEvent ev) {
    if (this.context.isModified()) {
      Utils.showWarn("Document must be saved before position grabbing!");
    }
    else {
      final StyledDocument doc = this.context.getDocument();
      if (doc != null) {
        final JEditorPane focused = NbDocument.findRecentEditorPane(this.context);
        if (focused != null) {
          final FileObject obj = NbEditorUtilities.getFileObject(doc);
          final int line = NbDocument.findLineNumber(doc, focused.getCaretPosition());
          final int column = NbDocument.findLineColumn(doc, focused.getCaretPosition());
          Utils.showInfo("DOC: " + obj + " " + (line+1)+':'+ (column+1));
        }
      }
    }
  }


}
