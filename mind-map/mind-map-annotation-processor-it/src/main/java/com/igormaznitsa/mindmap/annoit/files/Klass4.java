package com.igormaznitsa.mindmap.annoit.files;

import com.igormaznitsa.mindmap.annotations.MmdFileRef;
import com.igormaznitsa.mindmap.annotations.MmdTopic;

@MmdFileRef(uid = "Klass2")
public class Klass4 {
  @MmdTopic
  void klass4_method() {
  }


  @MmdFileRef(uid = "Klass1")
  @MmdTopic
  void klass4_method_class1(@MmdFileRef(uid = "Klass3") @MmdTopic int a,
                            @MmdFileRef(uid = "Klass3_1") @MmdTopic int b) {
  }
}
