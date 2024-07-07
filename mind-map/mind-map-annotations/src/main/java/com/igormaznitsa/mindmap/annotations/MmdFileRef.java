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

package com.igormaznitsa.mindmap.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation allows to make a reference to a some marked {@link MmdFile}
 * or {@link MmdFiles} class through MMD file UID or a class object.
 *
 * @see MmdFile
 * @see MmdFiles
 */
@Retention(RetentionPolicy.SOURCE)
@Target({
    ElementType.TYPE,
    ElementType.FIELD,
    ElementType.METHOD,
    ElementType.CONSTRUCTOR,
    ElementType.ANNOTATION_TYPE,
    ElementType.PACKAGE,
    ElementType.TYPE_PARAMETER,
    ElementType.PARAMETER,
    ElementType.TYPE_USE
})
public @interface MmdFileRef {
  /**
   * UID of some defined MmdFile. <b>Defined non-empty UID has higher priority than the target attribute</b>.
   *
   * @return UID which should be defined in a MmdFile annotation of a project class. Default value is empty one what means undefined.
   * @see MmdFile
   */
  String uid() default "";

  /**
   * Target class which should be marked ether by {@link MmdFile} or {@link @MmdFiles} annotation. First found one in class hierarchy will be used.
   * <b>Keep in mind that if provided {@link MmdFileRef#uid()} attribute then UID has higher priority</b>.
   *
   * @return a class to be used as target to search target mmd file.
   * @see MmdFile
   */
  Class<?> target() default MmdFileRef.class;

  /**
   * Flag shows that text fields contains some variables in format {@code ${variable.name}} and they should be replaced by values.
   *
   * @return true if text fields of the topic contains variables.
   * @since 1.6.8
   */
  boolean substitute() default false;
}
