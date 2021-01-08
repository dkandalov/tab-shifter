package tabshifter

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.project.DumbAware

object Actions {
    private fun tabShifter(event: AnActionEvent): TabShifter? {
        val project = event.project ?: return null
        val editorManager = FileEditorManagerEx.getInstanceEx(project) ?: return null
        if (editorManager.allEditors.isEmpty()) return null
        return TabShifter(Ide(editorManager, project))
    }

    class ShiftLeft: AnAction(), DumbAware {
        override fun actionPerformed(event: AnActionEvent) {
            tabShifter(event)?.moveTab(left)
        }
    }

    class ShiftUp: AnAction(), DumbAware {
        override fun actionPerformed(event: AnActionEvent) {
            tabShifter(event)?.moveTab(up)
        }
    }

    class ShiftRight: AnAction(), DumbAware {
        override fun actionPerformed(event: AnActionEvent) {
            tabShifter(event)?.moveTab(right)
        }
    }

    class ShiftDown: AnAction(), DumbAware {
        override fun actionPerformed(event: AnActionEvent) {
            tabShifter(event)?.moveTab(down)
        }
    }

    class MoveFocusLeft: AnAction(), DumbAware {
        override fun actionPerformed(event: AnActionEvent) {
            tabShifter(event)?.moveFocus(left)
        }
    }

    class MoveFocusUp: AnAction(), DumbAware {
        override fun actionPerformed(event: AnActionEvent) {
            tabShifter(event)?.moveFocus(up)
        }
    }

    class MoveFocusRight: AnAction(), DumbAware {
        override fun actionPerformed(event: AnActionEvent) {
            tabShifter(event)?.moveFocus(right)
        }
    }

    class MoveFocusDown: AnAction(), DumbAware {
        override fun actionPerformed(event: AnActionEvent) {
            tabShifter(event)?.moveFocus(down)
        }
    }

    class StretchRight: AnAction(), DumbAware {
        override fun actionPerformed(event: AnActionEvent) {
            tabShifter(event)?.stretchSplitter(right)
        }
    }

    class StretchLeft: AnAction(), DumbAware {
        override fun actionPerformed(event: AnActionEvent) {
            tabShifter(event)?.stretchSplitter(left)
        }
    }

    class StretchUp: AnAction(), DumbAware {
        override fun actionPerformed(event: AnActionEvent) {
            tabShifter(event)?.stretchSplitter(up)
        }
    }

    class StretchDown: AnAction(), DumbAware {
        override fun actionPerformed(event: AnActionEvent) {
            tabShifter(event)?.stretchSplitter(down)
        }
    }

    class EqualSizeSplit: AnAction(), DumbAware {
        override fun actionPerformed(event: AnActionEvent) {
            tabShifter(event)?.equalSizeSplitter()
        }
    }

    class ToggleMaximizeRestore: AnAction(), DumbAware {
        override fun actionPerformed(event: AnActionEvent) {
            tabShifter(event)?.toggleMaximizeRestoreSplitter()
        }
    }
}