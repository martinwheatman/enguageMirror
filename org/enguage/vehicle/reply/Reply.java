package org.enguage.vehicle.reply;

import org.enguage.Enguage;
import org.enguage.interp.Context;
import org.enguage.interp.intention.Redo;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Attributes;
import org.enguage.util.sys.Shell;
import org.enguage.vehicle.Utterance;
import org.enguage.vehicle.when.Moment;
import org.enguage.vehicle.when.When;

public class Reply { // a reply is basically a formatted answer
	
	static private Audit audit = new Audit( "Reply" );

	static public final int FAIL = -1; // FALSE -- -ve
	static public final int   NO =  0; // FALSE -- -ve
	static public final int  YES =  1; // TRUE  -- +ve
	static public final int  DNU =  2; // DO NOT UNDERSTAND
	static public final int  DNK =  3; // NOT KNOWN -- init
	static public final int   IK =  4; // I know, silly!
	static public final int  CHS =  5; // use stored expression
	static public final int  UDU =  6; // user does not understand
	
	static private boolean verbatim = false; // set to true in handleDNU()
	static public  boolean isVerbatim() { return verbatim; }
	static public  void    verbatimIs( boolean val ) { verbatim = val; }
	
	static private boolean understood = false;
	static public  boolean understoodIs( boolean was ) { return understood = was;}
	static public  boolean isUnderstood() { return Reply.understood; }
	
	static private String strangeThought = "DNU";
	static public  void   strangeThought( String thought ) { strangeThought = thought; }
	static public  String strangeThought(){ return strangeThought; }

	static private Strings dnu = new Strings( Enguage.DNU );
	static private String  dnuStr = Enguage.DNU;
	static public  void    dnu( String s ) { dnu = new Strings( dnuStr = s ); }
	static public  Strings dnu(){ return dnu; }
	static public  String  dnuStr(){ return dnuStr; }

	static private Strings dnk = new Strings( "DNK" );
	static private String  dnkStr = "DNK";
	static public  void    dnk( String s ) { dnk = new Strings( dnkStr = s ); }
	static public  Strings  dnk() { return dnk; }
	static public  String  dnkStr() { return dnkStr; }

	static private Strings ik = new Strings( "IK" );
	static private String  ikStr = "IK";
	static public  void    ik( String s ) { ik = new Strings( ikStr = s ); }
	static public  Strings ik() { return ik; }
	static public  String  ikStr() { return ikStr; }

	static private Strings no = new Strings( "no" );
	static private String  noStr = "no";
	static public  void    no(  String s ) { no = new Strings( noStr = s ); }
	static public  Strings no() { return no; }
	static public  String  noStr() { return noStr; }
	
	static private Strings yes    = new Strings( "yes" );
	static private String  yesStr = "yes";
	static public  void    yes( String s ) { yes = new Strings( yesStr = s ); }
	static public  Strings yes() { return yes; }
	static public  String  yesStr() { return yesStr; }

	static private Strings failure   = new Strings( Shell.FAIL );
	static private String  failureStr = Shell.FAIL;
	static public  void    failure(  String s ) { failure = new Strings( failureStr = s ); }
	static public  Strings failure() { return failure; }
	static public  String  failureStr() { return failureStr; }
	
	static private Strings success    = new Strings( Shell.SUCCESS );
	static private String  successStr = Shell.SUCCESS;
	static public  void    success( String s ) { success = new Strings( successStr = s ); }
	static public  Strings success() { return success; }
	static public  String  successStr() { return successStr; }

	static private String repeatFormat = "I said, ... .";
	static public  void   repeatFormat( String s ) { repeatFormat = s; }
	static public  String repeatFormat() { return repeatFormat; }

	static private String helpPrefix = "you can say, ";
	static public  void   helpPrefix( String s ) { helpPrefix = s; }
	static public  String helpPrefix() { return helpPrefix; }

	static private String  andConjunction = new String( "and" );
	static public  void    andConjunction( String s ) { andConjunction = s; }
	static public  String  andConjunction() { return andConjunction; }

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

	/* previous() is used to retrieve the reply from the previous thought. It is
	 * used in implementing imagination.  If the imagination session goes ok,
	 * we need the reply from that session. Was implemented with the equiv 
	 * intention in previous C incarnation.
	 */
	static private Strings previous = new Strings( "" );
	static public  Strings previous( Strings rep ) { return previous = rep; }
	static public  Strings previous() { return previous; }

	private boolean repeated = false;
	public  void    repeated( boolean s ) { repeated = s; }
	public  boolean repeated() { return repeated; }
	
	private boolean done = false;
	public  Reply   doneIs( boolean b ) { done = b; return this; }
	public  boolean isDone() { return done; }
	
	static public  Strings say = new Strings(); // public to reset it!
	static private Strings say() { return say; }
	static public  void    say( Strings sa ) {say.addAll( Shell.addTerminator( sa ));}
		
