import java.util.Comparator;
import java.io.Serializable;

public class QTreePenaltyComparator implements Comparator, Serializable {

    @Override
    public int compare(Object o1, Object o2) {
        QTreeNode qt1 = (QTreeNode) o1;
        QTreeNode qt2 = (QTreeNode) o2;
        int c = 0;
        if (qt1.getPenalty() - qt2.getPenalty() < 0) {
            c = -1;
        } else if (qt1.getPenalty() - qt2.getPenalty() > 0) {
            c = 1;
        }
        return c;
    }
}
