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
			boolean isElse = false;
			Reply r = new Reply();
			String cmd = argv.remove( 0 );
			if (cmd.equals( "else" )) {
				isElse = true;
				cmd = argv.remove( 0 );
			}
			if (cmd.equals( "create" )) {
				audit.debug( "creating sign with: " +   argv.toString());
				rc = new Autopoiesis( Autopoiesis.NEW,                                 argv.toString(),    Autopoiesis.create ).mediate( r ).toString();
				
			} else if (cmd.equals( "perform" )) {
				audit.debug( "adding a conceptual "+    argv.toString() );
				rc = new Autopoiesis( isElse ? Intention.ELSE_DO : Intention.DO,       argv.toString(),    Autopoiesis.prepend ).mediate( r ).toString();
				
			} else if (cmd.equals( "reply" )) {
				audit.debug( "adding a reply "+         argv.toString() );
				rc = new Autopoiesis( isElse? Intention.ELSE_REPLY : Intention.REPLY,  argv.toString(),    Autopoiesis.append ).mediate( r ).toString();
				
			} else if (cmd.equals( "imply" )) {
				audit.LOG( "Sign: prepending an implication '"+ argv.toString() +"'");
				rc = new Autopoiesis( isElse? Intention.ELSE_THINK : Intention.THINK,  argv.toString(),    Autopoiesis.prepend ).mediate( r ).toString();
				
			} else if (cmd.equals( "finally" )) {
				audit.debug( "adding a final clause? "+ argv.toString() );
				cmd = argv.remove( 0 );
				if (cmd.equals( "perform" ))
					rc = new Autopoiesis( isElse ? Intention.ELSE_DO    : Intention.DO,    argv.toString(), Autopoiesis.append ).mediate( r ).toString();
				else if (cmd.equals( "reply" ))
					rc = new Autopoiesis( isElse ? Intention.ELSE_REPLY : Intention.REPLY, argv.toString(), Autopoiesis.append ).mediate( r ).toString();
				else
					rc = new Autopoiesis( isElse ? Intention.ELSE_THINK : Intention.THINK, argv.toString(), Autopoiesis.append ).mediate( r ).toString();
			
			} else {
				audit.debug( "adding a thought "+ argv.toString() );
				rc = new Autopoiesis(     Intention.THINK, argv.toString(), Autopoiesis.append ).mediate( r ).toString();
		}	}
		return audit.out( rc );
	}
	public static void main( String args[]) {
		interpret( new Strings( "create variable whom needs phrase variable object" ));
		interpret( new Strings( "reply  ok variable whom needs variable object" ));
		interpret( new Strings( "imply  is variable object in variable name needs list" ));
		//interpret( new Strings( "reply  i know" ));
		//interpret( new Strings( "perform add variable object to variable name needs list" ));
		Autopoiesis.printSign();
}	}
