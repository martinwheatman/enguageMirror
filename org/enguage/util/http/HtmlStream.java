package org.enguage.util.http;

import org.enguage.util.attr.Attribute;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;
import org.enguage.util.sys.Fs;
import org.enguage.util.token.Token;
import org.enguage.util.token.TokenStream;

public class HtmlStream {
	public  static final int      ID = 154478; // "html"

	private final TokenStream ts;

	public HtmlStream( String fname ) {ts = new TokenStream( fname );}
	
	// ========================================================================
	private Attribute getAttribute( TokenStream ts ) {
		String name = ts.getString(), value = "";
		if (name.matches( "[a-zA-Z_]+")) {
			if (ts.getString().equals("=")) { // '='
				value = ts.getString();
				value = Strings.trim( value, '\'' );
				value =	Strings.trim( value, '"' );
			} else {
				value = name; // name => name'name'  
			}
		} else { // '/' or '>'
			ts.putBack();
			name = "";
		}
		return new Attribute( name, value );
	}

	public Html getHtml() {
		Html html = new Html();
		while (ts.hasNext()) {
			if (ts.getString().equalsIgnoreCase(  "<" )) {
				html.name( ts.getString() );
				if (html.name().equals("/")) {
					html.end( true );
					html.name( ts.getString() );
					ts.expectLiteral( ">" );
				} else {
					Attribute a = getAttribute( ts );
					while (!a.name().equals("")) { // isEmpty() !
						html.add( a );
						a = getAttribute( ts );
					}
					String word = ts.getString();
					if (word.equals( "/" )) {
						html.standAlone( true );
						ts.getString();
					}
				}
				break;
		}	}
		return html;
	}
	public Strings getText() {
		Strings text = new Strings();
		while (ts.hasNext()) {
			Token token = ts.getNext();
			if (token.string().equals("<")) {
				ts.putBack();
				break;
			}
			text.add( token.string() );
		}
		return text;
	}
	public static void main( String [] args) {
		String fname = "table.html";
		Audit.on();
		String table = "junk in here<table class='infobox'><tbody>"
				+ "<tr><th colspan='2'>Elizabeth ii</th></tr>"
				+ "<tr><th>Born</th><td>Princess Elizabeth of York<br/>21 April, 1926<br/>Mayfair, London</td>"
				+ "</tbody></table>";
		Fs.stringToFile( fname, table );
		
		HtmlStream hs = new HtmlStream( fname );
		
		Audit.log( "table="+ hs.getHtml());
		Audit.log( "tbody="+ hs.getHtml());
		Audit.log( "tbrow="+ hs.getHtml());
		Audit.log( "tbhdr="+ hs.getHtml());
		
		Strings name = hs.getText();
		Audit.log( "\nHeader ===> "+ name +"\n" );
		
		Audit.log( "tehdr="+ hs.getHtml());
		Audit.log( "terow="+ hs.getHtml());
		Audit.log( "" );
		Audit.log( "tbrow="+ hs.getHtml());
		Audit.log( "tbhdr="+ hs.getHtml());
		
		name = hs.getText();
		Audit.log( "\nName ===> "+ name +"\n" );
		
		Audit.log( "tehdr="+    hs.getHtml());
		Audit.log( "tbval="+    hs.getHtml());
		
		name = hs.getText();
		Audit.log( "\nDate  ===> "+ name +"\n" );
		
		Audit.log( "br="+    hs.getHtml());

		name = hs.getText();
		Audit.log( "\nPlace ===> "+ name +"\n" );
		
		Audit.log( "br="+    hs.getHtml());

		name = hs.getText();
		Audit.log( "\nValue ===> "+ name +"\n" );
		
		Audit.log( "tetd="+    hs.getHtml());
		Audit.log( "tebdy="+    hs.getHtml());
		Audit.log( "tetbl="+    hs.getHtml());
		Audit.log( "is empty="+ hs.getHtml().isEmpty());
}	}
