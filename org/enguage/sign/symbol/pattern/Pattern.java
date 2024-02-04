package org.enguage.sign.symbol.pattern;

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
	static final         long serialVersionUID = 0;
	private static        Audit          audit = new Audit( "Pattern" );

	private static final Locale  locale        = Locale.getDefault();
	private static final String  variable      = "variable";
	public  static final String  quoted        = "quoted";
	public  static final String  list          = "list";
	public  static final String  quotedPrefix  = quoted.toUpperCase( locale ) + "-";
	public  static final String  grouped       = "grouped";
	public  static final String  groupedPrefix = grouped.toUpperCase( locale ) + "-";
	public  static final String  ungrouped     = "ungrouped";
	public  static final String  ungrpedPrefix = ungrouped.toUpperCase( locale ) + "-";
	public  static final String  phrase        = "phrase";
	public  static final String  phrasePrefix  = phrase.toUpperCase( locale ) + "-";
	public  static final String  first         = "first-of";
	public  static final String  firstPrefix   = first.toUpperCase( locale ) + "-";
	public  static final String  rest          = "rest-of";
	public  static final String  restPrefix    = rest.toUpperCase( locale ) + "-";
	public  static final String  numeric       = "numeric";
	public  static final String  numericPrefix = numeric.toUpperCase( locale ) + "-";
	public  static final String  expression    = "expression";
	public  static final String  expr          = "expr";
	public  static final String  exprPrefix    = expr.toUpperCase( locale ) + "-";
	public  static final String  plural        = Plural.NAME; // "plural";
	public  static final String  pluralPrefix  = plural.toUpperCase( locale ) + "-";
	public  static final String  sinsign       = "said";
	public  static final String  sinsignPrefix = sinsign.toUpperCase( locale ) + "-";
	public  static final String  external      = "ext";
	public  static final String  externPrefix  = external.toUpperCase( locale ) + "-";
	
	public Pattern() { super(); }
	public Pattern( Strings words ) {
		
		// "if X do Y" -> [ <x prefix=["if"]/>, <y prefix=["do"] postfix="."/> ]
		Frag t = new Frag();
		for ( String word : words ) {
			
			if (word.equals( "an" )) word = "a";
			
			if (Strings.isUCwHyphUs( word )) {
				Strings arr = new Strings( word.toLowerCase( locale ), '-' );
				ListIterator<String> wi = arr.listIterator();
				String sw = wi.next();
				if (sw.equals( phrase )) {
					t.phrasedIs();
					if (wi.hasNext()) sw = wi.next();
					else audit.error( "ctor: PHRASE variable, missing name." );
				} else if (sw.equals( plural )) {
					t.pluralIs();
					if (wi.hasNext()) sw = wi.next();
					else audit.error( "ctor: PLURAL variable, missing name." );
				} else if (sw.equals( quoted )) {
					t.quotedIs();
					if (wi.hasNext()) sw = wi.next();
					else audit.error( "ctor: QUOTED variable, missing name." );
				} else if (sw.equals( numeric )) {
					t.numericIs();
					if (wi.hasNext()) sw = wi.next();
					else audit.error( "ctor: NUMERIC variable, missing name." );
				} else if (sw.equals( expr )) {
					t.exprIs();
					if (wi.hasNext()) sw = wi.next();
					else audit.error( "ctor: EXPR variable, missing name." );
				} else if (sw.equals( sinsign )) {
					t.signIs();
					if (wi.hasNext()) sw = wi.next();
					else audit.error( "ctor: SAID, missing name." );
				} else if (sw.equals( Config.andConjunction() )) {
					//audit.LOG( "found: "+ Reply.andConjunction() );
					if (wi.hasNext()) {
						sw = wi.next();
						if (sw.equals( list )) {
							if (wi.hasNext()) {
								//audit.LOG( "bingo: found isList" );
								sw = wi.next();
								t.listIs();
							} else
								audit.error( "ctor: AND-LIST variable, missing name." );
						} else
							audit.error( "ctor: AND-LIST? 'LIST' missing" );
					} else
						audit.error( "ctor: AND terminates variable" );
				}
				
				while ((sw.equals( phrase  ) ||
				        sw.equals( plural  ) ||
				        sw.equals( quoted  ) ||
				        sw.equals( expr    ) ||
				        sw.equals( sinsign ) ||
				        sw.equals( numeric )   )
					 && wi.hasNext())
				{
					audit.error( "ctor: mutually exclusive modifiers" );
					sw = wi.next();
				}
				
				Strings apostrophes = new Strings( sw, Englishisms.APOSTROPHE.charAt( 0 ));
				if (apostrophes.size() == 2 && apostrophes.get( 1 ).equals( "s" )) {
					sw = apostrophes.get( 0 );
					t.apostrophedIs( apostrophes.get( 1 ));
				}
				
				add( t.name( sw ));
				t = new Frag();
				
			} else
				t.prefixAppend( word );
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
			
			if (type.equals(phrase)) {
				add( new Frag( frag.prefix().copyBefore( word ), word ).phrasedIs());
				//add( new Frag( frag ).prefix( frag.prefix().copyAfter( word )))
				Strings nextPrefix = frag.prefix().copyAfter( word );
				if (!nextPrefix.isEmpty())
					add( new Frag( frag ).prefix( nextPrefix ));
			} else if (type.equals(numeric)) {
				add( new Frag( frag.prefix().copyBefore( word ), word ).numericIs());
				//add( new Frag( frag ).prefix( frag.prefix().copyAfter( word )));
				Strings nextPrefix = frag.prefix().copyAfter( word );
				if (!nextPrefix.isEmpty())
					add( new Frag( frag ).prefix( nextPrefix ));
			} else { // not type - a singlton?
				add( new Frag( frag.prefix().copyBefore( word ), word ));
				//add( new Frag( frag ).prefix( frag.prefix().copyAfter( word )));
				Strings nextPrefix = frag.prefix().copyAfter( word );
				if (!nextPrefix.isEmpty())
					add( new Frag( frag ).prefix( nextPrefix ));
			}
			
		} else if (frag.postfix().contains( word )) {
			
			if (type.equals(phrase)) {
				add( new Frag( frag ).postfix( frag.postfix().copyBefore( word )));
				//add( new Frag( frag.postfix().copyAfter( word ), word ).phrasedIs());
				Strings nextPrefix = frag.prefix().copyAfter( word );
				if (!nextPrefix.isEmpty())
					add( new Frag( frag ).prefix( nextPrefix ));
			} else if (type.equals(numeric)) {
				add( new Frag( frag ).postfix( frag.postfix().copyBefore( word )));
//				add( new Frag( frag.postfix().copyAfter( word ), word ).numericIs());
				Strings nextPrefix = frag.prefix().copyAfter( word );
				if (!nextPrefix.isEmpty())
					add( new Frag( frag ).prefix( nextPrefix ));
			} else {
				add( new Frag( frag ).postfix( frag.postfix().copyBefore( word )));
//				add( new Frag( frag.postfix().copyAfter( word ), word ));
				Strings nextPrefix = frag.prefix().copyAfter( word );
				if (!nextPrefix.isEmpty())
					add( new Frag( frag ).prefix( nextPrefix ));
			}
			
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
		Pattern ff = new Pattern();
		for (Frag f : this) {
			ff.addAll( new Pattern( f, word ));
		}
		return ff;
	}
	public Pattern split( String word, String type ) {
		Pattern ff = new Pattern();
		for (Frag f : this) {
			ff.addAll( new Pattern( f, word, type ));
		}
		return ff;
	}
	
	public Strings names() {
		Strings sa = new Strings();
		for (Frag f : this)
			sa.append( f.name() );
		return sa;
	}
	
	// TODO: not quite right, what about "l'eau" - the water
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
			
			if (word.equals( variable ))
				if (wi.hasNext() && null != (word = wi.next()) && !word.equals( variable ))
					out.append( toUpperCase( word ));
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
			
			else if (word.equals( quoted ))
				if (wi.hasNext() && null != (word = wi.next()))
					if (word.equals( variable ))
						if (wi.hasNext() && null != (word = wi.next()) && !word.equals( variable ))
							out.append( quotedPrefix + word.toUpperCase( locale ));
						else // quoted variable. OR phrase variable variable
							out.append( quoted ).append( word );
					else // quoted blah
						out.append( quoted ).append( word );
				else // quoted.
					out.append( quoted );
			
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

			// ... "why sentence because reason sentence" ???
// SAID IS NOT YET articulated! TODO:
//			else if (word.equals( "sign" ))
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
	private String doExpr( ListIterator<String> ui ) {
		Strings rep = Expression.getExpr( ui, new Strings() );
		return rep == null ? "" : rep.toString();

	}
	private String doList( ListIterator<Frag> patti,
	                       ListIterator<String>      utti  ) 
	{
		String  word = utti.next();
		Strings words = new Strings(),
		        vals  = new Strings();
		if (patti.hasNext()) {
			
			// peek at terminator
			String terminator = patti.next().prefix().get( 0 );
			patti.previous();
			//audit.debug( "Terminator is "+ terminator )
			
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
		} else { // read to end
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
		int sz = vals.size();
		if (sz == 0) return null;
		// deal with "they", "our", "we" etc as a list
		if (sz == 1) {
			String val = vals.get( 0 );
			if (!val.equals( subjGroup )
				&& !val.equals( objGroup  )
				&& !val.equals( possGroup )
				&& !val.equals( subjOther )
				&& !val.equals( objOther  )
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
			Where where = null;
			Strings terms = getNextBoilerplate( t, ti ); // null if this is last tag
			String term = terms==null || terms.isEmpty() ? null : terms.get( 0 );
			while (said.hasNext()) {
				// term==null? => read to end
				if (spatial && null != (where = Where.getWhere( said, term ))) {
					matched( where );
					break; // finding a where is the end of a variable...
				} else {
					u = said.next();
					if (term != null && term.equals( u )) {
						said.previous();
						break;
					} else
						vals.add( u );
		}	}	}
		String val = vals.toString();
		// TODO: ...again "l'eau"
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
			
			if (null == (val = doExpr( utti )))
				notMatched = 13;
			
		} else if (t.invalid( utti )) {
			notMatched = 16;
			
		} else if (null == (val = getVariable( t, patti, utti, spatial ))) {
			notMatched = 23;
		}
		return val;
	}
	
	/* TODO: Proposal: that a singular tag (i.e. non-PHRASE) can match with a known string
	 * i.e. an object id: e.g. theOldMan, thePub, fishAndChips.
	 * Matched first so as to avoid "sergeant at arms" as spatial
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
				//audit.debug( "FOUND value: "+ a );
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
	@Override
	public String toString() {
		String tmp;
		StringBuilder str = new StringBuilder();
		Iterator<Frag> ti = iterator();
		while (ti.hasNext())
			if (!(tmp = ti.next().toString()).equals(""))
				str.append( tmp +(ti.hasNext() ? " " : ""));
		return str.toString();
	}
	public String toFilename() {
		String tmp;
		StringBuilder str = new StringBuilder();
		Iterator<Frag> ti = iterator();
		while (ti.hasNext())
			if (!(tmp = ti.next().toPattern()).equals(""))
				str.append( tmp +(ti.hasNext() ? "_" : ""));
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
//	private static void toPatternTest( String utt  ) {
//		toPatternTest( utt, utt ); // check it against itself!
//	}
//	private static void toPatternTest( String utt, String answer ) {
//		String patt = toPattern( utt ).toString();
//		if (answer != null && !answer.equals( patt ))
//			audit.FATAL( "answer '"+ patt +"' doesn't equal expected: '" + answer +"'" );
//		audit.debug( ">"+ utt +"< to pattern is >"+ patt +"<" );
//	}
	private static  void matchTest( String pref, String var, String concept, String utterance ) {
		audit.in( "matchTest", utterance );
		Attributes as;
		Utterance u = new Utterance( new Strings( utterance ));
		audit.debug( "Utterance: "+ utterance );
		
		Sign s = new Sign( new Frag( pref, var ));
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
		//audit.LOG( "    Xml: "+ patt.toXml() )
		Audit.log( " cmplxy: "+ patt.cplex( true ) );

	}
	public  static  void main(String args[]) {
//		Audit.on();
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

//		audit.debug( "First: martin is alive" );
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
		
//		audit.debug( "Second: i am alive" );
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
		
//		Audit.on();
//		audit.tracing = false;
//		debug( debug );
//		Audit.off();
//		Audit.runtimeDebug = false;
//		Audit.traceAll( false );		
		
//		Audit.on();
//		//Audit.traceAll( true );		
//		
//		p = new Pattern( "help" );
//		audit.debug( "sign is now: "+ p.toXml());
//		p.newTest( "at the pub i am meeting my brother" );
//		
//		p.newTest( "doesnt match at all" );
//
//		// -- expr
//		p = new Pattern( "the FUNCITON of LIST-FNAME is EXPR-VAL" );
//		audit.debug( "sign is: "+ p.toXml());
//		
//		p.newTest( "the sum of x is x plus y" );

		// pronouns test...
//		p = new Pattern( "they are" );
//		audit.debug( "sign is: "+ p.toXml());
//		p.newTest( "they are from sainsburys" );

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
				"PHRASE-THESE",
				"need+needs",
				"i need milk" );
		matchTest(
				"i need",
				"PHRASE-THESE",
				"need+needs",
				"i need milk from the dairy aisle" );
		matchTest(
				"i need",
				"PHRASE-THESE",
				"need+needs",
				"i from the dairy aisle need milk" );
		matchTest(
				"i need",
				"PHRASE-THESE",
				"need+needs",
				"from the dairy aisle i need milk" );
		Audit.off();
//		newTest( "i need sliced bread from the bakery" )
		
		Audit.PASSED();
}	}
