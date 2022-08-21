package org.enguage.signs.vehicle.reply;

import java.util.Locale;

import org.enguage.signs.interpretant.Redo;
import org.enguage.signs.vehicle.Utterance;
import org.enguage.signs.vehicle.when.Moment;
import org.enguage.signs.vehicle.when.When;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Attributes;
import org.enguage.util.attr.Context;
import org.enguage.util.sys.Shell;

public class Reply { // a reply is basically a formatted answer
	
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

	private static  String  andConjunction = new String( "and" );
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
	
	private static  Strings say = new Strings();
	public static   Strings say() { return say; }
	public static  void    say( Strings sa ) {
		if (sa == null)  // null to reset it!
			say = new Strings();
		else
			say.addAll( Shell.addTerminator( sa ));
	}
	
	private Strings cache = null;
	
	/*
	 * Response
	 */
	private Response response = new Response();
	public  int      response() {return response.value();} 
	public  Reply    response( int i ) {response.value( i ); return this;}
	public  Reply    response( Strings strs ) {response.value( strs ); return this;}

	public  boolean  felicitous() {return response.value() >= Response.OK;}

	/* Answer:
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
			response.value( (response.value() == Response.UDU) ? Response.UDU : a.type());
		}
		return this;
	}
	public  Reply   rawAnswer( String rc, String method ) {
		answer( Moment.valid( rc ) ? // 88888888198888 -> 7pm
					new When( rc ).rep( Response.dnkStr() ).toString()
					: rc.equals( "" ) &&
					  (method.equals( "get" ) ||
				       method.equals( "getAttrVal" )) ?
						Response.dnkStr()
						: rc.equals( Shell.FAIL ) ?
							Response.failureStr()
							: rc.equals( Shell.SUCCESS ) ?
								Response.successStr()
								: rc );
		return this;
	}
	
	/* 
	 * Format
	 */
	private Format f = new Format();
	
	public  boolean verbose() { return !f.shrt(); }
	public  void    verbose( boolean v ) { f.shrt( v );}

	public static boolean isLiteral( Strings sa ) { return sa.areLowerCase() && !sa.contains( Strings.ELLIPSIS );}
	public  Reply   format( String  format ) { return format( new Strings( format ));}
	public  Reply   format( Strings format ) {
		cache = null; //de-cache any previous reply
		f.ormat( format );
		if (isLiteral( format ) && a.none()) // remove a.none?
			answer( format.toString() ); // overwrite answer!
		return this;
	}
	private Strings encache() {
		if (null == cache)
			cache = Utterance.externalise(
						a.injectAnswer( f.ormat() ),
						isVerbatim()
					);
		return cache;
	}
	private Strings handleDNU( Strings utterance ) {
		audit.in( "handleDNU", utterance.toString( Strings.CSV ));
		verbatimIs( true );
		if (Shell.terminators().get( 0 ).equals( Shell.terminatorIs( utterance )))
			utterance = Shell.stripTerminator( utterance );
		
		// Construct the DNU format
		format( new Strings( Response.dnu() + ", ..." ));
		answer( utterance.toString());
		
		/* Take this out for the moment... ...needs more thought:
		 * if (!strangeThought.equals( "" ))
		 *	fmt.add( " when thinking about "+ strangeThought());
		 */
		
		verbatimIs( false );
		if (Audit.detailedOn) audit.out();
		return audit.out( cache = Utterance.externalise(
				a.injectAnswer( f.ormat() ),
				isVerbatim()
			));
	}
	public Strings toStrings() {
		Strings reply = encache();
		if (understoodIs( Response.DNU != response.value() )) {
			if (!repeated())
				previous( reply ); // never used
			;
		} else
			reply = handleDNU( Utterance.previous() );
		return reply;
	}
	public String toString() {return encache().toString();}
	
	public Reply conclude( String thought ) {
		strangeThought("");

		if ( Response.DNU == response.value()) {
			// put this into reply via Reply.strangeThought()
			audit.ERROR( "Strange thought: I don't understand: '"+ thought +"'" );
			strangeThought( thought );

			// remove strange thought from Reply - just say DNU
			if (Redo.disambFound()) {
				audit.ERROR( "Previous ERROR: maybe just run out of meanings?" );
				strangeThought("");
			}

			// Construct the DNU format
			format( new Strings( Response.dnu() + ", ..." ));
			answer( thought );
			
			response.value( Response.FAIL );
		
		}
		return this;
	}
	public static void main( String args[] ) {
		Audit.allOn();

		Reply r = new Reply();
		Audit.log( "Initially: "+ r.toString());

		Response.dnu( "Pardon?" );
		Response.dnk( "Dunno" );
		Response.no(  "No" );
		Response.yes( "Yes" );
		
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
