import java.io.*;
import java.util.*;

class obtainRadixOneBasedIndex {
 
    public static void main(String[] args) {
        String fileIIS1 = args[0];
        //String fileIS2 = args[1];
        String fileIIO1 = args[1];
        String fileRT = args[2];
        int bmax = Integer.parseInt(args[3]);
        int fmax = Integer.parseInt(args[4]);
        int tmax = Integer.parseInt(args[5]);
        int th = Integer.parseInt(args[6]);
        //int N = Integer.parseInt(args[6]);
        //HashMap<Integer, TreeSet<String>> iis = readIS(fileIS1);
        //HashMap<Integer, HashMap<String, TreeSet<String>>> iio = readIO(fileIO1);
        HashMap<String, Integer> iis = obtainLightIndex.readIIS(fileIIS1);
        HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio = obtainLightIndex.readIIO(fileIIO1);
        //System.out.println(iis1);
        //System.out.println(iio1);
        RadixTreeOne rt = new RadixTreeOne("/", tmax, bmax, fmax, th);
        //try {
        processSubjectIndex(iis, rt, tmax, bmax, fmax);
        processObjectIndex(iio, rt, tmax, bmax, fmax);
        //System.out.println("num leaves: "+rt.getNumberOfLeaves());
        //System.out.println("(bqtb) rt: "+rt);
        /*String p = "<http://dbpedia.org/ontology/profession>";
        Integer cs1 = 1602395366;
        Integer cs2 = 1838974214;
        String str = "http://dbpedia.org/resource/Finance";*/
        System.out.println("Number of leaves: "+rt.getNumberOfLeaves());
        /*boolean bS = containsS(str, cs2, rt);
        boolean bO = containsO(str, cs1, p, rt);
        System.out.println("included as subject? "+bS+". included as object? "+bO);*/
        ////rt.buildQTrees();
        ////System.out.println("Number of leaves: "+rt.getNumberOfLeaves());
        /*String s = "http://dbpedia.org";
        String value = "resource/Finance";*/
        //Set<RadixTreeOne.LeafRadix> lrs = rt.getLeavesRadix(s);
        //for (RadixTreeOne.LeafRadix lr : lrs) {
        /*RadixTreeOne.LeafRadix lr =  rt.getLeafRadix(s, rt.root);
        if (lr == null) {
            System.out.println("s not found in rt.root");
            lr =  rt.getLeafRadix(s, rt.otherRoot);
        }
        if (lr == null) {
            System.out.println("s not found in rt.otherRoot");
            return;
        }
        RadixTreeOne.NodeRadix x = lr;
        while (x != null) {
            System.out.println("x.radix: "+x.radix);
            x = x.parent;
        }
            QTreeOne qt = lr.values;
            Integer hc = value.hashCode();
            Set<QTreeOne.QTreeOneLeaf> ls = qt.getLeaves(hc);
            short sh = (new Integer(hc % 65536)).shortValue();
            System.out.println("hc: "+hc+". sh: "+sh);
            for (QTreeOne.QTreeOneLeaf l : ls) {
                if (l.includes(hc)) {
                    System.out.println("IT INCLUDES hc");
                    System.out.println("l.bucket: "+l.bucket);
                if (l != null && l.valuesO != null && l.valuesO.get(p) != null && l.valuesO.get(p).get(cs1) != null) {
                    System.out.println("Obj: \n"+l.valuesO.get(p).get(cs1).contains(sh));
                }
                if (l != null && l.valuesS != null && l.valuesS.get(cs2) != null) {
                    System.out.println("Subj: \n"+l.valuesS.get(cs2).contains(sh));
                }}
            }*/
        //}
        //System.out.println("rt: "+rt);
        //} catch (Exception e) {
        //    e.printStackTrace();
        //} catch (Error e) {
        //    e.printStackTrace();
        //}
        //System.out.println("rt: "+rt);
        writeIndex(rt, fileRT);
        //writeObjectIndex(io, fileIO2);
        /*for (Integer cs : is.keySet()) {
            System.out.println("cs: "+cs);
            RadixTree rt = is.get(cs);
            System.out.println(rt);
        }*/
        //System.out.println(is);
        //System.out.println("io:");
        //System.out.println(io);
    }
/*
    public static QTreeOne.QTreeOneLeaf checkIfObject(RadixTreeOne rt, String p, Integer cs1, String s, String value) {

        RadixTreeOne.LeafRadix lr =  rt.getLeafRadix(s, rt.root);
        if (lr == null) {
            //System.out.println("s not found in rt.root");
            lr =  rt.getLeafRadix(s, rt.otherRoot);
        }
        if (lr == null) {
            //System.out.println("s not found in rt.otherRoot");
            return null;
        }
        QTreeOne qt = lr.values;
        Integer hc = value.hashCode();
        Set<QTreeOne.QTreeOneLeaf> ls = qt.getLeaves();
        short sh = (new Integer(hc % 65536)).shortValue();
        for (QTreeOne.QTreeOneLeaf l : ls) {
            if (l != null && l.includes(hc) && l.valuesO != null && l.valuesO.get(p) != null && l.valuesO.get(p).get(cs1) != null) {
                  boolean b = l.valuesO.get(p).get(cs1).contains(sh);
                  if (b) return l;
            }
        }
        return null;
    }*/
/*
    public static void writeObjectIndexText(HashMap<Integer, HashMap<String,QTree>> index, String fileName) {

        try {
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(fileName));
            for (Integer cs : index.keySet()) {
                 HashMap<String,QTree> psVs = index.get(cs);
                 osw.write(cs+"\n");
                 for (String p : psVs.keySet()) {
                     QTree qt = psVs.get(p);                     
                     TreeSet<Bucket> ts = qt.getBuckets();
                     int size = qt.getNumBuckets();
                     osw.write(p.hashCode()+" "+size+" "+ts+"\n");
                 }
            }
            osw.close();
        } catch (IOException e) {
            System.err.println("Problems writing file: "+fileName);
            System.exit(1);
        }
    }

    public static void writeIndexText(HashMap<Integer, QTree> index, String fileName) {

        try {
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(fileName));
            for (Integer cs : index.keySet()) {
                     QTree qt = index.get(cs);
                     
                     TreeSet<Bucket> ts = qt.getBuckets();
                     int size = qt.getNumBuckets();
                     osw.write(cs+" "+size+" "+ts+"\n");
            }
            osw.close();
        } catch (IOException e) {
            System.err.println("Problems writing file: "+fileName);
            System.exit(1);
        }
    }
*/
    public static void writeObjectIndex(HashMap<Integer, HashMap<String,RadixTreeOne>> index, String fileName) {

        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(fileName)));
            out.writeObject(index);
            out.close();
        } catch (IOException e) {
            System.err.println("Problems writing file: "+fileName);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void writeIndex(RadixTreeOne index, String fileName) {

        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(fileName)));
            out.writeObject(index);
            out.close();
        } catch (IOException e) {
            System.err.println("Problems writing file: "+fileName);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static HashMap<Integer, TreeSet<String>> readIS(String file) {
        HashMap<Integer, TreeSet<String>> iis = null;
        try {
            final ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)));

            iis = (HashMap<Integer, TreeSet<String>>) in.readObject();
        } catch (Exception e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }        
        return iis;
    }

    public static HashMap<Integer, HashMap<String, TreeSet<String>>> readIO(String file) {
        HashMap<Integer, HashMap<String, TreeSet<String>>> io = null; 
        try {
            final ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)));

            io = (HashMap<Integer, HashMap<String, TreeSet<String>>>) in.readObject();
        } catch (Exception e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }        
        return io;
    }

    public static void processSubjectIndex (HashMap<String, Integer> iis, RadixTreeOne rt, int tmax, int bmax, int fmax) { //, int N) {
        int k = 0;
//        RadixTreeOne aux = new RadixTreeOne("/", tmax, bmax, fmax);
//        System.out.println("Processing "+iis.keySet().size()+" subjects");
        int N = (int) (iis.keySet().size()/100);
        //int oldNL = -1;
        for (String e : iis.keySet()) {
/*            if (k == N) {
                rt.addAll(aux);
                aux = new RadixTreeOne("/", tmax, bmax, fmax);
                k = 0;
            }*/
            Integer cs = iis.get(e);
            String str = e;
            if (str.startsWith("<")) {
                str = str.substring(1, str.length()-1);
            }
            rt.addS(str, cs);
            //aux.addS(str, cs);
            if (k % N == 0) {
                System.out.print(".");
                //System.out.println("Number of leaves: "+rt.getNumberOfLeaves());
                /*int nl = rt.getNumberOfLeaves();
                if (oldNL >= 0 && nl < oldNL) {
                    System.out.println("k: "+k);
                }
                oldNL = nl;*/
            }
            k++;
        }
//        System.out.println("\nFinished processing subjects");
        //rt.addAll(aux);
    }
