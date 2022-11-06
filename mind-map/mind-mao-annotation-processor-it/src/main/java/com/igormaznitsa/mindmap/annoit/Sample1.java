package com.igormaznitsa.mindmap.annoit;

import com.igormaznitsa.mindmap.model.annotations.MmdColor;
import com.igormaznitsa.mindmap.model.annotations.MmdEmoticon;
import com.igormaznitsa.mindmap.model.annotations.MmdFile;
import com.igormaznitsa.mindmap.model.annotations.MmdTopic;

@MmdFile(rootTopic = @MmdTopic(emoticon = MmdEmoticon.ACCEPT_BUTTON, colorFill = MmdColor.BLUE, colorText = MmdColor.WHITE))
public class Sample1 {

  @MmdTopic(title = "method___1")
  public void method1(@MmdTopic int a, @MmdTopic int b) {

  }
}
