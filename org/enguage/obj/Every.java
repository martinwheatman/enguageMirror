package org.enguage.obj;

import java.util.ArrayList;
import java.util.Locale;

import org.enguage.Enguage;
import org.enguage.util.Attributes;
import org.enguage.util.Audit;
import org.enguage.util.Join;
import org.enguage.util.Shell;
import org.enguage.util.Strings;

public class Every {
	public  static final String        NAME = "every";
	private static       Audit        audit = new Audit( "Every" );
	
	static private String forEvery( Strings sa ) {
		String rc = Shell.FAIL;
		audit.in( "forEvery", "sa=[ "+ sa.toString( Strings.SQCSV ) +" ]" );
		/* "martin needs a cup of coffee and a biscuit" +
		 * perform "every : { SUBJECTS } needs { OBJECTS }"; =>
		 * { subject='martin' } needs { objects='a cup of coffee and a biscuit' } LOCATOR...
		 * recall each joined combination, e.g.:
		 *      martin needs a cup of coffee
		 *      martin needs a biscuit
		 * N.B. this is called off the back of a perform (a function call), but it
		 * produces an utterance; any arg needs to be expanded or added to the context(?)
		 */
		Attributes match = new Attributes( sa.strip( "{", "}" ));
		Join.on( true );
		ArrayList<Attributes> ala = Join.join( match, "and" );
		Join.on( false );
		
		if (ala.size() > 1) {
			rc = Shell.SUCCESS;
			for( Attributes m : ala ) {
				// re-issue rebuilt utterance
				
				// N.B. need to 'expandValues' here..
				String reply = Enguage.e.interpret(  Attributes.expandValues( sa.reinsert( m, "{", "}" ) ) );
				audit.debug( "individual reply => "+ reply );
				if (reply.equals( /*Enguage.DNU*/ "I don't understand" ) ||
					reply.toLowerCase( Locale.getDefault()).startsWith( "sorry" ))
				{	rc = Shell.FAIL;
					break;
			}	}
		} else
			audit.debug( "join failed" );
		
		// re-save original variables
		match.toVariables();
	
		//audit.on( false );
		audit.out( rc );
		audit.trace( false );
		return rc;
	}
	static public String interpret( Strings sa ) {
		audit.in( "interpret", sa.toString());
		return audit.out( forEvery( sa ) );
	}
}
