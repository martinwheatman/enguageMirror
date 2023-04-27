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

import org.enguage.Enguage;
import org.enguage.repertoires.Repertoires;
import org.enguage.signs.Sign;
import org.enguage.signs.SignBuilder;
import org.enguage.signs.interpretant.Intention;
import org.enguage.signs.objects.Variable;
import org.enguage.signs.symbol.Utterance;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.sys.Fs;

import com.yagadi.Assets;

public class Load {
	private static Audit audit = new Audit( "Load" );
	
	/* This is the STATIC loading of concepts at app startup -- read
	 * from the config.xml file.
	 */
	private static TreeSet<String>   loaded = new TreeSet<>();
	public  static SortedSet<String> loaded() {return loaded;}
	public  static void              loaded(String name) {loaded.add(name);}

	private static FileInputStream getFile( String name ) {
		FileInputStream is = null;
		try {
			is = new FileInputStream( name );
		} catch (IOException ignore) {}
		return is;
	}

	private static boolean isFlatpak = false;
	public  static  void   isFlatpak( boolean b ) {isFlatpak = b;}

	private static final String rwRpts() {return Fs.root() +Repertoires.LOC+ File.separator;}
	public  static String roRpts( String prefix ) {
		return prefix+ Enguage.RO_SPACE +Repertoires.LOC+ File.separator;
	}
	private static  String writtenName( String name ) {
		return roRpts( isFlatpak ? "/apps/":"" )+ name +".txt";
	}
	public  static  String spokenName( String s ) {return rwRpts()+ s +".txt";}
	private static  String deleteName( String s ) {return rwRpts()+ s +".del";}
	public  static void delete( String cname ) {
		if (cname != null) {
			File oldFile = new File( spokenName( cname ));
			File newFile = new File( deleteName( cname ));
			if (oldFile.exists() && !oldFile.renameTo( newFile ))
				audit.error( "renaming "+ oldFile +" to "+ newFile );
	}	}

	private static Strings terminators = new Strings( ". ? !" );
	public  static void    terminators( Strings a ){ terminators = a; }
	public  static Strings terminators() { return terminators; }
	
	public  static boolean isTerminator(   String s ) { return terminators().contains( s ); }
	public  static String    terminatorIs( Strings a ) {return (null != a) && a.size()>0 ? a.get( a.size() -1) : ""; }
	private static boolean isTerminated(   Strings a ) {
		boolean rc = false;
		if (null != a) {
			int last = a.size() - 1;
			if (last > -1) rc = isTerminator( a.get( last ));
		}
		return rc;
	}
	public static Strings stripTerminator( Strings a ) {
		if (isTerminated( a ))
			a.remove( a.size() - 1 );
		return a;
	}
	public static String stripTerminator( String s ) {
		Strings a = new Strings( s );
		if (isTerminated( a ))
			a.remove( a.size() - 1 );
		return a.toString();
	}
	public static Strings addTerminator( Strings a, String terminator ) {
		a.add( terminators.contains( terminator ) ? terminator : terminators.get( 0 ));
		return a;
	}
	public static Strings addTerminator( Strings a ) { return addTerminator( a, terminators.get( 0 ));}
	
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
	private static void load( InputStream fp, String from, String to ) {
		// adds signs and interprets utterances
		Strings content = preprocessFile( fp, from, to );
		ArrayList<Strings> utterances = content.divide( terminators, false );
		for (Strings utterance : utterances) {
			SignBuilder sb   = new SignBuilder( utterance );
			Sign        sign = sb.toSign();
			if (sign != null)  // by-pass 'latest' - already built
				Repertoires.signs.insert( sign );
			else // if we find, e.g. "this concept is spatial".
				Repertoires.mediate( new Utterance( utterance ));
		}
	}
	public static String loadConcept( String name, String from, String to ) {
		boolean wasLoaded   = true;
		String  conceptName = to==null ? name : name.replace( from, to );
		
		Variable.set( Assets.NAME, name );
		Intention.concept( conceptName );
		Audit.suspend();
		
		InputStream  is = null;
		
		if ((null != (is = getFile( spokenName( name )))) ||
		    (null != (is = Assets.getStream( writtenName( name )))))
			load( is, from, to );
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
			String conceptName = loadConcept( name, null, null );
			if (!conceptName.equals( "" ))
				loaded( conceptName );
			else {
				rc = false;
				Audit.LOG( "error loading "+ name );
		}	}
		return rc;
}	}
