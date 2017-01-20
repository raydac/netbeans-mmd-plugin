[![Codewake](https://www.codewake.com/badges/codewake2.svg)](https://www.codewake.com/p/nb-mind-map-plugin)
[![License Apache 2.0](https://img.shields.io/badge/license-Apache%20License%202.0-green.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/6cec4cb9ac42419aa003a27597c3c357)](https://www.codacy.com/app/rrg4400/netbeans-mmd-plugin)
[![Java 7.0+](https://img.shields.io/badge/java-7.0%2b-green.svg)](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
[![NetBeans](https://img.shields.io/badge/netbeans-8.0%2b-green.svg)](http://plugins.netbeans.org/plugin/60188/nb-mindmap-editor)
[![Intellij IDEA](https://img.shields.io/badge/idea-14.0.5%2b-green.svg)](https://plugins.jetbrains.com/plugin/8045)
[![PayPal donation](https://img.shields.io/badge/donation-PayPal-red.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=AHWJHJFBAWGL2)
[![Yandex.Money donation](https://img.shields.io/badge/donation-Я.деньги-yellow.svg)](https://money.yandex.ru/embed/small.xml?account=41001158080699&quickpay=small&yamoney-payment-type=on&button-text=01&button-size=l&button-color=orange&targets=%D0%9F%D0%BE%D0%B6%D0%B5%D1%80%D1%82%D0%B2%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D0%B5+%D0%BD%D0%B0+%D0%BF%D1%80%D0%BE%D0%B5%D0%BA%D1%82%D1%8B+%D1%81+%D0%BE%D1%82%D0%BA%D1%80%D1%8B%D1%82%D1%8B%D0%BC+%D0%B8%D1%81%D1%85%D0%BE%D0%B4%D0%BD%D1%8B%D0%BC+%D0%BA%D0%BE%D0%B4%D0%BE%D0%BC&default-sum=100&successURL=)

![Banner](https://raw.githubusercontent.com/raydac/netbeans-mmd-plugin/master/misc/banner.png)  

# Introduction
The Main part of a software project is not code but knowledge generated during the project. I guess knowledge is the main part of every business today and it is very important to move the generated knowledge from non-formal level to formal level to avoid its loss. There are many approaches to save knowledge and [a mind map diagram](https://en.wikipedia.org/wiki/Mind_map) is one of them. Also it is very important to keep the "distance" between project and its knowledge as short as possible and the ideal variant is to keep knowledge just within the project as a document set. If you use any external knowledge engineering tool then the distance already is not so short and for the cause I had decided to develop some plugin which would allow to form mind maps and keep knowledge graph just within projects and allow to work with the graphs just with the IDE.  

# Changelog
__1.4.1 (under development)__
- IJ: fixed issue "Project Project (Disposed) test already disposed" [#25](https://github.com/raydac/netbeans-mmd-plugin/issues/25)

__1.4.0-SNAPSHOT (18-dec-2016)__
- IJ: plugin adapted for compatibility with 14.0.5 (139.1803)
- ALL: added importers for Mindmup, Freemind, Novamind, Coggle and XMind formats
- NB:  disabled watching of file changes by default, added options flag to enable that
- NB:  added option to turn off "where used" refactoring actions for mind maps
- SCIARETO: added graph generator for file links in mind maps of project
- ALL: added support of Cut-Copy-Paste actions over Mind Maps
- ALL: added reset button into URI edit dialog
- ALL: added processing of dragged links and texts from external web browsers
- SCIARETO: fixed 'New Project' under MacOSX
- ALL: improved image plugin to import images from clipboard and max edge size inceased up to 350 pixels
- ALL: improved mind-map options panel, added functions to reset, import and export settings.
- SCIARETO: added CLI interface to convert data
- ALL: export into [Org-Mode](http://orgmode.org/) format [#24](https://github.com/raydac/netbeans-mmd-plugin/issues/24)
- IJ,NB: by default disabled autocreation of .projectKnowledge folder [#23](https://github.com/raydac/netbeans-mmd-plugin/issues/23)
- ALL: added keyboard shortcuts to fold and unfold topics (`-` and `=`) [#18](https://github.com/raydac/netbeans-mmd-plugin/issues/18)
- ALL: added attribute in mind map templates to show jumps by default
- IJ: added auto-positioning of root topic into center of view area just as document open
- SCIARETO: added text search in map 

__1.3.0 (01-sep-2016)__
- added sorting of attributes in mind map model to keep their order
- added plugin to add an image into a topic
- changed Plugin API
- fixed casual d'n'd topic operations through click over its fold-unfold icon
- added export into SVG image format
- published standalone editor "Scia Reto"
- fixed issue [#22](https://github.com/raydac/netbeans-mmd-plugin/issues/22), added key short-cut into options to tune "next line char" for topic text editor, by default SHIFT+ENTER

__1.2.0 (05-jun-2016)__
- __IJ: Minimal supported API version changed to 143.2370__
- fixed issue [#16](https://github.com/raydac/netbeans-mmd-plugin/issues/16), Added default key board shortcut to open popup context menu, CTRL+ALT+SPACE
- Added loading of custom plugins from folder defined through __nbmmd.plugin.folder__ property
- Added plugin "Emoticons"
- Added importer from text files written in style [text2mindmap](https://www.text2mindmap.com/)
- Added key board shortcuts to add focused items during focus move
- Added key board shortcuts for zooming in, out and resetting zoom.
- fixed issue [#10 "In Select topic dialog; Fold All only closes the root node"](https://github.com/raydac/netbeans-mmd-plugin/issues/10)
- fixed issue [#9 "Cannot find configurable: com.igormaznitsa.ideamindmap.settings.MindMapSettingsComponent"](https://github.com/raydac/netbeans-mmd-plugin/issues/9)
- fixed issue [#11 "Can't create topic with hash char"](https://github.com/raydac/netbeans-mmd-plugin/issues/11)
- reworked mind map format parser to make it compatible with PSI tree parser in IDEA
- code refactoring, inside API reworked to plugin based one

__1.1.4 (21-feb-2015)__
- improved PNG exporter to save images without background and with all unfolded topics
- added graphical print support into the IDEA plugin
- added tuning of key modifiers to activate scaling with mouse wheel
- fixed icons in color choosing buttons in IDEA version under Retina
- improved panel, now clicking on '+' of a topic with pressed CTRL will open only 1th level children
- improvements of stability in work with DnD operations
- refactoring of both plug-ins, improved stability of work

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
-  **SVG image** - vector SVG format image files.

## Import data as Mind map
At Present there is only format supported for import. It is plain UTF-8 encoded TAB-formatted text files.

## Content alowed in mind maps
Mainly you can keep short texts in your mind maps as topic names but sometime it is useful to keep and another information, the plugin allows to keep below listed information assets:

-  **Short text** - short text in one or two lines as topic name, mainly used as id for the topic.
-  **Long text** - long text information which can be opened and edited in special editor.
-  **URL link** - just URL link to be opened in browser
-  **File link** - link to a file, youc can add them through special dialog or just drag and drop file from NetBeans project tree to a topic, **the file will be opened within IDE as a document**
-  **Transition** - link to another topic in the same mind map, it allows to make some navigation in very big mind maps

You can add and edit new topic content through the mind map pop-up menu and if you want remove some content then just open editor for the content and remove all text information.

# Public snapshot repository for project libraries
To make accessible the snapshot version of the libraries during development, I have tuned public maven snapshot repository which can be added into project with snippet
```xml
<repositories>
 <repository>
  <id>coldcore.ru-snapshots</id>
  <name>ColdCore.RU Mvn Snapshots</name>
  <url>http://coldcore.ru/m2</url>
  <snapshots>
   <enabled>true</enabled>
  </snapshots>
  <releases>
   <enabled>false</enabled>
  </releases>
 </repository>
</repositories>
```
