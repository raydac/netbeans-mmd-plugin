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

package com.igormaznitsa.mindmap.plugins;

import static com.igormaznitsa.meta.common.utils.Assertions.assertNotNull;


import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.TopicFinder;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.api.AbstractExporter;
import com.igormaznitsa.mindmap.plugins.api.AbstractImporter;
import com.igormaznitsa.mindmap.plugins.api.MindMapPlugin;
import com.igormaznitsa.mindmap.plugins.attributes.emoticon.EmoticonPopUpMenuPlugin;
import com.igormaznitsa.mindmap.plugins.attributes.emoticon.EmoticonVisualAttributePlugin;
import com.igormaznitsa.mindmap.plugins.attributes.images.ImagePopUpMenuPlugin;
import com.igormaznitsa.mindmap.plugins.attributes.images.ImageVisualAttributePlugin;
import com.igormaznitsa.mindmap.plugins.exporters.ASCIIDocExporter;
import com.igormaznitsa.mindmap.plugins.exporters.FreeMindExporter;
import com.igormaznitsa.mindmap.plugins.exporters.MDExporter;
import com.igormaznitsa.mindmap.plugins.exporters.MindmupExporter;
import com.igormaznitsa.mindmap.plugins.exporters.ORGMODEExporter;
import com.igormaznitsa.mindmap.plugins.exporters.PNGImageExporter;
import com.igormaznitsa.mindmap.plugins.exporters.SVGImageExporter;
import com.igormaznitsa.mindmap.plugins.exporters.TextExporter;
import com.igormaznitsa.mindmap.plugins.importers.CoggleMM2MindMapImporter;
import com.igormaznitsa.mindmap.plugins.importers.Freemind2MindMapImporter;
import com.igormaznitsa.mindmap.plugins.importers.Mindmup2MindMapImporter;
import com.igormaznitsa.mindmap.plugins.importers.Novamind2MindMapImporter;
import com.igormaznitsa.mindmap.plugins.importers.Text2MindMapImporter;
import com.igormaznitsa.mindmap.plugins.importers.XMind2MindMapImporter;
import com.igormaznitsa.mindmap.plugins.misc.AboutPlugin;
import com.igormaznitsa.mindmap.plugins.misc.OptionsPlugin;
import com.igormaznitsa.mindmap.plugins.processors.AddChildPlugin;
import com.igormaznitsa.mindmap.plugins.processors.CloneTopicPlugin;
import com.igormaznitsa.mindmap.plugins.processors.EditTextPlugin;
import com.igormaznitsa.mindmap.plugins.processors.ExtraFilePlugin;
import com.igormaznitsa.mindmap.plugins.processors.ExtraJumpPlugin;
import com.igormaznitsa.mindmap.plugins.processors.ExtraNotePlugin;
import com.igormaznitsa.mindmap.plugins.processors.ExtraURIPlugin;
import com.igormaznitsa.mindmap.plugins.processors.RemoveTopicPlugin;
import com.igormaznitsa.mindmap.plugins.processors.TextAlignMenuPlugin;
import com.igormaznitsa.mindmap.plugins.tools.ChangeColorPlugin;
import com.igormaznitsa.mindmap.plugins.tools.CollapseAllPlugin;
import com.igormaznitsa.mindmap.plugins.tools.ShowJumpsPlugin;
import com.igormaznitsa.mindmap.plugins.tools.UnfoldAllPlugin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class MindMapPluginRegistry implements Iterable<MindMapPlugin> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MindMapPluginRegistry.class);
  private static final MindMapPluginRegistry INSTANCE = new MindMapPluginRegistry();
  private final List<MindMapPlugin> pluginList = new ArrayList<MindMapPlugin>();
  private final Map<Class<? extends MindMapPlugin>, List<? extends MindMapPlugin>> FIND_CACHE =
      new HashMap<Class<? extends MindMapPlugin>, List<? extends MindMapPlugin>>();

  private MindMapPluginRegistry() {
    registerPlugin(new FreeMindExporter());
    registerPlugin(new MDExporter());
    registerPlugin(new ASCIIDocExporter());
    registerPlugin(new MindmupExporter());
    registerPlugin(new PNGImageExporter());
    registerPlugin(new ORGMODEExporter());
    registerPlugin(new TextExporter());
    registerPlugin(new SVGImageExporter());

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
    registerPlugin(new TextAlignMenuPlugin());

    registerPlugin(new Text2MindMapImporter());
    registerPlugin(new Mindmup2MindMapImporter());
    registerPlugin(new Freemind2MindMapImporter());
    registerPlugin(new XMind2MindMapImporter());
    registerPlugin(new CoggleMM2MindMapImporter());
    registerPlugin(new Novamind2MindMapImporter());

    registerPlugin(new EmoticonPopUpMenuPlugin());
    registerPlugin(new EmoticonVisualAttributePlugin());

    registerPlugin(new ImagePopUpMenuPlugin());
    registerPlugin(new ImageVisualAttributePlugin());
  }

  @Nonnull
  public Set<TopicFinder> findAllTopicFinders() {
    final Set<TopicFinder> result = new HashSet<>();
    for (final MindMapPlugin p : this.pluginList) {
      if (p instanceof TopicFinder) {
        result.add((TopicFinder) p);
      }
    }

    return result;
  }

  @Nonnull
  public static MindMapPluginRegistry getInstance() {
    return INSTANCE;
  }

  public void registerPlugin(@Nonnull final MindMapPlugin plugin) {
    synchronized (FIND_CACHE) {
      this.pluginList.add(assertNotNull(plugin));
      LOGGER.info("Registered plugin " + plugin.getClass().getName());
      Collections.sort(this.pluginList);
      FIND_CACHE.clear();
    }
  }

  public void unregisterPluginForClass(@Nonnull final Class<? extends MindMapPlugin> pluginClass) {
    synchronized (FIND_CACHE) {
      final Iterator<MindMapPlugin> iterator = this.pluginList.iterator();
      while (iterator.hasNext()) {
        final MindMapPlugin plugin = iterator.next();
        if (pluginClass.isAssignableFrom(plugin.getClass())) {
          LOGGER.info("Unregistered plugin " + plugin.getClass().getName() + " for class " +
              pluginClass.getName());
          iterator.remove();
        }
      }
    }
  }

  public void unregisterPlugin(@Nonnull final MindMapPlugin plugin) {
    synchronized (FIND_CACHE) {
      if (this.pluginList.remove(assertNotNull(plugin))) {
        LOGGER.info("Unregistered plugin " + plugin.getClass().getName());
        Collections.sort(this.pluginList);
      }
      FIND_CACHE.clear();
    }
  }

  public int size() {
    synchronized (FIND_CACHE) {
      return this.pluginList.size();
    }
  }

  public void clear() {
    synchronized (FIND_CACHE) {
      this.pluginList.clear();
      FIND_CACHE.clear();
    }
  }

  @Nullable
  public AbstractExporter findExporterForMnemonic(@Nonnull final String mnemonic) {
    AbstractExporter result = null;
    synchronized (FIND_CACHE) {
      for (final MindMapPlugin p : this.pluginList) {
        if (p instanceof AbstractExporter) {
          final AbstractExporter exporter = (AbstractExporter) p;
          if (mnemonic.equals(exporter.getMnemonic())) {
            result = exporter;
            break;
          }
        }
      }
    }
    return result;
  }

  @Nullable
  public AbstractImporter findImporterForMnemonic(@Nonnull final String mnemonic) {
    AbstractImporter result = null;
    synchronized (FIND_CACHE) {
      for (final MindMapPlugin p : this.pluginList) {
        if (p instanceof AbstractImporter) {
          final AbstractImporter importer = (AbstractImporter) p;
          if (mnemonic.equals(importer.getMnemonic())) {
            result = importer;
            break;
          }
        }
      }
    }
    return result;
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
}
