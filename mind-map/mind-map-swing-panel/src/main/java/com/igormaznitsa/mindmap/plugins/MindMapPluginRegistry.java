/*
 * Copyright 2016 Igor Maznitsa.
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
package com.igormaznitsa.mindmap.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.plugins.exporters.FreeMindExporter;
import com.igormaznitsa.mindmap.plugins.exporters.MDExporter;
import com.igormaznitsa.mindmap.plugins.exporters.MindmupExporter;
import com.igormaznitsa.mindmap.plugins.exporters.PNGImageExporter;
import com.igormaznitsa.mindmap.plugins.exporters.TextExporter;
import com.igormaznitsa.mindmap.plugins.focused.ExtraFilePlugin;
import com.igormaznitsa.mindmap.plugins.focused.ExtraJumpPlugin;
import com.igormaznitsa.mindmap.plugins.focused.ExtraNotePlugin;
import com.igormaznitsa.mindmap.plugins.focused.ExtraURIPlugin;
import static com.igormaznitsa.meta.common.utils.Assertions.assertNotNull;
import com.igormaznitsa.mindmap.plugins.focused.AddChildPlugin;
import com.igormaznitsa.mindmap.plugins.focused.CloneTopicPlugin;
import com.igormaznitsa.mindmap.plugins.focused.EditTextPlugin;
import com.igormaznitsa.mindmap.plugins.focused.RemoveTopicPlugin;
import com.igormaznitsa.mindmap.plugins.misc.AboutPlugin;
import com.igormaznitsa.mindmap.plugins.misc.OptionsPlugin;
import com.igormaznitsa.mindmap.plugins.tools.ChangeColorPlugin;
import com.igormaznitsa.mindmap.plugins.tools.CollapseAllPlugin;
import com.igormaznitsa.mindmap.plugins.tools.ShowJumpsPlugin;
import com.igormaznitsa.mindmap.plugins.tools.UnfoldAllPlugin;

@ThreadSafe
public final class MindMapPluginRegistry implements Iterable<MindMapPlugin> {

  private final List<MindMapPlugin> pluginList = new CopyOnWriteArrayList<MindMapPlugin>();

  private static final MindMapPluginRegistry INSTANCE = new MindMapPluginRegistry();

  private MindMapPluginRegistry() {
    registerPlugin(new FreeMindExporter());
    registerPlugin(new MDExporter());
    registerPlugin(new MindmupExporter());
    registerPlugin(new PNGImageExporter());
    registerPlugin(new TextExporter());
    
    registerPlugin(new ExtraFilePlugin());
    registerPlugin(new ExtraNotePlugin());
    registerPlugin(new ExtraJumpPlugin());
    registerPlugin(new ExtraURIPlugin());
    
    registerPlugin(new EditTextPlugin());
    registerPlugin(new AddChildPlugin());
    registerPlugin(new CloneTopicPlugin());
    registerPlugin(new RemoveTopicPlugin());
    
    registerPlugin(new OptionsPlugin());
    registerPlugin(new AboutPlugin());
    
    registerPlugin(new ShowJumpsPlugin());
    registerPlugin(new CollapseAllPlugin());
    registerPlugin(new UnfoldAllPlugin());
    registerPlugin(new ChangeColorPlugin());
  }

  public void registerPlugin(@Nonnull final MindMapPlugin plugin) {
    this.pluginList.add(assertNotNull(plugin));
    Collections.sort(this.pluginList);
  }
  
  public void unregisterPlugin(@Nonnull final MindMapPlugin plugin) {
    if (this.pluginList.remove(assertNotNull(plugin))) {
      Collections.sort(this.pluginList);
    }
  }

  public int size(){
    return this.pluginList.size();
  }
  
  public void clear(){
    this.pluginList.clear();
  }
  
  @Nonnull
  @MustNotContainNull
  public <T> List<T> findFor(@Nullable final Class<T> klazz) {
    final List<T> result = new ArrayList<T>();
    
    if (klazz!=null){
      for(final MindMapPlugin p : this.pluginList) {
        if (klazz.isInstance(p)) {
          result.add(klazz.cast(p));
        }
      }
    }
    
    return result;
  }
  
  @Override
  @Nonnull
  public Iterator<MindMapPlugin> iterator() {
    return this.pluginList.iterator();
  }

  @Nonnull
  public static MindMapPluginRegistry getInstance() {
    return INSTANCE;
  }
}
