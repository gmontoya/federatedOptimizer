import java.util.PriorityQueue;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.io.Serializable;
import java.util.Random;

class QTree implements Serializable {

    private int bmax;
    private int fmax;
    private PriorityQueue<QTreeNode> pq;
    protected QTreeNode root;
    protected int numBuckets = 0;
    public QTree(int bm, int fm) {
        this.bmax = bm;
        this.fmax = fm;
        this.root = null;
        this.pq = new PriorityQueue<QTreeNode>(new QTreePenaltyComparator());
    }

    public int getOverlap(QTree other) {

        if (root == null  || other == null || other.root == null) {
            return 0;
        } else {
            return getOverlap(root, other.root);
        }
    }

    public String toString() {
        if (root == null) {
            return "-";
        } else {
            return root.toString();
        }
    }
    public int getNumBuckets() {
        return numBuckets;
    }
    public TreeSet<Bucket> getBuckets() {
        if (root == null) {
            return new TreeSet<Bucket>(new BucketMinComparator());
        } else {
            return root.getBuckets();
        }
    }

    public void checkInvariant() {

        if (root == null)
            return;
        root.checkInvariant();
    }

    public static int getOverlap(QTreeNode qt1, QTreeNode qt2) {

        LinkedList<QTreeNode> qqt1 = new LinkedList<QTreeNode>();        
        LinkedList<QTreeNode> qqt2 = new LinkedList<QTreeNode>();
        if (overlaps(qt1.getBucket(), qt2.getBucket())) {
            qqt1.add(qt1);
            qqt2.add(qt2);
        }
        int o = 0;
        while (qqt1.size()>0 && qqt2.size()>0) {
            QTreeNode e1 = qqt1.getFirst();
            QTreeNode e2 = qqt2.getFirst();
            //System.out.println("(in while) e1: "+e1+". e2: "+e2);
            QTreeNode el, es;
            LinkedList<QTreeNode> qqtl, qqts;
            boolean c_1_2 = false;
            if ((e1 instanceof QTreeBranch) && (e2 instanceof QTreeLeaf || longer(e1.getBucket(), e2.getBucket()))) {
                el = e1;
                qqtl = qqt1;
                es = e2;
                qqts = qqt2;
                c_1_2 = true;
            } else { //if  ((e2 instanceof QTreeBranch) && (e1 instanceof QTreeLeaf || longer(e2.getBucket(), e1.getBucket()))) {
                el = e2;
                qqtl = qqt2;
                es = e1;
                qqts = qqt1;
            } /*else {
                
                qqt1.removeFirst();
                qqt2.removeFirst();
                //o += getOverlap(e1.getBucket(), e2.getBucket());
                //continue;
            }*/
            //System.out.println("el: "+el);
            TreeSet<QTreeNode> cs = new TreeSet<QTreeNode>(new QTreeMinComparator()); 
            
            if (el instanceof QTreeBranch) {
                cs = ((QTreeBranch) el).children;
                
            } else {
                cs.add(el);
     
            }
            //System.out.println("cs: "+cs);
            qqtl.removeFirst();
            for (QTreeNode c : cs) {
                //System.out.println("c: "+c);
                for (QTreeNode d : qqts) {
                    //System.out.println("d: "+d);
                    if (c instanceof QTreeLeaf && d instanceof QTreeLeaf && overlaps(c.getBucket(), d.getBucket())) {
            
                        //int x = getOverlap(c.getBucket(), d.getBucket());
                        int x = 0;
                        if (c_1_2) {
                            x = getOverlap(((QTreeLeaf) c).getValues(), c.getBucket(), ((QTreeLeaf) d).getValues(), d.getBucket());
                        } else {
                            x = getOverlap(((QTreeLeaf) d).getValues(), d.getBucket(), ((QTreeLeaf) c).getValues(), c.getBucket());
                        }
                        //System.out.println("overlap c d: "+x);
                        o += x;
                    } else if (overlaps(c.getBucket(), d.getBucket())) {
                        if (c instanceof QTreeLeaf) {
                            qqtl.addLast(c);
                        } else {
                            qqtl.addFirst(c);
                        }
                        //System.out.println("enqueing c");
                        break;
                    }
                }
            }
        }
        return o;
    }
    public static int getOverlap(HashSet<Short> hs1, Bucket b1, HashSet<Short> hs2, Bucket b2) {
        HashSet<Short> hs = new HashSet<Short>();
        hs.addAll(hs1);
        hs.retainAll(hs2);
        //System.out.println("hs1: "+hs1+". b1: "+b1+". hs2: "+hs2+". b2: "+b2);
        return hs.size()*Math.min(b1.getCount()/hs1.size(), b2.getCount()/hs2.size());
    }

