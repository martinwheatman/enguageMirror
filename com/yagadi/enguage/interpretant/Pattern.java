package com.yagadi.enguage.interpretant;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;

import com.yagadi.enguage.object.Attributes;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Indent;
import com.yagadi.enguage.util.Number;
import com.yagadi.enguage.util.Strings;
import com.yagadi.enguage.vehicle.Language;
import com.yagadi.enguage.vehicle.Reply;

public class Pattern extends ArrayList<Patternette> { // was Tags
	static final         long serialVersionUID = 0;
	static private       Audit           audit = new Audit( "Tags", false );
	static private final String       variable = "variable";
	public static final String phrase = "phrase";
	public static final String phrasePrefix = phrase.toUpperCase( Locale.getDefault() ) + "-";
	public static final String numeric = "numeric";
	public static final String numericPrefix = numeric.toUpperCase( Locale.getDefault() ) + "-";
	
	public Pattern() { super(); }
	public Pattern( Strings words ) {
		// "if X do Y" -> [ <x prefix=["if"]/>, <y prefix=["do"] postfix="."/> ]
		Patternette t = new Patternette();
		for ( String word : words ) {
			if (Strings.isUpperCaseWithHyphens( word ) && !word.equals( "I" )) { // TODO: remove "I"
				Strings arr = new Strings( word, '-' ); // should at least be array of 1 element!
				int  j = 0, asz = arr.size();
				for (String subWord : arr) {
					subWord = subWord.toLowerCase( Locale.getDefault());
					if ( asz > ++j ) // 
						t.attribute( subWord, subWord ); // non-last words in array
					else
						t.name( subWord ); // last word in array
				}
				add( t );
				t = new Patternette();
			} else
				t.prefix( word );
		}
		if (!t.isEmpty()) add( t );
	}
	
	// Manual Autopoiesis... needs to deal with:
	// if variable x do phrase variable y => if X fo PHRASE-Y
	// i need numeric variable quantity variable units of phrase variable needs.
	// => i need NUMERIC-QUANTITY UNIT of PHRASE-NEEDS
	public Pattern( String str ) { this( toPattern( str )); }
	
	static public Strings toPattern( String u ) {
		// my name is variable name => my name is NAME
		Strings in  = new Strings( u ),
				out = new Strings();
		Iterator<String> wi = in.iterator();
		while ( wi.hasNext() ) {
			String word = wi.next();
			if (word.equals( variable )) {
				if (wi.hasNext() && null != (word = wi.next()) && !word.equals( variable )) // so we can't have VARIABLE, ok...
					out.append( word.toUpperCase( Locale.getDefault()) );
				else // variable. OR variable variable
					out.append( variable );
				
			} else if (word.equals( numeric ))
				if (wi.hasNext() && null != (word = wi.next()))
					if (word.equals( variable ))
						if (wi.hasNext() && (null != (word = wi.next() )) && !word.equals( variable ))
							out.append( numericPrefix + word.toUpperCase( Locale.getDefault()) );
						else // numeric variable. or numeric variable variable
							out.append( numeric ).append( variable );
					
					else if (word.equals( phrase ))
						if (wi.hasNext() && null != (word = wi.next()))
							if (word.equals( variable ))
								if (wi.hasNext() && (null != (word = wi.next() )) && !word.equals( variable ))
									out.append( numericPrefix + phrasePrefix + word.toUpperCase( Locale.getDefault()) );
								else // numeric phrase variable. OR numeric phrase variable variable
									out.append( numeric ).append( phrase ).append( variable );
							else // numeric phrase blah
								out.append( numeric ).append( phrase ).append( word );
						else // numeric phrase.
							out.append( numeric ).append( phrase );
					else // numeric blah
						out.append( numeric ).append( word );		
				else // numeric.
					out.append( numeric );
				
			else if (word.equals( phrase ))
				if (wi.hasNext() && null != (word = wi.next()))
					if (word.equals( variable ))
						if (wi.hasNext() && null != (word = wi.next()) && !word.equals( variable ))
							out.append( phrasePrefix + word.toUpperCase( Locale.getDefault()) );
						else // phrase variable. OR phrase variable variable
							out.append( phrase ).append( word );
					else // phrase blah
						out.append( phrase ).append( word );
				else // phrase.
					out.append( phrase );
			else // blah
				out.append( word );
		}
		return out;
	}
	
