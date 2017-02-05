//package semLAV;

import com.hp.hpl.jena.sparql.algebra.OpVisitorBase;
import java.util.*;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.algebra.*;
import com.hp.hpl.jena.sparql.algebra.op.*;

public class VisitorCollectTriples extends OpVisitorBase {

    Vector<Op> triples;

    public VisitorCollectTriples() {
        super();
        triples = new Vector<Op>();
    }

    public void visit(OpTriple opTriple) {
        triples.add(opTriple);
    }

    public void visit(OpBGP opBGP) {

        BasicPattern bp = opBGP.getPattern();
        List<Triple> aux = bp.getList();
        for (Triple t : aux) {
            triples.add(new OpTriple(t));
        }
    }


    public Vector<Op> getTriples() {
        return triples;
    }
}
