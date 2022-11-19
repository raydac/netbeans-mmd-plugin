package com.igormaznitsa.mindmap.annoit.files;

import com.igormaznitsa.mindmap.annotations.MmdFileLink;
import com.igormaznitsa.mindmap.annotations.MmdTopic;

@MmdFileLink(uid = "Klass2")
public class Klass4 {
  @MmdTopic
  void klass4_method() {
  }


  @MmdFileLink(uid = "Klass1")
  @MmdTopic
  void klass4_method_class1(@MmdFileLink(uid = "Klass3") @MmdTopic int a,
                            @MmdFileLink(uid = "Klass3_1") @MmdTopic int b) {
  }
}
