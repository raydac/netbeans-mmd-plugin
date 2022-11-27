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
package com.igormaznitsa.nbmindmap.nb.refactoring.elements;

import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.nbmindmap.nb.refactoring.MindMapLink;
import java.io.File;
import org.openide.ErrorManager;

public class DeleteElement extends AbstractElement {

  public DeleteElement(final MindMapLink mindMap, final File projectFolder, final MMapURI fileObject) {
    super(mindMap, projectFolder, fileObject);
  }

  @Override
  public String getText() {
    return String.format(BUNDLE.getString("DeleteElement.getText"),this.processedFile.asString(false, false));
  }

  @Override
  public void performChange() {
    super.performChange();
    try{
      final MindMap parsed = this.mindMapFile.asMindMap();
      if (parsed.deleteAllLinksToFile(this.projectFolder, this.processedFile)){
        this.mindMapFile.writeMindMap();
      }
    }catch(Exception ex){
      LOGGER.error("Error during mind map refactoring",ex); //NOI18N
      ErrorManager.getDefault().log(ErrorManager.EXCEPTION, "Can't process mind map and remove file link"); //NOI18N
    }
  }
  
}
