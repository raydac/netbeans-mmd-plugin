module com.igormaznitsa.mmd.ide.common {
  requires transitive com.igormaznitsa.mmd.model;
  requires transitive com.igormaznitsa.mmd.swing.panel;
  requires transitive java.datatransfer;
  requires transitive java.desktop;

  requires org.apache.commons.io;
  requires org.apache.commons.lang3;

  exports com.igormaznitsa.mindmap.ide.commons;
  exports com.igormaznitsa.mindmap.ide.commons.editors;
  exports com.igormaznitsa.mindmap.ide.commons.preferences;
}
