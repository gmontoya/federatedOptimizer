import java.util.PriorityQueue;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Comparator;
import java.io.Serializable;
import java.util.Random;

class QTreeOne implements Serializable {
    protected static final long serialVersionUID = 5221508779908127382L;
    private int bmax;
    private int fmax;
    private PriorityQueue<QTreeOneNode> pq;
    protected QTreeOneNode root;
    protected int numBuckets = 0;
    public QTreeOne(int bm, int fm) {
        this.bmax = bm;
        this.fmax = fm;
        this.root = null;
        this.pq = new PriorityQueue<QTreeOneNode>(new QTreeOnePenaltyComparator());
    }

    public int getCount () {
        int c = 0;
        if (root != null) {
            c = root.bucket.getCount();
        }
        return c;
    }
    public HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> getOverlap(QTreeOne other) {

        if (root == null  || other == null || other.root == null) {
            return new HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>();
        } else {
            return getOverlap(root, other.root);
        }
    }

    public HashMap<Integer, HashMap<Integer, Integer>> getFOverlap(QTreeOne other) {

        if (root == null  || other == null || other.root == null) {
            return new HashMap<Integer, HashMap<Integer, Integer>>();
        } else {
            return getFOverlap(root, other.root);
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
    public Set<BucketOne> getBuckets() {
        if (root == null) {
            return new HashSet<BucketOne>();
        } else {
            return root.getBuckets();
        }
    }

    public void checkInvariant() {

        if (root == null)
            return;
        root.checkInvariant();
    }

    public static HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> getOverlap(QTreeOneNode qt1, QTreeOneNode qt2) {

        LinkedList<QTreeOneNode> qqt1 = new LinkedList<QTreeOneNode>();        
        LinkedList<QTreeOneNode> qqt2 = new LinkedList<QTreeOneNode>();
        if (overlaps(qt1.getBucket(), qt2.getBucket())) {
            qqt1.add(qt1);
            qqt2.add(qt2);
        }
        HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> o = new HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>();
        while (qqt1.size()>0 && qqt2.size()>0) {
            QTreeOneNode e1 = qqt1.getFirst();
            QTreeOneNode e2 = qqt2.getFirst();
            QTreeOneNode el, es;
            LinkedList<QTreeOneNode> qqtl, qqts;
            boolean c_1_2 = false;
            if ((e1 instanceof QTreeOneBranch) && (e2 instanceof QTreeOneLeaf || longer(e1.getBucket(), e2.getBucket()))) {
                el = e1;
                qqtl = qqt1;
                es = e2;
                qqts = qqt2;
                c_1_2 = true;
            } else { 
                el = e2;
                qqtl = qqt2;
                es = e1;
                qqts = qqt1;
            } 
            Set<QTreeOneNode> cs = new HashSet<QTreeOneNode>(); //new QTreeOneMinComparator()); 
            
            if (el instanceof QTreeOneBranch) {
                cs = ((QTreeOneBranch) el).children;
                
            } else {
                cs.add(el);
     
            }
            qqtl.removeFirst();
            for (QTreeOneNode c : cs) {
                for (QTreeOneNode d : qqts) {
                    if (c instanceof QTreeOneLeaf && d instanceof QTreeOneLeaf && overlaps(c.getBucket(), d.getBucket())) {
            
                        HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> x = null;
                        if (c_1_2) {
                            x = getOverlap(((QTreeOneLeaf) c).getValuesO(), c.getBucket(), ((QTreeOneLeaf) d).getValuesS(), d.getBucket());
                        } else {
                            x = getOverlap(((QTreeOneLeaf) d).getValuesO(), d.getBucket(), ((QTreeOneLeaf) c).getValuesS(), c.getBucket());
                        }
                        o = combineCPs(o, x);
                    } else if (overlaps(c.getBucket(), d.getBucket())) {
                        if (c instanceof QTreeOneLeaf) {
                            qqtl.addLast(c);
                        } else {
                            qqtl.addFirst(c);
                        }
                        break;
                    }
                }
            }
        }
        return o;
    }

    public static HashMap<Integer, HashMap<Integer, Integer>> getFOverlap(QTreeOneNode qt1, QTreeOneNode qt2) {

        LinkedList<QTreeOneNode> qqt1 = new LinkedList<QTreeOneNode>();        
        LinkedList<QTreeOneNode> qqt2 = new LinkedList<QTreeOneNode>();
        if (overlaps(qt1.getBucket(), qt2.getBucket())) {
            qqt1.add(qt1);
            qqt2.add(qt2);
        }
        HashMap<Integer, HashMap<Integer, Integer>> o = new HashMap<Integer, HashMap<Integer, Integer>>();
        while (qqt1.size()>0 && qqt2.size()>0) {
            QTreeOneNode e1 = qqt1.getFirst();
            QTreeOneNode e2 = qqt2.getFirst();
            QTreeOneNode el, es;
            LinkedList<QTreeOneNode> qqtl, qqts;
            boolean c_1_2 = false;
            if ((e1 instanceof QTreeOneBranch) && (e2 instanceof QTreeOneLeaf || longer(e1.getBucket(), e2.getBucket()))) {
                el = e1;
                qqtl = qqt1;
                es = e2;
                qqts = qqt2;
                c_1_2 = true;
            } else { 
                el = e2;
                qqtl = qqt2;
                es = e1;
                qqts = qqt1;
            } 
            Set<QTreeOneNode> cs = new HashSet<QTreeOneNode>(); //new QTreeOneMinComparator()); 
            
            if (el instanceof QTreeOneBranch) {
                cs = ((QTreeOneBranch) el).children;
                
            } else {
                cs.add(el);
     
            }
            qqtl.removeFirst();
            for (QTreeOneNode c : cs) {
                for (QTreeOneNode d : qqts) {
                    if (c instanceof QTreeOneLeaf && d instanceof QTreeOneLeaf && overlaps(c.getBucket(), d.getBucket())) {
            
                        HashMap<Integer, HashMap<Integer, Integer>> x = null;
                        if (c_1_2) {
                            x = getFOverlap(((QTreeOneLeaf) c).getValuesS(), c.getBucket(), ((QTreeOneLeaf) d).getValuesS(), d.getBucket());
                        } else {
                            x = getFOverlap(((QTreeOneLeaf) d).getValuesS(), d.getBucket(), ((QTreeOneLeaf) c).getValuesS(), c.getBucket());
                        }
                        o = combineFCSs(o, x);
                    } else if (overlaps(c.getBucket(), d.getBucket())) {
                        if (c instanceof QTreeOneLeaf) {
                            qqtl.addLast(c);
                        } else {
                            qqtl.addFirst(c);
                        }
                        break;
                    }
                }
            }
        }
        return o;
    }

    public static HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> getOverlap(HashMap<String, HashMap<Integer, HashSet<Short>>> hs1, BucketOne b1, HashMap<Integer, HashSet<Short>> hs2, BucketOne b2) {
        HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> res = new HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>();
        for (String p : hs1.keySet()) {
            HashMap<Integer, HashSet<Short>> csVs1 = hs1.get(p);
            for (Integer cs1 : csVs1.keySet()) {
                HashSet<Short> vs1 = csVs1.get(cs1);
                for (Integer cs2 : hs2.keySet()) {
                    HashSet<Short> vs2 = hs2.get(cs2);
                    HashSet<Short> vs = new HashSet<Short>();
                    vs.addAll(vs1);
                    vs.retainAll(vs2);
                    int c = vs.size();
                    if (c>0) {
                        HashMap<Integer, HashMap<Integer, Integer>> cs1cs2Count = res.get(p);
                        if (cs1cs2Count == null) {
                            cs1cs2Count = new HashMap<Integer, HashMap<Integer, Integer>>();
                        }
                        HashMap<Integer, Integer> cs2Count = cs1cs2Count.get(cs1);
                        if (cs2Count == null) {
                            cs2Count = new HashMap<Integer, Integer>();
                        }
                        Integer count = cs2Count.get(cs2);
                        if (count == null) {
                            count = 0;
                        }
                        count += c;
                        cs2Count.put(cs2, count);
                        cs1cs2Count.put(cs1, cs2Count);
                        res.put(p, cs1cs2Count);
                    }
                }
            }
        }
        return res;
    }

    public static HashMap<Integer, HashMap<Integer, Integer>> getFOverlap(HashMap<Integer, HashSet<Short>> hs1, BucketOne b1, HashMap<Integer, HashSet<Short>> hs2, BucketOne b2) {
        HashMap<Integer, HashMap<Integer, Integer>> res = new HashMap<Integer, HashMap<Integer, Integer>>();
            for (Integer cs1 : hs1.keySet()) {
                HashSet<Short> vs1 = hs1.get(cs1);
                for (Integer cs2 : hs2.keySet()) {
                    HashSet<Short> vs2 = hs2.get(cs2);
                    HashSet<Short> vs = new HashSet<Short>();
                    vs.addAll(vs1);
                    vs.retainAll(vs2);
                    int c = vs.size();
                    if (c>0) {
                        HashMap<Integer, Integer> cs2Count = res.get(cs1);
                        if (cs2Count == null) {
                            cs2Count = new HashMap<Integer, Integer>();
                        }
                        Integer count = cs2Count.get(cs2);
                        if (count == null) {
                            count = 0;
                        }
                        count += c;
                        cs2Count.put(cs2, count);
                        res.put(cs1, cs2Count);

                    }
                }
            }
        return res;
    }

    public static boolean overlaps(BucketOne b1, BucketOne b2) {
        return (Math.max(b1.getMin(), b2.getMin()) <= Math.min(b1.getMax(), b2.getMax()));
    }

    /*public static int getOverlap(BucketOne b1, BucketOne b2) {
        return Math.min(b1.getCount(),b2.getCount());
    }*/

    public static boolean longer(BucketOne b1, BucketOne b2) {
        return (b1.getMax()-b1.getMin()) >= (b2.getMax()-b2.getMin());
    }

    public static QTreeOneNode getChild(Set<QTreeOneNode> children, int v) {

        Iterator<QTreeOneNode> it = children.iterator();
        QTreeOneNode e;
        QTreeOneNode bestB = null;
        double bestD = Double.MAX_VALUE;
        while (it.hasNext() && ((e=it.next())!=null)) { // && e.getBucket().getMin()<=v) {
            if ( e.getBucket().getMin()<=v && e.getBucket().getMax() >= v) {
                QTreeOneNode b = e;
                double d = distance(e.getBucket().getMin(), e.getBucket().getMax(), v);
                if (d < bestD) {
                    bestB = b;
                    bestD = d;
                }
            }
        }
        return bestB;
    }

    public static QTreeOneNode getChild(Set<QTreeOneNode> children, BucketOne b) {

        Iterator<QTreeOneNode> it = children.iterator();
        QTreeOneNode e;
        QTreeOneNode bestB = null;
        double bestD = Double.MAX_VALUE;
        while (it.hasNext() && ((e=it.next())!=null)) { // && e.getBucket().getMin()<=b.getMin()) {
            if (e.getBucket().getMin()<=b.getMin() && e.getBucket().getMax() >= b.getMax()) {
                int d = distance(e.getBucket().getMin(), e.getBucket().getMax(), b.getMin(), b.getMax());
                if (d < bestD) {
                    bestB = e;
                    bestD = d;
                }
            }
        }
        return bestB;
    }

    public static QTreeOneNode getChildren(Set<QTreeOneNode> children, int v, Queue<QTreeOneNode> others) {

        Iterator<QTreeOneNode> it = children.iterator();
        QTreeOneNode e;
        QTreeOneNode bestB = null;
        while (it.hasNext() && ((e=it.next())!=null)) {
            if (e.getBucket().getMin()<=v && e.getBucket().getMax() >= v) {
                QTreeOneNode b = e;           
                if (bestB == null) {
                    bestB = b;
                } else {
                    others.add(b);
                }
            }
        }
        return bestB;
    }

    public static int distance(int mnA, int mxA, int mnB, int mxB) {
        int d = Math.max(mxA, mxB)-Math.min(mnA, mnB);
        return d;
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

    public Set<QTreeOneLeaf> getLeaves(int v) {

        //System.out.println("root numBuckets: "+(root!=null?numBuckets:""));
        //System.out.println("(S) adding "+v+" to ");
        //System.out.println(root);
        Queue<QTreeOneNode> queue= new LinkedList<QTreeOneNode>();
        HashSet<QTreeOneLeaf> res = new HashSet<QTreeOneLeaf>();
        queue.add(root);
        while(queue.size()>0) {
            QTreeOneNode qt = queue.poll();
            QTreeOneNode p = null;
            while (qt != null && (qt instanceof QTreeOneBranch)) {
                p = qt;
                qt = getChildren(((QTreeOneBranch) qt).children, v, queue);
            }
 
            //System.out.println("p: "+p+". qt: "+qt);
            if (qt != null) {
                QTreeOneLeaf l = (QTreeOneLeaf) qt;
                if (l.includes(v)) {
                    res.add(l);
                }
            }
        }  
        return res;
    }

    public Set<QTreeOneLeaf> getLeaves() {

        Queue<QTreeOneNode> queue= new LinkedList<QTreeOneNode>();
        HashSet<QTreeOneLeaf> res = new HashSet<QTreeOneLeaf>();
        queue.add(root);
        while(queue.size()>0) {
            QTreeOneNode qt = queue.poll();
            if (qt instanceof QTreeOneLeaf) {
                QTreeOneLeaf l = (QTreeOneLeaf) qt; 
                res.add(l);
            } else {
                for (QTreeOneNode n : ((QTreeOneBranch) qt).children) {
                    queue.add(n);
                }
            }
        }
        return res;
    }

    public void addAll(QTreeOne other) {
        Set<QTreeOneLeaf> ls = other.getLeaves();
        for (QTreeOneLeaf l : ls) {
            addLeaf(l);
        }
    }

    public void addLeaf(QTreeOneLeaf l) {
        QTreeOneNode qt = root;
        QTreeOneNode p = null;
        //System.out.println("root numBuckets: "+(root!=null?numBuckets:""));
        //System.out.println("(S) adding "+v+" to ");
        //System.out.println(root);
        while (qt != null && (qt instanceof QTreeOneBranch)) {
            p = qt; 
            qt = getChild(((QTreeOneBranch) qt).children, l.bucket);
        }
        //System.out.println("p: "+p+". qt: "+qt);
        if (qt != null) {
            QTreeOneLeaf qtl = (QTreeOneLeaf) qt;
            if (qtl.includes(l.bucket.getMin()) && qtl.includes(l.bucket.getMax())) {
                qtl.addAllValues(l);
                if (p != null) {
                    ((QTreeOneBranch) p).updateBucketAddAux(l.bucket.getMin(), l.bucket.getMax(), l.bucket.getCount());
                }
                return;
            } else {
                QTreeOneBranch b = new QTreeOneBranch(p);
                QTreeOneLeaf nqt = new QTreeOneLeaf(b);
                nqt.addAllValues(l);
                numBuckets++;
                qt.parent = b;
                b.addChild(qt);
                b.addChild(nqt);
                qt = b;
            }
        } else {
            qt = new QTreeOneLeaf(p);
            ((QTreeOneLeaf) qt).addAllValues(l);
            numBuckets++;
        }
        //System.out.println("p: "+p+". qt: "+qt);
        if (p!= null) {
            QTreeOneBranch b = (QTreeOneBranch) p;
            b.addChild(qt);
        } else { 
            root = qt;
            //System.out.println("root has been updated");
        }
    }

    public void addS(int v, Integer cs) {
        QTreeOneNode qt = root;
        QTreeOneNode p = null;
        //System.out.println("root numBuckets: "+(root!=null?numBuckets:""));
        //System.out.println("(S) adding "+v+" to ");
        //System.out.println(root);
        while (qt != null && (qt instanceof QTreeOneBranch)) {
            p = qt; 
            qt = getChild(((QTreeOneBranch) qt).children, v);
        }
        //System.out.println("p: "+p+". qt: "+qt);
        if (qt != null) {
            QTreeOneLeaf l = (QTreeOneLeaf) qt;
            if (l.includes(v)) {
                l.addValueS(v, cs);
                if (p != null) {
                    ((QTreeOneBranch) p).updateBucketAddAux(v, v, 1);
                }
                return;
            } else {
                QTreeOneBranch b = new QTreeOneBranch(p);
                QTreeOneLeaf nqt = new QTreeOneLeaf(b);
                nqt.addValueS(v, cs);
                numBuckets++;
                qt.parent = b;
                b.addChild(qt);
                b.addChild(nqt);
                qt = b;
            }
        } else {
            qt = new QTreeOneLeaf(p);
            ((QTreeOneLeaf) qt).addValueS(v, cs);
            numBuckets++;
        }
        //System.out.println("p: "+p+". qt: "+qt);
        if (p!= null) {
            QTreeOneBranch b = (QTreeOneBranch) p;
            b.addChild(qt);
        } else { 
            root = qt;
            //System.out.println("root has been updated");
        }
    }
    //public static boolean bool = false;
    public void addO(int v, Integer cs, String pred) {
        QTreeOneNode qt = root;
        QTreeOneNode p = null;
        //bool = (v == 1592162457) && (cs == -552586974) && pred.equals("<http://data.linkedmdb.org/resource/oddlinker/link_target>");
        /*if (bool) {
          System.out.println("root numBuckets: "+(root!=null?numBuckets:""));
          System.out.println("(O) adding "+v+" to ");
          System.out.println(root);
        }*/
        while (qt != null && (qt instanceof QTreeOneBranch)) {
            p = qt;
            qt = getChild(((QTreeOneBranch) qt).children, v);
        }
/*        if (bool) {
          System.out.println("p: "+p+". qt: "+qt);
        }*/
        if (qt != null) {
            QTreeOneLeaf l = (QTreeOneLeaf) qt;
            if (l.includes(v)) {
                l.addValueO(v, cs, pred);
                if (p != null) {
                    ((QTreeOneBranch) p).updateBucketAddAux(v, v, 1);
                }
                return;
            } else {
                QTreeOneBranch b = new QTreeOneBranch(p);
                QTreeOneLeaf nqt = new QTreeOneLeaf(b);
                nqt.addValueO(v, cs, pred);
                numBuckets++;
                qt.parent = b;
                b.addChild(qt);
                b.addChild(nqt);
                qt = b;
            }
        } else {
            qt = new QTreeOneLeaf(p);
            ((QTreeOneLeaf) qt).addValueO(v, cs, pred);
            numBuckets++;
        }
/*        if (bool) {
        System.out.println("2. p: "+p+". qt: "+qt);
        }*/
        if (p!= null) {
            QTreeOneBranch b = (QTreeOneBranch) p;
            b.addChild(qt);
        } else {
            root = qt;
/*            if (bool) {
            System.out.println("root has been updated");}*/
        }
    }

    //public static boolean first = false;
        public static QTreeOneNode[] getMergeCandidates(Set<QTreeOneNode> es) {
            //System.out.println("getMergeCandidates. es: "+es);
            QTreeOneNode[] cs = new QTreeOneNode[2];
            double m = Double.MAX_VALUE;
            int mn =  Integer.MAX_VALUE;
            Iterator<QTreeOneNode> it1 = es.iterator();
            while (it1.hasNext()) {
                QTreeOneNode e1 = it1.next();
                Iterator<QTreeOneNode> it2 = es.iterator();
                while (it2.hasNext()) {
                    QTreeOneNode e2 = it2.next();
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

    class QTreeOneBranch extends QTreeOneNode {

        private Set<QTreeOneNode> children;
        //private HashSet<Short> values;

        public QTreeOneBranch(QTreeOneNode p) {
            parent = p;
            children = new HashSet<QTreeOneNode>(); //new QTreeOneMinComparator());
            bucket = new BucketOne();
            //System.out.println("numBuckets Branch constructor: "+numBuckets);
        }
        public HashMap<Integer, HashSet<Short>> getValuesS() {
            HashMap<Integer, HashSet<Short>> vs = new HashMap<Integer, HashSet<Short>>();
            for (QTreeOneNode n : children) {
                HashMap<Integer, HashSet<Short>> cvs = n.getValuesS();
                vs = QTreeOne.combineValuesS(vs, cvs);
            }
            return vs;
        }
        public HashMap<String, HashMap<Integer, HashSet<Short>>> getValuesO() {
            HashMap<String, HashMap<Integer, HashSet<Short>>> vs = new HashMap<String, HashMap<Integer, HashSet<Short>>>();
            for (QTreeOneNode n : children) {
                HashMap<String, HashMap<Integer, HashSet<Short>>> cvs = n.getValuesO();
                vs = QTreeOne.combineValuesO(vs, cvs);
            }
            return vs;
        }

        public void addChild(QTreeOneNode c) {
            addChildAux(c);
            if (numBuckets > bmax && pq.size()>0) {
/*                if (bool) {
                System.out.println("bmax exceded");
                }*/
                /*Iterator<QTreeNode> itn = pq.iterator();
                while (itn.hasNext()) {
                    QTreeNode aux = itn.next();
                    double penalty = aux.getPenalty();
                    System.out.println("elem "+aux+" has penalty: "+penalty);
                }*/
                QTreeOneBranch n = (QTreeOneBranch) pq.poll();
/*                if (bool) { System.out.println("n: \n"+n); }*/
                QTreeOneNode c1, c2, p;
                if (n.children.size()==2) {
                    Iterator<QTreeOneNode> it1 = n.children.iterator();
                    c1 = it1.next();
                    c2 = it1.next();
                    p =  n.parent;
                    if (p != null) {
                        ((QTreeOneBranch) p).removeChild(n);
                        //((QTreeBranch) p).decreaseNumBuckets(n.numBuckets);
                    } /*else {
                        ((QTreeBranch) n).removeChild(c1);
                        ((QTreeBranch) n).removeChild(c1);
                    }*/
                } else {
                    QTreeOneNode[] cs = getMergeCandidates(n.children);
                    c1 = cs[0];
                    c2 = cs[1];
/*                    if (bool) {
                      System.out.println("(b) c1: "+c1+". c2: "+c2);
                    }*/
                    p = n;
                    ((QTreeOneBranch) p).removeChild(c1);
                    //((QTreeBranch) p).decreaseNumBuckets(c1.numBuckets);
                    ((QTreeOneBranch) p).removeChild(c2);
                    //((QTreeBranch) p).decreaseNumBuckets(c2.numBuckets);
                }
/*                if (bool) {
                System.out.println("p: "+p);
                }*/
                numBuckets--;
                BucketOne nb = summarize(c1, c2);
                HashMap<Integer, HashSet<Short>> vsS = summarizeValuesS(c1, c2);
                HashMap<String, HashMap<Integer, HashSet<Short>>> vsO = summarizeValuesO(c1, c2);
                QTreeOneLeaf l = new QTreeOneLeaf(p, nb);
                l.setValuesS(vsS);
                l.setValuesO(vsO);
/*                if (bool) {
                    System.out.println("l: "+l);
                }*/
                //numBuckets++;
                if (p!=null) {
                    ((QTreeOneBranch) p).addChildAux(l);
/*                    if (bool) {
                    System.out.println("calling drop if possible from addChild"); }*/
                    dropIfPossible(p, fmax, pq);
                } /*else {
                    System.out.println("!! p == null");
                }*/
            }
            //checkInvariant();
        }

        public void checkInvariant() {
            int n = 0;
            for (QTreeOneNode qt : children) {
                n += qt.getBucket().getCount();
                qt.checkInvariant();
            }
            if (n != getBucket().getCount()) {
                System.out.println("Invariant broken by "+this);
            }
        }

        private void addChildAux(QTreeOneNode c) {
/*            if (bool) {
            System.out.println("bucket addChild start: "+bucket);
            System.out.println("children addChild start: "+children); }*/
            /*Iterator<QTreeNode> it = children.iterator();
            while (it.hasNext()) {
                System.out.println(it.next());
            }*/
            children.add(c);
/*            if (bool) { System.out.println("adding c: "+c); }*/
            //updatePriorityAdd(c);
            updateBucketAdd(c);
            //updatePriorityAdd(c);
            //System.out.println("numBuckets before increase: "+numBuckets);
            //increaseNumBuckets(c.numBuckets);
            //System.out.println("numBuckets after increase: "+numBuckets);

            if (children.size() > fmax) {
/*                if (bool) { System.out.println("children.size() > fmax"); }*/
                QTreeOneNode[] cs = getMergeCandidates(children);
                QTreeOneNode c1 = cs[0], c2 = cs[1];
                //System.out.println("(f) c1: "+c1+". c2: "+c2);
                QTreeOneNode cb = null;
                if (c1.equals(c)) {
                    cb = c2;
                } else if (c2.equals(c)) {
                    cb = c1;
                }
                if ((cb != null) && (cb instanceof QTreeOneBranch)) {
                    removeChild(c);
                    //decreaseNumBuckets(c.numBuckets);
                    //System.out.println("numBuckets after decrease1: "+numBuckets);
                    c.parent = cb;
                    ((QTreeOneBranch) cb).addChildAux(c);
                } else {
                    removeChild(c1);
                    //updateBucketRemove(c1);
                    removeChild(c2);
                    //updateBucketRemove(c2);
                    //decreaseNumBuckets(c1.numBuckets+c2.numBuckets);
                    //System.out.println("numBuckets after decrease2: "+numBuckets);
                    QTreeOneBranch nc = new QTreeOneBranch(this);
                    children.add(nc);
                    c1.parent = nc;
                    nc.addChildAux(c1);
                    c2.parent = nc;
                    nc.addChildAux(c2);
                    //System.out.println("calling drop if possible from addChildAux");
                    dropIfPossible(nc, fmax, pq);
                }
            }
/*            if (bool) {
            System.out.println("children addChild end: "+children); }*/
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

        public void removeChild(QTreeOneNode c) {
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

        public void updateBucketAdd(QTreeOneNode o) {
            //System.out.println("Updating bucket after adding "+o);
            BucketOne ob = o.getBucket();
            int mn = ob.getMin();
            int mx = ob.getMax();
            int c = ob.getCount();
            updateBucketAddAux(mn, mx, c);
            updatePriorityAdd(o);
        }

        public void updateBucketAddAux(int inf, int sup, int n) {
            if (parent != null) {
                ((QTreeOneBranch) parent).children.remove(this);
            }
            
            int mn = Math.min(bucket.getMin(), inf);
            int mx = Math.max(bucket.getMax(), sup);
            int c = bucket.getCount() + n;
            bucket = new BucketOne(mn, mx, c);
            //System.out.println("(add) bucket updated to: "+bucket);
            if (parent != null) {
                ((QTreeOneBranch) parent).children.add(this);
                ((QTreeOneBranch) parent).updateBucketAddAux(inf, sup, n);
                //((QTreeBranch) parent).updatePriorityAdd(this);
            }
        }

        public void updateBucketRemove(QTreeOneNode o) {
            //System.out.println("Updating bucket after removing "+o);
            BucketOne ob = o.getBucket();
            int mn = ob.getMin();
            int mx = ob.getMax();
            int c = ob.getCount();
            updateBucketRemoveAux(mn, mx, c);
            updatePriorityRemove(o);
        }

        public void updateBucketRemoveAux(int inf, int sup, int n) {
            if (parent != null) {
                ((QTreeOneBranch) parent).children.remove(this);
            }
            int mn = bucket.getMin();
            int mx = bucket.getMax();
            if (mn == inf) {
                mn = Integer.MAX_VALUE;
                for (QTreeOneNode qt : children) {
                    mn = Math.min(mn, qt.getBucket().getMin());
                }
            }
            
            if (mx == sup) {
                mx = Integer.MIN_VALUE;
                for (QTreeOneNode qt : children) {
                    mx = Math.max(mx, qt.getBucket().getMax());
                }
            }
            int c = bucket.getCount() - n;

            bucket = new BucketOne(mn, mx, c);
            //System.out.println("(remove) bucket updated to: "+bucket);
            if (parent != null) {
                ((QTreeOneBranch) parent).children.add(this);
                ((QTreeOneBranch) parent).updateBucketRemoveAux(inf, sup, n);
                //((QTreeBranch) parent).updatePriorityRemove(this);
            }
        }

        public void updatePriorityAdd(QTreeOneNode c) {
            //System.out.println("Updating priority after adding "+c);
            if (c instanceof QTreeOneBranch) {
                return;
            }
            double p = Double.MAX_VALUE;
            for (QTreeOneNode other : this.children) {
                if (other != c && !(other instanceof QTreeOneBranch)) {
                    p = Math.min(p, QTreeOne.getPenalty(c, other));
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

        public void updatePriorityRemove(QTreeOneNode c) {
            if (c instanceof QTreeOneBranch) {
                //pq.remove(c);
                return;
            }
            double p = Double.MAX_VALUE;
            for (QTreeOneNode other1 : this.children) {
                if (other1 instanceof QTreeOneBranch) {
                    continue;
                }
                for (QTreeOneNode other2 : this.children) {
                    if (other2 instanceof QTreeOneBranch) {
                        continue;
                    }
                    if (other1 != other2) {
                        p = Math.min(p, QTreeOne.getPenalty(other1, other2));
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
            if ((o == null) || !(o instanceof QTreeOneBranch)) {
                return false;
            }
            QTreeOneBranch b = (QTreeOneBranch) o;
            return bucket.equals(b.bucket) && children.equals(b.children);
        }

        public int compareTo(Object o) {
            if (!(o instanceof QTreeOneBranch)) {
                return 1;
            }
            QTreeOneBranch b = (QTreeOneBranch) o;
            if (children.size() == 0 && b.children.size() != 0) {
                return -1;
            } else if (children.size() != 0 && b.children.size() == 0) {
                return 1;
            }
            for (QTreeOneNode n1 : children) {
                for (QTreeOneNode n2 : children) {
                    if (n1.compareTo(n2) < 0) {
                        return -1;
                    } else if (n1.compareTo(n2)>0) {
                        return 1;
                    }
                }
            }
            return 0;
        }

        public String toString() {

            return pprint("");
        }

        public String pprint(String i) {
            String s = i+bucket.toString()+"\n";
            for (QTreeOneNode c : children) {
                s += c.pprint(i+" ");
            }
            return s;
        }

        public Set<BucketOne> getBuckets() {
            Set<BucketOne> ts = new HashSet<BucketOne>(); //new BucketOneMinComparator());
            for (QTreeOneNode qt : children) {
                ts.addAll(qt.getBuckets());
            }
            return ts;
        }
    }

        public static HashMap<Integer, HashSet<Short>> summarizeValuesS(QTreeOneNode c1, QTreeOneNode c2) {
            HashMap<Integer, HashSet<Short>> values1 = c1.getValuesS();
            HashMap<Integer, HashSet<Short>> values2 = c2.getValuesS();
            HashMap<Integer, HashSet<Short>> values = QTreeOne.combineValuesS(values1, values2); /*new HashMap<Integer, HashSet<Short>>(values1);
            for (Integer cs2 : values2.keySet()) {
                HashSet<Short> vs = values2.get(cs2);
                if (values1.containsKey(cs2)) {
                    vs.addAll(values1.get(cs2));
                }
                values.put(cs2, vs);
            }   */
            return values;
        }

        public static HashMap<String, HashMap<Integer, HashSet<Short>>> summarizeValuesO(QTreeOneNode c1, QTreeOneNode c2) {
            HashMap<String, HashMap<Integer, HashSet<Short>>> values1 = c1.getValuesO();
            HashMap<String, HashMap<Integer, HashSet<Short>>> values2 = c2.getValuesO();
            HashMap<String, HashMap<Integer, HashSet<Short>>> values = QTreeOne.combineValuesO(values1, values2); /*new HashMap<String, HashMap<Integer, HashSet<Short>>>(values1);
            for (String p2 : values2.keySet()) {
                HashMap<Integer, HashSet<Short>> csVs2 = values2.get(p2);
                for (Integer cs2 : csVs2.keySet()) {
                    HashSet<Short> vs2 = csVs2.get(cs2);
                    if (values1.containsKey(p2) && values1.get(p2).containsKey(cs2)) {
                        vs2.addAll(values1.get(p2).get(cs2));
                    }
                    csVs2.put(cs2, vs2);
                }
                values.put(p2, csVs2);
            }*/
            return values;
        }

        public static BucketOne summarize(QTreeOneNode c1, QTreeOneNode c2) {
            BucketOne b1 = c1.getBucket();
            BucketOne b2 = c2.getBucket();
            int mn = Math.min(b1.getMin(), b2.getMin());
            int mx = Math.max(b1.getMax(), b2.getMax());
            int c = b1.getCount() + b2.getCount();
            BucketOne b = new BucketOne(mn, mx, c);
            /*if ((b1.includes(614300494) || b2.includes(614300494)) && !b.includes(614300494)) {
                System.out.println("ERROR in summarize");
            }*/
            return b;
        }

        public static void dropIfPossible(QTreeOneNode qt, int fmax, PriorityQueue<QTreeOneNode> pq) {

           //System.out.println("Drop if possible start "+qt.bucket);
           if ((qt != null) && (qt instanceof QTreeOneBranch) && (qt.parent != null)) {
               QTreeOneBranch b = (QTreeOneBranch) qt;
               QTreeOneBranch p = (QTreeOneBranch) qt.parent;
/*               if (bool) { System.out.println("Drop if possible p "+p); }*/
                int n = b.children.size();
                int nP = p.children.size();
/*               if (bool) { System.out.println("n: "+n+". nP: "+nP); }*/
                if (n + nP <= fmax-1) {
                    //System.out.println("call to remove child from dropIP");
                    p.removeChild(qt);
                    pq.remove(qt);
                    for (QTreeOneNode e : b.children) {
                        e.parent = p;
                        p.addChildAux(e);
                    }
                }
                qt = p;
                //System.out.println("Drop if possible before end "+qt.bucket);
            }
/*            if (bool) {
                System.out.println("qt: "+qt);
            }*/
            //System.out.println("Drop if possible end");
        } 
        public static double getPenalty(QTreeOneNode c1, QTreeOneNode c2) {
            BucketOne b1 = c1.getBucket();
            BucketOne b2 = c2.getBucket();
            return Math.max(b1.getMax(), b2.getMax()) - Math.min(b1.getMin(), b2.getMin());
        }

    /*public static void smartLoading(int inf, int sup, int v, QTreeOne q) {
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
    }*/
    public static void randomLoading(QTreeOne q, int n) {

        //Random r = new Random();
        for (int i = 0; i < n; i++) {
            q.addS((int) (Math.random()*10000.0*Math.pow(-1.0,i)), 0);//r.nextInt());
        }
    }
    public static void naiveLoading(QTreeOne q, int n, int v) {
        for (int i = 0; i < n; i++) {
            q.addS(i*v, 0);//r.nextInt());
            q.addO(i*v, 0, "");
        }
    }
    public static void main(String[] args) {
        //int inf = Integer.parseInt(args[0]);
        //int sup = Integer.parseInt(args[1]);
        //int v = Integer.parseInt(args[2]);
        int n = Integer.parseInt(args[0]);
        int bmax = Integer.parseInt(args[1]);
        int fmax = Integer.parseInt(args[2]);
        int v1 = Integer.parseInt(args[3]);
        int v2 = Integer.parseInt(args[4]);
        QTreeOne q1 = new QTreeOne(bmax,fmax);
        //QTreeOne q2 = new QTreeOne(4,2);
        //smartLoading(inf, sup, v, q1);
        naiveLoading(q1, n, v1);
        System.out.println(q1);
        //System.out.println(q1.getBuckets());

        QTreeOne q2 = new QTreeOne(bmax,fmax);
        naiveLoading(q2, n, v2);
        System.out.println(q2);
        //System.out.println(q1.getBuckets());
        //q1.checkInvariant();
        System.out.println(q1.getOverlap(q2));
        System.out.println(q2.getOverlap(q1));
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
  class QTreeOneLeaf extends QTreeOneNode {
    HashMap<Integer, HashSet<Short>> valuesS;
    HashMap<String, HashMap<Integer, HashSet<Short>>> valuesO;

    public QTreeOneLeaf(QTreeOneNode p) {
        this(p, new BucketOne());
        valuesS = new HashMap<Integer, HashSet<Short>>();
        valuesO = new HashMap<String, HashMap<Integer, HashSet<Short>>>();
    }

    public HashMap<Integer, HashSet<Short>> getValuesS() {
        return valuesS;
    }

    public HashMap<String, HashMap<Integer, HashSet<Short>>> getValuesO() {
        return valuesO;
    }

    protected void setValuesS(HashMap<Integer, HashSet<Short>> vs) {
        valuesS = vs;
    }

    protected void setValuesO(HashMap<String, HashMap<Integer, HashSet<Short>>> vs) {
        valuesO = vs;
    }

    public QTreeOneLeaf(QTreeOneNode p, BucketOne b) {
        //this(p, b);
        //values = new HashSet<Short>();
        parent = p;
        bucket = b;
        valuesS = new HashMap<Integer, HashSet<Short>>();
        valuesO = new HashMap<String, HashMap<Integer, HashSet<Short>>>();
        //numBuckets = 1;
        /*if (parent != null) {
            parent.increaseNumBuckets(1);
        }*/
    }
    public void addAllValues(QTreeOneLeaf other) {
        valuesS = QTreeOne.combineValuesS(valuesS, other.valuesS);
        valuesO = QTreeOne.combineValuesO(valuesO, other.valuesO);

        if (bucket.count == 0) {
            bucket = new BucketOne(other.bucket.getMin(),other.bucket.getMax(),other.bucket.getCount());
        } else {
            bucket.count = bucket.getCount() + other.bucket.getCount();
        }
    }

    public void addValueS(int v, int cs) {
        //v=0; 
        //cs = 0;
        short s = (new Integer(v % 65536)).shortValue();
        //System.out.println("(QTreeLeaf) going to add "+s+" to "+this);
        HashSet<Short> vs = valuesS.get(cs);
        if (vs == null) {
            vs = new HashSet<Short>();
        }
        vs.add(s);
        valuesS.put(cs, vs);
        //System.out.println("bucket: "+bucket);
        if (bucket.count == 0) {
            bucket = new BucketOne(v,v,1);
        } else {
            bucket.increaseCount();
        }
        /*if (parent != null) {
            parent.addOneValue();
        }*/
    }

    public void addValueO(int v, int cs, String p) {
        //v=0;
        //cs=0;
        //p="";
        short s = (new Integer(v % 65536)).shortValue();
        //System.out.println("(QTreeLeaf) going to add "+s+" to "+this);
        HashMap<Integer, HashSet<Short>> csVs = valuesO.get(p);
        if (csVs == null) {
            csVs = new HashMap<Integer, HashSet<Short>>();
        }

        HashSet<Short> vs = csVs.get(cs);

        if (vs == null) {
            vs = new HashSet<Short>();
        }
        vs.add(s);
        csVs.put(cs, vs);
        valuesO.put(p, csVs);
        /*if (v == 614300494 && cs == 672184354 && p.equals("<http://xmlns.com/foaf/0.1/based_near>")) {
            System.out.println("Adding "+s+" in addValueO");
            QTreeOneNode x = this;
            while (x != null) {
                System.out.println("x.bucket: "+x.bucket);
                x = x.parent;
            }
        }*/
        if (bucket.count == 0) {
            bucket = new BucketOne(v,v,1);
        } else {
            bucket.increaseCount();
        }
        /*if (parent != null) {
            parent.addOneValue();
        }*/
    }

    public boolean equals(Object o) {
        if ((o == null) || !(o instanceof QTreeOneLeaf)) {
            return false;
        }
        QTreeOneLeaf l = (QTreeOneLeaf) o;
        return bucket.equals(l.bucket) && valuesS.equals(l.valuesS) && valuesO.equals(l.valuesO);
    }

    public int compareTo(Object o) {
        if (!(o instanceof QTreeOneLeaf)) {
            return -1;
        }
        QTreeOneLeaf l = (QTreeOneLeaf) o;
        if (valuesS.equals(l.valuesS) && valuesO.equals(l.valuesO)) {
            return 0;
        }
        return 1;  // may be improved
    }

    public Set<BucketOne> getBuckets() {
        Set<BucketOne> ts = new HashSet<BucketOne>(); //new BucketOneMinComparator());
        ts.add(bucket);
        return ts;
    }

    public boolean includes(int v) {
        return bucket.includes(v);
    }

    public String toString() {
        return bucket.toString()+" "+valuesS.toString() +" "+valuesO.toString()+"\n";
    }
    public void checkInvariant() {
        //skip
    }
    protected String pprint(String i) {
        return i+bucket.toString()+" "+valuesS.toString()+" "+valuesO.toString()+"\n";
    }
  }

  abstract class QTreeOneNode implements Serializable {
    protected BucketOne bucket;
    protected double penalty = Double.MAX_VALUE;
    //protected int numBuckets = 0;
    protected QTreeOneNode parent;

    public abstract boolean equals(Object o);
    public abstract int compareTo(Object o);
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

    public BucketOne getBucket() {
        return bucket;
    }

    public abstract Set<BucketOne> getBuckets();

    public double getPenalty() {
        return penalty;
    }

    public abstract String toString();
    public abstract void checkInvariant();
    protected abstract String pprint(String i);
    public abstract HashMap<Integer, HashSet<Short>> getValuesS();
    public abstract HashMap<String, HashMap<Integer, HashSet<Short>>> getValuesO();

  }

  public static class QTreeOneMinComparator implements Comparator, Serializable {

    @Override
    public int compare(Object o1, Object o2) {
        QTreeOneNode qt1 = (QTreeOneNode) o1;
        QTreeOneNode qt2 = (QTreeOneNode) o2;
        
        int c = 0;
        if (qt1.getBucket().getMin() < qt2.getBucket().getMin() || ((qt1.getBucket().getMin() == qt2.getBucket().getMin())&& qt1.getBucket().getMax()< qt2.getBucket().getMax()) || ((qt1.getBucket().getMin() == qt2.getBucket().getMin())&& (qt1.getBucket().getMax()== qt2.getBucket().getMax()) && qt1.compareTo(qt2) < 0)) {
            c = -1;
        } else if (qt1.getBucket().getMin() > qt2.getBucket().getMin() || ((qt1.getBucket().getMin() == qt2.getBucket().getMin())&& qt1.getBucket().getMax()> qt2.getBucket().getMax())  || ((qt1.getBucket().getMin() == qt2.getBucket().getMin())&& (qt1.getBucket().getMax()== qt2.getBucket().getMax()) && qt1.compareTo(qt2) > 0)) {
            c = 1;
        }
        return c;
    }
  }

  public static class QTreeOnePenaltyComparator implements Comparator, Serializable {

    @Override
    public int compare(Object o1, Object o2) {
        QTreeOneNode qt1 = (QTreeOneNode) o1;
        QTreeOneNode qt2 = (QTreeOneNode) o2;
        int c = 0;
        if (qt1.getPenalty() - qt2.getPenalty() < 0) {
            c = -1;
        } else if (qt1.getPenalty() - qt2.getPenalty() > 0) {
            c = 1;
        }
        return c;
    }
  }

  static class BucketOne implements Serializable {

    private int min;
    private int max;
    private int count;

    public BucketOne(int mn, int mx, int c) {
        this.min = mn;
        this.max = mx;
        this.count = c;
    }

    public BucketOne() {
        this.min = Integer.MAX_VALUE;
        this.max = Integer.MIN_VALUE;
        this.count = 0;
    }

    public void increaseCount() {
        this.count++;
    }

    public int getCount() {
        return this.count;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
    public boolean equals(Object o) {
        if ((o == null) || !(o instanceof BucketOne)) {
            return false;
        }
        BucketOne other = (BucketOne) o;
        return (min == other.min) && (max == other.max) && (count == other.count);
    }
    public boolean includes(int v) {
        return this.min <= v && v <= this.max;
    }
    public String toString() {
        return "{ min: "+min+", max: "+max+", count: "+count+"} ";
    }
  }

  public class BucketOneMinComparator implements Comparator, Serializable {

    @Override
    public int compare(Object o1, Object o2) {
        BucketOne b1 = (BucketOne) o1;
        BucketOne b2 = (BucketOne) o2;
        
        int c = 0;
        if (b1.getMin() < b2.getMin() || ((b1.getMin() == b2.getMin())&& b1.getMax()< b2.getMax()) ) {
            c = -1;
        } else if (b1.getMin() > b2.getMin() || ((b1.getMin() == b2.getMin())&& b1.getMax()> b2.getMax()) ) {
            c = 1;
        }
        return c;
    }
  }

    public static HashMap<Integer, HashSet<Short>> combineValuesS(HashMap<Integer, HashSet<Short>> vs1, HashMap<Integer, HashSet<Short>> vs2) {
        for (Integer cs : vs2.keySet()) {
            HashSet<Short> vs = new HashSet<Short>(vs2.get(cs));
            if (vs1.containsKey(cs)) {
                vs.addAll(vs1.get(cs));
            }
            vs1.put(cs, vs);
        }
        return vs1;
    }

    public static HashMap<String, HashMap<Integer, HashSet<Short>>> combineValuesO(HashMap<String, HashMap<Integer, HashSet<Short>>> vs1, HashMap<String, HashMap<Integer, HashSet<Short>>> vs2) {
        //boolean b = (vs1.get("<http://xmlns.com/foaf/0.1/based_near>") != null && vs1.get("<http://xmlns.com/foaf/0.1/based_near>").get(672184354) != null &&  vs1.get("<http://xmlns.com/foaf/0.1/based_near>").get(672184354).contains((short) 31566)) || (vs2.get("<http://xmlns.com/foaf/0.1/based_near>") != null && vs2.get("<http://xmlns.com/foaf/0.1/based_near>").get(672184354) != null &&  vs2.get("<http://xmlns.com/foaf/0.1/based_near>").get(672184354).contains((short) 31566));
        for (String p : vs2.keySet()) {
            HashMap<Integer, HashSet<Short>> csVs = new HashMap<Integer, HashSet<Short>>(vs2.get(p));
            HashMap<Integer, HashSet<Short>> csVs1 = vs1.get(p);
            if (csVs1 == null) {
                csVs1 = new HashMap<Integer, HashSet<Short>>();
            } else {
                csVs1 = new HashMap<Integer, HashSet<Short>>();
                for (Integer cs : vs1.get(p).keySet()) {
                    csVs1.put(cs, vs1.get(p).get(cs));
                }
            }
            for (Integer cs : csVs.keySet()) {
                HashSet<Short> vs = new HashSet<Short>(csVs.get(cs));
                if (csVs1.containsKey(cs)) {
                    vs.addAll(csVs1.get(cs));
                }
                csVs1.put(cs, vs);
            }
            vs1.put(p,csVs1);
        }
        /*if (b && !(vs1.get("<http://xmlns.com/foaf/0.1/based_near>") != null && vs1.get("<http://xmlns.com/foaf/0.1/based_near>").get(672184354) != null &&  vs1.get("<http://xmlns.com/foaf/0.1/based_near>").get(672184354).contains((short) 31566))) {
            System.out.println("Error in combineValuesO");
        }*/
        return vs1;
    }

    public static HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> combineCPs(HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> vs1, HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> vs2) {
        //System.out.println("Combining "+vs1+" with "+vs2);
        //boolean b = (vs1.get("<http://xmlns.com/foaf/0.1/based_near>") != null && vs1.get("<http://xmlns.com/foaf/0.1/based_near>").get(672184354) != null &&  vs1.get("<http://xmlns.com/foaf/0.1/based_near>").get(672184354).get(-405470945) != null) || (vs2.get("<http://xmlns.com/foaf/0.1/based_near>") != null && vs2.get("<http://xmlns.com/foaf/0.1/based_near>").get(672184354) != null &&  vs2.get("<http://xmlns.com/foaf/0.1/based_near>").get(672184354).get(-405470945) != null);
        for (String p : vs2.keySet()) {
            HashMap<Integer, HashMap<Integer, Integer>> cs1Cs2C2 = new HashMap<Integer, HashMap<Integer, Integer>>(vs2.get(p));
            HashMap<Integer, HashMap<Integer, Integer>> cs1Cs2C1 = vs1.get(p);
            if (cs1Cs2C1 == null) {
                cs1Cs2C1 = new HashMap<Integer, HashMap<Integer, Integer>>();
            } else {
                cs1Cs2C1 = new HashMap<Integer, HashMap<Integer, Integer>>();
                for (Integer cs1 : vs1.get(p).keySet()) {
                    HashMap<Integer, Integer> cs2C1 = new HashMap<Integer, Integer>(vs1.get(p).get(cs1));
                    cs1Cs2C1.put(cs1, cs2C1);
                }
            }
            for (Integer cs1 : cs1Cs2C2.keySet()) {
                HashMap<Integer, Integer> cs2C2 = new HashMap<Integer, Integer>(cs1Cs2C2.get(cs1));
                HashMap<Integer, Integer> cs2C1 = cs1Cs2C1.get(cs1);
                if (cs2C1 == null) {
                    cs2C1 = new HashMap<Integer, Integer>();
                } else {
                    cs2C1 = new HashMap<Integer, Integer>(cs2C1);
                }

                for (Integer cs2 : cs2C2.keySet()) {
                    Integer c2 = new Integer(cs2C2.get(cs2));
                    if (cs2C1.containsKey(cs2)) {
                        c2 = c2 + cs2C1.get(cs2);
                    }
                    cs2C1.put(cs2, c2);
                }
                cs1Cs2C1.put(cs1, cs2C1);
            }
            vs1.put(p,cs1Cs2C1);
        }
        /*if (b && !(vs1.get("<http://xmlns.com/foaf/0.1/based_near>") != null && vs1.get("<http://xmlns.com/foaf/0.1/based_near>").get(672184354) != null &&  vs1.get("<http://xmlns.com/foaf/0.1/based_near>").get(672184354).get(-405470945) != null)) {
            System.out.println("Error in combine CPs");
        }*/
        //System.out.println("result: "+vs1);
        return vs1;
    }

    public static HashMap<Integer, HashMap<Integer, Integer>> combineFCSs(HashMap<Integer, HashMap<Integer, Integer>> vs1, HashMap<Integer, HashMap<Integer, Integer>> vs2) {

            for (Integer cs1 : vs2.keySet()) {
                HashMap<Integer, Integer> cs2C2 = new HashMap<Integer, Integer>(vs2.get(cs1));
                HashMap<Integer, Integer> cs2C1 = vs1.get(cs1);
                if (cs2C1 == null) {
                    cs2C1 = new HashMap<Integer, Integer>();
                } else {
                    cs2C1 = new HashMap<Integer, Integer>(cs2C1);
                }

                for (Integer cs2 : cs2C2.keySet()) {
                    Integer c2 = new Integer(cs2C2.get(cs2));
                    if (cs2C1.containsKey(cs2)) {
                        c2 = c2 + cs2C1.get(cs2);
                    }
                    cs2C1.put(cs2, c2);
                }
                vs1.put(cs1, cs2C1);
            }
        //System.out.println("result: "+vs1);
        return vs1;
    }
}
