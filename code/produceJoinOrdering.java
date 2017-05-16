import java.io.*;
import java.util.*;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;

class produceJoinOrdering {
    public static void main(String[] args) {

        String queryFile = args[0];
        String fileCSS = args[1]; //statistics_<dataset>_css_reduced10000CS
        String fileHC = args[2]; //statistics_<dataset>_hc
        String fileAdditionalSets = args[3]; //statistics_<dataset>_as
        String filePredIndex = args[4]; //statistics_<dataset>_pi
        String fileCost = args[5]; //statistics_<dataset>_cost
        String fileCPS = args[6]; //cps_<dataset1>_<dataset2>_reduced10000CS_MIP or statistics<dataset>_cps_reduced10000CS
        //int budget = Integer.MAX_VALUE; //
        long budget = Long.parseLong(args[7]);
        HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css = produceStarJoinOrdering.readCSS(fileCSS);
        HashMap<Integer, Integer> hc = produceStarJoinOrdering.readMap(fileHC);
        HashMap<Integer, Set<String>> additionalSets = produceStarJoinOrdering.readAdditionalSets(fileAdditionalSets);
        HashMap<String, HashSet<Integer>> predicateIndex = produceStarJoinOrdering.readPredicateIndex(filePredIndex);
        HashMap<Integer, Integer> cost = produceStarJoinOrdering.readMap(fileCost);
        HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps = readCPS(fileCPS);
        
        LinkedList<String> order = new LinkedList<String>();
        //System.out.println(predicateIndex);
        BGP bgp = new BGP(queryFile);
        // sub-query --> <order, <cardinality,cost>>
        HashMap<HashSet<Triple>, Pair<Tree<Triple>, Pair<Long, Long>>> DPTable = new HashMap<HashSet<Triple>, Pair<Tree<Triple>, Pair<Long, Long>>>();
        Vector<HashSet<Triple>> stars = getStars(bgp, budget, css, predicateIndex, cost);
        //System.out.println("stars: "+stars);
        int i = 1;
        HashSet<Triple> triples = new HashSet<Triple>();
        HashMap<Node, Tree<Triple>> map = new HashMap<Node, Tree<Triple>>();
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
                Tree<Triple> p = getStarJoinOrder(s, css, hc, additionalSets, predicateIndex, cost);
                map.put(nn, p);
                i++;
            }
            //HashSet<Triple> ns = new HashSet<Triple>();
            //ns.add(nn);
            //triples.add(nn);
            //DPTable.put(ns, new Pair<Vector<Triple>, Pair<Long,Long>>(p, new Pair<Long, Long>(cost(p, css, predicateIndex, cost),0L)));
        }
        
        //System.out.println("map: "+map);
        //System.out.println("DPTable before add.. :"+DPTable);
        addRemainingTriples(DPTable, bgp, css, cps, predicateIndex, triples, cost, map, hc, additionalSets);
        //System.out.println("DPTable after add.. :"+DPTable);
        // may have to consider already intermediate results in the CP estimation for the cost
        estimateSelectivityCP(DPTable, css, cps, predicateIndex, triples, hc, additionalSets, cost);
        //System.out.println("DPTable after CP estimation.. :"+DPTable);
        computeJoinOrderingDP(DPTable, triples);
        Pair<Tree<Triple>, Pair<Long, Long>> res = DPTable.get(triples);
        if (res != null) {
            System.out.println("Plan: "+res.getFirst());
            System.out.println("Cardinality: "+res.getSecond().getFirst());
            System.out.println("Cost: "+res.getSecond().getSecond());
        } else {
            System.out.println("No plan was found");
        }
        //System.out.println("DPTable at the end: "+DPTable);
    }

    public static HashSet<Triple> rename(HashSet<Triple> star, HashMap<Node, Tree<Triple>> map, HashMap<Triple,Triple> renamed) {

        HashMap<Node, Node> invertMap = new HashMap<Node,Node>();
        for (Node n : map.keySet()) {
            Node s = map.get(n).getOneElement().getSubject();
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
    public static void computeJoinOrderingDP(HashMap<HashSet<Triple>, Pair<Tree<Triple>, Pair<Long,Long>>> DPTable, HashSet<Triple> triples) {

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

    public static void computeJoinOrderingDPAux(HashMap<HashSet<Triple>, Pair<Tree<Triple>, Pair<Long,Long>>> DPTable, HashSet<Triple> triples, int i) {
        // currently cost depends only on intermediate results, then for the same two sub-queries their order do not matter
        // start considering sub-queries from half the size of the current sub-queries (i)
        int k = (int) Math.round(Math.ceil(i/2.0));
        HashMap<HashSet<Triple>, Pair<Tree<Triple>, Pair<Long,Long>>> DPTableAux = new HashMap<HashSet<Triple>, Pair<Tree<Triple>, Pair<Long,Long>>>();
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
                Pair<Tree<Triple>, Pair<Long,Long>> pair = DPTableAux.get(newEntry);
                if ((pair == null) || pair.getSecond().getSecond()>cost) {
                    //System.out.println("merging : "+ns+" and "+ss+". card: "+card+". cost: "+cost+". pair: "+pair+". newEntry: "+newEntry);
                    Tree<Triple> order = new Branch<Triple>(DPTable.get(ns).getFirst(), DPTable.get(ss).getFirst());
                    DPTableAux.put(newEntry, new Pair<Tree<Triple>, Pair<Long,Long>>(order, new Pair<Long,Long>(card, cost)));
                }
            }
        }
        DPTable.putAll(DPTableAux);
    }

    // to estimate the cardinality between two sub-queries, consider the information already in DPTable about the joins
    // between two triple patterns, one from each sub-query to compute the selectivity of the joins and the cardinality of the new join
    public static long getCard(HashMap<HashSet<Triple>, Pair<Tree<Triple>, Pair<Long,Long>>> DPTable, HashSet<Triple> sq1, HashSet<Triple> sq2) {
        long cardSQ1 = DPTable.get(sq1).getSecond().getFirst();
        long cardSQ2 = DPTable.get(sq2).getSecond().getFirst();
        double accSel = 1;
        for (Triple n : sq1) {
            for (Triple m : sq2) {
                HashSet<Triple> join = new HashSet<Triple>();
                join.add(n);
                join.add(m);
                Pair<Tree<Triple>, Pair<Long, Long>> val = DPTable.get(join);
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

    public static void addRemainingTriples(HashMap<HashSet<Triple>, Pair<Tree<Triple>, Pair<Long,Long>>> DPTable, BGP bgp, HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css, HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps, HashMap<String, HashSet<Integer>> predicateIndex, HashSet<Triple> triples, HashMap<Integer, Integer> cost, HashMap<Node, Tree<Triple>> map,HashMap<Integer, Integer> hc, HashMap<Integer, Set<String>> additionalSets) {

        List<Triple> ts = bgp.getBody();
        //System.out.println("ts: "+ts);
        for (Triple t : ts) {
            Node s = t.getSubject();
            Node p = t.getPredicate();
            Node o = t.getObject();
            HashSet<Triple> tps1 = new HashSet<Triple>();
            if (map.containsKey(s)) {
                Node e = map.get(s).getOneElement().getSubject();
                tps1.addAll(map.get(s).getElements());
                s = e;
            }
            if (map.containsKey(o)) {
                Node e = map.get(o).getOneElement().getSubject();
                o = e;
            }
            // triples and newEntry include the triple with metanodes
            triples.add(t); 
            HashSet<Triple> newEntry = new HashSet<Triple>();
            newEntry.add(t);
            // but for the join ordering the metanodes are replaced by the star variable
            t = new Triple(s, p, o);    
            tps1.add(t);

            addCS(tps1, newEntry, css, predicateIndex, DPTable, hc, additionalSets, cost);
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
    public static void estimateSelectivityCP(HashMap<HashSet<Triple>, Pair<Tree<Triple>, Pair<Long,Long>>> DPTable, HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css, HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps, HashMap<String, HashSet<Integer>> predicateIndex, HashSet<Triple> triples,HashMap<Integer, Integer> hc, HashMap<Integer, Set<String>> additionalSets, HashMap<Integer, Integer> cost) {

        Vector<HashSet<Triple>> subqueries = new Vector<HashSet<Triple>>(DPTable.keySet());
        for (int i = 0; i < subqueries.size(); i++) {
            HashSet<Triple> sq1 = subqueries.get(i);
            for (int j = i+1; j < subqueries.size(); j++) {
                HashSet<Triple> sq2 = subqueries.get(j);
                Tree<Triple> ts1 = DPTable.get(sq1).getFirst();
                Tree<Triple> ts2 = DPTable.get(sq2).getFirst();
                addCheapestCP(ts1, ts2, sq1, sq2, css, cps, predicateIndex, DPTable);
                // just for the case of very expensive stars that were not included in the DPTable
                HashSet<Triple> ts = new HashSet<Triple>(ts1.getElements());
                ts.addAll(ts2.getElements());
                HashSet<Triple> sq = new HashSet<Triple>(sq1);
                sq.addAll(sq2);
                addCS(ts, sq, css, predicateIndex, DPTable, hc, additionalSets, cost);
            }
        }
    }

    public static void addCS(HashSet<Triple> ts1, HashSet<Triple> sq1, HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css, HashMap<String, HashSet<Integer>> predicateIndex, HashMap<HashSet<Triple>, Pair<Tree<Triple>, Pair<Long,Long>>> DPTable, HashMap<Integer, Integer> hc, HashMap<Integer, Set<String>> additionalSets, HashMap<Integer, Integer> cost) {

            boolean okay = true;
            Node subject = null;
            for (Triple t : ts1) {
                Node subjectTmp = t.getSubject();
                okay = okay && ((subject == null) || subject.equals(subjectTmp));
                subject = subjectTmp;
            }
            if (!okay) {
                return;
            }
            Tree<Triple> p = getStarJoinOrder(ts1, css, hc, additionalSets, predicateIndex, cost);
            HashSet<Triple> newEntry = new HashSet<Triple>(sq1);
            long c = cost(p.getElements(), css, predicateIndex, cost);
            Pair<Tree<Triple>, Pair<Long,Long>> data = DPTable.get(newEntry);
            if (data == null || data.getSecond().getFirst()>c) {
                DPTable.put(newEntry, new Pair<Tree<Triple>, Pair<Long,Long>>(p, new Pair<Long, Long>(c,0L)));
            }
    }

    public static void addCheapestCP(Tree<Triple> ts1, Tree<Triple> ts2, HashSet<Triple> sq1, HashSet<Triple> sq2, HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css, HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps, HashMap<String, HashSet<Integer>> predicateIndex, HashMap<HashSet<Triple>, Pair<Tree<Triple>, Pair<Long,Long>>> DPTable) {
                Set<Triple> setTS1 = ts1.getElements();
                Set<Triple> setTS2 = ts2.getElements();
                long cost12 = getCostCP(setTS1, setTS2, css, cps, predicateIndex);
                //System.out.println("cost of "+sq1+" and "+sq2+": "+cost12);
                long cost21 = getCostCP(setTS2, setTS1, css, cps, predicateIndex);
                //System.out.println("cost of "+sq2+" and "+sq1+": "+cost21);
                HashSet<Triple> set = new HashSet<Triple>(sq1);
                set.addAll(sq2);
                Tree<Triple> order = null;

                Pair<Tree<Triple>, Pair<Long,Long>> data = DPTable.get(set);
                long c = Long.MAX_VALUE;
                if (data != null) {
                    c = data.getSecond().getFirst();
                }
                // including the minimum cost if inferior to previous value
                // order not really considered here... just computing the cardinality
                if (cost12<Long.MAX_VALUE && cost12 < c && cost12 <= cost21) {
                    order = new Branch<Triple>(ts1, ts2);
                    DPTable.put(set, new Pair<Tree<Triple>, Pair<Long,Long>>(order, new Pair<Long, Long>(cost12,0L)));
                } else if (cost21<Long.MAX_VALUE && cost21 < c) {
                    order = new Branch<Triple>(ts2, ts1);
                    DPTable.put(set, new Pair<Tree<Triple>, Pair<Long,Long>>(order, new Pair<Long, Long>(cost21,0L)));
                }
    }
    public static long getCostCP(Set<Triple> sq1, Set<Triple> sq2, HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css, HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps, HashMap<String, HashSet<Integer>> predicateIndex) {

        HashSet<String> ps1 = new HashSet<String>();
        HashMap<String, Triple> map1 = new HashMap<String, Triple>();
        for (Triple t : sq1) {
            Node p = t.getPredicate(); 
            if (p.isURI()) {
                ps1.add("<"+p.getURI()+">");
                map1.put("<"+p.getURI()+">", t);
            }
        }
        
        HashSet<String> ps2 = new HashSet<String>();
        HashMap<String, Triple> map2 = new HashMap<String, Triple>();
        for (Triple t : sq2) {
            Node p = t.getPredicate(); 
            if (p.isURI()) {
                ps2.add("<"+p.getURI()+">");
                map2.put("<"+p.getURI()+">", t);
            }
        }

        HashSet<Integer> css1 = computeRelevantCS(ps1, predicateIndex);
        HashSet<Integer> css2 = computeRelevantCS(ps2, predicateIndex);

        HashMap<String, Integer> mult12 = new HashMap<String, Integer>();
        long c = 0L;
        HashMap<String, Integer> mult21 = new HashMap<String, Integer>();
        boolean exists = false;
        for (Integer cs1 : css1) {
            Pair<Integer, HashMap<String, Integer>> cPs = css.get(cs1);
            HashMap<String, Integer> ps = cPs.getSecond();

            for (Integer cs2 : css2) {
                Pair<Integer, HashMap<String, Integer>> cPs2 = css.get(cs2);
                HashMap<String, Integer> psB = cPs2.getSecond();

                HashMap<Integer, HashMap<String, Integer>> css2Ps = cps.get(cs1);
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
                                exists = true;
                                Integer m1 = links12.get(p);
                                Integer m2 = mult12.get(p);
                                if (m2 == null) {
                                    m2 = 0;
                                }
                                mult12.put(p, m1+m2);
                            }
                        }
                    }
                }
                /*css2Ps = cps.get(cs2);
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
                                Integer m1 = links21.get(p);
                                Integer m2 = mult21.get(p);
                                if (m2 == null) {
                                    m2 = 0;
                                }
                                mult21.put(p, m1+m2);
                            }
                        }
                    }
                }*/
            }
        }
        //System.out.println("Links 1 -> 2, count: "+c12);
        for (String p : mult12.keySet()) {
            Integer tmp = mult12.get(p);
            c += tmp;
        }
        //System.out.println("Links 2 -> 1, count: "+c21);
        /*for (String p : mult21.keySet()) {
            Integer tmp = mult21.get(p);
            c += tmp;
        }*/
        if (!exists) {
            c=Long.MAX_VALUE;
        }
        return c;
    }

    public static HashSet<Integer> computeRelevantCS(Set<String> ps, HashMap<String, HashSet<Integer>> predicateIndex) {

        HashSet<Integer> intersection = null;
        for (String p : ps) {
            HashSet<Integer> tmp = predicateIndex.get(p);
            if (intersection == null) {
                intersection = new HashSet<Integer>(tmp);
            } else {
                intersection.retainAll(tmp);
            }
        }
        return intersection;
    }

    public static long cost(Set<Triple> ts, HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css, HashMap<String, HashSet<Integer>> predicateIndex, HashMap<Integer, Integer> cost) {

        HashSet<String> ps = new HashSet<String>();
        //HashMap<String, Triple> map = new HashMap<String, Triple>();
        for (Triple t : ts) {
            Node p = t.getPredicate(); 
            if (p.isURI()) {
                ps.add("<"+p.getURI()+">");
                //map.put(p.getURI(), t);
            }
        }
        Integer key = produceStarJoinOrdering.getIKey(ps);        
        if (cost.containsKey(key)) {
            return cost.get(key);
        }
        long c = produceStarJoinOrdering.computeCost(ps, css, predicateIndex);
        //cost.put(key, c);
        return c;
    }

    public static Tree<Triple> getStarJoinOrder(HashSet<Triple> ts, HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css, HashMap<Integer, Integer> hc, HashMap<Integer, Set<String>> additionalSets, HashMap<String, HashSet<Integer>> predicateIndex, HashMap<Integer, Integer> cost) {
        HashSet<String> ps = new HashSet<String>();
        HashMap<String, Triple> map = new HashMap<String, Triple>();
        for (Triple t : ts) {
            Node p = t.getPredicate(); 
            if (p.isURI()) {
                ps.add("<"+p.getURI()+">");
                map.put(p.getURI(), t);
            }
        }
        LinkedList<String> orderedPs = produceStarJoinOrdering.getStarJoinOrdering(ps, css, hc, additionalSets, predicateIndex, cost);
        //Vector<Triple> sortedStar = new Vector<Triple>(); 
        Tree<Triple> sortedStar = null;
        for (String p : orderedPs) {
            if (map.containsKey(p)) {
                Triple t = map.get(p);
                ts.remove(t);
                if (sortedStar == null) {
                    sortedStar = new Leaf<Triple>(t);
                } else {
                    sortedStar = new Branch<Triple>(sortedStar, new Leaf<Triple>(t));
                }
                //sortedStar.add(t);
            }
        }
        for (Triple t : ts) {
            if (sortedStar == null) {
                sortedStar = new Leaf<Triple>(t);
            } else {
                sortedStar = new Branch<Triple>(sortedStar, new Leaf<Triple>(t));
            }
            //sortedStar.add(t);
        }
        return sortedStar;
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

    public static Vector<HashSet<Triple>> getStars(BGP bgp, long budget, HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css, HashMap<String, HashSet<Integer>> predicateIndex, HashMap<Integer, Integer> cost) {
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
            long card = cost(starSubj, css, predicateIndex, cost);
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
            long card = cost(starObj, css, predicateIndex, cost);
            if (card <= budget) {
                stars.add(starObj);
                ts.removeAll(starObj);
            }
        }
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
