package org.enguage.vehicle.number;

import java.util.ListIterator;

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
	static private boolean aImpliesNumeric = true;
	static public  void    aImpliesNumeric( boolean b ) {aImpliesNumeric = b;}
	static public  boolean aImpliesNumeric() { return aImpliesNumeric;}
	
	//
	// What follows are helper functions for Number...
	//
	public static Strings getParams( ListIterator<String> si ) {
		audit.in( "getParams", Strings.peek( si ));
		Strings params = null;
		if (si.hasNext()) {
			String token = si.next(); // <<<<<<<<<<<<< get expression ( and evaluate it! )
			if (Numerals.isNumeric( token )) { 
				params = new Strings( token ); // <<<< need to be appending (4-1), i.e. 3, not 4!!!
				while (si.hasNext() && Numerals.isNumeric( token = si.next())) 
					params.add( token );
				
				if (token.equals( "and" ) && si.hasNext() && Numerals.isNumeric( token = si.next()))
					params.append( "and" ).append( token );
				else {
					if (token.equals( "and" )) si.previous(); // replace "and"
					for (int sz = params.size(); sz > 1; sz--) {
						si.previous();
						params.remove( sz - 1 );
		}	}	}	}
		return audit.out( params );
	}
	
	public static void main(String args []) {
		Audit.allOn();
		audit.log( "PASSED." );
}	}
