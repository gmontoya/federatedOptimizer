import java.io.*;
import java.util.*;

class obtainQTreeBasedIndex {
 
    public static void main(String[] args) {
        String fileIS1 = args[0];
        String fileIS2 = args[1];
        String fileIO1 = args[2];
        String fileIO2 = args[3];
        int bmax = Integer.parseInt(args[4]);
        int fmax = Integer.parseInt(args[5]);
        HashMap<Integer, TreeSet<String>> is1 = readIS(fileIS1);
        HashMap<Integer, HashMap<String, TreeSet<String>>> io1 = readIO(fileIO1);
        //System.out.println(iis1);
        //System.out.println(iio1);
        HashMap<Integer, QTree> is2 = getSubjectIndex(is1, bmax, fmax);
        HashMap<Integer, HashMap<String,QTree>> io2 = getObjectIndex(io1, bmax, fmax);
        writeIndex(is2, fileIS2);
        writeObjectIndex(io2, fileIO2);
        //System.out.println(is);
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

    public static void writeObjectIndex(HashMap<Integer, HashMap<String,QTree>> index, String fileName) {

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

    public static void writeIndex(HashMap<Integer, QTree> index, String fileName) {

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

    public static HashMap<Integer,TreeSet<String>> readIS(String file) {
        HashMap<Integer,TreeSet<String>> iis = null;
        try {
            final ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)));

            iis = (HashMap<Integer,TreeSet<String>>) in.readObject();
        } catch (Exception e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }        
        return iis;
    }

    public static HashMap<Integer, HashMap<String, TreeSet<String>>> readIO(String file) {
        HashMap<Integer, HashMap<String, TreeSet<String>>> iio = null; 
        try {
            final ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)));

            iio = (HashMap<Integer, HashMap<String, TreeSet<String>>>) in.readObject();
        } catch (Exception e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }        
        return iio;
    }

/*    public static HashMap<Integer, QTree> getSubjectIndex (HashMap<String, Integer> iis, int bmax, int fmax) {
        HashMap<Integer, QTree> is = new HashMap<Integer, QTree>();
        //System.out.println("getSubjectIndex");
        for (String s : iis.keySet()) {
            Integer cs = iis.get(s);
            QTree qt = is.get(cs);
            if (qt == null) {
                qt = new QTree(bmax, fmax);
            } 
            int hc = s.hashCode();
            //System.out.println(hc);
            qt.add(hc);
            is.put(cs, qt);
        }
        return is;
    }*/
    public static HashMap<Integer, QTree> getSubjectIndex (HashMap<Integer, TreeSet<String>> is, int bmax, int fmax) {
        HashMap<Integer, QTree> is2 = new HashMap<Integer, QTree>();
        for (Integer cs : is.keySet()) {
            TreeSet<String> es = is.get(cs);
            Vector<String> ves = new Vector(es);
            QTree qt = new QTree(bmax, fmax);
            smartLoading(qt, ves, 0, ves.size()-1);
            is2.put(cs, qt);
        }
        return is2;
    }
/*
    public static HashMap<Integer, HashMap<String,QTree>> getObjectIndex (HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio, int bmax, int fmax) {
        HashMap<Integer, HashMap<String, QTree>> io = new HashMap<Integer, HashMap<String, QTree>>();
        //System.out.println("getObjectIndex");
        for (String o : iio.keySet()) {
            HashMap<String, HashMap<Integer,Integer>> psCsCount = iio.get(o);
            for (String p : psCsCount.keySet()) {
                HashMap<Integer,Integer> csCount = psCsCount.get(p);
                for (Integer cs : csCount.keySet()) {
                    HashMap<String,QTree> pos = io.get(cs);
                    if (pos == null) {
                        pos = new HashMap<String, QTree>();
                    } 
                    QTree os = pos.get(p);
                    if (os == null) {
                        os = new QTree(bmax, fmax);
                    } 
                    int hc = o.hashCode();
                    //System.out.println(hc);
                    os.add(hc);
                    pos.put(p, os);
                    io.put(cs, pos);
                }
            }
        }
        return io;
    }*/

    public static HashMap<Integer, HashMap<String,QTree>> getObjectIndex (HashMap<Integer, HashMap<String, TreeSet<String>>> io, int bmax, int fmax) {
        HashMap<Integer, HashMap<String, QTree>> io2 = new HashMap<Integer, HashMap<String, QTree>>();
        for (Integer cs : io.keySet()) {
            HashMap<String, TreeSet<String>> psEs = io.get(cs);
            HashMap<String, QTree> psQT = new HashMap<String,QTree>();
            for (String p : psEs.keySet()) {
                TreeSet<String> es = psEs.get(p);
                Vector<String> ves = new Vector(es);
                QTree qt = new QTree(bmax, fmax);
                smartLoading(qt, ves, 0, ves.size()-1);
                psQT.put(p, qt);
            }
            io2.put(cs, psQT);
        }
        return io2;
    }

    public static void smartLoading(QTree qt, Vector<String> es, int inf, int sup) {

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
                qt.add(str.hashCode());
                ll1.add(i);
                ll2.add(m-1);
                ll1.add(m+1);
                ll2.add(s);
            } else if (i==s) {
                String str = es.get(i);
                if (str.startsWith("<")) {
                    str = str.substring(1, str.length()-1);
                }
                qt.add(str.hashCode());
            }
        }
    }
}
