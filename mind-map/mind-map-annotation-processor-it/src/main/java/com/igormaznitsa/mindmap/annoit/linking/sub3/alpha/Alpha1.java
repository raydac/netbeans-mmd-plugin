package com.igormaznitsa.mindmap.annoit.linking.sub3.alpha;

import com.igormaznitsa.mindmap.annotations.MmdFileLink;
import com.igormaznitsa.mindmap.annotations.MmdTopic;

@MmdFileLink(uid = "alpha")
public class Alpha1 {
  @MmdTopic
  public void alpha() {

  }

  @MmdTopic(fileUid = "gamma", path = {"p1", "p2", "alpha_"})
  public void path1() {

  }

}
