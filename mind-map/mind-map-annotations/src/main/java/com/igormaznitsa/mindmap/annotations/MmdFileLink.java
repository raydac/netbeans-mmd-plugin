package com.igormaznitsa.mindmap.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation allows to link type to some MMD file UID defined for another type.
 *
 * @see MmdFile
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface MmdFileLink {
  String uid();
}
