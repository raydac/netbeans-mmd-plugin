package com.igormaznitsa.mindmap.annoit.collectors;

import com.igormaznitsa.mindmap.annotations.MmdFile;
import com.igormaznitsa.mindmap.annotations.MmdFiles;
import com.igormaznitsa.mindmap.annotations.MmdTopic;
import com.igormaznitsa.mindmap.annotations.MmdTopics;

@MmdFiles({
    @MmdFile(fileName = "FileOne", uid = "F1"),
    @MmdFile(fileName = "FileTwo", uid = "F2"),
})
public class Root {

  @MmdTopics({
      @MmdTopic(title = "Topic1", fileUid = "F1"),
      @MmdTopic(title = "Topic2", fileUid = "F2")
  })
  public void method1() {

  }
}
