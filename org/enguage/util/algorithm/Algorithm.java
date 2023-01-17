package org.enguage.util.algorithm;

import java.util.ArrayList;
import java.util.ListIterator;

import org.enguage.util.Audit;
import org.enguage.util.Strings;
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
public class Algorithm {

	private static Audit audit = new Audit( "Algorithm" );
	
	private static final String UNDEFINED = "undefined";
	
	private Strings fnName = new Strings( UNDEFINED );
	private Strings params = new Strings( UNDEFINED );
	private Strings   body = new Strings( UNDEFINED );
	
	Strings representamen = new Strings();
	Strings representamen() { return representamen; }
	void    representamen( String s ) { representamen.add( s ); }
	String  function() { return fnName.toString()+"("+ params.toString()+") { return "+ body +" } "; }
	
	static ArrayList<Algorithm> functions = new ArrayList<Algorithm>();
	static private void    save(  Algorithm a ) { functions.add( a );}
	
	// -- ok below!
	static  private int algFail = 0;
	private boolean getAlgorithm( ListIterator<String> li ) {
		audit.in( "getAlgorithm", Strings.peek( li ));
		return audit.out(     Strings.getWord(     li, "the", representamen ) && 0 != (algFail = 1) &&
			null != (fnName = Strings.getWords(    li,  "of", representamen ))&& 0 != (algFail = 2) &&
			null != (params = Parameters.getFormal(li,  "is", representamen ))&& 0 != (algFail = 3) &&
			null != (body   = Expression.getExpr(  li,        representamen ))&& 0 != (algFail = 4) &&
			!li.hasNext());
	}
	// ---- test code ----
	static private void algorithmTest( String s, boolean pass ) {
		audit.in( "algorithmTest", s + ", pass="+ pass );
		Algorithm a = new Algorithm();
		ListIterator<String> si = new Strings( s ).listIterator();
		if (a.getAlgorithm( si )) {
			Algorithm.save( a );
			Audit.log( a.toString() );
			if (!pass) {
				audit.out();
				audit.FATAL( "Should've failed!" );
			}
		} else {
			//Audit.log("li="+ peek(si) +", rep="+ a.representamen().toString());
			Strings.unload( si, a.representamen );
			String err = "(";
			for (int i=0; i<3; i++) {
				if (si.hasNext()) err += si.next();
				if (si.hasNext()) err += " ";
			}
			err += (si.hasNext() ? "..." : ".") + ")";
			Audit.log( "FAILED: "+ err );
			if (pass) {
				audit.out();
				audit.FATAL( "Should've passed!" + algFail );
		}	}
		audit.out();
	}
	public static void main( String args[]) {
		algorithmTest( "", false );
		algorithmTest( "a", false );
		algorithmTest( "a plus", false );
		algorithmTest( "a plus b", false );
		algorithmTest( "the value     of x is y z", false ); // duff body
		algorithmTest( "the factorial of 0 is 1", true );
		algorithmTest( "the factorial of 1 is 1", true );
		algorithmTest( "the value     of n is n", true );
		algorithmTest( "the value     of x is y", true );
		algorithmTest( "the double    of x is 2 times x", true );
		algorithmTest( "the sum       of x and y is x + y", true );
		algorithmTest( "the product   of x and y is x times y", true );
		// See Expression test code...
		// algorithmTest( "the factorial of n is n times the factorial of n minus 1", true );
		Audit.LOG( "+++ PASSES +++" );
}	}
