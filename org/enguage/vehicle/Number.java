package org.enguage.vehicle;

import java.util.ListIterator;

import org.enguage.Enguage;
import org.enguage.object.expression.Function;
import org.enguage.object.space.Overlay;
import org.enguage.util.Attribute;
import org.enguage.util.Audit;
import org.enguage.util.Strings;

public class Number {
	/*
	 * This class is too long, and needs reimplementing!
	 * Look at When.java (to/from value/string) for a better spec!
	 * 
	 */
			
	static private Audit audit = new Audit( "Number", false );
	/* a number is obtained from a string of characters, and can be:
	 * relative/absolute, exact/vague, positive/negative, e.g
	 * i need  /another 3/  cups of coffee
	 * i need /another few/ cups of coffee
	 * i need     /some/    cups of coffee
	 * i need       /a/     cup  of coffee
	 * i need   /1 plus 2/  cups of coffee
	 */
	/* NEED TO DEFINE (non-)Numerics, e.g.:
	 * <xml>
	 * 	<nonnumeric quantity='1'  relative='no'  exact='no'  multiplier='no'>some</nonnumeric>
	 * 	<nonnumeric quantity='2'  relative='no'  exact='yes' multiplier='no'>a pair</nonnumeric>
	 * 	<nonnumeric quantity='~2' relative='no'  exact='no'  multiplier='no'>a couple</nonnumeric>
	 * 	<nonnumeric quantity='~3' relative='no'  exact='no'  multiplier='no'>a few</nonnumeric>
	 * 	<nonnumeric quantity='1 more' relative='yes' exact='yes' multiplier='yes'>another</nonnumeric>
	 * 	<nonnumeric quantity='1'  relative='no'  exact='yes' multiplier='yes'>one</nonnumeric>
	 * </xml>
	 */
	/* 
	 * Much as this project is about CONNOTATIONALISM, numbers (and their descriptions)
	 * have a denotational dimension. Something like:
	 * a [few|couple of|] |
	 * about              } [<numeric: digits[{OP digits}]               { [more|less|]
	 * another            }          [[all|]squared|cubed][OP numeric]>]
	 * 
	 *                      **** THUS, THIS IS LANGUAGE SPECIFIC. ****
	 */
	static public  final String NotANumber = "not a number";
	static public  final String       MORE = "more";
	static public  final String      FEWER = "less";
	static private final Strings      SOME = new Strings( "some" );
	static private final Strings  A_COUPLE = new Strings( "a couple" );
	static private final Strings     A_FEW = new Strings( "a few" );

	
	
	// ==============================
	//  representamen Number parsing 
	// ==============================
	
	/* {}=repeating 0..n, []=optional
	 * 
	 * numeral == digit{digit}[.digit{digit}]
	 *  postOp == ["all"] "squared" | "cubed"
	 *      op == "plus" | "minus" | "times" | "divided by" |
	 *            "multiplied by" | "times by" | "+" | "-" | "*" | "/"
	 *    expr == numeral {[postOp] [op expr]}
	 *      
	 * E.g. expr = 1 plus 2 squared plus 3 squared plus 4 all squared. 
	 */
	private int idx;
	private String op;
	private String nextOp;

