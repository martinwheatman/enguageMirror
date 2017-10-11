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
	
	public static final Sign[] autopoiesis = {
		// PATTERN CREATION cases (7).
		// a1: On X, think Y.
		new Sign().append( new Intention( create, Intention.THINK +" X Y" ))
			.pattern( new Patternette(     "On ", "x"      ).quotedIs())
			.pattern( new Patternette( ", ", "y" ).phrasedIs()),
			
		// a2: On X, reply Y.
		new Sign().append( new Intention( create, Intention.REPLY +" X Y" ))
			.pattern( new Patternette(     "On ", "x" ).quotedIs())
			.pattern( new Patternette( ", reply ", "y" ).quotedIs()),
			
		// a3: On X, perform Y.
		new Sign().append( new Intention( create, Intention.DO +" X Y" )) // <<<< trying this
			.pattern( new Patternette(       "On ", "x" ).quotedIs())
			.pattern( new Patternette( ", perform ", "y" ).quotedIs()),
			
		// b1: Then on X think Y.
		new Sign().append( new Intention( append, Intention.THINK +" Y" ))
			.pattern( new Patternette( "Then on ", "x" ).quotedIs())
			.pattern( new Patternette( ", ", "y" ).phrasedIs()),
			
		// b2: Then on X reply Y.
		new Sign().append( new Intention( append, Intention.REPLY+" Y" ))
			.pattern( new Patternette( "Then  on ", "x" ).quotedIs())
			.pattern( new Patternette( ", reply   ", "y" ).quotedIs()),
			
		// b3: Then on X perform Y.
		new Sign().append( new Intention( append, Intention.DO +" Y" ))
			.pattern( new Patternette( "Then  on ", "x" ).quotedIs())
			.pattern( new Patternette( ", perform ", "y" ).quotedIs()),
		
		// At some point this could be improved to say "On X, perform Y; if not, reply Z." -- ??? think!
		// !b1: Else on X think Y.
		new Sign().append( new Intention( append, Intention.ELSE_THINK +" Y" ))
			.pattern( new Patternette( "Then on ", "x"      ).quotedIs())
			.pattern( new Patternette( ", if not, ", "y" ).phrasedIs()),
			
		// !b2: Else on X reply Y.
		new Sign().append( new Intention( append, Intention.ELSE_REPLY +" Y"))
			.pattern( new Patternette(  "Then on ", "x"      ).quotedIs())
			.pattern( new Patternette( ", if not, reply ", "y" ).quotedIs()),
			
		// !b3: Else on X perform Y.
		new Sign().append( new Intention( append, Intention.ELSE_DO +" Y" ))
			.pattern( new Patternette( "Then  on ", "x"      ).quotedIs())
			.pattern( new Patternette( ", if not, perform ", "y" ).quotedIs()),
					
		/*	Added new signs for the running of applications external to enguage...
		 */
		new Sign().append( new Intention( create, Intention.RUN +" X Y" ))
			.pattern( new Patternette( "On ", "x" ).quotedIs())
			.pattern( new Patternette( ", run ", "y" ).quotedIs()),
		
		new Sign().append( new Intention( append, Intention.RUN +" Y" ))
			.pattern( new Patternette( "Then  on ", "x"      ).quotedIs())
			.pattern( new Patternette( ", run ", "y" ).quotedIs()),

		new Sign().append( new Intention( append, Intention.ELSE_RUN +" Y" ))
			.pattern( new Patternette( "Then  on ", "x"      ).quotedIs())
			.pattern( new Patternette( ", if not, run ", "y" ).quotedIs()),

		/* c1: Finally on X perform Y. -- dont need think or reply?
		 */
		new Sign().append( new Intention( append, Intention.FINALLY+" Y" ))
			.pattern( new Patternette( " Finally on ", "x"      ).quotedIs())
			.pattern( new Patternette( ",   perform ", "y" ).quotedIs())
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
			audit.debug( "Adding "+ typeToString() +": ["+ sa.toString( Strings.CSV )+"]");
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