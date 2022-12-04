/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.igormaznitsa.sciareto.services;

import com.igormaznitsa.commons.version.Version;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.exporters.SVGImageExporter;
import com.igormaznitsa.mindmap.swing.ide.IDEBridge;
import com.igormaznitsa.mindmap.swing.ide.NotificationType;
import com.igormaznitsa.sciareto.SciaRetoStarter;
import com.igormaznitsa.sciareto.notifications.NotificationManager;
import com.igormaznitsa.sciareto.preferences.PreferencesManager;
import com.igormaznitsa.sciareto.preferences.SpecificKeys;
import com.igormaznitsa.sciareto.ui.SrI18n;
import com.igormaznitsa.sciareto.ui.UiUtils;
import com.igormaznitsa.sciareto.ui.platform.PlatformProvider;
import java.awt.Font;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.LocaleUtils;

public class SciaRetoBridge implements IDEBridge {

  private static final Logger LOGGER = LoggerFactory.getLogger(SciaRetoBridge.class);

  private final Map<String, Image> IMAGE_CACHE = new HashMap<>();

  @Nonnull
  private static String removeStartSlash(@Nonnull final String path) {
    String result = path;
    if (path.startsWith("/") || path.startsWith("\\")) { //NOI18N
      result = result.substring(1);
    }
    return result;
  }

  @Nonnull
  @Override
  public Locale getIDELocale() {
    if (true) return new Locale("ru");
    try {
      final String languageCode = PreferencesManager.getInstance().getPreferences()
          .get(SpecificKeys.PROPERTY_LANGUAGE, Locale.ENGLISH.getLanguage());
      return LocaleUtils.toLocale(languageCode);
    } catch (Exception ex){
      LOGGER.error("Error during get or decode locale record", ex);
      return Locale.ENGLISH;
    }
  }

  @Override
  @Nonnull
  public Version getIDEVersion() {
    return SciaRetoStarter.IDE_VERSION;
  }

  @Override
  @Nonnull
  public String getIDEGeneratorId() {
    return "com.igormaznitsa:scia-reto:" + this.getIDEVersion();
  }

  @Override
  public void showIDENotification(@Nonnull final String title, @Nonnull final String message,
                                  @Nonnull final NotificationType type) {
    final NotificationManager.Type msgtype;
    switch (type) {
      case INFO:
        LOGGER.info("IDENotification : (" + title + ") " + message); //NOI18N
        msgtype = NotificationManager.Type.INFO;
        break;
      case WARNING:
        LOGGER.warn("IDENotification : (" + title + ") " + message); //NOI18N
        msgtype = NotificationManager.Type.WARN;
        break;
      case ERROR:
        LOGGER.error("IDENotification : (" + title + ") " + message); //NOI18N
        msgtype = NotificationManager.Type.ERROR;
        break;
      default: {
        LOGGER.warn("*IDENotification : (" + title + ") " + message); //NOI18N
        msgtype = NotificationManager.Type.WARN;
      }
    }

    NotificationManager.getInstance().showNotification(null, title, msgtype, message);
  }

  @Override
  public void notifyRestart() {
    JOptionPane.showMessageDialog(
        null,
        SrI18n.getInstance().findBundle().getString("SciaRetoBridge.restart.text"),
        SrI18n.getInstance().findBundle().getString("SciaRetoBridge.restart.title"),
        JOptionPane.WARNING_MESSAGE);
    try {
      PlatformProvider.getPlatform().dispose();
    } finally {
      System.exit(0);
    }
  }

  @Override
  @Nonnull
  public Icon loadIcon(@Nonnull final String path, @Nonnull final Class<?> klazz) {
    Image image;
    synchronized (IMAGE_CACHE) {
      image = IMAGE_CACHE.get(path);
      if (image == null) {
        final InputStream in = klazz.getClassLoader().getResourceAsStream(
            Assertions.assertNotNull("Icon path must not be null",
                removeStartSlash(path))); //NOI18N
        if (in == null) {
          throw new IllegalArgumentException("Can't find icon resource : " + path); //NOI18N
        }
        try {
          image = ImageIO.read(in);
        } catch (IOException ex) {
          throw new IllegalArgumentException("Can't load icon resource : " + path, ex); //NOI18N
        }
        IMAGE_CACHE.put(path, image);
      }
    }
    return new ImageIcon(image);
  }

  @Override
  @Nonnull
  public Map<String, Object> lookup(@Nonnull final Map<String, Object> properties) {
    final Map<String, Object> result = new HashMap<>();
    if (properties.containsKey(SVGImageExporter.LOOKUP_PARAM_REQ_FONT)) {
      final Font font = (Font) properties.get(SVGImageExporter.LOOKUP_PARAM_REQ_FONT);
      if (font.getName().startsWith("Fira Code")) {
        final String fileName = "FiraCode-" + font.getName().substring(9).trim() + ".woff";
        try {
          final byte[] resource = IOUtils.resourceToByteArray("/fonts/woff/" + fileName);
          result.put(SVGImageExporter.LOOKUP_PARAM_RESP_WOFF_FONT_AS_ARRAY, resource);
        } catch (IOException ex) {
          LOGGER.error("Can't read WOFF resource for " + font.getName(), ex);
        }
      }
    }
    return result;
  }
}
