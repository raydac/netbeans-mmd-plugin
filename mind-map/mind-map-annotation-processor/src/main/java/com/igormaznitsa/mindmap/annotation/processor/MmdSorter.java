package com.igormaznitsa.mindmap.annotation.processor;

import com.igormaznitsa.mindmap.model.MindMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MmdSorter {

  public MmdSorter() {

  }

  public Map<String, MindMap> sort(final List<FoundMmdAnnotation> foundAnnotations) {
    return Collections.singletonMap("test.mmd", new MindMap(true));
  }
}
