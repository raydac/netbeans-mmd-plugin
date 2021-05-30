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

package com.igormaznitsa.ideamindmap.utils.logger;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerService;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public class IdeaLoggerService implements LoggerService {

  private final Map<Class<?>, Logger> cacheForClass = new HashMap<>();
  private final Map<String, Logger> cacheForCategory = new HashMap<>();

  @Override
  @Nonnull
  public Logger getLogger(@Nonnull final Class<?> aClass) {
    synchronized (this.cacheForClass) {
      Logger result = this.cacheForClass.get(aClass);
      if (result == null) {
        result = new IdeaLogger(aClass);
        cacheForClass.put(aClass, result);
      }
      return result;
    }
  }

  @Override
  @Nonnull
  public Logger getLogger(@Nonnull final String category) {
    synchronized (this.cacheForCategory) {
      Logger result = this.cacheForCategory.get(category);
      if (result == null) {
        result = new IdeaLogger(category);
        this.cacheForCategory.put(category, result);
      }
      return result;
    }
  }
}
