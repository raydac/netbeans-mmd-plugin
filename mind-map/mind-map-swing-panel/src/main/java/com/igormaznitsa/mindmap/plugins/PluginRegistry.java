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

import static com.igormaznitsa.meta.common.utils.Assertions.assertNotNull;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class PluginRegistry implements Iterable<AbstractPlugin> {

  private final List<AbstractPlugin> pluginList = new CopyOnWriteArrayList<AbstractPlugin>();

  private static final PluginRegistry INSTANCE = new PluginRegistry();

  private PluginRegistry() {
  }

  public void registerPlugin(@Nonnull final AbstractPlugin plugin) {
    this.pluginList.add(assertNotNull(plugin));
    Collections.sort(this.pluginList);
  }

  public void unregisterPlugin(@Nonnull final AbstractPlugin plugin) {
    if (this.pluginList.remove(assertNotNull(plugin))) {
      Collections.sort(this.pluginList);
    }
  }

  public int size(){
    return this.pluginList.size();
  }
  
  @Override
  @Nonnull
  public Iterator<AbstractPlugin> iterator() {
    return this.pluginList.iterator();
  }

  @Nonnull
  public static PluginRegistry getInstance() {
    return INSTANCE;
  }
}
