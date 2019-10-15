package org.enguage.objects.expr;

import java.util.ListIterator;

import org.enguage.objects.Variable;
import org.enguage.objects.space.Overlay;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.util.sys.Shell;
import org.enguage.vehicle.number.Number;
import org.enguage.vehicle.reply.Reply;

public class Function {
	
	static public  final String NAME = "function";
	static public  final int      id = 81133373; //Strings.hash( NAME );
	static private       Audit audit = new Audit( "Function" );
	
	public Function( String nm ) { name = nm; } // find
	public Function( String nm, Strings params, String body ) {
		this( nm );
		lambda = new Lambda( this, params, body );
	}
	
	private String   name = "";
	public  String   name() { return name; }
	
	private Lambda lambda = null;
	
	static private Strings create( String name, Strings args ) {
		// args=[ "x and y", "/", "body='x + y'" ]
		audit.in( "name="+ name +", args="+ args );
		Strings params = new Strings();
		ListIterator<String> si = args.listIterator();
		
		Strings.getWords( si, "/", params ); // added in perform call
		params = params.normalise(); // "x and y" => "x", "and", "y"
		params.remove( params.size()-1 ); // remove '/'
		if (params.size() > 2)
			params.remove( params.size()-2 ); // remove 'and'
		
		new Function( name, params, Attribute.getAttribute( si ).value() );
		audit.out();
		return Shell.Success;
	}
	public String toString() {return name + (lambda==null ? "<noLambda/>" : lambda.toString());}
	
	static private Function getFunction( String name, Strings actuals ) {
		audit.in( "getFunction", name +", "+ actuals.toString("[", ", ", "]"));
		Function fn = new Function( name );
		fn.lambda = new Lambda( fn, actuals ); // this is a 'find', body="" == !found
		if (fn.lambda.body().equals( "" )) {
			audit.debug( "FUNCTION: no body found for "+ actuals +"/"+ name );
			fn = null;
		}
		return (Function) audit.out( fn );
	}
	static private Strings substitute( String function, Strings actuals ) {
		audit.in( "substitute", "Function="+ function +", argv="+ actuals.toString( Strings.DQCSV ));
		Strings ss = null;
		
		// resolve actual expressions into value
		Strings tmp = new Strings();
		for (String a : actuals) {
			String b = ""+new Number( a ).valueOf();
			if (b.equals("not a number"))
				tmp.add( a );
			else
				tmp.add( b );
		}
		actuals = tmp;
		
		Function f = getFunction( function, actuals );
		if (f != null)
			ss = new Strings( f.lambda.body() )
					.substitute(
						new Strings( f.lambda.signature() ), // formals
						actuals.derefVariables() );
		return audit.out( ss );
	}
	static private Strings evaluate( String name, Strings argv ) {
		audit.in( "evaluate", "name="+ name +", args='"+ argv +"'" );
		Strings  rc = Reply.dnk();
		Strings ss = substitute( name, argv.divvy( "and" ));
		if (ss != null) {
			rc = new Number( ss.listIterator() ).valueOf();
			if (rc.equals( Number.NotANumber ))
				rc = Reply.dnk();
		}
		return audit.out( rc );
	}
	static public Strings interpret( String arg ) { return interpret( new Strings( arg ));}
	static public Strings interpret( Strings argv ) {
		audit.in( "interpret", argv.toString( Strings.DQCSV ));
		Strings rc = Shell.Fail;
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
			
			else
				audit.ERROR( "Unknown "+ NAME +".interpret() command: "+ cmd );
		}
		return (Strings) audit.out( rc );
	}
	// === test code below! ===
	static private void testCreate( String fn, String formals, String body ) {
		Audit.log( "The "+ fn +" of "+ formals +" is "+ body );
		interpret(
					new Strings( "create "+ fn +" "+ formals +" / "+
							     Attribute.asString( "body", body )
				 )			   );
	}
	static private void testQuery( String fn, String actuals ) {
		Audit.log( "What is the "+ fn +" of "+ actuals );
		Strings eval = interpret( new Strings("evaluate "+ fn +" "+ actuals ));
		Audit.log( eval.equals( new Strings( Reply.dnk())) ?
			eval.toString() : "The "+ fn +" of "+ actuals +" is "+ eval.toString() +"\n" );
	}
	static public void main( String args[]) {
		Overlay.Set( Overlay.Get());
		if (!Overlay.autoAttach())
			audit.ERROR( "Ouch!" );
		else {
			Reply.dnk( "I do not know\n" );
			testQuery(  "sum", "1 , 1" ); // error!
			
			testCreate( "sum", "a and b", "a + b" );
			testQuery(  "sum", "3 and 2" );
			
			testCreate( "sum", "a b c and d", "a + b + c + d" );
			testQuery(  "sum", "4 and 3 and 2 and 1" );
			
			Audit.log( "setting x to 1" );
			Variable.set( "x", "1" );
			Audit.log( "setting y to 2" );
			Variable.set( "y", "2" );
			testQuery(  "sum", "x and y" );
			
			testCreate( "factorial", "1", "1" );
			testQuery(  "factorial", "1" );
			//testQuery(  "factorial", "4" );
			Audit.log( "PASSED" );
}	}	}