    public static boolean overlaps(Bucket b1, Bucket b2) {
        return (Math.max(b1.getMin(), b2.getMin()) <= Math.min(b1.getMax(), b2.getMax()));
    }

    public static int getOverlap(Bucket b1, Bucket b2) {
        return Math.min(b1.getCount(),b2.getCount());
    }

    public static boolean longer(Bucket b1, Bucket b2) {
        return (b1.getMax()-b1.getMin()) >= (b2.getMax()-b2.getMin());
    }

    public static QTreeNode getChild(TreeSet<QTreeNode> children, int v) {

        Iterator<QTreeNode> it = children.iterator();
        QTreeNode e;
        QTreeNode bestB = null;
        double bestD = Double.MAX_VALUE;
        while (it.hasNext() && ((e=it.next())!=null) && e.getBucket().getMin()<=v) {
            if (e.getBucket().getMax() >= v) {
                QTreeNode b = e;
                double d = distance(e.getBucket().getMin(), e.getBucket().getMax(), v);
                if (d < bestD) {
                    bestB = b;
                    bestD = d;
                }
            }
        }
        return bestB;
    }

    public static double distance(int mn, int mx, int v) {
        double d;
        if (mx > mn) {
            d = Math.abs(0.5*(mn + mx)-v)/(mx - mn);
        } else {
            d = Math.abs(mx - v);
        }
        return d;
    }

    public void add(int v) {
        QTreeNode qt = root;
        QTreeNode p = null;
        //System.out.println("root numBuckets: "+(root!=null?numBuckets:""));
        //System.out.println("adding "+v+" to ");
        //System.out.println(root);
        while (qt != null && (qt instanceof QTreeBranch)) {
            p = qt; 
            qt = getChild(((QTreeBranch) qt).children, v);
        }
        //System.out.println("p: "+p+". qt: "+qt);
        if (qt != null) {
            QTreeLeaf l = (QTreeLeaf) qt;
            if (l.includes(v)) {
                l.addValue(v);
                if (p != null) {
                    ((QTreeBranch) p).updateBucketAddAux(v, v, 1);
                }
                return;
            } else {
                QTreeBranch b = new QTreeBranch(p);
                QTreeLeaf nqt = new QTreeLeaf(b, v);
                numBuckets++;
                qt.parent = b;
                b.addChild(qt);
                b.addChild(nqt);
                qt = b;
            }
        } else {
            qt = new QTreeLeaf(p, v);
            numBuckets++;
        }
        //System.out.println("p: "+p+". qt: "+qt);
        if (p!= null) {
            QTreeBranch b = (QTreeBranch) p;
            b.addChild(qt);
        } else { 
            root = qt;
            //System.out.println("root has been updated");
        }
    }
    //public static boolean first = false;
        public static QTreeNode[] getMergeCandidates(TreeSet<QTreeNode> es) {
            //System.out.println("getMergeCandidates. es: "+es);
            QTreeNode[] cs = new QTreeNode[2];
            double m = Double.MAX_VALUE;
            int mn =  Integer.MAX_VALUE;
            Iterator<QTreeNode> it1 = es.iterator();
            while (it1.hasNext()) {
                QTreeNode e1 = it1.next();
                Iterator<QTreeNode> it2 = es.iterator();
                while (it2.hasNext()) {
                    QTreeNode e2 = it2.next();
                    if (e1.equals(e2)) {
                        continue;
                    }
                    double p = getPenalty(e1, e2);
                    int count = e1.getBucket().getCount() + e2.getBucket().getCount();
                    if (p < m || ((p == m) && (count < mn))) { // || (p==m && first)) {
                        //System.out.println("updating candidates with "+e1+" and "+e2+" penalty: "+p); 
                        m = p;
                        mn = count;
                        cs[0] = e1;
                        cs[1] = e2;
                    }
                }
            }
            //first = !first;
            return cs;
        }

