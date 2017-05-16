import java.io.*;
import java.util.*;

class obtainRadixBasedIndex {
 
    public static void main(String[] args) {
        String fileIS1 = args[0];
        String fileIS2 = args[1];
        String fileIO1 = args[2];
        String fileIO2 = args[3];
        int bmax = Integer.parseInt(args[4]);
        int fmax = Integer.parseInt(args[5]);
        int tmax = Integer.parseInt(args[6]);
        HashMap<Integer, TreeSet<String>> iis = readIS(fileIS1);
        HashMap<Integer, HashMap<String, TreeSet<String>>> iio = readIO(fileIO1);
        //System.out.println(iis1);
        //System.out.println(iio1);
        HashMap<Integer, RadixTreeS> is = getSubjectIndex(iis, bmax, fmax, tmax);
        HashMap<Integer, HashMap<String,RadixTreeS>> io = getObjectIndex(iio, bmax, fmax, tmax);
        //System.out.println("is:");
        writeIndex(is, fileIS2);
        writeObjectIndex(io, fileIO2);
        /*for (Integer cs : is.keySet()) {
            System.out.println("cs: "+cs);
            RadixTree rt = is.get(cs);
            System.out.println(rt);
        }*/
        //System.out.println(is);
        //System.out.println("io:");
        //System.out.println(io);
    }


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

    public static void writeObjectIndex(HashMap<Integer, HashMap<String,RadixTreeS>> index, String fileName) {

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

    public static void writeIndex(HashMap<Integer, RadixTreeS> index, String fileName) {

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

    public static HashMap<Integer, RadixTreeS> getSubjectIndex (HashMap<Integer, TreeSet<String>> is, int bmax, int fmax, int tmax) {
        HashMap<Integer, RadixTreeS> is2 = new HashMap<Integer, RadixTreeS>();
        for (Integer cs : is.keySet()) {
            TreeSet<String> es = is.get(cs);
            ////Vector<String> ves = new Vector(es);
            RadixTreeS rt = new RadixTreeS("/", tmax, bmax, fmax);
            for (String e : es) { //
                String str = e;
                if (str.startsWith("<")) {
                    str = str.substring(1, str.length()-1);
                }
                rt.add(str);
            } //
            rt.buildQTrees(); //
            ////smartLoading(rt, ves, 0, ves.size()-1);
            ////rt.ensureMax();
            is2.put(cs, rt);
        }
        return is2;
    }

    public static HashMap<Integer, HashMap<String,RadixTreeS>> getObjectIndex (HashMap<Integer, HashMap<String, TreeSet<String>>> io, int bmax, int fmax, int tmax) {
        HashMap<Integer, HashMap<String, RadixTreeS>> io2 = new HashMap<Integer, HashMap<String, RadixTreeS>>();
        for (Integer cs : io.keySet()) {
            HashMap<String, TreeSet<String>> psEs = io.get(cs);
            HashMap<String, RadixTreeS> psRT = new HashMap<String,RadixTreeS>();
            for (String p : psEs.keySet()) {
                TreeSet<String> es = psEs.get(p);
                ////Vector<String> ves = new Vector(es);
                RadixTreeS rt = new RadixTreeS("/", tmax, bmax, fmax);
                for (String e : es) { //
                    String str = e;
                    if (str.startsWith("<")) {
                        str = str.substring(1, str.length()-1);
                    }
                    rt.add(str);
                } //
                rt.buildQTrees(); //
                ////smartLoading(rt, ves, 0, ves.size()-1);
                ////rt.ensureMax();
                psRT.put(p, rt);
            }
            io2.put(cs, psRT);
        }
        return io2;
    }
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
