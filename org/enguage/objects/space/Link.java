package org.enguage.objects.space;

import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.sys.Fs;
import org.enguage.util.sys.Shell;
import org.enguage.vehicle.reply.Reply;

public class Link {
	static public  final int     id = 217371; //Strings.hash( "link" );
	static private       Audit audit = new Audit( "Link" );
	
	/* Need to support:
	 *   Composites : martin/hand holding ruth/hand: martin/hand/holding = "ruth/hand"
	 *   Relatives  : martin/lhand holding ../rhand: martin/lhand/holding = "../rhand"
	 */

	// **************
	// ************** FS Helpers
	// **************
	// java fs model is s...  symlink-less!
	static private final String symLinkExtension = ".symlink" ;
	static public boolean isLink( String s ) {
		return	s.length() > symLinkExtension.length()
				&& s.substring( s.length() - symLinkExtension.length()).equals( symLinkExtension );
	}
	static public String linkName( String name ) {
		return isLink( name ) ? name : name + symLinkExtension;
	}
	static public boolean fromString( String nm, String val ) {
		//audit.in( "fromString", "rawName="+ nm +", value="+ val );
		//return audit.out( Fs.stringToFile( Overlay.fsname( linkName( nm ), Overlay.MODE_WRITE ), val ));
		return Fs.stringToFile( Overlay.fsname( linkName( nm ), Overlay.MODE_WRITE ), val );
	}
	static public String toString( String nm ) {
		//audit.in( "toString", "rawName="+ nm );
		return Fs.stringFromFile( linkName( nm )); //audit.out( Fs.stringFromFile( linkName( nm )));
	}
	
	static private String extrd( String e, String a ) {
		return Overlay.fsname( e +"/"+ a, Overlay.MODE_READ );
	}
	static private String extwr( String e, String a ) {
		return Overlay.fsname( e +"/"+ a, Overlay.MODE_WRITE );
	}

	// **************
	// ************** Link commands:
	// **************

	static private String get( String entity, String attr ) {
		return (String) 
			audit.info( "get", 
						"entity='"+ entity +"', attribute='"+ attr +"' ",
						toString( extrd( entity, attr )));
	}
	static private boolean create( String from, String name, String to ) { return set( from, name, to );}
	static private boolean set( String from, String name, String to ) {
		boolean rc = false;
		audit.in( "set", "from='"+ from +"', name='"+ name +"', to=["+ to +"]" );
		
		if (from.equals("") || name.equals("") || to.equals(""))
			audit.ERROR( "set: null param: from='"+ from +"', name='"+ name +"', to=["+ to +"]" );
		else if (rc = fromString( from+"/"+name, to ))
			audit.debug( "Ok, created: "+ Value.name( from, name, Overlay.MODE_WRITE ));

		return audit.out( rc );
	}

	static private boolean delete( String entity, String attribute ) {
		boolean status = false;
		audit.in( "delete", "entity="+ entity +", attribute="+ attribute );
		String  read   = extrd( entity, linkName( attribute )),
		        write  = extwr( entity, linkName( attribute )),
		        delete = Entity.deleteName( write );
		if (Fs.exists( read )) // ignore otherwise
			status = Fs.exists( write ) ?
					 Fs.rename( write, delete )
					 : Fs.stringToFile( delete, "" );
		return audit.out( status );
	}
	static private boolean destroy( String entity, String attr ) {
		return (boolean)
			audit.info( "destroy",
						"e='"+ entity+"', a='"+attr+"' ",
						Fs.destroy( extwr( entity, linkName( attr )))
					  );
	}
	static private boolean exists( String entity, String link ) {
		return (boolean)
			audit.info( "exists",
						"entity="+ entity +", link="+ link,
						Fs.exists( extrd( entity, linkName( link ))));
	}
	static private boolean exists(String entity, String attr, String value ) {
		String buffer = toString( extrd( entity, attr ));
		audit.debug( "exists: buffer is '"+ buffer +"'" );
		return (buffer == null || buffer.equals( "" )) ? // not found OR buffer too small
			false
			: null == value || buffer.equals( value ) ?
					true
					: exists( buffer, attr, value );
	}
	static private boolean attribute(String e, String l, String a, String val ) {
		Value v;
		return (boolean)
			audit.info( "attribute", "ent="+e+", link="+l+", attr="+a+", value="+val,
						!e.equals( "" ) &&
							(( (v = new Value( e, a )).exists() && val.equals(""))
								|| v.equals( val )
								|| attribute( toString( extrd( e, l )), l, a, val )
					  )		);
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
			String	cmd    = args.remove( 0 ),
					entity = args.remove( 0 ),
					attr   = args.remove( 0 ),
					target = argc > 3 ? args.remove( 0 ) : "",
					value  = argc > 4 ? args.remove( 0 ) : "";
			
			if (cmd.equals("set") || cmd.equals( "create" )) {
				rc = create( entity, attr, target ) ? Shell.SUCCESS : Shell.FAIL;
			} else if (cmd.equals("get")) {
				rc = get( entity, attr );
			} else if (cmd.equals("exists")) {
				rc = exists( entity, attr ) ? Reply.yesStr() : Reply.noStr();
			} else if( cmd.equals("destroy")) {
				if (exists( entity, attr ))
					rc = destroy( entity, attr ) ? Shell.SUCCESS : Shell.FAIL;
			} else if(cmd.equals("delete")) { // args[3] == target
				rc = (argc == 3 || exists( entity, attr, target )) && delete( entity, attr ) ?
					  Shell.SUCCESS : Shell.FAIL;
			} else if (cmd.equals( "exists" )) {
				rc = exists( entity, attr, target ) ? Shell.SUCCESS : Shell.FAIL;
			} else if (cmd.equals("attribute")) {
				rc = attribute( entity, attr, target, value ) ? Shell.SUCCESS : Shell.FAIL;
			} else
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
		Fs.location( "./src/assets" );
		
		Overlay o = Overlay.Get();

		if ((null == o || !o.attached() ) && !Overlay.autoAttach())
			audit.ERROR( "Ouch! >>>>>>>> Cannot autoAttach() to object space<<<<<<" );
		else {
			//Audit.allOn();
			test( "create martin loves ruth",   Shell.SUCCESS );
			test( "create martin hates ruth",   Shell.SUCCESS );
			test( "destroy martin hates ruth",  Shell.SUCCESS );
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
}	}	}