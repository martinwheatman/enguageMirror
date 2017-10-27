package com.yagadi.enguage.interpretant;

import com.yagadi.enguage.Enguage;
import com.yagadi.enguage.interpretant.repertoire.Autoload;
import com.yagadi.enguage.interpretant.repertoire.Concepts;
import com.yagadi.enguage.interpretant.repertoire.Repertoire;
import com.yagadi.enguage.object.Variable;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Net;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;
import com.yagadi.enguage.util.Tags;
import com.yagadi.enguage.vehicle.Context;
import com.yagadi.enguage.vehicle.Language;
import com.yagadi.enguage.vehicle.Question;
import com.yagadi.enguage.vehicle.Reply;
import com.yagadi.enguage.vehicle.Utterance;

/*
 * TODO: Allop should be split into the NAME/value pair and the generic repertoire. Discuss. 
 *       Start by introducing Repertoire.class => Signs (Signs ArrayList of Sign!)
 */
public class Allopoiesis extends Intention {
	private static Audit audit = new Audit( "Allop" );

	public  static final String NAME = Intention.ALLOP;
	public  static final String HELP = "help";
	private static final String DISAMBIGUATE = "disamb";
	
	public static final Sign commands[] = {
		/* These could be accompanied in a repertoire, but they have special 
		 * interpretations and so are built here alongside those interpretations.
		 */
		new Sign()
			.pattern( new Patternette( "remove primed answer ", "" ))
          		.appendIntention( Intention.allop, "removePrimedAnswer" )
          		.concept( NAME ),
	          	
    	new Sign()
			.pattern( new Patternette( "prime answer ", "answer" ).phrasedIs())
	          	.appendIntention( Intention.allop, "primeAnswer ANSWER" )
          		.concept( NAME ),
			          	
		new Sign()
			.pattern( new Patternette( "answering", "answers" ).phrasedIs())
			.pattern( new Patternette( "ask", "question" ).phrasedIs())
	          	.appendIntention( Intention.allop, "ask answering ANSWERS , QUESTION" )
          		.concept( NAME ),
	          	
		new Sign().pattern( new Patternette(  "describe ", "x" ))
				.appendIntention( Intention.allop, "describe X" )
	          	.concept( NAME )
				.help( "where x is a repertoire" ),
				 
		new Sign()
				.pattern( new Patternette( "list repertoires","" ))
				.appendIntention( Intention.allop, "list" )
          		.concept( NAME )
				.help( ""     ),
		new Sign()
				.pattern( new Patternette(           "help", "" ))
				.appendIntention( Intention.allop, "help" )
		  		.concept( NAME ),
		new Sign()
				.pattern( new Patternette(          "hello", "" ))
				.appendIntention( Intention.allop, "hello")
		  		.concept( NAME ),
		new Sign()
				.pattern( new Patternette(        "welcome", "" ))
				.appendIntention( Intention.allop, "welcome")
		  		.concept( NAME ),
		new Sign().pattern( new Patternette( "what can i say", "" ))
				 .appendIntention( Intention.allop, "repertoire"  )
	          	.concept( NAME )
				 .help( ""            ),
		new Sign()
				.pattern( new Patternette(   "load ", "NAME" ))
				.appendIntention( Intention.allop,   "load NAME" )
		  		.concept( NAME ),
/*		new Sign().concept( NAME ).content( new Patternette( "unload ", "NAME" )).attribute( new Intention( Intention.allop, "unload NAME" ),
		new Sign().concept( NAME ).content( new Patternette( "reload ", "NAME" )).attribute( NAME, "reload NAME" ),
// */	//new Sign().concept( NAME ).attribute( NAME, "save"    ).content( new Patternette( "save", "", "" ) ),
		//new Sign().concept( NAME ).attribute( NAME, "saveas $NAME" ).content( new Patternette("saveas ", "NAME", ".")),
															 		
		new Sign().pattern( new Patternette(     "say again",  "" )).appendIntention( Intention.allop, "repeat"       ),
		new Sign().pattern( new Patternette(        "spell ", "x" )).appendIntention( Intention.allop, "spell X"      ),
		new Sign().pattern( new Patternette(   "enable undo",  "" )).appendIntention( Intention.allop, "undo enable"  ),
		new Sign().pattern( new Patternette(  "disable undo",  "" )).appendIntention( Intention.allop, "undo disable" ),
		new Sign().concept( NAME ).pattern( new Patternette(          "undo",  "" )).appendIntention( Intention.allop, "undo"         ),
		new Sign().concept( NAME ).pattern( new Patternette( "this is false",  "" )).appendIntention( Intention.allop, "undo" ),
		new Sign().concept( NAME ).pattern( new Patternette( "this sentence is false",  "" )).appendIntention( Intention.allop, "undo" ),
		
//		new Sign().concept( NAME ).content( new Patternette("", "x", "is temporal" )).attribute( NAME, "temporal X"   ),
		
		new Sign().concept( NAME ).pattern( new Patternette(         "timing  on",  "" )).appendIntention( Intention.allop, "tracing on" ),
		new Sign().concept( NAME ).pattern( new Patternette(         "timing off",  "" )).appendIntention( Intention.allop, "tracing off" ),
		new Sign().concept( NAME ).pattern( new Patternette(        "tracing  on",  "" )).appendIntention( Intention.allop, "tracing on" ),
		new Sign().concept( NAME ).pattern( new Patternette(        "tracing off",  "" )).appendIntention( Intention.allop, "tracing off" ),
		new Sign().concept( NAME ).pattern( new Patternette(         "detail  on",  "" )).appendIntention( Intention.allop, "detailed on" ),
		new Sign().concept( NAME ).pattern( new Patternette(         "detail off",  "" )).appendIntention( Intention.allop, "detailed off" ),
		new Sign().concept( NAME )
				.pattern( new Patternette( "tcpip ",  "address" ))
				.pattern( new Patternette(      " ",  "port" ))
				.pattern( new Patternette(      " ",  "data" ).quotedIs())
					.appendIntention( Intention.allop, "tcpip ADDRESS PORT DATA" ),
		new Sign().concept( NAME ).pattern( new Patternette(              "show ", "x" ).phrasedIs())
				.appendIntention( Intention.allop, "show X" ),
		new Sign().concept( NAME ).pattern( new Patternette(         "debug ", "x" ).phrasedIs())
				.appendIntention( Intention.allop, "debug X" ),
		/* 
		 * it is possible to arrive at the following construct:   think="reply 'I know'"
		 * e.g. "if X, Y", if the instance is "if already exists, reply 'I know'"
		 * here reply is thought. Should be rewritten:
		 * representamen: "if X, reply Y", then Y is just the quoted string.
		 * However, the following should deal with this situation.
		 */
		new Sign().concept( NAME ).pattern( new Patternette( REPLY +" ", "x" ).quotedIs())
				.appendIntention( Intention.thenReply, "X" ),
		
		// fix to allow better reading of autopoietic  
		new Sign().concept( NAME ).pattern( new Patternette( "if so, ", "x" ).phrasedIs())
				.appendIntention( Intention.thenThink, "X" ),

		// for vocal description of concepts... autopoiesis!		
		new Sign().concept( NAME ).pattern( new Patternette( "perform ", "args" ).phrasedIs())
				.appendIntention( Intention.thenDo, "ARGS" ),
		/* 
		 * REDO: undo and do again, or disambiguate
		 */
		new Sign().concept( NAME ).pattern( new Patternette( "No ", "x" ).phrasedIs())
					.appendIntention( Intention.allop, "undo" )
					.appendIntention( Intention.elseReply, "undo is not available" )
					/* On thinking the below, if X is the same as what was said before,
					 * need to search for the appropriate sign from where we left off
					 * Dealing with ambiguity: "X", "No, /X/"
					 */
					.appendIntention( Intention.allop,  DISAMBIGUATE +" X" ) // this will set up how the inner thought, below, works
					.appendIntention( Intention.thenThink,  "X"    )
	 };
	
