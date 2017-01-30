package com.yagadi.enguage.sofa;

import com.yagadi.enguage.expression.Colloquial;
import com.yagadi.enguage.expression.Plural;
import com.yagadi.enguage.sofa.tier2.Item;
import com.yagadi.enguage.sofa.tier2.List;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

public class Sofa extends Shell {
	static private Audit audit = new Audit( "Sofa", true );

	public Sofa(){
		super( "Sofa" );
		if (!Overlay.autoAttach())
			audit.ERROR( "Ouch! in sofa" );
	}
	private static final String True  = SUCCESS;
	private static final String False = FAIL;

	public String doCall( Strings a ) {
		//audit.traceIn( "doCall", a.toString( Strings.CSV ));
		if (null != a && a.size() > 1) {
			/* Tags.matchValues() now produces:
			 * 		["a", "b", "c='d'", "e", "f='g'"]
			 * Sofa.interpret() typically deals with:
			 * 		["string", "get", "martin", "name"]
			 * 		["colloquial", "both", "'I have'", "'I've'"]
			 * Need to ensure first 4? name/value pairs are dereferenced
			 * Needs to be done here, as call() will be called independently
			 * May need to be selective on how this is done, depending on sofa 
			 * package class requirements...?
			 */
			for (int i=0; i<5 && i<a.size(); i++)
				a.set( i, Attribute.expandValues( a.get( i ) ).toString( Strings.SPACED ));
			//audit.audit("Sofa.doCall() => a is "+ a.toString());
			//a = a.normalise().contract( "=" );// rejig:"list","get","one two","three four"] => "list","get","one","two","three","four"]
			//audit.debug("Sofa.doCall() => "+ a.toString());
			
			String  type = a.get( 0 );
			return //audit.traceOut(
			    a.size() == 1 && type.equals(         True ) ? True :
				 a.size() == 1 && type.equals(        False ) ? False :
						type.equals(     "entity" ) ?      Entity.interpret( a.copyAfter( 0 ) ) :
						type.equals(       "link" ) ?        Link.interpret( a.copyAfter( 0 ) ) :
						type.equals(   Value.NAME ) ?       Value.interpret( a.copyAfter( 0 ) ) :
						type.equals(    List.NAME ) ?        List.interpret( a.copyAfter( 0 ) ) :
						type.equals( "preferences") ? Preferences.interpret( a.copyAfter( 0 ) ) :
						type.equals( Numeric.NAME ) ?     Numeric.interpret( a.copyAfter( 0 ) ) :
						type.equals( Variable.NAME) ?    Variable.interpret( a.copyAfter( 0 ) ) :
						type.equals(    "overlay" ) ?     Overlay.interpret( a.copyAfter( 0 ) ) :
						type.equals( "colloquial" ) ?  Colloquial.interpret( a.copyAfter( 0 ) ) :
						type.equals(  Plural.NAME ) ?      Plural.interpret( a.copyAfter( 0 ) ) :
						type.equals(    Item.NAME ) ?        Item.interpret( a.copyAfter( 0 ) ) :
						type.equals( Spatial.NAME ) ?     Spatial.interpret( a.copyAfter( 0 ) ) :
						type.equals(Temporal.NAME ) ?    Temporal.interpret( a.copyAfter( 0 ) ) :
						type.equals( Concept.NAME ) ?     Concept.interpret( a.copyAfter( 0 ) ) :
									  FAIL; // );
		}
		audit.ERROR("doCall() fails - "+ (a==null?"no params":"not enough params: "+ a.toString()));
		return FAIL; //audit.traceOut( FAIL ); //
	}
	
	// perhaps need to re-think this? Do we need this stage - other than for relative concept???
	private String doSofa( Strings prog ) {
		String cmd = prog.get( 0 );
		char firstCh = cmd.charAt( 0 );
		return ('"' == firstCh || '\'' == firstCh) ?
				Strings.stripQuotes( cmd )
				: doCall( prog );
	}

