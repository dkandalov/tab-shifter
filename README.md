[![Build Status](https://travis-ci.org/dkandalov/tab-shifter.svg?branch=master)](https://travis-ci.org/dkandalov/tab-shifter)

Tab Shifter
====
[**Tab Shifter**](http://plugins.jetbrains.com/plugin/7475) is a plugin for IntelliJ IDEA 
with a bunch of actions to move tabs between editor splitters and resize splitters
<br/>
Actions are added to `Main Menu -> Window -> Tab Shifter`.

OSX shortcuts:
 - `ctrl + alt + ]` - move tab to the right splitter (or create a new one if it's the rightmost splitter)
 - `ctrl + alt + [` - move tab to the left splitter
 - `ctrl + alt + P` - move tab to the splitter above
 - `ctrl + alt + '` - move tab to the splitter below (or create a new one if it's the bottom splitter)
 - `alt + shift + [` - stretch splitter left
 - `alt + shift + ]` - stretch splitter right
 - `alt + shift + =` - equal size splitter
 - `alt + shift + M` - maximize/restore splitter

Other OS shortcuts:
 - `alt + shift + ]` - move tab right
 - `alt + shift + [` - move tab left
 - `alt + shift + P` - move tab up
 - `alt + shift + '` - move tab down
 - `ctrl + alt + [` - stretch splitter left
 - `ctrl + alt + ]` - stretch splitter right
 - `ctrl + alt + =` - equal size splitter
 - `alt + shift + M` - maximize/restore splitter

To move focus between splitters:
 - `ctrl+alt+shift+]` - right
 - `ctrl+alt+shift+[` - left
 - `ctrl+alt+shift+P` - up
 - `ctrl+alt+shift+;` - down
 - `ctrl + alt + .` - (built-in action) recommended binding for `Goto Next Splitter` action

Obviously, the above shortcuts can be changed in `IDE Settings -> Keymap`.


Why?
====
Basically, this plugin treats splitting as "take the current editor and *move* it to the next split window".
If there is no split window, then create new one.

<img src="https://raw.githubusercontent.com/dkandalov/tab-shift/master/tab-shifter.gif" alt="" title="" align="center"/>

There are built-in actions to move tabs (see `Main Menu -> Window -> Editor Tabs -> Move Right/Down`).
Unfortunately, they don't do the right thing. For example:
 - open several tabs in editor
 - use built-in action to move tab right (`Main Menu -> Window -> Editor Tabs -> Move Right`);
   editor will be split into two and the tab will move to the right editor.
 - move back to leftmost editor and use built-in action to move tab right;
   the leftmost editor will be split into two editors again (three editors in total) 
   the tab will move into the middle editor. 
   **Desired behavior** is to recognize that there is already editor on the right side and move tab into it. 

If you like the idea, please vote for [this issue on youtrack](https://youtrack.jetbrains.com/issue/IDEA-68692).


Credits
====
Plugin idea by [Sandro Mancuso](https://twitter.com/sandromancuso) at [SoCraTes UK 2013](http://socratesuk.org).
Created using [LivePlugin](https://github.com/dkandalov/live-plugin).