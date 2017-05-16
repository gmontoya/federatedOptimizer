import java.util.*;
import java.io.*;

class RadixTree implements Serializable {

    public class NodeRadixComparator implements Comparator, Serializable {

    @Override
    public int compare(Object o1, Object o2) {
        NodeRadix n1 = (NodeRadix) o1;
        NodeRadix n2 = (NodeRadix) o2;

        int c = 0;
        if (n1.radix.compareTo(n2.radix)<0 || ((n1.radix.compareTo(n2.radix)==0) && n1.num < n2.num)) {
            c = -1;
        } else if (n1.radix.compareTo(n2.radix)>0 || ((n1.radix.compareTo(n2.radix)==0) && n1.num > n2.num)) {
            c = 1;
        }
        return c;
    }
    }

    public class NodeRadixMinComparator implements Comparator, Serializable {

    @Override
    public int compare(Object o1, Object o2) {
        NodeRadix n1 = (NodeRadix) o1;
        NodeRadix n2 = (NodeRadix) o2;

        int c = 0;
        if (n1.num < n2.num) {
            c = -1;
        } else if (n1.num > n2.num) {
            c = 1;
        }
        return c;
    }
    }

    NodeRadix root = null;
    int max; 
    int bmax, fmax;
    String sep;
    private PriorityQueue<NodeRadix> pq;

    public RadixTree(String s, int m, int bm, int fm) {
        max = m;
        sep = s;
        bmax = bm;
        fmax = fm;
        pq = new PriorityQueue<NodeRadix>(new NodeRadixMinComparator());
    }

    public int getOverlap(RadixTree rt) {
        if (root == null || rt == null || rt.root == null) {
            return 0;
        } else {
            return root.getOverlap("", rt.root, "");
        }
    }

    public int getNumberOfLeaves() {
        if (root == null) {
            return 0;
        } else {
            return root.getNumberOfLeaves();
        }
    }

    public void ensureMax() {

        while (getNumberOfLeaves()>max && pq.size()>0) {
            NodeRadix n = pq.poll();
            if (n.parent != null) {
                ((BranchRadix) n.parent).removeChild(n);
            }
        }
    }

    public String toString() {
        if (root == null) {
            return "-";
        } else {
            return root.toString();
        }
    }

    public NodeRadix findMatching(TreeSet<NodeRadix> children, String str) {
        Iterator<NodeRadix> it = children.iterator();

        //System.out.println("(fm) str: "+str);
        while (it.hasNext()) {
            NodeRadix n = it.next();
            //System.out.println("(fm) n: "+n);
            if (getLongestCommonPrefix(n.radix, str)>0) {
                return n;
            }
        }
        return null;
    }

    public static int getBreak(String s) {
        int b = 0;
        String str = s;
        for (int i = 0; i < 6 && str.length()>0; i++) {
            int pos = str.indexOf("/");
            if (pos >= 0) {
                str = str.substring(pos+1);
                b += pos+1;
            } else {
                str = "";
            }
        }
        if (str.length()==0) {
            b = s.lastIndexOf("/");
        } else {
            b--;
        }
        return b;
    }

