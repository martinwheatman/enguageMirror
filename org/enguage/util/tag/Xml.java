package org.enguage.util.tag;

import java.io.IOException;

import org.enguage.util.Audit;

public class Xml {
//	private static final Audit  audit = new Audit( "Xml" );

	private        final String     prefix;
	private        final String     name;
	private        final String     type;
//	private        final Attributes attrs;
//	
	private String doPrefix( TokenStream ts ) {
		StringBuilder prefixBuff = new StringBuilder();
		
		Token token = null;
		while (ts.hasNext()) {
			token = ts.getNext();
			if (token.string().equals( "<" )) {
				ts.putNext( token );
				break;
			} else
				prefixBuff.append( token.toString());
		}
		// add "<"'s whitespace to prefix!
		if (token != null)
			prefixBuff.append( token.space() );
		
		return prefixBuff.toString();
	}
	
	private String doName( TokenStream ts ) {
		if (ts.hasNext())
			return ts.getNext().string();
		return "";
	}
	
	public Xml( String fname ) {
		String pref  = "";
		String nam   = "";
		String dctyp = "XML";
		try (TokenStream ts = new TokenStream( fname )) {
			pref = doPrefix( ts );
			ts.getNext(); // '<'
			nam  = doName( ts );
			if (nam.equals( "!" )) {
				ts.getNext(); // DOCTYPE
				dctyp = ts.getNext().string();
				ts.getNext(); // '>'
			} else {
				Token token = ts.getNext();
				while (token.string().equals(">"))
					if (token.string().equals("/"))
						dctyp="standalone";
			}
					
		} catch (IOException iox) { // ignore
			Audit.log( "Error file not found "+ fname );
		} finally {
			prefix = pref;
			name = nam;
			type = dctyp;
	}	}
	
	public String toString() {
		return prefix
				+ (name.equals( "" ) ? "" :
					name.equals( "!" ) ? "<!DOCTYPE "+ type +">":
						"<"+ name +">"); 
//					+" "+ attrs.toString(); //+  // attrs doesn't have preceding space
//			(content().isEmpty() ?
//					"/>" : ( ">"+ content.toString() + "</"+ name +">" )));
	}
	public static void main( String[] args ) {
		String fname = "queen";
		Xml xml = new Xml( fname );
		
		Audit.log( "Xml is '"+ xml.toString() +"'" );
	}
}
