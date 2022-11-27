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

package com.igormaznitsa.mindmap.annotations.processor.logger;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerAdapter;
import com.igormaznitsa.mindmap.model.logger.LoggerService;

/**
 * NOP logger service to hide messages from mind map model builder.
 */
public class NopLoggerService implements LoggerService {

  private static final Logger INSTANCE = new LoggerAdapter("NOP");

  @Override
  public Logger getLogger(final Class<?> loggerClass) {
    return INSTANCE;
  }

  @Override
  public Logger getLogger(final String name) {
    return INSTANCE;
  }
}
