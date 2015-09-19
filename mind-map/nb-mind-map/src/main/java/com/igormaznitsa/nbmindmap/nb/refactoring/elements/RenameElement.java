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
package com.igormaznitsa.nbmindmap.nb.refactoring.elements;

import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.nbmindmap.nb.refactoring.MutableFileLink;
import static com.igormaznitsa.nbmindmap.nb.refactoring.elements.AbstractElement.readMindMap;
import java.io.File;
import org.openide.ErrorManager;

public class RenameElement extends AbstractElement {

  private MMapURI newFile;

  public RenameElement(final MutableFileLink mindMap, final File projectFolder, final MMapURI file) {
    super(mindMap, projectFolder, file);
  }

  public void setNewFile(final MMapURI uri) {
    this.newFile = uri;
  }

  @Override
  public String getText() {
    return "Rename file " + this.processedFile.asString(false, false);
  }

  @Override
  public void performChange() {
    super.performChange();
    try {
      if (this.newFile != null) {
        final MindMap parsed = readMindMap(this.mindMapFile.getFile());
        if (parsed.replaceAllLinksToFile(this.projectFolder, this.processedFile, this.newFile)) {
          writeMindMap(this.mindMapFile.getFile(), parsed);
        }
      }
      else {
        logger.warn("Detected null as new file uri for rename refactoring");
      }
    }
    catch (Exception ex) {
      logger.error("Error during mind map refactoring", ex);
      ErrorManager.getDefault().log(ErrorManager.EXCEPTION, "Can't process mind map and remove file link");
    }
  }

}
