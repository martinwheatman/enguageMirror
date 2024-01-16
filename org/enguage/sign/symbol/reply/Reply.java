package org.enguage.sign.symbol.reply;

import org.enguage.sign.Config;
import org.enguage.sign.object.sofa.Perform;
import org.enguage.sign.symbol.Utterance;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Attributes;
import org.enguage.util.attr.Context;
import org.enguage.util.audit.Audit;
import org.enguage.util.http.InfoBox;
import org.enguage.util.strings.Strings;
import org.enguage.util.strings.Terminator;

public class Reply {
	// a reply is basically a formatted answer...
	// [ answer='42' +
	//   format="the answer to life, the universe and everything is ..."; ] 
	
	private static  Audit audit = new Audit( "Reply" );

	private boolean repeated = false;
	public  void    repeated( boolean s ) {repeated = s;}
	public  boolean repeated()            {return repeated;}
	
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
	 * Response
	 */
	private Response      response = new Response();
	public  Response      response() {return response;}
	public  Reply         responseType( Response.Type t ) {response.type( t ); return this;}
	public  Reply         xresponse( Strings strs ) {
		response.type( Response.stringsToResponseType( strs ));
		return this;
	}

	/* Answer:
	 * Multiple answers should now be implemented in a Replies class!
	 *                                     or in List class, below.
	 * e.g. You need tea and biscuits and you are meeting your brother at 7pm.
	 */
	private Answer answer = new Answer();
	public  Answer answer() {return answer;}
	public  Reply  answer( String ans ) {
		// ans = "FALSE" OR "[ok, ]this is an answer"
		if (ans != null && !ans.equals( Perform.S_IGNORE )) {
			answer = new Answer(); // a.nswer = new Strings()
			answer.type( answer.stringToResponseType( ans ));
			answer.add( ans );
			// type is dependent on answer
			response.type( response.type() == Response.Type.E_UDU ? Response.Type.E_UDU : answer.type());
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
					Config.isVerbatim()
				);
	}
	private Strings handleDNU( Strings utterance ) {
		audit.in( "handleDNU", utterance.toString( Strings.CSV ));
		Config.verbatimIs( true );
		utterance = Terminator.stripTerminator( utterance );
		
		// Construct the DNU format
		format( new Strings( Config.dnu() + ", ..." ));
		answer( utterance.toString());
		
		/* Take this out for the moment... ...needs more thought
		 * if !strangeThought.equals( "" )
		 *	fmt.add( " when thinking about "+ strangeThought())
		 */
		
		Config.verbatimIs( false );
		return audit.out( replyToString() );
	}
	
	// Set in Config.java/config.xml
	private static Strings attributing = attributing( "according to X," );
	public  static Strings attributing() {return attributing;} 
	public  static Strings attributing( String a ) {
		attributing = new Strings( a ).reverse();
		return attributing;
	}
	
	// this is only called directly from Enguage.java - on replying to the user
	public static Strings attributeSource( Strings reply ) {
		audit.in( "attributeSource", "reply=["+ reply.toString( Strings.DQCSV) +"]");
		
		if (!InfoBox.wikiSource().equals( "" ) ) {
			// only attribute successful replies...
			if (!(reply.get(0).equalsIgnoreCase( "sorry" ) &&
			      reply.get(1).equals( "," )))
			{
				if (reply.get(0).equalsIgnoreCase( "ok" ) &&
					reply.get(1).equals( "," ))
				{
					reply.remove(0);
					reply.remove(0);
				}
				
				for (String s : attributing)
					reply.add( 0, s.equals( "X" ) ? InfoBox.wikiSource() : s );
			}
			// ...and finally, we want to scrub a source whether or not it was used!
			InfoBox.wikiSource( "" );
		}

		audit.out( reply );
		return reply;
	}

	public Strings toStrings() {
		Strings reply = replyToString();
		if (Config.understoodIs( Response.Type.E_DNU != response.type() )) {
			if (!repeated())
				Config.previous( reply ); // never used
			
		} else
			reply = handleDNU( Utterance.previous() );
		return reply;
	}
	public String toString() {return replyToString().toString();}
	
	public Reply conclude( String thought ) {
		Config.strangeThought("");

		if (Response.Type.E_DNU == response.type()) {
			// put this into reply via Reply.strangeThought()
			audit.error( "Strange thought: I don't understand: '"+ thought +"'" );
			Config.strangeThought( thought );

			// Construct the DNU format
			format( new Strings( Config.dnu() + ", ..." ));
			answer( thought );
			
			response.type( Response.Type.E_SOZ );
		}
		return this;
	}
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
