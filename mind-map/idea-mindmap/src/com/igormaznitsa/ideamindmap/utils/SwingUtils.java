package com.igormaznitsa.ideamindmap.utils;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public enum SwingUtils {
    ;

    public static void safeSwing(@NotNull  final Runnable runnable){
        if (SwingUtilities.isEventDispatchThread()){
            runnable.run();
        }else{
            SwingUtilities.invokeLater(runnable);
        }
    }
}
