/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
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

package com.igormaznitsa.mindmap.swing.ide;

import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import java.util.Iterator;
import java.util.ServiceLoader;

public class IDEBridgeFactory {
  private static final IDEBridge IDE_INFO_PROVIDER;

  static {
    final ServiceLoader<IDEBridge> service = ServiceLoader.load(IDEBridge.class, IDEBridge.class.getClassLoader());
    service.reload();
    final Iterator<IDEBridge> iterator = service.iterator();
    IDE_INFO_PROVIDER = iterator.hasNext() ? iterator.next() : new DefaultIDEBridge();
    LoggerFactory.getLogger(UIComponentFactoryProvider.class).info("IDE Info provider factory : " + IDE_INFO_PROVIDER.getClass().getName());
  }

  public static IDEBridge findInstance() {
    return IDE_INFO_PROVIDER;
  }

}
