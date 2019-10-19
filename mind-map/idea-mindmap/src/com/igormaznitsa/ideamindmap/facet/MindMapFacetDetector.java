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

package com.igormaznitsa.ideamindmap.facet;

import com.igormaznitsa.ideamindmap.filetype.MindMapFileType;
import com.intellij.facet.FacetType;
import com.intellij.framework.detection.FacetBasedFrameworkDetector;
import com.intellij.framework.detection.FileContentPattern;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.StandardPatterns;
import com.intellij.util.indexing.FileContent;
import javax.annotation.Nonnull;

public class MindMapFacetDetector extends FacetBasedFrameworkDetector<MindMapFacet, MindMapFacetConfiguration> {

  public MindMapFacetDetector() {
    super("NBMindMapFacetDetector");
  }

  @Override
  public FacetType<MindMapFacet, MindMapFacetConfiguration> getFacetType() {
    return MindMapFacetType.INSTANCE;
  }

  @Nonnull
  @Override
  public FileType getFileType() {
    return MindMapFileType.INSTANCE;
  }

  @Nonnull
  @Override
  public ElementPattern<FileContent> createSuitableFilePattern() {
    return FileContentPattern.fileContent().withName(StandardPatterns.string().endsWith(".mmd"));
  }
}
