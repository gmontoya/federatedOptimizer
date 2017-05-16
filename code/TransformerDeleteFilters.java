import com.hp.hpl.jena.sparql.algebra.*;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.syntax.*;
import com.hp.hpl.jena.sparql.expr.ExprList;
import java.util.*;
import com.hp.hpl.jena.sparql.expr.ExprWalker;
import com.hp.hpl.jena.sparql.expr.Expr;

class TransformerDeleteFilters extends TransformCopy {

    ExprList el;

    public TransformerDeleteFilters() {
        super();
        el = new ExprList();
    }

    public static boolean hasBound(Expr e) {

        ExprVisitorHasBound evhb = new ExprVisitorHasBound();
        ExprWalker.walk(evhb, e);
        return evhb.has();
    }

    public Op transform(OpLeftJoin opLeftJoin, Op left, Op right) {
        ExprList laux = opLeftJoin.getExprs();
        ExprList rest = new ExprList();
        if (laux != null) {
            Iterator<Expr> it = laux.iterator();
            while (it.hasNext()) {
                Expr e = it.next();
                if (hasBound(e)) {
                    rest.add(e);
                } else {
                    el.add(e);
                }
            }
        }
        return OpLeftJoin.create(left, right, rest);
    }

    public Op transform(OpFilter opFilter, Op subOp) {
        ExprList laux = opFilter.getExprs();
        ExprList rest = new ExprList();
        if (laux != null) {
            Iterator<Expr> it = laux.iterator();
            while (it.hasNext()) {
                Expr e = it.next();
                if (hasBound(e)) {
                    rest.add(e);
                } else {
                    el.add(e);
                }
            }
        }
        return OpFilter.filter(rest, subOp);
    }

    public ExprList getFilters() {
        return el;
    }
}