	//retrieves an op from the array and adjusts idx appropriately
	private String getOp() {
		if (!nextOp.equals( "" )) {
			op = nextOp;
			nextOp = "";
		} else if (idx >= representamen.size() ){
			audit.ERROR( "getOp(): Reading of end of val buffer");
			return "";
		} else {
			op = representamen.get( idx++ );
			if (idx < representamen.size() && op.equals( "divided" ))
				op +=(" "+representamen.get( idx++ )); // "by" ..."into"?
			
			else if (idx < representamen.size() && op.equals( "to" )) {
				op +=(" "+representamen.get( idx++ )); // the
				if (idx < representamen.size() && op.equals( "to the" )) {
					op +=(" "+representamen.get( idx++ )); // power
					if (idx < representamen.size() && op.equals( "to the power" )) {
						op +=(" "+representamen.get( idx++ )); // of
			}	}	}				
		}
		return op;
	}
	//retrieves a number from the array and adjusts idx appropriately
	private Float getNumber() {
		//audit.in( "getNumber", "idx="+ idx +", array=["+representamen.toString( Strings.CSV )+"]");
		/*
		 * this retrieves a SPOKEN number as generated by Android e.g. [ "3", "point", "1", "4", "2" ]
		 */
		String sign="+",
		       number = "";
		if (representamen.size() > 0) {
			String got = representamen.get( idx );
			if (got.equals( "plus" )) {
				sign = "+";
				idx++;
				
			} else if (got.equals( "minus" )) {
				sign = "-";
				idx++;
				
			} else if (got.equals( "+" ) || got.equals( "-" )) {
				sign = got;
				idx++;
			}
			number = representamen.get( idx++ );
			if (number.contains(".")) integer = false;
			if (idx < representamen.size()) {
				if ( representamen.get( idx ).equals( "point" )) {
					number += ".";
					idx++;
					integer = false;
				}
				while ( idx < representamen.size()) {
					String tmp = representamen.get( idx );
					if (tmp.length() != 1)
						break;
					else {
						int digit = tmp.charAt(0);
						if (digit >= '0' && digit <= '9')
							number += tmp;
						else
							break;
					}
					idx++;
		}	}	}
		Float rc = Float.NaN;
		//audit.debug( "parsing:"+ sign +"/"+number +":" );
		try { rc =  Float.parseFloat( sign+number ); } catch (Exception e) {}
		//audit.out( rc );
		return rc;
	}
	/* doPower( 3.0, [ "+", "2" ...]) => "3"
	 * doPower( 3.0, [ "squared", "*", "2" ...]) => "9"
	 */
	private int factorial( int n ) {
		return n == 0 ? 1 : n * factorial( n - 1 );
	}
	private Float doPower(Float value) {
		//audit.in( "doPower", op +" ["+representamen.copyAfter( idx-1 ).toString( Strings.CSV )+"]" );
		if (!Float.isNaN( value )) {
			if (idx<representamen.size() || !nextOp.equals("")) {
				op = getOp();
				if (op.equals( "cubed" )) {
					op = ""; // consumed!
					value = value * value * value;
				} else if (op.equals( "squared" )) {
					op = ""; // consumed!
					value *= value;
				} else if (op.equals( "factorial" )) {
					op = ""; // consumed!
					value = (Float.isNaN( value ) || !integer) ? Float.NaN
							 : (float)factorial( Math.round( value ));  // simple factorial?
				} else if (op.equals( "to the power of" )) {
					op = ""; // consumed!
					try {
						value = (float) Math.pow( (double)value, (double)doProduct( doPower( getNumber() )));
					} catch (Exception e) {
						value = Float.NaN;
					}
				} else
					nextOp = op;
		}	}
		//audit.out( value );
		return value;
	}
	/*
	 * product: restarts the product() process
	 * product( 3.0, [ "+", "2" ...]) => "3"
	 * product( 3.0, [ "*", "2", "+" ...]) => "6"
	 */
	/*
	 * Theres a bug here in that op and postOp should be dealt with in their own methods.
	 */
	private Float doProduct(Float value) {
		//audit.in( "doProduct", op +" ["+ representamen.copyAfter( idx-1 ).toString( Strings.CSV )+"]" );
		if (!Float.isNaN( value )) {
			// to process here we need an op and a value
			while( idx < representamen.size() ) { // must be at least two array items, e.g. ["x", "2", ...
				op = getOp();
				if (op.equals( "times" ) || op.equals( "x" )) {
					op = ""; // consumed!
					value *= doPower( getNumber());
				} else if (op.equals( "divided by" ) || op.equals( "/" )) {
					op = ""; // consumed!
					value /= doPower( getNumber());
				//} else if (op.equals( "all" )) {
				//	op = ""; // consumed!
				//	value = doPower( value );
				} else {
					nextOp = op;
					break;
			}	}
			if (idx >= representamen.size() && !nextOp.equals("")){
				value = doPower( value );
		}	}
		//audit.out( value );
		return value;
	}
	/*
	 * term([ "1", "+", "2" ]) => 3
	 * term([ "1", "+", "2", "*", "3" ]) => 7
	 */
	private Float doTerms() {
		//audit.in( "doTerms", op +", ["+ representamen.copyAfter( idx-1 ).toString(  Strings.CSV )+"]" );
		Float value = doProduct( doPower( getNumber() ));
		if (!Float.isNaN( value )) {
			while (idx < representamen.size()) {
				op = getOp();
				if (op.equals( "plus" ) || op.equals( "+" )) {
					op = ""; // consumed!
					value += doProduct( doPower( getNumber() ));
				} else if (op.equals( "minus" ) || op.equals( "-" )) {
					op = ""; // consumed!
					value -= doProduct( doPower( getNumber() ));
				} else if (op.equals( "all" )) {
					op = ""; // consumed!
					value = doProduct( value );
				} else {
					value = doProduct( doPower( getNumber() ));
					nextOp = op;
					break;
			}	}
			if (!nextOp.equals(""))
				value = doProduct( value );
			if (idx < representamen.size()) audit.ERROR( idx +" not end of array, on processing: "+ representamen.get( idx ));
		}
		//audit.out( value );
		return value;
	}
	// ======================================
	// ...above: representamen Number parsing 
	// ======================================
	
