package org.enguage.objects.space;

import org.enguage.objects.Every;
import org.enguage.objects.Numeric;
import org.enguage.objects.Preferences;
import org.enguage.objects.Sign;
import org.enguage.objects.Spatial;
import org.enguage.objects.Temporal;
import org.enguage.objects.Variable;
import org.enguage.objects.expr.Function;
import org.enguage.objects.list.Item;
import org.enguage.objects.list.List;
import org.enguage.objects.list.Trans;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.util.sys.Shell;
import org.enguage.vehicle.Colloquial;
import org.enguage.vehicle.Plural;

public class Sofa extends Shell {
	static private Audit audit = new Audit( "Sofa" );

	public Sofa(){
		super( "Sofa" );
		if (!Overlay.autoAttach())
			audit.ERROR( "Ouch! in sofa" );
	}
//	private static final Strings True  = Shell.Success;
//	private static final Strings False = Shell.Fail;

	public Strings doCall( Strings a ) {
		//audit.in( "doCall", a.toString( Strings.CSV ));
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
				if (a.get( i ).equals( ":" )) {
					a.remove( i );
					break;
				} else
					a.set( i, Attribute.getValue( a.get( i ) ));
			
			String  type = a.remove( 0 );
			return //audit.out(
			    a.size() == 0 && type.equals( Shell.SUCCESS  ) ? Shell.Success :
				 a.size() == 0 && type.equals( Shell.FAIL ) ? Shell.Fail :
						type.equals(     "entity" ) ?      Entity.interpret( a ) :
						type.equals(       "link" ) ?        Link.interpret( a ) :
						type.equals(   Value.NAME ) ?       Value.interpret( a ) :
						type.equals(    List.NAME ) ?        List.interpret( a ) :
						type.equals( "preferences") ? Preferences.interpret( a ) :
						type.equals( Numeric.NAME ) ?     Numeric.interpret( a ) :
						// TODO:
						// in prep. for removing 'variable' from perform utterance: default class!
						//type.equals(        "set" ) ?    Variable.interpret( a.prepend( "get" ) ) :
						//type.equals(        "get" ) ?    Variable.interpret( a.prepend( "set" ) ) :
						type.equals( Variable.NAME) ?    Variable.interpret( a ) :
						type.equals(    "overlay" ) ?     Overlay.interpret( a ) :
						type.equals( "colloquial" ) ?  Colloquial.interpret( a ) :
						type.equals(  Plural.NAME ) ?      Plural.interpret( a ) :
						type.equals(    Item.NAME ) ?        Item.interpret( a ) :
						type.equals( Spatial.NAME ) ?     Spatial.interpret( a ) :
						type.equals(Temporal.NAME ) ?    Temporal.interpret( a ) :
						type.equals(   Trans.NAME ) ?       Trans.interpret( a ) :
						type.equals(    Sign.NAME ) ?        Sign.interpret( a ) :
						type.equals(Function.NAME ) ?    Function.interpret( a ) :
						type.equals(   Every.NAME ) ?       Every.interpret( a ) :
							Fail; // );
		}
		audit.ERROR("doCall() fails - "+ (a==null?"no params":"not enough params: "+ a.toString()));
		return Fail; //audit.traceOut( FAIL ); //
	}
	
	// perhaps need to re-think this? Do we need this stage - other than for relative concept???
//	private Strings xdoSofa( Strings prog ) {
//		String cmd = prog.get( 0 );
//		char firstCh = cmd.charAt( 0 );
//		return (Strings.DOUBLE_QUOTE == firstCh ||
//				Strings.SINGLE_QUOTE == firstCh) ?
//					Strings.stripQuotes( cmd )
//					: doCall( prog );
//	}

	private Strings doNeg( Strings prog ) {
		//audit.traceIn( "doNeg", prog.toString( Strings.SPACED ));
		boolean negated = prog.get( 0 ).equals( "!" );
		Strings rc = doCall( prog.copyAfter( negated ? 0 : -1 ) ); // was do sofa
		if (negated) rc = rc.equals( Success ) ? Fail : rc.equals( Fail ) ? Success : rc;
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
	private Strings doOrList( Strings a ) {
		//audit.traceIn( "doOrList", a.toString( Strings.SPACED ));
		Strings rc = Fail;
		for (int i = 0, sz = a.size(); i<sz; i++) {
			Strings cmd = a.copyFromUntil( i, "||" );
			i += cmd.size(); // left pointing at "|" or null
			if (rc.equals( Fail )) rc = doNeg( cmd ); // only do if not yet succeeded -- was doAssign()
		}
		//return audit.traceOut( rc );
		return rc;
	}

	private Strings doAndList( Strings a ) {
		//audit.traceIn( "doAndList", a.toString( Strings.SPACED ));
		Strings rc = Success;
		for (int i=0, sz=a.size(); i<sz; i++) {
			Strings cmd = a.copyFromUntil( i, "&&" );
			//audit.debug( "cmd=" + cmd +", i="+ i );
			i += cmd == null ? 0 : cmd.size();
			if (rc.equals( Success )) rc = doOrList( cmd );
		}
		return rc; // */ audit.traceOut( rc );
	}

	private Strings doExpr( Strings a ) {
		//audit.traceIn( "doExpr", a.toString( Strings.SPACED ));
		Strings cmd = new Strings(); // -- build a command...
		while (0 < a.size() && !a.get( 0 ).equals( ")" )) {
			if (a.get( 0 ).equals( "(" )) {
				a.remove( 0 );
				cmd.addAll( doExpr( a ));
			} else {
				cmd.add( a.get( 0 ));
				a.remove( 0 ); // KEEP_ITEMS!
			}
			//audit.debug( "a="+ a.toString() +", cmd+"+ cmd.toString() );
		}
		Strings rc = doAndList( cmd );
		if ( 0 < a.size() ) a.remove( 0 ); // remove ")"
		return rc; // */audit.traceOut( rc );
	}
	public Strings interpret( Strings sa ) {
		Strings a = new Strings( sa );
		for (String s : sa) {
			if (   s.equals("&&") 
				|| s.equals("||")
				|| s.equals("(")
				|| s.equals("!")
			   ) {
				return doCall( a ); //doSofa( a );
		}	}
		return new Strings( doExpr( a )); // still need to check if it is a constant
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
