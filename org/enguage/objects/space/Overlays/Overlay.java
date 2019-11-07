package org.enguage.objects.space.Overlays;

import java.io.File;
import java.util.Iterator;

import org.enguage.objects.space.Value;
import org.enguage.objects.space.Overlays.Overlay;
import org.enguage.objects.space.Overlays.OverlayShell;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.sys.Fs;
import org.enguage.util.sys.Path;
import org.enguage.util.sys.Pent;
import org.enguage.util.sys.Shell;

class OverlayShell extends Shell {
	OverlayShell( Strings args ) { super( "Overlay", args ); }
	public Strings interpret( Strings argv ) { return Overlay.interpret( argv ); }
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
	static private       Audit   audit = new Audit( "Overlay" );
	static public  final int        id = 188374473; //Strings.hash( "overlay" );
	static private       boolean debug = false; //Enguage.runtimeDebugging || Enguage.startupDebugging;
	
	private final static String DELETE_CH = "!";  // RENAME_CH = "^";
	
	private Path p;
	String  path() { return p.toString(); }
	private boolean path( String dest ) {
		boolean rc = true;
		if (null != dest) {
			String src = p.pwd();    // remember where we are
			p.cd( dest );            // do the cd
			// check destination - might only exist in object space
			rc = Fs.existsEntity( Os.fsname( p.pwd(), Os.MODE_READ ));
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
		//new File( Os.root() ).mkdir(); // JIC -- ignore result
		/*
		 * For VERSION 2 - was plain Series.DEFAULT...
		 * If we put in the following it should result in /yagadi.com being overlaid
		 * with base overlay being /yagadi.com/yagadi.com.0  
		 * This will make it incompatible with version 1 ( as its series is iNeed ) 
		Series.create( new File( System.getProperty("user.dir")).getName(), p.toString() );
		 */
		//Series.create( Series.DEFAULT, p.toString() );
	}

	public int    count() { return Series.number(); }
	//public String toString() { return "[ "+ Os.user() +", "+ Series.name() +"("+ Series.number() +") ]"; }
	
	// called from Enguage...
	public boolean attached() { return Series.attached(); }
	//static public boolean attach( String series ) { return Series.attach( series ); }
	//static public void detach() { Series.detach(); }
	
	public void create() {
		audit.in( "create", "" );
		if (Series.attached())
			Series.append();
		else
			audit.debug( "not created -- not attached" );
		audit.out();
	}
	public boolean destroy() {
		// removes top overlay of current series
		audit.in( "destroy", "" );
		boolean rc = false;
		int topOverlay = Series.highest();
		if (topOverlay > 0 && !Series.name().equals( "" )) {
			String nm = Series.nth( topOverlay );
			//Audit.LOG( "Destroying "+ nm );
			rc = Fs.destroy( nm );
			if (rc) Series.count();
		}
		return audit.out( rc );
	}
	private String nthCandidate( String nm, int vn ) {
		//audit.in( "nthCandidate", "nm='"+ nm +"', vn="+ vn);
		// nthCandidate( "/home/martin/src/myfile.c", 27 ) => "/var/overlays/series.27/myfile.c"
		if (Series.highest() > -1 && vn > Series.highest()) {
			audit.ERROR("nthCandidate( "+ nm +", "+ vn +" ) called with a too high value (max="+ Series.highest()+")");
			vn = Series.highest();
		}
		return	//audit.out
				(
				(0 > vn) ? nm : 
				(Series.nth( vn ) + File.separator
						+ nm.substring( Series.base().length() /*+1*/ ))
				)
						; // +1 remove leading "/"
	}
	String topCandidate( String name ) {
		return nthCandidate( name, Series.number()-1);
	}
	String delCandidate( String name, int n ) {
		File f = new File( nthCandidate( name, n ));
		return f.getParent() +"/"+ DELETE_CH + f.getName(); 
	}
	String find( String vfname ) {
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
	boolean isOverlaid( String vfname ) {
		//audit.audit( "isOverlaid()? Comparing "+ vfname +" with "+ vfname.substring( 0, Series.base( Series.name() ).length()));
//		if (vfname == null) Audit.LOG( "vfname == null" );
//		if (0 == Series.base( Series.name() ).length()) Audit.LOG( "0 == Series.base( Series.name() ).length()" );
//		if (0 == vfname.length()) Audit.LOG( " 0 == vfname.length()" );
//		if (Series.base( Series.name() ).length() > vfname.length()) Audit.LOG( "Series.base( Series.name() ).length() > vfname.length()" );
//		if (!vfname.substring( 0, Series.base( Series.name() ).length()).equals( Series.base( Series.name() ))) Audit.LOG( "!vfname.substring( 0, Series.base( Series.name() ).length()).equals( Series.base( Series.name() ))" );
		return vfname != null // sanity
		//&& 0 != vfname.length() // so base == "x*", NAME == "x*"
		//&& 0 != Series.base().length()
		&& Series.base().length() <= vfname.length()
		&& vfname.substring( 0, Series.base().length()).equals( Series.base()); // NAME="xyz/abc" base="xyz" => true
	}
	
	// ---
	// --- Compact overlays
	static private boolean isDeleteName( String name ) {
		return new File( name ).getName().charAt( 0 ) == '!';
	}
	static private String nonDeleteName( String name ) {
		if (isDeleteName( name )) {
			File f = new File( name );
			name = f.getParent() +"/"+ f.getName().substring( 1 );
		}
		return name;
	}
	static public String deleteName( String name ) {
		if (!isDeleteName( name )) {
			File f = new File( name );
			name = f.getParent() +"/!"+ f.getName();
		}
		return name;
	}
	static private void moveFile( File src, File dest ) {
		audit.in( "moveFile", "moving folder: "+ src.toString() +" to "+ dest.toString());
		/* only called from combineUnderlays()
		 * so we're not propagating !files
		 */
    	if (src.isDirectory()) {
    		//audit.debug( "moving folder: "+ src.toString() +" to "+ dest.toString());
    		if (!dest.exists()) dest.mkdir();
    		//list all the directory contents
    		String files[] = src.list();
    		//audit.debug( "moving src of number:"+ src.listFiles().length );
    		for (String file : files) {
	    		//construct the src and dest file structure
	    		File srcFile = new File(src, file);
	    		File destFile = new File(dest, file);
	    		//recursive copy
	    		moveFile( srcFile, destFile );
    		}
    		//audit.debug( "deleting "+src.toString()+" of number:"+ src.listFiles().length );
    		if (!src.delete()) audit.ERROR( "folder move DELETE failed" );
    	} else {
    		/* was...
    		 *   if (dest.exists()) dest.delete();
             *	 src.renameTo( dest );
             */
    		// Of source files, if...
    		if (!isDeleteName( src.toString()) //  ...we have filename...
    				&& new File( deleteName( src.toString() )).exists()) // ...and !filename exists
    		{	//// remove !filename
        		//audit.debug( "a-delete !filename "+ Entity.deleteName( src.toString() ));
    			new File( deleteName( src.toString() )).delete();
    			//// move file across as before
        		//audit.debug( "a-move file across "+ src.toString() );
        		if (dest.exists()) dest.delete();
        		src.renameTo( dest );
    		} else if ( isDeleteName( src.toString()) // ...we have !filename
    				&& new File( nonDeleteName( src.toString() )).exists())  // ...and filename exists
    		{	//// just remove !filename (filename will be dealt with when we get there!)
        		//audit.debug( "b-deleting files "+ src.toString() );
        		src.delete();
    			if (src.exists()) audit.ERROR( "b) deleting !filename, with filename ("+ src.toString() +") FAILED" );
    		} else if (!isDeleteName( src.toString()) // we have filename..
    				&& !new File( deleteName( src.toString() )).exists()) // but not !filename
    		{	//// move file across - as before
        		//audit.debug( "c-move file across: "+ src.toString() +" to "+ dest.toString());
        		if (dest.exists()) if (!dest.delete()) audit.ERROR( "c) DELETE failed: "+ dest.toString() );
        		if (!src.renameTo( dest ))  audit.ERROR( "c) RENAME of "+ src.toString() +" to "+ dest.toString() +" failed" );
    		} else if (isDeleteName( src.toString()) // we have !filename 
    				&& !new File( nonDeleteName( src.toString() )).exists()) // but not filename
    		{	//// remove !filename...
        		//audit.debug( "d-remove !filename... "+ src.toString() );
    			//if (!
    					src.delete()
    					//) audit.ERROR( "deleting "+ src.toString() +" failed" )
    					;
    			//// and delete underlying file
        		//audit.debug( "d-...and delete underlying file "+ dest.toString());
    			new File( nonDeleteName( dest.toString() )).delete();
    		} else
    			audit.ERROR( "Fs.moveFile(): fallen off end moving "+ src.toString() );
    	}
    	audit.out();
	}	
	static public boolean compact( /* int targetNumber */) {
		audit.in( "compact", "combining "+ (Series.number()-1) +" underlays" );
		/*
		 * For expediency's sake, this function combines all underlaid  (i.e. protected) overlays
		 */
		boolean rc = false;
		//audit.debug( "top overlay is "+ highest );
		if (Series.attached() && Series.highest() > 1) {
			File src, dst;
			for (int overlay=Series.highest()-1; overlay>0; overlay--) {
				//audit.debug( "COMBINING "+ overlay +" with "+ (overlay-1) );
				src = new File( Series.nth( overlay ));
				if (src.exists()) {
					dst = new File( Series.nth( overlay-1 ));
					if (dst.exists()) {
						// move all new files into old overlay
						//audit.debug( "dst exist: moving overlay "+ src.toString() +" to "+ dst.toString());
		    			moveFile( src, dst );
		    			if (src.exists()) audit.ERROR( src.toString() +" still exists - after MOVE ;(" );
					} else {
						//audit.debug( "dst not existing: renaming overlay "+ src.toString() +" to "+ dst.toString());
						src.renameTo( dst );
		    			if (!dst.exists()) audit.ERROR( src.toString() +" still doesn't exists - after RENAME ;(" );
					}
					//count();
	    			//audit.debug( "number of overlays is now: "+ number() );
				} //else
					//audit.debug( "doing nothing: overlay "+ src.toString() +" does not exists" );
			}
			// then _rename_ top overlay - will now be ".1"
			//audit.debug("Now renaming top overlay "+ highest() +" to be 1" );
			src = new File( Series.nth( Series.highest() ));
			dst = new File( Series.nth( 1 ));
			if (dst.exists()) dst.delete();
			if (!src.renameTo( dst )) audit.ERROR( "RENAME "+ src.toString()+" to "+ dst.toString() +" FAILED" );
			//if (src.exists()) audit.debug( src.toString() +" still exists - after RENAME ;(" );
			// adjust count()
			Series.count();
			//audit.debug( "count really is: "+ number() );
			rc = true;
		}
		audit.out( "compact "+ (rc?"done":"failed") +", count="+ Series.number() +", highest="+ Series.highest() );
		return rc;
	}

	public boolean combineUnderlays() { return compact(); }
	
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
			String prefix = Os.home() + File.separator + Series.name() +".";
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
	static public boolean attachCwd( String userId ) {
		audit.in( "attachCwd", "userid="+userId );
		Os.root( "os" );
		Os.user( userId );
		Os.Set( Os.Get()); // set singleton
		String cwd = new File( System.getProperty( "user.dir" )).getName();
		return audit.out( Series.attach( cwd ));
	}
	
	// ===
	
	static public Strings interpret( Strings argv ) {
		String rc = Shell.FAIL;
		int argc = argv.size();
		Overlay o = Os.Get();
		
		Strings values = argv.copyAfter( 0 );
		String  value  = values.toString( Strings.PATH ),
				cmd    = argv.get( 0 );
		
		if (cmd.equals("attach") && (2 >= argc)) {
			//audit.debug( "enguage series existing="+ Boolean.valueOf( Series.existing( "enguage" )));
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
			
		} else if (cmd.equals( "rm" )) {
			argv.remove( 0 );
			String fname = argv.remove( argv.size()-1 );
			rc = new Value( argv.toString( Strings.CONCAT ), fname ).ignore() ? Shell.SUCCESS : Shell.FAIL;
		
		} else if ((1 == argc) && cmd.equals( "up" )) {
			if ( o.path( ".." ))
				rc = Shell.SUCCESS;
			else
				audit.debug( cmd +": Cannot cd to .." );
				
		} else if ((2 == argc) && cmd.equals( "cd" )) {
			if ( o.path( argv.get( 1 )))
				rc = Shell.SUCCESS;
			else
				Audit.log( cmd +": Cannot cd to "+ argv.get( 1 ));
		
		} else if (cmd.equals( "pwd" ) && (1 == argc)) {
			rc = Shell.SUCCESS;
			Audit.log( o.path());

		} else if (cmd.equals( "write" )) {
			rc = Shell.SUCCESS;
			Audit.log( "New file would be '"+ Os.fsname( value, Os.MODE_WRITE ) +"'" ); // last param ignored
			
		} else if (cmd.equals( "mkdir" ) && (2 == argc)) {
			rc = Shell.SUCCESS;
			String fname = Os.fsname( argv.get( 1 ), Os.MODE_WRITE );
			Audit.log( "Fname is "+ fname );
			Audit.log( ">>>mkdir("+ fname +") => "+ (new File( fname ).mkdirs()?"Ok":"Error"));
			
		} else if (cmd.equals( "read" ) && (2 == argc)) {
			rc = Shell.SUCCESS;
			Audit.log( "File found? is '"+ Os.fsname( argv.get( 1 ), Os.MODE_READ )+"'" );
			
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
		return new Strings( rc );
	}
	
	/*
	 * Some helpers from enguage.....
	 * implements the transaction bit -- this isn't ACID :(
	 */
	private boolean inTxn = false;
	public void startTxn( boolean undoIsEnabled ) {
		audit.in( "startTxn" );
		if (undoIsEnabled) {
			inTxn = true;
			create();
		}
		audit.out();
	}
	public void finishTxn( boolean undoIsEnabled ) {
		audit.in( "finishTxn" );
		if (undoIsEnabled) {
			inTxn = false;
			combineUnderlays();
		}
		audit.out();
	}
	public void reStartTxn() {
		audit.in( "restartTxn" );
		if (inTxn) {
			destroy(); // remove this overlay
			destroy(); // remove previous -- this is the undo bit
			create();  // restart a new txn
		}
		audit.out();
	}
	

	
	public static void main (String args []) {
		Audit.allOn();
		if (!attachCwd( "Overlay" ))
			Audit.log( "Ouch! Can't auto attach" );
		else {
			Audit.log( "osroot="+ Os.home() );
			Audit.log( "base="+ Series.name()+", n=" + Series.number() );
			OverlayShell os = new OverlayShell( new Strings( args ));
			os.run();
}	}	}
