package org.enguage.util.sys;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.enguage.Enguage;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;
import org.enguage.util.strings.Terminator;

public class Shell {

	static  Audit audit = new Audit( "Shell" );
	
	private String  prompt;
	public  String  prompt() { return prompt; }
	public  Shell   prompt( String p ) { prompt = p; return this; }
	
	private boolean aloud = true;
	public  boolean isAloud() { return aloud; }
	public  Shell   aloudIs( boolean is ) { aloud = is; return this; }
	
	private String prog;
	public  String name() { return prog; }
	public  Shell  name( String nm ) { prog = nm; return this; }
	
	private String who;
	private String dates;
	private String copyright;
	public  String copyright() { return prog +" (c) "+ (copyright!=null? copyright : who +", "+ dates); }
	public  Shell  copyright( String wh, String dts ) { who = wh; dates = dts; return this; }
	public  Shell  copyright( String statement ) { copyright = statement; return this; }

	private static long then = new GregorianCalendar().getTimeInMillis();
	public  static  long interval() {
		long now = new GregorianCalendar().getTimeInMillis();
		long rc = now - then;
		then = now;
		return rc;
	}
	
	public Shell( String name, String copyright ) {
		name( name ).prompt( "> " ).copyright( copyright );
	}
	public Shell( String name ) {
		name( name ).prompt( "> " ).copyright( "Martin Wheatman", "2001-4, 2011-20" );
	}
	public Shell( String name, Strings args ) {this( name );}
	
	private void doLine( String line, String from, String to ) {
		//remove Byte order mark...
		if (line.startsWith("\uFEFF")) { line = line.substring(1); }

		if (!line.equals("\n")) {
			Strings stream = new Strings();
			// truncate comment -- only in real files
			int i = line.indexOf( '#' );
			if (-1 != i) line = line.substring( 0, i );
			
			// if we're converting on the fly, e.g. want -> need
			if (from != null) line = line.replace( from, to );
			
			// will return "cd .." as ["cd", ".", "."], not ["cd" ".."] -- "cd.." is meaningless!
			// need new stage of non-sentence sign processing
			stream.addAll( new Strings( line ));
			ArrayList<Strings> sentences = stream.divide( Terminator.terminators() );
			if ( sentences.size() > 1 ) {
				Strings sentence = sentences.remove( 0 );
				stream = Strings.combine( sentences );
				if (!sentence.isEmpty()) {
					// strip sentence of its terminator, if "."
					// Expand sentence here...
					for (Strings s : expandSemicolonList( sentence ))
						Audit.log(
							Enguage.get().mediate( ""+s )
						);
		}	}	}
	}
	public void interpret( InputStream fp, String from, String to ) { // reads file stream and "interpret()"s it
		if (fp==System.in) System.err.print( prompt() );
		
		try (BufferedReader br = new BufferedReader( new InputStreamReader( fp ))) {
			String line;
			while ((line = br.readLine()) != null) {
				doLine( line, from, to );
				if (fp==System.in) System.err.print( prompt() );
			}
		} catch (java.io.IOException e ) {
			audit.error( "IO error in Shell::interpret(stdin);" );
	}	}
	public void run() { interpret( System.in, null, null ); } // we're not converting on-the-fly!
	
	private static void rejig(int rcSz, Strings primary, String connector, ArrayList<Strings>rc) {
		for (int i=0; i<rcSz; i++) {
			Strings tmp = new Strings();
			if (i==0)
				tmp.addAll( primary );
			else
				tmp.add( 0, connector );
			tmp.add( "," );
			tmp.addAll( rc.get( i ));
			rc.set( i, tmp );
	}	}
	private static List<Strings> expandSemicolonList( Strings sentence ) {
		/*  "on one: do two; do three; and, do four." =>
		 *  [ "on one, do two.", "and on one, do three.", "and on one, do four." ]
		 *  "and" many be replace by, for example "or", "then", "also"
		 */
		ArrayList<Strings> rc = new ArrayList<>();
		
		// create a primary and a list of secondaries
		boolean isPrimary = true;
		Strings primary = new Strings();    // On "a B c" (:)
		Strings secondary = new Strings (); // if so, do something[;|.]
		for (String s : sentence) 
			if (s.equals(":")) {
				isPrimary = false;
			} else if (s.equals(";")) {
				rc.add( secondary );
				secondary = new Strings();
			} else if (isPrimary) {
				primary.add( s );
			} else {
				secondary.add( s );
			}
		if (!secondary.isEmpty()) rc.add( secondary );
		
		// if we've found no semi-colon separated list...
		if (rc.isEmpty()) {
			// ...just pass back the original list
			rc.add( sentence );
		} else {
			// remove connector from last in list
			int rcSz = rc.size();
			String connector = "then"; // default: this is the only one used!
			Strings lastList = rc.get( rcSz-1 );
			if ( lastList.size() > 2 && lastList.get( 1 ).equals( ",") ) {
				connector=lastList.remove( 0 ); // remove connector
				lastList.remove( 0 );           // remove ","
				rc.set( rcSz-1, lastList );     // replace this last item
			}
			// re-jig list, adding-in primary, and the connector on subsequent lists
			rejig(rcSz, primary, connector, rc);
		}
		//audit.OUT( rc )
		return rc;
	}
	private static void test( String string ) {
		Strings list = new Strings( string );
		List<Strings> listOfLists = expandSemicolonList( list );
		audit.debug("expanded list is:");
		for (Strings s : listOfLists ) {
			audit.debug( ">>>"+ s.toString( Strings.SPACED ) );
	}	}
	public static void main( String[] args) {
//		test( "I need coffee" )
		test( "on this, here there and everywhere" );
		test( "on this: do here; there; and, everywhere" );
//		test( "On \"X needs PHRASE-Y\":"
//				+"	if Y exists in X needs list, reply \"I know\";"
//				+"	if not, add Y to X needs list;"
//				+"	then, reply \"ok, X needs Y\"." )
//		test( "On \"X needs PHRASE-Y\":"
//				+"	if Y exists in X needs list, reply \"I know\";"
//				+"	if not, add Y to X needs list;"
//				+"	reply \"ok, X needs Y\"." )
}	}
