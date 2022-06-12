package org.enguage.interp.repertoire;

import org.enguage.interp.intention.Intention;
import org.enguage.interp.pattern.Phrase;
import org.enguage.interp.pattern.Quote;
import org.enguage.interp.sign.Sign;

public class Autopoiesis {

	public static final Sign[] written = {
		// 3 x 3 signs (think/do/say * start/subseq/infelicit) + 1 "finally"
		new Sign( "On ", new Quote( "x" ), ",", new Phrase( "y" ))
			.append( new Intention( Intention.create, Intention.THINK +" X Y" )),
			
		new Sign( "On ",new Quote( "x" ), ", perform ", new Quote( "y" ))
			.append( new Intention( Intention.create, Intention.DO +" X Y" )),
			
		new Sign( "On ", new Quote( "x" ), ", reply ", new Quote( "y" ))
			.append( new Intention( Intention.create, Intention.REPLY +" X Y" )),
			
		new Sign( "Then  on ", new Quote( "x" ), ", ", new Phrase( "y" ))
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
