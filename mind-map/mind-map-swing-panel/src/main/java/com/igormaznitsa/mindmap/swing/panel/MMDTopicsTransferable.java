/*
 * Copyright 2015-2018 Igor Maznitsa.
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
package com.igormaznitsa.mindmap.swing.panel;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Map;
import javax.annotation.Nonnull;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;

/**
 * Transferable object to represent topic list in clipboard.
 * 
 * @since 1.3.1
 */
public class MMDTopicsTransferable implements Transferable {

  public static final DataFlavor MMD_DATA_FLAVOR;
  
  static {
    try{
    MMD_DATA_FLAVOR = new DataFlavor(DataFlavor.javaSerializedObjectMimeType + ";class=\"" + NBMindMapTopicsContainer.class.getName() + "\"", "nb-mindmap-topic-list",NBMindMapTopicsContainer.class.getClassLoader());
    }catch(ClassNotFoundException ex){
      throw new Error("Can't find class",ex);
    }
  }
  
  private static final DataFlavor[] FLAVORS = new DataFlavor[]{DataFlavor.stringFlavor, MMD_DATA_FLAVOR};
  private final Topic [] topics;
 
  private static final String END_OF_LINE = System.getProperty("line.separator","\n");
  
  public MMDTopicsTransferable(@Nonnull @MustNotContainNull final Topic ... topics) {
    this.topics = new Topic[topics.length];
    
    final MindMap fakeMap = new MindMap(null, false);
    
    for(int i=0;i<topics.length;i++){
      this.topics[i] = new Topic(fakeMap,topics[i],true);
    }
  }
  
  @Nonnull
  private static String convertTopicToText(@Nonnull final Topic topic) {
    final StringBuilder result = new StringBuilder();
    
    result.append(topic.getText());

    boolean addedExtras = false;
    
    result.append(END_OF_LINE).append("--------------------");
    if (!topic.getExtras().isEmpty()){
      addedExtras = true;
      for(final Map.Entry<Extra.ExtraType,Extra<?>> e : topic.getExtras().entrySet()) {
        result.append(END_OF_LINE).append(e.getKey().name()).append('=').append(e.getValue().getAsString());
      }
    }

    if (!topic.getAttributes().isEmpty()) {
      if (addedExtras) {
        result.append(END_OF_LINE);
      }
      for(final Map.Entry<String,String> e : topic.getAttributes().entrySet()) {
        result.append(END_OF_LINE).append(e.getKey()).append('=').append(e.getValue());
      }
    }
    result.append(END_OF_LINE).append("--------------------");
  
    for(final Topic c : topic.getChildren()){
      result.append(END_OF_LINE).append(END_OF_LINE).append(convertTopicToText(c));
    }
    
    return result.toString();
  }
  
  @Override
  @Nonnull
  @MustNotContainNull
  public DataFlavor[] getTransferDataFlavors() {
    return FLAVORS;
  }

  @Override
  public boolean isDataFlavorSupported(@Nonnull final DataFlavor flavor) {
    return flavor.isFlavorTextType() || flavor.isMimeTypeEqual(MMD_DATA_FLAVOR);
  }

  @Override
  @Nonnull
  public Object getTransferData(@Nonnull final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    if (flavor.isFlavorTextType()) {
      final StringBuilder result = new StringBuilder();
      
      for(final Topic t : this.topics){
        if (result.length()>0){
          result.append(END_OF_LINE).append(END_OF_LINE);
        }
        result.append(convertTopicToText(t));
      }
      
      return result.toString();
    } else if (flavor.isMimeTypeEqual(MMD_DATA_FLAVOR)) {
      return new NBMindMapTopicsContainer(this.topics);
    } else {
      throw new UnsupportedFlavorException(flavor);
    }
  }

}
