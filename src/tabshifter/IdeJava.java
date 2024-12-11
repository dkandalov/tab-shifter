package tabshifter;

import java.util.Arrays;

import javax.swing.SwingUtilities;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;

public class IdeJava {
        
        public static boolean isFocusOnEditor(Ide ide) {
                var manager = ide.getManager();
                
                // Get the currently focused component
                var component = IdeFocusManager.getGlobalInstance().getFocusOwner();
                if (component == null) return false;
                
                return Arrays.stream(manager.getSelectedEditors()).anyMatch(editor -> SwingUtilities.isDescendingFrom(component, editor.getComponent()));
        }
        
        public static void activateRecentToolWindow(Ide ide, Direction direction, boolean mustBeVisible) {
                var anchor = toAnchor(direction);
                
                ToolWindowManager manager = ToolWindowManager.getInstance(ide.getProject());
                                        
                ToolWindow window = null;
                
                for (String id : manager.getToolWindowIds()) {
                        window = manager.getToolWindow(id);
                        
                        if ( window != null && window.isVisible() && window.getAnchor() == anchor ) {
                                window.activate(null); return; 
                        }
                }
        }
        
        
        public static boolean activateRecentToolWindow(Ide ide, Direction direction) {
                var anchor = toAnchor(direction);
                
                Project project = ide.getProject();
                
                return ToolboxTracker.getInstance(project).activate(anchor);
        }
        
        public static void hideToolWindow(Ide ide, Direction direction, String id) {
                var anchor = toAnchor(direction);
                
                var project = ide.getProject();
                
                ToolboxTracker.getInstance(project).hide(id);
        }
        
        public static boolean activateRecentEditorConditioned(Ide ide, Direction direction) {
                var anchor = toAnchorReversed(direction);
                
                var window = getActiveToolWindow(ide);
                
                if ( window != null ) {
                        
                        if ( window.getAnchor() == anchor ) {
                                activateRecentActiveEditor(ide); return true;
                        }
                }
                
                return false;
        }
        
        public static void activateRecentActiveEditor(Ide ide) {
                var manager = ide.getManager();
                
                VirtualFile file = manager.getCurrentFile();
                
                if (file == null) return;
                
                manager.openFile(file, true);
        }
        
        private static ToolWindow getActiveToolWindow(Ide ide) {
                ToolWindowManager manager = ToolWindowManager.getInstance(ide.getProject());
                
                var id = manager.getActiveToolWindowId();
                if ( id != null ) {
                        ToolWindow window = manager.getToolWindow(id);
                        
                        if (window != null && window.isVisible() ) {
                                return window;
                        }
                }
                
                return null;
        }
        
        private static ToolWindowAnchor toAnchor(Direction direction) {
                if ( direction == Direction.left ) {
                        return ToolWindowAnchor.LEFT;
                } else if ( direction == Direction.right) {
                        return ToolWindowAnchor.RIGHT;
                } else if ( direction == Direction.down) {
                        return ToolWindowAnchor.BOTTOM;
                }
                else if ( direction == Direction.up) {
                        return ToolWindowAnchor.TOP;
                }
                
                return null; /* not really possible */
        }
        
        private static ToolWindowAnchor toAnchorReversed(Direction direction) {
                if ( direction == Direction.left ) {
                        return ToolWindowAnchor.RIGHT;
                } else if ( direction == Direction.right) {
                        return ToolWindowAnchor.LEFT;
                } else if ( direction == Direction.down) {
                        return ToolWindowAnchor.TOP;
                }
                else if ( direction == Direction.up) {
                        return ToolWindowAnchor.BOTTOM;
                }
                
                return null; /* not really possible */
        }
        
}
