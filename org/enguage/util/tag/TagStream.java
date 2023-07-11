package org.enguage.util.tag;

import java.io.UnsupportedEncodingException;
import java.util.ListIterator;

import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;
import org.enguage.util.sys.Fs;
import org.enguage.util.token.TokenStream;

public class TagStream {
	//private static final Audit audit = new Audit( "TagStream" );
	
	private final TokenStream tokenStream;
	
	public TagStream( TokenStream ts ) {
		tokenStream = ts;
	}
	
	public boolean hasNext() {
		return tokenStream.hasNext();
	}
	
	public Tag getNext() {
		return new Tag( tokenStream );
	}
	
	private static void treePrint( Tag t ) {
		
		if ( !t.name().equals( "" )) {	
			boolean increment = false;
			
			if (!t.name().equals(  "script" ) &&
				!t.name().equals(     "img" ) &&
				!t.name().equals(  "button" ) &&
				!t.name().equals(    "span" ) &&
				!t.name().equals(     "div" ) &&
				!t.name().equals(       "a" ) &&
				!t.name().equals(      "ul" ) &&
				!t.name().equals(      "li" ) &&
				!t.name().equals(  "footer" ) &&
				!t.name().equals("noscript" ) &&
				!t.name().equals(   "style" ) &&
				!t.name().equals(   "input" ) &&
				!t.name().equals(     "nav" ) &&
				!t.name().equals(    "meta" ) &&
				!t.name().equals(    "link" ) &&
				!t.name().equals(   "label" ) &&
				!t.name().equals(     "sup" )	) 
			{
				Audit.log( t.name() +" : "+
						(t.name().equals("td") ?
								t.children().toStrings( "br" )
										.toString(Strings.DQCSV)
								: ""
						)	);
				Audit.incr();
				increment = true;
			}
			
			ListIterator<Tag> ch = t.children().listIterator();
			while (ch.hasNext()) 
				treePrint( ch.next());
			
			if (increment)
				Audit.decr();
	}	}
	
	// Test code
	public static void main( String[] args ) {
		Fs.root( "." );
		
		String testdata = 
				"<html><body></body></html>\n"
				+ "<html><body><table><tbody>\n"
				+ "<tr><th>Name</th><td>Martin&#160;Wheatman</td></tr>\n"
				+ "<tr><th>Born</th><td>17 July 1963<br/>Kendal, Westmorland</td></tr>\n"
				+ "</tbody></table></body></html>\n";
		
		if (args.length > 0) {
			Audit.log( "Once upon a time... "+ args[ 0 ]);
			testdata = Fs.stringFromFile( args[ 0 ]);
		}

		TokenStream tokens = null;
		try {
			tokens = new TokenStream( testdata.getBytes( "UTF-8" ) );
		} catch (UnsupportedEncodingException ignore) {}

		TagStream ts = new TagStream( tokens );
		while (ts.hasNext())
			treePrint( ts.getNext());
		
		Audit.log( "The End." );
	}
}
