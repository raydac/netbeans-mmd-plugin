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
package com.igormaznitsa.mindmap.model.nio;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.model.nio.impl.J7PathService;

import java.io.File;
import java.util.Iterator;
import java.util.ServiceLoader;

import javax.annotation.Nonnull;

import com.igormaznitsa.meta.annotation.MustNotContainNull;

public final class Paths {

  private static final Logger LOGGER = LoggerFactory.getLogger(Paths.class);
    
  private static final PathService PATH_SERVICE;
    
  static {
    final ServiceLoader<PathService> service = ServiceLoader.load(PathService.class, Paths.class.getClassLoader());
    service.reload();
    final Iterator<PathService> iterator = service.iterator();
    PATH_SERVICE = iterator.hasNext() ? iterator.next() : new J7PathService();
    
    LOGGER.info("Detected path service : "+PATH_SERVICE.getClass().getName());
  }

  @Nonnull
  public static Path toPath(@Nonnull final File file) {
    return PATH_SERVICE.getForFile(file);
  }

  @Nonnull
  public static Path get(@Nonnull final String string, @MustNotContainNull final String[] next) {
    return PATH_SERVICE.getForPathItems(string, next);
  }


}
