package com.intellij.openapi.fileEditor.impl;

import javax.swing.*;

public class EditorWindow_AccessPanel_Hack extends EditorWindow {
    protected EditorWindow_AccessPanel_Hack(EditorsSplitters editorsSplitters) {
        super(editorsSplitters);
    }

    public static JPanel panelOf(EditorWindow editorWindow) {
        return editorWindow.myPanel;
    }
}
