import com.hp.hpl.jena.sparql.algebra.OpVisitorBase;
import java.util.*;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.algebra.OpWalker;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.algebra.AlgebraGenerator;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.graph.Node;

public class VisitorExclusiveGroup extends OpVisitorBase {

    String endpoint;
    boolean exclusive;
    HashMap<HashSet<Triple>, Vector<Tree<Pair<Integer,Triple>>>> plans;
    Vector<String> endpoints;
 
    public VisitorExclusiveGroup(HashMap<HashSet<Triple>, Vector<Tree<Pair<Integer,Triple>>>> ps, Vector<String> es) {
        super();
        exclusive = true;
        plans = ps;
        endpoint = null;
        endpoints = es;
    }

    public VisitorExclusiveGroup(HashMap<HashSet<Triple>, Vector<Tree<Pair<Integer,Triple>>>> ps, Vector<String> es, String e, boolean b) {
        super();
        //System.out.println("Starting visitor with e: "+e+" and b: "+b);
        exclusive = b;
        plans = ps;
        endpoint = e;
        endpoints = es;
    }

    public void visit(OpLeftJoin opLeftJoin) {
        //System.out.println("visitingLeftJoin "+opLeftJoin);
        Op left = opLeftJoin.getLeft();
        Op right = opLeftJoin.getRight();
        //System.out.println("before left, endpoint: "+endpoint+". exclusive: "+exclusive);
        //left.visit(this);
        //System.out.println("before right, endpoint: "+endpoint+". exclusive: "+exclusive);
        //right.visit(this);
        //System.out.println("after right, endpoint: "+endpoint+". exclusive: "+exclusive);
    }

    public void visit(OpJoin opJoin) {
        //System.out.println("visitingJoin "+opJoin);
        Op left = opJoin.getLeft();
        Op right = opJoin.getRight();
        //left.visit(this);
        //right.visit(this);
    }

    public void visit(OpUnion opUnion) {
        //System.out.println("visitingUnion "+opUnion);
        Op left = opUnion.getLeft();
        Op right = opUnion.getRight();
        //left.visit(this);
        //right.visit(this);
    }

    @Override
    public void visit(OpService service) {
        //System.out.println("visitingService "+service);
        Node n = service.getService();
        String tmpEndpoint = n.getURI();
        exclusive = exclusive && (tmpEndpoint != null) && (endpoint == null || tmpEndpoint.equals(endpoint));
        endpoint = tmpEndpoint;
        //super.visit(service);
    }

    public void visit (OpBGP opBGP) {
        //System.out.println("visitingBGP "+opBGP);
        BasicPattern bp = opBGP.getPattern();
        Iterator<Triple> it = bp.iterator();
        HashSet<Triple> triples = new HashSet<Triple>();
        while (it.hasNext()) {
            Triple t = it.next();
            triples.add(t); 
        }
        Vector<Tree<Pair<Integer,Triple>>> vTree = plans.get(triples);
        String s = null;
        if (vTree != null && (vTree.size() == 1)) {
            Set<Pair<Integer, Triple>> elems = vTree.get(0).getElements();
            s = endpoints.get(sameSource(elems));
        }
        exclusive = exclusive && (s != null) && (endpoint == null || s.equals(endpoint));
        if (s != null) {
            endpoint = s;
        }
        System.out.println("End of the visit. exclusive: "+exclusive+". endpoint: "+endpoint);
    }

    public boolean isExclusive() {
        return exclusive;
    }

    public String getEndpoint() {
        return endpoint;
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
}
