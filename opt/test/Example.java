package opt.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ListIterator;
import java.util.Scanner;

import org.enguage.Enguage;
import org.enguage.signs.objects.space.Overlay;
import org.enguage.signs.vehicle.pronoun.Pronoun;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.sys.Fs;

public class Example {
	
	private static Audit   audit = new Audit( "Example" );
	
	/*
	 * Individual Enguage test harness...
	 */
	private static String testPrompt = "";
	private static String testPrompt() {return testPrompt;}
	private static void   testPrompt( String prompt) {testPrompt = prompt;}
	
	private static String replyPrompt = "";
	private static String replyPrompt() { return replyPrompt;}
	private static void   replyPrompt( String prompt) { replyPrompt = prompt;}

	
	public  static void test( String  cmd ) {test( cmd, null );}
	public  static void test( String  cmd, String expected ) {test( cmd, expected, null );}
	private static void test( String  cmd, String expected, String unexpected ) {
		// expected == null => silent!
		if (expected != null)
			Audit.log( testPrompt()+ cmd +".");
		
		Strings reply = Enguage.get().mediate( new Strings( cmd ));

		if (expected != null) {
		
			if (expected.equals( "" ) || reply.equalsIgnoreCase( new Strings( expected ))) {
		
				audit.passed( replyPrompt()+ reply +"." );// 1st success
				
			} else if (unexpected == null) {              // no second chance
				//Repertoire.signs.show()
				audit.FATAL(
					"reply: '"+    reply    +"',\n             "+
					"expected: '"+ expected +"' "
				);
		
			} else if (unexpected.equals( "" ) ||
					 reply.equalsIgnoreCase( new Strings( unexpected ))) {
			
				audit.passed( replyPrompt()+ reply +".\n" );
			
			} else                                        // second chance failed too!
				//Repertoire.signs.show()
				audit.FATAL(
					"reply: '"      + reply      +"'\n             "+
					"expected: '"   + expected   +"'\n          "+
					"alternately: '"+ unexpected +"'\n          "
				);
		}
	}
		
	private static final String COMMENT_START = "#";
	private static final String LINE_TERM = ".";
	private static final String IN_UR_SEP = ":";
	private static final String IN_REPLY_SEP = "/"; // doesn't like '|'

	private static void runTestLine( String line ) {
		line = line.substring( 0, line.length() - 1 ); // remove "."
		String[] values = line.split( IN_UR_SEP );
		if (1 == values.length)
			test( values[ 0 ]);
		else {	
			String[] replies = values[ 1 ].split( IN_REPLY_SEP );
			if (replies.length == 1)
				test( values[0], replies[ 0 ]);
			else if (replies.length == 2)
				test( values[0], replies[ 0 ], replies[ 1 ]);
			else  if (replies.length == 0) {
				audit.FATAL( "Too few replies provided" );
				System.exit( 1 );
			} else {
				audit.FATAL( "Too many replies ("+ replies.length +") provided" );
				System.exit( 1 );
			}
		}
	}

	private static boolean runTestFile(String fname) {
		boolean rc = true;
		
		File f = new File( fname );
		if (f.exists()) {
			try (Scanner file = new Scanner( f )) {
				StringBuilder sb = new StringBuilder();
				while (file.hasNextLine()) {
					String line = file.nextLine();
					String[] bits = line.split( COMMENT_START );
					if (bits.length > 0) {
						line = " " + bits[0].trim();
						boolean endsInStop = line.endsWith( LINE_TERM );
						
						// split on '.'
						sb.append( line );
						if (endsInStop) {
							runTestLine( sb.toString() );
							sb = new StringBuilder();
						}
					}
				}
	
			} catch (FileNotFoundException fnf) {
				rc = false;
			}
		}
		return rc;
	}

	/*
	 * Full self-test...
	 */
	private static final String TEST_DIR = "etc/test/";
	private static final String TEST_EXT = ".txt";
	
	public static void unitTest( Strings tests ) {
		int testGrp = 0;
		
		Audit.interval(); // reset timer
		testPrompt(  "\nuser> "    );
		replyPrompt( "enguage> " );

		Pronoun.interpret( new Strings( "add masculine martin" ));
		Pronoun.interpret( new Strings( "add masculine james" ));
		Pronoun.interpret( new Strings( "add feminine  ruth" ));

		// sanity testing, remove yet preserve persistent data...
		String fsys = "./selftest";
		Fs.root( fsys );

		for (String test : tests) {
			if (!Fs.destroy( fsys ))
				audit.FATAL( "failed to remove old database - "+ fsys );
			audit.title( "TEST: "+ test );
			if (runTestFile( TEST_DIR+ test +TEST_EXT ))
				testGrp++;
		}

		Audit.log( testGrp +" test group(s) found" );
		audit.PASSED();
	}

	public static void unitTests() {
		Strings unitTests = new Strings( new File( TEST_DIR ).list());
		ListIterator<String> li = unitTests.listIterator();
		while (li.hasNext()) {
			String test = li.next();
			if (test.endsWith( ".txt" ))
				li.set( test.substring( 0, test.length()-4)); // remove ".txt"
		}
		unitTest( unitTests );		
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
		String     cmd;
		String     fsys = doArgs( cmds );

		Enguage.set( new Enguage( fsys ));
				
		cmd = cmds.isEmpty() ? "":cmds.remove( 0 );
		
		if (cmd.equals(  "-t"    ) ||
			cmd.equals( "--test" )   )
		{
			unitTests();
			
		} else if (cmd.equals(  "-T"    )) {
			unitTest( cmds );

		} else if (cmd.equals( "" )) {
			Overlay.attach( "uid" );
			Enguage.shell().aloudIs( true ).run();
		
		} else {
			// Command line parameters exists...
			// reconstruct original commands and interpret...
			// - remove full stop, if one given -
			cmds.prepend( cmd );
			cmds = new Strings( cmds.toString() );
			Audit.LOG( "cmds: "+ cmds.toString() );
			if (cmds.get( cmds.size()-1 ).equals( "." ))
				cmds.remove( cmds.size()-1 );

			// ...reconstruct original commands and interpret
			test( cmds.toString(), "" );
	}	}
}
