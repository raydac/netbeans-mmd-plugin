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

package com.igormaznitsa.mindmap.model.logger;

/**
 * Describes service which providing way to find or create loggers.
 */
public interface LoggerService {

  /**
   * Find or create logger for class.
   *
   * @param klazz class to be used as logger identifier, must not be null
   * @return found or created logger, must not be null
   */
  Logger getLogger(Class<?> klazz);

  /**
   * Find or create logger for name.
   *
   * @param name name to be used as logger identifier, must not be null
   * @return found or created logger, must not be null
   */
  Logger getLogger(String name);
}
