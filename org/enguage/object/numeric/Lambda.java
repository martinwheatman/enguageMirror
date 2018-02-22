package org.enguage.object.numeric;

import java.util.ListIterator;

import org.enguage.Enguage;
import org.enguage.object.Value;
import org.enguage.object.Variable;
import org.enguage.object.space.Overlay;
import org.enguage.util.Audit;
import org.enguage.util.Strings;

public class Lambda {
	static private Audit  audit = new Audit( "Lambda" );

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
	public Lambda( String name, Strings params, String body ) { // new
		sig = params;
		new Value( name,
				params.toString( Strings.CSV ) + ".txt" );
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
	
	public void main( String args[] ) {
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
		}
}	}
