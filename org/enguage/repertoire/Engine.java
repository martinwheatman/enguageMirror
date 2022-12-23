package org.enguage.repertoire;

import java.util.ListIterator;
import java.util.Locale;
import java.util.TreeSet;

import org.enguage.Enguage;
import org.enguage.repertoire.concept.Autoload;
import org.enguage.repertoire.concept.Load;
import org.enguage.repertoire.concept.Names;
import org.enguage.signs.Sign;
import org.enguage.signs.interpretant.Intention;
import org.enguage.signs.interpretant.Redo;
import org.enguage.signs.objects.Variable;
import org.enguage.signs.objects.list.Item;
import org.enguage.signs.objects.space.Overlay;
import org.enguage.signs.symbol.Utterance;
import org.enguage.signs.symbol.config.Englishisms;
import org.enguage.signs.symbol.pattern.Frag;
import org.enguage.signs.symbol.pattern.Frags;
import org.enguage.signs.symbol.reply.Reply;
import org.enguage.signs.symbol.reply.Response;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Context;
import org.enguage.util.sys.Server;

import opt.test.Example;

public final class Engine {
	
	private Engine() {}
	
	public    static final String NAME = Repertoire.ALLOP;
	private   static Audit audit = new Audit( NAME );
	
	protected static final Sign[] commands = {
			/* These could be accompanied in a repertoire, but they have special 
			 * interpretations and so are built here alongside those interpretations.
			 */
   			new Sign()
					.pattern( new Frag( "entitle", "SAID" ).phrasedIs())
					.appendIntention( Intention.allop, "entitle SAID" )
					.concept( NAME ),
   			new Sign()
					.pattern( new Frag( "subtitle", "SAID" ).phrasedIs())
					.appendIntention( Intention.allop, "subtitle SAID" )
					.concept( NAME ),
   			new Sign()
					.pattern( new Frag( "subtitle", "SAID" ).phrasedIs())
					.appendIntention( Intention.allop, "subtitle SAID" )
					.concept( NAME ),
   			new Sign()
					.pattern( new Frag( "run a self test", "" ))
					.appendIntention( Intention.allop, "selfTest" )
					.concept( NAME ),
			new Sign()
					.pattern( new Frag( "this is all imagined", "" ))
					.appendIntention( Intention.allop, "imagined" )
					.concept( NAME ),
			new Sign()
					.pattern( new Frag( "ok" ))
					.appendIntention( Intention.allop, "ok" )
					.concept( NAME ), 
			new Sign()
					.pattern( new Frag( "list repertoires","" ))
					.appendIntention( Intention.allop, "list" )
					.concept( NAME ),
			new Sign()
					.pattern( new Frag(         "help", "" ))
					.appendIntention( Intention.allop, "help" )
			  		.concept( NAME ),
			new Sign()
					.pattern( new Frag(         "hello", "" ))
					.appendIntention( Intention.allop, "hello")
			  		.concept( NAME ),
			new Sign()
					.pattern( new Frag(         "say", "SAID" ).phrasedIs() /*.quotedIs()*/ )
					.appendIntention( Intention.allop, "say SAID")
			  		.concept( NAME ),
			new Sign()
					.pattern( new Frag( "what can i say", "" ))
					.appendIntention( Intention.allop, "repertoire"  )
		          	.concept( NAME ),
			new Sign()
					.pattern( new Frag(   "load ", "NAME" ))
					.appendIntention( Intention.allop,    "load NAME" )
			  		.concept( NAME ),
			new Sign()
					.pattern( "unload ", "NAME" )
					.appendIntention( Intention.allop, "unload NAME" )
					.concept( NAME ),
			new Sign()
					.pattern( new Frag( "save spoken concepts as ", "NAME", "" ).phrasedIs())
					.appendIntention( Intention.allop, "saveAs NAME" )
					.concept( NAME ),										 		
			new Sign()
					.pattern( new Frag( "delete spoken concept ", "NAME", "" ).phrasedIs())
					.appendIntention( Intention.allop, "delete NAME" )
					.concept( NAME ),
																 		
			new Sign().pattern( new Frag(     "say again",  "" )).appendIntention( Intention.allop, "repeat"       ),
			new Sign().pattern( new Frag(        "spell ", "x" )).appendIntention( Intention.allop, "spell X"      ),
			new Sign().pattern( new Frag(   "enable undo",  "" )).appendIntention( Intention.allop, "undo enable"  ),
			new Sign().pattern( new Frag(  "disable undo",  "" )).appendIntention( Intention.allop, "undo disable" ),
			new Sign().concept( NAME ).pattern( new Frag(          "undo",  "" )).appendIntention( Intention.allop, "undo"         ),
			new Sign().concept( NAME ).pattern( new Frag( "this is false",  "" )).appendIntention( Intention.allop, "undo" ),
			new Sign().concept( NAME ).pattern( new Frag( "this sentence is false",  "" )).appendIntention( Intention.allop, "undo" ),
			new Sign().concept( NAME ).pattern( new Frag(    "group by", "x" )).appendIntention( Intention.allop, "groupby X" ),
						
			new Sign().concept( NAME ).pattern( new Frag(  "timing  on",  "" )).appendIntention( Intention.allop, "tracing on" ),
			new Sign().concept( NAME ).pattern( new Frag(  "timing off",  "" )).appendIntention( Intention.allop, "tracing off" ),
			new Sign().concept( NAME ).pattern( new Frag( "tracing  on",  "" )).appendIntention( Intention.allop, "tracing on" ),
			new Sign().concept( NAME ).pattern( new Frag( "tracing off",  "" )).appendIntention( Intention.allop, "tracing off" ),
			new Sign().concept( NAME ).pattern( new Frag(  "detail  on",  "" )).appendIntention( Intention.allop, "detailed on" ),
			new Sign().concept( NAME ).pattern( new Frag(  "detail off",  "" )).appendIntention( Intention.allop, "detailed off" ),
			new Sign().concept( NAME )
					.pattern( new Frag( "tcpip ",  "address" ))
					.pattern( new Frag(      " ",  "port" ))
					.pattern( new Frag(      " ",  "data" ).quotedIs())
						.appendIntention( Intention.allop, "tcpip ADDRESS PORT DATA" ),
			new Sign().concept( NAME ).pattern( new Frag(          "show ", "x" ).phrasedIs())
					.appendIntention( Intention.allop, "show X" ),
			new Sign().concept( NAME ).pattern( new Frag(         "debug ", "x" ).phrasedIs())
					.appendIntention( Intention.allop, "debug X" ),
			/* 
			 * it is possible to arrive at the following construct:   think="reply 'I know'"
			 * e.g. "if X, Y", if the instance is "if already exists, reply 'I know'"
			 * here reply is thought. Should be rewritten:
			 * representamen: "if X, reply Y", then Y is just the quoted string.
			 * However, the following should deal with this situation.
			 */
			new Sign().concept( NAME ).pattern( new Frag( Intention.REPLY +" ", "x" ).quotedIs())
					.appendIntention( Intention.thenReply, "X" ),
			
			// fix to allow better reading of autopoietic  
			new Sign().concept( NAME ).pattern( new Frag( "if so, ", "x" ).phrasedIs())
					.appendIntention( Intention.thenThink, "X" ),

			new Sign().concept( NAME ).pattern( new Frag( "if i know, ", "x" ).phrasedIs())
					.appendIntention( Intention.allop, "iknow X" ),

			// for vocal description of concepts... autopoiesis!		
			new Sign().concept( NAME ).pattern( new Frag( "perform ", "args" ).phrasedIs())
					.appendIntention( Intention.thenDo, "ARGS" ),
			/* 
			 * REDO: undo and do again, or disambiguate
			 */
			new Sign().concept( NAME ).pattern( new Frag( "No ", "x" ).phrasedIs())
						.appendIntention( Intention.allop, "undo" )
						.appendIntention( Intention.elseReply, "undo is not available" )
						/* On thinking the below, if X is the same as what was said before,
						 * need to search for the appropriate sign from where we left off
						 * Dealing with ambiguity: "X", "No, /X/"
						 */
						.appendIntention( Intention.allop,  Redo.DISAMBIGUATE +" X" ) // this will set up how the inner thought, below, works
						.appendIntention( Intention.thenThink,  "X"    )
		 };
	
