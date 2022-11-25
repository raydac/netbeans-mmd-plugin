/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

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
