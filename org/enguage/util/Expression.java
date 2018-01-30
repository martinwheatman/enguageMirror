package org.enguage.util;

import java.util.ListIterator;
/**
 * 1. What is the sum of x and y.
 *    The sum of x and y is x + y. <<< reply definition 'cos we don't know X or Y
 * 2. What is the sum of 3 and 2.
 *    5 the sum of three and two is 5.
 * 3. What is the sum of the number of biscuits and the number of coffees.
 *    6 the sum of the number of biscuits and the number of coffees is 6.
 * 
 * @author martin
 *
 */
public class Expression {

	static private Audit audit = new Audit( "Algorithm" );
	
	static private final String UNDEFINED = "undefined";
	
	private Strings fnName = new Strings( UNDEFINED );
	private Strings params = new Strings( UNDEFINED );
	private Strings   body = new Strings( UNDEFINED );
	
	Strings representamen = new Strings();
	Strings representamen() { return representamen; }
	void    representamen( String s ) { representamen.add( s ); }
	String  function() { return fnName.toString()+"("+ params.toString()+") { return "+ body +" } "; }
	
	// -- static helpers
	static private String peek( ListIterator<String> li ) {
		String s = "";
		if (li.hasNext()) {
			s = li.next();
			li.previous();
		}
		return s;
	}
	static private void unload( ListIterator<String> li, Strings sa ) {
		audit.in( "unload", peek( li ) +", sa="+ sa.toString());
		// this assumes all things got have been added to sa
		int sz=sa.size();
		while (0 != sz--) {
			sa.remove( 0 );
			li.previous();
		}
		audit.out();
	}
	static private boolean getWord( ListIterator<String> si, String word, Strings rep ) {
		audit.in( "getWord", peek( si )+", word="+word );
		if (si.hasNext())
			if (si.next().equals( word )) {
				audit.debug( "found: + word ");
				rep.add( word );
				return audit.out( true );
			} else
				si.previous();
		return audit.out( false );
	}
	static private String getName( ListIterator<String> si, Strings rep ) {
		String s = si.hasNext() ? si.next() : null;
		if (s != null)
			rep.add( s );
		else
			unload( si, rep );
		return s;
	}
	static private Strings getWords( ListIterator<String> li, String term, Strings rep ) {
		return getWords( li, 99, term, rep );
	}
	static private Strings getWords( ListIterator<String> li, int sanity, String term, Strings rep ) {
		Strings sa = new Strings();
		String  s  = "";
		
		while (--sanity>=0
				&& li.hasNext()
				&& !(s=li.next()).equals( term ))
			sa.add( s );
		
		if (sanity < 0 || !s.equals( term )) {
			unload( li, rep );
			sa = null;
		} else {
			rep.addAll( sa );
			rep.add( term );
		}
		return sa;
	}
	// -- static helpers ABOVE
	
