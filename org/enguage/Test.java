package org.enguage;

import org.enguage.sign.repertoire.Repertoire;
import org.enguage.util.Audit;
import org.enguage.util.Net;
import org.enguage.util.Strings;
import org.enguage.vehicle.Reply;

import org.enguage.Enguage;

public class Test {
	
	static private  Audit audit = new Audit( "Test" );
	static boolean   serverTest = false;
	
	static int       portNumber = 8080;
	static void portNumber( String pn ) { portNumber = Integer.parseInt( pn );}
	
	public static void main( String args[]) {
		if (args.length == 0)
			usage();
		else {
			
			int argc = 0;
			String location = "./src/assets";
			if (args.length > 1 && args[ argc ].equals( "-d" )) {
				argc++;
				location = args[ argc++ ];
			}
			Enguage.loadConfig( location );
			
			if ( args.length == argc + 1 &&
					(args[ argc ].equals( "-s" ) || args[ argc ].equals( "--shell" )))
				Enguage.get().aloudIs( true ).run();				
			else if (args.length == argc + 2 && args[ argc ].equals( "-p" ))
				Net.server( args[ ++argc ]);
			else
				sanityCheck( false, location );
	}	}
	private static void usage() {
		audit.LOG( "Usage: java -jar enguage.jar [-d <configDir>] [-p <port> | -s | -t ]" );
		audit.LOG( "where: default config dir=\".src/assets\"" );
		audit.LOG( "     : -p <port> listens on a TCP/IP port" );
		audit.LOG( "     : -s runs Engauge as a shell" );
		audit.LOG( "     : -t runs a test sanity check" );
	}
	
	private static void interpret( String cmd ) { interpret( cmd, "" );}
	private static void interpret( String cmd, String expected ) {
		
		boolean silentRunning = expected == null;
		if (!silentRunning)
			audit.log( "user> "+ cmd );
			
		String answer = serverTest ?
				Net.client( "localhost", portNumber, cmd )
				: Enguage.interpret( cmd );
		
		if (!silentRunning) {
			int len = expected.length();
			if (len > 0 && expected.charAt( len - 1 ) != '.') expected += ".";
			if (!Reply.understood() && !Repertoire.prompt().equals( "" ))
				audit.log( "Hint is:" + Repertoire.prompt() );
			else if (   !expected.equals( "" )
			         && !new Strings( answer )
			         		.equalsIgnoreCase( new Strings( expected )))
				audit.FATAL("reply:"+ answer +",\n    expected:"+ expected );
			else
				audit.log( "enguage> "+ answer +"\n" );
	}	}

