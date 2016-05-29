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
package com.igormaznitsa.nbmindmap.nb.services;

import javax.annotation.Nonnull;
import com.igormaznitsa.commons.version.Version;
import com.igormaznitsa.mindmap.swing.services.IDEInfoProvider;

public class NbIDEInfoProvider implements IDEInfoProvider {

  private final Version ideVersion;
  
  public NbIDEInfoProvider(){
    final String versionInfo = System.getProperty("netbeans.productversion");
    if (versionInfo == null){
      this.ideVersion = new Version("netbeans");
    }else if (versionInfo.equalsIgnoreCase("dev")){
      this.ideVersion = new Version("netbeans-8.1-dev");
    } else {
      this.ideVersion = new Version(versionInfo.replace(' ', '-')).changePrefix("netbeans").changePostfix("");
    }
  }
  
  @Override
  @Nonnull
  public Version getIDEVersion() {
    return this.ideVersion;
  }
  
}