	public Allopoiesis( int type, String value ) { super( type , value ); }
	public Allopoiesis( Intention in, boolean temp, boolean spatial ) {
		super( in, temp, spatial );
	}
	
	// this supports the command="" attribute loaded in the creation of command data structure
	// needs "command //delete "...". -- to remove a tag, to support '"X" is meaningless.'
	private Reply unknownCommand( Reply r, String cmd, Strings args ) {
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
		r.answer( Reply.yes()); // bland default reply to stop debug output look worrying
		
		Strings cmds =
				Context.deref(
					/*Variable.deref(*/
						new Strings( value() )
					/*)*/
				);
		cmds = cmds.normalise();
		String cmd = cmds.remove( 0 );

		if ( cmd.equals( "primeAnswer" )) {
			
			Question.primedAnswer( cmds.toString() ); // needs to be tidied up...
			
			
		} else if ( cmd.equals( "removePrimedAnswer" )) {
			
			Question.primedAnswer( null ); // tidy up any primed answer...
			
			
		} else if ( cmd.equals( "ask" )) {
			
			String question = cmds.toString();
			audit.debug( "Question is: "+ question );
			// question => concept
			
			Strings answers = Question.extractPotentialAnswers( cmds );
			audit.debug( "potential ANSWERs are ["+ answers.toString( Strings.DQCSV ) +"]");

			// question => answer
			String answer = new Question( question ).ask();
			Question.primedAnswer( null ); // tidy up any primed answer...
			
			r.format( answer );
			if (!answers.contains( answer ))
				r.userDNU();
			
		} else if ( cmd.equals( "undo" )) {
			Enguage e = Enguage.get();
			r.format( Reply.success() );
			if (cmds.size() == 1 && cmds.get( 0 ).equals( "enable" )) 
				undoEnabledIs( true );
			else if (cmds.size() == 1 && cmds.get( 0 ).equals( "disable" )) 
				undoEnabledIs( false );
			else if (cmds.size() == 0 && undoIsEnabled()) {
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
				r = unknownCommand( r, cmd, cmds );
			
		} else if (cmd.equals( DISAMBIGUATE )) {
			disambOn( cmds );
		
		} else if (cmd.equals( "load" )) {
			/* load is used by create, delete, ignore and restore to
			 * support their interpretation
			 */
			Strings files = cmds;
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
			r.format( Language.spell( cmds.get( 0 ), true ));
			
		} else if (cmd.equals( "tcpip" )) {
			
			String prefix = Variable.get( "XMLPRE" ),
					suffix = Variable.get( "XMLPOST" );
			
			int port = -1;
			try {
				port = Integer.valueOf( cmds.get( 1 ));
			} catch (Exception e1) {
				try {
					port = Integer.valueOf( Variable.get( "PORT" ));
				} catch (Exception e2) {
					port = 0;
			}	}
			
			r = Net.client(
					r,
					cmds.get( 0 ),
					port,
					(null==prefix ? "" : prefix) +
						Variable.derefUc( Strings.trim( cmds.get( 2 ), '"' )) +
						(null==suffix ? "" : suffix)
				);
			
		} else if (cmd.equals( "timing" )) {
			audit.log( cmd +" "+ cmds.toString());
			if (cmds.get( 0 ).equals("off")) {
				Audit.allOff();
				Audit.timings = false;
			} else {
				Audit.allOn();
				Audit.runtimeDebug = true;
				Audit.allTracing = true;
				Audit.detailedOn = true;
				Audit.timings = true;
			}
			r.format( Reply.success() );
			
		} else if (cmd.equals( "tracing" )) {
			audit.log( cmd +" "+ cmds.toString());
			if (cmds.get( 0 ).equals("off")) {
				Audit.allOff();
				Audit.timings = false;
				Audit.allTracing = false;
				Audit.detailedOn = false;
			} else {
				Audit.allOn();
				Audit.runtimeDebug = true;
				Audit.allTracing = true;
			}
			r.format( Reply.success() );
			
		} else if (cmd.equals( "detailed" )) {
			
			audit.log( cmds.toString());
			if (cmds.get( 0 ).equals("off")) {
				Audit.allOff();
				Audit.timings = false;
				Audit.allTracing = false;
				Audit.detailedOn = false;
			} else {
				Audit.allOn();
				Audit.runtimeDebug = true;
				Audit.allTracing = true;
				Audit.detailedOn = true;
			}
			r.format( Reply.success() );
			
		} else if (cmd.equals( "debug" )) {
			
			if (cmds.get( 0 ).equals( "off" )) {
				Audit.allOff();
				Audit.allTracing = false;
				Audit.timings = false;
				Audit.runtimeDebug = false;
				Audit.detailedOn = false;
				
			} else if (cmds.get( 1 ).equals( "tags" )) {
				Tags.debug( !Tags.debug() );
				
			} else {
				Audit.allOn();
				Audit.runtimeDebug = true;
			}
			r.format( Reply.success() );
			
			
		} else if (cmd.equals( "show" )) {
			
			//audit.audit( "cmds:"+ cmds +":sz="+ cmds.size() );
			if (1==cmds.size() && cmds.get( 0 ).length()>=4) {
				String option = cmds.get( 0 ).substring(0,4);
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
				} else if (option.equals( "vari" )) {
					Variable.interpret( new Strings( "show" ));
					r.format( Reply.success());
				} else
					audit.ERROR( "option: "+ option +" doesn't match anything" );
			} else {
				Repertoire.signs.show();
				r.format( Reply.success() );
			}


		} else if ( value().equals( "repeat" )) {
			if (Reply.previous() == null) {
				audit.log("Allop:repeating dnu");
				r.format( Reply.dnu());
			} else {
				audit.log("Allop:repeating: "+ Reply.previous());
				r.repeated( true );
				r.format( Reply.repeatFormat());
				r.answer( Reply.previous());
			}
			
		} else if (cmd.equals( "help"    )) {
			helped( true );
			r.format( Repertoire.allop.helpedToString( NAME ));

		} else if (cmd.equals( "welcome" )    ) {
			helped( true );
			r.say( new Strings( "welcome" ));
			r.format( Signs.help());

		} else if ( cmd.equals( "list" )) {
			//Strings reps = Enguage.e.signs.toIdList();
			/* This becomes less important as the interesting stuff becomes auto loaded 
			 * Don't want to list all repertoires once the repertoire base begins to grow?
			 * May want to ask "is there a repertoire for needs" ?
			 */
			r.format( "loaded repertoires include "+ new Strings( Concepts.loaded()).toString( Reply.andListFormat() ));
			
		} else if ( cmd.equals( "describe" ) && cmds.size() >= 2) {
			
			String name = cmds.toString( Strings.CONCAT );
			r.format( Repertoire.signs.helpedToString( name ));
			
		} else if ( cmd.equals( "repertoire" )) {
			r.format( Repertoire.signs.helpedToString());
			
		} else {
			
			r = unknownCommand( r, cmd, cmds );
		}
		return r;
	}
	public static void main( String args[]) {
		Audit.allOn(); //main()
		// NB. This test program needs more work.
		//Enguage eng = new Enguage( null );
		Reply r = new Reply();
		// TODO: this fails, cose Enguage.e is not initialised!!!
		Allopoiesis e = new Allopoiesis( Intention.allop, "ask answering yes or no , is it safe" );
		//Repertoire.load( "need" );
		e.mediate( r );
}	}
