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

        boolean isLeftmostSplit = (ide.currentSplit() == 0);
        if (isLeftmostSplit && ide.currentSplitTabCount() == 1) return;

        int nexIndex = wrap(ide.currentSplit() - 1, ide.splitCount());
        ide.closeCurrentFileInSplit(ide.currentSplit());
        ide.setFocusOnSplit(nexIndex);
        ide.reopenCurrentFile();
    }

    public void moveTabRight() {
        if (ide.splitCount() == 1 && ide.currentSplitTabCount() == 1) return;

        boolean isRightmostSplit = (ide.currentSplit() == ide.splitCount() - 1);
        if (isRightmostSplit && ide.currentSplitTabCount() == 1) return;

        if (isRightmostSplit) {
            ide.createSplitter(SwingConstants.VERTICAL);
            ide.closeCurrentFileInSplit(ide.currentSplit());
            ide.setFocusOnSplit(ide.currentSplit() + 1);
        } else {
            boolean wasTheOnlyTab = (ide.currentSplitTabCount() == 1);
            int shift = (wasTheOnlyTab ? 0 : 1);
            ide.closeCurrentFileInSplit(ide.currentSplit());
            ide.setFocusOnSplit(ide.currentSplit() + shift);
            ide.reopenCurrentFile();
        }
    }

    private static int wrap(int value, int ceiling) {
        return (value + ceiling) % ceiling;
    }
}
