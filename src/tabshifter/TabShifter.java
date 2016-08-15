package tabshifter;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Condition;
import com.intellij.ui.JBSplitter;
import org.jetbrains.annotations.Nullable;
import tabshifter.valueobjects.LayoutElement;
import tabshifter.valueobjects.Position;
import tabshifter.valueobjects.Split;
import tabshifter.valueobjects.Window;

import javax.swing.*;
import java.util.Optional;

import com.intellij.openapi.fileEditor.impl.EditorWindow;
import static tabshifter.EditorWindow_AccessToPanel_Hack.panelOf;
import static com.intellij.util.containers.ContainerUtil.find;
import static tabshifter.valueobjects.Split.Orientation.vertical;

public class TabShifter {
    public static final TabShifter none = new TabShifter(null) {
        @Override public void moveFocus(Directions.Direction direction) {}
        @Override public void moveTab(Directions.Direction direction) {}
    };
    private static final Logger logger = Logger.getInstance(TabShifter.class.getName());
    private final Ide ide;


    public TabShifter(Ide ide) {
        this.ide = ide;
    }

    public void moveFocus(Directions.Direction direction) {
        LayoutElement layout = calculateAndSetPositions(ide.snapshotWindowLayout());
        if (layout == LayoutElement.none) return;

        Window window = currentWindowIn(layout);
        if (window == null) return;

        Window targetWindow = direction.findTargetWindow(window, layout);
        if (targetWindow == null) return;

        ide.setFocusOn(targetWindow);
    }

    /**
     * Moves tab in the specified direction.
     *
     * This is way more complicated than it should have been. The main reasons are:
     *  - closing/opening or opening/closing tab doesn't guarantee that focus will be in the moved tab
     *      => need to track target window to move focus into it
     *  - EditorWindow object changes its identity after split/unsplit (i.e. points to another visual window)
     *      => need to predict target window position and look up window by expected position
     *
     */
    public void moveTab(Directions.Direction direction) {
        LayoutElement layout = calculateAndSetPositions(ide.snapshotWindowLayout());
        if (layout == LayoutElement.none) return;
        Window window = currentWindowIn(layout);
        if (window == null) return;

        Window targetWindow = direction.findTargetWindow(window, layout);

        Position newPosition;

        boolean isAtEdge = (targetWindow == null);
        if (isAtEdge) {
            if (window.hasOneTab || !direction.canExpand()) return;

            LayoutElement newLayout = insertSplit(direction.splitOrientation(), window, layout);
            calculateAndSetPositions(newLayout);
            LayoutElement sibling = findSiblingOf(window, newLayout);
            if (sibling == null) return; // should never happen
            newPosition = sibling.position;

            ide.createSplitter(direction.splitOrientation());
            ide.closeCurrentFileIn(window);

        } else {
            boolean willBeUnsplit = window.hasOneTab;
            if (willBeUnsplit) {
                LayoutElement unsplitLayout = removeFrom(layout, window);
                calculateAndSetPositions(unsplitLayout);
            }
            newPosition = targetWindow.position;

            ide.openCurrentFileIn(targetWindow);
            ide.closeCurrentFileIn(window);
        }

        LayoutElement newWindowLayout = calculateAndSetPositions(ide.snapshotWindowLayout());
        targetWindow = findWindowBy(newPosition, newWindowLayout);

        if (targetWindow == null) {
            // ideally this should never happen, logging in case something goes wrong
            logger.warn("No window for: " + newPosition);
        } else {
            ide.setFocusOn(targetWindow);
        }
    }



    public static JPanel findPanel(LayoutElement element) {

        JPanel panel = null;
        while (element != null) {
            if (element instanceof Split) {
                element = ((Split) element).first;
            } else if (element instanceof Ide.IdeWindow) {
                panel = panelOf(((Ide.IdeWindow) element).editorWindow);
                break;
            } else {
                throw new RuntimeException("Unknown element type: " + element.getClass().getName());
            }
        }

        return panel;



    }

