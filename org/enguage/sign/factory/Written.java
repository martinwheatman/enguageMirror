package org.enguage.sign.factory;

import java.util.Iterator;
import java.util.ListIterator;

import org.enguage.repertoires.concepts.Concept;
import org.enguage.sign.Sign;
import org.enguage.sign.interpretant.Intention;
import org.enguage.sign.symbol.pattern.Pattern;
import org.enguage.util.strings.Strings;

public class Written {

	/**  This class builds signs from a string of words. It has
	 *   a minimal set of tokens to define a structure - a semi-
	 *   colon separated list, e.g. On "X": do a; do b; do c.
	 */
	public static final String    SIGN_START_TOKEN = "On";
	public static final String  PATTERN_COMMA_TERM = ",";
	/*		'On "hello", reply "hello to you too".'
	 */
	public static final String  PATTERN_COLON_TERM  = ":";
	public static final String INTENTION_SEPARATOR  = ";";
	public static final String INTENTION_TERMINATOR = ".";
	/*	 	'On "i need QUOTED-THINGS":
	 *			add THINGS to my needs list;
	 *			if not, perform "do this"  ;
	 *			if so, run "ls -l"         ;
	 *			reply "ok, you need ...".'
	 */
			
	private final Strings utterance;
	
	public Written( Strings sa ) {utterance = new Strings( sa );}
	public Written( String   s ) {this( new Strings( s ));}

	/** =======================================================================
	 * A SIMPLE semi-colon separated list interpreter:
	 * (The emphasis is not on error detection.)
	 */
	private Strings insertNewIntetion( Sign sign, Strings sa ) {
		int type = Intention.extractType( sa );
		sign.append( new Intention( type, sa ));
		return new Strings(); // start new 'sa'
	}
	
	private Sign doIntentions(Iterator<String> utti, Sign sign) {
		Strings sa = new Strings();
		while (utti.hasNext()) {
			String s = utti.next();
			if (s.equals( INTENTION_SEPARATOR  ))
				sa = insertNewIntetion( sign, sa );
			else if (s.equals( INTENTION_TERMINATOR ))
				break;
			else
				sa.append( s );
		}
		
		// backwards compatibility: where utti is not terminated
		if (!sa.isEmpty()) 
			insertNewIntetion( sign, sa );
		
		return sign;
	}
	private Sign doPattern(Iterator<String> si) {
		Sign sign = null;
		if (si.hasNext()) {
			
			String string = si.next();
			
			// a pattern should be a "quoted string"
			if (string.charAt( 0 ) ==  '"' ) {
			
				sign = new Sign()
						.pattern( new Pattern( Strings.trim( string, '"' )))
						.concept( Concept.concept() ); // propagate current name
				
				if (si.hasNext()) {
					string = si.next();
					
					if ((string.equals(PATTERN_COMMA_TERM) ||
						 string.equals(PATTERN_COLON_TERM)   ) &&
						si.hasNext() )
					{
						doIntentions( si, sign );
					}
		}	}	}
		return sign;
	}
	public  Sign toSign() {
		ListIterator<String> si = utterance.listIterator();
		if (si.hasNext()) {
			String s = si.next();
			if (s.equals(SIGN_START_TOKEN))
				return doPattern( si );
			else
				si.previous();
		}
		return null;
}	} // Written - a "SignBuilder" factory - builds a sign from 'On "xyz": ...; ... .'
