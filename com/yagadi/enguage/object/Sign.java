/**
 * constructed from Autopoiesis.java example 21/02/17
 */
package com.yagadi.enguage.object;

import com.yagadi.enguage.interpretant.Autopoiesis;
import com.yagadi.enguage.interpretant.Intention;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;
import com.yagadi.enguage.vehicle.Reply;

public class Sign {
	static public  String  NAME = "sign";
	static private Audit  audit = new Audit( "Sign" );
	
	/*
	 * This will handle "sign create X", found in interpret.txt
	 */
	static public String interpret( Strings argv ) {
		audit.in( "interpret", argv.toString());
		String rc = Shell.FAIL;
		if (argv.size() > 0) {
			Reply r = new Reply();
			String cmd = argv.remove( 0 );
			if (cmd.equals( "create" )) {
				audit.debug( "creating sign with: " +     argv.toString());
				rc = new Autopoiesis( Autopoiesis.CREATE, argv.toString(),  Autopoiesis.CREATE ).mediate( r ).toString();
				
			} else if (cmd.equals( "perform" )) {
				audit.debug( "adding a conceptual "+    argv.toString() );
				rc = new Autopoiesis( Intention.DO,     argv.toString(),    Autopoiesis.ADD ).mediate( r ).toString();
				
			} else if (cmd.equals( "reply" )) {
				audit.debug( "adding a reply "+         argv.toString() );
				rc = new Autopoiesis( Intention.REPLY,  argv.toString(),    Autopoiesis.ADD ).mediate( r ).toString();
				
			} else if (cmd.equals( "imply" )) {
				audit.LOG( "Sign: prepending an implication '"+ argv.toString() +"'");
				rc = new Autopoiesis( Intention.THINK,  argv.toString(),    Autopoiesis.PREPEND ).mediate( r ).toString();
				
			} else if (cmd.equals( "finally" )) {
				audit.debug( "adding a final clause? "+ argv.toString() );
				if (argv.get( 0 ).equals( "perform" )) {
					argv.remove( 0 ); // remove perform
					rc = new Autopoiesis( Intention.DO,     argv.toString(), Autopoiesis.ADD ).mediate( r ).toString();
				} else if (argv.get( 0 ).equals( "reply" )) {
					argv.remove( 0 ); // remove reply
					rc = new Autopoiesis( Intention.REPLY,  argv.toString(), Autopoiesis.ADD ).mediate( r ).toString();
				} else {
					rc = new Autopoiesis( Intention.THINK,  argv.toString(), Autopoiesis.ADD ).mediate( r ).toString();
				}
			} else {
				audit.debug( "adding a thought "+ argv.toString() );
				rc = new Autopoiesis(     Intention.THINK,  argv.toString(), Autopoiesis.ADD ).mediate( r ).toString();
		}	}
		return audit.out( rc );
}	}