    class QTreeBranch extends QTreeNode {

        private TreeSet<QTreeNode> children;
        private HashSet<Short> values;

        public QTreeBranch(QTreeNode p) {
            parent = p;
            children = new TreeSet<QTreeNode>(new QTreeMinComparator());
            bucket = new Bucket(Integer.MAX_VALUE, Integer.MIN_VALUE, 0);
            //System.out.println("numBuckets Branch constructor: "+numBuckets);
        }
        public HashSet<Short> getValues() {
            HashSet<Short> vs = new HashSet<Short>();
            for (QTreeNode n : children) {
                vs.addAll(n.getValues());
            }
            return vs;
        }
        public void addChild(QTreeNode c) {
            addChildAux(c);
            if (numBuckets > bmax && pq.size()>0) {
                //System.out.println("bmax exceded");
                /*Iterator<QTreeNode> itn = pq.iterator();
                while (itn.hasNext()) {
                    QTreeNode aux = itn.next();
                    double penalty = aux.getPenalty();
                    System.out.println("elem "+aux+" has penalty: "+penalty);
                }*/
                QTreeBranch n = (QTreeBranch) pq.poll();
                //System.out.println("n: \n"+n);
                QTreeNode c1, c2, p;
                if (n.children.size()==2) {
                    Iterator<QTreeNode> it1 = n.children.iterator();
                    c1 = it1.next();
                    c2 = it1.next();
                    p =  n.parent;
                    if (p != null) {
                        ((QTreeBranch) p).removeChild(n);
                        //((QTreeBranch) p).decreaseNumBuckets(n.numBuckets);
                    } /*else {
                        ((QTreeBranch) n).removeChild(c1);
                        ((QTreeBranch) n).removeChild(c1);
                    }*/
                } else {
                    QTreeNode[] cs = getMergeCandidates(n.children);
                    c1 = cs[0];
                    c2 = cs[1];
                    //System.out.println("(b) c1: "+c1+". c2: "+c2);
                    p = n;
                    ((QTreeBranch) p).removeChild(c1);
                    //((QTreeBranch) p).decreaseNumBuckets(c1.numBuckets);
                    ((QTreeBranch) p).removeChild(c2);
                    //((QTreeBranch) p).decreaseNumBuckets(c2.numBuckets);
                }
                //System.out.println("c1: "+c1+". c2: "+c2);
                numBuckets--;
                Bucket nb = summarize(c1, c2);
                HashSet<Short> vs = summarizeValues(c1, c2);
                QTreeLeaf l = new QTreeLeaf(p, nb);
                l.setValues(vs);
                //numBuckets++;
                if (p!=null) {
                    ((QTreeBranch) p).addChildAux(l);
                    //System.out.println("calling drop if possible from addChild");
                    dropIfPossible(p, fmax, pq);
                }
            }
            //checkInvariant();
        }

        public void checkInvariant() {
            int n = 0;
            for (QTreeNode qt : children) {
                n += qt.getBucket().getCount();
                qt.checkInvariant();
            }
            if (n != getBucket().getCount()) {
                System.out.println("Invariant broken by "+this);
            }
        }

