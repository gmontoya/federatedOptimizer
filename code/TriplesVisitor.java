//package semLAV;

import com.hp.hpl.jena.sparql.algebra.OpVisitorBase;
import java.util.*;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.algebra.op.OpTriple;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.core.BasicPattern;

public class TriplesVisitor extends OpVisitorBase {

    List<Triple> elems;

    public TriplesVisitor() {
        super();
        elems = new ArrayList<Triple>();
    }

    public void visit(OpBGP opBGP) {

        BasicPattern bp = opBGP.getPattern();
        List<Triple> aux = bp.getList();
        elems.addAll(aux);
    }


    public List<Triple> getTriples() {
        return elems;
    }
}
