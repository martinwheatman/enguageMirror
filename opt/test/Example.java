package opt.test;

import org.enguage.Enguage;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class Example {

	private static final String[] defaultUtterance = {"i", "need", "a", "coffee"};

	public static void main( String[] args ) {
		// "Strings" is simply an array of 'String'
		Strings utterance = new Strings( args.length==0 ? defaultUtterance : args );
		Enguage enguage = new Enguage(); // default space is Enguage.RW_SPACE 
		
		Strings reply = enguage.mediate( utterance );
		
		Audit.log( reply );
	}
}