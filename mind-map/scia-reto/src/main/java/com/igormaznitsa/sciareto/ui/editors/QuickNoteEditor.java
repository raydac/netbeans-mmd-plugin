/*
 * Copyright (C) 2015-2026 Igor A. Maznitsa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.sciareto.ui.editors;

import static com.igormaznitsa.sciareto.ui.UiUtils.assertSwingThread;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.i18n.MmdI18n;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import java.awt.BorderLayout;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.fife.ui.rtextarea.RTextScrollPane;

public final class QuickNoteEditor extends JPanel {

  private static final Logger LOGGER = LoggerFactory.getLogger(QuickNoteEditor.class);

  private final ResourceBundle resourceBundle = MmdI18n.getInstance().findBundle();

  private final ScalableRsyntaxTextArea textArea;
  private final RTextScrollPane scrollPane;
  private final MindMapPanel panel;
  private final AtomicBoolean released = new AtomicBoolean();
  private Topic activeTopic;
  private String originalTopicText;
  private boolean ignoreDocumentChange;

  public QuickNoteEditor(@Nonnull final MindMapPanel mindMapPanel,
                         @Nonnull @MustNotContainNull final Topic[] topics) {
    super(new BorderLayout());
    this.panel = mindMapPanel;
    this.setBorder(BorderFactory.createTitledBorder(
        resourceBundle.getString("MMDGraphEditor.makePopUp.miQuickNote")));
    this.textArea = new ScalableRsyntaxTextArea(mindMapPanel.getConfiguration());
    this.textArea.setWrapStyleWord(true);
    this.textArea.setLineWrap(true);
    this.scrollPane = new RTextScrollPane(this.textArea);
    this.add(this.scrollPane, BorderLayout.CENTER);
    this.setTopic(topics);

    this.textArea.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        onDocumentChange(e);
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        onDocumentChange(e);
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        onDocumentChange(e);
      }
    });
  }

  private void onDocumentChange(@Nonnull final DocumentEvent event) {
    if (!this.ignoreDocumentChange) {
      if (!this.released.get() && this.activeTopic != null && this.textArea.isEnabled()) {
        try {
          final String documentText =
              event.getDocument().getText(0, event.getDocument().getLength());
          if (!this.originalTopicText.equals(documentText)) {
            final ExtraNote note =
                (ExtraNote) this.activeTopic.getExtras().get(Extra.ExtraType.NOTE);
            if (note == null || !note.isEncrypted()) {
              if (documentText.isEmpty()) {
                this.activeTopic.removeExtra(Extra.ExtraType.NOTE);
                if (note != null) {
                  this.panel.doLayout();
                }
              } else {
                this.activeTopic.setExtra(new ExtraNote(documentText));
                if (note == null) {
                  this.panel.doLayout();
                }
              }
              this.originalTopicText = documentText;
            }
          }
        } catch (Exception ex) {
          LOGGER.error("Unexpected error in onDocumentChange", ex);
        }
      }
    }
  }

  private void assertNotReleased() {
    if (this.released.get()) {
      throw new IllegalStateException("Released editor");
    }
  }

  public void release() {
    assertSwingThread();
    if (this.released.compareAndSet(false, true)) {
      this.textArea.setText("");
      this.activeTopic = null;
      this.originalTopicText = null;
    }
  }

  private void setText(@Nullable final String text) {
    if (text == null) {
      this.originalTopicText = null;
      this.textArea.setText("");
      this.textArea.setEnabled(false);
    } else {
      this.originalTopicText = text;
      this.textArea.setText(text);
      SwingUtilities.invokeLater(() -> this.scrollPane.getVerticalScrollBar().setValue(0));
    }
  }

  public void updateActiveTopic(@Nullable final Topic topic) {
    assertSwingThread();
    this.assertNotReleased();

    this.ignoreDocumentChange = true;
    try {
      if (topic == null) {
        this.textArea.setText("");
        this.textArea.setEnabled(false);
      } else {
        this.activeTopic = topic;
        final ExtraNote note = (ExtraNote) this.activeTopic.getExtras().get(Extra.ExtraType.NOTE);
        if (note == null) {
          this.setText("");
          this.textArea.setEnabled(true);
        } else {
          if (note.isEncrypted()) {
            this.setText("*** *****");
            this.textArea.setEnabled(false);
          } else {
            this.setText(note.getAsString());
            this.textArea.setEnabled(true);
          }
        }
      }
    } finally {
      this.ignoreDocumentChange = false;
    }

  }

  public void setTopic(@Nullable @MustNotContainNull final Topic[] topics) {
    assertSwingThread();
    this.assertNotReleased();

    this.updateActiveTopic(topics == null || topics.length != 1 ? null : topics[0]);
  }
}
