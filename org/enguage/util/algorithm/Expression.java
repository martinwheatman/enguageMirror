package org.enguage.util.algorithm;

import java.util.ListIterator;

import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.vehicle.Number;
import org.enguage.vehicle.Numerals;

public class Expression {
	
	static private Audit audit = new Audit( "Expression" );

	static private String getNumber( ListIterator<String> li, Strings rep ) {
		String n = Number.getNumber( li ).toString();
		if (n.equals( Number.NotANumber ))
			n = null;
		else
			rep.add( n );
		return n;
	}
	static private Strings getFactor( ListIterator<String> li, Strings rep ) {
		audit.in( "getFactor", Strings.peek( li ) +", rep="+ rep.toString());
		String factor;
		if (Strings.peek( li ).equals( "the" ))
			return audit.out( getFunction( li, rep ));
		if (null != (factor = getNumber(       li, rep )) ||
			null != (factor = Strings.getName( li, rep ))   )  // x - TODO: getParam( li, param, rep )
			return audit.out( new Strings( factor ));
		audit.out( "<null factor>" );
		return null;
	}
	static private Strings getFactorOp( ListIterator<String> li, Strings rep ) {
		audit.in( "getFactorOp", Strings.peek( li ) +", rep="+ rep.toString());
		if (li.hasNext()) {
			if ( Strings.getWord( li,          "*", rep ) ||
				 Strings.getWord( li,      "times", rep ) ||
				(Strings.getWord( li, "multiplied", rep ) && Strings.getWord( li, "by", rep )))
				return audit.out( new Strings( "times" ));
			if ( Strings.getWord( li,          "/", rep ) ||
				(Strings.getWord( li,    "divided", rep ) && Strings.getWord( li, "by", rep )))
				 return audit.out( new Strings( "divided by" ));
		}
		audit.out( "null" );
		return null;
	}
	static private boolean getTermOp( ListIterator<String> li, Strings rep ) {
		audit.in( "getTermOp", Strings.peek( li ) +", rep="+ rep.toString());
		return audit.out( li.hasNext() &&
				(Strings.getWord( li,     "+", rep ) ||
				 Strings.getWord( li,     "-", rep ) ||
				 Strings.getWord( li,  "plus", rep ) ||
				 Strings.getWord( li, "minus", rep ) ) );
	}
	static private Strings getTerm( ListIterator<String> li, Strings rep ) {
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
	static public Strings getExpr( ListIterator<String> li, Strings repr ) {
		audit.in( "getExpr", "li="+ Strings.peek(li) );
		Strings expr = getTerm( li, repr );
		while (getTermOp( li, repr )) {
			expr.add( repr.get( repr.size()-1 ));
			Strings tmp = getTerm( li, repr );
			if (tmp != null)
				expr.addAll( tmp );
		}
		if (li.hasNext()) {
			Strings.unload( li, repr );
			expr = null;
		}
		return audit.out( expr );
	}
	static public Strings getFunction( ListIterator<String> li, Strings repr ) {
		audit.in( "getExpr", "li="+ Strings.peek( li ));
		if (           Strings.getWord( li, "the", repr )
			&& null == Strings.getWords(    li,  "of", repr ))
			audit.ERROR( "function fail:"+ Strings.peek( li ));
		Expression.getExpr(  li, repr );
		if (li.hasNext()) {
			Strings.unload( li, repr );
			repr = null;
		}
		return audit.out( repr );
	}
	// ---- test code ----
	static private void expressionTest( String s ) {
		audit.in( "expressionTest", s );
		ListIterator<String> si = new Strings( s ).listIterator();
		Strings repr = new Strings(),
		        body = getFunction( si, repr );
		audit.log( "expr: "+ body);
		audit.out();
	}
	static public void main( String[] args) {
		Numerals.aImpliesNumeric( false ); // prevent a being seen as implying 1
		expressionTest( "1" );
		expressionTest( "fred" );
		expressionTest( "fred bill steve" );
		expressionTest( "a plus b" );
		expressionTest( "a + b" );
		expressionTest( "n times the factorial of n minus 1" );
		audit.log( "PASSED" );
}	}
