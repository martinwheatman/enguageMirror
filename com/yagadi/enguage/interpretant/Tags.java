package com.yagadi.enguage.interpretant;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;

import com.yagadi.enguage.object.Attributes;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Number;
import com.yagadi.enguage.util.Strings;
import com.yagadi.enguage.vehicle.Language;
import com.yagadi.enguage.vehicle.Reply;
import com.yagadi.enguage.vehicle.Utterance;

public class Tags extends ArrayList<Tag> {
	static final         long serialVersionUID = 0;
	static private       Audit           audit = new Audit( "Tags", false );
	
	public Tags() { super(); }
	
	// "if X do Y" -> [ <x pref="if "/>, <y pref="do "/>, <pref="."> ]
	public Tags( Strings words ) {
		//String prefix = "";
		Tag t = new Tag();
		for ( String word : words ) {
			if ((1 == word.length()) && Strings.isUpperCase( word ) && !word.equals("I")) {
				t.name( word.toLowerCase( Locale.getDefault())); //.prefix( new Strings( prefix ));
				add( t );
				t = new Tag();
				//prefix = "";
			} else if (Strings.isUpperCaseWithHyphens( word ) && !word.equals( "I" )) { // TODO: remove "I"
				Strings arr = new Strings( word, '-' ); // should at least be array of 1 element!
				if (null != arr) {
					int asz = arr.size();
					int j = 0;
					//Iterator<String> ai = arr.iterator();
					for (String subWord : arr) {
						subWord = subWord.toLowerCase( Locale.getDefault());
						if ( asz > ++j ) // 
							t.attribute( subWord, subWord ); // non-last words in array
						else
							t.name( subWord ); // last word in array
				}	}
				//t.prefix( new Strings( prefix ));
				add( t );
				t = new Tag();
				//prefix = "";
			} else
				t.prefix( word );
		}
		//t.prefix( new Strings( prefix ));
		if (!t.isEmpty()) add( t );
		//else audit.log( ">>>>>>>>>>>empty tag found");
	}
	
	// Manual Autopoiesis... needs to deal with:
	// if variable x do phrase variable y => if X fo PHRASE-Y
	// i need numeric variable quantity variable units of phrase variable needs.
	// => i need NUMERIC-QUANTITY UNIT of PHRASE-NEEDS
	
	public Tags( String str ) { this( Utterance.toPattern( str )); }
	
	static private       boolean debug = false;
	static public        boolean debug() { return debug; }
	static public        void    debug( boolean b ) { debug = b; }
	
	public boolean equals( Tags ta ) {
		if (ta == null || size() != ta.size())
			return false;
		else {
			Iterator<Tag> it = iterator(), tait = ta.iterator();
			while (it.hasNext())
				if (!it.next().equals( tait.next() ))
					return false;
		}
		return true;
	}
	public boolean matches( Tags patterns ) {
		if (patterns.size() == 0) return true; // ALL = "" 
		if (patterns == null || size() < patterns.size()) return false;
		Iterator<Tag> it = iterator(),
				pit = patterns.iterator();
		while (it.hasNext()) // ordered by patterns
			if (!it.next().matches( pit.next() ))
				return false;
		return true;
	}

	/* *************************************************************************
	 * matchValues() coming soon...
	 */
	/*private Attribute matchedAttr( Tag t, String val ) {
		return new Attribute(	t.name(),
										Attribute.expandValues( // prevents X="x='val'"
											t.name().equals("unit") ? Plural.singular( val ) : val
										).toString( Strings.SPACED ) );
	} // */
	
	private String doNumeric( ListIterator<String> ui ) {
		String toString = Number.getNumber( ui ).toString();
		return toString.equals( Number.NotANumber ) ? null : toString;
	}
	
