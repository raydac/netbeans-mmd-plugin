package com.igormaznitsa.mindmap.annoit;

import com.igormaznitsa.mindmap.model.annotations.Direction;
import com.igormaznitsa.mindmap.model.annotations.MmdTopic;

public class Sample1Successor extends Sample1 {

  @MmdTopic(direction = Direction.LEFT)
  public void method2() {

  }
}
