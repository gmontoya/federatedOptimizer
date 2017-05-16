import java.util.Comparator;
import java.io.Serializable;

public class BucketMinComparator implements Comparator, Serializable {

    @Override
    public int compare(Object o1, Object o2) {
        Bucket b1 = (Bucket) o1;
        Bucket b2 = (Bucket) o2;
        
        int c = 0;
        if (b1.getMin() < b2.getMin() || ((b1.getMin() == b2.getMin())&& b1.getMax()< b2.getMax()) ) {
            c = -1;
        } else if (b1.getMin() > b2.getMin() || ((b1.getMin() == b2.getMin())&& b1.getMax()> b2.getMax()) ) {
            c = 1;
        }
        return c;
    }
}
