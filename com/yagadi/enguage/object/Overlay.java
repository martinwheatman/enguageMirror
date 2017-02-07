package com.yagadi.enguage.object;

import java.io.File;
import java.util.Iterator;

import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Fs;
import com.yagadi.enguage.util.Path;
import com.yagadi.enguage.util.Pent;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

class OverlayShell extends Shell {
	OverlayShell( Strings args ) { super( "Overlay", args ); }
	public String interpret( Strings argv ) { return Overlay.interpret( argv ); }
}

/* The analysis of this file is all off following the port to Android
 * OverlaySpace contains several series, each series contains several overlays.
 * TODO: sort this out! v1.1?
 */

/* --- Q's:
 * could overlays be sparse (e.g. persons.0 persons.5? -- reuse link is in persons.0 could it be persons.<min>?
 */
//-- BEGIN Overlay
public class Overlay {
	static private Audit audit = new Audit( "Overlay" );
	private static boolean debug = false; //Enguage.runtimeDebugging || Enguage.startupDebugging;
	
	public static final String DEFAULT = "default";
	
	final static String MODE_READ   = "r";
	final static String MODE_WRITE  = "w";
	final static String MODE_APPEND = "a";
	final static String MODE_DELETE = "d";
	final static String MODE_RENAME = "m";
	final static String RENAME_CH   = "^";
	final static String DELETE_CH   = "!";
	final static String OPT_X       ="-x";
	
	private Path p;
	private String  path() { return p.toString(); }
	private boolean path( String dest ) {
		boolean rc = true;
		if (null != dest) {
			String src = p.pwd();    // remember where we are
			p.cd( dest );            // do the cd
			// check destination - might only exist in object space
			rc = Fs.existsEntity( Overlay.fsname( p.pwd(), "r" ));
			if (!rc) p = new Path( src ); // return to where we were
		}
		return rc;
	}

	private static String error = "";
	public  static String error() { return error; }
	 
	public Overlay() {
		Series.detach(); // start detach()ed;
		p = new Path( System.getProperty( "user.dir" ));
		//audit.debug( "initial path is "+ System.getProperty( "user.dir" ) +" (p="+ p.toString() +")" );
		new File( Ospace.root ).mkdir(); // JIC -- ignore result
		/*
		 * For VERSION 2 - was plain Series.DEFAULT...
		 * If we put in the following it should result in /yagadi.com being overlaid
		 * with base overlay being /yagadi.com/yagadi.com.0  
		 * This will make it incompatible with version 1 ( as its series is iNeed ) 
		Series.create( new File( System.getProperty("user.dir")).getName(), p.toString() );
		 */
		Series.create( Series.DEFAULT, p.toString() );
	}

	public int    count() { return Series.number(); }
	public String toString() { return "[ "+ Ospace.root +", "+ Series.name() +"("+ Series.number() +") ]"; }
	
	// called from Enguage...
	public boolean attached() { return Series.attached(); }
	//static public boolean attach( String series ) { return Series.attach( series ); }
	//static public void detach() { Series.detach(); }
	
