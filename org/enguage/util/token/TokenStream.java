package org.enguage.util.token;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.enguage.util.attr.Attribute;
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
	public boolean skipToken( int offset ) {
		while (offset-->0 && hasNext())
			getNext();
		return hasNext();
	}
	public void close() {
		try {is.close();} catch (IOException ignore) {};
	}
	// ************************************************************************
	
	private String type = "unknown";
	public  String type() {return type;}
	public  void   type( String t ) {type = t;}
	
//	private int size = 0;
//	public  int size() {return size;}
	
	private int offset = 0;
	public  int offset() {return offset;}
	
	private int  readAhead = 0;
	private void readAhead(int n) {
//		audit.in( "readAhead", "n="+n +" <<<< gotch--" );
		readAhead = n==160?32:n;
		gotch--;
//		audit.out();
	}
	
	private int getCh( InputStream is ) throws IOException {
		
		int ch = is.read();
//		audit.in("getCh", ch==-1?"-1":" >>>> gotch++" );
		if (ch != -1) gotch++;
		
//		if ((ch>='a' && ch<='z') || (ch>='A' && ch<='Z'))
//			audit.out( (ch) + " >" + ((char)ch) +"<" );
//		else
//			audit.out( ch );
		
		return ch; 
	}
	
	private int  gotch = 0;
	public  int  gotch(){return gotch;}
	
	private int  getChar() throws IOException {
		int ch;
		// &nbsp; == Non-breaking spaces... Â or &#160; ...
		if (readAhead == 0) {
			ch = getCh( is );
			if (ch == 195) { // ... check 195-130 sequence... found in desktop page
				ch = getCh( is );
				if (ch == 130) {
					ch = ' ';
				} else {
					readAhead( ch );
					ch = 195;
				}
			} else if (ch == 194) { // ...check 194-160 sequence... found on mobile page
				ch = getCh( is );
				if (ch == 160) {
					ch = ' ';
				} else {
					readAhead( ch );
					ch = 194;
				}
			}
		} else { // check "&#160;" sequence...
			ch = readAhead; gotch++;
			if (ch == '&') {
				ch = getCh( is );// read another
				if (ch == '#') {
					while (ch != ';')
						ch = getCh( is );
					
					readAhead = 0; // remove '&'
					ch = ' '; // replace &#nnn; with space
				} else {
					readAhead( ch );
					ch = '&';
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
		
		gotch = 0; // manipulated within getChar()
		
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
			if (Character.isLetter( ch )) {
				while (-1 != (ch = getChar())) 
					if (Character.isLetter( ch ) ||
						Character.isDigit(  ch ) || // alphanums
						ch == '\'')                 // embedded apostrophe's
						strbuf.append( (char)ch );
					else {
						readAhead( ch );
						break;
					}
				
			} else if (Character.isDigit( ch )) {
				while (-1 != (ch = getChar())) 
					if (Character.isDigit( ch ))
						strbuf.append( (char)ch );
					else {
						readAhead( ch );
						break;
					}
			
			} else if ('"' == ch) {
				while (-1 != (ch = getChar())) 
					if ('"' != ch ) {
						if ('\\' == ch ) ch = getChar();
						strbuf.append( (char)ch );
					} else {
						strbuf.append( (char)ch );
						{	/* a bug in wikipedia places a double quote at
							 * the end of the mobile 'search' input widget.
							 * e.g.:  <input .... name="value"">
							 */
							ch = getChar();
							if ('"' != ch )
								readAhead( ch );
						}
						break;
					}
			
			} else if ('\'' == ch) {
				while (-1 != (ch = getChar())) 
					if ('\'' != ch ) {
						if ('\\' == ch ) ch = getChar();
						strbuf.append( (char)ch );
					} else {
						strbuf.append( (char)ch );
						break;
					}
			}
			
		} catch(IOException ignore) {
			audit.error( "doPrefix(): error reading chars" );
		}
		String str = strbuf.toString();
		col += str.length();
		return new Token( wspbuf.toString(), str, row, col, gotch );
	}
	
	// *** Location
	private int  row = 1;
	private int  col = 1;
	public  int  row() {return row;}
	public  int  col(int strlen) {return col - strlen;}

	// ************************************************************************
	// a 'modern' interface...
	//
	private Token   nn   = null;
	private Token   next = null;
	private Token   prev = null;
	private Token   pp   = null;
	
	public  boolean hasNext() { // sets up 'next' and quantifies it
		if (next == null) next = getToken();
		return !next.isEmpty();
	}
	public  Token   getNext() {
		Token rc = next == null ? getToken() // just take one blind
				                  : next;    // if not been set up
		
		// remember where we are for flagging errors
		row += rc.row();
		col = (rc.row() == 0 ? col : 1) + rc.col();
		
		// shift buffers forwards
		pp   = prev;
		prev = rc;
		next = nn;
		nn   = null;
		
		offset++;
		return rc;
	}
	public  void putBack() {
		if (nn == null) { // buffers not full !!
			// shift buffers forwards
			nn   = next;
			next = prev;
			prev = pp;
			pp = null;
		
			offset--;
	}	}
	
	// ************************************************************************
	// Helper functions
	
	public String getString() {return getNext().string();}
	
	public  boolean expectLiteral( String expected ) {
		//audit.in( "expectLiteral", "expected="+ expected );
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
		//audit.out( rc );
		return rc; 
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
	
	public Attribute getAttribute() {
		Attribute attribute = new Attribute( "", "" );
		String  name = getString();
		if (name.matches( "[a-zA-Z_]+")) {
			if (getString().equals("=")) { // '='
				String value = Strings.trim( Strings.trim( getString(), '"' ), '\'' );
				attribute.name( name ). value( value ); 
			}
		} else
			putBack();
		return attribute;
	}

	// ************************************************************************
	// Test code
	//
	public static void main( String[] args ) {
		Audit.resume();
		//Audit.on();
		String[] testStrings =
			{	// non-breaking space is 1 char
//				"<td> Martin&#160;Wheatman</td>",
				
				// non-breaking space, here, is 2 chars
				"mÂW",
				
//				// there's an Â between 'aged' and '95' (10 chs)
				"(aged 95)",
//				
//				// here there isn't (9 chars)
				"(aged 95)",
//				
				"m°N"
			};
		
		audit.tracing( true );
		audit.debugging( true );
		
		for (String s : new Strings( testStrings )) {
			int size = 0;
			Audit.subtl( s +" (strlen=" +s.length() +")" );
			try (TokenStream ts = new TokenStream(s.getBytes( "UTF-8" ))) {
				size = 0;
				while (ts.hasNext()) {
					Token t = ts.getNext();
					if (t.isEmpty())
						Audit.log( "end of tx" );
					else
						size += ts.offset();
				}
				
				Audit.log( s +": counted = "+ size );
				
			} catch (Exception ex) {}
			
			try (TokenStream ts = new TokenStream( s.getBytes( "UTF-8" ))) {
				ts.skipToken( size - 1 );
				int ch = ts.getChar();
				Audit.log( "last char = "+ ch + " >" + ((char)ch) +"< (skipped: "+ (size-1) +")");
			} catch (Exception ex) {}
		}
}	}
