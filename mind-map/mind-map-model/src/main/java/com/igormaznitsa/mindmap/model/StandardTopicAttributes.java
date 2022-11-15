package com.igormaznitsa.mindmap.model;

/**
 * Standard attribute names for MMD topics.
 *
 * @since 1.6.0
 */
public interface StandardTopicAttributes {
  /**
   * HTML formatted color for border.
   */
  String MMD_TOPIC_ATTRIBUTE_COLOR_BORDER = "borderColor";
  /**
   * HTML formatted color for background fill.
   */
  String MMD_TOPIC_ATTRIBUTE_COLOR_FILL = "fillColor";
  /**
   * HTML formatted color for title.
   */
  String MMD_TOPIC_ATTRIBUTE_COLOR_TEXT = "textColor";
  /**
   * Boolean flag shows that topic should be on left for its parent. Works only for direct root children.
   */
  String MMD_TOPIC_ATTRIBUTE_SIDE_LEFT = "leftSide";
  /**
   * Flag shows that topic children should be hidden.
   */
  String MMD_TOPIC_ATTRIBUTE_COLLAPSED = "collapsed";

  /**
   * Internal MMD topic UID to be used as anchor for internal cross-topic links.
   */
  String MMD_TOPIC_ATTRIBUTE_LINK_UID = "topicLinkUID";

  /**
   * Identifier of emoticon to be associated with topic.
   */
  String MMD_TOPIC_ATTRIBUTE_EMOTICON = "mmd.emoticon";

  /**
   * Flag shows that topic associated note is encrypted one.
   */
  String MMD_TOPIC_ATTRIBUTE_NOTE_ENCRYPTED = "extras.note.encrypted";
  /**
   * Attribute contains non-encrypted password hint for encrypted note.
   */
  String MMD_TOPIC_ATTRIBUTE_NOTE_ENCRYPTED_PASSWORD_HINT = "extras.note.encrypted.hint";

}
