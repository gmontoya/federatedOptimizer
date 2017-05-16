import java.util.TreeSet;
import java.util.HashSet;

class QTreeLeaf extends QTreeNode {
    HashSet<Short> values;
    public QTreeLeaf(QTreeNode p, int n) {
        this(p, new Bucket(n, n, 1));
        values = new HashSet<Short>();
        short s = (new Integer(n % 65536)).shortValue();
        values.add(s);
    }
    public HashSet<Short> getValues() {
        return values;
    }

    protected void setValues(HashSet<Short> vs) {
        values = vs;
    }

    public QTreeLeaf(QTreeNode p, Bucket b) {
        //this(p, b);
        //values = new HashSet<Short>();
        parent = p;
        bucket = b;
        values = new HashSet<Short>();
        //numBuckets = 1;
        /*if (parent != null) {
            parent.increaseNumBuckets(1);
        }*/
    }

    public void addValue(int v) {
        short s = (new Integer(v % 65536)).shortValue();
        //System.out.println("(QTreeLeaf) going to add "+s+" to "+this);
        values.add(s);
        bucket.increaseCount();
        /*if (parent != null) {
            parent.addOneValue();
        }*/
    }

    public boolean equals(Object o) {
        if ((o == null) || !(o instanceof QTreeLeaf)) {
            return false;
        }
        QTreeLeaf l = (QTreeLeaf) o;
        return bucket.equals(l.bucket);
    }

    public TreeSet<Bucket> getBuckets() {
        TreeSet<Bucket> ts = new TreeSet<Bucket>(new BucketMinComparator());
        ts.add(bucket);
        return ts;
    }

    public boolean includes(int v) {
        return bucket.includes(v);
    }

    public String toString() {
        return bucket.toString()+" "+values.toString();
    }
    public void checkInvariant() {
        //skip
    }
    protected String pprint(String i) {
        return i+bucket.toString()+" "+values.toString()+"\n";
    }
}
