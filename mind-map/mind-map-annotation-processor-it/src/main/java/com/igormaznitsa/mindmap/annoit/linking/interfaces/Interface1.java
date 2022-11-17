package com.igormaznitsa.mindmap.annoit.linking.interfaces;

import com.igormaznitsa.mindmap.annotations.MmdTopic;

@MmdTopic
public interface Interface1 extends RootInterface {
  @MmdTopic
  void method1();

  @MmdTopic
  void method2();
}
