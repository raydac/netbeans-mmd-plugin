# Introduction
The Main part of a software project is not code but knowledge generated during the project. I guess knowledge is the main part of every business today and it is very important to move the generated knowledge to the formal level to avoid its loss. There are many approaches to save knowledge and [a mind map diagram](https://en.wikipedia.org/wiki/Mind_map) is one of them. Also it is very important to keep the "distance" between project and its knowledge as short as possible and the ideal variant is to keep knowledge just within the project as a document set. If you use any external knowledge engineering tool then the distance already is not so short and for the cause I had decided to develop some plugin which would allow to form mind maps and keep knowledge graph just within projects and allow to work with the graphs just with the IDE.  
[![http://ecx.images-amazon.com/images/I/51WXkpAYNzL._SX334_BO1,204,203,200_.jpg](http://ecx.images-amazon.com/images/I/51WXkpAYNzL._SX334_BO1,204,203,200_.jpg)](http://www.amazon.com/The-Knowledge-Creating-Company-Companies-Innovation/dp/0195092694)

# Implementation
Because my main programming tool is [NetBeans IDE](https://netbeans.org/), I have developed plugin for the IDE.
![Screenshot](https://raw.githubusercontent.com/raydac/netbeans-mmd-plugin/master/assets/screenshot1.png)

# Key features
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
-  **File link** - link to a file, **the file will be opened within IDE as a document**
-  **Transition** - link to another topic in the same mind map, it allows to make some navigation in very big mind maps

You can add and edit new toipic content through the maind map pop-up menu and if you want remove some content then just open editor for the content and remove all text information.

# Donation   
If you like the software you can make some donation to the author   
[![https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=AHWJHJFBAWGL2)