    public void add(String str) {
        //System.out.println("adding "+str+" to "+root);
        NodeRadix n = root;
        NodeRadix p = null;
        //String old_str = str;
        //String added = null;
        int pos = str.lastIndexOf("/"); //getBreak(str); //str.lastIndexOf("/");
        String value;
        if (pos < 0) {
            return;
        } else {
            value = str.substring(pos+1);
            str = str.substring(0, pos);
        }
        while (n != null && str.startsWith(n.radix)) {
            //System.out.println("(start) n: "+n);
            //System.out.println("(start) p: "+p);
            p = n;
                str = str.substring(n.radix.length());
                if (str.length() > 0 && str.charAt(0) == '/') {
                    str = str.substring(1);
                }
                if (n instanceof BranchRadix) {
                    n = findMatching(((BranchRadix) n).children, str);
                } else {
                    n = null;
                }
            //System.out.println("(end) n: "+n);
            //System.out.println("(end) p: "+p);
        }
        //System.out.println("n: "+n);
        //System.out.println("p: "+p);
        //System.out.println("str: "+str);
        // n == null || str.length == 0 || !str.startsWith(n.radix)
        //boolean add = true;

        if (str.length() == 0) { // CHECK THIS CASE
            if (p instanceof LeafRadix) {
                pq.remove(p);
                ((LeafRadix) p).addValue(value);
                pq.add(p);
                //add = false;
                return;
            } else {
                NodeRadix ec = ((BranchRadix) p).emptyChild();
                if (ec == null) {
                    n = new LeafRadix(str, null);
                    ((LeafRadix) n).addValue(value);
                    pq.add(n);
                } else {
                    n = ec;
                    pq.remove(n);
                    ((LeafRadix) n).addValue(value);
                    pq.add(n);
                    return;
                }
            }
        } else {            
             NodeRadix c = n;
             n = new LeafRadix(str, null);
             ((LeafRadix) n).addValue(value);
             pq.add(n);
             /*if (p != null) {
                NodeRadix c;
                if (p instanceof BranchRadix) {
                    c = findMatching(((BranchRadix) p).children, str);
                } else {
                    c = p;
                    p = p.parent;
                }*/
                if (p != null && c != null && p instanceof BranchRadix) {
                    ((BranchRadix) p).removeChild(c);
                }
                if (c != null) {
                    pos = getLongestCommonPrefix(c.radix, str);
                    String prefix = str.substring(0, pos);
                    BranchRadix b = new BranchRadix(prefix, null);
                    c.radix = c.radix.substring(prefix.length());
                    c.parent = b;
                    b.addChild(c);
                    n.radix = str.substring(prefix.length());
                    if (b.radix.length() > 0 && b.radix.charAt(b.radix.length()-1) == '/') {
                        b.radix = b.radix.substring(0, b.radix.length()-1);
                    }
                    n.parent = b;
                    b.addChild(n);
                    n = b;
                } else if (p instanceof LeafRadix) {
                    String prefix = p.radix;
                    BranchRadix b = new BranchRadix(prefix, null);
                    LeafRadix l = (LeafRadix) p;
                    p = p.parent;
                    if (p!=null) {
                        ((BranchRadix) p).removeChild(l);
                    }
                    l.parent = b;
                    b.addChild(l);
                    l.radix = "";
                    n.parent = b;
                    b.addChild(n);
                    n = b;
                }
             //}
        }
        if (p == null) {
            root = n;
        } else {
            n.parent = p;
            ((BranchRadix) p).addChild(n);
        }

        /*if (added != null && added.trim().length() == 0) {
            System.out.println("added: "+added+". old_str: "+old_str);
        }*/
    }

    public int getLongestCommonPrefix(String s1, String s2) {
        int p = 0;
        while(s1.length()>0 && s2.length()>0) {
            //System.out.println("s1: "+s1+". s2: "+s2);
            int p1 = s1.indexOf(sep);
            int p2 = s2.indexOf(sep);
            //System.out.println("p1: "+p1+". p2: "+p2);
            if (p1 < 0 && p2 < 0) {
                if (s1.equals(s2)) {
                    p += s1.length();
                }
                break;
            } else if ( p1 == p2 && (s1.substring(0,p1).equals(s2.substring(0,p2)))) {
                p += p1;
            } else if (p1 < 0 && p2 >=0 && s2.substring(0, p2).equals(s1)) {
                p += p2;
                break;
            } else if (p2 < 0 && p1 >=0 && s1.substring(0, p1).equals(s2)) {
                p += p1;
                break;
            } else {
                break;
            }
            if (p1 == 0) {
                p++;
                p1++;
                p2++;
            }
            s1 = s1.substring(p1);
            s2 = s2.substring(p2);
            
        }
        //System.out.println("p: "+p);
        return p;
    }
 
    abstract class NodeRadix implements Serializable {
        NodeRadix parent;
        String radix;
        int num;
        int numLeaves;

