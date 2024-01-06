package com.igormaznitsa.mindmap.annoit.attributes;

import com.igormaznitsa.mindmap.annotations.Direction;
import com.igormaznitsa.mindmap.annotations.HasMmdMarkedElements;
import com.igormaznitsa.mindmap.annotations.MmdColor;
import com.igormaznitsa.mindmap.annotations.MmdEmoticon;
import com.igormaznitsa.mindmap.annotations.MmdFile;
import com.igormaznitsa.mindmap.annotations.MmdTopic;

@MmdFile(
    fileName = "attributes",
    rootTopic = @MmdTopic(
        uid = "rootTopicUid",
        title = "Root topic",
        colorFill = MmdColor.Aqua,
        colorText = MmdColor.Orange,
        colorBorder = MmdColor.Red,
        emoticon = MmdEmoticon.ACORN,
        note = "Some test\nmultiline note\none more line",
        uri = "https://sciareto.com"
    ))
public class Root {

  @MmdTopic(title = "method one",
      colorFill = MmdColor.Tomato,
      emoticon = MmdEmoticon.ABACUS
  )
  public void Method1() {

  }

  @MmdTopic(title = "method three", jumpTo = "66722", emoticon = MmdEmoticon.ANCHOR)
  public void Method3() {

  }

  @MmdTopic(uid = "66722", title = "method two", emoticon = MmdEmoticon.ACCOUNT_BALANCES)
  public void Method2() {

  }

  @MmdTopic(path = "method one", emoticon = MmdEmoticon.ADMINISTRATOR, direction = Direction.LEFT)
  public void SubMethod1() {

  }

  @MmdTopic(path = "method one", colorFill = MmdColor.Black, colorText = MmdColor.White, emoticon = MmdEmoticon.ANDROID, jumpTo = "Root topic")
  public void SubMethod2() {

  }

  @MmdTopic(path = "method one", emoticon = MmdEmoticon.APPLE, jumpTo = "rootTopicUid")
  public void SubMethod3() {

  }


  @MmdTopic(path = "internals")
  @HasMmdMarkedElements
  public int internalTopics() {
    @MmdTopic(order = 3)
    int a = 0;
    @MmdTopic(order = 2)
    int b = 1;
    @MmdTopic(order = 1)
    int c = 3;

    //@MmdTopic sum
    return a + b + c;
  }

}
