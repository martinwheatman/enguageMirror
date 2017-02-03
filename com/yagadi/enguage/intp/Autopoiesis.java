package com.yagadi.enguage.intp;

import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Strings;
import com.yagadi.enguage.veh.Context;
import com.yagadi.enguage.veh.Reply;

public class Autopoiesis extends Intention {
	private static       Audit   audit = new Audit( "Autopoiesis" );

	public static final String NAME    = "autopoiesis";
	
	public static final String NEW     = "add";
	public static final String APPEND  = "app";
	public static final String PREPEND = "prep";
	
	public static final Sign[] autopoiesis = {
		// PATTERN PRE-CHECK cases (4)
		// 1: A implies B.
		new Sign().attribute( PREPEND, Intention.THINK +" A B" )
			.content( new Tag( "", "a"      ).attribute( Tag.quoted, Tag.quoted ))
			.content( new Tag( " implies ", "b", "." ).attribute( Tag.quoted, Tag.quoted )),
		// 2: A implies B, if not, say C.
		new Sign().attribute( PREPEND, Intention.ELSE_REPLY+" A C").attribute( PREPEND, Intention.THINK +" A B")
			.content( new Tag(          "", "a" ).attribute( Tag.quoted, Tag.quoted ))
			.content( new Tag( " implies ", "b" ).attribute( Tag.quoted, Tag.quoted ))
			.content( new Tag( " ; if not, reply ", "c", "." ).attribute( Tag.quoted, Tag.quoted )),
			
		// PATTERN CREATION cases (7).
		// a1: On X, think Y.
		new Sign().attribute( NEW, Intention.THINK +" X Y")
			.content( new Tag(     "On ", "x"      ).attribute( Tag.quoted, Tag.quoted ))
			.content( new Tag( ", ", "y", "" ).attribute( Tag.phrase, Tag.phrase )),
			
		// a2: On X, reply Y.
		new Sign().attribute( NEW, Intention.REPLY +" X Y")
			.content( new Tag(     "On ", "x" ).attribute( Tag.quoted, Tag.quoted ))
			.content( new Tag( ", reply ", "y", "" ).attribute( Tag.quoted, Tag.quoted )),
			
		// a3: On X, perform Y.
		new Sign().attribute( NEW, Intention.DO +" X Y") // <<<< trying this
			.content( new Tag(       "On ", "x" ).attribute( Tag.quoted, Tag.quoted ))
			.content( new Tag( ", perform ", "y", "" ).attribute( Tag.quoted, Tag.quoted )),
			
		// b1: Then on X think Y.
		new Sign().attribute( APPEND, Intention.THINK +" X Y")
			.content( new Tag( "Then on ", "x" ).attribute( Tag.quoted, Tag.quoted ))
			.content( new Tag( ", ", "y", "" ).attribute( Tag.phrase, Tag.phrase )),
			
		// b2: Then on X reply Y.
		new Sign().attribute( APPEND, Intention.REPLY+" X Y")
			.content( new Tag( "Then  on ", "x" ).attribute( Tag.quoted, Tag.quoted ))
			.content( new Tag( ", reply   ", "y", "" ).attribute( Tag.quoted, Tag.quoted )),
			
		// b3: Then on X perform Y.
		new Sign().attribute( APPEND, Intention.DO +" X Y")
			.content( new Tag( "Then  on ", "x" ).attribute( Tag.quoted, Tag.quoted ))
			.content( new Tag( ", perform ", "y", "" ).attribute( Tag.quoted, Tag.quoted )),
		
		// At some point this could be improved to say "On X, perform Y; if not, reply Z." -- ??? think!
		// !b1: Else on X think Y.
		new Sign().attribute( APPEND, Intention.ELSE_THINK +" X Y")
			.content( new Tag( "Then on ", "x"      ).attribute( Tag.quoted, Tag.quoted ))
			.content( new Tag( ", if not, ", "y", "" ).attribute( Tag.phrase, Tag.phrase )),
			
		// !b2: Else on X reply Y.
		new Sign().attribute( APPEND, Intention.ELSE_REPLY +" X Y")
			.content( new Tag(  "Then on ", "x"      ).attribute( Tag.quoted, Tag.quoted ))
			.content( new Tag( ", if not, reply ", "y", "" ).attribute( Tag.quoted, Tag.quoted )),
			
		// !b3: Else on X perform Y.
		new Sign().attribute( APPEND, Intention.ELSE_DO +" X Y" )
			.content( new Tag( "Then  on ", "x"      ).attribute( Tag.quoted, Tag.quoted ))
			.content( new Tag( ", if not, perform ", "y", "" ).attribute( Tag.quoted, Tag.quoted )),
			
/*		Added new signs for the running of applications external to enguage...
 */
		new Sign().attribute( NEW, Intention.RUN +" X Y")
		.content( new Tag( "On ", "x" ).attribute( Tag.quoted, Tag.quoted ))
		.content( new Tag( ", run ", "y", "" ).attribute( Tag.quoted, Tag.quoted )),
		
		new Sign().attribute( APPEND, Intention.RUN +" X Y" )
			.content( new Tag( "Then  on ", "x"      ).attribute( Tag.quoted, Tag.quoted ))
			.content( new Tag( ", run ", "y", "" ).attribute( Tag.quoted, Tag.quoted )),

		new Sign().attribute( APPEND, Intention.ELSE_RUN +" X Y" )
			.content( new Tag( "Then  on ", "x"      ).attribute( Tag.quoted, Tag.quoted ))
			.content( new Tag( ", if not, run ", "y", "" ).attribute( Tag.quoted, Tag.quoted )),

		// c1: Finally on X perform Y. -- dont need think or reply?
		new Sign().attribute( APPEND, Intention.FINALLY+" X Y")
			.content( new Tag( " Finally on ", "x"      ).attribute( Tag.quoted, Tag.quoted ))
			.content( new Tag( ",   perform ", "y", "" ).attribute( Tag.quoted, Tag.quoted ))
	};

