package org.enguage.repertoires.written;

import org.enguage.signs.Sign;
import org.enguage.signs.interpretant.Intention;
import org.enguage.signs.symbol.pattern.Phrase;
import org.enguage.signs.symbol.pattern.Quote;

public class Repertoire {
	public static final Sign[] signs = {
		// 3 x 3 signs (think/do/say * start/subseq/infelicit) + 1 "finally"
		new Sign( "On ", new Quote( "x" ), ",", new Phrase( "y" ))
			.append( new Intention( Intention.create, Intention.THINK +" X Y" )),
			
		new Sign( "On ",new Quote( "x" ), ", perform ", new Quote( "y" ))
			.append( new Intention( Intention.create, Intention.DO +" X Y" )),
			
		new Sign( "On ", new Quote( "x" ), ", reply ", new Quote( "y" ))
			.append( new Intention( Intention.create, Intention.REPLY +" X Y" )),
			
		new Sign( "Then, ", new Phrase( "y" ))
			.append( new Intention( Intention.append, Intention.THINK +" Y" )),
			
		new Sign( "Then, perform ",  new Quote( "y" ))
			.append( new Intention( Intention.append, Intention.DO +" Y" )),
			
		new Sign( "Then, reply   ", new Quote("y" ))
			.append( new Intention( Intention.append, Intention.REPLY+" Y" )),
			
		new Sign( "Then, if not, ",  new Phrase( "y" ))
			.append( new Intention( Intention.append, Intention.ELSE_THINK +" Y" )),
			
		new Sign( "Then, if not, perform ", new Quote( "y" ))
			.append( new Intention( Intention.append, Intention.ELSE_DO +" Y" )),
					
		new Sign( "Then, if not, reply ", new Quote( "y" ))
			.append( new Intention( Intention.append, Intention.ELSE_REPLY +" Y")),
			
		new Sign( "Then, if not, say so" )
			.append( new Intention( Intention.append, Intention.ELSE_REPLY +" \"\" ")),
			
		new Sign( " Finally,   perform ", new Quote( "y" ))
			.append( new Intention( Intention.append, Intention.FINALLY+" Y" )),
			
		//	Added 3 new signs for the running of applications external to enguage...
		new Sign( "On ", new Quote( "x" ), ", run ", new Quote( "y" ))
			.append( new Intention( Intention.create, Intention.RUN +" X Y" )),
		
		new Sign( "Then, run ", new Quote( "y" ))
			.append( new Intention( Intention.append, Intention.RUN +" Y" )),
	
		new Sign( "Then, if not, run ", new Quote( "y" ))
			.append( new Intention( Intention.append, Intention.ELSE_RUN +" Y" ))
	};
}
