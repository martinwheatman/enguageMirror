package org.enguage.object;

import java.util.ListIterator;

import org.enguage.util.Audit;
import org.enguage.util.Number;
import org.enguage.util.Shell;
import org.enguage.util.Strings;
import org.enguage.vehicle.Reply;

public class Function {
	static public  String  NAME = "function";
	static private Audit  audit = new Audit( "Function" );
	
	static private String BNAME = "body.txt";
	static private String FNAME = "formals.txt";
	
	public Function( String nm ) {
		name    = nm;
		formals = new Value( name, FNAME );
		body    = new Value( name, BNAME );
	}
	
	private Value    body;
	public  Function body( String b ) { body.set( b ); return this;}
	public  String   body() { return body.getAsString();}
	
	private Value    formals = null;
	public  Function formals( String f ) { formals.set( f ); return this; }
	public  String   formals() { return formals.getAsString(); }
	
	private String   name;
	public  String   name() { return name;}
	
	static public String create( String name, Strings args ) {
		audit.in( "create", "name="+ name +", args="+ args.toString( Strings.DQCSV ));
		// args=[ "x", "y", "/", "x", "+", "y" ]
		Strings params = new Strings();
		ListIterator<String> si = args.listIterator();
		Strings.getWords( si, "/", params );
		params = params.normalise();
		params.remove( params.size()-1 );
		if (params.size() > 2)
			params.remove( params.size()-2 );
		
		new Function( name )
				.formals( params.toString() )
				.body( Attribute.getAttribute( si ).value() );
		
		return audit.out( Shell.SUCCESS );
	}
	static private Strings substitute( String function, Strings actuals ) {
		// takes: "sum", ["3", "4"]
		audit.in( "substitue", "Function="+ function +", actuals="+ actuals );
		Function f = new Function( function );
		audit.log( "f="+ f.name() +", formals="+ f.formals() +", body="+ f.body());
		Strings ss = new Strings( new Value( function, BNAME ).getAsString() ) // body
				.substitute(
						new Strings( new Value( function, FNAME ).getAsString() ), // formals
						actuals );
		// returns: ["a", "+", "b"].substitute( ["a", "b"], ["3", "4"] );
		//          ["3", "+", "4"]!!!
		return audit.out( ss );
	}
	public String evaluate( String arg ) { return evaluate( new Strings( arg ));}
	public String evaluate( Strings argv ) {
		audit.in(  "evaluate", argv.toString( Strings.DQCSV ));
		String rc = Reply.dnk();
		// where function="factorial"
		//   and argv=[ "a", "b", "and", "c" ], OR [ 'a', 'and', 'b', 'and', c' ]
		Strings ss = substitute( name, argv.divvy( "and" ));
		if (ss != null) {
			rc = Number.getNumber( ss.listIterator()).valueOf();
			if (rc.equals( Number.NotANumber ))
				rc = argv.toString();
		}
		return audit.out( rc );
	}
	static public String interpret( Strings argv ) {
		audit.in( "interpret", argv.toString( Strings.DQCSV ));
		String  rc = Shell.FAIL;
		if (argv.size() >= 2) {
			String cmd = argv.remove( 0 );
			String function = argv.remove( 0 );
	
			if (cmd.equals( "create" )) {
				// [function] "create", "sum",  "a", "b", "/", "body", "=", "'a + b'"
				rc = create( function, argv.contract( "=" ) );
				
			} else if (cmd.equals( "evaluate" )) {
				// [function] "evaluate", "sum", "3", "and", "4"
				rc = new Function( function ).evaluate( argv.normalise() );
				
			} else 
				audit.ERROR( "Unknown "+ NAME +".interpret() command: "+ cmd );
		}
		return audit.out( rc );
	}
	static private String test( String fn, String formals, String body, String actuals ) {
		interpret( new Strings( "create "+ fn +" "+ formals +" / body='"+ body +"'" ));
		return interpret( new Strings("evaluate "+ fn +" "+ actuals ));
	}
	static public void main( String args[]) {
		Overlay.Set( Overlay.Get());
		if (!Overlay.autoAttach())
			audit.ERROR( "Ouch!" );
		else {
			Audit.traceAll( true );
			test( "sum", "a and b", "a + b", "3 and 2" );
			test( "sum", "a b c and d", "a + b + c + d", "4 and 3 and 2 and 1" );
			test( "factorial", "1", "1", "6" );
}	}	}
