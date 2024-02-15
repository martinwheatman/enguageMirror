package org.enguage.sign.object.sofa.fs;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeSet;

import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class Path {
	
	private static Audit audit = new Audit( "Path" );
	
	private static final char   PATH_SEP_CHAR = File.separatorChar; 
	private static final String PWD_PROPERTY = "user.dir"; 
	private static final String PARENT = ".."; 
	private static final String OPT_A = "-a";
	
	public class Pent implements Comparable<Pent> {
		/* This class describes a path entity, which in most cases will just be a name
		 * BUT sometime may include whether it is a directory, and a type.
		 */
		private String  name;
		public  String  name()  {return name;}
		
		public Pent( String nm ) {name = nm;}
		
		@Override
		public int hashCode() {return 0;} // not used
		@Override
		public boolean equals(Object o) {return false;}  // not used
		@Override
		public int compareTo(Pent another) {return name().compareTo( another.name() );}
	}
	
	public static String absolute( String pwd, String vfname ) {
		String savedName = vfname;
		try {
			vfname = new File(
					vfname.startsWith( File.separator ) ? vfname : pwd + File.separator + vfname
					).getCanonicalPath();
		} catch( IOException e) {
			audit.error( "getCanonicalPath("+savedName +" => "+ vfname +") failed:"+ e );
		}
		return vfname;
	}

	private   Strings names; // [ "", "home", "martin", "invoices", "invoice.0" ] or [ "..", "ruth" ]
	protected Strings names() { return names;}
	private   void    names( String s ) {names = new Strings( s, PATH_SEP_CHAR );}
	private   void    names( Strings ss ) {names = ss; }

	public Path() { this( System.getProperty( PWD_PROPERTY )); }
	public Path( String pwd ) {
		pents = new TreeSet<>();
		names( pwd == null ? System.getProperty( PWD_PROPERTY ) : pwd );
	}

	public String  toString() { return pwd(); }
	public String       pwd() {
		return names.isEmpty() ? "/" : names.toString( Strings.PATH );
	}
	
	public void    pop() {if (!names.isEmpty()) names.remove( names.size()-1 );} // don't pop leading ""
	public void    push( String val ) { names.add( val ); }
	public void    up() { pop();}
	public boolean isEmpty() { return names == null || names.isEmpty(); } // need arrayEmpty()
	public boolean isAbsolute() { return !names.isEmpty() && names.get( 0 ).equals( "" ); }
	public boolean isRelative() { return !names.isEmpty() && !names.get( 0 ).equals( "" ); }

	public boolean cd( String dest ) {
		boolean rc = false;
		// add the dest onto a proposed pwd
		Path candidate = new Path( pwd() );
		Strings dests = new Strings( dest, PATH_SEP_CHAR );
		if (!dests.isEmpty() && dests.get( 0 ).equals( "" )) // absolute
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
			audit.error( "cd: '"+ dest +"' not a directory!" );
		else if (!f.isDirectory())
			audit.error( "cd: '"+ dest +"' not found!" );
		else // success
			try {
				names( f.getCanonicalPath() );
				rc = true;
			} catch( Exception e ) {
				audit.error( "getCanonicalPath failed:"+ e.toString());
			}

		return rc;
	}
	
	// =======================
	private TreeSet<Pent> pents = new TreeSet<>(); // [ "0.address", "1.client", "3.id", "4.week.0", "4.week.1", ... ]
	public void pathListDelete() {pents = new TreeSet<>();}
	
	private void removeDeletedNames( File[] list ) {
		String name;
		for (File flist : list) {
			name = flist.getName();
			if( '!' == name.charAt( 0 )) {
				audit.debug( "Path: removing: "+ name );
				pents.remove( new Pent( name.substring( 1 ) ));
	}	}	}
	private void addNames( File[] list, String opts ) {
		String name;
		for (File flist : list) {
			name = flist.getName();
			if ( '!' != name.charAt( 0 ) &&
					(opts.equals( OPT_A ) || '.' != name.charAt( 0 ))) {	
				audit.debug( "Path: adding: "+ name );
				Pent p = new Pent( name );
				if (!pents.contains( p ))
					pents.add( p );
	}	}	}
	public void insertDir( String dname, String opts ) {
		audit.in( "insertDir", "dname="+ dname +", opts="+ opts );
		File dirp = new File( dname );
		if (dirp.isDirectory()) {
			File[] list = dirp.listFiles();
			if (list != null) {
				// first remove anything...
				removeDeletedNames( list );
				// ...then add in what is in this dir
				addNames( list, opts );
		}	}
		audit.out();
	}
	public Iterator<Pent> iterator() {return  pents.iterator();}

	public Iterator<Pent> list() {
		for (String fname : new File( pwd() ).list())
			insertDir( fname, "" );
		return iterator();
}	}