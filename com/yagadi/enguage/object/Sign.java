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
	
	static public String interpret( Strings argv ) {
		audit.in( "interpret", argv.toString());
		String rc = Shell.FAIL;
		if (argv.size() > 0) {
			Reply r = new Reply();
			String cmd = argv.remove( 0 );
			if (cmd.equals( "create" )) {
				audit.debug( "creating sign with:" + argv.toString());
				rc = new Autopoiesis( Autopoiesis.CREATE, Autopoiesis.CREATE, argv.toString() ).mediate( r ).toString();
				
			} else if (cmd.equals( "perform" )) {
				audit.debug( "adding a conceptualisation "+ argv.toString() );
				argv.remove( 0 ); // remove perform
				rc = new Autopoiesis( Autopoiesis.ADD,    Intention.DO,     argv.toString() ).mediate( r ).toString();
				
			} else if (cmd.equals( "reply" )) {
				audit.debug( "adding a reply "+ argv.toString() );
				rc = new Autopoiesis( Autopoiesis.ADD,    Intention.REPLY,  argv.toString() ).mediate( r ).toString();
				
			} else if (cmd.equals( "finally" )) {
				audit.debug( "adding a final clause? "+ argv.toString() );
				if (argv.get( 0 ).equals( "perform" )) {
					argv.remove( 0 ); // remove perform
					rc = new Autopoiesis( Autopoiesis.ADD,    Intention.DO,     argv.toString() ).mediate( r ).toString();
				} else if (argv.get( 0 ).equals( "reply" )) {
					argv.remove( 0 ); // remove reply
					rc = new Autopoiesis( Autopoiesis.ADD,    Intention.REPLY,  argv.toString() ).mediate( r ).toString();
				} else {
					rc = new Autopoiesis( Autopoiesis.ADD,    Intention.THINK,  argv.toString() ).mediate( r ).toString();
				}
			} else {
				audit.debug( "adding a thought "+ argv.toString() );
				rc = new Autopoiesis( Autopoiesis.ADD,    Intention.THINK,  argv.toString() ).mediate( r ).toString();
		}	}
		return audit.out( rc );
}	}
