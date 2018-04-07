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
package com.igormaznitsa.sciareto.ui;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;

public final class SystemUtils {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(SystemUtils.class);

  private SystemUtils(){
  }

  public static void setDebugLevelForJavaLogger(@Nonnull final Level newLevel) {
    final java.util.logging.Logger rootLogger = java.util.logging.LogManager.getLogManager().getLogger(""); //NOI18N
    final java.util.logging.Handler[] handlers = rootLogger.getHandlers();
    rootLogger.setLevel(newLevel);
    for (final java.util.logging.Handler h : handlers) {
      h.setLevel(newLevel);
    }
  }
  
  public static boolean isMac(){
    return org.apache.commons.lang.SystemUtils.IS_OS_MAC;
  }
  
  public static boolean deleteFile(@Nonnull final File file, final boolean moveToTrashIfPossible) {
    final com.sun.jna.platform.FileUtils fileUtils = com.sun.jna.platform.FileUtils.getInstance();
    boolean result = false;
    
    if (fileUtils.hasTrash() && moveToTrashIfPossible){
      LOGGER.info("Moving file to trash : " + file); //NOI18N
      try{
        fileUtils.moveToTrash(new File[]{file});
        result = true;
      }catch(IOException ex){
        LOGGER.error("Can't move file to trash : "+file, ex); //NOI18N
      }
    } else {
      LOGGER.info("Permanently deleting file : "+file); //NOI18N
      if (file.isDirectory()){
        try{
          org.apache.commons.io.FileUtils.deleteDirectory(file);
          result = true;
        }catch(IOException ex){
          LOGGER.error("Can't delete directory : "+file, ex); //NOI18N
        }
      } else if (file.isFile()){
        if (!file.delete()){
          LOGGER.error("Can't delete file : " + file); //NOI18N
        } else {
          result = true;
        }
      }
    }
    return result;
  }
  
  public static void saveUTFText(@Nonnull final File file, @Nonnull final CharSequence text) throws IOException {
    org.apache.commons.io.FileUtils.write(file, text, "UTF-8",false); //NOI18N
  }
}
