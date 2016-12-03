import java.io.*;
import java.util.*;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.core.Var;

class produceJoinOrderingFederationComplete {
    // DatasetId --> Position, for data related to CS-Subj
    static HashMap<Integer, Integer> datasetsIdPosSubj = new HashMap<Integer,Integer>();
    // DatasetId --> Position, for data related to CS-Obj
    static HashMap<Integer, Integer> datasetsIdPosObj = new HashMap<Integer,Integer>();

    // DatasetId --> DatasetId --> Position, for data related to CP between CS-Subjs
    static HashMap<Integer, HashMap<Integer, Integer>> datasetsIdsPosSubj = new HashMap<Integer, HashMap<Integer,Integer>>();
    // DatasetId --> DatasetId --> Position, for data related to CP between CS-Objs
    static HashMap<Integer, HashMap<Integer, Integer>> datasetsIdsPosObj = new HashMap<Integer, HashMap<Integer,Integer>>();
    // DatasetId --> DatasetId --> Position, for data related to CP between CS-Subj and CS-Obj
    static HashMap<Integer, HashMap<Integer, Integer>> datasetsIdsPosSubjObj = new HashMap<Integer, HashMap<Integer,Integer>>();

    static Vector<HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>>> cssSubj = new Vector<HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>>>();
    static Vector<HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>>> cssObj = new Vector<HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>>>();
    static Vector<HashMap<Integer, Integer>> hcsSubj = new Vector<HashMap<Integer, Integer>>();
    static Vector<HashMap<Integer, Integer>> hcsObj = new Vector<HashMap<Integer, Integer>>();
    static Vector<HashMap<Integer, Set<String>>> additionalSetsSubj = new Vector<HashMap<Integer, Set<String>>>();
    static Vector<HashMap<Integer, Set<String>>> additionalSetsObj = new Vector<HashMap<Integer, Set<String>>>();
    static Vector<HashMap<Integer, Integer>> costsSubj = new Vector<HashMap<Integer, Integer>>();
    static Vector<HashMap<Integer, Integer>> costsObj = new Vector<HashMap<Integer, Integer>>();
    static Vector<HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>>> cpsSubj = new Vector<HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>>>();
    static Vector<HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>>> cpsObj = new Vector<HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>>>();
    static Vector<HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>>> cpsSubjObj = new Vector<HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>>>();
    static Vector<String> datasets = new Vector<String>();
    static Vector<String> endpoints = new Vector<String>();
    static String folder;
    static HashMap<Integer,HashMap<String, HashSet<Integer>>> predicateIndexesSubj = new HashMap<Integer,HashMap<String, HashSet<Integer>>>();
    static HashMap<Integer,HashMap<String, HashSet<Integer>>> predicateIndexesObj = new HashMap<Integer,HashMap<String, HashSet<Integer>>>();
    static boolean distinct;
    static List<Var> projectedVariables;
    static boolean includeMultiplicity;
    static boolean original;

    public static HashMap<Integer, Integer> getCostSubj(Integer ds) {
        Integer pos = datasetsIdPosSubj.get(ds);
        if (pos == null) {
            pos = loadFilesSubj(ds);
        }
        HashMap<Integer, Integer> cost = costsSubj.get(pos);
        return cost;
    }

    public static HashMap<Integer, Integer> getCostObj(Integer ds) {
        Integer pos = datasetsIdPosObj.get(ds);
        if (pos == null) {
            pos = loadFilesObj(ds);
        }
        HashMap<Integer, Integer> cost = costsObj.get(pos);
        return cost;
    }

    public static HashMap<Integer, Set<String>> getAdditionalSetsSubj(Integer ds) {
        Integer pos = datasetsIdPosSubj.get(ds);
        if (pos == null) {
            pos = loadFilesSubj(ds);
        }
        HashMap<Integer, Set<String>> ass = additionalSetsSubj.get(pos);
        return ass;
    }

    public static HashMap<Integer, Set<String>> getAdditionalSetsObj(Integer ds) {
        Integer pos = datasetsIdPosObj.get(ds);
        if (pos == null) {
            pos = loadFilesObj(ds);
        }
        HashMap<Integer, Set<String>> ass = additionalSetsObj.get(pos);
        return ass;
    }

    public static HashMap<Integer, Integer> getHCSubj(Integer ds) {
        Integer pos = datasetsIdPosSubj.get(ds);
        if (pos == null) {
            pos = loadFilesSubj(ds);
        }
        HashMap<Integer, Integer> hc = hcsSubj.get(pos);
        return hc;
    }

    public static HashMap<Integer, Integer> getHCObj(Integer ds) {
        Integer pos = datasetsIdPosObj.get(ds);
        if (pos == null) {
            pos = loadFilesObj(ds);
        }
        HashMap<Integer, Integer> hc = hcsObj.get(pos);
        return hc;
    }

    public static HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> getCSSSubj(Integer ds) {
        Integer pos = datasetsIdPosSubj.get(ds);
        if (pos == null) {
            pos = loadFilesSubj(ds);
        }
        HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer,Integer>>>> cs = cssSubj.get(pos);
        return cs;
    }

    public static HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> getCSSObj(Integer ds) {
        Integer pos = datasetsIdPosObj.get(ds);
        if (pos == null) {
            pos = loadFilesObj(ds);
        }
        HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer,Integer>>>> cs = cssObj.get(pos);
        return cs;
    }

// HashMap<Integer, HashMap<Integer, Integer>> datasetsIdsPos
    public static HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> getCPSSubj(Integer ds1, Integer ds2) {
        boolean loaded = false;
        HashMap<Integer, Integer> ds2Pos = datasetsIdsPosSubj.get(ds1);
        Integer pos = -1;
        HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> c =null;
        if (ds2Pos != null) {
            pos = ds2Pos.get(ds2);
            if (pos != null) {
                loaded = true;
                c = cpsSubj.get(pos);
            }
        } else {
            ds2Pos = new HashMap<Integer, Integer>();
        }
        if (!loaded) {
            pos = cpsSubj.size();
            ds2Pos.put(ds2, pos);
            datasetsIdsPosSubj.put(ds1, ds2Pos);
            String datasetStr1 = datasets.get(ds1);
            String datasetStr2 = datasets.get(ds2);
            String fileCPS = folder;
            if (datasetStr1.equals(datasetStr2)) {
                fileCPS += "/"+datasetStr1+"/statistics"+datasetStr1+"_cps_reduced"; //10000CS";
            } else {
                fileCPS += "/cps_"+datasetStr1+"_"+datasetStr2+"_reduced_MIP"; //10000CS_MIP";
            }
            c = readCPS(fileCPS);
            cpsSubj.add(pos, c);
        } 
        return c;
    }

    public static HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> getCPSSubjObj(Integer ds1, Integer ds2) {
        boolean loaded = false;
        HashMap<Integer, Integer> ds2Pos = datasetsIdsPosSubjObj.get(ds1);
        Integer pos = -1;
        HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> c =null;
        if (ds2Pos != null) {
            pos = ds2Pos.get(ds2);
            if (pos != null) {
                loaded = true;
                c = cpsSubjObj.get(pos);
            }
        } else {
            ds2Pos = new HashMap<Integer, Integer>();
        }
        if (!loaded) {
            pos = cpsSubjObj.size();
            ds2Pos.put(ds2, pos);
            datasetsIdsPosSubjObj.put(ds1, ds2Pos);
            String datasetStr1 = datasets.get(ds1);
            String datasetStr2 = datasets.get(ds2);
            String fileCPS = folder;
            if (datasetStr1.equals(datasetStr2)) {
                fileCPS += "/"+datasetStr1+"/statistics"+datasetStr1+"_cps_subj_obj_reduced"; //10000CS";
            } else {
                fileCPS += "/cps_"+datasetStr1+"_"+datasetStr2+"_subj_obj_reduced_MIP"; //10000CS_MIP";
            }
            c = readCPS(fileCPS);
            cpsSubjObj.add(pos, c);
        } 
        return c;
    }

    public static HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> getCPSObj(Integer ds1, Integer ds2) {
        boolean loaded = false;
        HashMap<Integer, Integer> ds2Pos = datasetsIdsPosObj.get(ds1);
        Integer pos = -1;
        HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> c =null;
        if (ds2Pos != null) {
            pos = ds2Pos.get(ds2);
            if (pos != null) {
                loaded = true;
                c = cpsObj.get(pos);
            }
        } else {
            ds2Pos = new HashMap<Integer, Integer>();
        }
        if (!loaded) {
            pos = cpsObj.size();
            ds2Pos.put(ds2, pos);
            datasetsIdsPosObj.put(ds1, ds2Pos);
            String datasetStr1 = datasets.get(ds1);
            String datasetStr2 = datasets.get(ds2);
            String fileCPS = folder;
            if (datasetStr1.equals(datasetStr2)) {
                fileCPS += "/"+datasetStr1+"/statistics"+datasetStr1+"_cps_obj_reduced"; //10000CS";
            } else {
                fileCPS += "/cps_"+datasetStr1+"_"+datasetStr2+"_obj_reduced_MIP"; //10000CS_MIP";
            }
            c = readCPS(fileCPS);
            cpsObj.add(pos, c);
        } 
        return c;
    }

