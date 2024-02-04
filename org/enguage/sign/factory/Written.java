package org.enguage.sign.factory;

import java.util.Iterator;
import java.util.ListIterator;

import org.enguage.repertoires.concepts.Concept;
import org.enguage.sign.Sign;
import org.enguage.sign.interpretant.Intention;
import org.enguage.sign.symbol.pattern.Pattern;
import org.enguage.util.strings.Strings;

public class Written {

	// Builds a sign from a string of words: 
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
			
	public Written( Strings sa ) {utterance = new Strings( sa );}
	public Written( String   s ) {this( new Strings( s ));}
	
	private final Strings utterance; // already stripped of '.'

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
		if (si.hasNext()) {
			
			String   string = si.next(); // a pattern is a "quoted string"
			
			Pattern pattern = new Pattern( Strings.trim( string, '"' ));
			Sign       sign = new Sign().pattern( pattern ).concept( Concept.concept() );
			
			if (si.hasNext()) {
				string = si.next();
				if ((string.equals(PATTERN_COMMA_TERM) ||
					 string.equals(PATTERN_COLON_TERM)   ) &&
					si.hasNext() )
				{
					doIntentions( si, sign );
			}	}
			
			return sign;
		}
		return null;
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
}	} // SignBuilder - builds sign from 'On "xyz": ...' text.
