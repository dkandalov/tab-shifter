What is this?
====
This is [**Tab Shifter**](http://plugins.jetbrains.com/plugin/7475) a tiny plugin for IntelliJ IDEA to move and split editor tabs.
 - alt + shift + \] - move tab to the right split (or create new split if it's the rightmost split)
 - alt + shift + \[ - move tab to left split
 - alt + shift + ; - move tab to split below (or create new split if it's the bottom split)
 - alt + shift + p - move tab to split above

Also available in Windows -> Editor Tabs menu.


Why?
====
There are built-in actions to move tabs (see Window -> Editor Tabs -> Move Left/Right).
Unfortunately, they don't do "the right thing". This is an attempt to fix it.

The best way to understand how tab shifting work is to try it... seriously.
Otherwise, see screenshots below.

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