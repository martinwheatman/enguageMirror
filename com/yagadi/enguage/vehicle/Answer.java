package com.yagadi.enguage.vehicle;

import com.yagadi.enguage.util.Strings;

public class Answer {
	
	static private final String defaultPlaceholder = "whatever";

	public Answer() {}
	
	/** Answer - 
	 * e.g. [ "a cup of coffee", "a packet of biscuits" ]
	 */
	static private Strings placeholderAsStrings = new Strings( defaultPlaceholder );
	static public  Strings placeholderAsStrings() { return placeholderAsStrings; }
	static private String placeholder = defaultPlaceholder;
	static public  String placeholder() { return placeholder; }
	static public  void   placeholder( String ph ) {
		placeholderAsStrings = new Strings( placeholder = ph );
	}

	private boolean appending = false;
	public  boolean isAppending() { return appending; }
	public  void    appendingIs( boolean b ) { appending = b; }
	
	private Strings answer = new Strings();
	
	public  Strings valueOf() {return answer;}
	public  void    add( String s ) { answer.add( s );}
	public  boolean none() { return answer.size() == 0; }

	public int type( int type ) {
		/*
		 * nominally, answer.type is modified during interpretation,
		 * format.type is set on reply.
		 */
		if (type == Reply.UDU) return Reply.UDU;
		
		String s = valueOf().toString();
		     if (s.equalsIgnoreCase( Reply.yes()    )) return Reply.YES;
		else if (s.equalsIgnoreCase( Reply.success())) return Reply.YES;
		else if (s.equalsIgnoreCase( Reply.no()     )) return Reply.NO;
		else if (s.equalsIgnoreCase( Reply.failure())) return Reply.NO;
		else if (s.equalsIgnoreCase( Reply.ik()     )) return Reply.IK;
		else if (s.equalsIgnoreCase( Reply.dnk()    )) return Reply.NK;
		else if (s.equalsIgnoreCase( Reply.dnu()    )) return Reply.DNU;
		else return Reply.CHS;
	}

	@Override
	public  String  toString() { return answer.toString( Reply.andListFormat() ); }
}