        protected NodeRadix(String r, NodeRadix p) {
            parent = p;
            radix = r;
            num = 0;
            numLeaves = 0;
        }

        protected void updateNum(int n) {
            num += n;
            if (parent != null) {
                parent.updateNum(n);
            }
        }
        protected void updateNumLeaves(int n) {
            numLeaves += n;
            if (parent != null) {
                parent.updateNumLeaves(n);
            }
        }
        public abstract String toString();
 
        protected abstract String pprint(String i);
        public abstract int getOverlap(String pref1, NodeRadix other, String pref2);

        public int getNumberOfLeaves() {
            return numLeaves;
        }

        public abstract boolean equals(Object o);
    }

    public static int getOverlap(NodeRadix n1, NodeRadix n2) {
            LeafRadix l1 = (LeafRadix) n1;
            LeafRadix l2 = (LeafRadix) n2;
            return l1.values.getOverlap(l2.values);
    }

    class BranchRadix extends NodeRadix implements Serializable {

        private TreeSet<NodeRadix> children;

        public BranchRadix(String r, NodeRadix p) {
            super(r, p);
            children = new TreeSet<NodeRadix>(new NodeRadixComparator());
        }
        private int getOverlap(Iterator<NodeRadix> it1, String pref1, Iterator<NodeRadix> it2, String pref2) {
            int o = 0;
            int lcp = getLongestCommonPrefix(pref1, pref2); 
            //System.out.println("pref1: "+pref1+". pref2: "+pref2+". lcp: "+lcp);

            if (lcp == pref1.length() && lcp != pref2.length()) {
                String p = pref2.substring(pref1.length());
                NodeRadix n = null;
                while (it1.hasNext()) {
                    NodeRadix tmp = it1.next();
                    if (getLongestCommonPrefix(tmp.radix, p)>0) {
                        n = tmp;
                        break;
                    }
                }
                //System.out.println("n: "+n);
                if (n == null) {
                    return o;
                }
                while (it2.hasNext()) {
                    NodeRadix tmp = it2.next();
                    o += n.getOverlap("", tmp, p);
                }
            } else if (lcp == pref2.length() && lcp != pref1.length()) {
                String p = pref1.substring(pref2.length());
                NodeRadix n = null;
                while (it2.hasNext()) {
                    NodeRadix tmp = it2.next();
                    if (getLongestCommonPrefix(tmp.radix, p)>0) {
                        n = tmp;
                        break;
                    }
                }
                //System.out.println("n: "+n);
                if (n == null) {
                    return o;
                }
                while (it1.hasNext()) {
                    NodeRadix tmp = it1.next();
                    o += tmp.getOverlap(p, n, "");
                }
            } else if (lcp == pref2.length() && lcp == pref1.length()) {
                NodeRadix n1 = it1.next();
                NodeRadix n2 = it2.next();
                do {
                    String r1 = pref1.concat(n1.radix.concat("/"));
                    String r2 = pref2.concat(n2.radix.concat("/"));
                    int c = r1.compareTo(r2);
                    //System.out.println("c: "+c);
                    if (c<0) {
                        if (r2.startsWith(r1)) {
                            o += n1.getOverlap(pref1,n2,pref2);
                        }
                        if (it1.hasNext()) {
                            n1 = it1.next();
                            //System.out.println("advancing it1");
                        } else {
                            n1 = null;
                            //System.out.println("it1 ended");
                        }
                    } else if (c == 0) {
                        o += n1.getOverlap(pref1,n2,pref2);
                        if (it1.hasNext()) {
                            n1 = it1.next();
                            //System.out.println("advancing it1");
                        } else {
                            n1 = null;
                            //System.out.println("it1 ended");
                        }
                        if (it2.hasNext()) {
                            n2 = it2.next();
                            //System.out.println("advancing it2");
                        } else {
                            n2 = null;
                            //System.out.println("it2 ended");
                        }
                    } else {
                        if (r1.startsWith(r2)) {
                            o += n1.getOverlap(pref1,n2,pref2);
                        }
                        if (it2.hasNext()) {
                            n2 = it2.next();
                            //System.out.println("advancing it2");
                        } else {
                            n2 = null;
                            //System.out.println("it2 ended");
                        }
                    }
                } while (n1 != null && n2 != null);
            }
            return o;
        }
        private int getOverlap2(Iterator<NodeRadix> it1, String pref1, Iterator<NodeRadix> it2, String pref2) {
                int o = 0;
                if (!it1.hasNext() || !it2.hasNext()) {
                    return o;
                }
                NodeRadix n1 = it1.next();
                NodeRadix n2 = it2.next();
                System.out.println("pref1: "+pref1);
                System.out.println("pref2: "+pref2);
                do {
                    if (n1.radix.length()==0) {
                        if (it1.hasNext()) {
                            n1 = it1.next();
                            System.out.println("advancing it1");
                        } else {
                            n1 = null;
                        }
                        continue;
                    }
                    if (n2.radix.length()==0) {
                        if (it2.hasNext()) {
                            n2 = it2.next();
                            System.out.println("advancing it2");
                        } else {
                            n2 = null;
                        }
                        continue;
                    }
                    System.out.println("n1.radix: "+n1.radix);
                    System.out.println("n2.radix: "+n2.radix);        
                    String r1 = pref1.concat(n1.radix.concat("/"));
                    String r2 = pref2.concat(n2.radix.concat("/"));
                    System.out.println("r1: "+r1);
                    System.out.println("r2: "+r2);
                    int lcp = getLongestCommonPrefix(r1, r2);
                    System.out.println("lcp: "+lcp);
                    if (lcp == r1.length()) {
                        if (it2.hasNext()) {
                            n2 = it2.next();
                            System.out.println("advancing it2");
                        } else {
                            n2 = null;
                            System.out.println("it2 ended");
                        } 
                    }
                    if (lcp == r2.length()) {
                        if (it1.hasNext()) {
                            n1 = it1.next();
                            System.out.println("advancing it1");
                        } else {
                            n1 = null;
                            System.out.println("it1 ended");
                        }
                    }
                    if (lcp > 0 && lcp == r1.length() || lcp == r2.length()) {
                        if (n1 != null && n2 != null) {
                            o += n1.getOverlap(pref1,n2,pref2);
                        }
                        continue;
                    }                    
                    int c = r1.compareTo(r2);
                    System.out.println("c: "+c);
                    if (c<0) {
                        if (r2.startsWith(r1)) {
                            o += n1.getOverlap(pref1,n2,pref2);
                        }
                        if (it1.hasNext()) {
                            n1 = it1.next();
                            System.out.println("advancing it1");
                        } else {
                            n1 = null;
                            System.out.println("it1 ended");
                        }
                    } else if (c == 0) {
                        o += n1.getOverlap(pref1,n2,pref2);
                        if (it1.hasNext()) {
                            n1 = it1.next();
                            System.out.println("advancing it1");
                        } else {
                            n1 = null;
                            System.out.println("it1 ended");
                        }
                        if (it2.hasNext()) {
                            n2 = it2.next();
                            System.out.println("advancing it2");
                        } else {
                            n2 = null;
                            System.out.println("it2 ended");
                        }
                    } else {
                        if (r1.startsWith(r2)) {
                            o += n2.getOverlap(pref2,n1,pref1);
                        }
                        if (it2.hasNext()) {
                            n2 = it2.next();
                            System.out.println("advancing it2");
                        } else {
                            n2 = null;
                            System.out.println("it2 ended");
                        }
                    }
                } while (n1 != null && n2 != null);
                return o;
        }

