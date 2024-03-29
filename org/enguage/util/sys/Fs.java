package org.enguage.util.sys;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

public class Fs {
	private Fs() {}
	
	//private static Audit audit = new Audit( "Fs" )
	
	private static String root = ".";
	public  static String root() { return root; }
	public  static void   root( String name ) {
		root = name + File.separator;
		new File( root ).mkdirs();
	}

	// Composite specific
	public static boolean createEntity( String name ) {return new File( name ).mkdirs(); }
	public static boolean renameEntity( String from, String to ) {return new File( from ).renameTo( new File( to )); }
	public static boolean existsEntity( String name ) {return new File( name ).isDirectory(); }
	public static boolean destroyEntity( String name ) {return new File( name ).delete(); }
	// General
	public static boolean create( String name ) {
		if (name == null)
			name = ".";
		return new File( name ).mkdirs();
	}
	public static boolean rename( String from, String to ) { return new File( from ).renameTo( new File( to )); }
	public static boolean exists( String fname ) { return fname != null && new File( fname ).exists(); } // ultimately less File() creation!
	public static boolean destroy( String name ) {
		File f = new File( name );
		if (!f.exists()) // our work is finished!
			return true;
		
		else if (f.isDirectory()) {
			String[] flist = f.list();
			if (flist.length > 0)
				for (String file : flist)
					destroy( name+ File.separator +file );
		}
		return f.delete();
	}
	public static boolean stringToFile( String fname, String value ) {
		boolean rc = true;
		create( new File( fname ).getParent());
		try {
			PrintWriter pw = new PrintWriter( fname );
			pw.println( value );
			pw.close();
		} catch (FileNotFoundException e ) {
			rc = false;
		}
		return rc;
	}
	public static boolean stringAppendFile( String fname, String value ) {
		boolean rc = true;
		create( new File( fname ).getParent());
		try (BufferedWriter pw = new BufferedWriter( new FileWriter( fname, true ))) {
			pw.append( value );
		} catch (IOException e) {rc = false;}
		return rc;
	}
	public static String stringFromFile( String fname ) {
		String value = ""; 
		try (FileInputStream fis = new FileInputStream( fname )) {
			value = stringFromStream( fis );
		} catch (Exception ignoreMe) {}
		return value;
	}
	public static String stringFromStream( InputStream is ) {
		//audit.in( "stringFromStream" )
		String value = "";
		try {
			int n;
			byte[] buf = new byte[ 1024 ];
			StringBuilder sb = new StringBuilder();
			while (-1 != (n = is.read(buf))) {
				for (int i=n; i<1024; i++) buf[ i ] = ' ';
				sb.append( new String( buf ));
			}
			value = sb.toString().trim(); // remove trailing blanks?
		} catch (IOException e) {
			// just ignore non-existent files...
			//audit.debug( "Fs::stringFromInputStream(): IO exception: "+ e )
			value = "";
		}
		//audit.out( value )
		return value;
	}
	public static byte[] fileToBytes( File f ) {
		// possibly used in the webserver
		byte[] value = new byte[ // truncate this at 2Gib
			(int)(f.length() <= Integer.MAX_VALUE ? f.length() : Integer.MAX_VALUE)
		];
		try (
			DataInputStream fis = new DataInputStream( new FileInputStream( f ))
		) {
			fis.readFully( value );
		} catch (IOException e) {;}
		return value;
}	}
