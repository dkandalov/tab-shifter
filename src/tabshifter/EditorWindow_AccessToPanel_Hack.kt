package tabshifter

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.impl.EditorWindow
import javax.swing.JPanel

object EditorWindow_AccessToPanel_Hack {
    private val logger = Logger.getInstance(TabShifter::class.java.name)

    fun panelOf(editorWindow: EditorWindow): JPanel? =
        try {
            val panelField = try {
                EditorWindow::class.java.getDeclaredField("panel")
            } catch (e: NoSuchFieldException) {
                EditorWindow::class.java.getDeclaredField("myPanel")
            }
            panelField.isAccessible = true
            panelField[editorWindow] as JPanel
        } catch (e: NoSuchFieldException) {
            logger.warn("Couldn't find panel field")
            null
        } catch (e: IllegalAccessException) {
            logger.warn("Couldn't access panel field", e)
            null
        }
}