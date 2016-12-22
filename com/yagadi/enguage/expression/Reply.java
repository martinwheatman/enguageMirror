package com.yagadi.enguage.expression;

/* changes - removing preferences - kept in until implemented in app
 * or reinstated here...
 */

import java.util.ListIterator;
import java.util.Locale;

import com.yagadi.enguage.sofa.Attribute;
import com.yagadi.enguage.sofa.Attributes;
import com.yagadi.enguage.sofa.Numeric;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

public class Reply { // a reply is basically a formatted answer
	
	static private Audit audit = new Audit( "Reply" );

	static public final int   NO = 0; // FALSE -- -ve
	static public final int  YES = 1; // TRUE  -- +ve
	static public final int  DNU = 2; // DO NOT UNDERSTAND
	static public final int   NK = 3; // NOT KNOWN -- init
	static public final int   IK = 4; // I know, silly!
	static public final int  CHS = 5; // use stored expression
	
	public final static String  verbose     = "verbose"; // this is also defined in iNeed/MainActivity.java 
	public final static boolean initVerbose = false;
	public boolean verbose() {
		//Preferences p = Preferences.getPreferences();
		//if (p==null) return true; // default to a verbose state
		return true; //p.get( verbose, initVerbose );
	}
	
	static private boolean verbatim = false; // set to true in handleDNU()
	static public  boolean isVerbatim() { return verbatim; }
	static public  void    verbatimIs( boolean val ) { verbatim = val; }
	
	static private boolean understood = false;
	static public  boolean understood( boolean was ) { return understood = was;}
	static public  boolean understood() { return Reply.understood; }
	
	static private String strangeThought = "DNU";
	static public  void   strangeThought( String thought ) { strangeThought = thought; }
	static public  String strangeThought(){ return strangeThought; }

	static private String dnu = "DNU";
	static public  void   dnu( String s ) { dnu = s; }
	static public  String dnu(){ return dnu; }

	// TODO: these need to be Strings
	static private String dnk = "DNK";
	static public  void   dnk( String s ) { dnk = s; }
	static public  String dnk() { return dnk; }

	static private String ik = "IK";
	static public  void   ik( String s ) { ik = s; }
	static public  String ik() { return ik; }

	static private String no = "no";
	static public  void   no(  String s ) { no = s; }
	static public  String no() { return no; }
	
	static private String yes = "yes";
	static public  void   yes( String s ) { yes = s; }
	static public  String yes() { return yes; }

	static private String failure = Shell.FAIL;
	static public  void   failure(  String s ) { failure = s; }
	static public  String failure() { return failure; }
	
	static private String success = Shell.SUCCESS;
	static public  void   success( String s ) { success = s; }
	static public  String success() { return success; }

	static private String repeatFormat = "I said, ... .";
	static public  void   repeatFormat( String s ) { repeatFormat = s; }
	static public  String repeatFormat() { return repeatFormat; }

	static private String helpPrefix = "you can say, ";
	static public  void   helpPrefix( String s ) { helpPrefix = s; }
	static public  String helpPrefix() { return helpPrefix; }

	static private Strings andConjunctions = new Strings( ", and" );
	static public  void    andConjunctions( Strings sa ) { andConjunctions = sa; }
	static public  Strings andConjunctions() { return andConjunctions; }

	static private Strings andListFormat = new Strings( ", /, and ", '/' );
	static public  void    andListFormat( String s ) { andListFormat = new Strings( s, listSep().charAt( 0 )); }
	static public  Strings andListFormat() { return andListFormat; }

	static private Strings orConjunctions = new Strings( ", or" );
	static public  void    orConjunctions( Strings sa ) { orConjunctions = sa; }
	static public  Strings orConjunctions() { return orConjunctions; }
	
	static private Strings orListFormat = new Strings( ", /, or ", '/' );
	static public  void    orListFormat( String s ) { orListFormat = new Strings( s, listSep().charAt( 0 )); }
	static public  Strings orListFormat() { return orListFormat; }

	static private Strings referencers = new Strings( "the" );
	static public  void    referencers( Strings sa ) { referencers = sa; }
	static public  Strings referencers() { return referencers; }

	static private String listSep = "/";
	static public  void   listSep( String s ) { listSep = s; }
	static public  String listSep() { return listSep; }

	/* This is used to retrieve the reply from the previous thought. This is
	 * used in implementing imagination.  If the imagination session goes ok,
	 * we need the reply from that session. Was implemented with equiv 
	 * intention in previous C incarnation.
	 */
	static private String previous = "";
	static public  String previous( String rep ) { return previous = rep; }
	static public  String previous() { return previous; }