        public int getOverlap(String pref1, NodeRadix other, String pref2) {
            int o = 0;
            String r1 = pref1.concat(this.radix.length() == 0 ? "" : this.radix.concat("/"));
            String r2 = pref2.concat(other.radix.length() == 0 ? "" : other.radix.concat("/"));
            if (other instanceof LeafRadix) {
                NodeRadix ec = emptyChild();
                if (ec !=null && r1.equals(r2)) {
                    o += RadixTree.getOverlap(ec, other); //Math.min(this.num, other.num);
                } else if (r2.startsWith(r1)) {
                    Iterator<NodeRadix> it1 = children.iterator();                    
                    ArrayList<NodeRadix> l = new ArrayList<NodeRadix>(1);
                    l.add(other);
                    Iterator<NodeRadix> it2 = l.iterator();
                    o += getOverlap(it1, r1, it2, pref2);
                }
            } else if (r1.equals(r2)) {
                Iterator<NodeRadix> it1 = children.iterator();
                Iterator<NodeRadix> it2 = ((BranchRadix) other).children.iterator();
                o += getOverlap(it1, "", it2, "");
            } else if (r1.startsWith(r2)) {
                pref1 = r1.substring(r2.length());
                Iterator<NodeRadix> it1 = children.iterator();
                Iterator<NodeRadix> it2 = ((BranchRadix) other).children.iterator();
                o += getOverlap(it1, pref1, it2, "");
            } else if (r2.startsWith(r1)) {
                pref2 = r2.substring(r1.length());
                Iterator<NodeRadix> it1 = children.iterator();
                Iterator<NodeRadix> it2 = ((BranchRadix) other).children.iterator();
                o += getOverlap(it1, "", it2, pref2);
            }
            //System.out.println("overlap between "+this+" and "+other+": "+o);
            return o;
        }

