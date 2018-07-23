package org.enguage.sign.pattern;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Random;

import org.enguage.util.algorithm.Expression;
import org.enguage.util.Attribute;
import org.enguage.util.Attributes;
import org.enguage.util.Audit;
import org.enguage.util.Indent;
import org.enguage.util.Strings;
import org.enguage.vehicle.Number;
import org.enguage.vehicle.Plural;
import org.enguage.vehicle.Reply;
import org.enguage.vehicle.where.Where;

public class Pattern extends ArrayList<Patternette> {
	static final         long serialVersionUID = 0;
	static private       Audit           audit = new Audit( "Pattern" );
	
	static private final Locale  locale        = Locale.getDefault();
	static private final String  variable      = "variable";
	static public  final String  quoted        = "quoted";
	static public  final String  list          = "list";
	static public  final String  quotedPrefix  = quoted.toUpperCase( locale ) + "-";
	static public  final String  phrase        = "phrase";
	static public  final String  phrasePrefix  = phrase.toUpperCase( locale ) + "-";
	static public  final String  numeric       = "numeric";
	static public  final String  numericPrefix = numeric.toUpperCase( locale ) + "-";
	static public  final String  expression    = "expression";
	static public  final String  expr          = "expr";
	static public  final String  exprPrefix    = expr.toUpperCase( locale ) + "-";
	static public  final String  plural        = Plural.NAME; // "plural";
	static public  final String  pluralPrefix  = plural.toUpperCase( locale ) + "-";
	static public  final String  sinsignPrefix = "SIGN-";
	
	public Pattern() { super(); }
	public Pattern( Strings words ) {
		
		// "if X do Y" -> [ <x prefix=["if"]/>, <y prefix=["do"] postfix="."/> ]
		Patternette t = new Patternette();
		for ( String word : words ) {
			
			if (word.equals( "an" )) word = "a";
			
			if (Strings.isUpperCaseWithHyphens( word ) && !word.equals( "I" )) { // TODO: remove "I"
				Strings arr = new Strings( word.toLowerCase( locale ), '-' );
				ListIterator<String> wi = arr.listIterator();
				String sw = wi.next();
				if (sw.equals( phrase )) {
					t.phrasedIs();
					if (wi.hasNext()) sw = wi.next();
					else audit.ERROR( "ctor: PHRASE variable, missing name." );
				} else if (sw.equals( plural )) {
					t.pluralIs();
					if (wi.hasNext()) sw = wi.next();
					else audit.ERROR( "ctor: PLURAL variable, missing name." );
				} else if (sw.equals( quoted )) {
					t.quotedIs();
					if (wi.hasNext()) sw = wi.next();
					else audit.ERROR( "ctor: QUOTED variable, missing name." );
				} else if (sw.equals( numeric )) {
					t.numericIs();
					if (wi.hasNext()) sw = wi.next();
					else audit.ERROR( "ctor: NUMERIC variable, missing name." );
				} else if (sw.equals( expr )) {
					t.exprIs();
					if (wi.hasNext()) sw = wi.next();
					else audit.ERROR( "ctor: EXPR variable, missing name." );
				} else if (sw.equals( Reply.andConjunction() )) {
					//audit.LOG( "found: "+ Reply.andConjunction() );
					if (wi.hasNext()) {
						sw = wi.next();
						if (sw.equals( list )) {
							if (wi.hasNext()) {
								//audit.LOG( "bingo: found isList" );
								sw = wi.next();
								t.listIs();
							} else
								audit.ERROR( "ctor: AND-LIST variable, missing name." );
						} else
							audit.ERROR( "ctor: AND-LIST? 'LIST' missing" );
					} else
						audit.ERROR( "ctor: AND terminates variable" );
				}
				
				while ((sw.equals( phrase  ) ||
				        sw.equals( plural  ) ||
				        sw.equals( quoted  ) ||
				        sw.equals( expr    ) ||
				        sw.equals( numeric )    )
					 && wi.hasNext())
				{
					audit.ERROR( "ctor: mutually exclusive modifiers" );
					sw = wi.next();
				}
				
				t.name( sw );
				add( t );
				t = new Patternette();
				
			} else
				t.prefix( word );
		}
		if (!t.isEmpty()) add( t );
	}
	
