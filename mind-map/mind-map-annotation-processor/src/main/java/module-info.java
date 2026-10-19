module com.igormaznitsa.mmd.processor {
  requires com.igormaznitsa.mmd.annotations;
  requires com.igormaznitsa.mmd.model;
  requires com.igormaznitsa.mmd.swing.panel;
  requires java.compiler;
  requires java.desktop;
  requires jdk.compiler;
  requires org.apache.commons.io;
  requires org.apache.commons.lang3;
  requires org.apache.commons.text;

  provides com.igormaznitsa.mindmap.model.logger.LoggerService
      with com.igormaznitsa.mindmap.annotations.processor.logger.NopLoggerService;
  provides com.igormaznitsa.mindmap.swing.ide.IDEBridge
      with com.igormaznitsa.mindmap.annotations.processor.ProcessorBridge;
  provides javax.annotation.processing.Processor
      with com.igormaznitsa.mindmap.annotations.processor.MmdAnnotationProcessor;
}
