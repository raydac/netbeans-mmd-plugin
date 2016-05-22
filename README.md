[![License Apache 2.0](https://img.shields.io/badge/license-Apache%20License%202.0-green.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/6cec4cb9ac42419aa003a27597c3c357)](https://www.codacy.com/app/rrg4400/netbeans-mmd-plugin)
[![Java 7.0+](https://img.shields.io/badge/java-7.0%2b-green.svg)](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
[![NetBeans](https://img.shields.io/badge/netbeans-7.4%2b-green.svg)](http://plugins.netbeans.org/plugin/60188/nb-mindmap-editor)
[![Intellij IDEA](https://img.shields.io/badge/idea-13.0%2b-green.svg)](https://plugins.jetbrains.com/plugin/8045)
[![PayPal donation](https://img.shields.io/badge/donation-PayPal-red.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=AHWJHJFBAWGL2)
[![Yandex.Money donation](https://img.shields.io/badge/donation-Я.деньги-yellow.svg)](https://money.yandex.ru/embed/small.xml?account=41001158080699&quickpay=small&yamoney-payment-type=on&button-text=01&button-size=l&button-color=orange&targets=%D0%9F%D0%BE%D0%B6%D0%B5%D1%80%D1%82%D0%B2%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D0%B5+%D0%BD%D0%B0+%D0%BF%D1%80%D0%BE%D0%B5%D0%BA%D1%82%D1%8B+%D1%81+%D0%BE%D1%82%D0%BA%D1%80%D1%8B%D1%82%D1%8B%D0%BC+%D0%B8%D1%81%D1%85%D0%BE%D0%B4%D0%BD%D1%8B%D0%BC+%D0%BA%D0%BE%D0%B4%D0%BE%D0%BC&default-sum=100&successURL=)

![Banner](https://raw.githubusercontent.com/raydac/netbeans-mmd-plugin/master/misc/banner.png)  

# Introduction
The Main part of a software project is not code but knowledge generated during the project. I guess knowledge is the main part of every business today and it is very important to move the generated knowledge from non-formal level to formal level to avoid its loss. There are many approaches to save knowledge and [a mind map diagram](https://en.wikipedia.org/wiki/Mind_map) is one of them. Also it is very important to keep the "distance" between project and its knowledge as short as possible and the ideal variant is to keep knowledge just within the project as a document set. If you use any external knowledge engineering tool then the distance already is not so short and for the cause I had decided to develop some plugin which would allow to form mind maps and keep knowledge graph just within projects and allow to work with the graphs just with the IDE.  

# Changelog
___1.2.0-SNAPSHOT (Under development)___
- Added plugin "Emoticons"
- Added importer from text files written in style [text2mindmap](https://www.text2mindmap.com/)
- Added key board shortcuts for zooming in, out and resetting zoom.
- fixed issue [#10 "In Select topic dialog; Fold All only closes the root node"](https://github.com/raydac/netbeans-mmd-plugin/issues/10)
- fixed issue [#9 "Cannot find configurable: com.igormaznitsa.ideamindmap.settings.MindMapSettingsComponent"](https://github.com/raydac/netbeans-mmd-plugin/issues/9)
- the IDEA project adapted to be compatible with IDEA 16 platform
- fixed issue [#11 "Can't create topic with hash char"](https://github.com/raydac/netbeans-mmd-plugin/issues/11)
- reworked mind map format parser to make it compatible with PSI tree parser in IDEA
- refactoring

__1.1.4 (21-feb-2015)__
- improved PNG exporter to save images without background and with all unfolded topics
- added graphical print support into the IDEA plugin
- added tuning of key modifiers to activate scaling with mouse wheel
- fixed icons in color choosing buttons in IDEA version under Retina
- improved panel, now clicking on '+' of a topic with pressed CTRL will open only 1th level children
- improvements of stability in work with DnD operations
- refactoring of both plug-ins, improved stability of work

__1.1.3 (maintenance) (29-nov-2015)__
- the IDEA plugin enabled for all products based on the IDEA platform (but not all of them allow to tune facets) (IDEA, 1.0.3)
- enabled folder actions for folders within .projectKnowledge folder (NetBeans, 1.1.3)
- improved DnD processing to avoid potential NPE for objects without file references (IDEA, 1.0.3)
- added "Knowledge" view into Project pane to show separately .projectKnowledge folder content (IDEA, 1.0.3)
- fixed incompatibility of the Settings panel with OpenJDK (IDEA, 1.0.3)

__1.1.2 (maintenance) (22-nov-2015)__
- refactoring to increase compatibility with different IDEs
- removed logging with slf4j, added services to provide different logging for each IDE
- improved logic of opening file link in both IDEs
- changed byte-code version to Java 6 (but it still uses java.nio.file.Path from Java 7+)
- bug fixing in the IDEA version (1.0.2), changed minimal IDEA version to IDEA 13 (133 build), but it should be started under Java 7+

__1.1.1 (maintenance) (15-oct-2015)__
- [Intellij IDEA plugin published in the IDEA repository](https://plugins.jetbrains.com/plugin/8045)
- very small refactoring
- added support of word-wrap mode change into the plain text editor
- improved focus loose processing for topic text editor, now the editing text saved if editor lost focus (issue #1)
- "leftSide" attribute added into standard attribute list to support removing of topic contains only the attribute
- added extra check for file existence in file link edit
- fixed seldom NPE in refactoring module to avoid NPE if not found Project for FileObject or the Project doesn't have folder (issue #4)  

__1.1.0 (09-oct-2015)__
- minor bugfix and refactoring
- minimal Java version updated to 1.7+
- added "Knowledge" view to projects, it shows content of the .projectKnowledge project folder (the feature can be disabled through "Options")
- added base support for refactoring actions "Move", "Delete", "Rename" and "Find usages" (works for Java projects)
- added support of transitions show (which are renamed to "jumps") between topics on mind maps as arrowed lines
- added support of folders in file links, also now it is possible to define special flag to open a file link in the default system viewer
- added support of colorization for every topic
- file links to another mind maps are shown as NB MindMap icons
- file links to files with absolute path have special badge
- improved drag and drop for topics, D&D of a topic with CTRL (pressed on start of operation) will make link to the dragged topic
- improved print support  

__1.0.0 (06-sep-2015)__
- Initial version  

# Implementation
Because my main programming tool is [NetBeans IDE](https://netbeans.org/), I have developed plugin for the IDE. It is compatible with Java 1.7+ and NetBeans 7.4+.  
![Screenshot](https://raw.githubusercontent.com/raydac/netbeans-mmd-plugin/master/assets/screenshot1.png)  
[You can take a look at the nice screencast made by Geertjan Wielenga about the plugin](https://www.youtube.com/watch?v=7TUU25dsOfM)  

# Key features
## Data format
The Plugin keeps all mind map information in single UTF8 encoded plain text file formed in markdown compatible format, so the file can be easily read and interpret and without the plugin.  

```
Mind Map generated by NB MindMap plugin   
> __version__=`1.0`
---

# Root <br/>topic
> topicLinkUID=`14F9D4CD31DA`


## First level
- LINK
<pre>http://www.google.com</pre>

### Item 1\.1
- NOTE
<pre>Some note for item 1.1</pre>

### Item 1\.2
- TOPIC
<pre>14F9D4CD31DA</pre>
```

## New mind map creation
The Plugin creates new mind map just within project folders through the standard NetBeans new file wizard, just start the **New File** wizard and select the **Other** category and you will see the **NB Mind-map** record in the **File Types:** list. Select the **NB Mind-map** and press **Next>** in the wizard.  
![New file wizard](https://raw.githubusercontent.com/raydac/netbeans-mmd-plugin/master/assets/newfiledialog.png)  

## Work with mind maps
Mind maps are opened in IDE as documents with graphic interface and the plugin doesn't add any new actions into IDE menu so all operations over mind maps you can make through the document pop-up menu. The Pop-up menu is formed for the current document state and context.  
![Screenshot](https://raw.githubusercontent.com/raydac/netbeans-mmd-plugin/master/assets/popup.png)  

## Move topics
All manipulations over topic order and its position in the map hierarchy can be made only through mouse Drag&Drop operations (keep in mind that only one topic can be moved in the same time).  
![Screenshot](https://raw.githubusercontent.com/raydac/netbeans-mmd-plugin/master/assets/dragdroptopic.png)  

## Change look and feel of mind maps
You can tune most of graphic parameters for mind map rendering through the IDE **Options** (or just click **Options** in the pop-up menu).  
![Screenshot](https://raw.githubusercontent.com/raydac/netbeans-mmd-plugin/master/assets/optionspanel.png)  

## Export mind map data
Today it is very important to have way to export data to another formats to use the knowledge in another tools. The Plugin supports several formats to export data:  

-  **Plain text format** - just plain UTF8 encoded format.
-  **Markdown format** - UTF8 encoded specialized text format, it is compatible with Markdown used by GitHub.
-  **FreeMind map** - XML based format for [the FreeMind editor](http://freemind.sourceforge.net/), a popular mind map editing tool, also the format is supported for export by [X-Mind editor](https://www.xmind.net/).
-  **Mindmup map** - JSON based format for [the Mindmup online editor](https://www.mindmup.com).
-  **PNG image** - just standard PNG image files.

## Content alowed in mind maps
Mainly you can keep short texts in your mind maps as topic names but sometime it is useful to keep and another information, the plugin allows to keep below listed information assets:

-  **Short text** - short text in one or two lines as topic name, mainly used as id for the topic.
-  **Long text** - long text information which can be opened and edited in special editor.
-  **URL link** - just URL link to be opened in browser
-  **File link** - link to a file, youc can add them through special dialog or just drag and drop file from NetBeans project tree to a topic, **the file will be opened within IDE as a document**
-  **Transition** - link to another topic in the same mind map, it allows to make some navigation in very big mind maps

You can add and edit new topic content through the mind map pop-up menu and if you want remove some content then just open editor for the content and remove all text information.
