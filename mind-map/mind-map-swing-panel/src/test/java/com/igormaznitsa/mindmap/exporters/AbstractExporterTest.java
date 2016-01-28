/*
 * Copyright 2015 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.igormaznitsa.mindmap.exporters;

import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import java.io.ByteArrayOutputStream;
import javax.swing.JComponent;
import static org.mockito.Mockito.*;

public abstract class AbstractExporterTest <T extends AbstractMindMapExporter>{
  
  public byte [] export(final MindMap map, final MindMapPanelConfig nullableConfig) throws Exception {
    final T exporter = generateExporterInstance();
    
    final MindMapPanelConfig config = nullableConfig == null ? new MindMapPanelConfig() : nullableConfig;
    
    final MindMapPanel panel = mock(MindMapPanel.class);
    
    when(panel.getModel()).thenReturn(map);
    when(panel.getConfiguration()).thenReturn(config);
    
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    exporter.doExport(panel, prepareOptions(), buffer);
    return buffer.toByteArray();
  }
  
  public JComponent prepareOptions(){
    return null;
  }
  
  public abstract T generateExporterInstance();
  
}
