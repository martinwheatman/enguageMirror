package org.enguage.objects.list;

import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Attributes;
import org.enguage.util.sys.Shell;

public class Transitive {
	
	public  static final String NAME = "transitive";
	public  static final int      id = 245880631; //Strings.hash( NAME );
	private static       Audit audit = new Audit( NAME );
	
	private static Strings    concepts = new Strings();
	public  static boolean  isConcept(  String   s ) { return concepts.contains( s ); }
	public  static void    addConcept(  String   s ) {if (!concepts.contains( s )) concepts.add( s );}
	public  static void    addConcepts( Strings ss ) {for (String s : ss) addConcept( s );}

	public static String list() { return concepts.toString( Strings.CSV );}
	
	private static Attributes attrs = new Attributes();
	public  static void add( String name, String value ) {
		Attribute a = new Attribute( value, name );
		if (!attrs.contains( a )) {
			a = new Attribute( name, value );
			if (!attrs.contains( a ))
				attrs.add( a );
	}	}
	public  static boolean are( String name, String value ) {
		return attrs.value( name ).equals( value );
	}

	static public Strings interpret( Strings args ) {
		audit.in( "interpret", args.toString() );
		String rc = Shell.IGNORE;
		if (args.size() > 1) {
			rc = Shell.FAIL;
			String cmd = args.remove( 0 );
			if (cmd.equals( "add" )) {
				int sz = args.size();
				while (sz >= 2) {
					// this assumes left-to-right evaluation
					add( args.remove( 0 ), args.remove( 0 ));
					sz -= 2;
				}
				if (sz == 1)
					addConcepts( args );
				rc = Shell.SUCCESS;
		}	}
		return audit.out( new Strings( rc ));
	}
	public static void main( String args[] ) {
		Audit.log( interpret( new Strings( "add cause" )));
		Audit.log( interpret( new Strings( "add cause effect" )));
		Audit.log( "cause->effect: "+ are( "cause", "effect" ));
		Audit.log( "effect->cause: "+ are( "effect", "cause" ));
}	}
