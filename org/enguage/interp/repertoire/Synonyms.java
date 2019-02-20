package org.enguage.interp.repertoire;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.enguage.util.Strings;
import org.enguage.util.attr.Attributes;
import org.enguage.vehicle.Plural;

public class Synonyms {
	//private static Audit audit = new Audit( "Synonyms" );
	
	static private TreeMap<String,Integer> autoloaded = new TreeMap<String,Integer>();
	
	static private Attributes list = new Attributes();
	static private String     list( String synonym ) { return list.get( synonym );}
	static public  void       list( String synonym, String existing ) {
		if (existing.equals( "" ))
			list.remove( synonym );
		else
			list.add( synonym, existing );
	}
	static private Strings match( Strings utterance ) {
		//audit.out( "match", utterance.toString() );
		Strings matches = new Strings();
		Strings synonyms  = list.names();
		for (String uttered : utterance ) // e.g. uttered="want"
			if (synonyms.contains( uttered ))
				matches.add( uttered );
		return matches; //return (Strings) audit.out( matches ); //
	}
	public static void autoload( Strings utterance ) {
		//audit.in( "autoload", ""+utterance );
		// utterance="i want a coffee" => need+needs.txt/need/want
		for (String synonym : Synonyms.match( utterance )) {
			// e.g. synonym="want"
			String existing = Synonyms.list( synonym );
			// e.g. name="need"
			String nameMeA = synonym+"+"+Plural.plural( synonym );
			String loadMeA = existing+"+"+Plural.plural( existing );
			String nameMeB = Plural.plural( synonym )+"+"+synonym;
			String loadMeB = Plural.plural( existing )+"+"+existing;
			if (null != autoloaded.get( synonym ) || Concept.load( existing, existing, synonym ))
				autoloaded.put( synonym, 0 );
			else if (null != autoloaded.get( nameMeA ) || Concept.load( loadMeA, existing, synonym ))
				autoloaded.put( nameMeA, 0 );
			else if (null != autoloaded.get( nameMeB ) || Concept.load( loadMeB, existing, synonym ))
				autoloaded.put( nameMeB, 0 );
		}
		//audit.out();
	}
	static public void unload() {
		Strings removals = new Strings();
		
		// create a list of repertoire to remove...
		Set<Map.Entry<String,Integer>> set = autoloaded.entrySet();
		Iterator<Map.Entry<String,Integer>> i = set.iterator();
		while(i.hasNext()) {
			Map.Entry<String,Integer> me = (Map.Entry<String,Integer>)i.next();
			String repertoire = me.getKey();
			Integer nextVal = me.getValue() + 1;
			if (nextVal > Autoload.ttl())
				removals.add( repertoire );
			else
				autoloaded.put( repertoire, nextVal );
		}
		
		// ...now do the removals...
		Iterator<String> ri = removals.iterator();
		while (ri.hasNext()) {
			String repertoire = ri.next();
			Repertoire.signs.remove( repertoire );
			autoloaded.remove( repertoire );
	}	}
	public static void main( String args[]) {
		list( "want", "need" );
		autoload( new Strings( "I want a coffee" )); // -->load need+need, need, want
		//...
		unload();
}	}
