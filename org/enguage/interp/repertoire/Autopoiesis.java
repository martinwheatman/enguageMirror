package org.enguage.interp.repertoire;

import org.enguage.interp.intention.Intention;
import org.enguage.interp.pattern.Phrase;
import org.enguage.interp.pattern.Quote;
import org.enguage.interp.sign.Sign;

public class Autopoiesis {

	public static final Sign[] spoken = {
			
			//	OPENING REMARKS
			new Sign( "interpret", new Phrase( "x" ), "thus" )
				.appendIntention( Intention.thenThink, "set transformation to true" )
				.appendIntention( Intention.thenDo,    "sign create X")
				.appendIntention( Intention.thenReply, "go on" ),
			
			new Sign( "interpret", new Phrase( "x" ), "like this" )
				.appendIntention( Intention.thenThink, "set transformation to true" )
				.appendIntention( Intention.thenDo,    "sign create X")
				.appendIntention( Intention.thenReply, "go on" ),
			
			// CONCLUDING REMARKS
			new Sign( "that concludes interpretation" )
				.appendIntention( Intention.thenThink, "get the value of finalReply" ) // only set on toXreply
				.appendIntention( Intention.thenDo,    "sign append reply ..." ) // transformation must be true!
				.appendIntention( Intention.thenThink, "unset the value of finalReply" )
				.appendIntention( Intention.elseDo,    "variable set transformation false" )     // after sign reply...
				.appendIntention( Intention.thenDo,    "variable set transformation false" )     // after sign reply...
				.appendIntention( Intention.elseReply, "ok" )
				.appendIntention( Intention.thenReply, "ok" ),
				
			new Sign( "that is it" )
				.appendIntention( Intention.thenThink, "that concludes interpretation" ),
						
			new Sign( "that is all" )
				.appendIntention( Intention.thenThink, "that concludes interpretation" ),
				
					
				
			// THINK...
			new Sign( "then", new Phrase( "x" ))
				.appendIntention( Intention.thenDo,    "sign append think X" )
				.appendIntention( Intention.thenReply, "go on" ),
						
			new Sign( "first", new Phrase( "x" ))
				.appendIntention( Intention.thenThink, "then X" )
				.appendIntention( Intention.thenReply, "go on" ),
				
			new Sign( "think", new Phrase( "x" ))
				.appendIntention( Intention.thenThink, "then X" )
				.appendIntention( Intention.thenReply, "go on" ),
				
			new Sign( "next", new Phrase( "x" ))
				.appendIntention( Intention.thenThink, "then X" )
				.appendIntention( Intention.thenReply, "go on" ),
				
			new Sign( "then if not", new Phrase( "x" ))
				.appendIntention( Intention.thenDo  ,  "sign append else think X" )
				.appendIntention( Intention.thenReply, "go on" ),
				
				
			// ...DO
			new Sign( "then perform", new Phrase( "x" ))
				.appendIntention( Intention.thenDo,    "sign append perform X" )
				.appendIntention( Intention.thenReply, "go on" ),
					
			new Sign( "first perform", new Phrase( "x" ))
				.appendIntention( Intention.thenDo,    "sign append perform X" )
				.appendIntention( Intention.thenReply, "go on" ),
				
			new Sign( "next perform", new Phrase( "x" ))
				.appendIntention( Intention.thenDo,    "sign append perform X" )
				.appendIntention( Intention.thenReply, "go on" ),
	
			new Sign( "then if not perform", new Phrase( "x" ))
				.appendIntention( Intention.thenDo,    "sign append else perform X" )
				.appendIntention( Intention.thenReply, "go on" ),
							
			// ...SAY
			new Sign( "just reply", new Phrase( "x" ))
				.appendIntention( Intention.thenDo,    "sign append reply X" )
				.appendIntention( Intention.thenReply, "go on" ),
				
			new Sign( "then reply", new Phrase( "x" ))
				.appendIntention( Intention.thenDo,    "sign append reply X" )
				.appendIntention( Intention.thenReply, "go on" ),
					
			new Sign( "then if not reply", new Phrase( "x" ))
				.appendIntention( Intention.thenDo,    "sign append else reply X" )
				.appendIntention( Intention.thenReply, "go on" ),
	
			new Sign( "then whatever reply", new Phrase( "x" ))
				.appendIntention( Intention.thenDo,    "sign append else reply X" )
				.appendIntention( Intention.thenDo,    "sign append reply X" )
				.appendIntention( Intention.thenThink, "that concludes interpretation" ),
	
				
			new Sign( "finally", new Phrase( "x" ))
				.appendIntention( Intention.thenDo,    "sign finally X" )
				.appendIntention( Intention.thenReply, "go on" ),
	
			// IMPLIES
			new Sign( "this implies that", new Phrase( "IMPLICATION" ))
				.appendIntention( Intention.thenDo,    "sign imply IMPLICATION" )
				.appendIntention( Intention.thenReply, "go on" ),
				
			new Sign( "this implies that you", new Phrase( "IMPLICATION" ))
				.appendIntention( Intention.thenDo,    "sign imply IMPLICATION" )
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
			
		new Sign( "Then on ",  new Quote( "x" ), ", if not, say so" )
			.append( new Intention( Intention.append, Intention.ELSE_REPLY +" \"\" ")),
			
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
