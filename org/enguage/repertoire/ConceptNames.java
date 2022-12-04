package org.enguage.repertoire;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.enguage.Enguage;
import org.enguage.signs.interpretant.Intention;
import org.enguage.signs.interpretant.Redo;
import org.enguage.signs.objects.Variable;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.sys.Fs;
import org.enguage.util.tag.Tag;

import com.yagadi.Assets;

/** Concepts is: the list of concept names;
 *               the list of loaded concepts; and,
 *               a name-to-concept function.
 */
public class ConceptNames {
	private ConceptNames() {}
	
	public static  final String     NAME = "concepts";
	private static       Audit     audit = new Audit( NAME );
	
	private static boolean isFlatpak = false;
	public static  void    isFlatpak( boolean b ) {isFlatpak = b;}

	private static TreeSet<String> names = new TreeSet<>();
	public  static  void  remove( String name ) { names.remove( name );}
	public  static  void     add( String name ) { names.add(    name );}
	public  static  void  addAll( Strings nms ) { names.addAll(  nms );}
	
	public  static Strings tree( String base, String location ) {
		Strings  names = new Strings();
		String[] files = new File( base + location ).list();
		for (String file : files)
			if (file.endsWith( ".txt" ))
				names.add( (!location.equals(".") ? location+"/" : "")+ file );
			else if (new File( base + location +File.separator+ file ).isDirectory())
				names.addAll( tree( base, file ));
		return names;
	}

	public static void addConcepts( String[] names ) {
		if (names != null) for ( String name : names ) { // e.g. name="rpt/hello.txt"
			String[] components = name.split( "\\." );
			if (components.length > 1 && components[ 1 ].equals("txt"))
				add( components[ 0 ]);
	}	}
	private static final String rwRpts() {return Fs.root() +Repertoire.LOC+ File.separator;}
	public  static String roRpts( String prefix ) {
		return prefix+ Enguage.RO_SPACE +Repertoire.LOC+ File.separator;
	}
	public static  String writtenName( String name ) {
		return roRpts( isFlatpak ? "/apps/":"" )+ name +".txt";
	}
	public static  String spokenName( String s ) {return rwRpts()+ s +".txt";}
	public static void delete( String cname ) {
		if (cname != null) {
			File oldFile = new File( spokenName( cname ));
			File newFile = new File( rwRpts() + cname +".del" );
			if (!oldFile.renameTo( newFile ))
				audit.ERROR( "renaming "+ oldFile +" to "+ newFile );
	}	}
	private static FileInputStream getFile( String name ) {
		FileInputStream is = null;
		try {
			is = new FileInputStream( name );
		} catch (IOException ignore) {}
		return is;
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
	/* This is the STATIC loading of concepts at app startup -- read
	 * from the config.xml file.
	 */
	private static TreeSet<String>   loaded = new TreeSet<>();
	public  static SortedSet<String> loaded() {return loaded;}
	public  static void load( String name ) {
		if (!loaded.contains( name )) {
			if (!Audit.startupDebug) Audit.suspend();
			// loading won't use undo - disable
			Redo.undoEnabledIs( false );
			
			String conceptName = loadConcept( name, null, null );
			if (!conceptName.equals( "" ))
				loaded.add( conceptName );
			
			Redo.undoEnabledIs( true );
			if (!Audit.startupDebug) Audit.resume();
		}
	}
	public static void load( Tag concepts ) {
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
	private static boolean matchAnyBoilerplate( Strings utt, Strings bplt ) {
		Iterator<String> ui  = utt.iterator();
		Iterator<String> bi = bplt.iterator();
		boolean first = true;
		while (ui.hasNext() && bi.hasNext()) { // read through the utterance & pattern
			Iterator<String> pwi = new Strings( bi.next(), '_' ).iterator();
			if (pwi.hasNext()) {
				String pw = pwi.next();
				String ut = ui.next();
				// find the first matching token
				if (!first)
					while (!ut.equals(pw) && ui.hasNext())
						ut = ui.next();
				//now go thru' matching tokens
				while (ut.equals(pw) && ui.hasNext() && pwi.hasNext()) {
					ut = ui.next();
					pw = pwi.next();
				}
				// return if we have a mismatch
				if (!pw.equals(ut)) return false;
				// hyphen represents at least 1 utterance string, just read over it
				if (bi.hasNext())
					if (ui.hasNext())
						ui.next();
					else
						return false; // hyphen but no text...
				else
					return !ui.hasNext();
				first = false;
		}	}
		return true;
	}
	private static boolean matchSign( Strings utt, String sign ) {
		Strings allBplate = new Strings( sign, '-' );
		switch (allBplate.size()) {
		case 0: return true;
		case 1: return utt.contains( new Strings( allBplate.iterator().next(), '_' ));
		default:return matchAnyBoilerplate( utt, allBplate );
	}	}
	private static boolean matchConceptNames( Strings utterance, String cfname ) {
		// matching: "to my name is martin reply hello martin" with "to-reply-"
		for (String sign : new Strings( cfname, '+' )) // e.g. c="to_the_phrase-reply-"
			if (matchSign(utterance, sign))
				return true;
		return false;
	}
	private static boolean matchConcepts( Strings utterance, String concept ) {
		// Strip dir name from concept filename
		String[] tmp = concept.split( "/" ); // OR closeAirSupport/egress
		return matchConceptNames( utterance, tmp[ tmp.length-1 ]);
	}
	public static Strings match( Strings utterance ) {
		/* match([martin is a wally]):
		 *     names=[ "is_a+has_a" ] 
		 * } => ( is_a+has_a )
		 */
		Strings matches = new Strings();
		for (String concept : names ) // e.g."is_a+has_a" OR "to_the_phrase-reply_with"
			if (matchConcepts( utterance, concept ))
				matches.add( concept );
		return matches;
	}
	public static List<Strings> conjuntionAlley( Strings s ) {
		ArrayList<Strings> ls = new ArrayList<>();
		boolean found = false;
		Strings ss = new Strings();
		List<Strings> tmps = s.nonNullSplit( "and" );
		for (Strings tmp : tmps) {
			if (!found) {
				if (!ss.isEmpty()) ss.add( "and" );
				ss.addAll( tmp );
				found = !match(tmp).isEmpty();
				
			} else if (match(tmp).isEmpty()) {
				ss.add( "and" );
				ss.addAll( tmp );
				
			} else {
				ls.add( ss );
				ss = tmp;
				found = false;
		}	}
		if (!ss.isEmpty()) ls.add( ss );
		
		return ls;
}	}
