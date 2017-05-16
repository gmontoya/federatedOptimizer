import com.hp.hpl.jena.sparql.algebra.OpVisitorBase;
import java.util.*;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.algebra.OpWalker;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.algebra.AlgebraGenerator;
import com.hp.hpl.jena.sparql.algebra.*;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.syntax.*;

public class VisitorJoin extends OpVisitorBase {

    boolean join;
 
    public VisitorJoin() {
        super();
        join = true;
    }

    public void visit(OpLeftJoin opLeftJoin) {
        //System.out.println("visitingLeftJoin "+opLeftJoin);
        join = false;
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
        join = false;
        Op left = opUnion.getLeft();
        Op right = opUnion.getRight();
        //left.visit(this);
        //right.visit(this);
    }

    @Override
    public void visit(OpService service) {
        //System.out.println("visitingService "+service);
        Node n = service.getService();
        //String tmpEndpoint = n.getURI();
        //exclusive = exclusive && (tmpEndpoint != null) && (endpoint == null || tmpEndpoint.equals(endpoint));
        //endpoint = tmpEndpoint;
        //super.visit(service);
    }

    public void visit (OpBGP opBGP) {
        //System.out.println("visitingBGP "+opBGP);
        BasicPattern bp = opBGP.getPattern();
        /*Iterator<Triple> it = bp.iterator();
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
        System.out.println("End of the visit. exclusive: "+exclusive+". endpoint: "+endpoint);*/
    }

    public boolean isJoin() {
        return join;
    }
/*
    public String getEndpoint() {
        return endpoint;
    }*/
/*
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
    }*/
    public static void main(String args[]) {
        String queryFile = args[0];
        Query query = QueryFactory.read(queryFile);
        Op op = (new AlgebraGenerator()).compile(query);
        TransformerDeleteFilters t0 = new TransformerDeleteFilters();
        Op opBase = Transformer.transform(t0, op);
        ExprList el = t0.getFilters();
        VisitorJoin vj = new VisitorJoin();
        OpWalker ow = new OpWalker();
        ow.walk(opBase, vj);
        boolean isJoin = vj.isJoin();
        System.out.println(isJoin);
    }
}