	public static String floatToString( Float f ) {
		audit.in( "floatToString", f.toString() );
		String value;
		if (Float.isNaN( f ))
			value = Number.NotANumber;
		else {
			// 3.0 => "3" -- remove ".0"
			// 3.25 => "3 point 2 5" -- replace "." with "point", .nn should be spelled out
			int i;
			value = Float.toString( f );
			if (value.substring( value.length()-2 ).equals( ".0" ))
				value = value.substring( 0, value.length()-2 );
			if (-1 != (i = value.indexOf( "." )))
				value = value.substring( 0, i ) + " point " + Language.spell( value.substring( i+1 )); 
		}
		return audit.out( value );
		//return value;
	}

	private Strings representamen = new Strings();
	public  Strings representamen() { return representamen; }
	public  Number  append( String s ) {
		representamen.add( s );
		return this;
	}
	private  Number  append( Strings sa ) {
		representamen.addAll( sa );
		return this;
	}
	private void append( ListIterator<String> si, int n ) {
		for (int j=0; j<n; j++)
			if (si.hasNext())
				append( si.next() );
	}
	private void remove( ListIterator<String> si, int n ) {
		int sz = representamen.size();
		int req = sz - n;
		while (req<sz) {
			si.previous();
			representamen.remove( --sz );
	}	}
	/* NB Size relates to the numbers of words representing
	 * the number. So "another three" is 2
	 */
	//private int repSize = 0;
	//public  int repSize() { return repSize; }
	
	public Number() {
		representamen = new Strings();
		idx = 0;
		op = nextOp = "";
	}

	// properties of a number...
	private boolean relative = false;
	public  boolean relative() { return relative; }
	public  Number  relative( boolean b ) { relative = b; return this; }
	
	private boolean positive = true;
	public  boolean positive() { return positive; }
	public  Number  positive( boolean b ) { positive = b; return this; }
	
	private boolean exact = true;
	public  boolean exact() { return exact; }
	public  Number  exact( boolean b ) { exact = b; return this; }
	
	private boolean integer = true;
	public  boolean integer() { return integer; }
	public  Number  integer( boolean b ) { integer = b; return this; }
	
	private boolean valued = false;
	private Float  magnitude = Float.NaN;
	public  Number magnitude( Float f ) { magnitude = f; return this; }
	public  Float  magnitude() {
		audit.in( "magnitude" );
		if (!valued) {
			Float tmp = doTerms();
			if (!tmp.isNaN())
				magnitude = (magnitude.isNaN() ? 1 : magnitude) * tmp; 
			valued = true;
		}
		audit.out( magnitude );
		return magnitude;
	}
	public String magnitudeToString() { return floatToString( magnitude()); }
	
	public String toString() {
		// e.g. "36 all divided by 2 /more/"
		String rc = exact() ? "" : "about ";
		rc += representamen.toString( Strings.SPACED );
		if (rc.equals("")) rc = NotANumber;
		return rc;
	}
	public String valueOf() {
		audit.in( "valueOf" );
		String rc;
		if (representamen.size() == 0)
			rc = Number.NotANumber;
		else if (representamen.equals( SOME ))
			rc = "about 5";
		else if (representamen.equals( A_FEW ))
			rc = "about 3";
		else if (representamen.equals( A_COUPLE ))
			rc = "about 2";
		else {
			idx = 0; // initialise dx for f2Str()
			rc = floatToString( magnitude() );
			if (!rc.equals(Number.NotANumber))
				rc = (relative ? (positive ? "+" : "-" ) + (exact ? "=" : "~") : "") + rc;
		}	
		return audit.out( rc );
	}

