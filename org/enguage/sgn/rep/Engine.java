package org.enguage.sgn.rep;

import java.util.Locale;

import org.enguage.Enguage;
import org.enguage.obj.Variable;
import org.enguage.obj.list.Item;
import org.enguage.sgn.Sign;
import org.enguage.sgn.Signs;
import org.enguage.sgn.ctx.Context;
import org.enguage.sgn.itn.Intention;
import org.enguage.sgn.itn.Redo;
import org.enguage.sgn.ptt.Pattern;
import org.enguage.sgn.ptt.Patternette;
import org.enguage.util.Audit;
import org.enguage.util.Net;
import org.enguage.util.Strings;
import org.enguage.veh.Language;
import org.enguage.veh.Question;
import org.enguage.veh.reply.Reply;

public class Engine {
	
	public  static final String NAME = Repertoire.ALLOP;
	private static Audit audit = new Audit( NAME );
	
	public static final Sign commands[] = {
			/* These could be accompanied in a repertoire, but they have special 
			 * interpretations and so are built here alongside those interpretations.
			 */
			new Sign()
				.pattern( new Patternette( "remove the primed answer ", "" ))
	          		.appendIntention( Intention.allop, "removePrimedAnswer" )
	          		.concept( NAME ),
		          	
	    	new Sign()
				.pattern( new Patternette( "prime the answer ", "answer" ).phrasedIs())
		          	.appendIntention( Intention.allop, "primeAnswer ANSWER" )
					.appendIntention( Intention.thenReply, "ok, the next answer will be ANSWER" )
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
					.help( "" ),
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
					 .help( "" ),
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
			new Sign().concept( NAME ).pattern( new Patternette( "group by",  "x" )).appendIntention( Intention.allop, "groupby X" ),
						
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
			new Sign().concept( NAME ).pattern( new Patternette( Intention.REPLY +" ", "x" ).quotedIs())
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
						.appendIntention( Intention.allop,  Redo.DISAMBIGUATE +" X" ) // this will set up how the inner thought, below, works
						.appendIntention( Intention.thenThink,  "X"    )
		 };
	static public Reply getReply( Intention in, Reply r ) {
		r.answer( Reply.yes()); // bland default reply to stop debug output look worrying
		
		Strings cmds = Context.deref( new Strings( in.value() ));
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
			
		} else if ( cmd.equals( "groupby" )) {
			
			r.format( Reply.success());
			if (cmds.size() > 0 && !cmds.get( 0 ).equals( "X" ))
				Item.groupOn( cmds.get( 0 ).toUpperCase( Locale.getDefault()));
			else
				r.format( Reply.failure() +", i need to know what to group by" );
			
		} else if ( cmd.equals( "undo" )) {
			Enguage e = Enguage.get();
			r.format( Reply.success() );
			if (cmds.size() == 1 && cmds.get( 0 ).equals( "enable" )) 
				Redo.undoEnabledIs( true );
			else if (cmds.size() == 1 && cmds.get( 0 ).equals( "disable" )) 
				Redo.undoEnabledIs( false );
			else if (cmds.size() == 0 && Redo.undoIsEnabled()) {
				if (e.o.count() < 2) { // if there isn't an overlay to be removed
					audit.debug( "overlay count( "+ e.o.count() +" ) < 2" ); // audit
					r.answer( Reply.no() );
				} else {
					audit.debug("ok - restarting transaction");
					e.o.reStartTxn();
				}
			} else if (!Redo.undoIsEnabled())
				r.format( Reply.dnu() );
			else
				r = Redo.unknownCommand( r, cmd, cmds );
			
		} else if (cmd.equals( Redo.DISAMBIGUATE )) {
			Redo.disambOn( cmds );
		
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
			
			if (cmds.size() != 3)
				audit.ERROR( "tcpip command without 3 parameters: "+ cmds );
			else {
				String host    = cmds.remove( 0 ),
				       portStr = cmds.remove( 0 ),
				       msg     = cmds.remove( 0 );
				String prefix  = Variable.get( "XMLPRE", "" ),
				       suffix  = Variable.get( "XMLPOST", "" );
				
				int port = -1;
				try {
					port = Integer.valueOf( portStr );
				} catch (Exception e1) {
					try {
						port = Integer.valueOf( Variable.get( "PORT" ));
					} catch (Exception e2) {
						port = 0;
				}	}
			
				msg = prefix + Variable.derefUc( Strings.trim( msg , Strings.DOUBLE_QUOTE )) + suffix;
				String ans = Net.client( host, port, msg );
				r.answer( ans );
			}
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
				
			} else if (cmds.size() > 1 && cmds.get( 1 ).equals( "tags" )) {
				Pattern.debug( !Pattern.debug() );
				
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


		} else if ( in.value().equals( "repeat" )) {
			if (Reply.previous() == null) {
				audit.log("Allop:repeating dnu");
				r.format( Reply.dnu());
			} else {
				audit.log("Allop:repeating: "+ Reply.previous());
				r.repeated( true );
				r.format( Reply.repeatFormat());
				r.answer( Reply.previous());
			}
			
		} else if (cmd.equals( "help" )) {
			Redo.helped( true );
			r.format( Repertoire.allop.helpedToString( Repertoire.ALLOP ));

		} else if (cmd.equals( "welcome" )) {
			Redo.helped( true );
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
			
			r = Redo.unknownCommand( r, cmd, cmds );
		}
		return r;
}	}
