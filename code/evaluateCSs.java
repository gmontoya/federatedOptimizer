import java.util.*;
import java.io.*;

class evaluateCSs {

    public static void main(String[] args) {
        String css1File = args[0];
        String css2File = args[1];
        HashMap<Integer, HashMap<Integer, Integer>> css1 = readCSS(css1File);
        System.out.println("css1.size(): "+css1.size());
        //System.out.println("cps2.size(): "+cps2.size());
        HashMap<Integer, HashMap<Integer, Integer>> css2 = readCSS(css2File);
        System.out.println("css2.size(): "+css2.size());
        int c1 = countCommon(css1, css2);
        int c2 = countDifference(css1, css2);
        int c3 = countDifference(css2, css1);
        System.out.println(c1+" "+c2+" "+c3);
        //System.out.println(getMissingCSs(css1, css2));
    }

    public static int countCommon(HashMap<Integer, HashMap<Integer, Integer>> css1, HashMap<Integer, HashMap<Integer, Integer>> css2) {
        int c = 0;
            for (Integer cs1 : css1.keySet()) {
                if (css2.get(cs1)==null) {
                    continue;
                }
                HashMap<Integer, Integer> cs2count1 = css1.get(cs1);
                HashMap<Integer, Integer> cs2count2 = css2.get(cs1);
                for (Integer cs2 : cs2count1.keySet()) {
                    if (cs2count2.get(cs2)==null) {
                        continue;
                    }
                    c++;
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

    public static int countDifference(HashMap<Integer, HashMap<Integer, Integer>> css1, HashMap<Integer, HashMap<Integer, Integer>> css2) {
        int c = 0;
            for (Integer cs1 : css1.keySet()) {
                HashMap<Integer, Integer> cs2count1 = css1.get(cs1);
                HashMap<Integer, Integer> cs2count2 = css2.get(cs1);
                for (Integer cs2 : cs2count1.keySet()) {
                    if (cs2count2 == null || cs2count2.get(cs2)==null) {
                        c++;
                    }
                }
            }
        return c;
    }

    public static HashMap<Integer, HashMap<Integer, Integer>> readCSS(String file) {
        HashMap<Integer, HashMap<Integer, Integer>> css = null;
        try {
            ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)));

            css = (HashMap<Integer, HashMap<Integer, Integer>>) in.readObject();
        } catch (Exception e) {
            System.err.println("Problems reading file: "+file);
            e.printStackTrace();
            System.exit(1);
        }
        return css;
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
