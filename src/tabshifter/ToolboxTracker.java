package tabshifter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;

public class ToolboxTracker {
        private static Map<Project, ToolboxTracker> PROJECTS = new ConcurrentHashMap<>();
        
        public static ToolboxTracker getInstance(Project project) {
                return PROJECTS.computeIfAbsent(project, k -> new ToolboxTracker(project));
        }
        
        /////////////////////////////////////////////////////////////////////
        
        private final Project project;
        private final Map<String, String> byAnchors = new ConcurrentHashMap<>();
        private final Map<String, Info>   byId      = new ConcurrentHashMap<>();
        
        private ToolboxTracker(Project project) {
                this.project = project;
                
                init();
        }
        
        /**
         * Prepopulate the map with existing visible tool windows on startup
         */
        private void init() {
                for (String id : getManager().getToolWindowIds()) {
                        trackToolbox(id);
                }
        }
        
        private ToolWindowManager getManager() {
                return ToolWindowManager.getInstance(this.project);
        }
        
        public void trackToolbox(String id) {
                if ( id != null ) {
                        ToolWindow window = getManager().getToolWindow(id);
                        
                        if ( window != null ) {
                        
                                if ( window.isVisible() ) {
                                        byAnchors.put(window.getAnchor().toString(), id);
                                }
                                
                        }
                }
        }
        
        public String getLastActiveToolwindowForAnchor(ToolWindowAnchor anchor) {
                return byAnchors.get(anchor.toString());
        }
        
        public boolean activate(ToolWindowAnchor anchor) {
                String lastId = getLastActiveToolwindowForAnchor(anchor);
                
                if (lastId != null) {
                
                        ToolWindow window = getManager().getToolWindow(lastId);
                        
                        if ( window != null ) {
                        
                                var wasUs = wasUs(window);
                                
                                window.activate(() -> {
                                        getInfo(lastId).wasUs = wasUs;
                                });
                                
                                return true;
                        }
                }
                
                return false;
        }
        
        void hide(String lastId) {
                if ( Boolean.TRUE.equals(getInfo(lastId).wasUs) ) {
                        getManager().getToolWindow(lastId).hide();
                }
        }
        
        private boolean wasUs(ToolWindow window) {
                return !window.isVisible();
        }
        
        private Info getInfo(String lastId) {
                return byId.computeIfAbsent(lastId, (id) -> new Info());
        }
        
        public static final class Info {
                Boolean wasUs;
        }
}
