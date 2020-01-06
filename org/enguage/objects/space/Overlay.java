package org.enguage.objects.space;

import java.io.File;
import java.util.Iterator;

import org.enguage.objects.space.Overlay;
import org.enguage.objects.space.OverlayShell;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.sys.Fs;
import org.enguage.util.sys.Path;
import org.enguage.util.sys.Pent;
import org.enguage.util.sys.Shell;

class OverlayShell extends Shell {
	OverlayShell( Strings args ) { super( "Overlay", args ); }
	public Strings interpret( Strings argv ) { return interpret( argv ); }
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
	static       Audit   audit = new Audit( "Overlay" );
	static public  final int        id = 188374473; //Strings.hash( "overlay" );
	static private       boolean debug = false; //Enguage.runtimeDebugging || Enguage.startupDebugging;

	static private final String   DELETE_CH = "!";  // RENAME_CH = "^";
	static private final String    DETACHED = null;
	
	static public final String  DEFAULT = "Enguage"; //"sofa";
	static public final int MODE_READ   = 0; // "r";
	static public final int MODE_WRITE  = 1; // "w";
	static public final int MODE_APPEND = 2; // "a";
	static public final int MODE_DELETE = 3; // "d";
	//static public final String MODE_RENAME = "m"; 
	
	// manage singleton
	static private Overlay o = null;
	static public  Overlay Get() { return null != o ? o : (o = new Overlay()); }
	static public  void    Set( Overlay overlay ) { o = overlay; }
	
	private Path p;
	String  path() { return p.toString(); }
	public boolean path( String dest ) {
		boolean rc = true;
		if (null != dest) {
			String src = p.pwd();    // remember where we are
			p.cd( dest );            // do the cd
			// check destination - might only exist in object space
			rc = Fs.existsEntity( fname( p.pwd(), MODE_READ ));
			if (!rc) p = new Path( src ); // return to where we were
		}
		return rc;
	}
 
	public Overlay() {
		p = new Path( System.getProperty( "user.dir" ));
	}
	
	// --- object space
		
	static private String root = "os";
	static public  void   root( String nm ) { new File( Fs.rootDir() + (root = nm) ).mkdirs(); }
	static public  String root() { return Fs.rootDir() + root + File.separator; }
	
	static private String user = null;
	static public  void   user( String nm ) { new File( root() + (user = nm) ).mkdirs(); }
	static private String user() { return user==null ? "" : user + File.separator; }

	// e.g. fsdir/os/uid/
	static public  String home() { return root() + user(); } // should always end in "/"
	
	// --- Series management
	static private String series = DETACHED;
	static private void   series( String nm ) { if (nm != null) series = nm; }
	static public  String series() { return series; }
	
	static private boolean attached = false;
	static public  boolean attached() { return attached;}
	
	static private int    number = 0; // series.0, series.1, ..., series.n => 1+n
	static public  int    number() { return number; } // initialised to 1, for 0th overlay
	
	static private String  nth( int vn ) { return home() + series +"."+ vn;}
	
	static public  void    detach() {
		attached = false;
		series( DETACHED );
		number = 0;
	}
	
	static private  void initSeries( String nm ) {
		series( nm );
		number = 0;
		String candidates[] = new File( home() ).list();
		if (candidates != null) for (String candidate : candidates) {
			String[] cands = candidate.split("\\.");
			if (cands.length == 2 && cands[0].equals( series ))
				try {	
					Integer.parseInt( cands[ 1 ]);
					number++;
				} catch (Exception ex){}
	}	}
	static public boolean attachCwd( String userId ) {
		audit.in( "attachCwd", "userid="+userId );
		root( "os" );
		user( userId );
		Set( Get()); // set singleton
		String cwd = System.getProperty( "user.dir" );
		initSeries( new File( cwd ).getName() );
		attached = Link.fromString( home() + series(), cwd );
		return audit.out( attached );
	}

	static public  boolean exists() { return Fs.exists( home()+ series + Link.EXT );}
	static public  void    append() { if (attached()) new File( nth( number++ )).mkdirs(); }
	static public  boolean remove() { return number >= 0 && attached() && Fs.destroy( nth( --number ));}

