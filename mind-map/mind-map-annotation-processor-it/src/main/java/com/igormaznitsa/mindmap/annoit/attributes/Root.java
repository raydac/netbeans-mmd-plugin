package com.igormaznitsa.mindmap.annoit.attributes;

import com.igormaznitsa.mindmap.annotations.Direction;
import com.igormaznitsa.mindmap.annotations.MmdColor;
import com.igormaznitsa.mindmap.annotations.MmdEmoticon;
import com.igormaznitsa.mindmap.annotations.MmdFile;
import com.igormaznitsa.mindmap.annotations.MmdTopic;

@MmdFile(
    fileName = "attributes",
    rootTopic = @MmdTopic(
        uid = "rootTopicUid",
        title = "Root topic",
        colorFill = MmdColor.AQUA,
        colorText = MmdColor.ORANGE,
        colorBorder = MmdColor.RED,
        emoticon = MmdEmoticon.ACORN,
        note = "Some test\nmultiline note\none more line",
        uri = "https://sciareto.com"
    ))
public class Root {

  @MmdTopic(title = "method one",
      colorFill = MmdColor.TOMATO,
      emoticon = MmdEmoticon.ABACUS
  )
  public void Method1() {

  }

  @MmdTopic(uid = "66722", title = "method two", emoticon = MmdEmoticon.ACCOUNT_BALANCES)
  public void Method2() {

  }

  @MmdTopic(title = "method three", jumpTo = "66722", emoticon = MmdEmoticon.ANCHOR)
  public void Method3() {

  }

  @MmdTopic(path = "method one", emoticon = MmdEmoticon.ADMINISTRATOR, direction = Direction.LEFT)
  public void SubMethod1() {

  }

  @MmdTopic(path = "method one", emoticon = MmdEmoticon.ANDROID, jumpTo = "Root topic")
  public void SubMethod2() {

  }

  @MmdTopic(path = "method one", emoticon = MmdEmoticon.APPLE, jumpTo = "rootTopicUid")
  public void SubMethod3() {

  }
}
