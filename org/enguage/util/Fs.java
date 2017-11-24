package org.enguage.util;

/* changes - removing preferences - commented out until implemented
 * in app, or restored here
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.enguage.object.Entity;

import org.enguage.util.Audit;
import org.enguage.util.Fs;

public class Fs {
	static Audit audit = new Audit( "Fs" );
	
	static private String  location = "";
	static public  String  location() { return location; }
	static public  boolean location( String s ) {
		location = s;
		return s != null && new File( s ).exists();
	}

	static public final String root = 
		null != System.getenv( "HOME" ) ?
			System.getenv( "HOME" ) :
			//Environment.getExternalStorageDirectory().getPath();
			"./"; // -- this is in the non-android version.
			
	// Composite specific
	static public boolean createEntity( String name ) { return new File( name ).mkdirs(); }
	static public boolean renameEntity( String from, String to ) { return new File( from ).renameTo( new File( to )); }
	static public boolean existsEntity( String name ) { return new File( name ).isDirectory(); }
	static public boolean destroyEntity( String name ) { return new File( name ).delete(); }
	// General
	static public boolean create( String name ) { if (name == null) name = "."; return new File( name ).mkdirs(); }
	static public boolean rename( String from, String to ) { return new File( from ).renameTo( new File( to )); }
	//static public boolean exists( String NAME ) { return new File( NAME ).isDirectory() || new File( NAME ).isFile(); }
	static public boolean exists( String fname ) { return fname != null && new File( fname ).exists(); } // ultimately less File() creation!
	static public boolean destroy( String name ) {
		File dir = new File( name );
		String[] list = dir.list();
		if (list != null) for (int i=0; i<list.length; i++)
			destroy( name+ File.separator +list[ i ]);
		return dir.delete();
	}
	static public void stringToFile( String fname, String value ) {
		//audit.in("stringToFile", "name="+fname+", value="+value);
		create( new File( fname ).getParent()); // just in case?
		try {
			PrintWriter pw = new PrintWriter( fname );
			pw.println( value );
			pw.close();
		} catch (FileNotFoundException e ) {
			//audit.log( "Fs::stringToFile(): File "+ fname +" not found: "+ e );	
		}
		//audit.out();
	}
	static public String stringFromFile( String fname ) {
		//audit.in("stringFromFile", "name="+fname );
		String value = "";
		try {
			FileInputStream fis = new FileInputStream( fname );
			value = stringFromStream( fis );
			fis.close();
		} catch (Exception e) {} // just ignore non-existant files...
		//audit.out( value );
		return value;
	}
	static public String stringFromStream( InputStream is ) {
		//audit.in( "stringFromStream" );
		String value = "";
		try {
			byte buf[] = new byte[ 1024 ];
			while (-1 != is.read(buf)) value += new String( buf );
			value = value.trim(); // remove trailing blanks?
		} catch (IOException e) {
			// just ignore non-existent files...
			//audit.log( "Fs::stringFromInputStream(): IO exception: "+ e );
			value = "";
		}
		//audit.out( value );
		return value;
	}
	// java fs model is s...  symlink-less!
	static private final String symLinkExtension = ".symlink" ;
	static public boolean isLink( String s ) {
		return	s.length() > symLinkExtension.length()
				&& s.substring( s.length() - symLinkExtension.length()).equals( symLinkExtension );
	}
	static public String linkName( String name ) { return isLink( name ) ? name : name + symLinkExtension;}
	static public void   stringToLink( String fname, String value ) {
		//audit.traceIn("stringTOLink", "name="+ fname +", value="+value );
		if (null != fname) {
			//audit.audit("creating "+ (isLink( fname )?fname:fname+symLinkExtension));
			stringToFile( isLink( fname )?fname:fname+symLinkExtension, value );
		}
		//audit.traceOut();
	}
	static public String stringFromLink( String fname ){
		return isLink( fname ) ?
				Fs.stringFromFile( fname ) :
				Fs.stringFromFile( fname+symLinkExtension );
	}
	// This is only used in overlay.java::Series{}
	public static void moveFile( File src, File dest ) {
		//audit.traceIn( "moveFile", "moving folder: "+ src.toString() +" to "+ dest.toString());
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
    		if (!Entity.isDeleteName( src.toString()) //  ...we have filename...
    				&& new File( Entity.deleteName( src.toString() )).exists()) // ...and !filename exists
    		{	//// remove !filename
        		//audit.debug( "a-delete !filename "+ Entity.deleteName( src.toString() ));
    			new File( Entity.deleteName( src.toString() )).delete();
    			//// move file across as before
        		//audit.debug( "a-move file across "+ src.toString() );
        		if (dest.exists()) dest.delete();
        		src.renameTo( dest );
    		} else if ( Entity.isDeleteName( src.toString()) // ...we have !filename
    				&& new File( Entity.nonDeleteName( src.toString() )).exists())  // ...and filename exists
    		{	//// just remove !filename (filename will be dealt with when we get there!)
        		//audit.debug( "b-deleting files "+ src.toString() );
        		src.delete();
    			if (src.exists()) audit.ERROR( "b) deleting !filename, with filename ("+ src.toString() +") FAILED" );
    		} else if (!Entity.isDeleteName( src.toString()) // we have filename..
    				&& !new File( Entity.deleteName( src.toString() )).exists()) // but not !filename
    		{	//// move file across - as before
        		//audit.debug( "c-move file across: "+ src.toString() +" to "+ dest.toString());
        		if (dest.exists()) if (!dest.delete()) audit.ERROR( "c) DELETE failed: "+ dest.toString() );
        		if (!src.renameTo( dest ))  audit.ERROR( "c) RENAME of "+ src.toString() +" to "+ dest.toString() +" failed" );
    		} else if (Entity.isDeleteName( src.toString()) // we have !filename 
    				&& !new File( Entity.nonDeleteName( src.toString() )).exists()) // but not filename
    		{	//// remove !filename...
        		//audit.debug( "d-remove !filename... "+ src.toString() );
    			if (!src.delete()) audit.ERROR( "deleting !fname ("+ src.toString() +") failed");
    			//// and delete underlying file
        		//audit.debug( "d-...and delete underlying file "+ dest.toString());
    			new File( Entity.nonDeleteName( dest.toString() )).delete();
    		} else
    			audit.ERROR( "Fs.moveFile(): fallen off end moving "+ src.toString() );
    	}
    	//audit.traceOut();
}	}
