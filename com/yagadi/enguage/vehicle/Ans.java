package com.yagadi.enguage.vehicle;

import com.yagadi.enguage.util.Strings;

public class Ans {
	/** Answer - 
	 * e.g. [ "a cup of coffee", "a packet of biscuits" ]
	 */
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