        private void addChildAux(QTreeNode c) {
            //System.out.println("bucket addChild start: "+bucket);
            //System.out.println("children addChild start: "+children);
            /*Iterator<QTreeNode> it = children.iterator();
            while (it.hasNext()) {
                System.out.println(it.next());
            }*/
            children.add(c);
            //System.out.println("adding c: "+c);
            //updatePriorityAdd(c);
            updateBucketAdd(c);
            //updatePriorityAdd(c);
            //System.out.println("numBuckets before increase: "+numBuckets);
            //increaseNumBuckets(c.numBuckets);
            //System.out.println("numBuckets after increase: "+numBuckets);

            if (children.size() > fmax) {
                QTreeNode[] cs = getMergeCandidates(children);
                QTreeNode c1 = cs[0], c2 = cs[1];
                //System.out.println("(f) c1: "+c1+". c2: "+c2);
                QTreeNode cb = null;
                if (c1.equals(c)) {
                    cb = c2;
                } else if (c2.equals(c)) {
                    cb = c1;
                }
                if ((cb != null) && (cb instanceof QTreeBranch)) {
                    removeChild(c);
                    //decreaseNumBuckets(c.numBuckets);
                    //System.out.println("numBuckets after decrease1: "+numBuckets);
                    c.parent = cb;
                    ((QTreeBranch) cb).addChildAux(c);
                } else {
                    removeChild(c1);
                    //updateBucketRemove(c1);
                    removeChild(c2);
                    //updateBucketRemove(c2);
                    //decreaseNumBuckets(c1.numBuckets+c2.numBuckets);
                    //System.out.println("numBuckets after decrease2: "+numBuckets);
                    QTreeBranch nc = new QTreeBranch(this);
                    children.add(nc);
                    c1.parent = nc;
                    nc.addChildAux(c1);
                    c2.parent = nc;
                    nc.addChildAux(c2);
                    //System.out.println("calling drop if possible from addChildAux");
                    dropIfPossible(nc, fmax, pq);
                }
            }
            //System.out.println("children addChild end: "+children);
            /*it = children.iterator();
            QTreeNode n1 = null, n2 = null;
            if (it.hasNext()) {
                n1 = it.next();
                System.out.println(n1);
            }
            if (it.hasNext()) {
                n2 = it.next();
                System.out.println(n2);
            }
            //System.out.println("comparator: "+children.comparator());
            if (n1 != null && n2 != null) {
                System.out.println((new QTreeMinComparator()).compare(n1, n2)); 
                System.out.println((new QTreeMinComparator()).compare(n2, n1));
            }*/
        }

        public void removeChild(QTreeNode c) {
            if (c!= null) {
              //System.out.println("children removeChild start: "+children);
              children.remove(c);
              //System.out.println("removed c: "+c);
              //updatePriorityRemove(c);
              updateBucketRemove(c);

              //System.out.println("children removeChild end: "+children);
            }
        // Currently it only removes when it will add new children, no need to reduce more
        // It should also reduce the limits of the bucket if appropriate
        /*if ((children.size() == 0) && (parent != null)) {
            parent.removeChild(this);
        }*/
        }

        public void updateBucketAdd(QTreeNode o) {
            //System.out.println("Updating bucket after adding "+o);
            Bucket ob = o.getBucket();
            int mn = ob.getMin();
            int mx = ob.getMax();
            int c = ob.getCount();
            updateBucketAddAux(mn, mx, c);
            updatePriorityAdd(o);
        }

