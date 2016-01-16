package com.igormaznitsa.ideamindmap.print;

import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.igormaznitsa.ideamindmap.utils.SwingUtils;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.print.MMDPrint;
import com.igormaznitsa.mindmap.print.PrintPage;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MMDPrintPanel extends JBPanel {

  private static final long serialVersionUID = -2588424836865316862L;

  private static final Logger LOGGER = LoggerFactory.getLogger(MMDPrintPanel.class);

  private PageFormat pageFormat;
  private final Pages previewContainer;
  private double pageZoomFactor;

  private final MindMapPanel mmdPanel;
  private PrintPage[][] pages;

  public MMDPrintPanel(final Project project, final MindMapPanel mindMapPanel) {
    super(new BorderLayout());

    this.mmdPanel = mindMapPanel;

    super.setPreferredSize(new Dimension(600,450));

    final JBScrollPane scrollPane = new JBScrollPane();
    final PrinterJob printerJob = PrinterJob.getPrinterJob();
    printerJob.setJobName("MMD file");

    final JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
    toolBar.setFloatable(false);
    final JButton buttonPrint = new JButton("Print pages", AllIcons.PopUp.PRINTER);

    final JBPanel theInstance = this;

    buttonPrint.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final AtomicReference<PrintPage[][]> pages = new AtomicReference<PrintPage[][]>();

        printerJob.setPrintable(new Printable() {
          private List<PrintPage> listOfPages = null;

          @Override public int print(final Graphics graphics, final PageFormat format, final int pageIndex) throws PrinterException {
            if (this.listOfPages == null){
              this.listOfPages = null;
              pages.set(new MMDPrint(mmdPanel,(int)format.getImageableWidth(),(int)format.getImageableHeight(),1.0d).getPages());
            }

            if (this.listOfPages == null){
              this.listOfPages = new ArrayList<PrintPage>();
              for(final PrintPage [] row : pages.get()){
                for(final PrintPage p : row) listOfPages.add(p);
              }
            }

            if (pageIndex<0 || pageIndex>=this.listOfPages.size()){
              return Printable.NO_SUCH_PAGE;
            }else{
              final PrintPage page = this.listOfPages.get(pageIndex);
              graphics.translate((int)format.getImageableX(),(int)format.getImageableY());
              page.print(graphics);
              graphics.translate(-(int)format.getImageableX(),-(int)format.getImageableY());
              return Printable.PAGE_EXISTS;
            }
          }
        },pageFormat);

        if (printerJob.printDialog()){
          final Task.Backgroundable task = new Task.Backgroundable(project, "Print mind-map job") {
            @Override public void run(@NotNull final ProgressIndicator indicator) {
              try{
                indicator.setIndeterminate(true);
                printerJob.print();
              }catch(PrinterException ex){
                LOGGER.error("Print error",ex);
              }finally{
                indicator.stop();
              }
            }
          };
          ProgressManager.getInstance().run(task);
              SwingUtilities.invokeLater(new Runnable() {
                @Override public void run() {
                  final Window wnd = SwingUtilities.windowForComponent(theInstance);
                  if (wnd!=null) wnd.dispose();
                }
              });
              return;
        }
    }
    });
    toolBar.add(buttonPrint);

    final JButton buttonPrintOptions = new JButton("Page setup",AllIcons.PopUp.PAGE);
    buttonPrintOptions.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
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

  private void splitToPagesForFormat() {
    final MMDPrint printer = new MMDPrint(this.mmdPanel, (int)this.pageFormat.getImageableWidth(),
      (int)this.pageFormat.getImageableHeight(), 1.0d);
    this.pages = printer.getPages();
  }

}
