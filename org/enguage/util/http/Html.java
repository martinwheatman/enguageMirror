package org.enguage.util.http;

import org.enguage.sign.symbol.when.Date;
import org.enguage.sign.symbol.where.Address;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;
import org.enguage.util.sys.Fs;
import org.enguage.util.tag.Tag;
import org.enguage.util.tag.TagStream;
import org.enguage.util.token.TokenStream;

public class Html {
	public  static final int      ID = 154478; // "html"
	private static final String NAME = "Html";
	private static final Audit audit = new Audit( NAME );

	public static Strings interpret( Strings args ) {
		audit.in( "interpret", "args="+ args );
		Strings rc = new Strings( "sorry, i don't understand" ); // Shell.Fail;
		String cmd = args.remove( 0 );

		if (cmd.equals( "find" )) {
			rc = new Strings( "sorry, not found" );
			String name = args.remove( 0 );
			String tag  = args.remove( 0 );
			String value = args.remove( 0 );
			
			try (TokenStream ts =
					new TokenStream( Strings.trim( name, '"' )) )
			{
				int offset = -1;
				while (ts.hasNext()) {
					if (ts.getString().equalsIgnoreCase( "<" ) && ts.hasNext() &&
						ts.getString().equalsIgnoreCase( tag ) && ts.hasNext()   ) {
						
						offset = ts.offset() - 2; // minus: "<" & "tag" 

						while (!ts.getString().equalsIgnoreCase( ">" ) && ts.hasNext());
						
						if (ts.getString().equalsIgnoreCase( value )) {
							rc = new Strings( ""+offset );
							break;
				}	}	}
				
			} catch (Exception ex) {}
			
		} else if (cmd.equals( "retrieve" )) {
			
			rc = new Strings( "sorry, th not found" );
			String name = args.remove( 0 );
			String offset = args.remove( 0 );
			String type = args.remove( 0 ); // ["date"|"name"|"place"|"value"]
			
			try (TokenStream ts = new TokenStream( Strings.trim( name, '"' )) ) {
				
				ts.skipToken( Integer.valueOf( offset ));
				//read a few tags here ...
				TagStream tags = new TagStream( ts );
				Tag cell = tags.getNext();
				cell = tags.getNext();
				if (cell.name().equals( "td" )) {
					
					rc = new Strings( "sorry, "+type+" not found" );
					
					Strings values = cell.children().toStrings( "br" );
					
					if (type.equals( "date" )) {
						rc = new Strings( Date.getDate( values, "Sorry, I don't know" ));
						
					} else if (type.equals( "place" )) {
						rc = new Strings( Address.getAddress( values, "Sorry, I don't know"	));
						
					} else if (type.equals( "name" )) {
						rc = new Strings( "Sorry, I don't know" );
						for (String s : values)
							if (!Date.isDate( s ) &&
							    !Address.isAddress( s )) {
								rc = new Strings( s );
								break;
							}
				}	}
			} catch (Exception ex) {
				Audit.log( "==>"+ ex );
		}	}
		
		audit.out( rc );
		return rc;
	}
}
