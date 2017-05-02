package com.yagadi.enguage.util;

import java.util.ListIterator;

import com.yagadi.enguage.vehicle.Language;

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
	 * a [few|couple of|] }
	 * about              } [<numeric: digits[{OP digits}]               { [more|less|]
	 * another            }          [[all|]squared|cubed][OP numeric]>]
	 * 
	 *                      **** THUS, THIS IS LANGUAGE SPECIFIC. ****
	 */
	static public final String NotANumber = "not a number";
	static public final String MORE       = "more";
	static public final String FEWER      = "less";
	
	
	// ===== LtoR Number parsing 
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
			//audit.debug( "getting   SAVED op "+ nextOp );
			op = nextOp;
			nextOp = "";
		} else if (idx >= representamen.size() ){
			audit.ERROR( "getOp(): Reading of end of val buffer");
			return "";
		} else {
			op = representamen.get( idx++ );
			if (idx < representamen.size() && op.equals( "divided" ))
				op +=(" "+representamen.get( idx++ )); // "by"?
			//audit.debug( "getting NEXT op "+ op );
		}
		return op;
	}
	//retrieves a number from the array and adjusts idx appropriately
	private Float getNumber() {
		//audit.in( "getNumber", "idx="+ idx +", array=["+representamen.toString( Strings.CSV )+"]");
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
			if (idx < representamen.size()) {
				if ( representamen.get( idx ).equals( "point" )) { number += "."; idx++; }
				while ( idx < representamen.size()) {
					String tmp = representamen.get( idx );
					if (tmp.equals( "0" ) ||
						tmp.equals( "1" ) ||
						tmp.equals( "2" ) ||
						tmp.equals( "3" ) ||
						tmp.equals( "4" ) ||
						tmp.equals( "5" ) ||
						tmp.equals( "6" ) ||
						tmp.equals( "7" ) ||
						tmp.equals( "8" ) ||
						tmp.equals( "9" )
						)
						number += tmp;
					else
						break;
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
	private Float doPower(Float value) {
		//audit.in( "doPower", op +" ["+representamen.copyAfter( idx-1 ).toString( Strings.CSV )+"]" );
		if (!Float.isNaN( value )) {
			// to process here we need an op and a value
			if (idx<representamen.size() || !nextOp.equals("")) {
				op = getOp();
				//audit.debug( "power:"+ op +":" );
				if (op.equals( "cubed" )) {
					//audit.debug( "cubing: "+ value );
					op = ""; // consumed!
					value = value * value * value;
				} else if (op.equals( "squared" )) {
					//audit.debug( "squaring: "+ value );
					op = ""; // consumed!
					value *= value;
				} else {
					//audit.debug( "saving (non-)power op: "+ op );
					nextOp = op;
		}	}	}
		//audit.out( value );
		return value;
	}
	private Float doPower() {
		//audit.in( "doPower", op +" ["+representamen.copyAfter( idx-1 ).toString( Strings.CSV )+"]" );
		// to process here we need an op and a value
		Float f = doPower( getNumber());
		//audit.out( f );
		return f; 
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
				//if (localDebugSwitch) //audit.debug( "prod op:"+ op +":" );
				if (op.equals( "times" ) || op.equals( "x" )) {
					//if (localDebugSwitch) //audit.debug( "mult: "+ value +" x "+ array.get( idx ));
					op = ""; // consumed!
					value *= doPower();
				} else if (op.equals( "divided by" ) || op.equals( "/" )) {
					//if (localDebugSwitch) //audit.debug( "divi: "+ value +" / "+ array.get( idx ));
					op = ""; // consumed!
					value /= doPower();
				//} else if (op.equals( "all" )) {
				//	op = ""; // consumed!
				//	value = doPower( value );
				} else {
					//if (localDebugSwitch) //audit.debug( "saving (non-)prod op: "+ op );
					nextOp = op;
					break;
			}	}
			if (idx >= representamen.size() && !nextOp.equals("")){
				//if (localDebugSwitch) //audit.debug( "doing prod trailing nextOp" );
				value = doPower( value );
		}	}

		//audit.out( value );
		return value;
	}
	private Float doProduct() {
		//audit.in( "doProduct", op +" ["+representamen.copyAfter( idx-1 ).toString(  Strings.CSV )+"]" );
		// to process here we need an op and a value
		Float f = doProduct( doPower() );
		//audit.out( f );
		return f;
	}
	/*
	 * term([ "1", "+", "2" ]) => 3
	 * term([ "1", "+", "2", "*", "3" ]) => 7
	 */
	private Float doTerms() {
		//audit.in( "doTerms", op +", ["+ representamen.copyAfter( idx-1 ).toString(  Strings.CSV )+"]" );
		Float value = doProduct();
		if (!Float.isNaN( value )) {
			//if (localDebugSwitch) //audit.debug( "initial term = "+ value );
			while (idx < representamen.size()) {
				op = getOp();
				//if (localDebugSwitch) //audit.debug( "term op:"+ op +":" );
				if (op.equals( "plus" ) || op.equals( "+" )) {
					op = ""; // consumed!
					//if (localDebugSwitch) //audit.debug( "Doing plus: ["+ array.copyAfter( idx-1 ).toString(  Strings.CSV )+"]" );
					value += doProduct();
				} else if (op.equals( "minus" ) || op.equals( "-" )) {
					op = ""; // consumed!
					value -= doProduct();
				} else if (op.equals( "all" )) {
					op = ""; // consumed!
					value = doProduct( value );
				} else {
					value = doProduct();
					//if (localDebugSwitch) //audit.debug( "saving (non-)term op: "+ op );
					nextOp = op;
					break;
				}
				//if (localDebugSwitch) //audit.debug( "intermediate value = "+ value );
			}
			if (!nextOp.equals("")) {
				//if (localDebugSwitch) //audit.debug( "doing term trailing nextOp" );
				value = doProduct( value );
			}
			if (idx < representamen.size()) audit.ERROR( idx +" not end of array, on processing: "+ representamen.get( idx ));
		}
		//audit.out( value );
		return value;
	}
	// ----------------
	public static String floatToString( Float f ) {
		//audit.in( "floatToString", f.toString() );
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
		//return audit.out( value );
		return value;
	}
	// ----------------

	private Strings representamen = new Strings();
	public  Strings representamen() { return representamen; }
	public  Number  append( String s ) {
		repSize++;
		representamen.add( s );
		return this;
	}
	private void appendn( ListIterator<String> si, int n ) {
		for (int j=0; j<n; j++)
			if (si.hasNext())
				append( si.next() );
	}
	private void removen( ListIterator<String> si, int n ) {
		int size = repSize;
		for (int j=0; j<n; j++) {
			token = si.previous();
			representamen.remove( --size );
	}	}
	/* NB Size relates to the numbers of words representing
	 * the number. So "another three" is 2
	 */
	private int repSize = 0;
	public  int repSize() { return repSize; /*representamen.size();*/ }
	
	public Number() {
		representamen = new Strings();
		idx = 0;
		op = nextOp = "";
	}

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
	
	boolean valued = false;
	private Float  magnitude = Float.NaN;
	public  Number magnitude( Float f ) { magnitude = f; return this; }
	public  Float  magnitude() {
		//audit.in( "magnitude" );
		if (!valued) {
			Float tmp = doTerms();
			if (!tmp.isNaN())
				magnitude = (magnitude.isNaN() ? 1 : magnitude) * tmp; 
			valued = true;
		}
		//audit.out( magnitude );
		return magnitude;
	}
	public String magnitudeToString() { return floatToString( magnitude()); }
	
	public String toString() {
		//audit.in( "toString", representamen.toString( Strings.SPACED ));
		String rc = exact() ? "" : "about ";
		rc += representamen.toString( Strings.SPACED );
		if (rc.equals("")) rc = NotANumber;
		//audit.out( rc ); // "36 all divided by 2 /more/"
		return rc;
	}
	public String valueOf() {
		//audit.in( "valueOf", "("+ representamen.toString() +")");
		String rc;
		if (representamen.size() == 0) {
			//audit.debug( "Number.valueOf(): rep size==0" );
			rc = Number.NotANumber;
		} else if (representamen.equals( new Strings( "some" )))
			rc = "about 5";
		else if (representamen.equals( new Strings( "a few" )))
			rc = "about 3";
		else if (representamen.equals( new Strings( "a couple" )))
			rc = "about 2";
		else {
			idx = 0;
			try {
				rc = floatToString( magnitude());
				if (rc.equals(Number.NotANumber))
					audit.ERROR( "Number.valueOf(): floatToString("+ magnitude() +")  returns NaN" );
				else
					rc = (relative ? (positive ? "+" : "-" ) + (exact ? "=" : "~") : "") + rc;
				
			} catch (Exception nfe) {
				audit.ERROR( "Number.valueOf():"+ nfe.toString());
				rc = Number.NotANumber;
		}	}
		//audit.out( rc );
		return rc;
	}

	// ===== getNumber(): a Number Factory
	static private void putBack( ListIterator<String> si, int n ) {
		for (int i =0; i< n; i++) {
			//String s =
					si.previous();
			//audit.debug( "Putting back "+ s );
	}	}
	static private int postOpLen( ListIterator<String> si ) {
		//audit.in( "postOpLen", "");
		//audit.debug( "looking for: [all] cubed or [all] squared" );
		int read = 0, len = 0;
		if (si.hasNext()) {
			String s = si.next();
			read++;
			//audit.debug( "just read: "+ s );
			if (s.equals( "all" ) && si.hasNext()) {
				s = si.next();
				read++;
			}
			if (s.equals(   "cubed" )
			 || s.equals( "squared" ))
				len = read;
			putBack( si, read ); 
		}
		//audit.out( len ); 
		return len;
	}
	// TODO: refactor to load into representamen here...
	static private int opLen( ListIterator<String> si ) { // too simplistic?
		//audit.in( "opLen", "" );
		//audit.debug( "looking for: times, multiplied by, times by, divided by or +/-/*//" );
		int read = 0, len = 0;
		if (si.hasNext()) {
			String op = si.next();
			read++;
			//audit.debug( "--found op:"+ op );
			if (op.equals( "all" ) && si.hasNext()) {
				//audit.debug( "found ALL:"+ op );
				op = si.next();
				read++;
			}
			if (   op.equals( "times"      )
			    || op.equals( "multiplied" )
			    || op.equals( "divided"    ))
			{
				//audit.debug( "found product op:"+ op );
				if ( op.equals( "times" ))
					len = read; // times is ok on its own

				if (si.hasNext()) {
					op = si.next();
					read++;
					if (op.equals( "by" )) {
						//audit.debug( "found BY op:"+ op );
						len = read;
				}	}

			} else if (
				op.equals( "+" ) || op.equals( "plus"  ) ||
				op.equals( "-" ) || op.equals( "minus" ) ||
				op.equals( "x" ) || //op.equals( "times" ) ||
				op.equals( "/" )                            )
			{
				//audit.debug( "found singular op:"+ op );
				len = read;
			}
			putBack( si, read );
		}
		//audit.out( len );
		return len;
	}
	static public boolean isNumeric( String s ) {
		//audit.in( "isNumeric", s );
		float i = 0;
		try {
			i = Float.parseFloat( s );
		} catch (NumberFormatException nfe) {} //fail silently!
		//audit.out( s.equals( "0" ) || 0 != i );
		return s.equals( "0" ) || 0 != i;
	}
	/*
	 * getNumber() -- factory method.
	 */
	// used in getNumber() factory method...
	static private      String token      = "";
	private void getNumeric( ListIterator<String> si ) {
		//audit.in( "getNumeric", token );
		
		// "another" -> (+=) 1
		if (representamen.size() == 0) // != "another"
			append( token );
		else                    // ?= "another"
			representamen.replace( 0, token );
		
		// ...read into the array a succession of ops and numerals 
		while (si.hasNext()) {
			//audit.debug( "in list reading terms" );
			int opLen, postOpLen;
			
			while (0 < (postOpLen = postOpLen( si ))) { // ... all squared
				//audit.debug("postOpLen="+ postOpLen );
				appendn( si, postOpLen );
			} // optional, so no break if not found
			
			if (0 == (opLen = opLen( si )))
				break;
			else { // ... x 4
				//audit.debug("appending "+ opLen +" op tokens");
				appendn( si, opLen );
				// done op so now do a numeral..
				if (si.hasNext()) {
					token = si.next();
					if (isNumeric( token )) {
						//audit.debug( "appending "+ token );
						append(  token );
					} else {
						//audit.debug( "non-numeric: removing "+ opLen +" tokens" );
						removen( si, opLen );
						break;
		}	}	}	}
		//audit.out();
	}
	/* getNumber() identifies how many items in the array, from the index are numeric
	 *   [..., "68",    "guns", ...]         => 1 //  9
	 *   [..., "17", "x",  "4", "guns", ...] => 3 //  9
	 *   [..., "another", "68", "guns", ...] => 2 // +6
	 *   [..., "68",    "more", "guns", ...] => 2 // +6
	 *   [..., "some",  "guns", ...]         => 1 // <undefined few|many>
	 *   [..., "some",   "jam", ...]         => 0 -- jam is not plural!
	 *   [..., "a",      "gun", ...]         => 1 // 1
	 * TODO: these can be hardcoded for now but need to be specified somewhere somehow!
	 * 
	 * This MUST match eval()!
	 */
	static public Number getNumber( ListIterator<String> si ) {
		Number number = new Number();
		if (si.hasNext()) {
			token = si.next();
			
			// PRE-numeric
			if (token.equals(      "a")) {
				number.relative( false ).positive( true ).magnitude( 1F );
				token = si.hasNext() ? si.next() : "";
			}
			if (token.equals(  "about")) {
				number.exact( false ).append( "about" );
				token = si.hasNext() ? si.next() : "";
			}
			if (token.equals("another")) {
				number.relative( true ).positive( true ).magnitude( 1F );
				audit.debug( "FOUND 'another': "+ number.toString() +"("+ number.valueOf() +")" );
				token = si.hasNext() ? si.next() : "";
			}
			if (token.equals(    "few")) { 
				number.exact( false ).append( "3" );
				token = si.hasNext() ? si.next() : "";
			}
			if (token.equals( "couple"))
				if (si.hasNext()) {
					token = si.next();
					if (token.equals( "of" )) {
						number.append( "couple" );
						number.exact( false ).append( "of" );
						token = si.hasNext() ? si.next() : "";
					}
				} else
					si.previous();
	
			// NUMERIC - numerals
			if (isNumeric( token )) {
				number.getNumeric( si );
				
				// POST-numeric- deal with more OR less...  following numbers
				if (si.hasNext()) {
					token = si.next();
					if (token.equals( MORE ))
						number.relative( true ).positive(  true );
					else if (token.equals( FEWER ))
						number.relative( true ).positive( false );
					else
						si.previous();
				}
			} else
				if (!token.equals("")) si.previous();
			
			//  boolean <= NotaNumberIs( number.magnitude().isNaN() )
			if (!number.magnitude().isNaN()) { // we've found something
				// post process
				if ( number.representamen.size() == 0)
					number.append( number.magnitudeToString());
				if (number.relative())
					number.append( number.positive() ? MORE : FEWER );
		}	}
		//return (Number) audit.info( "getNumber", firstToken, number );
		return number;
	}
	//* ===== test code ====
	private static void numberTest( String term, String ans ) {
		ListIterator<String> si = new Strings( term ).listIterator();
		Number n = Number.getNumber( si );
		audit.log( "n is '"+ n.toString()
				+"' ("+ ans +"=="+ n.valueOf() +")" 
				+" sz="+ n.representamen().size() );
	} // -- */
	private static void anotherTest( String s ) {
		audit.in( "anotherTest", s );
		ListIterator<String> si = new Strings( s ).listIterator();
		Number n = Number.getNumber( si );
		audit.log(
				"'"+ s +"': toString='"+ n.toString() +"'"
				+" rep='"+ n.representamen() +"'"
				+" valueOf='"+ n.valueOf() +"'"
				+" sz="+ n.representamen().size()
				+" mag="+ n.magnitude()
				+ (si.hasNext() ? ", nxt token is "+ si.next() : "")
		);
		audit.out();
	}
	public static void main( String[] args ) {
		//udit.allOn();
		//audit.tracing = true;
	
		audit.log( "3.0 -> "+ floatToString( 3.0f ));
		audit.log( "3.25 -> "+ floatToString( 3.25f ));
		
		audit.log( "arithmetic test:");
		numberTest(  "thsi is not a number",  "5" );
		numberTest(  "3 plus 2",              "5" );
		numberTest(  "3 x    2",              "6" );
		numberTest(  "3 squared",             "9" );
		numberTest(  "3 squared plus 2",     "11" );
		numberTest(  "3 plus 1 all squared", "16" );
		// -- */	
		audit.log( "another test:" );
		Audit.incr();
		anotherTest( "another" );
		anotherTest( "another   cup  of coffee" );
		anotherTest( "another 2 cups of coffee" );
		anotherTest( "some coffee" );
		Audit.decr();
		// -- */	
		
		audit.log( "more/less test:");
		Audit.incr();
		anotherTest("about 6 more cups of coffee");
		anotherTest("6 more cups of coffee");
		anotherTest("6 less cups of coffee");
		anotherTest("5 more");
		anotherTest("5 less");
		anotherTest("another 6");
		// */
		Audit.decr();
		audit.log( "PASSED?" );
		/*
		anotherTest("36 + 4 all divided by 2");
		// -- * /
		numberTest(  "a week ago last thursday",  "1" );
		numberTest(  "last thursday",  "1" );
		
		//si = new Strings( "this is rubbish" ).listIterator();
		//n = Number.getNumber( si );
		////audit.//audit( "n is "+ n.toString() +" ("+ n.valueOf() +")");
		// -- */
}	}
