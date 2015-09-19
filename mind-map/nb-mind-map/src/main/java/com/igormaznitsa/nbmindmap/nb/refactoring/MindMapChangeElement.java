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
package com.igormaznitsa.nbmindmap.nb.refactoring;

import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.MindMap;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import org.apache.commons.io.FileUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;

public class MindMapChangeElement extends SimpleRefactoringElementImplementation {

  public enum Action {

    REMOVE("Link to file will be removed"),
    CHANGED_PATH("Link to file will be changed");

    private final String text;

    private Action(final String text) {
      this.text = text;
    }

    public String getText() {
      return this.text;
    }
  }

  private final MutableFileLink mindMap;
  private final RefactoringElementsBag session;
  private volatile String oldTextBody;
  private final Project project;
  private final Action action;
  private final MMapURI linkToFile;
  private final MMapURI newLinkToFile;

  public MindMapChangeElement(final Action action, final Project project, final RefactoringElementsBag session, final MutableFileLink mindMapFileLink, final MMapURI linkToFile, final MMapURI newLinkToFile) {
    super();
    this.project = project;
    this.mindMap = mindMapFileLink;
    this.session = session;
    this.action = action;
    this.linkToFile = linkToFile;
    this.newLinkToFile = newLinkToFile;
  }

  @Override
  public void undoChange() {
    if (this.oldTextBody != null) {
      final File theFile = this.mindMap.getFile();
      try {
        FileUtils.write(theFile, this.oldTextBody);
      }
      catch (IOException ex) {
        ErrorManager.getDefault().notify(ex);
        throw new CannotUndoMindMapException(theFile);
      }
    }
  }

  @Override
  public String getText() {
    return this.action.getText();
  }

  @Override
  public String getDisplayText() {
    return getText();
  }

  @Override
  public void performChange() {
    try {
      this.oldTextBody = FileUtils.readFileToString(this.mindMap.getFile(),"UTF-8");
      final MindMap parsedMap = new MindMap(new StringReader(this.oldTextBody));

      final File baseFolder = FileUtil.toFile(this.project.getProjectDirectory());

      boolean changed = false;

      switch (this.action) {
        case REMOVE: {
          changed = parsedMap.deleteAllLinksToFile(baseFolder, this.linkToFile);
        }
        break;
        case CHANGED_PATH: {
          changed = parsedMap.replaceAllLinksToFile(baseFolder, this.linkToFile, newLinkToFile);
        }
        break;
        default:
          throw new Error("Unexpected action " + this.action);
      }

      if (changed) {
        final String packed = parsedMap.packToString();
        FileUtils.write(this.mindMap.getFile(), packed, "UTF-8", false);
      }
    }
    catch (Exception ex) {
      ErrorManager.getDefault().notify(ex);
    }
  }

  @Override
  public Lookup getLookup() {
    return Lookup.EMPTY;
  }

  @Override
  public FileObject getParentFile() {
    return FileUtil.toFileObject(this.mindMap.getFile());
  }

  @Override
  public PositionBounds getPosition() {
    return null;
  }

}
