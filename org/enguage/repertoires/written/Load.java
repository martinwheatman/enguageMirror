package org.enguage.repertoires.written;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;

import org.enguage.repertoires.Repertoires;
import org.enguage.signs.Sign;
import org.enguage.signs.SignBuilder;
import org.enguage.signs.interpretant.Intention;
import org.enguage.signs.objects.Variable;
import org.enguage.signs.symbol.Utterance;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.Terminator;

import com.yagadi.Assets;

public class Load {
	private Load() {}
	
	private static Audit audit = new Audit( "Load" );
	
	/* This is the STATIC loading of concepts at app startup -- read
	 * from the config.xml file.
	 */
	private static TreeSet<String>   loaded = new TreeSet<>();
	public  static SortedSet<String> loaded() {return loaded;}
	public  static void              loaded(String name) {loaded.add(name);}

	private static FileInputStream getFile( String name ) {
		FileInputStream is = null;
		try {is = new FileInputStream( name );
		} catch (IOException ignore) {/* ignore */}
		return is;
	}

	public  static void delete( String cname ) {
		if (cname != null) {
			File oldFile = new File( Concept.spokenName( cname ));
			File newFile = new File( Concept.deleteName( cname ));
			if (oldFile.exists() && !oldFile.renameTo( newFile ))
				audit.error( "renaming "+ oldFile +" to "+ newFile );
	}	}

	private static String preprocessLine( String line, String from, String to ) {
		//remove Byte order mark...
		if (line.startsWith("\uFEFF"))
			line = line.substring(1);

		// truncate comment -- only in real files
		int i = line.indexOf( '#' );
		if (-1 != i)
			line = line.substring( 0, i );

		// if we're converting on the fly, e.g. want -> need
		if (from != null)
			line = line.replace( from, to );
		
		return line;
	}
	private static Strings preprocessFile( InputStream fp, String from, String to ) {
		Strings content = new Strings();
		Scanner br = new Scanner( new InputStreamReader( fp ));
			while (br.hasNextLine()) 
				content.addAll( new Strings( preprocessLine( br.nextLine(), from, to )));
		br.close();
		return content;
	}
	private static void loadFileContent( InputStream fp, String from, String to ) {
		// adds signs and interprets utterances
		Strings content = preprocessFile( fp, from, to );
		ArrayList<Strings> utterances = content.divide( Terminator.terminators(), false );
		for (Strings utterance : utterances) {
			SignBuilder sb   = new SignBuilder( utterance );
			Sign        sign = sb.toSign();
			if (sign != null)  // by-pass 'latest' - already built
				Repertoires.signs().insert( sign );
			else // if we find, e.g. "this concept is spatial".
				Repertoires.mediate( new Utterance( utterance ));
		}
	}
	public static String conceptFile( String name, String from, String to ) {
		boolean wasLoaded   = true;
		String  conceptName = to==null ? name : name.replace( from, to );
		
		Variable.set( Assets.NAME, name );
		Intention.concept( conceptName );
		Audit.suspend();
		
		InputStream  is = null;
		
		// should be found on one of these places... in this order(!)
		if ((null != (is = getFile( Concept.spokenName( name )             ))) ||
		    (null != (is = getFile( Concept.spokenRepName( name )          ))) ||
		    (null != (is = Assets.getStream( Concept.writtenName( name )   ))) ||
		    (null != (is = Assets.getStream( Concept.writtenRepName( name ))))   )
			loadFileContent( is, from, to );
		else
			wasLoaded = false;
		
		if (is != null) try{is.close();} catch(IOException e2) {}
		
		Audit.resume();
		Variable.unset( Assets.NAME );
		
		return wasLoaded ? conceptName : "";
	}
	public static boolean load( String name ) {
		boolean rc = true;
		if (!loaded().contains( name )) {
			String conceptName = conceptFile( name, null, null );
			if (!conceptName.equals( "" ))
				loaded( conceptName );
			else {
				rc = false;
				Audit.LOG( "error loading "+ name );
		}	}
		return rc;
}	}
