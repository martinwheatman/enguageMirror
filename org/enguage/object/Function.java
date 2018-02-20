package org.enguage.object;

import java.util.ListIterator;

import org.enguage.Enguage;
import org.enguage.util.Audit;
import org.enguage.util.Number;
import org.enguage.util.Shell;
import org.enguage.util.Strings;

public class Function {
	static public  String  NAME = "function";
	static private Audit  audit = new Audit( "Function" );
	
	public Function( String nm ) { name = nm; }
	public Function( String nm, Strings parameters, String body ) {
		this( nm );
		lambda( parameters, body );
	}
	
	private String   name;
	public  String   name() { return name; }
	
	private Strings formals = null;
	public  String  formals() { return formals.toString(); }
	public  void    formals( Strings params ) { formals = params; }
	
	public  String     body() { return lambda.getAsString();}
	
	private Value    lambda = null;
	public  Function lambda( Strings parameters, String body ) {
		if (null == lambda)
			formals( parameters );
			lambda = new Value( name, parameters.toString( Strings.CSV ) + ".txt" );
		lambda.set( body );
		return this;
	}
	
	public  String toString() {return "FUNCTION "+ name +"( "+ formals() +" ) "+ body();}
	
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
	private static boolean match( Strings names, Strings values ) {
		if (names.size() != values.size())
			return false;
		ListIterator<String> ni = names.listIterator();
		ListIterator<String> vi = values.listIterator();
		while (ni.hasNext()) {
			String n = ni.next();
			String v = vi.next();
			if (!n.equals( v )) { // height =/= height
				String s = Variable.get( v );
				if (s != null && s.equals( n )) // height =/=194
					return false;
			}
		}
		return true;
	}
	private static Function getFunction( String name, Strings values ) {
		// ("area", ["height", "width"])
		audit.in( "getFunction", name +", "+ values.toString("[", ", ", "]"));
		Function fn = new Function( name );
		String body = "";
	
		Strings dirEnts = Enguage.e.o.list( name );
		Strings params = null;
		if (null != dirEnts) for (String fname : dirEnts) {
			audit.log( "Dname: "+ fname );
			Strings tmp = new Strings( fname, '.' );
			     params = new Strings( tmp.get(0), ',' );
			if (match( params, values )) {
				audit.log( ">>>getting body: "+ name +", "+ fname );
				body = new Value( name, fname ).getAsString();
				audit.log( ">>>body   is   : "+ body );
				audit.log( ">>>params are  : "+ params );
				break;
		}	}
		// getBody
		if (params == null) params = values;
		fn.lambda( params, body );
		//return(Function) audit.out( fn.lambda( new Strings( "x y" ), "x + y" )); // this is what we're trying to build!
		return (Function) audit.out( fn );
	}
	static private Strings substitute( String function, Strings argv ) {
		// takes: "sum", ["3", "4"]
		audit.in( "substitue", "Function="+ function +", argv="+ argv.toString( "[",",","]") );
		
		Function f = getFunction( function, argv ); // e.g. sum 2 => sum/x,y.txt
		
		audit.log( "f="+ f.name() +", formals="+ f.formals() +", body="+ f.body());
		Strings ss = new Strings( f.body() ) // body
							.substitute(
									new Strings( f.formals() ), // formals
									argv );
//		Strings ss = new Strings( new Value( function, BNAME ).getAsString() ) // body
//		.substitute(
//				new Strings( new Value( function, FNAME ).getAsString() ), // formals
//				actuals );
		// returns: ["a", "+", "b"].substitute( ["a", "b"], ["3", "4"] );
		//          ["3", "+", "4"]!!!
		return audit.out( ss );
	}
	static public String evaluate( String name, Strings argv ) {
		audit.in(  "evaluate", argv.toString( Strings.DQCSV ));
		String rc = argv.toString();
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
			String      cmd = argv.remove( 0 ),
			       function = argv.remove( 0 );
			argv = argv.normalise().contract( "=" );
	
			if (cmd.equals( "create" )) {
				// [function] "create", "sum",  "a", "b", "/", "body", "=", "'a + b'"
				rc = create( function, argv );
				
			} else if (cmd.equals( "evaluate" )) {
				// [function] "evaluate", "sum", "3", "and", "4"
				rc = "The "+ function +" of "+ argv +" is "+ evaluate( function, argv );
				
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
		Enguage.e = new Enguage();
		Overlay.Set( Overlay.Get());
		if (!Overlay.autoAttach())
			audit.ERROR( "Ouch!" );
		else {
			Audit.traceAll( true );
			Variable.set( "x", "1" );
			Variable.set( "y", "2" );
			if (!match( new Strings( "1" ), new Strings( "1" )))
				audit.ERROR( "match fails on 1/1" );
			if (!match( new Strings( "x" ), new Strings( "1" )))
				audit.ERROR( "match fails on 1/1" );
			audit.log( "matching passes!" );
			test( "sum", "a and b", "a + b", "3 and 2" );
			//test( "sum", "a b c and d", "a + b + c + d", "4 and 3 and 2 and 1" );
			//test( "factorial", "1", "1", "6" );
}	}	}
