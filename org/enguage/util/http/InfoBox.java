package org.enguage.util.http;

import java.util.ListIterator;

import org.enguage.repertoires.Repertoires;
import org.enguage.repertoires.concepts.Autoload;
import org.enguage.sign.Sign;
import org.enguage.sign.symbol.reply.Reply;
import org.enguage.sign.symbol.when.Date;
import org.enguage.sign.symbol.where.Address;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Attributes;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class InfoBox {
	public  static final int      ID = 113445711;
	private static final String NAME = "Infobox";
	private static final Audit audit = new Audit( NAME );

	private static String INFO_BOX_CONCEPT = "InfoBoxGen";
	
	private static String wsrc = "";
	public  static String wikiSource() {return wsrc;}
	public  static void   wikiSource( String s ) {wsrc = s;}
	
	private static String decodeSource( String fname ) {
		String[] source = fname.split( "\\." );
		return source[ source.length-1 ];
	}

	private static String topic = "";
	public  static String topic() {return topic;}
	public  static void   topic( String t ) {topic = t;}

	private static String decodeTopic( String fname ) {
		String[] source = fname.split( "\\." );
		String[]   path = source[ source.length - 2 ].split( "/" );
		String    topic = path[ path.length - 1 ].replace( "_", " " );
		return topic;
	}
	private static Attributes listHeaderAttributes( String name, Attributes attrs ) {
		Attributes rc = new Attributes();
		boolean printMe = false;
		// extract attributes for the appropriate header
		ListIterator<Attribute> ni = attrs.listIterator();
		while (ni.hasNext()) {
			Attribute a = ni.next(); 
			if (a.name().equals( "header" ))
				printMe = a.value().equalsIgnoreCase( name );
			else if (printMe)
				rc.add( a );
		}
		return rc;
	}
	private static Strings retrieveValue( Strings name, Attributes attrs ) {
		Strings rc = null;
		// [html retrieve] largest_city_date ["./wiki/Queen_Elizabeth.wikipedia"]
		// attrs => [name="value", name1="value1", ... ]
		
		String  type = name.get( name.size()-1 ); // [value|date|name|address]
		if (type.equals( "date") ||
			type.equals("place") ||
			type.equals( "name") ||
			type.equals("value")   )
			// args is not the attribute name pattern
			type = name.remove( name.size()-1 );
		else
			type = "name";
		
		// Obtain the named value - split into sub-values
		String value = attrs.match( name );
		if (value.equals(""))
			rc = new Strings("sorry, "+ topic() +" does not have a '"+ name +"' attribute" );
		
		else {
			//Audit.log( "Value is >"+ value +"<" );
			Strings  subvals = new Strings( value.split( ";" ));
			
			rc = new Strings( "sorry, I don't know" );
			if (type.equals( "date" ))
				rc = new Strings( Date.getDate( subvals, "sorry, I can't find a date" ));
				
			else if (type.equals( "place" ))
				rc = new Strings( Address.getAddress( subvals, "sorry, I can't a place" ));
				
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
			rc.stripBrackets( "[", "]" ); // e.g. ref: [1] - on mobile page
		}
		return rc;
	}
	public  static Strings interpret( Strings args ) {
		audit.in( "interpret", "args="+ args.toString( Strings.DQCSV ));
		Strings rc = new Strings( "sorry, i don't understand" ); // Shell.Fail;
		String cmd = args.remove( 0 );

		// get the filename- last parameter - and set the source && topic
		String fname = Strings.trim( args.remove( args.size()-1 ), '"' );
		// source is now last portion of cached name
		wikiSource( decodeSource( fname )); // e.g. wikipedia
		topic( decodeTopic( fname ));

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
				String header = args.remove( 0 );
				Attributes headerAttrs = listHeaderAttributes( header, attrs );
				
				if (headerAttrs.isEmpty())
					rc = new Strings( "sorry, the "+ header +" is not a header" );
				else {
					ListIterator<Attribute> ai = headerAttrs.listIterator();
					while (ai.hasNext()) {
						Attribute adjective = ai.next();
						rc.append( adjective.name() +" "+ header );
						Repertoires.signs().insert(
								new Sign.Builder( 
											"On \""
											+ adjective.name() 
											+ " "
											+ header // NAME
											+ "\", according to wikipedia what is the "
											+ adjective.name() 
											+ " of "
											+ topic()
									).toSign().concept( INFO_BOX_CONCEPT )
						);
					}
					rc = new Strings( rc.toString( "", " or ", "" ));
					// Let Autoload know we've signs to remove...
					Autoload.put( INFO_BOX_CONCEPT );
			}	}
			
			
		} else if (cmd.equals( "retrieve" )) {

			// [html retrieve] largest_city_date ["./wiki/Queen_Elizabeth.wikipedia"]
			// attrs => [name="value", name1="value1", ... ]
			Strings name = new Strings( args.get( 0 ));
			rc = retrieveValue( name, attrs );
			
			
		} else if (cmd.equals( "exists" )) {
			
			Audit.log( "args: ["+ args.toString( Strings.DQCSV ) +"]" );
			if (args.size() != 2 ) 
				rc = new Strings("Sorry, exists method needs two parameters, got: "+ args );
			else {
				Strings name  = new Strings( args.get( 0 ));
				Strings value = new Strings( args.get( 1 ));
				
				rc = retrieveValue( name, attrs );
				
				rc.prepend( "is" );
				rc.prepend( "value" );
				for (String s : name )
					rc.add( 0, s );
				rc.prepend( "the" );
				for (String s : Reply.attributing())
					rc.add( 0, s.equals( "X" ) ? InfoBox.wikiSource() : s );

				rc.prepend( "," );
				if (rc.equals( value ))
					rc.prepend( "Yes" );
				else
					rc.prepend( "Sorry" ); // No isn't negative enough?
			}
			
		} else if (cmd.equals( "header" )) {

			rc = new Strings();
			String param = args.remove( 0 ); // "value"
			param = args.remove( 0 );        // ATTR
			
			Attributes headerAttrs = listHeaderAttributes( param, attrs );
			if (headerAttrs.size() == 1)
				rc = new Strings(
						" the "
						+ headerAttrs.get( 0 ).name() 
						+ " "
						+ param 
						+ " of "
						+ topic() 
						+ " is "
						+ headerAttrs.get( 0 ).value()
				);
			else
				rc = new Strings( "sorry, there is more than one header value" );
		}
		
		audit.out( rc );
		return rc;
}	}
