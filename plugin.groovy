import com.intellij.openapi.actionSystem.AnActionEvent
import tabshifter.Actions

import static liveplugin.PluginUtil.registerAction
import static liveplugin.PluginUtil.show

// add-to-classpath $HOME/Library/Application Support/IntelliJIdea13/live-plugins/tab-shift/out/production/tab-shifter

if (isIdeStartup) return

registerAction("TabShiftActions.ShiftLeft",
        "alt shift OPEN_BRACKET",
        "EditorTabsGroup",
        "Shift Left") { AnActionEvent event ->
    new Actions.ShiftLeft().actionPerformed(event)
}
registerAction("TabShiftActions.ShiftRight",
        "alt shift CLOSE_BRACKET",
        "EditorTabsGroup",
        "Shift Right") { AnActionEvent event ->
    new Actions.ShiftRight().actionPerformed(event)
}
registerAction("TabShiftActions.ShiftDown",
        "alt shift SEMICOLON",
        "EditorTabsGroup",
        "Shift Down") { AnActionEvent event ->
    new Actions.ShiftDown().actionPerformed(event)
}
registerAction("TabShiftActions.ShiftUp",
        "alt shift P",
        "EditorTabsGroup",
        "Shift Up") { AnActionEvent event ->
    new Actions.ShiftUp().actionPerformed(event)
}

show("Reloaded Tab Shifter plugin")