	private String nthCandidate( String nm, int vn ) {
		int topVn = number() - 1;
		if (topVn == -1)
			return nm;
		else if (vn > topVn) // limit to top version no.
			vn = topVn;
		return	(0 > vn) ? nm : (nth( vn ) + File.separator + nm);
	}
	private String topCandidate( String name ) {
		return nthCandidate( name, number()-1);
	}
	private String delCandidate( String name, int n ) {
		File f = new File( nthCandidate( name, n ));
		return f.getParent() +"/"+ DELETE_CH + f.getName(); 
	}
	private String find( String vfname ) {
		String fsname = null;
		int vn = number(); // number=3 ==> series.0,1,2, so initially decr vn
		while (--vn >= 0)
			if (Fs.exists( fsname = nthCandidate( vfname, vn ) ))
				return fsname;
			else if (Fs.exists( delCandidate( vfname, vn ) )) 
				return topCandidate( vfname ); // look no further - return top (non-existing) file	
		return vfname;
	}
	private boolean isOverlaid( String vfname ) {
		if (vfname == null) return false;
		if (vfname.startsWith( home() )) return false;
		if (!vfname.startsWith( "/" )) return true; // assumes were at head of overlaid fs!
		return vfname.startsWith( Link.content( home() + series )); // NAME="xyz/abc" base="xyz" => true
	}
	
	//  virtual to "real" filename mapping
	// /home/martin/person/martin/name.txt -> /home/martin/sofa/sofa.0/martin/name.txt
	// if write - top overlay NAME is returned, simple.
	// if read  - overlay space is searched for an existing file.
	//          - if not found, or if the file has been deleted, the (non-existing) write filename is returned
	//          - if rename found (e.g. old^new), change return the old NAME.
	static public String fname( String vfname, int modeChs ) {
		String fsname = vfname; // pass through!
		if (attached && o.isOverlaid( vfname ))
			switch (modeChs) {
				case MODE_READ   : fsname = o.find( vfname ); break;
				case MODE_DELETE : fsname = o.delCandidate( vfname, number() - 1 ); break;
				default          : fsname = o.topCandidate( vfname ); // MODE_WRITE | _APPEND
			}
		return fsname;
	}
	