        private NodeRadix emptyChild() {
            //boolean h = false;
            for (NodeRadix c : children) {
                if (c.radix.compareTo("")>0) {
                    break;
                }
                if (c.radix.equals("")) {
                    //h = true;
                    return c;
                }
            }
            return null; //h;
        }
        
        public void addChild(NodeRadix c) {
            children.add(c);
            updateNum(c.num);
            updateNumLeaves(c.numLeaves);
        }

        public void removeChild(NodeRadix c) {
            children.remove(c);
            updateNum(-c.num);
            updateNumLeaves(-c.numLeaves);
            if (num == 0 && parent != null) {
                ((BranchRadix) parent).removeChild(this);
            }
        }
        public String toString() {

            return pprint("");
        }

        public String pprint(String i) {
            String s = i+radix+" "+num+"\n";
            for (NodeRadix c : children) {
                s += c.pprint(i+" ");
            }
            return s;
        }

        public boolean equals(Object o) {
            if ((o == null) || !(o instanceof BranchRadix)) {
                return false;
            }
            BranchRadix b = (BranchRadix) o;
            return radix.equals(b.radix) && children.equals(b.children);
        }
    }

    class LeafRadix extends NodeRadix implements Serializable {
        QTree values;
        public LeafRadix(String r, NodeRadix p) {
            super(r, p);
            //qt = new QTree(bmax, fmax);
            numLeaves = 1;
            values = new QTree(bmax, fmax);
            /*if (r.trim().length() == 0) {
                System.out.println("(LeafRadix constructor) r: "+r);
            }*/
        }

        public void addValue(String v) {
            values.add(v.hashCode());
            updateNum(1);
            //qt.add(v.hashCode());
        }

        public String toString() {
            return radix+" "+num+" "+values.toString()+"\n";
        }
        protected String pprint(String i) {
            return i+radix+" "+num+" "+values.toString()+"\n";
        }
        public int getOverlap(String pref1, NodeRadix other, String pref2) {
            int o = 0;
            //System.out.println("pref1: "+pref1+". pref2: "+pref2);
            //System.out.println("this: "+this+".\n other: "+other);
            String r1 = pref1.concat(this.radix.length() == 0 ? "" : this.radix.concat("/"));
            String r2 = pref2.concat(other.radix.length() == 0 ? "" : other.radix.concat("/"));

            NodeRadix ec = (other instanceof BranchRadix) ? ((BranchRadix) other).emptyChild() : other;

            if (other instanceof LeafRadix || ec != null) { //((BranchRadix) other).hasEmptyChild()) {                
                if (r1.equals(r2)) { 
                    o += RadixTree.getOverlap(this, ec); //Math.min(this.num, other.num);
                }
            } else if ((other instanceof BranchRadix) && r1.startsWith(r2)) {
                BranchRadix b = (BranchRadix) other;
                Iterator<NodeRadix> it2 = b.children.iterator();                    
                ArrayList<NodeRadix> l = new ArrayList<NodeRadix>(1);
                l.add(this);
                Iterator<NodeRadix> it1 = l.iterator();
                o += b.getOverlap(it1, pref1, it2, r2);
            }
            //System.out.println("overlap between "+this+" and "+other+": "+o);
            return o;
        }
        public boolean equals(Object o) {
            if (o == null || !(o instanceof LeafRadix)) {
                return false;
            }
            LeafRadix l = (LeafRadix) o;
            boolean b = values.equals(l.values);
            b = b && radix.equals(l.radix);
            return b;
        }
    }

