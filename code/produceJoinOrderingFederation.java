import java.io.*;
import java.util.*;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.core.Var;

class produceJoinOrderingFederation {
    // DatasetId --> Position, for data related to CS
    static HashMap<Integer, Integer> datasetsIdPos = new HashMap<Integer,Integer>();
    // DatasetId --> DatasetId --> Position, for data related to CP
    static HashMap<Integer, HashMap<Integer, Integer>> datasetsIdsPos = new HashMap<Integer, HashMap<Integer,Integer>>();
    static Vector<HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>>> css = new Vector<HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>>>();
    static Vector<HashMap<Integer, Integer>> hcs = new Vector<HashMap<Integer, Integer>>();
    static Vector<HashMap<Integer, Set<String>>> additionalSets = new Vector<HashMap<Integer, Set<String>>>();
    static Vector<HashMap<Integer, Integer>> costs = new Vector<HashMap<Integer, Integer>>();
    static Vector<HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>>> cps = new Vector<HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>>>();
    static Vector<String> datasets = new Vector<String>();
    static Vector<String> endpoints = new Vector<String>();
    static String folder;
    static HashMap<Integer,HashMap<String, HashSet<Integer>>> predicateIndexes = new HashMap<Integer,HashMap<String, HashSet<Integer>>>();
    static boolean distinct;
    static List<Var> projectedVariables;
    static boolean includeMultiplicity;
    static boolean original;

    public static HashMap<Integer, Integer> getCost(Integer ds) {
        Integer pos = datasetsIdPos.get(ds);
        if (pos == null) {
            pos = loadFiles(ds);
        }
        HashMap<Integer, Integer> cost = costs.get(pos);
        return cost;
    }

    public static HashMap<Integer, Set<String>> getAdditionalSets(Integer ds) {
        Integer pos = datasetsIdPos.get(ds);
        if (pos == null) {
            pos = loadFiles(ds);
        }
        HashMap<Integer, Set<String>> ass = additionalSets.get(pos);
        return ass;
    }

    public static HashMap<Integer, Integer> getHC(Integer ds) {
        Integer pos = datasetsIdPos.get(ds);
        if (pos == null) {
            pos = loadFiles(ds);
        }
        HashMap<Integer, Integer> hc = hcs.get(pos);
        return hc;
    }

    public static HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> getCSS(Integer ds) {
        Integer pos = datasetsIdPos.get(ds);
        if (pos == null) {
            pos = loadFiles(ds);
        }
        HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer,Integer>>>> cs = css.get(pos);
        return cs;
    }
// HashMap<Integer, HashMap<Integer, Integer>> datasetsIdsPos
    public static HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> getCPS(Integer ds1, Integer ds2) {
        boolean loaded = false;
        HashMap<Integer, Integer> ds2Pos = datasetsIdsPos.get(ds1);
        Integer pos = -1;
        HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> c =null;
        if (ds2Pos != null) {
            pos = ds2Pos.get(ds2);
            if (pos != null) {
                loaded = true;
                c = cps.get(pos);
            }
        } else {
            ds2Pos = new HashMap<Integer, Integer>();
        }
        if (!loaded) {
            pos = cps.size();
            ds2Pos.put(ds2, pos);
            datasetsIdsPos.put(ds1, ds2Pos);
            String datasetStr1 = datasets.get(ds1);
            String datasetStr2 = datasets.get(ds2);
            String fileCPS = folder;
            if (datasetStr1.equals(datasetStr2)) {
                fileCPS += "/"+datasetStr1+"/statistics"+datasetStr1+"_cps_reduced10000CS";
            } else {
                fileCPS += "/cps_"+datasetStr1+"_"+datasetStr2+"_reduced10000CS_MIP";
            }
            c = readCPS(fileCPS);
            cps.add(pos, c);
        } 
        return c;
    }

    public static int loadFiles(Integer ds) {
        int pos = css.size();
        datasetsIdPos.put(ds, pos);
        String datasetStr = datasets.get(ds);
        String fileCSS = folder+"/"+datasetStr+"/statistics"+datasetStr+"_css_reduced10000CS";
        HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> cs = produceStarJoinOrdering.readCSS(fileCSS);
        css.add(pos, cs);
        String fileHC = folder+"/"+datasetStr+"/statistics"+datasetStr+"_hc_reduced10000CS";
        HashMap<Integer, Integer> hc = produceStarJoinOrdering.readMap(fileHC);
        hcs.add(pos, hc);
        String fileAdditionalSets = folder+"/"+datasetStr+"/statistics"+datasetStr+"_as_reduced10000CS";
        HashMap<Integer, Set<String>> ass = produceStarJoinOrdering.readAdditionalSets(fileAdditionalSets);
        additionalSets.add(pos, ass);
        String fileCost = folder+"/"+datasetStr+"/statistics"+datasetStr+"_cost_reduced10000CS";
        HashMap<Integer, Integer> cost = produceStarJoinOrdering.readMap(fileCost);
        costs.add(pos, cost);
        return pos;
    }

    public static HashMap<String, HashSet<Integer>> getPredicateIndex(Integer ds, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) {
        //System.out.println("getPredicateIndex for dataset "+ds);
        Integer pos = datasetsIdPos.get(ds);
        //System.out.println("it may be in position "+pos);
        HashMap<String, HashSet<Integer>> predIndex = null;
        if (pos == null) {
            pos = loadFiles(ds);
        }
        //System.out.println("its position is: "+pos+". current size of predicate indexes: "+predicateIndexes.size());
        predIndex = predicateIndexes.get(pos);
        if (predIndex == null) {
            predIndex = new HashMap<String, HashSet<Integer>>();
            for (String p : predicateIndex.keySet()) {
                HashMap<Integer,HashSet<Integer>> dsCss = predicateIndex.get(p);
                HashSet<Integer> css = dsCss.get(ds);
                if (css != null) {
                    predIndex.put(p, css);
                }
            }
            predicateIndexes.put(pos, predIndex);
        }
        return predIndex;
    }

    public static HashMap<String, HashMap<Integer,HashSet<Integer>>> readPredicateIndexes(String folder, Vector<String> datasets) {

        HashMap<String, HashMap<Integer,HashSet<Integer>>> predIndex = new HashMap<String, HashMap<Integer,HashSet<Integer>>>();
        for (int i = 0; i < datasets.size(); i++) {
            String datasetStr = datasets.get(i);
            String fileName = folder+"/"+datasetStr+"/statistics"+datasetStr+"_pi_reduced10000CS";
            
            try {
                ObjectInputStream in = new ObjectInputStream(
                    new BufferedInputStream(new FileInputStream(fileName)));

                HashMap<String, HashSet<Integer>> predIndexTmp = (HashMap<String, HashSet<Integer>>) in.readObject();
                in.close();
                for (String p : predIndexTmp.keySet()) {
                    HashSet<Integer> css = predIndexTmp.get(p);
                    HashMap<Integer,HashSet<Integer>> dsCss = predIndex.get(p);
                    if (dsCss == null) {
                        dsCss = new HashMap<Integer,HashSet<Integer>>();
                    }
                    dsCss.put(i, css);
                    predIndex.put(p, dsCss);
               }
            } catch (Exception e) {
                System.err.println("Problems reading file: "+fileName);
                e.printStackTrace();
                System.exit(1);
            }
        }
        return predIndex;
    }

