import com.hp.hpl.jena.sparql.algebra.*;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.syntax.*;
import com.hp.hpl.jena.sparql.expr.ExprList;
import java.util.*;
import com.hp.hpl.jena.sparql.expr.ExprWalker;
import com.hp.hpl.jena.sparql.expr.Expr;

class TransformDeleteServices extends TransformCopy {

    public TransformDeleteServices() {
        super();
    }

    public Op transform(OpService opService, Op subOp) {

        return subOp;
    }
}
