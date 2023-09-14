package org.enguage.util.http;

import java.util.ListIterator;

import org.enguage.sign.symbol.when.Date;
import org.enguage.sign.symbol.where.Address;
import org.enguage.util.attr.Attributes;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class InfoBox {
	public  static final int      ID = 154478; // "html"
	private static final String NAME = "Infobox";
	private static final Audit audit = new Audit( NAME );

	private static String source = "";
	public  static String source() {return source;}
	public  static void   source( String s ) {source = s;}

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

		if (args.size() != 1) {
			rc = new Strings( "sorry, usage: [count|list|retrieve] <desc> \"filename\"" );
			
		} else if (cmd.equals( "count" )) {
			// table count "xyz"
			rc = new Strings(
					args.get( 0 ).equals( "tables" ) ?
						 ""+ HtmlTable.tableCount
						 : "sorry, usage: count tables \"filename\""
				 );
			
		} else if (cmd.equals( "list" )) {
			cmd = args.remove( 0 ); // "values"

			// print out that data
			Strings names = attrs.names();
			
			// remove headers
			ListIterator<String> ni = names.listIterator();
			while (ni.hasNext())
				if (ni.next().equals( "header" ))
					ni.remove();
			
			rc = new Strings( cmd.equals( "values" ) ?
						""+ names.toString( Strings.DQCSV )
						: "usage: list values \"filename\""
				);
			
		} else if (cmd.equals( "retrieve" )) {
			
			Strings names = new Strings( args.get( 0 ));
			
			String type = names.get( names.size()-1 ); // [value|date|name|address]
			if (type.equals( "date") ||
				type.equals("place") ||
				type.equals( "name") ||
				type.equals("value")   )
			{	// args is not the attribute name pattern
				type = names.remove( names.size()-1 );
			}
			
			// Obtain the named value - split into sub-values
			String     value = attrs.match( names );
			Strings  subvals = new Strings( value.split( ";" ));
			
			rc = new Strings( "sorry, I don't know" );
			if (type.equals( "date" )) {
				rc = new Strings( Date.getDate( subvals, "sorry, I don't know" ));
				
			} else if (type.equals( "place" )) {
				rc = new Strings( Address.getAddress( subvals, "sorry, I don't know"	));
				
			} else if (type.equals( "name" )) {
				for (String s : subvals)
					if (!Date.isDate( s ) &&
					    !Address.isAddress( s ))
					{
						rc = new Strings( s );
						break;
					}
			} else if (type.equals( "place" ))
				rc = new Strings( value );
		}
		
		audit.out( rc );
		return rc;
}	}