/*
    public static void processSubjectIndex (HashMap<Integer, TreeSet<String>> is, RadixTreeOne rt) {
        for (Integer cs : is.keySet()) {
            TreeSet<String> es = is.get(cs);
            ////Vector<String> ves = new Vector(es);
          
            for (String e : es) { //
                String str = e;
                if (str.startsWith("<")) {
                    str = str.substring(1, str.length()-1);
                }
//                if (str.equals("http://bio2rdf.org/chebi:27856")) {
//                    boolean b = containsS(str, cs, rt);
//                    System.out.println("adding "+str+" with cs "+cs+" to leaf "+b);
//                }
                rt.addS(str, cs);
//                if (str.equals("http://bio2rdf.org/chebi:27856")) {
//                    boolean b = containsS(str, cs, rt);
//                    System.out.println("after adding: "+b);
//                }
            } //
            //rt.buildQTrees(); //
            ////smartLoading(rt, ves, 0, ves.size()-1);
            ////rt.ensureMax();
            //is2.put(cs, rt);
        }
    }
*/
/*
    public static Set<QTreeOne.QTreeOneLeaf> getLeaves(String s, RadixTreeOne rt) {
            int pos = s.lastIndexOf("/"); 
            String value;
            if (pos < 0) {
                return null;
            } else {
                value = s.substring(pos+1);
                s = s.substring(0, pos);
            }
            QTreeOne qt1 = rt.getQTree(s);
            Set<QTreeOne.QTreeOneLeaf> ls = qt1.getLeaves(value.hashCode());
            return ls;
    }

    public static boolean containsS(String s, Integer cs, RadixTreeOne rt) {
            int pos = s.lastIndexOf("/");
            String value;
            if (pos < 0) {
                return false;
            } else {
                value = s.substring(pos+1);
                s = s.substring(0, pos);
            }
            System.out.println("value.hashCode(): "+value.hashCode());
            Set<RadixTreeOne.LeafRadix> ls = rt.getLeavesRadix(s);
            boolean b = false;
            for (RadixTreeOne.LeafRadix l : ls) {
                if (l != null && l.valuesS != null && l.valuesS.get(cs) != null && l.valuesS.get(cs).contains(value.hashCode())) {
                    b=true;
                }
            }
            return b;
    }

    public static boolean containsO(String s, Integer cs, String p, RadixTreeOne rt) {
            int pos = s.lastIndexOf("/");
            String value;
            if (pos < 0) {
                return false;
            } else {
                value = s.substring(pos+1);
                s = s.substring(0, pos);
            }
            System.out.println("value.hashCode(): "+value.hashCode());
            Set<RadixTreeOne.LeafRadix> ls = rt.getLeavesRadix(s);
            boolean b = false;
            Integer pHC = p.hashCode();
            for (RadixTreeOne.LeafRadix l : ls) {
                if (l != null && l.valuesO != null && l.valuesO.get(pHC) != null && l.valuesO.get(pHC).get(cs) != null && l.valuesO.get(pHC).get(cs).contains(value.hashCode())) {
                    b=true;
                }
            }
            return b;
    }
*/
    public static void processObjectIndex (HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio, RadixTreeOne rt, int tmax, int bmax, int fmax) { //, int N) {
//        System.out.println("Processing "+iio.keySet().size()+" objects");
        int k = 0;
        int N = (int) (iio.keySet().size()/100);
//        RadixTreeOne aux = new RadixTreeOne("/", tmax, bmax, fmax);
        //int oldNL = -1;
        //boolean b = false;
        //QTreeOne.QTreeOneLeaf aux = null;
        for (String e : iio.keySet()) {
/*            if (k == N) {
                rt.addAll(aux);
                aux = new RadixTreeOne("/", tmax, bmax, fmax);
                k = 0;
            }*/
            HashMap<String, HashMap<Integer,Integer>> psCsCount = iio.get(e);
            String str = e;
            if (str.startsWith("<")) {
                str = str.substring(1, str.length()-1);
            }
            for (String p : psCsCount.keySet()) {
                HashMap<Integer,Integer> csCount = psCsCount.get(p);
                for (Integer cs : csCount.keySet()) {
                    //Integer c = csCount.get(cs);
                    //aux.addO(str, cs, p);
                    rt.addO(str, cs, p);
                    /*QTreeOne.QTreeOneLeaf aux2 = checkIfObject(rt, "<http://xmlns.com/foaf/0.1/based_near>", 672184354, "http://sws.geonames.org", "953987/");
                    if (aux != null && aux2 == null) {
                        System.out.println("aux: "+aux);
                        System.out.println("e: "+e+". p: "+p+". cs: "+cs);
                        System.exit(1);
                    } 
                    if (aux == null && aux2 != null) {
                        aux = aux2;
                        System.out.println("(incl) aux: "+aux);
                    }*/
                    /*if (k < 3) {
                        System.out.println(str+" "+cs+" "+p);
                    }*/
                }
            }
            if (k % N == 0) {
                System.out.print(".");
                /*int nl = rt.getNumberOfLeaves();
                if (oldNL >= 0 && nl < oldNL) {
                    System.out.println("k: "+k);
                }
                oldNL = nl;*/
            }
            k++;
        }
//        System.out.println("\nFinished processing objects");
        //rt.addAll(aux);
    }

