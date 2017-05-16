
import java.io.*;
import java.util.*;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.core.Var;

class testKeggCompoundId {

    public static void main(String[] args) throws Exception {

        String fileIIO1 = args[0];
        String fileIIS2 = args[1];
        String fileIO1 = args[2];
        String fileIS2 = args[3];
        String fileCPS = args[4];
        HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio1 = obtainMIPBasedIndex.readIIO(fileIIO1);
        //HashMap<String, Integer> iio1 = obtainMIPBasedIndex.readIIS(fileIIO1);
        HashMap<String, Integer> iis2 = obtainMIPBasedIndex.readIIS(fileIIS2);
        HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps =  produceJoinOrderingFederationComplete.readCPS(fileCPS);
       
        String fileCSS1 = args[5];
        String fileCSS2 = args[6];

        HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css1 = addInterDatasetLinksMIPs.readCSS(fileCSS1);
        HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css2 = addInterDatasetLinksMIPs.readCSS(fileCSS2);
        HashMap<Integer, HashMap<String,MIPVector>> mipIO1 = addInterDatasetLinksMIPs.readMipIOText(fileIO1, css1);
        HashMap<Integer, MIPVector> mipIS2 = addInterDatasetLinksMIPs.readMipISText(fileIS2);

        Set<String> set = new HashSet<String>(iio1.keySet());
        set.retainAll(iis2.keySet());
        System.out.println("There are "+set.size()+" objects in common");
        Iterator<String> it = set.iterator();
        for (int i =0 ; i < 10; i++) {
            System.out.println(it.next());
        }
        for (String e : set) {
            //Integer css1 = iio1.get(e);
            Integer cs2 = iis2.get(e);
            Set<String> ps2 = css2.get(cs2).getSecond().keySet();
            System.out.println("2. css: "+cs2+". ps: "+ps2);
            MIPVector v2 = mipIS2.get(cs2);
            System.out.println("associated mip: "+mipIS2.get(cs2));
            HashMap<String, HashMap<Integer,Integer>> psCSSCount = iio1.get(e);
            for (String o : psCSSCount.keySet()) {
                for (Integer cs1 : psCSSCount.get(o).keySet()) {
                    Set<String> ps1 = css1.get(cs1).getSecond().keySet();
                    System.out.println("1. css: "+cs1+". ps: "+ps1);
                    MIPVector v1 = mipIO1.get(cs1).get(o);
                    System.out.println("associated mip: "+mipIO1.get(cs1).get(o));
                    int overlap = (int) v1.getOverlap(v2);
                    System.out.println("mips overlap: "+overlap);
                    System.out.println("cps(cs1): "+cps.get(cs1));
                    if (cps.get(cs1) != null && cps.get(cs1).get(cs2) != null) {
                        System.out.println("Linking predicates: "+cps.get(cs1).get(cs2).keySet());
                    }
                    System.out.println("cps(cs2): "+cps.get(cs2));
                    if (cps.get(cs2) != null && cps.get(cs2).get(cs1) != null) {
                        System.out.println("Linking predicates: "+cps.get(cs2).get(cs1).keySet());
                    }
                }
            }
        }
        System.out.println(cps.size()); 
/*        int ds1 = Integer.parseInt(args[0]);
        int ds2 = Integer.parseInt(args[1]);
        String p1 = args[2];
        String p2 = args[3];

        produceJoinOrderingFederationComplete.folder = args[4];
        String datasetsFile = args[5];
        //HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps = produceJoinOrderingFederationComplete.getCPSSubjObj(ds1, ds2);
        Set<Triple> set1 = new HashSet<Triple>();
        Triple t1 = new Triple(NodeFactory.createVariable("z"), NodeFactory.createURI(p1), NodeFactory.createVariable("y"));
        set1.add(t1);
        Set<Triple> set2 = new HashSet<Triple>();
        Triple t2 = new Triple(NodeFactory.createVariable("x"), NodeFactory.createURI(p2), NodeFactory.createVariable("y"));
        set2.add(t2);
        produceJoinOrderingFederationComplete.datasets = produceJoinOrderingFederationComplete.readDatasets(datasetsFile);
        System.out.println("produceJoinOrderingFederationComplete.datasets: "+produceJoinOrderingFederationComplete.datasets);
        HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexSubj = produceJoinOrderingFederationComplete.readPredicateIndexesSubj(produceJoinOrderingFederationComplete.folder, produceJoinOrderingFederationComplete.datasets);
        HashMap<String, HashMap<Integer,HashSet<Integer>>> predicateIndexObj = produceJoinOrderingFederationComplete.readPredicateIndexesObj(produceJoinOrderingFederationComplete.folder, produceJoinOrderingFederationComplete.datasets);
        long tmpCost = produceJoinOrderingFederationComplete.getCostCPSubjObj(set1, ds1, set2, ds2, predicateIndexSubj, predicateIndexObj);
        System.out.println("cost: "+tmpCost);*/
    }
}
