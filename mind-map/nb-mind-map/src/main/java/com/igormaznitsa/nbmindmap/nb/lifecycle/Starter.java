/*
 * Copyright 2016 Igor Maznitsa.
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
package com.igormaznitsa.nbmindmap.nb.lifecycle;

import java.io.File;
import org.openide.modules.OnStart;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.external.ExternalPlugins;

@OnStart
public class Starter implements Runnable {
  
  private static final long serialVersionUID = 2421511056149761257L;

  private static final Logger LOGGER = LoggerFactory.getLogger(Starter.class);
  
  private static final String PROPERTY = "nbmmd.plugin.folder";
  
  @Override
  public void run() {
    final String pluginFolder = System.getProperty(PROPERTY);
    if (pluginFolder!=null) {
      final File folder = new File(pluginFolder);
      if (folder.isDirectory()) {
        LOGGER.info("Loading plugins from folder : "+folder);
        new ExternalPlugins(folder).init();
      } else {
        LOGGER.error("Can't find plugin folder : " + folder);
      }
    }else{
      LOGGER.info("Property "+PROPERTY+" is not defined");
    }
    
  }
  
  
}
