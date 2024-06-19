package org.enguage.sign.pattern;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;

import org.enguage.sign.Config;
import org.enguage.sign.Sign;
import org.enguage.sign.symbol.Utterance;
import org.enguage.sign.symbol.config.Englishisms;
import org.enguage.sign.symbol.config.Plural;
import org.enguage.sign.symbol.number.Number;
import org.enguage.sign.symbol.pronoun.Gendered;
import org.enguage.sign.symbol.pronoun.Pronoun;
import org.enguage.sign.symbol.where.Where;
import org.enguage.util.algorithm.Expression;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Attributes;
import org.enguage.util.audit.Audit;
import org.enguage.util.audit.Indentation;
import org.enguage.util.strings.Strings;

public class Pattern extends ArrayList<Frag> {
	
	static final         long  serialVersionUID = 0;
	private static       Audit            audit = new Audit( "Pattern" );

	/* ------------------------------------------------------------------------
	 * These are never used, but overriden to stop SonarLint complaining  ;-)
	 */
	@Override
	public boolean equals( Object o1 ) {return false;}
	@Override
	public int hashCode() {return 0;}
	// ------------------------------------------------------------------------
	
	private static final Locale  locale         = Locale.getDefault();
	private static final String  VARIABLE       = "variable";
	public  static final String  QUOTED         = "quoted";
	public  static final String  LIST           = "list";
	public  static final String  QUOTED_PREFIX  = QUOTED.toUpperCase( locale ) + "-";
	public  static final String  GROUPED        = "grouped";
	public  static final String  GROUPED_PREFIX = GROUPED.toUpperCase( locale ) + "-";
	public  static final String  UNGROUPED      = "ungrouped";
	public  static final String  UNGRPED_PREFIX = UNGROUPED.toUpperCase( locale ) + "-";
	public  static final String  PHRASE         = "phrase";
	public  static final String  PRHASE_PREFIX  = PHRASE.toUpperCase( locale ) + "-";
	public  static final String  FIRST          = "first-of";
	public  static final String  FIRST_PREFIX   = FIRST.toUpperCase( locale ) + "-";
	public  static final String  REST           = "rest-of";
	public  static final String  REST_PREFIX    = REST.toUpperCase( locale ) + "-";
	public  static final String  NUMERIC        = "numeric";
	public  static final String  NUMERIC_PREFIX = NUMERIC.toUpperCase( locale ) + "-";
	public  static final String  EXPRESSION     = "expression";
	public  static final String  EXPR           = "expr";
	public  static final String  EXPR_PREFIX    = EXPR.toUpperCase( locale ) + "-";
	public  static final String  PLURAL         = Plural.NAME;
	public  static final String  PLURAL_PREFIX  = PLURAL.toUpperCase( locale ) + "-";
	public  static final String  SINSIGN        = "said";
	public  static final String  SINSIGN_PREFIX = SINSIGN.toUpperCase( locale ) + "-";
	public  static final String  EXTERNAL       = "ext";
	public  static final String  EXTERN_PREFIX  = EXTERNAL.toUpperCase( locale ) + "-";
	
