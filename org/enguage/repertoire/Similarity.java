package org.enguage.repertoire;

import org.enguage.repertoire.concept.Autoload;
import org.enguage.repertoire.concept.Load;
import org.enguage.signs.objects.Variable;
import org.enguage.signs.symbol.config.Plural;
import org.enguage.signs.symbol.reply.Response;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Attributes;

public class Similarity {
	public  static final String NAME = "similarity";
	private static final Audit audit = new Audit( NAME );
	public  static final int      ID = 230093813; //Strings.hash( "similarity" )

	private  static Attributes similarities = new Attributes();
	
	private static boolean load( String name, String load, String from, String to ) {
		boolean loaded = (null != Autoload.get( name ));
		if (!loaded) {
			String conceptName = Load.loadConcept( load, from, to );
			if (!conceptName.equals( "" )) {
				Autoload.put( conceptName );
				loaded = true;
		}	}
		return loaded;
	}
	public  static void autoload( Strings utterance ) {
		// utterance="i want a coffee" => load( "want+wants.txt", "need+needs.txt", "need", "want" )
		for (String synonym : similarities.matchNames( utterance )) {
			String existing = similarities.value( synonym );
			if (!load( synonym, existing, existing, synonym )                      &&
				!load( synonym+"+"+Plural.plural( synonym ),
				       existing+"+"+Plural.plural( existing ),  existing, synonym ) &&
				!load( Plural.plural(  synonym )+"+"+synonym,
				       Plural.plural( existing )+"+"+existing, existing, synonym ))
				audit.error( "NOT loaded!" );
	}	}
	private  static void autoUnload( String name ) {
		if (Autoload.unloadConditionally( name ) ||
			Autoload.unloadConditionally( name+"+"+Plural.plural( name )) ||
			Autoload.unloadConditionally( Plural.plural( name )+"+"+name )) ;
	}
	public  static Strings interpret( Strings cmds ) {
		// e.g. ["create", "want", "need"]
		audit.in( "interpret", "cmds="+ cmds );
		Strings rc = Response.failure();
		int     sz = cmds.size();
		if (sz > 0) {
			String cmd = cmds.remove( 0 );
			rc = Response.success();
			
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
			
			else rc = Response.failure();				
		}
		audit.out( rc );
		return rc;
	}
	public  static void test( String cmd ) {
		interpret( new Strings( cmd ));
	}
	public static void main( String[] args ) {
		Audit.on();
		test( "create want / need" );
		test( "save" );
		test( "destroy want" );
}	}
