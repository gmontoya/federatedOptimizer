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
        //System.out.println("sameShape: "+sameShape(vTree));
        // To push down UNIONs
        if (vTree.size()>1 && sameShape(vTree)) {
            Tree<Pair<Set<Integer>, Triple>> tree = collapse(vTree);
            //System.out.println("collapsed tree: "+tree);
            Op op = transformPlus(tree);
            return op;
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

    private Tree<Pair<Set<Integer>, Triple>> collapse(Vector<Tree<Pair<Integer,Triple>>> vTree) {
        Tree<Pair<Integer,Triple>> t = vTree.get(0);
        boolean isLeaf = t instanceof Leaf<?>;
        Tree<Pair<Set<Integer>, Triple>> res;
        Vector<Tree<Pair<Integer,Triple>>> ls = new Vector<Tree<Pair<Integer,Triple>>>();
        Vector<Tree<Pair<Integer,Triple>>> rs = new Vector<Tree<Pair<Integer,Triple>>>();
        HashSet<Integer> set = new HashSet<Integer>();
        for (Tree<Pair<Integer,Triple>> t1 : vTree) {
            if (t1 instanceof Leaf<?>) {
                Integer e = t1.getOneElement().getFirst();
                set.add(e);                   
            } else {
                Branch<Pair<Integer, Triple>> b = (Branch<Pair<Integer, Triple>>) t1;
                ls.add(b.getLeft());
                rs.add(b.getRight());
            }
        }
        if (isLeaf) {
            Triple triple = t.getOneElement().getSecond();
            res = new Leaf(new Pair(set, triple));
        } else {
            res = new Branch(collapse(ls), collapse(rs));
        }
        return res;
    }

    private boolean sameShape(Vector<Tree<Pair<Integer,Triple>>> vTree) {
        Tree<Pair<Integer,Triple>> t = vTree.get(0);
        boolean isLeaf = t instanceof Leaf<?>;
        Triple triple = null;
        if (isLeaf) {
            triple  = t.getOneElement().getSecond();
        }
        Vector<Tree<Pair<Integer,Triple>>> ls = new Vector<Tree<Pair<Integer,Triple>>>();
        Vector<Tree<Pair<Integer,Triple>>> rs = new Vector<Tree<Pair<Integer,Triple>>>();
        boolean b = true;
        //System.out.println("isleaf? "+isLeaf);
        for (Tree<Pair<Integer,Triple>> t1 : vTree) {
            if (t1 instanceof Leaf<?>) {
                //System.out.println("t1 is Leaf");
                if (isLeaf) {
                    Triple taux =  t1.getOneElement().getSecond();
                    if (!triple.equals(taux)) {
                        //System.out.println("no taux equals triple: "+taux+" "+triple);
                        b = false;
                        break;
                    }
                } else {
                    //System.out.println("but t was not");
                    b = false;
                    break;
                }
            } else {
                //System.out.println("t1 is not Leaf");
                if (isLeaf) {
                    //System.out.println("but t was");
                    b = false;
                    break;
                } else {
                    Branch<Pair<Integer, Triple>> bAux = (Branch<Pair<Integer, Triple>>) t1;
                    ls.add(bAux.getLeft());
                    rs.add(bAux.getRight());
                }
            }
        }
        //System.out.println("b before end: "+b);
        //System.out.println("ls: "+ls);
        //System.out.println("rs: "+rs);
        if (b && !isLeaf) {
            b = sameShape(ls) && sameShape(rs);
        }
        return b;
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

    public List<String> getVariablesS(Tree<Pair<Set<Integer>,Triple>> tree) {

        List<String> l = new ArrayList<String>();
        Set<Pair<Set<Integer>, Triple>> elems = tree.getElements();
        for (Pair<Set<Integer>, Triple> e : elems) {
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

    public static Integer sameSourceS(Set<Pair<Set<Integer>, Triple>> elems) {
        boolean ss = true;
        Integer s = null;
        for (Pair<Set<Integer>, Triple> pair : elems) {
            Set<Integer> tmpS =  pair.getFirst();
            ss = ss && (tmpS.size() <= 1);
            Integer sAux = tmpS.iterator().next();
            ss = ss && (s==null||s.equals(sAux));
            s = sAux;
            if (!ss) {
                s = null;
                break;
            }
        }
        return s;
    }

// Tree<Pair<Set<Integer>, Triple>> tree

    public Op transformPlus(Tree<Pair<Set<Integer>,Triple>> tree) {
        Set<Pair<Set<Integer>, Triple>> elems = tree.getElements();
        Op op = OpNull.create();
        if (sameSourceS(elems) != null) {
            op = makeOpJoinTreeS(tree);
            String source = endpoints.get(elems.iterator().next().getFirst().iterator().next());
            List<String> vars = getVariablesS(tree);
            op = generateService(source, op, vars);
        } else if (tree instanceof Leaf<?>) {
            Set<Integer> sources = tree.getOneElement().getFirst();
            Triple t  = tree.getOneElement().getSecond();
            List<String> vars = getVariablesS(tree);
            for (Integer s : sources) {
                String source = endpoints.get(s);
                Op opAux = new OpTriple(t);
                opAux = generateService(source, opAux, vars);
                if (op instanceof OpNull) {
                    op = opAux;
                } else {
                    op = new OpUnion(op, opAux);
                }
            }
        } else {
            Branch<Pair<Set<Integer>, Triple>> b = (Branch<Pair<Set<Integer>, Triple>>) tree;
            Tree<Pair<Set<Integer>, Triple>> l = b.getLeft();
            Tree<Pair<Set<Integer>, Triple>> r = b.getRight();
            Op opL = transformPlus(l);
            Op opR = transformPlus(r);
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

    public Op makeOpJoinTreeS(Tree<Pair<Set<Integer>,Triple>> tree) {
        Op op = OpNull.create();
        if (tree instanceof Leaf<?>) {
            Triple t  = tree.getOneElement().getSecond();
            op = new OpTriple(t);
        } else {
            Branch<Pair<Set<Integer>, Triple>> b = (Branch<Pair<Set<Integer>, Triple>>) tree;
            Tree<Pair<Set<Integer>, Triple>> l = b.getLeft();
            Tree<Pair<Set<Integer>, Triple>> r = b.getRight();
            Op opL = makeOpJoinTreeS(l);
            Op opR = makeOpJoinTreeS(r);
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
