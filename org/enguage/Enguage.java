package org.enguage;

import java.io.File;

import org.enguage.interp.intention.Redo;
import org.enguage.interp.pattern.Pattern;
import org.enguage.interp.repertoire.Autoload;
import org.enguage.interp.repertoire.Concepts;
import org.enguage.interp.repertoire.Repertoire;
import org.enguage.interp.repertoire.Synonyms;
import org.enguage.objects.Variable;
import org.enguage.objects.space.Overlay;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.sys.Fs;
import org.enguage.util.sys.Net;
import org.enguage.util.sys.Shell;
import org.enguage.vehicle.Utterance;
import org.enguage.vehicle.pronoun.Pronoun;
import org.enguage.vehicle.reply.Reply;
import org.enguage.vehicle.where.Where;

public class Enguage {

	static public final String    DNU = "DNU";
	static public final String defLoc = "./src/assets";

	static private  Audit audit = new Audit( "Enguage" );

	static private Shell shell = new Shell( "Enguage" );
	static public  Shell shell() {return shell;}
	
	/*
	 * Enguage should be independent of Android, but...
	 */
	static private Config   config = new Config();
	static public  int  loadConfig( String content ) { return Enguage.config.load( content ); }

	static private Object  context = null; // if null, not on Android
	static public  Object  context() { return context; }
	static public  void    context( Object activity ) { context = activity; }

	static public  Overlay o = Overlay.Get();

	static public  void concepts( String[] names ) { Concepts.names( names );}

	static public  void   root( String rt ) { Fs.root( rt ); }
	static public  String root() { return Fs.root();}

	static public  void location( String loc ) {if(!Fs.location( loc )) audit.FATAL(loc +": not found");}

	static public void init( String pth, Object ctx, String[] cncpts) {init( pth, pth, ctx, cncpts );}
	static public void init( String loc, String root, Object ctx, String[] concepts) {
		location( loc );
		root( root );
		context( ctx );
		concepts( concepts );

		if ((null == o || !o.attached() ) && !Overlay.autoAttach())
			audit.ERROR( "Ouch! >>>>>>>> Cannot autoAttach() to object space<<<<<<" );
	}


	static public Strings mediate( Strings utterance ) {
		audit.in( "interpret", utterance.toString() );
		
		if (Net.serverOn()) Audit.log( "Server  given: " + utterance.toString() );
		
		// locations contextual per utterance
		Variable.unset( Where.LOCTN );
		Variable.unset( Where.LOCTR );
		
		if (Reply.isUnderstood()) // from previous interpretation!
			o.startTxn( Redo.undoIsEnabled() ); // all work in this new overlay

		Reply r = Repertoire.mediate( new Utterance( utterance ));

		// once processed, keep a copy
		Utterance.previous( utterance );

		if (Reply.isUnderstood()) {
			o.finishTxn( Redo.undoIsEnabled() );
			Redo.disambOff();
		} else {
			// really lost track?
			audit.debug( "Enguage:interpret(): not understood, forgetting to ignore: "
			             +Repertoire.signs.ignore().toString() );
			Repertoire.signs.ignoreNone();
			shell.aloudIs( true ); // sets aloud for whole session if reading from fp
		}

		// auto-unload here - autoloading() in Repertoire.interpret() 
		// asymmetry: load as we go; tidy-up once finished
		if (!Repertoire.induction() && !Autoload.ing()) Autoload.unload();

		Strings reply = Reply.say().appendAll( r.toStrings());
		Reply.say( null );
		if (Net.serverOn()) Audit.log( "Server replied: "+ reply );
		return audit.out( reply );
	}
	
	// ==== test code =====
	static private boolean   serverTest = false;

	static private int       portNumber = 8080;
	static private void      portNumber( String pn ) { portNumber = Integer.parseInt( pn );}

	private static void usage() {
		Audit.LOG( "Usage: java -jar enguage.jar [-d <configDir>] [-p <port> | -s | [--server <port>] -t ]" );
		Audit.LOG( "where: -d <configDir>" );
		Audit.LOG( "          config directory, default=\""+ defLoc +"\"\n" );
		Audit.LOG( "       -p <port>, --port <port>" );
		Audit.LOG( "          listens on local TCP/IP port number\n" );
		Audit.LOG( "       -c, --client" );
		Audit.LOG( "          runs Engauge as a shell\n" );
		Audit.LOG( "       -s, --server <port>" );
		Audit.LOG( "          switch to send test commands to a server." );
		Audit.LOG( "          This is only a test, and is on localhost." );
		Audit.LOG( "          (Needs to be initialised with -p nnnn)\n" );
		Audit.LOG( "       -t, --test" );
		Audit.LOG( "          runs a sanity check" );
	}
	static private void mediate( String cmd ) { mediate( cmd, null );}
	static private void mediate( String cmd, String expected ) { mediate( cmd, expected, null ); }
	static private void mediate( String cmd, String expected, String unexpected ) {
		
		// expected == null => silent!
		if (expected != null)
			Audit.log( "user> "+ cmd );
		
		Strings reply = serverTest ?
				new Strings( Net.client( "localhost", portNumber, cmd ))
				: Enguage.mediate( new Strings( cmd ));

		if (expected != null) {
			
			if (reply.equalsIgnoreCase( new Strings( expected )))
				audit.passed( "enguage> "+ reply +"\n" );      // 1st success
			
			else if (unexpected == null)                       // no second chance
				//Repertoire.signs.show();
				audit.FATAL(
						"reply: '"+ reply +"',\n             "+
								"expected: '"+ expected +"' "+
								"(reason="+ Pattern.notMatched() +")" );
			
			else if (reply.equalsIgnoreCase( new Strings( unexpected )))
				audit.passed( "enguage> "+ reply +"\n" );
			
			else                                             // second chance failed too!
				//Repertoire.signs.show();
				audit.FATAL(
						"reply: '"      + reply      +"'\n             "+
						"expected: '"   + expected   +"'\n          "+
						"alternately: '"+ unexpected +"'\n          "+
						"(reason="+ Pattern.notMatched() +")" );
	}	}
	