	private static final Strings FUDG1 = new Strings( "I don't know" );
	private static final Strings FUDG2 = new Strings( "I don't understand" );
	private int     type = DNU;
	public  int     type() { return type; }
	public  Reply   type( Strings response ) {
		
		if (type == UDU) return this;

		     if (response.beginsIgnoreCase(    yes )) type = YES;
		else if (response.beginsIgnoreCase( success)) type = YES;
		else if (response.beginsIgnoreCase( failure)) type =FAIL;
		else if (response.beginsIgnoreCase(    dnu )) type = DNU;
		else if (response.beginsIgnoreCase(     no )) type =  NO;
		else if (response.beginsIgnoreCase(  FUDG2 )) type = DNU;
		else if (response.beginsIgnoreCase(  FUDG1 )) type = DNK;
		else if (response.beginsIgnoreCase(    dnk )) type = DNK;
		else if (response.beginsIgnoreCase(     ik )) type =  IK;
		else type = CHS;
		return this;
	}
	public  boolean negative() {return  FAIL == type || NO == type ||  DNK == type || type == UDU; } // != !positive() !!!!!
	public  void userDNU() { type = UDU; }// forces us out to I don't know?


	private Strings cache = null;
	/** Answer:
	 * Multiple answers should now be implemented in a Replies class!
	 *                                     or in List class, below.
	 * e.g. You need tea and biscuits and you are meeting your brother at 7pm.
	 */
	public Answer a = new Answer();
	
	public  Reply   answer( String ans ) {
		if (ans != null && !ans.equals( Shell.IGNORE )) {
			if (!a.isAppending())
				a = new Answer(); // a.nswer = new Strings();
			a.add( ans );
			// type is dependent on answer
			cache = null;
			type = (type == UDU) ? UDU : a.type();
		}
		return this;
	}
	public  Reply   rawAnswer( String rc, String method ) {
		answer( Moment.valid( rc ) ? // 88888888198888 -> 7pm
					new When( rc ).rep( Reply.dnkStr() ).toString()
					: rc.equals( "" ) &&
					  (method.equals( "get" ) ||
				       method.equals( "getAttrVal" )) ?
						Reply.dnkStr()
						: rc.equals( Shell.FAIL ) ?
							Reply.failureStr()
							:	rc.equals( Shell.SUCCESS ) ?
									Reply.successStr()
									: rc );
		return this;
	}
	
	/** Format
	 * 
	 */
	private Format f = new Format();
	
	public  boolean verbose() { return !f.shrt(); }
	public  void    verbose( boolean v ) { f.shrt( v );}

	public static boolean isLiteral( Strings sa ) { return sa.areLowerCase() && !sa.contains( Strings.ELLIPSIS );}
	public  Reply   format( Strings format ) {
		cache = null; //de-cache any previous reply
		//Strings format = new Strings( sa );
		f.ormat( format );

		if (isLiteral( format ) && a.none()) // remove a.none?
			answer( format.toString() ); // overwrite answer!
		return this;
	}
	private Strings encache() {
		if (null == cache)
			cache = Utterance.externalise(
						a.injectAnswer(
								new Strings( say() ).appendAll( f.ormat())
						),
						isVerbatim()
					);
		return cache;
	}
	private void handleDNU( Strings utterance ) {
		if (Audit.detailedOn) audit.in( "handleDNU", utterance.toString( Strings.CSV ));
		verbatimIs( true );
		if (Shell.terminators().get( 0 ).equals( Shell.terminatorIs( utterance )))
			utterance = Shell.stripTerminator( utterance );
		
		// Construct the DNU format
		format( new Strings( Reply.dnu() + ", ..." ));
		answer( utterance.toString());
		
		/* Take this out for the moment... ...needs more thought:
		 * if (!strangeThought.equals( "" ))
		 *	fmt.add( " when thinking about "+ strangeThought());
		 */
		
		verbatimIs( false );
		if (Audit.detailedOn) audit.out();
	}
	public Strings toStrings() {
		Strings reply = encache();
		if (understoodIs( Reply.DNU != type() )) {
			if (!repeated())
				previous( reply ); // never used
			;
		} else
			handleDNU( Utterance.previous() );
		return reply;
	}
	public String toString() {return encache().toString();}
	
	public void conclude( Strings thought ) {
		strangeThought("");

		if ( DNU == type()) {
			// put this into reply via Reply.strangeThought()
			audit.ERROR( "Strange thought: I don't understand: '"+ thought.toString() +"'" );
			strangeThought( thought.toString() );

			// remove strange thought from Reply - just say DNU
			if (Redo.disambFound()) {
				audit.ERROR( "Previous ERROR: maybe just run out of meanings?" );
				strangeThought("");
			}
			type = FAIL;
		
		} else if ( NO == type() && a.toString().equalsIgnoreCase( ikStr()))
			answer( yesStr());
	}
	public static void main( String args[] ) {
		Audit.allOn();

		Reply.dnu( "Pardon?" );
		Reply.dnk( "Dunno" );
		Reply.no(  "No" );
		Reply.yes( "Yes" );
		
		Reply r = new Reply();
		Audit.log( "Initially: "+ r.toString());
		r.format( new Strings( "ok" ));
		Audit.log( "Initially2: "+ r.toString());
		r.answer( "42" );
		Audit.log( "THEN: "+ r.toString());
		r.answer( "53" );
		Audit.log( "W/no format:"+ r.toString());
		
		r.format( new Strings( "The answer to X is ..." ));
		
		Attributes attrs = new Attributes();
		attrs.add( new Attribute( "x", "life the universe and everything" ));
		Context.push( attrs );
		Audit.log( "Context is: "+ Context.valueOf());
		
		Audit.log( "Finally:"+ r.toString());
}	}
