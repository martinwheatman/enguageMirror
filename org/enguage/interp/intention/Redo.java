package org.enguage.interp.intention;

import org.enguage.interp.repertoire.Autoload;
import org.enguage.interp.repertoire.Repertoire;
import org.enguage.objects.Variable;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.sys.Shell;
import org.enguage.vehicle.Utterance;
import org.enguage.vehicle.reply.Reply;

public class Redo {

	private static Audit audit = new Audit( "Redo" );

	static public final String DISAMBIGUATE = "disamb";
	// this supports the command="" attribute loaded in the creation of command data structure
	// needs "command //delete "...". -- to remove a tag, to support '"X" is meaningless.'
	static public Reply unknownCommand( Reply r, String cmd, Strings args ) {
		audit.ERROR( "Unknown command "+ cmd +" "+ args.toString( Strings.CSV ));
		return r.format( Reply.dnu() );
	}
		
	// are we taking the hit of creating / deleting overlays
	static private boolean undoEnabled = false;
	static public  boolean undoIsEnabled() { return undoEnabled; }
	static public  void    undoEnabledIs( boolean enabled ) { undoEnabled = enabled; }
	
	// determines the behaviour of the app over prompting for help...
	static private boolean helped = false;
	static public  void    helped( boolean run ) { helped = run; }
	static public  boolean helped() { return helped; }
		
	// record whether the user has figured it out...
	// run in conjunction with the main intepreter and the app...
	static private final String  spokenVar = "SPOKEN";
	static private       boolean spoken    = false;
	static public        boolean spoken() { return spoken; }
	static public        void    spoken( boolean spk ) {
		//audit.traceIn( "spoken", spk ? Shell.SUCCESS : Shell.FAIL );
		if (!Repertoire.induction() && !Autoload.ing() && spk != spoken) {
			//audit.audit( "Allop.spoken(): remembering "+ spk );
			Variable.set( spokenVar, spk ? Shell.SUCCESS : Shell.FAIL );
			spoken = spk;
		}
		//audit.traceOut();
	}
	static public  void    spokenInit() { // called after encaching vars
		spoken = Variable.get( spokenVar, Shell.FAIL ).equals( Shell.SUCCESS );
	}
	//
	//
	// redo ----------------------------------------------------
	private static boolean disambFound = false;
	public  static void    disambFound( boolean b ) { disambFound = b; }
	public  static boolean disambFound() { return disambFound; }
	
	static public void disambOn( Strings cmd ) {
		//simply turn disambiguation on if this thought is same as last...
		audit.debug( "Allop:disambFound():REDOING:"+(disambFound()?"ON":"OFF")+":"+ Utterance.previous() +" =? "+ cmd +")" );
		if (	( Utterance.previous()                  .equals( cmd  )) //    X == (redo) X     -- case 1
		    ||	(    Utterance.previous().copyAfter( 0 ).equals( cmd  )  // no X == (redo) X...  -- case 2
		    	  && Utterance.previous().get(    0    ).equals( "no" )  // ..&& last[ 0 ] = "no"
		)	)	{
			if (Repertoire.signs.lastFoundAt() != -1) { // just in case!
				Repertoire.signs.ignore( Repertoire.signs.lastFoundAt() );
				audit.debug("Allop:disambOn():REDOING: Signs to avoid now: "+ Repertoire.signs.ignore().toString() );
				disambFound( true );
	}	}	}
	/* now, we have disamb found (ignore list has increased) so we are still adjusting
	 * the list AND the list itself! This is called at the end of an utterance
	 */
	static public void disambOff() {
		//audit.traceIn( "disambOff", "avoids="+ Repertoires.signs.ignore().toString());
		if (disambFound()) { //still adjusting the list!
			//audit.debug( "Allop:disambOff():COOKED!" );
			disambFound( false );
			Repertoire.signs.reorder();
		} else {
			//audit.debug( "Allop:disambOff():RAW, forget ignores: "+ Enguage.e.signs.ignore().toString());
			Repertoire.signs.ignoreNone();
		}
		//audit.traceOut();
	}
	// redo ----------------------------------------------------
	//
	//
}
