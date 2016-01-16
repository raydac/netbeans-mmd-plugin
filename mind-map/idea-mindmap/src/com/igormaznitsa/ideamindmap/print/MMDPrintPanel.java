package com.igormaznitsa.ideamindmap.print;

import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.print.MMDPrint;
import com.igormaznitsa.mindmap.print.PrintPage;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.JButton;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.List;

public class MMDPrintPanel extends JBPanel {

  private static final long serialVersionUID = -2588424836865316862L;

  private static final Logger LOGGER = LoggerFactory.getLogger(MMDPrintPanel.class);

  private PageFormat pageFormat;
  private final Pages previewContainer;
  private double pageZoomFactor;
  private final DialogProvider dialogProvider;

  private final MindMapPanel mmdPanel;
  private PrintPage[][] pages;

  private final double screenDPIScale;

  public MMDPrintPanel(final MindMapPanel mindMapPanel, final DialogProvider dialogProvider) {
    super(new BorderLayout());

    int screenResolution;
    try{
      screenResolution = Toolkit.getDefaultToolkit().getScreenResolution();
    }catch(HeadlessException ex){
      screenResolution = 72;
    }
    this.screenDPIScale = screenResolution / 72.0d;

    this.mmdPanel = mindMapPanel;
    this.dialogProvider = dialogProvider;

    super.setPreferredSize(new Dimension(600,450));

    final JBScrollPane scrollPane = new JBScrollPane();
    final PrinterJob printerJob = PrinterJob.getPrinterJob();

    final JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
    toolBar.setFloatable(false);
    final JButton buttonPrint = new JButton("Print pages", AllIcons.PopUp.PRINTER);
    buttonPrint.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
      }
    });
    toolBar.add(buttonPrint);

    final JButton buttonPrintOptions = new JButton("Page setup",AllIcons.PopUp.PAGE);
    buttonPrintOptions.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        pageFormat = printerJob.pageDialog(pageFormat);
        splitToPagesForFormat();
        scrollPane.revalidate();
        scrollPane.repaint();
      }
    });
    toolBar.add(buttonPrintOptions);

    java.awt.Toolkit.getDefaultToolkit().getScreenResolution();

    final List<String> scalesList = new ArrayList<String>();
    scalesList.add("10 %");
    for(int i=25; i < 225; i+=25){
      scalesList.add(Integer.toString(i)+" %");
    }

    final String[] scales = scalesList.toArray(new String[scalesList.size()]);
    this.pageZoomFactor = 0.1d;
    final ComboBox comboBoxScale = new ComboBox(scales);
    comboBoxScale.setEditable(false);

    comboBoxScale.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final int percent = Integer.parseInt(comboBoxScale.getSelectedItem().toString().split("\\s")[0]);
        pageZoomFactor = (double) percent / 100d;
        splitToPagesForFormat();
        scrollPane.revalidate();
        scrollPane.repaint();
      }
    });
    comboBoxScale.setMaximumSize(comboBoxScale.getPreferredSize());
    toolBar.addSeparator();
    toolBar.add(comboBoxScale);


    this.add(toolBar, BorderLayout.NORTH);

    this.pageFormat = printerJob.defaultPage();
    this.splitToPagesForFormat();
    this.previewContainer = new Pages(this);
    scrollPane.getViewport().setView(this.previewContainer);
    this.add(scrollPane, BorderLayout.CENTER);
  }

  PageFormat getPageFormat() {
    return this.pageFormat;
  }

  PrintPage[][] getPages() {
    return this.pages;
  }

  double getScale() {
    return this.pageZoomFactor;
  }

  double dpiToScreen(final double points){
    return points*this.screenDPIScale;
  }

  private void splitToPagesForFormat() {
    final MMDPrint printer = new MMDPrint(this.mmdPanel, (int)Math.round(dpiToScreen(this.pageFormat.getImageableWidth())),
      (int)Math.round(dpiToScreen(this.pageFormat.getImageableHeight())), this.pageZoomFactor);
    this.pages = printer.getPages();
  }

}