    public void grow(Directions.Direction direction) {
        LayoutElement layout = calculateAndSetPositions(ide.snapshotWindowLayout());

        if (layout instanceof Split) {
            Split split = (Split) layout;
            Window window = currentWindowIn(split);
            JPanel panel = panelOf(((Ide.IdeWindow) window).editorWindow);

            JBSplitter splitter = (JBSplitter) panel.getParent();
            boolean hasParentSplitter = splitter.getParent().getParent() instanceof JBSplitter;
            boolean isFirst = splitter.getFirstComponent() == panel;


            float increment = 0.05f;
            if (direction == Directions.left)
                increment *= -1f;

            if (hasParentSplitter && (
                    (isFirst && direction == Directions.left) ||
                            (!isFirst && direction == Directions.right))) {

                JBSplitter parentSplitter = (JBSplitter) splitter.getParent().getParent();
                float proportion = parentSplitter.getProportion() + increment;
                float  newProportion = Math.min(Math.max(proportion,0),1);

                parentSplitter.setProportion(newProportion);

                return;
            }
            float proportion = splitter.getProportion() + increment;
            float  newProportion = Math.min(Math.max(proportion,0),1);

            splitter.setProportion(newProportion);



        }

        if (layout == LayoutElement.none)
            return;



        Window window = currentWindowIn(layout);
        if (window == null)
            return;




        //Window targetWindow = direction.findTargetWindow(window, layout);
        LayoutElement sibling = findSiblingOf(window, layout);

        // Log the current position
        logger.info(
                String.format(
                        "Target position %s / size %s\nsibling position %s / size %s",
                        window.position.toString(),
                        window.size().toString(),
                        (sibling == null) ? "None" :
                                sibling.position.toString(),
                        (sibling == null) ? "None" :
                                sibling.size().toString()));
        


    }


    @Nullable private static Window currentWindowIn(LayoutElement windowLayout) {
        return find(Directions.allWindowsIn(windowLayout), new Condition<Window>() {
            @Override public boolean value(Window window) {
                return window.isCurrent;
            }
        });
    }

    private static LayoutElement findSiblingOf(Window window, LayoutElement element) {
        if (element instanceof Split) {
            Split split = (Split) element;

            if (split.first.equals(window)) return split.second;
            if (split.second.equals(window)) return split.first;

            LayoutElement first = findSiblingOf(window, split.first);
            if (first != null) return first;

            LayoutElement second = findSiblingOf(window, split.second);
            if (second != null) return second;

            return null;

        } else if (element instanceof Window) {
            return null;

        } else {
            throw new IllegalStateException();
        }
    }

    private static LayoutElement calculateAndSetPositions(LayoutElement element) {
        return calculateAndSetPositions(element, new Position(0, 0, element.size().width, element.size().height));
    }

    private static LayoutElement calculateAndSetPositions(LayoutElement element, Position position) {
        if (element instanceof Split) {
            Split split = (Split) element;

            Position firstPosition;
            Position secondPosition;
            if (split.orientation == vertical) {
                firstPosition = position.withToX(position.toX - split.second.size().width);
                secondPosition = position.withFromX(position.fromX + split.first.size().width);
            } else {
                firstPosition = position.withToY(position.toY - split.second.size().height);
                secondPosition = position.withFromY(position.fromY + split.first.size().height);
            }
            calculateAndSetPositions(split.first, firstPosition);
            calculateAndSetPositions(split.second, secondPosition);
        }

        element.position = position;
        return element;
    }




    @Nullable private static Window findWindowBy(final Position position, LayoutElement layout) {
        return find(Directions.allWindowsIn(layout), new Condition<Window>() {
            @Override
            public boolean value(Window window) {
                return position.equals(window.position);
            }
        });
    }


    private static LayoutElement removeFrom(LayoutElement element, Window window) {
        if (element instanceof Split) {
            Split split = (Split) element;
            LayoutElement first = removeFrom(split.first, window);
            LayoutElement second = removeFrom(split.second, window);

            if (first == null) return second;
            else if (second == null) return first;
            else return new Split(first, second, split.orientation);

        } else if (element instanceof Window) {
            return element.equals(window) ? null : element;

        } else {
            throw new IllegalStateException();
        }
    }

    private static LayoutElement insertSplit(Split.Orientation orientation, Window window, LayoutElement element) {
        if (element instanceof Split) {
            Split split = (Split) element;
            return new Split(
                    insertSplit(orientation, window, split.first),
                    insertSplit(orientation, window, split.second),
                    split.orientation);
        } else if (element instanceof Window) {
            if (element.equals(window)) {
                return new Split(window, new Window(true, false), orientation);
            } else {
                return element;
            }
        } else {
            throw new IllegalStateException();
        }
    }
}