	private int type = DNU;
	// todo: needs to be split out into answerType() and formatType()
	// needs to be split out into class Answer and class Format: think!
	private int calculateType() {
		if (a.nswer.toString( andListFormat ).equals( "" ) && f.ormat.size() == 0) {
			return DNU;
		} else if (a.nswer.toString( andListFormat ).equals( no ) && f.ormat.equals( new Strings( ik ))) {
			return CHS;
		} else if (a.nswer.toString( andListFormat ).equals( "" ) && f.ormat.contains( "..." )) {
			return NK;
		} else if (a.nswer.toString( andListFormat ).equals( "" ) && !f.ormat.contains( "..." )) {
				 if (f.ormat.equals( new Strings(   yes ))) return YES;
			else if (f.ormat.equals( new Strings(success))) return YES;
			else if (f.ormat.equals( new Strings(    no ))) return NO;
			else if (f.ormat.equals( new Strings(failure))) return NO;
			else if (f.ormat.equals( new Strings(    ik ))) return IK;
			else if (f.ormat.equals( new Strings(   dnk ))) return NK;
			else if (f.ormat.equals( new Strings(   dnu ))) return DNU;
			else return CHS;
		} else {
			     if (a.nswer.toString( andListFormat ).equalsIgnoreCase(   yes )) return YES;
			else if (a.nswer.toString( andListFormat ).equalsIgnoreCase(success)) return YES;
			else if (a.nswer.toString( andListFormat ).equalsIgnoreCase(    no )) return NO;
			else if (a.nswer.toString( andListFormat ).equalsIgnoreCase(failure)) return NO;
			else if (a.nswer.toString( andListFormat ).equalsIgnoreCase(    ik )) return IK;
			else if (a.nswer.toString( andListFormat ).equalsIgnoreCase(   dnk )) return NK;
			else if (a.nswer.toString( andListFormat ).equalsIgnoreCase(   dnu )) return DNU;
			else return CHS;
	}	}
	public int      getType() { return type; }
	public boolean positive() {return YES == type || CHS == type; } // != !negative() !!!!!
	public boolean negative() {return  NO == type ||  NK == type; } // != !positive() !!!!!

	/** Answer:
	 * Multiple answers should be implemented in Replies class!
	 *                                     or in List class, below.
	 * e.g. You need tea and biscuits and you are meeting your brother at 7pm.
	 */
	public Ans a = new Ans();
	
	//private Strings  answer = new Strings();
	//public  String  xanswerToString() { return a.nswer.toString( andListFormat ); }
	//public  Reply   xanswerAdd( String ans ) { a.nswer.add( ans ); return this; }
	public  Reply   answer( String ans ) {
		if (null == ans) {
			a.nswer = new Strings();
			cache = null;
			type = DNU;
		} else if (!ans.equals( Shell.IGNORE )) {
			if (!a.isAppending())
				a.nswer = new Strings();
			a.nswer.add( ans );
			// type is dependent on answer
			cache = null;
			type = calculateType();
		}
		return this;
	}
	
	/** Format
	 * 
	 */
	public Fmt f = new Fmt();
	
	//private Strings format = new Strings(); // format is empty!
	public  Reply   format( String s ) { format( new Strings( s )); return this; }
	public  Reply   format( Strings sa ) {
		cache = null; //de-cache any previous reply
		f.ormat = sa;
		if (!f.ormat.contains( "..." )) answer( "" );
		type = calculateType(); // type is dependent on format -- should it be???
		return this;
	}
	public Strings format() {
		//audit.traceIn("format", format.toString(0));
		if (!verbose()) {
			if (f.ormat.size() > 1 && f.ormat.get( 1 ).equals( "," ))
				if (f.ormat.get( 0 ).equalsIgnoreCase( yes ) || // yes is set to "OK", when yes is "yes", this fails...
					f.ormat.get( 0 ).equalsIgnoreCase(  no ) ||
					f.ormat.get( 0 ).equalsIgnoreCase( success )) {
					//audit.traceOut("returning only 1st");
					return new Strings( say() + " " +f.ormat.get( 0 )); // return only first
				} else if (f.ormat.get( 0 ).equalsIgnoreCase( failure )) {
					//audit.traceOut("returning rest");
					return new Strings( say()).append( f.ormat.copyAfter( 1 ).filter()); // forget 1st & 2nd
				}
			//audit.traceOut("returning filtered format");
			return new Strings( say()).append( f.ormat.filter( ));
		}
		//audit.traceOut("returning full format");
		return new Strings( say()).append( f.ormat );
	}
	
	private boolean repeated = false;
	public  void    repeated( boolean s ) { repeated = s; }
	public  boolean repeated() { return repeated; }