	/*
	 * c'tors 
	 */
	private static String nextName( String switchName, Iterator<String> wi ) {
		if (wi.hasNext())
			return wi.next();
		else
			audit.error( "ctor: "+ switchName +" variable, missing name." );
		return "";
	}
	private static String ignoreSubsequentSwitches( String sw, ListIterator<String> wi) {
		while ((sw.equals( PHRASE  ) ||
		        sw.equals( PLURAL  ) ||
		        sw.equals( QUOTED  ) ||
		        sw.equals( EXPR    ) ||
		        sw.equals( SINSIGN ) ||
		        sw.equals( NUMERIC )   )
			 && wi.hasNext())
		{
			audit.error( "ctor: mutually exclusive modifiers" );
			sw = wi.next();
		}
		return sw;
	}
	private static String doApostrophe( String sw, Frag t ) {
		Strings apostrophes = new Strings( sw, Englishisms.APOSTROPHE.charAt( 0 ));
		if (apostrophes.size() == 2 && apostrophes.get( 1 ).equals( "s" )) {
			sw = apostrophes.get( 0 );
			t.apostrophedIs( apostrophes.get( 1 ));
		}
		return sw;
	}
	private static String getSwitchesAndName( ListIterator<String> wi, Frag t ) {
		String sw = wi.next();
		// look for switches...
		if (sw.equals( PHRASE )) {
			t.phrasedIs();
			sw = nextName( PHRASE, wi );
			
		} else if (sw.equals( PLURAL )) {
			t.pluralIs();
			sw = nextName( PLURAL, wi );
			
		} else if (sw.equals( QUOTED )) {
			t.quotedIs();
			sw = nextName( QUOTED, wi );
			
		} else if (sw.equals( NUMERIC )) {
			t.numericIs();
			sw = nextName( NUMERIC, wi );
			
		} else if (sw.equals( EXPR )) {
			t.exprIs();
			sw = nextName( EXPR, wi );
			
		} else if (sw.equals( SINSIGN )) {
			t.signIs();
			sw = nextName( SINSIGN, wi );
			
		} else if (sw.equals( Config.andConjunction() )) {
			if (wi.hasNext()) {
				sw = wi.next();
				if (sw.equals( LIST )) {

					t.listIs();
					sw = nextName( "AND-LIST", wi );
					
				} else
					audit.error( "ctor: AND-LIST? 'LIST' missing" );
			} else
				audit.error( "ctor: AND terminates variable" );
		}
		
		sw = ignoreSubsequentSwitches( sw, wi );
		sw = doApostrophe( sw, t );
		
		return sw;
	}
	
	
	public Pattern() { super(); }
	public Pattern( Strings words ) {
		
		// "if X do Y" -> [ <x prefix=["if"]/>, <y prefix=["do"] postfix="."/> ]
		Frag t = new Frag();
		for ( String w : words ) {
			
			if (w.equals( "an" )) w = "a";
			
			if (Strings.isUCwHyphUs( w )) {
				Strings arr = new Strings( w.toLowerCase( locale ), '-' );
				
				String sw = getSwitchesAndName( arr.listIterator(), t );
				
				add( t.name( sw ));
				t = new Frag();
				
			} else
				t.prefixAppend( w );
		}
		if (!t.isEmpty()) add( t );
	}
	
	// Manual Autopoiesis... needs to deal with:
	// if variable x do phrase variable y => if X do PHRASE-Y
	// i need numeric variable quantity variable units of phrase variable needs.
	// => i need NUMERIC-QUANTITY UNIT of PHRASE-NEEDS
	public Pattern( String str ) { this( toPattern( new Strings( str ))); }
	
	/*
	 * This is a constructor to split a frag into two...
	 */
	public Pattern( Frag frag, String word, String type ) {
		if (frag.prefix().contains( word )) {

			// split prefix...
			Frag f = new Frag( frag.prefix().copyBefore( word ), word );
			if (type.equals(PHRASE))
				f.phrasedIs();
			else if (type.equals(NUMERIC))
				f.numericIs();
			
			add( f );
			
			Strings nextPrefix = frag.prefix().copyAfter( word );
			if (!nextPrefix.isEmpty())
				add( new Frag( frag ).prefix( nextPrefix ));
			
		} else if (frag.postfix().contains( word )) {
			// split postfix - this is not tested
			add( new Frag( frag ).postfix( frag.postfix().copyBefore( word )));
			Strings nextPrefix = frag.prefix().copyAfter( word );
			if (!nextPrefix.isEmpty())
				add( new Frag( frag ).prefix( nextPrefix ));
			
		} else // 
			add( frag );
	}
	public Pattern( Frag frag, String word ) {
		if (frag.prefix().contains( word )) {
			add( new Frag( frag.prefix().copyBefore( word ), word ));
			add( new Frag( frag ).prefix( frag.prefix().copyAfter( word )));
			
		} else if (frag.postfix().contains( word )) {
			add( new Frag( frag ).postfix( frag.postfix().copyBefore( word )));
			add( new Frag( frag.postfix().copyAfter( word ), word ));
			
		} else
			add( frag );
	}
	public Pattern split( String word ) {
		Pattern p = new Pattern();
		for (Frag f : this)
			p.addAll( new Pattern( f, word ));
		return p;
	}
	public Pattern split( String word, String type ) {
		Pattern p = new Pattern();
		for (Frag f : this)
			p.addAll( new Pattern( f, word, type ));
		return p;
	}
	
