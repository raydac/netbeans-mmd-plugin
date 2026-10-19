module com.igormaznitsa.mmd.model {
  requires java.logging;
  requires org.apache.commons.io;
  requires org.apache.commons.text;

  exports com.igormaznitsa.mindmap.model;
  exports com.igormaznitsa.mindmap.model.logger;
  exports com.igormaznitsa.mindmap.model.parser;
  exports com.igormaznitsa.mindmap.version;

  uses com.igormaznitsa.mindmap.model.logger.LoggerService;
}
