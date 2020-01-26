package org.enguage.objects;

import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.sys.Shell;
import org.enguage.vehicle.where.Where;

import com.yagadi.Assets;

public class Spatial {
	
	static public final String NAME = "spatial";
	static public final int      id = 233089091; //Strings.hash( NAME );
	static private      Audit audit = new Audit( NAME );
	
	private static Strings concepts = new Strings();
	private static void   addConcepts( Strings ss) {for (String s : ss) addConcept( s );}
	public  static void    addConcept(  String s ) {if (s != null && !concepts.contains( s )) concepts.add( s );}
	public  static boolean  isConcept(  String s) {return concepts.contains( s );}
	//public  static void    conceptIs(  String s ) {addConcept( s );}
	
	public static String list() { return concepts.toString( Strings.CSV );}
	
	static public Strings interpret( Strings args ) {
		audit.in( "interpret", args.toString() );
		String rc = Shell.IGNORE;
		if (args.size() > 0) {
			String cmd = args.remove( 0 );
			rc = Shell.SUCCESS;
			if (cmd.equals( "add" ))
				addConcepts( args );
			else if (cmd.equals( "addCurrent" ))
				addConcept( Variable.get( Assets.LOADING ));
			else if (cmd.equals( "locator" ))
				Where.locatorIs( args );
			else
				rc = Shell.FAIL;
		}
		audit.out( rc );
		return new Strings( rc );
	}
	public static void main( String args[] ) {
		Audit.log( interpret( new Strings( "add fred" )));
}	}
