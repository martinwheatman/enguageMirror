package org.enguage.objects.space.overlays;

import java.io.File;

import org.enguage.util.sys.Fs;
import org.enguage.util.sys.Path;

public class Os { // manages the root of object(overlay) space
	
	//static private Audit     audit = new Audit( "Os" );
	
	static public final int MODE_READ   = 0; // "r";
	static public final int MODE_WRITE  = 1; // "w";
	static public final int MODE_APPEND = 2; // "a";
	static public final int MODE_DELETE = 3; // "d";
	//static public final String MODE_RENAME = "m"; 
	
	static private String root = "os";
	static public  String root() { return Fs.rootDir() + root + File.separator; }
	static public  void   root( String nm ) { new File( Fs.rootDir() + (root = nm) ).mkdirs(); }
	
	static private String user = null;
	static private String user() { return user==null ? "" : user + File.separator; }
	static public  void   user( String nm ) { new File( root() + (user = nm) ).mkdirs(); }
	
	static public  String home() { return root() + user(); } // should always end in "/"
	
	// manage singleton
	static private Overlay o = null;
	static public  void    Set( Overlay overlay ) { o = overlay; }
	static public  Overlay Get() { return null != o ? o : (o = new Overlay()); }
	
	//  virtual to "real" filename mapping
	// /home/martin/person/martin/name.txt -> /home/martin/sofa/sofa.0/martin/name.txt
	// if write - top overlay NAME is returned, simple.
	// if read  - overlay space is searched for an existing file.
	//          - if not found, or if the file has been deleted, the (non-existing) write filename is returned
	//          - if rename found (e.g. old^new), change return the old NAME.
	static public String fsname( String vfname, int modeChs ) {
		String fsname = vfname; // pass through!
		if (vfname != null) {
			//Audit.LOG( "o.path:"+ o.path() );
			if (o.isOverlaid( vfname = Path.absolute( o.path(), vfname ) ))
				switch (modeChs) {
					case MODE_READ   : fsname = o.find( vfname ); break;
					case MODE_DELETE : fsname = o.delCandidate( vfname, Series.number() - 1 ); break;
					default          : fsname = o.topCandidate( vfname ); // MODE_WRITE | _APPEND
		}		}
		return fsname;
	}
	static public boolean attachCwd( String userId ) {
		Overlay.audit.in( "attachCwd", "userid="+userId );
		root( "os" );
		user( userId );
		Set( Get()); // set singleton
		String cwd = new File( System.getProperty( "user.dir" )).getName();
		return Overlay.audit.out( Series.attach( cwd ));
}	}
