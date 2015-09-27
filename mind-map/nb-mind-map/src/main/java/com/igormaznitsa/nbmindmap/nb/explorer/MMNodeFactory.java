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

import com.igormaznitsa.nbmindmap.utils.BadgeIcons;
import java.awt.Image;
import java.beans.BeanInfo;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NodeFactory.Registration(projectType = {
  "org-netbeans-modules-ant-freeform",
  "org-netbeans-modules-apisupport-project",
  "org-netbeans-modules-apisupport-project-suite",
  "org-netbeans-modules-apisupport-project-suite-jnlp",
  "org-netbeans-modules-apisupport-project-suite-osgi",
  "org-netbeans-modules-apisupport-project-suite-package",
  "org-netbeans-modules-autoproject",
  "org-netbeans-modules-cnd-api-project",
  "org-netbeans-modules-cnd-makeproject",
  "org-netbeans-modules-groovy-grailsproject",
  "org-netbeans-modules-j2ee-clientproject",
  "org-netbeans-modules-j2ee-earproject",
  "org-netbeans-modules-j2ee-ejbjarproject",
  "org-netbeans-modules-j2me-project",
  "org-netbeans-modules-java-j2seproject",
  "org-netbeans-modules-javacard-capproject",
  "org-netbeans-modules-javacard-clslibproject",
  "org-netbeans-modules-javacard-eapproject",
  "org-netbeans-modules-javacard-extlibproject",
  "org-netbeans-modules-javacard-webproject",
  "org-netbeans-modules-javacard-project",
  "org-netbeans-modules-maven",
  "org-netbeans-modules-mobility-project",
  "org-netbeans-modules-php-phpproject",
  "org-netbeans-modules-php-project",
  "org-netbeans-modules-web-clientproject",
  "org-netbeans-modules-web-project",
  "org-netbeans-modules-bpel-project",
  "org-netbeans-modules-javaee-project",
  "org-netbeans-modules-javafx2-project",
  "org-netbeans-modules-ruby-project",
  "org-netbeans-modules-sql-project",
  "org-netbeans-modules-xslt-project",
  "org-netbeans-modules-gradle-project",
  "org-netbeans-modules-scala-project",
  "org-netbeans-modules-scala-sbt",}, position = 10000)
public class MMNodeFactory implements NodeFactory {

  private static final Logger logger = LoggerFactory.getLogger(MMNodeFactory.class);

  @Override
  public NodeList<?> createNodes(final Project project) {
    logger.debug("Creating knowledge nodes for " + project);

    final Node node = new AbstractNode(Children.create(new ChildFactory<SourceGroup>() {
      private final MMKnowledgeSources sources = new MMKnowledgeSources(project);

      @Override
      protected Node createNodeForKey(SourceGroup key) {
        return sources.node(key);
      }

      @Override
      protected boolean createKeys(List<SourceGroup> toPopulate) {
        toPopulate.addAll(sources.keys());
        return true;
      }
    },true)){

      @Override
      public String getName() {
        return "Knowledge";
      }

      @Override
      public Image getOpenedIcon(int type) {
        return BadgeIcons.BADGED_FOLDER_OPEN;
      }

      @Override
      public Image getIcon(int type) {
        return BadgeIcons.BADGED_FOLDER;
      }
      
    };

    return NodeFactorySupport.fixedNodeList(node);
  }

}
