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
	private void readAhead(int n) {readAhead = n==160?32:n;}
//	private int  gotch = 0;
//	public  int  gotch(){return gotch;}
	
	private int  getChar() throws IOException {
		int ch;
		//gotch = 0;
		// Non-breaking spaces...
		if (readAhead == 0) { // check 195-130 sequence... desktop?
			ch = is.read(); //gotch++;
			if (ch == 195) {
				ch = is.read(); //gotch++;
				if (ch == 130) {
					ch = ' ';
				} else {
					readAhead( ch );
					ch = 195;
				}
			} else if (ch == 194) { // check 194-160 sequence... mobile?
				ch = is.read(); //gotch++;
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
				ch = is.read();  //gotch++;// read another
				if (ch == (int)'#') {
					while (ch != (int)';')
						ch = is.read(); //gotch++;
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
		int row = 0, col = 0;
		StringBuilder wspbuf = new StringBuilder();
		StringBuilder strbuf = new StringBuilder();
		
		try {
			int ch;
			
			//process whitespace
			while (-1 != (ch = getChar())) 
				if (Character.isWhitespace( ch )) {
					if (ch=='\n') {
						row++;
						col = 0;
					} else
						col++;
					wspbuf.append( (char)ch );
				} else {
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
		String str = strbuf.toString();
		col += str.length();
		return new Token( wspbuf.toString(), str, row, col );
	}
	
	// *** Location
	private int  row = 1;
	private int  col = 1;
	public  int  row() {return row;}
	public  int  col(int strlen) {return col - strlen;}

	private int  prevRow = 1;
	private int  prevCol = 1;
	
	// ************************************************************************
	// a 'modern' interface...
	//
	private Token   next = null;
	private Token   prev = null;
	public  boolean hasNext() { // sets up 'next' and quantifies it
		if (next == null) next = getToken();
		return !next.isEmpty();
	}
	public String getString() {return getNext().string();}
	public  Token   getNext() {
		audit.in( "getNext", "" );
		Token rc = next == null ? getToken() // just take one blind
				                  : next;    // if not been set up
		// locate
		prevCol = col;
		prevRow = row;
	
		row += rc.row();
		col = (rc.row() == 0 ? col : 1) + rc.col();
		audit.debug("row is: "+ row +", col="+ col );
		
		next = null;
		prev = rc;
		audit.out( rc );
		return rc;
	}
	public  void putBack() {
		audit.debug("putback!");
		// relocate
		col = prevCol;
		row = prevRow;
		// retrieve previous
		next = prev;
		prev = null;
	}
	// ************************************************************************
	// Helper functions
	public  boolean expectLiteral( String expected ) {
		audit.in( "expectLiteral", "expected="+ expected );
		boolean rc = false;
		if (hasNext()) {
			Token t = getNext();
			rc = t.equals( expected );
			if (!rc)
				audit.error(
					"expecting '"+ expected
					+ "', got '"+ t.string() + "',"
					+ "' at line:"+ row()
					+ ", col:"+ col(t.string().length())
				);
		}
		return audit.out( rc );
	}
	public  boolean expectEither( String sep, String term ) {
		boolean rc = false;
		if (hasNext()) {
			Token t = getNext();
			rc = t.equals( sep );
			if (!rc)
				if (t.equals( term ))
					putBack();
				else
					audit.error(
						"got '"+ t.string() +
						"' was expecting '"+ sep +
						"' at line: "+ row() +
						", col="+ col(t.string().length())

					);
		}
		return rc;
	}
	public boolean parseLiteral( String target ) {
		boolean rc = false;
		if (hasNext()) {
			if (getNext().equals( target )) // we've finished
				rc = true;
			putBack();
		}
		return rc;
	}
	public boolean doLiteral( String target ) {
		if (parseLiteral( target )) {
			getNext();
			return true;
		}
		return false;
	}
	public boolean parseDqString() {
		boolean rc = 
			hasNext() && '"' == getNext().string().charAt( 0 );
		putBack();
		return rc;
	}
	public boolean expectDqString() {
		if (parseDqString()) return true;
		String got = getString();
		audit.error(
				"expecting '\"',"
				+ " got '"+ got
				+ "' at line: "+ row()
				+ ", col="+ col(got.length())
		);
		return false;
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
