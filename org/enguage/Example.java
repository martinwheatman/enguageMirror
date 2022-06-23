package org.enguage;

import org.enguage.interp.repertoire.Autoload;
import org.enguage.interp.repertoire.Repertoire;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.sys.Fs;
import org.enguage.util.sys.Server;
import org.enguage.vehicle.pronoun.Pronoun;

public class Example {
	
	private static Audit   audit = new Audit( "Example" );

	private boolean serverTest = false;
	public  void    serverTest( boolean b ) {serverTest = b;}

	private int     portNumber = 8080;
	public  void    portNumber( String pn ) { portNumber = Integer.parseInt( pn );}

	public  void      selfTest( String cmd, Strings cmds ) {
		// If we're sanity testing, remove yet preserve persistent data...
		String fsys = "./selftest";
		Fs.root( fsys );
		
		if (!Fs.destroy( fsys ))
			audit.FATAL( "failed to remove old database - "+ fsys );
		else
			try {
				if (cmd.equals( "-T" ))
					testName = cmds.size()==0 ? testName : cmds.remove( 0 );
				else
					level = cmds.size()==0 ? level : Integer.valueOf( cmds.remove( 0 ));
				
				run();
				
			} catch (NumberFormatException nfe) {
				Audit.LOG( "Insanity: "+ nfe.toString() );
	}		}
	
	// Call this direct, so it's not counted!
	private static final String ihe =  "I have everything";
	private static void clearTheNeedsList() { clearTheNeedsList( ihe );}
	private static void clearTheNeedsList( String s ) { Enguage.mediate( new Strings( s ));	}
	private static void tidyUpViolenceTest( String fname ) {
		Enguage.mediate( new Strings( "delete "+ fname +" advocate list" ));
		Enguage.mediate( new Strings( "delete "+ fname +" fear     list" ));
		Enguage.mediate( new Strings( "delete _user causal list" ));
		Enguage.mediate( new Strings( "unset the value of they" ));
	}
	private static void tidyUpViolenceTest() { tidyUpViolenceTest( "violence" ); }
	
	private int       level = 0;
	private int     testGrp = 0;
	private String testName = null;
	private boolean runTheseTests() {return runTheseTests( null );}
	private boolean runTheseTests( String title ) {
		++testGrp;
		boolean runTheseTests = testName != null ?
				title != null && title.contains( testName )
				: level == 0 || level == testGrp || (level < 0 && -level != testGrp);
		if (runTheseTests) audit.title( "Test "+ testGrp + (title != null ? ": "+ title : ""));
		return runTheseTests;
	}
	
	private String testPrompt = "";
	private String testPrompt() {return testPrompt;}
	private void   testPrompt( String prompt) {testPrompt = prompt;}
	
	private String replyPrompt = "";
	private String replyPrompt() { return replyPrompt;}
	private void   replyPrompt( String prompt) { replyPrompt = prompt;}
	
	private void think( String cmd ) {run( cmd, null );}
	private void youCanSay( String thgt ) {think( "you can say "+ thgt);}
	
	public  void run( String  cmd, String expected ) {run( cmd, expected, null );}
	private void run( String  cmd, String expected, String unexpected ) {
		// expected == null => silent!
		if (expected != null)
			Audit.log( testPrompt()+ cmd +".");
		
		Strings reply = serverTest ?
				new Strings( Server.client( "localhost", portNumber, cmd ))
				: Enguage.mediate( new Strings( cmd ));

		if (expected == null) { // don't check anything
			;
		} else if (expected.equals( "" ) || reply.equalsIgnoreCase( new Strings( expected ))) {
		
			audit.passed( replyPrompt()+ reply +"." );      // 1st success
			
		} else if (unexpected == null) {                      // no second chance
			//Repertoire.signs.show();
			audit.FATAL(
				"reply: '"+    reply    +"',\n             "+
				"expected: '"+ expected +"' "
			);
		
		} else if (unexpected.equals( "" ) ||
				 reply.equalsIgnoreCase( new Strings( unexpected ))) {
		
			audit.passed( replyPrompt()+ reply +".\n" );
		
		} else                                           // second chance failed too!
			//Repertoire.signs.show();
			audit.FATAL(
				"reply: '"      + reply      +"'\n             "+
				"expected: '"   + expected   +"'\n          "+
				"alternately: '"+ unexpected +"'\n          "
			);
	}
	
