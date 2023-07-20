package org.enguage.sign.symbol.reply;

import org.enguage.util.strings.Strings;

public class Answer {
	public Answer() {/*static class*/}
	
	public  static final String DEFAULT_PLACEHOLDER = "whatever";

	/** Answer - 
	 * e.g. [ "a cup of coffee", "a packet of biscuits" ]
	 */
	private static Strings placeholderAsStrings = new Strings( DEFAULT_PLACEHOLDER );
	public  static Strings placeholderAsStrings() {return placeholderAsStrings;}
	private static String  placeholder = DEFAULT_PLACEHOLDER;
	public  static String  placeholder() {return placeholder;}
	public  static void    placeholder( String ph ) {placeholderAsStrings = new Strings( placeholder = ph );}

	private boolean appending = false;
	public  boolean isAppending() { return appending; }
	public  void    appendingIs( boolean b ) { appending = b; }
	
	// [ "black coffee", "eggs", "Taj Mahal" ]; w/embedded spaces!
	private Strings answers = new Strings();
	
	public  void    add( String s ) { answers.add( s );}
	
	public Strings injectAnswer( Strings sa ) {
		if (sa.isEmpty()) {
			sa = new Strings( toString() ); // use the raw answer
			if (sa.isEmpty()) // so a was equal to ""
				sa = Response.dnu();
		} else if (sa.contains( Strings.ELLIPSIS )) // if required put in answer (verbatim!)
			sa.replace( Strings.ellipsis, toString() );
		else if (sa.contains( Answer.placeholder() ))
			sa.replace( Answer.placeholderAsStrings(), toString() );
		return sa;
	}
	
	/*
	 * N.B. Should the answer be judged for felicity?
	 *      Should an answer have a type, at all?
	 */
	private int    type = Response.N_DNK;
	public  int    type() { return type; }
	public  String setType( String s ) {
		// This sets type to first non-NK type
		if (type == Response.N_DNK) {
			     if (s.startsWith( Response.yesStr()    )) type = Response.N_OK;
			else if (s.startsWith( Response.successStr())) type = Response.N_OK;
			else if (s.startsWith( Response.noStr()     )) type = Response.N_NO;
			else if (s.startsWith( Response.dnuStr()    )) type = Response.N_DNU;
			else if (s.startsWith( Response.failureStr())) type = Response.N_FAIL;
			else if (s.startsWith( Response.dnkStr()    )) type = Response.N_DNK;
			else type = Response.N_CHS;
		}
		return s;
	}

	@Override
	public  String toString() {return answers.toString( Reply.andListFormat() );}
}
