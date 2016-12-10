package com.yagadi.enguage.concept;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;

import com.yagadi.enguage.expression.Language;
import com.yagadi.enguage.expression.Plural;
import com.yagadi.enguage.expression.Reply;
import com.yagadi.enguage.sofa.Attribute;
import com.yagadi.enguage.sofa.Attributes;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Number;
import com.yagadi.enguage.util.Strings;

public class Tags extends ArrayList<Tag> {
	static final         long serialVersionUID = 0;
	static private       Audit           audit = new Audit( "Tags", false );
	
	static private       boolean debug = false;
	static public        boolean debug() { return debug; }
	static public        void    debug( boolean b ) { debug = b; }
	
	
	// with postfix boilerplate:
	// typically { [ ">>>", "name1" ], [ "/", "name2" ], [ "/", "name3" ], [ "<<<", "" ] }.
	// could be  { [ ">>>", "name1", "" ], [ "/", "name2", "" ], [ "/", "name3", "<<<" ] }.
	public String toXml() { return toXml( 0 );}
	public String toXml( int level ) {
		String str="";
		Iterator<Tag> ti = iterator();
		while (ti.hasNext())
			str += ti.next().toXml( level );
		return str;
	}
	public String toString() {
		String str="";
		Iterator<Tag> ti = iterator();
		while (ti.hasNext()) str += ti.next().toString();
		return str;
	}
	public String toText() {
		String str="";
		Iterator<Tag> ti = iterator();
		while (ti.hasNext()) str += ti.next().toText();
		return str;
	}
	public String toLine() {
		String str="";
		Iterator<Tag> ti = iterator();
		while (ti.hasNext()) {
			Tag t = ti.next();
			str += ( " "+t.prefix()+" <"+t.name() +" "+ t.attributes().toString() +"/> "+t.postfix());
		}
		return str;
	}

