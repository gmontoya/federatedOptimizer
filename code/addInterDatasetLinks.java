import java.util.*;
import java.io.*;

class addInterDatasetLinks {

    public static void main(String[] args) {

        String iisFile1 = args[0];
        String iioFile1 = args[1];
        String iisFile2 = args[2];
        String iioFile2 = args[3];
        String cpsFile1 = args[4];
        String cpsFile2 = args[5];
        //HashMap<String, Integer> iis1 = readIIS(iisFile1);
        HashMap<String, Integer> iis2 = readIIS(iisFile2);
        HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio1 = readIIO(iioFile1);
        //HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio2 = readIIO(iioFile2);
        HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> cps1 = new HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>();
        HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> cps2 = new HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>();
        // Links 1 -> 2
        findLinks(iio1, iis2, cps1);
        //findLinks(iio2, iis1, cps2);
        writeCPS(cps1, cpsFile1);
        iio1=null;
        iis2=null;
        cps1=null;
        HashMap<String, Integer> iis1 = readIIS(iisFile1);
        HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio2 = readIIO(iioFile2);
        findLinks(iio2, iis1, cps2);
        writeCPS(cps2, cpsFile2);
        //System.out.println(cps1);
        //System.out.println(cps2);
    }

    public static void findLinks(HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio, HashMap<String, Integer> iis, HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> cps) {   
        for (String obj : iio.keySet()) {
            Integer cs2 = iis.get(obj);
            if (cs2 == null) {
                continue;
            }
            HashMap<String, HashMap<Integer,Integer>> psCsCount = iio.get(obj);
            if (psCsCount == null) {
                continue;
            }
            for (String p : psCsCount.keySet()) {
                HashMap<Integer,Integer> csCount = psCsCount.get(p);
                if (csCount == null) {
                    continue;
                }
                for (Integer cs1 : csCount.keySet()) {
                    HashMap<Integer, HashMap<Integer, Integer>> cs1cs2Count = cps.get(p);
                    if (cs1cs2Count == null) {
                        cs1cs2Count = new HashMap<Integer, HashMap<Integer, Integer>>();
                    }
                    HashMap<Integer, Integer> cs2Count = cs1cs2Count.get(cs1);
                    if (cs2Count == null) {
                        cs2Count = new HashMap<Integer, Integer>();
                    }
                    Integer c = cs2Count.get(cs2);
                    if (c == null) {
                        c = 0; 
                    }
                    Integer cAux = csCount.get(cs1);
                    c+=cAux;
                    cs2Count.put(cs2, c);
                    cs1cs2Count.put(cs1, cs2Count);
                    cps.put(p, cs1cs2Count);     
                }
            }
        }
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

    public static HashMap<String, Integer> readIIS(String file) {
        HashMap<String, Integer> iis = null;
        try {
            final ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)));

            iis = (HashMap<String, Integer>) in.readObject();
        } catch (Exception e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }        
        return iis;
    }

    public static HashMap<String, HashMap<String, HashMap<Integer,Integer>>> readIIO(String file) {
        HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio = null; 
        try {
            final ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)));

            iio = (HashMap<String, HashMap<String, HashMap<Integer,Integer>>>) in.readObject();
        } catch (Exception e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }        
        return iio;
    }
}

