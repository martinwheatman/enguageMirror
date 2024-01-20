package org.enguage.sign.symbol.reply;

import org.enguage.sign.Config;
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

	
	// [ "black coffee", "eggs", "Taj Mahal" ]; w/embedded spaces!
	private Strings answers = new Strings();
	
	public  void    add( String s ) {answers.add( s );}
	
	public Strings injectAnswer( Strings sa ) {
		if (sa.isEmpty()) {
			sa = new Strings( toString() ); // use the raw answer
			if (sa.isEmpty()) // so a was equal to ""
				sa = Config.dnu();
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
	private Reply.Type type = Reply.Type.E_DNK;
	public  Reply.Type type() { return type; }
	public  Answer     type( Reply.Type t) {type = t; return this;}
	public  Reply.Type stringToResponseType( String s ) {
		// This sets type to first non-NK type
		Reply.Type t = Reply.Type.E_DNK;
		if (type == Reply.Type.E_DNK) {
			     if (s.toLowerCase().startsWith( Config.yesStr()    )) t = Reply.Type.E_OK;
			else if (s.toLowerCase().startsWith( Config.okayStr())) t = Reply.Type.E_OK;
			else if (s.toLowerCase().startsWith( Config.noStr()     )) t = Reply.Type.E_NO;
			else if (s.toLowerCase().startsWith( Config.dnuStr()    )) t = Reply.Type.E_DNU;
			else if (s.toLowerCase().startsWith( Config.notOkayStr())) t = Reply.Type.E_SOZ;
			else if (s.toLowerCase().startsWith( Config.dnkStr()    )) t = Reply.Type.E_DNK;
			else t = Reply.Type.E_CHS;
		}
		return t;
	}

	@Override
	public  String toString() {return answers.toString( Config.andListFormat() );}
}
