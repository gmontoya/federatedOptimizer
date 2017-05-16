import java.io.*;
import java.util.*;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.query.QueryFactory;

import org.apache.log4j.BasicConfigurator;

import com.hp.hpl.jena.sparql.expr.ExprList;

import com.hp.hpl.jena.sparql.algebra.*;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.syntax.*;

import com.fluidops.fedx.*;
import com.fluidops.fedx.structures.Endpoint;
import org.openrdf.query.*;
import com.fluidops.fedx.util.EndpointFactory;
import com.fluidops.fedx.algebra.FedXStatementPattern;
import org.openrdf.query.algebra.StatementPattern;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.AlgebraGenerator;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery;


class evaluateOurPlansWithFedXOrder {
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
    static Vector<HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>> cpsSubj = new Vector<HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>>();
    static Vector<HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>> cpsObj = new Vector<HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>>();
    static Vector<HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>> cpsSubjObj = new Vector<HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>>();
    static Vector<String> datasets = new Vector<String>();
    static Vector<String> endpoints = new Vector<String>();
    static HashSet<String> generalPredicates = new HashSet<String>();
    static String folder;
    static HashMap<Integer,HashMap<String, HashSet<Integer>>> predicateIndexesSubj = new HashMap<Integer,HashMap<String, HashSet<Integer>>>();
    static HashMap<Integer,HashMap<String, HashSet<Integer>>> predicateIndexesObj = new HashMap<Integer,HashMap<String, HashSet<Integer>>>();
    static boolean distinct;
    static List<Var> projectedVariables;
    static boolean includeMultiplicity;
    static boolean original;
    static HashMap<Integer, Vector<Integer>> globalStats;
    static HashMap<String, HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>>> propertyStats;
    static HashMap<String, HashMap<Integer, Integer>> classStats;

/*
    public static void collapse(Vector<Tree<Pair<Integer, Triple>> v) {
        Tree<Pair<Integer, Triple> p = v.get(0);
        for (int i = 1; i < v.size(); i++) {
            Tree<Pair<Integer, Triple> p2 = v.get(i);
            Tree<Pair<Integer, Triple> f = fusion(p, p2);
            
        }
    }
*/
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
    public static HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> getCPSSubj(Integer ds1, Integer ds2) {
        boolean loaded = false;
        HashMap<Integer, Integer> ds2Pos = datasetsIdsPosSubj.get(ds1);
        Integer pos = -1;
        HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> c =null;
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
                fileCPS += "/"+datasetStr1+"/statistics"+datasetStr1+"_cps_reduced10000CS";
            } else {
                fileCPS += "/cps_"+datasetStr1+"_"+datasetStr2+"_one_rtree_reduced10000CS_1"; //_reduced10000CS_MIP
            }
            c = readCPS(fileCPS);
            cpsSubj.add(pos, c);
        } 
        return c;
    }

    public static HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> getCPSSubjObj(Integer ds1, Integer ds2) {
        boolean loaded = false;
        HashMap<Integer, Integer> ds2Pos = datasetsIdsPosSubjObj.get(ds1);
        Integer pos = -1;
        HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> c =null;
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
                fileCPS += "/"+datasetStr1+"/statistics"+datasetStr1+"_cps_subj_obj_reduced10000CS";
            } else {
                fileCPS += "/cps_"+datasetStr1+"_"+datasetStr2+"_subj_obj_reduced10000CS_MIP";
            }
            c = readCPS(fileCPS);
            cpsSubjObj.add(pos, c);
        } 
        return c;
    }

    public static HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> getCPSObj(Integer ds1, Integer ds2) {
        boolean loaded = false;
        HashMap<Integer, Integer> ds2Pos = datasetsIdsPosObj.get(ds1);
        Integer pos = -1;
        HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> c =null;
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
                fileCPS += "/"+datasetStr1+"/statistics"+datasetStr1+"_cps_obj_reduced10000CS";
            } else {
                fileCPS += "/cps_"+datasetStr1+"_"+datasetStr2+"_obj_reduced10000CS_MIP";
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
        String fileCSS = folder+"/"+datasetStr+"/statistics"+datasetStr+"_css_reduced10000CS";
        HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> cs = produceStarJoinOrdering.readCSS(fileCSS);
        cssSubj.add(pos, cs);
        String fileHC = folder+"/"+datasetStr+"/statistics"+datasetStr+"_hc_reduced10000CS";
        HashMap<Integer, Integer> hc = produceStarJoinOrdering.readMap(fileHC);
        hcsSubj.add(pos, hc);
        String fileAdditionalSets = folder+"/"+datasetStr+"/statistics"+datasetStr+"_as_reduced10000CS";
        HashMap<Integer, Set<String>> ass = produceStarJoinOrdering.readAdditionalSets(fileAdditionalSets);
        additionalSetsSubj.add(pos, ass);
        String fileCost = folder+"/"+datasetStr+"/statistics"+datasetStr+"_cost_reduced10000CS";
        HashMap<Integer, Integer> cost = produceStarJoinOrdering.readMap(fileCost);
        costsSubj.add(pos, cost);
        return pos;
    }

    public static int loadFilesObj(Integer ds) {
        int pos = cssObj.size();
        datasetsIdPosObj.put(ds, pos);
        String datasetStr = datasets.get(ds);
        String fileCSS = folder+"/"+datasetStr+"/statistics"+datasetStr+"_css_obj_reduced10000CS";
        HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> cs = produceStarJoinOrdering.readCSS(fileCSS);
        cssObj.add(pos, cs);
        String fileHC = folder+"/"+datasetStr+"/statistics"+datasetStr+"_hc_obj_reduced10000CS";
        HashMap<Integer, Integer> hc = produceStarJoinOrdering.readMap(fileHC);
        hcsObj.add(pos, hc);
        String fileAdditionalSets = folder+"/"+datasetStr+"/statistics"+datasetStr+"_as_obj_reduced10000CS";
        HashMap<Integer, Set<String>> ass = produceStarJoinOrdering.readAdditionalSets(fileAdditionalSets);
        additionalSetsObj.add(pos, ass);
        String fileCost = folder+"/"+datasetStr+"/statistics"+datasetStr+"_cost_obj_reduced10000CS";
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

    public static void loadStatistics() {

        for (int ds1 = 0; ds1 < datasets.size(); ds1++) {
            loadFilesSubj(ds1);
            loadFilesObj(ds1);
            for (int ds2 = 0; ds2 < datasets.size(); ds2++) {
                HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> aux = getCPSSubjObj(ds1, ds2);
                aux = getCPSSubj(ds1, ds2);
                aux = getCPSObj(ds1, ds2);
            }
        }
    }

    public static HashMap<String, HashMap<Integer,HashSet<Integer>>> readPredicateIndexes(String folder, Vector<String> datasets, String suffix) {

        HashMap<String, HashMap<Integer,HashSet<Integer>>> predIndex = new HashMap<String, HashMap<Integer,HashSet<Integer>>>();
        for (int i = 0; i < datasets.size(); i++) {
            String datasetStr = datasets.get(i);
            String fileName = folder+"/"+datasetStr+"/statistics"+datasetStr+"_pi"+suffix+"_reduced10000CS";
            
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

    public static HashSet<String> readPredicates(String file) {
        HashSet<String> ps = new HashSet<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String l = br.readLine();
            //Vector<String> ps = new Vector<String>();
            while (l!=null) {
                ps.add(l); //NodeFactory.createURI(l));
                l = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }
        return ps;
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

    protected static ArrayList<HashSet<Triple>> getBGPs(Query query) {

        ArrayList<HashSet<Triple>> bgps = null;
        try {
            Op op = (new AlgebraGenerator()).compile(query);
            BGPVisitor bgpv = new BGPVisitor();
            OpWalker.walk(op, bgpv);
            bgps = bgpv.getBGPs();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return bgps;
    }

    public static void prepareFedX() {
        String fedxConfig = "/home/roott/federatedOptimizer/lib/fedX3.1/config2";
        String dataConfig = "/home/roott/fedBenchFederation.ttl";
        //System.out.println("evaluating "+queryStr);
        try {
            Config.initialize(fedxConfig);
            List<Endpoint> ep = EndpointFactory.loadFederationMembers(new File(dataConfig));
            FedXFactory.initializeFederation(ep);
            //Config.getConfig().set("optimize", ""+optimize);
        } catch (Exception e) {
            
        }
    }

    public static void main(String[] args) throws Exception {
        //BasicConfigurator.configure();
        long t0 = System.currentTimeMillis();
        String queryFile = args[0];
        int pos = queryFile.lastIndexOf("/");
        String queryId = queryFile.substring(pos>=0?(pos+1):0);
        String datasetsFile = args[1];
        folder = args[2];
        long budget = Long.parseLong(args[3]);
        includeMultiplicity = Boolean.parseBoolean(args[4]);
        original = Boolean.parseBoolean(args[5]);
        String fileName = args[6];
        String generalPredicatesFile = args[7];
        prepareFedX();
        generalPredicates = readPredicates(generalPredicatesFile);
        datasets = readDatasets(datasetsFile);
        globalStats = new HashMap<Integer, Vector<Integer>>();
        // Predicate --> DatasetId --> (numTriples, (numDistSubj, numDistObj))
        propertyStats = new HashMap<String, HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>>>();
        // Class --> DatasetId --> numEntities
        classStats = new HashMap<String, HashMap<Integer, Integer>>();
        produceJoinOrderingVOID.folder = folder;
        produceJoinOrderingVOID.datasets = datasets;
        produceJoinOrderingVOID.loadStatistics(globalStats, propertyStats, classStats);
        loadStatistics();
        // Predicate --> DatasetId --> set of CSId
        HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexSubj = readPredicateIndexesSubj(folder, datasets); 
        HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexObj = readPredicateIndexesObj(folder, datasets); 
        //System.out.println("predicateIndexSubj size: "+predicateIndexSubj.size());
        //System.out.println("predicateIndexObj size: "+predicateIndexObj.size());
        Query query = QueryFactory.read(queryFile);
        ArrayList<HashSet<Triple>> bgps = getBGPs(query);
        HashMap<HashSet<Triple>, Vector<Tree<Pair<Integer,Triple>>>> plans = new HashMap<HashSet<Triple>, Vector<Tree<Pair<Integer,Triple>>>>();
        //HashSet<ArrayList<Triple>, List<Var>> projectedVariables

        //BGP bgp = new BGP(queryFile);
        distinct = query.isDistinct();
        projectedVariables = query.getProjectVars();
        // sub-query --> <order, <cardinality,cost>>
        long t1 = System.currentTimeMillis();
        System.out.println("loading: "+(t1-t0));
        long t2 = System.currentTimeMillis();
        for (HashSet<Triple> triples : bgps) {
            HashSet<Triple> ts = new HashSet<Triple>(triples);
            boolean vars = false;
            for (Triple t : triples) {
                if (t.getPredicate().isVariable()) {
                    vars = true;
                    break;
                }
            }
            if (vars) {
                break;
            }
            HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long, Long>>> DPTable = new HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long, Long>>>();
            HashMap<HashSet<Node>, Pair<HashSet<Node>, HashSet<Node>>> log = new HashMap<HashSet<Node>, Pair<HashSet<Node>, HashSet<Node>>>();
            //t0 = System.currentTimeMillis();
            Vector<HashSet<Triple>> stars = getStars(triples, budget, predicateIndexSubj, predicateIndexObj); //css, predicateIndex, cost);
            //t1 = System.currentTimeMillis();
            //System.out.println("getStars: "+(t1-t0));
            //t0 = System.currentTimeMillis();
            //System.out.println("stars: "+stars);
            int i = 1;
            //HashSet<Triple> triples = new HashSet<Triple>();
            HashSet<Node> nodes = new HashSet<Node>();
            HashMap<Node, Vector<Tree<Pair<Integer,Triple>>>> map = new HashMap<Node, Vector<Tree<Pair<Integer,Triple>>>>();

            for (HashSet<Triple> s : stars) {
                // consider that previous star may be connected to the current star
                //# HashMap<Triple,Triple> renamed = new HashMap<Triple,Triple>();
                //# HashSet<Triple> renamedStar = rename(s, map, renamed);
                // for updating the bgp consider the renamed star
                //# Node nn = update(triples, renamedStar, i, map);
                // current star should be consistent with renamed star
                //# s.clear();
                /*#for (Triple t : renamedStar) {
                    Triple r = renamed.get(t);
                    s.add(r);
                }*/
                //LOG System.out.println("updated bgp "+bgp+" for star : "+s+" and for nn: "+nn);
                // current star is used for the join ordering, it does not contain any metanode
                //#if (s.size()>0) {
                Node nn = NodeFactory.createVariable("Star"+i); 
                    Vector<Tree<Pair<Integer,Triple>>> p = null;
                    //System.out.println("centerIsSubject(s)? "+centerIsSubject(s));
                    if (centerIsSubject(s)) {
                        p = getStarJoinOrderSubj(s, predicateIndexSubj); 
                    } /* else {  // REMOVING_OBJ_STATS
                        p = getStarJoinOrderObj(s, predicateIndexObj); 
                    }*/
                    //System.out.println("join ordering for the star: "+p);
                    if (p.size() ==0) {  // test case missing sources for a star
                        nodes.clear();
                        map.clear();
                        triples.clear();
                        break;
                    }
                    long cost = 0L;
                    if (centerIsSubject(s)) {
                        cost = cssCostTreeSubj(p, predicateIndexSubj);
                    } /*else {   // REMOVING_OBJ_STATS
                        cost = cssCostTreeObj(p, predicateIndexObj);
                    } */
                    map.put(nn, p);
                    i++;
                    HashSet<Node> ns = new HashSet<Node>();
                    ns.add(nn);
                    nodes.add(nn);
                    triples.removeAll(s);
                    DPTable.put(ns, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(p, new Pair<Long, Long>(cost,cost))); //0L))); 
                //#}
            }
            //t1 = System.currentTimeMillis();
            //System.out.println("stars have been added to DPTable: "+(t1-t0));
            //t0 = System.currentTimeMillis();
            //System.out.println("map: "+map);
            //System.out.println("DPTable before add.. :"+DPTable);
            addRemainingTriples(DPTable, triples, predicateIndexSubj, predicateIndexObj, nodes, map); //, css, cps, predicateIndex, triples, cost, map, hc, additionalSets);
            //t1 = System.currentTimeMillis();
            //System.out.println("addRemainingTriples: "+(t1-t0));
            //t0 = System.currentTimeMillis();
            //System.out.println("DPTable after add.. :"+DPTable);
            // may have to consider already intermediate results in the CP estimation for the cost
            HashMap<Pair<Integer, Triple>, HashMap<HashSet<Pair<Integer, Triple>>, Double>> selectivity = new HashMap<Pair<Integer, Triple>, HashMap<HashSet<Pair<Integer, Triple>>, Double>>();
            //HashMap<HashSet<Node>, Vector<Tree<Pair<Integer,Triple>>>> selectivity = new HashMap<HashSet<Node>, Double>();
            estimateSelectivityCP(DPTable, predicateIndexSubj, predicateIndexObj, nodes, triples, map, selectivity, log); //css, cps, predicateIndex, triples, hc, additionalSets, cost);
            //t1 = System.currentTimeMillis();
            //System.out.println("estimateSelectivity: "+(t1-t0));
            //t0 = System.currentTimeMillis();
            //System.out.println("DPTable after CP estimation.. :"+DPTable);
            //LOG System.out.println("selectivity :"+selectivity);
            //System.out.println("nodes :"+nodes);
            computeJoinOrderingDP(DPTable, nodes, selectivity, log);
            //t1 = System.currentTimeMillis();
            //System.out.println("computeJoinOrdering: "+(t1-t0));
            //t0 = System.currentTimeMillis();
            Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long, Long>> res = DPTable.get(nodes);
            if (res != null) {
                plans.put(ts, res.getFirst());
                //System.out.println("Cardinality: "+res.getSecond().getFirst());
                //System.out.println("Cost: "+res.getSecond().getSecond());
                //System.out.println(res.getFirst());
            }
            //System.out.println("DPTable at the end: "+DPTable);
            //System.out.println("log at the end: "+log);
        }
        t2 = System.currentTimeMillis() - t2;
        System.out.println("planning="+t2+"ms");
        //System.out.println("processing: "+(t1-t2));
        //t0 = System.currentTimeMillis();
        Query newQuery = produceQueryWithServiceClauses(query, plans);
        //System.out.println(newQuery);
        //System.exit(1);
        //t1 = System.currentTimeMillis();
        //System.out.println("rewriting: "+(t1-t0));
        //t0 = System.currentTimeMillis();
        if (newQuery != null) {
            // now invert the order to let fedX do the join order...
            Op op = (new AlgebraGenerator()).compile(newQuery);
            TransformerDeleteFilters t01 = new TransformerDeleteFilters();
            Op opBase = Transformer.transform(t01, op);
            ExprList el = t01.getFilters();
            Transform t = new TransformerInverseJoinOrder(el,true);
            Op newOp = Transformer.transform(t, opBase);
            newQuery = OpAsQuery.asQuery(newOp);
            System.out.println("Plan: "+newQuery);
            //write(newQuery.toString(), fileName);
            //System.out.println("Done query " + queryId + ": planning="+t2+"ms");
            evaluate(newQuery.toString(), queryId, true);
        } else {
            //System.out.println("Plan not found");
            evaluate(query.toString(), queryId, true);
        }
        FederationManager.getInstance().shutDown();
        //t1 = System.currentTimeMillis();
        //System.out.println("evaluation: "+(t1-t0));
        //System.out.println("DPTable at the end: "+DPTable);
        System.exit(0);
        //System.out.println("DPTable at the end: "+DPTable);
    }

    public static void evaluate(String queryStr, String queryId, boolean optimize) {

        //String fedxConfig = "/home/roott/federatedOptimizer/lib/fedX3.1/config2";
        //String dataConfig = "/home/roott/fedBenchFederation.ttl";
        //System.out.println("evaluating "+queryStr);
        try {
            //Config.initialize(fedxConfig); 
            //List<Endpoint> ep = EndpointFactory.loadFederationMembers(new File(dataConfig));
            //FedXFactory.initializeFederation(ep);
            Config.getConfig().set("optimize", ""+optimize);
            TupleQuery query = QueryManager.prepareTupleQuery(queryStr);
            /*List<StatementPattern> ss = null;
            try {
              StatementsVisitor sv = new StatementsVisitor();
              query.visit(sv);
              ss = sv.getStatements();
              for (StatementPattern s : ss) {
                if (s instanceof FedXStatementPattern) {
                    System.out.println("statement "+s+" with sources: "+s.getStatementSources());
                } else {
                    System.out.println("statement "+s+" with no sources");
                }
              }
            } catch (Exception e) {
              e.printStackTrace();
              System.exit(1);
            }*/
            long start = System.currentTimeMillis();
            TupleQueryResult res = query.evaluate();
            int n=0;
            while (res.hasNext()) {
                //BindingSet bs = res.next();
	        System.out.println(res.next());
                n++;
            }
            long duration = System.currentTimeMillis() - start; 

            System.out.println("Done query " + queryId + ": duration=" + duration + "ms, results=" + n);

            //System.out.println("finished");
            //FederationManager.getInstance().shutDown();
            //System.out.println("results="+n);
            //System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error executing query");
            System.exit(1);
        }
    }

    public static Query produceQueryWithServiceClauses(Query query, HashMap<HashSet<Triple>, Vector<Tree<Pair<Integer,Triple>>>> plans) {

        Op op = (new AlgebraGenerator()).compile(query);
        //System.out.println("op1: "+op);
        TransformerDeleteFilters t0 = new TransformerDeleteFilters();
        Op opBase = Transformer.transform(t0, op);
        ExprList el = t0.getFilters();
        Transform t = new TransformInOneDotOneSPARQL(plans, el, endpoints);
        Op newOp = Transformer.transform(t, opBase);
        //System.out.println("newOp: "+newOp);
        if (newOp instanceof OpNull) {
            return null;
        }
        Query newQuery = OpAsQuery.asQuery(newOp);
        return newQuery;
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
        //LOG System.out.println("rename start. star: "+star+". map: "+map);
        HashMap<Node, Node> invertMap = new HashMap<Node,Node>();
        for (Node n : map.keySet()) {
            Tree<Pair<Integer,Triple>> tree = map.get(n).get(0);
            Set<Triple> triples = obtainTriples(tree);
            for (Triple t : triples) {
                Node s = t.getSubject();
                Node o = t.getObject(); 
                //Node c = getCenter(tree);
                invertMap.put(s, n);
                invertMap.put(o, n);
            }
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
        //LOG System.out.println("rename end. rewStar: "+newStar);
        return newStar;
    }

    // CS and CP already provide cardinalities for sets of triples with one or two elements
    public static void computeJoinOrderingDP(HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable, HashSet<Node> nodes, HashMap<Pair<Integer, Triple>, HashMap<HashSet<Pair<Integer,Triple>>, Double>> selectivity, HashMap<HashSet<Node>, Pair<HashSet<Node>, HashSet<Node>>> log) {

        for (int i = 2; i <= nodes.size(); i++) {
            computeJoinOrderingDPAux(DPTable, nodes, i, selectivity, log);
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

    public static void computeJoinOrderingDPAux(HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable, HashSet<Node> nodes, int i, HashMap<Pair<Integer, Triple>, HashMap<HashSet<Pair<Integer,Triple>>, Double>> selectivity, HashMap<HashSet<Node>, Pair<HashSet<Node>, HashSet<Node>>> log) {
        // currently cost depends only on intermediate results, then for the same two sub-queries their order do not matter
        // start considering sub-queries from half the size of the current sub-queries (i)
        //LOG System.out.println("computeJoinOrderingDPAux, i: "+i);
        //# int k = (int) Math.round(Math.ceil(i/2.0));
        //System.out.println("DPTable: "+DPTable);
        //System.out.println("log: "+log);
        //System.out.println("selectivity: "+selectivity);
        HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTableAux = new HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>>(DPTable);
        HashMap<HashSet<Node>, Pair<HashSet<Node>, HashSet<Node>>> logAux =  new HashMap<HashSet<Node>, Pair<HashSet<Node>, HashSet<Node>>>();
        for (HashSet<Node> ns : DPTable.keySet()) {
            Vector<Tree<Pair<Integer,Triple>>> aux = DPTable.get(ns).getFirst();
            if (/*# ns.size() < k ||*/ aux.size()==0) {
                continue;
            }
            HashSet<Node> rn = new HashSet<Node>(nodes);
            rn.removeAll(ns);
            Vector<HashSet<Node>> subsets = new Vector<HashSet<Node>>();
            getAllSubsets(rn, i-ns.size(), subsets);
            for (HashSet<Node> ss : subsets) {
                //HashSet<Triple> intersection = new HashSet<Triple>(ns);
                //intersection.retainAll(ss);
                if (ss.size() == 0 || !DPTable.containsKey(ss)) {
                    continue;
                }
                //System.out.println("ss: "+ss);
                //#for (HashSet<Node> sss : DPTable.keySet()) {
                    //#if (!sss.containsAll(ss)) {
                    //if (!DPTable.containsKey(ss)|| DPTable.get(ss).getFirst().size()==0) { // || (intersection.size()>0)) {
                    //#    continue;
                    //#}
                    HashSet<Node> sss  = ss;
                    //Long card = getCard(DPTable, ns, sss, selectivity);
                    Vector<Tree<Pair<Integer,Triple>>> order = getJoinTree(DPTable, ns, sss, selectivity);
                    Long card = sumCardinalities(order);
                    HashSet<Node> newEntry = new HashSet<Node>(ns);
                    newEntry.addAll(sss);
                    Long cost = costTransfer(order);
                    //System.out.println("considering "+ns+" and "+sss+". cost: "+cost);
                    Vector<Tree<Pair<Integer,Triple>>> vTreeNS = DPTable.get(ns).getFirst();
                    Vector<Tree<Pair<Integer,Triple>>> vTreeSS = DPTable.get(sss).getFirst();
                    //LOG System.out.println("(B) vTreeNS: "+vTreeNS);
                    //LOG System.out.println("(B) vTreeSS: "+vTreeSS);
                    //Pair<Vector<Tree<Pair<Integer,Triple>>>, Vector<Tree<Pair<Integer,Triple>>>> p = removeRedundancy(vTreeNS, vTreeSS);
                    //vTreeNS = p.getFirst();
                    //vTreeSS = p.getSecond();
                    //System.out.println("ns: "+ns);
                    //System.out.println("ss: "+ss);
                    //System.out.println("sss: "+sss);
                    //LOG System.out.println("(A) vTreeNS: "+vTreeNS);
                    //LOG System.out.println("(A) vTreeSS: "+vTreeSS+"DPTable.get(sss).getFirst(): "+DPTable.get(sss).getFirst());
                    /*Long cost = -1L;
                    if (vTreeNS.size() == 1 && vTreeSS.size() == 1) {
                        Integer sourceNS = sameSource(vTreeNS.get(0).getElements());
                        Integer sourceSS = sameSource(vTreeSS.get(0).getElements());
                        if (sourceNS != null && sourceSS != null && sourceNS.equals(sourceSS)) {
                            cost = card;
                        }
                    }*/
                    //System.out.println("cost: "+cost);
                    /*if (cost == -1) {
                        //Long costLeft = DPTable.get(ns).getSecond().getSecond();
                        //Long costRight = DPTable.get(sss).getSecond().getSecond();
                        //cost = card + costLeft;
                        cost = card + costTransfer(DPTable, log, ns, sss);
                    }*/
                    //System.out.println("cost: "+cost);
                    /*#
                    Long costLeft = DPTable.get(ns).getSecond().getSecond();
                    Long costRight = DPTable.get(sss).getSecond().getSecond();
                    if (costRight < costLeft) {
                        Vector<Tree<Pair<Integer,Triple>>> tmp = vTreeNS;
                        vTreeNS = vTreeSS;
                        vTreeSS = tmp;
                    }
                    Long cost = card + DPTable.get(ns).getSecond().getSecond()+DPTable.get(sss).getSecond().getSecond();*/
                    
                    //HashSet<Node> newEntry = new HashSet<Node>(ns);
                    //newEntry.addAll(sss);
                    //LOG System.out.println("newEntry: "+newEntry+". cost: "+cost);
                    Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>> pair = DPTableAux.get(newEntry);
                    if ((pair == null) || pair.getSecond().getSecond()>cost) {
                        //Vector<Tree<Pair<Integer,Triple>>> order = makeTreeBasic(vTreeNS,vTreeSS);
                        //System.out.println("merging : "+ns+" and "+sss+". card: "+card+". cost: "+cost+". pair: "+pair+". newEntry: "+newEntry+". order: "+order);
                        DPTableAux.put(newEntry, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(order, new Pair<Long,Long>(card, cost)));
                        Pair<HashSet<Node>, HashSet<Node>> pairAux = new Pair<HashSet<Node>, HashSet<Node>>(ns, sss);
                        logAux.put(newEntry, pairAux);
                    }
                //#}
            }
        }
        DPTable.putAll(DPTableAux);
        log.putAll(logAux);
    }

    public static long sumCardinalities(Vector<Tree<Pair<Integer,Triple>>> vTree) {

        long c = 0L;
        try {
            for (Tree<Pair<Integer,Triple>> tree : vTree) {
                c = Math.addExact(c, tree.getCard());
            }
        } catch (ArithmeticException e) {
            c = Long.MAX_VALUE;
        }
        return c;
    }
/*
    public static Pair<Vector<Tree<Pair<Integer,Triple>>>,Vector<Tree<Pair<Integer,Triple>>>> removeRedundancy(Vector<Tree<Pair<Integer,Triple>>> vTreeNS, Vector<Tree<Pair<Integer,Triple>>> vTreeSS) {
        Vector<Tree<Pair<Integer,Triple>>> resNS = vTreeNS; //new Vector<Tree<Pair<Integer,Triple>>>();
        Vector<Tree<Pair<Integer,Triple>>> resSS = vTreeSS; //new Vector<Tree<Pair<Integer,Triple>>>();

        Set<Triple> triples = obtainTriples(vTreeNS.get(0));
        triples.retainAll(obtainTriples(vTreeSS.get(0)));

        for (Triple t : triples) {
            //Set<Integer> sourcesSS = obtainSources(vTreeNS, t);
            //Set<Integer> sourcesNS = obtainSources(vTreeSS, t);
            //Set<Integer> commonSources = new HashSet<Integer>(sourcesNS);
            //commonSources.retainAll(sourcesSS);
            //resNS = deleteOthers(resNS, t, commonSources);
            //resSS = deleteOthers(resSS, t, commonSources);   
            
            for (int i = 0; i < resNS.size();i++) {  
                Tree<Pair<Integer,Triple>> treeNS = resNS.get(i);
                for (int k = 0; k < resSS.size(); k++) {
                    Tree<Pair<Integer,Triple>> treeSS = resSS.get(k);
                    Integer sourceNS = obtainSource(treeNS, t);
                    Integer sourceSS = obtainSource(treeSS, t);
                    if (sourceNS.equals(sourceSS)) {
                        int connectedSameSourceNS = obtainNumberConnectedTriplesSameSource(treeNS, t, sourceNS);
                        int connectedSameSourceSS = obtainNumberConnectedTriplesSameSource(treeSS, t, sourceSS); 
                        if (connectedSameSourceNS >= connectedSameSourceSS) {
                            treeSS = remove(treeSS, t);
                        } else {
                            treeNS = remove(treeNS, t);
                        }
                    } else {
                        resSS.remove(treeSS);
                        resNS.remove(treeNS);
                        i--;
                        k--;
                    }
                }
            }
//                Set<Pair<Integer, Triple>> elems = tmp.getElements();

  //              for (Pair<Integer, Triple> e : elems) {
                    //System.out.println("removing e: "+e);
 //                   aux = remove(aux, e);
//                    if (aux== null) {
//                        break;
 //                   }
//                }
//            }
//            if (aux != null) {
//                res.add(aux);
//            }
            //System.out.println("after: "+vTreeSS);
//          }
        }
        Pair<Vector<Tree<Pair<Integer,Triple>>>,Vector<Tree<Pair<Integer,Triple>>>> pair = new Pair<Vector<Tree<Pair<Integer,Triple>>>,Vector<Tree<Pair<Integer,Triple>>>>(resNS, resSS);
        return pair;
    }*/

    public static long costTransfer(Vector<Tree<Pair<Integer,Triple>>> vTree) {

        long cost = 0L;

        for (Tree<Pair<Integer,Triple>> tree : vTree) {
            long c = 0L;
            try {
                c = costRec(tree);
            } catch (ArithmeticException e) {
                c = Long.MAX_VALUE;
            }
            //System.out.println("tree: "+tree+". cost: "+c);
            tree.setCost(c);
            try {
                cost = Math.addExact(cost, c);
            } catch (ArithmeticException e) {
                cost = Long.MAX_VALUE;
            }
        }
        return cost;
    }

    public static long costRec(Tree<Pair<Integer,Triple>> tree) {

        long c = tree.getCard();
        if (sameSource(tree.getElements()) == null) {
            Branch<Pair<Integer,Triple>> b = (Branch<Pair<Integer,Triple>>) tree;
            Tree<Pair<Integer,Triple>> left = b.getLeft();
            Tree<Pair<Integer,Triple>> right = b.getRight();
            c = Math.addExact(c, Math.addExact(left.getCost(), costRecAux(right)));
        }
        return c;
    }

    public static long costRecAux(Tree<Pair<Integer,Triple>> tree) {

        long c = 0L;
        if (sameSource(tree.getElements()) == null) {
            Branch<Pair<Integer,Triple>> b = (Branch<Pair<Integer,Triple>>) tree;
            Tree<Pair<Integer,Triple>> left = b.getLeft();
            Tree<Pair<Integer,Triple>> right = b.getRight();
            c = Math.addExact(c, Math.addExact(left.getCost(), costRecAux(right)));
        }
        return c;
    }

    public static double obtainNumberConnectedTriples(Tree<Pair<Integer,Triple>> tree, Triple t) {
        HashSet<Node> c1 = new HashSet<Node>();
        c1.add(t.getSubject());
        c1.add(t.getObject());
        int c = 0;
        Set<Pair<Integer, Triple>> elems = tree.getElements();
        for (Pair<Integer, Triple> e : elems) {
            Triple t2 = e.getSecond();
            Integer s2 = e.getFirst();
            HashSet<Node> c2 = new HashSet<Node>();
            c2.add(t2.getSubject());
            c2.add(t2.getObject());
            c2.retainAll(c1);
            c += c2.size();
        }
        return ((double) c)/elems.size();
    }

    public static double obtainNumberConnectedTriplesSameSource(Tree<Pair<Integer,Triple>> tree, Triple t, Integer source) {
        HashSet<Node> c1 = new HashSet<Node>();
        c1.add(t.getSubject());
        c1.add(t.getObject());
        int c = 0;
        Set<Pair<Integer, Triple>> elems = tree.getElements();
        for (Pair<Integer, Triple> e : elems) {
            Triple t2 = e.getSecond();
            Integer s2 = e.getFirst();
            if (s2.equals(source)) {
                HashSet<Node> c2 = new HashSet<Node>();
                c2.add(t2.getSubject());
                c2.add(t2.getObject());
                c2.retainAll(c1);
                c += c2.size();
            }
        }
        return ((double) c)/elems.size();
    }

    public static  Integer obtainSource(Tree<Pair<Integer,Triple>> tree, Triple t) {

        Integer source = 0;
        Set<Pair<Integer, Triple>> elems = tree.getElements();
        for (Pair<Integer, Triple> e : elems) {
            if (e.getSecond().equals(t)) {
                source = e.getFirst();
                break;
            }
        }
        return source;
    }

    public static Vector<Tree<Pair<Integer,Triple>>> deleteOthers(Vector<Tree<Pair<Integer,Triple>>> vTree, Triple t, Set<Integer> sources) {

        Vector<Tree<Pair<Integer,Triple>>> res = new Vector<Tree<Pair<Integer,Triple>>>();
        for (int i = 0; i < vTree.size(); i++) {
            Tree<Pair<Integer,Triple>> tree = vTree.get(i);
            Set<Pair<Integer, Triple>> elems = tree.getElements();
            boolean add = true;
            for (Pair<Integer, Triple> e : elems) {
                if (e.getSecond().equals(t) && !sources.contains(e.getFirst())) {
                    add = false;
                    break;
                }        
            }
            if (add) {
                res.add(tree); 
            }
        }
        return res;
    }

    public static Vector<Tree<Pair<Integer,Triple>>>  remove(Vector<Tree<Pair<Integer,Triple>>> vTree, Triple t) {

        Vector<Tree<Pair<Integer,Triple>>> res = new Vector<Tree<Pair<Integer,Triple>>>();
        for (int i = 0; i < vTree.size(); i++) {
            Tree<Pair<Integer,Triple>> tree = vTree.get(i);
            tree = remove(tree, t);
            if (tree != null) {
                res.add(tree);
            }
        }
        return res;
    }

    public static Tree<Pair<Integer,Triple>> remove(Tree<Pair<Integer,Triple>> tree, Triple t) {

        //System.out.println("tree: "+tree+". e: "+e);
        if (tree instanceof Branch<?>) {
            Branch<Pair<Integer,Triple>> b = (Branch<Pair<Integer,Triple>>) tree;
            Tree<Pair<Integer,Triple>> lTree = b.getLeft();
            Tree<Pair<Integer,Triple>> rTree = b.getRight();
            if (lTree instanceof Leaf<?> && lTree.getOneElement().getSecond().equals(t)) {
                return rTree;
            } else if (rTree instanceof Leaf<?> && rTree.getOneElement().getSecond().equals(t)) {
                return lTree;
            }
            if (lTree instanceof Branch<?>) {
                lTree = remove(lTree, t);
            }
            if (rTree instanceof Branch<?>) {
                rTree = remove(rTree, t);
            }
            tree = new Branch(lTree, rTree);
        } else if (tree instanceof Leaf<?> && tree.getOneElement().getSecond().equals(t)) {
            return null;
        }
        return tree;
    }

    // to estimate the cardinality between two sub-queries, consider the information already in DPTable about the joins
    // between two triple patterns, one from each sub-query to compute the selectivity of the joins and the cardinality of the new join
    public static Vector<Tree<Pair<Integer,Triple>>> getJoinTree(HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable, HashSet<Node> sq1, HashSet<Node> sq2, HashMap<Pair<Integer, Triple>, HashMap<HashSet<Pair<Integer,Triple>>, Double>> selectivity) {
        Vector<Tree<Pair<Integer,Triple>>> res = new Vector<Tree<Pair<Integer,Triple>>>();
        Vector<Tree<Pair<Integer,Triple>>> vTree1 = DPTable.get(sq1).getFirst();
        Vector<Tree<Pair<Integer,Triple>>> vTree2 = DPTable.get(sq2).getFirst();

        for (Tree<Pair<Integer,Triple>> tree1 : vTree1) {
            long cardSQ1 = tree1.getCard();
            for (Tree<Pair<Integer,Triple>> tree2 : vTree2) {
                long cardSQ2 = tree2.getCard();
                double sel = 1.0;
                Branch<Pair<Integer,Triple>> b = new Branch<Pair<Integer,Triple>>(tree1, tree2);
                // considering selectivity for joins from tree1 to tree2
                for (Pair<Integer, Triple> p : tree1.getElements()) {
                    HashMap<HashSet<Pair<Integer,Triple>>, Double> tsSel = selectivity.get(p);
                    if (tsSel ==  null) {
                        continue;
                    }
                    for (HashSet<Pair<Integer,Triple>> tmpSet : tsSel.keySet()) {
                        HashSet<Pair<Integer,Triple>> set = new HashSet<Pair<Integer,Triple>>(tree2.getElements());
                        set.retainAll(tmpSet);
                        if (set.size()>0) {
                            double tmpSel = tsSel.get(tmpSet);
                            //System.out.println("selectivity of "+p+" and "+set+" : "+tmpSel);
                            sel = sel*tmpSel;
                        }
                    }
                }
                // considering selectivity for joins from tree2 to tree1
                for (Pair<Integer, Triple> p : tree2.getElements()) {
                    HashMap<HashSet<Pair<Integer,Triple>>, Double> tsSel = selectivity.get(p);
                    if (tsSel ==  null) {
                        continue;
                    }
                    for (HashSet<Pair<Integer,Triple>> tmpSet : tsSel.keySet()) {
                        HashSet<Pair<Integer,Triple>> set = new HashSet<Pair<Integer,Triple>>(tree1.getElements());
                        set.retainAll(tmpSet);
                        if (set.size()>0) {
                            double tmpSel = tsSel.get(tmpSet);
                            //System.out.println("selectivity of "+p+" and "+set+" : "+tmpSel);
                            sel = sel*tmpSel;
                        }
                    }
                }
                //System.out.println("selectivity: "+sel);
                long card = 0L;
                try {
                    card = Math.round(Math.ceil(Math.multiplyExact(cardSQ1, cardSQ2) * sel));
                } catch (ArithmeticException e) {
                    card = Long.MAX_VALUE;
                }
                if (card > 0) {
                  b.setCard(card);
                  res.add(b);
                }
            }
        }
        return res;
    }

    public static void addRemainingTriples(HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable, HashSet<Triple> triples, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexSubj, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexObj, HashSet<Node> nodes, HashMap<Node, Vector<Tree<Pair<Integer,Triple>>>> map) {

        //System.out.println("ts: "+ts);
        //System.out.println("map: "+map);
        HashSet<Node> toAdd = new HashSet<Node>();
        for (Triple t : triples) {
            Node s = t.getSubject();
            Node p = t.getPredicate();
            Node o = t.getObject();
            //#if (!nodes.contains(s)) {
                toAdd.add(s);
                toAdd.add(o);
                Set<Triple> set = new HashSet<Triple>();
                set.add(t);
                //long c = costSubj(set, predicateIndexSubj);
                HashSet<Node> newEntry = new HashSet<Node>();
                newEntry.add(s);
                newEntry.add(o);
                Vector<Tree<Pair<Integer,Triple>>> vector = getStarJoinOrderSubj(set, predicateIndexSubj);
                long c = cssCostTreeSubj(vector, predicateIndexSubj);
//new Vector<Tree<Pair<Integer,Triple>>>();
                Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>> data = DPTable.get(newEntry);
                long prevC = Long.MAX_VALUE;
                if (data != null) {
                    prevC = data.getSecond().getFirst();
                }
                // including the minimum cost if inferior to previous value
                if (c < prevC) { // if not there, cost is zero
                    DPTable.put(newEntry, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(vector, new Pair<Long, Long>(c,c))); //0L))); 
                }
            /*#}
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
                    DPTable.put(newEntry, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(vector, new Pair<Long, Long>(c,c))); //0L))); 
                }
            }*/
        }
        nodes.addAll(toAdd);
    }

    public static boolean existsCPConnectionSubj(Set<Triple> sq1, Set<Triple> sq2) {

        return existsCSConnectionSubj(sq1) && existsCSConnectionSubj(sq2) && (existsCPConnectionAuxS(sq1, sq2) || existsCPConnectionAuxS(sq2, sq1));
    }

    public static boolean existsCPConnectionSubjObj(Set<Triple> sq1, Set<Triple> sq2) {
        //System.out.println("existsCSConnectionSubj(sq1): "+existsCSConnectionSubj(sq1));
        //System.out.println("existsCSConnectionObj(sq2): "+existsCSConnectionObj(sq2));
        //System.out.println("existsCPConnectionAuxSO(sq1, sq2): "+existsCPConnectionAuxSO(sq1, sq2));
        //System.out.println("existsCPConnectionAuxSO(sq2, sq1): "+existsCPConnectionAuxSO(sq2, sq1));
        return existsCSConnectionSubj(sq1) && existsCSConnectionObj(sq2) && existsCPConnectionAuxSO(sq1, sq2);
    }

    public static boolean existsCPConnectionObj(Set<Triple> sq1, Set<Triple> sq2) {

        return existsCSConnectionObj(sq1) && existsCSConnectionObj(sq2) && (existsCPConnectionAuxO(sq1, sq2) || existsCPConnectionAuxO(sq2, sq1));
    }

    public static Set<String> getCPConnectionSubj(Set<Triple> sq1, Set<Triple> sq2) {

        Set<String> set = new HashSet<String>();
        if (sq1.size() == 0 || sq2.size() == 0) 
            return set;
        Node c = sq2.iterator().next().getSubject();
        for (Triple t : sq1) {
            Node oTmp = t.getObject();
            if (oTmp.equals(c)) {
                set.add("<"+t.getPredicate().getURI()+">");
            } 
        }
        return set;
    }

    public static Set<String> getCPConnectionSubjObj(Set<Triple> sq1, Set<Triple> sq2) {

        Set<String> set = new HashSet<String>();
        if (sq1.size() == 0 || sq2.size() == 0)
            return set;
        Node c = sq2.iterator().next().getObject();
        for (Triple t : sq1) {
            Node oTmp = t.getObject();
            if (oTmp.equals(c)) {
                set.add("<"+t.getPredicate().getURI()+">");
            }
        }
        c = sq1.iterator().next().getSubject();
        for (Triple t : sq2) {
            Node sTmp = t.getSubject();
            if (sTmp.equals(c)) {
                set.add("<"+t.getPredicate().getURI()+">");
            }
        }
        return set;
    }

    public static Set<String> getCPConnectionObj(Set<Triple> sq1, Set<Triple> sq2) {

        Set<String> set = new HashSet<String>();
        if (sq1.size() == 0 || sq2.size() == 0)
            return set;
        Node c = sq2.iterator().next().getObject();
        for (Triple t : sq1) {
            Node sTmp = t.getSubject();
            if (sTmp.equals(c)) {
                set.add("<"+t.getPredicate().getURI()+">");
            }
        }
        return set;
    } 

    public static boolean existsCPConnectionAuxS(Set<Triple> sq1, Set<Triple> sq2) {

        boolean e = false;
        Node c = sq2.iterator().next().getSubject();
        for (Triple t : sq1) {
            Node oTmp = t.getObject();
            e = e || (oTmp.equals(c));
        }
        return e;
    }

    public static boolean existsCPConnectionAuxO(Set<Triple> sq1, Set<Triple> sq2) {

        boolean e = false;
        Node c = sq2.iterator().next().getObject();
        for (Triple t : sq1) {
            Node sTmp = t.getSubject();
            e = e || (sTmp.equals(c));
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
        Iterator<Triple> it = sq.iterator(); 
        Node s = it.next().getSubject();
        while (it.hasNext()) {
            Triple t = it.next();
            Node sTmp = t.getSubject();
            e = e && (s.equals(sTmp));
        }
        return e;
    }

    public static boolean existsCSConnectionObj(Set<Triple> sq) {
        boolean e = true;
        Iterator<Triple> it = sq.iterator();
        Node o = it.next().getObject();
        while (it.hasNext()) {
            Triple t = it.next();
            Node oTmp = t.getObject();
            e = e && (o.equals(oTmp));
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

    public static boolean existsNonGeneralPredicate(Set<Triple> ts) {

        boolean exists = false;
        for (Triple t : ts) {
            Node p = t.getPredicate();
            if (!p.isVariable()) {                
                String pStr = p.getURI();
                exists = exists || !generalPredicates.contains(pStr);
            }
        }
        //System.out.println("exists non general predicate in "+ts+": "+exists);
        return exists;
    }
/*
    public static long computeCostTransfer(long card, HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable, HashMap<HashSet<Node>, Pair<HashSet<Node>, HashSet<Node>>> log, HashSet<Node> set) {

        return card + costTransfer(DPTable, log, set);
    }*/

    public static void estimateSelectivityCP(HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexSubj, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexObj, HashSet<Node> nodes, HashSet<Triple> triples, HashMap<Node, Vector<Tree<Pair<Integer,Triple>>>> map, HashMap<Pair<Integer, Triple>, HashMap<HashSet<Pair<Integer, Triple>>, Double>> selectivity, HashMap<HashSet<Node>, Pair<HashSet<Node>, HashSet<Node>>> log) {
        //long t0 = System.currentTimeMillis();
        HashMap<Triple, Triple> renamed = new HashMap<Triple, Triple>();
        HashSet<Triple> newTs = renameBack(new HashSet<Triple>(triples), map, renamed);
        //t0 = System.currentTimeMillis() - t0;
        //System.out.println("renaming: "+t0);
        Vector<HashSet<Node>> toRemove = new Vector<HashSet<Node>>();
        Set<HashSet<Node>> keySet = new HashSet<HashSet<Node>>(DPTable.keySet());
        for (HashSet<Node> set1 : keySet) {
            HashMap<Integer, Tree<Pair<Integer,Triple>>> plansSet1 = new HashMap<Integer, Tree<Pair<Integer,Triple>>>();
            Vector<Tree<Pair<Integer,Triple>>> value1 = DPTable.get(set1).getFirst();
            for (Tree<Pair<Integer,Triple>> tree : value1) {
                Integer source = tree.getOneElement().getFirst();
                plansSet1.put(source, tree);
            }
            //System.out.println("set1: "+set1+". value1: "+value1+". plansSet1: "+plansSet1);
            for (HashSet<Node> set2 : keySet) {
                if (set1.equals(set2)) {
                    continue;
                }
                HashMap<Integer, Tree<Pair<Integer,Triple>>> plansSet2 = new HashMap<Integer, Tree<Pair<Integer,Triple>>>();
                Vector<Tree<Pair<Integer,Triple>>> value2 = DPTable.get(set2).getFirst();
                for (Tree<Pair<Integer,Triple>> tree : value2) {
                    Integer source = tree.getOneElement().getFirst();
                    plansSet2.put(source, tree);
                }
                //System.out.println("set2: "+set2+". value2: "+value2+". plansSet2: "+plansSet2);

                Set<Triple> ts1 = new HashSet<Triple>();
                if (value1.size()>0) {
                    ts1.addAll(obtainTriples(value1.get(0)));
                }
                Set<Triple> ts2 = new HashSet<Triple>();
                if (value2.size()>0) {
                    ts2.addAll(obtainTriples(value2.get(0)));
                }

            boolean cS1 = (ts1.size() == 0) || centerIsSubject(ts1);
            boolean cO1 = (ts1.size() == 0) || centerIsObject(ts1);
            boolean cS2 = (ts2.size() == 0) || centerIsSubject(ts2);
            boolean cO2 = (ts2.size() == 0) || centerIsObject(ts2);
            boolean case1 = cS1 && cS2;
            boolean case2 = cS1 && cS2;
            boolean case3 = cO1 && cO2;
            boolean case4 = cO1 && cO2;
            boolean case5 = cS1 && cO2;
            //System.out.println("case1: "+case1+". case2: "+case2+". case3: "+case3+". case4: "+case4+". case5: "+case5);
            Set<Triple> ts3 = new HashSet<Triple>();
            Set<Triple> ts4 = new HashSet<Triple>();
            Set<Triple> ts5 = new HashSet<Triple>();
            Set<Triple> ts6 = new HashSet<Triple>();
            //boolean hasTs3 = false;
            //boolean hasTs4 = false;
            //boolean hasTs5 = false;
            //boolean hasTs6 = false;
            if (ts1.size()>0 && cS1) {
                ts3.addAll(ts1);
                //hasTs3 = true;
            }
            /*if (ts1.size()>0 && cO1) {   // REMOVING_OBJ_STATS
                ts4.addAll(ts1);
                //hasTs4 = true;
            }*/
            if (ts2.size()>0 && cS2) {
                ts5.addAll(ts2);
                //hasTs5 = true;
            }
            /*if (ts2.size()>0 && cO2) {   // REMOVING_OBJ_STATS
                ts6.addAll(ts2);
                //hasTs6 = true;
            }*/
            /*#for (Triple ta : triples) { // t1 has meta-nodes
                Triple tb = renamed.get(ta); // t2 is t1 without the meta-nodes
                if (ta.getSubject().equals(s)) {
                    ts3.add(tb);
                }
                if (ta.getObject().equals(s)) {
                    ts4.add(tb);
                }
                if (ta.getSubject().equals(o)) {
                    ts5.add(tb);
                }
                if (ta.getObject().equals(o)) {
                    ts6.add(tb);
                }
            }*/
            boolean added = false;
            //boolean addedTs3 = false;
            //boolean addedTs4 = false;
            //boolean addedTs5 = false;
            //boolean addedTs6 = false;
            //t0 = System.currentTimeMillis();
            Set<String> ps35 = null;
            Set<String> ps53 = null;
            if (ts3.size() > 0 && ts5.size()> 0) {
                
                ps35 = getCPConnectionSubj(ts3, ts5);
                ps53 = getCPConnectionSubj(ts5, ts3);
            }
            boolean eNGPTs3 = existsNonGeneralPredicate(ts3);
            boolean eNGPTs5 = existsNonGeneralPredicate(ts5);
            HashMap<String,Set<Triple>> ps3 = obtainPredicates(ts3);
            HashMap<String,Set<Triple>> ps5 = obtainPredicates(ts5); 
            HashMap<Integer,HashSet<Integer>> relevantCSTs3 = computeRelevantCS(ps3.keySet(), predicateIndexSubj);
            HashMap<Integer,HashSet<Integer>> relevantCSTs5 = computeRelevantCS(ps5.keySet(), predicateIndexSubj);

            Set<String> ps46 = null;
            Set<String> ps64 = null;
            /*if (ts4.size() > 0 && ts6.size()> 0) {  // REMOVING_OBJ_STATS
                ps46 = getCPConnectionObj(ts4, ts6);
                ps64 = getCPConnectionObj(ts6, ts4);
            }*/

            Set<String> ps36 = null;
            Set<String> ps63 = null;
            /*if (ts3.size() > 0 && ts6.size()> 0) {  // REMOVING_OBJ_STATS
                ps36 = getCPConnectionSubjObj(ts3, ts6);
                ps63 = getCPConnectionSubjObj(ts6, ts3);
            }*/

            /*boolean eNGPTs4 = existsNonGeneralPredicate(ts4);  // REMOVING_OBJ_STATS
            boolean eNGPTs6 = existsNonGeneralPredicate(ts6);
            HashMap<String,Set<Triple>> ps4 = obtainPredicates(ts4);
            HashMap<String,Set<Triple>> ps6 = obtainPredicates(ts6);
            HashMap<Integer,HashSet<Integer>> relevantCSTs4 = computeRelevantCS(ps4.keySet(), predicateIndexObj);
            HashMap<Integer,HashSet<Integer>> relevantCSTs6 = computeRelevantCS(ps6.keySet(), predicateIndexObj);*/
 
            if (ts3.size() > 0 && ts5.size() > 0 && case1 && ps35.size()>0 && (eNGPTs3 || eNGPTs5)) {
                HashMap<Integer,HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>> useful = new HashMap<Integer,HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>>();
                HashMap<Integer,HashMap<Integer, Long>> usefulCost = new HashMap<Integer,HashMap<Integer, Long>>();
                //HashMap<Integer,HashSet<Integer>> usefulTs3 = new HashMap<Integer,HashSet<Integer>>();
                //HashMap<Integer,HashSet<Integer>> usefulTs5 = new HashMap<Integer,HashSet<Integer>>();
                //System.out.println("there are "+ relevantCSTs3.keySet().size()+" relevant datasets for ts3");
                //System.out.println("there are "+ relevantCSTs5.keySet().size()+" relevant datasets for ts5");
                long card35 = getCardinalityCPSubj(ts3, ps3, ps35, ts5, ps5, relevantCSTs3, relevantCSTs5, useful, usefulCost);
                HashSet<Node> set = new HashSet<Node>(set1);
                set.addAll(set2);
                Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>> data = DPTable.get(set);
                long c = Long.MAX_VALUE;
                if (data != null) {
                    c = data.getSecond().getFirst();
                }
                //System.out.println("card35: "+card35);
                if (card35>0 && card35 < c) {
                    Pair<HashSet<Node>, HashSet<Node>> pair =  new Pair<HashSet<Node>, HashSet<Node>>(set1, set2);
                    Vector<Tree<Pair<Integer,Triple>>> plan = makeCPTreeSubj(ps3, ps5, useful, usefulCost, pair, selectivity, ps35, plansSet1, plansSet2);
                    //System.out.println("card35: "+card35);
                    //System.out.println(plan);
                    long cost = costTransfer(plan);
                    DPTable.put(set, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(plan, new Pair<Long, Long>(card35,cost)));  
                    log.put(set, pair);
                //Vector<Tree<Pair<Integer,Triple>>> p = getStarJoinOrderSubj(ts3, predicateIndexSubj);
                //Vector<Tree<Pair<Integer,Triple>>> q = getStarJoinOrderSubj(ts5, predicateIndexSubj);
                //if (p.size()>0 && q.size()>0) {
                    //boolean tmpAdded = addCheapestCPSubj(p, q, keyS, keyO, predicateIndexSubj, DPTable);
                   // added = added || tmpAdded;
                    added = true;
                    //addedTs5 = true;
                }
            }
            if (ts3.size() > 0 && ts5.size() > 0 && case2 && ps53.size()>0 && (eNGPTs3 || eNGPTs5)) {
                HashMap<Integer,HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>> useful = new HashMap<Integer,HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>>();
                HashMap<Integer,HashMap<Integer, Long>> usefulCost = new HashMap<Integer,HashMap<Integer, Long>>();
                //HashMap<Integer,HashSet<Integer>> usefulTs3 = new HashMap<Integer,HashSet<Integer>>();
                //HashMap<Integer,HashSet<Integer>> usefulTs5 = new HashMap<Integer,HashSet<Integer>>();
                long card53 = getCardinalityCPSubj(ts5, ps5, ps53, ts3, ps3, relevantCSTs5, relevantCSTs3, useful, usefulCost);
                HashSet<Node> set = new HashSet<Node>(set1);
                set.addAll(set2);
                Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>> data = DPTable.get(set);
                long c = Long.MAX_VALUE;
                if (data != null) {
                    c = data.getSecond().getFirst();
                }
                //System.out.println("card53: "+card53);
                if (card53>0 && card53 < c) {
                    Pair<HashSet<Node>, HashSet<Node>> pair =  new Pair<HashSet<Node>, HashSet<Node>>(set2, set1);
                    Vector<Tree<Pair<Integer,Triple>>> plan = makeCPTreeSubj(ps5, ps3, useful, usefulCost, pair, selectivity, ps53, plansSet2, plansSet1);
                    //System.out.println("card53: "+card53+". plan: "+plan);
                    long cost = costTransfer(plan);
                    DPTable.put(set, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(plan, new Pair<Long, Long>(card53,cost)));
                    log.put(set, pair);
                //Vector<Tree<Pair<Integer,Triple>>> p = getStarJoinOrderSubj(ts3, predicateIndexSubj);
                //Vector<Tree<Pair<Integer,Triple>>> q = getStarJoinOrderSubj(ts5, predicateIndexSubj);
                //if (p.size()>0 && q.size()>0) {
                    //boolean tmpAdded = addCheapestCPSubj(p, q, keyS, keyO, predicateIndexSubj, DPTable);
                   // added = added || tmpAdded;
                    //addedTs3 = true;
                    added = true;
                }
            }
/*  // REMOVING_OBJ_STATS
            if (ts4.size() > 0 && ts6.size() > 0 && case3 && ps46.size()>0 && (eNGPTs4 || eNGPTs6)) {
                HashMap<Integer,HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>> useful = new HashMap<Integer,HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>>();
                HashMap<Integer,HashMap<Integer, Long>> usefulCost = new HashMap<Integer,HashMap<Integer, Long>>();
                long card46 = getCardinalityCPObj(ts4, ps4, ps46, ts6, ps6, relevantCSTs4, relevantCSTs6, useful, usefulCost);
                HashSet<Node> set = new HashSet<Node>(set1);
                set.addAll(set2);
                Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>> data = DPTable.get(set);
                long c = Long.MAX_VALUE;
                if (data != null) {
                    c = data.getSecond().getFirst();
                }
                //System.out.println("card46: "+card46);
                if (card46>0 && card46 < c) {
                    Pair<HashSet<Node>, HashSet<Node>> pair =  new Pair<HashSet<Node>, HashSet<Node>>(set1, set2);
                    Vector<Tree<Pair<Integer,Triple>>> plan = makeCPTreeObj(ps4, ps6, useful, usefulCost, pair, selectivity, ps46, plansSet1, plansSet2);
                    //System.out.println("card46: "+card46);
                    //System.out.println(plan);
                    long cost = costTransfer(plan);
                    DPTable.put(set, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(plan, new Pair<Long, Long>(card46,cost)));
                    log.put(set, pair);
                    //addedTs4 = true;
                    added = true;
                }
            }
            if (ts4.size() > 0 && ts6.size() > 0  && case4 && ps64.size()>0 && (eNGPTs4 || eNGPTs6)) {
                HashMap<Integer,HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>> useful = new HashMap<Integer,HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>>();
                HashMap<Integer,HashMap<Integer, Long>> usefulCost = new HashMap<Integer,HashMap<Integer, Long>>();
                //System.out.println("ts6: "+ts6+". ts4: "+ts4);
                //System.out.println("relevantCSTs6: "+relevantCSTs6+". relevantCSTs4: "+relevantCSTs4);
                long card64 = getCardinalityCPObj(ts6, ps6, ps64, ts4, ps4, relevantCSTs6, relevantCSTs4, useful, usefulCost);
                HashSet<Node> set = new HashSet<Node>(set1);
                set.addAll(set2);
                Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>> data = DPTable.get(set);
                long c = Long.MAX_VALUE;
                if (data != null) {
                    c = data.getSecond().getFirst();
                }
                //System.out.println("card64: "+card64);
                if (card64>0 && card64 < c) {
                    Pair<HashSet<Node>, HashSet<Node>> pair =  new Pair<HashSet<Node>, HashSet<Node>>(set2, set1);
                    Vector<Tree<Pair<Integer,Triple>>> plan = makeCPTreeObj(ps6, ps4, useful, usefulCost, pair, selectivity, ps64, plansSet2, plansSet1);
                    //System.out.println("card64: "+card64);
                    //System.out.println(plan);
                    long cost = costTransfer(plan);
                    DPTable.put(set, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(plan, new Pair<Long, Long>(card64,cost)));
                    log.put(set, pair);
                    //addedTs4 = true;
                    added = true;
                }
            }

            if (ts3.size() > 0 && ts6.size() > 0 && case5 && ps36.size()>0 && (eNGPTs3 || eNGPTs6)) {
                //System.out.println("inside case 5");
                //System.out.println(ts3);
                //System.out.println(ts6);
                //System.out.println(ps36);
                HashMap<Integer,HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>> useful = new HashMap<Integer,HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>>();
                HashMap<Integer,HashMap<Integer, Long>> usefulCost = new HashMap<Integer,HashMap<Integer, Long>>();
                //long card36 = getCardinalityCPSubjObj(ts3, ps3, ps36, ts6, ps6, relevantCSTs3, relevantCSTs6, useful);
                //if (card36 == 0) {
                long card36 = 0L;
                    Set<Triple> aux = new HashSet(ts6);
                    ts6.removeAll(ts3);
                    HashMap<String,Set<Triple>> ps6Aux = new HashMap<String,Set<Triple>>();
                    for (String k : ps6.keySet()) {
                        ps6Aux.put(k, new HashSet<Triple>(ps6.get(k)));
                    }
                    ps6 = obtainPredicates(ts6);
                    HashMap<Integer,HashSet<Integer>> relevantCSTs6Aux = new HashMap<Integer,HashSet<Integer>>();
                    for (Integer k :  relevantCSTs6.keySet()) {
                        relevantCSTs6Aux.put(k, new HashSet<Integer>(relevantCSTs6.get(k)));
                    }
                    relevantCSTs6 = computeRelevantCS(ps6.keySet(), predicateIndexObj);
                    //System.out.println("ts6 after removing ts3 triples: "+ts6);
                    if (ts6.size() > 0) {
                      card36 = getCardinalityCPSubjObj(ts3, ps3, ps36, ts6, ps6, relevantCSTs3, relevantCSTs6, useful, usefulCost);
                    }
                    if (card36 == 0) {
                        ts6 = aux;
                        relevantCSTs6 = relevantCSTs6Aux;
                        ps6 = ps6Aux;
                        ts3.removeAll(ts6);
                        ps3 = obtainPredicates(ts3);
                        relevantCSTs3 = computeRelevantCS(ps3.keySet(), predicateIndexSubj);
                        //System.out.println("ts3 after removing ts6 triples: "+ts3);
                        if (ts3.size()>0) {
                            card36 = getCardinalityCPSubjObj(ts3, ps3, ps36, ts6, ps6, relevantCSTs3, relevantCSTs6, useful, usefulCost);                      
                        }
                    }
                //}
                HashSet<Node> set = new HashSet<Node>(set1);
                set.addAll(set2);
                Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>> data = DPTable.get(set);
                long c = Long.MAX_VALUE;
                if (data != null) {
                    c = data.getSecond().getFirst();
                }
                //System.out.println("card36: "+card36);
                if (card36>0 && card36 < c) {
                    Pair<HashSet<Node>, HashSet<Node>> pair =  new Pair<HashSet<Node>, HashSet<Node>>(set1, set2);
                    Vector<Tree<Pair<Integer,Triple>>> plan = makeCPTreeSubjObj(ps3, ps6, useful, usefulCost, pair, selectivity, ps36, plansSet1, plansSet2);
                    //System.out.println("card36: "+card36);
                    //System.out.println(plan);
                    long cost = costTransfer(plan);
                    DPTable.put(set, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(plan, new Pair<Long, Long>(card36,cost)));
                    log.put(set, pair);
                    //addedTs3 = true;
                    added = true;
                }
            }*/

/*
            if (ts3.size() > 0 && ts6.size() > 0 && ps63.size()>0 && (eNGPTs3 || eNGPTs6)) {
                HashMap<Integer,HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>> useful = new HashMap<Integer,HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>>();
                long card63 = getCardinalityCPSubjObj(ts6, ps6, ps63, ts3, ps3, relevantCSTs6, relevantCSTs3, useful);
                HashSet<Node> set = new HashSet<Node>(keyS);
                set.addAll(keyO);
                Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>> data = DPTable.get(set);
                long c = Long.MAX_VALUE;
                if (data != null) {
                    c = data.getSecond().getFirst();
                }
                if (card63>0 && card63 < c) {
                    Vector<Tree<Pair<Integer,Triple>>> plan = makeCPTreeSubjObj(ps6, ps3, useful, predicateIndexSubj, predicateIndexObj);
                    DPTable.put(set, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(plan, new Pair<Long, Long>(card63,card63)));
                    added = true;
                }
            }*/
            //t0 = System.currentTimeMillis()-t0;
            //System.out.println("addCheapestCPSubj: "+t0);*/
            //t0 = System.currentTimeMillis();
            /*if (ts3.size() > 0 && ts5.size() > 0 && centerIsSubject(ts3) && centerIsSubject(ts5) && existsCPConnectionSubj(ts3, ts5) && (existsNonGeneralPredicate(ts3) || existsNonGeneralPredicate(ts5))) {
                //t0 = System.currentTimeMillis();
                Vector<Tree<Pair<Integer,Triple>>> p = getStarJoinOrderSubj(ts3, predicateIndexSubj);
                //t0 = System.currentTimeMillis()-t0;
                //System.out.println("first star: "+ts3+" : "+t0);
                //t0 = System.currentTimeMillis();
                Vector<Tree<Pair<Integer,Triple>>> q = getStarJoinOrderSubj(ts5, predicateIndexSubj);
                //t0 = System.currentTimeMillis()-t0;
                //System.out.println("second star: "+ts5+" : "+t0);
                //t0 = System.currentTimeMillis();
                if (p.size()>0 && q.size()>0) {
                    boolean tmpAdded = addCheapestCPSubj(p, q, keyS, keyO, predicateIndexSubj, DPTable);
                    added = added || tmpAdded;
                }
                //t0 = System.currentTimeMillis()-t0;
                //System.out.println("cp: "+t0);
            }*/
            //t0 = System.currentTimeMillis()-t0;
            //System.out.println("addCheapestCPSubj: "+t0);
            //t0 = System.currentTimeMillis();
            /*if (ts4.size() > 0 && ts6.size() > 0 && centerIsObject(ts4) && centerIsObject(ts6) && existsCPConnectionObj(ts4, ts6) && (existsNonGeneralPredicate(ts4) || existsNonGeneralPredicate(ts6))) {
                Vector<Tree<Pair<Integer,Triple>>> p = getStarJoinOrderObj(ts4, predicateIndexObj);
                Vector<Tree<Pair<Integer,Triple>>> q = getStarJoinOrderObj(ts6, predicateIndexObj);
                if (p.size()>0 && q.size()>0) {
                    boolean tmpAdded = addCheapestCPObj(p, q, keyS, keyO, predicateIndexObj, DPTable);
                    added = added || tmpAdded;
                }
            }*/
            //t0 = System.currentTimeMillis()-t0;
            //System.out.println("addCheapestCPObj: "+t0);
            //t0 = System.currentTimeMillis();
            /*if (ts3.size() > 0 && ts6.size() > 0 && centerIsSubject(ts3) && centerIsObject(ts6) && existsCPConnectionSubjObj(ts3, ts6) && (existsNonGeneralPredicate(ts3) || existsNonGeneralPredicate(ts6))) {
                HashSet<Triple> aux = new HashSet<Triple>(ts6);
                aux.removeAll(ts3); // Linking triples are in both characteristic sets, they should appear only in one
                Vector<Tree<Pair<Integer,Triple>>> p = getStarJoinOrderSubj(ts3, predicateIndexSubj);
                Vector<Tree<Pair<Integer,Triple>>> q = new Vector<Tree<Pair<Integer,Triple>>>();
                if (aux.size()>0) {
                    q = getStarJoinOrderObj(aux, predicateIndexObj);
                }
                if (p.size()>0 && q.size()>0) {
                    boolean tmpAdded = addCheapestCPSubjObj(p, q, keyS, keyO, predicateIndexSubj, predicateIndexObj, DPTable);
                    added = added || tmpAdded;
                }
            }*/
            //t0 = System.currentTimeMillis()-t0;
            //System.out.println("addCheapestCPSubjObj: "+t0);
            /*if ((addedTs3 || addedTs4) && (addedTs5 || addedTs6) && (!hasTs3 || addedTs3) && (!hasTs4 || addedTs4) && (!hasTs5 || addedTs5) && (!hasTs6 || addedTs6)) {
                toRemove.add(keyS);
                toRemove.add(keyO);
                addSelectivity(keyS, keyO, DPTable, selectivity);
            }*/
/*
            if (added) {
                //#toRemove.add(keyS);
                //#toRemove.add(keyO);
                addSelectivity(set1, set2, DPTable, selectivity);
            }*/
        }
        }
        for (HashSet<Node> s : toRemove) {
            DPTable.remove(s);
        }
    }

    public static long getCardinalityCPSubj(Set<Triple> sq1, HashMap<String, Set<Triple>> map1, Set<String> ps12, Set<Triple> sq2, HashMap<String, Set<Triple>> map2, HashMap<Integer,HashSet<Integer>> relevantCSTs1, HashMap<Integer,HashSet<Integer>> relevantCSTs2, HashMap<Integer,HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>> useful, HashMap<Integer,HashMap<Integer, Long>> usefulCost) {

        //double selCttes = getConstantSelectivity(sq1, ds1, sq2, ds2);
        Set<String> ps1 = map1.keySet();
        Set<String> ps2 = map2.keySet();

        long c = 0L;
        for (Integer ds1 : relevantCSTs1.keySet()) {
            Set<Integer> css1 = relevantCSTs1.get(ds1);
            for (Integer ds2 : relevantCSTs2.keySet()) {
                double selCttes = getConstantSelectivity(sq1, ds1, sq2, ds2);
                long card = 0L;
                boolean added = false;
                for (String p : ps12) {
                    //System.out.println("p: "+p);
                    HashMap<Integer, HashMap<Integer, Integer>> cs1cs2Count = getCPSSubj(ds1, ds2).get(p);
                    if (cs1cs2Count == null) {
                        //System.out.println("no information for p in cps");
                        continue;
                    }
                    Set<Integer> css2 = relevantCSTs2.get(ds2);
                    for (Integer cs1 : css1) {
                        HashMap<Integer, Integer> cs2Count = cs1cs2Count.get(cs1);
                        if (cs2Count == null) {
                            //System.out.println("no information for cs1 in cps(p)");
                            continue;
                        }
                        for (Integer cs2 : css2) {
                            Integer count = cs2Count.get(cs2);
                            if (count == null) {
                                //System.out.println("no information for cs2 in cps(p)(cs1)");
                                continue;
                            }

                            Pair<Integer, HashMap<String, Pair<Integer, Integer>>> countPsCountNum1 = getCSSSubj(ds1).get(cs1);
                            Pair<Integer, HashMap<String, Pair<Integer, Integer>>> countPsCountNum2 = getCSSSubj(ds2).get(cs2);
                            //System.out.println("count: "+count);
                            Long m1 = getMultiplicitySubjS(count, p, countPsCountNum1, ps1, map1, countPsCountNum2, ps2, map2);
                            //System.out.println("m1: "+m1);
                            if (m1 > 0) {
                                card += m1;
                                HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>> aux1 = useful.get(ds1);
                                //HashMap<Integer, Long> aux2 = usefulCost.get(ds1);
                                if (aux1 == null) {
                                    aux1 = new HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>();
                                    //aux2 = new HashMap<Integer, Long>();
                                }
                                Pair<HashSet<Integer>, HashSet<Integer>> pair = aux1.get(ds2);
                                //Long costAc = aux2.get(ds2);
                                HashSet<Integer> s1, s2;
                                if (pair == null) {
                                    s1 = new HashSet<Integer>();
                                    s2 = new HashSet<Integer>();
                                    //costAc = 0L;
                                } else {
                                    s1 = pair.getFirst();
                                    s2 = pair.getSecond();
                                } 
                                s1.add(cs1);
                                s2.add(cs2);
                                pair = new Pair<HashSet<Integer>, HashSet<Integer>>(s1, s2);
                                //costAc += m1;
                                aux1.put(ds2, pair);
                                //aux2.put(ds2, costAc);
                                useful.put(ds1, aux1);
                                added = true;
                                //usefulCost.put(ds1, aux2);
                            }
                        }
                    }
                }
/*                if (!added) {
                    HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>> aux1 = useful.get(ds1);
                    if (aux1 == null) {
                        aux1 = new HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>();
                    }
                    aux1.put(ds2, null);
                    useful.put(ds1, aux1);
                }*/
                long tmpCost = Math.round(Math.ceil(card*selCttes));
                if (tmpCost>0) {
                    HashMap<Integer, Long> aux2 = usefulCost.get(ds1);
                    if (aux2 == null) {
                        aux2 = new HashMap<Integer, Long>();
                    }
                    aux2.put(ds2, tmpCost);
                    usefulCost.put(ds1, aux2);
                } else {
                    HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>> aux1 = useful.get(ds1);
                    if (aux1 == null) {
                        aux1 = new HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>();
                    }
                    aux1.put(ds2, null);
                    useful.put(ds1, aux1);
                } 
		c += tmpCost;
            }
        }
        return c;
    }        

    public static Vector<Tree<Pair<Integer,Triple>>> makeCPTreeSubj(HashMap<String, Set<Triple>> ps1, HashMap<String, Set<Triple>> ps2, HashMap<Integer,HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>> useful, HashMap<Integer,HashMap<Integer, Long>> usefulCost, Pair<HashSet<Node>, HashSet<Node>> pairKey, HashMap<Pair<Integer, Triple>, HashMap<HashSet<Pair<Integer,Triple>>, Double>> selectivity, Set<String> linkPs, HashMap<Integer, Tree<Pair<Integer,Triple>>> plansSet1, HashMap<Integer, Tree<Pair<Integer,Triple>>> plansSet2) {
	    Vector<Tree<Pair<Integer,Triple>>> res = new Vector<Tree<Pair<Integer,Triple>>>();
	    for (Integer ds1 : useful.keySet()) {
		    HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>> ds2Pair = useful.get(ds1);
		    HashMap<Integer, Long> ds2Card = usefulCost.get(ds1);
                    Tree<Pair<Integer,Triple>> plan1 = plansSet1.get(ds1);
                    if (plan1 == null) {
                        continue;
                    }
		    //HashSet<Integer> relevantCss1 = computeRelevantCS(ps1.keySet(), ds1, predicateIndex);
		    //long c1 = computeCostS(ds1, getCSSSubj(ds1), relevantCss1, ps1.keySet(), ps1, true);
		    //System.out.println("ds1: "+ds1);
		    for (Integer ds2 : ds2Pair.keySet()) {
                            Pair<HashSet<Node>, HashSet<Node>> pairKeyAux = pairKey;
			    //System.out.println("ds2: "+ds2);
			    Long cardCP = (ds2Card != null) ? ds2Card.get(ds2) : null;
			    //Tree<Pair<Integer,Triple>> plan1 = plansSet1.get(ds1);
			    //cssCostTreeSubj(plan1, predicateIndex);
			    Tree<Pair<Integer,Triple>> plan2 = plansSet2.get(ds2);
                            if (plan2 == null) {
                                continue;
                            }
			    //cssCostTreeSubj(plan2, predicateIndex);
			    //HashSet<Integer> relevantCss2 = computeRelevantCS(ps2.keySet(), ds2, predicateIndex);
                //long c1 = computeCost(ds1, getCSSSubj(ds1), s1, ps1.keySet(), ps1, true);
                //long c2 = computeCostS(ds2, getCSSSubj(ds2), relevantCss2, ps2.keySet(), ps2, true);
                //System.out.println("plan1: "+plan1+". plan2: "+plan2);
                long c1 = plan1.getCard();
                long c2 = plan2.getCard();
                //System.out.println("plan1: "+plan1+". plan2: "+plan2);
                //System.out.println("c1: "+c1+". c2: "+c2);
                Tree<Pair<Integer,Triple>> nT = null;
                Integer ds1Aux = ds1;
                Integer ds2Aux = ds2;
                HashSet<Pair<Integer, Triple>> ts = new HashSet<Pair<Integer, Triple>>(plan2.getElements());
                Tree<Pair<Integer,Triple>> plan1Aux = plan1;
                Tree<Pair<Integer,Triple>> plan2Aux = plan2;
                if (c1 > c2) {
                    pairKeyAux = new Pair<HashSet<Node>, HashSet<Node>>(pairKey.getSecond(), (pairKey.getFirst())); 
                    //Tree<Pair<Integer,Triple>> aux = plan2;
                    plan2Aux = plan1;
                    plan1Aux = plan2;
                    //HashSet<Node> tmp = pairKey.getSecond();
                    //pairKey.setSecond(pairKey.getFirst());
                    //pairKey.setFirst(tmp);
                    //Integer aux2 = ds2Aux;
                    ds2Aux = ds1;
                    ds1Aux = ds2;
                    //nT = new Branch<Pair<Integer,Triple>>(plan1, plan2);
                } /*else {
                    nT = new Branch<Pair<Integer,Triple>>(plan2, plan1);
                }   */     
                if (cardCP != null && cardCP > 0) {
                    nT = makeTree(ds1Aux, plan1Aux, ds2Aux, plan2Aux);
                }
                //System.out.println("nT: "+nT);
                double sel = 0.0;
                if (nT != null) {
                    nT.setCard(cardCP);
                    sel = ((double) cardCP) / (c1*c2);
                    res.add(nT);
                } else {
                    sel = 0.0;
                }
                    for (String p : linkPs) {
                        for (Triple t : ps1.get(p)) {
                            Pair<Integer, Triple> pair1 = new Pair<Integer, Triple>(ds1, t);
                            HashMap<HashSet<Pair<Integer, Triple>>, Double> tsSel = selectivity.get(pair1);
                            if (tsSel == null) {
                                tsSel = new HashMap<HashSet<Pair<Integer, Triple>>, Double>();
                            }
                            //double sel = ((double) cardCP) / (c1*c2);
                            //System.out.println("Including selectivity for "+pair1+" and "+ts+" : "+sel);
                            tsSel.put(ts, sel);
                            selectivity.put(pair1, tsSel);
                        }
                    }
                    
               
            }
        }
        return res;
    }

    public static long getCardinalityCPObj(Set<Triple> sq1, HashMap<String, Set<Triple>> map1, Set<String> ps12, Set<Triple> sq2, HashMap<String, Set<Triple>> map2, HashMap<Integer,HashSet<Integer>> relevantCSTs1, HashMap<Integer,HashSet<Integer>> relevantCSTs2, HashMap<Integer,HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>> useful, HashMap<Integer,HashMap<Integer, Long>> usefulCost) {

        Set<String> ps1 = map1.keySet();
        Set<String> ps2 = map2.keySet();

        long c = 0L;
        for (Integer ds1 : relevantCSTs1.keySet()) {
            //System.out.println("ds1: "+ds1);
            Set<Integer> css1 = relevantCSTs1.get(ds1);
            for (Integer ds2 : relevantCSTs2.keySet()) {
                //System.out.println("ds2: "+ds2);
                double selCttes = getConstantSelectivity(sq1, ds1, sq2, ds2);
                long card = 0L;
                boolean added = false;
                for (String p : ps12) {
                    //System.out.println("p: "+p);
                    HashMap<Integer, HashMap<Integer, Integer>> cs1cs2Count = getCPSObj(ds1, ds2).get(p);
                    if (cs1cs2Count == null) {
                        //System.out.println("no information for p in cps");
                        continue;
                    }
                    Set<Integer> css2 = relevantCSTs2.get(ds2);
                    for (Integer cs1 : css1) {
                        HashMap<Integer, Integer> cs2Count = cs1cs2Count.get(cs1);
                        if (cs2Count == null) {
                            //System.out.println("no information for cs1 in cps(p)");
                            continue;
                        }
                        for (Integer cs2 : css2) {
                            Integer count = cs2Count.get(cs2);
                            if (count == null) {
                                //System.out.println("no information for cs2 in cps(p)(cs1)");
                                continue;
                            }
                            //System.out.println("count: "+count);
                            Pair<Integer, HashMap<String, Pair<Integer, Integer>>> countPsCountNum1 = getCSSObj(ds1).get(cs1);
                            Pair<Integer, HashMap<String, Pair<Integer, Integer>>> countPsCountNum2 = getCSSObj(ds2).get(cs2);

                            Long m1 = getMultiplicityObjS(count, p, countPsCountNum1, ps1, map1, countPsCountNum2, ps2, map2);
                            //System.out.println("m1: "+m1);
                            if (m1 > 0) {
                                card += m1;
                                HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>> aux1 = useful.get(ds1);
                                if (aux1 == null) {
                                    aux1 = new HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>();
                                }
                                Pair<HashSet<Integer>, HashSet<Integer>> pair = aux1.get(ds2);
                                HashSet<Integer> s1, s2;
                                if (pair == null) {
                                    s1 = new HashSet<Integer>();
                                    s2 = new HashSet<Integer>();
                                } else {
                                    s1 = pair.getFirst();
                                    s2 = pair.getSecond();
                                }
                                s1.add(cs1);
                                s2.add(cs2);
                                pair = new Pair<HashSet<Integer>, HashSet<Integer>>(s1, s2);
                                aux1.put(ds2, pair);
                                useful.put(ds1, aux1);
                                added = true;
                            }
                        }
                    }
                }
/*                if (!added) {
                    HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>> aux1 = useful.get(ds1);
                    if (aux1 == null) {
                        aux1 = new HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>();
                    }
                    aux1.put(ds2, null);
                    useful.put(ds1, aux1);
                }*/
                long tmpCost = Math.round(Math.ceil(card*selCttes));
                if (tmpCost > 0) {
                    HashMap<Integer, Long> aux2 = usefulCost.get(ds1);
                    if (aux2 == null) {
                        aux2 = new HashMap<Integer, Long>();
                    }
                    aux2.put(ds2, tmpCost);
                    usefulCost.put(ds1, aux2);
                } else {
                     HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>> aux1 = useful.get(ds1);
                    if (aux1 == null) {
                        aux1 = new HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>();
                    }
                    aux1.put(ds2, null);
                    useful.put(ds1, aux1);
                }
                c += tmpCost;
            }
        }
        return c;
    }

    public static long getCardinalityCPSubjObj(Set<Triple> sq1, HashMap<String, Set<Triple>> map1, Set<String> ps12, Set<Triple> sq2, HashMap<String, Set<Triple>> map2, HashMap<Integer,HashSet<Integer>> relevantCSTs1, HashMap<Integer,HashSet<Integer>> relevantCSTs2, HashMap<Integer,HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>> useful, HashMap<Integer,HashMap<Integer, Long>> usefulCost) {

        Set<String> ps1 = map1.keySet();
        Set<String> ps2 = map2.keySet();

        long c = 0L;
        for (Integer ds1 : relevantCSTs1.keySet()) {
            //System.out.println("ds1: "+ds1);
            Set<Integer> css1 = relevantCSTs1.get(ds1);
            for (Integer ds2 : relevantCSTs2.keySet()) {
                //System.out.println("ds2: "+ds2);
                double selCttes = getConstantSelectivity(sq1, ds1, sq2, ds2);
                long card = 0L;
                boolean added = false;
                for (String p : ps12) {
                    //System.out.println("p: "+p);
                    HashMap<Integer, HashMap<Integer, Integer>> cs1cs2Count = getCPSSubjObj(ds1, ds2).get(p);
                    if (cs1cs2Count == null) {
                        //System.out.println("no information for p in cps");
                        continue;
                    }
                    Set<Integer> css2 = relevantCSTs2.get(ds2);
                    for (Integer cs1 : css1) {
                        HashMap<Integer, Integer> cs2Count = cs1cs2Count.get(cs1);
                        if (cs2Count == null) {
                            //System.out.println("no information for cs1 in cps(p)");
                            continue;
                        }
                        for (Integer cs2 : css2) {
                            Integer count = cs2Count.get(cs2);
                            if (count == null) {
                                //System.out.println("no information for cs2 in cps(p)(cs1)");
                                continue;
                            }
                            //System.out.println("count: "+count);
                            Pair<Integer, HashMap<String, Pair<Integer, Integer>>> countPsCountNum1 = getCSSSubj(ds1).get(cs1);
                            Pair<Integer, HashMap<String, Pair<Integer, Integer>>> countPsCountNum2 = getCSSObj(ds2).get(cs2);

                            Long m1 = getMultiplicitySubjObjS(count, p, countPsCountNum1, ps1, map1, countPsCountNum2, ps2, map2);
                            //System.out.println("m1: "+m1);
                            if (m1 > 0) {
                                card += m1;
                                HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>> aux1 = useful.get(ds1);
                                if (aux1 == null) {
                                    aux1 = new HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>();
                                }
                                Pair<HashSet<Integer>, HashSet<Integer>> pair = aux1.get(ds2);
                                HashSet<Integer> s1, s2;
                                if (pair == null) {
                                    s1 = new HashSet<Integer>();
                                    s2 = new HashSet<Integer>();
                                } else {
                                    s1 = pair.getFirst();
                                    s2 = pair.getSecond();
                                }
                                s1.add(cs1);
                                s2.add(cs2);
                                pair = new Pair<HashSet<Integer>, HashSet<Integer>>(s1, s2);
                                aux1.put(ds2, pair);
                                useful.put(ds1, aux1);
                                added = true;
                            }
                        }
                    }
                }
                /*if (!added) {
                    HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>> aux1 = useful.get(ds1);
                    if (aux1 == null) {
                        aux1 = new HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>();
                    }
                    aux1.put(ds2, null);
                    useful.put(ds1, aux1);
                }*/
                long tmpCost = Math.round(Math.ceil(card*selCttes));
                if (tmpCost > 0) {
                    HashMap<Integer, Long> aux2 = usefulCost.get(ds1);
                    if (aux2 == null) {
                        aux2 = new HashMap<Integer, Long>();
                    }
                    aux2.put(ds2, tmpCost);
                    usefulCost.put(ds1, aux2);
                } else {
                    HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>> aux1 = useful.get(ds1);
                    if (aux1 == null) {
                        aux1 = new HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>();
                    }
                    aux1.put(ds2, null);
                    useful.put(ds1, aux1);
                }
                c += tmpCost;
            }
        }
        return c;
    }


    public static Integer getStartCS(Set<String> ps, HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> css, HashMap<String, HashSet<Integer>> predicateIndex, HashMap<Integer, Integer> hc, HashMap<Integer, Set<String>> additionalSets, HashMap<Integer, Integer> cost) {

        Integer key = produceStarJoinOrdering.getIKey(ps);
        //System.out.println("key: "+key);
        if (hc.containsKey(key)) {
            return key;
        }
        int c = produceStarJoinOrdering.computeCost(ps, css, predicateIndex);
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

    public static void completeHC(HashMap<Integer, Integer> costTmp, HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> css, HashMap<Integer, Integer> hc, HashMap<Integer, Set<String>> additionalSets, HashMap<Integer, Integer> cost, HashMap<String, HashSet<Integer>> predicateIndex) {

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
                    Integer key = produceStarJoinOrdering.getIKey(psAux);
                    Integer c = -1;
                    if (cost.containsKey(key)) {
                        c = cost.get(key);
                    } else if (costTmp.containsKey(key)) {
                        c = costTmp.get(key);
                    } else if (costAux.containsKey(key)) {
                        c = costAux.get(key);
                    } else {
                        c = produceStarJoinOrdering.computeCost(psAux, css, predicateIndex);
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

    public static Tree<Pair<Integer,Triple>> makeCSTreeSubj(HashMap<String, Set<Triple>> ps, Integer ds, HashSet<Integer> set, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) {

        HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> css = getCSSSubj(ds);
        HashMap<Integer, Integer> hc = getHCSubj(ds);
        HashMap<Integer, Set<String>> additionalSets = getAdditionalSetsSubj(ds);
        HashMap<Integer, Integer> cost = getCostSubj(ds);
        HashMap<String, HashSet<Integer>> predicateIndexDS = getPredicateIndexSubj(ds, predicateIndex);

        LinkedList<String> order = new LinkedList<String>();
        Integer s = getStartCS(ps.keySet(), css, predicateIndexDS, hc, additionalSets, cost); //produceStarJoinOrdering.getIKey(ps.keySet());
        //System.out.println("start cs: "+s);
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
            if (ps.containsKey(p)) {
                order.addFirst(p);
            }
            s = cheapestSS;
            sPreds = cPreds;
        }
        if ((sPreds != null) && sPreds.size()>0) {
            String p = sPreds.iterator().next();
            if (ps.containsKey(p)) {
                order.addFirst(p);
            }
        }
        //System.out.println("star order: ");
        //System.out.println(order);
        Tree<Pair<Integer,Triple>> sortedStar = convertToTreeS(order, ps, ds, predicateIndex, true);
        return sortedStar;
    }

    public static Vector<Tree<Pair<Integer,Triple>>> makeCPTreeObj(HashMap<String, Set<Triple>> ps1, HashMap<String, Set<Triple>> ps2, HashMap<Integer,HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>> useful, HashMap<Integer,HashMap<Integer, Long>> usefulCost, Pair<HashSet<Node>, HashSet<Node>> pairKey, HashMap<Pair<Integer, Triple>, HashMap<HashSet<Pair<Integer,Triple>>, Double>> selectivity, Set<String> linkPs, HashMap<Integer, Tree<Pair<Integer,Triple>>> plansSet1, HashMap<Integer, Tree<Pair<Integer,Triple>>> plansSet2) {
        Vector<Tree<Pair<Integer,Triple>>> res = new Vector<Tree<Pair<Integer,Triple>>>();
        for (Integer ds1 : useful.keySet()) {
            HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>> ds2Pair = useful.get(ds1);
            HashMap<Integer, Long> ds2Card = usefulCost.get(ds1);
            Tree<Pair<Integer,Triple>> plan1 = plansSet1.get(ds1);
            if (plan1 == null) {
                continue;
            }
            //HashSet<Integer> relevantCss1 = computeRelevantCS(ps1.keySet(), ds1, predicateIndex);
            //long c1 = computeCostS(ds1, getCSSObj(ds1), relevantCss1, ps1.keySet(), ps1, false);
            for (Integer ds2 : ds2Pair.keySet()) {
                Long cardCP = (ds2Card != null) ? ds2Card.get(ds2) : null;
                //Tree<Pair<Integer,Triple>> plan1 = plansSet1.get(ds1);
                //cssCostTreeObj(plan1, predicateIndex);
                Tree<Pair<Integer,Triple>> plan2 = plansSet2.get(ds2);
                if (plan2 == null) {
                    continue;
                }
                //cssCostTreeObj(plan2, predicateIndex);
                //HashSet<Integer> relevantCss2 = computeRelevantCS(ps2.keySet(), ds2, predicateIndex);
                //long c2 = computeCostS(ds2, getCSSObj(ds2), relevantCss2, ps2.keySet(), ps2, false);
                long c1 = plan1.getCard();
                long c2 = plan2.getCard();
                //System.out.println("plan1: "+plan1+". plan2: "+plan2);
                //System.out.println("c1: "+c1+". c2: "+c2);
                Tree<Pair<Integer,Triple>> nT = null;
                Integer ds1Aux = ds1;
                Integer ds2Aux = ds2;
                HashSet<Pair<Integer, Triple>> ts = new HashSet<Pair<Integer, Triple>>(plan2.getElements());
                if (c1 > c2) {
                    Tree<Pair<Integer,Triple>> aux = plan2;
                    plan2 = plan1;
                    plan1 = aux;
                    HashSet<Node> tmp = pairKey.getSecond();
                    pairKey.setSecond(pairKey.getFirst());
                    pairKey.setFirst(tmp);
                    Integer aux2 = ds2Aux;
                    ds2Aux = ds1Aux;
                    ds1Aux = aux2;
                }
                if (cardCP != null && cardCP > 0) {
                    nT = makeTree(ds1Aux, plan1, ds2Aux, plan2);
                }
                /*if (c1 <= c2) {
                    nT = new Branch<Pair<Integer,Triple>>(plan1, plan2);
                } else {
                    nT = new Branch<Pair<Integer,Triple>>(plan2, plan1);
                }*/
                double sel = 0.0;
                if (nT != null) {
                    nT.setCard(cardCP);
                    sel = ((double) cardCP) / (c1*c2);
                    res.add(nT);
                } else {
                    sel = 0.0;
                }
                    for (String p : linkPs) {
                        for (Triple t : ps1.get(p)) {
                            Pair<Integer, Triple> pair1 = new Pair<Integer, Triple>(ds1, t);
                            HashMap<HashSet<Pair<Integer, Triple>>, Double> tsSel = selectivity.get(pair1);
                            if (tsSel == null) {
                                tsSel = new HashMap<HashSet<Pair<Integer, Triple>>, Double>();
                            }
                            //System.out.println("Including selectivity for "+pair1+" and "+ts+" : "+sel);
                            //double sel = ((double) cardCP) / (c1*c2);
                            tsSel.put(ts, sel);
                            selectivity.put(pair1, tsSel);
                        }
                    }
                    
                
            }
        }
        return res;
    }

    public static Vector<Tree<Pair<Integer,Triple>>> makeCPTreeSubjObj(HashMap<String, Set<Triple>> ps1, HashMap<String, Set<Triple>> ps2, HashMap<Integer,HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>> useful, HashMap<Integer,HashMap<Integer, Long>> usefulCost, Pair<HashSet<Node>, HashSet<Node>> pairKey, HashMap<Pair<Integer, Triple>, HashMap<HashSet<Pair<Integer,Triple>>, Double>> selectivity, Set<String> linkPs, HashMap<Integer, Tree<Pair<Integer,Triple>>> plansSet1, HashMap<Integer, Tree<Pair<Integer,Triple>>> plansSet2) {
        Vector<Tree<Pair<Integer,Triple>>> res = new Vector<Tree<Pair<Integer,Triple>>>();
        for (Integer ds1 : useful.keySet()) {
            HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>> ds2Pair = useful.get(ds1);
            HashMap<Integer, Long> ds2Card = usefulCost.get(ds1);
            Tree<Pair<Integer,Triple>> plan1 = plansSet1.get(ds1);
            if (plan1 == null) {
                continue;
            }
            //HashSet<Integer> relevantCss1 = computeRelevantCS(ps1.keySet(), ds1, predicateIndexSubj);
            //long c1 = computeCostS(ds1, getCSSSubj(ds1), relevantCss1, ps1.keySet(), ps1, true);
            for (Integer ds2 : ds2Pair.keySet()) {
                Long cardCP = (ds2Card != null) ? ds2Card.get(ds2) : null;
                //Tree<Pair<Integer,Triple>> plan1 = plansSet1.get(ds1);
                //cssCostTreeSubj(plan1, predicateIndexSubj);
                Tree<Pair<Integer,Triple>> plan2 = plansSet2.get(ds2);
                if (plan2 == null) {
                    continue;
                }
                //cssCostTreeObj(plan2, predicateIndexObj);
                //HashSet<Integer> relevantCss2 = computeRelevantCS(ps2.keySet(), ds2, predicateIndexObj);
                //long c2 = computeCostS(ds2, getCSSObj(ds2), relevantCss2, ps2.keySet(), ps2, false);
                long c1 = plan1.getCard();
                long c2 = plan2.getCard();
                //System.out.println("plan1 : "+plan1+". plan2: "+plan2+". c1: "+c1+". c2: "+c2);
                //System.out.println("plan1: "+plan1+". plan2: "+plan2);
                //System.out.println("c1: "+c1+". c2: "+c2);
                Tree<Pair<Integer,Triple>> nT = null;
                Integer ds1Aux = ds1;
                Integer ds2Aux = ds2;
                HashSet<Pair<Integer, Triple>> ts = new HashSet<Pair<Integer, Triple>>(plan2.getElements());
                if (c1 > c2) {
                    Tree<Pair<Integer,Triple>> aux = plan2;
                    plan2 = plan1;
                    plan1 = aux;
                    HashSet<Node> tmp = pairKey.getSecond();
                    pairKey.setSecond(pairKey.getFirst());
                    pairKey.setFirst(tmp);
                    Integer aux2 = ds2Aux;
                    ds2Aux = ds1Aux;
                    ds1Aux = aux2;
                }
                if (cardCP != null && cardCP > 0) {
                    nT = makeTree(ds1Aux, plan1, ds2Aux, plan2);
                }
                /*if (c1 <= c2) {
                    nT = new Branch<Pair<Integer,Triple>>(plan1, plan2);
                } else {
                    nT = new Branch<Pair<Integer,Triple>>(plan2, plan1);
                }*/
                double sel = 0.0;
                if (nT != null) {
                    nT.setCard(cardCP);
                    sel = ((double) cardCP) / (c1*c2);
                    res.add(nT);
                } else {
                    sel = 0.0;
                }
                    for (String p : linkPs) {
                        for (Triple t : ps1.get(p)) {
                            Pair<Integer, Triple> pair1 = new Pair<Integer, Triple>(ds1, t);
                            HashMap<HashSet<Pair<Integer, Triple>>, Double> tsSel = selectivity.get(pair1);
                            if (tsSel == null) {
                                tsSel = new HashMap<HashSet<Pair<Integer, Triple>>, Double>();
                            }
                            //double sel = ((double) cardCP) / (c1*c2);
                            //System.out.println("Including selectivity for "+pair1+" and "+ts+" : "+sel);
                            tsSel.put(ts, sel);
                            selectivity.put(pair1, tsSel);
                        }
                    }
                     
                
            }
        }
        return res;
    }

    public static Tree<Pair<Integer,Triple>> makeCSTreeObj(HashMap<String, Set<Triple>> ps, Integer ds, HashSet<Integer> set, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) {

        HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> css = getCSSObj(ds);
        HashMap<Integer, Integer> hc = getHCObj(ds);
        HashMap<Integer, Set<String>> additionalSets = getAdditionalSetsObj(ds);
        HashMap<Integer, Integer> cost = getCostObj(ds);
        HashMap<String, HashSet<Integer>> predicateIndexDS = getPredicateIndexObj(ds, predicateIndex);

        LinkedList<String> order = new LinkedList<String>();
        Integer s = getStartCS(ps.keySet(), css, predicateIndexDS, hc, additionalSets, cost); 
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
            if (ps.containsKey(p)) {
                order.addFirst(p);
            }
            s = cheapestSS;
            sPreds = cPreds;
        }
        if ((sPreds != null) && sPreds.size()>0) {
            String p = sPreds.iterator().next();
            if (ps.containsKey(p)) {
                order.addFirst(p);
            }
        }
        Tree<Pair<Integer,Triple>> sortedStar = convertToTreeS(order, ps, ds, predicateIndex, false);
        return sortedStar;
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
            DPTable.put(newEntry, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(p, new Pair<Long, Long>(c,c))); //0L)));
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
            DPTable.put(newEntry, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(p, new Pair<Long, Long>(c,c))); //0L)));
        }
    }

    public static boolean addCheapestCPSubj(Vector<Tree<Pair<Integer,Triple>>> ts1, Vector<Tree<Pair<Integer,Triple>>> ts2, HashSet<Node> sq1, HashSet<Node> sq2, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex, HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable) {

        Pair<Long,Vector<Tree<Pair<Integer,Triple>>>> costTree = getCostCPTreeSubj(ts1, ts2, predicateIndex); 
        long cost = costTree.getFirst();
        Vector<Tree<Pair<Integer,Triple>>> tree = costTree.getSecond();

        //LOG System.out.println("(S) cost of "+tree+"("+ts1+" and "+ts2+"): "+cost);        
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
        if (cost>0 && cost < c) {
            //order = makeTree(ts1, ts2, costSS12.getSecond());
            DPTable.put(set, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(tree, new Pair<Long, Long>(cost,cost))); //0L)));
	    return true;
	}
        return false;
    }

    public static boolean addCheapestCPSubjObj(Vector<Tree<Pair<Integer,Triple>>> ts1, Vector<Tree<Pair<Integer,Triple>>> ts2, HashSet<Node> sq1, HashSet<Node> sq2, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexSubj, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexObj, HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable) {

        Pair<Long,Vector<Tree<Pair<Integer,Triple>>>> costTree = getCostCPTreeSubjObj(ts1, ts2, predicateIndexSubj, predicateIndexObj); 
        long cost = costTree.getFirst();
        Vector<Tree<Pair<Integer,Triple>>> tree = costTree.getSecond();
        
        //LOG System.out.println("(SO) cost of "+tree+"("+ts1+" and "+ts2+"): "+cost);        
        HashSet<Node> set = new HashSet<Node>(sq1);
        set.addAll(sq2);

        Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>> data = DPTable.get(set);
        long c = Long.MAX_VALUE;
        if (data != null) {
            c = data.getSecond().getFirst();
        }
        // including the minimum cost if inferior to previous value
        // order not really considered here... just computing the cardinality
        if (cost>0 && cost < c) {
            //order = makeTree(ts1, ts2, costSS12.getSecond());
            DPTable.put(set, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(tree, new Pair<Long, Long>(cost,cost))); //0L)));
            return true;
        }
        return false;
    }

    public static boolean addCheapestCPObj(Vector<Tree<Pair<Integer,Triple>>> ts1, Vector<Tree<Pair<Integer,Triple>>> ts2, HashSet<Node> sq1, HashSet<Node> sq2, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex, HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable) {

        Pair<Long,Vector<Tree<Pair<Integer,Triple>>>> costTree = getCostCPTreeObj(ts1, ts2, predicateIndex); 
        long cost = costTree.getFirst();
        Vector<Tree<Pair<Integer,Triple>>> tree = costTree.getSecond();
        //LOG System.out.println("(O) cost of "+tree+"("+ts1+" and "+ts2+"): "+cost);        
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
        if (cost>0 && cost < c) {
            //order = makeTree(ts1, ts2, costSS12.getSecond());
            DPTable.put(set, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(tree, new Pair<Long, Long>(cost,cost))); //0L)));
            return true;
        }
        return false;
    }

    public static Vector<Tree<Pair<Integer,Triple>>> makeTreeBasic(Vector<Tree<Pair<Integer,Triple>>> vTreeL, Vector<Tree<Pair<Integer,Triple>>> vTreeR) {
        //System.out.println("Make tree out of "+vTreeL+" and "+vTreeR);
        Vector<Tree<Pair<Integer,Triple>>> res = new Vector<Tree<Pair<Integer,Triple>>>();

        if (vTreeL.size() > 0 && vTreeR.size() > 0) {
            //Set<Triple> triples = obtainTriples(vTreeL.get(0));
            //triples.retainAll(obtainTriples(vTreeR.get(0)));
            for (int i = 0; i < vTreeL.size();i++) {
                Tree<Pair<Integer,Triple>> treeL = vTreeL.get(i);

                for (int k = 0; k < vTreeR.size(); k++) {
                    Tree<Pair<Integer,Triple>> treeR = vTreeR.get(k);
                    res.add(new Branch<Pair<Integer,Triple>>(treeL, treeR));
                    /*boolean add = true;
                    Tree<Pair<Integer,Triple>> tmpTreeL = treeL;
                    for (Triple t : triples) {
                        Integer sourceL = obtainSource(tmpTreeL, t);
                        Integer sourceR = obtainSource(treeR, t);
                        if (sourceL.equals(sourceR)) {
                            double connectedSameSourceL = obtainNumberConnectedTriplesSameSource(tmpTreeL, t, sourceL);
                            double connectedSameSourceR = obtainNumberConnectedTriplesSameSource(treeR, t, sourceR);
                            double connectedL = obtainNumberConnectedTriples(tmpTreeL, t);
                            double connectedR = obtainNumberConnectedTriples(treeR, t);
                            if (connectedSameSourceR > connectedSameSourceL || ((connectedSameSourceR == connectedSameSourceL) && connectedR >= connectedL)) {
                                tmpTreeL = remove(tmpTreeL, t);
                            } else {
                                treeR = remove(treeR, t);
                            }

                        } else {
                            add = false;
                            break;
                        }
                        if (tmpTreeL == null || treeR == null) {
                            break;
                        }
                    }
                    if (add && tmpTreeL != null && treeR != null) {
                        res.add(new Branch<Pair<Integer,Triple>>(tmpTreeL, treeR));
                    } else if (add && tmpTreeL != null) {
                        res.add(tmpTreeL);
                    } else if (add && treeR != null) {
                        res.add(treeR);
                    }*/
                }
            }
        } else if (vTreeR.size() > 0) {
            res.addAll(vTreeR);
        } else if (vTreeL.size() > 0) {
            res.addAll(vTreeL);
        }
        return res;
    }

    public static Vector<Tree<Pair<Integer,Triple>>> makeTree(Vector<Tree<Pair<Integer,Triple>>> vTreeL, Vector<Tree<Pair<Integer,Triple>>> vTreeR) {
        //System.out.println("Make tree out of "+vTreeL+" and "+vTreeR);
        Vector<Tree<Pair<Integer,Triple>>> res = new Vector<Tree<Pair<Integer,Triple>>>();

        if (vTreeL.size() > 0 && vTreeR.size() > 0) {
            Set<Triple> triples = obtainTriples(vTreeL.get(0));
            triples.retainAll(obtainTriples(vTreeR.get(0)));
        //for (Triple t : triples) {
            for (int i = 0; i < vTreeL.size();i++) {
                Tree<Pair<Integer,Triple>> treeL = vTreeL.get(i);
                
                for (int k = 0; k < vTreeR.size(); k++) {
                    Tree<Pair<Integer,Triple>> treeR = vTreeR.get(k);
                    boolean add = true;
                    Tree<Pair<Integer,Triple>> tmpTreeL = treeL;
                    for (Triple t : triples) {
                        Integer sourceL = obtainSource(tmpTreeL, t);
                        Integer sourceR = obtainSource(treeR, t);
                        if (sourceL.equals(sourceR)) {
                            double connectedSameSourceL = obtainNumberConnectedTriplesSameSource(tmpTreeL, t, sourceL);
                            double connectedSameSourceR = obtainNumberConnectedTriplesSameSource(treeR, t, sourceR);
                            double connectedL = obtainNumberConnectedTriples(tmpTreeL, t);
                            double connectedR = obtainNumberConnectedTriples(treeR, t);
                            if (connectedSameSourceR > connectedSameSourceL || ((connectedSameSourceR == connectedSameSourceL) && connectedR >= connectedL)) {
                                tmpTreeL = remove(tmpTreeL, t);
                            } else {
                                treeR = remove(treeR, t);
                            }
                        
                        } else {
                            //treeL = null;
                            //treeR = null; 
                            add = false;
                            break;
                        }
                        if (tmpTreeL == null || treeR == null) {
                            break;
                        }
                    }
                    if (add && tmpTreeL != null && treeR != null) {
                        res.add(new Branch<Pair<Integer,Triple>>(tmpTreeL, treeR));
                    } else if (add && tmpTreeL != null) {
                        res.add(tmpTreeL);
                    } else if (add && treeR != null) {
                        res.add(treeR);
                    }
                }
            }
        } else if (vTreeR.size() > 0) {
            res.addAll(vTreeR);
        } else if (vTreeL.size() > 0) {
            res.addAll(vTreeL);
        }
        return res;
    }

    public static Tree<Pair<Integer,Triple>> makeTree(Integer sourceL, Tree<Pair<Integer,Triple>> treeL, Integer sourceR, Tree<Pair<Integer,Triple>> treeR) {
        //System.out.println("Make tree out of "+vTreeL+" and "+vTreeR);
        Tree<Pair<Integer,Triple>> res = null;

            Set<Triple> triples = obtainTriples(treeL);
            triples.retainAll(obtainTriples(treeR));

                    boolean add = true;
                    Tree<Pair<Integer,Triple>> tmpTreeL = treeL;
                    for (Triple t : triples) {
                        if (sourceL.equals(sourceR)) {
                            double connectedSameSourceL = obtainNumberConnectedTriplesSameSource(tmpTreeL, t, sourceL);
                            double connectedSameSourceR = obtainNumberConnectedTriplesSameSource(treeR, t, sourceR);
                            double connectedL = obtainNumberConnectedTriples(tmpTreeL, t);
                            double connectedR = obtainNumberConnectedTriples(treeR, t);
                            if (connectedSameSourceR > connectedSameSourceL || ((connectedSameSourceR == connectedSameSourceL) && connectedR >= connectedL)) {
                                tmpTreeL = remove(tmpTreeL, t);
                            } else {
                                treeR = remove(treeR, t);
                            }

                        } else {
                            //treeL = null;
                            //treeR = null; 
                            add = false;
                            break;
                        }
                        if (tmpTreeL == null || treeR == null) {
                            break;
                        }
                    }
                    if (add && tmpTreeL != null && treeR != null) {
                        res = new Branch<Pair<Integer,Triple>>(tmpTreeL, treeR);
                    } else if (add && tmpTreeL != null) {
                        res = tmpTreeL;
                    } else if (add && treeR != null) {
                        res = treeR;
                    }
        return res;
    }
    public static HashMap<String, Set<Triple>> obtainPredicates(Set<Triple> set) {
        HashMap<String, Set<Triple>> map = new HashMap<String, Set<Triple>>();
        for (Triple t : set) {
            Node p = t.getPredicate();
            if (p.isURI()) {
                String pStr = "<"+p.getURI()+">";
                Set<Triple> s = map.get(pStr);
                if (s==null) {
                    s = new HashSet<Triple>();
                }
                s.add(t);
                map.put(pStr, s);
            }
        }
        return map;
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
            //LOG System.out.println("sourceLT: "+sourceLT);
            if (sourceLT == null) {
                continue;
            }
            HashMap<String, Triple> mapL = new HashMap<String, Triple>();
            Set<String> predicatesLT = obtainBoundPredicates(leftTree, mapL);
            HashSet<Integer> relevantCssLT = computeRelevantCS(predicatesLT, sourceLT, predicateIndex);
            Long costLT = computeCost(sourceLT, getCSSSubj(sourceLT), relevantCssLT, predicatesLT, mapL, true);
            for (Tree<Pair<Integer,Triple>> rightTree : vRT) {
                Integer sourceRT = sameSource(rightTree.getElements());
                //LOG System.out.println("sourceRT: "+sourceRT);
                if (sourceRT == null) {
                    continue;
                }         
                HashMap<String, Triple> mapR = new HashMap<String, Triple>();
                Set<String> predicatesRT = obtainBoundPredicates(rightTree, mapR);
                HashSet<Integer> relevantCssRT = computeRelevantCS(predicatesRT, sourceRT, predicateIndex);
                Long costRT = computeCost(sourceRT, getCSSSubj(sourceRT), relevantCssRT, predicatesRT, mapR, true);

                Tree<Pair<Integer,Triple>> nT = null;
                if (costLT <= costRT) {
                    nT = new Branch<Pair<Integer,Triple>>(leftTree, rightTree);
                } else {
                    nT = new Branch<Pair<Integer,Triple>>(rightTree, leftTree);
                }
                //long t0 = System.currentTimeMillis();
                long tmpCost = getCostCPSubj(obtainTriples(leftTree), sourceLT, obtainTriples(rightTree), sourceRT, predicateIndex);
                //t0 = System.currentTimeMillis() -t0;
                //System.out.println("getCostCPSubj: "+t0);
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
            //LOG System.out.println("sourceLT: "+sourceLT);
            if (sourceLT == null) {
                continue;
            }
            HashMap<String, Triple> mapL = new HashMap<String, Triple>();
            Set<String> predicatesLT = obtainBoundPredicates(leftTree, mapL);
            HashSet<Integer> relevantCssLT = computeRelevantCS(predicatesLT, sourceLT, predicateIndexSubj);
            Long costLT = computeCost(sourceLT, getCSSSubj(sourceLT), relevantCssLT, predicatesLT, mapL, true);
            for (Tree<Pair<Integer,Triple>> rightTree : vRT) {
                Integer sourceRT = sameSource(rightTree.getElements());
                //LOG System.out.println("sourceRT: "+sourceRT);
                if (sourceRT == null) {
                    continue;
                }         
                HashMap<String, Triple> mapR = new HashMap<String, Triple>();
                Set<String> predicatesRT = obtainBoundPredicates(rightTree, mapR);
                HashSet<Integer> relevantCssRT = computeRelevantCS(predicatesRT, sourceRT, predicateIndexObj);
                Long costRT = computeCost(sourceRT, getCSSObj(sourceRT), relevantCssRT, predicatesRT, mapR, false);

                Tree<Pair<Integer,Triple>> nT = null;
                if (costLT <= costRT) {
                    nT = new Branch<Pair<Integer,Triple>>(leftTree, rightTree);
                } else {
                    nT = new Branch<Pair<Integer,Triple>>(rightTree, leftTree);
                }
                long tmpCost = getCostCPSubjObj(obtainTriples(leftTree), sourceLT, obtainTriples(rightTree), sourceRT, predicateIndexSubj, predicateIndexObj);
                //LOG System.out.println("tmpCost: "+tmpCost);
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
            //LOG System.out.println("sourceLT: "+sourceLT);
            if (sourceLT == null) {
                continue;
            }
            HashMap<String, Triple> mapL = new HashMap<String, Triple>();
            Set<String> predicatesLT = obtainBoundPredicates(leftTree, mapL);
            HashSet<Integer> relevantCssLT = computeRelevantCS(predicatesLT, sourceLT, predicateIndex);
            Long costLT = computeCost(sourceLT, getCSSObj(sourceLT), relevantCssLT, predicatesLT, mapL, false);
            for (Tree<Pair<Integer,Triple>> rightTree : vRT) {
                Integer sourceRT = sameSource(rightTree.getElements());
                //LOG System.out.println("sourceRT: "+sourceRT);
                if (sourceRT == null) {
                    continue;
                }         
                HashMap<String, Triple> mapR = new HashMap<String, Triple>();
                Set<String> predicatesRT = obtainBoundPredicates(rightTree, mapR);
                HashSet<Integer> relevantCssRT = computeRelevantCS(predicatesRT, sourceRT, predicateIndex);
                Long costRT = computeCost(sourceRT, getCSSObj(sourceRT), relevantCssRT, predicatesRT, mapR, false);

                Tree<Pair<Integer,Triple>> nT = null;
                if (costLT <= costRT) {
                    nT = new Branch<Pair<Integer,Triple>>(leftTree, rightTree);
                } else {
                    nT = new Branch<Pair<Integer,Triple>>(rightTree, leftTree);
                }
                long tmpCost = getCostCPObj(obtainTriples(leftTree), sourceLT, obtainTriples(rightTree), sourceRT, predicateIndex);
                //LOG System.out.println("tmpCost: "+tmpCost);
                if (tmpCost > 0) { 
                    res.add(nT);
                    cost += tmpCost;
                }
            }
        }
        return new Pair<Long, Vector<Tree<Pair<Integer,Triple>>>>(cost, res);
    }

    public static Long getMultiplicitySubj(Integer m, String p,  Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs, Set<String> ps1, HashMap<String, Triple> map1, Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs2, Set<String> ps2, HashMap<String, Triple> map2) {

        double sel = 1.0;
        Integer count = cPs.getFirst();
        for (String p1 : ps1) {
            Triple t = map1.get(p1);
            Node s = t.getSubject();
            Node o = t.getObject();
            // COMMENT THE CONDITIONAL TO STICK TO THE PAPER FORMULA
            if (original || !p1.equals(p)) {
                boolean c = includeMultiplicity && projectedVariables.contains(s) && (!distinct || projectedVariables.contains(o));
                if (c) {
                    Integer p_m = cPs.getSecond().get(p1).getFirst();
                    sel = sel*(((double)p_m)/count);
                }
            }
        }
        count = cPs2.getFirst();
        for (String p2 : ps2) {
            Triple t = map2.get(p2);
            Node s = t.getSubject();
            Node o = t.getObject();
            boolean c = includeMultiplicity && projectedVariables.contains(s) && (!distinct || projectedVariables.contains(o));
            if (c) {
                Integer p_m = cPs2.getSecond().get(p2).getFirst();
                sel = sel*(((double)p_m)/count);
            }
        }
        return Math.round(Math.ceil(m*sel));
    }

    public static Long getMultiplicitySubjS(Integer m, String p,  Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs, Set<String> ps1, HashMap<String, Set<Triple>> map1, Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs2, Set<String> ps2, HashMap<String, Set<Triple>> map2) {

        double sel = 1.0;
        Integer count = cPs.getFirst();
        for (String p1 : ps1) {
          for (Triple t : map1.get(p1)) {
            Node s = t.getSubject();
            Node o = t.getObject();
            // COMMENT THE CONDITIONAL TO STICK TO THE PAPER FORMULA
            if (original || !p1.equals(p)) {
                boolean c = includeMultiplicity && projectedVariables.contains(s) && (!distinct || projectedVariables.contains(o));
                if (c) {
                    Integer p_m = cPs.getSecond().get(p1).getFirst();
                    sel = sel*(((double)p_m)/count);
                }
            }
          }
        }
        count = cPs2.getFirst();
        for (String p2 : ps2) {
          for (Triple t : map2.get(p2)) {
            Node s = t.getSubject();
            Node o = t.getObject();
            boolean c = includeMultiplicity && projectedVariables.contains(s) && (!distinct || projectedVariables.contains(o));
            if (c) {
                Integer p_m = cPs2.getSecond().get(p2).getFirst();
                sel = sel*(((double)p_m)/count);
            }
          }
        }
        return Math.round(Math.ceil(m*sel));
    }

    public static Long getMultiplicitySubjObj(Integer m, String p,  Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs, Set<String> ps1, HashMap<String, Triple> map1, Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs2, Set<String> ps2, HashMap<String, Triple> map2) {

        double sel = 1.0;
        Integer count = cPs.getFirst();
        for (String p1 : ps1) {
            Triple t = map1.get(p1);
            Node s = t.getSubject();
            Node o = t.getObject();
            // COMMENT THE CONDITIONAL TO STICK TO THE PAPER FORMULA
            if (original || !p1.equals(p)) {
                boolean c = includeMultiplicity && projectedVariables.contains(s) && (!distinct || projectedVariables.contains(o));
                if (c) {
                    Integer p_m = cPs.getSecond().get(p1).getFirst();
                    sel = sel*(((double)p_m)/count);
                }
            }
        }
        count = cPs2.getFirst();
        for (String p2 : ps2) {
            Triple t = map2.get(p2);
            Node s = t.getSubject();
            Node o = t.getObject();
            if (original || !p2.equals(p)) {
                boolean c = includeMultiplicity && projectedVariables.contains(o) && (!distinct || projectedVariables.contains(s));
                if (c) {
                    Integer p_m = cPs2.getSecond().get(p2).getFirst();
                    sel = sel*(((double)p_m)/count);
                }
            }
        }
        return Math.round(Math.ceil(m*sel));
    }

    public static Long getMultiplicitySubjObjS(Integer m, String p,  Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs, Set<String> ps1, HashMap<String, Set<Triple>> map1, Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs2, Set<String> ps2, HashMap<String, Set<Triple>> map2) {

        double sel = 1.0;
        Integer count = cPs.getFirst();
        for (String p1 : ps1) {
          for (Triple t : map1.get(p1)) {
            Node s = t.getSubject();
            Node o = t.getObject();
            // COMMENT THE CONDITIONAL TO STICK TO THE PAPER FORMULA
            if (original || !p1.equals(p)) {
                boolean c = includeMultiplicity && projectedVariables.contains(s) && (!distinct || projectedVariables.contains(o));
                if (c) {
                    Integer p_m = cPs.getSecond().get(p1).getFirst();
                    sel = sel*(((double)p_m)/count);
                }
            }
          }
        }
        count = cPs2.getFirst();
        for (String p2 : ps2) {
          for (Triple t : map2.get(p2)) {
            Node s = t.getSubject();
            Node o = t.getObject();
            if (original || !p2.equals(p)) {
                boolean c = includeMultiplicity && projectedVariables.contains(o) && (!distinct || projectedVariables.contains(s));
                if (c) {
                    Integer p_m = cPs2.getSecond().get(p2).getFirst();
                    sel = sel*(((double)p_m)/count);
                }
            }
          }
        }
        return Math.round(Math.ceil(m*sel));
    }

    public static Long getMultiplicityObj(Integer m, String p,  Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs, Set<String> ps1, HashMap<String, Triple> map1, Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs2, Set<String> ps2, HashMap<String, Triple> map2) {

        double sel = 1.0;
        Integer count = cPs.getFirst();
        for (String p1 : ps1) {
            Triple t = map1.get(p1);
            Node s = t.getSubject();
            Node o = t.getObject();
            if (original || !p1.equals(p)) {
                boolean c = includeMultiplicity && projectedVariables.contains(o) && (!distinct || projectedVariables.contains(s));
                if (c) {
                    Integer p_m = cPs.getSecond().get(p1).getFirst();
                    sel = sel*(((double)p_m)/count);
                }
            }
        }
        count = cPs2.getFirst();
        for (String p2 : ps2) {
            Triple t = map2.get(p2);
            Node s = t.getSubject();
            Node o = t.getObject();
                boolean c = includeMultiplicity && projectedVariables.contains(o) && (!distinct || projectedVariables.contains(s));
                if (c) {
                    Integer p_m = cPs2.getSecond().get(p2).getFirst();
                    sel = sel*(((double)p_m)/count);
                }
        }
        return Math.round(Math.ceil(m*sel));
    }

    public static Long getMultiplicityObjS(Integer m, String p,  Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs, Set<String> ps1, HashMap<String, Set<Triple>> map1, Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs2, Set<String> ps2, HashMap<String, Set<Triple>> map2) {

        double sel = 1.0;
        Integer count = cPs.getFirst();
        for (String p1 : ps1) {
          for (Triple t : map1.get(p1)) {
            Node s = t.getSubject();
            Node o = t.getObject();
            if (original || !p1.equals(p)) {
                boolean c = includeMultiplicity && projectedVariables.contains(o) && (!distinct || projectedVariables.contains(s));
                if (c) {
                    Integer p_m = cPs.getSecond().get(p1).getFirst();
                    sel = sel*(((double)p_m)/count);
                }
            }
          }
        }
        count = cPs2.getFirst();
        for (String p2 : ps2) {
            for (Triple t : map2.get(p2)) {
                Node s = t.getSubject();
                Node o = t.getObject();
                boolean c = includeMultiplicity && projectedVariables.contains(o) && (!distinct || projectedVariables.contains(s));
                if (c) {
                    Integer p_m = cPs2.getSecond().get(p2).getFirst();
                    sel = sel*(((double)p_m)/count);
                }
            }
        }
        return Math.round(Math.ceil(m*sel));
    }

    public static double getConstantSelectivity(Set<Triple> sq1, Integer ds1) {

        double sel = 1.0;
        for (Triple t : sq1) {
            Node s = t.getSubject();
            Node p = t.getPredicate();
            Node o = t.getObject();
            double tmpSel = 1.0;
            if (s.isVariable() && p.isVariable() && o.isVariable()) {
                tmpSel = 1.0;
            } else if (p.isVariable()) {
                Integer numTriples = globalStats.get(ds1).get(0);
                Integer numSubj = globalStats.get(ds1).get(3);
                Integer numObj = globalStats.get(ds1).get(4);
                if (!s.isVariable() && !o.isVariable()) {
                    tmpSel = tmpSel * (1.0 / (((double) numSubj) * numObj));
                } else if (!s.isVariable()) {
                    tmpSel = tmpSel * (1.0 / ((double) numSubj));
                } else if(!o.isVariable()) {
                    tmpSel = tmpSel * (1.0 / ((double) numObj));
                }
            } else if (p.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") && !o.isVariable()) {
                String oStr = "";
                if (o.isURI()) {
                    oStr = o.getURI();
                } else {
                    oStr = o.toString();
                }
                HashMap<Integer, Integer> tmpDsNumEnt = classStats.get(oStr);
                if (tmpDsNumEnt != null && tmpDsNumEnt.containsKey(ds1)) {

                    String pStr = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
                    HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>> dsNTNSNO = propertyStats.get(pStr);
                    if (dsNTNSNO != null && dsNTNSNO.containsKey(ds1)) {
                        Pair<Integer, Pair<Integer, Integer>> numTnumSnumO = dsNTNSNO.get(ds1);
                        //System.out.println("numTnumSnumO: "+numTnumSnumO);
                        Integer numTriples = numTnumSnumO.getFirst();
                        Integer numTriplesType = tmpDsNumEnt.get(ds1);
                        tmpSel = tmpSel * (numTriplesType / ((double) numTriples));
                    } else {
                        tmpSel = 0.0;
                    }
                } else if (tmpDsNumEnt != null) {
                    tmpSel = 0.0;
                }
            } else if (s.isVariable() && !o.isVariable()) {
                String pStr = p.getURI();
                HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>> dsNTNSNO = propertyStats.get(pStr);
                if (dsNTNSNO != null && dsNTNSNO.containsKey(ds1)) {
                    Pair<Integer, Pair<Integer, Integer>> numTnumSnumO = dsNTNSNO.get(ds1);
                    Integer numTriples = numTnumSnumO.getFirst();
                    Integer numObj = numTnumSnumO.getSecond().getSecond();
                    tmpSel = tmpSel * (1 / ((double) numObj));
                } else {
                    tmpSel = 0.0;
                }
            } else if (o.isVariable() && !s.isVariable()) {
                String pStr = p.getURI();
                HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>> dsNTNSNO = propertyStats.get(pStr);
                if (dsNTNSNO != null && dsNTNSNO.containsKey(ds1)) {
                    Pair<Integer, Pair<Integer, Integer>> numTnumSnumO = dsNTNSNO.get(ds1);
                    Integer numTriples = numTnumSnumO.getFirst();
                    Integer numSubj = numTnumSnumO.getSecond().getFirst();
                    tmpSel = tmpSel * (1 / ((double) numSubj));
                } else {
                    tmpSel = 0.0;
                }
            } else if (!s.isVariable() &&!o.isVariable()){
                if (p.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                    String oStr = "";
                    if (o.isURI()) {
                        oStr = o.getURI();
                    } else {
                        oStr = o.toString();
                    }
                    HashMap<Integer, Integer> tmpDsNumEnt = classStats.get(oStr);
                    if (tmpDsNumEnt != null && tmpDsNumEnt.containsKey(ds1)) {

                        String pStr = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
                        HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>> dsNTNSNO = propertyStats.get(pStr);
                        if (dsNTNSNO != null && dsNTNSNO.containsKey(ds1)) {
                            Pair<Integer, Pair<Integer, Integer>> numTnumSnumO = dsNTNSNO.get(ds1);
                            //System.out.println("numTnumSnumO: "+numTnumSnumO);
                            Integer numTriples = numTnumSnumO.getFirst();
                            Integer numTriplesType = tmpDsNumEnt.get(ds1);
                            tmpSel = tmpSel * (1.0/ ((double) numTriplesType));
                        } else {
                            tmpSel = 0.0;
                        }
                    } else {
                        tmpSel = 0.0;
                    }

                } else {
                String pStr = p.getURI();
                HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>> dsNTNSNO = propertyStats.get(pStr);
                if (dsNTNSNO != null && dsNTNSNO.containsKey(ds1)) {
                    Pair<Integer, Pair<Integer, Integer>> numTnumSnumO = dsNTNSNO.get(ds1);
                    Integer numTriples = numTnumSnumO.getFirst();
                    Integer numSubj = numTnumSnumO.getSecond().getFirst();
                    Integer numObj = numTnumSnumO.getSecond().getSecond();
                    tmpSel = tmpSel * (1 / (((double) numSubj) * numObj));
                } else  {
                    tmpSel = 0.0;
                }
                }
            }
            if (tmpSel < sel) {
                sel = tmpSel;
            }
        }
        //System.out.println("constant selectivity for "+sq1+" and "+ds1+": "+sel);
        return sel;
    }

    public static double getConstantSelectivity(Set<Triple> sq1, Integer ds1, Set<Triple> sq2, Integer ds2) {
        double d1 = getConstantSelectivity(sq1, ds1);
        double d2 = getConstantSelectivity(sq2, ds2);
        return d1*d2;
    }

    public static long getCostCPSubj(Set<Triple> sq1, Integer ds1, Set<Triple> sq2, Integer ds2, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) { 
        //long t0 = System.currentTimeMillis();
        double selCttes = getConstantSelectivity(sq1, ds1, sq2, ds2);
        //t0 = System.currentTimeMillis()-t0;
        //System.out.println("getConstantSelectivity: "+t0);
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
        //int n1 = 0;
        //int n2 = 0;
        HashSet<Integer> css1 = computeRelevantCS(ps1, ds1, predicateIndex);
        HashSet<Integer> css2 = computeRelevantCS(ps2, ds2, predicateIndex);
        //System.out.println("(s) there are "+css1.size()+" relevant css for "+ps1+" and "+ds1);
        //System.out.println("(s) there are "+css2.size()+" relevant css for "+ps2+" and "+ds2);
        Set<String> ps12 = getCPConnectionSubj(sq1, sq2);
        Set<String> ps21 = getCPConnectionSubj(sq2, sq1);
        //System.out.println("(s) ps12: "+ps12+". ps21: "+ps21);
        // Predicate --> Count
        HashMap<String, Long> mult12 = new HashMap<String, Long>();
        long c = 0L;
        HashMap<String, Long> mult21 = new HashMap<String, Long>();

        for (String p : ps12) {
            HashMap<Integer, HashMap<Integer, Integer>> cs1cs2Count = getCPSSubj(ds1, ds2).get(p);
            if (cs1cs2Count == null) {
                //System.out.println("cps has no information for p: "+p);
                continue;
            }
            for (Integer cs1 : css1) {
                Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs = getCSSSubj(ds1).get(cs1);
                HashMap<Integer, Integer> cs2Count = cs1cs2Count.get(cs1);
                if (cs2Count == null) {
                    //System.out.println("cps(p) has no information for cs1: "+cs1);
                    continue;
                }
                for (Integer cs2 : css2) {
                    Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs2 = getCSSSubj(ds2).get(cs2);
                    Integer count = cs2Count.get(cs2);
                    if (count == null) {
                        //System.out.println("cps(p)(cs1) has no information for cs2: "+cs2);
                        continue;
                    }
                //HashMap<Integer, HashMap<String, Integer>> css2Ps = getCPSSubj(ds1, ds2).get(cs1);
                //if (css2Ps != null) {
                    //n1++;
                    //System.out.println("Links 1 -> 2");
                    //HashMap<String, Integer> links12 = css2Ps.get(cs2);
                    //if (links12 != null) {
                        //System.out.println("There are "+links12.size()+" 12-linking predicates between the css");
                        //HashSet<String> relevantLinks = new HashSet<String>(links12.keySet());
                        //relevantLinks.retainAll(ps1);
                        //for (String p : relevantLinks) {
                            //n1++;
                            //Triple t1 = map1.get(p);
                            //Triple t2 = sq2.iterator().next();
                            //if (t1.getObject().equals(t2.getSubject())) {
                                Long m1 = getMultiplicitySubj(count, p, cPs, ps1, map1, cPs2, ps2, map2);
                                //System.out.println("(S1) multiplicity went from "+links12.get(p)+" to "+m1+" for predicate "+p);
                                Long m2 = mult12.get(p);
                                if (m2 == null) {
                                    m2 = 0L;
                                }
                                mult12.put(p, m1+m2);
                            //}
                        //}
                    //}
                }
            }
        }
        for (String p : ps21) {
            HashMap<Integer, HashMap<Integer, Integer>> cs2cs1Count = getCPSSubj(ds2, ds1).get(p);
            if (cs2cs1Count == null) {
                //System.out.println("cps has no information for p: "+p);
                continue;
            }
            for (Integer cs2 : css2) {
                Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs2 = getCSSSubj(ds2).get(cs2);
                HashMap<Integer, Integer> cs1Count = cs2cs1Count.get(cs2);
                if (cs1Count == null) {
                    //continue;
                    //System.out.println("cps(p) has no information for cs2: "+cs2);
                    continue;
                }
                for (Integer cs1 : css1) {
                    Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs = getCSSSubj(ds1).get(cs1);
                    Integer count = cs1Count.get(cs1);
                    if (count == null) {
                        //System.out.println("cps(p)(cs2) has no information for cs1: "+cs1);
                        continue;
                    }
                //css2Ps = getCPSSubj(ds2, ds1).get(cs2);
                //if (css2Ps != null) {
                    //n2++;
                    //System.out.println("Links 2 -> 1");
                    //HashMap<String, Integer> links21 = css2Ps.get(cs1);
                    //if (links21 != null) {
                        //System.out.println("There are "+links21.size()+" 21-linking predicates between the css");
                        //HashSet<String> relevantLinks = new HashSet<String>(links21.keySet());
                        //relevantLinks.retainAll(ps2);
                        //for (String p : relevantLinks) {
                            //n2++;
                            //Triple t2 = map2.get(p);
                            //Triple t1 = sq1.iterator().next();
                            //if (t2.getObject().equals(t1.getSubject())) {
                                Long m1 = getMultiplicitySubj(count, p, cPs2, ps2, map2, cPs, ps1, map1);
                                //System.out.println("(S2) multiplicity went from "+links21.get(p)+" to "+m1+" for predicate "+p);
                                Long m2 = mult21.get(p);
                                if (m2 == null) {
                                    m2 = 0L;
                                }
                                mult21.put(p, m1+m2);
                            //}
                        //}
                    //}
                }
            }
        }
        //System.out.println("(s) there are "+n1+" combinations 12, and "+n2+" combinations21");
        //System.out.println("Links 1 -> 2, count: "+c12);
        for (String p : mult12.keySet()) {
            Long m = mult12.get(p);
            //System.out.println("links12 with "+p+": "+m);
            c += m;
        }
        //System.out.println("Links 2 -> 1, count: "+c21);
        for (String p : mult21.keySet()) {
            Long m = mult21.get(p);
            //System.out.println("links21 with "+p+": "+m);
            c += m;
        }
         
        return Math.round(Math.ceil(c*selCttes));
    }

    public static long getCostCPSubjObj(Set<Triple> sq1, Integer ds1, Set<Triple> sq2, Integer ds2, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexSubj, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexObj) { 

        double selCttes = getConstantSelectivity(sq1, ds1, sq2, ds2);
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
        //int n1 = 0;
        HashSet<Integer> css1 = computeRelevantCS(ps1, ds1, predicateIndexSubj);
        HashSet<Integer> css2 = computeRelevantCS(ps2, ds2, predicateIndexObj);
        //System.out.println("(so) there are "+css1.size()+" relevant css for "+ps1+" and "+ds1);
        //System.out.println("(so) there are "+css2.size()+" relevant css for "+ps2+" and "+ds2);
        Set<String> ps12 = getCPConnectionSubjObj(sq1, sq2);
        //System.out.println("(so) ps12: "+ps12);
        // Predicate --> Count
        HashMap<String, Long> mult12 = new HashMap<String, Long>();
        long c = 0L;
        for (String p : ps12) {
            HashMap<Integer, HashMap<Integer, Integer>> cs1cs2Count = getCPSSubjObj(ds1, ds2).get(p);
            if (cs1cs2Count == null)
                continue;
            for (Integer cs1 : css1) {
                Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs = getCSSSubj(ds1).get(cs1);
                HashMap<Integer, Integer> cs2Count = cs1cs2Count.get(cs1);
                if (cs2Count == null)
                    continue;
                for (Integer cs2 : css2) {
                    Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs2 = getCSSObj(ds2).get(cs2);
                    Integer count = cs2Count.get(cs2);
                    if (count == null)
                        continue;
                    Long m1 = getMultiplicitySubjObj(count, p, cPs, ps1, map1, cPs2, ps2, map2);
                    Long m2 = mult12.get(p);
                    if (m2 == null) {
                        m2 = 0L;
                    }
                    mult12.put(p, m1+m2);
                }
            }
        }
/*
        for (Integer cs1 : css1) {
            Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs = getCSSSubj(ds1).get(cs1);

            for (Integer cs2 : css2) {
                Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs2 = getCSSObj(ds2).get(cs2);

                HashMap<Integer, HashMap<String, Integer>> css2Ps = getCPSSubjObj(ds1, ds2).get(cs1);
                if (css2Ps != null) {
                    //n1++;
                    //System.out.println("Links 1 -> 2");
                    HashMap<String, Integer> links12 = css2Ps.get(cs2);
                    if (links12 != null) {
                        //LOG System.out.println("There are "+links12.size()+" 12-linking predicates between the css");
                        HashSet<String> relevantLinks = new HashSet<String>(links12.keySet());
                        relevantLinks.retainAll(ps1);
                        for (String p : relevantLinks) {
                            n1++;
                            Triple t1 = map1.get(p);
                            Triple t2 = sq2.iterator().next();
                            if (t1.getObject().equals(t2.getObject())) {
                                Long m1 = getMultiplicitySubjObj(links12.get(p), p, cPs, ps1, map1, cPs2, ps2, map2);
                                //System.out.println("(SO) multiplicity went from "+links12.get(p)+" to "+m1+" for predicate "+p);
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
        System.out.println("(so) there are "+n1+" combinations 12");*/
        //System.out.println("Links 1 -> 2, count: "+c12);
        for (String p : mult12.keySet()) {
            Long m = mult12.get(p);
            c += m;
        }

        return Math.round(Math.ceil(c*selCttes));
    }

    public static long getCostCPObj(Set<Triple> sq1, Integer ds1, Set<Triple> sq2, Integer ds2, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) { 

        double selCttes = getConstantSelectivity(sq1, ds1, sq2, ds2);
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
        //int n1 = 0;
        //int n2 = 0;
        HashSet<Integer> css1 = computeRelevantCS(ps1, ds1, predicateIndex);
        HashSet<Integer> css2 = computeRelevantCS(ps2, ds2, predicateIndex);
        //System.out.println("(o) there are "+css1.size()+" relevant css for "+ps1+" and "+ds1);
        //System.out.println("(o) there are "+css2.size()+" relevant css for "+ps2+" and "+ds2);
        Set<String> ps12 = getCPConnectionObj(sq1, sq2);
        Set<String> ps21 = getCPConnectionObj(sq2, sq1);
        //System.out.println("(o) ps12: "+ps12+". ps21: "+ps21);
        // Predicate --> Count
        HashMap<String, Long> mult12 = new HashMap<String, Long>();
        long c = 0L;
        HashMap<String, Long> mult21 = new HashMap<String, Long>();

        for (String p : ps12) {
            HashMap<Integer, HashMap<Integer, Integer>> cs1cs2Count = getCPSObj(ds1, ds2).get(p);
            if (cs1cs2Count == null) 
                continue;
            for (Integer cs1 : css1) {
                Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs = getCSSObj(ds1).get(cs1);
                HashMap<Integer, Integer> cs2Count = cs1cs2Count.get(cs1);
                if (cs2Count == null)
                    continue;
                for (Integer cs2 : css2) {
                    Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs2 = getCSSObj(ds2).get(cs2);
                    Integer count = cs2Count.get(cs2);
                    if (count == null)
                        continue;
                    Long m1 = getMultiplicityObj(count, p, cPs, ps1, map1, cPs2, ps2, map2);
                    Long m2 = mult12.get(p);
                    if (m2 == null) {
                        m2 = 0L;
                    }
                    mult12.put(p, m1+m2);
                }
            }
        }
        for (String p : ps21) {
            HashMap<Integer, HashMap<Integer, Integer>> cs2cs1Count = getCPSObj(ds2, ds1).get(p);
            if (cs2cs1Count == null)
                continue;
            for (Integer cs2 : css2) {
                Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs2 = getCSSObj(ds2).get(cs2);
                HashMap<Integer, Integer> cs1Count = cs2cs1Count.get(cs2);
                if (cs1Count == null)
                    continue;
                for (Integer cs1 : css1) {
                    Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs = getCSSObj(ds1).get(cs1);
                    Integer count = cs1Count.get(cs1);
                    if (count == null)
                        continue;
                    Long m1 = getMultiplicityObj(count, p, cPs2, ps2, map2, cPs, ps1, map1);
                    Long m2 = mult21.get(p);
                    if (m2 == null) {
                        m2 = 0L;
                    }
                    mult21.put(p, m1+m2);
                }
            }
        }
/*
        for (Integer cs1 : css1) {
            Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs = getCSSObj(ds1).get(cs1);

            for (Integer cs2 : css2) {
                Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs2 = getCSSObj(ds2).get(cs2);

                HashMap<Integer, HashMap<String, Integer>> css2Ps = getCPSObj(ds1, ds2).get(cs1);
                if (css2Ps != null) {
                    //n1++;
                    //System.out.println("Links 1 -> 2");
                    HashMap<String, Integer> links12 = css2Ps.get(cs2);

                    if (links12 != null) {
                        //LOG System.out.println("There are "+links12.size()+" 12-linking predicates between the css");
                        HashSet<String> relevantLinks = new HashSet<String>(links12.keySet());
                        relevantLinks.retainAll(ps1);
                        for (String p : relevantLinks) {
                            n1++;
                            Triple t1 = map1.get(p); //sq1.iterator().next(); //map1.get(p);
                            Triple t2 = sq2.iterator().next();
                            if (t1.getSubject().equals(t2.getObject())) {
                                Long m1 = getMultiplicityObj(links12.get(p), p,  cPs, ps1, map1, cPs2, ps2, map2);
                                //System.out.println("(O1) multiplicity went from "+links12.get(p)+" to "+m1+" for predicate "+p);
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
                    //n2++;
                    //System.out.println("Links 2 -> 1");
                    HashMap<String, Integer> links21 = css2Ps.get(cs1);
                    if (links21 != null) {
                        //LOG System.out.println("There are "+links21.size()+" 21-linking predicates between the css");
                        HashSet<String> relevantLinks = new HashSet<String>(links21.keySet());
                        relevantLinks.retainAll(ps2);
                        for (String p : relevantLinks) {
                            n2++;
                            Triple t2 = map2.get(p);
                            Triple t1 = sq1.iterator().next();
                            if (t2.getSubject().equals(t1.getObject())) {
                                Long m1 = getMultiplicityObj(links21.get(p), p, cPs2, ps2, map2, cPs, ps1, map1);
                                //System.out.println("(O2) multiplicity went from "+links21.get(p)+" to "+m1+" for predicate "+p);
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
        System.out.println("(o) there are "+n1+" combinations 12, and "+n2+" combinations21");*/
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
        return Math.round(Math.ceil(c*selCttes));
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
            if (dsCss == null) {
                return new HashMap<Integer,HashSet<Integer>>();
            }
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
                c += computeCost(ds, getCSSSubj(ds), relevantCss, ps, map, true);
                //if (tmpC > 0) {
                    tmpTree.setCard(c);
                    tmpTree.setCost(c);
                //}
                //c+= tmpC;
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
                c += computeCost(ds, getCSSObj(ds), relevantCss, ps, map, false);
                tmpTree.setCard(c);
                tmpTree.setCost(c);
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
                c += computeCost(ds, getCSSSubj(ds), relevantCss, ps, map, true);
                tree.setCard(c);
                tree.setCost(c);
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
                c += computeCost(ds, getCSSObj(ds), relevantCss, ps, map, false);
                tree.setCard(c);
                tree.setCost(c);
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
        if (dsCss == null) {
            return c;
        }
        for (Integer ds : dsCss.keySet()) {
            c += computeCost(ds, getCSSSubj(ds), dsCss.get(ds), ps, map, true);
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
        if (dsCss == null) {
            return c;
        }
        for (Integer ds : dsCss.keySet()) {
            c += computeCost(ds, getCSSObj(ds), dsCss.get(ds), ps, map, false);
        }
        return c;
    }

    public static long computeCostS(Integer ds, HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> css, HashSet<Integer> relevantCss, Set<String> ps, HashMap<String, Set<Triple>> map, boolean subjStar) {
        Set<Triple> sq = new HashSet<Triple>();
        for (String p : ps) {
            sq.addAll(map.get(p));
        }
        double selCttes = getConstantSelectivity(sq, ds);
        long cost = 0L;
        for (Integer cs : relevantCss) {
            long costTmp = css.get(cs).getFirst();
            double sel = 1.0;
              for (String p : ps) {
               for (Triple t : map.get(p)) {
                Node s = t.getSubject();
                Node o = t.getObject();
                boolean c = true;
                if (subjStar) {
                    c = includeMultiplicity && projectedVariables.contains(s) && (!distinct || projectedVariables.contains(o));
                } else {
                    c = includeMultiplicity && projectedVariables.contains(o) && (!distinct || projectedVariables.contains(s));
                }
                if (c) {
                    sel = sel*(((double)css.get(cs).getSecond().get(p).getFirst())/costTmp);
                }
               }
              }
            cost += Math.round(Math.ceil(costTmp*sel));
        }
        return Math.round(Math.ceil(cost*selCttes));
    }

    public static long computeCost(Integer ds, HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> css, HashSet<Integer> relevantCss, Set<String> ps, HashMap<String, Triple> map, boolean subjStar) {
        Set<Triple> sq = new HashSet<Triple>();
        for (String p : ps) {
            sq.add(map.get(p));
        }
        double selCttes = getConstantSelectivity(sq, ds);
        long cost = 0L;
        for (Integer cs : relevantCss) {
            long costTmp = css.get(cs).getFirst();
            double sel = 1.0;
              for (String p : ps) {
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
                    sel = sel*(((double)css.get(cs).getSecond().get(p).getFirst())/costTmp);
                }
              }
            cost += Math.round(Math.ceil(costTmp*sel));
        }
        return Math.round(Math.ceil(cost*selCttes));
    }

    public static Tree<Pair<Integer,Triple>> convertToTreeS(LinkedList<String> orderedPs, HashMap<String, Set<Triple>> map, Integer ds, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex, boolean subjCenter) {
        Tree<Pair<Integer,Triple>> sortedStar = null;
        for (String p : orderedPs) {
            if (map.containsKey(p)) {
              for (Triple t : map.get(p)) {

                Leaf<Pair<Integer,Triple>> leaf = new Leaf<Pair<Integer,Triple>>(new Pair<Integer,Triple>(ds, t));
                if (sortedStar == null) {
                    sortedStar = leaf;
                } else {
                    sortedStar = new Branch<Pair<Integer,Triple>>(sortedStar, leaf);
                }
                //sortedStar.add(t);
              }
            }
        }
        //System.out.println("sorted star before considering constants: "+sortedStar);
        considerConstants(sortedStar, predicateIndex, subjCenter);
        return sortedStar;
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

        if ((tree == null) || tree instanceof Leaf<?>) {
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
        HashMap<String, Set<Triple>> map = new HashMap<String, Set<Triple>>();
        HashSet<Triple> remainingTriples = new HashSet<Triple>();
        for (Triple t : ts) {
            Node p = t.getPredicate(); 
            if (p.isURI()) {
                String pStr = "<"+p.getURI()+">";
                ps.add(pStr);
                Set<Triple> tsAux = map.get(pStr);
                if (tsAux == null) {
                    tsAux = new HashSet<Triple>();
                }
                tsAux.add(t);
                map.put(pStr, tsAux);
            } else {
                remainingTriples.add(t);
            }
        }
        HashMap<Integer, HashSet<Integer>> dsCss = computeRelevantCS(ps, predicateIndex);
        //System.out.println("dsCss for the star: "+dsCss);
        Vector<Tree<Pair<Integer,Triple>>> vTree = new Vector<Tree<Pair<Integer,Triple>>>();
        //HashMap<Integer, Tree<Triple>> dsTree = new HashMap<Integer, Tree<Triple>>();
        if (dsCss == null) {
            return vTree;
        }        
        for (Integer ds : dsCss.keySet()) {
            HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> css = getCSSSubj(ds);
            HashMap<Integer, Integer> hc = getHCSubj(ds);
            HashMap<Integer, Set<String>> additionalSets = getAdditionalSetsSubj(ds);
            HashMap<Integer, Integer> cost = getCostSubj(ds);
            HashMap<String, HashSet<Integer>> predicateIndexDS = getPredicateIndexSubj(ds, predicateIndex);
            LinkedList<String> orderedPs = produceStarJoinOrdering.getStarJoinOrdering(ps, css, hc, additionalSets, predicateIndexDS, cost);
            //System.out.println("list of sorted predicates: "+orderedPs);
            Tree<Pair<Integer,Triple>> sortedStar = convertToTreeS(orderedPs, map, ds, predicateIndex, true);
            //System.out.println("tree of sorted triples: "+sortedStar);
            for (Triple t : remainingTriples) {
                Leaf<Pair<Integer,Triple>> leaf = new Leaf<Pair<Integer,Triple>>(new Pair<Integer,Triple>(ds, t));
                if (sortedStar == null) {
                    sortedStar = leaf;
                } else {
                    sortedStar = new Branch<Pair<Integer,Triple>>(sortedStar, leaf);
                }
            }
            long c = cssCostTreeSubj(sortedStar, predicateIndex);
            if (c > 0) {
                vTree.add(sortedStar);
            }
        }
        return vTree;
    }

    public static Vector<Tree<Pair<Integer,Triple>>> getStarJoinOrderObj(Set<Triple> ts, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) { 
        HashSet<String> ps = new HashSet<String>();
        HashMap<String, Set<Triple>> map = new HashMap<String, Set<Triple>>();
        HashSet<Triple> remainingTriples = new HashSet<Triple>();
        for (Triple t : ts) {
            Node p = t.getPredicate(); 
            if (p.isURI()) {
                String pStr = "<"+p.getURI()+">";
                ps.add(pStr);
                Set<Triple> tsAux = map.get(pStr);
                if (tsAux == null) {
                    tsAux = new HashSet<Triple>();
                }
                tsAux.add(t);
                map.put(pStr, tsAux);
            } else {
                remainingTriples.add(t);
            }
        }
        HashMap<Integer, HashSet<Integer>> dsCss = computeRelevantCS(ps, predicateIndex);
        //System.out.println("dsCss for the star: "+dsCss+". ps: "+ps);
        Vector<Tree<Pair<Integer,Triple>>> vTree = new Vector<Tree<Pair<Integer,Triple>>>();
        //HashMap<Integer, Tree<Triple>> dsTree = new HashMap<Integer, Tree<Triple>>();
        if (dsCss == null) {
            return vTree;
        }
        for (Integer ds : dsCss.keySet()) {
            HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> css = getCSSObj(ds);
            HashMap<Integer, Integer> hc = getHCObj(ds);
            HashMap<Integer, Set<String>> additionalSets = getAdditionalSetsObj(ds);
            HashMap<Integer, Integer> cost = getCostObj(ds);
            HashMap<String, HashSet<Integer>> predicateIndexDS = getPredicateIndexObj(ds, predicateIndex);
            LinkedList<String> orderedPs = produceStarJoinOrdering.getStarJoinOrdering(ps, css, hc, additionalSets, predicateIndexDS, cost);
            //System.out.println("list of sorted predicates: "+orderedPs);
            Tree<Pair<Integer,Triple>> sortedStar = convertToTreeS(orderedPs, map, ds, predicateIndex, false);
            //System.out.println("tree of sorted triples: "+sortedStar);
            for (Triple t : remainingTriples) {
                Leaf<Pair<Integer,Triple>> leaf = new Leaf<Pair<Integer,Triple>>(new Pair<Integer,Triple>(ds, t));
                if (sortedStar == null) {
                    sortedStar = leaf;
                } else {
                    sortedStar = new Branch<Pair<Integer,Triple>>(sortedStar, leaf);
                }
            }
            long c = cssCostTreeObj(sortedStar, predicateIndex);
            if (c > 0) {
                vTree.add(sortedStar);
            }
        }
        return vTree;
    }

    public static Node update(HashSet<Triple> triples, HashSet<Triple> star, int i, HashMap<Node, Vector<Tree<Pair<Integer,Triple>>>> map) {

        boolean subjCenter = centerIsSubject(star);
        //System.out.println("updating ts: "+ts+" for star: "+star);
        //HashSet<Triple> tsAux = new HashSet<Triple>();
        triples.removeAll(star);
        //System.out.println("ts after removing star triples: "+ts);
        HashSet<Node> vsStar = new HashSet<Node>();
        HashMap<Node, HashSet<Triple>> vsRest = new HashMap<Node, HashSet<Triple>>();
        //int nonCenterLinks = 0;
        Node nv = null;
        if (subjCenter) {
            nv = NodeFactory.createVariable("Subj"+i);
        } else {
            nv = NodeFactory.createVariable("Obj"+i);
        }
        //Node np = NodeFactory.createVariable("Pred"+i);
        //Node no = NodeFactory.createVariable("Obj"+i);
        //Triple nt = new Triple(nv, np, no);

        for (Triple t : triples) {
            Node s = t.getSubject();
            Node o = t.getObject();
            HashSet<Triple> aux = vsRest.get(s);
            if (aux == null) {
                aux = new HashSet<Triple>();
            }
            aux.add(t);
            vsRest.put(s, aux);
            aux = vsRest.get(o);
            if (aux == null) {
                aux = new HashSet<Triple>();
            }
            aux.add(t);
            vsRest.put(o, aux);
        }
        //System.out.println("vsRest: "+vsRest);
        HashSet<Triple> toRemove = new HashSet<Triple>();
        Node c = getCenter(star);
        int centerLinks = 0;
        int otherLinks = 0;
        for (Triple t : star) {
            Node s = t.getSubject();
            Node o = t.getObject();
            // keep connections of the star with the remaining triples
            int nsc = 0;
            int noc = 0;
            if (vsRest.containsKey(s)) {
                nsc += vsRest.get(s).size();
            }
            if (map.containsKey(s)) {
                nsc += map.get(s).get(0).getElements().size();
            }
            if (vsRest.containsKey(o)) {
                noc += vsRest.get(o).size();
            }
            if (map.containsKey(o)) {
                noc += map.get(o).get(0).getElements().size();
            }
            if (subjCenter) {
                centerLinks += nsc;
                otherLinks += noc;
                if (noc > 0) {
                //ts.add(t);
                    toRemove.add(t);
                }
            }
            if (!subjCenter) {
                centerLinks += noc;
                otherLinks += nsc;
                if (nsc > 0) {
                //ts.add(t);
                    toRemove.add(t);
                }
            }
        }
        if ((otherLinks > 0)) { // || (otherLinks == 1 && centerLinks > 0)) {
            star.removeAll(toRemove);
            triples.addAll(toRemove);
            toRemove.clear();
        }
        //System.out.println("ts after adding star connecting triples: "+ts);
        //System.out.println("toRemove: "+toRemove);
        // triples should not appear twice, so remove connections from the star
        /*if (toRemove.size()>0) {
            star.removeAll(toRemove);
            toRemove.clear();
        } else { // stars not connected by an object also need a triple as connection
            Triple t = star.iterator().next();
            star.remove(t);
            ts.add(t);
        }*/
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
        for (Triple t : triples) {
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
        triples.removeAll(toRemove);
        triples.addAll(toAdd);
        return nv;
    }

    public static Vector<HashSet<Triple>> getStars(HashSet<Triple> triples, long budget, HashMap<String, HashMap<Integer,HashSet<Integer>>>  predicateIndexSubj, HashMap<String, HashMap<Integer,HashSet<Integer>>>  predicateIndexObj) {

        Vector<HashSet<Triple>> stars = new Vector<HashSet<Triple>>();
        List<Triple> ts = new LinkedList<Triple>(triples);
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
            if (card == 0) {
                stars.clear();
                return stars;
            }
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
            if (card == 0) {
                stars.clear();
                return stars;
            }
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

        return stars;
    }

    public static HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> readCPS(String file) {
        HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> cps = null;
        try {
            ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)));

            cps = (HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>) in.readObject();
        } catch (Exception e) {
            System.err.println("Problems reading file: "+file);
            e.printStackTrace();
            System.exit(1);
        }
        return cps;
    }
}

