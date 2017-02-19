package com.yagadi.enguage.interpretant;

import com.yagadi.enguage.Enguage;
import com.yagadi.enguage.object.Variable;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;
import com.yagadi.enguage.vehicle.Context;
import com.yagadi.enguage.vehicle.Language;
import com.yagadi.enguage.vehicle.Reply;
import com.yagadi.enguage.vehicle.Utterance;

/*
 * TODO: Allop should be split into the NAME/value pair and the generic repertoire. Discuss. 
 *       Start by introducing Repertoire.class => Signs (Signs ArrayList of Sign!)
 */
public class Allopoiesis extends Intention {
	private static Audit audit = new Audit( "Allop" );

	public  static final String NAME = "engine";
	public  static final String HELP = "help";
	private static final String DISAMBIGUATE = "disamb";
	
	public static final Sign commands[] = {
		/* These could be accompanied in a repertoire, but they have special 
		 * interpretations and so are built here alongside those interpretations.
		 */
		new Sign().content( new Tag(  "describe ", "x" )).concept( NAME   )
															 .attribute( NAME, "describe X" )
															 .help( "where x is a repertoire" ),
		new Sign().content( new Tag("list repertoires","" )).concept( NAME   )
															 .attribute( NAME, "list" )
															 .help( ""     ),
		new Sign().content( new Tag(           "help", "" )).attribute( NAME, "help" ),
		new Sign().content( new Tag(          "hello", "" )).attribute( NAME, "hello"),
		new Sign().content( new Tag(        "welcome", "" )).attribute( NAME, "welcome"),
		new Sign().content( new Tag( "what can i say", "" )).concept( NAME   )
															 .attribute( NAME, "repertoire"  )
															 .help( ""            ),
		new Sign().content( new Tag(   "load ", "NAME" )).attribute( NAME,   "load NAME" ),
/*		new Sign().content( new Tag( "unload ", "NAME" )).attribute( NAME, "unload NAME" ),
		new Sign().content( new Tag( "reload ", "NAME" )).attribute( NAME, "reload NAME" ),
// */	//new Sign().attribute( NAME, "save"    ).content( new Tag( "save", "", "" ) ),
		//new Sign().attribute( NAME, "saveas $NAME" ).content( new Tag("saveas ", "NAME", ".")),
															 		
		new Sign().content( new Tag(    "enable undo",  "" )).attribute( NAME, "undo enable"  ),
		new Sign().content( new Tag(   "disable undo",  "" )).attribute( NAME, "undo disable" ),
		new Sign().content( new Tag(           "undo",  "" )).attribute( NAME, "undo"         ),
		new Sign().content( new Tag(      "say again",  "" )).attribute( NAME, "repeat"       ),
		new Sign().content( new Tag(         "spell ", "x" )).attribute( NAME, "spell X"      ),
		
//		new Sign().content( new Tag("", "x", "is temporal" )).attribute( NAME, "temporal X"   ),
		
		new Sign().content( new Tag(         "timing  on",  "" )).attribute( NAME, "tracing on" ),
		new Sign().content( new Tag(         "timing off",  "" )).attribute( NAME, "tracing off" ),
		new Sign().content( new Tag(        "tracing  on",  "" )).attribute( NAME, "tracing on" ),
		new Sign().content( new Tag(        "tracing off",  "" )).attribute( NAME, "tracing off" ),
		new Sign().content( new Tag(         "detail  on",  "" )).attribute( NAME, "detailed on" ),
		new Sign().content( new Tag(         "detail off",  "" )).attribute( NAME, "detailed off" ),
		new Sign().content( new Tag(              "show ", "x" ).attribute( Tag.phrase, Tag.phrase ))
				.attribute( NAME, "show X" ),
		new Sign().content( new Tag(         "debug ", "x" ).attribute( Tag.phrase, Tag.phrase ))
				.attribute( NAME, "debug X" ),
		/* 
		 * it is possible to arrive at the following construct:   think="reply 'I know'"
		 * e.g. "if X, Y", if the instance is "if already exists, reply 'I know'"
		 * here reply is thought. Should be rewritten:
		 * representamen: "if X, reply Y", then Y is just the quoted string.
		 * However, the following should deal with this situation.
		 */
		new Sign().content( new Tag( REPLY +" ", "x" ).attribute( Tag.quoted, Tag.quoted ))
					.attribute( REPLY, "X" ),
		
		// fix to allow better reading of autopoietic  
		new Sign().content( new Tag( "if so, ", "x" ).attribute( Tag.phrase, Tag.phrase ))
					.attribute( THINK, "X" ),
		/* 
		 * REDO: undo and do again, or disambiguate
		 */
		new Sign().content( new Tag( "No ", "x" ).attribute( Tag.phrase, Tag.phrase ))
					.attribute( NAME, "undo" )
					.attribute( ELSE_REPLY, "undo is not available" )
					/* On thinking the below, if X is the same as what was said before,
					 * need to search for the appropriate sign from where we left off
					 * Dealing with ambiguity: "X", "No, /X/"
					 */
					.attribute( NAME,  DISAMBIGUATE +" X" ) // this will set up how the inner thought, below, works
					.attribute( THINK,  "X"    )
	 };
	
