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
package com.igormaznitsa.nbmindmap.gui;

import com.igormaznitsa.nbmindmap.gui.mmview.AbstractElement;
import com.igormaznitsa.nbmindmap.model.MindMap;
import com.igormaznitsa.nbmindmap.model.Topic;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class MouseSelectedArea {
  private final Point startPoint;
  private final Point currentPoint;
  
  public MouseSelectedArea(final Point point){
    this.startPoint = new Point(point);
    this.currentPoint = new Point(point);
  }
  
  public void update(final MouseEvent e){
    this.currentPoint.setLocation(e.getPoint());
  }
  
  public Rectangle asRectangle(){
    final int minX = Math.min(this.startPoint.x, this.currentPoint.x);
    final int minY = Math.min(this.startPoint.y, this.currentPoint.y);
    final int maxX = Math.max(this.startPoint.x, this.currentPoint.x);
    final int maxY = Math.max(this.startPoint.y, this.currentPoint.y);
    return new Rectangle(minX, minY, maxX - minX, maxY - minY);
  }
  
  public List<Topic> getAllSelectedElements(final MindMap map){
    final List<Topic> result = new ArrayList<Topic>();
    final Rectangle rect = asRectangle();
    addCoveredToList(result, map.getRoot(), rect.getBounds2D());
    return result;
  }
  
  private void addCoveredToList(final List<Topic> list, final Topic root, final Rectangle2D rect){
    if (root == null || root.getPayload() == null) return;
    if (rect.contains(((AbstractElement)root.getPayload()).getBounds())){
      list.add(root);
    }
    for(final Topic t : root.getChildren()){
      addCoveredToList(list, t, rect);
    }
  }
}
