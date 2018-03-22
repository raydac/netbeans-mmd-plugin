package com.igormaznitsa.sciareto.ui.editors;

import javax.annotation.Nullable;
import javax.swing.*;

public class EditorScrollPanel extends JScrollPane {

  public EditorScrollPanel() {
    this(null);
  }

  public EditorScrollPanel(@Nullable final JComponent view) {
    super(view);
    this.setAutoscrolls(true);
  }
}
