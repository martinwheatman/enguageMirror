package org.enguage.objects.space;

import java.io.File;
import java.io.IOException;

import org.enguage.util.Audit;
import org.enguage.util.sys.Fs;

public class Series { // relates to hypothetical attachment of series of overlays to fs 
	static private Audit audit = new Audit( "Series" );

	static public  final String DETACHED="";
	static private final String basePointer = File.separator + "reuse";
	static public  final String DEFAULT = "Enguage"; //"sofa";
	
	// return "enguage.0/.reuse";
	static private String baseName( String nm ) { return name( nm, 0 ) + basePointer; }
	
	//  return contentOf( "enguage.0/.reuse" ); -- "/home/martin/data"
	static public String base( String nm ) {
		//audit.in( "base", nm );
		if (nm==null || nm.equals( DETACHED )) return DETACHED;
		//audit.out( Filesystem.stringFromLink( baseName( nm )));
		return Link.toString( baseName( nm ));
	}
	static public boolean existing( String nm ) {
		//audit.log( "Series.existing(): basename is "+ baseName( nm ));
		return null != nm && !nm.equals( DETACHED ) && Fs.exists( baseName( nm ) + ".symlink" );
	}
	static public boolean create( String name, String whr ) {
		//audit.in( "create", "name="+name+", whence="+whr );
		boolean rc = true;
		try {
			Link.fromString( baseName( name ), new File( whr ).getCanonicalPath());
		} catch (IOException e) {
			audit.ERROR( "Series.create(): error in canonical path!" );
			rc = false;
		}
		//return audit.out( rc );
		return rc;
	}
	static private String name = DETACHED;
	static public  void   name( String nm ) { name = nm; }
	static public  String name() { return name; }
	static public  String name( int vn ) { return Ospace.charsAndInt( Ospace.root() + File.separator + name, vn ); }
	static public  String name( String nm, int vn ) { return Ospace.charsAndInt( Ospace.root() + File.separator + nm, vn ); }
	
	static private int  number = 0; // series.0, series.1, ..., series.n => 1+n
	static public  int  number() { return number; } // initialised to 1, for 0th overlay
	
	static private int  highest = -1; // series.0, series.1, ..., series.n => 1+n
	static public  int  highest() { return highest; } // initialised to 1, for 0th overlay
	
	static public boolean count() {
		//audit.in( "count", "name='"+ name +"'" );
		highest = -1;
		number = 0;
		String candidates[] = new File( Ospace.root() ).list();
		if (candidates != null) for (String candidate : candidates) {
			//audit.debug( "candidate: "+ candidate );
			if (   candidate.length() > name.length()+1
			    && (name + ".").equals( candidate.substring( 0, name.length()+1 )) )
			{
				number++;
				int n = Integer.parseInt( candidate.substring( name.length() + 1));
				if ( n>highest ) highest = n;
				//audit.debug( "   counted: "+ number +" overlay(s), highest="+ highest );
		}	}
		//return audit.out( highest > -1 );
		return highest > -1;
	}
	static public boolean attached() { return !name.equals( DETACHED ); }
	static public boolean attach( String series ) {
		name( series );
		/* Need to create it if it doesn't exists! To record pwd...
		 * TODO: Need to distinguish between ~/a and ~/b/a: series name a digest of 'reused'
		 * autoAttach() should take pwd as reused.symlink content and current dname series name
		 */
		if (!count())
			try {
				Series.create( series, new File( "." ).getCanonicalPath() ); // we are attached - it may not exist!
			} catch(IOException e) {audit.log("excception:"+ e.toString());}
		return Series.attached();
	}
	static public void detach() {
		name( DETACHED );
		highest = -1;
		number = 0;
	}
	static public void append() {
		//audit.in( "append", "" );
		highest++;
		File overlay = new File( name( highest ));
		if (!overlay.mkdir()) {
			highest--;
			audit.ERROR( "unable to create "+ overlay.toString());
		} else {
			number++;
			//audit.debug( "created, overlay "+ highest +", count ="+ number );
		}
		//audit.out();
	}
	public static void moveFile( File src, File dest ) {
		//audit.in( "moveFile", "moving folder: "+ src.toString() +" to "+ dest.toString());
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
    			//if (!
    					src.delete()
    					//) audit.ERROR( "deleting "+ src.toString() +" failed" )
    					;
    			//// and delete underlying file
        		//audit.debug( "d-...and delete underlying file "+ dest.toString());
    			new File( Entity.nonDeleteName( dest.toString() )).delete();
    		} else
    			audit.ERROR( "Fs.moveFile(): fallen off end moving "+ src.toString() );
    	}
    	//audit.out();
	}	
	static public boolean compact( /* int targetNumber */) {
		//audit.in( "compact", "combining "+ (number()-1) +" underlays" );
		/*
		 * For expediency's sake, this function combines all underlaid  (i.e. protected) overlays
		 */
		boolean rc = false;
		//audit.debug( "top overlay is "+ highest );
		if (attached() && highest > 1) {
			File src, dst;
			for (int overlay=highest()-1; overlay>0; overlay--) {
				//audit.debug( "COMBINING "+ overlay +" with "+ (overlay-1) );
				src = new File( name( overlay ));
				if (src.exists()) {
					dst = new File( name( overlay-1 ));
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
					count();
	    			//audit.debug( "number of overlays is now: "+ number() );
				} //else
					//audit.debug( "doing nothing: overlay "+ src.toString() +" does not exists" );
			}
			// then _rename_ top overlay - will now be ".1"
			//audit.debug("Now renaming top overlay "+ highest() +" to be 1" );
			src = new File( name( highest() ));
			dst = new File( name( 1 ));
			if (dst.exists()) dst.delete();
			if (!src.renameTo( dst )) audit.ERROR( "RENAME "+ src.toString()+" to "+ dst.toString() +" FAILED" );
			//if (src.exists()) audit.debug( src.toString() +" still exists - after RENAME ;(" );
			// adjust count()
			count();
			//audit.debug( "count really is: "+ number() );
			rc = true;
		}
		//audit.out( "combine "+ (rc?"done":"failed") +", count="+ number() +", highest="+ highest() );
		return rc;
	}
	public String toString() { return name();}
	
	public static void main( String argv[] ) {
		Audit.allOn();
		Series s = new Series();
		attach( "enguage" );
		audit.log( "Series is "+ s.toString());
}	}

