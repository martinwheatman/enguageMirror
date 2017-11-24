package org.enguage.object;

import java.io.File;
import java.io.IOException;

import org.enguage.util.Audit;
import org.enguage.util.Fs;
import org.enguage.util.Shell;
import org.enguage.util.Strings;

import org.enguage.object.Entity;
import org.enguage.object.Link;
import org.enguage.object.LinkShell;
import org.enguage.object.Overlay;
import org.enguage.object.Value;

class LinkShell extends Shell {
	LinkShell() { super( "LinkShell" );}
	public String interpret( Strings a ) { return Link.interpret( a ); }
}

// we may have had: my car's colour is red. ==> martin/car/colour -> ../../red
// We have:
//   create(  "user", "martin", 0 ); --> user -> martin // simle link
//   create("martin", "holding", [ "ruth", "hand" ]) --> ./martin/holding -> ../ruth/hand
// we also want (where ./"martin/car/" exists!):
//   create("martin", "car", [ "f209klg" ]) --> ./martin/car -> ../f209klg
// being done at wrong level(?) as we aslo assert:     ./f209klg/isa -> ../car
// we aslo want to support components:
// create( "martin/keys", "location", [ "mach", "kitchen" ]);
// => martin/keys/location -> ../../mach/kitchen
// Sooooo..... the number of parents "../" is dependent on the number of "levels/" in entity string!!!
// this has so far only been 1! e.g. entity="martin", attribute="attr" => link="../attr"
public class Link {
	static private Audit audit = new Audit( "Link" );
	
	public static String get( String entity, String attribute ) {
		//audit.traceIn( "get", "entity='"+ entity +"', attribute='"+ attribute +"'" );
		if ( entity.charAt( 0 ) == '$' ) entity = "_"+ entity.substring( 1 ); // not relevant here?
		String linkName = null != attribute ?
				Value.name( entity, Fs.linkName( attribute ), Overlay.MODE_READ ) :
				Entity.name(    Fs.linkName( entity ), Overlay.MODE_READ ) ;
		String str = "";
		//audit.debug( linkName +" => '"+ str +"'" );
		if (Fs.exists( linkName )) {
			str = Fs.stringFromLink( linkName );
			if (str.length() > 3)
				str = str.substring(null != attribute ? 3 : 0) ; // attr value will start "../"
		}
		return str; //audit.traceOut( str );
	}
	static public boolean create( String entity, String attribute, Strings values ) { return set( entity, attribute, values );}
	static public boolean set( String entity, String attribute, Strings values ) {
		//audit.traceIn( "set", "entity='"+ entity +"', attribute='"+ attribute +"', values=["+ Strings.toString( values, Strings.CSV ) +"]" );
		boolean status = false;
		if (entity!=null && attribute != null) {
			String linkName, content;
			if ( values != null && 0 < values.size() ) { // link martin mother janet -- mother is attribute!
				content = "../"; // differnt to C version as Strings.PATH, is not equiv to toPath in Link.c
				// component support... prefix "../"'s to match entity depth...
				for (int i=0, sz=entity.length(); i<sz; i++) {
					if ('/' == entity.charAt( i )) { // for every '/'...
						content += "../"; // ...use an extra "../"
						while (i<sz && '/' == entity.charAt( 1+i )) i++; // remove instances of "////"
				}	}
				// ...component support.
				content += values.toString( Strings.PATH ); // ../ruth/hand
				linkName = Value.name( entity, attribute, Overlay.MODE_WRITE ); // martin/holding
				Entity.create( entity );                                        // martin
			} else { // link NAME value -- simple representation of (versionable!) shell vars as symlinks
				linkName = Entity.name( entity, Overlay.MODE_WRITE );
				content = attribute;
			}
			//audit.debug( "deleting: "+ linkName );
			new File( linkName ).delete();
			//audit.debug( "creating: "+ linkName +", content:"+ content );
			Fs.stringToLink( linkName, content);
			status = true;
		}
		//audit.traceOut( status ? "TRUE" : "FALSE" );
		return status;
	}

