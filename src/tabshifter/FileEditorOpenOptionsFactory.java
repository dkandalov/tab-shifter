package tabshifter;

import com.intellij.openapi.fileEditor.impl.FileEditorOpenOptions;

final class FileEditorOpenOptionsFactory {
    private FileEditorOpenOptionsFactory() {}

    static FileEditorOpenOptions create() {
        // Java calls the zero-argument JVM constructor. Kotlin calls a synthetic
        // default-argument constructor whose signature changes when IDEA adds options.
        return new FileEditorOpenOptions();
    }
}
