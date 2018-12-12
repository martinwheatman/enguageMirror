package org.enguage.objects;

import java.util.ArrayList;
import java.util.Locale;

import org.enguage.Enguage;
import org.enguage.util.Audit;
import org.enguage.util.Join;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attributes;
import org.enguage.util.sys.Shell;

public class Every {
	public  static final String        NAME = "every";
	private static       Audit        audit = new Audit( "Every" );
	
	static private String forEvery( Strings sa ) {
		String rc = Shell.FAIL;
		//audit.in( "forEvery", "sa=[ "+ sa.toString( Strings.SQCSV ) +" ]" );
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
				Strings reply = Enguage.interpret(  Attributes.expandValues( sa.reinsert( m, "{", "}" ) ) );
				//audit.debug( "individual reply => "+ reply );
				if (reply.equals( /*Enguage.DNU*/ new Strings( "I don't understand" )) ||
					reply.get(0).toLowerCase( Locale.getDefault()).startsWith( "sorry" ))
				{	rc = Shell.FAIL;
					break;
			}	}
		} else
			audit.debug( "join failed" );
		
		// re-save original variables
		match.toVariables();
	
		//audit.out( rc );
		return rc;
	}
	static public Strings interpret( Strings sa ) {
		audit.in( "interpret", sa.toString());
		return audit.out( new Strings( forEvery( sa ) ));
}	}
