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
package com.igormaznitsa.sciareto.ui.platform;

import javax.annotation.Nonnull;
import org.apache.commons.lang.SystemUtils;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;

public final class PlatformProvider {
  private static final Platform INSTANCE;

  private static final Logger LOGGER = LoggerFactory.getLogger(PlatformProvider.class);
  
  static {
    Platform detected = null;
    if (SystemUtils.IS_OS_MAC_OSX){
      try{
        detected = (Platform) Class.forName("com.igormaznitsa.sciareto.ui.platform.PlatformMacOSX").newInstance();
      }catch(Exception ex){
        LOGGER.error("Can't init MACOSX platform specific part",ex);
        detected = null;
      }
    }
    INSTANCE = detected == null ? new PlatformDefault() : detected;
    LOGGER.info("Detected platform : "+INSTANCE.getName());
  }
  
  private PlatformProvider(){
    
  }
  
  @Nonnull
  public static final Platform getPlatform(){
    return INSTANCE;
  }
}
