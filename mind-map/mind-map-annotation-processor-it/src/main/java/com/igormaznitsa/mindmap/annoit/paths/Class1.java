package com.igormaznitsa.mindmap.annoit.paths;

import com.igormaznitsa.mindmap.annotations.MmdFileRef;
import com.igormaznitsa.mindmap.annotations.MmdTopic;

@MmdFileRef(target = RootFile.class)
@MmdTopic(path = {"a", "b", "c", "d"}, title = "xxx")
public class Class1 {
  @MmdTopic(path = {"c", "z"})
  public void class1Method1() {

  }

  @MmdTopic(path = {"a"})
  public void class1Method2() {

  }

  @MmdTopic(path = {"d"})
  public void class1Method3() {

  }

  @MmdTopic(path = {"xxx"})
  public void class1MethodXXX() {

  }

  @MmdTopic(path = RootFile.ROOT_ID)
  public void classABC1() {

  }

  @MmdTopic(path = RootFile.ROOT_ID)
  public void classABC2() {

  }

}
