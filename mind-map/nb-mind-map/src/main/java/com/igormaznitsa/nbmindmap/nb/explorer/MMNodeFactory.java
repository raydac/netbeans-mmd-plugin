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
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;

@NodeFactory.Registration(projectType = {
  "org-netbeans-modules-ant-freeform",
  "org-netbeans-modules-apisupport-project",
  "org-netbeans-modules-apisupport-project-suite",
  "org-netbeans-modules-apisupport-project-suite-jnlp",
  "org-netbeans-modules-apisupport-project-suite-osgi",
  "org-netbeans-modules-apisupport-project-suite-package",
  "org-netbeans-modules-autoproject",
  "org-netbeans-modules-bpel-project",
  "org-netbeans-modules-cnd-api-project",
  "org-netbeans-modules-gradle-project",
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
  "org-netbeans-modules-javacard-project",
  "org-netbeans-modules-javacard-webproject",
  "org-netbeans-modules-javaee-project",
  "org-netbeans-modules-javafx2-project",
  "org-netbeans-modules-maven",
  "org-netbeans-modules-mobility-project",
  "org-netbeans-modules-php-phpproject",
  "org-netbeans-modules-php-project",
  "org-netbeans-modules-ruby-project",
  "org-netbeans-modules-scala-project",
  "org-netbeans-modules-sql-project",
  "org-netbeans-modules-web-clientproject",
  "org-netbeans-modules-web-project",
  "org-netbeans-modules-xslt-project",
  "org-netbeans-modules-scala-sbt",}, position = 10000)
public class MMNodeFactory implements NodeFactory {

  private static final Logger logger = LoggerFactory.getLogger(MMNodeFactory.class);

  @Override
  public NodeList<?> createNodes(final Project project) {
    logger.info("Creating knowledge nodes for " + project);
    return new MMKnowledgeSources(project);
  }

}
