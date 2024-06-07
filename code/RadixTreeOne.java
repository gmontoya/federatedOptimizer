import java.util.*;
import java.io.*;

class RadixTreeOne implements Serializable {

    private static final long serialVersionUID = 5221508779908127382L;
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
    int threshold;
    String sep;
    private PriorityQueue<NodeRadix> pq;
    //NodeRadix otherRoot = null;
    QTreeOne defaultValues;

    public RadixTreeOne(String s, int m, int bm, int fm, int th) {
        max = m;
        sep = s;
        bmax = bm;
        fmax = fm;
        pq = new PriorityQueue<NodeRadix>(new NodeRadixMinComparator());
        threshold = th;
    }

    public HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> getOverlap(RadixTreeOne rt) {
        HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> res = new HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>();
        /*if (otherRoot != null && rt.otherRoot!=null) {
            res = otherRoot.getOverlap("", rt.otherRoot, "");
        } */
        if (root != null && rt.root!=null) {
            res = QTreeOne.combineCPs(res, root.getOverlap("", rt.root, ""));
        }
        /*if (otherRoot != null && rt.root!=null) {
            res = QTreeOne.combineCPs(res, otherRoot.getOverlap("", rt.root, ""));
        }
        if (root != null && rt.otherRoot!=null) {
            res = QTreeOne.combineCPs(res, root.getOverlap("", rt.otherRoot, ""));
        }*/
        return res;
    }

    public HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> getOverlapOld(RadixTreeOne rt) {
        HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> defaultOverlap = new HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>();
        if (defaultValues != null && rt != null && rt.defaultValues != null) {
            defaultOverlap = defaultValues.getOverlap(rt.defaultValues);
        }

        if (root == null || rt == null || rt.root == null) {
            return defaultOverlap;
        } else {
            return QTreeOne.combineCPs(defaultOverlap, root.getOverlap("", rt.root, ""));
        }
    }

    public HashMap<Integer, HashMap<Integer, Integer>> getFOverlap(RadixTreeOne rt) {
        HashMap<Integer, HashMap<Integer, Integer>> res = new HashMap<Integer, HashMap<Integer, Integer>>();
        /*if (defaultValues != null && rt != null && rt.defaultValues != null) {
            defaultOverlap = defaultValues.getFOverlap(rt.defaultValues);
        }*/

        if (root != null && rt != null && rt.root != null) {
            res = QTreeOne.combineFCSs(res, root.getFOverlap("", rt.root, ""));
        }
        return res;
    }

    public int getNumberOfLeaves() {
        int n = 0;
        if (root != null) {
            n += root.getNumberOfLeaves();
        }
        /*if (otherRoot != null) {
            n += otherRoot.getNumberOfLeaves();
        }*/
        return n;
    }

    public void buildQTrees() {
        HashSet<LeafRadix> ls = new HashSet<LeafRadix>();
        //boolean b = true;
        while (max >= 0 &&  getNumberOfLeaves()>max && pq.size()>0) {
            //System.out.println("num leaves: "+getNumberOfLeaves());
            //System.out.println("pq.size(): "+pq.size());
            NodeRadix n = pq.poll();
            //System.out.println("n: "+n);
            //b = n.num <= 1;
            //if (max >= 0 || b) {
            if (n.parent != null) {
                /*NodeRadix x = n.parent;
                int i = 0;
                while (x!=null && i < 100) {
                    System.out.println("x: "+x);
                    x = x.parent;
                    i++;
                }*/
                ((BranchRadix) n.parent).removeChild(n);
            }
            //n.parent = null;
            ls.add((LeafRadix) n);
            //} else {
            //    break;
            //}
        }
        addToDefault(ls);
        if (root != null) {
            root.buildQTrees(); // use valuesStr to make values and delete valuesStr
        }
        /*if (otherRoot != null) {
            otherRoot.buildQTrees(); // use valuesStr to make values and delete valuesStr
        }*/
    }

    public static int combine(int hc, String pref) {
        if (pref == null || pref.length() == 0) {
            return hc;
        }
        int x = ((int) Math.floor(Math.log(hc)/Math.log(31)));
        int y = (int) (pref.hashCode()*Math.pow(31,x)+hc);
        return y;
    }

    public void addToDefault(HashSet<LeafRadix> ls) {

        //System.out.println("Loading info from "+ls.size()+" leaves");
        TreeSet<Integer> vs = new TreeSet<Integer>();
        HashMap<Integer, Integer> vsCs = new HashMap<Integer, Integer>();
        HashMap<Integer, HashMap<String, HashSet<Integer>>> vsPsCs = new HashMap<Integer, HashMap<String, HashSet<Integer>>>();
        defaultValues = new QTreeOne(bmax, fmax);
        for (LeafRadix l : ls) {
            String prefix = "";
            NodeRadix n = l;
            while (n != null) { 
                prefix = n.radix + prefix; //(n.radix.length() == 0) ? prefix : (n.radix + "/" + prefix);
                n = n.parent;
            }
            l.parent = null;
            //System.out.println("l.valuesSStr.size(): "+l.valuesSStr.size());
            //System.out.println("l.valuesOStr.size(): "+l.valuesOStr.size());
            RadixTreeOne.buildQTreeAux(l.valuesS, l.valuesO, prefix, vs, vsCs, vsPsCs);
        }
        Vector<Integer> ves = new Vector(vs);
        //System.out.println("Loading info from "+ves.size()+" entities");
        RadixTreeOne.smartLoading(defaultValues, ves, vsCs, vsPsCs, 0, ves.size()-1);

    }

    public static void smartLoading(QTreeOne qt, Vector<Integer> es, HashMap<Integer, Integer> vsCs, HashMap<Integer, HashMap<String, HashSet<Integer>>> vsPsCs, int inf, int sup) {
        //System.out.println("es.size(): "+es.size());
        //System.out.println("vsCs.size(): "+vsCs.size()); 
        //System.out.println("vsPsCs.size(): "+vsPsCs.size());
        LinkedList<Integer> ll1 = new LinkedList<Integer>();
        LinkedList<Integer> ll2 = new LinkedList<Integer>();
        ll1.add(inf);
        ll2.add(sup);
        while(ll1.size()>0) {
            int i = ll1.removeFirst();
            int s = ll2.removeFirst();
            if (i < s) {
                int m = (int) Math.round(Math.floor((i+s)/2.0));
                Integer e = es.get(m);         
                include(e, qt, vsCs, vsPsCs);      
                ll1.add(i);
                ll2.add(m-1);
                ll1.add(m+1);
                ll2.add(s);
            } else if (i==s) {
                Integer e = es.get(i);
                include(e, qt, vsCs, vsPsCs);
            }
        }
    }  

