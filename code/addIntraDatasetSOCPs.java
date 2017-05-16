import java.util.*;
import java.io.*;

class addIntraDatasetSOCPs {

    public static void main(String[] args) throws Exception {

        String IIO_SFile = args[0];
        String IIO_OFile = args[1];
        String CSS_OFile = args[2];
        String CPS_S_OFile = args[3];
        HashMap<String, Integer> iio_o = obtainMIPBasedIndex.readIIS(IIO_OFile);
        HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio_s = obtainMIPBasedIndex.readIIO(IIO_SFile);
        HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css_o = addInterDatasetLinksMIPs.readCSS(CSS_OFile);
        HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps = new HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>>();
        HashSet<String> os = new HashSet<String>(iio_s.keySet());
        os.retainAll(iio_o.keySet());
        for (String o : os) {
            HashMap<String, HashMap<Integer,Integer>> psCsCount = iio_s.get(o);
            Set<String> psSubj = psCsCount.keySet();
            Integer csObj = iio_o.get(o);
            Set<String> psObj = css_o.get(csObj).getSecond().keySet();
            HashSet<String> ps = new HashSet<String>(psSubj);
            ps.retainAll(psObj);
            for (String p : ps) {
                HashMap<Integer,Integer> csCount = psCsCount.get(p);
                for (Integer csSubj : csCount.keySet()) {
                    Integer tmpCount = csCount.get(csSubj);
                    HashMap<Integer, HashMap<String, Integer>> csPsCount = cps.get(csSubj);
                    if (csPsCount == null) {
                        csPsCount = new HashMap<Integer, HashMap<String, Integer>>();
                    }
                    HashMap<String, Integer> psCount = csPsCount.get(csObj);
                    if (psCount == null) {
                        psCount = new HashMap<String, Integer>();
                    }
                    Integer count = psCount.get(p);
                    if (count == null) {
                        count = 0;
                    }
                    count += tmpCount;
                    psCount.put(p, count);
                    csPsCount.put(csObj, psCount);
                    cps.put(csSubj, csPsCount);
                }   
            }
        }
        addInterDatasetLinksMIPs.writeCPS(cps, CPS_S_OFile);
    }
}
