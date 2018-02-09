package org.enguage.object;

import java.util.ListIterator;

import org.enguage.util.Audit;
import org.enguage.util.Number;
import org.enguage.util.Shell;
import org.enguage.util.Strings;

public class Function {
	static public  String  NAME = "function";
	static private Audit  audit = new Audit( "Function", true );
	
	public Function( String nm ) {
		name = nm;
		formals = new Value( name, "formals" );
		body    = new Value( name, "body" );
	}
	
	private Value    body;
	public  Function body( String b ) { body.set( b ); return this;}
	public  String   body() { return body.getAsString();}
	
	private Value    formals = null;
	public  Function formals( String f ) { formals.set( f ); return this; }
	public  String   formals() { return formals.getAsString(); }
	
	private String   name;
	public  String   name() { return name;}
	
	static private Strings substitute( String function, Strings actuals ) {
		return new Strings( new Value( function, "body" ).getAsString() ) // body
				.substitute(
						new Strings( new Value( function, "formals" ).getAsString() ),
						actuals );
	}
	static public String evaluate( String function, Strings argv ) {
		// where function="factorial"
		//   and argv=[ "a", "b", "and", "c" ], OR [ 'a', 'and', 'b', 'and', c' ]
		String rc = Number.getNumber(
						substitute( function, argv.divvy( "and" )).listIterator()
					).valueOf();
		if (rc.equals( Number.NotANumber ))
			rc = argv.toString();
		return rc;
	}
	public String evaluate( String arg ) { return evaluate( new Strings( arg ));}
	public String evaluate( Strings argv ) {
		// where function="factorial"
		//   and argv=[ "a", "b", "and", "c" ], OR [ 'a', 'and', 'b', 'and', c' ]
		String rc = Number.getNumber(
						substitute( name, argv.divvy( "and" )).listIterator()
					).valueOf();
		if (rc.equals( Number.NotANumber ))
			rc = argv.toString();
		return rc;
	}
	static public String interpret( Strings argv ) {
		audit.in( "interpret", argv.toString());
		String  rc = Shell.FAIL,
		       cmd = argv.remove( 0 );

		if (cmd.equals( "eval" )) {
			// e.g. [a equals 1. b equals 2. what is the] sum of a and b
			//      [what is the] sum of 1 and 2.
			//       [what is the] sum of a and b. a=1 b=2
			String function = argv.remove( 0 );
			argv.remove( 0 ); // of
			rc = evaluate( function, argv );
		} else if (cmd.equals( "create" )) {
			String function = argv.remove( 0 );
			argv.remove( 0 ); // of
			create( function, argv  );
		} else
			audit.ERROR( "Unknown "+ NAME +".interpret() command: "+ cmd );
	
		return audit.out( rc );
	}
	static public void create( String name, Strings args ) {
		audit.log( "create: name="+ name +", args="+ args.toString());
		// args=[ "(", "x", "y", ")", "x", "+", "y" ]
		Strings repn = new Strings(),
				params = new Strings(),
				body   = new Strings();
		args.add( ";" );
		ListIterator<String> si = args.listIterator();
		Strings.getWord( si, "(", repn );
		Strings.getWords(si, ")", params );
		params.remove( params.size()-1 );
		Strings.getWords(si, ";", body );
		body.remove( body.size()-1 );
		
		audit.log( "ready to create: "+ name +"("+ params.toString( Strings.DQCSV ) +")"+ body.toString());
		new Function( name ).formals( params.toString() ).body( body.toString() );
	}
	
	static public void main( String args[]) {
		Overlay.Set( Overlay.Get());
		if (!Overlay.autoAttach())
			audit.ERROR( "Ouch!" );
		else {
			audit.log( "one> "+ interpret( new Strings( "this won't work!" )));

			create( "summ", new Strings( "( a b )  a + b" ));
			
			Function fn   = new Function( "sum"  ),
					 prod = new Function( "prod" ).formals( "a b" ).body( "a times b" ),
					 sum  = new Function( "summ"  );
			
			audit.log( "  fn.evaluate( \"3 and 4\" ) = "+   fn.evaluate( "3 and 4" ));
			audit.log( " sum.evaluate( \"3 and 2\" ) = "+  sum.evaluate( "3 and 2" ));
			audit.log( "prod.evaluate( \"3 and 4\" ) = "+ prod.evaluate( "3 and 4" ));
}	}	}
