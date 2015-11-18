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
package com.igormaznitsa.mindmap.model.logger.impl;

import com.igormaznitsa.mindmap.model.logger.Logger;

public class JavaLogger extends Logger {

  private final java.util.logging.Logger wrappedLogger;
  
  public JavaLogger (final Class<?> klazz) {
    super(klazz);
    this.wrappedLogger = java.util.logging.Logger.getLogger(klazz.getName());
  }

  public JavaLogger (final String name) {
    super(name);
    this.wrappedLogger = java.util.logging.Logger.getLogger(name);
  }

  @Override
  public void info (final String message) {
    this.wrappedLogger.info(message);
  }

  @Override
  public void warn (final String message) {
    this.wrappedLogger.warning(message);
  }

  @Override
  public void error (final String message) {
    this.wrappedLogger.log(java.util.logging.Level.WARNING,message);
  }

  @Override
  public void error (final String message, final Throwable error) {
    this.wrappedLogger.log(java.util.logging.Level.WARNING, message, error);
  }
  
  public java.util.logging.Logger getWrappedLogger(){
    return this.wrappedLogger;
  }
}
