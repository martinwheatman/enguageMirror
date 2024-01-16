package org.enguage.sign.object;

import org.enguage.sign.Assets;
import org.enguage.sign.object.sofa.Perform;
import org.enguage.sign.symbol.when.Day;
import org.enguage.sign.symbol.when.When;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class Temporal {
	
	static public final String NAME = "temporal";
	static public final int      ID = 240152112; //Strings.hash( NAME );
	static private      Audit audit = new Audit( NAME );
	
	private static Strings concepts = new Strings();
	public  static boolean isConcept( String s) { return concepts.contains( s ); }
	public  static void   addConcept( String s ) {if (!concepts.contains( s )) concepts.add( s );}
	public  static void   addConcepts( Strings ss) {for (String s : ss) addConcept( s );}

	public static String list() { return concepts.toString( Strings.CSV );}
	
	static public Strings perform( Strings args ) {
		audit.in( "interpret", args.toString() );
		String rc = Perform.S_IGNORE;
		if (args.size() > 0) {
			String cmd = args.remove( 0 );
			rc = Perform.S_SUCCESS;
			if (args.size() == 0) {
				if (cmd.equals( "addCurrent" ))
					addConcept( Variable.get( Assets.NAME ));
				else
					rc = Perform.S_FAIL;
			} else {
				if (cmd.equals( "dayOfWeek" )) {
					When w = Day.getWhen( args );
					rc = (w == null ? Perform.S_FAIL : Day.name( w.from().moment()));
				} else if (cmd.equals( "set" )) {
					String arg = args.remove( 0 );
					if ( arg.equals( "future" ))
						When.futureIs();
					else if ( arg.equals( "past" ))
						When.pastIs();
					else if ( arg.equals( "present" ))
						When.presentIs();
					else
						rc = Perform.S_FAIL;
				} else if (cmd.equals( "add" ))
					addConcepts( args );
				else
					rc = Perform.S_FAIL;
			}
		}
		return audit.out( new Strings( rc ));
	}
	public static void main( String args[] ) {
		audit.debug( perform( new Strings( "dayOfWeek 1225" )));
}	}
