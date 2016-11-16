import java.io.*;
import java.util.*;

class produceStarJoinOrdering {

    public static void main(String[] args) throws Exception {

        String fileStar = args[0];
        String fileCSS = args[1];
        String fileHC = args[2];
        String fileAdditionalSets = args[3];
        String filePredIndex = args[4];
        String fileCost = args[5];

        HashSet<String> ps = readPS(fileStar);
        HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css = readCSS(fileCSS);
        HashMap<Integer, Integer> hc = readMap(fileHC);
        HashMap<Integer, Set<String>> additionalSets = readAdditionalSets(fileAdditionalSets);
        HashMap<String, HashSet<Integer>> predicateIndex = readPredicateIndex(filePredIndex);
        HashMap<Integer, Integer> cost = readMap(fileCost);
        LinkedList<String> order = getStarJoinOrdering(ps, css, hc, additionalSets, predicateIndex, cost);
        System.out.println(order);
    }

    public static LinkedList<String> getStarJoinOrdering(HashSet<String> ps, HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css, HashMap<Integer, Integer> hc, HashMap<Integer, Set<String>> additionalSets, HashMap<String, HashSet<Integer>> predicateIndex, HashMap<Integer, Integer> cost) {
        LinkedList<String> order = new LinkedList<String>();

        
        Integer s = getStartCS(ps, css, predicateIndex, hc, additionalSets, cost);
        //System.out.println(s);
        Set<String> sPreds = null;
        if (s != null) {
            if (css.containsKey(s)) {
                sPreds = css.get(s).getSecond().keySet();
            } else if (additionalSets.containsKey(s)) {
                sPreds = additionalSets.get(s);
            }
        }
        while ((sPreds != null) && hc.containsKey(s)) {
            Integer cheapestSS = hc.get(s);
            Set<String> cPreds = null;
            if (css.containsKey(cheapestSS)) {
                cPreds = css.get(cheapestSS).getSecond().keySet();
            } else {
                cPreds = additionalSets.get(cheapestSS);
            }
            Set<String> aux = new HashSet<String>(sPreds);
            aux.removeAll(cPreds);
            String p = aux.iterator().next();
            if (ps.contains(p)) {
                order.addFirst(p);
            }
            s = cheapestSS;
            sPreds = cPreds;
        }
        if ((sPreds != null) && sPreds.size()>0) {
            String p = sPreds.iterator().next();
            if (ps.contains(p)) {
                order.addFirst(p);
            }
        }
        return order;
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

    public static Integer getStartCS(HashSet<String> ps, HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css, HashMap<String, HashSet<Integer>> predicateIndex, HashMap<Integer, Integer> hc, HashMap<Integer, Set<String>> additionalSets, HashMap<Integer, Integer> cost) {

        Integer key = getIKey(ps);
        //System.out.println("key: "+key);
        if (hc.containsKey(key)) {
            return key;
        } 
        int c = computeCost(ps, css, predicateIndex);
        //System.out.println("cost: "+c);
        if (c == 0) {
            return null;
        }
        HashMap<Integer, Integer> costTmp = new HashMap<Integer, Integer>();
        costTmp.put(key, c);
        additionalSets.put(key, ps);
        completeHC(costTmp, css, hc, additionalSets, cost, predicateIndex);
        return key;
    }

    public static void completeHC(HashMap<Integer, Integer> costTmp, HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css, HashMap<Integer, Integer> hc, HashMap<Integer, Set<String>> additionalSets, HashMap<Integer, Integer> cost, HashMap<String, HashSet<Integer>> predicateIndex) {

        while (costTmp.size() > 0) {
            HashMap<Integer, Integer> costAux = new HashMap<Integer, Integer>();
            for (Integer cs : costTmp.keySet()) {
                Set<String> ps = null;
                if (css.containsKey(cs)) {
                    ps = css.get(cs).getSecond().keySet();
                } else {
                    ps = additionalSets.get(cs);
                }
                if (ps.size() < 2) {
                    continue;
                }
                Integer cheapestSS = null;
                Integer cheapestCost = null;
                for (String p : ps) {
                    HashSet<String> psAux = new HashSet<String>(ps);
                    psAux.remove(p);
                    Integer key = getIKey(psAux);
                    Integer c = -1;
                    if (cost.containsKey(key)) {
                        c = cost.get(key);
                    } else if (costTmp.containsKey(key)) {
                        c = costTmp.get(key);
                    } else if (costAux.containsKey(key)) {
                        c = costAux.get(key);
                    } else {
                        c = computeCost(psAux, css, predicateIndex);
                        costAux.put(key, c);
                        additionalSets.put(key, psAux);
                    }
                    if ((cheapestSS == null) || (c < cheapestCost)) {
                        cheapestSS = key;
                        cheapestCost = c;
                    }
                }
                hc.put(cs, cheapestSS);
            }
            cost.putAll(costTmp);
            costTmp = costAux;
        }
    }

    public static int computeCost(Set<String> ps, HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css, HashMap<String, HashSet<Integer>> predicateIndex) {
        int cost = 0;
        HashSet<Integer> intersection = null;
        //System.out.println(predicateIndex);
        for (String p : ps) {
            //System.out.println(p);
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

    public static HashMap<String, HashSet<Integer>> readPredicateIndex(String fileName) {
        HashMap<String, HashSet<Integer>> predIndex = null;
        try {
            ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(fileName)));

            predIndex = (HashMap<String, HashSet<Integer>>) in.readObject();
            in.close();
        } catch (Exception e) {
            System.err.println("Problems writing file: "+fileName);
            e.printStackTrace();
            System.exit(1);
        }
        return predIndex;
    }

    public static HashMap<Integer, Set<String>> readAdditionalSets(String fileName) {
        HashMap<Integer, Set<String>> as = null;
        try {
            ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(fileName)));

            as = (HashMap<Integer, Set<String>>) in.readObject();
            in.close();
        } catch (Exception e) {
            System.err.println("Problems writing file: "+fileName);
            e.printStackTrace();
            System.exit(1);
        }
        return as;
    }

    public static HashMap<Integer, Integer> readMap(String fileName) {
        HashMap<Integer, Integer> map = null;
        try {
            ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(fileName)));

            map = (HashMap<Integer, Integer>) in.readObject();
            in.close();
        } catch (Exception e) {
            System.err.println("Problems writing file: "+fileName);
            e.printStackTrace();
            System.exit(1);
        }
        return map;
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
}