    public static int loadFilesSubj(Integer ds) {
        int pos = cssSubj.size();
        datasetsIdPosSubj.put(ds, pos);
        String datasetStr = datasets.get(ds);
        String fileCSS = folder+"/"+datasetStr+"/statistics"+datasetStr+"_css_reduced"; //10000CS";
        HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> cs = produceStarJoinOrdering.readCSS(fileCSS);
        cssSubj.add(pos, cs);
        String fileHC = folder+"/"+datasetStr+"/statistics"+datasetStr+"_hc_reduced"; //10000CS";
        HashMap<Integer, Integer> hc = produceStarJoinOrdering.readMap(fileHC);
        hcsSubj.add(pos, hc);
        String fileAdditionalSets = folder+"/"+datasetStr+"/statistics"+datasetStr+"_as_reduced"; //10000CS";
        HashMap<Integer, Set<String>> ass = produceStarJoinOrdering.readAdditionalSets(fileAdditionalSets);
        additionalSetsSubj.add(pos, ass);
        String fileCost = folder+"/"+datasetStr+"/statistics"+datasetStr+"_cost_reduced"; //10000CS";
        HashMap<Integer, Integer> cost = produceStarJoinOrdering.readMap(fileCost);
        costsSubj.add(pos, cost);
        return pos;
    }

    public static int loadFilesObj(Integer ds) {
        int pos = cssObj.size();
        datasetsIdPosObj.put(ds, pos);
        String datasetStr = datasets.get(ds);
        String fileCSS = folder+"/"+datasetStr+"/statistics"+datasetStr+"_css_obj_reduced"; //10000CS";
        HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> cs = produceStarJoinOrdering.readCSS(fileCSS);
        cssObj.add(pos, cs);
        String fileHC = folder+"/"+datasetStr+"/statistics"+datasetStr+"_hc_obj_reduced"; //10000CS";
        HashMap<Integer, Integer> hc = produceStarJoinOrdering.readMap(fileHC);
        hcsObj.add(pos, hc);
        String fileAdditionalSets = folder+"/"+datasetStr+"/statistics"+datasetStr+"_as_obj_reduced"; //10000CS";
        HashMap<Integer, Set<String>> ass = produceStarJoinOrdering.readAdditionalSets(fileAdditionalSets);
        additionalSetsObj.add(pos, ass);
        String fileCost = folder+"/"+datasetStr+"/statistics"+datasetStr+"_cost_obj_reduced"; //10000CS";
        HashMap<Integer, Integer> cost = produceStarJoinOrdering.readMap(fileCost);
        costsObj.add(pos, cost);
        return pos;
    }

    public static HashMap<String, HashSet<Integer>> getPredicateIndexSubj(Integer ds, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) {
        //System.out.println("getPredicateIndex for dataset "+ds);
        Integer pos = datasetsIdPosSubj.get(ds);
        //System.out.println("it may be in position "+pos);
        HashMap<String, HashSet<Integer>> predIndex = null;
        if (pos == null) {
            pos = loadFilesSubj(ds);
        }
        //System.out.println("its position is: "+pos+". current size of predicate indexes: "+predicateIndexes.size());
        predIndex = predicateIndexesSubj.get(pos);
        if (predIndex == null) {
            predIndex = new HashMap<String, HashSet<Integer>>();
            for (String p : predicateIndex.keySet()) {
                HashMap<Integer,HashSet<Integer>> dsCss = predicateIndex.get(p);
                HashSet<Integer> css = dsCss.get(ds);
                if (css != null) {
                    predIndex.put(p, css);
                }
            }
            predicateIndexesSubj.put(pos, predIndex);
        }
        return predIndex;
    }

    public static HashMap<String, HashSet<Integer>> getPredicateIndexObj(Integer ds, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) {
        //System.out.println("getPredicateIndex for dataset "+ds);
        Integer pos = datasetsIdPosObj.get(ds);
        //System.out.println("it may be in position "+pos);
        HashMap<String, HashSet<Integer>> predIndex = null;
        if (pos == null) {
            pos = loadFilesObj(ds);
        }
        //System.out.println("its position is: "+pos+". current size of predicate indexes: "+predicateIndexes.size());
        predIndex = predicateIndexesObj.get(pos);
        if (predIndex == null) {
            predIndex = new HashMap<String, HashSet<Integer>>();
            for (String p : predicateIndex.keySet()) {
                HashMap<Integer,HashSet<Integer>> dsCss = predicateIndex.get(p);
                HashSet<Integer> css = dsCss.get(ds);
                if (css != null) {
                    predIndex.put(p, css);
                }
            }
            predicateIndexesObj.put(pos, predIndex);
        }
        return predIndex;
    }

    public static HashMap<String, HashMap<Integer,HashSet<Integer>>> readPredicateIndexesSubj(String folder, Vector<String> datasets) {
        return readPredicateIndexes(folder, datasets, "");
    }

    public static HashMap<String, HashMap<Integer,HashSet<Integer>>> readPredicateIndexesObj(String folder, Vector<String> datasets) {
        return readPredicateIndexes(folder, datasets, "_obj");
    }

