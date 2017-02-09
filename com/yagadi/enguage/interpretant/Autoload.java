package com.yagadi.enguage.interpretant;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Strings;
import com.yagadi.enguage.vehicle.Language;
import com.yagadi.enguage.vehicle.Plural;

public class Autoload {
	/* Implements Dynamic Repertoires:
	 * attempts to load all words in an utterance, singularly, as a repertoire.
	 */
	private static Audit audit = new Audit( "Autoload" );
	
	private static int ttl = 5;
	public  static void ttl( String age ) { try { ttl = Integer.valueOf( age ); } catch( Exception e ) {}}
	
	public static int autoloading = 0;
	public static void    ing( boolean al ) { if (al || autoloading>0) autoloading += al ? 1 : -1; }
	public static boolean ing() { return autoloading>0; }

	static private TreeMap<String,Integer> autoloaded = new TreeMap<String,Integer>();

	// =====================================================================
	// =====================================================================
	private static String match( Strings uttered, TreeSet<String> candidates ) {
		int sz = 0;
		String rc = null;
		for (String candidate : candidates ) { // e.g. "i_am"
			Strings cand = new Strings( candidate, '+' );
			for (String c : cand) {
				Strings d = new Strings( c, '_' );
				if (   uttered.contains( d )
						&& d.size() > sz)
				{
					sz = cand.size();
					rc = candidate;
		}	}	}
		return rc;
	}
	/* private static void test( String utterance, TreeSet<String> repertoires ) {
		String s;
		if (null != ( s = match( new Strings( utterance ), repertoires ))) {
			System.out.println( utterance+" loads "+ s );
		}	} //  -- */
	// =====================================================================
	// =====================================================================
	static public  void load( Strings utterance ) {
		// should not be called if initialising or if autoloading
		if (ing()) {
			audit.ERROR("Autoload.load() called while already autoloading" );
		} else {
			audit.debug( "Autoload.load() utterance="+ utterance );
			Autoload.ing( true );
			Allopoiesis.undoEnabledIs( false ); // disable undo while loading repertoires
			
			//for (String candidate : utterance ) {
			String candidate;
			if (null != ( candidate = match( new Strings( utterance ), Concepts.names() ))) {

				if (!Language.isQuoted( candidate )// don't try to load: anything that is quoted, ...
					&&	!candidate.equals(",")             // ...punctuation, ...
					&&	!Strings.isUpperCase( candidate )) // ...hotspots, ...
				{
					// let's just singularise it: needs -> need
					if (Plural.isPlural( candidate ))
						candidate = Plural.singular( candidate );
					if (Concepts.loaded().contains( candidate )) {// don't load...
						audit.debug( "already loaded on init: "+ candidate );
					} else if (null==autoloaded.get( candidate )) { //...stuff already loaded.
						if (Concepts.loadConcept( candidate )) {
							if (Audit.detailedDebug) audit.debug( "autoloaded: "+ candidate );
							autoloaded.put( candidate, 0 ); // just loaded so set new entry to age=0
						} // ignore, if no repertoire!
					} else { // already exists, so reset age to 0
						if (Audit.detailedDebug) audit.debug("resetting age: " + candidate);
						autoloaded.put(candidate, 0);
			}	}	}
			
			Allopoiesis.undoEnabledIs( true );
			Autoload.ing( false );
	}	}
	static public void unload() {
		if (!ing()) {
			//if (Audit.runtimeDebug) audit.traceIn( "unload", "" );
			
			// read repertoires to remove/age
			Strings repsToRemove = new Strings();
			Set<Map.Entry<String,Integer>> set = autoloaded.entrySet();
			Iterator<Map.Entry<String,Integer>> i = set.iterator();
			while(i.hasNext()) {
				Map.Entry<String,Integer> me = (Map.Entry<String,Integer>)i.next();
				String repertoire = me.getKey();
				Integer nextVal = me.getValue() + 1;
				if (nextVal > ttl) {
					repsToRemove.add( repertoire );
				} else {
					if (Audit.detailedDebug) audit.debug( "ageing "+ repertoire +" (now="+ nextVal +"): " );
					autoloaded.put( repertoire, nextVal );
			}	}
			
			// now do the removals...
			Iterator<String> ri = repsToRemove.iterator();
			while (ri.hasNext()) {
				String repertoire = ri.next();
				if (Audit.detailedDebug) audit.debug( "unloaded: "+ repertoire );
				Repertoire.signs.remove( repertoire );
				autoloaded.remove( repertoire );
			}
			//if (Audit.runtimeDebug) audit.traceOut();
}	}	}
