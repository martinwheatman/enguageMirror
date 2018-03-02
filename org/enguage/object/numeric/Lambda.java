package org.enguage.object.numeric;

import java.util.ListIterator;

import org.enguage.Enguage;
import org.enguage.object.Value;
import org.enguage.object.Variable;
import org.enguage.object.space.Overlay;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.Number;

public class Lambda {
	static private Audit audit = new Audit( "Lambda" );

	public Lambda( Function f, Strings params, String body ) { // new
		sig = params;
		new Value(
				f.name(),
				sig.toString( Strings.CSV ) + ".lambda"
			).set( body );
	}
	public Lambda( String name, Strings values ) { // find
		Strings fnames = Enguage.e.o.list( name );
		if (null != fnames) for (String fname : fnames) {
			sig = new Strings( new Strings( fname, '.' ).get(0), ',' );
			if (match( sig, values )) {
				body = new Value( name, fname ).getAsString();
				break; // can we revisit?
	}	}	}
	
	private Strings sig = null;
	public  Strings sig() { return sig; }
	
	private String body = "";
	public  String body() { return body; }
	public  Lambda body( String b ) { body = b; return this; };
	
	public String toString() { return "( "+ sig +" ) "+ body;}
	
	private static boolean match( Strings names, Strings values ) {
		boolean rc = false;
		audit.in( "match", "names="+ names +", values="+ values );
		if (names.size() == values.size()) {
			rc = true;
			ListIterator<String> ni = names.listIterator(),
					vi = values.listIterator();
			while (rc && ni.hasNext()) {
				String n = ni.next(),
				       v = vi.next();
				// if name is numeric we must match this value
				rc = Number.isNumeric( n ) ? // nmae=1 => value=1 !
						n.equals( v ) :
				     Number.isNumeric( v ) ? // value = xxx, deref
						null == Variable.get( v ) : true;
			}	}
		return audit.out( rc );
	}
	
	static public void main( String args[] ) {
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
			audit.log( "passed" );
}	}	}
