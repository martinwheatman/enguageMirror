package org.enguage.vehicle;

import org.enguage.util.Audit;

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
	
	public static void main(String args []) {
		Audit.allOn();
		audit.log( "PASSED." );
}	}