	public void create() {
		if (debug) audit.in( "createOverlay", "" );
		if (Series.attached())
			Series.append();
		else
			audit.debug( "not created -- not attached" );
		if (debug) audit.out();
	}
	public boolean destroy() {
		// removes top overlay of current series
		if (debug) audit.in( "destroy", "" );
		boolean rc = false;
		int topOverlay = Series.highest();
		if (topOverlay > 0 && !Series.name().equals( "" )) {
			String nm = Series.name( topOverlay );
			//audit.debug( "Destroying "+ nm );
			rc = Fs.destroy( nm );
			if (rc) Series.count();
		}
		if (debug) audit.out( rc );
		return rc;
	}
	private String nthCandidate( String nm, int vn ) {
		//audit.in( "nthCandidate", "nm='"+ nm +"', vn="+ vn);
		// nthCandidate( "/home/martin/src/myfile.c", 27 ) => "/var/overlays/series.27/myfile.c"
		if (vn > Series.highest()) { vn = Series.highest(); audit.ERROR("nthCandidate called with too high a value"); }
		return	//audit.out(
				(0 > vn) ? nm : 
				(Series.name( vn ) + File.separator
						+ nm.substring( Series.base( Series.name() ).length() /*+1*/ ))
				//)
						; // +1 remove leading "/"
	}
	private String topCandidate( String name ) {
		return nthCandidate( name, Series.number()-1);
	}
	private String delCandidate( String name, int n ) {
		File f = new File( nthCandidate( name, n ));
		return f.getParent() +"/"+ DELETE_CH + f.getName(); 
	}
	private String find( String vfname ) {
		//audit.in( "find", "find: "+ vfname );
		String fsname = null;
		int vn = Series.number(); // number=3 ==> series.0,1,2, so initially decr vn
		boolean done = false;
		while (!done && --vn >= 0) {
			fsname = nthCandidate( vfname, vn );
			done = Fs.exists( fsname ); // first time around this will be topCandidate()
			if (!done) { // look for a delete marker
				if (done = Fs.exists( delCandidate( vfname, vn ) )) {
					fsname = topCandidate( vfname ); // look no further - return top (non-existing) file
				} else { // look for rename marker -- is this the right order: file - delete - rename?
//					fsname = o.renameCandidate( vfname, vn );
//					done = new File( fsname ).isFile() || new File(  fsname  ).isDirectory();
		}	}	}
//		if (!done) // orig file or non-existant file!
//			fsname = Filesystem.exists( vfname ) ? vfname : topCandidate( vfname );
		//return audit.out( fsname );
		return fsname;
	}
	// return true for path=/home/martin/persons/fred/waz/ere
	// where series = persons
	//   and /home/martin/sofa/persons.0/reuse -> /home/martin/persons
	private boolean isOverlaid( String vfname ) {
		//audit.audit( "isOverlaid()? Comparing "+ vfname +" with "+ vfname.substring( 0, Series.base( Series.name() ).length()));
		return null != Series.base( Series.name() )
		&& vfname != null // sanity
		&& 0 != Series.base( Series.name() ).length() && 0 != vfname.length() // so base == "x*", NAME == "x*"
		&& Series.base( Series.name() ).length() <= vfname.length()
		&& vfname.substring( 0, Series.base( Series.name() ).length()).equals( Series.base( Series.name() )); // NAME="xyz/abc" base="xyz" => true
	}
	// maps a virtual filename onto a "real" filesystem NAME.
	// /home/martin/person/martin/name.txt -> /home/martin/sofa/sofa.0/martin/name.txt
	// if write - top overlay NAME is returned, simple.
	// if read  - overlay space is searched for an existing file.
	//          - if not found, or if the file has been deleted, the (non-existing) write filename is returned
	//          - if rename found (e.g. old^new), change return the old NAME.
	public static String fsname( String vfname, String modeChs ) {
		//audit.in("fsname","vfname="+vfname+", mode="+modeChs );
		String fsname = vfname; // pass through!
		Overlay o = Get();
		if (o != null && vfname != null) {
			//-- String vfname = rationalisePath( absolutePath( o.p.toString(), new String( fsname ) ));
			vfname = Path.absolute( o.path(), vfname );
			//audit.debug("abs is:"+vfname);
			if (o.isOverlaid( vfname )) {
				if (modeChs.equals( MODE_READ )) {
					fsname = o.find( vfname );
					
				} else if (modeChs.equals( MODE_WRITE ) || modeChs.equals( MODE_APPEND )) {
					fsname = o.topCandidate( vfname );
					
				} else if (modeChs.equals( MODE_DELETE )) {
					fsname = o.delCandidate( vfname, Series.highest()); // .. was number()
		}	}	}
		//return audit.out( fsname );
		return fsname;
	}
	public boolean combineUnderlays() { return Series.compact(); }
	
