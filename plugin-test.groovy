import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import liveplugin.testrunner.IntegrationTestsRunner
import org.junit.Test

IntegrationTestsRunner.runIntegrationTests([TabShifterIntegrationTest], project)

class TabShifterIntegrationTest {
    @Test void "TODO"() {

    }

    TabShifterIntegrationTest(Map context) {
        editorManager = FileEditorManagerEx.getInstanceEx(context.project)
    }

    private final FileEditorManagerEx editorManager
}