    public static HashMap<String, HashMap<Integer,HashSet<Integer>>> readPredicateIndexes(String folder, Vector<String> datasets, String suffix) {

        HashMap<String, HashMap<Integer,HashSet<Integer>>> predIndex = new HashMap<String, HashMap<Integer,HashSet<Integer>>>();
        for (int i = 0; i < datasets.size(); i++) {
            String datasetStr = datasets.get(i);
            String fileName = folder+"/"+datasetStr+"/statistics"+datasetStr+"_pi"+suffix+"_reduced"; //10000CS";
            
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
        HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexSubj = readPredicateIndexesSubj(folder, datasets); 
        HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexObj = readPredicateIndexesObj(folder, datasets); 
        //System.out.println("predicateIndexSubj size: "+predicateIndexSubj.size());
        //System.out.println("predicateIndexObj size: "+predicateIndexObj.size());
        BGP bgp = new BGP(queryFile);
        distinct = bgp.getDistinct();
        projectedVariables = bgp.getProjectedVariables();
        // sub-query --> <order, <cardinality,cost>>
        HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long, Long>>> DPTable = new HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long, Long>>>();
        Vector<HashSet<Triple>> stars = getStars(bgp, budget, predicateIndexSubj, predicateIndexObj); //css, predicateIndex, cost);
        //System.out.println("stars: "+stars);
        int i = 1;
        //HashSet<Triple> triples = new HashSet<Triple>();
        HashSet<Node> nodes = new HashSet<Node>();
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
            //System.out.println("updated bgp "+bgp+" for star : "+s+" and for nn: "+nn);
            // current star is used for the join ordering, it does not contain any metanode
            if (s.size()>0) {
                
                Vector<Tree<Pair<Integer,Triple>>> p = null;
                //System.out.println("centerIsSubject(s)? "+centerIsSubject(s));
                if (centerIsSubject(s)) {
                    p = getStarJoinOrderSubj(s, predicateIndexSubj); 
                } else {
                    p = getStarJoinOrderObj(s, predicateIndexObj); 
                }
                //System.out.println("join ordering for the star: "+p);
                if (p.size() ==0) {  // test case missing sources for a star
                    nodes.clear();
                    map.clear();
                    bgp.getBody().clear();
                    break;
                }
                long cost = 0L;
                if (centerIsSubject(s)) {
                    cost = cssCostTreeSubj(p, predicateIndexSubj);
                } else {
                    cost = cssCostTreeObj(p, predicateIndexObj);
                }
                map.put(nn, p);
                i++;
                HashSet<Node> ns = new HashSet<Node>();
                ns.add(nn);
                nodes.add(nn);
                DPTable.put(ns, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(p, new Pair<Long, Long>(cost,0L))); 
            }
        }
        
        //System.out.println("map: "+map);
        //System.out.println("DPTable before add.. :"+DPTable);
        addRemainingTriples(DPTable, bgp, predicateIndexSubj, predicateIndexObj, nodes, map); //, css, cps, predicateIndex, triples, cost, map, hc, additionalSets);
        //System.out.println("DPTable after add.. :"+DPTable);
        // may have to consider already intermediate results in the CP estimation for the cost
        HashMap<HashSet<Node>, Double> selectivity = new HashMap<HashSet<Node>, Double>();
        //HashMap<HashSet<Node>, Vector<Tree<Pair<Integer,Triple>>>> selectivity = new HashMap<HashSet<Node>, Double>();
        estimateSelectivityCP(DPTable, predicateIndexSubj, predicateIndexObj, nodes, bgp, map, selectivity); //css, cps, predicateIndex, triples, hc, additionalSets, cost);
        //System.out.println("DPTable after CP estimation.. :"+DPTable);
        //System.out.println("selectivity :"+selectivity);
        //System.out.println("nodes :"+nodes);
        computeJoinOrderingDP(DPTable, nodes, selectivity);
        Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long, Long>> res = DPTable.get(nodes);
        if (res != null && res.getFirst().size() > 0) {
            String plan = toString(res.getFirst());
            System.out.println("Plan: "+plan);
            System.out.println("Cardinality: "+res.getSecond().getFirst());
            System.out.println("Cost: "+res.getSecond().getSecond());
            write(plan, fileName);
        } else {
            System.out.println("No plan was found");
        }
        //System.out.println("DPTable at the end: "+DPTable);
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

    public static Node getCenter(Set<Triple> ts) {
        Node s = null;
        Node o = null;
        Node c = null;
        for (Triple t : ts) {
            if (s == null || o == null) {
                s = t.getSubject();
                o = t.getObject();
                c = s;
            } else {
                if (s.equals(t.getSubject())) {
                    c = s;
                } else if (o.equals(t.getObject())) {
                    c = o;
                }
                break;
            }
        }
        return c;
    }

    public static Node getCenter(Tree<Pair<Integer, Triple>> tree) {
        Set<Pair<Integer,Triple>> es = tree.getElements();
        Node s = null;
        Node o = null;
        Node c = null;
        for (Pair<Integer,Triple> pair : es) {
            Triple t  = pair.getSecond();
            if (s == null || o == null) {
                s = t.getSubject();
                o = t.getObject();
                c = s;
            } else {
                if (s.equals(t.getSubject())) {
                    c = s;
                } else if (o.equals(t.getObject())) {
                    c = o;
                }
                break;
            }
        }
        return c;
    }

    public static boolean centerIsSubject(Set<Triple> ts) {
        Node c = getCenter(ts);
        return c.equals(ts.iterator().next().getSubject());
    }

    public static boolean centerIsSubject(Tree<Pair<Integer, Triple>> tree) {
        Node c = getCenter(tree);
        return c.equals(tree.getOneElement().getSecond().getSubject());
    }

    public static boolean centerIsObject(Set<Triple> ts) {
        if (ts.size() == 1) {
            return true;
        }
        Node c = getCenter(ts);
        return c.equals(ts.iterator().next().getObject());
    }

    public static boolean centerIsObject(Tree<Pair<Integer, Triple>> tree) {
        if (tree instanceof Leaf<?>) {
            return true;
        }
        Node c = getCenter(tree);
        return c.equals(tree.getOneElement().getSecond().getObject());
    }

    public static HashSet<Triple> renameBack(Set<Triple> star, HashMap<Node, Vector<Tree<Pair<Integer,Triple>>>> map, HashMap<Triple,Triple> renamed) {

        HashMap<Node, Node> hm = new HashMap<Node,Node>();
        for (Node n : map.keySet()) {
            Tree<Pair<Integer,Triple>> tree = map.get(n).get(0);
            Node c = getCenter(tree);
            hm.put(n, c);  // meta-node -> node
        }
        HashSet<Triple> newStar = new HashSet<Triple>();
        for (Triple t : star) {
            Node s = t.getSubject();
            if (hm.containsKey(s)) {
                s = hm.get(s);
            }
            Node p = t.getPredicate();
            Node o = t.getObject();
            if (hm.containsKey(o)) {
                o = hm.get(o);
            }
            Triple newTriple = new Triple(s, p, o);
            newStar.add(newTriple);
            renamed.put(t, newTriple); // with meta-nodes -> without meta-nodes
        }
        return newStar;
    }

    public static HashSet<Triple> rename(Set<Triple> star, HashMap<Node, Vector<Tree<Pair<Integer,Triple>>>> map, HashMap<Triple,Triple> renamed) {

        HashMap<Node, Node> invertMap = new HashMap<Node,Node>();
        for (Node n : map.keySet()) {
            Tree<Pair<Integer,Triple>> tree = map.get(n).get(0);
            Node c = getCenter(tree);
            invertMap.put(c, n);
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
    public static void computeJoinOrderingDP(HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable, HashSet<Node> nodes, HashMap<HashSet<Node>, Double> selectivity) {

        for (int i = 3; i <= nodes.size(); i++) {
            computeJoinOrderingDPAux(DPTable, nodes, i, selectivity);
        }
    }

    public static void getAllSubsets(HashSet<Node> rn, int j, Vector<HashSet<Node>> subsets) {
        if (j == 0) {
            subsets.add(new HashSet<Node>());
            return;
        } else if (rn.size() == 0) {
            return;
        }
        HashSet<Node> aux = new HashSet<Node>(rn);
        Node elem = rn.iterator().next();
        aux.remove(elem);
        Vector<HashSet<Node>> subsetsAux = new Vector<HashSet<Node>>();
        getAllSubsets(aux, j-1, subsetsAux);
        getAllSubsets(aux, j, subsets);
        for (HashSet<Node> ss : subsetsAux) {
            ss.add(elem);
            subsets.add(ss);
        }
    }

    public static void computeJoinOrderingDPAux(HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable, HashSet<Node> nodes, int i, HashMap<HashSet<Node>, Double> selectivity) {
        // currently cost depends only on intermediate results, then for the same two sub-queries their order do not matter
        // start considering sub-queries from half the size of the current sub-queries (i)
        //System.out.println("computeJoinOrderingDPAux, i: "+i);
        int k = (int) Math.round(Math.ceil(i/2.0));
        HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTableAux = new HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>>();
        for (HashSet<Node> ns : DPTable.keySet()) {
            Vector<Tree<Pair<Integer,Triple>>> aux = DPTable.get(ns).getFirst();
            if (ns.size() < k || aux.size()==0) {
                continue;
            }
            HashSet<Node> rn = new HashSet<Node>(nodes);
            rn.removeAll(ns);
            Vector<HashSet<Node>> subsets = new Vector<HashSet<Node>>();
            getAllSubsets(rn, i-ns.size(), subsets);
            for (HashSet<Node> ss : subsets) {
                //HashSet<Triple> intersection = new HashSet<Triple>(ns);
                //intersection.retainAll(ss);
                if (ss.size() == 0) {
                    continue;
                }
                for (HashSet<Node> sss : DPTable.keySet()) {
                    if (!sss.containsAll(ss)) {
                    //if (!DPTable.containsKey(ss)|| DPTable.get(ss).getFirst().size()==0) { // || (intersection.size()>0)) {
                        continue;
                    }
                    Long card = getCard(DPTable, ns, sss, selectivity);
                    Vector<Tree<Pair<Integer,Triple>>> vTreeNS = DPTable.get(ns).getFirst();
                    Vector<Tree<Pair<Integer,Triple>>> vTreeSS = DPTable.get(sss).getFirst();
                    //System.out.println("(B) vTreeNS: "+vTreeNS);
                    //System.out.println("(B) vTreeSS: "+vTreeSS);
                    vTreeSS = removeRedundancy(vTreeNS, vTreeSS);
                    //System.out.println("ns: "+ns);
                    //System.out.println("ss: "+ss);
                    //System.out.println("sss: "+sss);
                    //System.out.println("(A) vTreeNS: "+vTreeNS);
                    //System.out.println("(A) vTreeSS: "+vTreeSS+"DPTable.get(sss).getFirst(): "+DPTable.get(sss).getFirst());
                    Long cost = card + DPTable.get(ns).getSecond().getFirst()+DPTable.get(sss).getSecond().getFirst();
                    HashSet<Node> newEntry = new HashSet<Node>(ns);
                    newEntry.addAll(sss);
                    //System.out.println("newEntry: "+newEntry+". cost: "+cost);
                    Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>> pair = DPTableAux.get(newEntry);
                    if ((pair == null) || pair.getSecond().getSecond()>cost) {
                        Vector<Tree<Pair<Integer,Triple>>> order = makeTree(vTreeNS,vTreeSS, null);
                        //System.out.println("merging : "+ns+" and "+sss+". card: "+card+". cost: "+cost+". pair: "+pair+". newEntry: "+newEntry+". order: "+order);
                        DPTableAux.put(newEntry, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(order, new Pair<Long,Long>(card, cost)));
                    }
                }
            }
        }
        DPTable.putAll(DPTableAux);
    }

    public static Vector<Tree<Pair<Integer,Triple>>> removeRedundancy(Vector<Tree<Pair<Integer,Triple>>> vTreeNS, Vector<Tree<Pair<Integer,Triple>>> vTreeSS) {
        Vector<Tree<Pair<Integer,Triple>>> res = new Vector<Tree<Pair<Integer,Triple>>>();

        for (int i = 0; i < vTreeSS.size();i++) {
            //System.out.println("before: "+vTreeSS);
            Tree<Pair<Integer,Triple>> aux = vTreeSS.get(i);

            for (int k = 0; k < vTreeNS.size(); k++) {
                Tree<Pair<Integer,Triple>> tmp = vTreeNS.get(k);
                Set<Pair<Integer, Triple>> elems = tmp.getElements();

                for (Pair<Integer, Triple> e : elems) {
                    //System.out.println("removing e: "+e);
                    aux = remove(aux, e);
                    if (aux== null) {
                        break;
                    }
                }
            }
            if (aux != null) {
                res.add(aux);
            }
            //System.out.println("after: "+vTreeSS);
        }
        return res;
    }

    public static Tree<Pair<Integer,Triple>> remove(Tree<Pair<Integer,Triple>> tree, Pair<Integer,Triple> e) {

        //System.out.println("tree: "+tree+". e: "+e);
        if (tree instanceof Branch<?>) {
            Branch<Pair<Integer,Triple>> b = (Branch<Pair<Integer,Triple>>) tree;
            Tree<Pair<Integer,Triple>> lTree = b.getLeft();
            Tree<Pair<Integer,Triple>> rTree = b.getRight();
            if (lTree instanceof Leaf<?> && lTree.getOneElement().equals(e)) {
                return rTree;
            } else if (rTree instanceof Leaf<?> && rTree.getOneElement().equals(e)) {
                return lTree;
            }
            if (lTree instanceof Branch<?>) {
                lTree = remove(lTree, e);
            }
            if (rTree instanceof Branch<?>) {
                rTree = remove(rTree, e);
            }
            tree = new Branch(lTree, rTree);
        } else if (tree instanceof Leaf<?> && tree.getOneElement().equals(e)) {
            return null;
        }
        return tree;
    }

    // to estimate the cardinality between two sub-queries, consider the information already in DPTable about the joins
    // between two triple patterns, one from each sub-query to compute the selectivity of the joins and the cardinality of the new join
    public static long getCard(HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable, HashSet<Node> sq1, HashSet<Node> sq2, HashMap<HashSet<Node>, Double> selectivity) {
        long cardSQ1 = DPTable.get(sq1).getSecond().getFirst();
        long cardSQ2 = DPTable.get(sq2).getSecond().getFirst();
        double accSel = 1;
        for (Node n : sq1) {
            for (Node m : sq2) {
                HashSet<Node> join = new HashSet<Node>();
                join.add(n);
                join.add(m);
                Double sel = selectivity.get(join);
                if (sel == null) {
                    continue;
                }
                accSel = accSel * sel;
           }
        }
        //System.out.println(cardSQ1 * cardSQ2);
        //System.out.println(cardSQ1 * cardSQ2 *accSel);
        long card = Math.round(Math.ceil(cardSQ1 * cardSQ2 * accSel));
        return card;
    }

    public static void addRemainingTriples(HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable, BGP bgp, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexSubj, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexObj, HashSet<Node> nodes, HashMap<Node, Vector<Tree<Pair<Integer,Triple>>>> map) {

        List<Triple> ts = bgp.getBody();
        //System.out.println("ts: "+ts);
        //System.out.println("map: "+map);
        HashSet<Node> toAdd = new HashSet<Node>();
        for (Triple t : ts) {
            Node s = t.getSubject();
            Node p = t.getPredicate();
            Node o = t.getObject();
            if (!nodes.contains(s)) {
                toAdd.add(s);
                Set<Triple> set = new HashSet<Triple>();
                set.add(t);
                long c = costSubj(set, predicateIndexSubj);
                HashSet<Node> newEntry = new HashSet<Node>();
                newEntry.add(s);
                Vector<Tree<Pair<Integer,Triple>>> vector = new Vector<Tree<Pair<Integer,Triple>>>();
                Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>> data = DPTable.get(newEntry);
                long prevC = Long.MAX_VALUE;
                if (data != null) {
                    prevC = data.getSecond().getFirst();
                }
                // including the minimum cost if inferior to previous value
                if (c < prevC) { // if not there, cost is zero
                    DPTable.put(newEntry, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(vector, new Pair<Long, Long>(c,0L))); 
                }
            }
            if (!nodes.contains(o)) {
                toAdd.add(o);
                Set<Triple> set = new HashSet<Triple>();
                set.add(t);
                long c = costObj(set, predicateIndexObj);
                HashSet<Node> newEntry = new HashSet<Node>();
                newEntry.add(o);
                Vector<Tree<Pair<Integer,Triple>>> vector = new Vector<Tree<Pair<Integer,Triple>>>();
                Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>> data = DPTable.get(newEntry);
                long prevC = Long.MAX_VALUE;
                if (data != null) {
                    prevC = data.getSecond().getFirst();
                }
                // including the minimum cost if inferior to previous value
                if (c < prevC) {
                    DPTable.put(newEntry, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(vector, new Pair<Long, Long>(c,0L))); 
                }
            }
        }
        nodes.addAll(toAdd);
    }

    public static boolean existsCPConnectionSubj(Set<Triple> sq1, Set<Triple> sq2) {

        return existsCSConnectionSubj(sq1) && existsCSConnectionSubj(sq2) && (existsCPConnectionAux(sq1, sq2) || existsCPConnectionAux(sq2, sq1));
    }

    public static boolean existsCPConnectionSubjObj(Set<Triple> sq1, Set<Triple> sq2) {
        //System.out.println("existsCSConnectionSubj(sq1): "+existsCSConnectionSubj(sq1));
        //System.out.println("existsCSConnectionObj(sq2): "+existsCSConnectionObj(sq2));
        //System.out.println("existsCPConnectionAuxSO(sq1, sq2): "+existsCPConnectionAuxSO(sq1, sq2));
        //System.out.println("existsCPConnectionAuxSO(sq2, sq1): "+existsCPConnectionAuxSO(sq2, sq1));
        return existsCSConnectionSubj(sq1) && existsCSConnectionObj(sq2) && existsCPConnectionAuxSO(sq1, sq2);
    }

    public static boolean existsCPConnectionObj(Set<Triple> sq1, Set<Triple> sq2) {

        return existsCSConnectionObj(sq1) && existsCSConnectionObj(sq2) && (existsCPConnectionAux(sq1, sq2) || existsCPConnectionAux(sq2, sq1));
    }

    public static boolean existsCPConnectionAux(Set<Triple> sq1, Set<Triple> sq2) {

        boolean e = false;
        Node c = sq2.iterator().next().getSubject();
        for (Triple t : sq1) {
            Node oTmp = t.getObject();
            e = e || (oTmp.equals(c));
        }
        return e;
    }

    public static boolean existsCPConnectionAuxSO(Set<Triple> sq1, Set<Triple> sq2) {

        boolean e = false;
        Node c = sq2.iterator().next().getObject();
        for (Triple t : sq1) {
            Node oTmp = t.getObject();
            e = e || (oTmp.equals(c));
        }
        return e;
    }

    public static boolean existsCSConnectionSubj(Set<Triple> sq) {
        boolean e = true;
        Node s = null;
        for (Triple t : sq) {
            Node sTmp = t.getSubject();
            e = e && ((s == null) || s.equals(sTmp));
            s = sTmp;
        }
        return e;
    }

    public static boolean existsCSConnectionObj(Set<Triple> sq) {
        boolean e = true;
        Node o = null;
        for (Triple t : sq) {
            Node oTmp = t.getObject();
            e = e && ((o == null) || o.equals(oTmp));
            o = oTmp;
        }
        return e;
    }

    public static void addSelectivity(HashSet<Node> keyS, HashSet<Node> keyO, HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable, HashMap<HashSet<Node>, Double> selectivity) {

        HashSet<Node> set = new HashSet<Node>();
        set.addAll(keyS);
        Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long, Long>> val = DPTable.get(set);
        long cardLeft = val.getSecond().getFirst();
        set.clear();
        set.addAll(keyO);
        val = DPTable.get(set);
        long cardRight = val.getSecond().getFirst();
        set.addAll(keyS);
        val = DPTable.get(set);
        long cardJoin = val.getSecond().getFirst();
        double sel = ((double) cardJoin) / (cardLeft * cardRight);

        Double s = selectivity.get(set);
        if (s == null || s > sel) {
            selectivity.put(set, sel);
        }
    }

    public static void estimateSelectivityCP(HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexSubj, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexObj, HashSet<Node> nodes, BGP bgp, HashMap<Node, Vector<Tree<Pair<Integer,Triple>>>> map, HashMap<HashSet<Node>, Double> selectivity) {

        List<Triple> ts = bgp.getBody();
        HashMap<Triple, Triple> renamed = new HashMap<Triple, Triple>();
        HashSet<Triple> newTs = renameBack(new HashSet<Triple>(ts), map, renamed);
        Vector<HashSet<Node>> toRemove = new Vector<HashSet<Node>>();
        for (Triple t : ts) {
            Node s = t.getSubject();
            Node o = t.getObject();
            HashSet<Node> keyS = new HashSet<Node>();
            keyS.add(s);
            Vector<Tree<Pair<Integer,Triple>>> valueS = DPTable.get(keyS).getFirst();
            HashSet<Node> keyO = new HashSet<Node>();
            keyO.add(o);
            Vector<Tree<Pair<Integer,Triple>>> valueO = DPTable.get(keyO).getFirst();
            Set<Triple> ts1 = new HashSet<Triple>();
            if (valueS.size()>0) {
                ts1.addAll(obtainTriples(valueS.get(0)));
            }
            Set<Triple> ts2 = new HashSet<Triple>();
            if (valueO.size()>0) {
                ts2.addAll(obtainTriples(valueO.get(0)));
            }
            Set<Triple> ts3 = new HashSet<Triple>();
            Set<Triple> ts4 = new HashSet<Triple>();
            Set<Triple> ts5 = new HashSet<Triple>();
            Set<Triple> ts6 = new HashSet<Triple>();
            if (ts1.size()>0 && centerIsSubject(ts1)) {
                ts3.addAll(ts1);
            }
            if (ts1.size()>0 && centerIsObject(ts1)) {
                ts4.addAll(ts1);
            }
            if (ts2.size()>0 && centerIsSubject(ts2)) {
                ts5.addAll(ts2);
            }
            if (ts2.size()>0 && centerIsObject(ts2)) {
                ts6.addAll(ts2);
            }
            for (Triple t1 : ts) { // t1 has meta-nodes
                Triple t2 = renamed.get(t1); // t2 is t1 without the meta-nodes
                if (t1.getSubject().equals(s)) {
                    ts3.add(t2);
                }
                if (t1.getObject().equals(s)) {
                    ts4.add(t2);
                }
                if (t1.getSubject().equals(o)) {
                    ts5.add(t2);
                }
                if (t1.getObject().equals(o)) {
                    ts6.add(t2);
                }
            }
            boolean added = false;
            if (ts3.size() > 0 && ts5.size() > 0 && existsCPConnectionSubj(ts3, ts5)) {
                Vector<Tree<Pair<Integer,Triple>>> p = getStarJoinOrderSubj(ts3, predicateIndexSubj);
                Vector<Tree<Pair<Integer,Triple>>> q = getStarJoinOrderSubj(ts5, predicateIndexSubj);
                if (p.size()>0 && q.size()>0) {
                    added = added || addCheapestCPSubj(p, q, keyS, keyO, predicateIndexSubj, DPTable);
                }
            }
            if (ts4.size() > 0 && ts6.size() > 0 && existsCPConnectionObj(ts4, ts6)) {
                Vector<Tree<Pair<Integer,Triple>>> p = getStarJoinOrderObj(ts4, predicateIndexObj);
                Vector<Tree<Pair<Integer,Triple>>> q = getStarJoinOrderObj(ts6, predicateIndexObj);
                if (p.size()>0 && q.size()>0) {
                    added = added || addCheapestCPObj(p, q, keyS, keyO, predicateIndexObj, DPTable);
                }
            }
            if (ts3.size() > 0 && ts6.size() > 0 && existsCPConnectionSubjObj(ts3, ts6)) {
                HashSet<Triple> aux = new HashSet<Triple>(ts6);
                aux.removeAll(ts3); // Linking triples are in both characteristic sets, they should appear only in one
                Vector<Tree<Pair<Integer,Triple>>> p = getStarJoinOrderSubj(ts3, predicateIndexSubj);
                Vector<Tree<Pair<Integer,Triple>>> q = new Vector<Tree<Pair<Integer,Triple>>>();
                if (aux.size()>0) {
                    q = getStarJoinOrderObj(aux, predicateIndexObj);
                }
                if (p.size()>0 && q.size()>0) {
                    added = added || addCheapestCPSubjObj(p, q, keyS, keyO, predicateIndexSubj, predicateIndexObj, DPTable);
                }
            }
            if (added) {
                toRemove.add(keyS);
                toRemove.add(keyO);
                addSelectivity(keyS, keyO, DPTable, selectivity);
            }
        }
        for (HashSet<Node> s : toRemove) {
            DPTable.remove(s);
        }
    }

    // precondition: triples in sq1 share the same subject
    public static void addCSSubj(HashSet<Triple> ts1, HashSet<Triple> sq1, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex, HashMap<HashSet<Triple>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable) {

        Vector<Tree<Pair<Integer,Triple>>> p = getStarJoinOrderSubj(ts1, predicateIndex); 
        if (p.size() == 0) {
            return;
        }
        HashSet<Triple> newEntry = new HashSet<Triple>(sq1);
        long c = cssCostTreeSubj(p, predicateIndex); 
        Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>> data = DPTable.get(newEntry);
        if (data == null || data.getSecond().getFirst()>c) {
            DPTable.put(newEntry, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(p, new Pair<Long, Long>(c,0L)));
        }
    }

    // precondition: triples in sq1 share the same object
    public static void addCSObj(HashSet<Triple> ts1, HashSet<Triple> sq1, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex, HashMap<HashSet<Triple>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable) {

        Vector<Tree<Pair<Integer,Triple>>> p = getStarJoinOrderObj(ts1, predicateIndex); 
        if (p.size() == 0) {
            return;
        }
        HashSet<Triple> newEntry = new HashSet<Triple>(sq1);
        long c = cssCostTreeObj(p, predicateIndex); 
        Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>> data = DPTable.get(newEntry);
        if (data == null || data.getSecond().getFirst()>c) {
            DPTable.put(newEntry, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(p, new Pair<Long, Long>(c,0L)));
        }
    }

    public static boolean addCheapestCPSubj(Vector<Tree<Pair<Integer,Triple>>> ts1, Vector<Tree<Pair<Integer,Triple>>> ts2, HashSet<Node> sq1, HashSet<Node> sq2, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex, HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable) {

        Pair<Long,Vector<Tree<Pair<Integer,Triple>>>> costTree = getCostCPTreeSubj(ts1, ts2, predicateIndex); 
        long cost = costTree.getFirst();
        Vector<Tree<Pair<Integer,Triple>>> tree = costTree.getSecond();

        //System.out.println("(S) cost of "+tree+"("+ts1+" and "+ts2+"): "+cost);        
        //System.out.println("cost of "+sq2+" and "+sq1+": "+cost21);
        HashSet<Node> set = new HashSet<Node>(sq1);
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
            return true;
        }
        return false;
    }

    public static boolean addCheapestCPSubjObj(Vector<Tree<Pair<Integer,Triple>>> ts1, Vector<Tree<Pair<Integer,Triple>>> ts2, HashSet<Node> sq1, HashSet<Node> sq2, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexSubj, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexObj, HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable) {

        Pair<Long,Vector<Tree<Pair<Integer,Triple>>>> costTree = getCostCPTreeSubjObj(ts1, ts2, predicateIndexSubj, predicateIndexObj); 
        long cost = costTree.getFirst();
        Vector<Tree<Pair<Integer,Triple>>> tree = costTree.getSecond();
        
        //System.out.println("(SO) cost of "+tree+"("+ts1+" and "+ts2+"): "+cost);        
        HashSet<Node> set = new HashSet<Node>(sq1);
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
            return true;
        }
        return false;
    }

    public static boolean addCheapestCPObj(Vector<Tree<Pair<Integer,Triple>>> ts1, Vector<Tree<Pair<Integer,Triple>>> ts2, HashSet<Node> sq1, HashSet<Node> sq2, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex, HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable) {

        Pair<Long,Vector<Tree<Pair<Integer,Triple>>>> costTree = getCostCPTreeObj(ts1, ts2, predicateIndex); 
        long cost = costTree.getFirst();
        Vector<Tree<Pair<Integer,Triple>>> tree = costTree.getSecond();
        //System.out.println("(O) cost of "+tree+"("+ts1+" and "+ts2+"): "+cost);        
        //System.out.println("cost of "+sq2+" and "+sq1+": "+cost21);
        HashSet<Node> set = new HashSet<Node>(sq1);
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
            return true;
        }
        return false;
    }