	private String getNumber( ListIterator<String> li, Strings rep ) {
		String n = Number.getNumber( li ).toString();
		if (n.equals( Number.NotANumber ))
			n = null;
		else
			rep.add( n );
		return n;
	}
	private Strings getFactorOp( ListIterator<String> li, Strings rep ) {
		audit.in( "getFactorOp", peek( li ) +", rep="+ rep.toString());
		if (li.hasNext()) {
			if ( getWord( li,          "*", rep ) ||
				 getWord( li,      "times", rep ) ||
				(getWord( li, "multiplied", rep ) && getWord( li, "by", rep )))
				return audit.out( new Strings( "times" ));
			if ( getWord( li,          "/", rep ) ||
				(getWord( li,    "divided", rep ) && getWord( li, "by", rep )))
				 return audit.out( new Strings( "divided by" ));
		}
		audit.out( "null" );
		return null;
	}
	private Strings getFactor( ListIterator<String> li, Strings rep ) {
		audit.in( "getFactor", peek( li ) +", rep="+ rep.toString());
		String factor;
		if (null != (factor = getNumber( li, rep )) ) {
			audit.log( "peek after number="+ peek( li ));
			return audit.out( new Strings( factor ));
		}
				// 2
		if (null != (factor = getName(   li, rep ))   ) { // x - TODO: getParam( li, param, rep )
			audit.log( "peek after name="+ peek( li ));
			return audit.out( new Strings( factor ));
		}
		audit.out( "null" );
		return null;
	}
	private Strings getTerm( ListIterator<String> li, Strings rep ) {
		audit.in(  "getTerm", peek(li) +", found="+ rep.toString());
		Strings term = getFactor(li, rep );
		if (null != term) {
			Strings factorOp = getFactorOp( li, rep );
			while (factorOp != null) {
				Strings factor = getFactor(li, rep );
				if (factor != null) {
					term.addAll( factorOp );
					term.addAll( factor );
				}
				factorOp = getFactorOp( li, rep );
			}
		} else
			unload( li, rep );
		return audit.out( term );
	}
	private Strings getParams( ListIterator<String> li, String term, Strings rep ) {
		audit.in( "getParams", peek( li ) +", term="+ term +", rep="+ rep.toString());
		// TODO actual parameters:
		// this just does a b and c; we need: the number of coffees and the number of teas.
		// Needs access to reply = enguage( utterance )
		Strings params = getWords( li, 99, term, rep );
		int sz = params.size();
		if (sz > 2 && params.get( sz - 2 ).equals("and")) params.remove( sz - 2 );
		return audit.out( params );
	}
	private boolean getTermOp( ListIterator<String> li, Strings rep ) {
		audit.in( "getTermOp", peek( li ) +", rep="+ rep.toString());
		return audit.out( li.hasNext() &&
				(getWord( li,     "+", rep ) ||
				 getWord( li,     "-", rep ) ||
				 getWord( li,  "plus", rep ) ||
				 getWord( li, "minus", rep ) ) );
	}
	private Strings getExpr( ListIterator<String> li, Strings rep ) {
		audit.in( "getExpr", "li="+ peek(li) );
		Strings expr = getTerm( li, rep );
		while (getTermOp( li, rep )) {
			expr.add( rep.get( rep.size()-1 ));
			Strings tmp = getTerm( li, rep );
			if (tmp != null)
				expr.addAll( tmp );
		}
		if (li.hasNext()) {
			unload( li, rep );
			expr = null;
		}
		return audit.out( expr );
	}
	// -- ok below!
	public boolean getAlgorithm( ListIterator<String> li ) {
		audit.in( "getAlgorithm", peek( li ));
		return audit.out(     getWord(  li, "the", representamen ) &&
			null != (fnName = getWords( li,  "of", representamen ))&&
			null != (params = getParams(li,  "is", representamen ))&&
			null != (body   = getExpr(  li,        representamen ))&&
			!li.hasNext());
	}
	public String toString() {
		return representamen.toString() +"/"+ function();
	}
	// ---- test code ----
	static private void expressionTest( String s, boolean pass ) {
		audit.in( "expressionTest", s + ", pass="+ pass );
		Expression a = new Expression();
		ListIterator<String> si = new Strings( s ).listIterator();
		if (a.getAlgorithm( si )) {
			audit.log( a.toString() );
			if (!pass) audit.FATAL( "Should've failed!" );
		} else {
			//audit.log("li="+ peek(si) +", rep="+ a.representamen().toString());
			unload( si, a.representamen );
			String err = "(";
			for (int i=0; i<3; i++) {
				if (si.hasNext()) err += si.next();
				if (si.hasNext()) err += " ";
			}
			err += (si.hasNext() ? "..." : ".") + ")";
			audit.log( "FAILED: "+ err );
			if (pass) {
				audit.out();
				audit.FATAL( "Should've passed!" );
			}
		}
		audit.out();
	}
	public static void main( String args[]) {
		Audit.traceAll( true );
		/*
		expressionTest( "", false );
		expressionTest( "a", false );
		expressionTest( "a plus", false );
		expressionTest( "a plus b", false );
		expressionTest( "the value     of x is y z", false ); // duff body
		expressionTest( "the factorial of 0 is 1", true );
		expressionTest( "the factorial of 1 is 1", true );
		expressionTest( "the value     of n is n", true );
		expressionTest( "the value     of x is y", true );
		// */
		expressionTest( "the double    of x is 2 times x", true );
		expressionTest( "the sum       of x and y is x + y", true );
		expressionTest( "the product   of x and y is x times y", true );
		// TODO: work on this next!
		expressionTest( "the factorial of n is n times the factorial of n minus 1", true );
		audit.LOG( "+++ PASSES +++" );
}	}
