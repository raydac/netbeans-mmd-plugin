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

package com.igormaznitsa.mindmap.model.logger.impl;

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Basic implementations working over java.util.logging.Logger
 */
public class JavaLoggerServiceImpl implements LoggerService {

  private final Map<Class<?>, Logger> cacheForClasses = new ConcurrentHashMap<>();
  private final Map<String, Logger> cacheForNames = new ConcurrentHashMap<>();

  @Override
  public Logger getLogger(final Class<?> klazz) {
    return this.cacheForClasses.computeIfAbsent(requireNonNull(klazz), JavaLogger::new);
  }

  @Override
  public Logger getLogger(final String name) {
    return this.cacheForNames.computeIfAbsent(requireNonNull(name), JavaLogger::new);
  }

}