	static private       boolean debug = false;
	static public        boolean debug() { return debug; }
	static public        void    debug( boolean b ) { debug = b; }
	
	public boolean xequals( Pattern ta ) {
		if (ta == null || size() != ta.size())
			return false;
		else {
			Iterator<Patternette> it = iterator(), tait = ta.iterator();
			while (it.hasNext())
				if (!it.next().equals( tait.next() ))
					return false;
		}
		return true;
	}
	public boolean xmatches( Pattern patterns ) {
		if (patterns.size() == 0) return true; // ALL = "" 
		if (patterns == null || size() < patterns.size()) return false;
		Iterator<Patternette> it = iterator(),
				pit = patterns.iterator();
		while (it.hasNext()) // ordered by patterns
			if (!it.next().matches( pit.next() ))
				return false;
		return true;
	}

	/* *************************************************************************
	 * matchValues() coming soon...
	 */
	private String doNumeric( ListIterator<String> ui ) {
		String toString = Number.getNumber( ui ).toString();
		return toString.equals( Number.NotANumber ) ? null : toString;
	}
	private String getPhraseTerm( Patternette t, ListIterator<Patternette> ti ) {
		String term = null;
		if (t.postfix != null && !t.postfix.equals( "" ))
			term = t.postfixAsStrings().get( 0 );
		else if (ti.hasNext()) {
			// next prefix as array is...
			Strings arr = ti.next().prefix();
			// ...first token of which is the terminator
			term = (arr == null || arr.size() == 0) ? null : arr.get( 0 );
			ti.previous();
		}
		return term;
	}
	private String getVal( Patternette t, ListIterator<Patternette> ti, ListIterator<String> ui) {
		String u = "unseta";
		if (ui.hasNext()) u = ui.next();
		Strings vals = new Strings( u );
		if (t.isPhrased() || (ui.hasNext() &&  Reply.andConjunctions().contains( u ))) {
			String term = getPhraseTerm( t, ti );
			//audit.audit( "phrased, looking for terminator "+ term );
			// here: "... one AND two AND three" => "one+two+three"
			if (term == null) {  // just read to the end
				while (ui.hasNext()) {
					u = ui.next();
					vals.add( u );
				}
			} else {
				while (ui.hasNext()) {
					u = ui.next();
					//audit.debug( "next u="+ u );
					if (term.equals( u )) {
						ui.previous();
						break;
					} else {
						vals.add( u );
		}	}	}	}
		return vals.toString( Strings.SPACED );
	}
	private static ListIterator<String> matchBoilerplate( Strings tbp, ListIterator<String> ui ) {
		Iterator<String> tbpi = tbp.iterator();
		while ( tbpi.hasNext() && ui.hasNext()) 
			if (!Language.wordsEqualIgnoreCase( tbpi.next(), ui.next() ))
				return null; // string mismatch
		// have we reached end of boilerplate, but not utterance?
		return tbpi.hasNext() ? null : ui;
	}
	