	public static void sanityCheck( boolean serverTest, String location ) {

		//Audit.traceAll( true );

		if (!serverTest)
			Enguage.loadConfig( location );
		
		// ...useful ephemera...
		//interpret( "detail on" );
		//Repertoire.signs.show( "OTF" );
		//interpret( "tracing on" );

		int level = 0;
		
		if ( level == 0 || level == 1 ) {
			audit.title( "The Non-Computable concept of NEED" );
			
			// silently clear the decks
			interpret( "prime the answer yes" );
			interpret( "i don't need anything" );

			interpret( "what do i need",
					   "you don't need anything" );
			interpret( "i need 2 cups of coffee and a biscuit",
					   "ok, you need 2 cups of coffee, and a biscuit.");
			interpret( "what do i need",
					   "you need 2 cups of coffee, and a biscuit.");
			interpret( "how many coffees do i need",
					   "2, you need 2 coffees" );
			interpret( "i don't need any coffee",
					   "ok, you don't need any coffee" );
			interpret( "what do i need",
					   "you need a biscuit" );

			audit.title( "Semantic Thrust" );
			interpret( "i need to go to town",
					   "ok, you need to go to town" );
			interpret( "what do i need",
					   "you need a biscuit, and to go to town" );
			interpret( "i have a biscuit",
					   "ok, you don't need a biscuit" );
			interpret( "i have to go to town",
					   "I know" );
			interpret( "i don't need to go to town",
					   "ok, you don't need to go to town" );
			
			interpret( "prime the answer yes" );
			interpret( "I have everything",
				       "ok, you don't need anything" );
			
			interpret( "what do i need",
					   "you don't need anything" );
		}
		if ( level == 0 || level == 3 ) {
			audit.title( "Verbal Arithmetic" );
			interpret( "what is 1 + 2",                    "1 plus 2 is 3.");
			interpret( "times 2 all squared",              "times 2 all squared makes 36.");
			interpret( "what is 36 + 4 all divided by 2",  "36 plus 4 all divided by 2 is 20" );
			
			audit.title( "Factorial Description" );
			interpret( "what is the factorial of 4",       "I don't understand" );
			/* Ideally, we want:
			 * - the factorial of 1 is 1;
			 * - the factorial of n is n times the factorial of n - 1;
			 * - what is the factorial of 3.
			 */
			interpret( "to the phrase what is the factorial of 1 reply 1", "go on" );
			interpret( "ok", "ok" );
			interpret( "what is the factorial of 1",  "1" );
			
			interpret( "to the phrase what is the factorial of 0 reply 1", "go on" );
			interpret( "ok", "ok" );
			interpret( "what is the factorial of 0",  "1" );
			
			interpret( "interpret multiply numeric variable a by numeric variable b thus", "go on" );
			interpret( "first perform numeric evaluate variable a times variable b",       "go on" );
			interpret( "ok", "ok" );
			
			interpret( "multiply 2 by 3", "6" );
			
			interpret( "interpret subtract numeric variable c from numeric variable d thus", "go on" );
			interpret( "first perform numeric evaluate variable d - variable c",                           "go on" );
			interpret( "ok", "ok" );
			
			interpret( "subtract 2 from 3", "1" );
			
			interpret( "interpret what is the factorial of numeric variable n thus",  "go on" );
			interpret( "first subtract 1 from variable n",                            "go on" );
			interpret( "then what is the factorial of whatever",                      "go on" );
			interpret( "then multiply whatever by variable n",                        "go on" );
			interpret( "then reply whatever the factorial of variable n is whatever", "go on" );
			interpret( "ok", "ok" );
			
			interpret( "what is the factorial of 4",  "24 the factorial of 4 is 24" );
		}
		if ( level == 0 || level == 4 ) {
			audit.title( "Numerical Context" );
			interpret( "i need a coffee",
					   "ok, you need a coffee" );
			interpret( "and another",
					   "ok, you need 1 more coffee" );
			interpret( "how many coffees do i need",
					   "2, you need 2 coffees" );
			interpret( "i need a cup of tea",
					   "ok, you need a cup of tea" );
			interpret( "and another coffee",
					   "ok, you need 1 more coffee" );
			interpret( "what do i need",
					   "You need 3 coffees , and a cup of tea" );
			
			audit.title( "Correction" );
			interpret( "i need another coffee",
					   "ok, you need 1 more coffee.");
			interpret( "no i need another 3",
					   "ok, you need 3 more coffees.");
			interpret( "what do i need",
					   "you need 6 coffees, and a cup of tea.");
			interpret( "prime the answer yes" );
			interpret( "i don't need anything",
					   "ok, you don't need anything" );
		}
		if ( level == 0 || level == 5 ) {
			audit.title( "Annotation" );
			interpret( "delete martin was       list", "ok" );
			interpret( "delete martin wasNot    list", "ok" );
			interpret( "delete i      am        list", "ok" );
			interpret( "delete i      amNot     list", "ok" );
			interpret( "delete martin is        list", "ok" );
			interpret( "delete martin isNot     list", "ok" );
			interpret( "delete i      willBe    list", "ok" );
			interpret( "delete i      willNotBe list", "ok" );
			interpret( "delete martin willBe    list", "ok" );
			interpret( "delete martin willNotBe list", "ok" );
			
			/*
			 * Test 5.1 - IS
			 */
			// e.g. i am alive - 5.1
			interpret( "interpret i am variable state thus",         "go on" );
			interpret( "first add    variable state to   i am list", "go on" );
			interpret( "then  remove variable state from i amNot list", "go on" );
			interpret( "then whatever reply ok",                     "ok" );
			
			// e.g. i am not alive - 5.1
			interpret( "interpret i am not variable state thus",        "go on" );
			interpret( "first add    variable state to   i amNot list", "go on" );
			interpret( "then  remove variable state from i am    list", "go on" );
			interpret( "then whatever reply ok",                        "ok" );
			
			// e.g. am i alive? - 5.1
			interpret( "interpret am i variable state thus",                "go on" );
			interpret( "first variable state exists in i am list",          "go on" );
			interpret( "then reply yes i am variable state",                "go on" );
			interpret( "then if not variable state exists in i amNot list", "go on" );
			interpret( "then if not reply i do not know",                   "go on" );
			interpret( "then reply no i am not variable state",             "go on" );
			interpret( "ok", "ok" );
			
			//  e.g. martin is alive - 5.1
			interpret( "interpret variable entity is variable state thus",            "go on" );
			interpret( "first add    variable state to   variable entity is    list", "go on" );
			interpret( "then  remove variable state from variable entity isNot list", "go on" );
			interpret( "then whatever reply ok",                                      "ok" );
			
			// e.g. martin is not alive - 5.1
			interpret( "interpret variable entity is not variable state thus",       "go on" );
			interpret( "first add   variable state to   variable entity isNot list", "go on" );
			interpret( "then remove variable state from variable entity is    list", "go on" );
			interpret( "then whatever reply ok",                                     "ok" );
			
			// e.g. is martin alive - 5.1
			interpret( "interpret is variable entity variable state thus",        "go on" );
			interpret( "first variable state  exists in variable entity is list", "go on" );
			interpret( "then reply yes variable entity is variable state",        "go on" );
			interpret( "then if not variable state exists in variable entity isNot list", "go on" );
			interpret( "then reply no variable entity is not variable state",     "go on" );
			interpret( "then if not reply i do not know",                         "go on" );
			interpret( "ok", "ok" );

			// e.g. is martin not alive - 5.1
			interpret( "interpret is variable entity not variable state thus",       "go on" );
			interpret( "first variable state  exists in variable entity isNot list", "go on" );
			interpret( "then reply yes variable entity is not variable state",        "go on" );
			interpret( "then if not variable state exists in variable entity is list", "go on" );
			interpret( "then reply no variable entity is variable state",             "go on" );
			interpret( "then if not reply i do not know",                            "go on" );
			interpret( "ok", "ok" );

			// test 5.1
			interpret( "am i alive",     "i don't know" );
			interpret( "i am alive",     "ok" );
			interpret( "am i alive",     "yes i'm alive" );
			interpret( "i am not alive", "ok" );
			interpret( "am i alive",     "no i'm not alive" );
			
			// test 5.1
			interpret( "is martin alive", "i don't know" );
			interpret( "martin is alive", "ok" );
			interpret( "is martin alive", "yes martin is alive" );
			interpret( "martin is not alive", "ok" );
			interpret( "is martin alive",     "no martin is not alive" );
			interpret( "is martin not alive", "yes martin is not alive" );
			
			/*
			 *  Test 5.2 was/was not
			 */
			//  e.g. martin was alive - 5.2
			interpret( "interpret variable entity was variable state thus",            "go on" );
			interpret( "first add    variable state to   variable entity was    list", "go on" );
			interpret( "then  remove variable state from variable entity wasNot list", "go on" );
			interpret( "then whatever reply ok",                                       "ok" );
			
			// e.g. martin was not alive - 5.2
			interpret( "interpret variable entity was not variable state thus",       "go on" );
			interpret( "first add   variable state to   variable entity wasNot list", "go on" );
			interpret( "then remove variable state from variable entity was    list", "go on" );
			interpret( "then whatever reply ok",                                      "ok" );
			
			// e.g. was martin alive - 5.2
			interpret( "interpret was variable entity variable state thus",        "go on" );
			interpret( "first variable state  exists in variable entity was list", "go on" );
			interpret( "then reply yes variable entity was variable state",        "go on" );
			interpret( "then if not variable state exists in variable entity wasNot list", "go on" );
			interpret( "then reply no variable entity was not variable state",     "go on" );
			interpret( "then if not reply i do not know",                          "go on" );
			interpret( "ok", "ok" );

			// e.g. was martin not alive - 5.2
			interpret( "interpret was variable entity not variable state thus",       "go on" );
			interpret( "first variable state  exists in variable entity wasNot list", "go on" );
			interpret( "then reply yes variable entity was not variable state",       "go on" );
			interpret( "then if not variable state exists in variable entity was list", "go on" );
			interpret( "then reply no variable entity was variable state",            "go on" );
			interpret( "then if not reply i do not know",                             "go on" );
			interpret( "ok", "ok" );

			// test 5.2
			interpret( "was martin alive",     "i don't know" );
			interpret( "martin was alive",     "ok" );
			interpret( "was martin alive",     "yes martin was alive" );
			interpret( "martin was not alive", "ok" );
			interpret( "was martin alive",     "no martin was not alive" );
			interpret( "was martin not alive", "yes martin was not alive" );
			
			/*
			 *  Test 5.3 will be/will not be
			 */
			//  e.g. martin will be alive - 5.3
			interpret( "interpret variable entity will be variable state thus",           "go on" );
			interpret( "first add    variable state to   variable entity willBe    list", "go on" );
			interpret( "then  remove variable state from variable entity willNotBe list", "go on" );
			interpret( "then whatever reply ok",                                          "ok" );
			
			// e.g. martin will not be alive - 5.3
			interpret( "interpret variable entity will not be variable state thus",      "go on" );
			interpret( "first add   variable state to   variable entity willNotBe list", "go on" );
			interpret( "then remove variable state from variable entity willBe    list", "go on" );
			interpret( "then whatever reply ok",                                         "ok" );
			
			// e.g. will martin be alive - 5.3
			interpret( "interpret will variable entity be variable state thus",      "go on" );
			interpret( "first variable state exists in variable entity willBe list", "go on" );
			interpret( "then reply yes variable entity will be variable state",      "go on" );
			interpret( "then if not variable state exists in variable entity willNotBe list", "go on" );
			interpret( "then reply no variable entity will not be variable state",   "go on" );
			interpret( "then if not reply i do not know",                            "go on" );
			interpret( "ok", "ok" );

			// e.g. will martin not be alive - 5.3
			interpret( "interpret will variable entity not be variable state thus",      "go on" );
			interpret( "first variable state  exists in variable entity willNotBe list", "go on" );
			interpret( "then reply yes variable entity will not be variable state",      "go on" );
			interpret( "then if not variable state exists in variable entity willBe list", "go on" );
			interpret( "then reply no variable entity will be variable state",           "go on" );
			interpret( "then if not reply i do not know",                                "go on" );
			interpret( "ok", "ok" );

			// test 5.3
			interpret( "will i be alive",     "i don't know" );
			interpret( "i will be alive",     "ok" );
			interpret( "will i be alive",     "yes you'll be alive" );
			interpret( "i will not be alive", "ok" );
			interpret( "will i be alive",     "no you'll not be alive" );
			interpret( "will i not be alive", "yes you'll not be alive" );

			interpret( "will martin be alive",     "i don't know" );
			interpret( "martin will be alive",     "ok" );
			interpret( "will martin be alive",     "yes martin will be alive" );
			interpret( "martin will not be alive", "ok" );
			interpret( "will martin be alive",     "no martin will not be alive" );
			interpret( "will martin not be alive", "yes martin will not be alive" );

			// Test
			// Event: to move is to was (traverse time quanta)
			interpret( "interpret when i am dead then move what i am to what i was thus", "go on" );
			
			
			// dead is the opposite of alive
			// dead and alive are mutually exclusive
			// fat and thin and athletic are mutually exclusive.
			// I am fat.     Am I thin. No
			// I am not fat. Am i thin. I don't know
			
			// i am martin            - ok
			// i am martin wheatman   - ok
			// i am martin            - I know
			// i am harvey wallbanger - no you're martin
			// i've changed my name to harvey wallbanger - ok
			
			// my date of birth is
			// how old am i.
			
			/* TODO:
			 *  create a queen called elizabeth the first  (eliz = woman's name, a queen is a monarch => person)
			 *  she died in 1603
			 *  she reigned for 45 years (so she ascended/came to the throne in 1548!)
			 */
			interpret( "a queen is a monarch" );
			// my name is martin
			// my name is martin wheatman

		}
		if ( level == 0 || level == 6 ) {
			audit.title( "Disambiguation" );
			interpret( "the eagle has landed" //,
						   //"Are you an ornithologist."
					);
			interpret( "no the eagle has landed" //,
						   //"So , you're talking about the novel."
					);
			interpret( "no the eagle has landed" //, 
						   //"So you're talking about Apollo 11."
					);
			interpret( "no the eagle has landed" //,
						   //"I don't understand"
					);
			// Issue here: on DNU, we need to advance this on "the eagle has landed"
			// i.e. w/o "no ..."
		}
		if ( level == 0 || level == 7 ) {
			audit.title( "Temporal interpret" );
			interpret( "what day is christmas day" );
			//testInterpret( "what day is it today" );

			audit.title( "Temporospatial concept MEETING" );
			
			//Where.locatorIs( "at" ); is the same as...
			// new Sofa().interpret( new Strings( "spatial locator at" )); is the same as...
			interpret( "interpret spatially something can be phrase variable locator thus", "go on" );
			interpret( "first perform spatial locator variable locator", "go on" );
			interpret( "ok", "ok" );
			
			interpret( "spatially something can be to the left of",  "ok" );
			interpret( "spatially something can be to the right of", "ok" );
			interpret( "spatially something can be in front of",     "ok" );
			interpret( "spatially something can be on top of",       "ok" );
			interpret( "spatially something can be behind",          "ok" );
			interpret( "spatially something can be in",              "ok" );
			interpret( "spatially something can be on",              "ok" );
			interpret( "spatially something can be under",           "ok" );
			interpret( "spatially something can be underneath",      "ok" );
			interpret( "spatially something can be over",            "ok" );
			interpret( "spatially something can be at",              "ok" );
			
			/* TODO: interpret think of a variable entity thus.  // see sofa for particular details!
			 * first create a class variable entity.             // mkdir pub; touch pub/isa 
			 * then  create an anonymous entity variable entity. // mkdir pub/a
			 * then  set the context of the variable entity to a variable entity // ln -s pub/the pub/a
			 * ok.
			 */
			// Creating a pub _is_ needed
			interpret( "a pub is a place" );

			interpret( "I'm not meeting anybody",
					   "Ok , you're not meeting anybody" );
			interpret( "At 7 I'm meeting my brother at the pub",
					   "Ok , you're meeting your brother at 7 at the pub" );
			interpret( "When  am I meeting my brother",
					   "You're meeting your brother at 7" );
			interpret( "Where am I meeting my brother",
					   "You're meeting your brother at the pub" );
			interpret( "Am I meeting my brother",
					   "Yes , you're meeting your brother" );
			
			interpret( "I'm meeting my sister at the pub" );
			interpret( "When am I meeting my sister",
					   "I don't know when you're meeting your sister" );
			
			interpret( "When am I meeting my dad",
					   "i don't know if you're meeting your dad" );
			interpret( "Where am I meeting my dad" ,
					   "i don't know if you're meeting your dad" );
		}
		if (level == 0 || level == 8) {
			interpret( "tcpip localhost 999 \"999 is a test value for port address\"",   "ok" );
			interpret( "tcpip localhost 5678 \"this is a test, which will fail\"",    "Sorry" );
		}
		if (level == 0 || level == 9) {
			audit.title( "On-the-fly Langauge Learning" );
			/* TODO: create filename from pattern:
			 *    "i need phrase variable objects" => i_need-.txt (append? create overlay)
			 *    "this is part of the need concept" => need.txt (append)
			 *    Enguage.interpret() => overlay
			 *    Conceept.load() => can this outlive Enguage overlay???
			 */
			
			// First, what we can't say yet...
			interpret( "my name is martin",                 "I don't understand" );
			interpret( "if not  reply i already know this", "I don't understand" );
			interpret( "unset the value of name",           "ok" );
			
			// build-a-program...
			interpret( "interpret my name is phrase variable name thus", "go on" );
			interpret( "first set name to variable name",                "go on" );
			interpret( "then get the value of name",                     "go on" ); // not strictly necessary!
			interpret( "then reply hello whatever",                      "go on" );
			interpret( "ok",                                             "ok"    );

			interpret( "my name is ruth",   "hello   ruth" );
			interpret( "my name is martin", "hello martin" );
			
			
			//...or to put it another way
			interpret( "to the phrase i am called phrase variable name reply hi whatever", "go on" );
			interpret( "this implies name gets set to variable name",   "go on" );
			interpret( "this implies name is not set to variable name", "go on" );
			interpret( "if not reply i already know this",              "go on" );
			interpret( "ok", "ok" );
			
			interpret( "i am called martin", "i already know this" );
			
			// ...means.../...the means to...
			// 1. from the-means-to repertoire
			interpret( "to the phrase phrase variable x the means to phrase variable y reply i really do not understand", "go on" );
			interpret( "ok", "ok" );

			interpret( "do we have the means to become rich", "I really don't understand" );

			// 2. could this be built thus?
			interpret( "to phrase variable this means phrase variable that reply ok", "go on" );
			interpret( "this implies ok set induction to false",                         "go on" );
			interpret( "this implies perform sign think variable that",               "go on" );
			interpret( "this implies perform sign create variable this",              "go on" );
			interpret( "this implies ok set induction to true",                          "go on" );
			interpret( "ok", "ok" );
			
			interpret( "just call me phrase variable name means i am called variable name", "ok" );
			interpret( "just call me martin", "i already know this" );
		}
		if ( level == 0 || level == 10 ) {
			audit.title( "Ask: Confirmation" );
			
			interpret( "prime the answer yes" );
			interpret( "i have everything", "ok , you don't need anything" );
			
			interpret( "prime the answer no" );
			interpret( "i have everything", "ok , let us leave things as they are" );

			interpret( "prime the answer i do not understand" );
			interpret( "i have everything", "Ok , let us leave things as they are" );
			
			/* TODO:
			 * To the phrase: i am p v name       => set user name NAME
			 *                my name is p v name => set user name NAME
			 *                p v name            => set user name NAME
			 * Ask: what is your name?
			 */
		}
		audit.log( "PASSED" );
}	}
