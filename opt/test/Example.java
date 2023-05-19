package opt.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ListIterator;
import java.util.Scanner;

import org.enguage.Enguage;
import org.enguage.repertoires.concepts.Concept;
import org.enguage.signs.objects.sofa.Overlay;
import org.enguage.signs.symbol.pronoun.Pronoun;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.sys.Fs;

public class Example {
	private Example() {}
	
	private static final Audit          audit = new Audit( "Example" );

	private static final String COMMENT_START = "#";
	private static final String    TEST_START = "]";
	private static final String     LINE_TERM = ".";
	private static final String     IN_UR_SEP = ":";
	private static final String  IN_REPLY_SEP = "/"; // doesn't like '|'
	
	private static final String      LOCATION = "etc";
	private static final String      DICT_DIR = LOCATION+File.separator+ Concept.DICT +File.separator;
	private static final String      TEST_DIR = LOCATION+File.separator+ "test" +File.separator;
	private static final String       REP_DIR = LOCATION+File.separator+Concept.RPTS+File.separator;
	private static final String      TEST_EXT = Concept.TEXT_EXT;
	private static final String      DICT_EXT = Concept.ENTRY_EXT;
	private static final String       REP_EXT = Concept.REPT_EXT;
	
	private static final String   TEST_PROMPT = "\nuser> ";
	private static final String  REPLY_PROMPT = "enguage> ";
	
	public  static void test( String  cmd ) {test( cmd, null );}
	public  static void test( String  cmd, String expected ) {test( cmd, expected, null );}
	private static void test( String  cmd, String expected, String unexpected ) {
		// expected == null => don't check reply, expected == '-' => silent!
		if (expected == null || !expected.equals( "-" ))
			Audit.log( TEST_PROMPT+ cmd +".");
		
		Strings reply = Enguage.get().mediate( new Strings( cmd ));

		if (expected == null || !expected.equals( "-" ))
		
			if (expected == null || reply.equalsIgnoreCase( new Strings( expected )))
				audit.passed( REPLY_PROMPT+ reply +"." );// 1st success
				
			else if (unexpected == null)                // no second chance
				//Repertoire.signs.show()
				audit.FATAL(
					"reply: '"+    reply    +"',\n             "+
					"expected: '"+ expected +"' "
				);
		
			else if (reply.equalsIgnoreCase( new Strings( unexpected )))
				audit.passed( REPLY_PROMPT+ reply +".\n" );
			
			else                                        // second chance failed too!
				//Repertoire.signs.show()
				audit.FATAL(
					"reply: '"      + reply      +"'\n             "+
					"expected: '"   + expected   +"'\n          "+
					"alternately: '"+ unexpected +"'\n          "
				);
	}
	
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

	private static String ffname( String dir, String fname ) {
		return dir.equals(DICT_DIR)
				? dir + fname.charAt( 0 ) +File.separator+ fname
				: dir + fname;
	}
	private static boolean runTestFile( String dir, String fname, boolean comments ) {
		boolean rc = false;
		String ffname = ffname( dir, fname );
		try (Scanner file = new Scanner( new File( ffname ))) {
			rc = true;
			StringBuilder sb = new StringBuilder();
			while (file.hasNextLine()) {
				String line = file.nextLine();
				String[] bits = line.split( comments ? TEST_START : COMMENT_START );
				if ((!comments && bits.length > 0) ||
					( comments && bits.length > 1)   ) {
					line = " " + bits[comments?1:0].trim();
					boolean endsInStop = line.endsWith( LINE_TERM );
					
					// split on '.'
					sb.append( line );
					if (endsInStop) {
						runTestLine( sb.toString() );
						sb = new StringBuilder();
			}	}	}	
		} catch (FileNotFoundException ignore) {/*ignore*/}
		
		return rc;
	}

	/*
	 * Full self-test...
	 */	
	private static void doUnitTests( Strings tests ) {
		Pronoun.interpret( new Strings( "add masculine martin" ));
		Pronoun.interpret( new Strings( "add masculine james" ));
		Pronoun.interpret( new Strings( "add feminine  ruth" ));

		// remove old test data
		String fsys = "./selftest";
		Fs.root( fsys );

		//run test groups
		Audit.interval(); // reset timer
		int testGrp = 0;

		for (String test : tests) {
			if (!Fs.destroy( fsys ))
				audit.FATAL( "failed to remove old database - "+ fsys );
			
			audit.title( "TEST: "+ test );
			
			if (runTestFile( TEST_DIR, test +TEST_EXT, false ) ||
			    runTestFile( DICT_DIR, test +DICT_EXT, true  ) ||
			    runTestFile(  REP_DIR, test +REP_EXT,  true  )   )
				testGrp++;
		}
		Audit.log( testGrp +" test group(s) found" );
		audit.PASSED();
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

	public static void unitTests() {
		Strings unitTests = new Strings();
		unitTests.addAll( listUnitTests( TEST_DIR, TEST_EXT ));
		unitTests.addAll( listUnitTests( DICT_DIR, DICT_EXT ));
	    unitTests.addAll( listUnitTests(  REP_DIR,  REP_EXT ));
		doUnitTests( unitTests );
	}
		
	private static String doArgs(Strings cmds) {
		// traverse args and strip switches: -v -d -H -p -s
		String fsys = Enguage.RW_SPACE;
		String cmd;
		int i = 0;
		while (i < cmds.size()) {
			
			cmd = cmds.get( i );
			
			if (cmd.equals( "-h" ) || cmd.equals( "--help" )) {
				Enguage.usage();
				System.exit( 0 );
			
			} else if (cmd.equals( "-v" ) || cmd.equals( "--verbose" )) {
				cmds.remove( i );
				Enguage.verboseIs( true );
					
			} else if (cmd.equals( "-d" ) || cmd.equals( "--data" )) {
				cmds.remove( i );
				fsys = cmds.isEmpty() ? fsys : cmds.remove( i );
				
			} else
				break;
		}
		return fsys;
	}
	
	public static void main( String[] args ) {
		Strings    cmds = new Strings( args );
		String     fsys = doArgs( cmds );

		Enguage.set( new Enguage( fsys ));
				
		String cmd = cmds.isEmpty() ? "":cmds.remove( 0 );
		
		if (cmd.equals(  "-t"    ) ||
			cmd.equals( "--test" )   )
		{
			unitTests();
			
		} else if (cmd.equals(  "-T"    )) {
			doUnitTests( cmds );

		} else if (cmd.equals( "" )) {
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
