package com.igormaznitsa.mindmap.annoit.paths;

import com.igormaznitsa.mindmap.annotations.MmdFile;
import com.igormaznitsa.mindmap.annotations.MmdTopic;

@MmdFile(rootTopic = @MmdTopic(title = "root", uid = RootFile.ROOT_ID))
public class RootFile {

  public static final String ROOT_ID = "$ROOT$";

  @MmdTopic
  public void method1() {

  }

  @MmdTopic(path = {"a", "b", "c"})
  public void method2() {

  }
}
