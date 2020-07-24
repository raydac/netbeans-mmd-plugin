package com.igormaznitsa.sciareto.ui.misc;

import java.awt.Component;
import java.awt.Dimension;
import javax.annotation.Nonnull;
import javax.swing.JSplitPane;

public class SplitPaneExt extends JSplitPane {

    public SplitPaneExt(final int newOrientation) {
        super(newOrientation);
    }

    @Override
    @Nonnull
    public Component add(@Nonnull final Component component) {
        component.setMinimumSize(new Dimension(0, 0));
        return super.add(component);
    }
    
    @Override
    public void add(@Nonnull final Component component, @Nonnull final Object constraints){
        component.setMinimumSize(new Dimension(0,0));
        super.add(component, constraints);
    }

}