	private String doNeg( Strings prog ) {
		//audit.traceIn( "doNeg", prog.toString( Strings.SPACED ));
		boolean negated = prog.get( 0 ).equals( "!" );
		String rc = doSofa( prog.copyAfter( negated ? 0 : -1 ) );
		if (negated) rc = rc.equals( True ) ? False : rc.equals( False ) ? True : rc;
		return rc; // */audit.traceOut( rc );
	}

/*private static String doAssign( Strings prog ) { // x = a b .. z
	TRACEIN1( "'%s'", arrayAsChars( prog, SPACED ));
	int assignment = 0 == .compareTo( prog[ 1 ], "=" );
	Strings e = copyStringsAfter( prog, assignment ? 1 : -1 );
	long rc = doNeg( e );
	if (assignment) {
		if (0 == .compareTo( "value", prog[ 3 ])) { // deal with string return
			AUDIT2( "Assigning STRING %s = %s", prog.get( 0 ), rc ? (String )rc : "" );
			int n = arrayContainsCharsAt( symbols, prog.get( 0 ));
			if (n == -1) {
				symbols = arrayAppend( symbols, newChars( prog.get( 0 )));
				values = arrayAppend( values, newChars( rc ? (String )rc : "" ));
			} else
				arrayReplaceCharsAt( values, n, rc ? (String )rc : "" );
		} else if (0 == .compareTo( "exists", prog[ 3 ])) { // deal with string return
			AUDIT2( "Assigning BOOLEAN %s = %s", prog.get( 0 ), rc ? "true" : "false" );
			int n = arrayContainsCharsAt( symbols, prog.get( 0 ));
			if (n == -1) {
				symbols = arrayAppend( symbols, newChars( prog.get( 0 )));
				values = arrayAppend( values, newChars( rc ? "true" : "false" ));
			} else
				arrayReplaceCharsAt( values, n, rc ? "true" : "false" );
		} else {
			printf( "type conversion error in '%s'\n", arrayAsChars( prog, SPACED ));
	}	}
	deleteStrings( &e, KEEP_ITEMS );
	TRACEOUTint( rc );
	return rc ;
}// */

	// a b .. z {| a b .. z}
	private String doOrList( Strings a ) {
		//audit.traceIn( "doOrList", a.toString( Strings.SPACED ));
		String rc = False;
		for (int i = 0, sz = a.size(); i<sz; i++) {
			Strings cmd = a.copyFromUntil( i, "||" );
			i += cmd.size(); // left pointing at "|" or null
			if (rc.equals( False )) rc = doNeg( cmd ); // only do if not yet succeeded -- was doAssign()
		}
		//return audit.traceOut( rc );
		return rc;
	}

	private String doAndList( Strings a ) {
		//audit.traceIn( "doAndList", a.toString( Strings.SPACED ));
		String rc = True;
		for (int i=0, sz=a.size(); i<sz; i++) {
			Strings cmd = a.copyFromUntil( i, "&&" );
			//audit.debug( "cmd=" + cmd +", i="+ i );
			i += cmd == null ? 0 : cmd.size();
			if (rc.equals( True )) rc = doOrList( cmd );
		}
		return rc; // */ audit.traceOut( rc );
	}

	private String doExpr( Strings a ) {
		//audit.traceIn( "doExpr", a.toString( Strings.SPACED ));
		Strings cmd = new Strings(); // -- build a command...
		while (0 < a.size() && !a.get( 0 ).equals( ")" )) {
			if (a.get( 0 ).equals( "(" )) {
				a.remove( 0 );
				cmd.add( doExpr( a ));
			} else {
				cmd.add( a.get( 0 ));
				a.remove( 0 ); // KEEP_ITEMS!
			}
			//audit.debug( "a="+ a.toString() +", cmd+"+ cmd.toString() );
		}
		String rc = doAndList( cmd );
		if ( 0 < a.size() ) a.remove( 0 ); // remove ")"
		return rc; // */audit.traceOut( rc );
	}
	public String interpret( Strings sa ) {
		Strings a = new Strings( sa );
		for (String s : sa) {
			if (   s.equals("&&") 
				|| s.equals("||")
				|| s.equals("(")
				|| s.equals("!")
			   ) {
				return doSofa( a );
		}	}
		return doExpr( a ); // still need to check if it is a constant
	}
	
	public static void main( String[] argv ) { // sanity check...
		Sofa cmd = new Sofa();
		if (argv.length > 0) {
			cmd.interpret( new Strings( argv ));
		} else {
			Audit.allOn();
			Audit.traceAll( true );
			audit.log( "Sofa: Ovl is: "+ Overlay.Get().toString());
			cmd.run();
}	}	}
