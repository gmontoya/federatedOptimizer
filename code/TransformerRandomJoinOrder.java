import com.hp.hpl.jena.sparql.algebra.*;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.syntax.*;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.core.Var;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import java.util.*;

class TransformerRandomJoinOrder extends TransformCopy {

    ExprList el;
//    Vector<Op> services = new Vector<Op>();
    boolean bushy;

    public TransformerRandomJoinOrder(ExprList el, boolean b) {
        this.el = el;
        this.bushy = b;
    }

    public Op transform(OpNull opNull) {

        return opNull;
    }

    public Op transform(OpDistinct opDistinct, Op subOp) {
        if (subOp instanceof OpNull) {
            return subOp;
        }
        return new OpDistinct(subOp);
    }

    public Op transform(OpService opService, Op subOp) {
        //System.out.println("transforming opService: "+opService);
        if (subOp instanceof OpNull) {
            return subOp;
        }
        Op op = new OpService(opService.getService(), subOp, opService.getSilent());
//        services.add(op);
        return op;
    }

    public Op transform(OpProject opProject, Op subOp) {

        if (subOp instanceof OpNull) {
            return subOp;
        }
        return new OpProject(subOp, opProject.getVars());
    }

    private boolean isJoin(Op op) {
        VisitorJoin vj = new VisitorJoin();
        OpWalker ow = new OpWalker();
        ow.walk(op, vj);
        boolean b = vj.isJoin();
        return b;
    }

    private Op clean(Op op) {

        Transform t = new TransformDeleteServices();
        Op newOp = Transformer.transform(t, op);
        return newOp;
    }

    //public Op transform(OpUnion opUnion, Op left, Op right) {
        //System.out.println("transforming union: "+opUnion);
        //String e = exclusive(left, right, null, true);
        //System.out.println("exclusive: "+e);
        /*if (e != null) {
            Node n = NodeFactory.createURI(e);
            Op op = left;
            //System.out.println("left: "+left);
            //System.out.println("right: "+right+". class: "+right.getClass());
            if (left instanceof OpNull) {
                op = right;
            } else if (!(right instanceof OpNull)) {
                op = OpUnion.create(left, right);
            }
            op = new OpService(n, clean(op), false);
            return op;
        }
        if (left instanceof OpNull) {
            return right;
        } else if (right instanceof OpNull) {
            return left;
        } else {
            return OpUnion.create(left, right);
        }*/
    //}

    /*public Op transform(OpLeftJoin opLeftJoin, Op left, Op right) {
        //System.out.println("transforming leftjoin: "+opLeftJoin);
        String e = exclusive(left, right, null, true);
        //System.out.println("exclusive: "+e);
        if (e != null) {
            Node n = NodeFactory.createURI(e);
            Op op = left;
            if (left instanceof OpNull) {
                op = left;
            } else if (right instanceof OpNull) {
                op = left;
            } else {
                op = OpLeftJoin.create(left, right, opLeftJoin.getExprs());
            }
            if (!(op instanceof OpNull)) {
                op = new OpService(n, clean(op), false);
            }
            return op;
        }
        if (left instanceof OpNull) {
            return left;
        } else if (right instanceof OpNull) {
            return left;
        } else {
            return OpLeftJoin.create(left, right, opLeftJoin.getExprs());
        }
    }*/

    public boolean hasServices(Op op) {
        VisitorService vs = new VisitorService();
        OpWalker ow = new OpWalker();
        ow.walk(op, vs);
        boolean b = vs.hasService();
        return b;
    }

    public Vector<Op> getServices(Op op) {
        VisitorCollectServices vcs = new VisitorCollectServices();
        OpWalker ow = new OpWalker();
        ow.walk(op, vcs);
        Vector<Op> services = vcs.getServices();
        return services;
    }

    public Op makeNewJoin(Vector<Op> ops) {
        Vector<Op> aux = new Vector<Op>(ops);
        ops.clear();
        Random r = new Random();
        while (aux.size()>0) {
             int p =  r.nextInt(aux.size());
             ops.add(aux.remove(p));
        }
        Op op = OpNull.create();
        if (this.bushy) {
            while(ops.size()>1) {
                Vector<Op> tmp = new Vector<Op>();
                while(ops.size()>1) {
                    Op op1 = ops.remove(0);
                    Op op2 = ops.remove(0);
                    Op newOp = OpJoin.create(op1, op2);
                    tmp.add(newOp);
                }
                if (ops.size()>0) {
                    tmp.add(ops.remove(0));
                }
                ops = tmp;
            }
            if (ops.size()>0) {
                op = ops.remove(0);
            }
        } else {
             if (ops.size()>0) {
                 op = ops.remove(0);
             }
             while (ops.size()>0) {
                 Op tmp = ops.remove(0);
                 op = OpJoin.create(op, tmp);
             }
        }
        return op;
    }

    public Vector<Op> getTriples(Op op) {
        VisitorCollectTriples vct = new VisitorCollectTriples();
        OpWalker ow = new OpWalker();
        ow.walk(op, vct);
        Vector<Op> triples = vct.getTriples();
        return triples;
    }

