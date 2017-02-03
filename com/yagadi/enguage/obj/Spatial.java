package com.yagadi.enguage.obj;

import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

public class Spatial {
	
	public final static String NAME="spatial";
	static Audit audit = new Audit( NAME );
	
	private static Strings concepts = new Strings();
	public  static boolean isConcept(  String s) {return concepts.contains( s );}
	public  static void   addConcepts( Strings ss) {for (String s : ss) addConcept( s );}
	public  static void   addConcept(  String s ) {if (!concepts.contains( s )) concepts.add( s );}
	//public  static void    conceptIs(  String s ) {addConcept( s );}
	
	public static String list() { return concepts.toString( Strings.CSV );}
	
	static public String interpret( Strings args ) {
		audit.in( "interpret", args.toString() );
		String rc = Shell.IGNORE;
		if (args.size() > 1) {
			rc = Shell.FAIL;
			if (args.remove( 0 ).equals( "add" )) {
				addConcepts( args );
				rc = Shell.SUCCESS;
		}	}
		audit.out( rc );
		return rc;
	}
	public static void main( String args[] ) {
		audit.log( interpret( new Strings( "add fred" )));
}	}
