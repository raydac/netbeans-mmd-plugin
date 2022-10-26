package com.igormaznitsa.mindmap.annotation.processor.it;

import com.igormaznitsa.mindmap.model.annotations.MmdFile;
import com.igormaznitsa.mindmap.model.annotations.MmdTopic;

@MmdFile
@MmdTopic
public class Root {

  @MmdTopic Root() {

  }

  public void Huzza(
      @MmdTopic
      int some,
      @MmdTopic
      long another
  ) {

  }

  @MmdTopic
  public void SomeMethod(int a) {

  }

}