	// are we taking the hit of creating / deleting overlays
	//private boolean undoEnabled = false;
	//private boolean undoIsEnabled() { return undoEnabled; }
	//public  Allop undoEnabledIs( boolean enabled ) { undoEnabled = enabled; return this; }
	

	
	public Allopoiesis( String name, String value ) { super( name, value ); }
	
	// this supports the command="" attribute loaded in the creation of command data structure
	// needs "command //delete "...". -- to remove a tag, to support '"X" is meaningless.'
	private Reply unknownCommand( Reply r, Strings cmd ) {
		audit.ERROR( "Unknown command "+ cmd.toString( Strings.CSV ));
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
		if (!Repertoire.isInducting() && !Autoload.ing() && spk != spoken) {
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
	
	static private void disambOn( Strings cmd ) {
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

	public Reply mediate( Reply r ) {
		r.answer( Reply.yes()); // just to stop debug output look worrying
		
		Strings cmds =
				Context.deref(
					/*Variable.deref(*/
						new Strings( value )
					/*)*/
				);
		cmds = cmds.normalise();
		String cmd = cmds.get( 0 );
		int sz = cmds.size();

/*		audit.debug( "in Allop.mediate, ctx="+ Reply.context().toString());
		audit.debug( "in Allop.mediate, val=[ "+ value +" ]");
		audit.debug( "in Allop.mediate, cmd=[ "+ Strings.toString( cmd, Strings.CSV ) +" ]");
		audit.debug( "in Allop.mediate, NAME='"+ NAME +"', value='"+ value +"'");
// */
		if ( cmd.equals( "undo" )) {
			Enguage e = Enguage.get();
			r.format( Reply.success() );
			if (cmds.size() == 2 && cmds.get( 1 ).equals( "enable" )) 
				undoEnabledIs( true );
			else if (cmds.size() == 2 && cmds.get( 1 ).equals( "disable" )) 
				undoEnabledIs( false );
			else if (cmds.size() == 1 && undoIsEnabled()) {
				if (e.o.count() < 2) { // if there isn't an overlay to be removed
					audit.debug( "overlay count( "+ e.o.count() +" ) < 2" ); // audit
					r.answer( Reply.no() );
				} else {
					audit.debug("ok - restarting transaction");
					e.o.reStartTxn();
				}
			} else if (!undoIsEnabled())
				r.format( Reply.dnu() );
			else
				r = unknownCommand( r, cmds );
			
		} else if (sz == 2 && cmds.get( 1 ).equals( "learning" )) {
			Repertoire.inductingIs( cmd.equalsIgnoreCase( "start" ));
			
		} else if (cmd.equals( DISAMBIGUATE )) {
			disambOn( cmds.copyAfter( 0 ));
		
		} else if (cmd.equals( "load" )) {
			/* load is used by create, delete, ignore and restore to
			 * support their interpretation
			 */
			Strings files = cmds.copyAfter( 0 );
			audit.debug( "loading "+ files.toString( Strings.CSV ));
			for(int i=0; i<files.size(); i++)
				Concepts.load( files.get( i ));
/*			 
		} else if (cmd.equals( "unload" )) {
			Strings files = cmds.copyAfter( 0 );
			for(int i=0; i<files.size(); i++)
				Concept.unload( files.get( i ));

		} else if (cmd.equals( "reload" )) {
			Strings files = cmds.copyAfter( 0 );
			for(int i=0; i<files.size(); i++) Concept.unload( files.get( i ));
			for(int i=0; i<files.size(); i++) Concept.load( files.get( i ));
/*
		} else if (e.get( 0 ).equals( "save" ) || e.get( 0 ).equalsIgnoreCase( "saveAs" )) {
			if (e.get( 0 ).equalsIgnoreCase( "saveAs" ) && ( e.size() != 2))
				System.err.println( e.get( 0 ) +": NAME required." );
			else {
				if (e.get( 0 ).equalsIgnoreCase( "saveAs" )) {
					//(re)NAME concept
					System.out.println( "renaming concept" );
				}
				//save concept
				System.out.println( "Saving concept" );
			}
*/
		} else if (cmd.equals( "spell" )) {
			r.format( Language.spell( cmds.get( 1 ), true ));
			
		} else if (cmd.equals( "timing" )) {
			audit.log( cmds.toString());
			if (cmds.get( 1 ).equals("off")) {
				Audit.allOff();
				Audit.timings = false;
			} else {
				Audit.allOn();
				Audit.runtimeDebug = true;
				Audit.allTracing = true;
				Audit.detailedDebug = true;
				Audit.timings = true;
			}
			r.format( Reply.success() );
			
		} else if (cmd.equals( "tracing" )) {
			audit.log( cmds.toString());
			if (cmds.get( 1 ).equals("off")) {
				Audit.allOff();
				Audit.timings = false;
				Audit.allTracing = false;
				Audit.detailedDebug = false;
			} else {
				Audit.allOn();
				Audit.runtimeDebug = true;
				Audit.allTracing = true;
			}
			r.format( Reply.success() );
			
		} else if (cmd.equals( "detailed" )) {
			audit.log( cmds.toString());
			if (cmds.get( 1 ).equals("off")) {
				Audit.allOff();
				Audit.timings = false;
				Audit.detailedDebug = false;
			} else {
				Audit.allOn();
				Audit.runtimeDebug = true;
				Audit.allTracing = true;
				Audit.detailedDebug = true;
			}
			r.format( Reply.success() );
			
		} else if (cmd.equals( "debug" )) {
			if (cmds.get( 1 ).equals( "off" )) {
				Audit.allOff();
				Audit.allTracing = false;
				Audit.timings = false;
				Audit.runtimeDebug = false;
				Audit.detailedDebug = false;
				
			} else if (cmds.get( 1 ).equals( "tags" )) {
				Tags.debug( !Tags.debug() );
				
			} else {
				Audit.allOn();
				Audit.runtimeDebug = true;
			}
			r.format( Reply.success() );
			
			
		} else if (cmd.equals( "show" )) {
			//audit.audit( "cmds:"+ cmds +":sz="+ cmds.size() );
			if (2==cmds.size() && cmds.get( 1 ).length()>=4) {
				String option = cmds.get( 1 ).substring(0,4);
				if (option.equals( "auto" )) {
					Repertoire.autop.show();
					r.format( Reply.success() );
				} else if (   option.equals( "sign" )
				           || option.equals( "user" )) {
					Repertoire.signs.show();
					r.format( Reply.success() );
				} else if (option.equals( "engi" )) {
					Repertoire.allop.show();
					r.format( Reply.success() );
				} else if (option.equals( "all" )) {
					Repertoire.autop.show();
					Repertoire.allop.show();
					Repertoire.signs.show();
					r.format( Reply.success() );
				} else
					audit.ERROR( "option: "+ option +" doesn't match anything" );
			} else {
				Repertoire.signs.show();
				r.format( Reply.success() );
			}


		} else if ( value.equals( "repeat" )) {
			if (Reply.previous() == null) {
				audit.log("Allop:repeating dnu");
				r.format( Reply.dnu());
			} else {
				audit.log("Allop:repeating: "+ Reply.previous());
				r.repeated( true );
				r.format( Reply.repeatFormat());
				r.answer( Reply.previous());
			}
			
		} else if (   cmd.equals( "help"    )) {
			helped( true );
			r.format( Repertoire.allop.helpedToString( NAME ));

		} else if (cmd.equals( "hello"   ) ||
				   cmd.equals( "welcome" )    ) {
			helped( true );
			if (cmd.equals(  "hello"  )) r.say( new Strings( "hello" ));
			if (cmd.equals( "welcome" )) r.say( new Strings( "welcome" ));
			r.format( Signs.help());

		} else if ( cmd.equals( "list" )) {
			//Strings reps = Enguage.e.signs.toIdList();
			/* This becomes less important as the interesting stuff becomes auto loaded 
			 * Don't want to list all repertoires once the repertoire base begins to grow?
			 * May want to ask "is there a repertoire for needs" ?
			 */
			r.format( "loaded repertoires include "+ new Strings( Concepts.loaded()).toString( Reply.andListFormat() ));
			
		} else if ( cmd.equals( "describe" ) && cmds.size() >= 2) {
			String name = cmds.copyAfter( 0 ).toString( Strings.CONCAT );
			r.format( Repertoire.signs.helpedToString( name ));
			
		} else if ( cmd.equals( "repertoire" )) {
			r.format( Repertoire.signs.helpedToString());
			
		} else
			r = unknownCommand( r, cmds );
		return r;
	}
	public static void main( String args[]) {
		Audit.allOn(); //main()
		// NB. This test program needs more work.
		//Enguage eng = new Enguage( null );
		Reply r = new Reply();
		// TODO: this fails, cose Enguage.e is not initialised!!!
		Allopoiesis e = new Allopoiesis( "engine", "temporal meeting" );
		//Repertoire.load( "need" );
		e.mediate( r );
}	}