/*
    public static void processObjectIndex (HashMap<Integer, HashMap<String, TreeSet<String>>> io, RadixTreeOne rt) {

        for (Integer cs : io.keySet()) {
            HashMap<String, TreeSet<String>> psEs = io.get(cs);
            //HashMap<String, RadixTreeOne> psRT = new HashMap<String,RadixTreeOne>();
            for (String p : psEs.keySet()) {
                TreeSet<String> es = psEs.get(p);
                ////Vector<String> ves = new Vector(es);
                //RadixTreeOne rt = new RadixTreeOne("/", tmax, bmax, fmax);
                for (String e : es) { //
                    String str = e;
                    if (str.startsWith("<")) {
                        str = str.substring(1, str.length()-1);
                    }
                    //if (str.equals("http://bio2rdf.org/chebi:27856")) {
                    //    boolean b = containsO(str, cs, p, rt);
                    //    System.out.println("adding "+str+" with cs "+cs+" and pred "+p+" to leaf "+b);
                    //}
                    rt.addO(str, cs, p);
                    //if (str.equals("http://bio2rdf.org/chebi:27856")) {
                    //    boolean b = containsO(str, cs, p, rt);
                    //    System.out.println("after adding: "+b);
                    //}
                } //
                //rt.buildQTrees(); //
                ////smartLoading(rt, ves, 0, ves.size()-1);
                ////rt.ensureMax();
                //psRT.put(p, rt);
            }
            //io2.put(cs, psRT);
        }
        //return io2;
    }*/