	private int peekwals( Strings sa, ListIterator<String> si ) {
		boolean rc = true;
		ListIterator<String> sai = sa.listIterator();
		int i = si.nextIndex();
		while (rc && sai.hasNext() && si.hasNext()) {
			if (!sai.next().equals( si.next()))
				rc = false;
		}
		// if not put si back!
		while (si.nextIndex() > i) si.previous();
		return rc && !sai.hasNext() ? sa.size() : 0; // we haven't failed AND got to end of strings
	}
	// ===== getNumber(): a Number representamen Factory
	static private final Strings       ALL = new Strings(       "all" );
	static private final Strings     CUBED = new Strings(     "cubed" );
	static private final Strings   SQUARED = new Strings(   "squared" );
	static private final Strings  POWER_OF = new Strings( "to the power of" );
	static private final Strings FACTORIAL = new Strings( "factorial" );
	
	private void doPostOp( ListIterator<String> si ) {
		int oplen, x=0;
		String power = "";
		do{
			oplen = 0;
			if (si.hasNext()) {
				int    n = 0;
				if (   0 != (x = peekwals( ALL, si )))
				{
					for (int j=0; j<x; j++) si.next();
					n+=x; // can only be 1
				}
				if (   0 != (x = peekwals(     CUBED, si ))
				    || 0 != (x = peekwals(   SQUARED, si ))
					|| 0 != (x = peekwals( FACTORIAL, si ))) // we can say "all factorial"	
				{
					Strings.next( si, x );
					oplen = (n+=x); // success!
					
				} else if ( 0 != (x = peekwals(  POWER_OF, si ))) { // we can say "all to the ..."
					Strings.next( si, x ); // skip past "to the power of"
					//audit.log( "peek:"+ Strings.peek( si )); // ==5
					n+=x;  // add len "to the..."
					
					//audit.log( "do numeral: "+ (doNumeral( si )?"ok":"fail"));
					if (si.hasNext()) {
						if (!isNumeric( power = si.next() )) {
							si.previous();
							power = ""; // reset tmp!
						} else {
							//++x; // numeral is just len 1
							oplen = ++n; // success!
					}	}
				}
				Strings.previous( si, n );      // now, put them all back!
				if (n > 0) append( si, oplen ); // might go back 2 and tx none, or 2.
				if (!power.equals( "" )) append( si, 1 ); // in case there is a power to add
			}
		} while (++x<10 && oplen > 0);
	}
	private int doOp( ListIterator<String> si) {
		int len = 0;
		if (si.hasNext()) {
			String op = si.next();
			int n = 1;
			if (op.equals( "all" ) && si.hasNext()) {
				op = si.next();
				n++;
			}
			if (   op.equals( "times"      )
			    || op.equals( "multiplied" )
			    || op.equals( "divided"    ))
			{
				if ( op.equals( "times" ))
					len = n; // times is ok on its own

				if (si.hasNext()) {
					op = si.next();
					n++;
					if (op.equals( "by" )) {
						len = n;
				}	}

			} else if (
				op.equals( "+" ) || op.equals( "plus"  ) ||
				op.equals( "-" ) || op.equals( "minus" ) ||
				op.equals( "x" ) || //op.equals( "over" ) ||
				op.equals( "/" )                            )
			{
				len = n;
			}
			// n >= len
			Strings.previous( si, n );
			if (n > 0) append( si, len );
		}
		return len;
	}
	static public boolean isNumeric( String s ) {
		try {
			return !Float.isNaN( Float.parseFloat( s ));
		} catch (NumberFormatException nfe) {
			return false;
	}	}
	private Strings getParams( ListIterator<String> si ) {
		audit.in( "getParams", Strings.peek( si ));
		Strings params = null;
		if (si.hasNext()) {
			String token = si.next(); // <<<<<<<<<<<<< get expression ( and evaluate it! )
			if (isNumeric( token )) { 
				params = new Strings( token ); // <<<< need to be appending (4-1), i.e. 3, not 4!!!
				while (si.hasNext() && isNumeric( token = si.next())) 
					params.add( token );
				
				if (token.equals( "and" ) && si.hasNext() && isNumeric( token = si.next()))
					params.append( "and" ).append( token );
				else {
					if (token.equals( "and" )) si.previous(); // replace "and"
					for (int sz = params.size(); sz > 1; sz--) {
						si.previous();
						params.remove( sz - 1 );
		}	}	}	}
		return audit.out( params );
	}
	private String numEval( String fn, Strings params ) {
		String token = Function.interpret( "evaluate "+ fn +" "+ params.toString() );
		return isNumeric( token ) ? token : null;
	}
	private boolean doFunction( ListIterator<String> si ) {
		audit.in( "doFunction", Strings.peek( si ));
		boolean rc = false;
		if (si.hasNext()) {
			String token = si.next();
			if (token.equals( "the" ) && si.hasNext()) {
				// could this be: the <FUNCTION/> of <PARAMS/> ?
				audit.debug( "ok, looking for function" );
				String fn = si.next();
				if (si.hasNext()) {
					Strings params = null;
					if (si.next().equals( "of" ) && 
						null != (params = getParams( si )) &&
						null != (token  = numEval( fn, params ))) // 5 = sum, [ 2,  3 ]
					{	
						rc = true;
								append( "the" ).append(fn).append("of").append( params );
					} else {
						//remove/replace all params
						if (params !=null) for (int sz = params.size(); sz > 0; sz--) {
							si.previous();
							params.remove( sz - 1 );
						}
						Strings.previous( si, 3 ); // "the fname of ... and ...".
					}
				} else
					Strings.previous( si, 2 ); // "the fname." 
			} else
				si.previous(); // "the." 
		}
		return audit.out( rc );
	}
	private boolean doNumeral( ListIterator<String> si ) {
		audit.in( "doNumeral", Strings.peek( si ));
		boolean rc = false;
		if (si.hasNext()) {
			String token;
			if (rc = isNumeric( token = si.next() ))
				append( token );
			else
				si.previous(); // not numeric and not "the" and not "the."
		}
		audit.out( rc );
		return rc;
	}
	// used in getNumber() factory method...
	private void doExpr( ListIterator<String> si ) {
		audit.in( "doExpr", Strings.peek( si ));
		// ...read into the array a succession of ops and numerals 
		int done;
		do {
			// optional, so ignore any return code
			doPostOp( si ); // [all] squared|cubed|to the power of
			if (   0 < (done = doOp( si )) // ... times
				&& !doNumeral( si ))
			{ // if done op but not numeral remove op..
				remove( si, done );
				done = 0;
			}
		} while (si.hasNext() && done > 0);
		audit.out();
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
	static private boolean aImpliesNumeric = true;
	static public  void    aImpliesNumeric( boolean implies ) {
		aImpliesNumeric = implies;
	}
	private boolean doA( ListIterator<String> si ) {
		if (si.hasNext() && aImpliesNumeric) {
			if (si.next().equals( "a" )) {
				relative( false ).positive( true ).magnitude( 1F );
				
				if (si.hasNext()) {
					String tmp = si.next();
					if (tmp.equals( "few"))
						append( "few" ).exact( false ).magnitude( 3.0f );
						
					else if (tmp.equals( "couple")) {
						append( "couple" ).exact( false ).magnitude( 2.0f );
						if (si.hasNext())
							if (si.next().equals( "of" ))
								append( "of" );
							else
								si.previous(); // put !'of' back
						
					} else // something
						si.previous();
					return true; // a few, a couple, a something
					
				} else
					si.previous(); // put 'a' back
				// 'a' on its own -- fall through to false
			} else
				si.previous(); // put 'a' back
		}
		return false;
	}
	private boolean doAbout( ListIterator<String> si ) {
		if (si.hasNext())
			if (si.next().equals( "about")) {
				exact( false ).append( "about" );
				return true;
			} else
				si.previous();
		return false;
	}
	private boolean doAnother( ListIterator<String> si ) {
		audit.in( "doAnother", Strings.peek( si ));
		if (si.hasNext())
			if (si.next().equals( "another" )) {
				relative( true ).positive( true ).magnitude( 1F );
				//append( "another" );
				return true;
			} else
				si.previous();
		return audit.out( false );
	}
	private void doMoreOrLess() {
		//  boolean <= NotaNumberIs( number.magnitude().isNaN() )
		if (!magnitude().isNaN()) { // we've found something
			// post process
			if (representamen.size() == 0)
				append( magnitudeToString());
			if (relative())
				append( positive() ? MORE : FEWER );
		}
	}
	private void doPostExpr( ListIterator<String> si ) {
		// POST-numeric- deal with 'more' OR 'less'...  following numbers
		if (si.hasNext()) {
			String token = si.next();
			if (token.equals( MORE ))
				relative( true ).positive(  true );
			else if (token.equals( FEWER ))
				relative( true ).positive( false );
			else
				si.previous();
		}
	}
	private boolean doNum( ListIterator<String> si ) {
		boolean rc = false;
		if (si.hasNext()) {
			String token = si.next();
			if (isNumeric( token )) {
				rc = true;
				// "another" -> (+=) 1
				if (representamen.size() == 0) // != "another"
					append( token );
				else                    // ?= "another"
					representamen.replace( 0, token );
			} else
				si.previous();
		}
		return rc;
	}
	private Number doNumerical( ListIterator<String> si ) {
		audit.in( "doNumerical", Strings.peek( si ));
		if (doNum( si )) {
			doExpr( si );
			doPostExpr( si );
		}		
		return (Number) audit.out( this );
	}
	static public Number getNumber( ListIterator<String> si ) {
		audit.in( "getNumber", Strings.peek( si ));
		Number number = new Number();
		
		if (si.hasNext()) {
			if (number.doA(       si ) ||
				number.doAbout(   si ) ||
				number.doAnother( si )   );
			
			number.doNumerical( si ).doMoreOrLess();
		}
		return (Number) audit.out( number );
	}
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
		ListIterator<String> si = new Strings( s ).listIterator();
		Number n = Number.getNumber( si );
		audit.log(
				s +": toString=>"+ n.toString() +"<"
				+" rep=>"+ n.representamen() +"<"
				+" valueOf=>"+ n.valueOf() +"<"
				+" sz="+ n.representamen().size()
				+" mag="+ n.magnitude()
				+ (si.hasNext() ? ", nxt token is >>>"+ si.next() + "<<<" : "")
		);
		if (!expected.equals( "" ) && !expected.equals( n.valueOf() ))
			audit.FATAL( "value, "+ n.valueOf() +", does not equal expected ("+ expected +")");
		audit.out();
	}
	public static void main( String[] args ) {
		//Audit.allOn();
		//audit.on();
	
		audit.log( "3.0  -> "+ floatToString( 3.0f  ));
		audit.log( "3.25 -> "+ floatToString( 3.25f ));
		
		audit.log( "evaluation test:");
		evaluationTest(  "this is not a number",  "5" );
		evaluationTest(  "3 plus 2",              "5" );
		evaluationTest(  "3 x    2",              "6" );
		evaluationTest(  "3 squared",             "9" );
		evaluationTest(  "3 squared plus 2",     "11" );
		evaluationTest(  "3 plus 1 all squared", "16" );
		evaluationTest(  "3 times y",             "3" );
		// -- */	
		audit.log( "get number test:" );
		Audit.incr();
		getNumberTest( "another",                  "+=1" );
		getNumberTest( "another   cup  of coffee", "+=1" );
		getNumberTest( "another 2 cups of coffee", "+=2" );
		getNumberTest( "some coffee",         NotANumber );
		Audit.decr();
		// -- */	
		
		audit.log( "more/less test:");
		Audit.incr();
		getNumberTest( "about 6 more cups of coffee", "+~6" );
		getNumberTest( "6 more cups of coffee",       "+=6" );
		getNumberTest( "6 less cups of coffee",       "-=6" );
		getNumberTest( "5 more" );
		getNumberTest( "a few 1000 more" );
		getNumberTest( "a couple of 100" );
		getNumberTest( "a couple" );
		getNumberTest( "5 less",                      "-=5" );
		getNumberTest( "another 6",                   "+=6" );
		getNumberTest( "3 times 4   factorial",        "72" );
		getNumberTest( "3 times 4.2 factorial",  NotANumber );
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
			//Function.interpret( "create sum a b / "+ new Attribute( "body", "a + b" ));
			getNumberTest( "2 times the sum of 2 and 3",  "10" );
			getNumberTest( "2 times the factorial of 1",   "2" );
			Function.interpret( "create factorial n / "+ new Attribute( "body", "n times the factorial of n - 1" ));
			//Audit.allOn();
			//getNumberTest( "the factorial of 4", "24" );
			getNumberTest( "2 times the factorial of 2",  "4" );
			getNumberTest( "2 times the factorial of 2 and 4" ); // next token 'and' ?
		}
		Audit.decr();
		audit.log( "PASSED." );
}	}
