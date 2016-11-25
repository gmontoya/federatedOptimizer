import java.io.*;
import java.util.*;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.core.Var;

class produceJoinOrderingVOID {

    static Vector<String> datasets = new Vector<String>();
    static String folder;

    //static boolean distinct;
    //static List<Var> projectedVariables;

    public static Vector<String> readDatasets(String file) {
        Vector<String> ps = new Vector<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String l = br.readLine();
            //Vector<String> ps = new Vector<String>();
            while (l!=null) {
                ps.add(l);
                l = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }
        return ps;
    }

    public static void loadStatistics(HashMap<Integer, Vector<Integer>> globalStats, HashMap<String, HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>>> propertyStats, HashMap<String, HashMap<Integer, Integer>> classStats) {
        String prefixStr = "PREFIX void: <http://rdfs.org/ns/void#> ";
        String globalQueryStr = "SELECT DISTINCT * WHERE {    ?d a void:Dataset . ?d void:triples ?numTriples . ?d void:classes ?numClasses . ?d void:properties ?numProps . ?d void:distinctSubjects ?numSubjs . ?d void:distinctObjects ?numObjs }";
        String propertyQueryStr = "SELECT DISTINCT * WHERE {    ?pp void:property ?p .    ?pp void:triples ?numTriplesP .    ?pp void:distinctSubjects ?numSubjP .    ?pp void:distinctObjects ?numObjP }";
        String classQueryStr = "SELECT DISTINCT * WHERE {    ?pp void:class ?class .    ?pp void:entities ?numEntities }";
        for (int i = 0; i < datasets.size(); i++) {
            String datasetStr = datasets.get(i);
            String fileName = folder+"/"+datasetStr+"/"+datasetStr.toLowerCase()+"_void.n3";
            Model m = ModelFactory.createDefaultModel();
            m.read(fileName);

            // global stats
            String queryStr = prefixStr + globalQueryStr;
            Query query = QueryFactory.create(queryStr);
            QueryExecution results = QueryExecutionFactory.create(query, m);
            for (ResultSet rs = results.execSelect(); rs.hasNext();) {
                QuerySolution binding = rs.nextSolution();
                RDFNode n = binding.get("?numTriples"); 
                Integer numTriples = new Integer(n.toString());
                n = binding.get("?numClasses"); 
                Integer numClasses = new Integer(n.toString());
                n = binding.get("?numProps"); 
                Integer numProps = new Integer(n.toString());
                n = binding.get("?numSubjs"); 
                Integer numSubjs = new Integer(n.toString());
                n = binding.get("?numObjs"); 
                Integer numObjs = new Integer(n.toString());
                Vector<Integer> values = new Vector<Integer>();
                values.add(numTriples);
                values.add(numClasses);
                values.add(numProps);
                values.add(numSubjs);
                values.add(numObjs);
                globalStats.put(i, values);
            }
            // property stats
            queryStr = prefixStr + propertyQueryStr;
            query = QueryFactory.create(queryStr);
            results = QueryExecutionFactory.create(query, m);
            for (ResultSet rs = results.execSelect(); rs.hasNext();) {
                QuerySolution binding = rs.nextSolution();
                RDFNode n = binding.get("?p"); 
                String p = n.asResource().getURI();
                n = binding.get("?numTriplesP"); 
                Integer numTriplesP = new Integer(n.toString());
                n = binding.get("?numSubjP"); 
                Integer numSubjP = new Integer(n.toString());
                n = binding.get("?numObjP"); 
                Integer numObjP = new Integer(n.toString());
                HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>> dsNTNSNO = propertyStats.get(p);
                if (dsNTNSNO == null) {
                    dsNTNSNO = new HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>>();
                }
                dsNTNSNO.put(i, new Pair<Integer, Pair<Integer, Integer>>(numTriplesP, new Pair<Integer, Integer>(numSubjP, numObjP)));
                propertyStats.put(p, dsNTNSNO);
            }
            // class stats
            queryStr = prefixStr + classQueryStr;
            query = QueryFactory.create(queryStr);
            results = QueryExecutionFactory.create(query, m);
            for (ResultSet rs = results.execSelect(); rs.hasNext();) {
                QuerySolution binding = rs.nextSolution();
                RDFNode n = binding.get("?class"); 
                String c = "";
                if (n.isURIResource()) {
                    c = n.asResource().getURI();
                } else {
                    c = n.toString();
                }
                n = binding.get("?numEntities"); 
                Integer numEntities = new Integer(n.toString());
                HashMap<Integer, Integer> dsNE = classStats.get(c);
                if (dsNE == null) {
                    dsNE = new HashMap<Integer, Integer>();
                }
                dsNE.put(i, numEntities);
                classStats.put(c, dsNE);
            }
        }
    }

