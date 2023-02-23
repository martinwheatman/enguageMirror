package org.enguage.repertoires.written;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.enguage.Enguage;
import org.enguage.repertoires.Repertoires;
import org.enguage.signs.interpretant.Intention;
import org.enguage.signs.interpretant.Redo;
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
			if (!oldFile.renameTo( newFile ))
				audit.error( "renaming "+ oldFile +" to "+ newFile );
	}	}

	private static Strings terminators = new Strings( ". ? !" );
	public static void    terminators( Strings a ){ terminators = a; }
	public static Strings terminators() { return terminators; }
	
	public static boolean  isTerminator( String s ) { return terminators().contains( s ); }
	public static String   terminatorIs( Strings a ){ return (null != a) && a.size()>0 ? a.get( a.size() -1) : ""; }
	private static boolean isTerminated( Strings a ) {
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
	

	private static void rejig(int rcSz, Strings primary, String connector, ArrayList<Strings>rc) {
		for (int i=0; i<rcSz; i++) {
			Strings tmp = new Strings();
			if (i==0)
				tmp.addAll( primary );
			else
				tmp.add( 0, connector );
			tmp.add( "," );
			tmp.addAll( rc.get( i ));
			rc.set( i, tmp );
	}	}
	private static List<Strings> expandSemicolonList( Strings sentence ) {
		/*  "on one: do two; do three; and, do four." =>
		 *  [ "on one, do two.", "and on one, do three.", "and on one, do four." ]
		 *  "and" many be replace by, for example "or", "then", "also"
		 */
		ArrayList<Strings> rc = new ArrayList<>();
		
		// create a primary and a list of secondaries
		boolean isPrimary = true;
		Strings primary = new Strings();    // On "a B c" (:)
		Strings secondary = new Strings (); // if so, do something[;|.]
		for (String s : sentence) 
			if (s.equals(":")) {
				isPrimary = false;
			} else if (s.equals(";")) {
				rc.add( secondary );
				secondary = new Strings();
			} else if (isPrimary) {
				primary.add( s );
			} else {
				secondary.add( s );
			}
		if (!secondary.isEmpty()) rc.add( secondary );
		
		// if we've found no semi-colon separated list...
		if (rc.isEmpty()) {
			// ...just pass back the original list
			rc.add( sentence );
		} else {
			// remove connector from last in list
			int rcSz = rc.size();
			String connector = "then"; // default: this is the only one used!
			Strings lastList = rc.get( rcSz-1 );
			if ( lastList.size() > 2 && lastList.get( 1 ).equals( ",") ) {
				connector=lastList.remove( 0 ); // remove connector
				lastList.remove( 0 );           // remove ","
				rc.set( rcSz-1, lastList );     // replace this last item
			}
			// re-jig list, adding-in primary, and the connector on subsequent lists
			rejig(rcSz, primary, connector, rc);
		}
		return rc;
	}
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

	public static void loadFile( InputStream fp, String from, String to ) { // reads file stream and "interpret()"s it
		try (BufferedReader br = new BufferedReader( new InputStreamReader( fp ))) {
			String line;
			Strings stream = new Strings();
			while ((line = br.readLine()) != null) {
				stream.addAll( new Strings( preprocessLine( line, from, to )));
				ArrayList<Strings> sentences = stream.divide( terminators, false );
				if (sentences.size() > 1) {
					Strings sentence = sentences.remove( 0 );
					stream = Strings.combine( sentences );
					for (Strings s : expandSemicolonList( sentence ))
						Repertoires.mediate( new Utterance( s ));
			}	}
		} catch (java.io.IOException e ) {
			audit.error( "IO error in Shell::interpret(stdin);" );
	}	}
	
	public static String loadConcept( String name, String from, String to ) {
		boolean wasLoaded   = true;
		String  conceptName = to==null ? name : name.replace( from, to );
		
		Variable.set( Assets.NAME, name );
		Intention.concept( conceptName );
		Audit.suspend();
		
		InputStream  is = null;
		
		if ((null != (is = getFile( spokenName( name )))) ||
		    (null != (is = Assets.getStream( writtenName( name )))))
			loadFile( is, from, to );
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
			//if (!Audit.startupDebug) Audit.suspend();
			// loading won't use undo - disable
			Redo.undoEnabledIs( false );
			
			//audit.debug( ">>>>>LOADING "+ name );
			String conceptName = loadConcept( name, null, null );
			if (!conceptName.equals( "" ))
				loaded( conceptName );
			else {
				rc = false;
				Audit.LOG( "error loading "+ name );
			}
			
			Redo.undoEnabledIs( true );
			//if (!Audit.startupDebug) Audit.resume();
		}
		return rc;
	}
}
