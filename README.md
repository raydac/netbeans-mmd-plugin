[![License Apache 2.0](https://img.shields.io/badge/license-Apache%20License%202.0-green.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/6cec4cb9ac42419aa003a27597c3c357)](https://www.codacy.com/app/rrg4400/netbeans-mmd-plugin)
[![Java 7.0+](https://img.shields.io/badge/java-7.0%2b-green.svg)](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
[![NetBeans](https://img.shields.io/badge/netbeans-8.0%2b-green.svg)](http://plugins.netbeans.org/plugin/60188/nb-mindmap-editor)
[![Intellij IDEA](https://img.shields.io/badge/idea-14.0.5%2b-green.svg)](https://plugins.jetbrains.com/plugin/8045)
[![PayPal donation](https://img.shields.io/badge/donation-PayPal-red.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=AHWJHJFBAWGL2)
[![Yandex.Money donation](https://img.shields.io/badge/donation-Я.деньги-yellow.svg)](http://yasobe.ru/na/iamoss)

![Banner](https://raw.githubusercontent.com/raydac/netbeans-mmd-plugin/master/misc/banner.png)  

# Introduction
The Main part of a software project is not code but knowledge generated during the project. I guess knowledge is the main part of every business today and it is very important to move the generated knowledge from non-formal level to formal level to avoid its loss. There are many approaches to save knowledge and [a mind map diagram](https://en.wikipedia.org/wiki/Mind_map) is one of them. Also it is very important to keep the "distance" between project and its knowledge as short as possible and the ideal variant is to keep knowledge just within the project as a document set. If you use any external knowledge engineering tool then the distance already is not so short and for the cause I had decided to develop some plugin which would allow to form mind maps and keep knowledge graph just within projects and allow to work with the graphs just with the IDE.  

# Changelog
__1.4.5 (SNAPSHOT)__
- ALL: Paste plain text to add a new node [#48](https://github.com/raydac/netbeans-mmd-plugin/issues/48) 
- ALL: improved procesing of fold/unfold keyboard shortcuts to fold and unfold all children [#17](https://github.com/raydac/netbeans-mmd-plugin/issues/17)
- SR:  updated PlantUML version to 1.2018.12

__1.4.4 (04-aug-2018)__
- ALL: [#46](https://github.com/raydac/netbeans-mmd-plugin/issues/46) improved map model save procedure to not encode national charsets into URL format
- SR:  updated versions: PlantUML 1.2018.9, Batik 1.10, JLatexMath 1.0.7, JNA 4.5.2
- ALL: added support of line number in file link through pattern `<file_path>:<line_number>` [twit](https://twitter.com/IgorMaznitsa/status/1013153379122581504)
- IJ:  [#15](https://github.com/raydac/netbeans-mmd-plugin/issues/15) added node search panel usually activated by `CTRL+F` [twit](https://twitter.com/IgorMaznitsa/status/1013387683475935234)
- ALL: increased number of emoticons
- ALL: [#45](https://github.com/raydac/netbeans-mmd-plugin/issues/45) improved security in XML import
- SR:  added list of file extensions to be opened in system provided viewer
- ALL: added extra logic to check edited content to prevent false model change event
- SR:  improved PlantUML editor
- SR:  improved speed of work with project folders

__1.4.3 (01-may-2018)__
- SR:  license changed to LGPL 2.1
- ALL: increased step of scroll and decreased step of scale in panel
- ALL: added special icon for `mailto` scheme in URI
- ALL: fixed processing of drag'n'drop URL files from browsers under Windows
- IJ:  [#42](https://github.com/raydac/netbeans-mmd-plugin/issues/42) Fixed processing of project root in maven multi-project workspace
- ALL: [#44](https://github.com/raydac/netbeans-mmd-plugin/issues/44) Improved emoticon panel to increase slected icon visibility 
- ALL: [#43](https://github.com/raydac/netbeans-mmd-plugin/issues/43) Fixed non-processed exception "Clipboard is busy"
- IJ:  [#40](https://github.com/raydac/netbeans-mmd-plugin/issues/40) Fixed exception if turn off option `disable .projectKnowledge folder autocreation` in proect facet
- SR:  added support for [PlantUML](http://plantuml.com/) script rendering (files `.pu`,`.puml` and `.plantuml`)

[Full changelog](https://github.com/raydac/netbeans-mmd-plugin/blob/master/changelog.txt)   

# Implementation
Because my main programming tool is [NetBeans IDE](https://netbeans.org/), I have developed plugin for the IDE. It is compatible with Java 1.7+ and NetBeans 7.4+.  
![Screenshot](https://raw.githubusercontent.com/raydac/netbeans-mmd-plugin/master/assets/screenshot1.png)  
[You can take a look at the nice screencast made by Geertjan Wielenga about the plugin](https://www.youtube.com/watch?v=7TUU25dsOfM)  
___Also there is standalone version of the Mind Map plagin called "Scia Reto" editor. [It can be loaded from the latest release page](https://github.com/raydac/netbeans-mmd-plugin/releases/latest). Just select archive for your OS (EXE version is for Windows, DMG version is for Mac OS.)___

# Key features
## Data format
The Plugin keeps all mind map information in single UTF8 encoded plain text file formed in markdown compatible format, so the file can be easily read and interpret and without the plugin.  

```
Mind Map generated by NB MindMap plugin   
> __version__=`1.1`
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
Today it is very important to have way to export data to another formats to use the knowledge in another tools. The Plug-in supports several formats to export data:  

-  __Plain text__ format
-  __Markdown__ format
-  __Freemind__ format
-  __Mindmup__ format
-  __PNG__ image
-  __SVG__ image
- __ORG__ format

## Import data as Mind map
At present the editor allows to import main maps from many well-known mind map formats
- __Mindmup__ format
- __Freemind__ format
- __XMind__ format
- __Coggle__ format
- __Novamind__ format
also it can import mind map from tabbed text files.

## Content alowed in mind maps
Mainly you can keep short texts in your mind maps as topic names but sometime it is useful to keep and another information, the plugin allows to keep below listed information assets:

-  __Short text__ - short text in one or two lines as topic name, mainly used as id for the topic.
-  __Long text__ - long text information which can be opened and edited in special editor.
-  __URL link__ - just URL link to be opened in system browser
-  __File link__ - link to a file, youc can add them through special dialog or just drag and drop file from NetBeans project tree to a topic, **the file will be opened within IDE as a document**
-  __Transition__ - link to another topic in the same mind map, it allows to make some navigation in very big mind maps

You can add and edit new topic content through the mind map pop-up menu and if you want remove some content then just open editor for the content and remove all text information.
