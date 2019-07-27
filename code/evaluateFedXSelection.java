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
import com.hp.hpl.jena.sparql.algebra.AlgebraGenerator;
import com.hp.hpl.jena.sparql.algebra.OpWalker;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

class evaluateFedXSelection {
    // DatasetId --> Position, for data related to CS-Subj
    static HashMap<Integer, Integer> datasetsIdPosSubj = new HashMap<Integer,Integer>();
    // DatasetId --> DatasetId --> Position, for data related to CP between CS-Subjs
    static HashMap<Integer, HashMap<Integer, Integer>> datasetsIdsPosSubj = new HashMap<Integer, HashMap<Integer,Integer>>();
    static Vector<HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>>> cssSubj = new Vector<HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>>>();
    static Vector<HashMap<Integer, Integer>> hcsSubj = new Vector<HashMap<Integer, Integer>>();
    static Vector<HashMap<Integer, Integer>> hcsObj = new Vector<HashMap<Integer, Integer>>();
    static Vector<HashMap<Integer, Set<String>>> additionalSetsSubj = new Vector<HashMap<Integer, Set<String>>>();
    static Vector<HashMap<Integer, Integer>> costsSubj = new Vector<HashMap<Integer, Integer>>();
    static Vector<HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>> cpsSubj = new Vector<HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>>();
    static Vector<String> datasets = new Vector<String>();
    static Vector<String> endpoints = new Vector<String>();
    static String folder;
    static HashMap<Integer,HashMap<String, HashSet<Integer>>> predicateIndexesSubj = new HashMap<Integer,HashMap<String, HashSet<Integer>>>();
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

