package org.enguage.objects.space.overlays;

import java.io.File;
import java.util.Iterator;

import org.enguage.objects.space.Value;
import org.enguage.objects.space.overlays.Overlay;
import org.enguage.objects.space.overlays.OverlayShell;
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
	static       Audit   audit = new Audit( "Overlay" );
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
 
	public Overlay() {
		p = new Path( System.getProperty( "user.dir" ));
	}
	
	private String nthCandidate( String nm, int vn ) {
		int topVn = Series.number() - 1;
		if (topVn == -1)
			return nm;
		else if (vn > topVn) // limit to top version no.
			vn = topVn;
		return	(0 > vn) ? nm : 
				(Series.nth( vn ) + File.separator
						+ nm.substring( Series.base().length() /*+1*/ ));
	}
	String topCandidate( String name ) {
		return nthCandidate( name, Series.number()-1);
	}
	String delCandidate( String name, int n ) {
		File f = new File( nthCandidate( name, n ));
		return f.getParent() +"/"+ DELETE_CH + f.getName(); 
	}
	String find( String vfname ) {
		String fsname = null;
		int vn = Series.number(); // number=3 ==> series.0,1,2, so initially decr vn
		while (--vn >= 0)
			if (Fs.exists( fsname = nthCandidate( vfname, vn ) ))
				return fsname;
			else if (Fs.exists( delCandidate( vfname, vn ) )) 
				return topCandidate( vfname ); // look no further - return top (non-existing) file	
		return null;
	}
	boolean isOverlaid( String vfname ) {
		return vfname != null // sanity
		&& Series.base().length() <= vfname.length()
		&& vfname.substring( 0, Series.base().length()).equals( Series.base()); // NAME="xyz/abc" base="xyz" => true
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
			        count = Series.number();
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
			Series.append();//o.create();
			
		} else if (cmd.equals( "exists" ) && (2 == argc)) {
			rc =  Series.existing() ? "Yes":"No";
						
		} else if (cmd.equals(  "destroy"  ) && (1 == argc) ) {
			rc = Series.remove() ? Shell.SUCCESS : Shell.FAIL;
			
		} else if ((   cmd.equals(    "bond"  )
				    || cmd.equals( "combine"  ))
		           && (1 == argc) ) {
			rc = Series.compact() ? Shell.SUCCESS : Shell.FAIL;
			
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
		if (!Os.attachCwd( "Overlay" ))
			Audit.log( "Ouch! Can't auto attach" );
		else {
			Audit.log( "osroot="+ Os.home() );
			Audit.log( "base="+ Series.name()+", n=" + Series.number() );
			OverlayShell os = new OverlayShell( new Strings( args ));
			os.run();
}	}	}