    public static Vector<Tree<Pair<Integer,Triple>>> makeTree(Vector<Tree<Pair<Integer,Triple>>> treeL, Vector<Tree<Pair<Integer,Triple>>> treeR, HashMap<Integer, HashSet<Integer>> sources) {
        Vector<Tree<Pair<Integer,Triple>>> res = new Vector<Tree<Pair<Integer,Triple>>>();
        if (sources == null) {
            if (treeL.size() == 0) {
                res.addAll(treeR);
            } else if (treeR.size() == 0) {
                res.addAll(treeL);
            }
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

    public static Pair<Long, Vector<Tree<Pair<Integer,Triple>>>> getCostCPTreeSubj(Vector<Tree<Pair<Integer,Triple>>> vLT, Vector<Tree<Pair<Integer,Triple>>> vRT, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) {
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
            Long costLT = computeCost(getCSSSubj(sourceLT), relevantCssLT, predicatesLT, mapL, true);
            for (Tree<Pair<Integer,Triple>> rightTree : vRT) {
                Integer sourceRT = sameSource(rightTree.getElements());
                if (sourceRT == null) {
                    continue;
                }         
                HashMap<String, Triple> mapR = new HashMap<String, Triple>();
                Set<String> predicatesRT = obtainBoundPredicates(rightTree, mapR);
                HashSet<Integer> relevantCssRT = computeRelevantCS(predicatesRT, sourceRT, predicateIndex);
                Long costRT = computeCost(getCSSSubj(sourceRT), relevantCssRT, predicatesRT, mapR, true);

                Tree<Pair<Integer,Triple>> nT = null;
                if (costLT <= costRT) {
                    nT = new Branch<Pair<Integer,Triple>>(leftTree, rightTree);
                } else {
                    nT = new Branch<Pair<Integer,Triple>>(rightTree, leftTree);
                }
                long tmpCost = getCostCPSubj(obtainTriples(leftTree), sourceLT, obtainTriples(rightTree), sourceRT, predicateIndex);
                if (tmpCost > 0) { 
                    res.add(nT);
                    cost += tmpCost;
                }
            }
        }
        return new Pair<Long, Vector<Tree<Pair<Integer,Triple>>>>(cost, res);
    }

    public static Pair<Long, Vector<Tree<Pair<Integer,Triple>>>> getCostCPTreeSubjObj(Vector<Tree<Pair<Integer,Triple>>> vLT, Vector<Tree<Pair<Integer,Triple>>> vRT, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexSubj, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexObj) {
        Vector<Tree<Pair<Integer,Triple>>> res = new Vector<Tree<Pair<Integer,Triple>>>();
        long cost = 0L;
        for (Tree<Pair<Integer,Triple>> leftTree : vLT) {
            Integer sourceLT = sameSource(leftTree.getElements());
            if (sourceLT == null) {
                continue;
            }
            HashMap<String, Triple> mapL = new HashMap<String, Triple>();
            Set<String> predicatesLT = obtainBoundPredicates(leftTree, mapL);
            HashSet<Integer> relevantCssLT = computeRelevantCS(predicatesLT, sourceLT, predicateIndexSubj);
            Long costLT = computeCost(getCSSSubj(sourceLT), relevantCssLT, predicatesLT, mapL, true);
            for (Tree<Pair<Integer,Triple>> rightTree : vRT) {
                Integer sourceRT = sameSource(rightTree.getElements());
                if (sourceRT == null) {
                    continue;
                }         
                HashMap<String, Triple> mapR = new HashMap<String, Triple>();
                Set<String> predicatesRT = obtainBoundPredicates(rightTree, mapR);
                HashSet<Integer> relevantCssRT = computeRelevantCS(predicatesRT, sourceRT, predicateIndexObj);
                Long costRT = computeCost(getCSSObj(sourceRT), relevantCssRT, predicatesRT, mapR, false);

                Tree<Pair<Integer,Triple>> nT = null;
                if (costLT <= costRT) {
                    nT = new Branch<Pair<Integer,Triple>>(leftTree, rightTree);
                } else {
                    nT = new Branch<Pair<Integer,Triple>>(rightTree, leftTree);
                }
                long tmpCost = getCostCPSubjObj(obtainTriples(leftTree), sourceLT, obtainTriples(rightTree), sourceRT, predicateIndexSubj, predicateIndexObj);
                if (tmpCost > 0) { 
                    res.add(nT);
                    cost += tmpCost;
                }
            }
        }
        return new Pair<Long, Vector<Tree<Pair<Integer,Triple>>>>(cost, res);
    }

    public static Pair<Long, Vector<Tree<Pair<Integer,Triple>>>> getCostCPTreeObj(Vector<Tree<Pair<Integer,Triple>>> vLT, Vector<Tree<Pair<Integer,Triple>>> vRT, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) {
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
            Long costLT = computeCost(getCSSObj(sourceLT), relevantCssLT, predicatesLT, mapL, false);
            for (Tree<Pair<Integer,Triple>> rightTree : vRT) {
                Integer sourceRT = sameSource(rightTree.getElements());
                if (sourceRT == null) {
                    continue;
                }         
                HashMap<String, Triple> mapR = new HashMap<String, Triple>();
                Set<String> predicatesRT = obtainBoundPredicates(rightTree, mapR);
                HashSet<Integer> relevantCssRT = computeRelevantCS(predicatesRT, sourceRT, predicateIndex);
                Long costRT = computeCost(getCSSObj(sourceRT), relevantCssRT, predicatesRT, mapR, false);

                Tree<Pair<Integer,Triple>> nT = null;
                if (costLT <= costRT) {
                    nT = new Branch<Pair<Integer,Triple>>(leftTree, rightTree);
                } else {
                    nT = new Branch<Pair<Integer,Triple>>(rightTree, leftTree);
                }
                long tmpCost = getCostCPObj(obtainTriples(leftTree), sourceLT, obtainTriples(rightTree), sourceRT, predicateIndex);
                if (tmpCost > 0) { 
                    res.add(nT);
                    cost += tmpCost;
                }
            }
        }
        return new Pair<Long, Vector<Tree<Pair<Integer,Triple>>>>(cost, res);
    }

