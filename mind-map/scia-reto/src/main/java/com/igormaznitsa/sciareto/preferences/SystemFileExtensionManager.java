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
package com.igormaznitsa.sciareto.preferences;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SystemFileExtensionManager {

  public static final Logger LOGGER = LoggerFactory.getLogger(SystemFileExtensionManager.class);

  private static final String DEFAULT_OPEN_IN_SYSTEM_EXTENSIONS = "doc,dot,wbk,"
          + "docx,docm,dotx,dotm,docb,"
          + "xls,xlt,xlm,"
          + "xlsx,xlsm,xltx,"
          + "xlsb,xla,xlam,xll,xlw,"
          + "ppt,pot,pps,"
          + "pptx,pptm,potx,potm,ppam,ppsx,ppsm,sldx,sldm,"
          + "pub,xps,"
          + "pdf,djvu,epub,"
          + "tga,tif,bmp,jp2,ico,"
          + "avi,mp2,mpeg,mpg,mp3,mp4,mpa,mid,midi,mkv,mka,m3u,flac,mov,qt,wav,"
          + "3g2,3gp,3gp2,3gpp,3gpp2,asf,asx,dat,drv,f4v,flv,gtp,h264,m4v,mod,moov,mts,rm,rmvb,spl,srt,stl,swf,ts,vcd,vid,vob,webm,wm,wmv,yuv,"
          + "bin";

  private static final SystemFileExtensionManager INSTANCE = new SystemFileExtensionManager();
  private final Map<String,String> mapLowerCaseToOrig = new ConcurrentHashMap<>();

  private SystemFileExtensionManager() {
    this.fill(DEFAULT_OPEN_IN_SYSTEM_EXTENSIONS);
  }

  private void fill(@Nonnull final String commaSeparatedString) {
    this.mapLowerCaseToOrig.clear();
    for(final String s : commaSeparatedString.split("\\,")) {
      this.mapLowerCaseToOrig.put(s.toLowerCase(Locale.ENGLISH), s);
    }
  }

  @Nonnull
  public String getDefaultExtensionsAsCommaSeparatedString() {
    return DEFAULT_OPEN_IN_SYSTEM_EXTENSIONS;
  }

  @Nonnull
  public String makeExtensionsCommaSeparatedString() {
    return this.mapLowerCaseToOrig.values().stream().sorted().collect(Collectors.joining(","));
  }

  public boolean isSystemFileExtension(@Nonnull final String ext) {
    return this.mapLowerCaseToOrig.containsKey(ext.toLowerCase(Locale.ENGLISH));
  }

  public void setExtensionsAsCommaSeparatedString(@Nullable final String value) {
    this.fill(value == null ? DEFAULT_OPEN_IN_SYSTEM_EXTENSIONS : value);
  }

  @Nonnull
  public static SystemFileExtensionManager getInstance() {
    return INSTANCE;
  }

}
