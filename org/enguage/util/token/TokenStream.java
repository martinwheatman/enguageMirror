package org.enguage.util.token;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;
import org.enguage.util.sys.Fs;

public class TokenStream implements AutoCloseable {

	private static final Audit  audit = new Audit( "TokenStream" );

	private final InputStream is;
	
	public TokenStream( String fname ) {
		InputStream tmp = null;
		try {
			tmp = new ByteArrayInputStream(
					Fs.stringFromFile( fname ).getBytes( "UTF-8" ));
		} catch (UnsupportedEncodingException ignore) {}
		is = tmp;
	}
	public TokenStream( byte[] bs ) {
		is = new ByteArrayInputStream( bs );
	}
	public void close() {
		try {is.close();} catch (IOException ignore) {};
	}
	// ************************************************************************
	
	private String type = "unknown";
	public  String type() {return type;}
	public  void   type( String t ) {type = t;}
	
	private int  readAhead = 0;
	public  int  readAhead() {return readAhead;}
	public  void readAhead(int n) {readAhead = n==160?32:n;}
	
	public  int  getChar() throws IOException {
		int ch;
		// Non-breaking spaces...
		if (readAhead == 0) { // check 195-130 sequence... desktop?
			ch = is.read();
			if (ch == 195) {
				ch = is.read();
				if (ch == 130) {
					ch = ' ';
				} else {
					readAhead( ch );
					ch = 195;
				}
			} else if (ch == 194) { // check 194-160 sequence... mobile?
				ch = is.read();
				if (ch == 160) {
					ch = ' ';
				} else {
					readAhead( ch );
					ch = 194;
				}
			}
		} else { // check "&#160;" sequence...
			ch = readAhead;
			if (ch == (int)'&') {
				ch = is.read(); // read another
				if (ch == (int)'#') {
					while (ch != (int)';')
						ch = is.read();
					readAhead = 0; // remove '&'
					ch = (int)' '; // replace &#nnn; with space
				} else {
					readAhead( ch );
					ch = (int)'&';
				}
			} else
				readAhead = 0;
		}
		return ch;
	}
	
	private Token getToken() {
		StringBuilder wspbuf = new StringBuilder();
		StringBuilder strbuf = new StringBuilder();
		
		try {
			int ch;
			
			//process whitespace
			while (-1 != (ch = getChar())) 
				if (Character.isWhitespace( ch ))
					wspbuf.append( (char)ch );
				else {
					strbuf.append( (char)ch ); // 1st non-whitespace
					break;
				}
			
			// now process that non-whitespace char
			if (Character.isLetter( ch ))
				while (-1 != (ch = getChar())) 
					if (Character.isLetter( ch ) ||
						Character.isDigit(  ch ) || // alphanums
						ch == '\'')                 // embedded apostrophe's
						strbuf.append( (char)ch );
					else {
						readAhead( ch );
						break;
					}
				
			else if (Character.isDigit( ch ))
				while (-1 != (ch = getChar())) 
					if (Character.isDigit( ch ))
						strbuf.append( (char)ch );
					else {
						readAhead( ch );
						break;
					}
			
			else if ('"' == ch)
				while (-1 != (ch = getChar())) 
					if ('"' != ch ) {
						if ('\\' == ch ) ch = getChar();
						strbuf.append( (char)ch );
					} else {
						strbuf.append( (char)ch );
						{	/* a bug in wikipedia places a double quote at the end
							 * the mobile 'search' input widget.
							 */
							ch = getChar();
							if ('"' != ch )
								readAhead( ch );
						}
						break;
					}
			
			else if ('\'' == ch)
				while (-1 != (ch = getChar())) 
					if ('\'' != ch ) {
						if ('\\' == ch ) ch = getChar();
						strbuf.append( (char)ch );
					} else {
						strbuf.append( (char)ch );
						break;
					}
			
		} catch(IOException ignore) {
			audit.error( "doPrefix(): error reading chars" );
		}
		return new Token( wspbuf.toString(), strbuf.toString() );
	}
	
	// ************************************************************************
	// a 'modern' interface...
	//
	private Token   next = null;
	public  boolean hasNext() { // sets up 'next' and quantifies it
		if (next == null) next = getToken();
		return !next.isEmpty();
	}
	public  Token   getNext() {
		Token rc = next;
		if (next == null)    // if not been set up
			rc = getToken(); // just take one blind
		else
			next = null;     // no going back
		return rc; 
	}
	public  boolean putNext( Token t ) {
		if (next != null) return false; // don't overwrite next!
		next = t;
		return true;
	}
	
	// ************************************************************************
	// Test code
	//
	public static void main( String[] args ) {
		int i = 0;
		Audit.resume();
		String[] testStrings =
			{ "<td> Martin&#160;Wheatman</td>",
			  "<td> MartinÂWheatman</td>",
			  "</span> (aged 95)<br>" };
		
		for (String testStringa : new Strings( testStrings )) {
			try (TokenStream ms = new TokenStream(
						testStringa.getBytes( "UTF-8" )
				)	)
			{
				
				while (ms.hasNext() && i++ < 45)
					Audit.log("===>"+ ms.getNext());
	
			} catch (Exception ex) {}
}	}	}
