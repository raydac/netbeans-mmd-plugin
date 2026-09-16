# Annotation examples

Shapes taken from `mind-map-annotation-processor-it`. Trim to what the current map needs.

## One map on a type

```java
package com.example.auth;

import com.igormaznitsa.mindmap.annotations.MmdColor;
import com.igormaznitsa.mindmap.annotations.MmdFile;
import com.igormaznitsa.mindmap.annotations.MmdTopic;

@MmdFile(
    uid = "auth-map",
    fileName = "auth-overview",
    folder = "docs/mmd",
    rootTopic = @MmdTopic(
        uid = "auth-root",
        title = "Auth",
        note = "Session tokens only. Do not call from the Swing EDT.",
        colorFill = MmdColor.LightSteelBlue))
public class AuthService {

  @MmdTopic(title = "login", note = "Returns a session or throws AuthException")
  public Session login(final Credentials credentials) {
    return this.sessions.create(credentials);
  }

  @MmdTopic(path = "login", title = "password check")
  void verifyPassword(final Credentials credentials) {
    this.passwords.verify(credentials);
  }
}
```

## Topics in another class of the same map

```java
package com.example.auth;

import com.igormaznitsa.mindmap.annotations.MmdFileRef;
import com.igormaznitsa.mindmap.annotations.MmdTopic;

@MmdFileRef(target = AuthService.class)
@MmdTopic(title = "Token store")
public class TokenStore {

  @MmdTopic(path = "Token store", uid = "token-put")
  public void put(final String id, final Session session) {
  }

  @MmdTopic(path = "Token store", jumpTo = "token-put")
  public Session get(final String id) {
    return null;
  }
}
```

`@MmdFileRef(uid = "auth-map")` is equivalent when the file `uid` is set. Prefer `target` when the declaring class is on the classpath; prefer `uid` across modules.

## Two maps from one type

```java
@MmdFiles({
    @MmdFile(fileName = "api", uid = "F-api"),
    @MmdFile(fileName = "ops", uid = "F-ops")
})
public class Gateway {

  @MmdTopics({
      @MmdTopic(title = "HTTP API", fileUid = "F-api"),
      @MmdTopic(title = "health", fileUid = "F-ops")
  })
  public void start() {
  }
}
```

Without `fileUid`, a topic on this type fails with multiple target files.

## Path, jump, colors

```java
@MmdFile(
    fileName = "attributes",
    rootTopic = @MmdTopic(uid = "rootTopicUid", title = "Root topic"))
public class Root {

  @MmdTopic(title = "method one", colorFill = MmdColor.Tomato)
  public void methodOne() {
  }

  @MmdTopic(uid = "66722", title = "method two")
  public void methodTwo() {
  }

  @MmdTopic(title = "method three", jumpTo = "66722")
  public void methodThree() {
  }

  @MmdTopic(path = "method one", direction = Direction.LEFT)
  public void subOnLeft() {
  }

  @MmdTopic(path = "method one", jumpTo = "rootTopicUid")
  public void subJumpRoot() {
  }
}
```

`jumpTo` may be a UID or a title (`"Root topic"`).

## Locals and comment marks

Needs `@HasMmdMarkedElements` on the executable. Comment marks also need `-Ammd.comment.scan=true`.

```java
@MmdFileRef(target = AuthService.class)
@MmdTopic(title = "LOCAL_VARIABLES")
public class Handshake {

  @HasMmdMarkedElements
  private void run() {
    @MmdTopic(title = "system", colorFill = MmdColor.Green)
    int system = 0;

    //@MmdTopic some text1
    this.stepOne();

    // @mmdTopic (note="Hello note\nnext line", colorfill=yellow) visible
    this.stepTwo();
  }
}
```

A sibling method **without** `@HasMmdMarkedElements` may contain `@MmdTopic` on locals; those inner marks are ignored.

## Interface hierarchy

`@MmdFile` on the root type; `@MmdTopic` on implementing types and methods. File targeting walks ancestors, so implementors do not need a second `@MmdFile` if they sit under that root.
