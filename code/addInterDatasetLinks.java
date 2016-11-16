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
        HashMap<String, Integer> iis1 = readIIS(iisFile1);
        HashMap<String, Integer> iis2 = readIIS(iisFile2);
        HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio1 = readIIO(iioFile1);
        HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio2 = readIIO(iioFile2);
        HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps1 = new HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>>();
        HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps2 = new HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>>();
        // Links 1 -> 2
        findLinks(iio1, iis2, cps1);
        findLinks(iio2, iis1, cps2);
        writeCPS(cps1, cpsFile1);
        writeCPS(cps2, cpsFile2);
        System.out.println(cps1);
        System.out.println(cps2);
    }

    public static void findLinks(HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio, HashMap<String, Integer> iis, HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps) {   
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
                    HashMap<Integer, HashMap<String, Integer>> css2Ps = cps.get(cs1);
                    if (css2Ps == null) {
                        css2Ps = new HashMap<Integer, HashMap<String, Integer>>();
                    }
                    HashMap<String, Integer> ps = css2Ps.get(cs2);
                    if (ps == null) {
                        ps = new HashMap<String, Integer>();
                    }
                    Integer c = ps.get(p);
                    if (c == null) {
                        c = 0; 
                    }
                    Integer cAux = csCount.get(cs1);
                    c+=cAux;
                    ps.put(p, c);
                    css2Ps.put(cs2, ps);
                    cps.put(cs1, css2Ps);     
                }
            }
        }
    }

    public static void writeCPS(HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps, String fileName) {

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

