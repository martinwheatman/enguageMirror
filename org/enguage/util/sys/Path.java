package org.enguage.util.sys;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeSet;

import org.enguage.sign.object.sofa.Link;
import org.enguage.util.Strings;

public class Path {
	//static private Audit audit = new Audit( "Path" );
	
	final static char   PATH_SEP_CHAR = File.separatorChar; 
	final static String PWD_PROPERTY = "user.dir"; 
	final static String PARENT = ".."; 
	final static String OPT_A = "-a"; 
	final static String OPT_X = "-x";
	
	public static String absolute( String pwd, String vfname ) {
		String savedName = vfname;
		try {
			vfname = new File(
					vfname.startsWith( File.separator ) ? vfname : pwd + File.separator + vfname
					).getCanonicalPath();
		} catch( IOException e) {
			System.err.println( "getCanonicalPath("+savedName +" => "+ vfname +") failed:"+ e );
		}
		return vfname;
	}

	private   Strings names; // [ "", "home", "martin", "invoices", "invoice.0" ] or [ "..", "ruth" ]
	protected Strings names() { return names;}
	private   void    names( String s ){ names = new Strings( s, PATH_SEP_CHAR ); }
	private   void    names( Strings ss ){ names = ss; }

	public Path() { this( System.getProperty( PWD_PROPERTY )); }
	public Path( String pwd ) {
		pents = new TreeSet<Pent>();
		names( pwd == null ? System.getProperty( PWD_PROPERTY ) : pwd );
		//audit.debug( "Path set to "+ loaded.toString( Strings.CSV ));
	}

	public String  toString() { return pwd(); }
	public String       pwd() {
		return names.size() == 0 ? "/" : names.toString( Strings.PATH );
	}
	
	public void    pop() { if (0<names.size()) names.remove( names.size()-1 ); } // don't pop leading ""
	public void    push( String val ) { names.add( val ); }
	public void    up() { pop();}
	public boolean isEmpty() { return names == null || 0 == names.size(); } // need arrayEmpty()
	public boolean isAbsolute() { return 0<names.size() && names.get( 0 ).equals( "" ); }
	public boolean isRelative() { return 0<names.size() && !names.get( 0 ).equals( "" ); }

	//TODO:
	// cd -/fred => cd ../fred, cd ../../fred, cd ../../../fred etc,
	// cd +/fred => cd fred, cd */fred, cd */*/fred, cd */*/*/fred etc.
	//  so cd ../-/fred   => cd ../../fred ../../fred etc (but not ../fred)
	// and cd marv/+/fred => cd marv/*/fred marv/*/*/fred,  but NOT cd ruth/fred
	// TODO:
	// cd martin@ change to the target of that link, so if /home/martin@ -> /cygwin/p cd ~ => /cygwin/p
	// TODO:
	// cd ~ => cd `getenv( "HOME" )`
	public boolean cd( String dest ) {
		boolean rc = false;
		// add the dest onto a proposed pwd
		Path candidate = new Path( pwd() );
		Strings dests = new Strings( dest, PATH_SEP_CHAR );
		if (dests.size() > 0 && dests.get( 0 ).equals( "" )) // absolute
			candidate.names( dests );
		else
			for (int i = 0; i<dests.size(); ++i )
				if (dests.get( i ).equals( PARENT ))
					candidate.pop();
				else if (!dests.get( i ).equals( "." ))
					candidate.push( dests.get( i ));
		// see if this proposal exists
		File f = new File( candidate.pwd() );
		if (f.isFile())
			System.err.println( "cd: '"+ dest +"' not a directory!" );
		else if (!f.isDirectory())
			System.err.println( "cd: '"+ dest +"' not found!" );
		else // success
			try {
				names( f.getCanonicalPath() );
				rc = true;
			} catch( Exception e ) {
				System.err.println( "getCanonicalPath failed:"+ e.toString());
			}

		return rc;
	}
	// =======================
	private TreeSet<Pent> pents = new TreeSet<Pent>(); // [ "0.address", "1.client", "3.id", "4.week.0", "4.week.1", ... ]
	public void pathListDelete() { pents = new TreeSet<Pent>(); }
	
	private String filter( String s ) {
		String str = "";
		for (int i=0, sz=s.length();
				i<20
				&& i<sz
				&& (Character.isLetterOrDigit( s.charAt( i )) || ' ' == s.charAt( i ));
				i++)
			str += s.charAt( i );
		return str;
	}
	public void insertDir( String dname, String opts ) {
		File dirp = new File( dname );
		if (dirp.isDirectory()) {
			File[] list = dirp.listFiles();
			if (list != null) {
				// first remove anything...
				String name;
				for (File flist : list) {
					name = flist.getName();
					if( '!' == name.charAt( 0 ))
						pents.remove( new Pent( name.substring( 1 ) ));
				}
				// ...then add in what is in this dir
				for (File flist : list) {
					name = flist.getName();
					if( '!' != name.charAt( 0 )) {
						if( opts.equals( OPT_A ) || '.' != name.charAt( 0 )) {
							String value = "";
							if (opts.equals( OPT_X )) {
								if (flist.isFile()) // TTD: move this to String - newCharsFromFilePreview()
									value = filter( Fs.stringFromFile( flist.getPath()));
								else if (Link.isLink( name ))
									value = filter( Link.content( name));
								else if (flist.isDirectory())
									value = "<"+ filter(name) +"/>";
							}
							
							//this needs to do the removes etc.
							
							Pent p = new Pent( name, value, flist.isDirectory() );
							if (!pents.contains( p ))
								pents.add( p ); // pents.arrayInsert( p, pentCmp );
			}	}	}	}
	}	}
	public Iterator<Pent> iterator() { return  pents.iterator(); }

	public Iterator<Pent> list() {
		for (String fname : new File( pwd() ).list()) insertDir( fname, "" );
		return iterator();
}	}