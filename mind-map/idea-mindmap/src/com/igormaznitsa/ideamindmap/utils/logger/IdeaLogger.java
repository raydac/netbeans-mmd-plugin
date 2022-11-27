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

package com.igormaznitsa.ideamindmap.utils.logger;

import com.igormaznitsa.mindmap.model.logger.Logger;

public class IdeaLogger extends Logger {

  private final com.intellij.openapi.diagnostic.Logger wrappedLogger;

  public IdeaLogger(final String category) {
    super(category);
    this.wrappedLogger = com.intellij.openapi.diagnostic.Logger.getInstance(category);
  }

  public IdeaLogger(final Class<?> aClass) {
    super(aClass);
    this.wrappedLogger = com.intellij.openapi.diagnostic.Logger.getInstance(aClass);
  }

  @Override
  public void info(final String message) {
    this.wrappedLogger.info(message);
  }

  @Override
  public void warn(final String message) {
    this.wrappedLogger.warn(message);
  }

  @Override
  public void error(final String message) {
    this.wrappedLogger.error(message);
  }

  @Override
  public void error(final String message, final Throwable throwable) {
    this.wrappedLogger.error(message, throwable);
  }
}
