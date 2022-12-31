package com.igormaznitsa.mindmap.annoit.paths;

import com.igormaznitsa.mindmap.annotations.MmdFile;
import com.igormaznitsa.mindmap.annotations.MmdTopic;

@MmdFile(rootTopic = @MmdTopic(title = "root", uid = "$ROOT$"))
public class RootFile {

  @MmdTopic
  public void method1() {

  }

  @MmdTopic(path = {"a", "b", "c"})
  public void method2() {

  }
}
