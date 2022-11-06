package com.igormaznitsa.mindmap.annoit;

import com.igormaznitsa.mindmap.model.annotations.MmdFile;
import com.igormaznitsa.mindmap.model.annotations.MmdTopic;

@MmdFile
public class Sample1 {

  @MmdTopic(title = "method___1")
  public void method1(@MmdTopic int a, @MmdTopic int b) {

  }
}