/*
    public static void smartLoading(RadixTree rt, Vector<String> es, int inf, int sup) {
        
        LinkedList<Integer> ll1 = new LinkedList<Integer>();
        LinkedList<Integer> ll2 = new LinkedList<Integer>();
        ll1.add(inf);
        ll2.add(sup);
        while(ll1.size()>0) {
            int i = ll1.removeFirst();
            int s = ll2.removeFirst();
            if (i < s) {
                int m = (int) Math.round(Math.floor((i+s)/2.0));
                String str = es.get(m);
                if (str.startsWith("<")) {
                    str = str.substring(1, str.length()-1);
                }
                rt.add(str);
                ll1.add(i);
                ll2.add(m-1);
                ll1.add(m+1);
                ll2.add(s);
            } else if (i==s) {
                String str = es.get(i);
                if (str.startsWith("<")) {
                    str = str.substring(1, str.length()-1);
                }
                rt.add(str);
            }
        }
    }*/
/*
    public static HashMap<Integer, RadixTree> getSubjectIndex (HashMap<String, Integer> iis, int bmax, int fmax, int tmax) {
        HashMap<Integer, RadixTree> is = new HashMap<Integer, RadixTree>();
        //System.out.println("getSubjectIndex");
        //int i = 0;
        for (String s : iis.keySet()) {
            String str = s;
            if (s.startsWith("<")) {
                str = str.substring(1, str.length()-1);
            }
            Integer cs = iis.get(s);
            RadixTree rt = is.get(cs);
            if (rt == null) {
                rt = new RadixTree("/", tmax, bmax, fmax);
            } 
            //int hc = s.hashCode();
            //System.out.println(hc);
            //System.out.println("going to add *"+str+"*");
            //if (cs == -2062119584) {
            //    System.out.println(str);
            //}
            rt.add(str);
            //System.out.println("done "+rt);
            is.put(cs, rt);
            //i++;
            //if (i > 100) {
            //    break;
            //} 
        }
        for (Integer cs : is.keySet()) {
            RadixTree rt = is.get(cs);
            rt.ensureMax();
        }
        return is;
    }*/
