package opt.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ListIterator;
import java.util.Scanner;

import org.enguage.Enguage;
import org.enguage.repertoires.concepts.Concept;
import org.enguage.sign.Sign;
import org.enguage.sign.factory.Spoken;
import org.enguage.sign.factory.Written;
import org.enguage.sign.object.sofa.Overlay;
import org.enguage.sign.object.sofa.Value;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;
import org.enguage.util.sys.Fs;

/**
 * The UnitTest class runs all/selected tests from etc/test/*.txt
 */
public class UnitTest {
	private UnitTest() {}
	
	private static final Audit          audit = new Audit( "UnitTest" );

	private static final String   UT_LOCATION = "./selftest";
	
	private static final String COMMENT_START = "#";
	private static final String    TEST_START = "]";
	private static final String     LINE_TERM = ".";
	private static final String     IN_UR_SEP = ":";
	private static final String  IN_REPLY_SEP = "\\|"; // doesn't like '|'
	
	private static final String      LOCATION = "etc";
	private static final String      DICT_DIR = LOCATION+File.separator+ Concept.DICT +File.separator;
	private static final String      TEST_DIR = LOCATION+File.separator+ "test" +File.separator;
	private static final String       REP_DIR = LOCATION+File.separator+Concept.RPTS+File.separator;
	private static final String      TEST_EXT = Concept.TEXT_EXT;
	private static final String      DICT_EXT = Concept.ENTRY_EXT;
	private static final String       REP_EXT = Concept.REPT_EXT;
	private static final String      WIKI_EXT = ".wiki";
	
	private static final String   TEST_PROMPT = "\nuser> ";
	private static final String  REPLY_PROMPT = "enguage> ";
	
	public  static void test( String  cmd ) {test( cmd, null );}
	public  static void test( String  cmd, String expected ) {test( cmd, expected, null );}
	private static void test( String  cmd, String expected, String unexpected ) {
		

		// decapitalise the first letter in command...
		char capital = cmd.charAt( 0 );
		if (Character.isUpperCase( capital ))
			cmd = Character.toLowerCase( capital ) + cmd.substring( 1 );


		// expected == null => don't check reply, expected == '-' => silent!
		if (expected == null || !expected.equals( "-" ))
			Audit.log( TEST_PROMPT+ cmd +".");
		
		String reply = Enguage.get().mediate( cmd );

		if (expected == null || !expected.equals( "-" )) {
		
			if (expected == null || new Strings(reply).equals( new Strings( expected )))
				Audit.passed( REPLY_PROMPT+ reply +"." );// 1st success
				
			else if (unexpected == null) {              // no second chance
				// Repertoires.signs().show();
				audit.FATAL(
					"reply: '"+    reply    +"',\n              "+
					"expected: '"+ expected +"' "
				);
		
			} else if (new Strings(reply).equals( new Strings( unexpected )))
				Audit.passed( REPLY_PROMPT+ reply +".\n" );
			
			else                                        // second chance failed too!
				//Repertoire.signs.show()
				audit.FATAL(            "reply: '"+ reply      +"'\n"+
				       "              expected: '"+ expected   +"'\n"+
				       "           alternately: '"+ unexpected
				);
	}	}
	
	private static void runTestLine( String line ) {
		line = line.substring( 0, line.length() - 1 ); // remove "."
		String[] values = line.split( IN_UR_SEP );
		if (1 == values.length)
			test( values[ 0 ].trim());
		else {	
			String[] replies = values[ 1 ].split( IN_REPLY_SEP );
			if (replies.length == 1)
				test( values[0].trim(), replies[ 0 ].trim());
			
			else if (replies.length == 2)
				test( values[0].trim(), replies[ 0 ].trim(), replies[ 1 ].trim());
			
			else  if (replies.length == 0)
				audit.FATAL( "Too few replies provided" );
			
			else
				audit.FATAL( "Too many replies ("+ replies.length +") provided" );
	}	}

	private static String fullFname( String dir, String fname ) {
		return dir.equals(DICT_DIR) // dictionaries are alphabetic: dict/w/word
				? dir + fname.charAt( 0 ) +File.separator+ fname
				: dir + fname;
	}
	private static boolean runTestFile( String dir, String fname ) {
		boolean embTest = !dir.equals( TEST_DIR );
		String marker = embTest ? TEST_START : COMMENT_START;
		try (Scanner file = new Scanner( new File( fullFname( dir, fname ) ))) {
			StringBuilder utterance = new StringBuilder();
			while (file.hasNextLine()) {
				String line = file.nextLine();
				String[] pieces = line.split( marker );
				if ((!embTest && pieces.length > 0) ||
					( embTest && pieces.length > 1)   ) {
					line = " " + pieces[embTest?1:0].trim();
					utterance.append( line );
					
					// found a full line?
					if (line.endsWith( LINE_TERM )) {
						runTestLine( utterance.toString() );
						utterance = new StringBuilder();
						
			}	}	}
			return true;
		} catch (FileNotFoundException ignore) {/*ignore*/}
		return false;
	}

