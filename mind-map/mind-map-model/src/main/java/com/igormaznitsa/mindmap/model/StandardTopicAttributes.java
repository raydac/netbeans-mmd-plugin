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

  /**
   * Type of text align in topic title. Supported left, center and right.
   */
  String MMD_TOPIC_ATTRIBUTE_TITLE_ALIGN = "align";

  /**
   * Encoded Base64 image data saved for topic.
   */
  String MMD_TOPIC_ATTRIBUTE_IMAGE_DATA = "mmd.image";

  /**
   * Name of image saved for topic.
   */
  String MMD_TOPIC_ATTRIBUTE_IMAGE_NAME = "mmd.image.name";

  /**
   * URI of image saved in topic.
   */
  String MMD_TOPIC_ATTRIBUTE_IMAGE_URI = "mmd.image.uri";

}
