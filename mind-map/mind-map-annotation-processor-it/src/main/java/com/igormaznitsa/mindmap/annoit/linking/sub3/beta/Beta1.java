package com.igormaznitsa.mindmap.annoit.linking.sub3.beta;

import com.igormaznitsa.mindmap.annotations.MmdFileLink;
import com.igormaznitsa.mindmap.annotations.MmdTopic;

@MmdFileLink(uid = "beta")
public class Beta1 {
  @MmdTopic
  public void beta() {

  }

  @MmdTopic(fileUid = "gamma", path = {"p1", "p2", "beta_"})
  public void path2() {

  }

}
