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

import com.igormaznitsa.nbmindmap.nb.editor.MMDDataObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class MMKnowledgeSources implements NodeList<SourceGroup>, ChangeListener, DataFilter {
  private static final long serialVersionUID = -1360299214288653958L;
  
  private static Map<String,String[]> PROJECT_SOURCES = new HashMap<>();
  static{
    PROJECT_SOURCES.put("org.netbeans.modules.php.project.PhpProject", new String[]{"PHPSOURCE"});
    PROJECT_SOURCES.put("org.netbeans.modules.maven.NbMavenProjectImpl", new String[]{"java","main","resources"});
    PROJECT_SOURCES.put("org.netbeans.modules.groovy.grailsproject.GrailsProject", new String[]{"docs","grails-app","scripts","src","web-app","conf","controllers","domain","services","utils","views"});
    PROJECT_SOURCES.put("org.netbeans.modules.java.j2seproject.J2SEProject", new String[]{"java", "main", "resources"});
    PROJECT_SOURCES.put("org.netbeans.modules.web.clientproject.ClientSideProject", new String[]{"HTML5-Sources"});
    PROJECT_SOURCES.put("org.netbeans.modules.j2ee.earproject.EarProject", new String[]{"java", "main", "resources"});
    PROJECT_SOURCES.put("org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject", new String[]{"java", "main", "resources"});
    PROJECT_SOURCES.put("org.netbeans.modules.web.project.WebProject", new String[]{"java", "main", "resources"});
  }
  
  private final Project project;
  private final Sources projectSources;
  private final ChangeListener changeListener;
  private final ChangeSupport changeSupport = new ChangeSupport(this);

  private static final Logger logger = LoggerFactory.getLogger(MMKnowledgeSources.class);
  
  public MMKnowledgeSources(final Project project) {
    this.project = project;
    this.projectSources = ProjectUtils.getSources(project);
    this.changeListener = WeakListeners.change(this, this.projectSources);
  }
  

  private static SourceGroup [] getSourceGroups(final Project project){
    final String klazz = project.getClass().getName();
    logger.info("Request sources for project type "+klazz);
    final String [] srcNames = PROJECT_SOURCES.get(klazz);
    if (srcNames!=null){
      final List<SourceGroup> result = new ArrayList<>();
      for(final String groupName : srcNames){
        for(final SourceGroup g : ProjectUtils.getSources(project).getSourceGroups(groupName)){
          result.add(g);
        }
      }
      return result.toArray(new SourceGroup[result.size()]);
    }else{
      return ProjectUtils.getSources(project).getSourceGroups(Sources.TYPE_GENERIC);
    }
  }
  
  @Override
  public List<SourceGroup> keys() {
    final SourceGroup[] sourceGroups = getSourceGroups(this.project);
    final List<SourceGroup> keysList = new ArrayList<>();
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
    Node node = null;
    if (key != null) {
      final FileObject rootFolder = key.getRootFolder();
      final DataFolder folder = getFolder(rootFolder);
      if (folder != null) {
        return new SourceNode(project, folder, this, key.getDisplayName());
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
        logger.error("Can't find data folder for file : " + fileObject, ex);
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
    if (obj instanceof  MMDDataObject) return true;
    
    final FileObject fobj = obj.getPrimaryFile();
    if (fobj.getName().startsWith(".")) return false;
    return fobj.isFolder() || MMDDataObject.MMD_EXT.equalsIgnoreCase(fobj.getExt());
  }
  
}
