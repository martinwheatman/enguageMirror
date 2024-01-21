package org.enguage.sign.symbol.reply;

import org.enguage.sign.Config;
import org.enguage.sign.object.sofa.Perform;
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
	
	private static final Audit                audit = new Audit( "Reply" );
	public  static final String DEFAULT_PLACEHOLDER = "whatever";

	private boolean repeated = false;
	public  void    repeated( boolean s ) {repeated = s;}
	public  boolean repeated()            {return repeated;}
	
	// This is used to control Intentions.mediate() loop
	private boolean done = false;
	public  Reply   doneIs( boolean b ) {done = b; return this;}
	public  boolean isDone() {return done;}
	
	// --- say list
	private static  Strings say = new Strings();
	public  static  Strings say() {return say;}
	public  static  void    say( Strings sa ) {
		if (sa == null)  // null to reset it!
			say = new Strings();
		else
			say.addAll( Terminator.addTerminator( sa ));
	}
	
	/* ------------------------------------------------------------------------
	 * Response type
	 */
	public enum Type {
		E_DNU, // DO NOT UNDERSTAND
		E_UDU, // user does not understand
		E_DNK, // NOT KNOWN -- init
		E_SOZ, // SORRY -- -ve
		E_NO,  // FALSE -- -ve
		E_OK,  // TRUE  -- +ve identical to 'yes'
		E_CHS; // narrative verdict - meh!
	}

	private Type  type = Type.E_DNU;
	public  Type  type() {return type;}
	public  Reply type( Type t ) {type = t; return this;}
	
	public  static Type stringsToResponseType( Strings uttr ) {
		     if (uttr.begins( Config.yes()     )) return Type.E_OK;
		else if (uttr.begins( Config.okay()    )) return Type.E_OK;
		else if (uttr.begins( Config.notOkay() )) return Type.E_SOZ;
		else if (uttr.begins( Config.dnu()     )) return Type.E_DNU;
		else if (uttr.begins( Config.udu()     )) return Type.E_UDU;
		else if (uttr.begins( Config.no()      )) return Type.E_NO;
		else if (uttr.begins( Config.dnk()     )) return Type.E_DNK;
		else return Type.E_CHS;
	}
	public static Reply.Type stringToResponseType( String s ) {
		// This sets type to first non-NK type
		     if (s.toLowerCase().startsWith( Config.yesStr()    )) return Reply.Type.E_OK;
		else if (s.toLowerCase().startsWith( Config.okayStr()   )) return Reply.Type.E_OK;
		else if (s.toLowerCase().startsWith( Config.noStr()     )) return Reply.Type.E_NO;
		else if (s.toLowerCase().startsWith( Config.dnuStr()    )) return Reply.Type.E_DNU;
		else if (s.toLowerCase().startsWith( Config.notOkayStr())) return Reply.Type.E_SOZ;
		else if (s.toLowerCase().startsWith( Config.dnkStr()    )) return Reply.Type.E_DNK;
		else return Reply.Type.E_CHS;
	}
	
	public boolean isFelicitous() {
		return  Type.E_OK  == type ||
				Type.E_CHS == type;
	}

	/** Answer - 
	 * e.g. [ "a cup of coffee", "a packet of biscuits" ]
	 */
	private static Strings placeholderAsStrings = new Strings( Reply.DEFAULT_PLACEHOLDER );
	public  static Strings placeholderAsStrings() {return placeholderAsStrings;}
	private static String  placeholder = Reply.DEFAULT_PLACEHOLDER;
	public  static String  placeholder() {return placeholder;}
	public  static void    placeholder( String ph ) {placeholderAsStrings = new Strings( placeholder = ph );}

	/* ------------------------------------------------------------------------
	 * Answer - a simple string "42" or list "coffee and biscuits"?
	 */
	private String answer = "";	
	public  Reply  answer( String ans ) {
		if (!ans.equals( Perform.S_IGNORE ))
			answer = ans;
		return this;
	}
	public  Reply  answerReset() {answer = ""; return this;}
	public  String answer() {return answer;}
	
	/* ------------------------------------------------------------------------
	 * Format - the shape of the reply "x y Z" intention, e.g. x y 24
	 */
	private Strings format = new Strings();

	public  String  format() {return format.toString();}
	public  Reply   format( String  f ) {return format( new Strings( f ));}
	public  Reply   format( Strings f ) {
		format = Context.deref( f );
		return this;
	}
	
	private static  boolean verbatim = false; // set to true in handleDNU()
	private static  boolean isVerbatim() { return verbatim; }
	private static  void    verbatimIs( boolean val ) { verbatim = val; }

	/* previous() is used to retrieve the reply from the previous thought. It is
	 * used in implementing imagination.  If the imagination session goes ok,
	 * we need the reply from that session. Was implemented with the equiv 
	 * intention in previous C incarnation.
	 */
	private static  Strings previous = new Strings( "" );
	public  static  Strings previous( Strings rep ) { return previous = rep; }
	public  static  Strings previous() { return previous; }
	
	public void dnu( Strings thought ) {
		verbatimIs( true ); // repeat exactly on DNU
		// Construct the DNU format
		format( new Strings( Config.dnu() + ", ..." ));
		answer( thought.toString() );
		// must come after answer()
		type( Type.E_SOZ );
		verbatimIs( false );
	}
	public void idk() {
		format( Config.dnkStr());
		type( Reply.Type.E_DNK );
		answerReset();
	}
	

	private Strings replyToStrings() {
		if (format.isEmpty()) {
			format = new Strings( answer() ); // use the raw answer
			if (format.isEmpty()) // so a was equal to ""
				format = Config.dnu();
			
		} else
			if (format.contains( Strings.ELLIPSIS )) // if required put in answer (verbatim!)
			format.replace( Strings.ellipsis, answer() );
		
		else if (format.contains( Reply.placeholder() ))
			format.replace( Reply.placeholderAsStrings(), answer() );
		
		return  Utterance.externalise( format, isVerbatim() );
	}

	public Strings sayThis() {
		Strings reply = replyToStrings();
		if (Config.understoodIs( Type.E_DNU != type() )) {
			// used in disambiguation ordering :/
			if (!repeated())
				previous( reply ); // never used
			
		} else
			dnu( Utterance.previous() );
		
		return reply;
	}
	public String toString() {return replyToStrings().toString();}
	
	public static void main( String[] args ) {
		Audit.on();

		Reply r = new Reply();
		audit.debug( "Initially: "+ r.toString());

		Config.dnu( "Pardon?" );
		Config.dnk( "Dunno" );
		Config.no(  "No" );
		Config.yes( "Yes" );
		
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