	public Autopoiesis( String name, String value ) { super( name, value ); }	
	
	static private Sign s = null;
	
	static private String concept = "";
	static public    void concept( String name ) { concept = name; }
	static public  String concept() { return concept; }
	
	
	// this supports the meta="" autopoeisis rule
	// TODO: needs "concepts //delete "...". -- to remove a tag, to support '"X" is meaningless.'
	// Supports:
	// add  "pattern" "action"
	// app  "pattern" "action"
	// prep "pattern" "action"
	public Reply mediate( Reply r ) {
		if (Audit.startupDebug) audit.in( "mediate", "NAME="+ NAME +", value="+ value +", "+ Context.valueOf());

		// NAME="add", VALUE='"help" "add item to list"'
		Strings sa = Context.deref(
				//Variable.deref(
					new Strings( value )
				//)
			);
		
		// sa.get( 0 )='reply', sa.get( 1 )='"help"', sa.get( 2 )='"aural help is not yet implemented"'
		
		if (3 != sa.size())
			audit.ERROR( name +": wrong number ("+ sa.size() +") of params ["+ sa.toString( Strings.CSV ) +"]");
		else {
			String val = Strings.trim( sa.get( 2 ), '"' );
			if (name.equals( "app" ) || name.equals( "prep" )) {
				if (null == s)
					// this should return DNU...
					audit.ERROR( "adding to non existent concept: ["+ sa.toString( Strings.CSV )+"]");
				else {
					if (name.equals( "app" )) {
						if (Audit.startupDebug) audit.debug( "Appending  to EXISTING rule: ["+ sa.toString( Strings.CSV )+"]");
						s.append(  sa.get( 0 ), val );
					} else {
						if (Audit.startupDebug) audit.debug( "Prepending to EXISTING rule: ["+ sa.toString( Strings.CSV )+"]");
						s.prepend( sa.get( 0 ), val );              // prepend NAME="val"
				}	}
			} else if (name.equals( "add" )) { // autopoeisis?
				/* TODO: need to differentiate between
				 * "X is X" and "X is Y" -- same shape, different usage.
				 * At least need to avoid this (spot when "X is X" happens)
				 */
				if ( sa.get( 1 ).equals( "help" )) {
					if (Audit.startupDebug) audit.debug( "Adding HELP: ["+ sa.toString( Strings.CSV )+"]");
					s.help( val ); // add: help="text" to cached sign
				} else { // create a new cached sign
					if (Audit.startupDebug) audit.debug( "Adding NEW rule: ["+ sa.toString( Strings.CSV )+"]");
					Tags tmp = new Tags( Strings.trim( sa.get( 1 ), '"' ));
					s = new Sign()
							.content( tmp )
							.concept( concept() )
							.attribute( sa.get( 0 ), val );
					// then add it into the list of signs
					Repertoire.signs.insert( s );
		}	}	}
		r.answer( Reply.yes().toString() );
		if (Audit.startupDebug) audit.out( "result="+ r.toString() );
		return r;
}	}
