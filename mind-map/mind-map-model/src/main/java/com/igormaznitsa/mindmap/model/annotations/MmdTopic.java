package com.igormaznitsa.mindmap.model.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation allows to mark source elements to generate MMD topic.
 *
 * @since 1.5.3
 */
@Retention(RetentionPolicy.SOURCE)
@Target({
    ElementType.TYPE,
    ElementType.FIELD,
    ElementType.METHOD,
    ElementType.CONSTRUCTOR,
    ElementType.LOCAL_VARIABLE,
    ElementType.ANNOTATION_TYPE,
    ElementType.PACKAGE,
    ElementType.TYPE_PARAMETER,
    ElementType.PARAMETER,
    ElementType.TYPE_USE
})
public @interface MmdTopic {

  /**
   * Allows provide UID for the topic to be used as identifier in another topics.
   *
   * @return any text UID or empty if not provided.
   * @see MmdTopic#jumpTo()
   */
  String uid() default "";

  /**
   * Identifier of an emoticon to be added to the generated mind map topic.
   *
   * @return emoticon identifier
   */
  MmdEmoticon emoticon() default MmdEmoticon.EMPTY;

  /**
   * Identifier of MMD file which should be parent for the topic.
   *
   * @return MMD file UID or empty if should select file automatically.
   * @see MmdFile#uid()
   */
  String mmdFileUid() default "";

  /**
   * Title for generated topic.
   *
   * @return Text to be used as title for generated topic.
   */
  String value() default "";

  /**
   * File path to be added into topic.
   *
   * @return if there is any MMD file with such UID then its path in use else just path added if non-empty.
   * @see MmdFile#uid()
   */
  String file() default "";

  /**
   * Add source file line as file link but only if there is no directly provided file path.
   *
   * @return true if should autogenerate source file line link, false otherwise
   * @see #file()
   */
  boolean asFileLineLink() default true;

  /**
   * Path to the topic in MMD file
   *
   * @return array contains path from root, every path item can be a topic UID or just title text if there is no any topic with such UID.
   * @see #uid()
   */
  String[] path() default "";

  /**
   * Allows provide jump link to a topic in the same file.
   *
   * @return target topic UID or topic title text.
   * @see #uid()
   */
  String jumpTo() default "";

  /**
   * Allows add text note for the topic.
   *
   * @return text for topic, empty if there is no note
   */
  String note() default "";

  /**
   * URI to be added into topic.
   *
   * @return URI or empty text if there is no URI
   */
  String uri() default "";

  /**
   * Text color for the topic.
   *
   * @return text color for the topic.
   */
  MmdColor colorText() default MmdColor.DEFAULT;

  /**
   * Background fill color for the topic.
   *
   * @return background fill color for the topic.
   */
  MmdColor colorFill() default MmdColor.DEFAULT;

  /**
   * Border fill color for the topic.
   *
   * @return border fill color for the topic.
   */
  MmdColor colorBorder() default MmdColor.DEFAULT;
}