    public static Vector<String> readDatasets(String file) {
        Vector<String> ps = new Vector<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String l = br.readLine();
            //Vector<String> ps = new Vector<String>();
            while (l!=null) {
                StringTokenizer st = new StringTokenizer(l);
                ps.add(st.nextToken());
                endpoints.add(st.nextToken());
                l = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }
        return ps;
    }

    public static void main(String[] args) {

        String queryFile = args[0];
        String datasetsFile = args[1];
        folder = args[2];
        long budget = Long.parseLong(args[3]);
        includeMultiplicity = Boolean.parseBoolean(args[4]);
        original = Boolean.parseBoolean(args[5]);
        String fileName = args[6];
        datasets = readDatasets(datasetsFile);
        // Predicate --> DatasetId --> set of CSId
        HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex = readPredicateIndexes(folder, datasets); 
        //System.out.println("predicateIndex size: "+predicateIndex.size());
        BGP bgp = new BGP(queryFile);
        distinct = bgp.getDistinct();
        projectedVariables = bgp.getProjectedVariables();
        // sub-query --> <order, <cardinality,cost>>
        HashMap<HashSet<Triple>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long, Long>>> DPTable = new HashMap<HashSet<Triple>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long, Long>>>();
        Vector<HashSet<Triple>> stars = getStars(bgp, budget, predicateIndex); //css, predicateIndex, cost);
        System.out.println("stars: "+stars);
        int i = 1;
        HashSet<Triple> triples = new HashSet<Triple>();
        HashMap<Node, Vector<Tree<Pair<Integer,Triple>>>> map = new HashMap<Node, Vector<Tree<Pair<Integer,Triple>>>>();
        for (HashSet<Triple> s : stars) {
            // consider that previous star may be connected to the current star
            HashMap<Triple,Triple> renamed = new HashMap<Triple,Triple>();
            HashSet<Triple> renamedStar = rename(s, map, renamed);
            // for updating the bgp consider the renamed star
            Node nn = update(bgp, renamedStar, i);
            // current star should be consistent with renamed star
            s.clear();
            for (Triple t : renamedStar) {
                Triple r = renamed.get(t);
                s.add(r);
            }
            System.out.println("updated bgp "+bgp+" for star : "+s+" and for nn: "+nn);
            // current star is used for the join ordering, it does not contain any metanode
            if (s.size()>0) {
                Vector<Tree<Pair<Integer,Triple>>> p = getStarJoinOrder(s, predicateIndex); //, css, hc, additionalSets, predicateIndex, cost);
                System.out.println("join ordering for the star: "+p);
                if (p.size() ==0) {  // test case missing sources for a star
                    triples.clear();
                    map.clear();
                    bgp.getBody().clear();
                    break;
                }
                map.put(nn, p);
                i++;
            }
            //HashSet<Triple> ns = new HashSet<Triple>();
            //ns.add(nn);
            //triples.add(nn);
            //DPTable.put(ns, new Pair<Vector<Triple>, Pair<Long,Long>>(p, new Pair<Long, Long>(cost(p, css, predicateIndex, cost),0L)));
        }
        
        System.out.println("map: "+map);
        System.out.println("DPTable before add.. :"+DPTable);
        addRemainingTriples(DPTable, bgp, predicateIndex, triples, map); //, css, cps, predicateIndex, triples, cost, map, hc, additionalSets);
        System.out.println("DPTable after add.. :"+DPTable);
        // may have to consider already intermediate results in the CP estimation for the cost
        estimateSelectivityCP(DPTable, predicateIndex, triples); //css, cps, predicateIndex, triples, hc, additionalSets, cost);
        System.out.println("DPTable after CP estimation.. :"+DPTable);
        computeJoinOrderingDP(DPTable, triples);
        Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long, Long>> res = DPTable.get(triples);
        if (res != null && res.getFirst().size() > 0) {
            String plan = toString(res.getFirst());
            System.out.println("Plan: "+plan);
            System.out.println("Cardinality: "+res.getSecond().getFirst());
            System.out.println("Cost: "+res.getSecond().getSecond());
            write(plan, fileName);
        } else {
            System.out.println("No plan was found");
        }
        System.out.println("DPTable at the end: "+DPTable);
    }

