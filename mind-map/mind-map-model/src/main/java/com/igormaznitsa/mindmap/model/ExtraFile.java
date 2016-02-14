/*
 * Copyright 2015 Igor Maznitsa.
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
package com.igormaznitsa.mindmap.model;

import java.io.File;
import java.net.URISyntaxException;

import javax.annotation.Nonnull;

import org.apache.commons.io.FilenameUtils;

public class ExtraFile extends Extra<MMapURI> implements ExtraLinkable {

  private static final long serialVersionUID = -478916403235887225L;

  private final MMapURI fileUri;

  private volatile String cachedString;

  private final boolean mmdFileFlag;

  public ExtraFile(@Nonnull final MMapURI file) {
    this.fileUri = file;
    this.mmdFileFlag = this.fileUri.getExtension().equalsIgnoreCase("mmd"); //NOI18N
  }

  public ExtraFile(@Nonnull final String text) throws URISyntaxException {
    this(new MMapURI(text));
  }

  public boolean isMMDFile() {
    return this.mmdFileFlag;
  }

  @Override
  @Nonnull
  public MMapURI getValue() {
    return fileUri;
  }

  @Override
  @Nonnull
  public ExtraType getType() {
    return ExtraType.FILE;
  }

  @Override
  @Nonnull
  public String getAsString() {
    if (this.cachedString == null) {
      this.cachedString = this.fileUri.asFile(null).getPath();
    }
    return this.cachedString;
  }

  @Override
  @Nonnull
  public String provideAsStringForSave() {
    return this.fileUri.toString();
  }

  @Override
  @Nonnull
  public MMapURI getAsURI() {
    return this.fileUri;
  }

  public boolean isAbsolute() {
    return this.fileUri.isAbsolute();
  }

  @Nonnull
  private static String ensureFolderPath(@Nonnull final String str) {
    if (str.endsWith("/") || str.endsWith("\\")) return str;
    return str + File.separatorChar;
  }
  
  public boolean hasParent(@Nonnull final File baseFolder, @Nonnull final MMapURI folder) {
    final File theFile = this.fileUri.asFile(baseFolder);
    final File thatFile = folder.asFile(baseFolder);

    final String theFilePath = FilenameUtils.normalize(theFile.getAbsolutePath());
    final String thatFilePath = ensureFolderPath(FilenameUtils.normalize(thatFile.getAbsolutePath()));
    
    if (!theFilePath.equals(thatFilePath) && theFilePath.startsWith(thatFilePath)) {
      final String diff = theFilePath.substring(thatFilePath.length()-1);
      return diff.startsWith("\\") || diff.startsWith("/");
    }
    else {
      return false;
    }
  }
  
  public boolean isSameOrHasParent(@Nonnull final File baseFolder, @Nonnull final MMapURI file) {
    final File theFile = this.fileUri.asFile(baseFolder);
    final File thatFile = file.asFile(baseFolder);

    final String theFilePath = FilenameUtils.normalize(theFile.getAbsolutePath());
    final String thatFilePath = FilenameUtils.normalize(thatFile.getAbsolutePath());

    if (theFilePath.startsWith(thatFilePath)) {
      final String diff = theFilePath.substring(thatFilePath.length());
      return diff.isEmpty() || diff.startsWith("\\") || diff.startsWith("/") || thatFilePath.endsWith("/") || thatFilePath.endsWith("\\");
    }
    else {
      return false;
    }
  }

  public boolean isSame(@Nonnull final File baseFolder, @Nonnull final MMapURI file) {
    final File theFile = this.fileUri.asFile(baseFolder);
    final File thatFile = file.asFile(baseFolder);

    final String theFilePath = FilenameUtils.normalize(theFile.getAbsolutePath());
    final String thatFilePath = FilenameUtils.normalize(thatFile.getAbsolutePath());

    return theFilePath.equals(thatFilePath);
  }

}
