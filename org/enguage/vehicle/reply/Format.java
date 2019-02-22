package org.enguage.vehicle.reply;

import org.enguage.interp.Context;
import org.enguage.objects.Variable;
import org.enguage.util.Audit;
import org.enguage.util.Strings;

public class Format {
	/** Format:
	 * e.g. ["Ok", ",", "you", "need", Strings.ELLIPSIS"]
	 */
	
	//static private Audit audit = new Audit( "Fmt" );
	
	private boolean shrt = false;
	public  boolean shrt() { return shrt; }
	public  void    shrt( boolean b ) { shrt = b; }
	
	private boolean variable = false;
	public  boolean variable() {return variable;}
	
	private Strings format = new Strings();
	public  Strings ormat() { return format; }
	public  void    ormat( Strings sa ) {
		variable = sa.size() > 0 && sa.get(0).equals( Strings.ELLIPSIS );
		// Decontextualise format, while we're in the moment!
		format = Context.deref( sa );
	}
	static public void main( String args[] ) {
		Format f = new Format();
		Variable.set( "FMTEST", "martin" );
		f.ormat( new Strings( "FMTEST needs "+ Strings.ELLIPSIS ));
		Audit.log( "fmt: "+ f.ormat());
}	}
