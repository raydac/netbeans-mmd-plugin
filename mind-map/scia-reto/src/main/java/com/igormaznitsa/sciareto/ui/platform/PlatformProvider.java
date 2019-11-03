/* 
 * Copyright (C) 2018 Igor Maznitsa.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package com.igormaznitsa.sciareto.ui.platform;

import javax.annotation.Nonnull;
import org.apache.commons.lang.SystemUtils;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;

public final class PlatformProvider {
  private static final Platform INSTANCE;

  private static final Logger LOGGER = LoggerFactory.getLogger(PlatformProvider.class);
  private static final boolean DETECTED_ERROR_DURING_PROVIDER_INIT;
  
  static {
    Platform detected = null;

    boolean detectedError = false;
    
    if (SystemUtils.IS_OS_MAC_OSX){
      try{
        detected = (Platform) Class.forName("com.igormaznitsa.sciareto.ui.platform.PlatformMacOSX").getConstructor().newInstance(); //NOI18N
      }catch(Throwable ex){
        LOGGER.error("Can't init MACOSX platform specific part",ex); //NOI18N
        detected = null;
        detectedError = true;
      }
    } else if (SystemUtils.IS_OS_LINUX) {
      try {
        detected = (Platform) Class.forName("com.igormaznitsa.sciareto.ui.platform.PlatformLinux").getConstructor().newInstance(); //NOI18N
      } catch (Throwable ex) {
        LOGGER.error("Can't init LINUX platform specific part", ex); //NOI18N
        detected = null;
        detectedError = true;
      }
    } else if (SystemUtils.IS_OS_WINDOWS) {
      try {
        detected = (Platform) Class.forName("com.igormaznitsa.sciareto.ui.platform.PlatformWindows").getConstructor().newInstance(); //NOI18N
      } catch (Throwable ex) {
        LOGGER.error("Can't init WINDOWS platform specific part", ex); //NOI18N
        detected = null;
        detectedError = true;
      }
    }
    
    INSTANCE = detected == null ? new PlatformDefault() : detected;
    LOGGER.info("Platform features provider is '"+INSTANCE.getName()+'\''); //NOI18N
  
    DETECTED_ERROR_DURING_PROVIDER_INIT = detectedError;
  }
  
  private PlatformProvider(){
    
  }
  
  @Nonnull
  public static Platform getPlatform(){
    return INSTANCE;
  }

  public static boolean isErrorDetected(){
    return DETECTED_ERROR_DURING_PROVIDER_INIT;
  }
}