    public static HashMap<Integer, Set<String>> getAdditionalSetsSubj(Integer ds) {
        Integer pos = datasetsIdPosSubj.get(ds);
        if (pos == null) {
            pos = loadFilesSubj(ds);
        }
        HashMap<Integer, Set<String>> ass = additionalSetsSubj.get(pos);
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

    public static HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> getCSSSubj(Integer ds) {
        Integer pos = datasetsIdPosSubj.get(ds);
        if (pos == null) {
            pos = loadFilesSubj(ds);
        }
        HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer,Integer>>>> cs = cssSubj.get(pos);
        return cs;
    }

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
                fileCPS += "/statistics"+datasetStr1+"_cps_reduced10000CS";
            } else {
                fileCPS += "/cps_"+datasetStr1+"_"+datasetStr2+"_one_rtree_reduced10000CS_1"; 
            }
            c = readCPS(fileCPS);
            cpsSubj.add(pos, c);
        } 
        return c;
    }

    public static int loadFilesSubj(Integer ds) {
        int pos = cssSubj.size();
        datasetsIdPosSubj.put(ds, pos);
        String datasetStr = datasets.get(ds);
        String fileCSS = folder+"/statistics"+datasetStr+"_css_reduced10000CS";
        HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> cs = produceStarJoinOrdering.readCSS(fileCSS);
        cssSubj.add(pos, cs);
        String fileHC = folder+"/statistics"+datasetStr+"_hc_reduced10000CS";
        HashMap<Integer, Integer> hc = produceStarJoinOrdering.readMap(fileHC);
        hcsSubj.add(pos, hc);
        String fileAdditionalSets = folder+"/statistics"+datasetStr+"_as_reduced10000CS";
        HashMap<Integer, Set<String>> ass = produceStarJoinOrdering.readAdditionalSets(fileAdditionalSets);
        additionalSetsSubj.add(pos, ass);
        String fileCost = folder+"/statistics"+datasetStr+"_cost_reduced10000CS";
        HashMap<Integer, Integer> cost = produceStarJoinOrdering.readMap(fileCost);
        costsSubj.add(pos, cost);
        return pos;
    }

    public static HashMap<String, HashSet<Integer>> getPredicateIndexSubj(Integer ds, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) {
        Integer pos = datasetsIdPosSubj.get(ds);
        HashMap<String, HashSet<Integer>> predIndex = null;
        if (pos == null) {
            pos = loadFilesSubj(ds);
        }
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

    public static HashMap<String, HashMap<Integer,HashSet<Integer>>> readPredicateIndexesSubj(String folder, Vector<String> datasets) {
        return readPredicateIndexes(folder, datasets, "");
    }

    public static void loadStatistics() {

        for (int ds1 = 0; ds1 < datasets.size(); ds1++) {
            loadFilesSubj(ds1);
            for (int ds2 = 0; ds2 < datasets.size(); ds2++) {
                HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> aux = getCPSSubj(ds1, ds2);
            }
        }
    }

    public static HashMap<String, HashMap<Integer,HashSet<Integer>>> readPredicateIndexes(String folder, Vector<String> datasets, String suffix) {

        HashMap<String, HashMap<Integer,HashSet<Integer>>> predIndex = new HashMap<String, HashMap<Integer,HashSet<Integer>>>();
        for (int i = 0; i < datasets.size(); i++) {
            String datasetStr = datasets.get(i);
            String fileName = folder+"/statistics"+datasetStr+"_pi"+suffix+"_reduced10000CS";
            
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

    public static void main(String[] args) throws Exception {
        long t0 = System.currentTimeMillis();
        String queryFile = args[0];
        int pos = queryFile.lastIndexOf("/");
        String queryId = queryFile.substring(pos>=0?(pos+1):0);
        String datasetsFile = args[1];
        folder = args[2];
        long budget = Long.parseLong(args[3]);
        includeMultiplicity = Boolean.parseBoolean(args[4]);
        original = Boolean.parseBoolean(args[5]);
        String fedXConfigFile = args[6];
        String fedXDescriptionFile = args[7];
        evaluateSPARQLQuery.prepareFedX(fedXConfigFile, fedXDescriptionFile);
        datasets = readDatasets(datasetsFile);
        loadStatistics();
        // Predicate --> DatasetId --> set of CSId
        HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexSubj = readPredicateIndexesSubj(folder, datasets); 
        Query query = QueryFactory.read(queryFile);
        ArrayList<HashSet<Triple>> bgps = getBGPs(query);
        HashMap<HashSet<Triple>, Vector<Tree<Pair<Integer,Triple>>>> plans = new HashMap<HashSet<Triple>, Vector<Tree<Pair<Integer,Triple>>>>();
        distinct = query.isDistinct();
        projectedVariables = query.getProjectVars();
        long t1 = System.currentTimeMillis();
        System.out.println("loading: "+(t1-t0));
        Config.getConfig().set("optimize", "true");
        String queryString = Files.lines(Paths.get(queryFile)).collect(Collectors.joining("\n"));
        long t2 = System.currentTimeMillis();
        HashMap<Triple, Set<Integer>> selection = bridge.getSelection(queryString, endpoints);
        t2 = System.currentTimeMillis() - t2;
        System.out.println("decomposition="+t2+"ms");
        long t3 = System.currentTimeMillis();
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
            Vector<HashSet<Integer>> sources = new Vector<HashSet<Integer>>();
            Vector<SubQuery> stars = getStars(triples, budget, predicateIndexSubj, selection, sources); 
            int i = 1;
            HashSet<Node> nodes = new HashSet<Node>();
            HashMap<Node, Vector<Tree<Pair<Integer,Triple>>>> map = new HashMap<Node, Vector<Tree<Pair<Integer,Triple>>>>();
            int j = 0;
            for (SubQuery sq : stars) {
                Set<Integer> ss = sources.get(j++);
                Node nn = NodeFactory.createVariable("Star"+i); 
                    Vector<Tree<Pair<Integer,Triple>>> p = null;
                    if (sq.isSubjectStar()) {
                        p = subjectCssSQJoinOrderFed(sq, ss, predicateIndexSubj);
                    } 
                    if (p.size() ==0) {  // test case missing sources for a star
                        nodes.clear();
                        map.clear();
                        triples.clear();
                        break;
                    }
                    long cost = 0L;
                    if (sq.isSubjectStar()) {
                        cost = subjectCssVSTCost(p, predicateIndexSubj);
                    } 
                    map.put(nn, p);
                    i++;
                    HashSet<Node> ns = new HashSet<Node>();
                    ns.add(nn);
                    nodes.add(nn);
                    triples.removeAll(sq.getTriples());
                    DPTable.put(ns, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(p, new Pair<Long, Long>(cost,cost))); 
                
            }
            addRemainingTriples(DPTable, triples, predicateIndexSubj, nodes, map, selection); 
            HashMap<Pair<Integer, Triple>, HashMap<HashSet<Pair<Integer, Triple>>, Double>> selectivity = new HashMap<Pair<Integer, Triple>, HashMap<HashSet<Pair<Integer, Triple>>, Double>>();
            estimateSelectivityCP(DPTable, predicateIndexSubj, nodes, triples, map, selectivity, log); 
            computeJoinOrderingDP(DPTable, nodes, selectivity, log);
            Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long, Long>> res = DPTable.get(nodes);
            if (res != null) {
                plans.put(ts, res.getFirst());
            }
        }
        t3 = System.currentTimeMillis() - t3;
        System.out.println("ordering="+t3+"ms");
        Query newQuery = produceQueryWithServiceClauses(query, plans);
        if (newQuery != null) {
            Op op = (new AlgebraGenerator()).compile(newQuery);
            VisitorCountTriples vct = new VisitorCountTriples();
            OpWalker.walk(op, vct);
            int c = vct.getCount();
            System.out.println("NumberSelectedSources="+c);
            VisitorCountServices vcs = new VisitorCountServices();
            OpWalker.walk(op, vcs);
            c = vcs.getCount();
            System.out.println("NumberServices="+c);
            System.out.println("Plan: "+newQuery);
            evaluate(newQuery.toString(), queryId, false);
        } else {
            evaluate(query.toString(), queryId, true);
        }
        FederationManager.getInstance().shutDown();
        System.exit(0);
    }

    public static void evaluate(String queryStr, String queryId, boolean optimize) {

        try {
            Config.getConfig().set("optimize", ""+optimize);
            TupleQuery query = QueryManager.prepareTupleQuery(queryStr);
            long start = System.currentTimeMillis();
            TupleQueryResult res = query.evaluate();
            int n=0;
            while (res.hasNext()) {
	        System.out.println(res.next());
                n++;
            }
            long duration = System.currentTimeMillis() - start; 

            System.out.println("Done query " + queryId + ": duration=" + duration + "ms, results=" + n);

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
            Set<Triple> triples = obtainTriples(tree);
            for (Triple t : triples) {
                Node s = t.getSubject();
                Node o = t.getObject(); 
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
        HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTableAux = new HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>>(DPTable);
        HashMap<HashSet<Node>, Pair<HashSet<Node>, HashSet<Node>>> logAux =  new HashMap<HashSet<Node>, Pair<HashSet<Node>, HashSet<Node>>>();
        for (HashSet<Node> ns : DPTable.keySet()) {
            Vector<Tree<Pair<Integer,Triple>>> aux = DPTable.get(ns).getFirst();
            if (aux.size()==0) {
                continue;
            }
            HashSet<Node> rn = new HashSet<Node>(nodes);
            rn.removeAll(ns);
            Vector<HashSet<Node>> subsets = new Vector<HashSet<Node>>();
            getAllSubsets(rn, i-ns.size(), subsets);
            for (HashSet<Node> ss : subsets) {
                if (ss.size() == 0 || !DPTable.containsKey(ss)) {
                    continue;
                }
                    HashSet<Node> sss  = ss;
                    Vector<Tree<Pair<Integer,Triple>>> order = getJoinTree(DPTable, ns, sss, selectivity);
                    Long card = sumCardinalities(order);
                    HashSet<Node> newEntry = new HashSet<Node>(ns);
                    newEntry.addAll(sss);
                    Long cost = costTransfer(order);
                    Vector<Tree<Pair<Integer,Triple>>> vTreeNS = DPTable.get(ns).getFirst();
                    Vector<Tree<Pair<Integer,Triple>>> vTreeSS = DPTable.get(sss).getFirst();
                    Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>> pair = DPTableAux.get(newEntry);
                    if ((pair == null) || pair.getSecond().getSecond()>cost) {
                        DPTableAux.put(newEntry, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(order, new Pair<Long,Long>(card, cost)));
                        Pair<HashSet<Node>, HashSet<Node>> pairAux = new Pair<HashSet<Node>, HashSet<Node>>(ns, sss);
                        logAux.put(newEntry, pairAux);
                    }
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

    public static long costTransfer(Vector<Tree<Pair<Integer,Triple>>> vTree) {

        long cost = 0L;

        for (Tree<Pair<Integer,Triple>> tree : vTree) {
            long c = 0L;
            try {
                c = costRec(tree);
            } catch (ArithmeticException e) {
                c = Long.MAX_VALUE;
            }
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
                            sel = sel*tmpSel;
                        }
                    }
                }
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

    public static void addRemainingTriples(HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable, HashSet<Triple> triples, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexSubj, HashSet<Node> nodes, HashMap<Node, Vector<Tree<Pair<Integer,Triple>>>> map, HashMap<Triple, Set<Integer>> selection) {

        HashSet<Node> toAdd = new HashSet<Node>();
        for (Triple t : triples) {
            Node s = t.getSubject();
            Node p = t.getPredicate();
            Node o = t.getObject();
                toAdd.add(s);
                toAdd.add(o);
                Collection<Triple> collection = new HashSet<Triple>();
                collection.add(t);
                HashSet<Node> newEntry = new HashSet<Node>();
                newEntry.add(s);
                newEntry.add(o);
                HashSet<Integer> relevantSources = getRelevantSources(collection, selection);
                SubQuery sq = new SubQuery(collection);
                sq.setSubjectStar();
                Vector<Tree<Pair<Integer,Triple>>> vector = subjectCssSQJoinOrderFed(sq, relevantSources, predicateIndexSubj);
                
                long c = subjectCssVSTCost(vector, predicateIndexSubj);

                Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>> data = DPTable.get(newEntry);
                long prevC = Long.MAX_VALUE;
                if (data != null) {
                    prevC = data.getSecond().getFirst();
                }
                // including the minimum cost if inferior to previous value
                if (c < prevC) { 
                    DPTable.put(newEntry, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(vector, new Pair<Long, Long>(c,c)));
                }
        }
        nodes.addAll(toAdd);
    }

    public static boolean existsCPConnectionSubj(Set<Triple> sq1, Set<Triple> sq2) {

        return existsCSConnectionSubj(sq1) && existsCSConnectionSubj(sq2) && (existsCPConnectionAuxS(sq1, sq2) || existsCPConnectionAuxS(sq2, sq1));
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

    public static boolean existsCPConnectionAuxS(Set<Triple> sq1, Set<Triple> sq2) {

        boolean e = false;
        Node c = sq2.iterator().next().getSubject();
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

    public static void estimateSelectivityCP(HashMap<HashSet<Node>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexSubj, HashSet<Node> nodes, HashSet<Triple> triples, HashMap<Node, Vector<Tree<Pair<Integer,Triple>>>> map, HashMap<Pair<Integer, Triple>, HashMap<HashSet<Pair<Integer, Triple>>, Double>> selectivity, HashMap<HashSet<Node>, Pair<HashSet<Node>, HashSet<Node>>> log) {
        HashMap<Triple, Triple> renamed = new HashMap<Triple, Triple>();
        HashSet<Triple> newTs = renameBack(new HashSet<Triple>(triples), map, renamed);
        Vector<HashSet<Node>> toRemove = new Vector<HashSet<Node>>();
        Set<HashSet<Node>> keySet = new HashSet<HashSet<Node>>(DPTable.keySet());
        for (HashSet<Node> set1 : keySet) {
            HashMap<Integer, Tree<Pair<Integer,Triple>>> plansSet1 = new HashMap<Integer, Tree<Pair<Integer,Triple>>>();
            Vector<Tree<Pair<Integer,Triple>>> value1 = DPTable.get(set1).getFirst();
            for (Tree<Pair<Integer,Triple>> tree : value1) {
                Integer source = tree.getOneElement().getFirst();
                plansSet1.put(source, tree);
            }
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
                Set<Triple> ts3 = new HashSet<Triple>();
                Set<Triple> ts4 = new HashSet<Triple>();
                Set<Triple> ts5 = new HashSet<Triple>();
                Set<Triple> ts6 = new HashSet<Triple>();
                // Subject centered triples from first set
                if (ts1.size()>0 && cS1) {
                    ts3.addAll(ts1);
                }
                // Subject centered triples from second set
                if (ts2.size()>0 && cS2) {
                    ts5.addAll(ts2);
                }
                boolean added = false;
                Set<String> ps35 = null;
                Set<String> ps53 = null;
                // Finding links between the star-shaped subqueries
                if (ts3.size() > 0 && ts5.size()> 0) {
                
                    ps35 = getCPConnectionSubj(ts3, ts5);
                    ps53 = getCPConnectionSubj(ts5, ts3);
                }
                HashMap<String,Set<Triple>> ps3 = obtainPredicates(ts3);
                HashMap<String,Set<Triple>> ps5 = obtainPredicates(ts5);
                HashMap<Integer,HashSet<Integer>> relevantCSTs3 = computeRelevantCS(ps3.keySet(), predicateIndexSubj);
                HashMap<Integer,HashSet<Integer>> relevantCSTs5 = computeRelevantCS(ps5.keySet(), predicateIndexSubj);

                Set<String> ps46 = null;
                Set<String> ps64 = null;
                Set<String> ps36 = null;
                Set<String> ps63 = null;
            if (ts3.size() > 0 && ts5.size() > 0 && case1 && ps35.size()>0) { 
                HashMap<Integer,HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>> useful = new HashMap<Integer,HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>>();
                HashMap<Integer,HashMap<Integer, Long>> usefulCost = new HashMap<Integer,HashMap<Integer, Long>>();
                long card35 = getCardinalityCPSubj(ts3, ps3, ps35, ts5, ps5, relevantCSTs3, relevantCSTs5, useful, usefulCost);
                HashSet<Node> set = new HashSet<Node>(set1);
                set.addAll(set2);
                Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>> data = DPTable.get(set);
                long c = Long.MAX_VALUE;
                if (data != null) {
                    c = data.getSecond().getFirst();
                }
                if (card35>0 && card35 < c) {
                    Pair<HashSet<Node>, HashSet<Node>> pair =  new Pair<HashSet<Node>, HashSet<Node>>(set1, set2);
                    Vector<Tree<Pair<Integer,Triple>>> plan = makeCPTreeSubj(ps3, ps5, useful, usefulCost, pair, selectivity, ps35, plansSet1, plansSet2);
                    long cost = costTransfer(plan);
                    DPTable.put(set, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(plan, new Pair<Long, Long>(card35,cost)));  
                    log.put(set, pair);
                    added = true;
                }
            }
            if (ts3.size() > 0 && ts5.size() > 0 && case2 && ps53.size()>0) { 
                HashMap<Integer,HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>> useful = new HashMap<Integer,HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>>();
                HashMap<Integer,HashMap<Integer, Long>> usefulCost = new HashMap<Integer,HashMap<Integer, Long>>();
                long card53 = getCardinalityCPSubj(ts5, ps5, ps53, ts3, ps3, relevantCSTs5, relevantCSTs3, useful, usefulCost);
                HashSet<Node> set = new HashSet<Node>(set1);
                set.addAll(set2);
                Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>> data = DPTable.get(set);
                long c = Long.MAX_VALUE;
                if (data != null) {
                    c = data.getSecond().getFirst();
                }
                if (card53>0 && card53 < c) {
                    Pair<HashSet<Node>, HashSet<Node>> pair =  new Pair<HashSet<Node>, HashSet<Node>>(set2, set1);
                    Vector<Tree<Pair<Integer,Triple>>> plan = makeCPTreeSubj(ps5, ps3, useful, usefulCost, pair, selectivity, ps53, plansSet2, plansSet1);
                    long cost = costTransfer(plan);
                    DPTable.put(set, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(plan, new Pair<Long, Long>(card53,cost)));
                    log.put(set, pair);
                    added = true;
                }
            }
        }
        }
        for (HashSet<Node> s : toRemove) {
            DPTable.remove(s);
        }
    }

    public static long getCardinalityCPSubj(Set<Triple> sq1, HashMap<String, Set<Triple>> map1, Set<String> ps12, Set<Triple> sq2, HashMap<String, Set<Triple>> map2, HashMap<Integer,HashSet<Integer>> relevantCSTs1, HashMap<Integer,HashSet<Integer>> relevantCSTs2, HashMap<Integer,HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>> useful, HashMap<Integer,HashMap<Integer, Long>> usefulCost) {

        Set<String> ps1 = map1.keySet();
        Set<String> ps2 = map2.keySet();

        long c = 0L;
        for (Integer ds1 : relevantCSTs1.keySet()) {
            Set<Integer> css1 = relevantCSTs1.get(ds1);
            for (Integer ds2 : relevantCSTs2.keySet()) {
                Collection<Triple> ts = new Vector<Triple>();
                for (String pred : ps1) {
                    ts.addAll(map1.get(pred));
                }
                for (String pred : ps2) {
                    ts.addAll(map2.get(pred));
                }
                boolean includeCttes = includeConstants(ts);
                long card = 0L;
                if (includeCttes) {
                    card = Long.MIN_VALUE;
                }
                boolean added = false;
                for (String p : ps12) {
                    HashMap<Integer, HashMap<Integer, Integer>> cs1cs2Count = getCPSSubj(ds1, ds2).get(p);
                    if (cs1cs2Count == null) {
                        continue;
                    }
                    Set<Integer> css2 = relevantCSTs2.get(ds2);
                    for (Integer cs1 : css1) {
                        HashMap<Integer, Integer> cs2Count = cs1cs2Count.get(cs1);
                        if (cs2Count == null) {
                            continue;
                        }
                        for (Integer cs2 : css2) {
                            Integer count = cs2Count.get(cs2);
                            if (count == null) {
                                continue;
                            }

                            Pair<Integer, HashMap<String, Pair<Integer, Integer>>> countPsCountNum1 = getCSSSubj(ds1).get(cs1);
                            Pair<Integer, HashMap<String, Pair<Integer, Integer>>> countPsCountNum2 = getCSSSubj(ds2).get(cs2);
                            Long m1 = getMultiplicitySubjS(count, p, countPsCountNum1, ps1, map1, countPsCountNum2, ps2, map2);
                            if (m1 > 0) {
                                if (includeCttes) {
                                    card = Math.max(card, m1);
                                } else {
                                    card += m1;
                                }
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
                long tmpCost = Math.round(Math.ceil(card));
                if (tmpCost>0) {
                    HashMap<Integer, Long> aux2 = usefulCost.get(ds1);
                    if (aux2 == null) {
                        aux2 = new HashMap<Integer, Long>();
                    }
                    aux2.put(ds2, tmpCost);
                    usefulCost.put(ds1, aux2);
                    c += tmpCost;
                } else {
                    HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>> aux1 = useful.get(ds1);
                    if (aux1 == null) {
                        aux1 = new HashMap<Integer, Pair<HashSet<Integer>, HashSet<Integer>>>();
                    }
                    aux1.put(ds2, null);
                    useful.put(ds1, aux1);
                } 
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
            for (Integer ds2 : ds2Pair.keySet()) {
                Pair<HashSet<Node>, HashSet<Node>> pairKeyAux = pairKey;
                Long cardCP = (ds2Card != null) ? ds2Card.get(ds2) : null;
                Tree<Pair<Integer,Triple>> plan2 = plansSet2.get(ds2);
                if (plan2 == null) {
                    continue;
                }
                long c1 = plan1.getCard();
                long c2 = plan2.getCard();
                Tree<Pair<Integer,Triple>> nT = null;
                Integer ds1Aux = ds1;
                Integer ds2Aux = ds2;
                HashSet<Pair<Integer, Triple>> ts = new HashSet<Pair<Integer, Triple>>(plan2.getElements());
                Tree<Pair<Integer,Triple>> plan1Aux = plan1;
                Tree<Pair<Integer,Triple>> plan2Aux = plan2;
                if (c1 > c2) {
                    pairKeyAux = new Pair<HashSet<Node>, HashSet<Node>>(pairKey.getSecond(), (pairKey.getFirst())); 
                    plan2Aux = plan1;
                    plan1Aux = plan2;
                    ds2Aux = ds1;
                    ds1Aux = ds2;
                } 
                if (cardCP != null && cardCP > 0) {
                    nT = makeTree(ds1Aux, plan1Aux, ds2Aux, plan2Aux);
                }
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
                        tsSel.put(ts, sel);
                        selectivity.put(pair1, tsSel);
                    }
                }
            }
        }
        return res;
    }

    public static Integer getStartCS(Set<String> ps, HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> css, HashMap<String, HashSet<Integer>> predicateIndex, HashMap<Integer, Integer> hc, HashMap<Integer, Set<String>> additionalSets, HashMap<Integer, Integer> cost) {

        Integer key = produceStarJoinOrdering.getIKey(ps);
        if (hc.containsKey(key)) {
            return key;
        }
        int c = produceStarJoinOrdering.computeCost(ps, css, predicateIndex);
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

    public static Vector<Tree<Pair<Integer,Triple>>> makeTreeBasic(Vector<Tree<Pair<Integer,Triple>>> vTreeL, Vector<Tree<Pair<Integer,Triple>>> vTreeR) {
        Vector<Tree<Pair<Integer,Triple>>> res = new Vector<Tree<Pair<Integer,Triple>>>();

        if (vTreeL.size() > 0 && vTreeR.size() > 0) {
            for (int i = 0; i < vTreeL.size();i++) {
                Tree<Pair<Integer,Triple>> treeL = vTreeL.get(i);

                for (int k = 0; k < vTreeR.size(); k++) {
                    Tree<Pair<Integer,Triple>> treeR = vTreeR.get(k);
                    res.add(new Branch<Pair<Integer,Triple>>(treeL, treeR));
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
        Vector<Tree<Pair<Integer,Triple>>> res = new Vector<Tree<Pair<Integer,Triple>>>();

        if (vTreeL.size() > 0 && vTreeR.size() > 0) {
            Set<Triple> triples = obtainTriples(vTreeL.get(0));
            triples.retainAll(obtainTriples(vTreeR.get(0)));
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

    public static Long getMultiplicitySubj(Integer m, String p,  Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs, Set<String> ps1, HashMap<String, Triple> map1, Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs2, Set<String> ps2, HashMap<String, Triple> map2) {

        double sel = 1.0;
        Integer count = cPs.getFirst();
        for (String p1 : ps1) {
            Triple t = map1.get(p1);
            Node s = t.getSubject();
            Node o = t.getObject();
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

        Collection<String> preds = new Vector<String>(ps1);
        preds.remove(p);
        double selFirstStar = getSelectivityStar(cPs, preds, map1, true);
        double selSecondStar = getSelectivityStar(cPs2, ps2, map2, true);
        
        return Math.round(Math.ceil(m*selFirstStar*selSecondStar));
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
                }
            } else {
                Set<Integer> keysToDelete = new HashSet<Integer>(intersection.keySet());
                keysToDelete.removeAll(dsCss.keySet());
                for (Integer k : keysToDelete) {
                    intersection.remove(k);
                }
                for (Integer ds : dsCss.keySet()) {
                    if (intersection.containsKey(ds)) {
                        HashSet<Integer> css = new HashSet<Integer>(dsCss.get(ds));
                        css.retainAll(intersection.get(ds));
                        if (css.size()>0) {
                            intersection.put(ds, css);
                        } else {
                            intersection.remove(ds);
                        }
                    }
                }
            }
        }
        return intersection;
    }

    // precondition: triples in vTree share the same subject and all triples in each tree are evaluated at the same source
    public static long subjectCssVSTCost(Vector<Tree<Pair<Integer,Triple>>> vTree, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) {
        long cost = 0L;
        for (Tree<Pair<Integer,Triple>> tmpTree : vTree) {
			long tmp = tmpTree.getCard();
			tmpTree.setCost(tmp);
			cost += tmp;
        }
        return cost;
    }

    static class SubQuery {
		
        Collection<Triple> triples;
        Set<String> predicates = null;
        HashMap<String, Set<Triple>> map;
        Set<Triple> otherTriples;
        boolean isSubjectStar;
        HashMap<Integer, HashSet<Integer>> dsCss = null;
		
        public SubQuery(Collection<Triple> ts) {
            triples = ts;
        }
		
        public void setSubjectStar() {
            isSubjectStar = true;
        }

        public String toString() {

            return triples.toString()+" "+isSubjectStar+" "+dsCss;
        }

        public void setRelevantCss(HashMap<Integer, HashSet<Integer>> dsCss) {
            this.dsCss = dsCss;
        }

        public void setRelevantCss(Integer ds, HashSet<Integer> css) {
            if (this.dsCss == null) {
                this.dsCss = new HashMap<Integer, HashSet<Integer>>();
            }
            dsCss.put(ds, css);
        }
        
        public HashMap<Integer, HashSet<Integer>> getRelevantCss() {
            return dsCss;
        }

        public HashSet<Integer> getRelevantCss(Integer ds) {
            return dsCss.get(ds);
        }
				
        public boolean isSubjectStar() {

            return isSubjectStar;
        }

        public Collection<Triple> getTriples() {
            return triples;
        }

        public Set<String> getPredicates() {
            if (predicates == null) {
                predicates = new HashSet<String>();
                map = new HashMap<String, Set<Triple>>();
                otherTriples = new HashSet<Triple>();
                for (Triple t : triples) {
                    Node p = t.getPredicate(); 
                    if (p.isURI()) {
                        String pStr = "<"+p.getURI()+">";
                        predicates.add(pStr);
                        Set<Triple> ts = map.get(pStr);
                        if (ts == null) {
                            ts = new HashSet<Triple>();
                            ts.add(t);
                            map.put(pStr, ts);
                        }
                    } else {
                        otherTriples.add(t);
                    }
                }				
            } 
            return predicates;
        }		

        public Set<Triple> getTriples(String p) {
            return map.get(p);
        }

        public Set<Triple> getUnboundedPredicateTriples() {
            return otherTriples;
        }
    }
    
    public static long subjectCssSQCostFed(SubQuery sq, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) {
        HashMap<Integer, HashSet<Integer>> dsCss = sq.getRelevantCss();
        if (dsCss==null) {
            Set<String> ps = sq.getPredicates();
            dsCss = computeRelevantCS(ps, predicateIndex);
            sq.setRelevantCss(dsCss);
	    }
        long c = 0L;
        if (dsCss == null) {
            return c;
        }
        for (Integer ds : dsCss.keySet()) {
            c += subjectCssSQCostDS(ds, sq);
        }
        return c;
    }
    
    public static boolean includeConstants(Collection<Triple> sq) {
        boolean i = false;
        for (Triple t : sq) {
            Node o = t.getObject();
            if (!o.isVariable()) {
                i = true;
                break;
            } 
        }
        return i;
    }

    public static long subjectCssSQCostDS(Integer ds, SubQuery subquery) {
		HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> css = getCSSSubj(ds);
        Collection<Triple> sq = subquery.getTriples();

        long cost = 0L;
        boolean includeCttes = includeConstants(sq);
        if (includeCttes) {
            cost = Long.MIN_VALUE;
        }
        for (Integer cs : subquery.getRelevantCss(ds)) {
            
            long costTmp = css.get(cs).getFirst();
            
            double sel = getSelectivityStar(css.get(cs), subquery.getPredicates(), subquery.map, subquery.isSubjectStar());
            if (includeCttes) {
                long v = Math.round(Math.ceil(costTmp * sel));
                cost = Math.max(cost, v);
            } else {
                cost += Math.round(Math.ceil(costTmp*sel));
            }
        }
        return cost; 
    }

    public static double getSelectivityStar(Pair<Integer, HashMap<String, Pair<Integer, Integer>>> costPs, Collection<String> predicates, HashMap<String, Set<Triple>> map, boolean isSubjStar) {

        long costTmp = costPs.getFirst();
        double sel = 1.0, selCttesTmp = 1.0;

        for (String p : predicates) {
            for (Triple t : map.get(p)) {
                Node s = t.getSubject();
                Node o = t.getObject();
                boolean c = true;
                if (isSubjStar) {
                    c = includeMultiplicity && projectedVariables.contains(s) && (!distinct || projectedVariables.contains(o));
                } 
                if (c) {
                    sel = sel*(((double)costPs.getSecond().get(p).getFirst())/costTmp);
                }
                if (!o.isVariable()) {
                    double v = 1.0 / ((double)costPs.getSecond().get(p).getSecond());
                    selCttesTmp = Math.min(selCttesTmp, v);
                }
            }
        }
        return sel*selCttesTmp;
    }
    
    public static Tree<Pair<Integer,Triple>> convertToTreeS(LinkedList<String> orderedPs, SubQuery sq, Integer ds, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) {
        Tree<Pair<Integer,Triple>> sortedStar = null;
        Vector<Triple> triples = new Vector<Triple>();
        
        // List of Triples
        for (String p : orderedPs) {
			Collection<Triple> ts = sq.getTriples(p);
			if (ts != null) {
				triples.addAll(ts);
			}
		}
		List<Triple> best = null;
		long bestCost = Long.MAX_VALUE;
		
		for (int i = triples.size()-1; i >=0; i--) {
			List<Triple> front = new Vector<Triple>(triples.subList(0, i));
		    List<Triple> tail = new Vector<Triple>(triples.subList(i+1, triples.size()));
            Triple t = triples.get(i);
            boolean updated = false;
			// Try to move t to the left if there is a constant as object
			if (!t.getObject().isVariable()) {
				int size = front.size();
				for (int j = size-1; j >=0; j--) {					
			        List<Triple> tmp = new Vector<Triple>();
			        tmp.add(t);
			        SubQuery sqInit = new SubQuery(front);
                    if (sq.isSubjectStar()) {
			            sqInit.setSubjectStar();
                    }
			        sqInit.setRelevantCss(ds, computeRelevantCS(sqInit.getPredicates(), ds, predicateIndex));
			        long cInit = subjectCssSQCostDS(ds, sqInit);
			        SubQuery sqTmp = new SubQuery(tmp);
			        sqTmp.setSubjectStar();
			        sqTmp.setRelevantCss(ds, computeRelevantCS(sqTmp.getPredicates(), ds, predicateIndex));
			        long cTmp = subjectCssSQCostDS(ds, sqTmp);
			        if (cTmp >= cInit) {
				        break;
			        } else {
                        Triple u = front.remove(j);
                        tail.add(0, u);
			        }
                    updated = true;
				}
			}
			// consider the current order and update the best if necessary 
		    if (best == null || updated) {
			    List<Triple> current = new Vector<Triple>();
			    current.addAll(front);
			    current.add(t);
			    current.addAll(tail);
			    SubQuery sqCurrent = new SubQuery(current);
                if (sq.isSubjectStar()) {
			        sqCurrent.setSubjectStar();
                }
			    sqCurrent.setRelevantCss(ds, computeRelevantCS(sqCurrent.getPredicates(), ds, predicateIndex));
			    long cCurrent = subjectCssSQCostDS(ds, sqCurrent);
			    if (cCurrent < bestCost) {
				    best = current;
				    bestCost = cCurrent;
			    }
		    }
		}
		
		// convert to tree
        for (Triple t : best) {
            Leaf<Pair<Integer,Triple>> leaf = new Leaf<Pair<Integer,Triple>>(new Pair<Integer,Triple>(ds, t));
            if (sortedStar == null) {
                sortedStar = leaf;
            } else {
                sortedStar = new Branch<Pair<Integer,Triple>>(sortedStar, leaf);
            }
        }
        sortedStar.setCard(bestCost);
        return sortedStar;
    }

    public static Vector<Tree<Pair<Integer,Triple>>> subjectCssSQJoinOrderFed(SubQuery sq, Set<Integer> ss, HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndex) { 
        HashSet<String> ps = new HashSet<String>(sq.getPredicates());
        if (sq.getRelevantCss()==null) {
            for (Integer s : ss) {
                HashSet<Integer> css = computeRelevantCS(sq.getPredicates(), s, predicateIndex);
                if (css != null) {
                    sq.setRelevantCss(s, css);
                }
            }
        }		
        
        Set<Triple> remainingTriples = sq.getUnboundedPredicateTriples();
        
        HashMap<Integer, HashSet<Integer>> dsCss = sq.getRelevantCss();        
        Vector<Tree<Pair<Integer,Triple>>> vTree = new Vector<Tree<Pair<Integer,Triple>>>();
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
            Tree<Pair<Integer,Triple>> sortedStar = convertToTreeS(orderedPs, sq, ds, predicateIndex);
            for (Triple t : remainingTriples) {
                Leaf<Pair<Integer,Triple>> leaf = new Leaf<Pair<Integer,Triple>>(new Pair<Integer,Triple>(ds, t));
                if (sortedStar == null) {
                    sortedStar = leaf;
                } else {
                    sortedStar = new Branch<Pair<Integer,Triple>>(sortedStar, leaf);
                }
            }

            if (sortedStar.getCard() > 0) {
                vTree.add(sortedStar);
            }
        }
        return vTree;
    }

    public static Node update(HashSet<Triple> triples, HashSet<Triple> star, int i, HashMap<Node, Vector<Tree<Pair<Integer,Triple>>>> map) {

        boolean subjCenter = centerIsSubject(star);
        triples.removeAll(star);
        HashSet<Node> vsStar = new HashSet<Node>();
        HashMap<Node, HashSet<Triple>> vsRest = new HashMap<Node, HashSet<Triple>>();
        Node nv = null;
        if (subjCenter) {
            nv = NodeFactory.createVariable("Subj"+i);
        } else {
            nv = NodeFactory.createVariable("Obj"+i);
        }

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
                    toRemove.add(t);
                }
            }
            if (!subjCenter) {
                centerLinks += noc;
                otherLinks += nsc;
                if (nsc > 0) {
                    toRemove.add(t);
                }
            }
        }
        if ((otherLinks > 0)) { 
            star.removeAll(toRemove);
            triples.addAll(toRemove);
            toRemove.clear();
        }
        // possible connections with the star triples
        for (Triple t : star) {
            Node s = t.getSubject();
            Node o = t.getObject();
            vsStar.add(o);
            vsStar.add(s);
        }
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
        triples.removeAll(toRemove);
        triples.addAll(toAdd);
        return nv;
    }

    public static Vector<SubQuery> getStars(HashSet<Triple> triples, long budget, HashMap<String, HashMap<Integer,HashSet<Integer>>>  predicateIndexSubj, HashMap<Triple, Set<Integer>> selection, Vector<HashSet<Integer>> sources) {

        Vector<SubQuery> stars = new Vector<SubQuery>();
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
            HashSet<Integer> relevantSources = getRelevantSources(starSubj, selection);
            HashSet<Integer> ss = new HashSet<Integer>();
            SubQuery sq = new SubQuery(starSubj);
            sq.setSubjectStar();
            long card = 0L; 

            for (Integer f : relevantSources) {
                sq.setRelevantCss(f, computeRelevantCS(sq.getPredicates(), f, predicateIndexSubj));
                card += subjectCssSQCostDS(f, sq); 
                if (card > budget) {
                    break;
                } else if (0 < card) {
                    ss.add(f);
                }
            }
            if (card == 0) {
                stars.clear();
                return stars;
            }
            
            if (card <= budget) {
                stars.add(sq);
                sources.add(ss);
                ts.removeAll(starSubj);
            }
        }
        return stars;
    }

    public static HashSet<Integer> getRelevantSources(Collection<Triple> ts, HashMap<Triple, Set<Integer>> selection) {

        HashSet<Integer> intersection = null;
        for (Triple t : ts) {
            Set<Integer> ss = selection.get(t);
            if (ss == null) {
                return new HashSet<Integer>();
            }
            if (intersection == null) {
                intersection = new HashSet<Integer>(ss);
            } else {
                intersection.retainAll(ss);
            }
        }
        return intersection;
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