	public Strings list( String dname ) {
		
		if (debug) audit.in( "list", dname );
		/* dname will usually be ".", or "1subDir"
		 * 
		 * vpwd is: /home/martin/src/files/
		 * maps to: /var/overlays/enguage.0/files
		 * and    : /var/overlays/enguage.0/files/1subDir
		 * NB. 1subDir doesn't "really" exist
		 * 
		 * this needs to read:
		 * 
		 * /home/martin/yagadi/enguage.0/pwd/dna/me/
		 * /home/martin/yagadi/enguage.1/pwd/dna/me/
		 * ...
		 * /home/martin/yagadi/enguage.n/pwd/dna/me/
		 * 
		 * adding and removing files to build up a state-of-affairs.
		 */
		Strings rc = new Strings();
		p.pathListDelete();
		
		String absName = null;
		try {
			if (null == dname) {
				absName =  ".";
			} else if (dname.charAt( 0 ) == '/') {
				absName = new File( dname ).getCanonicalPath();
			} else {
				absName = new File( p.pwd() + File.separator + dname ).getCanonicalPath();
			}
		} catch( Exception e ) {
			audit.ERROR( "cannonical file error:"+ e.toString() );
		}
		
		p.insertDir( absName, "" );
		if (p.pwd().length() <= absName.length() && isOverlaid( p.pwd() )) {
			int			n = -1,
			        count = count();
			String prefix = Ospace.root + File.separator + Series.name() +".";
			String suffix = absName.substring( p.pwd().length());
			while (++n < count)
				p.insertDir( prefix + n + suffix, "" );
		}
		Iterator<Pent> pi = p.iterator(); 
		while ( pi.hasNext() )
			rc.add( pi.next().name() );
		
		if (debug) audit.out( rc );
		return rc;
	}
	static public boolean autoAttach() {
		boolean rc = true;
		Overlay.Set( Overlay.Get()); // set singleton
		String candidate = new File( System.getProperty("user.dir")).getName();
		if (candidate.equals( "" ) || !Series.attach( candidate )) {
			candidate = Series.DEFAULT;
			if (!Series.existing( candidate ))
				Series.create( candidate, new File( System.getProperty("user.dir")).getPath() );
			rc = Series.attach( candidate );
		}
		if (rc)
			audit.debug( "Attached to "+ candidate );
		else
			audit.log( "Unable to attach to "+ candidate );
		return rc;
	}
	
	// ===
	
	static private Overlay singletonO = null;
	static public Overlay Get() {
		if (null==singletonO) singletonO = new Overlay();
		return singletonO ;
	}
	static public void Set( Overlay o ) { singletonO = o; }

