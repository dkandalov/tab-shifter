package tabshifter

import com.intellij.openapi.fileEditor.impl.EditorWindow
import javax.swing.JComponent
import kotlin.reflect.full.declaredMemberProperties

object EditorWindow_AccessToPanel_Hack {
    fun panelOf(editorWindow: EditorWindow): JComponent =
        EditorWindow::class.declaredMemberProperties.find { it.name == "component" }?.get(editorWindow) as JComponent
}