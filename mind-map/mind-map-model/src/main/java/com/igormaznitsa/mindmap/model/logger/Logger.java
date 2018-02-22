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
package com.igormaznitsa.mindmap.model.logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class Logger {
  
  public Logger(@Nonnull final Class<?> klazz) {
  }
  
  public Logger(@Nonnull final String name) {
  }
  
  public abstract void info(@Nullable String message);
  public abstract void warn(@Nullable String message);
  public abstract void error(@Nullable String message);
  public abstract void error(@Nullable String message,@Nullable Throwable error);
}