    public static void main(String[] args) {

        String queryFile = args[0];
        String datasetsFile = args[1];
        folder = args[2];
        String fileName = args[3];
        datasets = readDatasets(datasetsFile);
        // DatasetId --> Vector<Integer> <numTriples, numClasses, numProperties, numSubj, numObj>
        HashMap<Integer, Vector<Integer>> globalStats = new HashMap<Integer, Vector<Integer>>();
        // Predicate --> DatasetId --> (numTriples, (numDistSubj, numDistObj))
        HashMap<String, HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>>> propertyStats = new HashMap<String, HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>>>();
        // Class --> DatasetId --> numEntities
        HashMap<String, HashMap<Integer, Integer>> classStats = new HashMap<String, HashMap<Integer, Integer>>();
        loadStatistics(globalStats, propertyStats, classStats);
        //System.out.println("predicateIndex size: "+predicateIndex.size());
        BGP bgp = new BGP(queryFile);
        //distinct = bgp.getDistinct();
        //projectedVariables = bgp.getProjectedVariables();
        // sub-query --> <order, <cardinality,cost>>
        HashMap<HashSet<Triple>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long, Long>>> DPTable = new HashMap<HashSet<Triple>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long, Long>>>();
        HashSet<Triple> triples = new HashSet<Triple>();

        addTriples(DPTable, bgp, triples, globalStats, propertyStats, classStats); //, css, cps, predicateIndex, triples, cost, map, hc, additionalSets);
        //System.out.println("DPTable after add.. :"+DPTable);
        // may have to consider already intermediate results in the CP estimation for the cost
        estimateSelectivityJoins(DPTable, triples, globalStats, propertyStats, classStats); //css, cps, predicateIndex, triples, hc, additionalSets, cost);
        //System.out.println("DPTable after CP estimation.. :"+DPTable);
        computeJoinOrderingDP(DPTable, triples);
        Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long, Long>> res = DPTable.get(triples);
        if (res != null) {
            String plan = produceJoinOrderingFederation.toString(res.getFirst());
            System.out.println("Plan: "+plan);
            System.out.println("Cardinality: "+res.getSecond().getFirst());
            System.out.println("Cost: "+res.getSecond().getSecond());
            produceJoinOrderingFederation.write(plan, fileName);
        } else {
            System.out.println("No plan was found");
        }
        //System.out.println("DPTable at the end: "+DPTable);
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

    // DBTable already provides cardinalities for sets of triples with one or two elements
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
                    Vector<Tree<Pair<Integer,Triple>>> order = makeTree(DPTable.get(ns).getFirst(), DPTable.get(ss).getFirst());
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

    public static void addTriples(HashMap<HashSet<Triple>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable, BGP bgp, HashSet<Triple> triples, HashMap<Integer, Vector<Integer>> globalStats, HashMap<String, HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>>> propertyStats, HashMap<String, HashMap<Integer, Integer>> classStats) {

        List<Triple> ts = bgp.getBody();
        //System.out.println("ts: "+ts);
        for (Triple t : ts) {

            triples.add(t); 
            HashSet<Triple> newEntry = new HashSet<Triple>();
            newEntry.add(t);
            Set<Integer> dss = new HashSet<Integer>();
            Long card = getCardinalityEstimationTriple(t, dss, globalStats, propertyStats, classStats);

            addTriple(t, dss, newEntry, card, DPTable); 
        }
    }

    public static void addTriple(Triple t, Set<Integer> dss, HashSet<Triple> newEntry, Long card, HashMap<HashSet<Triple>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable) {
        Vector<Tree<Pair<Integer,Triple>>> vTree = new Vector<Tree<Pair<Integer,Triple>>>();
        for (Integer ds : dss) {
            Leaf<Pair<Integer,Triple>> leaf = new Leaf<Pair<Integer,Triple>>(new Pair<Integer,Triple>(ds, t));
            vTree.add(leaf);
        }
        DPTable.put(newEntry, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(vTree, new Pair<Long, Long>(card,0L)));
    }

    public static long getCardinalityEstimationTriple(Triple t, Set<Integer> dss, HashMap<Integer, Vector<Integer>> globalStats, HashMap<String, HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>>> propertyStats, HashMap<String, HashMap<Integer, Integer>> classStats) {
        Node s = t.getSubject();
        Node p = t.getPredicate();
        Node o = t.getObject();
        Long card = 0L;
        if (s.isVariable() && p.isVariable() && o.isVariable()) {
            for (Integer ds : globalStats.keySet()) {
                Integer tmpCard = globalStats.get(ds).get(0);
                card += tmpCard; 
                dss.add(ds);
            }
        } else if (p.isVariable()) {
            for (Integer ds : globalStats.keySet()) {
                Integer numTriples = globalStats.get(ds).get(0);
                Integer numSubj = globalStats.get(ds).get(3);
                Integer numObj = globalStats.get(ds).get(4);
                if (!s.isVariable() && !o.isVariable()) {
                    Long c = Math.round(Math.ceil(numTriples / (((double) numSubj) * numObj)));
                    card += c; 
                } else if (!s.isVariable()) {
                    Long c = Math.round(Math.ceil(numTriples / ((double) numSubj)));
                    card += c; 
                } else if(!o.isVariable()) {
                    Long c = Math.round(Math.ceil(numTriples / ((double) numObj)));
                    card += c; 
                }
                dss.add(ds);
            }
        } else if (p.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") && !o.isVariable()) {
            String oStr = "";
            if (o.isURI()) {
                oStr = o.getURI();
            } else {
                oStr = o.toString();
            }
            HashMap<Integer, Integer> tmpDsNumEnt = classStats.get(oStr);
            if (tmpDsNumEnt != null) {
                for (Integer ds : tmpDsNumEnt.keySet()) {
                     Integer c = tmpDsNumEnt.get(ds);
                     card += c;
                     dss.add(ds);
                }
            }
        } else if (s.isVariable() && o.isVariable()) {
            String pStr = p.getURI();
            HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>> tmpDsTSO = propertyStats.get(pStr);
            if (tmpDsTSO != null) {
                for (Integer ds : tmpDsTSO.keySet()) {
                    Integer c = propertyStats.get(pStr).get(ds).getFirst();
                    card += c;
                    dss.add(ds);
                }
            }
        } else if (s.isVariable()) {
             String pStr = p.getURI();
             HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>> dsNTNSNO = propertyStats.get(pStr);
             if (dsNTNSNO != null) {
                 for (Integer ds : dsNTNSNO.keySet()) {
                    Pair<Integer, Pair<Integer, Integer>> numTnumSnumO = dsNTNSNO.get(ds);
                    Integer numTriples = numTnumSnumO.getFirst();
                    Integer numObj = numTnumSnumO.getSecond().getSecond();
                    Long c = Math.round(Math.ceil(numTriples / ((double) numObj)));
                    card += c; 
                    dss.add(ds);
                 }
             }
        } else if (o.isVariable()) {
             String pStr = p.getURI();
             HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>> dsNTNSNO = propertyStats.get(pStr);
             if (dsNTNSNO != null) {
                 for (Integer ds : dsNTNSNO.keySet()) {
                    Pair<Integer, Pair<Integer, Integer>> numTnumSnumO = dsNTNSNO.get(ds);
                    Integer numTriples = numTnumSnumO.getFirst();
                    Integer numSubj = numTnumSnumO.getSecond().getFirst();
                    Long c = Math.round(Math.ceil(numTriples / ((double) numSubj)));
                    card += c; 
                    dss.add(ds);
                 }
             }
        } else if (p.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
            String oStr = "";
            if (o.isURI()) {
                oStr = o.getURI();
            } else {
                oStr = o.toString();
            }
            HashMap<Integer, Integer> tmpDsNumEnt = classStats.get(oStr);
            if (tmpDsNumEnt != null) {
                String pStr = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
                HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>> dsNTNSNO = propertyStats.get(pStr);
                if (dsNTNSNO != null) {
                    for (Integer ds : tmpDsNumEnt.keySet()) {
                        Pair<Integer, Pair<Integer, Integer>> numTnumSnumO = dsNTNSNO.get(ds);
                        Integer numTriples = numTnumSnumO.getFirst();
                        Integer numSubj = numTnumSnumO.getSecond().getFirst();
                        Integer numObj = numTnumSnumO.getSecond().getSecond();
                        Long c = Math.round(Math.ceil(numTriples / (((double) numSubj) * numObj)));
                        card += c; 
                        dss.add(ds);
                    }
                }
            }
        } else {
             String pStr = p.getURI();
             HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>> dsNTNSNO = propertyStats.get(pStr);
             if (dsNTNSNO != null) {
                 for (Integer ds : dsNTNSNO.keySet()) {
                    Pair<Integer, Pair<Integer, Integer>> numTnumSnumO = dsNTNSNO.get(ds);
                    Integer numTriples = numTnumSnumO.getFirst();
                    Integer numSubj = numTnumSnumO.getSecond().getFirst();
                    Integer numObj = numTnumSnumO.getSecond().getSecond();
                    Long c = Math.round(Math.ceil(numTriples / (((double) numSubj) * numObj)));
                    card += c; 
                    dss.add(ds);
                 }
             }
        }
        return card;
    }

    public static long getCardinalityEstimationTriple(Triple t, Integer ds, HashMap<Integer, Vector<Integer>> globalStats, HashMap<String, HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>>> propertyStats, HashMap<String, HashMap<Integer, Integer>> classStats) {
        Node s = t.getSubject();
        Node p = t.getPredicate();
        Node o = t.getObject();
        Long card = 0L;
        if (s.isVariable() && p.isVariable() && o.isVariable()) {
            Integer tmpCard = globalStats.get(ds).get(0);
            card += tmpCard; 
        } else if (p.isVariable()) {
            Integer numTriples = globalStats.get(ds).get(0);
            Integer numSubj = globalStats.get(ds).get(3);
            Integer numObj = globalStats.get(ds).get(4);
            if (!s.isVariable() && !o.isVariable()) {
                Long c = Math.round(Math.ceil(numTriples / (((double) numSubj) * numObj)));
                card += c; 
            } else if (!s.isVariable()) {
                Long c = Math.round(Math.ceil(numTriples / ((double) numSubj)));
                card += c; 
            } else if(!o.isVariable()) {
                Long c = Math.round(Math.ceil(numTriples / ((double) numObj)));
                card += c; 
            }
        } else if (p.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") && !o.isVariable()) {
            String oStr = "";
            if (o.isURI()) {
                oStr = o.getURI();
            } else {
                oStr = o.toString();
            }
            HashMap<Integer, Integer> tmpDsNumEnt = classStats.get(oStr);
            if (tmpDsNumEnt != null) {
                 Integer c = tmpDsNumEnt.get(ds);
                 card += c;
            }
        } else if (s.isVariable() && o.isVariable()) {
            String pStr = p.getURI();
            HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>> tmpDsTSO = propertyStats.get(pStr);
            if (tmpDsTSO != null) {
                Integer c = propertyStats.get(pStr).get(ds).getFirst();
                card += c;
            }
        } else if (s.isVariable()) {
             String pStr = p.getURI();
             HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>> dsNTNSNO = propertyStats.get(pStr);
             if (dsNTNSNO != null) {
                Pair<Integer, Pair<Integer, Integer>> numTnumSnumO = dsNTNSNO.get(ds);
                Integer numTriples = numTnumSnumO.getFirst();
                Integer numObj = numTnumSnumO.getSecond().getSecond();
                Long c = Math.round(Math.ceil(numTriples / ((double) numObj)));
                card += c; 
             }
        } else if (o.isVariable()) {
             String pStr = p.getURI();
             HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>> dsNTNSNO = propertyStats.get(pStr);
             if (dsNTNSNO != null) {
                 Pair<Integer, Pair<Integer, Integer>> numTnumSnumO = dsNTNSNO.get(ds);
                 Integer numTriples = numTnumSnumO.getFirst();
                 Integer numSubj = numTnumSnumO.getSecond().getFirst();
                 Long c = Math.round(Math.ceil(numTriples / ((double) numSubj)));
                 card += c; 
             }
        } else if (p.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
            String oStr = "";
            if (o.isURI()) {
                oStr = o.getURI();
            } else {
                oStr = o.toString();
            }
            HashMap<Integer, Integer> tmpDsNumEnt = classStats.get(oStr);
            if (tmpDsNumEnt != null) {
                String pStr = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
                HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>> dsNTNSNO = propertyStats.get(pStr);
                if (dsNTNSNO != null) {
                    Pair<Integer, Pair<Integer, Integer>> numTnumSnumO = dsNTNSNO.get(ds);
                    Integer numTriples = numTnumSnumO.getFirst();
                    Integer numSubj = numTnumSnumO.getSecond().getFirst();
                    Integer numObj = numTnumSnumO.getSecond().getSecond();
                    Long c = Math.round(Math.ceil(numTriples / (((double) numSubj) * numObj)));
                    card += c; 
                }
            }
        } else {
             String pStr = p.getURI();
             HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>> dsNTNSNO = propertyStats.get(pStr);
             if (dsNTNSNO != null) {
                 Pair<Integer, Pair<Integer, Integer>> numTnumSnumO = dsNTNSNO.get(ds);
                 Integer numTriples = numTnumSnumO.getFirst();
                 Integer numSubj = numTnumSnumO.getSecond().getFirst();
                 Integer numObj = numTnumSnumO.getSecond().getSecond();
                 Long c = Math.round(Math.ceil(numTriples / (((double) numSubj) * numObj)));
                 card += c; 
             }
        }
        return card;
    }

    public static void estimateSelectivityJoins(HashMap<HashSet<Triple>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable, HashSet<Triple> triples, HashMap<Integer, Vector<Integer>> globalStats, HashMap<String, HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>>> propertyStats, HashMap<String, HashMap<Integer, Integer>> classStats) {

        Vector<HashSet<Triple>> subqueries = new Vector<HashSet<Triple>>(DPTable.keySet());
        for (int i = 0; i < subqueries.size(); i++) {
            HashSet<Triple> sq1 = subqueries.get(i);
            Vector<Tree<Pair<Integer,Triple>>> vTree1 = DPTable.get(sq1).getFirst();
            for (int j = i+1; j < subqueries.size(); j++) {
                HashSet<Triple> sq2 = subqueries.get(j);
                Vector<Tree<Pair<Integer,Triple>>> vTree2 = DPTable.get(sq2).getFirst();
                addSubquery(sq1, vTree1, sq2, vTree2, DPTable, globalStats, propertyStats, classStats);
            }
        }
    }

    public static long subjMult(Triple t, Integer source, HashMap<Integer, Vector<Integer>> globalStats, HashMap<String, HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>>> propertyStats, HashMap<String, HashMap<Integer, Integer>> classStats) {
        Node s = t.getSubject();
        Node p = t.getPredicate();
        Node o = t.getObject();
        Long card = 0L;
        if (s.isVariable() && p.isVariable() && o.isVariable()) {
            Integer tmpCard = globalStats.get(source).get(3);
            card += tmpCard; 
        } else if (!s.isVariable()) { // p, o are constant or variable
            Long c = 1L;
            card += c; 
        } else if (p.isVariable()) { // s is variable, o is constant
            Integer numSubj = globalStats.get(source).get(3);
            Integer numObj = globalStats.get(source).get(4);
            Long c = Math.round(Math.ceil(((double) numSubj) / numObj));
            card += c; 
        } else if (p.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") && !o.isVariable()) { // s is variable
            String oStr = "";
            if (o.isURI()) {
                oStr = o.getURI();
            } else {
                oStr = o.toString();
            }
            HashMap<Integer, Integer> tmpDsNumEnt = classStats.get(oStr);
            if (tmpDsNumEnt != null) {
                 Integer c = tmpDsNumEnt.get(source);
                 card += c;
            }
        } else if (s.isVariable() && o.isVariable()) { // p is constant
            String pStr = p.getURI();
            HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>> tmpDsTSO = propertyStats.get(pStr);
            if (tmpDsTSO != null) {
                Integer c = tmpDsTSO.get(source).getSecond().getFirst();
                card += c;
            }
        } else if (s.isVariable()) { // p, o are constant
             String pStr = p.getURI();
             HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>> dsNTNSNO = propertyStats.get(pStr);
             if (dsNTNSNO != null) {
                Pair<Integer, Pair<Integer, Integer>> numTnumSnumO = dsNTNSNO.get(source);
                Integer numSubj = numTnumSnumO.getSecond().getFirst();
                Integer numObj = numTnumSnumO.getSecond().getSecond();
                Long c = Math.round(Math.ceil(numSubj / ((double) numObj)));
                card += c; 
             }
        } 
        return card;
    }

    public static long objMult(Triple t, Integer source, HashMap<Integer, Vector<Integer>> globalStats, HashMap<String, HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>>> propertyStats, HashMap<String, HashMap<Integer, Integer>> classStats) {
        Node s = t.getSubject();
        Node p = t.getPredicate();
        Node o = t.getObject();
        Long card = 0L;
        if (s.isVariable() && p.isVariable() && o.isVariable()) {
            Integer tmpCard = globalStats.get(source).get(4);
            card += tmpCard; 
        } else if (!o.isVariable()) { // s, p are constant or variable
            Long c = 1L;
            card += c; 
        } else if (p.isVariable()) { // o is variable, s is constant
            Integer numSubj = globalStats.get(source).get(3);
            Integer numObj = globalStats.get(source).get(4);
            Long c = Math.round(Math.ceil(((double) numObj) / numSubj));
            card += c; 
        } else if (s.isVariable() && o.isVariable()) { // p is constant
            String pStr = p.getURI();
            HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>> tmpDsTSO = propertyStats.get(pStr);
            if (tmpDsTSO != null) {
                Integer c = tmpDsTSO.get(source).getSecond().getSecond();
                card += c;
            }
        } else if (o.isVariable()) { // p, s are constant
             String pStr = p.getURI();
             HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>> dsNTNSNO = propertyStats.get(pStr);
             if (dsNTNSNO != null) {
                Pair<Integer, Pair<Integer, Integer>> numTnumSnumO = dsNTNSNO.get(source);
                Integer numSubj = numTnumSnumO.getSecond().getFirst();
                Integer numObj = numTnumSnumO.getSecond().getSecond();
                Long c = Math.round(Math.ceil(numObj / ((double) numSubj)));
                card += c; 
             }
        } 
        return card;
    }

    public static void addSubquery(HashSet<Triple> sq1, Vector<Tree<Pair<Integer,Triple>>> vTree1, HashSet<Triple> sq2, Vector<Tree<Pair<Integer,Triple>>> vTree2, HashMap<HashSet<Triple>, Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>> DPTable, HashMap<Integer, Vector<Integer>> globalStats, HashMap<String, HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>>> propertyStats, HashMap<String, HashMap<Integer, Integer>> classStats) {

        Vector<Tree<Pair<Integer,Triple>>> resTree =new Vector<Tree<Pair<Integer,Triple>>>();
        Long card = 0L;
        Long cardJVL = 0L;
        Long cardJVR = 0L;

        for (Tree<Pair<Integer, Triple>> leftTree : vTree1) {
            Pair<Integer, Triple> leftPair = leftTree.getOneElement();
            Integer leftSource = leftPair.getFirst();
            Triple leftTriple = leftPair.getSecond();
            Node lS = leftTriple.getSubject();
            Node lP = leftTriple.getPredicate();
            Node lO = leftTriple.getObject();
            Long cardLT = getCardinalityEstimationTriple(leftTriple, leftSource, globalStats, propertyStats, classStats);

            for (Tree<Pair<Integer, Triple>> rightTree : vTree2) {
                Pair<Integer, Triple> rightPair = rightTree.getOneElement();
                Integer rightSource = rightPair.getFirst();
                Triple rightTriple = rightPair.getSecond();
                Node rS = rightTriple.getSubject();
                Node rP = rightTriple.getPredicate();
                Node rO = rightTriple.getObject();
                Long cardRT = getCardinalityEstimationTriple(rightTriple, rightSource, globalStats, propertyStats, classStats);
                Long cardJVLTmp = 0L;
                Long cardJVRTmp = 0L;

                if (lS.equals(rS)) { // subject-subject join
                    cardJVLTmp += subjMult(leftTriple, leftSource, globalStats, propertyStats, classStats);
                    cardJVRTmp += subjMult(rightTriple, rightSource, globalStats, propertyStats, classStats);
                } else if (lO.equals(rS)) { // object-subject join
                    cardJVLTmp += objMult(leftTriple, leftSource, globalStats, propertyStats, classStats);
                    cardJVRTmp += subjMult(rightTriple, rightSource, globalStats, propertyStats, classStats);
                } else if (lS.equals(rO)) {
                    cardJVLTmp += subjMult(leftTriple, leftSource, globalStats, propertyStats, classStats);
                    cardJVRTmp += objMult(rightTriple, rightSource, globalStats, propertyStats, classStats);
                } else if (lO.equals(rO)) {
                    cardJVLTmp += objMult(leftTriple, leftSource, globalStats, propertyStats, classStats);
                    cardJVRTmp += objMult(rightTriple, rightSource, globalStats, propertyStats, classStats);
                }
                if (cardJVLTmp > 0 && cardJVRTmp > 0) {
                    Tree<Pair<Integer,Triple>> nT = null;
                    if (cardLT <= cardRT) {
                        nT = new Branch<Pair<Integer,Triple>>(leftTree, rightTree);
                    } else {
                        nT = new Branch<Pair<Integer,Triple>>(rightTree, leftTree);
                    }
                    resTree.add(nT);
                    cardJVL += cardJVLTmp;
                    cardJVR += cardJVRTmp;
                }
            }
        }
        if (cardJVL <= 0 || cardJVR <= 0) {
            return;
        }
        Long cardL = DPTable.get(sq1).getSecond().getFirst();
        Long cardR = DPTable.get(sq2).getSecond().getFirst();
        card = Math.round(Math.ceil(((double) cardL)*cardR/Math.max(cardJVL, cardJVR)));

        HashSet<Triple> set = new HashSet<Triple>(sq1);
        set.addAll(sq2);

        Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>> data = DPTable.get(set);
        long c = Long.MAX_VALUE;
        if (data != null) {
            c = data.getSecond().getFirst();
        }
        // including the minimum cost if inferior to previous value
        // order not really considered here... just computing the cardinality
        if (card > 0 && card < c) {
            DPTable.put(set, new Pair<Vector<Tree<Pair<Integer,Triple>>>, Pair<Long,Long>>(resTree, new Pair<Long, Long>(card,0L)));
        }
    }

    public static Vector<Tree<Pair<Integer,Triple>>> makeTree(Vector<Tree<Pair<Integer,Triple>>> treeL, Vector<Tree<Pair<Integer,Triple>>> treeR) {
        Vector<Tree<Pair<Integer,Triple>>> res = new Vector<Tree<Pair<Integer,Triple>>>();
        for (int i = 0; i < treeL.size(); i++) {
            Tree<Pair<Integer,Triple>> tL = treeL.get(i);
            for (int j = 0; j < treeR.size(); j++) {
                Tree<Pair<Integer,Triple>> tR = treeR.get(j);
                Tree<Pair<Integer,Triple>> nT = new Branch<Pair<Integer,Triple>>(tL, tR);
                res.add(nT);
            }
        }
        return res;
    }
}
