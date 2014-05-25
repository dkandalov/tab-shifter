package socrates.tabshifter;

import org.junit.Test;

import static javax.swing.SwingConstants.VERTICAL;
import static org.mockito.Mockito.*;

public class TabShifterTest {
    private final Ide ide = mock(Ide.class);
    private final TabShifter tabShifter = new TabShifter(ide);

    @Test public void movingRightTheOnlyTab() {
        stub(ide.currentSplitIndex()).toReturn(0);
        stub(ide.currentSplitTabCount()).toReturn(1);
        stub(ide.splitCount()).toReturn(1);

        tabShifter.moveTabRight();

        verify(ide, never()).createSplitter(anyInt());
        verify(ide, never()).closeCurrentFileInSplit(anyInt());
        verify(ide, never()).setFocusOnSplit(anyInt());
    }

    @Test public void movingTabRightToNewSplitter() {
        stub(ide.currentSplitIndex()).toReturn(0);
        stub(ide.currentSplitTabCount()).toReturn(2);
        stub(ide.splitCount()).toReturn(1);

        tabShifter.moveTabRight();

        verify(ide).createSplitter(VERTICAL);
        verify(ide).closeCurrentFileInSplit(0);
        verify(ide).setFocusOnSplit(1);
    }

    @Test public void movingTabRightToExistingSplitter() {
        stub(ide.currentSplitIndex()).toReturn(0);
        stub(ide.currentSplitTabCount()).toReturn(2);
        stub(ide.splitCount()).toReturn(2);

        tabShifter.moveTabRight();

        verify(ide).closeCurrentFileInSplit(0);
        verify(ide).setFocusOnSplit(1);
        verify(ide).reopenMovedTab();
    }

    @Test public void movingLeftTheOnlyTab() {
        stub(ide.currentSplitIndex()).toReturn(0);
        stub(ide.currentSplitTabCount()).toReturn(1);
        stub(ide.splitCount()).toReturn(0);

        tabShifter.moveTabLeft();

        verify(ide, never()).createSplitter(anyInt());
        verify(ide, never()).closeCurrentFileInSplit(anyInt());
        verify(ide, never()).setFocusOnSplit(anyInt());
    }

    @Test public void movingLeftToExistingSplitter() {
        stub(ide.splitCount()).toReturn(2);
        stub(ide.currentSplitIndex()).toReturn(0);
        stub(ide.currentSplitTabCount()).toReturn(2);

        tabShifter.moveTabLeft();

        verify(ide).closeCurrentFileInSplit(0);
        verify(ide).setFocusOnSplit(1);
        verify(ide).reopenMovedTab();
    }
}