	/*
	 * Full self-test...
	 */	
	private static void doTheseUnitTests( Strings tests ) {

		// remove old test data
		String fsys = UT_LOCATION;
		Fs.root( fsys );

		//run test groups
		Audit.interval(); // reset timer
		int testGrp = 0;

		for (String test : tests) {
			if (!Fs.destroy( fsys + "/uid" )) // preserve wiki
				audit.FATAL( "failed to remove old database - "+ fsys );
			
			Audit.title( "TEST: "+ test );
			
			// true=code before comment, false=
			if (runTestFile( TEST_DIR, test +WIKI_EXT ) || // find wiki over normal tests
			    runTestFile( DICT_DIR, test +DICT_EXT ) || // find dictionary over old tests
			    runTestFile( TEST_DIR, test +TEST_EXT ) ||
			    runTestFile(  REP_DIR, test +REP_EXT  )    )
				testGrp++;
		}
		Audit.log( "\n"+ testGrp +" test group(s) found" );
		Audit.PASSED();
	}

	public static Strings listUnitTests( String dirname, String ext ) {
		Strings dirlist = new Strings( new File( dirname ).list() );
		Strings unitTests = new Strings();
		ListIterator<String> li = dirlist.listIterator();
		if (dirname.equals( DICT_DIR ))
			while (li.hasNext())
				unitTests.addAll(
						listUnitTests( 
								dirname +File.separator+ li.next() +File.separator, 
								ext
				)		);

		else
			while (li.hasNext()) {
				String test = li.next();
				if (test.endsWith( ext ))
					unitTests.add( test.substring( 0, test.length()-ext.length() ));
			}
		return unitTests;
	}

	private static void doAllUnitTests() {
		Strings unitTests = new Strings();
		unitTests.addAll( listUnitTests( TEST_DIR, TEST_EXT ));
		unitTests.addAll( listUnitTests( DICT_DIR, DICT_EXT ));
	    unitTests.addAll( listUnitTests(  REP_DIR,  REP_EXT ));
		doTheseUnitTests( unitTests );
	}
		
	public static void doAllWikiTests() {
		Strings unitTests = new Strings();
		unitTests.addAll( listUnitTests( TEST_DIR, WIKI_EXT ));
		doTheseUnitTests( unitTests );
	}
		
	private static void createVoicedSign() {
		// create a sign in overlay space
		audit.in("createVoicedSign", "");
		Fs.root( UT_LOCATION );
		Overlay.attach("uid");
		
		// a pre-configured sign for the overlay.txt
		String string = "On \"SOMEONE is the PHRASE-SOMETHING\":\n" +
				"\tis SOMEONE a person;\n" +
				"\tif not, reply \"sorry , SOMEONE is not a person\";\n" +
				"\tperform \"variable set martin best\";\n" +
				"\treply \"ok , dokey\".";

		// create a sign and save it (as a value)
		Sign sign = new Written( string ).toSign();
		new Value( Spoken.CONCEPTS, sign.pattern().toFilename() )
				.set( sign.toString() );
		audit.out();
	}
	
	public static void main( String[] args ) {
		
		Strings    cmds = new Strings( args );
		String     fsys = Enguage.doArgs( cmds, UT_LOCATION );

		// The overlay test shows that predefined signs are 
		// loaded on startup: a sign is written into a blank
		// overlay space, and is read in the normal way :-/
		createVoicedSign();
					
		Enguage.set( new Enguage( fsys ));
				
		String cmd = cmds.isEmpty() ? "":cmds.remove( 0 );
		
		if (     cmd.equals(  "-t"    ) ||
		         cmd.equals( "--test" )   )
			doAllUnitTests();
		
		else if (cmd.equals(  "-w"    ) ||
		         cmd.equals( "--wiki" )   )
			doAllWikiTests();
		
		else if (cmd.equals(  "-T"    ))
			doTheseUnitTests( cmds );

		else if (cmd.equals( "" )) {
			Overlay.attach( "uid" );
			Enguage.shell().aloudIs( true ).run();
		
		} else {
			// Command line parameters exists...
			// - remove full stop, if one given -
			cmds = new Strings( cmds.toString() );
			
			if (cmds.get( cmds.size()-1 ).equals( "." ))
				cmds.remove( cmds.size()-1 );

			// ...reconstruct original commands and interpret
			cmds.prepend( cmd );
			test( cmds.toString(), null );
	}	}
}
