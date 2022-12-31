package com.igormaznitsa.mindmap.annoit.paths;

import com.igormaznitsa.mindmap.annotations.MmdTopic;

public class Class2 extends Class1 {
  @MmdTopic
  public void Class2Method() {

  }

  @MmdTopic(path = "$ROOT$")
  public void Class3Method() {

  }
}
