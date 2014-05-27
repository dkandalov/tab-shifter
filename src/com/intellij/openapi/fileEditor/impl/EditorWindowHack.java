package com.intellij.openapi.fileEditor.impl;

import javax.swing.*;

public class EditorWindowHack extends EditorWindow {
    protected EditorWindowHack(EditorsSplitters editorsSplitters) {
        super(editorsSplitters);
    }

    public static JPanel panelOf(EditorWindow editorWindow) {
        return editorWindow.myPanel;
    }
}
