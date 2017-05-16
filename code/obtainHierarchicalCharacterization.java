import java.io.*;
import java.util.*;

class obtainHierarchicalCharacterization {

    public static HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> readCSS(String fileName) {
        HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css = null;
        try {
            ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(fileName)));

            css = (HashMap<Integer, Pair<Integer, HashMap<String, Integer>>>) in.readObject();
            in.close();
        } catch (Exception e) {
            System.err.println("Problems writing file: "+fileName);
            e.printStackTrace();
            System.exit(1);
        }
        return css;
    }

    public static Integer getIKey(Set<String> ps) {
        Integer ikey = -1;
        Vector<String> psAux = new Vector<String>(ps);
        Collections.sort(psAux);
        String key = "";
        for (String p : psAux) {
            key = key + p;
        }
        ikey = key.hashCode();
        return ikey;
    }

    public static HashMap<String, HashSet<Integer>> createPredicateIndex(HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css) {

        HashMap<String, HashSet<Integer>> predIndex = new HashMap<String, HashSet<Integer>>();
        for (Integer cs : css.keySet()) {
            //System.out.println("cs: "+cs);
            Set<String> ps = css.get(cs).getSecond().keySet();
            for (String p : ps) {
                //System.out.println("p: "+p);
                HashSet<Integer> relevantCSS = predIndex.get(p);
                if (relevantCSS == null) {
                    relevantCSS =  new HashSet<Integer>();
                }
                relevantCSS.add(cs);
                predIndex.put(p, relevantCSS);
            }
        }
        return predIndex;
    }

    public static void main(String[] args) {

        String fileCSS = args[0];
        String fileHC = args[1];
        String fileCost = args[2];
        String filePredIndex = args[3];
        String fileAdditionalSets = args[4];
        // CS_ID -> <CS_ID_CS, <Count, Predicate -> Count>>
        HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css = readCSS(fileCSS);
        // 
        //System.out.println(css);
        HashMap<String, HashSet<Integer>> predicateIndex = createPredicateIndex(css);
        writePredIndex(predicateIndex, filePredIndex);
        //System.out.println(predicateIndex);
        HashMap<Integer, Integer> hc = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> cost = new HashMap<Integer, Integer>();
        HashMap<Integer, Set<String>> csPsTmp = new HashMap<Integer, Set<String>>();
        computeHierarchicalCharacterization(css, predicateIndex, hc, cost, csPsTmp);
        //System.out.println(csPsTmp);
        //System.out.println(hc);
        //System.out.println(cost);
        //System.out.println(predicateIndex);
        writeMap(hc, fileHC);
        writeMap(cost, fileCost);
        writeAdditionalSets(csPsTmp, fileAdditionalSets);
    }

    public static void writeAdditionalSets(HashMap<Integer, Set<String>> csPsTmp, String fileName) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(fileName)));
            out.writeObject(csPsTmp);
            out.close();
        } catch (IOException e) {
            System.err.println("Problems writing file: "+fileName);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void writePredIndex(HashMap<String, HashSet<Integer>> predicateIndex, String fileName) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(fileName)));
            out.writeObject(predicateIndex);
            out.close();
        } catch (IOException e) {
            System.err.println("Problems writing file: "+fileName);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void writeMap(HashMap<Integer, Integer> map, String fileName) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(fileName)));
            out.writeObject(map);
            out.close();
        } catch (IOException e) {
            System.err.println("Problems writing file: "+fileName);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void computeHierarchicalCharacterization(HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css, HashMap<String, HashSet<Integer>> predicateIndex, HashMap<Integer, Integer> hc, HashMap<Integer, Integer> cost, HashMap<Integer, Set<String>> csPsTmp) {

        HashMap<Integer, Integer> costTmp = new HashMap<Integer, Integer>();
        for (Integer cs : css.keySet()) {
            Set<String> ps = css.get(cs).getSecond().keySet();
            Integer c = computeCost(ps, css, predicateIndex);
            costTmp.put(cs, c);
        }
        int i = 0;
        while (costTmp.size() > 0) {
            System.out.println("There are "+costTmp.size()+" elements in H_"+i);
            HashMap<Integer, Integer> costAux = new HashMap<Integer, Integer>();
            for (Integer cs : costTmp.keySet()) {
                Set<String> ps = null;
                if (css.containsKey(cs)) {
                    ps = css.get(cs).getSecond().keySet();
                } else {
                    ps = csPsTmp.get(cs);
                }
                if (ps.size() < 2) {
                    continue;
                }
                Integer cheapestSS = null;
                Integer cheapestCost = null;
                boolean toAdd = false;
                HashSet<String> psTmp = null;
                for (String p : ps) {
                    HashSet<String> psAux = new HashSet<String>(ps);
                    psAux.remove(p);
                    Integer key = getIKey(psAux);
                    Integer c = -1;
                    boolean toAddTmp = false;
                    if (cost.containsKey(key)) {
                        c = cost.get(key);
                    } else if (costTmp.containsKey(key)) {
                        c = costTmp.get(key);
                    } else if (costAux.containsKey(key)) {
                        c = costAux.get(key);
                    } else {
                        c = computeCost(psAux, css, predicateIndex);
                        toAddTmp = true;
                    }
                    if ((cheapestSS == null) || (c < cheapestCost)) {
                        cheapestSS = key;
                        cheapestCost = c;
                        toAdd = toAddTmp;
                        psTmp = psAux;
                    }
                }
                if (toAdd) {
                    costAux.put(cheapestSS, cheapestCost);
                    csPsTmp.put(cheapestSS, psTmp);
                }
                hc.put(cs, cheapestSS);
            }
            cost.putAll(costTmp);
            costTmp = costAux;
            i++;
        }
    }

    public static int computeCost(Set<String> ps, HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css, HashMap<String, HashSet<Integer>> predicateIndex) {
        int cost = 0;
        HashSet<Integer> intersection = null;
        for (String p : ps) {
            HashSet<Integer> tmp = predicateIndex.get(p);
            if (intersection == null) {
                intersection = new HashSet<Integer>(tmp);
            } else {
                intersection.retainAll(tmp);
            }
        }
        for (Integer cs : intersection) {
            cost += css.get(cs).getFirst();
        }
        return cost;
    }
}