	// Manual Autopoiesis... needs to deal with:
	// if variable x do phrase variable y => if X do PHRASE-Y
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
			
			if (word.equals( "an" )) word = "a"; // English-ism!
			
			if (word.equals( variable ))
				if (wi.hasNext() && null != (word = wi.next()) && !word.equals( variable ))
					out.append( word.toUpperCase( locale ));
				else // variable. OR variable variable
					out.append( variable );
				
			else if (word.equals( numeric ))
				if (wi.hasNext() && null != (word = wi.next()))
					if (word.equals( variable ))
						if (wi.hasNext() && (null != (word = wi.next() )) && !word.equals( variable ))
							out.append( numericPrefix + word.toUpperCase( locale ));
						else // numeric variable. or numeric variable variable
							out.append( numeric ).append( variable );
					else // numeric blah
						out.append( numeric ).append( word );		
				else // numeric.
					out.append( numeric );
				
			else if (word.equals( expression ))
				if (wi.hasNext() && null != (word = wi.next()))
					if (word.equals( variable ))
						if (wi.hasNext() && (null != (word = wi.next() )) && !word.equals( variable ))
							out.append( exprPrefix + word.toUpperCase( locale ));
						else // numeric variable. or numeric variable variable
							out.append( expression ).append( variable );
					else // numeric blah
						out.append( expression ).append( word );		
				else // numeric.
					out.append( expression );
				
			else if (word.equals( phrase ))
				if (wi.hasNext() && null != (word = wi.next()))
					if (word.equals( variable ))
						if (wi.hasNext() && null != (word = wi.next()) && !word.equals( variable ))
							out.append( phrasePrefix + word.toUpperCase( locale ));
						else // phrase variable. OR phrase variable variable
							out.append( phrase ).append( word );
					else // phrase blah
						out.append( phrase ).append( word );
				else // phrase.
					out.append( phrase );
			
			else if (word.equals( "and" ))
				if (wi.hasNext() && null != (word = wi.next()) && word.equals( "list" ))
					if (wi.hasNext() && null != (word = wi.next()) && word.equals( "variable" ))
						if (wi.hasNext() && null != (word = wi.next()) && !word.equals( "variable" ))
							out.append( word.toUpperCase( locale )+"-AND-LIST" );
						else // and list variable variable
							out.append( "and" ).append( "list" ).append( "variable" );
					else // and list blah
						out.append( "and" ).append( "list" ).append( word );						
				else // so we can't have just VARIABLE, ok...
					out.append( "and" ).append( word );						

