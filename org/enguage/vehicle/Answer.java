package org.enguage.vehicle;

import org.enguage.util.Strings;

import org.enguage.vehicle.Reply;

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
	
	private Strings answers = new Strings();
	
	public  Strings valueOf() {return answers;}
	public  void    add( String s ) { answers.add( setType( s ));}
	public  boolean none() { return answers.size() == 0; }

	private int    type = Reply.NK;
	public  int    type() { return type; }
	private String setType( String s ) {
		// This sets type to first non-NK type
		if (type == Reply.NK) {
			     if (s.equalsIgnoreCase( Reply.yes()    )) type = Reply.YES;
			else if (s.equalsIgnoreCase( Reply.success())) type = Reply.YES;
			else if (s.equalsIgnoreCase( Reply.no()     )) type = Reply.NO;
			else if (s.equalsIgnoreCase( Reply.dnu()    )) type = Reply.DNU;
			else if (s.equalsIgnoreCase( Reply.failure())) type = Reply.FAIL;
			else if (s.equalsIgnoreCase( Reply.ik()     )) type = Reply.IK;
			else if (s.equalsIgnoreCase( Reply.dnk()    )) type = Reply.NK;
			else type = Reply.CHS;
		}	
		return s;
	}

	@Override
	public  String  toString() { return answers.toString( Reply.andListFormat() ); }
}
