import java.io.*;
import java.util.*;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.query.QueryFactory;

import com.hp.hpl.jena.sparql.expr.ExprList;

import com.hp.hpl.jena.sparql.algebra.*;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.syntax.*;

import com.fluidops.fedx.*;
import com.fluidops.fedx.structures.Endpoint;
import org.openrdf.query.*;

class evaluateSPARQLQuery {
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
    static Vector<String> generalPredicates = new Vector<String>();
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
                fileCPS += "/"+datasetStr1+"/statistics"+datasetStr1+"_cps_reduced10000CS";
            } else {
                fileCPS += "/cps_"+datasetStr1+"_"+datasetStr2+"_reduced10000CS_MIP";
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
                fileCPS += "/"+datasetStr1+"/statistics"+datasetStr1+"_cps_subj_obj_reduced10000CS";
            } else {
                fileCPS += "/cps_"+datasetStr1+"_"+datasetStr2+"_subj_obj_reduced10000CS_MIP";
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

    public static Vector<String> readPredicates(String file) {
        Vector<String> ps = new Vector<String>();
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

    private static ArrayList<HashSet<Triple>> getBGPs(Query query) {

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

    public static void main(String[] args) {

        String queryFile = args[0];
        String datasetsFile = args[1];
        folder = args[2];
        long budget = Long.parseLong(args[3]);
        includeMultiplicity = Boolean.parseBoolean(args[4]);
        original = Boolean.parseBoolean(args[5]);
        String fileName = args[6];
        String generalPredicatesFile = args[7];
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
            Vector<HashSet<Triple>> stars = getStars(triples, budget, predicateIndexSubj, predicateIndexObj); //css, predicateIndex, cost);
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
                Node nn = update(triples, renamedStar, i, map);
                // current star should be consistent with renamed star
                s.clear();
                for (Triple t : renamedStar) {
                    Triple r = renamed.get(t);
                    s.add(r);
                }
                //LOG System.out.println("updated bgp "+bgp+" for star : "+s+" and for nn: "+nn);
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
                        triples.clear();
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
                    DPTable.put(ns, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(p, new Pair<Long, Long>(cost,cost))); //0L))); 
                }
            }
        
            //System.out.println("map: "+map);
            //System.out.println("DPTable before add.. :"+DPTable);
            addRemainingTriples(DPTable, triples, predicateIndexSubj, predicateIndexObj, nodes, map); //, css, cps, predicateIndex, triples, cost, map, hc, additionalSets);
            //System.out.println("DPTable after add.. :"+DPTable);
            // may have to consider already intermediate results in the CP estimation for the cost
            HashMap<HashSet<Node>, Double> selectivity = new HashMap<HashSet<Node>, Double>();
            //HashMap<HashSet<Node>, Vector<Tree<Pair<Integer,Triple>>>> selectivity = new HashMap<HashSet<Node>, Double>();
            estimateSelectivityCP(DPTable, predicateIndexSubj, predicateIndexObj, nodes, triples, map, selectivity); //css, cps, predicateIndex, triples, hc, additionalSets, cost);
            //System.out.println("DPTable after CP estimation.. :"+DPTable);
            //LOG System.out.println("selectivity :"+selectivity);
            //System.out.println("nodes :"+nodes);
            computeJoinOrderingDP(DPTable, nodes, selectivity);
            Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long, Long>> res = DPTable.get(nodes);
            if (res != null) {
                plans.put(ts, res.getFirst());
                //System.out.println(res.getFirst());
            }
        }
        Query newQuery = produceQueryWithServiceClauses(query, plans);
        if (newQuery != null) {
            //System.out.println("Plan: "+newQuery);
            //write(newQuery.toString(), fileName);
            evaluate(newQuery.toString());
        } else {
            System.out.println("No plan was found");
        }
        //System.out.println("DPTable at the end: "+DPTable);
    }

    public static void evaluate(String queryStr) {

        String fedxConfig = "/home/roott/federatedOptimizer/lib/fedX3.1/config2";
        //System.out.println("evaluating "+queryStr);
        try {
            FedXFactory.initializeFederation(fedxConfig, Collections.<Endpoint>emptyList());

            TupleQuery query = QueryManager.prepareTupleQuery(queryStr);
            TupleQueryResult res = query.evaluate();
            int n=0;
            while (res.hasNext()) {
                BindingSet bs = res.next();
	        //System.out.println(bs);
                n++;
            }
            //System.out.println("finished");
            FederationManager.getInstance().shutDown();
            System.out.println("results="+n);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error executing query");
            System.exit(1);
        }
    }

    public static Query produceQueryWithServiceClauses(Query query, HashMap<HashSet<Triple>, Vector<Tree<Pair<Integer,Triple>>>> plans) {

        Op op = (new AlgebraGenerator()).compile(query);
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
        //LOG System.out.println("computeJoinOrderingDPAux, i: "+i);
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
                    Long costLeft = DPTable.get(ns).getSecond().getSecond();
                    Long costRight = DPTable.get(sss).getSecond().getSecond();
                    if (costRight < costLeft) {
                        Vector<Tree<Pair<Integer,Triple>>> tmp = vTreeNS;
                        vTreeNS = vTreeSS;
                        vTreeSS = tmp;
                    }
                    Long cost = card + DPTable.get(ns).getSecond().getSecond()+DPTable.get(sss).getSecond().getSecond();
                    HashSet<Node> newEntry = new HashSet<Node>(ns);
                    newEntry.addAll(sss);
                    //LOG System.out.println("newEntry: "+newEntry+". cost: "+cost);
                    Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>> pair = DPTableAux.get(newEntry);
                    if ((pair == null) || pair.getSecond().getSecond()>cost) {
                        Vector<Tree<Pair<Integer,Triple>>> order = makeTree(vTreeNS,vTreeSS);
                        //LOG System.out.println("merging : "+ns+" and "+sss+". card: "+card+". cost: "+cost+". pair: "+pair+". newEntry: "+newEntry+". order: "+order);
                        DPTableAux.put(newEntry, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(order, new Pair<Long,Long>(card, cost)));
                    }
                }
            }
        }
        DPTable.putAll(DPTableAux);
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

    public static void addRemainingTriples(HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable, HashSet<Triple> triples, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexSubj, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexObj, HashSet<Node> nodes, HashMap<Node, Vector<Tree<Pair<Integer,Triple>>>> map) {

        //System.out.println("ts: "+ts);
        //System.out.println("map: "+map);
        HashSet<Node> toAdd = new HashSet<Node>();
        for (Triple t : triples) {
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
                    DPTable.put(newEntry, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(vector, new Pair<Long, Long>(c,c))); //0L))); 
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
                    DPTable.put(newEntry, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(vector, new Pair<Long, Long>(c,c))); //0L))); 
                }
            }
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

    public static void estimateSelectivityCP(HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexSubj, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexObj, HashSet<Node> nodes, HashSet<Triple> triples, HashMap<Node, Vector<Tree<Pair<Integer,Triple>>>> map, HashMap<HashSet<Node>, Double> selectivity) {

        HashMap<Triple, Triple> renamed = new HashMap<Triple, Triple>();
        HashSet<Triple> newTs = renameBack(new HashSet<Triple>(triples), map, renamed);
        Vector<HashSet<Node>> toRemove = new Vector<HashSet<Node>>();
        for (Triple t : triples) {
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
            for (Triple t1 : triples) { // t1 has meta-nodes
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
            if (ts3.size() > 0 && ts5.size() > 0 && centerIsSubject(ts3) && centerIsSubject(ts5) && existsCPConnectionSubj(ts3, ts5) && (existsNonGeneralPredicate(ts3) || existsNonGeneralPredicate(ts5))) {
                Vector<Tree<Pair<Integer,Triple>>> p = getStarJoinOrderSubj(ts3, predicateIndexSubj);
                Vector<Tree<Pair<Integer,Triple>>> q = getStarJoinOrderSubj(ts5, predicateIndexSubj);
                if (p.size()>0 && q.size()>0) {
                    boolean tmpAdded = addCheapestCPSubj(p, q, keyS, keyO, predicateIndexSubj, DPTable);
                    added = added || tmpAdded;
                }
            }
            if (ts4.size() > 0 && ts6.size() > 0 && centerIsObject(ts4) && centerIsObject(ts6) && existsCPConnectionObj(ts4, ts6) && (existsNonGeneralPredicate(ts4) || existsNonGeneralPredicate(ts6))) {
                Vector<Tree<Pair<Integer,Triple>>> p = getStarJoinOrderObj(ts4, predicateIndexObj);
                Vector<Tree<Pair<Integer,Triple>>> q = getStarJoinOrderObj(ts6, predicateIndexObj);
                if (p.size()>0 && q.size()>0) {
                    boolean tmpAdded = addCheapestCPObj(p, q, keyS, keyO, predicateIndexObj, DPTable);
                    added = added || tmpAdded;
                }
            }
            if (ts3.size() > 0 && ts6.size() > 0 && centerIsSubject(ts3) && centerIsObject(ts6) && existsCPConnectionSubjObj(ts3, ts6) && (existsNonGeneralPredicate(ts3) || existsNonGeneralPredicate(ts6))) {
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
                long tmpCost = getCostCPSubj(obtainTriples(leftTree), sourceLT, obtainTriples(rightTree), sourceRT, predicateIndex);
                //LOG System.out.println("tmpCost: "+tmpCost);
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

    public static Long getMultiplicitySubj(Integer m, String p,  Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs, HashSet<String> ps1, HashMap<String, Triple> map1, Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs2, HashSet<String> ps2, HashMap<String, Triple> map2) {

        double sel = 1.0;
        //double selTmp = 1.0;
        Integer count = cPs.getFirst();
        //System.out.println("p: "+p);
        for (String p1 : ps1) {
            Triple t = map1.get(p1);
            Node s = t.getSubject();
            Node o = t.getObject();
            // COMMENT THE CONDITIONAL TO STICK TO THE PAPER FORMULA
            if (original || !p1.equals(p)) {
                //System.out.println("p1: "+p1);
                boolean c = includeMultiplicity && projectedVariables.contains(s) && (!distinct || projectedVariables.contains(o));
                if (c) {
                    Integer p_m = cPs.getSecond().get(p1).getFirst();
                    sel = sel*(((double)p_m)/count);
                    //System.out.println(p+": "+css.get(cs).getSecond().get(p)); 
                }
            }
                /*if (!o.isVariable()) {
                    Integer p_m = cPs.getSecond().get(p1).getFirst();
                    Integer p_d = cPs.getSecond().get(p1).getSecond();
                    System.out.println("o: "+o+". p_m: "+p_m+". p_d: "+p_d);
                    double tmp = 1.0/ p_d;
                    if (tmp < selTmp) {
                        selTmp = tmp;
                    }
                }*/
            
        }
        //System.out.println("selTmp: "+selTmp);
        //sel = sel * selTmp; // CONSIDER CONSTANTS first star
        //selTmp = 1.0;
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
            /*if (!o.isVariable()) {
                Integer p_m = cPs2.getSecond().get(p2).getFirst();
                Integer p_d = cPs2.getSecond().get(p2).getSecond();
                System.out.println("o: "+o+". p_m: "+p_m+". p_d: "+p_d);
                double tmp = 1.0/ p_d;
                if (tmp < selTmp) {
                    selTmp = tmp;
                }
            }*/
        }
        //System.out.println("selTmp: "+selTmp);
        //sel = sel * selTmp;  // CONSIDER CONSTANTS second star
        //System.out.println("m: "+m+". sel: "+sel);
        //System.out.println("sel: "+sel);
        return Math.round(Math.ceil(m*sel));
    }

    public static Long getMultiplicitySubjObj(Integer m, String p,  Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs, HashSet<String> ps1, HashMap<String, Triple> map1, Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs2, HashSet<String> ps2, HashMap<String, Triple> map2) {

        double sel = 1.0;
        //double selTmp = 1.0;
        Integer count = cPs.getFirst();
        //System.out.println("p: "+p);
        for (String p1 : ps1) {
            Triple t = map1.get(p1);
            Node s = t.getSubject();
            Node o = t.getObject();
            // COMMENT THE CONDITIONAL TO STICK TO THE PAPER FORMULA
            if (original || !p1.equals(p)) {
                //System.out.println("p1: "+p1);
                boolean c = includeMultiplicity && projectedVariables.contains(s) && (!distinct || projectedVariables.contains(o));
                if (c) {
                    Integer p_m = cPs.getSecond().get(p1).getFirst();
                    sel = sel*(((double)p_m)/count);
                    //System.out.println(p+": "+css.get(cs).getSecond().get(p)); 
                }
            }
                /*if (!o.isVariable()) {
                    Integer p_m = cPs.getSecond().get(p1).getFirst();
                    Integer p_d = cPs.getSecond().get(p1).getSecond();
                    //System.out.println("o: "+o+". p_m: "+p_m+". p_d: "+p_d);
                    double tmp = 1.0/ p_d;
                    if (tmp < selTmp) {
                        selTmp = tmp;
                    }
                }*/
            
        }
        //System.out.println("selTmp: "+selTmp);
        //sel = sel * selTmp; // CONSIDER CONSTANTS first star
        //selTmp = 1.0;
        count = cPs2.getFirst();
        for (String p2 : ps2) {
            Triple t = map2.get(p2);
            Node s = t.getSubject();
            Node o = t.getObject();
            //System.out.println("p2: "+p2);
            if (original || !p2.equals(p)) {
                boolean c = includeMultiplicity && projectedVariables.contains(o) && (!distinct || projectedVariables.contains(s));
                if (c) {
                    Integer p_m = cPs2.getSecond().get(p2).getFirst();
                    sel = sel*(((double)p_m)/count);
                    //System.out.println(p+": "+css.get(cs).getSecond().get(p)); 
                }
            }
                /*if (!s.isVariable()) {
                    Integer p_m = cPs2.getSecond().get(p2).getFirst();
                    Integer p_d = cPs2.getSecond().get(p2).getSecond();
                    //System.out.println("s: "+s+". p_m: "+p_m+". p_d: "+p_d);
                    double tmp = 1.0/ p_d;
                    if (tmp < selTmp) {
                        selTmp = tmp;
                    }
                }*/
            
        }
        //System.out.println("selTmp: "+selTmp);
        //sel = sel * selTmp;  // CONSIDER CONSTANTS second star
        //System.out.println("m: "+m+". sel: "+sel);
        //System.out.println("sel: "+sel);
        return Math.round(Math.ceil(m*sel));
    }

    public static Long getMultiplicityObj(Integer m, String p,  Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs, HashSet<String> ps1, HashMap<String, Triple> map1, Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs2, HashSet<String> ps2, HashMap<String, Triple> map2) {

        double sel = 1.0;
        //double selTmp = 1.0;
        Integer count = cPs.getFirst();
        //System.out.println("p: "+p);
        for (String p1 : ps1) {
                //System.out.println("p1: "+p1);
            Triple t = map1.get(p1);
            Node s = t.getSubject();
            Node o = t.getObject();
            if (original || !p1.equals(p)) {
                boolean c = includeMultiplicity && projectedVariables.contains(o) && (!distinct || projectedVariables.contains(s));
                if (c) {
                    Integer p_m = cPs.getSecond().get(p1).getFirst();
                    sel = sel*(((double)p_m)/count);
                    //System.out.println(p+": "+css.get(cs).getSecond().get(p)); 
                }
            }
                /*if (!s.isVariable()) {
                    Integer p_m = cPs.getSecond().get(p1).getFirst();
                    Integer p_d = cPs.getSecond().get(p1).getSecond();
                    //System.out.println("s: "+s+". p_m: "+p_m+". p_d: "+p_d);
                    double tmp = 1.0/ p_d;
                    if (tmp < selTmp) {
                        selTmp = tmp;
                    }
                }*/
            
        }
        //System.out.println("selTmp: "+selTmp);
        //sel = sel * selTmp; // CONSIDER CONSTANTS first star
        //selTmp = 1.0;
        count = cPs2.getFirst();
        for (String p2 : ps2) {
            Triple t = map2.get(p2);
            Node s = t.getSubject();
            Node o = t.getObject();
            //System.out.println("p2: "+p2);
//            if (original || !p2.equals(p)) {
                boolean c = includeMultiplicity && projectedVariables.contains(o) && (!distinct || projectedVariables.contains(s));
                if (c) {
                    Integer p_m = cPs2.getSecond().get(p2).getFirst();
                    sel = sel*(((double)p_m)/count);
                    //System.out.println(p+": "+css.get(cs).getSecond().get(p)); 
                }
            //}
                /*if (!s.isVariable()) {
                    Integer p_m = cPs2.getSecond().get(p2).getFirst();
                    Integer p_d = cPs2.getSecond().get(p2).getSecond();
                    //System.out.println("s: "+s+"p_m: "+p_m+". p_d: "+p_d);
                    double tmp = 1.0/ p_d;
                    if (tmp < selTmp) {
                        selTmp = tmp;
                    }
                }*/
            
        }
        //System.out.println("selTmp: "+selTmp);
        //sel = sel * selTmp;  // CONSIDER CONSTANTS second star
        //System.out.println("m: "+m+". sel: "+sel);
        //System.out.println("sel: "+sel);
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

        HashSet<Integer> css1 = computeRelevantCS(ps1, ds1, predicateIndex);
        HashSet<Integer> css2 = computeRelevantCS(ps2, ds2, predicateIndex);
        //System.out.println("there are "+css1.size()+" relevant css for "+ps1+" and "+ds1);
        //System.out.println("there are "+css2.size()+" relevant css for "+ps2+" and "+ds2);
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
                        //System.out.println("There are "+links12.size()+" 12-linking predicates between the css");
                        HashSet<String> relevantLinks = new HashSet<String>(links12.keySet());
                        relevantLinks.retainAll(ps1);
                        for (String p : relevantLinks) {
                            Triple t1 = map1.get(p);
                            Triple t2 = sq2.iterator().next();
                            if (t1.getObject().equals(t2.getSubject())) {
                                Long m1 = getMultiplicitySubj(links12.get(p), p, cPs, ps1, map1, cPs2, ps2, map2);
                                //System.out.println("(S1) multiplicity went from "+links12.get(p)+" to "+m1+" for predicate "+p);
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
                        //System.out.println("There are "+links21.size()+" 21-linking predicates between the css");
                        HashSet<String> relevantLinks = new HashSet<String>(links21.keySet());
                        relevantLinks.retainAll(ps2);
                        for (String p : relevantLinks) {
                            Triple t2 = map2.get(p);
                            Triple t1 = sq1.iterator().next();
                            if (t2.getObject().equals(t1.getSubject())) {
                                Long m1 = getMultiplicitySubj(links21.get(p), p, cPs2, ps2, map2, cPs, ps1, map1);
                                //System.out.println("(S2) multiplicity went from "+links21.get(p)+" to "+m1+" for predicate "+p);
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

        HashSet<Integer> css1 = computeRelevantCS(ps1, ds1, predicateIndexSubj);
        HashSet<Integer> css2 = computeRelevantCS(ps2, ds2, predicateIndexObj);
        //LOG System.out.println("there are "+css1.size()+" relevant css for "+ps1+" and "+ds1);
        //LOG System.out.println("there are "+css2.size()+" relevant css for "+ps2+" and "+ds2);

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
                        //LOG System.out.println("There are "+links12.size()+" 12-linking predicates between the css");
                        HashSet<String> relevantLinks = new HashSet<String>(links12.keySet());
                        relevantLinks.retainAll(ps1);
                        for (String p : relevantLinks) {
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

        HashSet<Integer> css1 = computeRelevantCS(ps1, ds1, predicateIndex);
        HashSet<Integer> css2 = computeRelevantCS(ps2, ds2, predicateIndex);
        //LOG System.out.println("there are "+css1.size()+" relevant css for "+ps1+" and "+ds1);
        //LOG System.out.println("there are "+css2.size()+" relevant css for "+ps2+" and "+ds2);

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
                        //LOG System.out.println("There are "+links12.size()+" 12-linking predicates between the css");
                        HashSet<String> relevantLinks = new HashSet<String>(links12.keySet());
                        relevantLinks.retainAll(ps1);
                        for (String p : relevantLinks) {
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
                    //System.out.println("Links 2 -> 1");
                    HashMap<String, Integer> links21 = css2Ps.get(cs1);
                    if (links21 != null) {
                        //LOG System.out.println("There are "+links21.size()+" 21-linking predicates between the css");
                        HashSet<String> relevantLinks = new HashSet<String>(links21.keySet());
                        relevantLinks.retainAll(ps2);
                        for (String p : relevantLinks) {
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
                return intersection;
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

    public static long computeCost(Integer ds, HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> css, HashSet<Integer> relevantCss, Set<String> ps, HashMap<String, Triple> map, boolean subjStar) {
        Set<Triple> sq = new HashSet<Triple>();
        for (String p : ps) {
            sq.add(map.get(p));
        }
        double selCttes = getConstantSelectivity(sq, ds);
        long cost = 0L;
        //System.out.println("map: "+map+". ps: "+ps);
        for (Integer cs : relevantCss) {
            //System.out.println("cs: "+css.get(cs));
            long costTmp = css.get(cs).getFirst();
            double sel = 1.0;
            //System.out.println("distinct: "+costTmp);
            //double selTmp = 1.0;
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
                /*if (subjStar) {
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
                }*/
              }
            //}
            //sel = sel * selTmp; // CONSIDER CONSTANTS
            //System.out.println("costTmp: "+costTmp+". sel: "+sel);
            cost += Math.round(Math.ceil(costTmp*sel));
        }
        //System.out.println("cost: "+cost);
        //return cost;
        return Math.round(Math.ceil(cost*selCttes));
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
            long c = cssCostTreeSubj(sortedStar, predicateIndex);
            if (c > 0) {
                vTree.add(sortedStar);
            }
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