	private boolean done = false;
	public  Reply   doneIs( boolean b ) { done = b; return this; }
	public  boolean isDone() { return done; }

	public  Strings say = new Strings();
	public  String  say() { return say.toString( Strings.SPACED ); }
	public  void    say( Strings sa ) { say.addAll( Shell.addTerminator( sa )); }
	
	private String cache = null;

	public static String lastOutput = null;
	private String encache() {
		//audit.in( "encache", format().toString() +", type="+ type );
		if (null == cache) {
			Strings utterance = format();
			if (0 == utterance.size())
				// todo: is answer ever null??? 
				utterance = a.nswer == null ? new Strings( dnu() ) : new Strings( a.nswer );

			// ... then post-process:
			// if not terminated, add first terminator -- see Tag.c::newTagsFromDescription()
			if (utterance.size() > 0 && !Shell.isTerminator( utterance.get( utterance.size() -1)) &&
				!((utterance.size() > 1) && Shell.isTerminator( utterance.get( utterance.size() -2)) && Language.isQuote( utterance.get( utterance.size() -1))))
				utterance.add( Shell.terminators().get( 0 ));

			// ...finally, if required put in answer (verbatim!)
			if (utterance.size() == 0)
				if ( a.nswer.toString( andListFormat ).equals( "" ))
					utterance = new Strings( dnu() );
				else
					utterance = a.nswer; // use the raw answer???
			else if (utterance.contains( Strings.ELLIPSIS )) {
//				audit.ERROR( "replyToString() used to look for ellipsis - now done in Intention" );
				if ( a.nswer.toString( andListFormat ).equals( "" ))
					utterance = new Strings( dnk() ); // need an answer, but we don't have one
				else
					// ok replace "..." with answer -- where reply maybe "martin/mother/computer"
					utterance.replace( Strings.ellipsis, new Strings( a.nswer.toString( andListFormat ) ));
			}
			
			// outbound and general colloquials
			if (!isVerbatim())
				utterance = Colloquial.applyOutgoing( utterance );
				
			// ...deref any context...
			
			// set it to lowercase - removing emphasis on AND
			ListIterator<String> ui = utterance.listIterator();
			while (ui.hasNext())
				ui.set( ui.next().toLowerCase( Locale.getDefault() ));
			
			// English-dependent processing...
			utterance = Language.indefiniteArticleVowelSwap(
							Language.sentenceCapitalisation( 
								Language.pronunciation( utterance )));
			
			cache = Language.asString( Numeric.deref( /*Variable.deref(*/ utterance /*)*/));
			// ...deref any envvars...  ...any numerics...
		}
		//audit.out( cache );
		return cache;
	}
	private void handleDNU( Strings utterance ) {
		if (Audit.detailedDebug) audit.in( "handleDNU", utterance.toString( Strings.CSV ));
		verbatimIs( true );
		if (Shell.terminators().get( 0 ).equals( Shell.terminatorIs( utterance )))
			utterance = Shell.stripTerminator( utterance );
		
		// Construct the DNU format
		format( new Strings( Reply.dnu() ).append( "," ).append( "..." ));
		answer( utterance.toString( Strings.SPACED ));
		
		/* Take this out for the moment... ...needs more thought:
		 * if (!strangeThought.equals( "" ))
		 *	fmt.add( " when thinking about "+ strangeThought());
		 */
		
		verbatimIs( false );
		if (Audit.detailedDebug) audit.out();
	}
	public String toString( Strings utterance ) {
		String reply = encache();
		if (Reply.understood( Reply.DNU != getType() )) {
			if (!repeated())
				Reply.lastOutput( reply );
		} else
			handleDNU( Utterance.previous() );
		return reply;
	}
	public String toString() { return encache(); }
		
	static public  String lastOutput() { return lastOutput; }  //
	static public  String lastOutput( String l ) { return lastOutput = l; }
	public static void main( String args[] ) {
		Audit.allOn();
		
		Reply.dnu( "Pardon?" );
		Reply.dnk( "Dunno" );
		Reply.no(  "No" );
		Reply.yes( "Yes" );
		
		Reply r = new Reply();
		audit.log( "Initially: "+ r.toString());
		r.answer( "42" );
		audit.log( "THEN: "+ r.toString());
		r.answer( "53" );
		audit.log( "W/no format:"+ r.toString());
		
		r.format( "The answer to X is ..." );
		
		Attributes attrs = new Attributes();
		attrs.add( new Attribute( "x", "life the universe and everything" ));
		Context.push( attrs );
		audit.log( "Context is: "+ Context.valueOf());
		
		audit.log( "Finally:"+ r.toString());
}	}
