package com.yagadi.enguage;

import com.yagadi.enguage.interpretant.repertoire.Repertoire;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Strings;
import com.yagadi.enguage.vehicle.Question;
import com.yagadi.enguage.vehicle.Reply;

public class Test {
	
	static private  Audit audit = new Audit( "Test" );
	
	private static void testInterpret( String cmd, String expected ) {
		
		if (expected != null)
			audit.log( "enguage> "+ cmd );
			
		String answer = Enguage.interpret( cmd );
		
		if (expected != null) {
			int len = expected.length();
			if (len > 0 && expected.charAt( len - 1 ) != '.') expected += ".";
			if (!Reply.understood() && !Repertoire.prompt().equals( "" ))
				audit.log( "Hint is:" + Repertoire.prompt() );
			else if (   !expected.equals( "" )
			         && !new Strings( answer )
			         		.equalsIgnoreCase( new Strings( expected )))
				audit.FATAL("reply:"+ answer +",\n    expected:"+ expected );
			else
				audit.log( answer +"\n" );
	}	}
	private static void testInterpret( String cmd ) { testInterpret( cmd, "" );}

	public static void main( String args[]) {
		// useful ephemera
		//testInterpret( "detail on" );
		//Repertoire.signs.show( "OTF" );
		//testInterpret( "tracing on" );
		int argc = 0;
		if (args.length > 1 && args[ argc ].equals( "-c" )) {
			argc++;
			Enguage.loadConfig( args[ argc++ ]);
		} else
			Enguage.loadConfig( "./src/assets" );

		int level = 0;

		if ( level == 0 || level == 1 ) {
			audit.title( "The Non-Computable concept of NEED" );
			
			// silently clear the decks
			Question.primedAnswer( "yes" );
			testInterpret( "i don't need anything", null );

			testInterpret( "what do i need",
						   "you don't need anything" );
			testInterpret( "i need 2 cups of coffee and a biscuit",
						   "ok, you need 2 cups of coffee, and a biscuit.");
			testInterpret( "what do i need",
						   "you need 2 cups of coffee, and a biscuit.");
			testInterpret( "how many coffees do i need",
						   "2, you need 2 coffees" );
			testInterpret( "i don't need any coffee",
						   "ok, you don't need any coffee" );
			testInterpret( "what do i need",
						   "you need a biscuit" );

			audit.title( "Semantic Thrust" );
			testInterpret( "i need to go to town",
						   "ok, you need to go to town" );
			testInterpret( "what do i need",
						   "you need a biscuit, and to go to town" );
			testInterpret( "i have a biscuit",
						   "ok, you don't need a biscuit" );
			testInterpret( "i have to go to town",
						   "I know" );
			testInterpret( "i don't need to go to town",
						   "ok, you don't need to go to town" );
			
			Question.primedAnswer( "yes" );
			testInterpret( "I have everything",
					       "ok, you don't need anything" );
			
			testInterpret( "what do i need",
						   "you don't need anything" );
		}
		if ( level == 0 || level == 3 ) {
			audit.title( "Verbal Arithmetical" );
			testInterpret( "what is 1 + 2",
						   "1 plus 2 is 3.");
			testInterpret( "times 2 all squared",
						   "times 2 all squared makes 36.");
			testInterpret( "what is 36 + 4 all divided by 2",
						   "36 plus 4 all divided by 2 is 20" );
			
			audit.title( "Factorial Description" );
			testInterpret( "what is the factorial of 4", "I don't understand" );
			/* Ideally, we want:
			 * - the factorial of 1 is 1;
			 * - the factorial of n is n times the factorial of n - 1;
			 * - what is the factorial of 3.
			 */
			testInterpret( "to the phrase what is the factorial of 1 reply 1", "go on" );
			testInterpret( "ok", "ok" );
			testInterpret( "what is the factorial of 1",  "1" );
			
			testInterpret( "interpret multiply numeric variable a by numeric variable b thus", "go on" );
			testInterpret( "first perform numeric evaluate variable a times numeric phrase variable b",      "go on" );
			testInterpret( "ok", "ok" );
			
			testInterpret( "multiply 2 by 3", "6" );
			
			testInterpret( "interpret subtract numeric variable c from numeric variable d thus", "go on" );
			testInterpret( "first perform numeric evaluate variable d - variable c",                           "go on" );
			testInterpret( "ok", "ok" );
			
			testInterpret( "subtract 2 from 3", "1" );
			
			testInterpret( "interpret what is the factorial of numeric variable n thus",  "go on" );
			testInterpret( "first subtract 1 from variable n",                            "go on" );
			testInterpret( "then what is the factorial of whatever",                      "go on" );
			testInterpret( "then multiply whatever by variable n",                        "go on" );
			testInterpret( "then reply whatever the factorial of variable n is whatever", "go on" );
			testInterpret( "ok", "ok" );
			
			testInterpret( "what is the factorial of 4",  "24 the factorial of 4 is 24" );
		}
		if ( level == 0 || level == 4 ) {
			audit.title( "Numerical Context" );
			testInterpret( "i need a coffee",
						   "ok, you need a coffee" );
			testInterpret( "and another",
						   "ok, you need 1 more coffee" );
			testInterpret( "how many coffees do i need",
						   "2, you need 2 coffees" );
			testInterpret( "i need a cup of tea",
						   "ok, you need a cup of tea" );
			testInterpret( "and another coffee",
						   "ok, you need 1 more coffee" );
			testInterpret( "what do i need",
						   "You need 3 coffees , and a cup of tea" );
			
			audit.title( "Correction" );
			testInterpret( "i need another coffee",
						   "ok, you need 1 more coffee.");
			testInterpret( "no i need another 3",
						   "ok, you need 3 more coffees.");
			testInterpret( "what do i need",
						   "you need 6 coffees, and a cup of tea.");
			Question.primedAnswer( "yes" );
			testInterpret( "i don't need anything",
						   "ok, you don't need anything" );
		}
		if ( level == 0 || level == 5 ) {
			audit.title( "Annotation" );
			testInterpret( "delete martin was       list", "ok" );
			testInterpret( "delete martin wasNot    list", "ok" );
			testInterpret( "delete i      am        list", "ok" );
			testInterpret( "delete i      amNot     list", "ok" );
			testInterpret( "delete martin is        list", "ok" );
			testInterpret( "delete martin isNot     list", "ok" );
			testInterpret( "delete i      willBe    list", "ok" );
			testInterpret( "delete i      willNotBe list", "ok" );
			testInterpret( "delete martin willBe    list", "ok" );
			testInterpret( "delete martin willNotBe list", "ok" );
			
			//  e.g. martin is alive
			testInterpret( "interpret variable entity is variable state thus",            "go on" );
			testInterpret( "first add    variable state to   variable entity is    list", "go on" );
			testInterpret( "then  remove variable state from variable entity isNot list", "go on" );
			testInterpret( "then whatever reply ok",                                      "ok" );
			
			// e.g. martin is not alive
			testInterpret( "interpret variable entity is not variable state thus",       "go on" );
			testInterpret( "first add   variable state to   variable entity isNot list", "go on" );
			testInterpret( "then remove variable state from variable entity is    list", "go on" );
			testInterpret( "then whatever reply ok",                                     "ok" );
			
			// e.g. is martin alive
			testInterpret( "interpret is variable entity variable state thus",        "go on" );
			testInterpret( "first variable state  exists in variable entity is list", "go on" );
			testInterpret( "then reply yes variable entity is variable state",        "go on" );
			testInterpret( "then if not variable state exists in variable entity isNot list", "go on" );
			testInterpret( "then reply no variable entity is not variable state",     "go on" );
			testInterpret( "then if not reply i do not know",                         "go on" );
			testInterpret( "ok", "ok" );

			// e.g. is martin not alive
			testInterpret( "interpret is variable entity not variable state thus",       "go on" );
			testInterpret( "first variable state  exists in variable entity isNot list", "go on" );
			testInterpret( "then reply yes variable entity is not variable state",        "go on" );
			testInterpret( "then if not variable state exists in variable entity is list", "go on" );
			testInterpret( "then reply no variable entity is variable state",             "go on" );
			testInterpret( "then if not reply i do not know",                            "go on" );
			testInterpret( "ok", "ok" );

			// test 5.1
			testInterpret( "is martin alive", "i don't know" );
			testInterpret( "martin is alive", "ok" );
			testInterpret( "is martin alive", "yes martin is alive" );
			testInterpret( "martin is not alive", "ok" );
			testInterpret( "is martin alive",     "no martin is not alive" );
			testInterpret( "is martin not alive", "yes martin is not alive" );
			
			// e.g. i am alive
			testInterpret( "interpret i am variable state thus",         "go on" );
			testInterpret( "first add    variable state to   i am list", "go on" );
			testInterpret( "then  remove variable state from i amNot list", "go on" );
			testInterpret( "then whatever reply ok",                     "ok" );
			
			// e.g. i am not alive
			testInterpret( "interpret i am not variable state thus",        "go on" );
			testInterpret( "first add    variable state to   i amNot list", "go on" );
			testInterpret( "then  remove variable state from i am    list", "go on" );
			testInterpret( "then whatever reply ok",                        "ok" );
			
			// e.g. am i alive?
			testInterpret( "interpret am i variable state thus",                "go on" );
			testInterpret( "first variable state exists in i am list",          "go on" );
			testInterpret( "then reply yes i am variable state",                "go on" );
			testInterpret( "then if not variable state exists in i amNot list", "go on" );
			testInterpret( "then if not reply i do not know",                   "go on" );
			testInterpret( "then reply no i am not variable state",             "go on" );
			testInterpret( "ok", "ok" );
			
			// test 5.2
			testInterpret( "am i alive",     "i don't know" );
			testInterpret( "i am alive",     "ok" );
			testInterpret( "am i alive",     "yes i'm alive" );
			testInterpret( "i am not alive", "ok" );
			testInterpret( "am i alive",     "no i'm not alive" );
			
			// Test 5.3 was/was not
			//  e.g. martin was alive
			testInterpret( "interpret variable entity was variable state thus",            "go on" );
			testInterpret( "first add    variable state to   variable entity was    list", "go on" );
			testInterpret( "then  remove variable state from variable entity wasNot list", "go on" );
			testInterpret( "then whatever reply ok",                                       "ok" );
			
			// e.g. martin was not alive
			testInterpret( "interpret variable entity was not variable state thus",       "go on" );
			testInterpret( "first add   variable state to   variable entity wasNot list", "go on" );
			testInterpret( "then remove variable state from variable entity was    list", "go on" );
			testInterpret( "then whatever reply ok",                                      "ok" );
			
			// e.g. was martin alive
			testInterpret( "interpret was variable entity variable state thus",        "go on" );
			testInterpret( "first variable state  exists in variable entity was list", "go on" );
			testInterpret( "then reply yes variable entity was variable state",        "go on" );
			testInterpret( "then if not variable state exists in variable entity wasNot list", "go on" );
			testInterpret( "then reply no variable entity was not variable state",     "go on" );
			testInterpret( "then if not reply i do not know",                          "go on" );
			testInterpret( "ok", "ok" );

			// e.g. was martin not alive
			testInterpret( "interpret was variable entity not variable state thus",       "go on" );
			testInterpret( "first variable state  exists in variable entity wasNot list", "go on" );
			testInterpret( "then reply yes variable entity was not variable state",       "go on" );
			testInterpret( "then if not variable state exists in variable entity was list", "go on" );
			testInterpret( "then reply no variable entity was variable state",            "go on" );
			testInterpret( "then if not reply i do not know",                             "go on" );
			testInterpret( "ok", "ok" );

			// test 5.3
			testInterpret( "was martin alive",     "i don't know" );
			testInterpret( "martin was alive",     "ok" );
			testInterpret( "was martin alive",     "yes martin was alive" );
			testInterpret( "martin was not alive", "ok" );
			testInterpret( "was martin alive",     "no martin was not alive" );
			testInterpret( "was martin not alive", "yes martin was not alive" );
			
			// Test 5.4 will be/will not be
			//  e.g. martin will be alive
			testInterpret( "interpret variable entity will be variable state thus",           "go on" );
			testInterpret( "first add    variable state to   variable entity willBe    list", "go on" );
			testInterpret( "then  remove variable state from variable entity willNotBe list", "go on" );
			testInterpret( "then whatever reply ok",                                          "ok" );
			
			// e.g. martin will not be alive
			testInterpret( "interpret variable entity will not be variable state thus",      "go on" );
			testInterpret( "first add   variable state to   variable entity willNotBe list", "go on" );
			testInterpret( "then remove variable state from variable entity willBe    list", "go on" );
			testInterpret( "then whatever reply ok",                                         "ok" );
			
			// e.g. will be martin alive
			testInterpret( "interpret will variable entity be variable state thus",      "go on" );
			testInterpret( "first variable state exists in variable entity willBe list", "go on" );
			testInterpret( "then reply yes variable entity will be variable state",      "go on" );
			testInterpret( "then if not variable state exists in variable entity willNotBe list", "go on" );
			testInterpret( "then reply no variable entity will not be variable state",   "go on" );
			testInterpret( "then if not reply i do not know",                            "go on" );
			testInterpret( "ok", "ok" );

			// e.g. will be martin not alive
			testInterpret( "interpret will variable entity not be variable state thus",      "go on" );
			testInterpret( "first variable state  exists in variable entity willNotBe list", "go on" );
			testInterpret( "then reply yes variable entity will not be variable state",      "go on" );
			testInterpret( "then if not variable state exists in variable entity willBe list", "go on" );
			testInterpret( "then reply no variable entity will be variable state",           "go on" );
			testInterpret( "then if not reply i do not know",                                "go on" );
			testInterpret( "ok", "ok" );

			// test 5.4
			testInterpret( "will i be alive",     "i don't know" );
			testInterpret( "i will be alive",     "ok" );
			testInterpret( "will i be alive",     "yes you'll be alive" );
			testInterpret( "i will not be alive", "ok" );
			testInterpret( "will i be alive",     "no you'll not be alive" );
			testInterpret( "will i not be alive", "yes you'll not be alive" );

			testInterpret( "will martin be alive",     "i don't know" );
			testInterpret( "martin will be alive",     "ok" );
			testInterpret( "will martin be alive",     "yes martin will be alive" );
			testInterpret( "martin will not be alive", "ok" );
			testInterpret( "will martin be alive",     "no martin will not be alive" );
			testInterpret( "will martin not be alive", "yes martin will not be alive" );

			// Test
			// ... and  X is/was/will be Y: first-person to third-person...
			// Event: to move is to was (traverse time quanta)
			
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
			testInterpret( "a queen is a monarch" );
			// my name is martin
			// my name is martin wheatman

		}
		if ( level == 0 || level == 6 ) {
			audit.title( "Disambiguation" );
			testInterpret( "the eagle has landed" //,
						   //"Are you an ornithologist."
					);
			testInterpret( "no the eagle has landed" //,
						   //"So , you're talking about the novel."
					);
			testInterpret( "no the eagle has landed" //, 
						   //"So you're talking about Apollo 11."
					);
			testInterpret( "no the eagle has landed" //,
						   //"I don't understand"
					);
			// Issue here: on DNU, we need to advance this on "the eagle has landed"
			// i.e. w/o "no ..."
		}
		if ( level == 0 || level == 7 ) {
			audit.title( "Temporal interpret" );
			testInterpret( "what day is christmas day" );
			//testInterpret( "what day is it today" );

			audit.title( "Temporospatial concept MEETING" );
			
			//Where.locatorIs( "at" ); is the same as...
			// new Sofa().interpret( new Strings( "spatial locator at" )); is the same as...
			testInterpret( "interpret spatially something can be phrase variable locator thus", "go on" );
			testInterpret( "first perform spatial locator variable locator", "go on" );
			testInterpret( "ok", "ok" );
			
			testInterpret( "spatially something can be to the left of",  "ok" );
			testInterpret( "spatially something can be to the right of", "ok" );
			testInterpret( "spatially something can be in front of",     "ok" );
			testInterpret( "spatially something can be on top of",       "ok" );
			testInterpret( "spatially something can be behind",          "ok" );
			testInterpret( "spatially something can be in",              "ok" );
			testInterpret( "spatially something can be on",              "ok" );
			testInterpret( "spatially something can be under",           "ok" );
			testInterpret( "spatially something can be underneath",      "ok" );
			testInterpret( "spatially something can be over",            "ok" );
			testInterpret( "spatially something can be at",              "ok" );
			
			/* TODO: interpret think of a variable entity thus.  // see sofa for particular details!
			 * first create a class variable entity.             // mkdir pub; touch pub/isa 
			 * then  create an anonymous entity variable entity. // mkdir pub/a
			 * then  set the context of the variable entity to a variable entity // ln -s pub/the pub/a
			 * ok.
			 */
			// Creating a pub _is_ needed
			testInterpret( "a pub is a place" );

			testInterpret( "I'm not meeting anybody",
					"Ok , you're not meeting anybody" );
			testInterpret( "At 7 I'm meeting my brother at the pub",
					"Ok , you're meeting your brother at 7 at the pub" );
			testInterpret( "When  am I meeting my brother",
					"You're meeting your brother at 7" );
			testInterpret( "Where am I meeting my brother",
					"You're meeting your brother at the pub" );
			testInterpret( "Am I meeting my brother",
					"Yes , you're meeting your brother" );
			
			testInterpret( "I'm meeting my sister at the pub" );
			testInterpret( "When am I meeting my sister",
					"I don't know when you're meeting your sister" );
			
			testInterpret( "When am I meeting my dad",
					"i don't know if you're meeting your dad" );
			testInterpret( "Where am I meeting my dad" ,
					"i don't know if you're meeting your dad" );
		}
		if (level == 0 || level == 8) {
			testInterpret( "tcpip localhost 999 \"999 is a test value for port address\"",   "ok" );
			testInterpret( "tcpip localhost 5678 \"this is a test, which will fail\"",    "Sorry" );
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
			testInterpret( "my name is martin",                 "I don't understand" );
			testInterpret( "if not  reply i already know this", "I don't understand" );
			testInterpret( "unset the value of name",           "ok" );
			
			// build-a-program...
			testInterpret( "interpret my name is phrase variable name thus", "go on" );
			testInterpret( "first set name to variable name",                "go on" );
			testInterpret( "then get the value of name",                     "go on" ); // not strictly necessary!
			testInterpret( "then reply hello whatever",                      "go on" );
			testInterpret( "ok",                                             "ok"    );

			testInterpret( "my name is ruth",   "hello   ruth" );
			testInterpret( "my name is martin", "hello martin" );
			
			
			//...or to put it another way
			testInterpret( "to the phrase i am called phrase variable name reply hi whatever", "go on" );
			testInterpret( "this implies name gets set to variable name",   "go on" );
			testInterpret( "this implies name is not set to variable name", "go on" );
			testInterpret( "if not reply i already know this",              "go on" );
			testInterpret( "ok", "ok" );
			
			testInterpret( "i am called martin", "i already know this" );
			
			// ...means.../...the means to...
			// 1. from the-means-to repertoire
			testInterpret( "to the phrase phrase variable x the means to phrase variable y reply i really do not understand", "go on" );
			testInterpret( "ok", "ok" );

			testInterpret( "do we have the means to become rich", "I really don't understand" );

			// 2. could this be built thus?
			testInterpret( "to phrase variable this means phrase variable that reply ok", "go on" );
			testInterpret( "this implies perform sign think variable that",               "go on" );
			testInterpret( "this implies perform sign create variable this",              "go on" );
			testInterpret( "ok", "ok" );
			
			testInterpret( "just call me phrase variable name means i am called variable name", "ok" );
			testInterpret( "just call me martin", "i already know this" );
		}
		if ( level == 0 || level == 10 ) {
			audit.title( "Ask: Confirmation" );
			
			Question.primedAnswer( "yes" );
			testInterpret( "i have everything", "ok , you don't need anything" );
			
			Question.primedAnswer( "no" );
			testInterpret( "i have everything", "ok , let us leave things as they are" );

			Question.primedAnswer( "i do not understand" );
			testInterpret( "i have everything", "Ok , let us leave things as they are" );
			
			/* TODO:
			 * To the phrase: i am p v name       => set user name NAME
			 *                my name is p v name => set user name NAME
			 *                p v name            => set user name NAME
			 * Ask: what is your name?
			 */
		}
		audit.log( "PASSED" );
}	}
