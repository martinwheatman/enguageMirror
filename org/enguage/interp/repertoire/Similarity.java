package org.enguage.interp.repertoire;

import org.enguage.objects.Variable;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Attributes;
import org.enguage.vehicle.Plural;
import org.enguage.vehicle.reply.Reply;

public class Similarity {
	static public final String NAME = "similarity";
	//static private      Audit audit = new Audit( NAME );
	static public final int      id = 230093813; //Strings.hash( "similarity" );

	static private Attributes similarities = new Attributes();
	
	private static boolean load( String name, String load, String from, String to ) {
		if (null != Autoload.get( name ))
			return true;
		else {
			String conceptName = Concepts.loadConcept( load, from, to );
			if (!conceptName.equals( "" )) {
				Autoload.put( conceptName );
				return true;
		}	}
		return false;
	}
	static public void autoload( Strings utterance ) {
		// utterance="i want a coffee" => load( "want+wants.txt", "need+needs.txt", "need", "want" )
		for (String synonym : similarities.matchNames( utterance )) {
			String existing = similarities.value( synonym );
			if (!load( synonym, existing, existing, synonym )                      &&
				!load( synonym+"+"+Plural.plural( synonym ),
				       existing+"+"+Plural.plural( existing ),  existing, synonym ) &&
				!load( Plural.plural(  synonym )+"+"+synonym,
				       Plural.plural( existing )+"+"+existing, existing, synonym ));
	}	}
	static private void autoUnload( String name ) {
		if (Autoload.unloadConditionally( name ) ||
			Autoload.unloadConditionally( name+"+"+Plural.plural( name )) ||
			Autoload.unloadConditionally( Plural.plural( name )+"+"+name )) ;
	}
	static public Strings interpret( Strings cmds ) {
		// e.g. ["create", "want", "need"]
		Strings rc = Reply.failure();
		int     sz = cmds.size();
		if (sz > 0) {
			String cmd = cmds.remove( 0 );
			rc = Reply.success();
			
			if (cmd.equals( "between" ) && sz>3) {
				// e.g. "create want / need"
				Attribute a = new Attribute(
						Attribute.value( cmds.getUntil( "and" )).toString( Strings.UNDERSC ),   //from
						Attribute.value( cmds                  ).toString( Strings.UNDERSC ) ); // to
				if (!similarities.hasName( a.name() )) similarities.add( a );
			
			} else if (cmd.equals( "destroy" ) && sz==2) {
				// e.g. "destroy want"
				String name = cmds.toString( Strings.UNDERSC );
				similarities.remove( name );				
				autoUnload( name );
				
			} else if (cmd.equals( "save" ))
				Variable.set( NAME, similarities.toString());
			
			else if (cmd.equals( "recall" ))
				similarities = new Attributes( Variable.get( NAME ));
			
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
}
}
