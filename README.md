Tab Shifter
====
[**Tab Shifter**](http://plugins.jetbrains.com/plugin/7475) is a tiny plugin for IntelliJ IDEA to move and split editor tabs.
 - ctrl + alt + \] - move tab to the right split (or create new split if it's the rightmost split)
 - ctrl + alt + \[ - move tab to left split
 - ctrl + alt + ; - move tab to split above
 - ctrl + alt + ' - move tab to split below (or create new split if it's the bottom split)
 - ctrl + alt + . - (not part of this plugin) recommended to bind to Goto Next Splitter action
Also available in Windows -> Editor Tabs menu.


Why?
====
There are built-in actions to move tabs (see Window -> Editor Tabs -> Move Left/Right).
Unfortunately, they don't do "the right thing". This is an attempt to fix it.

Basically, this plugin treats splitting as "take current editor and _move_ it to the next split window".
If there is no split window, then create one.

The best way to understand how tab shifting work is to try it... seriously.
Otherwise, see screenshots below (TODO create gif animation).

Editor without any splits.
<img src="https://raw.githubusercontent.com/dkandalov/tab-shift/master/screenshot0.png" alt="" title="" align="center"/>

Moved current tab right. (Built-in action would duplicate current tab.)
<img src="https://raw.githubusercontent.com/dkandalov/tab-shift/master/screenshot1.png" alt="" title="" align="center"/>

Switched focus to tab in the left split and moved tab right. (Built-in action which would create yet another split.)
<img src="https://raw.githubusercontent.com/dkandalov/tab-shift/master/screenshot2.png" alt="" title="" align="center"/>

Shifted current tab right.
(It was the rightmost split so creating new split.)
<img src="https://raw.githubusercontent.com/dkandalov/tab-shift/master/screenshot3.png" alt="" title="" align="center"/>


Credits
====
Plugin idea by [Sandro Mancuso](https://twitter.com/sandromancuso) at [SoCraTes UK 2013](http://socratesuk.org).
Originally based on [this mini-plugin](https://gist.github.com/dkandalov/6643735).