/*
    public static HashMap<Integer, HashMap<String,RadixTree>> getObjectIndex (HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio, int bmax, int fmax, int tmax) {
        HashMap<Integer, HashMap<String, RadixTree>> io = new HashMap<Integer, HashMap<String, RadixTree>>();
        //System.out.println("getObjectIndex");
        for (String o : iio.keySet()) {
            String str = o;
            if (o.startsWith("<")) {
                str = str.substring(1, str.length()-1);
            }
            HashMap<String, HashMap<Integer,Integer>> psCsCount = iio.get(o);
            for (String p : psCsCount.keySet()) {
                HashMap<Integer,Integer> csCount = psCsCount.get(p);
                for (Integer cs : csCount.keySet()) {
                    HashMap<String,RadixTree> pos = io.get(cs);
                    if (pos == null) {
                        pos = new HashMap<String, RadixTree>();
                    } 
                    RadixTree os = pos.get(p);
                    if (os == null) {
                        os = new RadixTree("/", tmax, bmax, fmax);
                    } 
                    //int hc = o.hashCode();
                    //System.out.println(hc);
                    //System.out.println("going to add *"+str+"*");
                    os.add(str);
                    pos.put(p, os);
                    io.put(cs, pos);
                }
            }
        }
        for (Integer cs : io.keySet()) {
            HashMap<String, RadixTree> predSum = io.get(cs);
            for (String pred : predSum.keySet()) {
                RadixTree rt = predSum.get(pred);
                rt.ensureMax();
            }
        }
        //System.out.println("done");
        return io;
    }*/

}
