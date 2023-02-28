package org.enguage.repertoires;

import java.util.Locale;

import org.enguage.Enguage;
import org.enguage.signs.Sign;
import org.enguage.signs.interpretant.Intention;
import org.enguage.signs.interpretant.Redo;
import org.enguage.signs.objects.Variable;
import org.enguage.signs.objects.list.Item;
import org.enguage.signs.objects.space.Overlay;
import org.enguage.signs.symbol.Utterance;
import org.enguage.signs.symbol.config.Englishisms;
import org.enguage.signs.symbol.reply.Reply;
import org.enguage.signs.symbol.reply.Response;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Context;
import org.enguage.util.sys.Server;

import opt.test.Example;

public final class Engine {
	
	private Engine() {}
	
	public  static final String NAME = Repertoires.ENGINE;
	private static final Audit audit = new Audit( NAME );
	
	protected static final Sign[] commands = {
			/* These could be accompanied in a repertoire, but they have special 
			 * interpretations and so are built here alongside those interpretations.
			 */
   			new Sign()
					.pattern( "entitle PHRASE-SAID" )
					.append( Intention.allop, "entitle SAID" )
					.concept( NAME ),
   			new Sign()
					.pattern( "subtitle PHRASE-SAID" )
					.append( Intention.allop, "subtitle SAID" )
					.concept( NAME ),
   			new Sign()
					.pattern( "echo PHRASE-SAID" )
					.append( Intention.allop, "echo SAID" )
					.concept( NAME ),
   			new Sign()
					.pattern( "run a self test" )
					.append( Intention.allop, "selfTest" )
					.concept( NAME ),
			new Sign()
					.pattern( "this is all imagined" )
					.append( Intention.allop, "imagined" )
					.concept( NAME ),
			new Sign()
					.pattern(  "ok" )
					.append( Intention.allop, "ok" )
					.concept( NAME ), 
			new Sign()
					.pattern( "list repertoires","" )
					.append( Intention.allop, "list" )
					.concept( NAME ),
			new Sign()
					.pattern( "help", "" )
					.append( Intention.allop, "help" )
			  		.concept( NAME ),
			new Sign()
					.pattern( "say PHRASE-SAID" )
					.append( Intention.allop, "say SAID")
			  		.concept( NAME ),
			new Sign()
					.pattern( "what can i say" )
					.append( Intention.allop, "repertoire"  )
		          	.concept( NAME ),														 		
			new Sign()
					.pattern( "say again" )
					.append( Intention.allop, "repeat"       )
			  		.concept( NAME ),
			new Sign()
					.pattern( "spell X" )
					.append( Intention.allop, "spell X"      )
			  		.concept( NAME ),
			new Sign()
					.pattern( "enable undo" )
					.append( Intention.allop, "undo enable"  )
			  		.concept( NAME ),
			new Sign()
					.pattern( "disable undo" )
					.append( Intention.allop, "undo disable" )
			  		.concept( NAME ),
			new Sign()
					.pattern( "undo" )
					.append( Intention.allop, "undo"         )
			  		.concept( NAME ),
			new Sign()
					.pattern( "this is false" )
					.append( Intention.allop, "undo" )
			  		.concept( NAME ),
			new Sign()
					.pattern( "this sentence is false" )
					.append( Intention.allop, "undo" )
			  		.concept( NAME ),
			new Sign()
					.pattern( "group by X" )
					.append( Intention.allop, "groupby X" )
			  		.concept( NAME ),
			new Sign()
					.pattern( "tcpip ADDRESS PORT QUOTED-DATA" )
					.append( Intention.allop, "tcpip ADDRESS PORT DATA" )
			  		.concept( NAME ),
			/* 
			 * it is possible to arrive at the following construct:   think="reply 'I know'"
			 * e.g. "if X, Y", if the instance is "if already exists, reply 'I know'"
			 * here reply is thought. Should be rewritten:
			 * representamen: "if X, reply Y", then Y is just the quoted string.
			 * However, the following should deal with this situation.
			 */
			new Sign()
					.pattern( "reply PHRASE-X" )
					.append( Intention.thenReply, "X" )
					.concept( NAME ),
					
//			new Sign() // fix to allow better reading of autopoietic  
//					.pattern( new Frag( "if so,", "x" ).phrasedIs())
//					.appendIntention( Intention.thenThink, "X" )
//					.concept( NAME ),
					
			new Sign() // for vocal description of concepts... autopoiesis!
					.pattern( "perform PHRASE-ARGS" )
					.append( Intention.thenDo, "ARGS" )
					.concept( NAME ),
			/* 
			 * REDO: undo and do again, or disambiguate
			 */
			new Sign().pattern( "No PHRASE-X" )
					.append( Intention.allop, "undo" )
					.append( Intention.elseReply, "undo is not available" )
					/* On thinking the below, if X is the same as what was said before,
					 * need to search for the appropriate sign from where we left off
					 * Dealing with ambiguity: "X", "No, /X/"
					 */
					.append( Intention.allop,  Redo.DISAMBIGUATE +" X" ) // this will set up how the inner thought, below, works
					.append( Intention.thenThink,  "X"    )
					.concept( NAME )
		 };
	
