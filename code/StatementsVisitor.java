import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import com.fluidops.fedx.algebra.FedXStatementPattern;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.Reduced;
import org.openrdf.query.algebra.QueryModelNode;

public class StatementsVisitor extends QueryModelVisitorBase<Exception> {

	protected List<FedXStatementPattern> stmts = new ArrayList<FedXStatementPattern>();
		
	public StatementsVisitor() {
		super();
	}

	public List<FedXStatementPattern> getFedXStatements() {
		return stmts;
	}
	
	@Override
	public void meet(Union union) throws Exception {
		super.meet(union);
	}

        @Override
        public void meet(LeftJoin leftjoin) throws Exception {
                super.meet(leftjoin);
        }
	
	@Override
	public void meet(Filter filter)  throws Exception {
		super.meet(filter);
	}
	
	@Override
	public void meet(Service service) throws Exception {
                super.meet(service);
	}

        @Override
        public void meet(Reduced reduced) throws Exception {
                super.meet(reduced);
        }
	
	@Override
	public void meet(Join node) throws Exception {
	        super.meet(node);	
	}

        @Override
        public void meetOther(QueryModelNode node) throws Exception {
                if (node instanceof FedXStatementPattern) {
                        //super.meetOther(node);          // depth first
                        meetFedXStatementPattern((FedXStatementPattern) node);
                } else {
                        super.meetOther(node);
                }
        }
	
	@Override
	public void meet(StatementPattern node) {
		//stmts.add(node);
	}

        public void meetFedXStatementPattern(FedXStatementPattern node) {
                stmts.add(node);
        }
}
