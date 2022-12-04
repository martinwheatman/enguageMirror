package org.enguage.repertoire.concept;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.enguage.Enguage;
import org.enguage.repertoire.Repertoire;
import org.enguage.repertoire.Similarity;
import org.enguage.signs.interpretant.Redo;
import org.enguage.signs.objects.space.Overlay;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.sys.Fs;

/** Implements Dynamic Repertoires:
 * attempts to load all words in an utterance as a repertoire.
 */
public class Autoload {
	private static Audit audit = new Audit( "Autoload" );
	
	/**
	 * ttl - Time to live. Specifies the number of unrelated utterances before a concept is purged
	 */
	private static int  ttl = 5;
	public  static void ttl( String age ) {try {ttl = Integer.valueOf( age );} catch (Exception e){}}
	public  static int ttl() { return ttl;}
	
	/**
	 * autoloading - Simple flag to prevent autoloading when autoloading.
	 */
	public static boolean autoloading = false;
	public static void    ing( boolean al ) { autoloading = al; }
	public static boolean ing() { return autoloading; }

	private static TreeMap<String,Integer> autoloaded = new TreeMap<String,Integer>();
	public  static Integer get( String name ) { return autoloaded.get( name );}
	public  static void    put( String name ) { autoloaded.put( name, 0 );}
	public  static void remove( String name ) { autoloaded.remove( name );}
	public  static boolean containsKey( String name ) { return autoloaded.containsKey( name );}
	
	public static Strings loaded() {
		Strings out = new Strings();
		for(Map.Entry<String,Integer> entry : autoloaded.entrySet())
			out.add( " "+ entry.getKey());
		return out;
	}

	public static void load( Strings utterance ) {
		if (!ing()) {
			Autoload.ing( true );
			Redo.undoEnabledIs( false ); // disable undo while loading repertoires
			
			Strings concepts = new Strings();
			for (String candidate : Names.match( utterance ))
				if (null == autoloaded.get( candidate ))  // Candidate already loaded, OR
				{
					String conceptName = Load.loadConcept( candidate, null, null );     // just loaded so...
					if (!conceptName.equals( "" )) {
						concepts.add( conceptName );
						autoloaded.put( conceptName, 0 ); // ...set new entry to age=0
					} else // ignore, if no repertoire!
						audit.ERROR( "failed to autoload: "+ candidate );
				} else {
					concepts.add( candidate );
					autoloaded.put( candidate, 0 ); // reset to age=0
				}
			if (concepts.size() > 0) audit.debug( "Autoload: "+ concepts );
			
			Similarity.autoload( utterance );			
				
			Redo.undoEnabledIs( true );
			Autoload.ing( false );
	}	}
	public static void unload( String name ) {
		Repertoire.signs.remove( name );
		remove( name );
	}
	public static boolean unloadConditionally( String name ) {
		if (containsKey( name )) {
			unload( name );
			return true;
		}
		return false;
	}
	public static void unload() {
		
		if (!Repertoire.transformation() &&
			!Autoload.ing())
		{
			Strings repsToRemove = new Strings();
			
			// create a list of repertoire to remove...
			Set<Map.Entry<String,Integer>> set = autoloaded.entrySet();
			Iterator<Map.Entry<String,Integer>> i = set.iterator();
			while(i.hasNext()) {
				Map.Entry<String,Integer> me = (Map.Entry<String,Integer>)i.next();
				String repertoire = me.getKey();
				Integer nextVal = me.getValue() + 1;
				if (nextVal > ttl)
					repsToRemove.add( repertoire );
				else
					autoloaded.put( repertoire, nextVal );
			}
			
			// ...now do the removals...
			Iterator<String> ri = repsToRemove.iterator();
			while (ri.hasNext()) unload( ri.next() );

	}	}
	public static void main( String args[] ) {
		Audit.allOn();
		Audit.allTracing = true;
		if (!Fs.location( Enguage.RO_SPACE ))
			audit.FATAL( Enguage.RO_SPACE +": not found" );
		else {
			Overlay.attach( "autoload" );
			load( new Strings( "i need a coffee" ));
			load( new Strings( "martin needs a coffee" ));
}	}	}