	// doesn't yet support null attribute
	public static boolean delete( String entity, String attribute ) { 
		Entity.ignore( Fs.linkName( Value.name( entity, attribute, Overlay.MODE_WRITE )));
		return true; // lets be optimistic!
	}
	// doesn't yet support null attribute
	public static boolean destroy( String entity, String attribute ) {
		//audit.traceIn( "destroy", "e='"+ entity+"', a='"+attribute+"'" );
		String name = Fs.linkName( Value.name( entity, attribute, Overlay.MODE_WRITE ));
		//audit.debug( "deleting NAME='"+ NAME +"'" );
		//return audit.traceOut( Filesystem.destroy( NAME ));
		return Fs.destroy( name );
	}
	private static boolean arrayCharsEqual( Strings a, Strings b ) {
		boolean rc = null == a && null == b; // both empty -- equal
		if (null != a && null != b) { // both not empty -- inspect array contents!
			int ai = 0, bi = 0, az=a.size(), bz=b.size();
			// skip any parent dirs, so "../../martin" is equal to "martin"
			while (ai<az && a.get( ai ).equals("..")) ai++;
			while (bi<bz && b.get( bi ).equals("..")) bi++;
			//ai--; bi--; // step back on both!
			while (az>ai && bz>bi && b.get( bi ).equals( a.get( ai ))) {
				ai++; bi++;
			}
			rc = az == ai && bz == bi;
		}
		return rc;
	}
	// martin/car/colour/silver, martin/car -> ../pj55ozw + pj55ozw/colour -> silver => found
	private static boolean linkAndValue(String name, Strings value) {
		//audit.traceIn( "linkAndValue", "NAME='"+ NAME +"', value=["+ Strings.toString( value, Strings.CSV ) +"]");
		boolean found = false;
		String candidate = Overlay.fsname( Fs.linkName( name ), Overlay.MODE_READ );
		if (Fs.exists( candidate )) {
			found = true;
			String buffer= Fs.stringFromLink( candidate );
			//audit.debug( candidate + ": found -> "+ (found ? buffer : "FALSE"));
			// now see if we've found a link... is it the right one?
			if (value != null && 0 != value.size()) { // found AND we have a required value...
				Strings b = new Strings( buffer, '/' );
				//audit.debug( "Checking value '"+ Strings.toString( b, Strings.CSV ) +"' <==> '"+ Strings.toString( value, Strings.CSV ) +"'");
				found = arrayCharsEqual( b, value );
				//audit.debug( "Found is "+ (found?"TRUE":"FALSE"));
		}	}
		return found ; //audit.traceOut( found );
	}

	// Currently this supports:
	// linkExists( "martin", "holding", [ "ruth", "hand" ]) => martin/holding -> ../ruth/hand
	// We also want:
	// linkExists( "martin", "car", [ "colour", "red" ]) => martin/car/colour -> ../red
	// possibly "../../red", possibly "red"?
	// need to deal with martin/car -> ../pj55ozw/bodyShell -> ../1234567890/colour -> red
	// so first link found might not be the only one!
	// we may even have user -> martin ???
	// 2013/10/01 - some of this doesn't work: link exists person isa isa (should get thru to class)
	//   TODO:    - should this call transExists()?
	public static boolean exists( Strings a ) {
		//audit.traceIn( "exists", Strings.toString( a, Strings.CSV ));
		boolean found = false;
		if ( null != a && (a.size() >= 2)) {
			String candidate = new String( a.get( 0 ));
			int ai = 0, az = a.size();
			while (ai<az && !found) { // keep going until we've found it, or run out of array
				a = a.copyAfter( 0 );
				//audit.debug( "candidate='"+ candidate +"', "+ Strings.toString( a, Strings.CSV ));
				if (!(found = linkAndValue( candidate, a ))) { // ...not found
					String buffer = null,
					       cand = Fs.linkName( candidate );
					if (Fs.exists( cand )) {
						buffer = Fs.stringFromLink( cand );
						//audit.debug( "found intermediate link => "+ buffer );
						if (!buffer.equals("")) {
							if ('/' == buffer.charAt( 0 ))
								candidate = Overlay.fsname( buffer, Overlay.MODE_READ );
							else {
								try {
									candidate = new File( candidate + "/../" + buffer ).getCanonicalPath();
								} catch(IOException e) {
									found = false;
					}	}	}	}
				if (a == null || a.size() == 0) break;

				// swap next array value onto candidate - whether or not we've found a link...
				candidate += ( "/" + a.get( 0 ));
		}	}	}
		return found; //audit.traceOut( found );
	}
/*	// doesn't yet support null values
	private boolean linkExistsOld(String entity, String link, Strings values ) {
		String NAME = Filesystem.sym( Attribute.name( entity, link, Overlay.MODE_READ ));
		return (null != NAME && NAMEStrings() > 3) // not found OR buffer too small
			&& (null != values || 0 != values.size() || 0 == NAME.substring(3).equals( Strings.toString( values, Strings.PATH ) )); // no value to compare of value compares ok
	}*/