	public Strings names() {
		Strings sa = new Strings();
		for (Frag f : this)
			sa.append( f.name() );
		return sa;
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
	 *  |-----------+-----------+
	 *  | Tags 0-99 |Words 0-99 |
	 *  |-----------+-----------+
	 *  
	 *  Infinite:
	 *  |----------|   |------------+-----------|
	 *  | 1000000  | - | Words 0-99 | Tags 0-99 |
	 *  |----------|   |------------+-----------|
	 *  Range at 1000, 1-99 count element 
	 */
	private static final int RANGE = 1000; // means full range will be up to 1 billion
	private static final int INFTY = RANGE*RANGE*RANGE;
	private static final int LARGE = RANGE*RANGE;
	private static final int SMALL = RANGE;
	
	public int cplex( boolean ud ) {
		boolean infinite = false;
		int cons = ud?1:0; // we want to keep user defined signs ahead of written ones
		int vars = 0;      // this might mean when a thought is saved it must get loaded/unloaded
		                   // like other written repertoires.
		for (Frag t : this) {
			cons += t.nconsts();
			if (t.isPhrased())
				infinite = true;
			else if (!t.name().equals( "" ))
				vars++; // count non-phrase named tags as words
		}
		return (infinite ? INFTY - LARGE*cons - SMALL*vars
				         : LARGE*vars - SMALL*cons);
	}
	
	// initialise with values from Pronoun, provide functions to update from pronoun...
	private static  String subjGroup = Pronoun.pronoun(Pronoun.SUBJECTIVE, Pronoun.PLURAL, Gendered.PERSONAL); // i.e. local copy of "they"
	public  static  void   subjGroup( String pl ) { subjGroup = pl;}
	private static  String objGroup = Pronoun.pronoun(Pronoun.OBJECTIVE, Pronoun.PLURAL, Gendered.PERSONAL); // i.e. local copy of "they"
	public  static  void   objGroup( String pl ) { objGroup = pl;}
	private static  String possGroup = Pronoun.pronoun(Pronoun.SUBJECTIVE, Pronoun.PLURAL, Gendered.PERSONAL); // i.e. local copy of "they"
	public  static  void   possGroup( String pl) { possGroup = pl;}
	private static  String subjOther = Pronoun.pronoun(Pronoun.SUBJECTIVE, Pronoun.PLURAL, Gendered.NEUTRAL); // i.e. local copy of "they"
	public  static  void   subjOther( String pl ) { subjOther = pl;}
	private static  String objOther = Pronoun.pronoun(Pronoun.OBJECTIVE, Pronoun.PLURAL, Gendered.NEUTRAL); // i.e. local copy of "they"
	public  static  void   objOther( String pl ) { objOther = pl;}
	private static  String possOther = Pronoun.pronoun(Pronoun.SUBJECTIVE, Pronoun.PLURAL, Gendered.NEUTRAL); // i.e. local copy of "they"
	public  static  void   possOther( String pl) { possOther = pl;}
	
	private static  boolean debug = false;
	public  static  boolean debug() { return debug; }
	public  static  void    debug( boolean b ) { debug = b; }
	
	/* *************************************************************************
	 * matchValues() coming soon...
	 */
	private Attributes matched = null; // lazy creation
	private void matched( Attribute a ) {
		if (null == matched) matched = new Attributes();
		matched.add( a ); // remember what it was matched with!
	}
	private void matched( Where w ) {
		if (null != w) {
			matched( new Attribute( Where.LOCTR, w.locatorAsString(  0 )));
			matched( new Attribute( Where.LOCTN, w.locationAsString( 0 )));
	}	}
	
	private int notMatched = 0;
	private String term = "";
	private String word = "";
	
	public  String notMatched() {
		switch (notMatched) {
			case  0: return "matched";
			case  1: return "precheck 1";
			case  2: return "precheck 2";
			case 11: return term +" != "+ word;
			case 12: return "... "+term +" != "+ word +" ...";
			case 13: return "invalid expr";
			case 14: return "and-list runs into hotspot";
			case 15: return "not numeric";
			case 16: return "invalid flags";
			case 17: return "unterminated and-list";
			case 18: return "... "+term +" != "+ word +"..";
			case 19: return "... "+term +" != "+ word +".";
			case 20: return "trailing hotspot value missing";
			case 21: return "more pattern";
			case 22: return "more utterance";
			case 23: return "missing apostrophe";
			default: return "unknown:"+ notMatched;
	}	}
	
	private String doNumeric( ListIterator<String> ui ) {
		String toString = new Number( ui ).toString();
		return toString.equals( Number.NOT_A_NUMBER ) ? null : toString;
	}
	private String doFunc( ListIterator<String> ui ) {
		Strings rep = Expression.getExprList( ui, new Strings() );
		return rep == null ? "" : rep.toString();

	}
	private String doList( ListIterator<Frag> patti,
	                       ListIterator<String>      utti  ) 
	{
		String  wd    = utti.next();
		Strings words = new Strings();
		Strings vals  = new Strings();
		if (patti.hasNext()) {
			
			// peek at terminator
			String terminator = patti.next().prefix().get( 0 );
			patti.previous();
			//audit.debug( "Terminator is "+ terminator )
			
			words.add( wd );  // add at least one val!
			if (utti.hasNext()) wd = utti.next();
			
			while ( !wd.equals( terminator )) {
				
				if ( wd.equals( "and" )) {
					vals.add( words.toString() );
					words = new Strings();
				} else
					words.add( wd );
				
				if (utti.hasNext())
					wd = utti.next();
				else
					return null;
			}
			utti.previous(); // replace terminator!
			
		} else { // read to end
			words.add( wd ); // at least one!
			while (utti.hasNext()) {
				wd = utti.next();
				if ( wd.equals( "and" )) {
					vals.add( words.toString() );
					words = new Strings();
				} else
					words.add( wd );
		}	}
		
		if (words.size() > 0) vals.add( words.toString());
		int sz = vals.size();
		if (sz == 0) return null;
		// deal with "they", "our", "we" etc as a list
		if (sz == 1) {
			String val = vals.get( 0 );
			if (   !val.equals( subjGroup )
				&& !val.equals(  objGroup )
				&& !val.equals( possGroup )
				&& !val.equals( subjOther )
				&& !val.equals(  objOther )
				&& !val.equals( possOther )) return null;
		}
		return vals.toString("", " and ", "");
	}
	
	private boolean matchBoilerplate(
			Strings bp, // ["one", "two", ...
			ListIterator<String> said, //head=>"one"=>"two"...
			boolean spatial )
	{
		Iterator<String> bpi = bp.iterator();
		while (bpi.hasNext() && said.hasNext()) {
			term = bpi.next();
			if (spatial)
				matched( Where.getWhere( said, term ) );
			
			if (!term.equalsIgnoreCase( word = said.next() )) {
				said.previous();
				notMatched = 11;
				return false; // string mismatch
		}	}
		
		notMatched = 12;
		return !bpi.hasNext();
	}
	private Strings getNextBoilerplate( Frag t, ListIterator<Frag> ti ) {
		Strings term = null;
		if (t.postfix().size() != 0)
			term = t.postfix();
		else if (ti.hasNext()) {
			term = ti.next().prefix();
			ti.previous();
		}
		return term;
	}
	private String getVariable(
			Frag t,
			ListIterator<Frag> ti,
			ListIterator<String> said,
			boolean spatial )
	{
		String u = "";
		if (said.hasNext()) u = said.next();
		Strings vals = new Strings( u );
		if (t.isPhrased() || t.isSign() ||
				// BUG: this is u.next is a conjection!
				(said.hasNext() && Config.andConjunction().equals( u )))
		{
			boolean done = false;
			Where where = null;
			Strings terms = getNextBoilerplate( t, ti ); // null if this is last tag
			String tm = terms==null || terms.isEmpty() ? null : terms.get( 0 );
			while (said.hasNext() && !done) {
				// term==null? => read to end
				if (spatial && null != (where = Where.getWhere( said, tm ))) {
					matched( where );
					done = true; // finding a where is the end of a variable...
				} else {
					u = said.next();
					if (tm != null && tm.equals( u )) {
						said.previous();
						done = true;
					} else
						vals.add( u );
		}	}	}
		String val = vals.toString();
		// ...again "l'eau"
		if (t.isApostrophed())
			val = val.endsWith( Englishisms.apostrophed() ) ? val.substring( 0, val.length()-2 ) : null;
		
		return val;
	}
	private String getValue(
			Frag t,
			ListIterator<Frag> patti,
			ListIterator<String> utti,
			boolean spatial)
	{
		String val = null;
		if (t.isNumeric()) {
			
			if (null == (val = doNumeric( utti )))
				notMatched = 15;
			
		} else if (t.isList()) {
			
			if (null == (val = doList( patti, utti )))
				notMatched = 17;
			
		} else if (t.isExpr()) {
			
			if (null == (val = doFunc( utti )))
				notMatched = 13;
			
		} else if (t.invalid( utti )) {
			notMatched = 16;
			
		} else if (null == (val = getVariable( t, patti, utti, spatial ))) {
			notMatched = 23;
		}
		return val;
	}
	
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
		ListIterator<Frag> patti = listIterator();           // [ 'this    is    a   <test/>' ]
		ListIterator<String>       utti = utterance.listIterator(); // [ "this", "is", "a", "test"   ]
		
		Frag next = null;
		while (patti.hasNext() && utti.hasNext()) {
			
			Frag t = (next != null) ? next : patti.next();
			next = null;
			
			if (!matchBoilerplate( t.prefix(), utti, spatial )) { // ...match prefix
				//notMatched set within matchBoilerplate()
				return null;
				
			} else if (!t.named()) { // last tag - no postfix?
				
				if (utti.hasNext()) { // end of array on null (end?) tag...
					
					if (spatial)
						matched( Where.getWhere( utti, null ));
					
				} else { // check 4 trailing where
					if (patti.hasNext()) next = patti.next();
				}
				
			} else if (!utti.hasNext() && t.named()) { // "do i need" == "do i need THIS"
				
				notMatched = 20;
				return null;
				
			} else { // do these loaded match?
				
				String val = getValue( t, patti, utti, spatial );
				if (val == null) return null;
				
				Attribute a = t.matchedAttr( val );
				matched( a );
			}
			
			if (!matchBoilerplate( t.postfix(), utti, spatial )) {
				notMatched += 7; // 18 or 19!
				return null;
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
	/** 
	 * Print Routines
	 * toString()- to Pattern() toXml()...
	 */
	public String toFilename() {
		boolean hadPostfix = false;
		StringBuilder sb = new StringBuilder();
		Iterator<Frag> ti = iterator();
		while (ti.hasNext()) {
			Frag f = ti.next();
			String tmp = f.toFilename();
			if (!tmp.equals("")) {
				sb.append( (hadPostfix ? "_" : "") + tmp );
				hadPostfix = !f.postfix().isEmpty();
		}	}
		return sb.toString();
	}
	
	// Otherwise, content prints...
	
	// with postfix boilerplate:
	// typically { [ ">>>", "name1" ], [ "/", "name2" ], [ "/", "name3" ], [ "<<<", "" ] }.
	// could be  { [ ">>>", "name1", "" ], [ "/", "name2", "" ], [ "/", "name3", "<<<" ] }.
	public String toXml() { return toXml( new Indentation( "   " )); }
	public String toXml( Indentation indent ) {
		String oldName = "";
		StringBuilder str  = new StringBuilder();
		str.append( "\n"+indent );
		Iterator<Frag> ti = iterator();
		while (ti.hasNext()) {
			Frag t = ti.next();
			str.append( (t.name().equals( oldName ) ? "\n"+indent : "") + t.toXml( indent ));
			oldName = t.name();
		}
		return str.toString();
	}
	
	// N.B. not quite right, what about "l'eau" - the water
	// want to move to u.c. but preserve l.c. apostrophe...
	private static  String toUpperCase( String word ) {
		// "martin's" => "MARTIN's"
		Strings uppers = new Strings( word, Englishisms.APOSTROPHE.charAt( 0 ));
		uppers.set( 0, uppers.get( 0 ).toUpperCase( locale ));
		return uppers.toString( Strings.CONCAT );
	}
	
	public  static  Strings toPattern( Strings in ) {
		// my name is variable name => my name is NAME
		Strings out = new Strings();
		Iterator<String> wi = in.iterator();
		while ( wi.hasNext() ) {
			String word = wi.next();
			
			if (word.equals( "an" )) word = "a"; // English-ism!
			
			if (word.equals( VARIABLE )) {
				if (wi.hasNext() && null != (word = wi.next()) && !word.equals( VARIABLE ))
					out.append( toUpperCase( word ));
				else // variable. OR variable variable
					out.append( VARIABLE );
				
			} else if (word.equals( NUMERIC )) {
				if (wi.hasNext() && null != (word = wi.next())) {
					if (word.equals( VARIABLE )) {
						if (wi.hasNext() && (null != (word = wi.next() )) && !word.equals( VARIABLE ))
							out.append( NUMERIC_PREFIX + word.toUpperCase( locale ));
						else // numeric variable. or numeric variable variable
							out.append( NUMERIC ).append( VARIABLE );
					} else // numeric blah
						out.append( NUMERIC ).append( word );		
				} else // numeric.
					out.append( NUMERIC );
				
			} else if (word.equals( EXPRESSION )) {
				if (wi.hasNext() && null != (word = wi.next())) {
					if (word.equals( VARIABLE )) {
						if (wi.hasNext() && (null != (word = wi.next() )) && !word.equals( VARIABLE ))
							out.append( EXPR_PREFIX + word.toUpperCase( locale ));
						else // numeric variable. or numeric variable variable
							out.append( EXPRESSION ).append( VARIABLE );
					} else // numeric blah
						out.append( EXPRESSION ).append( word );		
				}else // numeric.
					out.append( EXPRESSION );
				
			} else if (word.equals( PHRASE )) {
				if (wi.hasNext() && null != (word = wi.next())) {
					if (word.equals( VARIABLE )) {
						if (wi.hasNext() && null != (word = wi.next()) && !word.equals( VARIABLE ))
							out.append( PRHASE_PREFIX + word.toUpperCase( locale ));
						else // phrase variable. OR phrase variable variable
							out.append( PHRASE ).append( word );
					} else // phrase blah
						out.append( PHRASE ).append( word );
				} else // phrase.
					out.append( PHRASE );
			
			} else if (word.equals( QUOTED )) {
				if (wi.hasNext() && null != (word = wi.next())) {
					if (word.equals( VARIABLE )) {
						if (wi.hasNext() && null != (word = wi.next()) && !word.equals( VARIABLE ))
							out.append( QUOTED_PREFIX + word.toUpperCase( locale ));
						else // quoted variable. OR phrase variable variable
							out.append( QUOTED ).append( word );
					} else // quoted blah
						out.append( QUOTED ).append( word );
				} else // quoted.
					out.append( QUOTED );
			
			} else if (word.equals( "and" )) {
				if (wi.hasNext() && null != (word = wi.next()) && word.equals( "list" )) {
					if (wi.hasNext() && null != (word = wi.next()) && word.equals( "variable" )) {
						if (wi.hasNext() && null != (word = wi.next()) && !word.equals( "variable" ))
							out.append( word.toUpperCase( locale )+"-AND-LIST" );
						else // and list variable variable
							out.append( "and" ).append( "list" ).append( "variable" );
					} else // and list blah
						out.append( "and" ).append( "list" ).append( word );
				} else // so we can't have just VARIABLE, ok...
					out.append( "and" ).append( word );

			// ... "why sentence because reason sentence" ???
// SAID IS NOT YET articulated!
//			else if (word.equals( "sign" 
//				if (wi.hasNext() && null != (word = wi.next()) && word.equals( "list" ))
//					if (wi.hasNext() && null != (word = wi.next()) && word.equals( "variable" ))
//						if (wi.hasNext() && null != (word = wi.next()) && !word.equals( "variable" ))
//							out.append( word.toUpperCase( locale )+"-AND-LIST" );
//						else // and list variable variable
//							out.append( "and" ).append( "list" ).append( "variable" );
//					else // and list blah
//						out.append( "and" ).append( "list" ).append( word );						
//				else // so we can't have just VARIABLE, ok...
//					out.append( "and" ).append( word );						

			} else // blah
				out.append( word );
		}
		return out;
	}
	
	@Override
	public String toString() {
		String tmp;

		boolean first = true;
		StringBuilder str = new StringBuilder();
		Iterator<Frag> ti = iterator();
		
		while (ti.hasNext()) {
		
			if (first)
				first = false;
			else
				str.append( " " );
			
			tmp = ti.next().toString();
			if (!tmp.equals(""))
				str.append( tmp );
		}
		return str.toString();
	}
	public String toText() {
		StringBuilder str = new StringBuilder();
		Iterator<Frag> ti = iterator();
		while (ti.hasNext()) {
			str.append( ti.next().toText());
			if (ti.hasNext()) str.append( " " );
		}
		return str.toString();
	}
	public String toLine() {
		StringBuilder str = new StringBuilder();
		Iterator<Frag> ti = iterator();
		while (ti.hasNext()) {
			Frag t = ti.next();
			str.append ( " "+t.prefix()+" <"+t.name() +" "
			//+ t.attributes().toString()
					+"/> "+t.postfix());
		}
		return str.toString();
	}
	
	// --- test code...
	public  static  void printTagsAndValues( Pattern interpretant, String phrase, Attributes expected ) {
		audit.in( "printTagsAndValues", "ta="+ interpretant.toString() +", phr="+ phrase +", expected="+ 
				(expected == null ? "":expected.toString()) );
		Attributes values = interpretant.matchValues( new Strings( phrase ), true );
		
		if (values == null)
			audit.debug( "no match" );
		else {
			// de-reference values...
			String vals = values.toString();
			if (null == expected)
				audit.debug( "values => ["+ vals +"]" );
			else if (values.matches( expected ))
				audit.debug( "PASSED => ["+ vals +"]" );
			else {
				audit.debug( "FAILED: expecting: "+ expected +", got: "+ vals );
				audit.debug( "      :       got: "+ vals );
		}	}
		audit.out();
	}

	/*
	 * Test code...
	 */
	private static  void matchTest( String pref, String varibl, String concept, String utterance ) {
		audit.in( "matchTest", utterance );
		Attributes as;
		Utterance u = new Utterance( new Strings( utterance ));
		audit.debug( "Utterance: "+ utterance );
		
		Sign s = new Sign( new Frag( pref, varibl ));
		s.concept( concept );
		audit.debug( "     Sign: "+ s.toXml(0, -1) );
		
		if (null != (as = u.match( s )))
			Audit.passed( "  matches: "+ as.toString());
		else
			audit.debug( "notMatched ("+ s.pattern().notMatched() +")" );
		audit.out();
	}
	private static  void complexityTest( String str ) {
		Pattern patt = new Pattern( toPattern( new Strings( str )));
		Audit.log( "pattern: "+ patt );
		Audit.log( " cmplxy: "+ patt.cplex( true ) );

	}
	public  static  void main(String[] args) {

		complexityTest(	"i am legend" );
		complexityTest(	"variable nm needs numeric variable quantity units of phrase variable object" );
		complexityTest(	"spatially something can be phrase variable locator" );
		complexityTest(	"is variable var not set to phrase variable value" );
		complexityTest(	"i am not variable action phrase variable value" );
		complexityTest(	"i am     variable action phrase variable value" );
		complexityTest(	"set value of variable x to phrase variable y" );
		complexityTest(	"set variable attribute of variable x to phrase variable y" );
		
		Where.doLocators("at/from/in");
		Where.addConcept( "need+needs" );
		Pattern p = new Pattern( "i need PHRASE-THESE" );
		audit.debug( "pattern is: "+ p.toXml());
		
		Audit.on();
		matchTest(
				"i need",
				PRHASE_PREFIX + "THESE",
				"need+needs",
				"i need milk" );
		matchTest(
				"i need",
				PRHASE_PREFIX + "THESE",
				"need+needs",
				"i need milk from the dairy aisle" );
		matchTest(
				"i need",
				PRHASE_PREFIX + "THESE",
				"need+needs",
				"i from the dairy aisle need milk" );
		matchTest(
				"i need",
				PRHASE_PREFIX + "THESE",
				"need+needs",
				"from the dairy aisle i need milk" );
		Audit.off();
		Audit.PASSED();
}	}
