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
		
		if (argv.size() > 0)
		{
			String var1 = Variable.get( "prepending" ),
					var2 = Variable.get( "headAppending" );
			boolean prepending    = var1 != null && var1.equals( "true" ),
					headAppending = var2 != null && var2.equals( "true" );
			
			boolean isElse = false;
			Reply r = new Reply();
			String cmd = argv.remove( 0 );
			if (cmd.equals( "else" )) {
				isElse = true;
				cmd = argv.remove( 0 );
			}

			if (cmd.equals( "create" )) {
				audit.debug( "creating sign with: " +   argv.toString());
				rc = new Autopoiesis( Autopoiesis.create,                                 argv.toString(),    Autopoiesis.create ).mediate( r ).toString();
				
			} else if (cmd.equals( "perform" )) {
				audit.debug( "adding a conceptual "+    argv.toString() );
				rc = new Autopoiesis(
							isElse ? Intention.elseDo : Intention.thenDo,
							argv.toString(),
						   prepending ?
								Autopoiesis.prepend :
							   headAppending ?
								Autopoiesis.headAppend :
								Autopoiesis.append
						).mediate( r ).toString();
				
			} else if (cmd.equals( "reply" )) {
				//audit.LOG( (prepending?"pre":"app")+"ing a reply "+  (isElse? Intention.ELSE_REPLY : Intention.REPLY) +" "+ argv.toString() );
				rc = new Autopoiesis(
						isElse? Intention.elseReply : Intention.thenReply, 
						argv.toString(), 
						prepending ?
							Autopoiesis.prepend :
						   headAppending ?
							Autopoiesis.headAppend :
							Autopoiesis.append
					  ).mediate( r ).toString();
				
			} else if (cmd.equals( "think" )) {
				audit.debug( "adding a thought "+ argv.toString() );
				rc = new Autopoiesis(
							isElse? Intention.elseThink : Intention.thenThink,
							argv.toString(), 
							prepending ?
								Autopoiesis.prepend :
								 headAppending ?
									Autopoiesis.headAppend :
									Autopoiesis.append
					  ).mediate( r ).toString();
			} else if (cmd.equals( "imply" )) {
				audit.debug( "Sign: prepending an implication '"+ argv.toString() +"'");
				rc = new Autopoiesis(
						isElse? Intention.elseThink : Intention.thenThink,
						argv.toString(),
						Autopoiesis.prepend
					 ).mediate( r ).toString();
				
			} else if (cmd.equals( "finally" )) {
				audit.debug( "adding a final clause? "+ argv.toString() );
				cmd = argv.remove( 0 );
				if (cmd.equals( "perform" ))
					rc = new Autopoiesis( isElse ? Intention.elseDo    : Intention.thenDo,    argv.toString(), Autopoiesis.append ).mediate( r ).toString();
				else if (cmd.equals( "reply" ))
					rc = new Autopoiesis( isElse ? Intention.elseReply : Intention.thenReply, argv.toString(), Autopoiesis.append ).mediate( r ).toString();
				else
					rc = new Autopoiesis( isElse ? Intention.elseThink : Intention.thenThink, argv.toString(), Autopoiesis.append ).mediate( r ).toString();
			
			} else {
				audit.ERROR( "Unknown Sign.interpret() command: "+ cmd );
		}	}
		return audit.out( rc );
	}
	public static void main( String args[]) {
		interpret( new Strings( "this won't work!" ));
		interpret( new Strings( "create variable whom needs phrase variable object" ));
		interpret( new Strings( "reply  ok variable whom needs variable object" ));
		interpret( new Strings( "imply  is variable object in variable name needs list" ));
		//interpret( new Strings( "reply  i know" ));
		//interpret( new Strings( "perform add variable object to variable name needs list" ));
		Autopoiesis.printSign();
}	}
