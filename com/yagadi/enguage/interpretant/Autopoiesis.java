package com.yagadi.enguage.interpretant;

import java.util.ArrayList;
import java.util.Iterator;

import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Strings;
import com.yagadi.enguage.vehicle.Context;
import com.yagadi.enguage.vehicle.Reply;

public class Autopoiesis extends Intention {
	private static       Audit   audit = new Audit( "Autopoiesis" );

	public static final String NAME    = Intention.AUTOP;
	
	public static final Sign[] written = {
		// 3 x 3 signs (think/do/say * start/subseq/infelicit) + 1 "finally"
		new Sign( "On ", new Quote( "x" ), ",", new Phrase( "y" ))
			.append( new Intention( create, Intention.THINK +" X Y" )),
			
		new Sign( "On ",new Quote( "x" ), ", perform ", new Quote( "y" ))
			.append( new Intention( create, Intention.DO +" X Y" )),
			
		new Sign( "On ", new Quote( "x" ), ", reply ", new Quote( "y" ))
			.append( new Intention( create, Intention.REPLY +" X Y" )),
			
		new Sign( "Then on ", new Quote( "x" ), ", ", new Phrase( "y" ))
			.append( new Intention( append, Intention.THINK +" Y" )),
			
		new Sign( "Then  on ", new Quote( "x" ), ", perform ",  new Quote( "y" ))
			.append( new Intention( append, Intention.DO +" Y" )),
			
		new Sign( "Then  on ", new Quote( "x" ), ", reply   ", new Quote("y" ))
			.append( new Intention( append, Intention.REPLY+" Y" )),
			
		new Sign( "Then on ",  new Quote( "x" ), ", if not, ",  new Phrase( "y" ))
			.append( new Intention( append, Intention.ELSE_THINK +" Y" )),
			
		new Sign( "Then  on ", new Quote( "x" ), ", if not, perform ", new Quote( "y" ))
			.append( new Intention( append, Intention.ELSE_DO +" Y" )),
					
		new Sign( "Then on ",  new Quote( "x" ), ", if not, reply ", new Quote( "y" ))
			.append( new Intention( append, Intention.ELSE_REPLY +" Y")),
			
		new Sign( " Finally on ", new Quote( "x" ), ",   perform ", new Quote( "y" ))
			.append( new Intention( append, Intention.FINALLY+" Y" )),
			
		//	Added 3 new signs for the running of applications external to enguage...
		new Sign( "On ", new Quote( "x" ), ", run ", new Quote( "y" ))
			.append( new Intention( create, Intention.RUN +" X Y" )),
		
		new Sign( "Then  on ",new Quote( "x" ), ", run ", new Quote( "y" ))
			.append( new Intention( append, Intention.RUN +" Y" )),

		new Sign( "Then  on ", new Quote( "x" ), ", if not, run ", new Quote( "y" ))
			.append( new Intention( append, Intention.ELSE_RUN +" Y" ))
	};
	public static final Sign[] spoken = {
//		On "interpret PHRASE-X thus":
//			set induction to true;
//			perform "sign create X";
//			then, reply "go on".
		new Sign( "interpret", new Phrase( "x" ), "thus" )
			.appendIntention( Intention.thenThink, "set induction to true" )
			.appendIntention( Intention.thenDo,    "sign create X")
			.appendIntention( Intention.thenReply, "go on" ),
		
		new Sign( "interpret", new Phrase( "x" ), "like this" )
			.appendIntention( Intention.thenThink, "set induction to true" )
			.appendIntention( Intention.thenDo,    "sign create X")
			.appendIntention( Intention.thenReply, "go on" ),
		
//		On "ok":
//			set induction to false;
//			get the value of finalReply;
//			if not, reply "ok";
//			perform "sign reply ...";
//			unset the value of finalReply;
//			then, reply "ok".
		new Sign( "that concludes interpretation" )
			.appendIntention( Intention.thenThink, "set induction to false" )
			.appendIntention( Intention.thenThink, "get the value of finalReply" )
			.appendIntention( Intention.elseReply, "ok" )
			.appendIntention( Intention.thenDo,    "sign reply ..." )
			.appendIntention( Intention.thenThink, "unset the value of finalReply" )
			.appendIntention( Intention.thenReply, "ok" ),
			
//		On "that is it", ok.
		new Sign( "ok" )
			.appendIntention( Intention.thenThink, "that concludes interpretation" ),
				
//		On "that is it", ok.
		new Sign( "that is it" )
			.appendIntention( Intention.thenThink, "that concludes interpretation" ),
					
//		On "that is all", ok.
		new Sign( "that is all" )
			.appendIntention( Intention.thenThink, "that concludes interpretation" ),
			
				
			
// THINK...
//		On "then PHRASE-X":
//			perform "sign think X";
//			then, reply "go on".
		new Sign( "then", new Phrase( "x" ))
			.appendIntention( Intention.thenThink, "get the value of induction" )
			.appendIntention( Intention.elseReply, "i do not understand" )
			.appendIntention( Intention.thenDo,    "sign think X" )
			.appendIntention( Intention.thenReply, "go on" ),
					
//		On "first PHRASE-X", then X.
		new Sign( "first", new Phrase( "x" ))
			.appendIntention( Intention.thenThink, "get the value of induction" )
			.appendIntention( Intention.elseReply, "i do not understand" )
			.appendIntention( Intention.thenThink, "then X" )
			.appendIntention( Intention.thenReply, "go on" ),
			
//		On "think PHRASE-X", then X.
		new Sign( "think", new Phrase( "x" ))
			.appendIntention( Intention.thenThink, "get the value of induction" )
			.appendIntention( Intention.elseReply, "i do not understand" )
			.appendIntention( Intention.thenThink, "then X" )
			.appendIntention( Intention.thenReply, "go on" ),
			
//		On "next  PHRASE-X", then X.
		new Sign( "next", new Phrase( "x" ))
			.appendIntention( Intention.thenThink, "get the value of induction" )
			.appendIntention( Intention.elseReply, "i do not understand" )
			.appendIntention( Intention.thenThink, "then X" )
			.appendIntention( Intention.thenReply, "go on" ),
			
//			On "then if not PHRASE-X":
//			get the value of induction;
//			if not, reply "i do not understand";
//			perform "sign else think X";
//			then, reply "go on".
		new Sign( "then if not", new Phrase( "x" ))
			.appendIntention( Intention.thenThink, "get the value of induction" )
			.appendIntention( Intention.elseReply, "i do not understand" )
			.appendIntention( Intention.thenDo  ,  "sign else think X" )
			.appendIntention( Intention.thenReply, "go on" ),
			
			
// ...DO
//		On "then perform PHRASE-X":
//			perform "sign perform X";
//			then, reply "go on".
		new Sign( "then perform", new Phrase( "x" ))
			.appendIntention( Intention.thenThink, "get the value of induction" )
			.appendIntention( Intention.elseReply, "i do not understand" )
			.appendIntention( Intention.thenDo,    "sign perform X" )
			.appendIntention( Intention.thenReply, "go on" ),
				
//		On "first perform PHRASE-X", then perform X.
		new Sign( "first perform", new Phrase( "x" ))
			.appendIntention( Intention.thenThink, "get the value of induction" )
			.appendIntention( Intention.elseReply, "i do not understand" )
			.appendIntention( Intention.thenDo,    "sign perform X" )
			.appendIntention( Intention.thenReply, "go on" ),
			
//		On "next perform PHRASE-X", then perform X.
		new Sign( "next perform", new Phrase( "x" ))
			.appendIntention( Intention.thenThink, "get the value of induction" )
			.appendIntention( Intention.elseReply, "i do not understand" )
			.appendIntention( Intention.thenDo,    "sign perform X" )
			.appendIntention( Intention.thenReply, "go on" ),

//			On "then if not perform PHRASE-X":
//			get the value of induction;
//			if not, reply "i do not understand";
//			perform "sign else perform X";
//			then, reply "go on".
		new Sign( "then if not perform", new Phrase( "x" ))
			.appendIntention( Intention.thenThink, "get the value of induction" )
			.appendIntention( Intention.elseReply, "i do not understand" )
			.appendIntention( Intention.thenDo,    "sign else perform X" )
			.appendIntention( Intention.thenReply, "go on" ),
						
			
// ...SAY
//		On "just reply PHRASE-X", then reply X. -- translation!
		new Sign( "just reply", new Phrase( "x" ))
			.appendIntention( Intention.thenThink, "get the value of induction" )
			.appendIntention( Intention.elseReply, "i do not understand" )
			.appendIntention( Intention.thenDo,    "sign reply X" )
			.appendIntention( Intention.thenReply, "go on" ),
			
//		On "then reply PHRASE-X":
//			perform "sign reply X";
//			then, reply "go on".
		new Sign( "then reply", new Phrase( "x" ))
			.appendIntention( Intention.thenThink, "get the value of induction" )
			.appendIntention( Intention.elseReply, "i do not understand" )
			.appendIntention( Intention.thenDo,    "sign reply X" )
			.appendIntention( Intention.thenReply, "go on" ),
				
//			On "then if not reply PHRASE-X":
//			get the value of induction;
//			if not, reply "i do not understand";
//			perform "sign else reply X";
//			then, reply "go on".
		new Sign( "then if not reply", new Phrase( "x" ))
			.appendIntention( Intention.thenThink, "get the value of induction" )
			.appendIntention( Intention.elseReply, "i do not understand" )
			.appendIntention( Intention.thenDo,    "sign else reply X" )
			.appendIntention( Intention.thenReply, "go on" ),

//		On "then whatever reply PHRASE-X", then reply X.
		new Sign( "then whatever reply", new Phrase( "x" ))
			.appendIntention( Intention.thenThink, "get the value of induction" )
			.appendIntention( Intention.elseReply, "i do not understand" )
			.appendIntention( Intention.thenDo,    "sign else reply X" )
			.appendIntention( Intention.thenDo,    "sign reply X" )
			.appendIntention( Intention.thenThink, "that concludes interpretation" ),

			
//			On "finally PHRASE-X":
//			perform "sign finally X";
//			then, reply "ok".
		new Sign( "finally", new Phrase( "x" ))
			.appendIntention( Intention.thenDo,    "sign finally X" )
			.appendIntention( Intention.thenReply, "go on" ),

//		On "this implies PHRASE-B":
//			perform "sign imply B ";
//			then, reply "go on".
		new Sign( "this implies", new Phrase( "b" ))
			.appendIntention( Intention.thenThink, "get the value of induction" )
			.appendIntention( Intention.elseReply, "i do not understand" )
			.appendIntention( Intention.thenDo,    "sign imply B" )
			.appendIntention( Intention.thenReply, "go on" ),
	};

