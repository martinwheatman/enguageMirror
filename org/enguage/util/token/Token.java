package org.enguage.util.token;

public class Token {
	
	public  static final String byteEncoding = "UTF-8";
			
	private final String string;
	private final String  space;
	private final int       row; // number of '\n' in the token (whitespace!)
	private final int       col; // width of last line of token
	private final int      size; // number of chars read
	
	public Token( String wp, String chs, int r, int c, int gotch ) {
		space =  wp==null ? "" : wp;
		string= chs==null ? "" : chs;
		row = r; col = c;
		size = gotch;
	}
	
	public String string() {return string;}
	public String  space() {return  space;}
	public int       row() {return    row;}
	public int       col() {return    col;}
	public int      size() {return   size;}
	
	public boolean isEmpty() {
		return space.equals("") && string.equals("");
	}
	public boolean equals( String s ){
		return string.equals( s );
	}

	@Override
	public String toString() {return space + string;}
}
