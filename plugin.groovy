import com.intellij.openapi.actionSystem.AnActionEvent
import tabshifter.Actions

import static liveplugin.PluginUtil.registerAction
import static liveplugin.PluginUtil.show

// add-to-classpath $HOME/Library/Application Support/IntelliJIdea15/live-plugins/tab-shift/out/production/tab-shifter/

if (isIdeStartup) return

registerAction("TabShiftActions.ShiftRight",
		"ctrl alt CLOSE_BRACKET",
		"EditorTabsGroup",
		"Shift Right") { AnActionEvent event ->
	new Actions.ShiftRight().actionPerformed(event)
}
registerAction("TabShiftActions.ShiftLeft",
        "ctrl alt OPEN_BRACKET",
        "EditorTabsGroup",
        "Shift Left") { AnActionEvent event ->
    new Actions.ShiftLeft().actionPerformed(event)
}
registerAction("TabShiftActions.ShiftUp",
        "ctrl alt P",
        "EditorTabsGroup",
        "Shift Up") { AnActionEvent event ->
    new Actions.ShiftUp().actionPerformed(event)
}
registerAction("TabShiftActions.ShiftDown",
		"ctrl alt SEMICOLON",
		"EditorTabsGroup",
		"Shift Down") { AnActionEvent event ->
	new Actions.ShiftDown().actionPerformed(event)
}

show("Reloaded Tab Shifter plugin")
