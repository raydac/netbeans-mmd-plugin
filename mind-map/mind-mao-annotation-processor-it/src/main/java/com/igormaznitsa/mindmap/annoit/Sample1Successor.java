package com.igormaznitsa.mindmap.annoit;

import com.igormaznitsa.mindmap.model.annotations.Direction;
import com.igormaznitsa.mindmap.model.annotations.MmdColor;
import com.igormaznitsa.mindmap.model.annotations.MmdTopic;

public class Sample1Successor extends Sample1 {

  @MmdTopic(direction = Direction.LEFT)
  public void method2() {

  }

  @MmdTopic(jumpTo = "mmdroot", note = "some note", colorFill = MmdColor.YELLOW)
  public void method3(@MmdTopic long data1, @MmdTopic long data2) {
    int varA;

    int varB;
  }
}
