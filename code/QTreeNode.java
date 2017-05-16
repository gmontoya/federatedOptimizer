import java.io.Serializable;
import java.util.TreeSet;
import java.util.HashSet;

abstract class QTreeNode implements Serializable {
    protected Bucket bucket;
    protected double penalty = Double.MAX_VALUE;
    //protected int numBuckets = 0;
    protected QTreeNode parent;

    public abstract boolean equals(Object o);

    /*public void increaseNumBuckets(int n) {
        numBuckets += n;
        if (parent != null) {
            parent.increaseNumBuckets(n);
        }
    }*/

    /*public void decreaseNumBuckets(int n) {
        numBuckets -= n;
        if (parent != null) {
            parent.decreaseNumBuckets(n);
        }
    }*/

    public Bucket getBucket() {
        return bucket;
    }

    public abstract TreeSet<Bucket> getBuckets();

    public double getPenalty() {
        return penalty;
    }

    public abstract String toString();
    public abstract void checkInvariant();
    protected abstract String pprint(String i);
    public abstract HashSet<Short> getValues();
    
}
