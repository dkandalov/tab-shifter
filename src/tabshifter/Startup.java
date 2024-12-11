package tabshifter;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.FocusEvent;

import com.intellij.ide.IdeEventQueue;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import org.jetbrains.annotations.NotNull;

public final class Startup implements StartupActivity {
        
        private static final Logger LOG = Logger.getInstance(Startup.class);
        
        
        @Override public void runActivity(@NotNull Project project) {
                
                ToolboxTracker.getInstance(project);
                
                project.getMessageBus().connect().subscribe(ToolWindowManagerListener.TOPIC, new ToolWindowManagerListener() {
                        
                        @Override public void stateChanged(ToolWindowManager manager) {
                                System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
                                ToolboxTracker.getInstance(project).trackToolbox(manager.getLastActiveToolWindowId());
                        }
                        
                });
                
                project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
                        
                        @Override public void selectionChanged(FileEditorManagerEvent event) {
                                System.out.println("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
                                
                                ToolWindowManager manager = ToolWindowManager.getInstance(project);
                                
                                ToolboxTracker.getInstance(project).trackToolbox(manager.getLastActiveToolWindowId());
                        }
                });
                
                
                if ( false ) {
                        // Test did not work
                        
                        IdeEventQueue.getInstance().addPostprocessor(new IdeEventQueue.EventDispatcher() {
                                @Override public boolean dispatch(@NotNull AWTEvent awtEvent) {
                                        
                                        if ( awtEvent instanceof FocusEvent ) {
                                                FocusEvent focusEvent       = (FocusEvent) awtEvent;
                                                Component  focusedComponent = focusEvent.getComponent();
                                                if ( focusedComponent != null ) {
                                                        
                                                        String componentName = focusedComponent.getName();
                                                        
                                                        
                                                        if ( componentName != null && componentName.contains("EditorComponent") ) {
                                                                System.out.println("Focus switched to editor.");
                                                                ToolboxTracker.getInstance(project).trackToolbox("editor");
                                                        }
                                                        else {
                                                                System.out.println("Focus switched to: " + componentName);
                                                        }
                                                }
                                        }
                                        return false; // Let other listeners process the event.
                                }
                        }, new Disposable() {
                                @Override public void dispose() {
                                        
                                        
                                }
                        });
                }
        }
}
