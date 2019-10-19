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

package com.igormaznitsa.mindmap.swing.services;

import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import java.util.Iterator;
import java.util.ServiceLoader;
import javax.annotation.Nonnull;

public final class UIComponentFactoryProvider {
  private static final UIComponentFactory UI_COMPONENT_FACTORY;

  static {
    final ServiceLoader<UIComponentFactory> service = ServiceLoader.load(UIComponentFactory.class, UIComponentFactoryProvider.class.getClassLoader());
    service.reload();
    final Iterator<UIComponentFactory> iterator = service.iterator();
    UI_COMPONENT_FACTORY = iterator.hasNext() ? iterator.next() : new DefaultSwingUIComponentService();
    LoggerFactory.getLogger(UIComponentFactoryProvider.class).info("UI Component factory : " + UI_COMPONENT_FACTORY.getClass().getName());
  }

  @Nonnull
  public static UIComponentFactory findInstance() {
    return UI_COMPONENT_FACTORY;
  }
}