    public static RadixTree simpleLoadTree(String file, int m, int bm, int fm) throws Exception {
        RadixTree t = new RadixTree("/", m, bm, fm);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String l = br.readLine();

        while (l!=null) {
            t.add(l);
            l = br.readLine();
        }
        br.close();
        return t;
    }

    public static RadixTree loadTree(String file, int m, int bm, int fm) throws Exception {
        RadixTree t = new RadixTree("/", m, bm, fm);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String l = br.readLine();

        while (l!=null) {
            int p = l.indexOf(" ");
            /*if (p<0) {
                System.out.println("l: "+l);
            }*/
            String s = l.substring(0, p);
            String r = l.substring(p+1);
            if (s.startsWith("<")) {
                p = s.lastIndexOf("/");
                s = s.substring(1, s.length()-1); //p >= 0 ? s.substring(1, p) : s.substring(1, s.length()-1);
                //s = p >= 0 ? s.substring(1, p) : s.substring(1, s.length()-1);
                t.add(s);
                /*if (s.trim().length() == 0) {
                    System.out.println("(s) l: "+l);
                }*/
            }

            p = r.indexOf(" ");
            s = r.substring(0, p);
            r = r.substring(p+1);
            if (s.startsWith("<")) {
                p = s.lastIndexOf("/");
                s = s.substring(1, s.length()-1); //p >= 0 ? s.substring(1, p) : s.substring(1, s.length()-1);
                //s = p >= 0 ? s.substring(1, p) : s.substring(1, s.length()-1);
                t.add(s);
                /*if (s.trim().length() == 0) {
                    System.out.println("(p) l: "+l);
                }*/
            }
            if (r.startsWith("<")) {
                p = r.lastIndexOf("/");
                s = r.substring(1, r.length()-3); //p >= 0 ? r.substring(1, p) : r.substring(1, r.length()-3);
                //s = p >= 0 ? r.substring(1, p) : r.substring(1, r.length()-3);
                t.add(s);
                /*if (s.trim().length() == 0) {
                    System.out.println("(o) l: "+l);
                }*/
            }
            //System.out.println("after adding "+l+": "+t);
            l = br.readLine();
        }
        br.close();
        return t;
    }

    public static void main(String[] args) throws Exception {
        int m = Integer.parseInt(args[0]);
        String file = args[1];
        int bmax = Integer.parseInt(args[2]);
        int fmax = Integer.parseInt(args[3]);
        RadixTree t1 = simpleLoadTree(file, m, bmax, fmax);
        t1.ensureMax();
        System.out.println(t1);
        System.out.println(t1.getNumberOfLeaves());
    }

    public static void main0(String[] args) throws Exception {
        int m = Integer.parseInt(args[0]);
        String file = args[1];
        int bmax = Integer.parseInt(args[3]);
        int fmax = Integer.parseInt(args[4]);
        RadixTree t1 = loadTree(file, m, bmax, fmax);
        t1.ensureMax();
        System.out.println(t1);
        System.out.println(t1.getNumberOfLeaves());
        file = args[2];
        RadixTree t2 = loadTree(file, m, bmax, fmax);
        t2.ensureMax();
        System.out.println(t2);
        System.out.println(t2.getNumberOfLeaves());
        System.out.println("Overlap: "+t1.getOverlap(t2));
    }
}
