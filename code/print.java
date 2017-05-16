import java.util.*;

class print {

    public static void main(String[] args) {
        String fileIIS1 = args[0];
        //String fileIIO1 = args[0];
        HashMap<String, Integer> iis = obtainLightIndex.readIIS(fileIIS1);
        //HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio = obtainLightIndex.readIIO(fileIIO1);
        processSubjectIndex(iis);
        //processObjectIndex(iio);
    }

    public static void processSubjectIndex (HashMap<String, Integer> iis) {
        
        for (String e : iis.keySet()) {
            Integer cs = iis.get(e);
            String str = e;
            if (str.startsWith("<")) {
                str = str.substring(1, str.length()-1);
            }
            System.out.println(str+" "+cs);  
        }
    }

    public static void processObjectIndex (HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio) {
        for (String e : iio.keySet()) {
            HashMap<String, HashMap<Integer,Integer>> psCsCount = iio.get(e);
            String str = e;
            if (str.startsWith("<")) {
                str = str.substring(1, str.length()-1);
            }
            for (String p : psCsCount.keySet()) {
                HashMap<Integer,Integer> csCount = psCsCount.get(p);
                for (Integer cs : csCount.keySet()) {
                    System.out.println(str+" "+cs+" "+p);
                }
            }
            
        }
        
    }
}