    public static Long getMultiplicitySubj(Integer m, String p,  Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs, HashSet<String> ps1, HashMap<String, Triple> map1, Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs2, HashSet<String> ps2, HashMap<String, Triple> map2) {

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

    public static Long getMultiplicitySubjObj(Integer m, String p,  Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs, HashSet<String> ps1, HashMap<String, Triple> map1, Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs2, HashSet<String> ps2, HashMap<String, Triple> map2) {

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
            if (original || !p2.equals(p)) {
                Triple t = map2.get(p2);
                Node s = t.getSubject();
                Node o = t.getObject();
                boolean c = includeMultiplicity && projectedVariables.contains(o) && (!distinct || projectedVariables.contains(s));
                if (c) {
                    Integer p_m = cPs2.getSecond().get(p2).getFirst();
                    sel = sel*(((double)p_m)/count);
                    //System.out.println(p+": "+css.get(cs).getSecond().get(p)); 
                }
                if (!s.isVariable()) {
                    Integer p_m = cPs2.getSecond().get(p2).getFirst();
                    Integer p_d = cPs2.getSecond().get(p2).getSecond();
                    //System.out.println("p_m: "+p_m+". p_d: "+p_d);
                    double tmp = 1.0/ p_d;
                    if (tmp < selTmp) {
                        selTmp = tmp;
                    }
                }
            }
        }
        sel = sel * selTmp;  // CONSIDER CONSTANTS second star
        //System.out.println("m: "+m+". sel: "+sel);
        //System.out.println("sel: "+sel);
        return Math.round(Math.ceil(m*sel));
    }