	public static Reply interp( Intention in, Reply r ) {
		r.answer( Response.yesStr()); // bland default reply to stop debug output look worrying
		
		Strings cmds = Context.deref( new Strings( in.value() )).normalise();
		String  cmd  = cmds.remove( 0 );

		if (cmd.equals( "imagined" )) {
			Enguage.get().imagined( true );
			r.format( new Strings( "ok, this is all imagined" ));
			
		} else if (cmd.equals( "selfTest" )) {
			Example.unitTests();
			r.format( new Strings( "number of tests passed was "+ audit.numberOfTests() ));
			
		} else if (cmd.equals( "groupby" )) {
			r.format( Response.success());
			if (!cmds.isEmpty() && !cmds.get( 0 ).equals( "X" ))
				Item.groupOn( cmds.get( 0 ).toUpperCase( Locale.getDefault()));
			else
				r.format( new Strings( Response.failure() +", i need to know what to group by" ));
			
		} else if (cmd.equals( "undo" )) {
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
		
		} else if (cmd.equals( "spell" )) {
			r.format( new Strings( Englishisms.spell( cmds.get( 0 ), true )));
			
		} else if (cmd.equals( "iknow" )) {
			String tmp = Repertoires.mediate( new Utterance( cmds )).toString();
			if (tmp.charAt( tmp.length() - 1) == '.')
				tmp = tmp.substring( 0, tmp.length() - 1 );
			r.answer( tmp );
			
		} else if (cmd.equals( "tcpip" )) {
			if (cmds.size() != 3)
				audit.error( "tcpip command without 3 parameters: "+ cmds );
			else {
				String host    = cmds.remove( 0 ),
				       portStr = cmds.remove( 0 ),
				       msg     = cmds.remove( 0 ),
				       prefix  = Variable.get( "XMLPRE", "" ),
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
			cmds.toUpperCase();
			audit.title( cmds.toString() );

		} else if (cmd.equals( "subtitle" )) {
			cmds.toUpperCase();
			audit.subtl( cmds.toString() );

		} else if (cmd.equals( "echo" )) {
			audit.subtl( cmds.toString() );

		} else if (cmd.equals( "say" )) {
			// 'say' IS: 'say "what";' OR: 'say egress is back to the wheel;'
			// so we need to trim the quoted speech...
			if (cmds.size() == 1)
				Reply.say( Variable.deref(
						new Strings( Strings.trim( cmds.get( 0 ), Strings.DOUBLE_QUOTE ))
						 )				 );
			else
				Reply.say( Variable.deref( new Strings( cmds )));
			
		} else if (cmd.equals( "ok" ) && cmds.isEmpty()) {
			r.format( // think( "that concludes interpretation" )
				new Variable( "transformation" ).isSet( "true" ) ?
						Enguage.get().mediate( new Strings( "that concludes interpretation" )).toString()
						: "ok"
			);

		} else {
			r = Redo.unknownCommand( r, cmd, cmds );
		}
		return r;
}	}