	/* TODO: Proposal: that a singular tag (i.e. non-PHRASE) can match with a known string
	 * i.e. an object id: e.g. theOldMan, thePub, fishAndChips camelised
	 */
	private int notMatched = 0;
	public String notMatched() {
		return notMatched == 0 ? "matched" :
			notMatched == 1 ? "precheck 1" :
			notMatched == 2 ? "precheck 2" :
			notMatched == 11 ? "prefix" :
			notMatched == 15 ? "not numeric" :
			notMatched == 16 ? "invalid flags" :
			notMatched == 19 ? "postfix" :
			notMatched == 21 ? "more pattern" :
			notMatched == 22 ? "more utterance" : ("unknown:"+ notMatched);
	}
	public Attributes matchValues( Strings utterance ) {
		
		notMatched = 0;
		
		// First, a sanity check
		if (size() == 0) {
			notMatched = 1;
			return null; // manual/vocal Tags creation can produce null Tags objects
                         // see "first reply well fancy that" in Enguage sanity test.
		}
		Strings  prefix = get( 0 ).prefix();
		if ( prefix.size() > 0 &&
		    !prefix.get( 0 ).equalsIgnoreCase( utterance.get( 0 ) ))
		{
			notMatched = 2;
			return null;
		}
		
		/* We need to be able to extract:
		 * NAME="value"				... <NAME/>
		 * NAME="some value"		... <NAME phrased="phrased"/>
		 * NAME="68"                ... <NAME numeric='numeric'/>
		 * ???NAME="an/array/or/list"	... <NAME array="array"/>
		 * ???NAME="value one/value two/value three" <NAME phrased="phrased" array="array"/>
		 */
		Attributes         matched = null; // lazy creation
		ListIterator<Patternette>    patti = listIterator();           // [ 'this    is    a   <test/>' ]
		ListIterator<String>  utti = utterance.listIterator(); // [ "this", "is", "a", "test"   ]
		
		Patternette next = null;
		while (patti.hasNext() && utti.hasNext()) {
			
			Patternette t = (next != null) ? next : patti.next();
			next = null;
			
			if (null == (utti = matchBoilerplate( t.prefix(), utti ))) { // ...match prefix
				notMatched = 11;
				return null;
				
			} else if (!utti.hasNext() && t.name().equals( "" )) { // end of array on null (end?) tag...
				if (patti.hasNext()) next = patti.next();
				
			} else if (utti.hasNext() && !t.name().equals( "" )) { // do these loaded match?
				String val = null;
				if (t.isNumeric()) {
					
					if (null == (val = doNumeric( utti ))) {
						notMatched = 15;
						return null;
					}
					
				} else if (t.invalid( utti )) {
					notMatched = 16;
					return null;
				}
					
				else
					val = getVal( t, patti, utti );
					
				// ...add value
				if (null == matched) matched = new Attributes();
				matched.add( t.matchedAttr( val )); // remember what it was matched with!
				
				if (null == (utti = matchBoilerplate( t.postfixAsStrings(), utti ))) {
					notMatched = 19;
					return null;
				}
		}	}
		
		if (patti.hasNext()) {
			notMatched = 21;
			return null;
		}
		if (utti.hasNext()) {
			notMatched = 22;
			return null;
		}
		return null == matched ? new Attributes() : matched;
	}
	// with postfix boilerplate:
	// typically { [ ">>>", "name1" ], [ "/", "name2" ], [ "/", "name3" ], [ "<<<", "" ] }.
	// could be  { [ ">>>", "name1", "" ], [ "/", "name2", "" ], [ "/", "name3", "<<<" ] }.
	public String toXml() { return toXml( new Indent( "   " )); }
	public String toXml( Indent indent ) {
		String oldName = "";
		String str  = "\n"+indent.toString();
		Iterator<Patternette> ti = iterator();
		while (ti.hasNext()) {
			Patternette t = ti.next();
			str += (t.name().equals( oldName ) ? "\n"+indent.toString() : "") + t.toXml( indent );
			oldName = t.name();
		}
		return str;
	}
	public String toString() {
		String str="";
		Iterator<Patternette> ti = iterator();
		while (ti.hasNext()) {
			str += ti.next().toString();
			if (ti.hasNext()) str += " ";
		}
		return str;
	}
	public String toText() {
		String str="";
		Iterator<Patternette> ti = iterator();
		while (ti.hasNext()) {
			str += ti.next().toText();
			if (ti.hasNext()) str += " ";
		}
		return str;
	}
	public String toLine() {
		String str="";
		Iterator<Patternette> ti = iterator();
		while (ti.hasNext()) {
			Patternette t = ti.next();
			str += ( " "+t.prefix().toString()+" <"+t.name() +" "+ t.attributes().toString() +"/> "+t.postfix());
		}
		return str;
	}
	
