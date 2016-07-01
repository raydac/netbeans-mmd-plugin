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
package com.igormaznitsa.sciareto.ui;

import java.io.File;
import java.io.IOException;
import javax.annotation.Nonnull;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;

public final class SystemUtils {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(SystemUtils.class);
  
  private SystemUtils(){
  }

  public static boolean deleteFile(@Nonnull final File file, final boolean moveToTrashIfPossible) {
    final com.sun.jna.platform.FileUtils fileUtils = com.sun.jna.platform.FileUtils.getInstance();
    boolean result = false;
    
    if (fileUtils.hasTrash() && moveToTrashIfPossible){
      LOGGER.info("Moving file to trash : " + file);
      try{
        fileUtils.moveToTrash(new File[]{file});
        result = true;
      }catch(IOException ex){
        LOGGER.error("Can't move file to trash : "+file, ex);
      }
    } else {
      LOGGER.info("Permanently deleting file : "+file);
      if (file.isDirectory()){
        try{
          org.apache.commons.io.FileUtils.deleteDirectory(file);
          result = true;
        }catch(IOException ex){
          LOGGER.error("Can't delete directory : "+file, ex);
        }
      } else if (file.isFile()){
        if (!file.delete()){
          LOGGER.error("Can't delete file : " + file);
        }
      }
    }
    return result;
  }
  
  public static void saveUTFText(@Nonnull final File file, @Nonnull final CharSequence text) throws IOException {
    org.apache.commons.io.FileUtils.write(file, text, "UTF-8",false);
  }
}
