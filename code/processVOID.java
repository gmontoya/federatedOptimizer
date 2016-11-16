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

class processVOID {

    static Vector<String> datasets = new Vector<String>();
    static String folder;

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
            String fileName = folder+"/"+datasetStr+"_void.n3";
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

        String datasetsFile = args[0];
        folder = args[1];
        String globalFile = args[2];
        String propertyFile = args[3];
        String classFile = args[4];
        datasets = readDatasets(datasetsFile);
        // DatasetId --> Vector<Integer> <numTriples, numClasses, numProperties, numSubj, numObj>
        HashMap<Integer, Vector<Integer>> globalStats = new HashMap<Integer, Vector<Integer>>();
        // Predicate --> DatasetId --> (numTriples, (numDistSubj, numDistObj))
        HashMap<String, HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>>> propertyStats = new HashMap<String, HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>>>();
        // Class --> DatasetId --> numEntities
        HashMap<String, HashMap<Integer, Integer>> classStats = new HashMap<String, HashMap<Integer, Integer>>();
        loadStatistics(globalStats, propertyStats, classStats);
        //System.out.println(propertyStats);
        saveGlobal(globalStats, globalFile);
        saveProperty(propertyStats, propertyFile);
        saveClass(classStats, classFile);
    }

    public static void saveClass(HashMap<String, HashMap<Integer, Integer>> classStats, String fileName) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(fileName)));
            out.writeObject(classStats);
            out.close();
        } catch (IOException e) {
            System.err.println("Problems writing file: "+fileName);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void saveProperty(HashMap<String, HashMap<Integer, Pair<Integer, Pair<Integer, Integer>>>> propertyStats, String fileName) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(fileName)));
            out.writeObject(propertyStats);
            out.close();
        } catch (IOException e) {
            System.err.println("Problems writing file: "+fileName);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void saveGlobal(HashMap<Integer, Vector<Integer>> globalStats, String fileName) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(fileName)));
            out.writeObject(globalStats);
            out.close();
        } catch (IOException e) {
            System.err.println("Problems writing file: "+fileName);
            e.printStackTrace();
            System.exit(1);
        }
    }
}
