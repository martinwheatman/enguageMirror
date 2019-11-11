package org.enguage.objects.space.overlays;

import java.io.File;

import org.enguage.objects.space.Link;
import org.enguage.util.Audit;
import org.enguage.util.sys.Fs;

public class Series { // relates to hypothetical attachment of series of overlays to fs 
	
	static public  final String     DEFAULT = "Enguage"; //"sofa";
	static private final String    DETACHED = null;
	
	static private String name = DETACHED;
	static private void   name( String nm ) { if (nm != null) name = nm; }
	static public  String name() { return name; }
	
	static private int    number = 0; // series.0, series.1, ..., series.n => 1+n
	static public  int    number() { return number; } // initialised to 1, for 0th overlay
	
	static public void    count() {
		number = 0;
		String candidates[] = new File( Os.home() ).list();
		if (candidates != null) for (String candidate : candidates) {
			String[] cands = candidate.split("\\.");
			if (cands.length == 2 && cands[0].equals( name ))
				try {	
					Integer.parseInt( cands[ 1 ]);
					number++;
				} catch (Exception ex){}
	}	}
	
	// ---
	static private String  path( String p ) { return Os.home() + p; }
	static public  String  nth( int vn ) { return path( name ) +"."+ vn;}
	
	static public  String  base() { return detached() ? "" : Link.toString( path( name ));}
	
	static private boolean detached() { return name == DETACHED;}
	static public  void detach() {
		name( DETACHED );
		number = 0;
	}
	
	static public  boolean attached() { return name != DETACHED;}
	static public  boolean attach( String nm ) {
		name( nm );
		count();
		return existing() || Link.fromString( path( nm ), System.getProperty( "user.dir" ));
	}
	
	static public  boolean existing() { return !detached() && Fs.exists( path( name ) + Link.EXT );}
	static public  void      append() { if (attached()) new File( nth( number++ )).mkdirs(); }
	static public  boolean   remove() { return number >= 0 && attached() && Fs.destroy( Series.nth( --number ));}

	public String toString() { return name();}
	
	static public void main( String argv[] ) {
		Audit.allOn();
		Series s = new Series();
		attach( "enguage" );
		Audit.log( "Series is "+ s.toString());
}	}