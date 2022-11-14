package com.igormaznitsa.mindmap.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation allows to mark source elements to generate MMD topic.
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
public @interface MmdTopic {

  /**
   * Allows to provide UID for the topic to be used as identifier in another topics.
   *
   * @return any text UID or empty if not provided.
   * @see MmdTopic#jumpTo()
   */
  String uid() default "";

  /**
   * Identifier of MMD file which should be a parent for the topic.
   *
   * @return MMD file UID or empty one if auto-select allowed.
   * @see MmdFile#uid()
   */
  String fileUid() default "";

  /**
   * Path to the topic in MMD file
   *
   * @return array contains path from root, every path item can be a topic UID or just title text if
   * there is no any topic with such UID.
   * @see #uid()
   */
  String[] path() default {};

  /**
   * Identifier of an emoticon to be added to the generated mind map topic.
   *
   * @return emoticon identifier
   */
  MmdEmoticon emoticon() default MmdEmoticon.EMPTY;

  /**
   * Title for generated topic.
   *
   * @return Text to be used as title for generated topic.
   */
  String title() default "";

  /**
   * File link path to be added into topic.
   *
   * @return file path to be added into topic as file link (can contain line number in format <i>path:line</i>), can be empty if omitted.
   * @see MmdFile#uid()
   */
  String fileLink() default "";

  /**
   * Add the source file with line position as file link but only if file attribute is empty. <b>File link path field has bigger priority.</b>
   *
   * @return true if should autogenerate source file line link, false otherwise
   * @see #fileLink()
   */
  boolean anchor() default true;

  /**
   * Allows to provide jump link to a topic in the same file.
   *
   * @return target topic UID or topic title text.
   * @see #uid()
   */
  String jumpTo() default "";

  /**
   * Allows to add text note for the topic.
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

  /**
   * Flag to ask topic to be collapsed
   *
   * @return true if topic should be collapsed, false otherwise
   */
  boolean collapse() default false;

  /**
   * Recommended direction for the topic.
   *
   * @return direction which should be used for the topic if it is possible
   */
  Direction direction() default Direction.AUTO;
}
