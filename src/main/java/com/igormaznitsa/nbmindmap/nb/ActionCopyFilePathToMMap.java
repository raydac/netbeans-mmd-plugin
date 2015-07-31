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

import com.igormaznitsa.nbmindmap.utils.Utils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

@ActionID(category = "Source", id = "com.igormaznitsa.nbmindmap.nb.ActionCopyFilePathToMMap")
@ActionRegistration(iconBase = "com/igormaznitsa/nbmindmap/icons/nbmm16.png", displayName = "#CTL_ActionCopyFilePathToMMap")
@ActionReferences({
  @ActionReference(path = "Menu/Source", position = 200, separatorBefore = 150),
  @ActionReference(path = "Editors/TabActions", position = 200, separatorBefore = 150)
})
@NbBundle.Messages("CTL_ActionCopyFilePathToMMap=Add File path to Mind map")
public final class ActionCopyFilePathToMMap implements ActionListener {

  private final DataObject dataObject;

  public ActionCopyFilePathToMMap(final DataObject obj) {
    this.dataObject = obj;
  }

  @Override
  public void actionPerformed(ActionEvent ev) {
    Utils.showInfo(""+this.dataObject);
  }

}
