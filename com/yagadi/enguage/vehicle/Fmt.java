package com.yagadi.enguage.vehicle;

import com.yagadi.enguage.object.Variable;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Strings;

public class Fmt {
	/** Fmt:
	 * e.g. ["Ok", ",", "you", "need", Strings.ELLIPSIS"]
	 */
	
	static private Audit audit = new Audit( "Fmt" );
	
	private boolean shrt = false;
	public  boolean shrt() { return shrt; }
	public  void    shrt( boolean b ) { shrt = b; }
	
	private boolean variable = false;
	public  boolean variable() {return variable;}
	
	private Strings ormat = new Strings();
	public  Strings ormat() { return ormat; }
	public  void    ormat(String s) {
		ormat = new Strings( s );
		if (ormat.get(0).equals( Strings.ELLIPSIS )) variable = true;
	}
	static public void main( String args[] ) {
		Fmt f = new Fmt();
		Variable.set( "FMTEST", "martin" );
		f.ormat( "FMTEST needs "+ Strings.ELLIPSIS );
		audit.log( "fmt: "+ f.ormat());
}	}
