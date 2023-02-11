/*
 * Copyright (C) 2015-2023 Igor A. Maznitsa
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

package com.igormaznitsa.mindmap.ide.commons.editors;

import com.igormaznitsa.mindmap.ide.commons.AbstractUiStarter;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import javax.swing.JPanel;

public class AbstractMindMapTreePanelTest extends AbstractUiStarter {

  @Override
  public JPanel makePanel(UIComponentFactory componentFactory,
                          DialogProvider dialogProvider) {

    final MindMap mindMap = new MindMap(true);
    new Topic(mindMap, mindMap.getRoot(), "Topic1");
    new Topic(mindMap, mindMap.getRoot(), "Topic2");
    new Topic(mindMap, mindMap.getRoot(), "Topic3");

    final AbstractMindMapTreePanel panel = new AbstractMindMapTreePanel(
        componentFactory,
        new MindMap(true), null, true, e -> {}){
    };
    return panel.getPanel();
  }

  public static void main(String... args) {
    AbstractUiStarter.main(AbstractMindMapTreePanelTest.class.getName());
  }

}