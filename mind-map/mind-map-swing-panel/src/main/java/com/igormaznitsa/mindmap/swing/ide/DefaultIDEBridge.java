/*
 * Copyright 2015-2018 Igor Maznitsa.
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

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.commons.version.Version;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

class DefaultIDEBridge implements IDEBridge {

  private static final Version IDE_VERSION = new Version("UNKNOWN");
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultIDEBridge.class);

  private final Map<String, Image> IMAGE_CACHE = new HashMap<>();

  private static String removeStartSlash(final String path) {
    String result = path;
    if (path.startsWith("/") || path.startsWith("\\")) {
      result = result.substring(1);
    }
    return result;
  }

  @Override
  public Version getIDEVersion() {
    return IDE_VERSION;
  }

  @Override
  public void showIDENotification(final String title, final String message,
                                  final NotificationType type) {
    final int messageType;
    switch (type) {
      case INFO:
        LOGGER.info("IDENotification : (" + title + ") " + message);
        messageType = JOptionPane.INFORMATION_MESSAGE;
        break;
      case WARNING:
        LOGGER.warn("IDENotification : (" + title + ") " + message);
        messageType = JOptionPane.WARNING_MESSAGE;
        break;
      case ERROR:
        LOGGER.error("IDENotification : (" + title + ") " + message);
        messageType = JOptionPane.ERROR_MESSAGE;
        break;
      default: {
        LOGGER.warn("*IDENotification : (" + title + ") " + message);
        messageType = JOptionPane.WARNING_MESSAGE;
      }
    }

    Utils.safeSwingCall(() -> JOptionPane.showMessageDialog(null, message, title, messageType));
  }

  @Override
  public void notifyRestart() {
    JOptionPane.showMessageDialog(null, "Work of application will be completed for request! You have to restart it!", "Restart application", JOptionPane.WARNING_MESSAGE);
    System.exit(0);
  }

  @Override
  public Icon loadIcon(final String path, final Class<?> klazz) {
    Image image;
    synchronized (IMAGE_CACHE) {
      image = IMAGE_CACHE.get(path);
      if (image == null) {
        final InputStream in = klazz.getClassLoader().getResourceAsStream(
            requireNonNull(removeStartSlash(path), "Icon path must not be null"));
        if (in == null) {
          throw new IllegalArgumentException("Can't find icon resource : " + path);
        }
        try {
          image = ImageIO.read(in);
        } catch (IOException ex) {
          throw new IllegalArgumentException("Can't load icon resource : " + path, ex);
        }
        IMAGE_CACHE.put(path, image);
      }
    }
    return new ImageIcon(image);
  }

}
