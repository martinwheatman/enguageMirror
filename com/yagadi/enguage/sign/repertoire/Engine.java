package com.yagadi.enguage.sign.repertoire;

import com.yagadi.enguage.sign.Sign;
import com.yagadi.enguage.sign.intention.Intention;
import com.yagadi.enguage.sign.intention.Redo;
import com.yagadi.enguage.sign.pattern.Patternette;

public class Engine {
	
	public  static final String NAME = Repertoire.ALLOP;

	public static final Sign commands[] = {
			/* These could be accompanied in a repertoire, but they have special 
			 * interpretations and so are built here alongside those interpretations.
			 */
			new Sign()
				.pattern( new Patternette( "remove the primed answer ", "" ))
	          		.appendIntention( Intention.allop, "removePrimedAnswer" )
	          		.concept( NAME ),
		          	
	    	new Sign()
				.pattern( new Patternette( "prime the answer ", "answer" ).phrasedIs())
		          	.appendIntention( Intention.allop, "primeAnswer ANSWER" )
	          		.concept( NAME ),
				          	
			new Sign()
				.pattern( new Patternette( "answering", "answers" ).phrasedIs())
				.pattern( new Patternette( "ask", "question" ).phrasedIs())
		          	.appendIntention( Intention.allop, "ask answering ANSWERS , QUESTION" )
	          		.concept( NAME ),
		          	
			new Sign().pattern( new Patternette(  "describe ", "x" ))
					.appendIntention( Intention.allop, "describe X" )
		          	.concept( NAME )
					.help( "where x is a repertoire" ),
					 
			new Sign()
					.pattern( new Patternette( "list repertoires","" ))
					.appendIntention( Intention.allop, "list" )
	          		.concept( NAME )
					.help( "" ),
			new Sign()
					.pattern( new Patternette(           "help", "" ))
					.appendIntention( Intention.allop, "help" )
			  		.concept( NAME ),
			new Sign()
					.pattern( new Patternette(          "hello", "" ))
					.appendIntention( Intention.allop, "hello")
			  		.concept( NAME ),
			new Sign()
					.pattern( new Patternette(        "welcome", "" ))
					.appendIntention( Intention.allop, "welcome")
			  		.concept( NAME ),
			new Sign().pattern( new Patternette( "what can i say", "" ))
					 .appendIntention( Intention.allop, "repertoire"  )
		          	.concept( NAME )
					 .help( "" ),
			new Sign()
					.pattern( new Patternette(   "load ", "NAME" ))
					.appendIntention( Intention.allop,   "load NAME" )
			  		.concept( NAME ),
	/*		new Sign().concept( NAME ).content( new Patternette( "unload ", "NAME" )).attribute( new Intention( Intention.allop, "unload NAME" ),
			new Sign().concept( NAME ).content( new Patternette( "reload ", "NAME" )).attribute( NAME, "reload NAME" ),
	// */	//new Sign().concept( NAME ).attribute( NAME, "save"    ).content( new Patternette( "save", "", "" ) ),
			//new Sign().concept( NAME ).attribute( NAME, "saveas $NAME" ).content( new Patternette("saveas ", "NAME", ".")),
																 		
			new Sign().pattern( new Patternette(     "say again",  "" )).appendIntention( Intention.allop, "repeat"       ),
			new Sign().pattern( new Patternette(        "spell ", "x" )).appendIntention( Intention.allop, "spell X"      ),
			new Sign().pattern( new Patternette(   "enable undo",  "" )).appendIntention( Intention.allop, "undo enable"  ),
			new Sign().pattern( new Patternette(  "disable undo",  "" )).appendIntention( Intention.allop, "undo disable" ),
			new Sign().concept( NAME ).pattern( new Patternette(          "undo",  "" )).appendIntention( Intention.allop, "undo"         ),
			new Sign().concept( NAME ).pattern( new Patternette( "this is false",  "" )).appendIntention( Intention.allop, "undo" ),
			new Sign().concept( NAME ).pattern( new Patternette( "this sentence is false",  "" )).appendIntention( Intention.allop, "undo" ),
			
//			new Sign().concept( NAME ).content( new Patternette("", "x", "is temporal" )).attribute( NAME, "temporal X"   ),
			
			new Sign().concept( NAME ).pattern( new Patternette(         "timing  on",  "" )).appendIntention( Intention.allop, "tracing on" ),
			new Sign().concept( NAME ).pattern( new Patternette(         "timing off",  "" )).appendIntention( Intention.allop, "tracing off" ),
			new Sign().concept( NAME ).pattern( new Patternette(        "tracing  on",  "" )).appendIntention( Intention.allop, "tracing on" ),
			new Sign().concept( NAME ).pattern( new Patternette(        "tracing off",  "" )).appendIntention( Intention.allop, "tracing off" ),
			new Sign().concept( NAME ).pattern( new Patternette(         "detail  on",  "" )).appendIntention( Intention.allop, "detailed on" ),
			new Sign().concept( NAME ).pattern( new Patternette(         "detail off",  "" )).appendIntention( Intention.allop, "detailed off" ),
			new Sign().concept( NAME )
					.pattern( new Patternette( "tcpip ",  "address" ))
					.pattern( new Patternette(      " ",  "port" ))
					.pattern( new Patternette(      " ",  "data" ).quotedIs())
						.appendIntention( Intention.allop, "tcpip ADDRESS PORT DATA" ),
			new Sign().concept( NAME ).pattern( new Patternette(              "show ", "x" ).phrasedIs())
					.appendIntention( Intention.allop, "show X" ),
			new Sign().concept( NAME ).pattern( new Patternette(         "debug ", "x" ).phrasedIs())
					.appendIntention( Intention.allop, "debug X" ),
			/* 
			 * it is possible to arrive at the following construct:   think="reply 'I know'"
			 * e.g. "if X, Y", if the instance is "if already exists, reply 'I know'"
			 * here reply is thought. Should be rewritten:
			 * representamen: "if X, reply Y", then Y is just the quoted string.
			 * However, the following should deal with this situation.
			 */
			new Sign().concept( NAME ).pattern( new Patternette( Intention.REPLY +" ", "x" ).quotedIs())
					.appendIntention( Intention.thenReply, "X" ),
			
			// fix to allow better reading of autopoietic  
			new Sign().concept( NAME ).pattern( new Patternette( "if so, ", "x" ).phrasedIs())
					.appendIntention( Intention.thenThink, "X" ),

			// for vocal description of concepts... autopoiesis!		
			new Sign().concept( NAME ).pattern( new Patternette( "perform ", "args" ).phrasedIs())
					.appendIntention( Intention.thenDo, "ARGS" ),
			/* 
			 * REDO: undo and do again, or disambiguate
			 */
			new Sign().concept( NAME ).pattern( new Patternette( "No ", "x" ).phrasedIs())
						.appendIntention( Intention.allop, "undo" )
						.appendIntention( Intention.elseReply, "undo is not available" )
						/* On thinking the below, if X is the same as what was said before,
						 * need to search for the appropriate sign from where we left off
						 * Dealing with ambiguity: "X", "No, /X/"
						 */
						.appendIntention( Intention.allop,  Redo.DISAMBIGUATE +" X" ) // this will set up how the inner thought, below, works
						.appendIntention( Intention.thenThink,  "X"    )
		 };
}
