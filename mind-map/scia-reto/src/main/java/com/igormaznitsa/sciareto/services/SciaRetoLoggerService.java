/* 
 * Copyright (C) 2018 Igor Maznitsa.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package com.igormaznitsa.sciareto.services;

import java.util.logging.Level;
import com.igormaznitsa.mindmap.model.logger.impl.JavaLoggerServiceImpl;

public class SciaRetoLoggerService extends JavaLoggerServiceImpl {

  public SciaRetoLoggerService() {
    super();
    final java.util.logging.Logger rootLogger = java.util.logging.LogManager.getLogManager().getLogger(""); //NOI18N
    rootLogger.setLevel(Level.WARNING);
  }
}
