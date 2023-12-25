package com.igormaznitsa.mindmap.annoit.inmethods;

import com.igormaznitsa.mindmap.annoit.paths.RootFile;
import com.igormaznitsa.mindmap.annotations.HasMmdMarkedElements;
import com.igormaznitsa.mindmap.annotations.MmdColor;
import com.igormaznitsa.mindmap.annotations.MmdFileRef;
import com.igormaznitsa.mindmap.annotations.MmdTopic;

@MmdFileRef(target = RootFile.class)
@MmdTopic(title = "LOCAL_VARIABLES")
public class Klazz1 {

  @HasMmdMarkedElements
  Klazz1(String some) {
    @MmdTopic
    int hello = 10;
  }

  // method's annotations must not be presented
  public void method1() {
    int a = 0;
    @MmdTopic(title = "someB")
    int b = 1;
  }

  @HasMmdMarkedElements
  private void method2() {
    @MmdTopic(title = "system", colorFill = MmdColor.Green)
    int system;
    System.out.println("Hello");

    {
      {
        if (System.nanoTime() > 100) {
          @MmdTopic(title = "EEE")
          int eee = 666;
        }
      }
    }
  }
}
