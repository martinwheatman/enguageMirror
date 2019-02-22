package org.enguage.objects;

import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.sys.Shell;
import org.enguage.vehicle.when.Day;
import org.enguage.vehicle.when.When;

public class Temporal {
	
	static public final String NAME = "temporal";
	static public final int      id = 240152112; //Strings.hash( NAME );
	static private      Audit audit = new Audit( NAME );
	
	private static Strings concepts = new Strings();
	public  static boolean isConcept( String s) { return concepts.contains( s ); }
	public  static void    addConcept( String s ) {if (!concepts.contains( s )) concepts.add( s );}
	//private static void    conceptIs( String s ) {addConcept( s );}
	public  static void   addConcepts( Strings ss) {for (String s : ss) addConcept( s );}

	public static String list() { return concepts.toString( Strings.CSV );}
	
	static public Strings interpret( Strings args ) {
		audit.in( "interpret", args.toString() );
		String rc = Shell.IGNORE;
		if (args.size() > 1) {
			rc = Shell.FAIL;
			String cmd = args.remove( 0 );
			if (cmd.equals( "dayOfWeek" )) {
				When w = Day.getWhen( args );
				rc = (w == null ? Shell.FAIL : Day.name( w.from().moment()));
				
			} else if (cmd.equals( "set" )) {
				rc = Shell.SUCCESS;
				String arg = args.remove( 0 );
				if ( arg.equals( "future" ))
					When.futureIs();
				else if ( arg.equals( "past" ))
					When.pastIs();
				else if ( arg.equals( "present" ))
					When.presentIs();
				
			} else if (cmd.equals( "add" )) {
				addConcepts( args );
				rc = Shell.SUCCESS;
		}	}
		return audit.out( new Strings( rc ));
	}
	public static void main( String args[] ) {
		audit.log( interpret( new Strings( "dayOfWeek 1225" )));
}	}
