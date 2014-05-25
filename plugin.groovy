import com.intellij.openapi.actionSystem.AnActionEvent
import socrates.tabshifter.Actions

import static liveplugin.PluginUtil.registerAction
import static liveplugin.PluginUtil.show

// add-to-classpath $HOME/Library/Application Support/IntelliJIdea13/live-plugins/tab-shift/out/production/tab-shifter

if (isIdeStartup) return

def shiftLeft = new Actions.ShiftLeft()
registerAction("TabShiftActions.ShiftLeft",
        "alt shift OPEN_BRACKET",
        "EditorTabsGroup",
        "Shift Left") { AnActionEvent event ->
    shiftLeft.actionPerformed(event)
}
def shiftRight = new Actions.ShiftRight()
registerAction("TabShiftActions.ShiftRight",
        "alt shift CLOSE_BRACKET",
        "EditorTabsGroup",
        "Shift Right") { AnActionEvent event ->
    shiftRight.actionPerformed(event)
}

show("Reloaded Tab Shifter plugin")