    public static Long getMultiplicityObj(Integer m, String p,  Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs, HashSet<String> ps1, HashMap<String, Triple> map1, Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs2, HashSet<String> ps2, HashMap<String, Triple> map2) {

        double sel = 1.0;
        double selTmp = 1.0;
        Integer count = cPs.getFirst();
        //System.out.println("p: "+p);
        for (String p1 : ps1) {
                //System.out.println("p1: "+p1);
                Triple t = map1.get(p1);
                Node s = t.getSubject();
                Node o = t.getObject();
                boolean c = includeMultiplicity && projectedVariables.contains(o) && (!distinct || projectedVariables.contains(s));
                if (c) {
                    Integer p_m = cPs.getSecond().get(p1).getFirst();
                    sel = sel*(((double)p_m)/count);
                    //System.out.println(p+": "+css.get(cs).getSecond().get(p)); 
                }
                if (!s.isVariable()) {
                    Integer p_m = cPs.getSecond().get(p1).getFirst();
                    Integer p_d = cPs.getSecond().get(p1).getSecond();
                    //System.out.println("p_m: "+p_m+". p_d: "+p_d);
                    double tmp = 1.0/ p_d;
                    if (tmp < selTmp) {
                        selTmp = tmp;
                    }
                }
        }
        sel = sel * selTmp; // CONSIDER CONSTANTS first star
        selTmp = 1.0;
        count = cPs2.getFirst();
        for (String p2 : ps2) {
            //System.out.println("p2: "+p2);
            if (original || !p2.equals(p)) {
                Triple t = map2.get(p2);
                Node s = t.getSubject();
                Node o = t.getObject();
                boolean c = includeMultiplicity && projectedVariables.contains(o) && (!distinct || projectedVariables.contains(s));
                if (c) {
                    Integer p_m = cPs2.getSecond().get(p2).getFirst();
                    sel = sel*(((double)p_m)/count);
                    //System.out.println(p+": "+css.get(cs).getSecond().get(p)); 
                }
                if (!s.isVariable()) {
                    Integer p_m = cPs2.getSecond().get(p2).getFirst();
                    Integer p_d = cPs2.getSecond().get(p2).getSecond();
                    //System.out.println("p_m: "+p_m+". p_d: "+p_d);
                    double tmp = 1.0/ p_d;
                    if (tmp < selTmp) {
                        selTmp = tmp;
                    }
                }
            }
        }
        sel = sel * selTmp;  // CONSIDER CONSTANTS second star
        //System.out.println("m: "+m+". sel: "+sel);
        //System.out.println("sel: "+sel);
        return Math.round(Math.ceil(m*sel));
    }

