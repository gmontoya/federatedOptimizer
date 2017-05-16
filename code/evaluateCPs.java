import java.util.*;
import java.io.*;

class evaluateCPs {

    public static void main(String[] args) {
        String cps1File = args[0];
        String cps2File = args[1];
        HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> cps1 = readCPS(cps1File);
        System.out.println("cps1.size(): "+cps1.size());
        //System.out.println("cps2.size(): "+cps2.size());
        HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> cps2 = readCPS(cps2File);
        System.out.println("cps2.size(): "+cps2.size());
        int c1 = countCommon(cps1, cps2);
        int c2 = countDifference(cps1, cps2);
        int c3 = countDifference(cps2, cps1);
        System.out.println(c1+" "+c2+" "+c3);
        System.out.println(getMissingCPs(cps1, cps2));
    }

    public static int countCommon(HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> cps1, HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> cps2) {
        int c = 0;
        for (String p : cps1.keySet()) {
            if (cps2.get(p) == null) {
                continue;
            }
            HashMap<Integer, HashMap<Integer, Integer>> cs1cs2count1 = cps1.get(p);
            HashMap<Integer, HashMap<Integer, Integer>> cs1cs2count2 = cps2.get(p);
            for (Integer cs1 : cs1cs2count1.keySet()) {
                if (cs1cs2count2.get(cs1)==null) {
                    continue;
                }
                HashMap<Integer, Integer> cs2count1 = cs1cs2count1.get(cs1);
                HashMap<Integer, Integer> cs2count2 = cs1cs2count2.get(cs1);
                for (Integer cs2 : cs2count1.keySet()) {
                    if (cs2count2.get(cs2)==null) {
                        continue;
                    }
                    c++;
                }
            }
        }
        return c;
    }

    public static String getMissingCPs(HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> cps1, HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> cps2) {
        String res = "";
        loop: for (String p : cps1.keySet()) {
            HashMap<Integer, HashMap<Integer, Integer>> cs1cs2count1 = cps1.get(p);
            HashMap<Integer, HashMap<Integer, Integer>> cs1cs2count2 = cps2.get(p);
            for (Integer cs1 : cs1cs2count1.keySet()) {
                HashMap<Integer, Integer> cs2count1 = cs1cs2count1.get(cs1);
                HashMap<Integer, Integer> cs2count2 = cs1cs2count2 != null ? cs1cs2count2.get(cs1) : null;
                for (Integer cs2 : cs2count1.keySet()) {
                    if (cs1cs2count2 == null || cs2count2 == null || cs2count2.get(cs2)==null) {
                        res=cs1+" "+p+" "+cs2;
                        break loop;
                    }
                }
            }
        }
        return res;
    }

    public static int countDifference(HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> cps1, HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> cps2) {
        int c = 0;
        for (String p : cps1.keySet()) {
            HashMap<Integer, HashMap<Integer, Integer>> cs1cs2count1 = cps1.get(p);
            HashMap<Integer, HashMap<Integer, Integer>> cs1cs2count2 = cps2.get(p);
            for (Integer cs1 : cs1cs2count1.keySet()) {
                HashMap<Integer, Integer> cs2count1 = cs1cs2count1.get(cs1);
                HashMap<Integer, Integer> cs2count2 = cs1cs2count2 != null ? cs1cs2count2.get(cs1) : null;
                for (Integer cs2 : cs2count1.keySet()) {
                    if (cs1cs2count2 == null || cs2count2 == null || cs2count2.get(cs2)==null) {
                        c++;
                    }
                }
            }
        }
        return c;
    }

    public static HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> readCPS(String file) {
        HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> cps = null;
        try {
            ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)));

            cps = (HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>) in.readObject();
        } catch (Exception e) {
            System.err.println("Problems reading file: "+file);
            e.printStackTrace();
            System.exit(1);
        }
        return cps;
    }
    public static HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> readCPSI(String file) {
        HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> cps = null;
        try {
            ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)));

            cps = (HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>) in.readObject();
        } catch (Exception e) {
            System.err.println("Problems reading file: "+file);
            e.printStackTrace();
            System.exit(1);
        }
        return cps;
    }
}
