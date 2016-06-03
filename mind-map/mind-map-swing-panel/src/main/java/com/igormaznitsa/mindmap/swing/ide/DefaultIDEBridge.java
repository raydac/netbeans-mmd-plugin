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
package com.igormaznitsa.mindmap.swing.ide;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import com.igormaznitsa.commons.version.Version;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;

class DefaultIDEBridge implements IDEBridge {

  private static final Version IDE_VERSION = new Version("UNKNOWN");
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultIDEBridge.class);
  
  private final Map<String,Image> IMAGE_CACHE = new HashMap<String, Image>();
  
  @Override
  @Nonnull
  public Version getIDEVersion() {
    return IDE_VERSION;
  }

  @Override
  public void showIDENotification(@Nonnull final String title, @Nonnull final String message, @Nonnull final NotificationType type) {
    final int msgtype;
    switch (type) {
      case INFO:
        LOGGER.info("IDENotification : (" + title + ") " + message);
        msgtype = JOptionPane.INFORMATION_MESSAGE;
        break;
      case WARNING:
        LOGGER.warn("IDENotification : (" + title + ") " + message);
        msgtype = JOptionPane.WARNING_MESSAGE;
        break;
      case ERROR:
        LOGGER.error("IDENotification : (" + title + ") " + message);
        msgtype = JOptionPane.ERROR_MESSAGE;
        break;
      default:{
        LOGGER.warn("*IDENotification : (" + title + ") " + message);
        msgtype = JOptionPane.WARNING_MESSAGE;
      }
    }
    
    Utils.safeSwingCall(new Runnable() {
      @Override
      public void run() {
        JOptionPane.showMessageDialog(null, message, title, msgtype);
      }
    });
  }

  @Override
  public void notifyRestart() {
    JOptionPane.showMessageDialog(null, "Work of application will be completed for request! You have to restart it!","Restart application",JOptionPane.WARNING_MESSAGE);
    System.exit(0);
  }

  @Nonnull
  private static String removeStartSlash(@Nonnull final String path) {
    String result = path;
    if (path.startsWith("/") || path.startsWith("\\")){
      result = result.substring(1);
    }
    return result;
  }
  
  @Override
  @Nonnull
  public Icon loadIcon(@Nonnull final String path,@Nonnull final Class<?> klazz) {
    Image image = null;
    synchronized(IMAGE_CACHE){
      image = IMAGE_CACHE.get(path);
      if (image == null){
        final InputStream in = klazz.getClassLoader().getResourceAsStream(Assertions.assertNotNull("Icon path must not be null",removeStartSlash(path)));
        if (in == null) throw new IllegalArgumentException("Can't find icon resource : "+path);
        try{
          image = ImageIO.read(in);
        }catch(IOException ex){
          throw new IllegalArgumentException("Can't load icon resource : "+path,ex);
        }
        IMAGE_CACHE.put(path, image);
      }
    }
    return new ImageIcon(image);
  }

}
