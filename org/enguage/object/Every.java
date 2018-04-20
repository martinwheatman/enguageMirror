package org.enguage.object;

import java.util.ArrayList;
import java.util.Locale;

import org.enguage.Enguage;
import org.enguage.util.Audit;
import org.enguage.util.Join;
import org.enguage.util.Shell;
import org.enguage.util.Strings;

public class Every {
	public  static final String        NAME = "every";
	private static       Audit        audit = new Audit( "Every" );
	
	static private String forEvery( Strings sa ) {
		audit.in( "forEvery", "sa=[ "+ sa.toString( Strings.SQCSV ) +" ]" );
		String rc = Shell.FAIL;
		/* "martin needs a cup of coffee and a biscuit" +
		 * perform "list forEach dummy dummy { SUBJECTS } needs { OBJECTS }"; =>
		 * { subject='martin' } needs { objects='a cup of coffee and a biscuit' }
		 * recall each joined combination, e.g.:
		 *      martin needs a cup of coffee
		 *      martin needs a biscuit
		 */
		Attributes match = new Attributes( sa.strip( "{", "}" ));
		Join.on( true );
		ArrayList<Attributes> ala = Join.join( match, "and" );
		Join.on( false );
		
		if (ala.size() > 1) {
			rc = Shell.SUCCESS;
			for( Attributes m : ala ) {
				// re-issue rebuilt utterance
				
				String reply = Enguage.e.interpret( sa.reinsert( m, "{", "}" ));
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