	static public boolean isDeleteName( String name ) {
		return new File( name ).getName().charAt( 0 ) == '!';
	}
	static public String nonDeleteName( String name ) {
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
			        count = number();
			String prefix = home() + File.separator + series() +".";
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
	
	// ===
	
	/*
	 * Some helpers from enguage.....
	 * implements the transaction bit -- this isn't ACID :(
	 */
	// --- Compact overlays
	static void moveFile( File src, File dest ) {
		//audit.in( "moveFile", "moving folder: "+ src.toString() +" to "+ dest.toString());
		// don't propagate !files
		if (src.isDirectory()) {
			if (!dest.exists()) dest.mkdir();
			String files[] = src.list();
			if (files != null) for (String file : files)
	    		moveFile( new File( src, file ), new File( dest, file ));
			src.delete();
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
		//audit.out();
	}
	static public boolean compact() {
		audit.in( "compact", "combining "+ (number()-1) +" underlays" );
		boolean rc = false;
		if (attached() && number > 2) {
			File src, dst;
			for (int overlay=number-2; overlay>0; overlay--) {
				src = new File( nth( overlay ));
				if (src.exists()) {
					dst = new File( nth( overlay-1 ));
					if (dst.exists())
		    			moveFile( src, dst );
					else
						src.renameTo( dst );					
			}	}
			// then _rename_ top overlay - will now be ".1"
			//audit.debug("Now renaming top overlay "+ highest() +" to be 1" );
			src = new File( nth( number() - 1 ));
			dst = new File( nth( 1 ));
			if (dst.exists()) dst.delete();
			if (!src.renameTo( dst )) audit.ERROR( "RENAME "+ src.toString()+" to "+ dst.toString() +" FAILED" );
			
			number = 2;
			
			rc = true;
		}
		audit.out( "compact "+ (rc?"done":"failed") +", count="+ number() +", highest="+ (number()-1) );
		return rc;
	}

	// --- Transactions
	static boolean inTxn = false;
	static public void reStartTxn() {
		audit.in( "restartTxn" );
		if (inTxn) {
			remove(); // remove this overlay
			remove(); // remove previous -- this is the undo bit
			append(); // restart a new txn
		}
		audit.out();
	}
	static public void finishTxn( boolean undoIsEnabled ) {
		audit.in( "finishTxn" );
		if (undoIsEnabled) {
			inTxn = false;
			compact();
		}
		audit.out();
	}
	static public void startTxn( boolean undoIsEnabled ) {
		audit.in( "startTxn" );
		if (undoIsEnabled) {
			inTxn = true;
			append();
		}
		audit.out();
	}

	// --- Test code...
	
	static public Strings interpret( Strings argv ) {
		String rc = Shell.FAIL;
		int argc = argv.size();
		//Overlay o = Get();
		
		Strings values = argv.copyAfter( 0 );
		String  value  = values.toString( Strings.PATH ),
				cmd    = argv.get( 0 );
		
		if (cmd.equals("attach") && (2 >= argc)) {
			//audit.debug( "enguage series existing="+ Boolean.valueOf( Series.existing( "enguage" )));
			if (2 == argc) {
				initSeries( argv.get( 1 ));
				if (0 >= number)
					audit.debug( "No such series "+ argv.get( 1 ));
				else
					rc = Shell.SUCCESS;
			} else if ( attached())
				audit.debug( "Not attached" );
			else
				rc = Shell.SUCCESS;
				
		} else if (cmd.equals("detach") && (1 == argc)) {
			detach();
			rc = Shell.SUCCESS;
			
		} else if ((cmd.equals( "save" ) || cmd.equals( "create" )) && (1 == argc)) {
			//audit.audit( "Creating "+ o.series());
			rc = Shell.SUCCESS;
			append();//o.create();
			
		} else if (cmd.equals( "exists" ) && (2 == argc)) {
			rc =  exists() ? "Yes":"No";
						
		} else if (cmd.equals(  "destroy"  ) && (1 == argc) ) {
			rc = remove() ? Shell.SUCCESS : Shell.FAIL;
			
		} else if ((   cmd.equals(    "bond"  )
				    || cmd.equals( "combine"  ))
		           && (1 == argc) ) {
			rc = compact() ? Shell.SUCCESS : Shell.FAIL;
			
		} else if (cmd.equals( "rm" )) {
			argv.remove( 0 );
			String fname = argv.remove( argv.size()-1 );
			rc = new Value( argv.toString( Strings.CONCAT ), fname ).ignore() ? Shell.SUCCESS : Shell.FAIL;
		
//		} else if ((1 == argc) && cmd.equals( "up" )) {
//			if ( o.path( ".." ))
//				rc = Shell.SUCCESS;
//			else
//				audit.debug( cmd +": Cannot cd to .." );
//				
//		} else if ((2 == argc) && cmd.equals( "cd" )) {
//			if ( o.path( argv.get( 1 )))
//				rc = Shell.SUCCESS;
//			else
//				Audit.log( cmd +": Cannot cd to "+ argv.get( 1 ));
//		
//		} else if (cmd.equals( "pwd" ) && (1 == argc)) {
//			rc = Shell.SUCCESS;
//			Audit.log( o.path());

		} else if (cmd.equals( "write" )) {
			rc = Shell.SUCCESS;
			Audit.log( "New file would be '"+ fname( value, MODE_WRITE ) +"'" ); // last param ignored
			
		} else if (cmd.equals( "mkdir" ) && (2 == argc)) {
			rc = Shell.SUCCESS;
			String fname = fname( argv.get( 1 ), MODE_WRITE );
			Audit.log( "Fname is "+ fname );
			Audit.log( ">>>mkdir("+ fname +") => "+ (new File( fname ).mkdirs()?"Ok":"Error"));
			
		} else if (cmd.equals( "read" ) && (2 == argc)) {
			rc = Shell.SUCCESS;
			Audit.log( "File found? is '"+ fname( argv.get( 1 ), MODE_READ )+"'" );
			
//		} else if (cmd.equals( "ls" )) {
//			rc = Shell.SUCCESS;
//			for( String s : o.list( argc==2 ? argv.get( 1 ) : "." ))
//				rc += "\n" + s;
			
		} else
			audit.debug( "Usage: attach <series>\n"
			                 +"     : detach\n"
			                 +"     : save\n"
			                 +"     : write <pathname>\n"
			                 +"     : read  <pathname>\n"
			                 +"     : mkdir <pathname>\n"
			                 +"     : pwd <pathname>\n"
			                 +"     : cd <pathname>\n"
			                 +"     : ls <pathname>\n"
			                 +"given: "+ argv.toString( Strings.CSV ));
		return new Strings( rc );
	}
	
	public static void main (String args []) {
		Audit.allOn();
		if (!attachCwd( "Overlay" ))
			Audit.log( "Ouch! Can't auto attach" );
		else {
			Audit.log( "osroot="+ home() );
			Audit.log( "base="+ series()+", n=" + number() );
			OverlayShell os = new OverlayShell( new Strings( args ));
			os.run();
}	}	}
