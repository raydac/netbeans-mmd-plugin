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
package com.igormaznitsa.nbmindmap.nb.refactoring;

import com.igormaznitsa.nbmindmap.nb.refactoring.elements.MoveFileActionPlugin;
import com.igormaznitsa.nbmindmap.nb.refactoring.elements.RenameFileActionPlugin;
import com.igormaznitsa.nbmindmap.nb.refactoring.elements.SafeDeleteFileActionPlugin;
import com.igormaznitsa.nbmindmap.nb.refactoring.elements.WhereUsedActionPlugin;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceProvider(service = RefactoringPluginFactory.class)
public class MindMapRefactoringFactory implements RefactoringPluginFactory {

  private static final Logger logger = LoggerFactory.getLogger(MindMapRefactoringFactory.class);
  
  @Override
  public RefactoringPlugin createInstance(final AbstractRefactoring refactoring) {
    logger.info("Request to create refactoring plugin : "+refactoring);
    
    final RefactoringPlugin result;

    if (refactoring instanceof SafeDeleteRefactoring) {
      result = new SafeDeleteFileActionPlugin((SafeDeleteRefactoring) refactoring);
    }
    else if (refactoring instanceof MoveRefactoring) {
      result = new MoveFileActionPlugin((MoveRefactoring)refactoring);
    }
    else if (refactoring instanceof RenameRefactoring) {
      result = new RenameFileActionPlugin((RenameRefactoring)refactoring);
    }
    else if (refactoring instanceof WhereUsedQuery) {
      result = new WhereUsedActionPlugin((WhereUsedQuery)refactoring);
    }
    else {
      result = null;
    }
    return result;
  }

}
