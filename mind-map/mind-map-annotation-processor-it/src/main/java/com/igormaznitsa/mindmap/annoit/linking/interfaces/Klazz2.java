package com.igormaznitsa.mindmap.annoit.linking.interfaces;

import com.igormaznitsa.mindmap.annotations.MmdTopic;

public class Klazz2 extends Klazz1 {
  @MmdTopic
  private void someMethod() {

  }

  @MmdTopic(uid = "$$$111")
  public void method2() {

  }

  private void someMethod2(@MmdTopic int a, @MmdTopic int b) {

  }
}
