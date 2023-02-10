package com.igormaznitsa.ideamindmap.swing;

import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.igormaznitsa.mindmap.ide.commons.editors.AbstractFileEditPanel;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.intellij.openapi.vfs.LocalFileSystem;
import java.io.File;
import javax.swing.Icon;

public class FileEditPanel extends AbstractFileEditPanel {

    public FileEditPanel(
            final UIComponentFactory uiComponentFactory, 
            final DialogProvider dialogProvider, 
            final File projectFolder, 
            final DataContainer initialData
    ) {
        super(uiComponentFactory, dialogProvider, projectFolder, initialData);
    }

    @Override
    protected Icon findIcon(final IconId id) {
        switch (id) {
            case BUTTON_CHOOSE: return AllIcons.Buttons.FILE_MANAGER;
            case LABEL_BROWSE: return AllIcons.Buttons.FILE_LINK_BIG;
            case BUTTON_RESET: return AllIcons.Buttons.CROSS;
            default: return null;
        }
    }

    @Override
    protected void openFileInSystemViewer(final File file) {
        IdeaUtils.openInSystemViewer(this.dialogProvider, LocalFileSystem.getInstance().findFileByIoFile(file));
    }
}
