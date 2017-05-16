import java.util.Comparator;
import java.io.Serializable;

public class QTreeMinComparator implements Comparator, Serializable {

    @Override
    public int compare(Object o1, Object o2) {
        QTreeNode qt1 = (QTreeNode) o1;
        QTreeNode qt2 = (QTreeNode) o2;
        
        int c = 0;
        if (qt1.getBucket().getMin() < qt2.getBucket().getMin() || ((qt1.getBucket().getMin() == qt2.getBucket().getMin())&& qt1.getBucket().getMax()< qt2.getBucket().getMax()) ) {
            c = -1;
        } else if (qt1.getBucket().getMin() > qt2.getBucket().getMin() || ((qt1.getBucket().getMin() == qt2.getBucket().getMin())&& qt1.getBucket().getMax()> qt2.getBucket().getMax()) ) {
            c = 1;
        }
        return c;
    }
}
