package com.igormaznitsa.mindmap.annoit.case1.sub1.sib1_1;

import com.igormaznitsa.mindmap.annotations.MmdFileLink;
import com.igormaznitsa.mindmap.annotations.MmdTopic;

@MmdFileLink(uid = "sub1")
public class InternalLogic {
  @MmdTopic
  public int someLogicMethod(@MmdTopic int a, @MmdTopic int b, @MmdTopic int c){
    return 1;
  }
}
