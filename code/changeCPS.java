import java.util.*;
import java.io.*;

class changeCPS {

    public static void main(String[] args) throws Exception {
        String outputFile = args[0];
        String inputFile = args[1];
//        String outputFile = args[1];
        HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps = readCPS(inputFile);
        HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> newCPS = convert(cps);
        writeCPS(newCPS, outputFile);
    }

    public static HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> convert(HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps) {
        HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> res = new  HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>();
        for (Integer cs1 : cps.keySet()) {
            HashMap<Integer, HashMap<String, Integer>> cs2psCount = cps.get(cs1);
            for (Integer cs2 : cs2psCount.keySet()) {
                HashMap<String, Integer> psCount = cs2psCount.get(cs2);
                for (String p : psCount.keySet()) {
                    Integer count = psCount.get(p);
                    HashMap<Integer, HashMap<Integer, Integer>> cs1cs2Count = res.get(p);
                    if (cs1cs2Count == null) {
                        cs1cs2Count = new HashMap<Integer, HashMap<Integer, Integer>>();
                    }
                    HashMap<Integer, Integer> cs2Count = cs1cs2Count.get(cs1);
                    if (cs2Count == null) {
                        cs2Count = new HashMap<Integer, Integer>();
                    }
                    cs2Count.put(cs2, count);
                    cs1cs2Count.put(cs1, cs2Count);
                    res.put(p, cs1cs2Count);
                }
            }
        }
        return res;
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

    public static HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> readCPS(String file) throws Exception {
        final ObjectInputStream in = new ObjectInputStream(
            new BufferedInputStream(new FileInputStream(file)));

        final HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps = (HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>>) in.readObject();
        return cps;
    }
}
