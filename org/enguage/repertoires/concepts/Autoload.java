package org.enguage.repertoires.concepts;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.enguage.repertoires.Repertoires;
import org.enguage.sign.Config;
import org.enguage.util.Audit;
import org.enguage.util.Strings;

/** Implements Dynamic Repertoires:
 * attempts to load all words in an utterance as a repertoire.
 */
public class Autoload {
	private Autoload() {}
	
	private static Audit audit = new Audit( "Autoload" );
	
	private static TreeMap<String,Integer> autoloaded = new TreeMap<>();
	public  static Integer get( String name ) {return autoloaded.get( name );}
	public  static void    put( String name ) {autoloaded.put( name, 0 );}
	
	/**
	 * ttl - Time to live. Specifies the number of unrelated utterances before a concept is purged
	 */
	private static int  ttl = 5;
	public  static void ttl( String age ) {
		try {ttl = Integer.valueOf( age );}
		catch (Exception e){/*ignore*/}
	}
	/*
	 * autoloading - Simple flag to prevent autoloading when autoloading.
	 */
	private static boolean autoloading = false;

	/*
	 * load methods...
	 */
	private static void add( String candidate, Strings concepts ) {
		// set age to 0, or -1 if we're still configuring  
		autoloaded.put( candidate, Config.complete() ? 0 : -1 );
		concepts.add( candidate );
	}
	public static void autoload( Strings utterance ) {
		if (!autoloading) {
			autoloading = true;
			
			Strings concepts = new Strings();
			for (String candidate : Concept.match( utterance ))
				if (null != autoloaded.get( candidate ) ||
				    !Concept.loadConceptFile( candidate ).equals( "" ))
					add( candidate, concepts ); // reload should reset age
				else
					audit.error( "failed to autoload: "+ candidate ); 
				
			if (!concepts.isEmpty()) audit.debug( "Autoload: "+ concepts );
			
			Similarity.autoload( utterance );			
				
			autoloading = false;
	}	}
	/*
	 * Unloading functions...
	 */
	public static void unloadNamed( String name ) {
		audit.debug( ">>>>> Auto Unloading "+ name );
		Repertoires.signs().remove( name );
		autoloaded.remove( name );
	}
	public static boolean unloadConditionally( String name ) {
		if (autoloaded.containsKey( name )) {
			unloadNamed( name );
			return true;
		}
		return false;
	}
	public static void unloadAged() {
		Strings repsToRemove = new Strings();
		
		// first, create a list of repertoire to remove...
		Set<Map.Entry<String,Integer>> set = autoloaded.entrySet();
		Iterator<Map.Entry<String,Integer>> i = set.iterator();
		while (i.hasNext()) {
			Map.Entry<String,Integer> me = i.next();
			String repertoire = me.getKey();
			Integer       age = me.getValue();
			if (age != -1) {
				if (age > ttl)
					repsToRemove.add( repertoire );
				else
					autoloaded.put( repertoire, ++age );
		}	}
		
		// then, do the removals...
		Iterator<String> ri = repsToRemove.iterator();
		while (ri.hasNext()) unloadNamed( ri.next() );
	}
	
	public static Strings loaded() {
		Strings out = new Strings();
		for (Map.Entry<String,Integer> entry : autoloaded.entrySet())
			out.add( " "+ entry.getKey());
		return out;
}	}
