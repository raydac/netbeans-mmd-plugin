package com.igormaznitsa.mindmap.model.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation allows to collect multiple mmd file descriptors.
 *
 * @see MmdFile
 */
@Retention(RetentionPolicy.SOURCE)
@Target({
    ElementType.TYPE
})
@Inherited
public @interface MmdFiles {
  MmdFile[] value() default {};
}
