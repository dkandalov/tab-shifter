package tabshifter;

import com.intellij.openapi.fileEditor.impl.EditorWindow;

import static java.lang.Integer.toHexString;

public class Window extends LayoutElement {
    public final EditorWindow editorWindow;
    public final boolean hasOneTab;
    public final boolean isCurrent;

    public Window(EditorWindow editorWindow, boolean hasOneTab, boolean isCurrent) {
        this.editorWindow = editorWindow;
        this.hasOneTab = hasOneTab;
        this.isCurrent = isCurrent;
    }

    @Override public Size size() {
        return new Size(1, 1);
    }

    @Override public String toString() {
        return "Window-" + toHexString(hashCode());
    }
}
