import org.openrdf.query.parser.ParsedOperation;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.query.QueryLanguage;
import com.fluidops.fedx.structures.QueryInfo;
import com.fluidops.fedx.structures.QueryType;
import org.openrdf.query.parser.ParsedQuery;
import com.fluidops.fedx.optimizer.Optimizer;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.query.impl.EmptyBindingSet;
import com.fluidops.fedx.FederationManager;
import com.fluidops.fedx.Config;
import org.openrdf.query.algebra.StatementPattern;
import com.fluidops.fedx.algebra.FedXStatementPattern;
import com.fluidops.fedx.EndpointManager;
import com.fluidops.fedx.structures.Endpoint;
import com.fluidops.fedx.algebra.StatementSource;
//import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.fluidops.fedx.algebra.ExclusiveGroup;
import com.fluidops.fedx.algebra.ExclusiveStatement;
import com.fluidops.fedx.algebra.StatementSourcePattern;

import org.openrdf.model.Value;
import org.openrdf.model.URI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Vector;
import java.util.HashSet;
import java.util.ArrayList;
import com.hp.hpl.jena.graph.Triple;

class bridge {

    public static void main(String[] args) throws Exception {
        String queryFile = args[0];
        String datasetsFile = args[1];
        String queryString = Files.lines(Paths.get(queryFile)).collect(Collectors.joining("\n"));
        evaluateSPARQLQuery.readDatasets(datasetsFile);
        Vector<String> endpoints = evaluateSPARQLQuery.endpoints;
        evaluateSPARQLQuery.prepareFedX();
        Thread.sleep(100);
        //System.out.println("prepated fedX");
        Config.getConfig().set("optimize", "true");
        HashMap<Triple, Set<Integer>> selection = getSelection(queryString, endpoints);
        System.out.println(selection);
        FederationManager.getInstance().shutDown();
        System.exit(0);
    }

    public static void main2(String[] args) throws Exception {
        String queryFile = args[0];
        String datasetsFile = args[1];
        String queryString = Files.lines(Paths.get(queryFile)).collect(Collectors.joining("\n"));
        evaluateSPARQLQuery.readDatasets(datasetsFile);
        Vector<String> endpoints = evaluateSPARQLQuery.endpoints;
        evaluateSPARQLQuery.prepareFedX();
        Thread.sleep(100);
        //System.out.println("prepated fedX");
        Config.getConfig().set("optimize", "true");
        HashMap<Set<Triple>, Set<Integer>> decomposition = getDecomposition(queryString, endpoints);
        System.out.println(decomposition);
        FederationManager.getInstance().shutDown();
        System.exit(0);
    }    

