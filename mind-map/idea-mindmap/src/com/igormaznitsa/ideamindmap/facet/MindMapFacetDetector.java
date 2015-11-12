package com.igormaznitsa.ideamindmap.facet;

import com.igormaznitsa.ideamindmap.filetype.MindMapFileType;
import com.intellij.facet.FacetType;
import com.intellij.framework.detection.FacetBasedFrameworkDetector;
import com.intellij.framework.detection.FileContentPattern;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.StandardPatterns;
import com.intellij.util.indexing.FileContent;
import org.jetbrains.annotations.NotNull;

public class MindMapFacetDetector extends FacetBasedFrameworkDetector<MindMapFacet,MindMapFacetConfiguration> {

  public MindMapFacetDetector() {
    super("NBMindMapFacetDetector");
  }

  @Override public FacetType<MindMapFacet, MindMapFacetConfiguration> getFacetType() {
    return MindMapFacetType.INSTANCE;
  }

  @NotNull @Override public FileType getFileType() {
    return MindMapFileType.INSTANCE;
  }

  @NotNull @Override public ElementPattern<FileContent> createSuitableFilePattern() {
    return FileContentPattern.fileContent().withName(StandardPatterns.string().endsWith(".mmd"));
  }
}
