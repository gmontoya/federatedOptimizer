import java.io.*;
import java.util.*;

class mergeCharacteristicSets {

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

    public static void writeCSS(HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css, String fileName) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(fileName)));
            out.writeObject(css);
            out.close();
        } catch (IOException e) {
            System.err.println("Problems writing file: "+fileName);
            e.printStackTrace();
            System.exit(1);
        }
    }

    class CharacteristicSet {

        protected int id;
        protected int count;
        protected HashMap<String, Integer> ps;
        public CharacteristicSet(int id, int count, HashMap<String, Integer> ps) {
            this.id = id;
            this.count = count;
            this.ps = ps;
        } 
        public void setCount(int c) {
            this.count = c;
        }
        public int getCount() {
            return this.count;
        }
        public void setPredicateCount(String p, int c) {
            this.ps.put(p, c);
        }
    }
    public static void main(String[] args) throws Exception {
        String fileCSS1 = args[0];
        String fileCSS2 = args[1];
        int N = Integer.parseInt(args[2]);
        int min = Integer.MAX_VALUE;
        Integer idMin = null;
        HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css1 = readCSS(fileCSS1);
        HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> reducedCSS = new HashMap<Integer, Pair<Integer, HashMap<String, Integer>>>();
        System.out.println(css1);
        for (Integer cs1 : css1.keySet()) {
            Pair<Integer, HashMap<String, Integer>> cPs = css1.get(cs1);
            Integer count = cPs.getFirst();
            if (reducedCSS.size() < N) {
                reducedCSS.put(cs1, cPs);
                if (min > count) {
                    min = count;
                    idMin = cs1;
                }
            } else {
                Pair<Integer, HashMap<String, Integer>> tmpCPs = reducedCSS.get(idMin); 
                if ((count > min) || ((count == min) && (tmpCPs.getSecond().keySet().size() < cPs.getSecond().keySet().size()))) {
                    
                    reducedCSS.remove(idMin);
                    reducedCSS.put(cs1, cPs);
                    //System.out.println("reducingM "+idMin);
                    mergeCS(tmpCPs, reducedCSS);
                } else {
                    //System.out.println("reducing "+cs1);
                    mergeCS(cPs, reducedCSS);
                }
                int tmpMin = Integer.MAX_VALUE;
                Integer tmpIdMin = null;                
                for (Integer tmp : reducedCSS.keySet()) {
                   int tmpCount = reducedCSS.get(tmp).getFirst();
                   if (tmpCount < tmpMin) {
                       tmpMin = tmpCount;
                       tmpIdMin = tmp;
                   }
                }
                min = tmpMin;
                idMin = tmpIdMin;    
            }
        }
        writeCSS(reducedCSS, fileCSS2);
        System.out.println(css1.size()+" characteristic sets have been reduced to "+reducedCSS.size());
        System.out.println(reducedCSS);
    }

    public static void mergeCS(Pair<Integer, HashMap<String, Integer>> countPreds, HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> reducedCSS) {

        Integer tmpCount = countPreds.getFirst();
        HashMap<String, Integer> tmpPsCount = countPreds.getSecond();
        Set<String> tmpPs = tmpPsCount.keySet();

        Integer best = null;
        Integer bestCount = null;
        HashMap<String, Integer> bestPsCount = null;
        Set<String> bestPs = null;

        for (Integer cs2 : reducedCSS.keySet()) {
            Integer auxCount = reducedCSS.get(cs2).getFirst();
            HashMap<String, Integer> auxPsCount = reducedCSS.get(cs2).getSecond();
            Set<String> auxPs = auxPsCount.keySet();

            if (auxPs.containsAll(tmpPs)) {
                if ((best == null) || (bestPs.size()>auxPs.size()) || ((bestPs.size()==auxPs.size()) && (bestCount<auxCount))) {
                    best = cs2;
                    bestCount = auxCount;
                    bestPsCount = auxPsCount;
                    bestPs = auxPs;
                }
            }
        }
        if (best != null) {
            bestCount += tmpCount;
            for (String p : tmpPs) {
                Integer c = bestPsCount.get(p) + tmpPsCount.get(p);
                //System.out.println("c: "+c+". p: "+p+". bestPsCount.get(p): "+bestPsCount.get(p)+". tmpPsCount.get(p): "+ tmpPsCount.get(p));
                bestPsCount.put(p, c);
            }
            reducedCSS.put(best, new Pair(bestCount, bestPsCount));
            return;
        } 
        // best = null;
        HashSet<String> intersection = null;
        for (Integer cs2 : reducedCSS.keySet()) {
            HashSet<String> tmp = new HashSet<String>(reducedCSS.get(cs2).getSecond().keySet());
            tmp.retainAll(tmpPs);
            if ((tmp.size()>0) && ((best == null) || intersection.size() < tmp.size())) {
                best = cs2;
                intersection = tmp;
            }
        }
        if (best != null) {
            Set<String> complement = new HashSet<String>(tmpPs);
            complement.removeAll(intersection);
            HashMap<String, Integer> tmpPsCountI = new HashMap<String, Integer>();
            HashMap<String, Integer> tmpPsCountC = new HashMap<String, Integer>();
            for (String p : tmpPs) {
                Integer c = tmpPsCount.get(p);
                if (intersection.contains(p)) {
                    tmpPsCountI.put(p, c);
                } else {
                    tmpPsCountC.put(p, c);
                }
            }

            Pair<Integer, HashMap<String, Integer>> countPredsI = new Pair(tmpCount, tmpPsCountI);
            Pair<Integer, HashMap<String, Integer>> countPredsC = new Pair(tmpCount, tmpPsCountC);
            //System.out.println(countPredsI);
            mergeCS(countPredsI, reducedCSS);
            //System.out.println(countPredsC);
            mergeCS(countPredsC, reducedCSS);
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