			else // blah
				out.append( word );
		}
		return out;
	}
	
	/*  The complexity of a pattern, used to rank signs in a repertoire.
	 *  "the eagle has landed" comes before "the X has landed", BUT
	 *  "the    X    Y-PHRASE" comes before "the Y-PHRASE" so it is not
	 *  a simple count of tags, phrased hot-spots "hoover-up" tags!
	 *  Phrased hot-spot has a large complexity, and any normal tags
	 *  will bring this complexity down!
	 *  
	 *  Three planes of complexity: bplate hotspots phrased-hotspots
	 *  ==========================
	 *  complexity increases with   1xm bp 1->100.
	 *  complexity increases with 100xn tags 100->10000
	 *  if phrase exists, complexity counts down from 1000000:
	 *  10000 x m bp,   range = 10000 -> 100000
	 *    100 x n tags, range =   100 -> 10000, as before
	 *
	 *  Boilerplate complexity:
	 *  Has been fine tuned to reduce number of clashes when inserting into TreeMap
	 *  Using a random number (less processing?/more random?)  Make it least
	 *  
	 *  Finite:
	 *  |-----------+-----------+------------------|
	 *  | Tags 0-99 |Words 0-99 | Random num 0-99  |
	 *  |-----------+-----------+------------------|
	 *  
	 *  Infinite:
	 *  |----------|   |------------+-----------|------------------+
	 *  | 1000000  | - | Words 0-99 | Tags 0-99 | Random num 0-99  |
	 *  |----------|   |------------+-----------|------------------+
	 *  Range at 1000, random component is 1-100 * 10 - 100 -1000 with 1-99 being
	 *  the count element 
	 */
	private static Random rn = new Random();
	private static final int      RANGE = 1000; // means full range will be up to 1 billion
	private static final int FULL_RANGE = RANGE*RANGE*RANGE;
	private static final int  MID_RANGE = RANGE*RANGE;
	private static final int  LOW_RANGE = RANGE;
	
	public int complexity() {
		boolean infinite = false;
		int  boilerplate = 0,
		       namedTags = 0,
		             rnd = rn.nextInt( RANGE/10 ); // word count component
		
		for (Patternette t : this) {
			boilerplate += t.prefix().size();
			if (t.isPhrased()) //attributes().get( phrase ).equals( phrase ))
				infinite = true;
			else if (!t.name().equals( "" ))
				namedTags ++; // count named tags
		}
		// limited to 100bp == 1tag, or phrase + 100 100tags/bp
		return infinite ?
				FULL_RANGE - MID_RANGE*boilerplate - LOW_RANGE*namedTags + rnd
				: MID_RANGE*namedTags + LOW_RANGE*boilerplate + rnd*10;
	}
	
	static private       boolean debug = false;
	static public        boolean debug() { return debug; }
	static public        void    debug( boolean b ) { debug = b; }
	
	/* *************************************************************************
	 * matchValues() coming soon...
	 */
	private Attributes matched = null; // lazy creation
	private void matched( Attribute a ) {
		if (null == matched) matched = new Attributes();
		matched.add( a ); // remember what it was matched with!
	}
	private void matched( Where w ) {
		// ?? addAll( w.toAttributes()); ??
		if (null == matched) matched = new Attributes();
		matched( new Attribute( Where.LOCATOR,  w.locator ().toString()));
		matched( new Attribute( Where.LOCATION, w.location().toString()));
	}
	
	private String doNumeric( ListIterator<String> ui ) {
		String toString = Number.getNumber( ui ).toString();
		return toString.equals( Number.NotANumber ) ? null : toString;
	}
	private String doExpr( ListIterator<String> ui ) {
		Strings rep = Expression.getExpr( ui, new Strings() );
		return rep == null ? "" : rep.toString();

	}
	private String doList( ListIterator<Patternette> patti,
	                       ListIterator<String>      utti  ) 
	{
		String  word = utti.next();
		Strings words = new Strings(),
		        vals  = new Strings();
		if (patti.hasNext()) {
			
			// peek at terminator
			String terminator = patti.next().prefix().get( 0 );
			patti.previous();
			//audit.log( "Terminator is "+ terminator );
			
			words.add( word );  // add at least one val!
			if (utti.hasNext()) word = utti.next();
			
			while ( !word.equals( terminator )) {
				
				if ( word.equals( "and" )) {
					vals.add( words.toString() );
					words = new Strings();
				} else
					words.add( word );
				
				if (utti.hasNext())
					word = utti.next();
				else
					return null;
			}
			utti.previous(); // replace terminator!
		} else { // read to end {
			words.add( word ); // at least one!
			while (utti.hasNext()) {
				word = utti.next();
				if ( word.equals( "and" )) {
					vals.add( words.toString() );
					words = new Strings();
				} else
					words.add( word );
		}	}
		if (words.size() > 0) vals.add( words.toString());
		if (vals.size() <= 1) return null;
		// remove "and"s from AND-LIST, replacing with ","
		//audit.log( "P::doList(): "+ vals.toString("", " and ", ""));
		return vals.toString("", " and ", "");
	}
	private String getNextBoilerplate( Patternette t, ListIterator<Patternette> ti ) {
		String term = null;
		if (t.postfix().size() != 0)
			term = t.postfix().get( 0 );
		else if (ti.hasNext()) {
			// next prefix as array is...
			Strings arr = ti.next().prefix();
			// ...first token of which is the terminator
			term = (arr == null || arr.size() == 0) ? null : arr.get( 0 );
			ti.previous();
		}
		return term;
	}
	private String getVal( Patternette t, ListIterator<Patternette> ti, ListIterator<String> ui, boolean whr ) {
		String u = "unseta";
		if (ui.hasNext()) u = ui.next();
		Strings vals = new Strings( u );
		if (t.isPhrased() || (ui.hasNext() &&  Reply.andConjunction().equals( u ))) {
			Where where = null;
			String term = getNextBoilerplate( t, ti );
			//audit.audit( "phrased, looking for terminator "+ term );
			// here: "... one AND two AND three" => "one+two+three"
			if (term == null) {  // just read to the end
				while (ui.hasNext()) {
					u = ui.next();
					if (whr && null != (where = Where.getWhere( u, null, ui ))) // null => read to end
						matched( where );
					else
						vals.add( u );
				}
			} else {
				while (ui.hasNext()) {
					u = ui.next();
					if (whr && null != (where = Where.getWhere( u, term, ui ))) // null => read to end
						matched( where );
					else if (term.equals( u )) {
						ui.previous();
						break;
					} else
						vals.add( u );
		}	}	}
		return vals.toString();
	}
	static private int notMatched = 0;
	static public String notMatched() {
		return  notMatched ==  0 ? "matched" :
				notMatched ==  1 ? "precheck 1" :
				notMatched ==  2 ? "precheck 2" :
				notMatched == 11 ? "prefixa" :
				notMatched == 12 ? "prefixb" :
				notMatched == 13 ? "invalid expr" :
				notMatched == 14 ? "and-list runs into hotspot" :
				notMatched == 15 ? "not numeric" :
				notMatched == 16 ? "invalid flags" :
				notMatched == 17 ? "unterminated and-list" :
				notMatched == 18 ? "postfixa" :
				notMatched == 19 ? "postfixb" :
				notMatched == 21 ? "more pattern" :
				notMatched == 22 ? "more utterance" : ("unknown:"+ notMatched);
	}
	private ListIterator<String> matchBoilerplate( Strings bp, ListIterator<String> ui, boolean spatial ) {
		String term, uttered;
		Iterator<String> bpi = bp.iterator();
		while ( bpi.hasNext() && ui.hasNext())
			if (!(term = bpi.next()).equalsIgnoreCase( uttered = ui.next() )) {
				Where w;
				if (spatial && null != (w = Where.getWhere( uttered, term, ui )))
					matched( w );
				else {
					notMatched = 11;
					return null; // string mismatch
				}
				if (ui.hasNext()) uttered = ui.next(); // getWhere doen't consume terminator
			}
		notMatched = 12;
		return bpi.hasNext() ? null : ui;
	}
	
	/* TODO: Proposal: that a singular tag (i.e. non-PHRASE) can match with a known string
	 * i.e. an object id: e.g. theOldMan, thePub, fishAndChips camelised
	 */
	public  Attributes matchValues( Strings utterance, boolean spatial ) {
		
		notMatched = 0;
		matched = null;
		
		// First, a sanity check
		if (size() == 0) {
			notMatched = 1;
			return null; // manual/vocal Tags creation can produce null Tags objects
                         // see "first reply well fancy that" in Enguage sanity test.
		}
		
		/* We need to be able to extract:
		 * NAME="value"				... <NAME/>
		 * NAME="some value"		... <NAME phrased="phrased"/>
		 * NAME="68"                ... <NAME numeric='numeric'/>
		 * ???NAME="an/array/or/list"	... <NAME array="array"/>
		 * ???NAME="value one/value two/value three" <NAME phrased="phrased" array="array"/>
		 */
		ListIterator<Patternette> patti = listIterator();           // [ 'this    is    a   <test/>' ]
		ListIterator<String>       utti = utterance.listIterator(); // [ "this", "is", "a", "test"   ]
		
		Patternette next = null;
		while (patti.hasNext() && utti.hasNext()) {
			
			Patternette t = (next != null) ? next : patti.next();
			next = null;
			
			if (null == (utti = matchBoilerplate( t.prefix(), utti, spatial ))) { // ...match prefix
				//notMatched set within matchBoilerplate()
				return null;
				
			} else if (!utti.hasNext() && t.name().equals( "" )) { // end of array on null (end?) tag...
				if (patti.hasNext()) next = patti.next();
				
			} else if (utti.hasNext() &&  t.name().equals( "" )) { // check 4 trailing where
				if (spatial) {
					Where w;
					String uttered = utti.next();
					if (null != (w = Where.getWhere( uttered, null, utti )))
						matched( w );
				}
			} else if (utti.hasNext() && !t.name().equals( "" )) { // do these loaded match?
				
				String val = null;
				
				if (t.isNumeric()) {
					
					if (null == (val = doNumeric( utti ))) {
						notMatched = 15;
						return null;
					}
					
				} else if (t.isList()) {
					
					if (null == (val = doList( patti, utti ))) {
						notMatched = 17;
						return null;
					}
					
				} else if (t.isExpr()) {
					
					if (null == (val = doExpr( utti ))) {
						notMatched = 13;
						return null;
					}
					
				} else if (t.invalid( utti )) {
					notMatched = 16;
					return null;
					
				} else
					val = getVal( t, patti, utti, spatial );
					
				// ...add value
				//if (null == matched) matched = new Attributes();
				//matched.add( t.matchedAttr( val )); // remember what it was matched with!
				matched( t.matchedAttr( val ));
				
				if (null == (utti = matchBoilerplate( t.postfix(), utti, spatial ))) {
					notMatched += 7; // 18 or 19!
					return null;
		}	}	}
		
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
			str += ( " "+t.prefix().toString()+" <"+t.name() +" "
			//+ t.attributes().toString()
					+"/> "+t.postfix().toString());
		}
		return str;
	}
	
	// --- test code...
	static public void printTagsAndValues( Pattern interpretant, String phrase, Attributes expected ) {
		audit.in( "printTagsAndValues", "ta="+ interpretant.toString() +", phr="+ phrase +", expected="+ 
				(expected == null ? "":expected.toString()) );
		Attributes values = interpretant.matchValues( new Strings( phrase ), true );
		
		if (values == null)
			audit.log( "no match" );
		else {
			// de-reference values...
			String vals = values.toString();
			if (null == expected)
				audit.log( "values => ["+ vals +"]" );
			else if (values != null && values.matches( expected ))
				audit.log( "PASSED => ["+ vals +"]" );
			else {
				audit.log( "FAILED: expecting: "+ expected +", got: "+ vals );
				audit.log( "      :       got: "+ vals );
		}	}
		audit.out();
	}
