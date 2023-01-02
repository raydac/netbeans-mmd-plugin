package com.igormaznitsa.mindmap.annoit.paths;

import com.igormaznitsa.mindmap.annotations.MmdFileRef;
import com.igormaznitsa.mindmap.annotations.MmdTopic;

@MmdFileRef(target = RootFile.class)
@MmdTopic(title = "multi-path1", path = "goose")
public class Class4 {

  @MmdTopic(title = "multi-path1", path = "goose")
  public void class4method1() {

  }

  @MmdTopic(title = "multi-path1", path = {"goose"})
  public void class4method2() {

  }

  @MmdTopic(title = "multi-path1", path = {"goose"})
  public void class4method3() {

  }

}
