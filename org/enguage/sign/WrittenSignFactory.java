package org.enguage.sign;

import java.util.Iterator;
import java.util.ListIterator;

import org.enguage.sign.interpretant.Intention;
import org.enguage.sign.symbol.pattern.Frags;
import org.enguage.util.strings.Strings;

public class WrittenSignFactory {

	// Builds a sign from a string of words: 
	public static final String    SIGN_START_TOKEN = "On";
	public static final String  PATTERN_COMMA_TERM = ",";
	/*		'On "hello", reply "hello to you too".'
	 */
	public static final String  PATTERN_COLON_TERM = ":";
	public static final String INTENTION_SEPARATOR = ";";
	/*	 	'On "i need QUOTED-THINGS":
	 *			add THINGS to my needs list;
	 *			if not, perform "do this"  ;
	 *			if so, run "ls -l"         ;
	 *			reply "ok, you need ...".'
	 */
			
	public WrittenSignFactory( Strings sa ) {utterance = new Strings( sa );}
	public WrittenSignFactory( String   s ) {this( new Strings( s ));}
	
	private final Strings utterance; // already stripped of '.'

	private Sign doIntentions(Iterator<String> utti, Sign sign) {
		Strings sa = new Strings();
		while (utti.hasNext()) {
			String s = utti.next();
			if (s.equals( INTENTION_SEPARATOR )) {
				int type = Intention.getType( sa );
				sign.append( new Intention( type, sa ));
				sa = new Strings();
			} else
				sa.append( s );
		}
		
		// backwards compatibility:
		if (!sa.isEmpty()) {
			// getType affects sa!!! be explicit in ordering
			int type = Intention.getType( sa );
			sign.append( new Intention( type, sa ));
		}
		return sign;
	}
	private Sign doPattern(Iterator<String> si) {
		if (si.hasNext()) {
			String s = si.next();
			Frags frags = new Frags( Strings.trim( s, '"' ));
			if (si.hasNext()) {
				s = si.next();
				if ((s.equals(PATTERN_COMMA_TERM) ||
					 s.equals(PATTERN_COLON_TERM))
					&& si.hasNext())
				{
					return doIntentions( si,
								new Sign()
									.pattern( frags )
									.concept( Intention.concept() )	
						   );
				}
		}	}
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
