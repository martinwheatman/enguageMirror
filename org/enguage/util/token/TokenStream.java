package org.enguage.util.token;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.enguage.util.audit.Audit;

public class TokenStream implements AutoCloseable {

	private static final Audit  audit = new Audit( "TokenStream" );

	private final InputStream is;
	
	public TokenStream( File f ) throws FileNotFoundException {
		is = new FileInputStream( f );
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
		
		if (readAhead == 0) return is.read();
		
		int ch = readAhead;
		if (ch != (int)'&') {
			readAhead = 0;
			return ch;
		} else {
			ch = is.read();
			if (ch != '#') {
				readAhead( ch );
				return (int)' '; // don't return '&'
			} else {
				while (ch != ';')
					ch = is.read();
				return ' ';
			}
		}
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
		try (TokenStream ms = new TokenStream( new File( "queen" ))) {
			
			while (ms.hasNext() && i++ < 12)
				Audit.log("===>"+ ms.getNext());

		} catch (Exception ex) {}
}	}