    public Op transform(OpJoin opJoin, Op left, Op right) {
        //System.out.println("transforming join: "+opJoin);
        Op op = OpJoin.create(left, right);
        boolean b1 = isJoin(left);
        boolean b2 = isJoin(right);
        //System.out.println("b1: "+b1);
        //System.out.println("b2: "+b2);
        if (b1 && b2) {
            boolean hasServices1 = hasServices(left);
            boolean hasServices2 = hasServices(right);
            //System.out.println("hasServices1: "+hasServices1);
            //System.out.println("hasServices2: "+hasServices2);
            if (hasServices1 && hasServices2) {
                Vector<Op> services = new Vector<Op>(getServices(left));
                //System.out.println("services1: "+services);
                services.addAll(getServices(right));
                //System.out.println("services1+2: "+services);
                op = makeNewJoin(services);
            } else if (!hasServices1 && !hasServices2) {
                Vector<Op> triples = new Vector<Op>(getTriples(left));
                //System.out.println("triples1: "+triples);
                triples.addAll(getTriples(right));
                //System.out.println("triples2: "+triples);
                op = makeNewJoin(triples);
            }
        }
        //System.out.println("transformed join: "+op);
        return op;
    }
/*
    public Op transform(OpGroup opGroup, Op subOp) {
        System.out.println("tranforming group: "+opGroup);
        String e = exclusive(opGroup, subOp, null, true);
        //System.out.println("exclusive: "+e);
        if (e != null) {
            Node n = NodeFactory.createURI(e);
            Op op = subOp;
            if (!(subOp instanceof OpNull)) {
                op = new OpGroup(subOp, opGroup.getGroupVars(), opGroup.getAggregators());
            }
            op = new OpService(n, clean(op), false);
            return op;
        }
        if (subOp instanceof OpNull) {
            return subOp;
        }
        return new OpGroup(subOp, opGroup.getGroupVars(), opGroup.getAggregators());
        //return opGroup.apply(this, subOp);

    }*/
/*
    public Op transform(OpFilter opFilter, Op subOp) {
        //System.out.println("tranforming filter: "+opFilter);
        String e = exclusive(opFilter, subOp, null, true);
        //System.out.println("exclusive: "+e);
        if (e != null) {
            Node n = NodeFactory.createURI(e);
            Op op = subOp;
            if (!(subOp instanceof OpNull)) {
                op = OpFilter.filter(opFilter.getExprs(), subOp);
            }
            op = new OpService(n, clean(op), false);
            return op;
        }
        if (subOp instanceof OpNull) {
            return subOp;
        }
        return OpFilter.filter(opFilter.getExprs(), subOp);
    }*/

    public Op transform(OpBGP opBGP) {
        //System.out.println("transforming BGP "+opBGP);
        Vector<Op> triples = new Vector<Op>(getTriples(opBGP));
        Op op = makeNewJoin(triples);
        //System.out.println("transformed BGP "+op);
        return op;
    }
/*
    public static Op merge(Op acc, Op op) {

        if (op instanceof OpService) {
            return new OpUnion(acc, op);
        } else if (op instanceof OpJoin) {
            Op left = ((OpJoin) op).getLeft();
            Op right = ((OpJoin) op).getRight();
            List<Op> commonL = getCommon(acc, left);
            List<Op> commonR = getCommon(acc, right);

        }
    }
*/
    public static ExprList getExprConcerned(List<String> l, ExprList el) {

        //System.out.println("searching concerned for triple elements: "+l);
        ExprList r = new ExprList();
        Iterator<Expr> i = el.iterator();
        while (i.hasNext()) {
            Expr e = i.next();
            Collection<Var> vs = new ArrayList<Var>();
            e.varsMentioned(vs);
            //System.out.println("mentioned variables in "+e.toString()+" are: "+Arrays.toString(vs.toArray()));
            for (Var v : vs) {
                if (l.contains(v.getVarName())) {
                    r.add(e);
                    break;
                }
            }
        }
        return r;
    }

    public Op generateService(String source, Op op, List<String> vars) {

        Node n = NodeFactory.createURI(source);
        ExprList l0 = getExprConcerned(vars, el);
        if (l0.size()>0) {
            op = OpFilter.filter(l0, op);
        }
        op = new OpService(n, op, false);    
        return op;
    }

    public List<String> getVariables(Tree<Pair<Integer,Triple>> tree) {

        List<String> l = new ArrayList<String>();
        Set<Pair<Integer, Triple>> elems = tree.getElements();
        for (Pair<Integer, Triple> e : elems) {
            Triple t = e.getSecond();
            l.addAll(getVars(t));
        }
        return l;
    }

    public static List<String> getVars(Triple t) {

        List<String> l = new ArrayList<String>();
        Node n = t.getObject();
        if (n.isVariable()) {
            l.add(n.getName());
        }
        n = t.getPredicate();
        if (n.isVariable()) {
            l.add(n.getName());
        }
        n = t.getSubject();
        if (n.isVariable()) {
            l.add(n.getName());
        }
        return l;
    }

    public static void main(String[] args) {

        String queryFile = args[0];
        boolean b = Boolean.parseBoolean(args[1]);
        Query query = QueryFactory.read(queryFile);
        Op op = (new AlgebraGenerator()).compile(query);
        TransformerDeleteFilters t0 = new TransformerDeleteFilters();
        Op opBase = Transformer.transform(t0, op);
        ExprList el = t0.getFilters();
        Transform t = new TransformerRandomJoinOrder(el,b);
        Op newOp = Transformer.transform(t, opBase);
        Query newQuery = OpAsQuery.asQuery(newOp);
        System.out.println(newQuery);
    }
}
