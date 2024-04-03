package org.enguage.util.algorithm;

import java.util.ListIterator;

import org.enguage.sign.symbol.number.Number;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class Expression {
	
	private static Audit audit = new Audit( "Expression" );

	private static String getNumber( ListIterator<String> li, Strings rep ) {
		String n = new Number( li ).toString();
		if (n.equals( Number.NOT_A_NUMBER ))
			n = null;
		else
			rep.add( n );
		return n;
	}
	private static Strings getFactor( ListIterator<String> li, Strings rep ) {
		audit.in( "getFactor", Strings.peek( li ) +", rep="+ rep.toString());
		String factor;
		if (Strings.peek( li ).equals( "the" ))
			return audit.out( getExprList( li, rep ));
		if (null != (factor = getNumber(       li, rep )) ||
			null != (factor = Strings.getName( li, rep ))   ) 
			// x - TODO: getParam( li, param, rep )
			return audit.out( new Strings( factor ));
		audit.out( "<null factor>" );
		return null;
	}
	private static Strings getFactorOp( ListIterator<String> li, Strings rep ) {
		audit.in( "getFactorOp", Strings.peek( li ) +", rep="+ rep.toString());
		if (li.hasNext()) {
			if ( Strings.getWord( li,          "*", rep ) ||
				 Strings.getWord( li,      "times", rep ) ||
				(   Strings.getWord( li, "multiplied", rep )
				 && Strings.getWord( li, "by", rep )))
				return audit.out( new Strings( "times" ));
			if ( Strings.getWord( li,          "/", rep ) ||
				(Strings.getWord( li,    "divided", rep ) && Strings.getWord( li, "by", rep )))
				 return audit.out( new Strings( "divided by" ));
		}
		audit.out( "null" );
		return null;
	}
	private static boolean getTermOp( ListIterator<String> li, Strings rep ) {
		audit.in( "getTermOp", Strings.peek( li ) +", rep="+ rep.toString());
		return audit.out( li.hasNext() &&
				(Strings.getWord( li,     "+", rep ) ||
				 Strings.getWord( li,     "-", rep ) ||
				 Strings.getWord( li,  "plus", rep ) ||
				 Strings.getWord( li, "minus", rep ) ) );
	}
	private static Strings getTerm( ListIterator<String> li, Strings rep ) {
		audit.in(  "getTerm", Strings.peek(li) +", found="+ rep.toString());
		Strings term = getFactor(li, rep ), factor, factorOp;;
		if (null != term) {
			factorOp = getFactorOp( li, rep );
			while (factorOp != null) {
				factor = getFactor(li, rep );
				if (factor != null) {
					term.addAll( factorOp );
					term.addAll( factor   );
				}
				factorOp = getFactorOp( li, rep );
			}
		} else
			Strings.unload( li, rep );
		return audit.out( term );
	}
	private static Strings getExpr( ListIterator<String> li, Strings repr ) {
		audit.in( "getExpr", "li="+ Strings.peek(li) );
		Strings expr = getTerm( li, repr );
		while (getTermOp( li, repr )) {
			expr.add( repr.get( repr.size()-1 ));
			Strings tmp = getTerm( li, repr );
			if (tmp != null)
				expr.addAll( tmp );
		}
		if (li.hasNext() && !Strings.peek( li ).equals( "and" )) {
			Strings.unload( li, repr );
			expr = null;
		}
		return audit.out( expr );
	}
	private static Strings getParamList(ListIterator<String> li, Strings repr ) {
		Expression.getExpr(  li, repr );
		while (Strings.getWord( li, "and", repr ))
			Expression.getExpr(  li, repr );
		return repr;
	}
	public  static Strings getExprList( ListIterator<String> li, Strings repr ) {
		audit.in( "getExprList", "li="+ Strings.peek( li ));
		if (           Strings.getWord( li, "the", repr )
			&& null == Strings.getWords(    li,  "of", repr ))
			audit.error( "function fail:"+ Strings.peek( li ));
		
		// This needs to be an AND list of expressions
		Expression.getParamList(  li, repr );
		
		if (li.hasNext()) {
			Strings.unload( li, repr );
			repr = null;
		}
		return audit.out( repr );
	}
	// ---- test code ----
	private static boolean expressionTest( String s ) {
		audit.in( "expressionTest", s );
		ListIterator<String> si = new Strings( s ).listIterator();
		Strings repr = new Strings();
		Strings body = getExprList( si, repr );
		Audit.log( "expr: "+ body);
		audit.out();
		return body != null;
	}
	public  static void main( String[] args) {
		Number.aImpliesNumeric( false ); // prevent a being seen as implying 1
		if (!expressionTest( "1" )               ||
			!expressionTest( "fred" )            ||
			 expressionTest( "fred bill steve" ) ||  // this should fail
			!expressionTest( "a plus b" )        ||
			!expressionTest( "a + b" )) audit.FATAL("faila");
		//audit.debugging( true );
		//audit.tracing( true );
		if (!expressionTest( "n times the factorial of n minus 1" )) audit.FATAL("failb");
		if (!expressionTest( "m plus  the product   of m and n minus 1" )) audit.FATAL("failc");
		Audit.log( "PASSED" );
}	}
