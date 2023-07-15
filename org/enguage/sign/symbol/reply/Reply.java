package org.enguage.sign.symbol.reply;

import java.util.Locale;

import org.enguage.sign.symbol.Utterance;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Attributes;
import org.enguage.util.attr.Context;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;
import org.enguage.util.strings.Terminator;

public class Reply {
	// a reply is basically a formatted answer...
	// [ answer='42' +
	//   format="the answer to life, the universe and everything is ..."; ] 
	
	private static  Audit audit = new Audit( "Reply" );

	private static  boolean verbatim = false; // set to true in handleDNU()
	public  static  boolean isVerbatim() { return verbatim; }
	public  static  void    verbatimIs( boolean val ) { verbatim = val; }
	
	private static  boolean understood = true;
	public  static  boolean understoodIs( boolean was ) { return understood = was;}
	public  static  boolean isUnderstood() { return Reply.understood; }
	
	private static  String  strangeThought = "DNU";
	public  static  void    strangeThought( String thought ) { strangeThought = thought; }
	public  static  String  strangeThought(){ return strangeThought; }

	private static  String  repeatFormat = "i said, ... .";
	public  static  void    repeatFormat( String s ) { repeatFormat = s.toLowerCase( Locale.getDefault() ); }
	public  static  String  repeatFormat() { return repeatFormat; }

	private static  String  andConjunction = "and";
	public  static  void    andConjunction( String s ) { andConjunction = s.toLowerCase( Locale.getDefault() ); }
	public  static  String  andConjunction() { return andConjunction; }

	private static  Strings andListFormat = new Strings( ", /, and ", '/' );
	public  static  void    andListFormat( String s ) { andListFormat = new Strings( s, listSep().charAt( 0 )); }
	public  static  Strings andListFormat() { return andListFormat; }

	private static  Strings orConjunctions = new Strings( ", or" );
	public  static  void    orConjunctions( Strings sa ) { orConjunctions = sa; }
	public  static  Strings orConjunctions() { return orConjunctions; }
	
	private static  Strings orListFormat = new Strings( ", /, or ", '/' );
	public  static  void    orListFormat( String s ) { orListFormat = new Strings( s, listSep().charAt( 0 )); }
	public  static  Strings orListFormat() { return orListFormat; }

	private static  Strings referencers = new Strings( "the" );
	public  static  void    referencers( Strings sa ) { referencers = sa; }
	public  static  Strings referencers() { return referencers; }

	private static  String  listSep = "/";
	public  static  void    listSep( String s ) { listSep = s; }
	public  static  String  listSep() { return listSep; }

	/* previous() is used to retrieve the reply from the previous thought. It is
	 * used in implementing imagination.  If the imagination session goes ok,
	 * we need the reply from that session. Was implemented with the equiv 
	 * intention in previous C incarnation.
	 */
	private static  Strings previous = new Strings( "" );
	public  static  Strings previous( Strings rep ) { return previous = rep; }
	public  static  Strings previous() { return previous; }

	private boolean repeated = false;
	public  void    repeated( boolean s ) { repeated = s; }
	public  boolean repeated() { return repeated; }
	
	private boolean done = false;
	public  Reply   doneIs( boolean b ) { done = b; return this; }
	public  boolean isDone() { return done; }
	
	// --- say list
	private static  Strings say = new Strings();
	public  static  Strings say() {return say;}
	public  static  void    say( Strings sa ) {
		if (sa == null)  // null to reset it!
			say = new Strings();
		else
			say.addAll( Terminator.addTerminator( sa ));
	}
	
	// to interact with the 'say' list - as String or Strings
	private static String  accumulateCmdStr = "remember this";
	private static Strings accumulateCmds = new Strings( accumulateCmdStr );
	public  static String  accumulateCmdStr() {return accumulateCmdStr;}
	public  static Strings accumulateCmds() {return accumulateCmds;}
	public  static void    accumulateCmdStr(String ac) {
		accumulateCmdStr = ac;
		accumulateCmds = new Strings( ac );
	}
	public  static void    accumulateCmdStr(Strings ac) {
		accumulateCmds = ac;
		accumulateCmdStr = ac.toString();
	}

