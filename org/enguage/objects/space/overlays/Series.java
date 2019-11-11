package org.enguage.objects.space.overlays;

import java.io.File;

import org.enguage.objects.space.Link;
import org.enguage.util.Audit;
import org.enguage.util.sys.Fs;

public class Series { // relates to hypothetical attachment of series of overlays to fs 
	static private Audit audit = new Audit( "Series" );

	static public  final String     DEFAULT = "Enguage"; //"sofa";
	static private final String    DETACHED = "";
	
	static private String series = DETACHED;
	static private void   name( String nm ) { if (nm != null) series = nm; }
	static public  String name() { return series; }
	
	static private String path( String p ) { return Os.home() + File.separator + p; }
	static private String nthName( String nm, int vn ) { return path( nm ) +"."+ vn; }
	static public  String nth( int vn ) { return nthName( series, vn );}
	
	static public  String  base() { return detached() ? DETACHED : Link.toString( path( series ));}
	
	static public  boolean detached( String nm ) { return nm.equals( DETACHED );}
	static public  boolean detached() { return detached( series );}
	static public  boolean attached() { return !detached();}
	
	static         boolean create( String nm, String path ) { return Link.fromString( path( nm ), path );}
	static private boolean create( String path ) { return create( series, path );}
	
	static public  boolean attach( String name ) {
		audit.in( "attach", "series="+ name );
		name( name );
		count();
		return audit.out( existing() || create( System.getProperty( "user.dir" )));
	}
	static public void detach() {
		name( DETACHED );
		highest = -1;
		number = 0;
	}
	
	static public boolean existing() { return !detached() && Fs.exists( path( series ) + Link.EXT );}
	static public void append() {
		if (!new File( nth( ++highest )).mkdirs())
			highest--;
		else
			number++;
	}
	static public void remove() {
		if (highest > 0 && attached()) {
			if (Fs.destroy( Series.nth( highest ) )) {
				while (highest > -1 && !Fs.exists( nth( highest )))
					highest--;
				number--;
	}	}	}

	// ---
	static private int  number = 0; // series.0, series.1, ..., series.n => 1+n
	static public  int  number() { return number; } // initialised to 1, for 0th overlay
	
	static private int  highest = -1; // series.6, series.7, ..., series.m => m
	static public  int  highest() { return highest; } // initialised to 1, for 0th overlay
	
	static public void count() {
		highest = -1;
		number = 0;
		String candidates[] = new File( Os.home() ).list();
		if (candidates != null) for (String candidate : candidates) {
			String[] cands = candidate.split("\\.");
			if (cands.length == 2 && cands[0].equals( series ))
				try {	
					int n = Integer.parseInt( cands[ 1 ]);
					number++;
					if (n > highest) highest = n;
				} catch (Exception ex){}
		}
	}
	public String toString() { return name();}
	
	static public void main( String argv[] ) {
		Audit.allOn();
		Series s = new Series();
		attach( "enguage" );
		Audit.log( "Series is "+ s.toString());
}	}

