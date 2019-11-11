package org.enguage.objects.space.overlays;

import java.io.File;

import org.enguage.objects.space.Link;
import org.enguage.util.Audit;
import org.enguage.util.sys.Fs;

public class Series { // relates to hypothetical attachment of series of overlays to fs 
	
	static       Audit   audit = new Audit( "Series" );
	
	static public  final String     DEFAULT = "Enguage"; //"sofa";
	static private final String    DETACHED = null;
	
	static private String name = DETACHED;
	static private void   name( String nm ) { if (nm != null) name = nm; }
	static public  String name() { return name; }
	
	static private int    number = 0; // series.0, series.1, ..., series.n => 1+n
	static public  int    number() { return number; } // initialised to 1, for 0th overlay
	
	static public void    count() {
		number = 0;
		String candidates[] = new File( Os.home() ).list();
		if (candidates != null) for (String candidate : candidates) {
			String[] cands = candidate.split("\\.");
			if (cands.length == 2 && cands[0].equals( name ))
				try {	
					Integer.parseInt( cands[ 1 ]);
					number++;
				} catch (Exception ex){}
	}	}
	
	// ---
	static private String  path( String p ) { return Os.home() + p; }
	static public  String  nth( int vn ) { return path( name ) +"."+ vn;}
	
	static public  String  base() { return detached() ? "" : Link.content( path( name ));}
	
	static private boolean detached() { return name == DETACHED;}
	static public  void detach() {
		name( DETACHED );
		number = 0;
	}
	
	static public  boolean attached() { return name != DETACHED;}
	static public  boolean attach( String nm ) {
		name( nm );
		count();
		return existing() || Link.fromString( path( nm ), System.getProperty( "user.dir" ));
	}
	
	static public  boolean existing() { return !detached() && Fs.exists( path( name ) + Link.EXT );}
	static public  void      append() { if (attached()) new File( nth( number++ )).mkdirs(); }
	static public  boolean   remove() { return number >= 0 && attached() && Fs.destroy( Series.nth( --number ));}

	/*
	 * Some helpers from enguage.....
	 * implements the transaction bit -- this isn't ACID :(
	 */
	// --- Compact overlays
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
    		if (!Overlay.isDeleteName( src.toString()) //  ...we have filename...
    				&& new File( Overlay.deleteName( src.toString() )).exists()) // ...and !filename exists
    		{	//// remove !filename
        		//audit.debug( "a-delete !filename "+ Entity.deleteName( src.toString() ));
    			new File( Overlay.deleteName( src.toString() )).delete();
    			//// move file across as before
        		//audit.debug( "a-move file across "+ src.toString() );
        		if (dest.exists()) dest.delete();
        		src.renameTo( dest );
    		} else if ( Overlay.isDeleteName( src.toString()) // ...we have !filename
    				&& new File( Overlay.nonDeleteName( src.toString() )).exists())  // ...and filename exists
    		{	//// just remove !filename (filename will be dealt with when we get there!)
        		//audit.debug( "b-deleting files "+ src.toString() );
        		src.delete();
    			if (src.exists()) audit.ERROR( "b) deleting !filename, with filename ("+ src.toString() +") FAILED" );
    		} else if (!Overlay.isDeleteName( src.toString()) // we have filename..
    				&& !new File( Overlay.deleteName( src.toString() )).exists()) // but not !filename
    		{	//// move file across - as before
        		//audit.debug( "c-move file across: "+ src.toString() +" to "+ dest.toString());
        		if (dest.exists()) if (!dest.delete()) audit.ERROR( "c) DELETE failed: "+ dest.toString() );
        		if (!src.renameTo( dest ))  audit.ERROR( "c) RENAME of "+ src.toString() +" to "+ dest.toString() +" failed" );
    		} else if (Overlay.isDeleteName( src.toString()) // we have !filename 
    				&& !new File( Overlay.nonDeleteName( src.toString() )).exists()) // but not filename
    		{	//// remove !filename...
        		//audit.debug( "d-remove !filename... "+ src.toString() );
    			//if (!
    					src.delete()
    					//) audit.ERROR( "deleting "+ src.toString() +" failed" )
    					;
    			//// and delete underlying file
        		//audit.debug( "d-...and delete underlying file "+ dest.toString());
    			new File( Overlay.nonDeleteName( dest.toString() )).delete();
    		} else
    			audit.ERROR( "Fs.moveFile(): fallen off end moving "+ src.toString() );
    	}
    	audit.out();
	}	
	static public boolean compact() {
		audit.in( "compact", "combining "+ (Series.number()-1) +" underlays" );
		boolean rc = false;
		if (attached() && number > 2) {
			File src, dst;
			for (int overlay=Series.number()-2; overlay>0; overlay--) {
				src = new File( Series.nth( overlay ));
				if (src.exists()) {
					dst = new File( Series.nth( overlay-1 ));
					if (dst.exists())
		    			moveFile( src, dst );
					else
						src.renameTo( dst );					
				}
			}
			// then _rename_ top overlay - will now be ".1"
			//audit.debug("Now renaming top overlay "+ highest() +" to be 1" );
			src = new File( Series.nth( Series.number() -1 ));
			dst = new File( Series.nth( 1 ));
			if (dst.exists()) dst.delete();
			if (!src.renameTo( dst )) audit.ERROR( "RENAME "+ src.toString()+" to "+ dst.toString() +" FAILED" );
			
			number = 2;
			
			rc = true;
		}
		audit.out( "compact "+ (rc?"done":"failed") +", count="+ Series.number() +", highest="+ (Series.number()-1) );
		return rc;
	}
	
	static private boolean inTxn = false;
	static public void startTxn( boolean undoIsEnabled ) {
		audit.in( "startTxn" );
		if (undoIsEnabled) {
			inTxn = true;
			Series.append();//create();
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
	static public void reStartTxn() {
		audit.in( "restartTxn" );
		if (inTxn) {
			Series.remove(); // destroy(); // remove this overlay
			Series.remove(); // destroy(); // remove previous -- this is the undo bit
			Series.append(); // create();  // restart a new txn
		}
		audit.out();
	}
	

	public String toString() { return name();}
	
	static public void main( String argv[] ) {
		Audit.allOn();
		Series s = new Series();
		attach( "enguage" );
		Audit.log( "Series is "+ s.toString());
}	}