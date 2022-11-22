package com.igormaznitsa.mindmap.swing.i18n;

import com.igormaznitsa.mindmap.swing.ide.IDEBridgeFactory;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Provider way to get all string constants for mind map panel project.
 * @since 1.6.0
 */
public final class MmdI18n {
  private static final String RESOURCE_PATH = "com/igormaznitsa/mindmap/swing/panel/Bundle";
  private static final MmdI18n INSTANCE = new MmdI18n();

  private MmdI18n() {

  }

  public static MmdI18n getInstance() {
    return INSTANCE;
  }

  public ResourceBundle findBundle() {
    return this.findBundle(IDEBridgeFactory.findInstance().getIDELocale());
  }

  public ResourceBundle findBundle(final Locale locale) {
    return ResourceBundle.getBundle(RESOURCE_PATH, locale);
  }
}