//	private static void toPatternTest( String utt  ) {
//		toPatternTest( utt, utt ); // check it against itself!
//	}
//	private static void toPatternTest( String utt, String answer ) {
//		String patt = toPattern( utt ).toString();
//		if (answer != null && !answer.equals( patt ))
//			audit.FATAL( "answer '"+ patt +"' doesn't equal expected: '" + answer +"'" );
//		audit.log( ">"+ utt +"< to pattern is >"+ patt +"<" );
//	}
	private void newTest( String utterance ) {
		audit.in( "newTest", utterance );
		Attributes as;
		audit.log( "Utterance: "+ utterance );
		if (null != (as = matchValues( new Strings( utterance ), true )))
			audit.log( "  matches: "+ as.toString());
		else
			audit.log( "notMatched (="+ notMatched +")" );
		audit.out();
	}
	static public void main(String args[]) {
//		Audit.allOn();
//		audit.tracing = true;
//		debug( true );
		
//		Pattern t = new Pattern();
//		t.add( new Patternette( "what is ", "X" ).numericIs() );
//		printTagsAndValues( t, "what is 1 + 2", new Attributes().add( "X", "1 + 2" ));
//
//		printTagsAndValues( new Pattern( "i need phrase variable need" ),
//				"I need coffee", 
//				new Attributes()
//					.add( "need",     "coffee" )
//		);
//		printTagsAndValues( new Pattern( "i need numeric variable quantity variable unit of phrase variable need" ),
//				"I need a cup of coffee", 
//				new Attributes()
//					.add( "quantity", "1" )
//					.add( "unit",     "cup" )
//					.add( "need",     "coffee" )
//		);
		
//		//toPattern() tests...
//		toPatternTest( "the factorial of n" );
//		toPatternTest( "the factorial of n blah" );
//		toPatternTest( "the factorial of variable variable", "the factorial of variable" );
//		toPatternTest( "the factorial of variable n", "the factorial of N" );
//		toPatternTest( "the factorial of variable n blah", "the factorial of N blah" );
//
//		toPatternTest( "the factorial of phrase" );
//		toPatternTest( "the factorial of phrase n" );
//		toPatternTest( "the factorial of phrase variable variable","the factorial of phrase variable" );
//		toPatternTest( "the factorial of phrase variable n", "the factorial of PHRASE-N" );
//		toPatternTest( "the factorial of phrase variable n blah", "the factorial of PHRASE-N blah" );
//
//		toPatternTest( "the factorial of numeric" );
//		toPatternTest( "the factorial of numeric n" );
//		toPatternTest( "the factorial of numeric variable variable", "the factorial of numeric variable" );
//		toPatternTest( "the factorial of numeric variable n", "the factorial of NUMERIC-N" );
//		toPatternTest( "the factorial of numeric variable n blah", "the factorial of NUMERIC-N blah" );
//
//		toPatternTest( "the sum of and list variable params is blah", "the sum of PARAMS-AND-LIST is blah" );

//		audit.log( "First: martin is alive" );
//		Audit.incr();
//		printTagsAndValues( new Pattern(
//				"first phrase variable x" ),
//				"first variable state exists in variable entity is list", 
//				new Attributes()
//					.add( "x",  "variable state exists in variable entity is list" )
//		);
//		printTagsAndValues( new Pattern(
//				"phrase variable object exists in variable subject variable list list" ),
//				"variable state exists in variable entity is list", 
//				new Attributes()
//					.add( "object",  "variable state" )
//					.add( "subject", "variable entity" )
//					.add( "list",    "is" )
//		);
//		Audit.decr();
		
//		audit.log( "Second: i am alive" );
//		Audit.incr();
//		printTagsAndValues( new Pattern(
//				"phrase variable object exists in variable subject variable list list" ),
//				"first variable state exists in i am list", 
//				new Attributes()
//				.add( "object",  "variable state" )
//				.add( "subject", "i" )
//				.add( "list",    "am" )
//				);
//		printTagsAndValues( new Pattern(
//				"first phrase variable x" ),
//				"first variable state exists in i am list", 
//				new Attributes()
//					.add( "object",  "variable state" )
//					.add( "subject", "i" )
//					.add( "list",    "am" )
//		);
//		Audit.decr();
		
//		Audit.allOn();
//		audit.tracing = false;
//		debug( debug );
//		Audit.allOff();
//		Audit.runtimeDebug = false;
//		Audit.traceAll( false );		
		
		audit.LOG(
			"pattern: "+
			toPattern(
				"variable nm needs numeric variable quantity units of phrase variable object"
		)	);

		Where.doLocators("at/from/in");
		Pattern p = new Pattern( "i need PHRASE-OBJECTS" );
		audit.log( "sign is: "+ p.toXml());
		
		p.newTest( "i need milk" );
		p.newTest( "i need milk from the dairy aisle" );
		p.newTest( "i from the dairy aisle need milk" );
		p.newTest( "from the dairy aisle i need milk" );
		p.newTest( "i need sliced bread from the bakery" );
		
		Audit.allOn();
		//Audit.traceAll( true );		
		
		p = new Pattern( "help" );
		audit.log( "sign is now: "+ p.toXml());
		p.newTest( "at the pub i am meeting my brother" );
		
		p.newTest( "doesnt match at all" );

		// -- expr
		p = new Pattern( "the FUNCITON of LIST-FNAME is EXPR-VAL" );
		audit.log( "sign is: "+ p.toXml());
		
		p.newTest( "the sum of x is x plus y" );

		// pronouns test...
		p = new Pattern( "they are" );
		audit.log( "sign is: "+ p.toXml());
		p.newTest( "they are from sainsburys" );

		audit.log( "PASSED" );
}	}
