package org.enguage.signs.interpretant;

import org.enguage.Enguage;
import org.enguage.repertoires.Repertoires;
import org.enguage.signs.symbol.Utterance;
import org.enguage.signs.symbol.reply.Reply;
import org.enguage.signs.symbol.reply.Response;
import org.enguage.util.Audit;
import org.enguage.util.Strings;

public class Redo {
	
	private Redo() {}

	private static Audit audit = new Audit( "Redo" );

	public  static final String DISAMBIGUATE = "disamb";
	public  static final String UNDO         = "undo";
	
	// this supports the command="" attribute loaded in the creation of command data structure
	// needs "command //delete "...". -- to remove a tag, to support '"X" is meaningless.'
	public  static Reply unknownCommand( Reply r, String cmd, Strings args ) {
		return r.format( Response.dnu() );
	}
		
	// are we taking the hit of creating / deleting overlays
	private static boolean undoEnabled = false;
	public  static boolean undoIsEnabled() { return undoEnabled; }
	public  static void    undoEnabledIs( boolean enabled ) { undoEnabled = enabled; }
	
	private static boolean disambFound = false;
	public  static void    disambFound( boolean b ) { disambFound = b; }
	public  static boolean disambFound() { return disambFound; }
	
	public  static void    disambOn( Strings cmd ) { // called from Engine!
		audit.in( "disambOn", "cmd="+ cmd );
		//simply turn disambiguation on if this thought is same as last...
		audit.debug( "disambFound:"+(disambFound()?"ON":"OFF")+":"+ Utterance.previous() +" =? "+ cmd +")" );

		if (Utterance.previous() != null
			&& (       Utterance.previous()               .equals( cmd  ) // first time around
			    || 
			    	(   Utterance.previous().get(    0    ).equals( "no" ) // subsequent times
			    	 && Utterance.previous().copyAfter( 0 ).equals( cmd  ) // no, ...
			   )   )	
			&& (Repertoires.signs.lastFoundAt() != -1))
		{
			Enguage.firstMatch( true );
			audit.debug("going to skip: "+ Repertoires.signs.lastFoundAt() );
			Repertoires.signs.ignore( Repertoires.signs.lastFoundAt() );
			audit.debug("Signs to avoid now: "+ Repertoires.signs.ignore() );
			disambFound( true );
		}
		audit.out();
	}
	
	/* now, we have disamb found (ignore list has increased) 
	 * so we are still adjusting the list AND the list itself! 
	 * This is called at the end of an utterance
	 */
	public static void disambOff( Strings reply ) {
		audit.in( "disambOff", "(da="+ disambFound() +")" );
		disambFound( false );
		if (reply.beginsIgnoreCase( new Strings( "i don't understand" ))) {//disambFound()) { //still adjusting the list!
			Repertoires.signs.ignoreNone();
		}
		audit.out();
	}
}
