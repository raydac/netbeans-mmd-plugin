module com.igormaznitsa.mmd.swing.panel {
  requires transitive com.igormaznitsa.mmd.model;
  requires transitive java.datatransfer;
  requires transitive java.desktop;

  requires java.compiler;
  requires java.prefs;
  requires java.xml;
  requires org.apache.commons.io;
  requires org.apache.commons.lang3;
  requires org.apache.commons.text;
  requires org.json;
  requires org.jsoup;

  exports com.igormaznitsa.mindmap.plugins;
  exports com.igormaznitsa.mindmap.plugins.api;
  exports com.igormaznitsa.mindmap.plugins.api.parameters;
  exports com.igormaznitsa.mindmap.plugins.exporters;
  exports com.igormaznitsa.mindmap.plugins.external;
  exports com.igormaznitsa.mindmap.plugins.misc;
  exports com.igormaznitsa.mindmap.plugins.processors;
  exports com.igormaznitsa.mindmap.plugins.tools;
  exports com.igormaznitsa.mindmap.print;
  exports com.igormaznitsa.mindmap.swing.colorpicker;
  exports com.igormaznitsa.mindmap.swing.i18n;
  exports com.igormaznitsa.mindmap.swing.ide;
  exports com.igormaznitsa.mindmap.swing.panel;
  exports com.igormaznitsa.mindmap.swing.panel.ui;
  exports com.igormaznitsa.mindmap.swing.panel.ui.gfx;
  exports com.igormaznitsa.mindmap.swing.panel.utils;
  exports com.igormaznitsa.mindmap.swing.services;

  uses com.igormaznitsa.mindmap.swing.ide.IDEBridge;
  uses com.igormaznitsa.mindmap.swing.services.ImageIconService;
  uses com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
}
