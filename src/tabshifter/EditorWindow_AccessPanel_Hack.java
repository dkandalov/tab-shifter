package tabshifter;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.impl.EditorWindow;

import javax.swing.*;
import java.lang.reflect.Field;

public class EditorWindow_AccessPanel_Hack {
    private static final Logger logger = Logger.getInstance(TabShifter.class.getName());

    public static JPanel panelOf(EditorWindow editorWindow) {
        Field panelField;
        try {
            panelField = EditorWindow.class.getDeclaredField("myPanel");
            panelField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            logger.warn("Could find myPanel field");
            return null;
        }
        try {
            return (JPanel) panelField.get(editorWindow);
        } catch (IllegalAccessException e) {
            logger.warn("Couldn't access myPanel field", e);
            return null;
        }
    }
}
