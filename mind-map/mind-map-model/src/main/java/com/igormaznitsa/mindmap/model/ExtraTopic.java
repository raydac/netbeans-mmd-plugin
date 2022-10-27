/*
 * Copyright 2015-2018 Igor Maznitsa.
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

package com.igormaznitsa.mindmap.model;

import java.io.File;
import java.util.Locale;
import java.util.regex.Pattern;

public class ExtraTopic extends Extra<String> {
  public static final String TOPIC_UID_ATTR = "topicLinkUID"; //NOI18N
  private static final long serialVersionUID = -8556885025460722094L;
  private final String topicUID;

  public ExtraTopic(final String topicUID) {
    this.topicUID = topicUID;
  }

  public static ExtraTopic makeLinkTo(final MindMap map, final Topic topic) {
    ExtraTopic result = null;
    if (topic != null) {
      String uid = topic.getAttribute(TOPIC_UID_ATTR);
      if (uid == null) {
        String time = Long.toHexString(System.currentTimeMillis() & 0x7FFFFFFFFFFFFFFFL)
            .toUpperCase(Locale.ENGLISH);
        char extra = 'A';
        while (true) {
          uid = time + extra;
          if (map.findTopicForLink(new ExtraTopic(uid)) != null) {
            if (extra == 'Z') {
              time = Long.toHexString(System.nanoTime() & 0x7FFFFFFFFFFFFFFFL)
                  .toUpperCase(Locale.ENGLISH);
              extra = 'A';
            } else {
              extra++;
            }
          } else {
            break;
          }
        }
        topic.setAttribute(TOPIC_UID_ATTR, uid);
      }
      result = new ExtraTopic(uid);
    }
    return result;
  }

  @Override
  public int hashCode() {
    return this.topicUID.hashCode();
  }

  @Override
  public boolean equals(final Object that) {
    if (that == null) {
      return false;
    }
    if (this == that) {
      return true;
    }
    if (that instanceof ExtraTopic) {
      return this.topicUID.equals(((ExtraTopic) that).topicUID);
    } else {
      return false;
    }
  }

  @Override
  public boolean containsPattern(final File baseFolder, final Pattern pattern) {
    return false;
  }

  @Override
  public String getValue() {
    return this.topicUID;
  }

  @Override
  public ExtraType getType() {
    return ExtraType.TOPIC;
  }

  @Override
  public String getAsString() {
    return this.topicUID;
  }

  @Override
  public String provideAsStringForSave() {
    return this.getAsString();
  }
}
