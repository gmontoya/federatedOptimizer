import java.util.*;
import java.io.*;

class addInterDatasetLinksMIPs {

    public static void main(String[] args) {

        String mipISFile1 = args[0];
        String mipIOFile1 = args[1];
        String mipISFile2 = args[2];
        String mipIOFile2 = args[3];
        String cssFile1 = args[4];
        String cssFile2 = args[5];
        String cpsFile1 = args[6];
        String cpsFile2 = args[7];
        HashMap<Integer, MIPVector> mipIS1 = readMipISText(mipISFile1);
        HashMap<Integer, MIPVector> mipIS2 = readMipISText(mipISFile2);
        HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css1 = readCSS(cssFile1);
        HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css2 = readCSS(cssFile2);
        HashMap<Integer, HashMap<String,MIPVector>> mipIO1 = readMipIOText(mipIOFile1, css1);
        HashMap<Integer, HashMap<String,MIPVector>> mipIO2 = readMipIOText(mipIOFile2, css2);
        HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps1 = new HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>>();
        HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps2 = new HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>>();
        // Links 1 -> 2
        findLinks(mipIO1, mipIS2, cps1);
        //System.out.println("Total of computed CPs: "+cps1.size());
        writeCPS(cps1, cpsFile1);
        //System.out.println(cps1);
        cps1 = null;
        findLinks(mipIO2, mipIS1, cps2);
        //System.out.println("Total of computed CPs: "+cps2.size());
        writeCPS(cps2, cpsFile2);
        
        //System.out.println(cps2);
    }

    // input mipIO: CS_ID -> Predicate -> MIPVector
    // input liis: CS_ID -> MIPVector
    // output cps: CS_ID -> CS_ID -> Predicate -> count
    public static void findLinks(HashMap<Integer, HashMap<String,MIPVector>> mipIO, HashMap<Integer, MIPVector> mipIS, HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps) {   

        for (Integer csSubject : mipIO.keySet()) {
            HashMap<String,MIPVector> psMIPVector = mipIO.get(csSubject);
            for (String p : psMIPVector.keySet()) {
                MIPVector v1 = psMIPVector.get(p);
                for (Integer csObject : mipIS.keySet()) {
                    MIPVector v2 = mipIS.get(csObject);
                    int overlap = (int) v1.getOverlap(v2);
                    if (overlap == 0) {
                        continue;
                    }
                    HashMap<Integer, HashMap<String, Integer>> csObjPsCount = cps.get(csSubject);
                    if (csObjPsCount == null) {
                        csObjPsCount = new HashMap<Integer, HashMap<String, Integer>>();
                    }
                    HashMap<String, Integer> psCount = csObjPsCount.get(csObject);
                    if (psCount == null) {
                        psCount = new HashMap<String, Integer>();
                    }
                    Integer count = psCount.get(p);
                    if (count == null) {
                        count = 0;
                    }
                    count += overlap;
                    psCount.put(p, count);
                    csObjPsCount.put(csObject, psCount);
                    cps.put(csSubject, csObjPsCount);
                }
            }
        }
    }

    public static HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> readCSS(String file) {

        HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css = null;
        try {
            ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)));

            css = (HashMap<Integer, Pair<Integer, HashMap<String, Integer>>>) in.readObject();
            in.close();
        } catch (Exception e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }
        return css;
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

    public static HashMap<Integer, MIPVector> readMipISText(String file) {
        HashMap<Integer, MIPVector> mis = new HashMap<Integer, MIPVector>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String l = br.readLine();
            //Vector<String> ps = new Vector<String>();
            while (l!=null) {
                StringTokenizer st = new StringTokenizer(l, " [],");
                Integer cs = new Integer(st.nextToken());
                int size = Integer.parseInt(st.nextToken());
                MIPVector v = new MIPVector();
                v.setSize(size);
                v.clearMIP();
                Vector<Long> m = v.getMIP();
                while(st.hasMoreTokens()) {
                    Long elem = Long.parseLong(st.nextToken());
                    m.add(elem);
                }
                v.setN(m.size());
                mis.put(cs, v);
                l = br.readLine();
            }
            br.close();

        } catch (Exception e) {
            System.err.println("Problems reading file: "+file);
            e.printStackTrace();
            System.exit(1);
        }        
        return mis;
    }

    public static String getPredicate(int hcp, Integer cs, HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css) {

        String p = null;
        Pair<Integer, HashMap<String, Integer>> tmp = css.get(cs);
        if (tmp == null) {
            
            return p;
        }
        HashMap<String, Integer> psCount = tmp.getSecond();

        for (String pAux : psCount.keySet()) {
            if (pAux.hashCode() == hcp) {
                p = pAux;
                break;
            }
        }
        return p;
    }
    public static HashMap<Integer, HashMap<String,MIPVector>> readMipIOText(String file, HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css) {
        HashMap<Integer, HashMap<String,MIPVector>> mio = new HashMap<Integer, HashMap<String,MIPVector>>(); 
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String l = br.readLine();
            Integer cs = null;
            HashMap<String,MIPVector> hm = new HashMap<String, MIPVector>();

            //Vector<String> ps = new Vector<String>();
            while (l!=null) {
                StringTokenizer st = new StringTokenizer(l, " [],");
                if (st.countTokens()==1) {
                    // we are done with the previous
                    if ((cs != null) && (hm.size() > 0)) {
                        mio.put(cs, hm);
                        hm = new HashMap<String, MIPVector>();
                    }
                    cs = new Integer (st.nextToken());
                } else {
                    int hcp = Integer.parseInt(st.nextToken());
                    String p = getPredicate(hcp, cs, css);
                    if (p == null) {
                        l = br.readLine();
                        continue;
                    }
                    int size = Integer.parseInt(st.nextToken());
                    MIPVector v = new MIPVector();
                    v.setSize(size);
                    v.clearMIP();
                    Vector<Long> m = v.getMIP();
                    while(st.hasMoreTokens()) {
                        Long elem = Long.parseLong(st.nextToken());
                        m.add(elem);
                    }
                    v.setN(m.size());
                    hm.put(p, v);
                }
                l = br.readLine();
            }
            if ((cs != null) && (hm.size() > 0)) {
                mio.put(cs, hm);
            }
            br.close();
        } catch (Exception e) {
            System.err.println("Problems reading file: "+file);
            e.printStackTrace();
            System.exit(1);
        }        
        return mio;
    }
}