	public Tags() { super(); }
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
	// "if X do Y" -> [ <x pref="if "/>, <y pref="do "/>, <pref="."> ]
	public Tags( String str ) {
		Strings words = new Strings( str ); // correct? ...FromChars? ...NonWS?
		String prefix = "";
		Tag t = new Tag();
		Iterator<String> wi = words.iterator();
		while ( wi.hasNext() ) {
			String word = wi.next();
			if ((1 == word.length()) && Strings.isUpperCase( word ) && !word.equals("I")) {
				t.name( word.toLowerCase( Locale.getDefault())).prefix( prefix );
				add( t );
				t = new Tag();
				prefix = new String( Tag.emptyPrefix );
			} else if (Strings.isUpperCaseWithHyphens( word ) && !word.equals( "I" )) { // TODO: remove "I"
				Strings arr = new Strings( word, '-' ); // should at least be array of 1 element!
				if (null != arr) {
					int asz = arr.size();
					int j = 0;
					Iterator<String> ai = arr.iterator();
					 while (ai.hasNext()) {
						String subWord = ai.next().toLowerCase( Locale.getDefault());
						if ( asz > ++j ) // 
							t.attribute( subWord, subWord ); // non-last words in array
						else
							t.name( subWord ); // last word in array
				}	}
				t.prefix( prefix );
				add( t );
				t = new Tag();
				prefix =  new String( Tag.emptyPrefix );
			} else
				prefix += (word + " ");
		}
		t.prefix( prefix );
		if (!t.isNull()) add( t );
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
			Strings arr = ti.next().prefixAsStrings();
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
		if (t.phrased() || (ui.hasNext() &&  Reply.andConjunctions().contains( u ))) {
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
		Strings  prefix = get( 0 ).prefixAsStrings();
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
			if (null == (ui = matchBoilerplate( t.prefixAsStrings(), ui ))) { // ...match prefix
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
		
		if (ti.hasNext()) {
			//if (debug) audit.traceOut( "tags still contains:"+ ti.next().toText() );
			return null;
		}
		if (ui.hasNext()) {
			//if (debug) audit.traceOut( "utterance still contains:"+ ui.next());
			return null;
		}
		
		//if (debug) audit.traceOut( "matched => "+ (matched==null ? "no values" : matched.toString()));
		return null == matched ? new Attributes() : matched;
	}

	
	// --- test code...
	static String
			prefix = "what is",
			prefix2 = "wh is",
            postfix = "?",
	       	testPhrase = prefix + " " + "1 + 2" + postfix, //+" "+ prefix +" "+ "3+4"+ postfix;
	       	testPhrase2 = prefix + " " + "1 + 2" + postfix +" "+ prefix2 +" "+ "5 + 4"+ postfix;
	
	private static void printTagsAndValues( Tags ta, String phrase ) {
		//audit.audit( "Matching phrase: "+ phrase +"\n      with:"+ ta.toLine());
		Attributes values = ta.matchValues( new Strings( phrase ));
		if ( null == values )
			audit.log( "values => null\n" );
		else {
			String vals = "", tmp;
			for( Attribute a : values ) {
				ListIterator<String> li = new Strings( a.value()).listIterator();
				Number n = Number.getNumber( li );
				tmp = n.valueOf();
				vals += " " + (Number.NotANumber == tmp ? a.value() : tmp);
			}
			audit.log( "values => ["+ vals +" ]\n" );
	}	}

	public static void main(String args[]) {
		Audit.allOn();
		audit.tracing = true;
		debug( true );
		
		Tags ta = new Tags();
		Tag t = new Tag( prefix + " ", "X", postfix ).attribute( Tag.numeric, Tag.numeric );
		ta.add( t );
		printTagsAndValues( ta, testPhrase );
		
		// ok, add a PHRASE- attribute and repeat this test...
		ta = new Tags();
		t = new Tag( prefix + " ", "X", postfix ).attribute( Tag.numeric, Tag.numeric );
		t.attribute( Tag.phrase, Tag.phrase );
		ta.add( t );
		printTagsAndValues( ta, testPhrase );
		// */
		
		// ok, add a PHRASE- attribute and repeat this test...
		ta = new Tags();
		t = new Tag( prefix + " ", "X", postfix ).attribute( Tag.numeric, Tag.numeric );
		ta.add( t );
		t = new Tag( prefix2 + " ", "Y", postfix ).attribute( Tag.numeric, Tag.numeric );
		ta.add( t );
		printTagsAndValues( ta, testPhrase2 );
		// */
		
		ta = new Tags();
		t = new Tag( "I've seen ", "X", "" ).attribute( Tag.phrase, Tag.phrase );
		ta.add( t );
		printTagsAndValues( ta, "I've seen all good people" );

		ta = new Tags();
		t = new Tag( "I've seen ", "X", " people" ).attribute( Tag.phrase, Tag.phrase );
		ta.add( t );
		printTagsAndValues( ta, "I've seen all good people" );

		ta = new Tags();
		t = new Tag( "I've seen ", "X", "" ).attribute( Tag.phrase, Tag.phrase );
		ta.add( t );
		t = new Tag("turn their heads", "Y" ).attribute( Tag.phrase, Tag.phrase );
		ta.add( t );
		printTagsAndValues( ta, "I've seen all good people turn their heads each day" );

		ta = new Tags();
		t = new Tag( "I've seen ", "X", "" ).attribute( Tag.quoted, Tag.quoted );
		ta.add( t );
		printTagsAndValues( ta, "I've seen \"all good people\"" );

		ta = new Tags();
		t = new Tag( "I've seen ", "X", "" ).attribute( Tag.numeric, Tag.numeric );
		ta.add( t );
		t = new Tag( " ", "Y", " people " ).attribute( Tag.phrase, Tag.phrase );
		ta.add( t );
		printTagsAndValues( ta, "I've seen a damn good people" );

		ta = new Tags();
		t = new Tag( "I've seen ", "X", "" ).attribute( Tag.numeric, Tag.numeric ).attribute( Tag.phrase, Tag.phrase );
		ta.add( t );
		t = new Tag( " ", "Y", " people " ).attribute( Tag.phrase, Tag.phrase );
		ta.add( t );
		printTagsAndValues( ta, "I've seen 2 * 3 good people" );
// */
		ta = new Tags();
		t = new Tag( "add / ", "X" ).attribute( Tag.phrase, Tag.phrase );
		ta.add( t );
		t = new Tag( " / to ", "Y" );
		ta.add( t );
		t = new Tag( "  ", "Z", " list" );
		ta.add( t );
		printTagsAndValues( ta, "add / to go to town / to _user needs list" );
//*
		ta = new Tags();
		t = new Tag( "what is ", "X", "" ).attribute( Tag.numeric, Tag.numeric ).attribute( Tag.phrase, Tag.phrase );
		ta.add( t );
		printTagsAndValues( ta, "what is 1 * 2 * 3" );
//*/
}	}
