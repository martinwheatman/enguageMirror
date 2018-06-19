package org.enguage.vehicle;

import java.util.ListIterator;

import org.enguage.Enguage;
import org.enguage.object.expression.Function;
import org.enguage.object.space.Overlay;
import org.enguage.util.Attribute;
import org.enguage.util.Audit;
import org.enguage.util.Strings;

public class Numerals {
	static private Audit audit = new Audit( "Numeric", false );

	static public boolean isNumeric( String s ) {
		try {
			return !Float.isNaN( Float.parseFloat( s ));
		} catch (NumberFormatException nfe) {
			return false;
	}	}
	static public Float valueOf( String s ) {
		try {
			return Float.parseFloat( s );
		} catch (NumberFormatException nfe) {
			return Float.NaN;
	}	}

	
	// -----
	
	private Numerals( Strings toks ) {representamen=toks;}
	
	private Strings representamen = new Strings();
	public  Strings representamen() { return representamen; }
	public  Numerals  append( String s ) {
		representamen.add( s );
		return this;
	}

	/* getNumber() identifies how many items in the array, from the index are numeric
	 *   [..., "68",    "guns", ...]         => 1 //  9
	 *   [..., "17", "x",  "4", "guns", ...] => 3 //  9
	 *   [..., "another", "68", "guns", ...] => 2 // +6
	 *   [..., "68",    "more", "guns", ...] => 2 // +6
	 *   [..., "some",  "guns", ...]         => 1 // <undefined few|many>
	 *   [..., "some",   "jam", ...]         => 0 -- jam is not plural!
	 *   [..., "a",      "gun", ...]         => 1 // 1
	 */
	//* ===== test code ====
	static private void evaluationTest( String term, String ans ) {
		ListIterator<String> si = new Strings( term ).listIterator();
		Number n = Number.getNumber( si );
		audit.log( "n is '"+ n.toString()
				+"' ("+ ans +"=="+ n.valueOf() +")" 
				+" sz="+ n.representamen().size() );
	}
	static private void getNumberTest( String s ) { getNumberTest( s, "" ); }
	static private void getNumberTest( String s, String expected ) {
		audit.in( "getNumberTest", s );
		Strings orig = new Strings( s );
		ListIterator<String> si = orig.listIterator();
		Number n = Number.getNumber( si );
		String val = n.valueOf(), strg = n.toString();
//		if (orig.size() != n.representamen.size())
//			audit.log( orig +"<=== is not ===>"+ n.representamen());
		audit.log(
				s +": toString=>"+ strg +"<"
				+" rep=>"+ n.representamen() +"<"
				+" valueOf=>"+ val +"<"
				+" mag="+ n.magnitude()
				+ (si.hasNext() ? ", nxt token is >>>"+ si.next() + "<<<" : "")
		);
		if (!expected.equals( "" ) && !expected.equals( n.valueOf() ))
			audit.FATAL( "getNumberTest(): "+ val +" is not ("+ expected +")");
		audit.out();
	}
	public static void main(String args []) {
		Audit.allOn();
		//audit.on();
	
		audit.log( "3.0  -> "+ Number.floatToString( 3.0f  ));
		audit.log( "3.25 -> "+ Number.floatToString( 3.25f ));
		
		audit.title( "evaluation test:");
		evaluationTest(  "this is not a number",  "5" );
		evaluationTest(  "3 plus 2",              "5" );
		evaluationTest(  "3 x    2",              "6" );
		evaluationTest(  "3 squared",             "9" );
		evaluationTest(  "3 squared plus 2",     "11" );
		evaluationTest(  "3 plus 1 all squared", "16" );
		evaluationTest(  "3 times y",             "3" );
		// -- */	
		audit.title( "get number test:" );
		Audit.incr();
		getNumberTest( "another",                  "+=1" );
		getNumberTest( "another   cup  of coffee", "+=1" );
		getNumberTest( "another 2 cups of coffee", "+=2" );
		getNumberTest( "some coffee",         Number.NotANumber );
		Audit.decr();
		// -- */	
		
		audit.title( "more/less test:");
		Audit.incr();
		Audit.allOn();
		getNumberTest( "about 6 more cups of coffee", "+~6" );
		Audit.allOff();
		getNumberTest( "6 more cups of coffee",       "+=6" );
		getNumberTest( "6 less cups of coffee",       "-=6" );
		getNumberTest( "5 more" );
		Audit.allOn();
		getNumberTest( "another",                     "+=1" );
		getNumberTest( "another coffee",              "+=1" );
		Audit.allOff();
		getNumberTest( "a few 1000 more" );
		getNumberTest( "a couple of 100" );
		getNumberTest( "a couple" );
		getNumberTest( "5 less",                      "-=5" );
		getNumberTest( "another 6",                   "+=6" );
		getNumberTest( "3 times 4   factorial",        "72" );
		getNumberTest( "3 times 4.2 factorial",         "3" );
		getNumberTest( "2 to the power of",             "2" );
		getNumberTest( "2 to the power of 5",          "32" );
		
		Enguage.e = new Enguage();
		Overlay.Set( Overlay.Get());
		if (!Overlay.autoAttach())
			audit.ERROR( "Ouch!" );
		else {
			/* factorial( n ): n times the factorial of n minus 1.
			 * Mersenne number( n ): 2^n ALL minus 1  -- not a definition???
			 * Mersenne prime(  n ): iff 2^n ALL minus 1 is prime --nad!
			 */
			Function.interpret( "create sum x y / "+ new Attribute( "body", "x + y" ));
			getNumberTest( "2 times the sum of 2 and 3",  "10" );
			//getNumberTest( "2 times the factorial of 1",   "2" );
			Function.interpret( "create factorial n / "+ new Attribute( "body", "n times the factorial of n - 1" ));
			//Audit.allOn();
			//getNumberTest( "the factorial of 4", "24" );
			//getNumberTest( "2 times the factorial of 2",  "4" );
			//getNumberTest( "2 times the factorial of 2 and 4" ); // next token 'and' ?
		}
		Audit.decr();
		audit.log( "PASSED." );
}	}