	private String getPhraseTerm( Tag t, ListIterator<Tag> ti ) {
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
	private String getVal( Tag t, ListIterator<Tag> ti, ListIterator<String> ui) {
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
	public Attributes matchValues( Strings utterance ) {
		
		// First, a sanity check
		if (size() == 0) return null; // manual/vocal Tags creation can produce null Tags objects
		                              // see "first reply well fancy that" in Enguage sanity test.
		Strings  prefix = get( 0 ).prefix();
		if ( prefix.size() > 0 &&
		    !prefix.get( 0 ).equalsIgnoreCase( utterance.get( 0 ) ))
		{
			return null;
		}
		
		/* We need to be able to extract:
		 * NAME="value"				... <NAME/>
		 * NAME="some value"		... <NAME phrased="phrased"/>
		 * NAME="68"                ... <NAME numeric='numeric'/>
		 * ???NAME="an/array/or/list"	... <NAME array="array"/>
		 * ???NAME="value one/value two/value three" <NAME phrased="phrased" array="array"/>
		 */
		Attributes matched = null; // lazy creation
		ListIterator<Tag>    ti = listIterator();           // [ 'this    is    a   <test/>' ]
		ListIterator<String> ui = utterance.listIterator(); // [ "this", "is", "a", "test"   ]
		
		//if (debug) audit.traceIn( "matchValues", "'"+ toLine() +"'" ); // +"', a =>'"+Strings.toString( sa, Strings.SPACED) +"'" );
		Tag readAhead = null;
		// step thru' [..., "pref"+<pattern/>, ...] && [..., "pref", "value", ...] together
		while ( ti.hasNext() && ui.hasNext() ) {
			Tag t = (readAhead != null) ? readAhead : ti.next();
			readAhead = null;
			if (null == (ui = matchBoilerplate( t.prefix(), ui ))) { // ...match prefix
				//if (debug) audit.traceOut("prefix mismatch:"+ (!ti.hasNext() ? "LENGTH" : null == t ? "NULL" : t.prefix()));
				return null;
				
			} else if (!ui.hasNext() && t.name().equals( "" )) { // end of array on null (end?) tag...
				// ...don't move ai on: !ai & we're finished, !NAME(ti) & check ai with next tag
				//if (debug) audit.audit( "Tags.matchValues():EOU && blankTag("+ t.toString() +") -- read over empty tag" );
				if (ti.hasNext()) readAhead = ti.next();
				
			} else if (ui.hasNext() && !t.name().equals( "" )) { // do these loaded match?
				String val = null;
				if (t.isNumeric()) {
					
					if (null == (val = doNumeric( ui ))) {
						//if (debug) audit.traceOut( "non-numeric" );
						return null;
					}
					
				} else if (t.invalid( ui )) {
					//if (debug) audit.traceOut( "invalidTagReason" );
					return null;
					
				} else
					val = getVal( t, ti, ui );
					
				// ...add value
				if (null == matched) matched = new Attributes();
				matched.add( t.matchedAttr( val )); // remember what it was matched with!
				
				if (null == (ui = matchBoilerplate( t.postfixAsStrings(), ui ))) {
					//if (debug) audit.traceOut( "postfix mismatch:"+ t.postfix() );
					return null;
		}	}	}
		
		if (ti.hasNext() || ui.hasNext()) return null;
		
		return null == matched ? new Attributes() : matched;
	}
	// with postfix boilerplate:
	// typically { [ ">>>", "name1" ], [ "/", "name2" ], [ "/", "name3" ], [ "<<<", "" ] }.
	// could be  { [ ">>>", "name1", "" ], [ "/", "name2", "" ], [ "/", "name3", "<<<" ] }.
	public String toXml() { return toXml( 0 );}
	public String toXml( int level ) {
		String str="";
		Iterator<Tag> ti = iterator();
		while (ti.hasNext()) {
			str += ti.next().toXml( level );
			if (ti.hasNext()) str += " ";
		}
		return str;
	}
	public String toString() {
		String str="";
		Iterator<Tag> ti = iterator();
		while (ti.hasNext()) {
			str += ti.next().toString();
			if (ti.hasNext()) str += " ";
		}
		return str;
	}
	public String toText() {
		String str="";
		Iterator<Tag> ti = iterator();
		while (ti.hasNext()) {
			str += ti.next().toText();
			if (ti.hasNext()) str += " ";
		}
		return str;
	}
	public String toLine() {
		String str="";
		Iterator<Tag> ti = iterator();
		while (ti.hasNext()) {
			Tag t = ti.next();
			str += ( " "+t.prefix().toString()+" <"+t.name() +" "+ t.attributes().toString() +"/> "+t.postfix());
		}
		return str;
	}
	
	// --- test code...
	//private static void printTagsAndValues( Tags interpretant, String phrase ) {
	//	printTagsAndValues( interpretant, phrase, null );
	//}
	public static void printTagsAndValues( Tags interpretant, String phrase, Attributes expected ) {
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

	public static void main(String args[]) {
		Audit.allOn();
		audit.tracing = true;
		debug( true );
		
		Tags t = new Tags();
		t.add( new Tag( "what is ", "X" ).attribute( Tag.numeric, Tag.numeric ) );
		printTagsAndValues( t, "what is 1 + 2", new Attributes().add( "X", "1 + 2" ));

		printTagsAndValues( new Tags( "i need phrase variable need" ),
				"I need coffee", 
				new Attributes()
					.add( "need",     "coffee" )
		);
		printTagsAndValues( new Tags( "i need numeric variable quantity variable unit of phrase variable need" ),
				"I need a cup of coffee", 
				new Attributes()
					.add( "quantity", "1" )
					.add( "unit",     "cup" )
					.add( "need",     "coffee" )
		);
}	}
