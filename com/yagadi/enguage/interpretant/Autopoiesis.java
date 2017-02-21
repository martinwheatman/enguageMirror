package com.yagadi.enguage.interpretant;

import java.util.Iterator;

import com.yagadi.enguage.object.Attribute;
import com.yagadi.enguage.object.Attributes;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Strings;
import com.yagadi.enguage.vehicle.Context;
import com.yagadi.enguage.vehicle.Reply;

public class Autopoiesis extends Intention {
	private static       Audit   audit = new Audit( "Autopoiesis" );

	public static final String NAME    = "autopoiesis";
	
	public static final String UNDEF   = "und";
	public static final String NEW     = "add";
	public static final String APPEND  = "app";
	public static final String PREPEND = "prep";
	public static final String CREATE  = "cre";
	public static final String ADD     = "add";
	
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
	public Autopoiesis( String name, String intnt, String value ) { super( name, value ); intent = intnt;}	
	
	private String intent = UNDEF;
	public  String intent() { return intent;}
	
	static private Sign s = null;
	
	static private String concept = "";
	static public    void concept( String name ) { concept = name; }
	static public  String concept() { return concept; }
	
	public Reply mediate( Reply r ) {
		audit.in( "mediate", "NAME="+ NAME +", value="+ value +", "+ Context.valueOf());
		Strings sa = Context.deref( new Strings( value ));
		if (intent.equals( CREATE )) {
			audit.log( "creating new sign: ["+ value +"]");
			Repertoire.signs.insert(
				s = new Sign()
					.content( new Tags( value ))
					.concept( concept() )
			);
		} else if (!intent.equals( UNDEF )) {
			if (null != s) {
				audit.debug( "Adding to EXISTING sign: '"+ value +"'");
				s.append( intent, value );
			}
			
		} else
		
		if (3 != sa.size())
			audit.ERROR( name +": wrong number ("+ sa.size() +") of params ["+ sa.toString( Strings.CSV ) +"]");
		else {
			String attr = sa.get( 0 ),
			       pattern = sa.get( 1 ),
			       val = Strings.trim( sa.get( 2 ), '"' );
			if (name.equals( APPEND ) || name.equals( PREPEND )) {
				if (null == s)
					// this should return DNU...
					audit.ERROR( "adding to non existent concept: ["+ sa.toString( Strings.CSV )+"]");
				else {
					audit.debug( name +"ending  to EXISTING rule: ["+ sa.toString( Strings.CSV )+"]");
					if (name.equals( APPEND ))
						s.append(  attr, val );
					else
						s.prepend( attr, val );
				}
			} else if (name.equals( NEW )) { // autopoeisis?
				/* TODO: need to differentiate between
				 * "X is X" and "X is Y" -- same shape, different usage.
				 * At least need to avoid this (spot when "X is X" happens)
				 */
				audit.debug( "Adding "+ name +": ["+ sa.toString( Strings.CSV )+"]");
				if ( sa.get( 1 ).equals( "help" ))
					s.help( val ); // add: help="text" to cached sign
				else // create then add a new cached sign into the list of signs
					Repertoire.signs.insert(
						s = new Sign()
							.content( new Tags( Strings.trim( pattern, '"' )) )
							.concept( concept() )
							.attribute( attr, val ));
		}	}
		return (Reply) audit.out( r.answer( Reply.yes().toString() ));
	}
	public static void test(Attributes a) {
		Reply r = new Reply();
		Iterator<Attribute> ai = a.iterator();
		while (!r.isDone() && ai.hasNext()) {
			Attribute an = ai.next();
			String  name = an.name(),
			       value = an.value();
			audit.log( name +"='"+ value +"'" );
			r = new Autopoiesis( name, value ).mediate( r );
		}
		audit.log( Repertoire.signs.toString() );
		audit.log( r.toString());
	}
	public static void main( String args[]) {
		//Audit.allOn();
		//audit.trace( true );
		
		Attributes a = new Attributes();
		
		a.add( new Attribute( Autopoiesis.NEW,    THINK +" \"a PATTERN z\" \"one two three four\""   ));
		a.add( new Attribute( Autopoiesis.APPEND, DO    +" \"a PATTERN z\" \"two three four\""   ));
		a.add( new Attribute( Autopoiesis.APPEND, REPLY +" \"a PATTERN z\" \"three four\"" ));
		test( a );
		
		Reply r = new Reply();
		r = new Autopoiesis( CREATE, CREATE, "a PATTERN z"        ).mediate( r );
		r = new Autopoiesis( ADD,    THINK,  "one two three four" ).mediate( r );
		r = new Autopoiesis( ADD,    DO,     "two three four"     ).mediate( r );
		r = new Autopoiesis( ADD,    REPLY,  "three four"         ).mediate( r );

		audit.log( Repertoire.signs.toString() );
		audit.log( r.toString());
}	}
