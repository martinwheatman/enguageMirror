package org.enguage.signs.vehicle.reply;

import org.enguage.util.Strings;

public class Answer {
	
	static private final String defaultPlaceholder = "whatever";

	public Answer() {}
	
	/** Answer - 
	 * e.g. [ "a cup of coffee", "a packet of biscuits" ]
	 */
	static private Strings placeholderAsStrings = new Strings( defaultPlaceholder );
	static public  Strings placeholderAsStrings() { return placeholderAsStrings; }
	static private String  placeholder = defaultPlaceholder;
	static public  String  placeholder() { return placeholder; }
	static public  void    placeholder( String ph ) {placeholderAsStrings = new Strings( placeholder = ph );}

	private boolean appending = false;
	public  boolean isAppending() { return appending; }
	public  void    appendingIs( boolean b ) { appending = b; }
	
	// [ "black coffee", "eggs", "Taj Mahal" ]; w/embedded spaces!
	private Strings answers = new Strings();
	
	public  Strings valueOf() {return answers;}
	public  void    add( String s ) { answers.add( setType( s ));}
	public  boolean none() { return answers.isEmpty(); }
	
	public Strings injectAnswer( Strings sa ) {
		if (sa.isEmpty()) {
			sa = new Strings( answers ); // use the raw answer
			if (sa.isEmpty()) // so a was equal to ""
				sa = Response.dnu();
		} else if (sa.contains( Strings.ELLIPSIS )) // if required put in answer (verbatim!)
			sa.replace( Strings.ellipsis, answers );
		else if (sa.contains( Answer.placeholder() ))
			sa.replace( Answer.placeholderAsStrings(), answers );
		return sa;
	}
	
	private int    type = Response.DNK;
	public  int    type() { return type; }
	private String setType( String s ) {
		// This sets type to first non-NK type
		if (type == Response.DNK) {
			     if (s.equalsIgnoreCase( Response.yesStr()    )) type = Response.OK;
			else if (s.equalsIgnoreCase( Response.successStr())) type = Response.OK;
			else if (s.equalsIgnoreCase( Response.noStr()     )) type = Response.NO;
			else if (s.equalsIgnoreCase( Response.dnuStr()    )) type = Response.DNU;
			else if (s.equalsIgnoreCase( Response.failureStr())) type = Response.FAIL;
			else if (s.equalsIgnoreCase( Response.dnkStr()    )) type = Response.DNK;
			else type = Response.CHS;
		}	
		return s;
	}

	@Override
	public  String toString() { return answers.toString( Reply.andListFormat() ); }
}
