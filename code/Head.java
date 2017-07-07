//package semLAV;

import java.util.*;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.shared.PrefixMapping;

public class Head {

    private String name;
    private List<Node> args;

    public Head(String n, List<? extends Node> as) {

        this.name = n;
        this.args = new ArrayList<Node>();
        for (Node a : as) {
            this.args.add(a);
        }
    }

    public List<Node> getArguments() {
        List<Node> as = new ArrayList<Node>();
        for (Node n : this.args) {
            as.add(n);
        }
        return as;
    }

    public String getName() {
        return this.name;
    }

    public Head replace (List<Node> mapping) {
        return new Head(this.name, mapping);
    }

    public String toString() {

        String s = name + "(";
        for (Node n : args) {
            s = s + n + ", ";
        }
        if (args.size()>0) {
            s = s.substring(0, s.length()-2);
        }
        s = s + ")";
        return s;
    }

    public Predicate toPredicate(HashMap<String, String> cs, PrefixMapping p) {

        String s = name+"(";
        for (Node n : args) {
            String a = null;
            if (n.isURI()) {
                a = p.expandPrefix(n.toString());
            } else {
                a = n.toString();
            }
            Set<String> keys = cs.keySet();
            for (String k : keys) {
                String v = cs.get(k);
                if (v.equals(a)) {
                    a = k;
                    break;
                }
            }
            s = s + a + ", ";
        }
        if (args.size() > 0) {
            s = s.substring(0, s.length()-2);
        }
        s = s + ")";
        return new Predicate(s);
    }
}