    public static void include(Integer e, QTreeOne qt, HashMap<Integer, Integer> vsCs, HashMap<Integer, HashMap<String, HashSet<Integer>>> vsPsCs) {
                if (vsCs.containsKey(e)) {
                    int cs = vsCs.get(e);
                    //System.out.println("e: "+e+". cs: "+cs);
                    qt.addS(e, cs);
                }
                if (vsPsCs.containsKey(e)) {
                    HashMap<String, HashSet<Integer>> psCss = vsPsCs.get(e);
                    for (String p : psCss.keySet()) {
                         HashSet<Integer> css = psCss.get(p);
                         for (Integer cs : css) {
                             /*if (e == 767287329 && cs == -1001423639 && p.equals("<http://bio2rdf.org/ns/bio2rdf#xRef>")) {
                                 System.out.println("e: "+e+". cs: "+cs+". p: "+p);
                             }*/
                             qt.addO(e, cs, p);
                         }
                    }
                }
//System.out.println("after include, numBuckets of defaultValues: "+qt.getNumBuckets());
    }

    public void ensureMax() {

        while (max >= 0 && getNumberOfLeaves()>max && pq.size()>0) {
            NodeRadix n = pq.poll();
            if (n.parent != null) {
                ((BranchRadix) n.parent).removeChild(n);
            }
        }
    }

