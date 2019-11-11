package org.enguage.objects.expr;

import java.util.ListIterator;

import org.enguage.Enguage;
import org.enguage.objects.Variable;
import org.enguage.objects.space.Value;
import org.enguage.objects.space.overlays.Os;
import org.enguage.objects.space.overlays.Overlay;
import org.enguage.util.Audit;
import org.enguage.util.Strings;

public class Lambda {
	static private Audit audit = new Audit( "Lambda" );

	private static boolean match( Strings names, Strings values ) {
		boolean rc = false;
		audit.in( "match", "names="+ names +", values="+ values.toString( Strings.DQCSV ) );
		if (names.size() == values.size()) {
			rc = true;
			ListIterator<String> ni = names.listIterator(),
			                     vi = values.listIterator();
			while (rc && ni.hasNext()) {
				String n = ni.next(),
				       v = vi.next();
				// if name is numeric we must match this value
				audit.debug( "Matching "+ n +", "+ v );
				rc = Strings.isNumeric( n ) ? // name=1 => value=1 !
						n.equals( v ) :
							Strings.isNumeric( v ) ? // value = xxx, deref
								null == Variable.get( v ) : true;
			}
		} else
			audit.debug( "Lambda: name/val mis-match in params: "+ names +"/"+ values.toString( Strings.DQCSV ));
		return audit.out( rc );
	}

	public Lambda( Function f, Strings params, String body ) { // new/create
		signature = params;
		audit.debug( "creating: "+ signature.toString( Strings.CSV ) +"/"+ f.name());
		new Value(
				signature.toString( Strings.CSV ),
				f.name()
			).set( body );
	}
	public Lambda( Function fn, Strings values ) { // existing/find
		audit.in( "ctor", "finding: "+ fn +"( "+ values +" )" );
		Strings onames = Enguage.o.list( "." );
		if (null != onames) for (String formals : onames) 
			if (match( (signature = new Strings( formals, ',' )), values )
				&& !(body = new Value( formals, fn.name() ).getAsString()).equals(""))
				break; // bingo! (can we revisit if this ain't right?)
		if (body.equals(""))
			Audit.log( "Lambda: "+ fn +"/"+ values.toString( Strings.CSV ) +" not found" );
		audit.out();
	}
	
	private Strings signature = null;
	public  Strings signature() { return signature; }
	
	private String body = "";
	public  String body() { return body; }
	
	public String toString() { return "( "+ signature.toString( Strings.CSV ) +" ): {"+ body +"}";}
	
	//
	// === test code ===
	private static void matchTest( String names, String values, boolean pass ) {
		audit.in( "matchTest", names +"/"+ values );
		if (pass != match( new Strings( names ), new Strings( values )))
			audit.FATAL( "matched on "+ names +"/"+ values );
		else
			audit.debug( "matching "+ names +" with "+ values +" passed" );
		audit.out();
	}
	static public void main( String args[] ) {
		Os.Set( Os.Get());
		if (!Overlay.attachCwd( "Lambda" ))
			audit.ERROR( "Ouch!" );
		else {
			Variable.set( "x",   "1" );
			Variable.set( "y",   "2" );
			matchTest(    "1",   "1",  true );
			matchTest(    "x",   "1",  true );
			matchTest(  "x y", "1 2",  true );
			matchTest(    "x", "1 2", false ); // n vals != n names
			matchTest(  "x 1", "1 2", false ); // 1 != 2
			Audit.log(  "match tests PASSED" );
			
			//Audit.allOn();
			Audit.log( "Creating a blank function, called 'sum'..." );
			Function f = new Function( "sum" );
			Audit.log( "Creating a new lambda..." );
			new Lambda( f, new Strings( "a b" ), "a plus b" );
			Audit.log( "Finding it:" );
			Audit.incr();
			Lambda l = new Lambda( f, new Strings( "2 3" ));
			Audit.decr();
			Audit.log( "PASSED: "+ l.toString() );
}	}	}