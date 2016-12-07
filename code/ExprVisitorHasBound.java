import com.hp.hpl.jena.sparql.expr.ExprFunction1;
import com.hp.hpl.jena.sparql.expr.E_Bound;
import com.hp.hpl.jena.sparql.expr.ExprVisitorBase;

class ExprVisitorHasBound extends ExprVisitorBase {

    boolean has;

    public ExprVisitorHasBound() {
        has = false;
    }

    public void visit(ExprFunction1 func) {
        if (func instanceof E_Bound) {
            has = true;
        }
    }

    public boolean has() {
        return has;
    }
}
