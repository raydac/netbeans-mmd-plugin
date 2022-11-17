package com.igormaznitsa.mindmap.annoit.linking.interfaces;

import com.igormaznitsa.mindmap.annotations.MmdColor;
import com.igormaznitsa.mindmap.annotations.MmdTopic;

@MmdTopic(colorFill = MmdColor.TOMATO)
public interface Interface3 extends Interface2 {
  @MmdTopic
  void method3_1();
}

class SomeOutboundClass extends Klazz3 implements Interface3 {

  @Override
  public void method3_1() {

  }

  @MmdTopic(colorFill = MmdColor.YELLOW)
  void itsMethod() {
  }
}