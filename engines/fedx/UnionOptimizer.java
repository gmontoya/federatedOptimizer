/*
 * Copyright (C) 2008-2013, fluid Operations AG
 *
 * FedX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.fluidops.fedx.optimizer;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

import com.fluidops.fedx.algebra.EmptyNUnion;
import com.fluidops.fedx.algebra.EmptyResult;
import com.fluidops.fedx.algebra.NUnion;
import com.fluidops.fedx.exception.OptimizationException;
import com.fluidops.fedx.structures.QueryInfo;

import com.fluidops.fedx.algebra.StatementSourcePattern;
import java.util.Vector;
import com.fluidops.fedx.algebra.StatementSource;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;
import com.fluidops.fedx.algebra.ExclusiveStatement;
/**
 * Optimizer to flatten the UNION operations.
 * 
 * @author Andreas Schwarte
 *
 */
public class UnionOptimizer extends QueryModelVisitorBase<OptimizationException> implements FedXOptimizer{

	protected final QueryInfo queryInfo;
		
	public UnionOptimizer(QueryInfo queryInfo) {
		super();
		this.queryInfo = queryInfo;
	}

	@Override
	public void optimize(TupleExpr tupleExpr) {
		tupleExpr.visit(this);
	}
	
	
	@Override
	public void meet(Union union) {
		
		// retrieve the union arguments, also those of nested unions
		List<TupleExpr> args = new ArrayList<TupleExpr>();
		handleUnionArgs(union, args);
		
		// remove any tuple expressions that do not produce any result
		List<TupleExpr> filtered = new ArrayList<TupleExpr>(args.size());
                boolean isSimple = true; // new
                 
		for (TupleExpr arg : args) {
			if (arg instanceof EmptyResult)
				continue;
			filtered.add(arg);
                        if (!(arg instanceof StatementPattern)) { // new
                            isSimple = false;
                        }
		}
//System.out.println("is simple? "+isSimple+" filtered: "+filtered);
                if (isSimple && filtered.size() > 1) { // new 
                    if (filtered.get(0) instanceof ExclusiveStatement) {
                        ExclusiveStatement stm = (ExclusiveStatement) filtered.get(0);
                        StatementSourcePattern newSt = new StatementSourcePattern(new StatementPattern(stm.getSubjectVar(), stm.getPredicateVar(), stm.getObjectVar(), stm.getContextVar()), stm.getQueryInfo());
                        boolean e = true;
                        Vector<StatementSource> ss = new Vector<StatementSource>();
                        ss.addAll(stm.getStatementSources());
                        //System.out.println("stm: "+stm);
                        Var s1 = stm.getSubjectVar();
                        Var p1 = stm.getPredicateVar();
                        Var o1 = stm.getObjectVar();
                        for (int i = 1; i < filtered.size(); i++) {                                         
                            //System.out.println("filtered.get(i): "+filtered.get(i));
                            if (!(filtered.get(i) instanceof ExclusiveStatement)) {
                                e = false;
                                break;
                                
                            }
                            ExclusiveStatement stm2 = (ExclusiveStatement) filtered.get(i);
                            Var s2 = stm2.getSubjectVar();
                            Var p2 = stm2.getPredicateVar();
                            Var o2 = stm2.getObjectVar();
//                            System.out.println(stm.getSubjectVar()+" equals "+ stm2.getSubjectVar()+": "+stm.getSubjectVar().equals(stm2.getSubjectVar()));
//                            System.out.println(stm.getPredicateVar()+" equals "+stm2.getPredicateVar()+": "+stm.getPredicateVar().equals(stm2.getPredicateVar()));
//                            System.out.println(stm.getObjectVar()+" equals "+ stm2.getObjectVar()+": "+stm.getObjectVar().equals(stm2.getObjectVar()));
                            boolean sameSubj = s1.hasValue() && s2.hasValue() ? s1.getValue().equals(s2.getValue()) : s1.equals(s2);
                            boolean samePred = p1.hasValue() && p2.hasValue() ? p1.getValue().equals(p2.getValue()) : p1.equals(p2);
                            boolean sameObj = o1.hasValue() && o2.hasValue() ? o1.getValue().equals(o2.getValue()) : o1.equals(o2);
                            if (!sameSubj || !samePred || !sameObj) {
                                e = false;
                                break;
                            }
                            ss.addAll(stm2.getStatementSources());
                        }
                        //System.out.println("e: "+e+". newSt: "+newSt);
                        if (e) {
                            for (StatementSource s : ss) {
                                newSt.addStatementSource(s);
                            }                            
                            union.replaceWith(newSt);
                            return;
                        }
                    }
                }		
		// create a NUnion having the arguments in one layer
		// however, check if we only have zero or one argument first
		if (filtered.size()==0) {
			union.replaceWith(new EmptyNUnion(args, queryInfo));
		}
		
		else if (filtered.size()==1) {
			union.replaceWith(filtered.get(0));
		}
		
                else {
			union.replaceWith( new NUnion(filtered, queryInfo) );			
		}
	}
	
	/**
	 * Add the union arguments to the args list, includes a recursion 
	 * step for nested unions.
	 * 
	 * @param union
	 * @param args
	 */
	protected void handleUnionArgs(Union union, List<TupleExpr> args) {
		
		if (union.getLeftArg() instanceof Union) {
			handleUnionArgs((Union)union.getLeftArg(), args);
		} else {
			args.add(union.getLeftArg());
		}
		
		if (union.getRightArg() instanceof Union) {
			handleUnionArgs((Union)union.getRightArg(), args);
		} else {
			args.add(union.getRightArg());
		}
	}

}
