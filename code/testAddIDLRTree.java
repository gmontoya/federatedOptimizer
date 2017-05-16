import java.util.*;
import java.io.*;

class testAddIDLRTree {

    public static void main(String[] args) throws Exception {
        String fileCPS = args[0]; //"/home/roott/fedBenchData/cps_Drugbank_ChEBI_reduced10000CS";
        HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps = readCPS(fileCPS);
        String rTreeIOFile1 = args[1];
        String rTreeISFile2 = args[2];
        String ioFile1 = args[3];
        String isFile2 = args[4];
        
        //HashMap<Integer, RadixTree> rTreeIS1 = addInterDatasetLinksRTree.readRadixTreeIS(rTreeISFile1);
        HashMap<Integer, RadixTree> rTreeIS2 = addInterDatasetLinksRTree.readRadixTreeIS(rTreeISFile2);
        HashMap<Integer, HashMap<String,RadixTree>> rTreeIO1 = addInterDatasetLinksRTree.readRadixTreeIO(rTreeIOFile1);
        //HashMap<Integer, HashMap<String,RadixTree>> rTreeIO2 = addInterDatasetLinksRTree.readRadixTreeIO(rTreeIOFile2);
        HashMap<Integer, HashSet<String>> is2 = readIS(isFile2);
        HashMap<Integer, HashMap<String, HashSet<String>>> io1 = readIO(ioFile1);

        for (Integer cs1 : cps.keySet()) {
            for (Integer cs2 : cps.get(cs1).keySet()) {
                for (String p : cps.get(cs1).get(cs2).keySet()) {
                    Integer c = cps.get(cs1).get(cs2).get(p);
                    if (c>0) {
                        System.out.println("cs1: "+cs1);
                        System.out.println("cs2: "+cs2);
                        System.out.println("p: "+p);
                        System.out.println("c: "+c);
                        System.out.println(rTreeIO1.get(cs1).get(p));
                        System.out.println(rTreeIS2.get(cs2));
                        System.out.println(io1.get(cs1).get(p));
                        System.out.println(is2.get(cs2));
                        return;
                    }
                }
            }
        }
    }

    public static HashMap<Integer, HashSet<String>> readIS(String file) {
        HashMap<Integer, HashSet<String>> is = null;
        try {
            final ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)));

            is = (HashMap<Integer, HashSet<String>>) in.readObject();
        } catch (Exception e) {
            System.err.println("Problems reading file: "+file);
            e.printStackTrace();
            System.exit(1);
        }
        return is;
    }

    public static HashMap<Integer, HashMap<String, HashSet<String>>> readIO(String file) {
        HashMap<Integer, HashMap<String, HashSet<String>>> io = null;
        try {
            final ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)));

            io = (HashMap<Integer, HashMap<String, HashSet<String>>>) in.readObject();
        } catch (Exception e) {
            System.err.println("Problems reading file: "+file);
            e.printStackTrace();
            System.exit(1);
        }
        return io;
    }

    public static HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> readCPS(String file) {
        HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps = null;
        try {
            ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)));

            cps = (HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>>) in.readObject();
        } catch (Exception e) {
            System.err.println("Problems reading file: "+file);
            e.printStackTrace();
            System.exit(1);
        }
        return cps;
    }

}