    public static long getCostCPSubj(Set<Triple> sq1, Integer ds1, Set<Triple> sq2, Integer ds2, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) { 

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
            Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs = getCSSSubj(ds1).get(cs1);

            for (Integer cs2 : css2) {
                Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs2 = getCSSSubj(ds2).get(cs2);

                HashMap<Integer, HashMap<String, Integer>> css2Ps = getCPSSubj(ds1, ds2).get(cs1);
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
                                Long m1 = getMultiplicitySubj(links12.get(p), p, cPs, ps1, map1, cPs2, ps2, map2);
                                Long m2 = mult12.get(p);
                                if (m2 == null) {
                                    m2 = 0L;
                                }
                                mult12.put(p, m1+m2);
                            }
                        }
                    }
                }
                css2Ps = getCPSSubj(ds2, ds1).get(cs2);
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
                                Long m1 = getMultiplicitySubj(links21.get(p), p, cPs2, ps2, map2, cPs, ps1, map1);
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

    public static long getCostCPSubjObj(Set<Triple> sq1, Integer ds1, Set<Triple> sq2, Integer ds2, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexSubj, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexObj) { 

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

        HashSet<Integer> css1 = computeRelevantCS(ps1, ds1, predicateIndexSubj);
        HashSet<Integer> css2 = computeRelevantCS(ps2, ds2, predicateIndexObj);

        // Predicate --> Count
        HashMap<String, Long> mult12 = new HashMap<String, Long>();
        long c = 0L;

        for (Integer cs1 : css1) {
            Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs = getCSSSubj(ds1).get(cs1);

            for (Integer cs2 : css2) {
                Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs2 = getCSSObj(ds2).get(cs2);

                HashMap<Integer, HashMap<String, Integer>> css2Ps = getCPSSubjObj(ds1, ds2).get(cs1);
                if (css2Ps != null) {
                    //System.out.println("Links 1 -> 2");
                    HashMap<String, Integer> links12 = css2Ps.get(cs2);

                    if (links12 != null) {
                        HashSet<String> relevantLinks = new HashSet<String>(links12.keySet());
                        relevantLinks.retainAll(ps1);
                        for (String p : relevantLinks) {
                            Triple t1 = map1.get(p);
                            Triple t2 = sq2.iterator().next();
                            if (t1.getObject().equals(t2.getObject())) {
                                Long m1 = getMultiplicitySubjObj(links12.get(p), p, cPs, ps1, map1, cPs2, ps2, map2);
                                Long m2 = mult12.get(p);
                                if (m2 == null) {
                                    m2 = 0L;
                                }
                                mult12.put(p, m1+m2);
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

        return c;
    }

    public static long getCostCPObj(Set<Triple> sq1, Integer ds1, Set<Triple> sq2, Integer ds2, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) { 

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
            Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs = getCSSObj(ds1).get(cs1);

            for (Integer cs2 : css2) {
                Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs2 = getCSSObj(ds2).get(cs2);

                HashMap<Integer, HashMap<String, Integer>> css2Ps = getCPSObj(ds1, ds2).get(cs1);
                if (css2Ps != null) {
                    //System.out.println("Links 1 -> 2");
                    HashMap<String, Integer> links12 = css2Ps.get(cs2);

                    if (links12 != null) {
                        HashSet<String> relevantLinks = new HashSet<String>(links12.keySet());
                        relevantLinks.retainAll(ps2);
                        for (String p : relevantLinks) {
                            Triple t1 = sq1.iterator().next(); //map1.get(p);
                            Triple t2 = map2.get(p); //sq2.iterator().next();
                            if (t1.getObject().equals(t2.getSubject())) {
                                Long m1 = getMultiplicityObj(links12.get(p), p,  cPs, ps1, map1, cPs2, ps2, map2);
                                Long m2 = mult12.get(p);
                                if (m2 == null) {
                                    m2 = 0L;
                                }
                                mult12.put(p, m1+m2);
                            }
                        }
                    }
                }
                css2Ps = getCPSObj(ds2, ds1).get(cs2);
                if (css2Ps != null) {
                    //System.out.println("Links 2 -> 1");
                    HashMap<String, Integer> links21 = css2Ps.get(cs1);
                    if (links21 != null) {
                        HashSet<String> relevantLinks = new HashSet<String>(links21.keySet());
                        relevantLinks.retainAll(ps1);
                        for (String p : relevantLinks) {
                            Triple t2 = sq2.iterator().next(); //map2.get(p);
                            Triple t1 = map1.get(p); //sq1.iterator().next();
                            if (t2.getObject().equals(t1.getSubject())) {
                                Long m1 = getMultiplicityObj(links21.get(p), p, cPs2, ps2, map2, cPs, ps1, map1);
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
                    //System.out.println("including "+css.size()+" css associated to dataset "+ds+": "+css);
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
                        //System.out.println(css.size()+" relevant css for predicate "+p+" in dataset "+ds+": "+css);
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
    public static long cssCostTreeSubj(Vector<Tree<Pair<Integer,Triple>>> vTree, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) {
        long cost = 0L;
        for (Tree<Pair<Integer,Triple>> tmpTree : vTree) {
            HashMap<String, Triple> map = new HashMap<String, Triple>();
            Set<String> ps = obtainBoundPredicates(tmpTree, map);
            Integer ds = tmpTree.getOneElement().getFirst();
            Integer key = produceStarJoinOrdering.getIKey(ps);        
            long c = 0L;
            HashMap<Integer, Integer> costHM = getCostSubj(ds);
            /*if (costHM.containsKey(key)) {
                c += costHM.get(key);
            } else {*/
                HashSet<Integer> relevantCss = computeRelevantCS(ps, ds, predicateIndex);
                c += computeCost(getCSSSubj(ds), relevantCss, ps, map, true);
            //}
            cost += c;
        }
        return cost;
    }

    // precondition: triples in vTree share the same object and all triples in each tree are evaluated at the same source
    public static long cssCostTreeObj(Vector<Tree<Pair<Integer,Triple>>> vTree, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) {
        long cost = 0L;
        for (Tree<Pair<Integer,Triple>> tmpTree : vTree) {
            HashMap<String, Triple> map = new HashMap<String, Triple>();
            Set<String> ps = obtainBoundPredicates(tmpTree, map);
            Integer ds = tmpTree.getOneElement().getFirst();
            Integer key = produceStarJoinOrdering.getIKey(ps);        
            long c = 0L;
            HashMap<Integer, Integer> costHM = getCostObj(ds);
            /*if (costHM.containsKey(key)) {
                c += costHM.get(key);
            } else {*/
                HashSet<Integer> relevantCss = computeRelevantCS(ps, ds, predicateIndex);
                c += computeCost(getCSSObj(ds), relevantCss, ps, map, false);
            //}
            cost += c;
        }
        return cost;
    }

    // precondition: triples in tree share the same subject and all triples in each tree are evaluated at the same source
    public static long cssCostTreeSubj(Tree<Pair<Integer,Triple>> tree, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) {
        long cost = 0L;
            HashMap<String, Triple> map = new HashMap<String, Triple>();
            Set<String> ps = obtainBoundPredicates(tree, map);
            Integer ds = tree.getOneElement().getFirst();
            Integer key = produceStarJoinOrdering.getIKey(ps);        
            long c = 0L;
            HashMap<Integer, Integer> costHM = getCostSubj(ds);
            /*if (costHM.containsKey(key)) {
                c += costHM.get(key);
            } else {*/
                HashSet<Integer> relevantCss = computeRelevantCS(ps, ds, predicateIndex);
                c += computeCost(getCSSSubj(ds), relevantCss, ps, map, true);
            //}
            cost += c;
        return cost;
    }

    // precondition: triples in tree share the same object and all triples in each tree are evaluated at the same source
    public static long cssCostTreeObj(Tree<Pair<Integer,Triple>> tree, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) {
        long cost = 0L;
            HashMap<String, Triple> map = new HashMap<String, Triple>();
            Set<String> ps = obtainBoundPredicates(tree, map);
            Integer ds = tree.getOneElement().getFirst();
            Integer key = produceStarJoinOrdering.getIKey(ps);        
            long c = 0L;
            HashMap<Integer, Integer> costHM = getCostObj(ds);
            /*if (costHM.containsKey(key)) {
                c += costHM.get(key);
            } else {*/
                HashSet<Integer> relevantCss = computeRelevantCS(ps, ds, predicateIndex);
                c += computeCost(getCSSObj(ds), relevantCss, ps, map, false);
            //}
            cost += c;
        return cost;
    }

    public static long costSubj(Set<Triple> ts, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) {

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
            c += computeCost(getCSSSubj(ds), dsCss.get(ds), ps, map, true);
        }
        return c;
    }

    public static long costObj(Set<Triple> ts, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) {

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
            c += computeCost(getCSSObj(ds), dsCss.get(ds), ps, map, false);
        }
        return c;
    }

    public static long computeCost(HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> css, HashSet<Integer> relevantCss, Set<String> ps, HashMap<String, Triple> map, boolean subjStar) {
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
                boolean c = true;
                if (subjStar) {
                    c = includeMultiplicity && projectedVariables.contains(s) && (!distinct || projectedVariables.contains(o));
                } else {
                    c = includeMultiplicity && projectedVariables.contains(o) && (!distinct || projectedVariables.contains(s));
                }
                if (c) {
                    // p_m css.get(cs).getSecond().get(p).getFirst()
                    // p_d css.get(cs).getSecond().get(p).getSecond()
                    sel = sel*(((double)css.get(cs).getSecond().get(p).getFirst())/costTmp);
                    //System.out.println(p+": "+css.get(cs).getSecond().get(p)); 
                }
                if (subjStar) {
                    c = !o.isVariable();
                } else {
                    c = !s.isVariable();
                }
                if (c) {
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

    public static Tree<Pair<Integer,Triple>> convertToTree(LinkedList<String> orderedPs, HashMap<String, Triple> map, Integer ds, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex, boolean subjCenter) {
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
        //System.out.println("sorted star before considering constants: "+sortedStar);
        considerConstants(sortedStar, predicateIndex, subjCenter);
        return sortedStar;
    }

    public static void considerConstants(Tree<Pair<Integer,Triple>> tree, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex, boolean subjCenter) {

        if (tree instanceof Leaf<?>) {
            return;
        }
        Branch<Pair<Integer, Triple>> b = (Branch<Pair<Integer, Triple>>) tree;
        Tree<Pair<Integer, Triple>> lTree = b.getLeft();
        Tree<Pair<Integer, Triple>> rTree = b.getRight();
        Triple t = rTree.getOneElement().getSecond();
        long cLeft = 0L; //cssCostTree(lTree, predicateIndex);
        long cResult = 0L; //cssCostTree(b, predicateIndex);
        long cRight = 0L; //cssCostTree(rTree, predicateIndex);
        if (subjCenter) {
            if (t.getObject().isVariable()) {
                return;
            }
            cLeft = cssCostTreeSubj(lTree, predicateIndex);
            cResult = cssCostTreeSubj(b, predicateIndex);
            cRight = cssCostTreeSubj(rTree, predicateIndex);
        } else {
            if (t.getSubject().isVariable()) {
                return;
            }
            cLeft = cssCostTreeObj(lTree, predicateIndex);
            cResult = cssCostTreeObj(b, predicateIndex);
            cRight = cssCostTreeObj(rTree, predicateIndex);
        }
        if (cRight <= cLeft + cResult) {
            if (lTree instanceof Leaf<?>) {
                 Tree<Pair<Integer, Triple>> aux = lTree;
                 b.setLeft(rTree);
                 b.setRight(aux);
            } else {
                 Branch<Pair<Integer, Triple>> lBranch = (Branch<Pair<Integer, Triple>>) lTree;
                 Tree<Pair<Integer, Triple>> aux = lBranch.getRight();
                 lBranch.setRight(rTree);
                 considerConstants(lBranch, predicateIndex, subjCenter);
                 b.setRight(aux);
            }
        }
    }

    public static Vector<Tree<Pair<Integer,Triple>>> getStarJoinOrderSubj(Set<Triple> ts, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) { 
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
            HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> css = getCSSSubj(ds);
            HashMap<Integer, Integer> hc = getHCSubj(ds);
            HashMap<Integer, Set<String>> additionalSets = getAdditionalSetsSubj(ds);
            HashMap<Integer, Integer> cost = getCostSubj(ds);
            HashMap<String, HashSet<Integer>> predicateIndexDS = getPredicateIndexSubj(ds, predicateIndex);
            LinkedList<String> orderedPs = produceStarJoinOrdering.getStarJoinOrdering(ps, css, hc, additionalSets, predicateIndexDS, cost);
            //System.out.println("list of sorted predicates: "+orderedPs);
            Tree<Pair<Integer,Triple>> sortedStar = convertToTree(orderedPs, map, ds, predicateIndex, true);
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

    public static Vector<Tree<Pair<Integer,Triple>>> getStarJoinOrderObj(Set<Triple> ts, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) { 
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
        //System.out.println("dsCss for the star: "+dsCss+". ps: "+ps);
        Vector<Tree<Pair<Integer,Triple>>> vTree = new Vector<Tree<Pair<Integer,Triple>>>();
        //HashMap<Integer, Tree<Triple>> dsTree = new HashMap<Integer, Tree<Triple>>();

        for (Integer ds : dsCss.keySet()) {
            HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> css = getCSSObj(ds);
            HashMap<Integer, Integer> hc = getHCObj(ds);
            HashMap<Integer, Set<String>> additionalSets = getAdditionalSetsObj(ds);
            HashMap<Integer, Integer> cost = getCostObj(ds);
            HashMap<String, HashSet<Integer>> predicateIndexDS = getPredicateIndexObj(ds, predicateIndex);
            LinkedList<String> orderedPs = produceStarJoinOrdering.getStarJoinOrdering(ps, css, hc, additionalSets, predicateIndexDS, cost);
            //System.out.println("list of sorted predicates: "+orderedPs);
            Tree<Pair<Integer,Triple>> sortedStar = convertToTree(orderedPs, map, ds, predicateIndex, false);
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

        boolean subjCenter = centerIsSubject(star);
        List<Triple> ts = bgp.getBody();
        //System.out.println("updating ts: "+ts+" for star: "+star);
        //HashSet<Triple> tsAux = new HashSet<Triple>();
        ts.removeAll(star);
        //System.out.println("ts after removing star triples: "+ts);
        HashSet<Node> vsStar = new HashSet<Node>();
        HashSet<Node> vsRest = new HashSet<Node>();
        Node nv = null;
        if (subjCenter) {
            nv = NodeFactory.createVariable("Subj"+i);
        } else {
            nv = NodeFactory.createVariable("Obj"+i);
        }
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
            if (subjCenter && vsRest.contains(o)) {
                ts.add(t);
                toRemove.add(t);
            }
            if (!subjCenter && vsRest.contains(s)) {
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

    public static Vector<HashSet<Triple>> getStars(BGP bgp, long budget, HashMap<String, HashMap<Integer,HashSet<Integer>>>  predicateIndexSubj, HashMap<String, HashMap<Integer,HashSet<Integer>>>  predicateIndexObj) {

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
            long card = costSubj(starSubj, predicateIndexSubj);
            Triple t = starSubj.iterator().next();
            if (card <= budget) {
                stars.add(starSubj);
                ts.removeAll(starSubj);
            }
        }
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
            long card = costObj(starObj, predicateIndexObj);
            Triple t = starObj.iterator().next();
            if (card <= budget) {
                boolean add = true;
                for (HashSet<Triple> s : stars) {
                    if (s.containsAll(starObj)) {
                        add = false;
                    }
                }
                if (add) {
                    stars.add(starObj);
                    ts.removeAll(starObj);
                }
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

