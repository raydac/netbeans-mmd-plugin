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

import com.igormaznitsa.mindmap.plugins.api.MindMapPlugin;
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
import com.igormaznitsa.mindmap.plugins.processors.ExtraFilePlugin;
import com.igormaznitsa.mindmap.plugins.processors.ExtraJumpPlugin;
import com.igormaznitsa.mindmap.plugins.processors.ExtraNotePlugin;
import com.igormaznitsa.mindmap.plugins.processors.ExtraURIPlugin;
import java.util.HashMap;
import java.util.Map;
import com.igormaznitsa.mindmap.plugins.processors.AddChildPlugin;
import com.igormaznitsa.mindmap.plugins.processors.CloneTopicPlugin;
import com.igormaznitsa.mindmap.plugins.processors.EditTextPlugin;
import com.igormaznitsa.mindmap.plugins.processors.RemoveTopicPlugin;
import com.igormaznitsa.mindmap.plugins.importers.Text2MindMapImporter;
import com.igormaznitsa.mindmap.plugins.misc.AboutPlugin;
import com.igormaznitsa.mindmap.plugins.misc.OptionsPlugin;
import com.igormaznitsa.mindmap.plugins.tools.ChangeColorPlugin;
import com.igormaznitsa.mindmap.plugins.tools.CollapseAllPlugin;
import com.igormaznitsa.mindmap.plugins.tools.ShowJumpsPlugin;
import com.igormaznitsa.mindmap.plugins.tools.UnfoldAllPlugin;
import static com.igormaznitsa.meta.common.utils.Assertions.assertNotNull;
import com.igormaznitsa.mindmap.plugins.attributes.emoticon.EmoticonPopUpMenuPlugin;
import com.igormaznitsa.mindmap.plugins.attributes.emoticon.EmoticonVisualAttributePlugin;
import static com.igormaznitsa.meta.common.utils.Assertions.assertNotNull;
import static com.igormaznitsa.meta.common.utils.Assertions.assertNotNull;
import static com.igormaznitsa.meta.common.utils.Assertions.assertNotNull;

@ThreadSafe
public final class MindMapPluginRegistry implements Iterable<MindMapPlugin> {

  private final List<MindMapPlugin> pluginList = new CopyOnWriteArrayList<MindMapPlugin>();

  private static final MindMapPluginRegistry INSTANCE = new MindMapPluginRegistry();

  private final Map<Class<? extends MindMapPlugin>, List<? extends MindMapPlugin>> FIND_CACHE = new HashMap<Class<? extends MindMapPlugin>, List<? extends MindMapPlugin>>();

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
    
    registerPlugin(new Text2MindMapImporter());
    
    registerPlugin(new EmoticonPopUpMenuPlugin());
    registerPlugin(new EmoticonVisualAttributePlugin());
  }

  public void registerPlugin(@Nonnull final MindMapPlugin plugin) {
    synchronized (FIND_CACHE) {
      this.pluginList.add(assertNotNull(plugin));
      Collections.sort(this.pluginList);
      FIND_CACHE.clear();
    }
  }

  public void unregisterPlugin(@Nonnull final MindMapPlugin plugin) {
    synchronized (FIND_CACHE) {
      if (this.pluginList.remove(assertNotNull(plugin))) {
        Collections.sort(this.pluginList);
      }
      FIND_CACHE.clear();
    }
  }

  public int size() {
    return this.pluginList.size();
  }

  public void clear() {
    this.pluginList.clear();
  }

  @Nonnull
  @MustNotContainNull
  public <T extends MindMapPlugin> List<T> findFor(@Nullable final Class<T> klazz) {
    synchronized (FIND_CACHE) {
      List<T> result = (List<T>) FIND_CACHE.get(klazz);

      if (result == null) {
        result = new ArrayList<T>();
        if (klazz != null) {
          for (final MindMapPlugin p : this.pluginList) {
            if (klazz.isInstance(p)) {
              result.add(klazz.cast(p));
            }
          }
        }
        result = Collections.unmodifiableList(result);
        FIND_CACHE.put(klazz, result);
      }
      return result;
    }
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
