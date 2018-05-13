package org.enguage.util;

import java.util.ListIterator;

public class Numeric {
	static private Audit audit = new Audit( "Numeric", false );

	private Strings representamen = new Strings();
	public  Strings representamen() { return representamen; }
	public  Numeric  append( String s ) {
		repSize++;
		representamen.add( s );
		return this;
	}
	private void appendn( ListIterator<String> si, int n ) {
		for (int j=0; j<n; j++)
			if (si.hasNext())
				append( si.next() );
	}
	private String removen( ListIterator<String> si, int n ) {
		int size = repSize;
		for (int j=0; j<n; j++) {
			token = si.previous();
			representamen.remove( --size );
		}
		return token;
	}
	private Float valueOf() {
		return 0.0f;
	}
	/* NB Size relates to the numbers of words representing
	 * the number. So "another three" is 2
	 */
	private int repSize = 0;
	public  int repSize() { return repSize; /*representamen.size();*/ }
	

	// ===== getNumber(): a Number Factory
	static private int postOpLen( ListIterator<String> si ) {
		// e.g. [all] cubed or [all] squared
		int len = 0;
		if (si.hasNext()) {
			int    n = 1;
			String s = si.next();
			if (   s.equals( "all" )
				&& si.hasNext()
				&& null != (s = si.next())) n++;
			if (   s.equals(   "cubed" )
			    || s.equals( "squared" )) // || to the power of || 
				len = n;
			Strings.previousN( si, n ); // put them all back!
		} 
		return len;
	}
	static private int opLen( ListIterator<String> si ) {
		int len = 0;
		if (si.hasNext()) {
			String op = si.next();
			int read = 1;
			if (op.equals( "all" ) && si.hasNext()) {
				op = si.next();
				read++;
			}
			if (   op.equals( "times"      )
			    || op.equals( "multiplied" )
			    || op.equals( "divided"    ))
			{
				if ( op.equals( "times" ))
					len = read; // times is ok on its own

				if (si.hasNext()) {
					op = si.next();
					read++;
					if (op.equals( "by" )) {
						len = read;
				}	}

			} else if (
				op.equals( "+" ) || op.equals( "plus"  ) ||
				op.equals( "-" ) || op.equals( "minus" ) ||
				op.equals( "x" ) || //op.equals( "over" ) ||
				op.equals( "/" )                            )
			{
				len = read;
			}
			Strings.previousN( si, read );
		}
		//audit.out( len );
		return len;
	}
	static public boolean isNumeric( String s ) {
		boolean rc = true;
		try {
			Float.parseFloat( s );
		} catch (NumberFormatException nfe) {
			rc = false;
		}
		return rc;
	}
	static private  String token = "";
	       private boolean getNumeric( ListIterator<String> si ) {
		//audit.in( "getNumeric", token );
		// "another" -> (+=) 1
		if (representamen.size() == 0) // != "another"
			append( token );
		else                    // ?= "another"
			representamen.replace( 0, token );
		
		// ...read into the array a succession of ops and numerals 
		while (si.hasNext()) {
			int opLen, postOpLen;
			
			while (0 < (postOpLen = postOpLen( si ))) // ... all squared
				appendn( si, postOpLen );
			// optional, so no break if not found
			
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
						si.previous(); // replace token
						token = removen( si, opLen );
						break;
		}	}	}	}
		//audit.out();
		return true;
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
	public static void main(String args []) {
		Strings s = new Strings( "1 + 2" );
		Numeric n = new Numeric();
		if (n.getNumeric( s.listIterator() ))
			audit.log( "n="+ n.valueOf());
		else
			audit.log( "no numberic value" );
}	}