    public static void write(String content, String fileName) {

        try {
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(fileName));
            osw.write(content+"\n");
            osw.close();
        } catch (IOException e) {
            System.err.println("Problems writing file: "+fileName);
            System.exit(1);
        }
    }

    public static String tripleTreeToString(Tree<Pair<Integer, Triple>> tree) {
        String str = "";
        if (tree instanceof Leaf<?>) {
            Triple t  = tree.getOneElement().getSecond();
            Node s = t.getSubject();
            if (s.isURI()) {
                str += "<"+s.getURI()+"> ";
            } else {
                str += s.toString()+" ";
            }
            Node p = t.getPredicate();
            if (p.isURI()) {
                str += "<"+p.getURI()+"> ";
            } else {
                str += p.toString()+" ";
            }
            Node o = t.getObject();
            if (o.isURI()) {
                str += "<"+o.getURI()+">";
            } else {
                str += o.toString();
            }
            //str = t.toString();
        } else {
            Branch<Pair<Integer, Triple>> b = (Branch<Pair<Integer, Triple>>) tree;
            Tree<Pair<Integer, Triple>> l = b.getLeft();
            Tree<Pair<Integer, Triple>> r = b.getRight();
            str = " { " + tripleTreeToString(l) + " . " + tripleTreeToString(r) + " } ";            
        }
        return str;
    }

    public static Integer sameSource(Set<Pair<Integer, Triple>> elems) {
        boolean ss = true;
        Integer s = null;
        for (Pair<Integer, Triple> pair : elems) {
            Integer tmpS =  pair.getFirst();
            ss = ss && (s==null||s.equals(tmpS));
            s = tmpS;
            if (!ss) {
                s = null;
                break;
            }
        }
        return s;
    }

    public static boolean sameSource(Set<Pair<Integer, Triple>> elems, Integer s) {
        boolean ss = true;
        for (Pair<Integer, Triple> pair : elems) {
            Integer tmpS =  pair.getFirst();
            ss = ss && s.equals(tmpS);
            if (!ss) {
                break;
            }
        }
        return ss;
    }

    public static String treeToString(Tree<Pair<Integer, Triple>> tree) {
        Set<Pair<Integer, Triple>> elems = tree.getElements();
        String str = "";
        if (sameSource(elems) != null) {
            str = tripleTreeToString(tree);
            String source = endpoints.get(elems.iterator().next().getFirst());
            str = "SERVICE <"+source+"> { "+str+" } .";
        } else {
            Branch<Pair<Integer, Triple>> b = (Branch<Pair<Integer, Triple>>) tree;
            Tree<Pair<Integer, Triple>> l = b.getLeft();
            Tree<Pair<Integer, Triple>> r = b.getRight();
            str = " { " + treeToString(l) + " } . { " + treeToString(r) + " } ";
        }
        return str;
    }

    public static String toString(Vector<Tree<Pair<Integer,Triple>>> plan) {
        String str = "";
        //if (plan.size()>1) {
            str += " SELECT "+(distinct?"DISTINCT ":"");
        for (Var v : projectedVariables) {
            str += v.toString()+" ";
        }
        if (plan.size()>1) {
            str += " { ";
        }
        str += " { ";
        //}

        str += treeToString(plan.get(0));
        for (int i = 1; i < plan.size(); i++) {
            str += " } UNION { "+treeToString(plan.get(i));
        }
        str += " }";
        if (plan.size()>1) {
            str += " }";
        }
        return str;
    }
    public static HashSet<Triple> rename(HashSet<Triple> star, HashMap<Node, Vector<Tree<Pair<Integer,Triple>>>> map, HashMap<Triple,Triple> renamed) {

        HashMap<Node, Node> invertMap = new HashMap<Node,Node>();
        for (Node n : map.keySet()) {
            Node s = map.get(n).get(0).getOneElement().getSecond().getSubject();
            invertMap.put(s, n);
        }
        HashSet<Triple> newStar = new HashSet<Triple>();
        for (Triple t : star) {
            Node s = t.getSubject();
            if (invertMap.containsKey(s)) {
                s = invertMap.get(s);
            }
            Node p = t.getPredicate();
            Node o = t.getObject();
            if (invertMap.containsKey(o)) {
                o = invertMap.get(o);
            }
            Triple newTriple = new Triple(s, p, o);
            newStar.add(newTriple);
            renamed.put(newTriple, t);
        }
        return newStar;
    }

    // CS and CP already provide cardinalities for sets of triples with one or two elements
    public static void computeJoinOrderingDP(HashMap<HashSet<Triple>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable, HashSet<Triple> triples) {

        for (int i = 3; i <= triples.size(); i++) {
            computeJoinOrderingDPAux(DPTable, triples, i);
        }
    }

    public static void getAllSubsets(HashSet<Triple> rn, int j, Vector<HashSet<Triple>> subsets) {
        if (j == 0) {
            subsets.add(new HashSet<Triple>());
            return;
        } else if (rn.size() == 0) {
            return;
        }
        HashSet<Triple> aux = new HashSet<Triple>(rn);
        Triple elem = rn.iterator().next();
        aux.remove(elem);
        Vector<HashSet<Triple>> subsetsAux = new Vector<HashSet<Triple>>();
        getAllSubsets(aux, j-1, subsetsAux);
        getAllSubsets(aux, j, subsets);
        for (HashSet<Triple> ss : subsetsAux) {
            ss.add(elem);
            subsets.add(ss);
        }
    }

    public static void computeJoinOrderingDPAux(HashMap<HashSet<Triple>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable, HashSet<Triple> triples, int i) {
        // currently cost depends only on intermediate results, then for the same two sub-queries their order do not matter
        // start considering sub-queries from half the size of the current sub-queries (i)
        int k = (int) Math.round(Math.ceil(i/2.0));
        HashMap<HashSet<Triple>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTableAux = new HashMap<HashSet<Triple>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>>();
        for (HashSet<Triple> ns : DPTable.keySet()) {
            if (ns.size() < k) {
                continue;
            }
            HashSet<Triple> rn = new HashSet<Triple>(triples);
            rn.removeAll(ns);
            Vector<HashSet<Triple>> subsets = new Vector<HashSet<Triple>>();
            getAllSubsets(rn, i-ns.size(), subsets);
            for (HashSet<Triple> ss : subsets) {
                //HashSet<Triple> intersection = new HashSet<Triple>(ns);
                //intersection.retainAll(ss);
                if (!DPTable.containsKey(ss)) { // || (intersection.size()>0)) {
                    continue;
                }
                Long card = getCard(DPTable, ns, ss);
                Long cost = card + DPTable.get(ns).getSecond().getFirst()+DPTable.get(ss).getSecond().getFirst();
                HashSet<Triple> newEntry = new HashSet<Triple>(ns);
                newEntry.addAll(ss);
                Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>> pair = DPTableAux.get(newEntry);
                if ((pair == null) || pair.getSecond().getSecond()>cost) {
                    //System.out.println("merging : "+ns+" and "+ss+". card: "+card+". cost: "+cost+". pair: "+pair+". newEntry: "+newEntry);
                    Vector<Tree<Pair<Integer,Triple>>> order = makeTree(DPTable.get(ns).getFirst(), DPTable.get(ss).getFirst(), null);
                    DPTableAux.put(newEntry, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(order, new Pair<Long,Long>(card, cost)));
                }
            }
        }
        DPTable.putAll(DPTableAux);
    }

    // to estimate the cardinality between two sub-queries, consider the information already in DPTable about the joins
    // between two triple patterns, one from each sub-query to compute the selectivity of the joins and the cardinality of the new join
    public static long getCard(HashMap<HashSet<Triple>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable, HashSet<Triple> sq1, HashSet<Triple> sq2) {
        long cardSQ1 = DPTable.get(sq1).getSecond().getFirst();
        long cardSQ2 = DPTable.get(sq2).getSecond().getFirst();
        double accSel = 1;
        for (Triple n : sq1) {
            for (Triple m : sq2) {
                HashSet<Triple> join = new HashSet<Triple>();
                join.add(n);
                join.add(m);
                Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long, Long>> val = DPTable.get(join);
                if (val == null) {
                    continue;
                }
                long cardJoin = val.getSecond().getFirst();
                join.remove(m);
                val = DPTable.get(join);
                long cardLeft = val.getSecond().getFirst();
                join.clear();
                join.add(m);
                val = DPTable.get(join);
                long cardRight = val.getSecond().getFirst();
                //System.out.println("cardJoin: "+cardJoin);
                //System.out.println("cardLeft: "+cardLeft);
                //System.out.println("cardRight: "+cardRight);
                double sel = ((double) cardJoin) / (cardLeft * cardRight);
                //System.out.println("sel: "+sel);
                accSel = accSel * sel;
           }
        }
        //System.out.println(cardSQ1 * cardSQ2);
        //System.out.println(cardSQ1 * cardSQ2 *accSel);
        long card = Math.round(Math.ceil(cardSQ1 * cardSQ2 * accSel));
        return card;
    }

    public static void addRemainingTriples(HashMap<HashSet<Triple>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable, BGP bgp, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex, HashSet<Triple> triples, HashMap<Node, Vector<Tree<Pair<Integer,Triple>>>> map) {
//HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css, HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps, HashMap<String, HashSet<Integer>> predicateIndex, HashSet<Triple> triples, HashMap<Integer, Integer> cost, HashMap<Node, Tree<Triple>> map,HashMap<Integer, Integer> hc, HashMap<Integer, Set<String>> additionalSets) {

        List<Triple> ts = bgp.getBody();
        //System.out.println("ts: "+ts);
        for (Triple t : ts) {
            Node s = t.getSubject();
            Node p = t.getPredicate();
            Node o = t.getObject();
            HashSet<Triple> tps1 = new HashSet<Triple>();
            if (map.containsKey(s)) {
                Node e = map.get(s).get(0).getOneElement().getSecond().getSubject();
                Vector<Tree<Pair<Integer,Triple>>> tmp = map.get(s);
                for (Tree<Pair<Integer,Triple>> tmpTree : tmp) {
                    for (Pair<Integer,Triple> pair : tmpTree.getElements()) {
                        tps1.add(pair.getSecond());
                    }
                }
                s = e;
            }
            if (map.containsKey(o)) {
                Node e = map.get(o).get(0).getOneElement().getSecond().getSubject();
                o = e;
            }
            // triples and newEntry include the triple with metanodes
            triples.add(t); 
            HashSet<Triple> newEntry = new HashSet<Triple>();
            newEntry.add(t);
            // but for the join ordering the metanodes are replaced by the star variable
            t = new Triple(s, p, o);    
            tps1.add(t);

            addCS(tps1, newEntry, predicateIndex, DPTable); //css, predicateIndex, DPTable, hc, additionalSets, cost);
        }
    }

