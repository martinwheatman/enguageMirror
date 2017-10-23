package com.yagadi.enguage.vehicle;

import com.yagadi.enguage.interpretant.Allopoiesis;
import com.yagadi.enguage.object.Attribute;
import com.yagadi.enguage.object.Attributes;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

public class Reply { // a reply is basically a formatted answer
	
	static private Audit audit = new Audit( "Reply" );

	static public final int FAIL = -1; // FALSE -- -ve
	static public final int   NO =  0; // FALSE -- -ve
	static public final int  YES =  1; // TRUE  -- +ve
	static public final int  DNU =  2; // DO NOT UNDERSTAND
	static public final int   NK =  3; // NOT KNOWN -- init
	static public final int   IK =  4; // I know, silly!
	static public final int  CHS =  5; // use stored expression
	static public final int  UDU =  6; // user does not understand
	
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

	/* previous() is used to retrieve the reply from the previous thought. It is
	 * used in implementing imagination.  If the imagination session goes ok,
	 * we need the reply from that session. Was implemented with the equiv 
	 * intention in previous C incarnation.
	 */
	static private String previous = "";
	static public  String previous( String rep ) { return previous = rep; }
	static public  String previous() { return previous; }

	private boolean repeated = false;
	public  void    repeated( boolean s ) { repeated = s; }
	public  boolean repeated() { return repeated; }
	
	//static private String lastOutput = null;
	//static public  String lastOutput() { return lastOutput; }
	//static public  String lastOutput( String l ) { return lastOutput = l; }

	private boolean done = false;
	public  Reply   doneIs( boolean b ) { done = b; return this; }
	public  boolean isDone() { return done; }
	
	public  Strings say = new Strings();
	public  String  say() { return say.toString( Strings.SPACED ); }
	public  void    say( Strings sa ) { say.addAll( Shell.addTerminator( sa )); }
	
	private String cache = null;
	
	private int     type = DNU;
	public  int     type() { return type; }
	private void    type( int t ) {type = t;}
	public  boolean negative() {return  FAIL == type || NO == type ||  NK == type || type == UDU; } // != !positive() !!!!!
	public  Reply setType( Strings response ) {
		
		if (type == UDU) return this;

			 if (response.beginsIgnoreCase( new Strings(   yes ))) type( YES );
		else if (response.beginsIgnoreCase( new Strings(success))) type( YES );
		else if (response.beginsIgnoreCase( new Strings(    no ))) type(  NO );
		else if (response.beginsIgnoreCase( new Strings(failure))) type(FAIL );
		else if (response.beginsIgnoreCase( new Strings(    ik ))) type(  IK );
		else if (response.beginsIgnoreCase( new Strings(   dnk ))) type(  NK );
		else if (response.beginsIgnoreCase( new Strings( "I don't know" ))) type( NK );
		else if (response.beginsIgnoreCase( new Strings(   dnu ))) type(  DNU );
		else if (response.beginsIgnoreCase( new Strings( "I don't understand" ))) type(  DNU );
		else type( CHS );
		return this;
	}
	public void userDNU() {
		type( UDU ); // forces us out to I don't know?
	}


	/** Answer:
	 * Multiple answers should now be implemented in a Replies class!
	 *                                     or in List class, below.
	 * e.g. You need tea and biscuits and you are meeting your brother at 7pm.
	 */
	public Answer a = new Answer();
	
	public  Reply   answer( String ans ) {
		if (!ans.equals( Shell.IGNORE )) {
			if (!a.isAppending())
				a = new Answer(); // a.nswer = new Strings();
			a.add( ans );
			// type is dependent on answer
			cache = null;
			type( (type == UDU) ? UDU : a.type() );
		}
		return this;
	}
	
	/** Format
	 * 
	 */
	private Format f = new Format();
	
	public  boolean verbose() { return !f.shrt(); }
	public  void    verbose( boolean v ) { f.shrt( v );}

	public  Reply   format( String s ) {
		cache = null; //de-cache any previous reply
		f.ormat( s );

		if (new Strings( s ).areLowerCase() &&
			 a.none() &&
			 !s.contains("...")) {
			audit.debug( "overwriting answer with "+ s );
			answer( s ); // overwrite answer!
		}
		return this;
	}
	private String encache() {
		if (null == cache) {
			Strings reply = new Strings( say() ).append( f.ormat());
			if (0 == reply.size())
				reply = new Strings( a.valueOf() ); // use the raw answer

			if (reply.size() == 0) // was: a.toString().equals( "" )
				reply = new Strings( dnu() );
			else if (reply.contains( Strings.ELLIPSIS )) // if required put in answer (verbatim!)
				reply.replace( Strings.ellipsis, new Strings( a.toString() ));
			else if (reply.contains( Answer.placeholder() ))
				reply.replace( Answer.placeholderAsStrings(), new Strings( a.toString() ));

			// ... then post-process:
			cache = Utterance.externalise( reply, isVerbatim() );
		}
		return cache;
	}
	private void handleDNU( Strings utterance ) {
		if (Audit.detailedOn) audit.in( "handleDNU", utterance.toString( Strings.CSV ));
		verbatimIs( true );
		if (Shell.terminators().get( 0 ).equals( Shell.terminatorIs( utterance )))
			utterance = Shell.stripTerminator( utterance );
		
		// Construct the DNU format
		format( Reply.dnu() + ", ..." );
		answer( utterance.toString());
		
		/* Take this out for the moment... ...needs more thought:
		 * if (!strangeThought.equals( "" ))
		 *	fmt.add( " when thinking about "+ strangeThought());
		 */
		
		verbatimIs( false );
		if (Audit.detailedOn) audit.out();
	}
	public String toString( Strings utterance ) {
		String reply = encache();
		if (understood( Reply.DNU != type() )) {
			if (!repeated())
				previous( reply ); // never used
			;
		} else
			handleDNU( Utterance.previous() );
		return reply;
	}
	public String toString() { return encache(); }
	
	public void conclude( Strings thought ) {
		strangeThought("");

		if ( DNU == type()) {
			/* TODO: At this point do I want to cancel all skipped signs? 
			 * Or just check if we've skipped any signs and thus report 
			 * this as simply a warning not an ERROR?
			 */
			// put this into reply via Reply.strangeThought()
			audit.ERROR( "Strange thought: I don't understand: '"+ thought.toString() +"'" );
			strangeThought( thought.toString() );
			// remove strange thought from Reply - just say DNU
			if (Allopoiesis.disambFound()) {
				audit.ERROR( "Previous ERROR: maybe just run out of meanings?" );
				strangeThought("");
			}
			type( FAIL );
		
		} else if ( NO == type() && a.toString().equalsIgnoreCase( ik()))
			answer( yes());
	}
	public static void main( String args[] ) {
		Audit.allOn();

		Reply.dnu( "Pardon?" );
		Reply.dnk( "Dunno" );
		Reply.no(  "No" );
		Reply.yes( "Yes" );
		
		Reply r = new Reply();
		audit.log( "Initially: "+ r.toString());
		r.format( "ok" );
		audit.log( "Initially2: "+ r.toString());
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
