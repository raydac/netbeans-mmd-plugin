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
package com.igormaznitsa.mindmap.model;

import java.util.Locale;

public class ExtraTopic extends Extra<String> {
  private static final long serialVersionUID = -8556885025460722094L;

  private final String topicUID;
  
  public static final String TOPIC_UID_ATTR = "topicLinkUID"; //NOI18N
  
  public static ExtraTopic makeLinkTo(final MindMap map,final Topic topic){
    ExtraTopic result = null;
    if (topic!=null){
      String uid = topic.getAttribute(TOPIC_UID_ATTR);
      if (uid == null){
        String time = Long.toHexString(System.currentTimeMillis() & 0x7FFFFFFFFFFFFFFFL).toUpperCase(Locale.ENGLISH);
        char extra = 'A';
        while(true){
          uid = time + extra;
          if (map.findTopicForLink(new ExtraTopic(uid))!=null){
            if (extra == 'Z'){
              time = Long.toHexString(System.nanoTime()& 0x7FFFFFFFFFFFFFFFL).toUpperCase(Locale.ENGLISH);
              extra = 'A';
            }else
            extra ++;
          }else{
            break;
          }
        }
        topic.setAttribute(TOPIC_UID_ATTR, uid);
      }
      result = new ExtraTopic(uid);
    }
    return result;
  }
  
  public ExtraTopic(final String topicUID){
    this.topicUID = topicUID;
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
}
