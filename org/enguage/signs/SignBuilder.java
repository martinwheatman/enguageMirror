package org.enguage.signs;

import java.util.Iterator;
import java.util.ListIterator;

import org.enguage.signs.interpretant.Intention;
import org.enguage.signs.symbol.pattern.Frags;
import org.enguage.util.Audit;
import org.enguage.util.Strings;

public class SignBuilder {

	private static final Audit audit= new Audit( "SignBuilkder" );

	/* Builds signs from the following text:
	 * 
	 *	'On "hello", reply "hello to you too".'
	 *
	 * 	'On "i need QUOTED-THINGS":
	 *		add THINGS to my needs list;
	 *		if not, perform "do this";
	 *		if so, run "ls -l";
	 *		reply "ok, you need ...".'
	 *	
	 *	'what do i need.' => returns null: this is an utterance.
	 */
	
	public SignBuilder( Strings sa ) {utterance = new Strings( sa );}
	public SignBuilder( String   s ) {this( new Strings( s ));}
	
	private final Strings utterance;

	private Sign doIntentions(Iterator<String> utti, Sign sign) {
		Strings sa = new Strings();
		while (utti.hasNext()) {
			String s = utti.next();
			if (s.equals( ";" )) {
				int type = Intention.getType( sa );
				sign.append( new Intention( type, sa ));
				sa = new Strings();
			} else
				sa.append( s );
		}
		
		// backwards compatibility:
		if (!sa.isEmpty()) {
			// There used to be a 'connector' on the last intention, such as:
			//    on "XYZ": ... ; then, ... . 
			// By inspection this has only been "then,"
			if (sa.get(0).equals( "then" ) &&
				sa.get(1).equals(    "," )   )
			{
				sa.remove(0);
				sa.remove(0);
			}
			
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
				if ((s.equals(",") || s.equals(":"))
					&& si.hasNext())
				{
					return doIntentions( si,
								new Sign()
									.pattern( frags )
									.concept( Intention.concept()
							)	);
				}
		}	}
		return null;
	}
	public  Sign toSign() {
		ListIterator<String> si = utterance.listIterator();
		if (si.hasNext()) {
			String s = si.next();
			if (s.equals("On"))
				return doPattern( si );
			else
				si.previous();
		}
		return null;
	}
	// ---
	// ---
	private static void test( String utterance ) {
		audit.IN( "test", "Utterance='"+ utterance +"'" );
		Sign  sign = new SignBuilder( utterance ).toSign();
		audit.OUT( sign != null ?
			  "sign(numInts="+ sign.intentions().size() +"): "+ sign.toString()
			: "mediate: '"+ utterance +"'" );
	}
	public static void main( String[] args ) {
		test( "On \"i need QUOTED-THINGS\":\n"
				+"\tadd THINGS to my needs list;\n"
				+"\tif not, perform \"do this\";\n"
				+"\trun \"ls -l\";\n"
				+"\treply \"ok, you need ...\""
		);
		test( "On \"hello\", reply \"hello to you too\"" );
		test( "what do i need" );
}	}