	public static void main( String args[] ) {
		//Audit.startupDebug = true;
		Strings    cmds = new Strings( args );
		String     cmd  = cmds.size()==0 ? "":cmds.remove( 0 );
		String location = defLoc;
		if (cmds.size() > 0 && cmd.equals( "-d" )) {
			location = cmds.remove(0);
			cmd = cmds.size()==0 ? "":cmds.remove(0);
		}

		Enguage.init( location, null, null, new File( location + "/concepts" ).list());

		loadConfig( Fs.stringFromFile( location + "/config.xml" ));

		boolean serverTest = false;
		if (cmds.size() > 0 && (cmd.equals( "-s" ) || cmd.equals( "--server" ))) {
			serverTest = true;
			cmds.remove(0);
			cmd = cmds.size()==0 ? "":cmds.remove(0);
			portNumber( cmds.remove( 0 ));
			cmd = cmds.size()==0 ? "":cmds.remove(0);
		}
				
		if (cmd.equals( "-c" ) || cmd.equals( "--client" ))
			Enguage.shell.aloudIs( true ).run();
		
		else if (cmds.size()>0 && (cmd.equals( "-p" ) || cmd.equals( "--port" )))
			Net.server( cmds.remove( 0 ));
		
		else if (cmd.equals( "-t" ) || cmd.equals( "--test" ))
			sanityCheck( serverTest, location );
		
		else usage();
	}
	
	// === test code ===
	// Call this direct, so it's not counted!
	static private final String ihe =  "I have everything";
	static private void clearTheNeedsList() { clearTheNeedsList( ihe );}
	static private void clearTheNeedsList( String s ) { Enguage.mediate( new Strings( s ));	}
	
