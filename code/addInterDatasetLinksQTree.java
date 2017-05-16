import java.util.*;
import java.io.*;

class addInterDatasetLinksQTree {

    public static void main(String[] args) {

        String qTreeISFile1 = args[0];
        String qTreeIOFile1 = args[1];
        String qTreeISFile2 = args[2];
        String qTreeIOFile2 = args[3];
        String cpsFile1 = args[4];
        String cpsFile2 = args[5];
        HashMap<Integer, QTree> qTreeIS1 = readQTreeIS(qTreeISFile1);
        HashMap<Integer, QTree> qTreeIS2 = readQTreeIS(qTreeISFile2);
        HashMap<Integer, HashMap<String,QTree>> qTreeIO1 = readQTreeIO(qTreeIOFile1);
        HashMap<Integer, HashMap<String,QTree>> qTreeIO2 = readQTreeIO(qTreeIOFile2);
        HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> cps1 = new HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>();
        HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> cps2 = new HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>();
        // Links 1 -> 2
        findLinks(qTreeIO1, qTreeIS2, cps1);
        //System.out.println("Total of computed CPs: "+cps1.size());
        writeCPS(cps1, cpsFile1);
        //System.out.println(cps1);
        cps1 = null;
        findLinks(qTreeIO2, qTreeIS1, cps2);
        //System.out.println("Total of computed CPs: "+cps2.size());
        writeCPS(cps2, cpsFile2);
        
        //System.out.println(cps2);
    }

    // input mipIO: CS_ID -> Predicate -> MIPVector
    // input liis: CS_ID -> MIPVector
    // output cps: CS_ID -> CS_ID -> Predicate -> count
    public static void findLinks(HashMap<Integer, HashMap<String,QTree>> qTreeIO, HashMap<Integer, QTree> qTreeIS, HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> cps) {

        for (Integer csSubject : qTreeIO.keySet()) {
            HashMap<String,QTree> psQTree = qTreeIO.get(csSubject);
            for (String p : psQTree.keySet()) {
                QTree qt1 = psQTree.get(p);
                for (Integer csObject : qTreeIS.keySet()) {
                    QTree qt2 = qTreeIS.get(csObject);
                    int overlap = (int) qt1.getOverlap(qt2);
                    if (overlap == 0) {
                        continue;
                    }
                    HashMap<Integer, HashMap<Integer, Integer>> csSubjcsObjCount = cps.get(p);
                    if (csSubjcsObjCount == null) {
                        csSubjcsObjCount = new HashMap<Integer, HashMap<Integer, Integer>>();
                    }
                    HashMap<Integer, Integer> csObjCount = csSubjcsObjCount.get(csSubject);
                    if (csObjCount == null) {
                        csObjCount = new HashMap<Integer, Integer>();
                    }
                    Integer count = csObjCount.get(csObject);
                    if (count == null) {
                        count = 0;
                    }
                    count += overlap;
                    csObjCount.put(csObject, count);
                    csSubjcsObjCount.put(csSubject, csObjCount);
                    cps.put(p, csSubjcsObjCount);
                }
            }
        }
    }

    public static HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> readCSS(String file) {

        HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css = null;
        try {
            ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)));

            css = (HashMap<Integer, Pair<Integer, HashMap<String, Integer>>>) in.readObject();
            in.close();
        } catch (Exception e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }
        return css;
    }

    public static void writeCPS(HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> cps, String fileName) {

        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(fileName)));
            out.writeObject(cps);
            out.close();
        } catch (IOException e) {
            System.err.println("Problems writing file: "+fileName);
            System.exit(1);
        }
    }

    public static HashMap<Integer, QTree> readQTreeIS(String file) {
        HashMap<Integer, QTree> mis = null;
        try {
            ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)));

            mis = (HashMap<Integer, QTree>) in.readObject();
            in.close();
        } catch (Exception e) {
            System.err.println("Problems reading file: "+file);
            e.printStackTrace();
            System.exit(1);
        }        
        return mis;
    }

    public static HashMap<Integer, HashMap<String,QTree>> readQTreeIO(String file) {
        HashMap<Integer, HashMap<String,QTree>> mio = null; 
        try {
            ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)));

            mio = (HashMap<Integer, HashMap<String,QTree>>) in.readObject();
            in.close();
        } catch (Exception e) {
            System.err.println("Problems reading file: "+file);
            e.printStackTrace();
            System.exit(1);
        }        
        return mio;
    }
}

