//package semLAV;

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

public class ConjunctiveQuery {

    Head head;
    List<Triple> body;
    PrefixMapping p;

    public ConjunctiveQuery(String fileName) {

        this(new File(fileName));
    }

    public ConjunctiveQuery(File f) {

        String fn = f.getAbsolutePath();
        Query query = QueryFactory.read(fn);
        int begin = fn.lastIndexOf("/")+1;
        int end = fn.lastIndexOf(".");
        end = end == -1 ? fn.length() : end;
        fn = fn.substring(begin, end);
        ConjunctiveQueryInit(query, fn);
    }

    public ConjunctiveQuery(Query q, String fn) {

        ConjunctiveQueryInit(q, fn);
    }

    private void ConjunctiveQueryInit(Query query, String fn) {
        p = query.getPrefixMapping();
        List<? extends Node> projectedVars = query.getProjectVars();
        this.head = new Head(fn, projectedVars);
        Op op = Algebra.compile(query);
        myVisitor123 mv = new myVisitor123();
        OpWalker ow = new OpWalker();
        ow.walk(op, mv);
        this.body = mv.getTriples();
    } 

    public PrefixMapping getPrefixMapping() {

        return p;
    }

    public int getNumberSubgoals() {
        return body.size();
    }

    public List<Triple> getBody() {
        return this.body;
    }

    public Head getHead() {
        return head;
    }

    public String toString() {

        String s = head.toString() + " :- ";
        for (Triple t : body) {
            s = s + t.getPredicate()+"("+t.getSubject()+", "+t.getObject()+"), ";
        }
        if (body.size() > 0) {
            s = s.substring(0, s.length()-2);
        }
        return s;
    }
}