	//---
	// doesn't yet support null value
	// assumes all links are "../X"
	public static boolean transExists(String entity, String trans, String value ) {
		boolean rc = false;
		String buffer = Fs.stringFromLink( Value.name( entity, trans, Overlay.MODE_READ ) );
		if (buffer.length() <= 3) // not found OR buffer too small
			rc = false;
		else if (null == value || buffer.substring(3).equals( value ))
			rc = true;
		else if (!buffer.substring(3).equals("class"))
			rc = transExists( buffer.substring(3), trans, value );
		return rc;
	}

	// doesn't yet support null value
	// assumes all links are "../X"
	public static boolean transAttrExists(String entity, String trans, String value ) {
		boolean rc = false;
		if (new File( Value.name( entity, value, Overlay.MODE_READ )).isFile()) 
			rc = true;
		else {
			String buffer = Fs.stringFromLink( Value.name( entity, trans, Overlay.MODE_WRITE ));
			if (buffer.length() <= 3) // not found OR buffer too small
				rc = false;
			else if (null == value || buffer.substring(3).equals( value ))
				rc = true;
			else if (!buffer.substring(3).equals("class"))
				rc = transAttrExists( buffer.substring(3), trans, value );
		}
		return rc;
	}
	//---
	static public String interpret( Strings a ) {
		//audit.traceIn( "interpret", "["+ Strings.toString( a, Strings.CSV ) +"]" );
		String rc = Shell.SUCCESS;
		int argc = a.size();
		Strings args = a.copyAfter( 2 );
		Strings b = a.copyAfter( 0 );
		
		if(( argc >= 3 ) &&  a.get( 0 ).equals("set")) {
			rc = create( a.get( 1 ), a.get( 2 ), args ) ? Shell.SUCCESS : Shell.FAIL;
		} else if(( argc == 3 ) && a.get( 0 ).equals("get")) {
			rc = get( a.get( 1 ), a.get( 2 ));
		} else if(( argc == 2 ) && a.get( 0 ).equals("get")) {
			rc = get( a.get( 1 ), null );
		} else if(( argc >= 2 ) && a.get( 0 ).equals("exists")) {
			rc = exists( b ) ? Shell.SUCCESS : Shell.FAIL;
		} else if(( argc == 3 ) && a.get( 0 ).equals("delete")) {
			rc = delete( a.get( 1 ), a.get( 2 )) ? Shell.SUCCESS : Shell.FAIL;
		} else if(( argc >= 3 ) && a.get( 0 ).equals("destroy")) {
			if (exists( b )) rc = destroy( a.get( 1 ), a.get( 2 )) ? Shell.SUCCESS : Shell.FAIL;
		} else if(( argc == 4 ) && a.get( 0 ).equals("delete")) {
			if (exists( b )) rc = delete( a.get( 1 ), a.get( 2 )) ? Shell.SUCCESS : Shell.FAIL;
		} else if (( argc == 4 ) && a.get( 0 ).equals("transExists")) {
			rc = transExists( a.get( 1 ), a.get( 2 ), a.get( 3 )) ? Shell.SUCCESS : Shell.FAIL;
		} else if(( argc == 4 ) && a.get( 0 ).equals("transAttrExists")) {
			rc = transAttrExists( a.get( 1 ), a.get( 2 ), a.get( 3 )) ? Shell.SUCCESS : Shell.FAIL;
		} else {
			rc = Shell.FAIL;
			System.out.println(
					"Usage: link: [set|get|exists|transExists|transAttrExists|delete] <ent> <link> [<value>]\n"+
					"given: "+ a.toString( Strings.SPACED ));
		}
		return rc; // audit.traceOut( rc );	
	}
	public static void main( String args[] ) {
		if (!Overlay.autoAttach())
			audit.ERROR( "Ouch!" );
		else
			new LinkShell().run();
}	}