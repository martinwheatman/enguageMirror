package com.yagadi.enguage.sign.repertoire;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.yagadi.enguage.object.Overlay;
import com.yagadi.enguage.sign.intention.Redo;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Fs;
import com.yagadi.enguage.util.Strings;
import com.yagadi.enguage.vehicle.Language;

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

	static public void load( Strings utterance ) {
		//audit.in( "load", utterance.toString());
		// should not be called if initialising or if autoloading
		if (!ing()) {
			Autoload.ing( true );
			Redo.undoEnabledIs( false ); // disable undo while loading repertoires
			
			Strings tmp = new Strings();
			for (String candidate : Concepts.matches( utterance )) {
				if (!Language.isQuoted( candidate )// don't try to load: anything that is quoted, ...
					&&	!candidate.equals(",")             // ...punctuation, ...
					&&	!Strings.isUpperCase( candidate )) // ...hotspots, ...
				{
					//audit.debug( "candidate is "+ candidate );
					// let's just singularise it: needs -> need
					//if (Plural.isPlural( candidate )) candidate = Plural.singular( candidate );
					
					if (Concepts.loaded().contains( candidate )) {// don't load...
						;//audit.debug( "already loaded on init: "+ candidate );
					} else if (null==autoloaded.get( candidate )) { //...stuff already loaded.
						if (Concept.load( candidate )) {
							//audit.debug( "autoloaded: "+ candidate );
							autoloaded.put( candidate, 0 ); // just loaded so set new entry to age=0
							tmp.add( candidate );
						} else // ignore, if no repertoire!
							audit.ERROR( "not loaded" );
					} else { // already exists, so reset age to 0
						//udit.debug("resetting age: " + candidate);
						autoloaded.put(candidate, 0);
			}	}	}
			
			//audit.debug( "Autoload.load(): "+ utterance +" => ["+ tmp.toString( Strings.CSV ) +"]");
			Redo.undoEnabledIs( true );
			Autoload.ing( false );
			//audit.out();
	}	}
	static public void unload() {
		if (!ing()) {
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
				else {
					//audit.debug( "Aging "+ repertoire +" (now="+ nextVal +"): " );
					autoloaded.put( repertoire, nextVal );
			}	}
			
			// ...now do the removals...
			Iterator<String> ri = repsToRemove.iterator();
			while (ri.hasNext()) {
				String repertoire = ri.next();
				//if (Audit.detailedDebug) audit.debug( "unloaded: "+ repertoire );
				Repertoire.signs.remove( repertoire );
				autoloaded.remove( repertoire );
			}
			//audit.debug( "unloanding => ["+ repsToRemove.toString( Strings.CSV ) +"]" );
	}	}
	public static void main( String args[] ) {
		Audit.allOn();
		Audit.allTracing = true;
		if (!Fs.location( "./src/assets" ))
			audit.FATAL( "./src/assets: not found" );
		else if (!Overlay.autoAttach()) {
			audit.ERROR( " can't auto attach" );
		} else {
			Concepts.names( "./src/assets" );
			load( new Strings( "i need a coffee" ));
			load( new Strings( "martin needs a coffee" ));
}	}	}