	static private boolean runThisTest( int level, int test ) {
		return level == 0 || level == test || (level < 0 && level != -test);
	}
	public static void sanityCheck( boolean serverTest, String location ) {
		// ...useful ephemera...
		//interpret( "detail on" );
		//interpret( "tracing on" );
		//Audit.allOn();
		//Repertoire.signs.show( "OTF" );

		int level = 0; // TODO: 0 = every level, -n = ignore level n
		int testNo = 0;
		
		Audit.interval(); // reset timer

		Pronoun.interpret( new Strings( "add masculine martin" ));
		Pronoun.interpret( new Strings( "add masculine james" ));
		Pronoun.interpret( new Strings( "add feminine ruth" ));

		//if (runThisTest( level, ++testNo )) {
		//	mediate( "", "" );
		//}
		if (runThisTest( level, ++testNo )) { // WHY - These tests were for IJCSSA journal article
			audit.title( "Simple action demo" );
			mediate( "i am baking a cake",     "i know", "ok, you're baking a cake" );
			mediate( "am i baking a cake",     "yes, you're     baking a cake" );
			mediate( "i am not baking a cake", "ok,  you're not baking a cake" );
			
			audit.title( "Why/because" );
			mediate( "i am baking a cake so i need 3 eggs",
					   "ok, you need 3 eggs because you're baking a cake" );
			
			mediate( "am i baking a cake",      "yes, you're baking a cake" );
			mediate( "how many eggs do i need", "3, you need 3 eggs" );
			
			mediate( "so why do i need 3 eggs", "because you're baking a cake" );
			mediate( "do I need 3 eggs because I am baking a cake",
				       "yes, you need 3 eggs because you're baking a cake" );
			// simple check for infinite loops
			mediate( "i am baking a cake because i need 3 eggs",
					   "ok, you're baking a cake because you need 3 eggs" );
			mediate( "why am i baking a cake",  "because you need 3 eggs" );
			
			audit.subtl( "Distinguishing negative responses" );
			// I do understand, "sophie needs dr martens", but
			// I don't understand, "sophie is very fashionable"
			mediate( "sophie needs dr martens because sophie is very fashionable",
                       "I don't understand" );
			mediate( "sophie is very fashionable because sophie needs dr martens",
                       "I don't understand" );
			mediate( "do i need 250 grams of flour because i am baking a cake",
                       "Sorry, it is not the case that you need 250 grams of flour" );
			mediate( "why am i heating the oven",
					   "Sorry, it is not the case that you're heating the oven" );
			
			audit.subtl( "Transitivity" );
			mediate( "i need to go to the shops because i need 3 eggs",
					   "ok, you need to go to the shops because you need 3 eggs" );
			mediate( "is i need 3 eggs the cause of i need to go to the shops",
					   "yes, you need to go to the shops because you need 3 eggs" );
			mediate( "is i am baking a cake the cause of i need to go to the shops",
					   "yes, you need to go to the shops because you're baking a cake" );
			// this test steps over one reason...
			mediate( "do i need to go to the shops because i am baking a cake",
					   "yes, you need to go to the shops because you're baking a cake" );
			
			audit.subtl( "Why might.../abduction" );
			mediate( "i am not baking a cake",  "ok, you're not baking a cake" );
			mediate( "am i baking a cake",      "no, you're not baking a cake" );
			mediate( "i do not need any eggs",  "ok, you don't need any eggs" );
			mediate( "why do i need 3 eggs",    "sorry, it is not the case that you need 3 eggs" );
			mediate( "why might i need 3 eggs", "because you're baking a cake" );
		}
		if (runThisTest( level, ++testNo )) { // need+needs test			
			audit.title( "Group-as-entity");
			clearTheNeedsList( "MartinAndRuth does not need anything" );
			
			mediate( "martin and ruth need a coffee and a tea",
			         "ok, martin and ruth need a coffee and a tea" );
			
			mediate( "what do martin and ruth need",
			         "martin and ruth need a coffee , and a tea" );
			
			mediate( "martin and ruth do not need a tea", 
			         "ok, martin and ruth don't need a tea" );
			
			mediate( "what do martin and ruth need",
			         "martin and ruth need a coffee" );
			
			mediate( "martin and ruth need some biscuits",
			         "ok, martin and ruth need some biscuits" );
			
			mediate( "what do martin and ruth need",
			         "martin and ruth need a coffee, and some biscuits" );
			// Tidy up
			mediate( "martin and ruth do not need anything", "ok , martin and ruth don't need anything" );

			audit.title( "Combos, multiple singular entities");
			mediate( "james and martin and ruth all need a chocolate biscuit",
			         "ok, james and martin and ruth all need a chocolate biscuit" );
			
			mediate( "martin and ruth both need a cocoa and a chocolate biscuit",
			         "ok, martin and ruth both need a cocoa and a chocolate biscuit" );
			
			mediate( "what does martin need",
					 "martin needs a chocolate biscuit, and a cocoa" );
			clearTheNeedsList( "james  doesn't need anything" );
			clearTheNeedsList( "martin doesn't need anything" );
			clearTheNeedsList( "ruth   doesn't need anything" );
		}
		if (runThisTest( level, ++testNo )) { // 
			audit.title( "Pronouns - see need+needs.txt" );
			clearTheNeedsList();
			
			mediate( "i need biscuits and coffee", "ok, you need biscuits and coffee" );
			mediate( "they are from Sainsbury's",  "ok, they are from sainsbury's" );
			mediate( "i need a pint of milk",      "ok, you need a pint of milk" );
			mediate( "it is from the dairy aisle", "ok, it is from the dairy aisle" );
			mediate( "i need cheese and eggs from the dairy aisle",
					                                 "ok, you need cheese and eggs" );
			mediate( "group by",                   "sorry, i need to know what to group by" );
			mediate( "group by location",          "ok" );
			
			mediate( "what do i need from sainsbury's",
					   "you need biscuits, and coffee from sainsbury's" );
			
			mediate( "what do i need from the dairy aisle",
					   "you need a pint of milk, cheese, and eggs from the dairy aisle" );
			
			mediate( "i don't need anything from the dairy aisle",
					   "ok, you don't need anything from the dairy aisle" );
		}
		if (runThisTest( level, ++testNo )) { // 
			mediate( "what do i need",             "you need biscuits, and coffee from sainsbury's" );
			mediate( "i need an apple" );
			mediate( "how many apples do i need",  "1, you need 1 apples" ); // <<<<<<<<< see this!
		}
		if (runThisTest( level, ++testNo )) { // 
			audit.title( "The Non-Computable concept of NEED" );
			clearTheNeedsList();
			
			mediate( "what do i need",	           "you don't need anything" );
			mediate( "i need 2 cups of coffee and a biscuit",
					                               "ok, you need 2 cups of coffee and a biscuit");
			mediate( "what do i need",             "you need 2 cups of coffee, and a biscuit");
			mediate( "how many coffees do i need", "2, you need 2 coffees" );
			mediate( "i need 2 coffees",           "i know" );
			mediate( "i don't need any coffee",    "ok, you don't need any coffee" );
			mediate( "what do i need",             "you need a biscuit" );

			audit.title( "Semantic Thrust" );
			mediate( "i need to go to town",       "ok, you need to go to town" );
			mediate( "what do i need",             "you need a biscuit, and to go to town" );
			mediate( "i have the biscuit",         "ok, you don't need any biscuit" );
			mediate( "i have to go to town",       "I know" );
			mediate( "i don't need to go to town", "ok, you don't need to go to town" );
			mediate( "what do i need",             "you don't need anything" );
			
			audit.title( "Numerical Context" );
			clearTheNeedsList();
			mediate( "i need a coffee",     "ok, you need a coffee" );
			mediate( "and another",         "ok, you need another coffee" );
			mediate( "how many coffees do i need", "2, you need 2 coffees" );
			mediate( "i need a cup of tea", "ok, you need a cup of tea" );
			mediate( "and another coffee",  "ok, you need another coffee" );
			mediate( "what do i need",      "You need 3 coffees , and a cup of tea" );
			
			audit.title( "Correction" );
			mediate( "i need another coffee", "ok, you need another coffee" );
			mediate( "no i need another 3",   "ok, you need another 3 coffees" );
			mediate( "what do i need",        "you need 6 coffees, and a cup of tea" );
			mediate( "prime the answer yes",  "ok, the next answer will be yes" );
			mediate( "i don't need anything", "ok, you don't need anything" );
			
			audit.title( "Late Binding Floating Qualifiers" );
			clearTheNeedsList();
			mediate( "i need biscuits",       "ok, you need biscuits" );
			mediate( "i need milk from the dairy aisle", "ok, you need milk from the dairy aisle" );
			mediate( "what do i need",        "you need biscuits; and, milk from the dairy aisle" );
			mediate( "from the dairy aisle what do i need",  "you need milk from the dairy aisle" );
			mediate( "what from the dairy aisle do i need",  "you need milk from the dairy aisle" );
			mediate( "what do i need from the dairy aisle",  "you need milk from the dairy aisle" );
		}
		if (runThisTest( level, ++testNo )) { // variables, arithmetic and lambda tests
			audit.title( "james's experimental example" );
			//interpret( "england is a country",  "ok, england is a country" );
			mediate( "preston is in england", "ok, preston is in england" );
			mediate( "i am in preston",       "ok, you're in england" );
		}
		if (runThisTest( level, ++testNo )) { // 
			audit.title( "Simple Variables" );
			mediate( "the value of name is fred",       "ok, name is set to fred" );
			mediate( "get the value of name",           "fred" );
			mediate( "set the value of name to fred bloggs", "ok, name is set to fred bloggs" );
			mediate( "what is the value of name",       "fred bloggs, the value of name is fred bloggs" );
			
			audit.subtl( "Simple Numerics" );
			mediate( "set the weight of martin to 104", "ok" );
			mediate( "get the weight of martin",        "Ok, the weight of martin is 104" );
			
			// non-numerical values
			audit.title( "Simply ent/attr model" );
			mediate( "the height of martin is 194",  "Ok,  the height of martin is 194" );
			mediate( "what is the height of martin", "194, the height of martin is 194" );

			audit.title( "Apostrophe's ;-)" );
			mediate( "what is martin's height", "194, martin's height is 194" );
			mediate( "martin's height is 195",  "Ok,  martin's height is 195" );
			mediate( "what is the height of martin", "195, the height of martin is 195" );
		}
		if (runThisTest( level, ++testNo )) {
			// TODO: WSC - alternative states tests
			// mut ex: dead is the opposite of alive, no?
			//         dead and alive are mutually exclusive
			// mut ex: i am martin            - ok
			//         i am martin wheatman   - ok
			//         i am martin            - I know
			//         i am harvey wallbanger - no you're martin
			//         i've changed my name to harvey wallbanger - ok
			// fat and thin and athletic are mutually exclusive.
			//         I am fat.     Am I thin. No
			//         I am not fat. Am i thin. I don't know
			audit.title( "WSC - advocacy and fear");
			audit.subtl( "contradiction test... can't swap between states directly");
			mediate( "demonstrators fear violence",        "ok, demonstrators fear violence" );
			mediate( "demonstrators advocate violence",    "no, demonstrators fear violence" );
			mediate( "demonstrators do not fear violence", "ok, demonstrators don't fear violence" );
			mediate( "demonstrators advocate violence",    "ok, demonstrators advocate violence" );
			mediate( "demonstrators fear violence",        "no, demonstrators advocate violence" );
			mediate( "demonstrators don't advocate violence", "ok, demonstrators don't advocate violence" );
			// tidy up
			mediate( "delete violence advocate list", "ok" );
			mediate( "delete violence fear     list", "ok" );
			mediate( "unset the value of they" );
			
			
			audit.subtl( "Common sense: opposing views" );
			mediate( "the councillors   fear     violence", "ok, the councillors       fear violence" );
			mediate( "the demonstrators advocate violence", "ok, the demonstrators advocate violence" );
			// test 1
			mediate( "the councillors refused the demonstrators a permit because they fear violence",
					 "ok, the councillors refused the demonstrators a permit because they fear violence" );
			mediate( "who are they", "they are the councillors" );
			// test 2
			mediate( "the councillors refused the demonstrators a permit because they advocate violence",
					 "ok, the councillors refused the demonstrators a permit because they advocate violence" );
			mediate( "who are they", "they are the demonstrators" );
			// tidy up
			mediate( "delete violence advocate list", "ok" );
			mediate( "delete violence fear     list", "ok" );
			mediate( "unset the value of they" );

			
			audit.subtl( "Common sense: aligned views - advocate" );
			mediate( "the councillors   advocate violence", "ok, the councillors   advocate violence" );
			mediate( "the demonstrators advocate violence", "ok, the demonstrators advocate violence" );
			// test  1
			mediate( "the councillors refused the demonstrators a permit because they fear violence",
					 "I don't think they fear violence" );
			mediate( "who are they", "I don't know" );
			// test 2
			mediate( "the councillors refused the demonstrators a permit because they advocate violence",
					 "ok, the councillors refused the demonstrators a permit because they advocate violence" );
			mediate( "who are they", "they are the councillors , and the demonstrators" );
			// tidy up
			mediate( "delete violence advocate list", "ok" );
			mediate( "delete violence fear     list", "ok" );
			mediate( "unset the value of they" );
			
			
			audit.subtl( "Common sense: aligned views - fear" );
			mediate( "the councillors fear violence because the voters fear violence",
					"ok, the councillors fear violence because the voters fear violence" );
			mediate( "the demonstrators fear violence", "ok, the demonstrators fear violence" );
			// test 1
			mediate( "the councillors refused the demonstrators a permit because they fear violence",
					 "ok, the councillors refused the demonstrators a permit because they fear violence" );
			mediate( "who are they", "they are the voters, the councillors , and the demonstrators" );
			// test 2
			mediate( "the councillors refused the demonstrators a permit because they advocate violence",
					 "I don't think they advocate violence" );
			mediate( "who are they", "i don't know" );
			// tidy up
			mediate( "delete violence advocate list", "ok" );
			mediate( "delete violence fear     list", "ok" );
			mediate( "unset the value of they" );
		}
		if (runThisTest( level, ++testNo )) {
			audit.title( "Annotation" ); // TODO: camelise attribute names
			mediate( "delete martin was       list", "ok" );
			mediate( "delete martin wasNot    list", "ok" );
			mediate( "delete i      am        list", "ok" );
			mediate( "delete i      amNot     list", "ok" );
			mediate( "delete martin is        list", "ok" );
			mediate( "delete martin isNot     list", "ok" );
			mediate( "delete i      willBe    list", "ok" );
			mediate( "delete i      willNotBe list", "ok" );
			mediate( "delete martin willBe    list", "ok" );
			mediate( "delete martin willNotBe list", "ok" );
			
			/*
			 * Test 5.1 - IS
			 */
			// e.g. i am alive - 5.1
			mediate( "interpret i am variable state thus",         "go on" );
			mediate( "first add    variable state to   i am list", "go on" );
			mediate( "then  remove variable state from i amNot list", "go on" );
			mediate( "then whatever reply ok",                     "ok" );
			
			// e.g. i am not alive - 5.1
			mediate( "interpret i am not variable state thus",        "go on" );
			mediate( "first add    variable state to   i amNot list", "go on" );
			mediate( "then  remove variable state from i am    list", "go on" );
			mediate( "then whatever reply ok",                        "ok" );
			
			// e.g. am i alive? - 5.1
			mediate( "interpret am i variable state thus",                "go on" );
			mediate( "first variable state exists in i am list",          "go on" );
			mediate( "then reply yes i am variable state",                "go on" );
			mediate( "then if not variable state exists in i amNot list", "go on" );
			mediate( "then if not reply i do not know",                   "go on" );
			mediate( "then reply no i am not variable state",             "go on" );
			mediate( "ok", "ok" );
			
			//  e.g. martin is alive - 5.1
			mediate( "interpret variable entity is variable state thus",            "go on" );
			mediate( "first add    variable state to   variable entity is    list", "go on" );
			mediate( "then  remove variable state from variable entity isNot list", "go on" );
			mediate( "then whatever reply ok",                                      "ok" );
			
			// e.g. martin is not alive - 5.1
			mediate( "interpret variable entity is not variable state thus",       "go on" );
			mediate( "first add   variable state to   variable entity isNot list", "go on" );
			mediate( "then remove variable state from variable entity is    list", "go on" );
			mediate( "then whatever reply ok",                                     "ok" );
			
			// e.g. is martin alive - 5.1
			mediate( "interpret is variable entity variable state thus",        "go on" );
			mediate( "first variable state  exists in variable entity is list", "go on" );
			mediate( "then reply yes variable entity is variable state",        "go on" );
			mediate( "then if not variable state exists in variable entity isNot list", "go on" );
			mediate( "then reply no variable entity is not variable state",     "go on" );
			mediate( "then if not reply i do not know",                         "go on" );
			mediate( "ok", "ok" );

			// e.g. is martin not alive - 5.1
			mediate( "interpret is variable entity not variable state thus",       "go on" );
			mediate( "first variable state  exists in variable entity isNot list", "go on" );
			mediate( "then reply yes variable entity is not variable state",        "go on" );
			mediate( "then if not variable state exists in variable entity is list", "go on" );
			mediate( "then reply no variable entity is variable state",             "go on" );
			mediate( "then if not reply i do not know",                            "go on" );
			mediate( "ok", "ok" );

			// test 5.1
			mediate( "am i alive",     "i don't know" );
			mediate( "i am alive",     "ok" );
			mediate( "am i alive",     "yes i'm alive" );
			mediate( "i am not alive", "ok" );
			mediate( "am i alive",     "no i'm not alive" );
			
			// test 5.1
			mediate( "is martin alive", "i don't know" );
			mediate( "martin is alive", "ok" );
			mediate( "is martin alive", "yes martin is alive" );
			mediate( "martin is not alive", "ok" );
			mediate( "is martin alive",     "no martin is not alive" );
			mediate( "is martin not alive", "yes martin is not alive" );
			
			/*
			 *  Test 5.2 was/was not
			 */
			//  e.g. martin was alive - 5.2
			mediate( "interpret variable entity was variable state thus",            "go on" );
			mediate( "first add    variable state to   variable entity was    list", "go on" );
			mediate( "then  remove variable state from variable entity wasNot list", "go on" );
			mediate( "then whatever reply ok",                                       "ok" );
			
			// e.g. martin was not alive - 5.2
			mediate( "interpret variable entity was not variable state thus",       "go on" );
			mediate( "first add   variable state to   variable entity wasNot list", "go on" );
			mediate( "then remove variable state from variable entity was    list", "go on" );
			mediate( "then whatever reply ok",                                      "ok" );
			
			// e.g. was martin alive - 5.2
			mediate( "interpret was variable entity variable state thus",        "go on" );
			mediate( "first variable state  exists in variable entity was list", "go on" );
			mediate( "then reply yes variable entity was variable state",        "go on" );
			mediate( "then if not variable state exists in variable entity wasNot list", "go on" );
			mediate( "then reply no variable entity was not variable state",     "go on" );
			mediate( "then if not reply i do not know",                          "go on" );
			mediate( "ok", "ok" );

			// e.g. was martin not alive - 5.2
			mediate( "interpret was variable entity not variable state thus",       "go on" );
			mediate( "first variable state  exists in variable entity wasNot list", "go on" );
			mediate( "then reply yes variable entity was not variable state",       "go on" );
			mediate( "then if not variable state exists in variable entity was list", "go on" );
			mediate( "then reply no variable entity was variable state",            "go on" );
			mediate( "then if not reply i do not know",                             "go on" );
			mediate( "ok", "ok" );

			// test 5.2
			mediate( "was martin alive",     "i don't know" );
			mediate( "martin was alive",     "ok" );
			mediate( "was martin alive",     "yes martin was alive" );
			mediate( "martin was not alive", "ok" );
			mediate( "was martin alive",     "no martin was not alive" );
			mediate( "was martin not alive", "yes martin was not alive" );
			
			/*
			 *  Test 5.3 will be/will not be
			 */
			//  e.g. martin will be alive - 5.3
			mediate( "interpret variable entity will be variable state thus",           "go on" );
			mediate( "first add    variable state to   variable entity willBe    list", "go on" );
			mediate( "then  remove variable state from variable entity willNotBe list", "go on" );
			mediate( "then whatever reply ok",                                          "ok" );
			
			// e.g. martin will not be alive - 5.3
			mediate( "interpret variable entity will not be variable state thus",      "go on" );
			mediate( "first add   variable state to   variable entity willNotBe list", "go on" );
			mediate( "then remove variable state from variable entity willBe    list", "go on" );
			mediate( "then whatever reply ok",                                         "ok" );
			
			// e.g. will martin be alive - 5.3
			mediate( "interpret will variable entity be variable state thus",      "go on" );
			mediate( "first variable state exists in variable entity willBe list", "go on" );
			mediate( "then reply yes variable entity will be variable state",      "go on" );
			mediate( "then if not variable state exists in variable entity willNotBe list", "go on" );
			mediate( "then reply no variable entity will not be variable state",   "go on" );
			mediate( "then if not reply i do not know",                            "go on" );
			mediate( "ok", "ok" );

			// e.g. will martin not be alive - 5.3
			mediate( "interpret will variable entity not be variable state thus",      "go on" );
			mediate( "first variable state  exists in variable entity willNotBe list", "go on" );
			mediate( "then reply yes variable entity will not be variable state",      "go on" );
			mediate( "then if not variable state exists in variable entity willBe list", "go on" );
			mediate( "then reply no variable entity will be variable state",           "go on" );
			mediate( "then if not reply i do not know",                                "go on" );
			mediate( "ok", "ok" );

			// test 5.3
			mediate( "will i be alive",     "i don't know" );
			mediate( "i will be alive",     "ok" );
			mediate( "will i be alive",     "yes you'll be alive" );
			mediate( "i will not be alive", "ok" );
			mediate( "will i be alive",     "no you'll not be alive" );
			mediate( "will i not be alive", "yes you'll not be alive" );

			mediate( "will martin be alive",     "i don't know" );
			mediate( "martin will be alive",     "ok" );
			mediate( "will martin be alive",     "yes martin will be alive" );
			mediate( "martin will not be alive", "ok" );
			mediate( "will martin be alive",     "no martin will not be alive" );
			mediate( "will martin not be alive", "yes martin will not be alive" );

			// Test
			// Event: to move is to was (traverse time quanta)
			// interpret( "interpret when i am dead then move what i am to what i was thus", "go on" );
		}
		if (runThisTest( level, ++testNo )) {
			audit.title( "Verbal Arithmetic" );
			mediate( "what is 1 + 2",                    "1 plus 2 is 3" );
			mediate( "times 2 all squared",              "times 2 all squared makes 36" );
			mediate( "what is 36 + 4     divided by 2",  "36 plus 4     divided by 2 is 38" );
			mediate( "what is 36 + 4 all divided by 2",  "36 plus 4 all divided by 2 is 20" );
			
			audit.title( "Simple Functions" );
			mediate( "the sum of x and y is x plus y",  "ok, the sum of x and y is x plus y" );
			mediate( "what is the sum of 3 and 2",      "the sum of 3 and 2 is 5 " );
			mediate( "set x to 3",                      "ok, x is set to 3" );
			mediate( "set y to 4",                      "ok, y is set to 4" );
			mediate( "what is the value of x",          "3, the value of x is 3" );
			mediate( "what is the sum of x and y",      "the sum of x and y is 7" );
			
			audit.title( "Factorial Description" );
			mediate( "what is the factorial of 4",       "I don't know" );
			/* Ideally, we want:
			 * - the factorial of 1 is 1;
			 * - the factorial of n is n times the factorial of n - 1;
			 * - what is the factorial of 3.
			 */
			mediate( "the factorial of 1 is 1",          "ok, the factorial of 1 is 1" );
			
			// in longhand this is...
			mediate( "to the phrase what is the factorial of 0 reply 1", "ok" );
			mediate( "what is the factorial of 0",  "1" );
			
			mediate( "interpret multiply numeric variable a by numeric variable b thus", "go on" );
			mediate( "first perform numeric evaluate variable a times variable b",       "go on" );
			mediate( "ok", "ok" );
			
			mediate( "the product of x and y is x times y" );
			mediate( "what is the product of 3 and 4",  "the product of 3 and 4 is 12" );
			//TODO:
			//interpret( "what is the product of x and y",  "the product of x and y is x times y" );
			mediate( "the square of x is x times x",    "Ok, the square of x is x times x" );
			mediate( "what is 2 times the square of 2", "2 times the square of 2 is 8" );
			
			// again, in longhand this is...
			mediate( "interpret subtract numeric variable c from numeric variable d thus", "go on" );
			mediate( "first perform numeric evaluate variable d minus variable c",         "go on" );
			mediate( "ok", "ok" );
			
			mediate( "subtract 2 from 3", "1" );
			
			// interpret( "the factorial of n is n times the factorial of n - 1", "ok" );
			// interpret( "what is the factorial of n",   "n is n times the factorial of n minus 1" );
			mediate( "interpret what is the factorial of numeric variable n thus",  "go on" );
			mediate( "first subtract 1 from variable n",                            "go on" );
			mediate( "then what is the factorial of whatever",                      "go on" );
			mediate( "then multiply whatever by variable n",  "go on" );
			mediate( "then reply whatever the factorial of variable n is whatever", "go on" );
			mediate( "ok", "ok" );
			
			mediate( "what is the factorial of 4", "24 the factorial of 4 is 24" );
		}
		if (runThisTest( level, ++testNo )) {
			audit.title( "Temporal interpret" );
			mediate( "what day is christmas day" );
			//testInterpret( "what day is it today" );
			// my date of birth is
			// how old am i.
			// age is the given date minus the date of inception
			// if no date given, use the current date.
			// persons age given in years
			// what is my age [in <epoch default="years"/>]
		}
		if (runThisTest( level, ++testNo )) { // 
			audit.subtl( "Temporospatial concept MEETING" );
			
			/* TODO: interpret think of a variable entity thus.  // see sofa for particular details!
			 * first create a class variable entity.             // mkdir pub; touch pub/isa 
			 * then  create an anonymous entity variable entity. // mkdir pub/a
			 * then  set the context of the variable entity to a variable entity // ln -s pub/the pub/a
			 * ok.
			 */
			mediate( "I'm not meeting anybody",
					   "Ok , you're not meeting anybody" );
			mediate( "At 7 I'm meeting my brother at the pub",
					   "Ok , you're meeting your brother at 7 at the pub" );
			mediate( "When  am I meeting my brother",
					   "You're meeting your brother at 7" );
			mediate( "Where am I meeting my brother",
					   "You're meeting your brother at the pub" );
			mediate( "Am I meeting my brother",
					   "Yes , you're meeting your brother" );
			
			mediate( "I'm meeting my sister at the pub" );
			mediate( "When am I meeting my sister",
					   "I don't know when you're meeting your sister" );
			
			mediate( "When am I meeting my dad",
					   "i don't know if you're meeting your dad" );
			mediate( "Where am I meeting my dad" ,
					   "i don't know if you're meeting your dad" );
			
		}
		if (runThisTest( level, ++testNo )) { // Language features
			
			audit.title( "Generic Pronouns" );
			clearTheNeedsList( "martin doesn't need anything" );
			mediate( "martin needs a coffee", "ok, martin needs a coffee" );
			mediate( "what does he need",     "martin needs a coffee" );
			clearTheNeedsList( "martin doesn't need anything" );
			
			mediate( "ruth needs a tea",      "ok, ruth needs a tea" );
			mediate( "what does she need",    "ruth needs a tea" );
			clearTheNeedsList( "ruth   doesn't need anything" );
			
			mediate( "laurel and hardy need a coffee and a tea",
			         "ok, laurel and hardy need a coffee and a tea" );
			
			mediate( "what do they need",     "laurel and hardy need a coffee , and a tea" );
			clearTheNeedsList( "MartinAndRuth does not need anything" );
		}
		if (runThisTest( level, ++testNo )) { // 
			/* TODO:
			 *  create a queen called elizabeth the first  (eliz = woman's name, a queen is a monarch => person)
			 *  she died in 1603
			 *  she reigned for 45 years (so she ascended/came to the throne in 1548!)
			 */
			mediate( "a queen is a monarch", "ok, a queen is a monarch" );
		}
		if (runThisTest( level, ++testNo )) { // 
			audit.subtl( "Disambiguation" );
			mediate( "the eagle has landed"    /* "Are you an ornithologist" */);
			mediate( "no the eagle has landed" /* "So , you're talking about the novel" */ );
			mediate( "no the eagle has landed" /*"So you're talking about Apollo 11" */	);
			mediate( "no the eagle has landed" /* "I don't understand" */ );
			// Issue here: on DNU, we need to advance this on "the eagle has landed"
			// i.e. w/o "no ..."
		}
		if (runThisTest( level, ++testNo )) { // 
			audit.subtl( "TCP/IP test" );
			// bug here??? config.xml has to be 8080 (matching this) so does  // <<<< see this!
			// config port get chosen over this one???
			mediate( "tcpip localhost "+ Net.TestPort +" \"a test port address\"", "ok" );
			mediate( "tcpip localhost 5678 \"this is a test, which will fail\"",  "Sorry" );
			mediate( "simon says put your hands on your head" ); //, "ok, success" );
		}
		if (runThisTest( level, ++testNo )) { // code generation features
			
			audit.title( "Polymorphism - setup new idea and save" );
			mediate( "want is like need",   "ok, want is like need" );
			Synonyms.interpret( new Strings( "save" ));
			
			audit.subtl( "unset synonyms" );
			mediate( "want is unlike need",   "ok, want is unlike need" );
			
			audit.title( "Recall values and use" );
			Synonyms.interpret( new Strings( "recall" ));
			mediate( "what do i want",      "you don't want anything" );
			mediate( "i want another pony", "ok, you want another pony" );
			mediate( "what do i want",      "you want another pony" );
			clearTheNeedsList( "i don't want anything" );
		}
		if (runThisTest( level, ++testNo )) { // 
			audit.title( "On-the-fly Langauge Learning" );
			/* TODO: create filename from pattern:
			 *    "i need phrase variable objects" => i_need-.txt (append? create overlay)
			 *    "this is part of the need concept" => need.txt (append)
			 *    Enguage.interpret() => overlay
			 *    Conceept.load() => can this outlive Enguage overlay???
			 */

			// First, what we can't say yet...
			mediate( "my name is martin",                 "I don't understand" );
			mediate( "if not  reply i already know this", "I don't understand" );
			mediate( "unset the value of name",           "ok" );

			// build-a-program...
			mediate( "interpret my name is phrase variable name thus", "go on" );
			mediate( "first set name to variable name",                "go on" );
			mediate( "then get the value of name",                     "go on" ); // not strictly necessary!
			mediate( "then reply hello whatever",                      "go on" );
			mediate( "ok",                                             "ok"    );

			mediate( "my name is ruth",   "hello   ruth" );
			mediate( "my name is martin", "hello martin" );


			//...or to put it another way
			mediate( "to the phrase i am called phrase variable name reply hi whatever", "ok" );
			mediate( "this implies name gets set to variable name",   "go on" );
			mediate( "this implies name is not set to variable name", "go on" );
			mediate( "if not reply i already know this",              "go on" );
			mediate( "ok", "ok" );

			mediate( "i am called martin", "i already know this" );

			// ...means.../...the means to...
			// 1. from the-means-to repertoire
			mediate( "to the phrase phrase variable x the means to phrase variable y reply i really do not understand", "ok" );
			mediate( "ok", "ok" );

			mediate( "do we have the means to become rich", "I really don't understand" );

			// 2. could this be built thus?
			mediate( "to phrase variable this means phrase variable that reply ok", "ok" );
			mediate( "this implies ok set induction to false",                      "go on" );
			mediate( "this implies perform sign think variable that",               "go on" );
			mediate( "this implies perform sign create variable this",              "go on" );
			mediate( "this implies ok set induction to true",                       "go on" );
			mediate( "ok", "ok" );

			mediate( "just call me phrase variable name means i am called variable name", "ok" );
			mediate( "just call me martin", "i already know this" );
		}
		if (runThisTest( level, ++testNo )) {
			audit.title( "Light bins" );
			mediate( "there are 6 light bins",        "ok, there are 6 light bins" );
			mediate( "how many light bins are there", "6,  there are 6 light bins" );
			mediate( "show me light bin 6",           "ok, light bin 6 is flashing", "sorry" );
		}
//		if (runThisTest( level, ++testNo )) { // 
//			audit.title( "Ask: Confirmation" );
//
//			//interpret( "prime the answer yes", "ok, the next answer will be yes" );
//			interpret( "i have everything", "ok , you don't need anything" );
//
//			//interpret( "prime the answer no", "ok, the next answer will be no" );
//			//interpret( "i have everything", "ok , let us leave things as they are" );
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
//		}
		Audit.log( testNo +" test group(s) found" );
		audit.PASSED();
}	}