	// --- test code...
	public static void printTagsAndValues( Pattern interpretant, String phrase, Attributes expected ) {
		audit.in( "printTagsAndValues", "ta="+ interpretant.toString() +", phr="+ phrase +", expected="+ 
				(expected == null ? "":expected.toString()) );
		Attributes values = interpretant.matchValues( new Strings( phrase ));
		
		// de-reference values...
		String vals = values.toString();
		if (null == expected)
			audit.log( "values => ["+ vals +"]" );
		else if (values != null && values.matches( expected ))
			audit.log( "PASSED => ["+ vals +"]" );
		else 
			audit.log( "FAILED: expecting: "+ expected +", got: "+ vals );
		audit.out();
	}
	private static void toPatternTest( String utt  ) {
		toPatternTest( utt, utt ); // check it against itself!
	}
	private static void toPatternTest( String utt, String answer ) {
		String patt = toPattern( utt ).toString();
		if (answer != null && !answer.equals( patt ))
			audit.FATAL( "answer '"+ patt +"' doesn't equal expected: '" + answer +"'" );
		audit.log( ">"+ utt +"< to pattern is >"+ patt +"<" );
	}

	public static void main(String args[]) {
		Audit.allOn();
		audit.tracing = true;
		debug( true );
		
		audit.LOG( "pattern: "+ toPattern( "variable name needs numeric variable quantity units of phrase variable object" ));

		Pattern t = new Pattern();
		t.add( new Patternette( "what is ", "X" ).attribute( numeric, numeric ) );
		printTagsAndValues( t, "what is 1 + 2", new Attributes().add( "X", "1 + 2" ));

		printTagsAndValues( new Pattern( "i need phrase variable need" ),
				"I need coffee", 
				new Attributes()
					.add( "need",     "coffee" )
		);
		printTagsAndValues( new Pattern( "i need numeric variable quantity variable unit of phrase variable need" ),
				"I need a cup of coffee", 
				new Attributes()
					.add( "quantity", "1" )
					.add( "unit",     "cup" )
					.add( "need",     "coffee" )
		);
		
		//toPattern() tests...
		toPatternTest( "the factorial of n" );
		toPatternTest( "the factorial of n blah" );
		toPatternTest( "the factorial of variable variable", "the factorial of variable" );
		toPatternTest( "the factorial of variable n", "the factorial of N" );
		toPatternTest( "the factorial of variable n blah", "the factorial of N blah" );

		toPatternTest( "the factorial of phrase" );
		toPatternTest( "the factorial of phrase n" );
		toPatternTest( "the factorial of phrase variable variable","the factorial of phrase variable" );
		toPatternTest( "the factorial of phrase variable n", "the factorial of PHRASE-N" );
		toPatternTest( "the factorial of phrase variable n blah", "the factorial of PHRASE-N blah" );

		toPatternTest( "the factorial of numeric" );
		toPatternTest( "the factorial of numeric n" );
		toPatternTest( "the factorial of numeric variable variable", "the factorial of numeric variable" );
		toPatternTest( "the factorial of numeric variable n", "the factorial of NUMERIC-N" );
		toPatternTest( "the factorial of numeric variable n blah", "the factorial of NUMERIC-N blah" );

		toPatternTest( "the factorial of numeric phrase" );
		toPatternTest( "the factorial of numeric phrase n" );
		toPatternTest( "the factorial of numeric phrase n blah" );
		toPatternTest( "the factorial of numeric phrase variable" );
		toPatternTest( "the factorial of numeric phrase variable variable", "the factorial of numeric phrase variable" );
		toPatternTest( "the factorial of numeric phrase variable n", "the factorial of NUMERIC-PHRASE-N" );
		toPatternTest( "the factorial of numeric phrase variable n blah", "the factorial of NUMERIC-PHRASE-N blah" );
		audit.log( "PASSED" );
}	}
