package com.igormaznitsa.mindmap.annoit.linking.interfaces;

import com.igormaznitsa.mindmap.annotations.MmdColor;
import com.igormaznitsa.mindmap.annotations.MmdTopic;

@MmdTopic(colorFill = MmdColor.GREEN)
public class Klazz1 implements Interface2 {
  @MmdTopic
  @Override
  public void method1() {

  }

  @MmdTopic
  @Override
  public void method2() {

  }

  @MmdTopic
  @Override
  public void method2_1() {

  }
}
