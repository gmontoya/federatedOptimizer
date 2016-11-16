import java.util.*;
import java.io.*;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.*;
import com.hp.hpl.jena.sparql.syntax.*; 
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.shared.PrefixMapping;

/* Class that provides access to a query triple patterns */
public class BGP {

    List<Triple> body;
    PrefixMapping p;
    List<Var> projectedVariables;
    boolean distinct;

    public BGP(String fileName) {

        this(new File(fileName));
    }

    public BGP(File f) {

        String fn = f.getAbsolutePath();
        Query query = QueryFactory.read(fn);
        //int begin = fn.lastIndexOf("/")+1;
        //int end = fn.lastIndexOf(".");
        //end = end == -1 ? fn.length() : end;
        //fn = fn.substring(begin, end);
        BGPInit(query);
    }

    public BGP(Query q) {

        BGPInit(q);
    }

    private void BGPInit(Query query) {
        p = query.getPrefixMapping();
        List<? extends Node> projectedVars = query.getProjectVars();
        //this.head = new Head(fn, projectedVars);
        Op op = Algebra.compile(query);
        TriplesVisitor mv = new TriplesVisitor();
        OpWalker ow = new OpWalker();
        ow.walk(op, mv);
        this.body = mv.getTriples();
        this.distinct = query.isDistinct();
        this.projectedVariables = query.getProjectVars();
    } 

    public PrefixMapping getPrefixMapping() {

        return p;
    }

    public int getNumberTriples() {
        return body.size();
    }

    public List<Triple> getBody() {
        return this.body;
    }

    public List<Var> getProjectedVariables() {

        return projectedVariables;
    }

    public boolean getDistinct() {
        return distinct; 
    }

    public String toString() {

        String s = "";
        for (Triple t : body) {
            s = s + t.toString()+". ";
        }
        if (body.size() > 0) {
            s = s.substring(0, s.length()-2);
        }
        return s;
    }
}
