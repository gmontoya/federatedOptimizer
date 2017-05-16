import java.io.*;
import java.util.*;


class checkConsistency {

    public static void main(String[] args) throws Exception {
        String fileCSS = args[0];
        String fileCPS = args[1];
        String fileIIS = args[2];
        String fileIIO = args[3];
        HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css = readCSS(fileCSS);
        HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps = readCPS(fileCPS);
        HashMap<String, Integer> iis = readIIS(fileIIS);
        HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio = readIIO(fileIIO);

        for (Integer csL : cps.keySet()) {
            if (css.get(csL)==null) {
                report(csL, "left cs in cps");
            } else {
                HashMap<Integer, HashMap<String, Integer>> csRPsCount = cps.get(csL);
                for (Integer csR : csRPsCount.keySet()) {
                    if (css.get(csR)==null) {
                        report(csR, "right cs in cps for csL: "+csL);
                    }
                }
            }
        }
        for (String s : iis.keySet()) {
            Integer cs = iis.get(s);
            if (css.get(cs)==null) {
                report(cs, "subject cs in iis");
            }
        }
        for (String o : iio.keySet()) {
            HashMap<String, HashMap<Integer, Integer>> psCsCount = iio.get(o);
            for (String p : psCsCount.keySet()) {
                HashMap<Integer, Integer> csCount = psCsCount.get(p);
                for (Integer cs : csCount.keySet()) {
                    if (css.get(cs)==null) {
                        report(cs, "cs in iio");
                    }
                }
            }
        }
    }

    public static void report(Integer cs, String msg) {
        System.out.println(cs+": "+msg);
    }

    public static HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> readCSS(String file) throws Exception {
        final ObjectInputStream in = new ObjectInputStream(
            new BufferedInputStream(new FileInputStream(file)));

        final HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css = (HashMap<Integer, Pair<Integer, HashMap<String, Integer>>>) in.readObject();
        return css;
    }

    public static HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> readCPS(String file) throws Exception {
        final ObjectInputStream in = new ObjectInputStream(
            new BufferedInputStream(new FileInputStream(file)));

        final HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps = (HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>>) in.readObject();
        return cps;
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
