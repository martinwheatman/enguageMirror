package org.enguage.object.expression;

import java.util.ListIterator;

import org.enguage.Enguage;
import org.enguage.object.Attribute;
import org.enguage.object.Variable;
import org.enguage.object.space.Overlay;
import org.enguage.util.Audit;
import org.enguage.util.Number;
import org.enguage.util.Shell;
import org.enguage.util.Strings;
import org.enguage.vehicle.Reply;

public class Function {
	
	static public  String  NAME = "function";
	static private Audit  audit = new Audit( "Function" );
	
	// TODO: should be a tree!
	static private Strings list = new Strings();
	static public  void    functionIs( String fn ) { list.append( fn );}
	static public  boolean isFunction( String fn ) { return list.contains( fn );}
	
	public Function( String nm ) { name = nm; } // find
	public Function( String nm, Strings params, String body ) {
		this( nm );
		lambda = new Lambda( this, params, body );
	}
	
	private String   name;
	public  String   name() { return name; }
	
	private Lambda lambda = null;
	
	public  String toString() { return NAME +" "+ name + lambda==null ? "<null>" : lambda.toString();}
	
	static public String create( String name, Strings args ) {
		// args=[ "x and y", "/", "body='x + y'" ]
		audit.in( "create", "name="+ name +", args="+ args.toString( Strings.DQCSV ));
		
		Strings params = new Strings();
		ListIterator<String> si = args.listIterator();
		
		Strings.getWords( si, "/", params ); // added in perform call
		params = params.normalise(); // "x and y" => "x", "and", "y"
		params.remove( params.size()-1 ); // remove '/'
		if (params.size() > 2)
			params.remove( params.size()-2 ); // remove 'and'
		
		new Function( name, params, Attribute.getAttribute( si ).value() );
		
		return audit.out( Shell.SUCCESS );
	}
	private static Function getFunction( String name, Strings values ) {
		audit.in( "getFunction", name +", "+ values.toString("[", ", ", "]"));
		Function fn = new Function( name );
		fn.lambda = new Lambda( name, values );
		if (fn.lambda.body().equals( "" ))
			fn = null;
		return (Function) audit.out( fn );
	}
	static private Strings substitute( String function, Strings argv ) {
		audit.in( "substitute", "Function="+ function +", argv="+ argv.toString( "[",",","]") );
		Strings ss = null;
		Function f = getFunction( function, argv );
		if (f != null)
			ss = new Strings( f.lambda.body() )
					.substitute(
						new Strings( f.lambda.sig() ), // formals
						argv.derefVariables() );
		return audit.out( ss );
	}
	static public String evaluate( String name, Strings argv ) {
		audit.in(  "evaluate", argv.toString( Strings.DQCSV ));
		String  rc = Reply.dnk();
		Strings ss = substitute( name, argv.divvy( "and" ));
		if (ss != null) {
			rc = Number.getNumber( ss.listIterator()).valueOf();
			if (rc.equals( Number.NotANumber ))
				rc = Reply.dnk();
		}
		return audit.out( rc );
	}
	static public String interpret( Strings argv ) {
		audit.in( "interpret", argv.toString( Strings.DQCSV ));
		String  rc = Shell.FAIL;
		if (argv.size() >= 2) {
			String      cmd = argv.remove( 0 ),
			       function = argv.remove( 0 );
			argv = argv.normalise().contract( "=" );
	
			if (cmd.equals( "create" ))
				// [function] "create", "sum", "a", "b", "/", "body='a + b'"
				rc = create( function, argv );
				
			else if (cmd.equals( "evaluate" ))
				// [function] "evaluate", "sum", "3", "and", "4"
				rc = evaluate( function, argv );
			/* TODO:
			where does this get set as an answer to be replaced by whatever?
			set it as a default/class answer to be used if answer is still blank?
			*/
			else
				audit.ERROR( "Unknown "+ NAME +".interpret() command: "+ cmd );
		}
		return audit.out( rc );
	}
	// test code below!
	static private void create( String fn, String formals, String body ) {
		audit.log( "The "+ fn +" of "+ formals +" is "+ body );
		interpret( new Strings( "create "+ fn +" "+ formals +" / body='"+ body +"'" ));
	}
	static private void query( String fn, String actuals ) {
		audit.log( "What is the "+ fn +" of "+ actuals );
		String eval = interpret( new Strings("evaluate "+ fn +" "+ actuals ));
		audit.log( eval.equals( Reply.dnk()) ?
			eval : "The "+ fn +" of "+ actuals +" is "+ eval +"\n" );
	}
	static public void main( String args[]) {
		Enguage.e = new Enguage();
		Overlay.Set( Overlay.Get());
		if (!Overlay.autoAttach())
			audit.ERROR( "Ouch!" );
		else {
			Reply.dnk( "I do not know" );
			//Audit.traceAll( true );
			query(  "sum", "1 , 1" );
			
			create( "sum", "a and b", "a + b" );
			query(  "sum", "3 and 2" );
			
			create( "sum", "a b c and d", "a + b + c + d" );
			query(  "sum", "4 and 3 and 2 and 1" );
			
			audit.log( "setting x to 1" );
			Variable.set( "x", "1" );
			audit.log( "setting y to 2" );
			Variable.set( "y", "2" );
			query(  "sum", "x and y" );
			
			create( "factorial", "1", "1" );
			query(  "factorial", "6" );
}	}	}