	public  void run() {
		// ...useful ephemera...
		//interpret( "detail on" );
		//interpret( "tracing on" );
		//Audit.allOn();
		//Repertoire.signs.show( "OTF" );
		
		Audit.interval(); // reset timer
		testPrompt(  "\nuser> "    );
		replyPrompt( "enguage> " );

		Pronoun.interpret( new Strings( "add masculine martin" ));
		Pronoun.interpret( new Strings( "add masculine james" ));
		Pronoun.interpret( new Strings( "add feminine  ruth" ));

//		if (runTheseTests( "title" )) {
//		testRun( "", "" );
//	}
		if (runTheseTests( "holding" )) {
			run( "who are we",        "ok, we means you and i" );
			run( "we are ruth and i", "ok, we means you and ruth" );
			run( "who are we",        "ok, we means you and ruth" );
			run( "we are you and i",  "ok, we means you and i"    );
			run( "who are we",        "ok, we means you and i"    );
			
			run( "martin is not holding hands with ruth", "yes, i know" );
			run( "martin is     holding hands with ruth", 
					 "ok, martin is     holding hands with ruth" );
			run( "martin is not holding hands with ruth", 
					 "ok, martin is not holding hands with ruth" );

			run( "whose hand am i holding", "sorry, you're not holding anyone's hand" );
			run( "we are holding hands",    "ok, we are holding hands" );
			run( "whose hand am i holding", "ok, you're holding my hand" );
			
			run( "we are ruth and i",       "ok, we means you and ruth" );
			run( "we are holding hands",    "ok, you're holding hands with ruth" );
			run( "whose hand am i holding", "ok, you're holding ruth's hand" );
			
			run( "i am not holding anyone's hand", "ok, you're not holding anyone's hand" );
			run( "whose hand am i holding", "sorry, you're not holding anyone's hand" );
			run( "if we are holding hands then whose hand am i holding",
					 "ok, you're holding ruth's hand" );
			run( "whose hand am i holding", "sorry, you're not holding anyone's hand" );
		}
		if (runTheseTests( "can - capabilities" )) {
			run( "flowers can    be yellow", "ok, flowers can   be yellow" );
			run( "flowers cannot be green",  "ok, flowers can't be green" );
			run( "flowers cannot be red",    "ok, flowers can't be red" );
			run( "can flowers    be red",    "no, flowers can't be red" );
			run( "no flowers can be red",    "ok, flowers can be red" );

			run( "can flowers    be green",   "no, flowers can't be green" );
			run( "can flowers    be yellow",  "yes, flowers can be yellow" );
			run( "can flowers    be red",     "yes, flowers can be red" );
			run( "can flowers    be blue",    "sorry, i don't know" );
		}
		if (runTheseTests( "Megan's Enjoy Example" )) {
			run( "i enjoy yellow", "ok, you enjoy yellow" );
			run( "i enjoy dogs",   "ok, you enjoy dogs" );

			run( "do i enjoy yellow",    "yes, you enjoy yellow" );
			run( "do i enjoy chocolate", "you haven't told me you enjoy chocolate" );
		}
		if (runTheseTests( "Megan's Thales Experiment" )) {
			/*
//			test( "you can say variable colour is a colour", "ok" );
//			test( "this implies that you add variable colour to your colour list", "ok" );
//			
//			test( "red is a colour", "ok" );
//			test( "blue is a colour", "ok" );
//			test( "green is a colour", "ok" );
//			test( "yellow is a colour", "ok" );
			
			test( "set colour to yellow", "ok, colour is set to yellow" );
			test( "set colour to red",    "ok, colour is set to red" );
			test( "set colour to fred",   "sorry, fred is not a colour i know" );
			
			//
			// before logging in, test that we can't just affect things...
			test( "switch to submarines screen", "sorry, you need to be logged in" );
			test( "clear the filter",            "sorry, you need to be logged in" );
			
			test( "login as megan",              "ok, you're logged in as megan" );
			test( "switch to submarines screen", "ok, switched to submarines screen" );
			test( "what is the value of screen", "submarines, the value of screen is submarines" );
			test( "filter by submarines only",   "ok, you're filtering by submarines only" );
			
			test( "logout",                      "ok, you have logged out" );
			*/
			run( "martin is a user",   "ok");
			run( "set user as martin", "ok, martin is logged on");
		}
		if (runTheseTests( "BCS HCI Workshop" )) { // code generation features
			
			run( "to the phrase hello reply hello to you too", "" );
			run( "hello",                 "hello to you too" );
			run( "to the phrase my name is variable name reply hello variable name", "" );
			run( "please my name is martin", "hello martin" );
			
			clearTheNeedsList();
			run( "what do i need",        "you don't need anything" );
			run( "i need a coffee",       "ok, you need a coffee" );
			run( "what do i need",        "you need a coffee" );
			
			run( "what do i think",       "i'm sorry, I don't understand the question" );
			
			run( "what do i want",        "i'm sorry, i don't understand the question" );
			run( "want is like need",     "ok, want is like need" );
			run( "what do i want",        "you don't want anything" );

			run( "interpret something can be variable quality thus", "" );
			run( "first add variable quality to my quality list",    "" );
			run( "then reply ok variable quality is a quality",  "" );
			
			run( "something can be cool", "ok cool is a quality" );
			
			run( "interpret variable things are variable quality thus", "" );
			run( "first add variable things to my variable quality list", "" );
			run( "and then reply ok variable things are variable quality", "" );
			
			run( "ferraris are cool",     "ok ferraris are cool" );
			run( "want is like need",     "" );
			run( "i want a ferrari because ferraris are cool", "ok, you want a ferrari because ferraris are cool" );
			run( "why do I want a ferrari", "because ferraris are cool" );
			
			// Tidy up...
			run( "I don't want anything", "ok, you don't want anything" );
			run( "want is not like need", "ok, want is not like need" );
			run( "what do i want",        "i'm sorry, i don't understand the question" );
		}
		
		if (runTheseTests( "Simple Food Diary" )) {
			run( "i just ate breakfast",             "ok, you have eaten breakfast today" );
			run( "today i have eaten a mars bar",    "ok, you have eaten a mars bar today" );
			run( "i have eaten 2 packets of crisps", "ok, you have eaten 2 packets of crisps today" );
			
			run( "what have i eaten today",
			      "you have eaten breakfast today , a mars bar today , and 2 packets of crisps today" );
		}
				
		if (runTheseTests( "WSC - advocacy and fear" )) {
			// TODO: WSC - alternative states tests
			// mut ex: dead is the opposite of alive, no?
			//         dead and alive are mutually exclusive
			// mut ex: i am martin            - ok
			//         i am martin wheatman   - ok
			//         i am martin            - yes, i know
			//         i am harvey wallbanger - no you're martin
			//         i've changed my name to harvey wallbanger - ok
			// fat and thin and athletic are mutually exclusive.
			//         I am fat.     Am I thin. No
			//         I am not fat. Am i thin. I don't know
			audit.subtl( "Contradiction test... can't swap between states directly");
			run( "demonstrators fear violence",        "ok, demonstrators fear violence" );
			run( "demonstrators advocate violence",    "no, demonstrators fear violence" );
			run( "demonstrators do not fear violence", "ok, demonstrators don't fear violence" );
			run( "demonstrators advocate violence",    "ok, demonstrators advocate violence" );
			run( "demonstrators fear violence",        "no, demonstrators advocate violence" );
			run( "demonstrators don't advocate violence", 
					 "ok, demonstrators don't advocate violence" );
			// tidy up
			tidyUpViolenceTest();
			
			// ----------------------------------------------------------------
			audit.subtl( "Openly stated: opposing views" );
			run( "the councillors   fear     violence", "ok, the councillors       fear violence" );
			run( "the demonstrators advocate violence", "ok, the demonstrators advocate violence" );
			// test 1
			run( "the councillors refused the demonstrators a permit because they fear violence",
					 "ok, the councillors refused the demonstrators a permit because they fear violence" );
			run( "who are they", "they are the councillors" );
			// test 2
			run( "the councillors refused the demonstrators a permit because they advocate violence",
					 "ok, the councillors refused the demonstrators a permit because they advocate violence" );
			run( "who are they", "they are the demonstrators" );
			// tidy up
			tidyUpViolenceTest();
			
			// ----------------------------------------------------------------
			audit.subtl( "Openly stated: aligned views - advocate" );
			run( "the councillors   advocate violence", "ok, the councillors   advocate violence" );
			run( "the demonstrators advocate violence", "ok, the demonstrators advocate violence" );
			
			// test  1
			run( "the councillors refused the demonstrators a permit because they fear violence",
					 "i'm sorry, I don't think they fear violence" );
			run( "who are they", "I don't know" );
			
			// test 2
			run( "the councillors refused the demonstrators a permit because they advocate violence",
					 "ok, the councillors refused the demonstrators a permit because they advocate violence" );
			run( "who are they", "they are the councillors , and the demonstrators" );
			
			// tidy up
			tidyUpViolenceTest();
			
			// ----------------------------------------------------------------
			audit.subtl( "Openly stated: aligned views - fear" );
			run( "the councillors fear violence because the voters fear violence",
					 "ok, the councillors fear violence because the voters fear violence" );
			run( "the demonstrators fear violence", "ok, the demonstrators fear violence" );
			
			// test 1
			run( "the councillors refused the demonstrators a permit because they fear violence",
					 "ok, the councillors refused the demonstrators a permit because they fear violence" );
			run( "who are they", "they are the councillors , the voters , and the demonstrators" );
			
			// test 2
			run( "the councillors refused the demonstrators a permit because they advocate violence",
					 "i'm sorry, I don't think they advocate violence" );
			run( "who are they", "i don't know" );
			
			// tidy up
			tidyUpViolenceTest();
			
			// ----------------------------------------------------------------
			audit.subtl( "the common sense view" );
			// This should be configured within the repertoire...
			run( "common sense would suggest the councillors fear violence",
					 "ok, common sense would suggest the councillors fear violence" );
			
			run( "the councillors refused the demonstrators a permit because they fear violence",
					 "ok, the councillors refused the demonstrators a permit because they fear violence" );
			run( "who are they", "they are the councillors" );
			
			// tidy up - N.B. these will affect previous tests on re-runs
			tidyUpViolenceTest( "csviolence" );
		}
		if (runTheseTests( "Saving spoken concepts" )) {
			// First, check we're clean!  -- may not be the case with BCS HCI code
			run( "hello",                          "hello to you too", "I don't understand" );
			// Build a repertoire...
			run( "to the phrase hello reply hello to you too", "ok" );
			run( "hello",                          "hello to you too" );
			run( "to the phrase my name is variable name reply hello variable name", "ok" );
			// OK, now save this...
			run( "save  spoken concepts as hello", "ok" );
			
			// then remove the cached concept...
			Autoload.unload( "hello" );
			
			// So, this will reload from the saved repertoire
			run( "hello",                          "hello to you too" );
			
			// OK, now delete the repertoire (and the sign)
			run( "delete spoken concept hello",    "ok" );
			// ...and its gone. We're clean again!
			run( "hello",                          "i don't understand" );
		}
		if (runTheseTests( "Why/because, IJCSSA article" )) {
			audit.subtl( "Simple action demo" );
			run( "i am baking a cake",     "yes, i know", "ok, you're     baking a cake" );
			run( "am i baking a cake",     "yes,               you're     baking a cake" );
			run( "i am not baking a cake",                "ok, you're not baking a cake" );
			
			audit.title( "Why/because" );
			run( "i am baking a cake so i need 3 eggs",
					   "ok, you need 3 eggs because you're baking a cake" );
			
			run( "am i baking a cake",      "yes, you're baking a cake" );
			run( "how many eggs do i need", "3, you need 3 eggs" );
			
			run( "so why do i need 3 eggs", "because you're baking a cake" );
			run( "do I need 3 eggs because I am baking a cake",
				       "yes, you need 3 eggs because you're baking a cake" );
			// simple check for infinite loops
			run( "i am baking a cake because i need 3 eggs",
					   "i'm sorry, you need 3 eggs because you're baking a cake" );
			
			audit.subtl( "Distinguishing negative responses" );
			// I do understand, "sophie needs dr martens", but
			// I don't understand, "sophie is very fashionable"
			run( "sophie needs dr martens because sophie is very fashionable",
                       "I don't understand, sophie is very fashionable" );
			run( "sophie is very fashionable because sophie needs dr martens",
                       "I don't understand, sophie is very fashionable" );
			run( "do i need 250 grams of flour because i am baking a cake",
                       "i'm sorry, it is not the case that you need 250 grams of flour" );
			run( "why am i heating the oven",
					   "i'm sorry, i didn't know you're heating the oven" );
			
			audit.subtl( "Transitivity" );
			run( "i need to go to the shops because i need 3 eggs",
					   "ok, you need to go to the shops because you need 3 eggs" );
			run( "do i need to go to the shops because i need 3 eggs",
					   "yes, you need to go to the shops because you need 3 eggs" );
			// this test steps over one reason...
			run( "do i need to go to the shops because i am baking a cake",
					   "yes, you need to go to the shops because you're baking a cake" );
			
			audit.subtl( "Why might.../abduction" );
			run( "i am not baking a cake",  "ok, you're not baking a cake" );
			run( "am i baking a cake",      "no, you're not baking a cake" );
			run( "i do not need any eggs",  "ok, you don't need any eggs" );
			run( "why do i need 3 eggs",    "no, you don't need 3 eggs" );
			run( "why might i need 3 eggs", "because you're baking a cake" );
			
		}
		if (runTheseTests( "The Non-Computable concept of Need" )) { // 
			
			// regression test: "do i need" != >do i need OBJECT<
			// blank var at end of utterance
			run( "do i need",                  "i'm sorry, i don't understand the question" );
			
			clearTheNeedsList();
			run( "what do i need",	            "you don't need anything" );
			run( "i need 2 cups of coffee and a biscuit",
					                            "ok, you need 2 cups of coffee and a biscuit");
			run( "what do i need",             "you need 2 cups of coffee, and a biscuit");
			run( "how many coffees do i need", "2, you need 2 coffees" );
			run( "i need 2 coffees",           "yes, i know" );
			run( "i don't need any coffee",    "ok, you don't need any coffee" );
			run( "what do i need",             "you need a biscuit" );

			audit.title( "Semantic Thrust" );
			run( "i need to go to town",       "ok, you need to go to town" );
			run( "what do i need",             "you need a biscuit, and to go to town" );
			run( "i have the biscuit",         "ok, you don't need any biscuit" );
			run( "i have to go to town",       "yes, i know" );
			run( "i don't need to go to town", "ok, you don't need to go to town" );
			run( "what do i need",             "you don't need anything" );
			
			audit.title( "Numerical Context" );
			clearTheNeedsList();
			run( "i need a coffee",     "ok, you need a coffee" );
			run( "and another",         "ok, you need another coffee" );
			run( "how many coffees do i need", "2, you need 2 coffees" );
			run( "i need a cup of tea", "ok, you need a cup of tea" );
			run( "and another coffee",  "ok, you need another coffee" );
			run( "what do i need",      "You need 3 coffees , and a cup of tea" );
			
			audit.title( "Correction" );
			run( "i need another coffee", "ok, you need another coffee" );
			run( "no i need another 3",   "ok, you need another 3 coffees" );
			run( "what do i need",        "you need 6 coffees, and a cup of tea" );
			run( "i don't need anything", "ok, you don't need anything" );
			
			audit.title( "Group-as-entity" );		
			clearTheNeedsList( "MartinAndRuth does not need anything" );
			
			run( "martin and ruth need a coffee and a tea",
			         "ok, martin and ruth need a coffee and a tea" );
			
			run( "what do martin and ruth need",
			         "martin and ruth need a coffee , and a tea" );
			
			run( "martin and ruth do not need a tea", 
			         "ok, martin and ruth don't need a tea" );
			
			run( "what do martin and ruth need",
			         "martin and ruth need a coffee" );
			
			run( "martin and ruth need some biscuits",
			         "ok, martin and ruth need some biscuits" );
			
			run( "what do martin and ruth need",
			         "martin and ruth need a coffee, and some biscuits" );
			// Tidy up
			run( "martin and ruth do not need anything", "ok , martin and ruth don't need anything" );

			audit.title( "Combos, multiple singular entities");
			run( "james and martin and ruth all need a chocolate biscuit",
			         "ok, james and martin and ruth all need a chocolate biscuit" );
			
			run( "martin and ruth both need a cocoa and a chocolate biscuit",
			         "ok, martin and ruth both need a cocoa and a chocolate biscuit" );
			
			run( "what does martin need",
					 "martin needs a chocolate biscuit, and a cocoa" );
			clearTheNeedsList( "james  doesn't need anything" );
			clearTheNeedsList( "martin doesn't need anything" );
			clearTheNeedsList( "ruth   doesn't need anything" );
			
			audit.title( "Pronouns - see need+needs.txt" );
			clearTheNeedsList();
			
			run( "i need biscuits and coffee", "ok, you need biscuits and coffee" );
			run( "they are from Sainsbury's",  "ok, they are from sainsbury's" );
			run( "i need a pint of milk",      "ok, you need a pint of milk" );
			run( "it is from the dairy aisle", "ok, it is from the dairy aisle" );
			run( "i need cheese and eggs from the dairy aisle",
					                               "ok, you need cheese and eggs" );
			//mediate( "group by",                   "i'm sorry, i need to know what to group by" );
			run( "group by location",          "ok" );
			
			run( "what do i need from sainsbury's",
					   "you need biscuits, and coffee from sainsbury's" );
			
			run( "what do i need from the dairy aisle",
					   "you need a pint of milk, cheese, and eggs from the dairy aisle" );
			
			run( "i don't need anything from the dairy aisle",
					   "ok, you don't need anything from the dairy aisle" );
			
			audit.title( "Late Binding Floating Qualifiers" );
			clearTheNeedsList();
			run( "i need biscuits",       "ok, you need biscuits" );
			run( "i need milk from the dairy aisle", "ok, you need milk from the dairy aisle" );
			run( "i from the dairy aisle need milk", "yes, i know" );
			run( "from the dairy aisle i need milk", "yes, i know" );
			run( "what do i need",        "you need biscuits; and, milk from the dairy aisle" );
			run( "from the dairy aisle what do i need",  "you need milk from the dairy aisle" );
			run( "what from the dairy aisle do i need",  "you need milk from the dairy aisle" );
			run( "what do i need from the dairy aisle",  "you need milk from the dairy aisle" );
			
			audit.title( "Numbers ERROR!" );
			clearTheNeedsList();
			run( "i need an apple", "" );
			run( "how many apples do i need",  "1, you need 1 apples" ); // <<<<<<<<< see this!
		}
//		if (runThisTest( "james's experiment" )) { // variables, arithmetic and lambda tests
//			//interpret( "england is a country",  "ok, england is a country" );
//			test( "preston is in england", "ok, preston is in england" );
//			test( "i am in preston",       "ok, you're in england" );
//		}
		if (runTheseTests( "Simple Variables" )) { // 
			run( "the value of name is fred",       "ok, name is set to fred" );
			run( "get the value of name",           "fred" );
			run( "set the value of name to fred bloggs", "ok, name is set to fred bloggs" );
			run( "what is the value of name",       "fred bloggs, the value of name is fred bloggs" );
			
			audit.subtl( "Simple Numerics" );
			run( "set the weight of martin to 104", "ok" );
			run( "get the weight of martin",        "Ok, the weight of martin is 104" );
			
			// non-numerical values
			audit.title( "Simply ent/attr model" );
			run( "the height of martin is 194",  "Ok,  the height of martin is 194" );
			run( "what is the height of martin", "194, the height of martin is 194" );

			audit.title( "Apostrophe's ;-)" );
			run( "what is martin's height", "194, martin's height is 194" );
			run( "martin's height is 195",  "Ok,  martin's height is 195" );
			run( "what is the height of martin", "195, the height of martin is 195" );
		}
		if (runTheseTests( "Annotation" )) {
			run( "delete martin was       list", "ok" );
			run( "delete martin wasNot    list", "ok" );
			run( "delete i      am        list", "ok" );
			run( "delete i      amNot     list", "ok" );
			run( "delete martin is        list", "ok" );
			run( "delete martin isNot     list", "ok" );
			run( "delete i      willBe    list", "ok" );
			run( "delete i      willNotBe list", "ok" );
			run( "delete martin willBe    list", "ok" );
			run( "delete martin willNotBe list", "ok" );
			
			/*
			 * Test 5.1 - IS
			 */
			// e.g. i am alive - 5.1
			run( "interpret i am variable state thus",         "ok" );
			run( "first add    variable state to   i am list", "ok" );
			run( "then  remove variable state from i amNot list", "ok" );
			run( "then reply ok", "ok" );
			
			// e.g. i am not alive - 5.1
			run( "interpret i am not variable state thus",        "ok" );
			run( "first add    variable state to   i amNot list", "ok" );
			run( "then  remove variable state from i am    list", "ok" );
			run( "then reply ok", "ok" );
			
			// e.g. am i alive? - 5.1
			run( "interpret am i variable state thus",                "ok" );
			run( "first variable state exists in i am list",          "ok" );
			run( "then reply yes i am variable state",                "ok" );
			run( "then if not variable state exists in i amNot list", "ok" );
			run( "then if not reply i do not know",                   "ok" );
			run( "then reply no i am not variable state",             "ok" );
			run( "ok", "ok" );
			
			//  e.g. martin is alive - 5.1
			run( "interpret variable entity is variable state thus",            "ok" );
			run( "first add    variable state to   variable entity is    list", "ok" );
			run( "then  remove variable state from variable entity isNot list", "ok" );
			run( "then reply ok", "ok" );
			
			// e.g. martin is not alive - 5.1
			run( "interpret variable entity is not variable state thus",       "ok" );
			run( "first add   variable state to   variable entity isNot list", "ok" );
			run( "then remove variable state from variable entity is    list", "ok" );
			run( "then reply ok", "ok" );
			
			// e.g. is martin alive - 5.1
			run( "interpret is variable entity variable state thus",        "ok" );
			run( "first variable state  exists in variable entity is list", "ok" );
			run( "then reply yes variable entity is variable state",        "ok" );
			run( "then if not variable state exists in variable entity isNot list", "ok" );
			run( "then reply no variable entity is not variable state",     "ok" );
			run( "then if not reply i do not know",                         "ok" );
			run( "ok", "ok" );

			// e.g. is martin not alive - 5.1
			run( "interpret is variable entity not variable state thus",       "ok" );
			run( "first variable state  exists in variable entity isNot list", "ok" );
			run( "then reply yes variable entity is not variable state",        "ok" );
			run( "then if not variable state exists in variable entity is list", "ok" );
			run( "then reply no variable entity is variable state",             "ok" );
			run( "then if not reply i do not know",                            "ok" );
			run( "ok", "ok" );

			// test 5.1
			run( "am i alive",     "i don't know" );
			run( "i am alive",     "ok" );
			run( "am i alive",     "yes i'm alive" );
			run( "i am not alive", "ok" );
			run( "am i alive",     "no i'm not alive" );
			
			// test 5.1
			run( "is martin alive", "i don't know" );
			run( "martin is alive", "ok" );
			run( "is martin alive", "yes martin is alive" );
			run( "martin is not alive", "ok" );
			run( "is martin alive",     "no martin is not alive" );
			run( "is martin not alive", "yes martin is not alive" );
			
			/*
			 *  Test 5.2 was/was not
			 */
			//  e.g. martin was alive - 5.2
			run( "interpret variable entity was variable state thus",            "ok" );
			run( "first add    variable state to   variable entity was    list", "ok" );
			run( "then  remove variable state from variable entity wasNot list", "ok" );
			run( "then reply ok", "ok" );
			
			// e.g. martin was not alive - 5.2
			run( "interpret variable entity was not variable state thus",       "ok" );
			run( "first add   variable state to   variable entity wasNot list", "ok" );
			run( "then remove variable state from variable entity was    list", "ok" );
			run( "then reply ok", "ok" );
			
			// e.g. was martin alive - 5.2
			run( "interpret was variable entity variable state thus",        "ok" );
			run( "first variable state  exists in variable entity was list", "ok" );
			run( "then reply yes variable entity was variable state",        "ok" );
			run( "then if not variable state exists in variable entity wasNot list", "ok" );
			run( "then reply no variable entity was not variable state",     "ok" );
			run( "then if not reply i do not know",                          "ok" );
			run( "ok", "ok" );

			// e.g. was martin not alive - 5.2
			run( "interpret was variable entity not variable state thus",       "ok" );
			run( "first variable state  exists in variable entity wasNot list", "ok" );
			run( "then reply yes variable entity was not variable state",       "ok" );
			run( "then if not variable state exists in variable entity was list", "ok" );
			run( "then reply no variable entity was variable state",            "ok" );
			run( "then if not reply i do not know",                             "ok" );
			run( "ok", "ok" );

			// test 5.2
			run( "was martin alive",     "i don't know" );
			run( "martin was alive",     "ok" );
			run( "was martin alive",     "yes martin was alive" );
			run( "martin was not alive", "ok" );
			run( "was martin alive",     "no martin was not alive" );
			run( "was martin not alive", "yes martin was not alive" );
			
			/*
			 *  Test 5.3 will be/will not be
			 */
			//  e.g. martin will be alive - 5.3
			run( "interpret variable entity will be variable state thus",           "ok" );
			run( "first add    variable state to   variable entity willBe    list", "ok" );
			run( "then  remove variable state from variable entity willNotBe list", "ok" );
			run( "then reply ok", "ok" );
			
			// e.g. martin will not be alive - 5.3
			run( "interpret variable entity will not be variable state thus",      "ok" );
			run( "first add   variable state to   variable entity willNotBe list", "ok" );
			run( "then remove variable state from variable entity willBe    list", "ok" );
			run( "then reply ok", "ok" );
			
			// e.g. will martin be alive - 5.3
			run( "interpret will variable entity be variable state thus",      "ok" );
			run( "first variable state exists in variable entity willBe list", "ok" );
			run( "then reply yes variable entity will be variable state",      "ok" );
			run( "then if not variable state exists in variable entity willNotBe list", "ok" );
			run( "then reply no variable entity will not be variable state",   "ok" );
			run( "then if not reply i do not know",                            "ok" );
			run( "ok", "ok" );

			// e.g. will martin not be alive - 5.3
			run( "interpret will variable entity not be variable state thus",      "ok" );
			run( "first variable state  exists in variable entity willNotBe list", "ok" );
			run( "then reply yes variable entity will not be variable state",      "ok" );
			run( "then if not variable state exists in variable entity willBe list", "ok" );
			run( "then reply no variable entity will be variable state",           "ok" );
			run( "then if not reply i do not know",                                "ok" );
			run( "ok", "ok" );

			// test 5.3
			run( "will i be alive",     "i don't know" );
			run( "i will be alive",     "ok" );
			run( "will i be alive",     "yes you'll be alive" );
			run( "i will not be alive", "ok" );
			run( "will i be alive",     "no you'll not be alive" );
			run( "will i not be alive", "yes you'll not be alive" );

			run( "will martin be alive",     "i don't know" );
			run( "martin will be alive",     "ok" );
			run( "will martin be alive",     "yes martin will be alive" );
			run( "martin will not be alive", "ok" );
			run( "will martin be alive",     "no martin will not be alive" );
			run( "will martin not be alive", "yes martin will not be alive" );

			// Test
			// Event: to move is to was (traverse time quanta)
			// interpret( "interpret when i am dead then move what i am to what i was thus", "ok" );
		}
		if (runTheseTests( "Verbal Arithmetic" )) {
			run( "what's 1 + 2",                     "1 plus 2 is 3" );
			run( "times 2 all squared",              "times 2 all squared makes 36" );
			run( "what is 36 + 4     divided by 2",  "36 plus 4     divided by 2 is 38" );
			run( "what is 36 + 4 all divided by 2",  "36 plus 4 all divided by 2 is 20" );
			
			audit.title( "Simple Functions" );
			run( "the sum of x and y is x plus y",  "ok, the sum of x and y is x plus y" );
			run( "what is the sum of 3 and 2",      "the sum of 3 and 2 is 5 " );
			run( "set x to 3",                      "ok, x is set to 3" );
			run( "set y to 4",                      "ok, y is set to 4" );
			run( "what is the value of x",          "3, the value of x is 3" );
			run( "what is the sum of x and y",      "the sum of x and y is 7" );
			
			audit.title( "Factorial Description" );
			//mediate( "what is the factorial of 4",       "I don't know" );
			/* Ideally, we want:
			 * - the factorial of 1 is 1;
			 * - the factorial of n is n times the factorial of n - 1;
			 * - what is the factorial of 3.
			 */
			run( "the factorial of 1 is 1",          "ok, the factorial of 1 is 1" );
			
			// in longhand this is...
			run( "to the phrase what is the factorial of 0 reply 1", "ok" );
			run( "what is the factorial of 0",  "1" );
			
			run( "interpret multiply numeric variable a by numeric variable b thus", "ok" );
			run( "first evaluate variable a times variable b",                       "ok" );
			run( "ok", "ok" );
			
			run( "the product of x and y is x times y", "" );
			run( "what is the product of 3 and 4",  "the product of 3 and 4 is 12" );
			//TODO:
			//interpret( "what is the product of x and y",  "the product of x and y is x times y" );
			run( "the square of x is x times x",    "Ok, the square of x is x times x" );
			run( "what is 2 times the square of 2", "2 times the square of 2 is 8" );
			
			// again, in longhand this is...
			run( "interpret subtract numeric variable c from numeric variable d thus", "ok" );
			run( "first evaluate variable d minus variable c",                         "ok" );
			run( "ok", "ok" );
			
			run( "subtract 2 from 3", "1" );
			
			// interpret( "the factorial of n is n times the factorial of n - 1", "ok" );
			// interpret( "what is the factorial of n",   "n is n times the factorial of n minus 1" );
//			mediate( "interpret what is the factorial of numeric variable n thus",  "ok" );
//			mediate( "first subtract 1 from variable n",                            "ok" );
//			mediate( "then what is the factorial of whatever",                      "ok" );
//			mediate( "then multiply whatever by variable n",  "ok" );
//			mediate( "then reply whatever the factorial of variable n is whatever", "ok" );
//			mediate( "ok", "ok" );
			
			run( "the factorial of n is n times the factorial of n minus 1",
					"ok, the factorial of n is n times the factorial of n minus 1" );
			run( "what is the factorial of 4", "the factorial of 4 is 24" );
		}
		if (runTheseTests( "Temporal interpret" )) {
			run( "what day is christmas day", "" );
			//testInterpret( "what day is it today" );
			// my date of birth is
			// how old am i.
			// age is the given date minus the date of inception
			// if no date given, use the current date.
			// persons age given in years
			// what is my age [in <epoch default="years"/>]
		}
		if (runTheseTests( "Temporospatial concept MEETING" )) {
			/* TODO: interpret think of a variable entity thus.  // see sofa for particular details!
			 * first create a class variable entity.             // mkdir pub; touch pub/isa 
			 * then  create an anonymous entity variable entity. // mkdir pub/a
			 * then  set the context of the variable entity to a variable entity // ln -s pub/the pub/a
			 * ok.
			 */
			run( "I'm not meeting anybody",
					   "Ok , you're not meeting anybody" );
			run( "At 7 I'm meeting my brother at the pub",
					   "Ok , you're meeting your brother at 7 at the pub" );
			run( "When  am I meeting my brother",
					   "You're meeting your brother at 7" );
			run( "Where am I meeting my brother",
					   "You're meeting your brother at the pub" );
			run( "Am I meeting my brother",
					   "Yes , you're meeting your brother" );
			
			run( "I'm meeting my sister at the pub", "" );
			run( "When am I meeting my sister",
					   "I don't know when you're meeting your sister" );
			
			run( "When am I meeting my dad",
					   "i don't know if you're meeting your dad" );
			run( "Where am I meeting my dad" ,
					   "i don't know if you're meeting your dad" );
		}
		if (runTheseTests( "Generic Pronouns" )) { // Language features
			clearTheNeedsList( "martin doesn't need anything" );
			run( "martin needs a coffee", "ok, martin needs a coffee" );
			run( "what does he need",     "martin needs a coffee" );
			clearTheNeedsList( "martin doesn't need anything" );
			
			run( "ruth needs a tea",      "ok, ruth needs a tea" );
			run( "what does she need",    "ruth needs a tea" );
			clearTheNeedsList( "ruth   doesn't need anything" );
			
			run( "laurel and hardy need a coffee and a tea",
			         "ok, laurel and hardy need a coffee and a tea" );
			
			run( "what do they need",     "laurel and hardy need a coffee , and a tea" );
			clearTheNeedsList( "MartinAndRuth does not need anything" );
			
			run( "james needs 3 eggs because he is baking a cake",
					 "ok, james needs 3 eggs because he is baking a cake" );
		}
		if (runTheseTests()) { // 
			/* TODO:
			 *  create a queen called elizabeth the first  (eliz = woman's name, a queen is a monarch => person)
			 *  she died in 1603
			 *  she reigned for 45 years (so she ascended/came to the throne in 1548!)
			 */
			run( "a queen is a monarch", "ok, a queen is a monarch" );
		}
		if (runTheseTests( "Disambiguation" )) {
			run( "the eagle has landed",    ""   /* "Are you an ornithologist" */);
			run( "no the eagle has landed", "" /* "So , you're talking about the novel" */ );
			run( "no the eagle has landed", "" /*"So you're talking about Apollo 11" */	);
			run( "no the eagle has landed", "" /* "I don't understand" */ );
			// Issue here: on DNU, we need to advance this on "the eagle has landed"
			// i.e. w/o "no ..."
		}
		if (runTheseTests( "TCP/IP test" )) {
			// bug here??? config.xml has to be 8080 (matching this) so does  // <<<< see this!
			// config port get chosen over this one???
			run( "tcpip localhost "+ Server.TestPort +" \"a test port address\"", "ok" );
			run( "tcpip localhost 5678 \"this is a test, which will fail\"",  "i'm sorry" );
			run( "simon says put your hands on your head", "" ); //, "ok, success" );
		}
		if (runTheseTests( "Polymorphism - setup new idea and save" )) { // code generation features
			
			clearTheNeedsList( "i don't want anything" );
			run( "want is unlike need", "ok, want is unlike need", "yes, i know" );
			run( "what do i want",      "i'm sorry, i don't understand the question" );
			
			run( "want is like need",   "ok, want is like need" );
			run( "what do i want",      "you want another pony", "you don't want anything" );
			run( "i want another pony", "yes, i know", "ok, you want another pony" );
			run( "what do i want",      "you want another pony" );
		}
		if (runTheseTests( "On-the-fly Language Learning" )) { // 
			/* TODO: create filename from pattern:
			 *    "i need phrase variable objects" => i_need-.txt (append? create overlay)
			 *    "this is part of the need concept" => need.txt (append)
			 *    Enguage.interpret() => overlay
			 *    Conceept.load() => can this outlive Enguage overlay???
			 */

			// First, what we can't say yet...
			run( "my name is martin",                 "I don't understand, my name is martin" );
			run( "if not  reply i already know this", "I don't understand, if not reply i already know this" );
			run( "unset the value of name",           "ok" );

			// build-a-program...
			run( "interpret my name is phrase variable name thus", "ok" );
			run( "first set name to variable name",                "ok" );
			run( "then get the value of name",                     "ok" ); // not strictly necessary!
			run( "then reply hello whatever",                      "ok" );
			run( "ok",                                             "ok"    );

			run( "my name is ruth",   "hello   ruth" );
			run( "my name is martin", "hello martin" );


			//...or to put it another way
			run( "interpret i am called phrase variable name like this", "ok" );
			run( "first reply hi whatever",                             "ok" );
			run( "this implies that you set name to variable name",    "ok" );
			run( "this implies that name is not set to variable name", "ok" );
			run( "then if not reply i already know this",              "ok" );

			run( "i am called martin", "i already know this" );

			// ...means.../...the means to...
			// 1. from the-means-to repertoire
			run( "to the phrase phrase variable x the means to phrase variable y reply i really do not understand", "ok" );
			
			run( "do we have the means to become rich", "I really don't understand" );

			// 2. could this be built thus?
			run( "to phrase variable this means phrase variable that reply ok", "ok" );
			run( "this implies that you set transformation to false",        "ok" );
			run( "this implies that you perform sign think variable that",   "ok" );
			run( "this implies that you perform sign create variable this",  "ok" );
			run( "this implies that you set transformation to true",         "ok" );

			run( "just call me phrase variable name means i am called variable name", "ok" );
			run( "just call me martin", "i already know this" );
		}
		if (runTheseTests( "Example: 9-line input" )) {
			run( "havoc 1 this is a Type II control",  "ok, go ahead" );
			run( "lines 1 through 3 are not applicable",
					 "ok, lines 1 through 3 are not applicable" );
			run( "target elevation is 142 feet",      "ok, target elevation is 142 feet" );
			run( "target description is vehicle in the open",
					 "ok, target description is vehicle in the open" );
			run( "target location is three zero uniform, whiskey Foxtrot, 15933 13674",
					 "ok, target location is three zero uniform, whiskey Foxtrot, 15933 13674" );
			run( "none",                              "ok, mark type is none" );
			run( "friendlies are 30 clicks east of target",
					 "ok, friendlies are present" );
			run( "egress back into the wheel",        "ok, egress back into the wheel" );
			
			run( "read back",
					"line 1 is not applicable . "+
					"line 2 is not applicable . "+
					"line 3 is not applicable . "+
					"target elevation is 142 feet . "+
					"target description is vehicle in the open . "+
					"target location is three zero uniform , whiskey foxtrot , 15933 13674 . "+
					"none . "+
					"friendlies are 30 clicks east of target . "+
					"egress is back into the wheel . "+
					"ok" );
			
//			test( "no friendlies",                "ok, no friendlies are present" );
//			test( "where are friendlies",         "no friendlies are present" );
			run( "what is the target elevation", "142 feet, target elevation is 142 feet"     );
			run( "where are friendlies",         "30 clicks east of target, friendlies are 30 clicks east of target" );
		}
		if (runTheseTests( "Light bins" )) {
			run( "there are 6 light bins",        "ok, there are 6 light bins" );
			run( "how many light bins are there", "6,  there are 6 light bins" );
			run( "show me light bin 6",           "ok, light bin 6 is flashing", "i'm sorry" );
		}
		if (runTheseTests( "Checking spoken concepts - have we remembered Hello" )) {
			// see if we've remembered hello... shouldn't have
			run( "hello", "i don't understand" );
		}
		if (runTheseTests( "Ask: Confirmation" )) {
//
			run( "the colour of the sky is blue", "ok, the colour of the sky is blue" );
			run( "what is the colour of the sky", "blue , the colour of the sky is blue" );
			
			run( "to the phrase ask me phrase variable question reply variable question", "ok" );

			run( "the answer is blue", "ok, the next answer will be blue" );
			run( "ask me what is the colour of the sky", "what is the colour of the sky" );
			run( "blue", "i don't understand, blue" );
//
//			//interpret( "prime the answer i do not understand", "ok, the next answer will be i don't understand" );
//			//interpret( "i have everything", "Ok , let us leave things as they are" );
//
//			/* TODO:
//			 * To the phrase: i am p v name       => set user name NAME
//			 *                my name is p v name => set user name NAME
//			 *                p v name            => set user name NAME
//			 * Ask: what is your name?
//			 */
		}
		if (runTheseTests( "should" )) {
			// Construct a 'simple' approach to "should":
			// afford "what should we do"
			think( "to the phrase what should we do reply we should..." );
			think( "this implies that you get the value of should" );
			think( "then if not reply i don't know what we should do"   );
			
			// test this...
			run( "what should we do", "i don't know what we should do" );

			// afford "you should ..."
			think( "to the phrase you should phrase variable action reply ok variable action" );
			think( "this implies that you set the value of should to variable action" );
			
			// Construct an argument:
			// afford "wearing a mask prevents the spread of covid"...
			youCanSay( "wearing a mask prevents the spread of covid" );
			youCanSay( "wearing a mask does not prevent the spread of covid" );

			// afford the premise: "a mask soaks up moisture from your breath"
			youCanSay( "a mask soaks up moisture from your breath" );
			youCanSay( "a mask does not soak up moisture from your breath" );
			
			// Test the argument...
			run( "wearing a mask prevents the spread of covid because a mask soaks up moisture from your breath",
			     "ok , wearing a mask prevents the spread of covid because a mask soaks up moisture from my breath" );
			run( "you should wear a mask because wearing a mask prevents the spread of covid",
				 "ok , i should wear a mask because wearing a mask prevents the spread of covid" );

			// We can now conclude...
			run( "what should we do", "we should wear a mask" );
		}
		if (runTheseTests( "business scripts" )) {
			Audit.LOG( System.getenv( "PATH" ));
			run( "to the phrase my engine is 1598cc reply ok your engine is 1598cc", "ok" );
			run( "this implies that you run value engine/capacity ", "ok" );
			//run( "my engine is 1598cc", "ok" );
		}
		
		Audit.log( testGrp +" test group(s) found" );
		audit.PASSED();
}	}
