package com.yagadi.enguage.expression;

import com.yagadi.enguage.sofa.Variable;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Strings;

public class Fmt {
	/** Fmt:
	 * e.g. ["Ok", ",", "you", "need", "..."]
	 */
	
	static private Audit audit = new Audit( "Fmt" );
	
	private boolean shrt = false;
	public  boolean shrt() { return shrt; }
	public  void    shrt( boolean b ) { shrt = b; }
	
	private boolean v = false;
	public  boolean variable() {return v;}
	
	private Strings ormat = new Strings();
	/*private Strings format() {
		if (!verbose()) {
			if (f.ormat().size() > 1 && f.ormat().get( 1 ).equals( "," ))
				if (f.ormat().get( 0 ).equalsIgnoreCase( yes ) || // yes is set to "OK", when yes is "yes", this fails...
					f.ormat().get( 0 ).equalsIgnoreCase(  no ) ||
					f.ormat().get( 0 ).equalsIgnoreCase( success )) {
					return new Strings( say() + " " +f.ormat().get( 0 ));
				} else if (f.ormat().get( 0 ).equalsIgnoreCase( failure )) {
					return new Strings( say()).append( f.ormat().copyAfter( 1 ).filter()); // forget 1st & 2nd
				}
			return new Strings( say()).append( f.ormat().filter( ));
		}
		return f.ormat( );
	}*/
	public  Strings ormat() {return ormat;}
	public  void    ormat(String s) {
		ormat = Colloquial.applyOutgoing(
					Context.deref(
						Variable.deref(
							new Strings( s )
				)	)	);		
		if (ormat.contains("...")) v = true;
	}
	static public void main( String args[] ) {
		Fmt f = new Fmt();
		f.ormat( "SUBJECT needs ..." );
		audit.log( "fmt: "+ f.ormat());
}	}