	static public String interpret( Strings argv ) {
		String rc = Shell.FAIL;
		int argc = argv.size();
		Overlay o = Overlay.Get();
		
		Strings values = argv.copyAfter( 0 );
		String  value  = values.toString( Strings.PATH ),
				cmd    = argv.get( 0 );
		
		if (cmd.equals("attach") && (2 >= argc)) {
			audit.debug( "enguage series existing="+ Boolean.valueOf( Series.existing( "enguage" )));
			if (2 == argc) {
				if (Series.attach( argv.get( 1 )))
					audit.debug( "No such series "+ argv.get( 1 ));
				else
					rc = Shell.SUCCESS;
			} else if ( Series.attached())
				audit.debug( "Not attached" );
			else
				rc = Shell.SUCCESS;
				
		} else if (cmd.equals("detach") && (1 == argc)) {
			Series.detach();
			rc = Shell.SUCCESS;
			
		} else if ((cmd.equals( "save" ) || cmd.equals( "create" )) && (1 == argc)) {
			//audit.audit( "Creating "+ o.series());
			rc = Shell.SUCCESS;
			o.create();
			
		//} else if (0 == cmd.equals( "exists" ) && (2 == argc)) {
		//	rc =  o.existingSeries( argv.get( 1 )) ? "Yes":"No";
			
		} else if (cmd.equals( "create" ) && ((2 == argc) || (3 == argc)) ) {
			if (!Series.create( argv.get( 1 ), argc == 3 ? argv.get( 2 ):System.getProperty("user.dir") ))
				audit.debug( argv.get( 1 ) + " already exists" );
			else
				rc = Shell.SUCCESS;
				
		//} else if (cmd.equals( "delete" ) && (2 == argc) ) {
		//	if (Series.deleteSeries( argv.get( 1 )))
		//		rc = Shell.SUCCESS;
		//	else
		//		audit.debug( argv.get( 1 ) + " doesn't exists" );
			
		} else if (cmd.equals(  "destroy"  ) && (1 == argc) ) {
			rc = o.destroy() ? Shell.SUCCESS : Shell.FAIL;
			
		} else if ((   cmd.equals(    "bond"  )
				    || cmd.equals( "combine"  ))
		           && (1 == argc) ) {
			rc = o.combineUnderlays() ? Shell.SUCCESS : Shell.FAIL;
			
		//} else if (0 == cmd.equals( "rm" ) && (2 == argc) ) {
		//	if (!entityIgnore( argv.get( 1 ))) rc =  argv.get( 1 ) +" doesn't exists" );
		
		} else if ((1 == argc) && cmd.equals( "up" )) {
			if ( o.path( ".." ))
				rc = Shell.SUCCESS;
			else
				audit.debug( cmd +": Cannot cd to .." );
				
		} else if ((2 == argc) && cmd.equals( "cd" )) {
			if ( o.path( argv.get( 1 )))
				rc = Shell.SUCCESS;
			else
				audit.log( cmd +": Cannot cd to "+ argv.get( 1 ));
		
		} else if (cmd.equals( "pwd" ) && (1 == argc)) {
			rc = Shell.SUCCESS;
			audit.log( o.path());

		} else if (cmd.equals( "write" )) {
			rc = Shell.SUCCESS;
			audit.log( "New file would be '"+ Overlay.fsname( value, "w" ) +"'" ); // last param ignored
			
		} else if (cmd.equals( "mkdir" ) && (2 == argc)) {
			rc = Shell.SUCCESS;
			String fname = Overlay.fsname( argv.get( 1 ), "w" );
			audit.log( "Fname is "+ fname );
			audit.log( ">>>mkdir("+ fname +") => "+ (new File( fname ).mkdirs()?"Ok":"Error"));
			
		} else if (cmd.equals( "read" ) && (2 == argc)) {
			rc = Shell.SUCCESS;
			audit.log( "File found? is '"+ Overlay.fsname( argv.get( 1 ), "r" )+"'" );
			
		} else if (cmd.equals( "ls" )) {
			rc = Shell.SUCCESS;
			for( String s : o.list( argc==2 ? argv.get( 1 ) : "." ))
				rc += "\n" + s;
			
		} else
			audit.debug( "Usage: attach <series>\n"
			                 +"     : detach\n"
			                 +"     : save\n"
		//	                 +"     : show [<n>] <pathname>\n"
			                 +"     : write <pathname>\n"
			                 +"     : read  <pathname>\n"
			                 +"     : delete <pathname>\n"
			                 +"     : mkdir <pathname>\n"
			                 +"     : pwd <pathname>\n"
			                 +"     : cd <pathname>\n"
			                 +"     : ls <pathname>\n"
			                 +"given: "+ argv.toString( Strings.CSV ));
		return rc;
	}
	
	/*
	 * Some helpers from enguage.....
	 * implements the transaction bit -- this isn't ACID :(
	 */
	private boolean inTxn = false;
	public void startTxn( boolean undoIsEnabled ) {
		if (undoIsEnabled) {
			inTxn = true;
			create();
	}	}
	public void finishTxn( boolean undoIsEnabled ) {
		if (undoIsEnabled) {
			inTxn = false;
			combineUnderlays();
	}	}
	public void reStartTxn() {
		if (inTxn) {
			destroy(); // remove this overlay
			destroy(); // remove previous -- this is the undo bit
			create();  // restart a new txn
	}	}
	

	
	public static void main (String args []) {
		Audit.allOn();
		if (!autoAttach())
			audit.log( "Ouch! Can't auto attach" );
		else {
			audit.log( "osroot="+ Ospace.root );
			audit.log( "base="+ Series.name()+", n=" + Series.number() );
			OverlayShell os = new OverlayShell( new Strings( args ));
			os.run();
}	}	}
