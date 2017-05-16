import java.util.*;
import java.io.*;

class addInterDatasetLinksRTreeOne {

    public static void main(String[] args) {

        String rTreeFile1 = args[0];
        String rTreeFile2 = args[1];
        String cpsFile1 = args[2];
        String cpsFile2 = args[3];
        RadixTreeOne rTree1 = readRadixTreeOne(rTreeFile1);
        RadixTreeOne rTree2 = readRadixTreeOne(rTreeFile2);
        HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> cps1 = rTree1.getOverlap(rTree2); 
        writeCPS(cps1, cpsFile1);
        cps1.clear();
        HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> cps2 = rTree2.getOverlap(rTree1);
        
        // Links 1 -> 2
        //findLinks(rTree1, rTree2, cps1);
        //System.out.println("Total of computed CPs: "+cps1.size());
        //writeCPS(cps1, cpsFile1);
        //System.out.println(cps1);
        //cps1 = null;
        //findLinks(rTree2, rTree1, cps2);
        //System.out.println("Total of computed CPs: "+cps2.size());
        writeCPS(cps2, cpsFile2);
        
        //System.out.println(cps2);
    }

    // input mipIO: CS_ID -> Predicate -> MIPVector
    // input liis: CS_ID -> MIPVector
    // output cps: CS_ID -> CS_ID -> Predicate -> count
/*
    public static void findLinks(RadixTreeOne rTree1, RadixTreeOne rTree2, HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> cps) {

        for (Integer csSubject : rTreeIO.keySet()) {
            HashMap<String,RadixTreeS> psRTree = rTreeIO.get(csSubject);
            for (String p : psRTree.keySet()) {
                RadixTreeS rt1 = psRTree.get(p);
                for (Integer csObject : rTreeIS.keySet()) {
                    RadixTreeS rt2 = rTreeIS.get(csObject);
                    int overlap = (int) rt1.getOverlap(rt2);
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
*/
/*    public static HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> readCSS(String file) {

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
*/
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

    public static RadixTreeOne readRadixTreeOne(String file) {
        RadixTreeOne rt = null;
        try {
            ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)));

            rt = (RadixTreeOne) in.readObject();
            in.close();
        } catch (Exception e) {
            System.err.println("Problems reading file: "+file);
            e.printStackTrace();
            System.exit(1);
        }        
        return rt;
    }
/*
    public static HashMap<Integer, HashMap<String,RadixTreeS>> readRadixTreeIO(String file) {
        HashMap<Integer, HashMap<String,RadixTreeS>> mio = null; 
        try {
            ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)));

            mio = (HashMap<Integer, HashMap<String,RadixTreeS>>) in.readObject();
            in.close();
        } catch (Exception e) {
            System.err.println("Problems reading file: "+file);
            e.printStackTrace();
            System.exit(1);
        }        
        return mio;
    }*/
}

