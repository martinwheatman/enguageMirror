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
	public  Strings ormat() {return ormat;}
	public  void    ormat(Strings s) {
		ormat = s;
		if (ormat.contains("...")) v = true;
	}
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
