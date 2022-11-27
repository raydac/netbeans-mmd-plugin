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

import static java.util.Objects.requireNonNull;

/**
 * Just adapter for abstract logger class.
 */
public class LoggerAdapter extends Logger {

  public LoggerAdapter(final String namme) {
    super(requireNonNull(namme));
  }

  public LoggerAdapter(final Class<?> klazz) {
    super(requireNonNull(klazz));
  }

  @Override
  public void info(final String message) {

  }

  @Override
  public void warn(final String message) {

  }

  @Override
  public void error(final String message) {

  }

  @Override
  public void error(final String message, final Throwable error) {

  }
}
