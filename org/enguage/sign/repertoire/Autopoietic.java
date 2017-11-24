package org.enguage.sign.repertoire;

import org.enguage.sign.Sign;
import org.enguage.sign.intention.Intention;
import org.enguage.sign.pattern.Phrase;
import org.enguage.sign.pattern.Quote;

public class Autopoietic {

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
				.appendIntention( Intention.thenThink, "get the value of finalReply" ) // only set on toXreply
				.appendIntention( Intention.thenDo,    "sign reply ..." ) // induction must be true!
				.appendIntention( Intention.thenThink, "unset the value of finalReply" )
				.appendIntention( Intention.elseDo,    "variable set induction false" )     // after sign reply...
				.appendIntention( Intention.thenDo,    "variable set induction false" )     // after sign reply...
				.appendIntention( Intention.elseReply, "ok" )
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
				.appendIntention( Intention.thenDo,    "sign think X" )
				.appendIntention( Intention.thenReply, "go on" ),
						
	//		On "first PHRASE-X", then X.
			new Sign( "first", new Phrase( "x" ))
				.appendIntention( Intention.thenThink, "then X" )
				.appendIntention( Intention.thenReply, "go on" ),
				
	//		On "think PHRASE-X", then X.
			new Sign( "think", new Phrase( "x" ))
				.appendIntention( Intention.thenThink, "then X" )
				.appendIntention( Intention.thenReply, "go on" ),
				
	//		On "next  PHRASE-X", then X.
			new Sign( "next", new Phrase( "x" ))
				.appendIntention( Intention.thenThink, "then X" )
				.appendIntention( Intention.thenReply, "go on" ),
				
	//			On "then if not PHRASE-X":
	//			get the value of induction;
	//			if not, reply "i do not understand";
	//			perform "sign else think X";
	//			then, reply "go on".
			new Sign( "then if not", new Phrase( "x" ))
				.appendIntention( Intention.thenDo  ,  "sign else think X" )
				.appendIntention( Intention.thenReply, "go on" ),
				
				
	// ...DO
	//		On "then perform PHRASE-X":
	//			perform "sign perform X";
	//			then, reply "go on".
			new Sign( "then perform", new Phrase( "x" ))
				.appendIntention( Intention.thenDo,    "sign perform X" )
				.appendIntention( Intention.thenReply, "go on" ),
					
	//		On "first perform PHRASE-X", then perform X.
			new Sign( "first perform", new Phrase( "x" ))
				.appendIntention( Intention.thenDo,    "sign perform X" )
				.appendIntention( Intention.thenReply, "go on" ),
				
	//		On "next perform PHRASE-X", then perform X.
			new Sign( "next perform", new Phrase( "x" ))
				.appendIntention( Intention.thenDo,    "sign perform X" )
				.appendIntention( Intention.thenReply, "go on" ),
	
	//			On "then if not perform PHRASE-X":
	//			get the value of induction;
	//			if not, reply "i do not understand";
	//			perform "sign else perform X";
	//			then, reply "go on".
			new Sign( "then if not perform", new Phrase( "x" ))
				.appendIntention( Intention.thenDo,    "sign else perform X" )
				.appendIntention( Intention.thenReply, "go on" ),
							
				
	// ...SAY
	//		On "just reply PHRASE-X", then reply X. -- translation!
			new Sign( "just reply", new Phrase( "x" ))
				.appendIntention( Intention.thenDo,    "sign reply X" )
				.appendIntention( Intention.thenReply, "go on" ),
				
	//		On "then reply PHRASE-X":
	//			perform "sign reply X";
	//			then, reply "go on".
			new Sign( "then reply", new Phrase( "x" ))
				.appendIntention( Intention.thenDo,    "sign reply X" )
				.appendIntention( Intention.thenReply, "go on" ),
					
	//			On "then if not reply PHRASE-X":
	//			get the value of induction;
	//			if not, reply "i do not understand";
	//			perform "sign else reply X";
	//			then, reply "go on".
			new Sign( "then if not reply", new Phrase( "x" ))
				.appendIntention( Intention.thenDo,    "sign else reply X" )
				.appendIntention( Intention.thenReply, "go on" ),
	
	//		On "then whatever reply PHRASE-X", then reply X.
			new Sign( "then whatever reply", new Phrase( "x" ))
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
				.appendIntention( Intention.thenDo,    "sign imply B" )
				.appendIntention( Intention.thenReply, "go on" ),
		};
	
	
	public static final Sign[] written = {
		// 3 x 3 signs (think/do/say * start/subseq/infelicit) + 1 "finally"
		new Sign( "On ", new Quote( "x" ), ",", new Phrase( "y" ))
			.append( new Intention( Intention.create, Intention.THINK +" X Y" )),
			
		new Sign( "On ",new Quote( "x" ), ", perform ", new Quote( "y" ))
			.append( new Intention( Intention.create, Intention.DO +" X Y" )),
			
		new Sign( "On ", new Quote( "x" ), ", reply ", new Quote( "y" ))
			.append( new Intention( Intention.create, Intention.REPLY +" X Y" )),
			
		new Sign( "Then on ", new Quote( "x" ), ", ", new Phrase( "y" ))
			.append( new Intention( Intention.append, Intention.THINK +" Y" )),
			
		new Sign( "Then  on ", new Quote( "x" ), ", perform ",  new Quote( "y" ))
			.append( new Intention( Intention.append, Intention.DO +" Y" )),
			
		new Sign( "Then  on ", new Quote( "x" ), ", reply   ", new Quote("y" ))
			.append( new Intention( Intention.append, Intention.REPLY+" Y" )),
			
		new Sign( "Then on ",  new Quote( "x" ), ", if not, ",  new Phrase( "y" ))
			.append( new Intention( Intention.append, Intention.ELSE_THINK +" Y" )),
			
		new Sign( "Then  on ", new Quote( "x" ), ", if not, perform ", new Quote( "y" ))
			.append( new Intention( Intention.append, Intention.ELSE_DO +" Y" )),
					
		new Sign( "Then on ",  new Quote( "x" ), ", if not, reply ", new Quote( "y" ))
			.append( new Intention( Intention.append, Intention.ELSE_REPLY +" Y")),
			
		new Sign( " Finally on ", new Quote( "x" ), ",   perform ", new Quote( "y" ))
			.append( new Intention( Intention.append, Intention.FINALLY+" Y" )),
			
		//	Added 3 new signs for the running of applications external to enguage...
		new Sign( "On ", new Quote( "x" ), ", run ", new Quote( "y" ))
			.append( new Intention( Intention.create, Intention.RUN +" X Y" )),
		
		new Sign( "Then  on ",new Quote( "x" ), ", run ", new Quote( "y" ))
			.append( new Intention( Intention.append, Intention.RUN +" Y" )),
	
		new Sign( "Then  on ", new Quote( "x" ), ", if not, run ", new Quote( "y" ))
			.append( new Intention( Intention.append, Intention.ELSE_RUN +" Y" ))
	};
}
