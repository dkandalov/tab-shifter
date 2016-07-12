import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import tabshifter.Actions

import static liveplugin.PluginUtil.registerAction
import static liveplugin.PluginUtil.show

// add-to-classpath $PLUGIN_PATH/out/production/tab-shifter/

if (isIdeStartup) return

static register(String id, String shortcut, String text, AnAction action) {
	registerAction("TabShiftActions.${id}", shortcut, "EditorTabsGroup", text) { AnActionEvent event ->
		action.actionPerformed(event)
	}
}

register("ShiftLeft", "ctrl alt OPEN_BRACKET", "Shift Left", new Actions.ShiftLeft())
register("ShiftUp", "ctrl alt P", "Shift Up", new Actions.ShiftUp())
register("ShiftRight", "ctrl alt CLOSE_BRACKET", "Shift Right", new Actions.ShiftRight())
register("ShiftDown", "ctrl alt SEMICOLON", "Shift Down", new Actions.ShiftDown())

register("MoveFocusLeft", "ctrl alt shift OPEN_BRACKET", "Move Focus Left", new Actions.MoveFocusLeft())
register("MoveFocusUp", "ctrl alt shift P", "Move Focus Up", new Actions.MoveFocusUp())
register("MoveFocusRight", "ctrl alt shift CLOSE_BRACKET", "Move Focus Right", new Actions.MoveFocusRight())
register("MoveFocusDown", "ctrl alt shift SEMICOLON", "Move Focus Down", new Actions.MoveFocusDown())

show("Reloaded Tab Shifter plugin")
