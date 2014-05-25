package socrates.tabshifter;

import javax.swing.*;

public class TabShifter {
    private final Ide ide;

    public TabShifter(Ide ide) {
        this.ide = ide;
    }

    public void moveTabLeft() {
        if (ide.splitCount() == 1) {
            moveTabRight();
            return;
        }
        int splitIndex = ide.currentSplit();

        boolean isLeftmostSplit = (splitIndex == 0);
        if (isLeftmostSplit && ide.currentSplitTabCount() == 1) return;

        int nexIndex = wrap(splitIndex - 1, ide.splitCount());
        ide.closeCurrentFileInSplit(splitIndex);
        ide.setFocusOnSplit(nexIndex);
        ide.reopenMovedTab();
    }

    public void moveTabRight() {
        if (ide.splitCount() == 1 && ide.currentSplitTabCount() == 1) return;
        int splitIndex = ide.currentSplit();

        boolean isRightmostSplit = (splitIndex == ide.splitCount() - 1);
        if (isRightmostSplit && ide.currentSplitTabCount() == 1) return;

        if (isRightmostSplit) {
            ide.createSplitter(SwingConstants.VERTICAL);
            ide.closeCurrentFileInSplit(splitIndex);
            ide.setFocusOnSplit(splitIndex + 1);
        } else {
            boolean wasTheOnlyTab = (ide.currentSplitTabCount() == 1);
            int shift = (wasTheOnlyTab ? 0 : 1);
            ide.closeCurrentFileInSplit(splitIndex);
            ide.setFocusOnSplit(splitIndex + shift);
            ide.reopenMovedTab();
        }
    }

    private static int wrap(int value, int ceiling) {
        return (value + ceiling) % ceiling;
    }
}
