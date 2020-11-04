package tabshifter

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.impl.EditorWindow
import java.lang.reflect.Field
import javax.swing.JPanel

object EditorWindow_AccessToPanel_Hack {
    private val logger = Logger.getInstance(TabShifter::class.java.name)

    fun panelOf(editorWindow: EditorWindow?): JPanel? {
        val panelField: Field
        try {
            panelField = EditorWindow::class.java.getDeclaredField("myPanel")
            panelField.isAccessible = true
        } catch (e: NoSuchFieldException) {
            logger.warn("Couldn't find myPanel field")
            return null
        }
        return try {
            panelField[editorWindow] as JPanel
        } catch (e: IllegalAccessException) {
            logger.warn("Couldn't access myPanel field", e)
            null
        }
    }
}