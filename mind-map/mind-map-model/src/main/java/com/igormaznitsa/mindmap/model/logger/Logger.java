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
 * Abstract logger to be used by mind map based components.
 */
public abstract class Logger {

  /**
   * Constructor based on class.
   *
   * @param klazz class to be used as identifier of logger, must not be null
   */
  public Logger(final Class<?> klazz) {
  }

  /**
   * Constructor based on logger name.
   *
   * @param name name to be used as identifier of logger, must not be null
   */
  public Logger(final String name) {
  }

  /**
   * Log info record
   *
   * @param message text to be logged as info record, must not be null
   */
  public abstract void info(String message);

  /**
   * Log warning record
   *
   * @param message text to be logged as warning record, must not be null
   */
  public abstract void warn(String message);

  /**
   * Log error record
   *
   * @param message text to be logged as error record, must not be null
   */
  public abstract void error(String message);

  /**
   * Log error record
   *
   * @param message text to be logged as error record, must not be null
   * @param error   to be logged, must not be null
   */
  public abstract void error(String message, Throwable error);
}
