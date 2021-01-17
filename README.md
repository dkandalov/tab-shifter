[![Build Status](https://travis-ci.org/dkandalov/tab-shifter.svg?branch=master)](https://travis-ci.org/dkandalov/tab-shifter)

Tab Shifter
====
[**Tab Shifter**](http://plugins.jetbrains.com/plugin/7475) is a plugin for IntelliJ IDEA 
with a bunch of actions to move tabs between editor splitters and resize splitters.
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
 - `ctrl + alt + shift + ]` - right
 - `ctrl + alt + shift + [` - left
 - `ctrl + alt + shift + P` - up
 - `ctrl + alt + shift + ;` - down
 - `ctrl + alt + .` - (built-in action) recommended binding for `Goto Next Splitter` action

Of course, all the shortcuts can be changed in `IDE Settings -> Keymap`.


Why?
====
The main motivation for this plugin is to have an action which *moves* the current tab to the next split window 
(if there was no split window, create a new one). See [this issue on youtrack](https://youtrack.jetbrains.com/issue/IDEA-68692).

<img src="https://raw.githubusercontent.com/dkandalov/tab-shift/master/tab-shifter.gif" alt="" title="" align="center"/>

There are built-in actions to split and move tabs (see `Main Menu -> Window -> Editor Tabs) but, unfortunately, they don't do the right thing. 
In particular:
 - `Split Vertically/Horizontally` duplicates the current tab in the new split.
 - `Split and Move Right/Down` always splits the current window (even if there is already split window in the specified direction)
   and has no symmetric actions to move left/up (instead, you have to unsplit).


Credits
====
Plugin idea by [Sandro Mancuso](https://twitter.com/sandromancuso) at [SoCraTes UK 2013](http://socratesuk.org).
Initially created using [LivePlugin](https://github.com/dkandalov/live-plugin).