	public Autopoiesis( Intention base, boolean temp, boolean spatial ) { super( base, temp, spatial ); }	
	public Autopoiesis( int type, String value, int intnt ) { super( type, value ); intent = intnt;}	
	
	private int intent = undef;
	public  int intent() { return intent;}
	
	static private Sign s = null;
	static public  void printSign() { audit.LOG( "Autop().printSign:\n"+ s.pattern().toXml()); }
	
	static private String concept = "";
	static public    void concept( String name ) { concept = name; }
	static public  String concept() { return concept; }
	
	public Reply mediate( Reply r ) {
		audit.in( "mediate", "NAME="+ NAME +", value="+ value +", "+ Context.valueOf());
		Strings sa = Context.deref( new Strings( value ));
		
		// needs to switch on type (intent)
		if (intent == create ) { // manually adding a sign
			
			//audit.debug( "autop: creating new sign: ["+ value +"]");
			Repertoire.signs.insert(
				s = new Sign()
					.pattern( new Pattern( value )) // manual Pattern
					.concept( Repertoire.AUTOPOIETIC )
			);
			
		} else if (intent == append ) { // add intent to end of interpretant
			if (null != s) s.append( new Intention( type, Pattern.toPattern( value ).toString()));
			
		} else if (intent == prepend ) { // add intent to start of interpretant
			if (null != s) s.prepend( new Intention( type, Pattern.toPattern( value ).toString() ));
			
		} else if (intent == headAppend ) { // add intent to first but one...  
			if (null != s) s.insert( 1, new Intention( type, Pattern.toPattern( value ).toString() ));
			
		// following these are trad. autopoiesis...this need updating as above!!!
		} else if (type == append || type == prepend ) {
			if (null == s)
				// this should return DNU...
				audit.ERROR( "adding to non existent concept: ["+ sa.toString( Strings.CSV )+"]");
			else {
				String attr = sa.get( 0 ),
					    val = Strings.trim( sa.get( 1 ), '"' );
				if (type == append )
					s.append( new Intention( nameToType(  attr ), val ));
				else
					s.prepend( new Intention( nameToType( attr ), val ));
			}
			
		} else if (type == create ) { // autopoeisis?
			String attr    = sa.get( 0 ),
			       pattern = sa.get( 1 ),
			       val     = Strings.trim( sa.get( 2 ), '"' );
			/* TODO: need to differentiate between
			 * "X is X" and "X is Y" -- same shape, different usage.
			 * At least need to avoid this (spot when "X is X" happens)
			 */
			audit.debug( "Adding >"+ value +"< ["+ sa.toString( Strings.CSV )+"]");
			if ( pattern.equals( "help" ))
				s.help( val ); // add: help="text" to cached sign
			else // create then add a new cached sign into the list of signs
				Repertoire.signs.insert(
					s = new Sign()
						.pattern( new Pattern( new Strings( Strings.trim( pattern, '"' ))) )
						.concept( concept() )
						.append( new Intention( Intention.nameToType( attr ), val )));
		}
		return (Reply) audit.out( r.answer( Reply.yes().toString() ));
	}
	// ---
	public static Reply test(Reply r, ArrayList<Intention> intents) {
		Iterator<Intention> ins = intents.iterator();
		while (!r.isDone() && ins.hasNext()) {
			Intention in = ins.next();
			audit.log( typeToString( in.type )  +"='"+ in.value +"'" );
			r = new Autopoiesis( in, false, false ).mediate( r );
		}
		return r;
	}
	public static void main( String args[]) {
		//Audit.allOn();
		//audit.trace( true );
		
		audit.title( "trad autopoiesis... add to a list and then add that list" );
		Reply r = new Reply();
		ArrayList<Intention> a = new ArrayList<Intention>();
		a.add( new Intention( create,    THINK      +" \"a PATTERN z\" \"one two three four\""   ));
		a.add( new Intention( append, ELSE_REPLY +" \"two three four\""   ));
		a.add( new Intention( append, REPLY      +" \"three four\"" ));
		test( r, a );
		audit.log( Repertoire.signs.toString() );
		audit.log( r.toString());
		
		audit.title( "manual sign creation... add each intention individually" );
		r = new Reply();
		r = new Autopoiesis( create,    "b variable pattern z", create ).mediate( r );
		r = new Autopoiesis( thenThink, "one two three four"  , append ).mediate( r );
		r = new Autopoiesis( elseReply, "two three four"      , append ).mediate( r );
		r = new Autopoiesis( thenReply, "three four"          , append ).mediate( r );
		audit.log( Repertoire.signs.toString() );
		audit.log( r.toString());
		
		
		audit.title( "sign self-build II... add pairs of attributes" );
		// now built like this...
		// To PATTERN reply TYPICAL REPLY
		r = new Reply();
		s = new Sign()
				.pattern( new Pattern( "c variable pattern z" ))
				.concept( concept() );
		String reply = "three four";
		s.append( new Intention( thenReply, reply ));
		// ...This implies COND
		s.prepend( new Intention( thenThink, "one two three four" ));
		// ...if not reply EXECP REPLY
		s.insert( 1, new Intention( elseReply, "two three four" ));
		
		Repertoire.signs.insert( s );
		r.answer( Reply.yes().toString() );

		
		audit.log( Repertoire.signs.toString() );
		audit.log( r.toString());
}	}