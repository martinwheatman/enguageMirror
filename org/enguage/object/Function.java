package org.enguage.object;

import org.enguage.util.Audit;
import org.enguage.util.Number;
import org.enguage.util.Shell;
import org.enguage.util.Strings;

public class Function {
	static public  String  NAME = "function";
	static private Audit  audit = new Audit( "Function" );
	
	public static Strings divvy( Strings input, String sep ) {
		Strings output = new Strings(),
				tmp    = new Strings();
		for (String s : input )
			if (s.equals( sep )) {
				if (tmp.size() > 0) output.add( tmp.toString());
				tmp = new Strings();
			} else 
				tmp.add( s );
		if (tmp.size() > 0) output.add( tmp.toString());
		return output;
	}
	public static Strings substitute( String function, Strings actuals ) {
		return new Strings( new Value( function, "body" ).getAsString() ) // body
				.substitute(
						new Strings( new Value( function, "formals" ).getAsString() ),
						actuals );
	}

	/*
	 * This will handle:
	 * 		"eval sum 1 and 2"
	 */
	static public String interpret( Strings argv ) {
		audit.in( "interpret", argv.toString());
		String  rc = Shell.FAIL,
		       cmd = argv.remove( 0 );

//		if (cmd.equals( "match" )) {
//			String function = argv.remove( 0 );
//			audit.log( "matching "+ function +" params with: " +  argv.toString());
//			Strings actuals = divvy( argv, "and" );
//			Value v = new Value( function, "parameters" );
//			Strings parameters = new Strings( v.getAsString() );
//			if (actuals.size() != parameters.size())
//				audit.FATAL( "sizes don't match: ["+ parameters.toString( Strings.DQCSV ) +"] ["+ actuals.toString( Strings.DQCSV ) +"]");
//			else {
//				rc = "";
//				ListIterator<String> pi = parameters.listIterator();
//				ListIterator<String> ai = actuals.listIterator();
//				while (pi.hasNext()) {
//					String param = pi.next();
//					String actual = ai.next();
//					rc += param +"="+ actual;
//					if (ai.hasNext()) rc += " ";
//			}	}
//		} else if (cmd.equals( "substitute" )) {
//			// e.g. [a equals 1. b equals 2. what is the] sum of a and b
//			//      [what is the] sum of 1 and 2.
//			//       [what is the] sum of a and b. a=1 b=2
//			String function = argv.remove( 0 );
//			argv.remove( 0 ); // of
//			//audit.log( "substituting "+ function +" params with: " +  argv.toString());
//			rc = substitute( function, divvy( argv, "and" )).toString();
			
		//} else 
		if (cmd.equals( "eval" )) {
			// e.g. [a equals 1. b equals 2. what is the] sum of a and b
			//      [what is the] sum of 1 and 2.
			//       [what is the] sum of a and b. a=1 b=2
			String function = argv.remove( 0 );
			argv.remove( 0 ); // of
			//audit.log( "substituting "+ function +" params with: " +  argv.toString());
			rc = Number.getNumber( substitute( function, divvy( argv, "and" )).listIterator() ).valueOf();
			if (rc.equals( Number.NotANumber ))
				rc = argv.toString();
		} else
			audit.ERROR( "Unknown "+ NAME +".interpret() command: "+ cmd );
	
		return audit.out( rc );
	}
	public static void main( String args[]) {
		Overlay.Set( Overlay.Get());
		if (!Overlay.autoAttach())
			audit.ERROR( "Ouch!" );
		else {
			audit.log( "one> "+ interpret( new Strings( "this won't work!" )));
			
			new Value( "sum", "formals" ).set( "a b" );
			new Value( "sum", "body"       ).set( "a + b" );
			new Value( "product", "formals" ).set( "x y" );
			new Value( "product", "body"       ).set( "x times y" );
			//audit.log( "two> where "+ interpret( new Strings( "match sum 1 and 2" )));
			//audit.log( "three> eval: "+ interpret( new Strings( "substitute sum of 1 and 2" )));
			audit.log( "eval> answer: "+ interpret( new Strings( "eval sum of 3 and 2" )));
			audit.log( "eval> answer: "+ interpret( new Strings( "eval product of 3 and 2" )));
			audit.log( "eval> answer: "+ interpret( new Strings( "eval product of c and b" )));
}	}	}
