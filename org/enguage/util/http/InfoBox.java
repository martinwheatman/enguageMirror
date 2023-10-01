package org.enguage.util.http;

import java.util.ListIterator;

import org.enguage.repertoires.Repertoires;
import org.enguage.repertoires.concepts.Autoload;
import org.enguage.sign.Sign;
import org.enguage.sign.symbol.when.Date;
import org.enguage.sign.symbol.where.Address;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Attributes;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class InfoBox {
	public  static final int      ID = 154478; // "html"
	private static final String NAME = "Infobox";
	private static final Audit audit = new Audit( NAME );

	private static String INFO_BOX_CONCEPT = "InfoBoxGen";
	
	private static String source = "";
	public  static String source() {return source;}
	public  static void   source( String s ) {source = s;}

	// Create and insert a new sign for this option...
	private static void insertSign( String[] source, Attribute a, String option) {
		// source = [ "", "/selftest/wiki/The_Eiffel_Tower", "wikipedia" ]
		// a      = "Architectural='300m'"
		// Option = Height
		String[]  path = source[ source.length - 2 ].split( "/" );
		String   topic = path[ path.length - 1 ].replace( "_", " " );
		
		Sign.Builder sb = new Sign.Builder( 
				"On \""
				+ a.name() 
				+" "
				+ option
				+"\", according to wikipedia what is the "
				+ a.name() 
				+" of "
				+ topic
		);
		Sign sign = sb.toSign();
		if (sign==null)
			Audit.log( "sb is null" );
		else {
			sign.concept( INFO_BOX_CONCEPT );
			Repertoires.signs().insert( sign );
	}	}

	public  static Strings interpret( Strings args ) {
		audit.in( "interpret", "args="+ args.toString( Strings.DQCSV ));
		Strings rc = new Strings( "sorry, i don't understand" ); // Shell.Fail;
		String cmd = args.remove( 0 );

		// get the filename- last parameter - and set the source
		String fname = Strings.trim( args.remove( args.size()-1 ), '"' );
		// source is now last portion of cached name
		String[] source = Strings.trim( fname, '"').split( "\\." );
		InfoBox.source( source[ source.length-1 ]);

		Attributes attrs = HtmlTable.doHtml( new HtmlStream( fname ));
		
		if (cmd.equals( "list" )) {
			String option = args.remove( 0 ); // "values" or "header"

			if (option.equals( "values" )) {
				
				Strings names = attrs.names();
				ListIterator<String> ni = names.listIterator();
				while (ni.hasNext())
					if (ni.next().equals( "header" ))
						ni.remove();
				
				rc = new Strings( ""+ names.toString( Strings.DQCSV ));
				
			} else if (option.equals( "header" )) {
				
				rc = new Strings();
				boolean printMe = false;
				option = args.remove( 0 );
				
				// extract attribute names following the appropriate header
				ListIterator<Attribute> ni = attrs.listIterator();
				while (ni.hasNext()) {
					Attribute a = ni.next(); 
					if (a.name().equals( "header" ))
						printMe = a.value().equalsIgnoreCase( option );
					else if (printMe) {
						rc.append( a.name() +" "+ option );
						//set up a new interpration
						insertSign( source, a, option );
				}	}
				if (rc.isEmpty())
					rc = new Strings( "sorry, there "+ option +" is not a header" );
				else {
					rc = new Strings( rc.toString( "", " or ", "" ));
					// Let Autoload know we've loaded signs to remove...
					Autoload.put( INFO_BOX_CONCEPT );
				}
			}
			
		} else if (cmd.equals( "retrieve" )) {

			if (args.size() != 1)
				rc = new Strings( "sorry, usage: [count|list|retrieve] <desc> \"filename\" "+ args );
			
			// [html retrieve] largest_city_date ["./wiki/Queen_Elizabeth.wikipedia"]
			// attrs => [name="value", name1="value1", ... ]
			
			Strings names = new Strings( args.get( 0 ));
			String  type  = names.get( names.size()-1 ); // [value|date|name|address]
			if (type.equals( "date") ||
				type.equals("place") ||
				type.equals( "name") ||
				type.equals("value")   )
				// args is not the attribute name pattern
				type = names.remove( names.size()-1 );
			else
				type = "name";
			
			// Obtain the named value - split into sub-values
			String     value = attrs.match( names );
			if (value.equals(""))
				rc = new Strings("sorry, "+ fname +" does not have a "+ names +" value" );
			
			else {
				//Audit.log( "Value is >"+ value +"<" );
				Strings  subvals = new Strings( value.split( ";" ));
				
				rc = new Strings( "sorry, I don't know" );
				if (type.equals( "date" ))
					rc = new Strings( Date.getDate( subvals, "sorry, I don't know" ));
					
				else if (type.equals( "place" ))
					rc = new Strings( Address.getAddress( subvals, "sorry, I don't know"	));
					
				else if (type.equals( "name" )) {
					for (String s : subvals)
						if (!Date.isDate( s ) &&
						    !Address.isAddress( s ))
						{
							rc = new Strings( s );
							break;
						}
				} else if (type.equals( "value" ))
					rc = new Strings( value );
		}	}
		
		audit.out( rc );
		return rc;
}	}
