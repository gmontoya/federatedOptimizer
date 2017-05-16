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
import com.fluidops.fedx.algebra.ExclusiveGroup;
import com.fluidops.fedx.algebra.ExclusiveStatement;
import com.fluidops.fedx.algebra.StatementSourcePattern;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.Reduced;
import org.openrdf.query.algebra.QueryModelNode;

public class GroupVisitor extends QueryModelVisitorBase<Exception> {

	protected List<ExclusiveGroup> groups = new ArrayList<ExclusiveGroup>();
        protected List<ExclusiveStatement> exclStmts = new ArrayList<ExclusiveStatement>();
        protected List<StatementSourcePattern> stmts = new ArrayList<StatementSourcePattern>();
	public GroupVisitor() {
		super();
	}

	public List<ExclusiveGroup> getExclusiveGroups() {
		return groups;
	}
	public List<ExclusiveStatement> getExclusiveStatements() {
                return exclStmts;
        }
        public List<StatementSourcePattern> getStatementSourcePatterns() {
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
                if (node instanceof StatementSourcePattern) {
                        //super.meetOther(node);          // depth first
                        meetStatementSourcePattern((StatementSourcePattern) node);
                } else if (node instanceof ExclusiveStatement) {
                        meetExclusiveStatement((ExclusiveStatement) node);
                } else if (node instanceof ExclusiveGroup) {
                        meetExclusiveGroup((ExclusiveGroup) node);
                } else  {
                        super.meetOther(node);
                }
        }
	
	@Override
	public void meet(StatementPattern node) {
		//stmts.add(node);
	}

        public void meetExclusiveGroup(ExclusiveGroup node) {
                groups.add(node);
        }
        public void meetExclusiveStatement(ExclusiveStatement node) {
                exclStmts.add(node);
        }
        public void meetStatementSourcePattern(StatementSourcePattern node) {
                stmts.add(node);
        }
}
