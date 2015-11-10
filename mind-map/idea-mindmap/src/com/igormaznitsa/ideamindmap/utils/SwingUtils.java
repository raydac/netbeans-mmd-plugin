package com.igormaznitsa.ideamindmap.utils;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public enum SwingUtils {
  ;

  public static void assertSwingThread() {
    if (!SwingUtilities.isEventDispatchThread())
      throw new Error("Must be Swing event dispatching thread, but detected '" + Thread.currentThread().getName() + '\'');
  }

  public static void safeSwing(@NotNull final Runnable runnable) {
    if (SwingUtilities.isEventDispatchThread()) {
      runnable.run();
    }
    else {
      SwingUtilities.invokeLater(runnable);
    }
  }
}
