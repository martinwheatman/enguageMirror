package org.enguage.vehicle.reply;

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
	static public  void    placeholder( String ph ) {placeholderAsStrings = new Strings( placeholder = ph );
	}

	private boolean appending = false;
	public  boolean isAppending() { return appending; }
	public  void    appendingIs( boolean b ) { appending = b; }
	
	// [ "black coffee", "eggs", "Taj Mahal" ]; w/embedded spaces!
	private Strings answers = new Strings();
	
	public  Strings valueOf() {return answers;}
	public  void    add( String s ) { answers.add( setType( s ));}
	public  boolean none() { return answers.size() == 0; }
	
	public Strings injectAnswer( Strings sa ) {
		if (0 == sa.size()) {
			sa = new Strings( answers ); // use the raw answer
			if (sa.size() == 0) // so a was equal to ""
				sa = Reply.dnu();
		} else if (sa.contains( Strings.ELLIPSIS )) // if required put in answer (verbatim!)
			sa.replace( Strings.ellipsis, answers );
		else if (sa.contains( Answer.placeholder() ))
			sa.replace( Answer.placeholderAsStrings(), answers );
		
// If we need to inject answers into strings like "...'s"
//	Pattern pattern = Pattern.compile( Strings.ELLIPSIS, Pattern.LITERAL );
//	ListIterator<String> i = sa.listIterator();
//	while (i.hasNext()) {
//		Matcher matcher = pattern.matcher( i.next() );
//		if (matcher.find())
//			i.set(matcher.replaceAll( answers.toString() ));
//	}
		
		return sa;
	}
	
	private int    type = Reply.DNK;
	public  int    type() { return type; }
	private String setType( String s ) {
		// This sets type to first non-NK type
		if (type == Reply.DNK) {
			     if (s.equalsIgnoreCase( Reply.yesStr()    )) type = Reply.YES;
			else if (s.equalsIgnoreCase( Reply.successStr())) type = Reply.YES;
			else if (s.equalsIgnoreCase( Reply.noStr()     )) type = Reply.NO;
			else if (s.equalsIgnoreCase( Reply.dnuStr()    )) type = Reply.DNU;
			else if (s.equalsIgnoreCase( Reply.failureStr())) type = Reply.FAIL;
			else if (s.equalsIgnoreCase( Reply.dnkStr()    )) type = Reply.DNK;
			else type = Reply.CHS;
		}	
		return s;
	}

	@Override
	public  String toString() { return answers.toString( Reply.andListFormat() ); }
}
