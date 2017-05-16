import java.io.*;
import java.util.*;

class reduceCPS {

    public static Integer getIKey(String file) {
        Integer ikey = -1;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String l = br.readLine();
            Vector<String> ps = new Vector<String>();
            while (l!=null) {
                ps.add(l);
                l = br.readLine();
            }
            br.close();
            Collections.sort(ps);
            String key = "";
            for (String p : ps) {
               key = key + p;
            }
            ikey = key.hashCode();
        } catch (IOException e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }
        return ikey;
    }

    public static HashSet<String> readPS(String file) {
        HashSet<String> ps = new HashSet<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String l = br.readLine();
            //Vector<String> ps = new Vector<String>();
            while (l!=null) {
                ps.add(l);
                l = br.readLine();
            }
            br.close();
            //Collections.sort(ps);
            //String key = "";
            //for (String p : ps) {
            //   key = key + p;
            //}
            //ikey = key.hashCode();
        } catch (IOException e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }
        return ps;
    }

    public static void main(String[] args) throws Exception {
        String fileCPS1 = args[0];
        String fileCPS2 = args[1];
        int M = Integer.parseInt(args[2]);
        int b = 0;
        int a = 0;
        HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps = readCPS(fileCPS1);
        HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> reducedCPS = new HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>>();
        for (Integer css1 : cps.keySet()) {
             HashMap<Integer, HashMap<String, Integer>> css2PsCount = cps.get(css1);
             HashMap<Integer, HashMap<String, Integer>> reducedCss2PsCount = new HashMap<Integer, HashMap<String, Integer>>();
             for (Integer css2 : css2PsCount.keySet()) {
                 HashMap<String, Integer> psCount = css2PsCount.get(css2);
                 HashMap<String, Integer> reducedPsCount = new HashMap<String, Integer>();
                 b++;
                 for (String ps : psCount.keySet()) {
                     int c = psCount.get(ps);
                     if (c >= M) {
                         reducedPsCount.put(ps, c);
                     }
                 }
                 if (reducedPsCount.size()>0) {
                     a++;
                     reducedCss2PsCount.put(css2, reducedPsCount);
                 }
             }
             if (reducedCss2PsCount.size()>0) {
                 reducedCPS.put(css1, reducedCss2PsCount);
             }
        }

        writeCPS(reducedCPS, fileCPS2);
        System.out.println(b+" characteristic pairs have been reduced to "+a);
        //System.out.println(reducedCPS);
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

    public static void showInfo(Pair<Integer, HashMap<String, Integer>> infoCS) {

        Integer count = infoCS.getFirst();
        HashMap<String, Integer> predsMult = infoCS.getSecond();
        System.out.println("count: "+count);
        for (String p : predsMult.keySet()) {
            Integer m = predsMult.get(p);
            System.out.println("mult("+p+"): "+m); 
        }
    }
    
    public static void showLinks(HashMap<String, Integer> links) {

        for (String p : links.keySet()) {
            Integer m = links.get(p);
            System.out.println("link_mult("+p+"): "+m); 
        }
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

    public static HashMap<String, Integer> readIIS(String file) throws Exception {
        final ObjectInputStream in = new ObjectInputStream(
            new BufferedInputStream(new FileInputStream(file)));

        final HashMap<String, Integer> iis = (HashMap<String, Integer>) in.readObject();
        return iis;
    }

    public static HashMap<String, HashSet<Integer>> readIIO(String file) throws Exception {
        final ObjectInputStream in = new ObjectInputStream(
            new BufferedInputStream(new FileInputStream(file)));

        final HashMap<String, HashSet<Integer>> iio = (HashMap<String, HashSet<Integer>>) in.readObject();
        return iio;
    }
}
