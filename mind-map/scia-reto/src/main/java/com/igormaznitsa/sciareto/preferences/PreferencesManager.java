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
package com.igormaznitsa.sciareto.preferences;

import com.igormaznitsa.meta.common.utils.IOUtils;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import java.awt.Font;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.codec.binary.Base64;

public class PreferencesManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(PreferencesManager.class);
  private static final PreferencesManager INSTANCE = new PreferencesManager();
  private static final String PROPERTY_UUID = "installation.uuid"; //NOI18N
  private final Preferences prefs;
  private final UUID installationUUID;
  private final Map<String, Object> localCache = new HashMap<>();

  private PreferencesManager() {
    this.prefs = Preferences.userNodeForPackage(PreferencesManager.class);
    String packedUuid = this.prefs.get(PROPERTY_UUID, null);
    if (packedUuid == null) {
      try {
        final UUID newUUID = UUID.randomUUID();
        packedUuid = Base64.encodeBase64String(IOUtils.packData(newUUID.toString().getBytes("UTF-8"))); //NOI18N
        this.prefs.put(PROPERTY_UUID, packedUuid);
        this.prefs.flush();
        LOGGER.info("Generated new installation UUID : " + newUUID.toString()); //NOI18N

        final Thread thread = new Thread(() -> {
          LOGGER.info("Send first start metrics"); //NOI18N
          com.igormaznitsa.sciareto.metrics.MetricsService.getInstance().onFirstStart();
        }, "SCIARETO_FIRST_START_METRICS"); //NOI18N
        thread.setDaemon(true);
        thread.start();

      } catch (Exception ex) {
        LOGGER.error("Can't generate UUID", ex); //NOI18N
      }
    }
    try {
      this.installationUUID = UUID.fromString(new String(IOUtils.unpackData(Base64.decodeBase64(packedUuid)), "UTF-8")); //NOI18N
      LOGGER.info("Installation UUID : " + this.installationUUID.toString()); //NOI18N
    } catch (UnsupportedEncodingException ex) {
      LOGGER.error("Can't decode UUID", ex); //NOI18N
      throw new Error("Unexpected error", ex); //NOI18N
    }
  }

  @Nonnull
  public static PreferencesManager getInstance() {
    return INSTANCE;
  }

  @Nullable
  public Font getFont(@Nonnull final Preferences pref, @Nonnull final String key, @Nullable final Font dflt) {
    synchronized (this.localCache) {
      Font result = (Font) this.localCache.get(key);
      if (result == null) {
        result = PrefUtils.str2font(pref.get(key, null), dflt);
        if (result != null) {
          this.localCache.put(key, result);
        }
      }
      return result;
    }
  }

  public void setFont(@Nonnull final Preferences pref, @Nonnull final String key, @Nullable final Font font) {
    synchronized (this.localCache) {
      if (font == null) {
        this.localCache.remove(key);
        pref.remove(key);
      } else {
        final String packed = PrefUtils.font2str(font);
        this.localCache.put(key, font);
        pref.put(key, packed);
      }
    }
  }

  public boolean getFlag(@Nonnull final Preferences pref, @Nonnull final String key, final boolean dflt) {
    synchronized (this.localCache) {
      Boolean result = (Boolean) this.localCache.get(key);
      if (result == null) {
        result = Boolean.parseBoolean(pref.get(key, Boolean.toString(dflt)));
        this.localCache.put(key, result);
      }
      return result;
    }
  }

  public void setFlag(@Nonnull final Preferences pref, @Nonnull final String key, final boolean flag) {
    synchronized (this.localCache) {
      this.localCache.put(key, flag);
      pref.put(key, Boolean.toString(flag));
    }
  }

  @Nonnull
  public UUID getInstallationUUID() {
    return this.installationUUID;
  }

  @Nonnull
  public synchronized Preferences getPreferences() {
    return this.prefs;
  }

  public synchronized void flush() {
    try {
      this.prefs.flush();
    } catch (BackingStoreException ex) {
      LOGGER.error("Can't flush preferences", ex); //NOI18N
    }
  }

}
