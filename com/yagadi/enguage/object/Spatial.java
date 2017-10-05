package com.yagadi.enguage.object;

import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;
import com.yagadi.enguage.vehicle.where.Where;

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
			String cmd = args.remove( 0 );
			rc = Shell.SUCCESS;
			if (cmd.equals( "add" ))
				addConcepts( args );
			else if (cmd.equals( "locator" ))
				Where.locatorIs( args.toString() );
			else
				rc = Shell.FAIL;
		}
		audit.out( rc );
		return rc;
	}
	public static void main( String args[] ) {
		audit.log( interpret( new Strings( "add fred" )));
}	}