/*
    public static void addRemainingTriples(HashMap<HashSet<Triple>, Pair<Vector<Triple>, Pair<Long,Long>>> DPTable, BGP bgp, HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css, HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps, HashMap<String, HashSet<Integer>> predicateIndex, HashSet<Triple> triples, HashMap<Integer, Integer> cost, HashMap<Node, Vector<Triple>> map,HashMap<Integer, Integer> hc, HashMap<Integer, Set<String>> additionalSets) {

        List<Triple> ts = bgp.getBody();
        //System.out.println("ts: "+ts);
        for (Triple t : ts) {
            Node s = t.getSubject();
            Node p = t.getPredicate();
            Node o = t.getObject();
            Vector<Triple> tps1 = new Vector<Triple>();
            Vector<Triple> tps2 = new Vector<Triple>();
            if (map.containsKey(s)) {
                Node e = map.get(s).get(0).getSubject();
                tps1.addAll(map.get(s));
                s = e;
            }
            if (map.containsKey(o)) {
                Node e = map.get(o).get(0).getSubject();
                tps2.addAll(map.get(o));
                o = e;
            }
            triples.add(t); 
            HashSet<Triple> newEntry = new HashSet<Triple>();
            newEntry.add(t);
            t = new Triple(s, p, o);    
            Vector<Triple> tps = new Vector<Triple>();
            tps.add(t);
            if (tps1.size()>0 && tps2.size()>0) {
                HashSet<Triple> set = new HashSet<Triple>();
                set.addAll(tps1);
                set.addAll(tps);
                tps1 = getStarJoinOrder(set, css, hc, additionalSets, predicateIndex, cost);
                addCheapestCP(tps1, tps2, newEntry, new HashSet<Triple>(), css, cps, predicateIndex, DPTable);
            } else if (tps1.size()>0) {
                addCS(tps1, tps, new HashSet<Triple>(), newEntry, css, predicateIndex, DPTable, hc, additionalSets, cost);
            } else if (tps2.size()>0) {
                addCheapestCP(tps, tps2, newEntry, new HashSet<Triple>(), css, cps, predicateIndex, DPTable);
            } else {
                addCS(tps, new Vector<Triple>(), newEntry, new HashSet<Triple>(), css, predicateIndex, DPTable, hc, additionalSets, cost);
            }
           
        }
    }
*/
    public static boolean existsCPConnection(HashSet<Triple> sq1, HashSet<Triple> sq2) {

        return existsCSConnection(sq1) && existsCSConnection(sq2) && (existsCPConnectionAux(sq1, sq2) || existsCPConnectionAux(sq2, sq1));
    }

    public static boolean existsCPConnectionAux(HashSet<Triple> sq1, HashSet<Triple> sq2) {

        boolean e = false;
        Node c = sq2.iterator().next().getSubject();
        for (Triple t : sq1) {
            Node oTmp = t.getObject();
            e = e || (oTmp.equals(c));
        }
        return e;
    }

    public static boolean existsCSConnection(HashSet<Triple> sq) {
        boolean e = true;
        Node s = null;
        for (Triple t : sq) {
            Node sTmp = t.getSubject();
            e = e && ((s == null) || s.equals(sTmp));
            s = sTmp;
        }
        return e;
    }

    public static void estimateSelectivityCP(HashMap<HashSet<Triple>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex, HashSet<Triple> triples) {
//HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css, HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps, HashMap<String, HashSet<Integer>> predicateIndex, HashSet<Triple> triples,HashMap<Integer, Integer> hc, HashMap<Integer, Set<String>> additionalSets, HashMap<Integer, Integer> cost) {

        Vector<HashSet<Triple>> subqueries = new Vector<HashSet<Triple>>(DPTable.keySet());
        for (int i = 0; i < subqueries.size(); i++) {
            HashSet<Triple> sq1 = subqueries.get(i);
            for (int j = i+1; j < subqueries.size(); j++) {
                HashSet<Triple> sq2 = subqueries.get(j);
                Vector<Tree<Pair<Integer,Triple>>> ts1 = DPTable.get(sq1).getFirst();
                Vector<Tree<Pair<Integer,Triple>>> ts2 = DPTable.get(sq2).getFirst();
                if (existsCPConnection(sq1, sq2)) {
                    addCheapestCP(ts1, ts2, sq1, sq2, predicateIndex, DPTable); //css, cps, predicateIndex, DPTable);
                }
                HashSet<Triple> sq = new HashSet<Triple>(sq1);
                sq.addAll(sq2);
                if (existsCSConnection(sq)) {
                    // just for the case of very expensive stars that were not included in the DPTable
                    HashSet<Triple> ts = new HashSet<Triple>();
                    for (Tree<Pair<Integer,Triple>> tmpTree : ts1) {
                        for (Pair<Integer,Triple> pair : tmpTree.getElements()) {
                            ts.add(pair.getSecond());
                        }
                    }
                    for (Tree<Pair<Integer,Triple>> tmpTree : ts2) {
                        for (Pair<Integer,Triple> pair : tmpTree.getElements()) {
                            ts.add(pair.getSecond());
                        }
                    }
                    //HashMap<Integer, Integer> css1 = new HashMap<Integer, Integer>(ts1.getCSDS());
                    //HashMap<Integer, Integer> css1 = css.retainAll(ts2.getCS());

                    addCS(ts, sq, predicateIndex, DPTable); //, hc, additionalSets, cost);
                }
            }
        }
    }

    // precondition: triples in sq1 share the same subject
    public static void addCS(HashSet<Triple> ts1, HashSet<Triple> sq1, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex, HashMap<HashSet<Triple>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable) {

//HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css, HashMap<String, HashSet<Integer>> predicateIndex, HashMap<HashSet<Triple>, Pair<Tree<Triple>, Pair<Long,Long>>> DPTable, HashMap<Integer, Integer> hc, HashMap<Integer, Set<String>> additionalSets, HashMap<Integer, Integer> cost) {

        Vector<Tree<Pair<Integer,Triple>>> p = getStarJoinOrder(ts1, predicateIndex); //css, hc, additionalSets, predicateIndex, cost);
        if (p.size() == 0) {
            return;
        }
        HashSet<Triple> newEntry = new HashSet<Triple>(sq1);
        long c = cssCostTree(p, predicateIndex); //css, predicateIndex, cost);
        Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>> data = DPTable.get(newEntry);
        if (data == null || data.getSecond().getFirst()>c) {
            DPTable.put(newEntry, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(p, new Pair<Long, Long>(c,0L)));
        }
    }

    public static void addCheapestCP(Vector<Tree<Pair<Integer,Triple>>> ts1, Vector<Tree<Pair<Integer,Triple>>> ts2, HashSet<Triple> sq1, HashSet<Triple> sq2, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex, HashMap<HashSet<Triple>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable) {
//HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css, HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps, HashMap<String, HashSet<Integer>> predicateIndex, HashMap<HashSet<Triple>, Pair<Tree<Triple>, Pair<Long,Long>>> DPTable) {
        //Set<Triple> setTS1 = ts1.getElements();
        //Set<Triple> setTS2 = ts2.getElements();
        Pair<Long,Vector<Tree<Pair<Integer,Triple>>>> costTree = getCostCPTree(ts1, ts2, predicateIndex); //css, cps, predicateIndex);
        long cost = costTree.getFirst();
        Vector<Tree<Pair<Integer,Triple>>> tree = costTree.getSecond();
        
        //System.out.println("cost of "+sq2+" and "+sq1+": "+cost21);
        HashSet<Triple> set = new HashSet<Triple>(sq1);
        set.addAll(sq2);

        Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>> data = DPTable.get(set);
        long c = Long.MAX_VALUE;
        if (data != null) {
            c = data.getSecond().getFirst();
        }
        // including the minimum cost if inferior to previous value
        // order not really considered here... just computing the cardinality
        if (cost<Long.MAX_VALUE && cost < c) {
            //order = makeTree(ts1, ts2, costSS12.getSecond());
            DPTable.put(set, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(tree, new Pair<Long, Long>(cost,0L)));
        }
    }

    public static Vector<Tree<Pair<Integer,Triple>>> makeTree(Vector<Tree<Pair<Integer,Triple>>> treeL, Vector<Tree<Pair<Integer,Triple>>> treeR, HashMap<Integer, HashSet<Integer>> sources) {
        Vector<Tree<Pair<Integer,Triple>>> res = new Vector<Tree<Pair<Integer,Triple>>>();
        if (sources == null) {
            for (int i = 0; i < treeL.size(); i++) {
                Tree<Pair<Integer,Triple>> tL = treeL.get(i);
                for (int j = 0; j < treeR.size(); j++) {
                    Tree<Pair<Integer,Triple>> tR = treeR.get(j);
                    Tree<Pair<Integer,Triple>> nT = new Branch<Pair<Integer,Triple>>(tL, tR);
                    res.add(nT);
                }
            }
        } else {
            for (Integer s1 : sources.keySet()) {
                HashSet<Integer> ss2 = sources.get(s1);
                for (int i = 0; i < treeL.size(); i++) {
                    Tree<Pair<Integer,Triple>> tL = treeL.get(i);
                    if (!sameSource(tL.getElements(), s1)) {
                        continue;
                    }
                    for (Integer s2 : ss2) {
                        for (int j = 0; j < treeR.size(); j++) {
                            Tree<Pair<Integer,Triple>> tR = treeR.get(j);
                            if (!sameSource(tR.getElements(), s2)) {
                                continue;
                            }
                            Tree<Pair<Integer,Triple>> nT = new Branch<Pair<Integer,Triple>>(tL, tR);
                            res.add(nT);
                        }
                    }
                }
            }
        }
        return res;
    }

    public static Set<String> obtainBoundPredicates(Tree<Pair<Integer,Triple>> tree, HashMap<String, Triple> map) {
        Set<String> ps = new HashSet<String>();
        Set<Pair<Integer,Triple>> elems = tree.getElements();
        for (Pair<Integer,Triple> pair : elems) {
            Triple t = pair.getSecond();
            Node p = t.getPredicate();
            if (p.isURI()) {
                String pStr = "<"+p.getURI()+">";
                ps.add(pStr);
                map.put(pStr, t);
            }
        }
        return ps;
    }

    public static Set<Triple> obtainTriples(Tree<Pair<Integer,Triple>> tree) {
        Set<Triple> triples = new HashSet<Triple>();
        Set<Pair<Integer,Triple>> elems = tree.getElements();
        for (Pair<Integer,Triple> pair : elems) {
            Triple t = pair.getSecond();
            triples.add(t);
        }
        return triples;
    }

    public static Pair<Long, Vector<Tree<Pair<Integer,Triple>>>> getCostCPTree(Vector<Tree<Pair<Integer,Triple>>> vLT, Vector<Tree<Pair<Integer,Triple>>> vRT, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) {
        Vector<Tree<Pair<Integer,Triple>>> res = new Vector<Tree<Pair<Integer,Triple>>>();
        long cost = 0L;
        for (Tree<Pair<Integer,Triple>> leftTree : vLT) {
            Integer sourceLT = sameSource(leftTree.getElements());
            if (sourceLT == null) {
                continue;
            }
            HashMap<String, Triple> mapL = new HashMap<String, Triple>();
            Set<String> predicatesLT = obtainBoundPredicates(leftTree, mapL);
            HashSet<Integer> relevantCssLT = computeRelevantCS(predicatesLT, sourceLT, predicateIndex);
            Long costLT = computeCost(getCSS(sourceLT), relevantCssLT, predicatesLT, mapL);
            for (Tree<Pair<Integer,Triple>> rightTree : vRT) {
                Integer sourceRT = sameSource(rightTree.getElements());
                if (sourceRT == null) {
                    continue;
                }         
                HashMap<String, Triple> mapR = new HashMap<String, Triple>();
                Set<String> predicatesRT = obtainBoundPredicates(rightTree, mapR);
                HashSet<Integer> relevantCssRT = computeRelevantCS(predicatesRT, sourceRT, predicateIndex);
                Long costRT = computeCost(getCSS(sourceRT), relevantCssRT, predicatesRT, mapR);

                Tree<Pair<Integer,Triple>> nT = null;
                if (costLT <= costRT) {
                    nT = new Branch<Pair<Integer,Triple>>(leftTree, rightTree);
                } else {
                    nT = new Branch<Pair<Integer,Triple>>(rightTree, leftTree);
                }
                long tmpCost = getCostCP(obtainTriples(leftTree), sourceLT, obtainTriples(rightTree), sourceRT, predicateIndex);
                if (tmpCost > 0) { 
                    res.add(nT);
                    cost += tmpCost;
                }
            }
        }
        return new Pair<Long, Vector<Tree<Pair<Integer,Triple>>>>(cost, res);
    }

    public static Long getMultiplicity(Integer m, String p,  Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs, HashSet<String> ps1, HashMap<String, Triple> map1, Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs2, HashSet<String> ps2, HashMap<String, Triple> map2) {

        double sel = 1.0;
        double selTmp = 1.0;
        Integer count = cPs.getFirst();
        //System.out.println("p: "+p);
        for (String p1 : ps1) {
            // COMMENT THE CONDITIONAL TO STICK TO THE PAPER FORMULA
            if (original || !p1.equals(p)) {
                //System.out.println("p1: "+p1);
                Triple t = map1.get(p1);
                Node s = t.getSubject();
                Node o = t.getObject();
                boolean c = includeMultiplicity && projectedVariables.contains(s) && (!distinct || projectedVariables.contains(o));
                if (c) {
                    Integer p_m = cPs.getSecond().get(p1).getFirst();
                    sel = sel*(((double)p_m)/count);
                    //System.out.println(p+": "+css.get(cs).getSecond().get(p)); 
                }
                if (!o.isVariable()) {
                    Integer p_m = cPs.getSecond().get(p1).getFirst();
                    Integer p_d = cPs.getSecond().get(p1).getSecond();
                    //System.out.println("p_m: "+p_m+". p_d: "+p_d);
                    double tmp = 1.0/ p_d;
                    if (tmp < selTmp) {
                        selTmp = tmp;
                    }
                }
            }
        }
        sel = sel * selTmp; // CONSIDER CONSTANTS first star
        selTmp = 1.0;
        count = cPs2.getFirst();
        for (String p2 : ps2) {
            //System.out.println("p2: "+p2);
            Triple t = map2.get(p2);
            Node s = t.getSubject();
            Node o = t.getObject();
            boolean c = includeMultiplicity && projectedVariables.contains(s) && (!distinct || projectedVariables.contains(o));
            if (c) {
                Integer p_m = cPs2.getSecond().get(p2).getFirst();
                sel = sel*(((double)p_m)/count);
                //System.out.println(p+": "+css.get(cs).getSecond().get(p)); 
            }
            if (!o.isVariable()) {
                Integer p_m = cPs2.getSecond().get(p2).getFirst();
                Integer p_d = cPs2.getSecond().get(p2).getSecond();
                //System.out.println("p_m: "+p_m+". p_d: "+p_d);
                double tmp = 1.0/ p_d;
                if (tmp < selTmp) {
                    selTmp = tmp;
                }
            }
        }
        sel = sel * selTmp;  // CONSIDER CONSTANTS second star
        //System.out.println("m: "+m+". sel: "+sel);
        //System.out.println("sel: "+sel);
        return Math.round(Math.ceil(m*sel));
    }

    public static long getCostCP(Set<Triple> sq1, Integer ds1, Set<Triple> sq2, Integer ds2, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) { //HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css, HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps, HashMap<String, HashSet<Integer>> predicateIndex) {

        HashSet<String> ps1 = new HashSet<String>();
        HashMap<String, Triple> map1 = new HashMap<String, Triple>();
        for (Triple t : sq1) {
            Node p = t.getPredicate(); 
            if (p.isURI()) {
                String pStr = "<"+p.getURI()+">";
                ps1.add(pStr);
                map1.put(pStr, t);
            }
        }
        
        HashSet<String> ps2 = new HashSet<String>();
        HashMap<String, Triple> map2 = new HashMap<String, Triple>();
        for (Triple t : sq2) {
            Node p = t.getPredicate(); 
            if (p.isURI()) {
                String pStr = "<"+p.getURI()+">";
                ps2.add(pStr);
                map2.put(pStr, t);
            }
        }

        HashSet<Integer> css1 = computeRelevantCS(ps1, ds1, predicateIndex);
        HashSet<Integer> css2 = computeRelevantCS(ps2, ds2, predicateIndex);

        // Predicate --> Count
        HashMap<String, Long> mult12 = new HashMap<String, Long>();
        long c = 0L;
        HashMap<String, Long> mult21 = new HashMap<String, Long>();

        for (Integer cs1 : css1) {
            Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs = getCSS(ds1).get(cs1);
            //HashMap<String, Pair<Integer, Integer>> ps = cPs.getSecond();

            for (Integer cs2 : css2) {
                Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs2 = getCSS(ds2).get(cs2);
                //HashMap<String, Pair<Integer, Integer>> psB = cPs2.getSecond();

                HashMap<Integer, HashMap<String, Integer>> css2Ps = getCPS(ds1, ds2).get(cs1);
                if (css2Ps != null) {
                    //System.out.println("Links 1 -> 2");
                    HashMap<String, Integer> links12 = css2Ps.get(cs2);

                    if (links12 != null) {
                        HashSet<String> relevantLinks = new HashSet<String>(links12.keySet());
                        relevantLinks.retainAll(ps1);
                        for (String p : relevantLinks) {
                            Triple t1 = map1.get(p);
                            Triple t2 = sq2.iterator().next();
                            if (t1.getObject().equals(t2.getSubject())) {
                                // COMMENTED TO OMMIT CONSIDERING THE MULTIPLICITY
                                //Long m1;
                                //if (includeMultiplicity) {  // getMultiplicity also consider constants
                                Long m1 = getMultiplicity(links12.get(p), p,  cPs, ps1, map1, cPs2, ps2, map2);
                                /*} else {
                                    m1 = (long) links12.get(p);
                                }*/
                                Long m2 = mult12.get(p);
                                if (m2 == null) {
                                    m2 = 0L;
                                }
                                mult12.put(p, m1+m2);
                            }
                        }
                    }
                }
                css2Ps = getCPS(ds2, ds1).get(cs2);
                if (css2Ps != null) {
                    //System.out.println("Links 2 -> 1");
                    HashMap<String, Integer> links21 = css2Ps.get(cs1);
                    if (links21 != null) {
                        HashSet<String> relevantLinks = new HashSet<String>(links21.keySet());
                        relevantLinks.retainAll(ps2);
                        for (String p : relevantLinks) {
                            Triple t2 = map2.get(p);
                            Triple t1 = sq1.iterator().next();
                            if (t2.getObject().equals(t1.getSubject())) {
                                // COMMENTED TO OMMIT CONSIDERING THE MULTIPLICITY
                                //Long m1;
                                //if (includeMultiplicity) {  // getMultiplicity also consider constants
                                Long m1 = getMultiplicity(links21.get(p), p, cPs2, ps2, map2, cPs, ps1, map1);
                                /*} else {
                                    m1 = (long) links21.get(p);
                                }*/
                                Long m2 = mult21.get(p);
                                if (m2 == null) {
                                    m2 = 0L;
                                }
                                mult21.put(p, m1+m2);
                            }
                        }
                    }
                }
            }
        }

        //System.out.println("Links 1 -> 2, count: "+c12);
        for (String p : mult12.keySet()) {
            Long m = mult12.get(p);
            c += m;
        }
        //System.out.println("Links 2 -> 1, count: "+c21);
        for (String p : mult21.keySet()) {
            Long m = mult21.get(p);
            c += m;
        }

        return c;
    }

    public static HashSet<Integer> computeRelevantCS(Set<String> ps, Integer ds, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) {

        HashSet<Integer> intersection = null;
        for (String p : ps) {
            HashMap<Integer,HashSet<Integer>> dsCss = predicateIndex.get(p);
            if (dsCss == null) {
                return new HashSet<Integer>();
            }
            HashSet<Integer> relevantCss = dsCss.get(ds);
            if (relevantCss == null) {
                return new HashSet<Integer>();
            }
            if (intersection == null) {
                intersection = new HashSet<Integer>(relevantCss);
            } else {
                intersection.retainAll(relevantCss);
            }
        }
        return intersection;
    }


    // predicateIndex: Predicate --> DatasetId --> set of CSId
    // returns Dataset --> set of CSId
    public static HashMap<Integer,HashSet<Integer>> computeRelevantCS(Set<String> ps, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) {

        HashMap<Integer,HashSet<Integer>> intersection = null;
        for (String p : ps) {
            HashMap<Integer,HashSet<Integer>> dsCss = predicateIndex.get(p);
            if (intersection == null) {
                intersection = new HashMap<Integer,HashSet<Integer>>();
                for (Integer ds : dsCss.keySet()) {
                    HashSet<Integer> css = new HashSet<Integer>(dsCss.get(ds));
                    intersection.put(ds, css);
                    //System.out.println("including "+css.size()+" css associated to dataset "+ds);
                }
            } else {
                //System.out.println("else. intersection with keys: "+intersection.keySet());
                Set<Integer> keysToDelete = new HashSet<Integer>(intersection.keySet());
                keysToDelete.removeAll(dsCss.keySet());
                //System.out.println("The predicate "+p+" has "+dsCss.keySet().size()+" relevant datasets: "+dsCss.keySet());
                //System.out.println("removing datasets "+keysToDelete+" from intersection");
                for (Integer k : keysToDelete) {
                    intersection.remove(k);
                }
                for (Integer ds : dsCss.keySet()) {
                    //System.out.println("considering dataset: "+ds);
                    //System.out.println("and intersection with keys: "+intersection.keySet());
                    if (intersection.containsKey(ds)) {
                        HashSet<Integer> css = new HashSet<Integer>(dsCss.get(ds));
                        //System.out.println(css.size()+" relevant css for predicate "+p+" in dataset "+ds);
                        css.retainAll(intersection.get(ds));
                        //System.out.println(css.size()+" relevant css after considering only the ones already in the intersection");
                        if (css.size()>0) {
                            intersection.put(ds, css);
                        } else {
                            intersection.remove(ds);
                        }
                    }
                }
            }
            //System.out.println("intersection size: "+intersection.size()+". with keys: "+intersection.keySet());
        }
        return intersection;
    }
    // precondition: triples in vTree share the same subject and all triples in each tree are evaluated at the same source
    public static long cssCostTree(Vector<Tree<Pair<Integer,Triple>>> vTree, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) {
        long cost = 0L;
        for (Tree<Pair<Integer,Triple>> tmpTree : vTree) {
            HashMap<String, Triple> map = new HashMap<String, Triple>();
            Set<String> ps = obtainBoundPredicates(tmpTree, map);
            Integer ds = tmpTree.getOneElement().getFirst();
            Integer key = produceStarJoinOrdering.getIKey(ps);        
            long c = 0L;
            HashMap<Integer, Integer> costHM = getCost(ds);
            /*if (costHM.containsKey(key)) {
                c += costHM.get(key);
            } else {*/
                HashSet<Integer> relevantCss = computeRelevantCS(ps, ds, predicateIndex);
                c += computeCost(getCSS(ds), relevantCss, ps, map);
            //}
            cost += c;
        }
        return cost;
    }

    // precondition: triples in tree share the same subject and all triples in each tree are evaluated at the same source
    public static long cssCostTree(Tree<Pair<Integer,Triple>> tree, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) {
        long cost = 0L;
            HashMap<String, Triple> map = new HashMap<String, Triple>();
            Set<String> ps = obtainBoundPredicates(tree, map);
            Integer ds = tree.getOneElement().getFirst();
            Integer key = produceStarJoinOrdering.getIKey(ps);        
            long c = 0L;
            HashMap<Integer, Integer> costHM = getCost(ds);
            /*if (costHM.containsKey(key)) {
                c += costHM.get(key);
            } else {*/
                HashSet<Integer> relevantCss = computeRelevantCS(ps, ds, predicateIndex);
                c += computeCost(getCSS(ds), relevantCss, ps, map);
            //}
            cost += c;
        return cost;
    }

    public static long cost(Set<Triple> ts, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) {
//HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css, HashMap<String, HashSet<Integer>> predicateIndex, HashMap<Integer, Integer> cost) {

        HashSet<String> ps = new HashSet<String>();
        HashMap<String, Triple> map = new HashMap<String, Triple>();
        for (Triple t : ts) {
            Node p = t.getPredicate(); 
            if (p.isURI()) {
                String pStr = "<"+p.getURI()+">";
                ps.add(pStr);
                map.put(pStr, t);
            }
        }

        HashMap<Integer, HashSet<Integer>> dsCss = computeRelevantCS(ps, predicateIndex);
        Integer key = produceStarJoinOrdering.getIKey(ps);        
        long c = 0L;
        for (Integer ds : dsCss.keySet()) {
            HashMap<Integer, Integer> cost = getCost(ds);
            /*if (cost.containsKey(key)) {
                c += cost.get(key);
            } else {*/
                c += computeCost(getCSS(ds), dsCss.get(ds), ps, map);
            //}
        }
        return c;
    }

    public static long computeCost(HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> css, HashSet<Integer> relevantCss, Set<String> ps, HashMap<String, Triple> map) {
        long cost = 0L;
        //System.out.println("map: "+map+". ps: "+ps);
        for (Integer cs : relevantCss) {
            //System.out.println("cs: "+css.get(cs));
            long costTmp = css.get(cs).getFirst();
            double sel = 1.0;
            //System.out.println("distinct: "+costTmp);
            double selTmp = 1.0;
            // COMMENTED TO OMMIT CONSIDERING THE MULTIPLICITY
            //if (includeMultiplicity) {
              for (String p : ps) {
                //System.out.println("p: "+p);
                Triple t = map.get(p);
                Node s = t.getSubject();
                Node o = t.getObject();
                boolean c = includeMultiplicity && projectedVariables.contains(s) && (!distinct || projectedVariables.contains(o));
                if (c) {
                    // p_m css.get(cs).getSecond().get(p).getFirst()
                    // p_d css.get(cs).getSecond().get(p).getSecond()
                    sel = sel*(((double)css.get(cs).getSecond().get(p).getFirst())/costTmp);
                    //System.out.println(p+": "+css.get(cs).getSecond().get(p)); 
                }
                if (!o.isVariable()) {
                    Integer p_m = css.get(cs).getSecond().get(p).getFirst();
                    Integer p_d = css.get(cs).getSecond().get(p).getSecond();
                    //System.out.println("p_m: "+p_m+". p_d: "+p_d);
                    double tmp = 1.0/ css.get(cs).getSecond().get(p).getSecond();
                    if (tmp < selTmp) {
                        selTmp = tmp;
                    }
                }
              }
            //}
            sel = sel * selTmp; // CONSIDER CONSTANTS
            //System.out.println("costTmp: "+costTmp+". sel: "+sel);
            cost += Math.round(Math.ceil(costTmp*sel));
        }
        //System.out.println("cost: "+cost);
        return cost;
    }

    public static Tree<Pair<Integer,Triple>> convertToTree(LinkedList<String> orderedPs, HashMap<String, Triple> map, Integer ds, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) {
        Tree<Pair<Integer,Triple>> sortedStar = null;
        for (String p : orderedPs) {
            if (map.containsKey(p)) {
                Triple t = map.get(p);

                Leaf<Pair<Integer,Triple>> leaf = new Leaf<Pair<Integer,Triple>>(new Pair<Integer,Triple>(ds, t));
                if (sortedStar == null) {
                    sortedStar = leaf;
                } else {
                    sortedStar = new Branch<Pair<Integer,Triple>>(sortedStar, leaf);
                }
                //sortedStar.add(t);
            }
        }
        considerConstants(sortedStar, predicateIndex);
        return sortedStar;
    }

    public static void considerConstants(Tree<Pair<Integer,Triple>> tree, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) {

        if (tree instanceof Leaf<?>) {
            return;
        }
        Branch<Pair<Integer, Triple>> b = (Branch<Pair<Integer, Triple>>) tree;
        Tree<Pair<Integer, Triple>> lTree = b.getLeft();
        Tree<Pair<Integer, Triple>> rTree = b.getRight();
        long cLeft = cssCostTree(lTree, predicateIndex);
        long cResult = cssCostTree(b, predicateIndex);
        long cRight = cssCostTree(rTree, predicateIndex);
        if (cRight <= cLeft + cResult) {
            if (lTree instanceof Leaf<?>) {
                 Tree<Pair<Integer, Triple>> aux = lTree;
                 b.setLeft(rTree);
                 b.setRight(aux);
            } else {
                 Branch<Pair<Integer, Triple>> lBranch = (Branch<Pair<Integer, Triple>>) lTree;
                 Tree<Pair<Integer, Triple>> aux = lBranch.getRight();
                 lBranch.setRight(rTree);
                 considerConstants(lBranch, predicateIndex);
                 b.setRight(aux);
            }
        }
    }

    public static Vector<Tree<Pair<Integer,Triple>>> getStarJoinOrder(HashSet<Triple> ts, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) { 
        HashSet<String> ps = new HashSet<String>();
        HashMap<String, Triple> map = new HashMap<String, Triple>();
        HashSet<Triple> remainingTriples = new HashSet<Triple>();
        for (Triple t : ts) {
            Node p = t.getPredicate(); 
            if (p.isURI()) {
                String pStr = "<"+p.getURI()+">";
                ps.add(pStr);
                map.put(pStr, t);
            } else {
                remainingTriples.add(t);
            }
        }
        HashMap<Integer, HashSet<Integer>> dsCss = computeRelevantCS(ps, predicateIndex);
        //System.out.println("dsCss for the star: "+dsCss);
        Vector<Tree<Pair<Integer,Triple>>> vTree = new Vector<Tree<Pair<Integer,Triple>>>();
        //HashMap<Integer, Tree<Triple>> dsTree = new HashMap<Integer, Tree<Triple>>();

        for (Integer ds : dsCss.keySet()) {
            HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> css = getCSS(ds);
            HashMap<Integer, Integer> hc = getHC(ds);
            HashMap<Integer, Set<String>> additionalSets = getAdditionalSets(ds);
            HashMap<Integer, Integer> cost = getCost(ds);
            HashMap<String, HashSet<Integer>> predicateIndexDS = getPredicateIndex(ds, predicateIndex);
            LinkedList<String> orderedPs = produceStarJoinOrdering.getStarJoinOrdering(ps, css, hc, additionalSets, predicateIndexDS, cost);
            //System.out.println("list of sorted predicates: "+orderedPs);
            Tree<Pair<Integer,Triple>> sortedStar = convertToTree(orderedPs, map, ds, predicateIndex);
            //System.out.println("tree of sorted triples: "+sortedStar);
            for (Triple t : remainingTriples) {
                Leaf<Pair<Integer,Triple>> leaf = new Leaf<Pair<Integer,Triple>>(new Pair<Integer,Triple>(ds, t));
                if (sortedStar == null) {
                    sortedStar = leaf;
                } else {
                    sortedStar = new Branch<Pair<Integer,Triple>>(sortedStar, leaf);
                }
            }
            vTree.add(sortedStar);
        }
        return vTree;
    }

    public static Node update(BGP bgp, HashSet<Triple> star, int i) {

        List<Triple> ts = bgp.getBody();
        //System.out.println("updating ts: "+ts+" for star: "+star);
        //HashSet<Triple> tsAux = new HashSet<Triple>();
        ts.removeAll(star);
        //System.out.println("ts after removing star triples: "+ts);
        HashSet<Node> vsStar = new HashSet<Node>();
        HashSet<Node> vsRest = new HashSet<Node>();
        Node nv = NodeFactory.createVariable("Subj"+i);
        //Node np = NodeFactory.createVariable("Pred"+i);
        //Node no = NodeFactory.createVariable("Obj"+i);
        //Triple nt = new Triple(nv, np, no);

        for (Triple t : ts) {
            Node s = t.getSubject();
            Node o = t.getObject();
            vsRest.add(s);
            vsRest.add(o);
        }
        //System.out.println("vsRest: "+vsRest);
        HashSet<Triple> toRemove = new HashSet<Triple>();
        for (Triple t : star) {
            Node s = t.getSubject();
            Node o = t.getObject();
            // keep connections of the star with the remaining triples
            if (vsRest.contains(o)) {
                ts.add(t);
                toRemove.add(t);
            }
        }
        //System.out.println("ts after adding star connecting triples: "+ts);
        //System.out.println("toRemove: "+toRemove);
        // triples should not appear twice, so remove connections from the star
        if (toRemove.size()>0) {
            star.removeAll(toRemove);
            toRemove.clear();
        } else { // stars not connected by an object also need a triple as connection
            Triple t = star.iterator().next();
            star.remove(t);
            ts.add(t);
        }
        //System.out.println("ts after adding missing star connecting triples: "+ts);

        // possible connections with the star triples
        for (Triple t : star) {
            Node s = t.getSubject();
            Node o = t.getObject();
            vsStar.add(o);
            vsStar.add(s);
        }
        //System.out.println("vsStar: "+vsStar);
        Vector<Triple> toAdd = new Vector<Triple>();
        for (Triple t : ts) {
            Node s = t.getSubject();
            Node p = t.getPredicate();
            Node o = t.getObject();
            if (vsStar.contains(s) && vsStar.contains(o)) {
                toAdd.add(new Triple(nv, p, nv));
                toRemove.add(t);
            } else if (vsStar.contains(s)) {
                toAdd.add(new Triple(nv, p, o));
                toRemove.add(t);
            } else if (vsStar.contains(o)) {
                toAdd.add(new Triple(s, p, nv));
                toRemove.add(t);
            }
        }
        //System.out.println("toRemove: "+toRemove);
        //System.out.println("toAdd: "+toAdd);
        ts.removeAll(toRemove);
        ts.addAll(toAdd);
        return nv;
    }

    public static Vector<HashSet<Triple>> getStars(BGP bgp, long budget, HashMap<String, HashMap<Integer,HashSet<Integer>>>  predicateIndex) {
//HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css, HashMap<String, HashSet<Integer>> predicateIndex, HashMap<Integer, Integer> cost) {
        Vector<HashSet<Triple>> stars = new Vector<HashSet<Triple>>();
        List<Triple> ts = new LinkedList<Triple>(bgp.getBody());
        HashMap<Node, HashSet<Triple>> bySubject = new HashMap<Node, HashSet<Triple>>();
        for (Triple t : ts) {
            
            Node s = t.getSubject();
            HashSet<Triple> sts = bySubject.get(s);
            if (sts==null) {
                sts = new HashSet<Triple>();
            }
            sts.add(t);
            bySubject.put(s, sts);
        }

        for (Node s : bySubject.keySet()) {
            HashSet<Triple> starSubj = bySubject.get(s);
            long card = cost(starSubj, predicateIndex);
            if (card <= budget) {
                stars.add(starSubj);
                ts.removeAll(starSubj);
            }
        }
        /* // stars by object, have to adjust the rest of the code before including these stars
        HashMap<Node, HashSet<Triple>> byObject = new HashMap<Node, HashSet<Triple>>();
        for (Triple t : ts) {
            
            Node o = t.getObject();
            HashSet<Triple> ots = byObject.get(o);
            if (ots==null) {
                ots = new HashSet<Triple>();
            }
            ots.add(t);
            byObject.put(o, ots);
        }

        for (Node o : byObject.keySet()) {
            HashSet<Triple> starObj = byObject.get(o);
            long card = cost(starObj, predicateIndex);
            if (card <= budget) {
                stars.add(starObj);
                ts.removeAll(starObj);
            }
        }*/
        return stars;
    }

    public static HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> readCPS(String file) {
        HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps = null;
        try {
            ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)));

            cps = (HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>>) in.readObject();
        } catch (Exception e) {
            System.err.println("Problems reading file: "+file);
            e.printStackTrace();
            System.exit(1);
        }
        return cps;
    }
}

