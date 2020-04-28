package org.enguage.interp.repertoire;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.enguage.interp.intention.Redo;
import org.enguage.objects.space.Overlay;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.sys.Fs;

import com.yagadi.Assets;

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
	
	static public Strings loaded() {
		Strings out = new Strings();
		for(Map.Entry<String,Integer> entry : autoloaded.entrySet())
			out.add( " "+ entry.getKey());
		return out;
	}

	static public void load( Strings utterance ) {
		if (!ing()) {
			Autoload.ing( true );
			Redo.undoEnabledIs( false ); // disable undo while loading repertoires
			
			for (String candidate : Concepts.matched( utterance ))
				if (null == autoloaded.get( candidate ))  // Candidate already loaded, OR
				{
					String conceptName = Assets.loadConcept( candidate, null, null );     // just loaded so...
					if (!conceptName.equals( "" ))
						autoloaded.put( conceptName, 0 ); // ...set new entry to age=0
					else // ignore, if no repertoire!
						audit.ERROR( "failed to autoload" );
				} else
					autoloaded.put( candidate, 0 ); // reset to age=0
			
			Similarity.autoload( utterance );			
				
			Redo.undoEnabledIs( true );
			Autoload.ing( false );
	}	}
	static void unload( String name ) {
		Repertoire.signs.remove( name );
		remove( name );
	}
	static boolean unloadConditionally( String name ) {
		if (containsKey( name )) {
			unload( name );
			return true;
		}
		return false;
	}
	static public void unload() {
		
		if (!Repertoire.transformation() &&
			!Repertoire.translation()    && // shouldn't be true?
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
		if (!Fs.location( Assets.LOCATION ))
			audit.FATAL( Assets.LOCATION + File.separator +": not found" );
		else if (!Overlay.attach( "autoload" ))
			audit.ERROR( " can't auto attach" );
		else {
			//Concepts.names();
			load( new Strings( "i need a coffee" ));
			load( new Strings( "martin needs a coffee" ));
}	}	}