	public static Reply interp( Intention in, Reply r ) {
		r.answer( Response.yesStr()); // bland default reply to stop debug output look worrying
		
		Strings cmds = Context.deref( new Strings( in.value() )).normalise();
		String  cmd  = cmds.remove( 0 );

		if ( cmd.equals( "imagined" )) {
			
			Enguage.get().imagined( true );
			r.format( new Strings( "ok, this is all imagined" ));
			
		} else if ( cmd.equals( "selfTest" )) {
			
			Example.unitTests();
			r.format( new Strings( "number of tests passed was "+ audit.numberOfTests() ));
			
		} else if ( cmd.equals( "groupby" )) {
			
			r.format( Response.success());
			if (!cmds.isEmpty() && !cmds.get( 0 ).equals( "X" ))
				Item.groupOn( cmds.get( 0 ).toUpperCase( Locale.getDefault()));
			else
				r.format( new Strings( Response.failure() +", i need to know what to group by" ));
			
		} else if ( cmd.equals( "undo" )) {
			
			r.format( Response.success() );
			if (cmds.size() == 1 && cmds.get( 0 ).equals( "enable" )) 
				Redo.undoEnabledIs( true );
			else if (cmds.size() == 1 && cmds.get( 0 ).equals( "disable" )) 
				Redo.undoEnabledIs( false );
			else if (cmds.isEmpty() && Redo.undoIsEnabled()) {
				if (Overlay.number() < 2) { // if there isn't an overlay to be removed
					audit.debug( "overlay count( "+ Overlay.number() +" ) < 2" ); // audit
					r.answer( Response.noStr() );
				} else {
					audit.debug("ok - restarting transaction");
					Overlay.reStartTxn();
				}
			} else if (!Redo.undoIsEnabled())
				r.format( Response.dnu() );
			else
				r = Redo.unknownCommand( r, cmd, cmds );
			
		} else if (cmd.equals( Redo.DISAMBIGUATE )) {
			Redo.disambOn( cmds );
		
		} else if (cmd.equals( "load" )) {
			/* load is used by create, delete, ignore and restore to
			 * support their interpretation
			 */
			Strings files = cmds;
			//audit.debug( "loading "+ files.toString( Strings.CSV ));
			for(int i=0; i<files.size(); i++)
				Load.load( files.get( i ));
			 
		} else if (cmd.equals( "unload" )) {
			Strings files = cmds.copyAfter( 0 );
			for(int i=0; i<files.size(); i++)
				Autoload.unload( files.get( i ));
		/*
		 *else if (cmd.equals( "reload" )) 
		 *	Strings files = cmds.copyAfter( 0 )
		 *	for(int i=0; i<files.size(); i++) Concept.unload( files.get( i ))
		 *	for(int i=0; i<files.size(); i++) Concept.load( files.get( i ))
		 */

		} else if (cmd.equals( "saveAs" )) {
			
			String name = cmds.toString( Strings.UNDERSC );
			audit.debug( "Saving concepts as "+ name );
			Names.add( name );
			r.format(	Repertoire.signs.saveAs(
								Repertoire.AUTOPOIETIC,
								name
						) ? Response.success() : Response.failure()
					);

		} else if (cmd.equals( "delete" )) {
			 
			String concept = cmds.toString( Strings.UNDERSC );
			audit.debug( "Deleting "+ concept +" concept");
			Names.remove( concept );
			Load.delete( concept );
			Repertoire.signs.remove( concept );
			r.format( Response.success() );

		} else if (cmd.equals( "spell" )) {
			r.format( new Strings( Englishisms.spell( cmds.get( 0 ), true )));
			
		} else if (cmd.equals( "iknow" )) {
			
			String tmp = Repertoire.mediate( new Utterance( cmds )).toString();
			if (tmp.charAt( tmp.length() - 1) == '.')
				tmp = tmp.substring( 0, tmp.length() - 1 );
			r.answer( tmp );
			
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
				String ans = Server.client( host, port, msg );
				r.answer( ans );
			}
		} else if (cmd.equals( "timing" )) {
			Audit.log( cmd +" "+ cmds.toString());
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
			r.format( Response.success() );
			
		} else if (cmd.equals( "tracing" )) {
			Audit.log( cmd +" "+ cmds.toString());
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
			r.format( Response.success() );
			
		} else if (cmd.equals( "detailed" )) {
			
			Audit.log( cmds.toString());
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
			r.format( Response.success() );
			
		} else if (cmd.equals( "debug" )) {
			
			if (cmds.get( 0 ).equals( "off" )) {
				Audit.allOff();
				Audit.allTracing = false;
				Audit.timings = false;
				Audit.runtimeDebug = false;
				Audit.detailedOn = false;
				
			} else if (cmds.size() > 1 && cmds.get( 1 ).equals( "tags" )) {
				Frags.debug( !Frags.debug() );
				
			} else {
				Audit.allOn();
				Audit.runtimeDebug = true;
			}
			r.format( Response.success() );
			
			
		} else if (cmd.equals( "show" )) {
			
			if (1==cmds.size()) {
				String option = cmds.get( 0 );
				if (option.equals( "auto" )) {
					Repertoire.autop.show();
					r.format( Response.success() );
				} else if (   option.equals( "sign" )
				           || option.equals( "user" )) {
					Repertoire.signs.show();
					r.format( Response.success() );
				} else if (cmds.get( 0 ).equals( Repertoire.AUTOPOIETIC )) {
					Repertoire.signs.show(Repertoire.AUTOPOIETIC);
					r.format( Response.success() );
				} else if (option.equals( Repertoire.ALLOP )) {
					Repertoire.allop.show();
					r.format( Response.success() );
				} else if (option.equals( "all" )) {
					Repertoire.autop.show();
					Repertoire.allop.show();
					Repertoire.signs.show();
					r.format( Response.success() );
				} else if (option.equals( "variable" )) {
					Variable.interpret( new Strings( "show" ));
					r.format( Response.success());
				} else
					audit.ERROR( "option: "+ option +" doesn't match anything" );
			} else {
				Repertoire.signs.show();
				r.format( Response.success() );
			}


		} else if ( in.value().equals( "repeat" )) {
			if (Reply.previous() == null) {
				Audit.log("Allop:repeating dnu");
				r.format( Response.dnu());
			} else {
				Audit.log("Allop:repeating: "+ Reply.previous());
				r.repeated( true );
				r.format( new Strings( Reply.repeatFormat()));
				r.answer( Reply.previous().toString());
			}
			
		} else if (cmd.equals( "entitle" )) {
			ListIterator<String> li = cmds.listIterator();
			while (li.hasNext()) {
				String s = li.next();
				li.set( s.toUpperCase());
			}	
			audit.title( cmds.toString() );

		} else if (cmd.equals( "subtitle" )) {
			ListIterator<String> li = cmds.listIterator();
			while (li.hasNext()) {
				String s = li.next();
				li.set( s.toUpperCase());
			}	
			audit.subtl( cmds.toString() );

		} else if (cmd.equals( "echo" )) {
			ListIterator<String> li = cmds.listIterator();
			while (li.hasNext()) {
				String s = li.next();
				li.set( s.toUpperCase());
			}	
			Audit.log( cmds.toString() );

		} else if (cmd.equals( "say" )) {
			// 'say' IS: 'say "what";' OR: 'say egress is back to the wheel;'
			// so we need to trim the quoted speech...
			if (cmds.size() == 1)
				Reply.say( Variable.deref(
						new Strings( Strings.trim( cmds.get( 0 ), Strings.DOUBLE_QUOTE ))
				 )				 );
			else
				Reply.say( Variable.deref( new Strings( cmds )));

		} else if ( cmd.equals( "list" )) {
			//Strings reps = Enguage.e.signs.toIdList()
			/* This becomes less important as the interesting stuff becomes auto loaded 
			 * Don't want to list all repertoires once the repertoire base begins to grow?
			 * May want to ask "is there a repertoire for needs" ?
			 */
			r.format( new Strings( "loaded repertoires include "+ new Strings( (TreeSet<String>)Load.loaded()).toString( Reply.andListFormat() )));
			
		} else if ( cmd.equals( "ok" ) && cmds.isEmpty()) {

			r.format( // think( "that concludes interprtation" )
				new Variable( "transformation" ).isSet( "true" ) ?
						Enguage.get().mediate( new Strings( "that concludes interpretation" )).toString()
						: "ok"
			);

		} else {
			
			r = Redo.unknownCommand( r, cmd, cmds );
		}
		return r;
}	}
