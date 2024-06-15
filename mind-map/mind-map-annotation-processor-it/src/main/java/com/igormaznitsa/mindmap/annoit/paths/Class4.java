package com.igormaznitsa.mindmap.annoit.paths;

import com.igormaznitsa.mindmap.annotations.MmdFileRef;
import com.igormaznitsa.mindmap.annotations.MmdTopic;
import java.util.List;
import java.util.Objects;

@MmdFileRef(target = RootFile.class)
@Deprecated
@SuppressWarnings("unchecked")
@MmdTopic(title = "multi-root", path = "goose", anchor = true)
public class Class4 {

  @MmdTopic(title = "multi-path1", path = "goose")
  public void class4method1() {
  }

  @MmdTopic(title = "multi-path1", path = {"goose"})
  public void class4method2() {

  }

  @MmdTopic(title = "multi-path1", path = {"goose"})
  public void class4method3() {

  }

  @Deprecated
  @MmdTopic(title = "between", path = "goose")
  @SafeVarargs
  public final void class4method1(List<Class4>... args) {
    Objects.requireNonNull(args);
  }

}
