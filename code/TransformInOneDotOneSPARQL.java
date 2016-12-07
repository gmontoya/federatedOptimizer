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


import java.util.*;

class TransformInOneDotOneSPARQL extends TransformCopy {

    HashMap<HashSet<Triple>, Vector<Tree<Pair<Integer,Triple>>>> plans;
    ExprList el;
    Vector<String> endpoints;

    public TransformInOneDotOneSPARQL(HashMap<HashSet<Triple>, Vector<Tree<Pair<Integer,Triple>>>> ps, ExprList el, Vector<String> es) {
        this.plans = ps;
        this.el = el;
        this.endpoints = es;
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
        return new OpService(opService.getService(), subOp, opService.getSilent());
    }

    public Op transform(OpProject opProject, Op subOp) {

        if (subOp instanceof OpNull) {
            return subOp;
        }
        return new OpProject(subOp, opProject.getVars());
    }

    private String exclusive(Op op1, Op op2, String e, boolean b) {
        //System.out.println("op1: "+op1+". op2: "+op2);
        VisitorExclusiveGroup veg = new VisitorExclusiveGroup(plans, endpoints, e, b);
        OpWalker ow = new OpWalker();
        ow.walk(op1, veg);
        boolean b1 = veg.isExclusive();
        if (b1) {
            String e1 = veg.getEndpoint();
            veg = new VisitorExclusiveGroup(plans, endpoints, e1, b1);
            OpWalker.walk(op2, veg);
            boolean b2 = veg.isExclusive();
            String e2 = veg.getEndpoint();
            //System.out.println("e2: "+e2);
            if (b2) {
                return e2;
            }
        }
        return null;
    }

    private Op clean(Op op) {

        Transform t = new TransformDeleteServices();
        Op newOp = Transformer.transform(t, op);
        return newOp;
    }

    public Op transform(OpUnion opUnion, Op left, Op right) {
        //System.out.println("transforming union: "+opUnion);
        String e = exclusive(left, right, null, true);
        //System.out.println("exclusive: "+e);
        if (e != null) {
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
        }
    }

    public Op transform(OpLeftJoin opLeftJoin, Op left, Op right) {
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
            op = new OpService(n, clean(op), false);
            return op;
        }
        if (left instanceof OpNull) {
            return left;
        } else if (right instanceof OpNull) {
            return left;
        } else {
            return OpLeftJoin.create(left, right, opLeftJoin.getExprs());
        }
    }

    public Op transform(OpJoin opJoin, Op left, Op right) {
        //System.out.println("tranforming join: "+opJoin);
        String e = exclusive(left, right, null, true);
        //System.out.println("exclusive: "+e);
        if (e != null) {
            Node n = NodeFactory.createURI(e);
            Op op = left;
            if (left instanceof OpNull) {
                op = left;
            } else if (right instanceof OpNull) {
                op = right;
            } else {
                op = OpJoin.create(left, right);
            }
            op = new OpService(n, clean(op), false);
            return op;
        }
        if (left instanceof OpNull) {
            return left;
        } else if (right instanceof OpNull) {
            return right;
        } else {
            return OpJoin.create(left, right);
        }

    }

    public Op transform(OpGroup opGroup, Op subOp) {
        //System.out.println("tranforming group: "+opGroup);
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

    }

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
    }

    public Op transform(OpBGP opBGP) {
        //System.out.println("transform BGP "+opBGP);
        BasicPattern bp = opBGP.getPattern();
        Iterator<Triple> it = bp.iterator();
        HashSet<Triple> triples = new HashSet<Triple>();
        while (it.hasNext()) {
            triples.add(it.next());
        }
        Op u = OpNull.create();
        //System.out.println("plans: "+plans);
        //System.out.println("triples: "+triples);
        Vector<Tree<Pair<Integer,Triple>>> vTree = plans.get(triples);
        if (vTree == null) {
            return u;
        }
        //System.out.println("vTree: "+vTree);
        for (Tree<Pair<Integer,Triple>> tree : vTree) {
            Op op = transform(tree);
            if (u instanceof OpNull) {
                u = op;
            } else {
                u = new OpUnion(u, op);
            }
        }
        return u;
    }

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

    public Op transform(Tree<Pair<Integer,Triple>> tree) {
        Set<Pair<Integer, Triple>> elems = tree.getElements();
        Op op = OpNull.create();
        if (sameSource(elems) != null) {
            op = makeOpJoinTree(tree);
            String source = endpoints.get(elems.iterator().next().getFirst());
            List<String> vars = getVariables(tree);
            op = generateService(source, op, vars);
        } else {
            Branch<Pair<Integer, Triple>> b = (Branch<Pair<Integer, Triple>>) tree;
            Tree<Pair<Integer, Triple>> l = b.getLeft();
            Tree<Pair<Integer, Triple>> r = b.getRight();
            Op opL = transform(l);
            Op opR = transform(r);
            if (!(opL instanceof OpNull) && !(opR instanceof OpNull)) {
                op = OpJoin.create(opL, opR);
            } else if (!(opL instanceof OpNull)) {
                op = opL;
            } else if (!(opR instanceof OpNull)) {
                op = opR;
            }
        }
        return op;
    }

    public Op makeOpJoinTree(Tree<Pair<Integer,Triple>> tree) {
        Op op = OpNull.create();
        if (tree instanceof Leaf<?>) {
            Triple t  = tree.getOneElement().getSecond();
            op = new OpTriple(t);
        } else {
            Branch<Pair<Integer, Triple>> b = (Branch<Pair<Integer, Triple>>) tree;
            Tree<Pair<Integer, Triple>> l = b.getLeft();
            Tree<Pair<Integer, Triple>> r = b.getRight();
            Op opL = makeOpJoinTree(l);
            Op opR = makeOpJoinTree(r);
            if (!(opL instanceof OpNull) && !(opR instanceof OpNull)) {
                op = OpJoin.create(opL, opR);
            } else if (!(opL instanceof OpNull)) {
                op = opL;
            } else if (!(opR instanceof OpNull)) {
                op = opR;
            }
        }
        return op;
    }
}
