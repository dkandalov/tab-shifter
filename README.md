Tab Shifter
====
[**Tab Shifter**](http://plugins.jetbrains.com/plugin/7475) is a tiny plugin for IntelliJ IDEA to move and split editor tabs.<br/>
It adds several actions to ``Main Menu -> Window -> Editor Tabs``.

OSX shortcuts:
 - ``ctrl + alt + \]`` - move tab to the right split (or create new split if it's the rightmost split)
 - ``ctrl + alt + \[`` - move tab to the left split
 - ``ctrl + alt + ;`` - move tab to the split above
 - ``ctrl + alt + '`` - move tab to the split below (or create new split if it's the bottom split)
 - ``ctrl + alt + .`` - (not part of this plugin) recommended to bind to Goto Next Splitter action

Other OS shortcuts:
 - ``alt + shift + \]`` - move tab right
 - ``alt + shift + \[`` - move tab left
 - ``alt + shift + ;`` - move tab up
 - ``alt + shift + '`` - move tab down

If any of these shortcuts have conflicts, then obviously feel free to change them.


Why?
====
Basically, this plugin treats splitting as "take current editor and *move* it to the next split window".
If there is no split window, then create new one.

<img src="https://raw.githubusercontent.com/dkandalov/tab-shift/master/tab-shifter.gif" alt="" title="" align="center"/>

There are built-in actions to move tabs (see ``Main Menu -> Window -> Editor Tabs -> Move Right/Down``).
Unfortunately, they don't do the right thing. For example:
 - open several tabs in editor
 - use built-in action to move tab right (``Main Menu -> Window -> Editor Tabs -> Move Right``);
   editor will be split into two and the tab will move to the right editor.
 - move back to leftmost editor and use built-in action to move tab right;
   leftmost editor will be split into two editors again (three editors in total) 
   the tab will move into the middle editor. 
   **Desired behavior** is to recognize that there is already editor on the right side and move tab into it. 


Credits
====
Plugin idea by [Sandro Mancuso](https://twitter.com/sandromancuso) at [SoCraTes UK 2013](http://socratesuk.org).
Created using [LivePlugin](https://github.com/dkandalov/live-plugin).