        public void updateBucketAddAux(int inf, int sup, int n) {
            if (parent != null) {
                ((QTreeBranch) parent).children.remove(this);
            }
            
            int mn = Math.min(bucket.getMin(), inf);
            int mx = Math.max(bucket.getMax(), sup);
            int c = bucket.getCount() + n;
            bucket = new Bucket(mn, mx, c);
            //System.out.println("(add) bucket updated to: "+bucket);
            if (parent != null) {
                ((QTreeBranch) parent).children.add(this);
                ((QTreeBranch) parent).updateBucketAddAux(inf, sup, n);
                //((QTreeBranch) parent).updatePriorityAdd(this);
            }
        }

        public void updateBucketRemove(QTreeNode o) {
            //System.out.println("Updating bucket after removing "+o);
            Bucket ob = o.getBucket();
            int mn = ob.getMin();
            int mx = ob.getMax();
            int c = ob.getCount();
            updateBucketRemoveAux(mn, mx, c);
            updatePriorityRemove(o);
        }

        public void updateBucketRemoveAux(int inf, int sup, int n) {
            if (parent != null) {
                ((QTreeBranch) parent).children.remove(this);
            }
            int mn = bucket.getMin();
            int mx = bucket.getMax();
            if (mn == inf) {
                mn = Integer.MAX_VALUE;
                for (QTreeNode qt : children) {
                    mn = Math.min(mn, qt.getBucket().getMin());
                }
            }
            
            if (mx == sup) {
                mx = Integer.MIN_VALUE;
                for (QTreeNode qt : children) {
                    mx = Math.max(mx, qt.getBucket().getMax());
                }
            }
            int c = bucket.getCount() - n;

            bucket = new Bucket(mn, mx, c);
            //System.out.println("(remove) bucket updated to: "+bucket);
            if (parent != null) {
                ((QTreeBranch) parent).children.add(this);
                ((QTreeBranch) parent).updateBucketRemoveAux(inf, sup, n);
                //((QTreeBranch) parent).updatePriorityRemove(this);
            }
        }

        public void updatePriorityAdd(QTreeNode c) {
            //System.out.println("Updating priority after adding "+c);
            if (c instanceof QTreeBranch) {
                return;
            }
            double p = Double.MAX_VALUE;
            for (QTreeNode other : this.children) {
                if (other != c && !(other instanceof QTreeBranch)) {
                    p = Math.min(p, QTree.getPenalty(c, other));
                    //System.out.println("after considering "+other+" p is: "+p);
                }
            }
            if (p < penalty) {
                //System.out.println("updating penalty from "+penalty+" to "+p);
                pq.remove(this);
                penalty = p;
                pq.add(this);
            }
        }

        public void updatePriorityRemove(QTreeNode c) {
            if (c instanceof QTreeBranch) {
                //pq.remove(c);
                return;
            }
            double p = Double.MAX_VALUE;
            for (QTreeNode other1 : this.children) {
                if (other1 instanceof QTreeBranch) {
                    continue;
                }
                for (QTreeNode other2 : this.children) {
                    if (other2 instanceof QTreeBranch) {
                        continue;
                    }
                    if (other1 != other2) {
                        p = Math.min(p, QTree.getPenalty(other1, other2));
                    }
                }
            }
            if (p > penalty) {
                pq.remove(this);
                penalty = p;
                if (penalty < Double.MAX_VALUE) {
                    pq.add(this);
                }
            }
        }

        public boolean equals(Object o) {
            if ((o == null) || !(o instanceof QTreeBranch)) {
                return false;
            }
            QTreeBranch b = (QTreeBranch) o;
            return bucket.equals(b.bucket) && children.equals(b.children);
        }
        public String toString() {

            return pprint("");
        }

        public String pprint(String i) {
            String s = i+bucket.toString()+"\n";
            for (QTreeNode c : children) {
                s += c.pprint(i+" ");
            }
            return s;
        }

        public TreeSet<Bucket> getBuckets() {
            TreeSet<Bucket> ts = new TreeSet<Bucket>(new BucketMinComparator());
            for (QTreeNode qt : children) {
                ts.addAll(qt.getBuckets());
            }
            return ts;
        }
    }

