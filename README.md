# What is it?
It is a small plugin working under NetBeans platform 7.4+ and allow keep knowledge graph just within projects. It makes real help in knowledge formalization and some time can be used as a knowledge engineering tool.

# How it looks like?
![Screenshot](https://raw.githubusercontent.com/raydac/netbeans-mmd-plugin/master/assets/screenshot1.png)

# How to create new mind map?
Just start the **New File** wizard and select the **Other** category and you will see the **NB Mind-map** record in the **File Types:** list. Select the **NB Mind-map** and press **Next>** in the wizard.
![Screenshot](https://raw.githubusercontent.com/raydac/netbeans-mmd-plugin/master/assets/newfiledialog.png)

# There are no any actions or toolbar buttons! How to work?
You can make manipulations with content through its context popup menu. The Pop-up menu provides all actions allowed for selected topic or topics.
![Screenshot](https://raw.githubusercontent.com/raydac/netbeans-mmd-plugin/master/assets/popup.png)

# Which kind of data I can keep in my maps?
You can keep in maps listed information:
- **Multiline short texts** - formed as topic name, it should not be too long else your map will not be useful. Multiline texts can be entered with **SHFT+ENTER**.
- **Long plain texts** - the information is not visible on the map and will be opened in special editor through double click on its icon (shown in the topic)
- **File links** - you can save file links in topics, by default the plugin checks that the file is placed in the project and makes relative path, you can change the behaviour in **Options** of the plugin. Double click on the file link icon will open the file as an IDE document. **Lifehack: you can just drag and drop files from projects to topics**
- **URLs** - you can attach a URL per topic and open it through double click on its icon.
- **Transitions** - Local jumps to another topics in the same document. It allows to make some navigation in big mind maps or make context based links between  topics.
**NB! In opposite to another mind map editors, NB MindMap allows to keep both a file link and a URL in the same topic, during export to FreeMind, one of links will be lost in the result file** 

# Is it possible to export data?
Yes, the plugin supports export in several more or less standard formats:
- **TXT** - just a plain UTF8 encoded text file
- **MD**  - markdown format which can be used in GitHub for instance.
- **MM**   - FreeMind mind map file format, can be opened in [FreeMind editor 0.8+](http://freemind.sourceforge.net/wiki/index.php/Main_Page) or imported into [XMind](https://www.xmind.net/). **NB! Keep in mind that during export some links can be lost because FreeMind allows only link per topic!**
- **PNG** - standard lossless PNG image.

# Some useful keyboard shortcuts
-  **TAB** - if only topic has focus then its child will be created.
-  **DEL** - remove selected topics
-  **CTRL(or SHFT)+SPACE** - if there is not any selected topic then select the root topic, if there is selected topic then start its editing.
-  **UP,LEFT,RIGHT,DOWN** - navigation through topic tree.