    public static void getSubqueries(String queryString, List<ExclusiveGroup> groups, List<ExclusiveStatement> exclStmts, List<StatementSourcePattern> stmts) {
        //TupleExpr te = getTupleExpr(queryFile);
        try {
            TupleExpr te = getTupleExpr(queryString);
            GroupVisitor gv = new GroupVisitor();
            te.visit(gv);
            groups.addAll(gv.getExclusiveGroups());
            exclStmts.addAll(gv.getExclusiveStatements());
            stmts.addAll(gv.getStatementSourcePatterns());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static List<FedXStatementPattern> getStatements(String queryString) {
        //TupleExpr te = getTupleExpr(queryFile);
        List<FedXStatementPattern> ss = new ArrayList<FedXStatementPattern>();
        try {
            TupleExpr te = getTupleExpr(queryString);
            StatementsVisitor sv = new StatementsVisitor();
            te.visit(sv);
            ss = sv.getFedXStatements();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return ss;
    }

    public static Node getNode(org.openrdf.query.algebra.Var v) {
        Node n = null;
        if (v.hasValue()) {
            Value value = v.getValue();
            String str = value.stringValue();
            if (value instanceof URI) {
                n = NodeFactory.createURI(str);
            } else {
                n = NodeFactory.createLiteral(str);
            }
        } else {
            n = NodeFactory.createVariable(v.getName());
        }
        return n;
    }

    public static HashMap<Set<Triple>, Set<Integer>> getDecomposition(String queryString, Vector<String> endpoints) {
        List<ExclusiveGroup> groups = new ArrayList<ExclusiveGroup>();
        List<ExclusiveStatement> exclStmts = new ArrayList<ExclusiveStatement>();
        List<StatementSourcePattern> stmts = new ArrayList<StatementSourcePattern>();
        getSubqueries(queryString, groups, exclStmts, stmts);
        HashMap<Set<Triple>, Set<Integer>> decomposition = new HashMap<Set<Triple>, Set<Integer>>();
        for (ExclusiveGroup eg : groups) {
            List<ExclusiveStatement> ess = eg.getStatements();
            StatementSource ss = eg.getOwner();
            String x = ss.getEndpointID();
            Endpoint endpoint = EndpointManager.getEndpointManager().getEndpoint(x);
            Integer e = endpoints.indexOf(endpoint.getEndpoint());
            HashSet<Integer> es = new HashSet<Integer>();
            es.add(e);
            Set<Triple> ts = new HashSet<Triple>();
            for (ExclusiveStatement exclStmt : ess) {
                org.openrdf.query.algebra.Var subj = exclStmt.getSubjectVar();
                org.openrdf.query.algebra.Var pred = exclStmt.getPredicateVar();
                org.openrdf.query.algebra.Var obj = exclStmt.getObjectVar();
                Node ns = getNode(subj);
                Node np = getNode(pred);
                Node no = getNode(obj);
                Triple t = new Triple(ns, np, no);
                ts.add(t);
            }
            decomposition.put(ts, es);
        }
        for (ExclusiveStatement exclStmt : exclStmts) {
            StatementSource ss = exclStmt.getOwner();
            String x = ss.getEndpointID();
            Endpoint endpoint = EndpointManager.getEndpointManager().getEndpoint(x);
            Integer e = endpoints.indexOf(endpoint.getEndpoint());
            HashSet<Integer> es = new HashSet<Integer>();
            es.add(e);
            Set<Triple> ts = new HashSet<Triple>();
            org.openrdf.query.algebra.Var subj = exclStmt.getSubjectVar();
            org.openrdf.query.algebra.Var pred = exclStmt.getPredicateVar();
            org.openrdf.query.algebra.Var obj = exclStmt.getObjectVar();
            Node ns = getNode(subj);
            Node np = getNode(pred);
            Node no = getNode(obj);
            Triple t = new Triple(ns, np, no);
            ts.add(t);
            decomposition.put(ts, es);
        }
        for (StatementSourcePattern ssp : stmts) {
            List<StatementSource> sss = ssp.getStatementSources();
            HashSet<Integer> es = new HashSet<Integer>();
            for (StatementSource ss : sss) {
                String x = ss.getEndpointID();
                Endpoint endpoint = EndpointManager.getEndpointManager().getEndpoint(x);
                Integer e = endpoints.indexOf(endpoint.getEndpoint());
                es.add(e);
            }
            Set<Triple> ts = new HashSet<Triple>();
            org.openrdf.query.algebra.Var subj = ssp.getSubjectVar();
            org.openrdf.query.algebra.Var pred = ssp.getPredicateVar();
            org.openrdf.query.algebra.Var obj = ssp.getObjectVar();
            Node ns = getNode(subj);
            Node np = getNode(pred);
            Node no = getNode(obj);
            Triple t = new Triple(ns, np, no);
            ts.add(t);
            decomposition.put(ts, es);
        }
        return decomposition;
    }

    public static HashMap<Triple, Set<Integer>> getSelection(String queryString, Vector<String> endpoints) {
        //System.out.println("queryString: "+queryString);
        List<FedXStatementPattern> ss = getStatements(queryString);
        HashMap<Triple, Set<Integer>> selection = new HashMap<Triple, Set<Integer>>();
        for (FedXStatementPattern s : ss) {
            org.openrdf.query.algebra.Var subj = s.getSubjectVar();
            org.openrdf.query.algebra.Var pred = s.getPredicateVar();
            org.openrdf.query.algebra.Var obj = s.getObjectVar();
            //System.out.println(subj+" "+pred+" "+obj);
            List<StatementSource> l = s.getStatementSources();
            HashSet<Integer> es = new HashSet<Integer>();
            Node ns = getNode(subj);
            Node np = getNode(pred);
            Node no = getNode(obj);
            Triple t = new Triple(ns, np, no);
            for (StatementSource e : l) {
                String x = e.getEndpointID();
                Endpoint endpoint = EndpointManager.getEndpointManager().getEndpoint(x);
                String y = endpoint.getEndpoint();
                //System.out.println(x+" "+y);
                es.add(endpoints.indexOf(y));
            }
            selection.put(t, es);
        }
        return selection;
    }

    public static TupleExpr getTupleExpr(String queryString) throws Exception { 
        //String queryString = Files.lines(Paths.get(queryFile)).collect(Collectors.joining("\n"));
        ParsedOperation query = QueryParserUtil.parseOperation(QueryLanguage.SPARQL, queryString, null);
        QueryInfo qInfo = new QueryInfo(queryString, QueryType.SELECT);
        TupleExpr tupleExpr = ((ParsedQuery)query).getTupleExpr();
        try {
            tupleExpr = Optimizer.optimize(tupleExpr, new DatasetImpl(), EmptyBindingSet.getInstance(), FederationManager.getInstance().getStrategy(), qInfo);
            return tupleExpr;
        } catch (Exception e) {
            System.err.println("Unable to retrieve query plan: " + e.getMessage());
            System.exit(1);
        }
        return null;
    }
}