        public static HashSet<Short> summarizeValues(QTreeNode c1, QTreeNode c2) {
            HashSet<Short> values1 = c1.getValues();
            HashSet<Short> values2 = c2.getValues();
            HashSet<Short> values = new HashSet<Short>(values1);
            values.addAll(values2);
            return values;
        }

        public static Bucket summarize(QTreeNode c1, QTreeNode c2) {
            Bucket b1 = c1.getBucket();
            Bucket b2 = c2.getBucket();
            int mn = Math.min(b1.getMin(), b2.getMin());
            int mx = Math.max(b1.getMax(), b2.getMax());
            int c = b1.getCount() + b2.getCount();
            Bucket b = new Bucket(mn, mx, c);
            return b;
        }

        public static void dropIfPossible(QTreeNode qt, int fmax, PriorityQueue<QTreeNode> pq) {

           //System.out.println("Drop if possible start "+qt.bucket);
           if ((qt != null) && (qt instanceof QTreeBranch) && (qt.parent != null)) {
               QTreeBranch b = (QTreeBranch) qt;
               QTreeBranch p = (QTreeBranch) qt.parent;
               //System.out.println("Drop if possible p.bucket "+p.bucket);
                int n = b.children.size();
                int nP = p.children.size();
                if (n + nP <= fmax-1) {
                    //System.out.println("call to remove child from dropIP");
                    p.removeChild(qt);
                    pq.remove(qt);
                    for (QTreeNode e : b.children) {
                        e.parent = p;
                        p.addChildAux(e);
                    }
                }
                qt = p;
                //System.out.println("Drop if possible before end "+qt.bucket);
            }
            //System.out.println("Drop if possible end");
        } 
        public static double getPenalty(QTreeNode c1, QTreeNode c2) {
            Bucket b1 = c1.getBucket();
            Bucket b2 = c2.getBucket();
            return Math.max(b1.getMax(), b2.getMax()) - Math.min(b1.getMin(), b2.getMin());
        }

    public static void smartLoading(int inf, int sup, int v, QTree q) {
        LinkedList<Integer> ll1 = new LinkedList<Integer>();
        LinkedList<Integer> ll2 = new LinkedList<Integer>();
        ll1.add(inf);
        ll2.add(sup);
        while(ll1.size()>0) {
            int i = ll1.removeFirst();
            int s = ll2.removeFirst();
            if (i <= s) {
                int m = (int) Math.round(Math.floor((i+s)/2.0));
                q.add(m*v);
                ll1.add(i);
                ll2.add(m-1);
                ll1.add(m+1);
                ll2.add(s);
            }
        }
    }
    public static void randomLoading(QTree q, int n) {

        //Random r = new Random();
        for (int i = 0; i < n; i++) {
            q.add((int) (Math.random()*10000.0*Math.pow(-1.0,i)));//r.nextInt());
        }
    }
    public static void main(String[] args) {
        //int inf = Integer.parseInt(args[0]);
        //int sup = Integer.parseInt(args[1]);
        //int v = Integer.parseInt(args[2]);
        int n = Integer.parseInt(args[0]);
        int bmax = Integer.parseInt(args[1]);
        int fmax = Integer.parseInt(args[2]);
        QTree q1 = new QTree(bmax,fmax);
        QTree q2 = new QTree(4,2);
        //smartLoading(inf, sup, v, q1);
        randomLoading(q1, n);
        System.out.println(q1);
        System.out.println(q1.getBuckets());
        //q1.checkInvariant();
/*
        inf = Integer.parseInt(args[3]);
        sup = Integer.parseInt(args[4]);
        v = Integer.parseInt(args[5]);
        smartLoading(inf, sup, v, q2);
        System.out.println(q2);
        System.out.println(q2.numBuckets);
        int o = q1.getOverlap(q2);
        System.out.println("overlap: "+o);*/
    }
}

