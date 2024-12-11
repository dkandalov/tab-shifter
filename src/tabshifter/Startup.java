package tabshifter;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import org.jetbrains.annotations.NotNull;

public class Startup implements StartupActivity {
        @Override
        public void runActivity(@NotNull Project project) {
                
                new Thread(() -> {
                        
                        try {
                                Thread.sleep(250);
                                
                                ToolboxTracker.getInstance(project);
                        }
                        catch(InterruptedException ignore) {}
                        
                }).start();
                
                project.getMessageBus()
                       .connect()
                       .subscribe(ToolWindowManagerListener.TOPIC, new ToolWindowManagerListener() {
                               
                               @Override public void stateChanged(ToolWindowManager manager) {
                                       
                                       ToolboxTracker.getInstance(project).track(manager.getLastActiveToolWindowId());
                               }
                               
                       })
                ;
        }
}