    public String toString() {
        String s = "";
        /*if (defaultValues != null) {
            s = defaultValues.toString();
        }*/
        if (root == null) {
            s = s + "-";
        } else {
            s = s + root.toString();
        }
        /*if (otherRoot == null) {
            s = s + "-";
        } else {
            s = s + otherRoot.toString();
        }*/
        return s;
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

    public QTreeOne getQTree(String str) {
        NodeRadix n = root;
        //System.out.println("Current root: "+root);
        //System.out.println("adding n: "+n);
        NodeRadix p = null;
        
        while (n != null && str.startsWith(n.radix)) {
                p = n;
                //System.out.println("p.radix: "+p.radix);
                str = str.substring(n.radix.length());
                /*if (str.length() > 0 && str.charAt(0) == '/') {
                    str = str.substring(1);
                }*/
                if (n instanceof BranchRadix) {
                    /*for (NodeRadix x : ((BranchRadix) n).children) {
                        System.out.println("child.radix: "+x.radix);
                    }*/
                    n = findMatching(((BranchRadix) n).children, str);
                } else {
                    n = null;
                }
        }
        //System.out.println("n: "+n+". p: "+p+". str: "+str+". value: "+value);
        if (str.length() == 0) { // CHECK THIS CASE
            if (p instanceof LeafRadix)
                return ((LeafRadix) p).values;
            NodeRadix ec = ((BranchRadix) p).emptyChild();
            return ((LeafRadix) ec).values;
        }
        return null;
    }

    public LeafRadix getLeafRadix(String prefix, NodeRadix n) {
        NodeRadix p = null;
        while (n != null && prefix.startsWith(n.radix)) {
            p = n;
            prefix = prefix.substring(n.radix.length());
            if (n instanceof BranchRadix) {
                n = findMatching(((BranchRadix) n).children, prefix);
            } else {
                n = null;
            }
        }
        if (prefix.length() == 0) { 
            if (p instanceof LeafRadix) {
                return ((LeafRadix) p);
            } else {
                NodeRadix ec = ((BranchRadix) p).emptyChild();
                if (ec != null) {
                    return ((LeafRadix) ec);
                }
            }
        }
        return null;
    }

    public Set<LeafRadix> getLeaves() {

        Queue<NodeRadix> queue= new LinkedList<NodeRadix>();
        HashSet<LeafRadix> res = new HashSet<LeafRadix>();
        queue.add(root);
        while(queue.size()>0) {
            NodeRadix rn = queue.poll();
            if (rn instanceof LeafRadix) {
                LeafRadix l = (LeafRadix) rn;
                res.add(l);
            } else {
                for (NodeRadix c : ((BranchRadix) rn).children) {
                    queue.add(c);
                }
            }
        }
        return res;
    }

    public void addAll(RadixTreeOne other) {
        Set<LeafRadix> ls = other.getLeaves();
        for (LeafRadix l : ls) {
            addLeaf(l, root, true);
        }
    }

    public void addLeaf(LeafRadix l, NodeRadix n, boolean main) {
        //System.out.println("Current root: "+root);
        //System.out.println("adding n: "+n);
        String radix = l.radix;
        NodeRadix p = null;
        l.parent = null;
        while (n != null && radix.startsWith(n.radix)) {
            p = n;
            radix = radix.substring(n.radix.length());
            if (n instanceof BranchRadix) {
                n = findMatching(((BranchRadix) n).children, radix);
            } else {
                n = null;
            }
        }
        //System.out.println("n: "+n+". p: "+p+". str: "+str+". value: "+value);
        if (radix.length() == 0) { // CHECK THIS CASE
            if (p instanceof LeafRadix) {
                pq.remove(p);
                ((LeafRadix) p).addAllValues(l);
                pq.add(p);
                return;
            } else {
                NodeRadix ec = ((BranchRadix) p).emptyChild();
                //System.out.println("n: "+n+". p: "+p+". ec: "+ec+". value: "+value);
                if (ec == null) {
                    n = l; //new LeafRadix(radix, null);
                    //((LeafRadix) n).addValueS(value, cs);
                    pq.add(n);
                } else {
                    n = ec;
                    pq.remove(n);
                    ((LeafRadix) n).addAllValues(l);
                    pq.add(n);
                    return;
                }
            }
        } else {  
             NodeRadix c = n;          
             n = l; //new LeafRadix(radix, null);
             n.radix = radix;
             //((LeafRadix) n).addValueS(value, cs);
             pq.add(n);
                if (p != null && c != null && p instanceof BranchRadix) {
                    ((BranchRadix) p).removeChild(c);
                } 
                if (c != null) {
                    int pos = getLongestCommonPrefix(c.radix, radix);
                    String prefix = radix.substring(0, pos);
                    BranchRadix b = new BranchRadix(prefix, null);
                    c.radix = c.radix.substring(prefix.length());
                    c.setParent(b);
                    b.addChild(c);
                    n.radix = radix.substring(prefix.length());
                    n.setParent(b);
                    b.addChild(n);
                    n = b;
                } else if (p instanceof LeafRadix) {
                    String prefix = p.radix;
                    BranchRadix b = new BranchRadix(prefix, null);
                    LeafRadix nl = (LeafRadix) p;
                    p = p.parent;
                    if (p!=null) {
                        ((BranchRadix) p).removeChild(nl);
                    }
                    nl.setParent(b);
                    b.addChild(nl);
                    nl.radix = "";
                    n.setParent(b);
                    b.addChild(n);
                    n = b;
                }
        }
        if (p == null) {
            if (main) {
                root = n;
            } /*else {
                otherRoot = n;
            }*/
        } else {
            n.setParent(p);
            ((BranchRadix) p).addChild(n);
        }
    }

    public void addS(String str, Integer cs) {
        NodeRadix n = root;
        //System.out.println("Current root: "+root);
        //System.out.println("adding n: "+n);
        NodeRadix p = null;
        int i = getIndex(str);
        //int pos = str.lastIndexOf("/"); //getBreak(str); //str.lastIndexOf("/");
        String value;
        if (i > 0) {
            value = str.substring(i+1);
            str = str.substring(0, i);
        } else {
            return;
        }
        while (n != null && str.startsWith(n.radix)) {
            p = n;
                str = str.substring(n.radix.length());
                /*if (str.length() > 0 && str.charAt(0) == '/') {
                    str = str.substring(1);
                }*/
                if (n instanceof BranchRadix) {
                    n = findMatching(((BranchRadix) n).children, str);
                } else {
                    n = null;
                }
        }
        //System.out.println("n: "+n+". p: "+p+". str: "+str+". value: "+value);
        if (str.length() == 0) { // CHECK THIS CASE
            if (p instanceof LeafRadix) {
                pq.remove(p);
                ((LeafRadix) p).addValueS(value, cs);
                pq.add(p);
                return;
            } else {
                NodeRadix ec = ((BranchRadix) p).emptyChild();
                //System.out.println("n: "+n+". p: "+p+". ec: "+ec+". value: "+value);
                if (ec == null) {
                    n = new LeafRadix(str, null);
                    ((LeafRadix) n).addValueS(value, cs);
                    pq.add(n);
                } else {
                    n = ec;
                    pq.remove(n);
                    ((LeafRadix) n).addValueS(value, cs);
                    pq.add(n);
                    return;
                }
            }
        } else {  
             NodeRadix c = n;          
             n = new LeafRadix(str, null);
             ((LeafRadix) n).addValueS(value, cs);
             pq.add(n);
             //if (p != null) {
                //NodeRadix c;
                /*if (p instanceof BranchRadix) {
                    c = findMatching(((BranchRadix) p).children, str);
                } else {
                    c = p;
                    p = p.parent;
                }*/
                if (p != null && c != null && p instanceof BranchRadix) {
                    ((BranchRadix) p).removeChild(c);
                } /*else if (p instanceof LeafRadix) {
                    c = p;
                    p = p.parent;
                }*/
                if (c != null) {
                    int pos = getLongestCommonPrefix(c.radix, str);
                    String prefix = str.substring(0, pos);
                    BranchRadix b = new BranchRadix(prefix, null);
                    c.radix = c.radix.substring(prefix.length());
                    c.setParent(b);
                    b.addChild(c);
                    n.radix = str.substring(prefix.length());
                    /*if (b.radix.length() > 0 && b.radix.charAt(b.radix.length()-1) == '/') {
                        b.radix = b.radix.substring(0, b.radix.length()-1);
                    }*/
                    n.setParent(b);
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
                    l.setParent(b);
                    b.addChild(l);
                    l.radix = "";
                    n.setParent(b);
                    b.addChild(n);
                    n = b;
                }
             //}
        }
        //System.out.println("new n: "+n);
        if (p == null) {
            root = n;
        } else {
            n.setParent(p);
            ((BranchRadix) p).addChild(n);
        }
    }
    public static int getIndex(String str) {
        int i = str.indexOf("/");
        int j = str.indexOf("/", i+1);
        if (j == i+1) {
            j = str.indexOf("/", j+1);
        }
        if (j>0) {
            return j;
        } else if (i>0) {
            return i;
        } else {
            return -1;
        }
    }

    public static int getIndex0(String str) {
        int pos = str.lastIndexOf("/");
        if (pos < 0) {
            return -1;
        } else {
            return pos;
        }
    }

/*    public void addSNew(String str, Integer cs) {
        //int pos = str.lastIndexOf("/");
        String value;
        int i = getIndex(str);
        if (i>0) {
            value = str.substring(i+1);
            str = str.substring(0, i);
        } else {
            return;
        }

        LeafRadix l = getLeafRadix(str, root);
        //System.out.println("Leaf for "+str+" in root "+root+": "+l);
        if (l != null) {
            pq.remove(l);
            l.addValueS(value, cs);
            pq.add(l);
            return;
        }
        l = getLeafRadix(str, otherRoot);
        //System.out.println("Leaf for "+str+" in otherRoot "+otherRoot+": "+l);
        if (l != null) {
            pq.remove(l);
            l.addValueS(value, cs);
            if (l.values.getCount()>threshold) {
                if (l.parent != null && (l.parent instanceof BranchRadix)) {
                    ((BranchRadix) (l.parent)).removeChild(l);
                } else {
                    otherRoot = null;
                }
                    NodeRadix x = l;
                    String radix = "";
                    while (x != null) {
                        radix=x.radix+radix;
                        x=x.parent;
                    }
                    l.radix = radix;
                    addLeaf(l, root, true);
            } else {
                pq.add(l);
            }
            return;
        }
        l = new LeafRadix(str, null);
        l.addValueS(value, cs);
        NodeRadix x = l;
        String radix = "";
        while (x != null) {
            radix=x.radix+radix;
            x=x.parent;
        }
        l.radix = radix;
        addLeaf(l, otherRoot, false);
    }*/

/*
    public void addONew(String str, Integer cs, String pred) {
        //int pos = str.lastIndexOf("/");
        String value;
        int i = getIndex(str);
        if (i>0) {
            value = str.substring(i+1);
            str = str.substring(0, i);
        } else {
            return;
        }
        LeafRadix l = getLeafRadix(str, root);
        //System.out.println("Leaf for "+str+" in root "+root+": "+l);
        if (l != null) {
            pq.remove(l);
            l.addValueO(value, cs, pred);
            pq.add(l);
            return;
        }
        l = getLeafRadix(str, otherRoot);
        //System.out.println("Leaf for "+str+" in otherRoot "+otherRoot+": "+l);
        if (l != null) {
            pq.remove(l);
            l.addValueO(value, cs, pred);
            if (l.values.getCount()>threshold) {
                if (l.parent != null && (l.parent instanceof BranchRadix)) {
                    ((BranchRadix) (l.parent)).removeChild(l);
                } else {
                    otherRoot = null;
                }
                    NodeRadix x = l;
                    String radix = "";
                    while (x != null) {
                        radix=x.radix+radix;
                        x=x.parent;
                    }
                    l.radix = radix;
                    addLeaf(l, root, true);
            } else {
                pq.add(l);
            }
            return;
        }
        l = new LeafRadix(str, null);
        l.addValueO(value, cs, pred);
        NodeRadix x = l;
        String radix = "";
        while (x != null) {
            radix=x.radix+radix;
            x=x.parent;
        }
        l.radix = radix;
        addLeaf(l, otherRoot, false);
        //System.out.println("(end) root: "+root+". otherRoot: "+otherRoot);
    }
*/
    public void addO(String str, Integer cs, String pred) {
        //System.out.println("adding "+str+" with cs: "+cs+" and pred "+pred);
        NodeRadix n = root;
        NodeRadix p = null;
        int i = getIndex(str);
        //int pos = str.lastIndexOf("/"); //getBreak(str); //str.lastIndexOf("/");
        String value;
        if (i > 0) {
            value = str.substring(i+1);
            str = str.substring(0, i);
        } else {
            return;
        }
        /*if (pos < 0) {
            return;
        } else {
            value = str.substring(pos+1);
            str = str.substring(0, pos);
        }*/
        while (n != null && str.startsWith(n.radix)) {
            p = n;
            //if (str.startsWith(n.radix)) {
                str = str.substring(n.radix.length());
                /*if (str.length() > 0 && str.charAt(0) == '/') {
                    str = str.substring(1);
                }*/
                if (n instanceof BranchRadix) {
                    n = findMatching(((BranchRadix) n).children, str);
                } else {
                    n = null;
                }
            /*} else {
                n = null;
            }*/
        }
        //System.out.println("(O) n: "+n+". p: "+p+". str: "+str+". value: "+value);
        if (str.length() == 0) { // CHECK THIS CASE
            if (p instanceof LeafRadix) {
                pq.remove(p);
                ((LeafRadix) p).addValueO(value, cs, pred);
                pq.add(p);
                return;
            } else {
                NodeRadix ec = ((BranchRadix) p).emptyChild();
                if (ec == null) {
                    n = new LeafRadix(str, null);
                    ((LeafRadix) n).addValueO(value, cs, pred);
                    pq.add(n);
                } else {
                    n = ec;
                    pq.remove(n);
                    ((LeafRadix) n).addValueO(value, cs, pred);
                    pq.add(n);
                    return;
                }
            }
        } else {
             NodeRadix c = n;
             n = new LeafRadix(str, null);
             ((LeafRadix) n).addValueO(value, cs, pred);
             pq.add(n);
             //if (p != null) {
                /*NodeRadix c;
                if (p instanceof BranchRadix) {
                    c = findMatching(((BranchRadix) p).children, str);
                } else {
                    c = p;
                    p = p.parent;
                }*/
                if (p != null && c != null && p instanceof BranchRadix) {
                    ((BranchRadix) p).removeChild(c);
                } 
                //System.out.println("c: "+c+". p: "+p);
                if (c != null) {
                    int pos = getLongestCommonPrefix(c.radix, str);
                    String prefix = str.substring(0, pos);
                    BranchRadix b = new BranchRadix(prefix, null);
                    c.radix = c.radix.substring(prefix.length());
                    c.setParent(b);
                    b.addChild(c);
                    n.radix = str.substring(prefix.length());
                    /*if (b.radix.length() > 0 && b.radix.charAt(b.radix.length()-1) == '/') {
                        b.radix = b.radix.substring(0, b.radix.length()-1);
                    }*/
                    n.setParent(b);
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
                    l.setParent(b);
                    b.addChild(l);
                    l.radix = "";
                    n.setParent(b);
                    b.addChild(n);
                    n = b;
                }
             //}
        }
        if (p == null) {
            root = n;
        } else {
            n.setParent(p);
            ((BranchRadix) p).addChild(n);
        }
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
            setParent(p);
            radix = r;
            num = 0;
            numLeaves = 0;
        }
        public void setParent(NodeRadix p) {
            /*NodeRadix x = p;
            while (x!=null) {
                if (x == this) {
                    System.out.println("node "+this+" is its own ancestor");
                    System.exit(1);
                }
                x = x.parent;
            }*/
            parent = p;
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
        public abstract HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> getOverlap(String pref1, NodeRadix other, String pref2);
        public abstract HashMap<Integer, HashMap<Integer, Integer>> getFOverlap(String pref1, NodeRadix other, String pref2);
        public int getNumberOfLeaves() {
            return numLeaves;
        }
        public abstract void buildQTrees();
 
        public abstract boolean equals(Object o);
    }

    public static HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> getOverlap(NodeRadix n1, NodeRadix n2) {
            LeafRadix l1 = (LeafRadix) n1;
            LeafRadix l2 = (LeafRadix) n2;
            HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> o = l1.values.getOverlap(l2.values);
            //System.out.println("o: "+o);
            return o;
    }

    public static HashMap<Integer, HashMap<Integer, Integer>> getFOverlap(NodeRadix n1, NodeRadix n2) {
            LeafRadix l1 = (LeafRadix) n1;
            LeafRadix l2 = (LeafRadix) n2;
            HashMap<Integer, HashMap<Integer, Integer>> o = l1.values.getFOverlap(l2.values);
            //System.out.println("o: "+o);
            return o;
    }

    class BranchRadix extends NodeRadix implements Serializable {
        private static final long serialVersionUID = 8964534877945125548L;
        private TreeSet<NodeRadix> children;

        public BranchRadix(String r, NodeRadix p) {
            super(r, p);
            children = new TreeSet<NodeRadix>(new NodeRadixComparator());
        }
/*
        private HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> getOverlap(Set<NodeRadix> s1, String pref1, Set<NodeRadix> s2, String pref2) {

            HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> o = new HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>();
            for (NodeRadix n1 : s1) {
                for (NodeRadix n2 : s2) {
                    o = QTreeOne.combineCPs(o, n1.getOverlap(pref1, n2, pref2));
                }
            }
            return o;
        }
*/
        private HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> getOverlap(Iterator<NodeRadix> it1, String pref1, Iterator<NodeRadix> it2, String pref2) {
            
            HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> o = new HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>();
            if (it1 == null || it2 == null || !it1.hasNext() || !it2.hasNext()) {
                return o;
            }
            int lcp = getLongestCommonPrefix(pref1, pref2); 
            //System.out.println("pref1: "+pref1+". pref2: "+pref2+". lcp: "+lcp);

            if (lcp == pref1.length() && lcp != pref2.length()) {
                String p = pref2.substring(pref1.length());
                NodeRadix n = null, ec = null;
                while (it1.hasNext()) {
                    NodeRadix tmp = it1.next();
                    if (getLongestCommonPrefix(tmp.radix, p)>0) {
                        n = tmp;
                        break;
                    } else if (tmp.radix.length() == 0) {
                        ec = tmp;
                    }
                }
                //System.out.println("n: "+n);
                if (n == null && ec == null) {
                    return o;
                } else if (n == null && ec != null) {
                    n = ec;
                }
                while (it2.hasNext()) {
                    NodeRadix tmp = it2.next();
                    o = QTreeOne.combineCPs(o, n.getOverlap("", tmp, p));
                }
            } else if (lcp == pref2.length() && lcp != pref1.length()) {
                String p = pref1.substring(pref2.length());
                NodeRadix n = null, ec = null;
                while (it2.hasNext()) {
                    NodeRadix tmp = it2.next();
                    if (getLongestCommonPrefix(tmp.radix, p)>0) {
                        n = tmp;
                        break;
                    } else if (tmp.radix.length() == 0) {
                        ec = tmp;
                    }
                }
                //System.out.println("n: "+n);
                if (n == null && ec == null) {
                    return o;
                } else if (n == null && ec != null) {
                    n = ec;
                }
                while (it1.hasNext()) {
                    NodeRadix tmp = it1.next();
                    o = QTreeOne.combineCPs(o, tmp.getOverlap(p, n, ""));
                }
            } else if (lcp == pref2.length() && lcp == pref1.length()) {
                NodeRadix n1 = it1.next();
                NodeRadix n2 = it2.next();
                do {
                    String r1 = pref1.concat(n1.radix); //.concat("/"));
                    String r2 = pref2.concat(n2.radix); //.concat("/"));
                    int c = r1.compareTo(r2);
                    //System.out.println("c: "+c);
                    if (c<0) {
                        if (r2.startsWith(r1)) {
                            o = QTreeOne.combineCPs(o, n1.getOverlap(pref1,n2,pref2));
                        }
                        if (it1.hasNext()) {
                            n1 = it1.next();
                            //System.out.println("advancing it1");
                        } else {
                            n1 = null;
                            //System.out.println("it1 ended");
                        }
                    } else if (c == 0) {
                        o = QTreeOne.combineCPs(o, n1.getOverlap(pref1,n2,pref2));
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
                            o = QTreeOne.combineCPs(o, n1.getOverlap(pref1,n2,pref2));
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

        private HashMap<Integer, HashMap<Integer, Integer>> getFOverlap(Iterator<NodeRadix> it1, String pref1, Iterator<NodeRadix> it2, String pref2) {
            HashMap<Integer, HashMap<Integer, Integer>> o = new HashMap<Integer, HashMap<Integer, Integer>>();
            if (it1 == null || it2 == null || !it1.hasNext() || !it2.hasNext()) {
                return o;
}
            int lcp = getLongestCommonPrefix(pref1, pref2); 
            //System.out.println("pref1: "+pref1+". pref2: "+pref2+". lcp: "+lcp);

            if (lcp == pref1.length() && lcp != pref2.length()) {
                String p = pref2.substring(pref1.length());
                NodeRadix n = null, ec = null;
                while (it1.hasNext()) {
                    NodeRadix tmp = it1.next();
                    if (getLongestCommonPrefix(tmp.radix, p)>0) {
                        n = tmp;
                        break;
                    } else if (tmp.radix.length() == 0) {
                        ec = tmp;
                    }
                }
                //System.out.println("n: "+n);
                if (n == null && ec == null) {
                    return o;
                } else if (n == null && ec != null) {
                    n = ec;
                }
                while (it2.hasNext()) {
                    NodeRadix tmp = it2.next();
                    o = QTreeOne.combineFCSs(o, n.getFOverlap("", tmp, p));
                }
            } else if (lcp == pref2.length() && lcp != pref1.length()) {
                String p = pref1.substring(pref2.length());
                NodeRadix n = null, ec = null;
                while (it2.hasNext()) {
                    NodeRadix tmp = it2.next();
                    if (getLongestCommonPrefix(tmp.radix, p)>0) {
                        n = tmp;
                        break;
                    } else if (tmp.radix.length() == 0) {
                        ec = tmp;
                    }
                }
                //System.out.println("n: "+n);
                if (n == null && ec == null) {
                    return o;
                } else if (n == null && ec != null) {
                    n = ec;
                }
                while (it1.hasNext()) {
                    NodeRadix tmp = it1.next();
                    o = QTreeOne.combineFCSs(o, tmp.getFOverlap(p, n, ""));
                }
            } else if (lcp == pref2.length() && lcp == pref1.length()) {
                NodeRadix n1 = it1.next();
                NodeRadix n2 = it2.next();
                do {
                    String r1 = pref1.concat(n1.radix); //.concat("/"));
                    String r2 = pref2.concat(n2.radix); //.concat("/"));
                    int c = r1.compareTo(r2);
                    //System.out.println("c: "+c);
                    if (c<0) {
                        if (r2.startsWith(r1)) {
                            o = QTreeOne.combineFCSs(o, n1.getFOverlap(pref1,n2,pref2));
                        }
                        if (it1.hasNext()) {
                            n1 = it1.next();
                            //System.out.println("advancing it1");
                        } else {
                            n1 = null;
                            //System.out.println("it1 ended");
                        }
                    } else if (c == 0) {
                        o = QTreeOne.combineFCSs(o, n1.getFOverlap(pref1,n2,pref2));
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
                            o = QTreeOne.combineFCSs(o, n1.getFOverlap(pref1,n2,pref2));
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
/*
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
*/
        public HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> getOverlap(String pref1, NodeRadix other, String pref2) {
            HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> o = new HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>();
            String r1 = pref1.concat(this.radix); //.length() == 0 ? "" : this.radix.concat("/"));
            String r2 = pref2.concat(other.radix); //.length() == 0 ? "" : other.radix.concat("/"));
            //System.out.println("(B) pref1: "+pref1+". pref2: "+pref2+". r1: "+r1+". r2: "+r2+". other is leaf: "+(other instanceof LeafRadix));
            if (other instanceof LeafRadix) {
                NodeRadix ec = emptyChild();
                //System.out.println("ec: "+ec);
                if (ec !=null && r1.equals(r2)) {
                    o = QTreeOne.combineCPs(o, RadixTreeOne.getOverlap(ec, other)); //Math.min(this.num, other.num);
                } else if (r2.startsWith(r1)) {
                    Iterator<NodeRadix> it1 = children.iterator();                    
                    ArrayList<NodeRadix> l = new ArrayList<NodeRadix>(1);
                    l.add(other);
                    Iterator<NodeRadix> it2 = l.iterator();
                    o = QTreeOne.combineCPs(o, getOverlap(it1, r1, it2, pref2)); //children, r1, new HashSet<NodeRadix>(l), pref2)); //it1, r1, it2, pref2));
                }
            } else if (r1.equals(r2)) {
                Iterator<NodeRadix> it1 = children.iterator();
                Iterator<NodeRadix> it2 = ((BranchRadix) other).children.iterator();
                o = QTreeOne.combineCPs(o, getOverlap(it1, "", it2, "")); //children, "", ((BranchRadix) other).children, "")); //it1, "", it2, ""));
            } else if (r1.startsWith(r2)) {
                pref1 = r1.substring(r2.length());
                Iterator<NodeRadix> it1 = children.iterator();
                Iterator<NodeRadix> it2 = ((BranchRadix) other).children.iterator();
                o = QTreeOne.combineCPs(o, getOverlap(it1, pref1, it2, "")); //children, pref1, ((BranchRadix) other).children, "")); //it1, pref1, it2, ""));
            } else if (r2.startsWith(r1)) {
                pref2 = r2.substring(r1.length());
                Iterator<NodeRadix> it1 = children.iterator();
                Iterator<NodeRadix> it2 = ((BranchRadix) other).children.iterator();
                o = QTreeOne.combineCPs(o, getOverlap(it1, "", it2, pref2)); //children, "", ((BranchRadix) other).children, pref2)); //it1, "", it2, pref2));
            }
            //System.out.println("overlap between "+this+" and "+other+": "+o);
            return o;
        }

        public HashMap<Integer, HashMap<Integer, Integer>> getFOverlap(String pref1, NodeRadix other, String pref2) {
            HashMap<Integer, HashMap<Integer, Integer>> o = new HashMap<Integer, HashMap<Integer, Integer>>();
            String r1 = pref1.concat(this.radix); //.length() == 0 ? "" : this.radix.concat("/"));
            String r2 = pref2.concat(other.radix); //.length() == 0 ? "" : other.radix.concat("/"));
            //System.out.println("(B) pref1: "+pref1+". pref2: "+pref2+". r1: "+r1+". r2: "+r2+". other is leaf: "+(other instanceof LeafRadix));
            if (other instanceof LeafRadix) {
                NodeRadix ec = emptyChild();
                //System.out.println("ec: "+ec);
                if (ec !=null && r1.equals(r2)) {
                    o = QTreeOne.combineFCSs(o, RadixTreeOne.getFOverlap(ec, other)); //Math.min(this.num, other.num);
                } else if (r2.startsWith(r1)) {
                    Iterator<NodeRadix> it1 = children.iterator();                    
                    ArrayList<NodeRadix> l = new ArrayList<NodeRadix>(1);
                    l.add(other);
                    Iterator<NodeRadix> it2 = l.iterator();
                    o = QTreeOne.combineFCSs(o, getFOverlap(it1, r1, it2, pref2));
                }
            } else if (r1.equals(r2)) {
                Iterator<NodeRadix> it1 = children.iterator();
                Iterator<NodeRadix> it2 = ((BranchRadix) other).children.iterator();
                o = QTreeOne.combineFCSs(o, getFOverlap(it1, "", it2, ""));
            } else if (r1.startsWith(r2)) {
                pref1 = r1.substring(r2.length());
                Iterator<NodeRadix> it1 = children.iterator();
                Iterator<NodeRadix> it2 = ((BranchRadix) other).children.iterator();
                o = QTreeOne.combineFCSs(o, getFOverlap(it1, pref1, it2, ""));
            } else if (r2.startsWith(r1)) {
                pref2 = r2.substring(r1.length());
                Iterator<NodeRadix> it1 = children.iterator();
                Iterator<NodeRadix> it2 = ((BranchRadix) other).children.iterator();
                o = QTreeOne.combineFCSs(o, getFOverlap(it1, "", it2, pref2));
            }
            //System.out.println("overlap between "+this+" and "+other+": "+o);
            return o;
        }

        private NodeRadix emptyChild() {
            //boolean h = false;
            Iterator<NodeRadix> it = children.iterator();
            while (it.hasNext()) {
                NodeRadix c = it.next();
                if (c.radix.compareTo("")>0) {
                    break;
                }
                if (c.radix.equals("")) {
                    if (c instanceof LeafRadix) {
                        return c;
                    } else {
                        it = ((BranchRadix) c).children.iterator();
                    }
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

        public void buildQTrees() {
            for (NodeRadix c : children) {
                c.buildQTrees();
            }
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
        private static final long serialVersionUID = 8964534877945125548L;
        QTreeOne values;
        HashMap<Integer, HashSet<Integer>> valuesS;
        HashMap<String, HashMap<Integer, HashSet<Integer>>> valuesO;
        boolean done = false;
        public LeafRadix(String r, NodeRadix p) {
            super(r, p);
            //qt = new QTree(bmax, fmax);
            numLeaves = 1;
            values = new QTreeOne(bmax, fmax);
            valuesS = new HashMap<Integer, HashSet<Integer>>();
            valuesO = new HashMap<String, HashMap<Integer, HashSet<Integer>>>();
            done = true; //
            /*if (r.trim().length() == 0) {
                System.out.println("(LeafRadix constructor) r: "+r);
            }*/
        }

        public void addAllValues(LeafRadix other) {
            values.addAll(other.values);
            updateNum(other.num);
        }

        public void addValueS(String v, Integer cs) {
            /*HashSet<Integer> vs = valuesS.get(cs);
            if (vs ==  null) {
                vs = new HashSet<Integer>();
            }
            vs.add(v.hashCode());
            valuesS.put(cs, vs);*/ //
            //System.out.println("in addValueS. added "+v+". vs.size(): "+vs.size()+". valuesSStr.size(): "+valuesSStr.size());
            /*if (v.equals("953987/") && cs == -405470945) {
                System.out.println("in addValueS. added "+v+". cs: "+cs);
            }*/
            values.addS(v.hashCode(), cs);
            updateNum(1);
            //qt.add(v.hashCode());
        }

        public void addValueO(String v, Integer cs, String p) {
            //Integer pHC = p.hashCode();
            /*HashMap<Integer, HashSet<Integer>> csVs = valuesO.get(p);
            if (csVs == null) {
                csVs = new HashMap<Integer, HashSet<Integer>>();
            }
            HashSet<Integer> vs = csVs.get(cs);
            if (vs ==  null) {
                vs = new HashSet<Integer>();
            }
            vs.add(v.hashCode());
            csVs.put(cs, vs);
            valuesO.put(p, csVs); */ //
            //System.out.println("in addValueO. added "+v+". vs.size(): "+vs.size()+". csVs.size(): "+csVs.size()+". valuesOStr.size(): "+valuesOStr.size());
            /*if (v.equals("953987/") && cs == 672184354) {
                System.out.println("in addValueO. added "+v+". cs: "+cs+". p: "+p);
                NodeRadix x = this;
                while (x != null) {
                    System.out.println("x.radix: "+x.radix);
                    x = x.parent;
                }
            }*/
            values.addO(v.hashCode(), cs, p);
            updateNum(1);
            //qt.add(v.hashCode());
        }

        public String toString() {
            return radix+" "+num+" "+values.toString()+"\n"; //+" "+valuesOStr.toString()+"\n";
        }

        protected String pprint(String i) {
            return i+radix+" "+num+" "+values.toString()+"\n"; //+" "+valuesOStr.toString()+"\n";
        }

        public HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> getOverlap(String pref1, NodeRadix other, String pref2) {
            HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> o = new HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>();
            //System.out.println("(L) pref1: "+pref1+". pref2: "+pref2);
            //System.out.println("this: "+this+".\n other: "+other);
            String r1 = pref1.concat(this.radix); //.length() == 0 ? "" : this.radix.concat("/"));
            String r2 = pref2.concat(other.radix); //.length() == 0 ? "" : other.radix.concat("/"));

            //System.out.println("(L) r1: "+r1+". r2: "+r2+". is other a leaf: "+(other instanceof LeafRadix));
            NodeRadix ec = (other instanceof BranchRadix) ? ((BranchRadix) other).emptyChild() : other;

            if (r1.equals(r2) && ((other instanceof LeafRadix) || (ec != null))) { //((BranchRadix) other).hasEmptyChild()) {                
                o = QTreeOne.combineCPs(o, RadixTreeOne.getOverlap(this, ec)); //Math.min(this.num, other.num);
            } else if ((other instanceof BranchRadix) && r1.startsWith(r2)) {
                BranchRadix b = (BranchRadix) other;
                Iterator<NodeRadix> it2 = b.children.iterator();                    
                ArrayList<NodeRadix> l = new ArrayList<NodeRadix>(1);
                l.add(this);
                Iterator<NodeRadix> it1 = l.iterator();
                o = QTreeOne.combineCPs(o, b.getOverlap(it1, pref1, it2, r2)); //new HashSet<NodeRadix>(l), pref1, b.children, r2)); //it1, pref1, it2, r2));
            }
            //System.out.println("overlap between "+this+" and "+other+": "+o);
            return o;
        }

        public HashMap<Integer, HashMap<Integer, Integer>> getFOverlap(String pref1, NodeRadix other, String pref2) {
            HashMap<Integer, HashMap<Integer, Integer>> o = new HashMap<Integer, HashMap<Integer, Integer>>();
            //System.out.println("(L) pref1: "+pref1+". pref2: "+pref2);
            //System.out.println("this: "+this+".\n other: "+other);
            String r1 = pref1.concat(this.radix); //.length() == 0 ? "" : this.radix.concat("/"));
            String r2 = pref2.concat(other.radix); //.length() == 0 ? "" : other.radix.concat("/"));

            //System.out.println("(L) r1: "+r1+". r2: "+r2+". is other a leaf: "+(other instanceof LeafRadix));
            NodeRadix ec = (other instanceof BranchRadix) ? ((BranchRadix) other).emptyChild() : other;

            if (r1.equals(r2) && ((other instanceof LeafRadix) || (ec != null))) { //((BranchRadix) other).hasEmptyChild()) {                
                o = QTreeOne.combineFCSs(o, RadixTreeOne.getFOverlap(this, ec)); //Math.min(this.num, other.num);
            } else if ((other instanceof BranchRadix) && r1.startsWith(r2)) {
                BranchRadix b = (BranchRadix) other;
                Iterator<NodeRadix> it2 = b.children.iterator();                    
                ArrayList<NodeRadix> l = new ArrayList<NodeRadix>(1);
                l.add(this);
                Iterator<NodeRadix> it1 = l.iterator();
                o = QTreeOne.combineFCSs(o, b.getFOverlap(it1, pref1, it2, r2));
            }
            //System.out.println("overlap between "+this+" and "+other+": "+o);
            return o;
        }

        public void buildQTrees() {
            TreeSet<Integer> vs = new TreeSet<Integer>();
            HashMap<Integer, Integer> vsCs = new HashMap<Integer, Integer>();
            HashMap<Integer, HashMap<String, HashSet<Integer>>> vsPsCs = new HashMap<Integer, HashMap<String, HashSet<Integer>>>();
            RadixTreeOne.buildQTreeAux(valuesS, valuesO, "", vs, vsCs, vsPsCs);
            Vector<Integer> ves = new Vector(vs);
            RadixTreeOne.smartLoading(values, ves, vsCs, vsPsCs, 0, ves.size()-1);
            done = true;
        }
        public boolean equals(Object o) {
            if (o == null || !(o instanceof LeafRadix)) {
                return false;
            }
            LeafRadix l = (LeafRadix) o;
            boolean b = (done == l.done) && (done ? values.equals(l.values) : (valuesS.equals(l.valuesS) && valuesO.equals(l.valuesO)));
            b = b && radix.equals(l.radix);
            return b;
        }
    }

    public static void buildQTreeAux(HashMap<Integer, HashSet<Integer>> valuesS, HashMap<String, HashMap<Integer, HashSet<Integer>>> valuesO, String prefix, TreeSet<Integer> vs, HashMap<Integer, Integer> vsCs, HashMap<Integer, HashMap<String, HashSet<Integer>>> vsPsCs) {
            for (Integer cs : valuesS.keySet()) {
                HashSet<Integer> vss = valuesS.get(cs);
                for (Integer v : vss) {
                    //Integer s = prefix.hashCode() + v;
                    int hc = combine(v, prefix); //prefix.hashCode() + v.hashCode();
                    vs.add(hc);
                    vsCs.put(hc,cs);
                }
            }
            valuesS.clear();
            for (String p : valuesO.keySet()) {
                HashMap<Integer, HashSet<Integer>> csVs = valuesO.get(p);
                for (Integer cs : csVs.keySet()) {
                    HashSet<Integer> vss = csVs.get(cs);
                    for (Integer v : vss) {
                        int hc = combine(v, prefix);
                        //int hc = s.hashCode();
                        vs.add(hc);
                        HashMap<String, HashSet<Integer>> psCs = vsPsCs.get(hc);
                        if (psCs == null) {
                            psCs = new HashMap<String, HashSet<Integer>>();
                        }
                        HashSet<Integer> css = psCs.get(p);
                        if (css == null) {
                            css = new HashSet<Integer>();
                        }
                        css.add(cs);
                        psCs.put(p, css);
                        vsPsCs.put(hc,psCs);
                    }
                }
            }
            valuesO.clear();
    }

    public static RadixTreeOne simpleLoadTree(String file, int m, int bm, int fm, int th) throws Exception {
        RadixTreeOne t = new RadixTreeOne("/", m, bm, fm, th);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String l = br.readLine();

        while (l!=null) {
            StringTokenizer st = new StringTokenizer(l, " ");
            String e = st.nextToken();
            int cs = Integer.parseInt(st.nextToken());
            String p = st.nextToken();
            t.addO(e,cs,p);
            l = br.readLine();
        }
        br.close();
        return t;
    }

    public static RadixTreeOne loadTree(String file, int m, int bm, int fm, int th) throws Exception {
        RadixTreeOne t = new RadixTreeOne("/", m, bm, fm, th);
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
                //System.out.println("addS: *"+s+"*");
                t.addS(s,0);
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
                ////t.addS(s,0);
                /*if (s.trim().length() == 0) {
                    System.out.println("(p) l: "+l);
                }*/
            }
            if (r.startsWith("<")) {
                p = r.lastIndexOf("/");
                s = r.substring(1, r.length()-3); //p >= 0 ? r.substring(1, p) : r.substring(1, r.length()-3);
                //s = p >= 0 ? r.substring(1, p) : r.substring(1, r.length()-3);
                t.addO(s,0,"");
                //System.out.println("addO: *"+s+"*");
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
        //String file1 = args[1];
        //String file2 = args[2];
        int bmax = Integer.parseInt(args[1]);
        int fmax = Integer.parseInt(args[2]);
        int th = Integer.parseInt(args[3]);
        String file = args[4];
        RadixTreeOne rt = simpleLoadTree(file, m, bmax, fmax, th);
        System.out.println(rt);
    }
    public static void maini1(String[] args) throws Exception {
        int m = Integer.parseInt(args[0]);
        //String file1 = args[1];
        //String file2 = args[2];
        int bmax = Integer.parseInt(args[1]);
        int fmax = Integer.parseInt(args[2]);
        int th = Integer.parseInt(args[3]);
        /*String str = args[5];
        int pos = str.lastIndexOf("/"); //getBreak(str); //str.lastIndexOf("/");
        String value;
        if (pos < 0) {
            return;
        } else {
            value = str.substring(pos+1);
            str = str.substring(0, pos);
        }*/
        RadixTreeOne t1 =  new RadixTreeOne("/", m, bmax, fmax, th); //loadTree(file1, m, bmax, fmax);
        RadixTreeOne t2 = new RadixTreeOne("/", m, bmax, fmax, th); // loadTree(file2, m, bmax, fmax);
        t1.addS("http:/bla/ble/bli", 0);
        t1.addS("http://blo/blu/bla", 0);
        t1.addS("http:///ble/bli/blo", 0);
        t1.addS("http://blo/bli/ble", 0);
        t1.addS("http://blo/blu/bly", 0);
        t1.addS("http://blo/bli/bla", 0);
        t1.addS("http://blo/bli/ble1", 0);
        t1.addS("http://blo/blu/bly1", 0);
        t1.addS("http://blo/bli/bla1", 0);
        t1.addS("http://blo/bli/ble2", 0);
        t1.addS("http://blo/blu/bly2", 0);
        t1.addS("http://blo/bli/bla2", 0);
        //t1.buildQTrees();
        System.out.println(t1);
        t2.addS("http:///ble/bli/blo", 0);
        t2.addS("http://blo/blu/bla", 0);
        t2.addS("http:/bla/ble/bli", 0);
        //System.out.println(t1.getNumberOfLeaves());
        //t2.buildQTrees();
        System.out.println(t2);
        //System.out.println(t2.getNumberOfLeaves());
        /*QTreeOne qt1 = t1.getQTree(str);
        QTreeOne qt2 = t2.getQTree(str);
        QTreeOne.QTreeOneLeaf l1 = qt1.getLeaf(value.hashCode());
        QTreeOne.QTreeOneLeaf l2 = qt2.getLeaf(value.hashCode());
        System.out.println(qt1.getOverlap(qt2));
        System.out.println(qt2.getOverlap(qt1));
        System.out.println(QTreeOne.getOverlap(l1, l2));
        System.out.println(QTreeOne.getOverlap(l2, l1));*/
        //System.out.println("Overlap 1 2: "+t1.getOverlap(t2));
        //System.out.println("Overlap 2 1: "+t2.getOverlap(t1));
    }
/*
    public static void main0(String[] args) throws Exception {
        int m = Integer.parseInt(args[0]);
        String file = args[1];
        int bmax = Integer.parseInt(args[3]);
        int fmax = Integer.parseInt(args[4]);
        RadixTreeOne t1 = loadTree(file, m, bmax, fmax);
        t1.ensureMax();
        System.out.println(t1);
        System.out.println(t1.getNumberOfLeaves());
        file = args[2];
        RadixTreeOne t2 = loadTree(file, m, bmax, fmax);
        t2.ensureMax();
        System.out.println(t2);
        System.out.println(t2.getNumberOfLeaves());
        System.out.println("Overlap 1 2: "+t1.getOverlap(t2));
        System.out.println("Overlap 2 1: "+t2.getOverlap(t1));
    }*/
}