	private static String propagateReplyStr = "say so";
	public  static String propagateReplyStr() {return  propagateReplyStr;}
	public  static void   propagateReplyStr(String pr) {
		propagateReplyStr = pr;
		propagateReplys = new Strings( pr );
	}
	private static Strings propagateReplys = new Strings( propagateReplyStr );
	public  static Strings propagateReplys() {return  propagateReplys;}
	public  static void    propagateReplys(Strings pr) {
		propagateReplys = pr;
		propagateReplyStr = pr.toString();
	}
	
	/* ------------------------------------------------------------------------
	 * Response
	 */
	private Response response = new Response();
	public  int      response() {return response.type();} 
	public  Reply    response( int i ) {response.type( i ); return this;}
	public  Reply    response( Strings strs ) {
		response.setType( strs );
		return this;
	}

	public  boolean  felicitous() {return response.type() >= Response.N_OK;}

	/* Answer:
	 * Multiple answers should now be implemented in a Replies class!
	 *                                     or in List class, below.
	 * e.g. You need tea and biscuits and you are meeting your brother at 7pm.
	 */
	private Answer answer = new Answer();
	public  Answer answer() {return answer;}
	public  Reply  answer( String ans ) {
		// ans = "FALSE" OR "[ok, ]this is an answer"
		if (ans != null && !ans.equals( Response.IGNORE )) {
			if (!answer.isAppending())
				answer = new Answer(); // a.nswer = new Strings()
			answer.setType( ans );
			answer.add( ans );
			// type is dependent on answer
			response.type( response.type() == Response.N_UDU ? Response.N_UDU : answer.type());
		}
		return this;
	}
	public  Reply answerReset() {answer = new Answer(); return this;}
	
	
	/* 
	 * Format - the value of the reply "x y Z" intention, e.g. x y 24
	 */
	private Strings format = new Strings();
	public  String  format() {return format.toString();}
	public  Reply   format( String  f ) { return format( new Strings( f ));}
	public  Reply   format( Strings f ) {
		format = Context.deref( f );
		return this;
	}
	
	/*
	 * toString() ... 
	 */
	private Strings replyToString() {
		return  Utterance.externalise(
					answer.injectAnswer( format ),
					isVerbatim()
				);
	}
	private Strings handleDNU( Strings utterance ) {
		audit.in( "handleDNU", utterance.toString( Strings.CSV ));
		verbatimIs( true );
		utterance = Terminator.stripTerminator( utterance );
		
		// Construct the DNU format
		format( new Strings( Response.dnu() + ", ..." ));
		answer( utterance.toString());
		
		/* Take this out for the moment... ...needs more thought
		 * if !strangeThought.equals( "" )
		 *	fmt.add( " when thinking about "+ strangeThought())
		 */
		
		verbatimIs( false );
		return audit.out( replyToString() );
	}
	public Strings toStrings() {
		Strings reply = replyToString();
		if (understoodIs( Response.N_DNU != response.type() )) {
			if (!repeated())
				previous( reply ); // never used
			
		} else
			reply = handleDNU( Utterance.previous() );
		return reply;
	}
	public String toString() {return replyToString().toString();}
	
	public Reply conclude( String thought ) {
		audit.in("conclude", "");
		strangeThought("");

		if (Response.N_DNU == response.type()) {
			// put this into reply via Reply.strangeThought()
			audit.error( "Strange thought: I don't understand: '"+ thought +"'" );
			strangeThought( thought );

			// Construct the DNU format
			format( new Strings( Response.dnu() + ", ..." ));
			answer( thought );
			
			response.type( Response.N_FAIL );
		}
		audit.out();
		return this;
	}
	public static void main( String[] args ) {
		Audit.on();

		Reply r = new Reply();
		audit.debug( "Initially: "+ r.toString());

		Response.dnu( "Pardon?" );
		Response.dnk( "Dunno" );
		Response.no(  "No" );
		Response.yes( "Yes" );
		
		audit.debug( "Initially: "+ r.toString());
		r.format( new Strings( "ok" ));
		audit.debug( "Initially2: "+ r.toString());
		r.answer( "42" );
		audit.debug( "THEN: "+ r.toString());
		r.answer( "53" );
		audit.debug( "W/no format:"+ r.toString());
		
		r.format( new Strings( "The answer to X is ..." ));
		
		Attributes attrs = new Attributes();
		attrs.add( new Attribute( "x", "life the universe and everything" ));
		Context.push( attrs );
		audit.debug( "Context is: "+ Context.valueOf());
		
		audit.debug( "Finally:"+ r.toString());
}	}
