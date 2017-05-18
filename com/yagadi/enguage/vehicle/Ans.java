package com.yagadi.enguage.vehicle;

import com.yagadi.enguage.util.Strings;

public class Ans {
	
	static private final String defaultPlaceholder = "whatever";

	public Ans() {}
	
	/** Answer - 
	 * e.g. [ "a cup of coffee", "a packet of biscuits" ]
	 */
	static private String placeholder = defaultPlaceholder;
	static public  String placeholder() { return placeholder; }
	static public  void   placeholder( String ph ) {
		placeholderAsStrings = new Strings( placeholder = ph );
	}

	static private Strings placeholderAsStrings = new Strings( defaultPlaceholder );
	static public  Strings placeholderAsStrings() { return placeholderAsStrings; }

	private boolean appending = false;
	public  boolean isAppending() { return appending; }
	public  void    appendingIs( boolean b ) { appending = b; }
	
	private Strings nswer = new Strings();
	
	public  Strings valueOf() {return nswer;}
	public  void    add( String s ) { nswer.add( s );}
	public  boolean none() { return nswer.size() == 0; }

	@Override
	public  String  toString() { return nswer.toString( Reply.andListFormat() ); }
}
