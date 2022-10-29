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

public final class ImageIconServiceProvider {
  private static final ImageIconService IMAGEICON_SERVICE;

  static {
    final ServiceLoader<ImageIconService> service = ServiceLoader.load(ImageIconService.class, ImageIconService.class.getClassLoader());
    service.reload();
    final Iterator<ImageIconService> iterator = service.iterator();
    IMAGEICON_SERVICE = iterator.hasNext() ? iterator.next() : new DefaultImageIconService();
    LoggerFactory.getLogger(ImageIconServiceProvider.class).info("Image Icon Service factory : " + IMAGEICON_SERVICE.getClass().getName());
  }

  public static ImageIconService findInstance() {
    return IMAGEICON_SERVICE;
  }
}
