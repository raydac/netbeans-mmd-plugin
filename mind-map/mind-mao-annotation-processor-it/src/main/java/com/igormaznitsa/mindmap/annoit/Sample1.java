package com.igormaznitsa.mindmap.annoit;

import com.igormaznitsa.mindmap.model.annotations.MmdColor;
import com.igormaznitsa.mindmap.model.annotations.MmdEmoticon;
import com.igormaznitsa.mindmap.model.annotations.MmdFile;
import com.igormaznitsa.mindmap.model.annotations.MmdTopic;

@MmdFile(rootTopic = @MmdTopic(emoticon = MmdEmoticon.ACCEPT_BUTTON, colorFill = MmdColor.BLUE, colorText = MmdColor.WHITE, uid = "mmdroot"))
public class Sample1 {

  @MmdTopic(title = "method___1")
  public void method1(@MmdTopic(uid = "aaa") int a, @MmdTopic(jumpTo = "aaa") int b) {

  }

  @MmdTopic(path = {"methods", "static"})
  public static void static1() {

  }
}
