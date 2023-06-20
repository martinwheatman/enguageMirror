package opt.test;

import org.enguage.Enguage;
import org.enguage.util.Audit;
import org.enguage.util.Strings;

public class Example {

	private static final String[] defaultUtterance = {"i", "need", "a", "coffee"};

	public static void main( String[] args ) {
		
		// "Strings" is simply an array of 'String'
		Strings utterance = new Strings( args.length==0 ? defaultUtterance : args );
		Enguage enguage = new Enguage( Enguage.RW_SPACE );
		
		Strings reply = enguage.mediate( utterance );
		
		Audit.log( reply );
	}
}