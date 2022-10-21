package org.enguage.signs.objects.space;

import org.enguage.signs.symbol.reply.Response;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.util.sys.Fs;
import org.enguage.util.sys.Shell;

public class Link {
	static public  final String NAME = "link";
	static public  final int      id = 217371; //Strings.hash( "link" );
	static private       Audit audit = new Audit( "Link" );
	
	/* Need to support:
	 *   Composites : martin/hand holding ruth/hand: martin/hand/holding = "ruth/hand"
	 *   Relatives  : martin/lhand holding ../rhand: martin/lhand/holding = "../rhand"
	 */

	// **************
	// ************** FS Helpers - java fs model is s...  symlink-less!
	// **************
	static public final String EXT = ".symlink" ;
	static public boolean isLink( String s ) {
		return	s.length() > EXT.length()
				&& s.substring( s.length() - EXT.length()).equals( EXT );
	}
	static public String linkName( String name ) {return isLink( name ) ? name : name + EXT;}
	static public boolean fromString( String nm, String val ) {
		return Fs.stringToFile( Overlay.fname( linkName( nm ), Overlay.MODE_WRITE ), val );
	}
	static public String content( String nm ) { return Fs.stringFromFile( linkName( nm ));}
	
//	static private String extrd( String e, String a ) {return Overlay.fsname( e +"/"+ a, Overlay.MODE_READ );}
//	static private String extwr( String e, String a ) {return Overlay.fsname( e +"/"+ a, Overlay.MODE_WRITE );}

	// **************
	// ************** two recursive Link commands:
	// **************

	static private boolean exists(String entity, String attr, String value ) {
		Value  v   = new Value( entity, attr );
		String val = v.getAsString();
		return v.exists() && (val.equals( value ) || exists( val, attr, value ));
	}
	static private boolean attribute(String e, String l, String a, String val ) {
		Value v;
		return !e.equals( "" ) &&
				(((v = new Value( e, a     )).exists() && v.equals( val )) ||
				 ((v = new Value( e, l+EXT )).exists() && attribute( v.getAsString(), l, a, val )));
	}
	//---
	static private void usage( Strings a ) { usage( a.toString());}
	static private void usage( String a ) {
		audit.ERROR(
				"Usage: link: [set|get|exists|attribute|destroy|delete] <ent> <link> [<value>]\n"+
				"given: "+ a );
	}
	static public Strings interpret( Strings args ) {
		audit.in( "interpret", "["+ args.toString( Strings.CSV ) +"]" ); 
		String rc = Shell.FAIL;
		int argc = args.size();
		if (argc >= 3 || argc <= 5) {
			rc = Shell.SUCCESS;
			String	cmd    = args.remove( 0 ),
					entity = args.remove( 0 ),
					attr   = args.remove( 0 ),
					target = argc > 3 ? args.remove( 0 ) : "",
					value  = argc > 4 ? args.remove( 0 ) : "";
			
			// We now get passed and un-stripped attribute...
			if (Attribute.isAttribute( entity )) entity = new Attribute( entity ).value();
			if (Attribute.isAttribute( attr   )) attr   = new Attribute( attr   ).value();
			if (Attribute.isAttribute( target )) target = new Attribute( target ).value();
					
			if (cmd.equals("set") || cmd.equals( "create" ))
				rc = new Value( entity, attr+EXT ).set( target ) ? Shell.SUCCESS : Shell.FAIL;
				
			else if (cmd.equals("get"))
				rc = new Value( entity, attr+EXT ).getAsString();
				
			else if (cmd.equals("exists"))
				rc = target.equals( "" ) ?
						new Value( entity, attr+EXT ).exists() ? Response.yesStr() : Response.noStr()
						: exists( entity, attr+EXT, target ) ? Response.yesStr() : Response.noStr();
				
			else if (cmd.equals("delete"))
				if (target.equals( "" ))
					new Value( entity, attr+EXT ).ignore();
				else if (exists( entity, attr+EXT, target ))
					new Value( entity, attr+EXT ).ignore();
				else
					rc =  Shell.FAIL;
				
			else if (cmd.equals("attribute"))
				rc = attribute( entity, attr, target, value ) ? Shell.SUCCESS : Shell.FAIL;
			
			else
				usage( "cmd="+ cmd +", ent="+ entity +", attr="+ attr +", [ "+ args +" ]" );
		} else
			usage( args );
		return audit.out( new Strings( rc ));
	}
	private static Strings test( String cmd, String expected ) {
		Strings reply = interpret( new Strings( cmd ).contract( "/" ));
		if (expected != null && !reply.equals( new Strings( expected )))
			audit.FATAL( cmd + "\nExpecting: "+ expected + "\n  but got: "+ reply );
		else
			audit.passed();
		return reply;
	}
	public static void main( String args[] ) {
		
		Fs.root( null );
		
		Overlay.attach( "Link" );
		
		test( "create martin loves ruth",          Shell.SUCCESS );
		test( "create martin hates name=\"ruth\"", Shell.SUCCESS );
		test( "delete martin hates ruth",          Shell.SUCCESS );
		test( "exists martin hates",        "no" );
		test( "exists martin hates ruth",   "no" );
		test( "exists martin loves",        "yes" );
		test( "exists martin loves ruth",   "yes" );
		test( "create engineer isa person", Shell.SUCCESS );
		test( "create martin isa engineer", Shell.SUCCESS );
		test( "exists martin isa",          "yes" );
		test( "exists martin isa person",   "yes" );
		test( "exists person isa martin",   "no" );
		
		new Value( "person", "age" ).set( "42" );
		test( "attribute martin isa age 42",     Shell.SUCCESS );
		test( "attribute martin isa age 55",     Shell.FAIL );
			
		audit.PASSED();
}	}
