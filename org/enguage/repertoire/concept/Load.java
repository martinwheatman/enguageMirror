package org.enguage.repertoire.concept;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.SortedSet;
import java.util.TreeSet;

import org.enguage.Enguage;
import org.enguage.repertoire.Repertoire;
import org.enguage.signs.interpretant.Intention;
import org.enguage.signs.interpretant.Redo;
import org.enguage.signs.objects.Variable;
import org.enguage.util.Audit;
import org.enguage.util.sys.Fs;
import org.enguage.util.tag.Tag;

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

	private static final String rwRpts() {return Fs.root() +Repertoire.LOC+ File.separator;}
	public  static String roRpts( String prefix ) {
		return prefix+ Enguage.RO_SPACE +Repertoire.LOC+ File.separator;
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
				audit.ERROR( "renaming "+ oldFile +" to "+ newFile );
	}	}

	
	public static String loadConcept( String name ) {
		return loadConcept( name, null, null );
	}
	public static String loadConcept( String name, String from, String to ) {
		boolean wasLoaded   = true;
		boolean wasSilenced = false;
		boolean wasAloud    = Enguage.shell().isAloud();
		String  conceptName = to==null ? name : name.replace( from, to );
		
		Variable.set( Assets.NAME, name );
		
		// silence inner thought...
		if (!Audit.startupDebug) {
			wasSilenced = true;
			Audit.suspend();
			Enguage.shell().aloudIs( false );
		}
		
		Intention.concept( conceptName );
		InputStream  is = null;
		
		if ((null != (is = getFile( spokenName( name )))) ||
			(null != (is = Assets.getStream( writtenName( name )))))
			Enguage.shell().interpret( is, from, to );
		else
			wasLoaded = false;
		
		if (is != null) try{is.close();} catch(IOException e2) {}
		
		//...un-silence after inner thought
		if (wasSilenced) {
			Audit.resume();
			Enguage.shell().aloudIs( wasAloud );
		}
		
		Variable.unset( Assets.NAME );
		return wasLoaded ? conceptName : "";
	}

	public  static void load( String name ) {
		if (!loaded().contains( name )) {
			if (!Audit.startupDebug) Audit.suspend();
			// loading won't use undo - disable
			Redo.undoEnabledIs( false );
			
			audit.debug( ">>>>>LOADING "+ name );
			String conceptName = loadConcept( name );
			if (!conceptName.equals( "" ))
				loaded( conceptName );
			
			Redo.undoEnabledIs( true );
			if (!Audit.startupDebug) Audit.resume();
		}
	}
	public static void loadTag( Tag concepts ) {
		if (null != concepts) {
			Repertoire.transformation( true );
			for (int j=0; j<concepts.content().size(); j++) {
				String name = concepts.content().get( j ).name;
				if (name.equals( "concept" )) {
					String op = concepts.content().get( j ).attribute( "op" );
					String id = concepts.content().get( j ).attribute( "id" );

					if (!op.equals( "ignore" ))
						load( id ); // using itself!!
					
			}	}
			Repertoire.transformation( false );
		} else
			audit.ERROR( "Concepts tag not found!" );
	}
}
