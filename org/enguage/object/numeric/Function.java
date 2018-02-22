package org.enguage.object.numeric;

import java.util.ListIterator;

import org.enguage.Enguage;
import org.enguage.object.Attribute;
import org.enguage.object.Value;
import org.enguage.object.Variable;
import org.enguage.object.space.Overlay;
import org.enguage.util.Audit;
import org.enguage.util.Number;
import org.enguage.util.Shell;
import org.enguage.util.Strings;
import org.enguage.vehicle.Reply;

public class Function {
	
	class Lambda {
		public Lambda() {} // find
		public Lambda( Strings params, String body ) { // create
			sig = params;
			v = new Value( name,
					params.toString( Strings.CSV ) + ".txt" );
		}
		private Value v = null;
		
		private Strings sig = null;
		public  Strings sig() { return sig; }
		public  Lambda  sig( Strings s ) { sig = s; return this; }
		
		private String body = "";
		public  String body() { return body; }
		public  Lambda body( String b ) { body = b; return this; };
		
		public void set( Strings params, String body ) {
			if (null == v) {
				sig = params;
				v = new Value( name,
						params.toString( Strings.CSV ) + ".txt" );
			}
			v.set( body ); // writes v!
		}
		public String get() { return v.getAsString();}
		public String toString() { return "( "+ sig +" ) "+ body;}
	}
	
	static public  String  NAME = "function";
	static private Audit  audit = new Audit( "Function" );
	
	public Function( String nm ) { name = nm; } // find
	public Function( String nm, Strings params, String body ) {
		this( nm );
		lambda.set( params, body );
	}
	
	private String   name;
	public  String   name() { return name; }
	
	private Lambda lambda = new Lambda();
	
	public  String toString() { return "FUNCTION "+ name + lambda.toString();}
	
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
		boolean rc = false;
		audit.in( "match", "names="+ names +", values="+ values );
		if (names.size() == values.size()) {
			rc = true;
			ListIterator<String> ni = names.listIterator(),
			                     vi = values.listIterator();
			while (rc && ni.hasNext()) {
				String n = ni.next(), s,
				       v = vi.next();
				if (!n.equals( v )) { // height != height -- numeric???
					if (null != (s = Variable.get( v ))
							&& s.equals( n ))  { // e.g. height != 194
						audit.log( "failing on match" );
						rc = false;
		}	}	}	}
		return audit.out( rc );
	}
	private static Function getFunction( String name, Strings values ) {
		// ("area", ["height", "width"])
		audit.in( "getFunction", name +", "+ values.toString("[", ", ", "]"));
		Function fn = null;
		String     body = "";
		Strings  params = null,
		        dirEnts = Enguage.e.o.list( name );
		// get params and body (lambda)
		if (null != dirEnts)
			for (String fname : dirEnts) {
				Strings tmp = new Strings( fname, '.' );
				     params = new Strings( tmp.get(0), ',' );
				if (match( params, values )) {
					body = new Value( name, fname ).getAsString();
					break; // can we revisit?
			}	}

		if (params != null) {
			fn = new Function( name );
			fn.lambda.sig( params );
			fn.lambda.body( body );
		}
		return (Function) audit.out( fn );
	}
	static private Strings substitute( String function, Strings argv ) {
		// takes: "sum", ["3", "4"]
		audit.in( "substitue", "Function="+ function +", argv="+ argv.toString( "[",",","]") );
		Strings ss = null;
		Function f = getFunction( function, argv ); // e.g. sum 2 => sum/x,y.txt
		if (f != null)
			ss = new Strings( f.lambda.body() )
							.substitute(
									new Strings( f.lambda.sig ), // formals
									argv );
		return audit.out( ss );
	}
	static public String evaluate( String name, Strings argv ) {
		audit.in(  "evaluate", argv.toString( Strings.DQCSV ));
		String  rc = Reply.dnk(); //argv.toString();
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
				// [function] "create", "sum", "a", "b", "/", "body='a + b'"
				rc = create( function, argv );
				
			} else if (cmd.equals( "evaluate" )) {
				// [function] "evaluate", "sum", "3", "and", "4"
				rc = evaluate( function, argv );
				
			} else
				audit.ERROR( "Unknown "+ NAME +".interpret() command: "+ cmd );
		}
		return audit.out( rc );
	}
	static private void test( String fn, String formals, String body, String actuals ) {
		interpret( new Strings( "create "+ fn +" "+ formals +" / body='"+ body +"'" ));
		String eval = interpret( new Strings("evaluate "+ fn +" "+ actuals ));
		audit.log( eval.equals( Reply.dnk()) ?
			eval :
			"The "+ fn +" of "+ actuals +" is "+ eval );
	}
	static public void main( String args[]) {
		Enguage.e = new Enguage();
		Overlay.Set( Overlay.Get());
		if (!Overlay.autoAttach())
			audit.ERROR( "Ouch!" );
		else {
			Variable.set( "x", "1" );
			Variable.set( "y", "2" );
			if (!match( new Strings( "1" ), new Strings( "1" )))
				audit.FATAL( "match fails on 1/1" );
			if (!match( new Strings( "x" ), new Strings( "1" )))
				audit.FATAL( "match fails on x/1" );
			if (!match( new Strings( "y" ), new Strings( "2" )))
				audit.FATAL( "match fails on y/2" );
			//Audit.traceAll( true );
			audit.debug( "matching passes!" );
			test( "sum", "a and b", "a + b", "3 and 2" );
			//Audit.traceAll( false );
			test( "sum", "a b c and d", "a + b + c + d", "4 and 3 and 2 and 1" );
			test( "factorial", "1", "1", "6" );
}	}	}
