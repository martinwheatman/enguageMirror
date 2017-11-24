/**
 * constructed from Autopoiesis.java example 21/02/17
 */
package org.enguage.object;

import org.enguage.sign.intention.Intention;
import org.enguage.sign.repertoire.Repertoire;
import org.enguage.util.Audit;
import org.enguage.util.Shell;
import org.enguage.util.Strings;
import org.enguage.vehicle.Reply;

import com.yagadi.enguage.object.Variable;

public class Sign {
	static public  String  NAME = "sign";
	static private Audit  audit = new Audit( "Sign" );
	
	/*
	 * This will handle "sign create X", found in interpret.txt
	 */
	static public String interpret( Strings argv ) {
		audit.in( "interpret", argv.toString());
		String rc = Shell.FAIL;
		
		if (Repertoire.isInducting() &&
				argv.size() > 0)
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
				rc = new Intention( Intention.create, argv.toString(), Intention.create ).autopoiesis( r ).toString();
				
			} else if (cmd.equals( "perform" )) {
				audit.debug( "adding a conceptual "+    argv.toString() );
				rc = new Intention(
							isElse ? Intention.elseDo : Intention.thenDo,
							argv.toString(),
						   prepending ?
								Intention.prepend :
							   headAppending ?
								Intention.headAppend :
								Intention.append
						).autopoiesis( r ).toString();
				
			} else if (cmd.equals( "reply" )) {
				if (argv.size() > 0) {
					rc = new Intention(
							isElse? Intention.elseReply : Intention.thenReply, 
							argv.toString(), 
							prepending ?
								Intention.prepend :
								headAppending ?
								Intention.headAppend :
								Intention.append
						  ).autopoiesis( r ).toString();
				}
			} else if (cmd.equals( "think" )) {
				audit.debug( "adding a thought "+ argv.toString() );
				rc = new Intention(
							isElse? Intention.elseThink : Intention.thenThink,
							argv.toString(), 
							prepending ?
								Intention.prepend :
								 headAppending ?
									Intention.headAppend :
									Intention.append
					  ).autopoiesis( r ).toString();
			} else if (cmd.equals( "imply" )) {
				audit.debug( "Sign: prepending an implication '"+ argv.toString() +"'");
				rc = new Intention(
						isElse? Intention.elseThink : Intention.thenThink,
						argv.toString(),
						Intention.prepend
					 ).autopoiesis( r ).toString();
				
			} else if (cmd.equals( "finally" )) {
				audit.debug( "adding a final clause? "+ argv.toString() );
				cmd = argv.remove( 0 );
				if (cmd.equals( "perform" ))
					rc = new Intention( isElse ? Intention.elseDo    : Intention.thenDo,    argv.toString(), Intention.append ).autopoiesis( r ).toString();
				else if (cmd.equals( "reply" ))
					rc = new Intention( isElse ? Intention.elseReply : Intention.thenReply, argv.toString(), Intention.append ).autopoiesis( r ).toString();
				else
					rc = new Intention( isElse ? Intention.elseThink : Intention.thenThink, argv.toString(), Intention.append ).autopoiesis( r ).toString();
			
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
		Intention.printSign();
}	}
