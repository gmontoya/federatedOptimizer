import java.util.*;
import java.io.*;

class addInterDatasetCss {

    public static void main(String[] args) {

        String iisFile1 = args[0];
        String iisFile2 = args[1];
        String cssFile = args[2];
   
        HashMap<String, Integer> iis1 = readIIS(iisFile1);
        HashMap<String, Integer> iis2 = readIIS(iisFile2);

        //HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio1 = readIIO(iioFile1);
        //HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio2 = readIIO(iioFile2);
        HashMap<Integer, HashMap<Integer, Integer>> css = new HashMap<Integer, HashMap<Integer, Integer>>();
        //HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> cps2 = new HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>();
        // Links 1 -> 2
        findLinks(iis1, iis2, css);
        //findLinks(iio2, iis1, cps2);
        writeCSS(css, cssFile);
        /*iio1=null;
        iis2=null;
        cps1=null;
        HashMap<String, Integer> iis1 = readIIS(iisFile1);
        HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio2 = readIIO(iioFile2);
        findLinks(iio2, iis1, cps2);
        writeCPS(cps2, cpsFile2);*/
        //System.out.println(cps1);
        //System.out.println(cps2);
    }

    public static void findLinks(HashMap<String, Integer> iis1, HashMap<String, Integer> iis2, HashMap<Integer, HashMap<Integer, Integer>> css) {
        for (String e : iis1.keySet()) {
            Integer cs2 = iis2.get(e);
            if (cs2 == null) {
                continue;
            }
            Integer cs1 = iis1.get(e);
            
            HashMap<Integer, Integer> cs2Count = css.get(cs1);
            
            if (cs2Count == null) {
                cs2Count = new HashMap<Integer, Integer>();
            }
            Integer c = cs2Count.get(cs2);
            if (c == null) {
                c = 0;
            }
            c++;
            cs2Count.put(cs2, c);
            css.put(cs1, cs2Count);
        }
    }

    public static void writeCSS(HashMap<Integer, HashMap<Integer, Integer>> css, String fileName) {

        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(fileName)));
            out.writeObject(css);
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

