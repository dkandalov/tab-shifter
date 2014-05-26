import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.ui.Splitter
import com.intellij.ui.tabs.JBTabs
import socrates.tabshifter.Actions

import javax.swing.*
import java.awt.*
import java.util.List

import static java.lang.Math.max
import static liveplugin.PluginUtil.registerAction
import static liveplugin.PluginUtil.show

// add-to-classpath $HOME/Library/Application Support/IntelliJIdea13/live-plugins/tab-shift/out/production/tab-shifter

if (isIdeStartup) return

class Size {
    final int width
    final int height

    Size(int width, int height) {
        this.width = width
        this.height = height
    }

    @Override String toString() {
        return "Size{" + "width=" + width + ", height=" + height + "}"
    }
}

class Position {
    final int fromX
    final int fromY
    final int toX
    final int toY

    Position(int fromX, int fromY, int toX, int toY) {
        this.fromX = fromX
        this.fromY = fromY
        this.toX = toX
        this.toY = toY
    }

    Position withFromX(int value) {
        new Position(value, fromY, toX, toY)
    }

    Position withFromY(int value) {
        new Position(fromX, value, toX, toY)
    }

    Position withToX(int value) {
        new Position(fromX, fromY, value, toY)
    }

    Position withToY(int value) {
        new Position(fromX, fromY, toX, value)
    }

    @Override
    public String toString() {
        return "(" + fromX + "," + fromY + ")->(" + toX + "," + toY + ")"
    }
}

// based on com.intellij.openapi.fileEditor.impl.EditorsSplitters.getOrderedWindows
Map<Component, Size> sizeOfSplitLayout(JPanel panel) {
    Component component = panel.getComponent(0)
    if (component instanceof Splitter) {
        Splitter splitter = (Splitter) component
        def first = sizeOfSplitLayout((JPanel) splitter.firstComponent)
        def second = sizeOfSplitLayout((JPanel) splitter.secondComponent)
        def firstSize = first.get(splitter.firstComponent.getComponent(0))
        def secondSize = second.get(splitter.secondComponent.getComponent(0))
        def size = splitter.vertical ?
            new Size(max(firstSize.width, secondSize.width), firstSize.height + secondSize.height) :
            new Size(firstSize.width + secondSize.width, max(firstSize.height, secondSize.height))
        def result = new HashMap<>()
        result.put(component, size)
        result.putAll(first)
        result.putAll(second)
        result
    } else if (component instanceof JPanel || component instanceof JBTabs) {
        def result = new HashMap<>()
        result.put(component, new Size(1, 1))
        result
    } else {
        throw new IllegalStateException()
    }
}

List<Position> positionsOfWindows(JPanel panel, Position position, Map<Component, Size> sizeByComponent) {
    Component component = panel.getComponent(0)
    if (component instanceof Splitter) {
        Splitter splitter = (Splitter) component

        def firstSize = sizeByComponent.get(splitter.firstComponent.getComponent(0))
        def secondSize = sizeByComponent.get(splitter.secondComponent.getComponent(0))

        def firstPosition
        def secondPosition
        if (splitter.vertical) {
            firstPosition = position.withToY(position.toY - secondSize.height)
            secondPosition = position.withFromY(position.fromY + firstSize.height)
        } else {
            firstPosition = position.withToX(position.toX - secondSize.width)
            secondPosition = position.withFromX(position.fromX + firstSize.width)
        }
        def first = positionsOfWindows((JPanel) splitter.firstComponent, firstPosition, sizeByComponent)
        def second = positionsOfWindows((JPanel) splitter.secondComponent, secondPosition, sizeByComponent)
        first + second
    } else if (component instanceof JPanel || component instanceof JBTabs) {
        [position]
    } else {
        throw new IllegalStateException()
    }
}

def editorManager = FileEditorManagerEx.getInstanceEx(project)
if (editorManager.splitters.componentCount > 0) {
    JPanel root = (JPanel) editorManager.splitters.getComponent(0)
    def sizeByComponent = sizeOfSplitLayout(root)
    show(sizeByComponent)

    def rootSize = sizeByComponent.get(root.getComponent(0))
    show(positionsOfWindows(
            root,
            new Position(0, 0, rootSize.width, rootSize.height),
            sizeByComponent
    ))
}


return
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
