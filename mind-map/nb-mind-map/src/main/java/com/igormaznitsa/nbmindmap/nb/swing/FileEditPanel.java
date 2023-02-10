package com.igormaznitsa.nbmindmap.nb.swing;

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.ide.commons.editors.AbstractFileEditPanel;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.igormaznitsa.nbmindmap.utils.NbUtils;
import java.io.File;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class FileEditPanel extends AbstractFileEditPanel {
  public FileEditPanel(
      @Nonnull final UIComponentFactory uiComponentFactory,
      @Nonnull final DialogProvider dialogProvider,
      @Nullable final File projectFolder,
      @Nonnull final DataContainer initialData
  ) {
    super(uiComponentFactory, dialogProvider, projectFolder, initialData);
  }

  private Icon loadIcon(@Nonnull final String name) {
    return new ImageIcon(
        requireNonNull(this.getClass().getResource("/com/igormaznitsa/nbmindmap/icons/" + name)));
  }

  @Override
  @Nullable
  protected Icon findIcon(@Nonnull final IconId id) {
    switch (id) {
      case BUTTON_RESET:
        return this.loadIcon("cross16.png");
      case LABEL_BROWSE:
        return this.loadIcon("file_link.png");
      case BUTTON_CHOOSE:
        return this.loadIcon("file_manager.png");
      default:
        return null;
    }
  }

  @Override
  protected void openFileInSystemViewer(File file) {
    NbUtils.openInSystemViewer(this.getPanel(), file);
  }
}
