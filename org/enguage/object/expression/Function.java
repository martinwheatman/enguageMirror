package org.enguage.object.expression;

import java.util.ListIterator;

import org.enguage.Enguage;
import org.enguage.object.Variable;
import org.enguage.object.space.Overlay;
import org.enguage.util.Attribute;
import org.enguage.util.Audit;
import org.enguage.util.Shell;
import org.enguage.util.Strings;
import org.enguage.vehicle.Number;
import org.enguage.vehicle.Reply;

public class Function {
	
	static public  String  NAME = "function";
	static private Audit  audit = new Audit( "Function" );
	
	public Function( String nm ) { name = nm; } // find
	public Function( String nm, Strings params, String body ) {
		this( nm );
		lambda = new Lambda( this, params, body );
	}
	
	private String   name = "";
	public  String   name() { return name; }
	
	private Lambda lambda = null;
	
	static private String create( String name, Strings args ) {
		// args=[ "x and y", "/", "body='x + y'" ]
		audit.in( "create", "name="+ name +", args="+ args.toString( Strings.DQCSV ));
		
		Strings params = new Strings();
		ListIterator<String> si = args.listIterator();
		
		Strings.getWords( si, "/", params ); // added in perform call
		params = params.normalise(); // "x and y" => "x", "and", "y"
		params.remove( params.size()-1 ); // remove '/'
		if (params.size() > 2)
			params.remove( params.size()-2 ); // remove 'and'
		
		audit.debug( "creating: "+ params +"/"+ name );
		new Function( name, params, Attribute.getAttribute( si ).value() );
		
		return audit.out( Shell.SUCCESS );
	}
	public String toString() {
		return name + (lambda == null ? "<noLambda/>" : lambda.toString());
	}
	static private Function getFunction( String name, Strings actuals ) {
		audit.in( "getFunction", name +", "+ actuals.toString("[", ", ", "]"));
		Function fn = new Function( name );
		fn.lambda = new Lambda( name, actuals ); // this is a 'find', body="" == !found
		if (fn.lambda.body().equals( "" )) {
			audit.debug( "FUNCTION: no body found for "+ actuals +"/"+ name );
			fn = null;
		}
		return (Function) audit.out( fn );
	}
	static private Strings substitute( String function, Strings actuals ) {
		audit.in( "substitute", "Function="+ function +", argv="+ actuals.toString( Strings.DQCSV ));
		Strings ss = null;
		Function f = getFunction( function, actuals );
		if (f != null)
			ss = new Strings( f.lambda.body() )
					.substitute(
						new Strings( f.lambda.signature() ), // formals
						actuals.derefVariables() );
		return audit.out( ss );
	}
	static private String evaluate( String name, Strings argv ) {
		audit.in(  "evaluate", name +":"+ argv.toString( Strings.DQCSV ));
		String  rc = Reply.dnk();
		Strings ss = substitute( name, argv.divvy( "and" ));
		if (ss != null) {
			rc = Number.getNumber( ss.listIterator()).valueOf();
			if (rc.equals( Number.NotANumber ))
				rc = Reply.dnk();
		}
		return audit.out( rc );
	}
	static public String interpret( String arg ) { return interpret( new Strings( arg ));}
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
	// === test code below! ===
	static private void testCreate( String fn, String formals, String body ) {
		audit.log( "The "+ fn +" of "+ formals +" is "+ body );
		interpret(
					new Strings( "create "+ fn +" "+ formals +" / "+
							     Attribute.asString( "body", body )
				 )			   );
	}
	static private void testQuery( String fn, String actuals ) {
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
			testQuery(  "sum", "1 , 1" ); // error!
			
			testCreate( "sum", "a and b", "a + b" );
			testQuery(  "sum", "3 and 2" );
			
			testCreate( "sum", "a b c and d", "a + b + c + d" );
			testQuery(  "sum", "4 and 3 and 2 and 1" );
			
			audit.log( "setting x to 1" );
			Variable.set( "x", "1" );
			audit.log( "setting y to 2" );
			Variable.set( "y", "2" );
			testQuery(  "sum", "x and y" );
			
			testCreate( "factorial", "1", "1" );
			testQuery(  "factorial", "1" );
			//testQuery(  "factorial", "4" );
			audit.log( "PASSED" );
}	}	}
