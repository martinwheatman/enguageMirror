package org.enguage.repertoire;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.TreeSet;

import org.enguage.Enguage;
import org.enguage.objects.Variable;
import org.enguage.signs.intention.Intention;
import org.enguage.signs.intention.Redo;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.sys.Fs;
import org.enguage.util.tag.Tag;

import com.yagadi.Assets;

/** Concepts is: the list of concept names;
 *               the list of loaded concepts; and,
 *               a name-to-concept function.
 */
public class Concepts {
	static public  final String     NAME = "concepts";
	static private       Audit     audit = new Audit( NAME );
	
	static private boolean isFlatpak = false;
	static public  void    isFlatpak( boolean b ) {isFlatpak = b;}

	static private TreeSet<String> names = new TreeSet<String>();
	static public  void  remove( String name ) { names.remove( name );}
	static public  void     add( String name ) { names.add(    name );}
	static public  void  addAll( Strings nms ) { names.addAll(  nms );}
	
	static public Strings tree( String base, String location ) {
		Strings names = new Strings();
		String[] files = new File( base + location ).list();
		for (String file : files)
			if (file.endsWith( ".txt" ))
				names.add( (!location.equals(".") ? location+"/" : "")+ file );
			else if (new File( base + location +"/"+file ).isDirectory())
				names.addAll( tree( base, file ));
		return names;
	}

	static public void addConcepts( String[] names ) {
		if (names != null) for ( String name : names ) { // e.g. name="cpt/hello.txt"
			String[] components = name.split( "\\." );
			if (components.length > 1 && components[ 1 ].equals("txt"))
				add( components[ 0 ]);
	}	}
	static final private String rwRpts() {return Fs.root() +Repertoire.LOC+ File.separator;}
	static public  String roRpts( String prefix ) {
		return prefix+ Enguage.RO_SPACE +Repertoire.LOC+ File.separator;
	}
	static public  String writtenName( String name ) {
		return roRpts( isFlatpak ? "/apps/":"" )+ name +".txt";
	}
	static public  String spokenName( String s ) {return rwRpts()+ s +".txt";}
	static public void delete( String cname ) {
		if (cname != null) {
			File oldFile = new File( spokenName( cname )),
			     newFile = new File( rwRpts() + cname +".del" );
			if (!oldFile.renameTo( newFile ))
				audit.ERROR( "renaming "+ oldFile +" to "+ newFile );
	}	}
	static private FileInputStream getFile( String name ) {
		FileInputStream is = null;
		try {
			is = new FileInputStream( name );
		} catch (IOException ignore) {}
		return is;
	}
	static public String loadConcept( String name, String from, String to ) {
		boolean wasLoaded   = true,
		        wasSilenced = false,
		        wasAloud    = Enguage.shell().isAloud();
		String conceptName = to==null ? name : name.replace( from, to );
		
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
	static private TreeSet<String> loaded = new TreeSet<String>();
	static public  TreeSet<String> loaded() { return loaded; }
	static public void load( String name ) {
		if (!loaded.contains( name )) {
			// loading won't use undo - disable
			Redo.undoEnabledIs( false );
			String conceptName = loadConcept( name, null, null );
			if (!conceptName.equals( "" ))
				loaded.add( conceptName );
			Redo.undoEnabledIs( true );
	}	}
	static public void load( Tag concepts ) {
		if (null != concepts) {
			Repertoire.transformation( true );
			for (int j=0; j<concepts.content().size(); j++) {
				String name = concepts.content().get( j ).name;
				if (name.equals( "concept" )) {
					String op = concepts.content().get( j ).attribute( "op" ),
							 id = concepts.content().get( j ).attribute( "id" );

					if (!Audit.startupDebug) Audit.suspend();

					if (!op.equals( "ignore" ))
						load( id ); // using itself!!
					
					if (!Audit.startupDebug) Audit.resume();
			}	}
			Repertoire.transformation( false );
		} else
			audit.ERROR( "Concepts tag not found!" );
	}
	static private boolean matchesHyphenatedPattern( Strings s, String pattern ) {
		// where pattern may be "to_the_phrase-reply_with"
		Strings pcomp = new Strings( pattern, '-' );
		Iterator<String> ui  = s.iterator();
		Iterator<String> phi = pcomp.iterator();
		if (pcomp.size()>1) { // hyphens found...
			boolean first = true;
			while (ui.hasNext() && phi.hasNext()) { // read through the utterance & pattern
				Iterator<String> pwi = new Strings( phi.next(), '_' ).iterator();
				if (pwi.hasNext()) {
					String pw = pwi.next(),
					       ut = ui.next();
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
					if (phi.hasNext())
						if (ui.hasNext())
							ui.next();
						else
							return false; // hyphen but no text...
					else
						return !ui.hasNext();
					first = false;
			}	}
		} else if(pcomp.size() == 1) // no hyphens found - simple match
			return s.contains( new Strings( phi.next(), '_' ));
		
		return true;
	}
	public static Strings matched( Strings utterance ) {
		//audit.in( "matches", utterance.toString() );
		// matches: utt=[martin is a wally], candiates=[ "is_a+has_a" ] => add( is_a+has_a )
		Strings matches = new Strings();
		for (String candidate : names ) { // e.g. "is_a+has_a" OR "to_the_phrase-reply_with"
			String[] candida = candidate.split( "/" );  // OR cloasAirSupport/egress
			Strings  candid  = new Strings( candida[ candida.length-1 ], '+' );
			// matching: "to my name is martin reply hello martin" with "to-reply-"
			for (String c : candid) { // e.g. c="to_the_phrase-reply-"
				if (matchesHyphenatedPattern(utterance, c))
					matches.add( candidate );
		}	}
		//return audit.out( matches );
		return matches;
	}
	/*
	 *  --- test code
	 */
	private static void test( String s, boolean matchesToReply ) {
		Strings sa = matched( new Strings( s ));
		Audit.log( sa.size() == 0 ? "Doesn't match" :
			       ("matches: " 
		            +sa.toString( Strings.DQCSV ) 
		            +(matchesToReply ? " should":" shouldn't")
		            +" match to-reply-"));
	}
	public static void main( String args[]) {
		addConcepts( com.yagadi.Assets.listConcepts());
		test( "i need a coffee", false );
		test( "to the phrase my name is variable name reply hello variable name", true );
		test( "to reply hello variable name", false );
		test( "to hello reply", false );
		test( "hello to fred reply way", false );
}	}
