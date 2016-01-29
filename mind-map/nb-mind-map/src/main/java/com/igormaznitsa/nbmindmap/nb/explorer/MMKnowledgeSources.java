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
package com.igormaznitsa.nbmindmap.nb.explorer;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.nbmindmap.nb.editor.MMDDataObject;
import com.igormaznitsa.nbmindmap.utils.BadgeIcons;
import com.igormaznitsa.nbmindmap.utils.NbUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.support.GenericSources;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;

public final class MMKnowledgeSources implements NodeList<SourceGroup>, ChangeListener, DataFilter {

  private static final long serialVersionUID = -1360299214288653958L;

  public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle");

  public static final String KNOWLEDGE_FOLDER_NAME = ".projectKnowledge";

  public static final String PREFERENCE_KEY_KNOWLEDGEFOLDER_ALLOWED = "knowledgeFolderGenerationAllowed";

  private final Project project;
  private final Sources projectSources;
  private final ChangeListener changeListener;
  private final ChangeSupport changeSupport = new ChangeSupport(this);

  private static final Logger LOGGER = LoggerFactory.getLogger(MMKnowledgeSources.class);

  public MMKnowledgeSources(final Project project) {
    this.project = project;
    this.projectSources = ProjectUtils.getSources(project);
    this.changeListener = WeakListeners.change(this, this.projectSources);
  }

  public static FileObject findProjectKnowledgeFolder(final Project project){
    if (project == null) return null;
    final FileObject projectFolder = project.getProjectDirectory();
    return projectFolder.getFileObject(KNOWLEDGE_FOLDER_NAME);
  }
  
  private static boolean isKnowledgeFolderAllowedForCreation() {
    return NbUtils.getPreferences().getBoolean(PREFERENCE_KEY_KNOWLEDGEFOLDER_ALLOWED, true);
  }

  private static SourceGroup[] getSourceGroups(final Project project) {
    final String klazz = project.getClass().getName();
    LOGGER.info("Request sources for project type " + klazz);

    SourceGroup knowledgeSrc = null;
    try {
      FileObject knowledgeFolder = project.getProjectDirectory().getFileObject(KNOWLEDGE_FOLDER_NAME);
      if (knowledgeFolder == null && isKnowledgeFolderAllowedForCreation()) {
        knowledgeFolder = project.getProjectDirectory().createFolder(KNOWLEDGE_FOLDER_NAME);
      }
      if (knowledgeFolder != null) {
        final String rootKnowledgeFolderName = BUNDLE.getString("KnowledgeSourceGroup.displayName");
        knowledgeSrc = GenericSources.group(project, knowledgeFolder, KNOWLEDGE_FOLDER_NAME, rootKnowledgeFolderName, new ImageIcon(BadgeIcons.BADGED_FOLDER), new ImageIcon(BadgeIcons.BADGED_FOLDER_OPEN));
      }
      else {
        LOGGER.info("Knowledge folder is not presented in " + project);
      }
    }
    catch (IOException ex) {
      LOGGER.error("Can't make source group for knowledge folder", ex);
    }

    final SourceGroup[] result;
    if (knowledgeSrc == null) {
      result = new SourceGroup[0];
    }
    else {
      result = new SourceGroup[]{knowledgeSrc};
    }

    return result;
  }

  @Override
  public List<SourceGroup> keys() {
    final SourceGroup[] sourceGroups = getSourceGroups(this.project);
    final List<SourceGroup> keysList = new ArrayList<SourceGroup>();
    FileObject fileObject;
    for (final SourceGroup g : sourceGroups) {
      fileObject = g.getRootFolder();
      DataFolder srcDir = getFolder(fileObject);
      if (srcDir != null) {
        keysList.add(g);
      }
    }
    return keysList;
  }

  @Override
  public void addChangeListener(final ChangeListener l) {
    this.changeSupport.addChangeListener(l);
  }

  @Override
  public void removeChangeListener(final ChangeListener l) {
    this.changeSupport.removeChangeListener(l);
  }

  @Override
  public Node node(final SourceGroup key) {
    SourceNode node = null;
    if (key != null) {
      final FileObject rootFolder = key.getRootFolder();
      final DataFolder folder = getFolder(rootFolder);
      if (folder != null) {
        node = new SourceNode(project, folder, this, key.getDisplayName());
        if (KNOWLEDGE_FOLDER_NAME.equals(folder.getName())) {
          node.setIcons(BadgeIcons.BADGED_FOLDER, BadgeIcons.BADGED_FOLDER_OPEN);
          node.setShortDescription(BUNDLE.getString("KnowledgeSourceGroup.tooltip"));
        }
      }
    }
    return node;
  }

  @Override
  public void addNotify() {
    this.projectSources.addChangeListener(this.changeListener);
  }

  @Override
  public void removeNotify() {
    this.projectSources.removeChangeListener(this.changeListener);
  }

  private DataFolder getFolder(final FileObject fileObject) {
    if (fileObject != null && fileObject.isValid()) {
      try {
        DataFolder dataFolder = DataFolder.findFolder(fileObject);
        return dataFolder;
      }
      catch (Exception ex) {
        LOGGER.error("Can't find data folder for file : " + fileObject, ex);
      }
    }
    return null;
  }

  @Override
  public void stateChanged(final ChangeEvent e) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        fireChanged();
      }
    });
  }

  private void fireChanged() {
    this.changeSupport.fireChange();
  }

  @Override
  public boolean acceptDataObject(final DataObject obj) {
    if (obj instanceof MMDDataObject) {
      return true;
    }

    return !obj.getPrimaryFile().getName().startsWith(".");
  }

}
