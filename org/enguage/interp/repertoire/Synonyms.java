package org.enguage.interp.repertoire;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.enguage.objects.Variable;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Attributes;
import org.enguage.vehicle.Plural;
import org.enguage.vehicle.reply.Reply;

public class Synonyms {
	static public final String NAME = "synonyms";
	//static private      Audit audit = new Audit( NAME );
	static public final int      id = 237427137; //Strings.hash( "synonyms" );

	static private TreeMap<String,Integer> autoloaded = new TreeMap<String,Integer>();
	static private Attributes                synonyms = new Attributes();
	
	private static boolean attempt( String name, String load, String from, String to ) {
		if (null != autoloaded.get( name ) || com.yagadi.Assets.loadConcept( load, from, to )) {
			autoloaded.put( name, 0 );
			return true;
		}
		return false;
	}
	public static void autoload( Strings utterance ) {
		// utterance="i want a coffee" => load( "want+wants.txt", "need+needs.txt", "need", "want" )
		for (String synonym : synonyms.matchNames( utterance )) {
			String existing = synonyms.get( synonym );
			if (!attempt( synonym, existing, existing, synonym )                    &&
				!attempt( synonym+"+"+Plural.plural( synonym ),
				         existing+"+"+Plural.plural( existing ),  existing, synonym ) &&
				!attempt( Plural.plural(  synonym )+"+"+synonym,
				          Plural.plural( existing )+"+"+existing, existing, synonym ));
	}	}
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
	static public Strings interpret( Strings cmds ) {
		// e.g. ["create", "want", "need"]
		Strings rc = Reply.failure();
		int     sz = cmds.size();
		if (sz > 0) {
			String cmd = cmds.remove( 0 );
			rc = Reply.success();
			
			if (cmd.equals( "create" ) && sz>3)
				// e.g. "create want / need"
				synonyms.add( Attribute.value( cmds.getUntil( "/" )).toString( Strings.UNDERSC ), //from
				              Attribute.value( cmds                ).toString( Strings.UNDERSC ));//to
				
			else if (cmd.equals( "destroy" ) && sz==2)
				synonyms.remove( cmds.toString( Strings.UNDERSC ));
			
			else if (cmd.equals( "save" ))
				Variable.set( NAME, synonyms.toString());
			
			else if (cmd.equals( "recall" ))
				synonyms = new Attributes( Variable.get( NAME ));
			
			else rc = Reply.failure();				
		}
		return rc;
	}
	static public void test( String cmd ) {
		interpret( new Strings( cmd ));
	}
	public static void main( String args[]) {
		Audit.allOn();
		test( "create want / need" );
		test( "save" );
//		autoload( w/"I want a coffee" ); // -->loads need+needs, with need="want"
//		//...
//		unload();
		test( "destroy want" );
}	}
