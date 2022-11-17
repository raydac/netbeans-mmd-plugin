package com.igormaznitsa.mindmap.annoit.linking.interfaces;

import com.igormaznitsa.mindmap.annotations.MmdTopic;

public class Klazz2 extends Klazz1 {
  @MmdTopic
  private void someMethod() {

  }

  private void someMethod2(@MmdTopic int a, @MmdTopic int b) {

  }
}
