package org.enguage.vehicle.number;

import org.enguage.util.Audit;

public class Numerals {
	//static private Audit audit = new Audit( "Numerals", false );

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
	
	public static void main(String args []) {
		Audit.allOn();
//		Audit.log( "params found "+ getParams( new Strings( "4 - 1" ).listIterator() ));
//		Audit.log( "params found "+ getParams( new Strings( "4 and 1" ).listIterator() ));
//		Audit.log( "params found "+ getParams( new Strings( "x and y" ).listIterator() ));
		Audit.log( "PASSED." );
}	}
