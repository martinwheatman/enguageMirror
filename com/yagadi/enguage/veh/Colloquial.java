package com.yagadi.enguage.veh;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

public class Colloquial {
	static private Audit audit = new Audit( "Colloquial" );
	
	private TreeMap<Strings,Strings> intl;
	private TreeMap<Strings,Strings> extl;

	public Colloquial() {
		intl = new TreeMap<Strings,Strings>();
		extl = new TreeMap<Strings,Strings>();
	}

	private void add( Strings ex, Strings in ) {
		intl.put( ex, in );
		extl.put( in, ex );
	}
	// ---
	// this doesn't spot overlapping colloquia...
	// wandered lonely --> moved aimlessly
	// aimlessly as a cloud --> duff analogy
	// lonely as a cloud --> very lonely
	// I wandered lonely as a cloud --> I moved duff analogy (not as required: I moved aimlessly very lonely.)
	public Strings externalise( Strings a ) {
		Set<Map.Entry<Strings,Strings>> set = extl.entrySet();
		Iterator<Map.Entry<Strings,Strings>> i = set.iterator();
		while(i.hasNext()) {
			Map.Entry<Strings,Strings> me = (Map.Entry<Strings,Strings>)i.next();
			//audit.log("Coll: externalising:"+ me.getValue() +":with"+ me.getKey() +":");
			a.replace( me.getValue(), me.getKey());
		}
		return a;
	}
	public String toString() {
		String str = "";
		Set<Map.Entry<Strings,Strings>> set = extl.entrySet();
		Iterator<Map.Entry<Strings,Strings>> i = set.iterator();
		while(i.hasNext()) {
			Map.Entry<Strings,Strings> me = (Map.Entry<Strings,Strings>)i.next();
			str += me.getValue() +"/"+ me.getKey() +" ";
		}
		return str;
	}
	public Strings internalise( Strings a ) {
		Set<Map.Entry<Strings,Strings>> set = intl.entrySet();
		Iterator<Map.Entry<Strings,Strings>> i = set.iterator();
		while(i.hasNext()) {
			Map.Entry<Strings,Strings> me = (Map.Entry<Strings,Strings>)i.next();
			a.replace( me.getValue(), me.getKey());
		}
		return a;
	}
	
	static private Colloquial user = new Colloquial();
	static public  Colloquial user() { return user; }
	
	static private Colloquial host = new Colloquial();
	static public  Colloquial host() {return host;}
	
	static private Colloquial symmetric = new Colloquial();
	static public  Colloquial symmetric() {return symmetric;}
	
	static public Strings applyOutgoing( Strings s ) {
		return symmetric().externalise(      // 2. [ "You", "do", "not", "know" ] -> [ "You", "don't", "know" ]
				    host().externalise( s ));// 1. [ "_user", "does", "not", "know" ] -> [ "You", "do", "not", "know" ]
	}
	static public Strings applyIncoming( Strings s ) {
		/* this is called in Repertoires.interpret(), so that any colloquia
		 * used in the repertoire files are correctly interpreted.
		 */
		return  user().internalise(       // user phrases expand
		   symmetric().internalise( s )); // general expansion
	}

	static public String interpret( Strings a ) {
		if (null == a) return Shell.FAIL;
		//audit.traceIn( "interpret", a.toString( Strings.CSV ));
		if (a.size() >= 3) {
			
			Strings intl, extl;
			if (a.size() == 3) {
				intl = new Strings( Strings.trim( a.get( 1 ), '"' ));
				extl= new Strings( Strings.trim( a.get( 2 ), '"' ));
			} else {
				intl  = new Strings();
				extl = new Strings();
 				boolean doingFirst = true;
				for (int i=1; i<a.size(); i++) {
					String item = a.get( i );
					if ( item.equals( "=" ))
						doingFirst = false;
					else if ( doingFirst )
						intl.add( item );
					else
						extl.add( item );
			}	}
			
			if (a.get( 0 ).equals("both"))
				symmetric().add( intl, extl );
			else if (a.get( 0 ).equals("host"))
				host().add( intl, extl );
			else if (a.get( 0 ).equals("user"))
				user().add( intl, extl );
			else
				audit.ERROR( "Colloquial.interpret(): unknown command: "+ a.toString( Strings.CSV ));
		} else
			audit.ERROR( "Colloquial.interpret(): wrong number of params: "+ a.toString( Strings.CSV ));
		//return audit.traceOut( Shell.SUCCESS );
		return Shell.SUCCESS;
	}
	public static void main( String args[] ) {
		Audit.allOn();
		Strings a = new Strings( "This test passes" );
		Colloquial c = new Colloquial();
		c.add( new Strings( "This" ), new Strings( "Hello" ));
		c.add( new Strings( "test passes" ), new Strings( "world" ));
		a = c.externalise( a );
		audit.log( a.toString( Strings.SPACED ));
		a = c.internalise( a );
		audit.log( a.toString( Strings.SPACED ));
		
		interpret( new Strings( "both \"does not\"  \"doesn't\"" ));
		interpret( new Strings( "both \"do not\" \"don't\"" ));
		interpret( new Strings( "both \"cannot\" \"can't\"" ));
		//interpret( new Strings( "both \"can not\" \"cannot\"" ));
		interpret( new Strings( "both \"I have\" \"I've\"" ));
		interpret( new Strings( "both \"i am\" \"I'm\"" ));
		interpret( new Strings( "both \"i would\" \"I'd\"" ));
		interpret( new Strings( "both \"i will\" \"I'll\"" ));
		interpret( new Strings( "both \"you are\" \"you're\"" ));
		interpret( new Strings( "both \"you would\" \"you'd\"" ));
		interpret( new Strings( "both \"you will\" \"you'll\"" ));
		interpret( new Strings( "both \"fish'n'chips\" \"fish and chips\"" ));

		interpret( new Strings( "host \"_host's\" \"my\"" ));
		interpret( new Strings( "host \"_user's\" \"your\"" ));
		interpret( new Strings( "host \"_user needs\" \"you need\"" ));
		interpret( new Strings( "host \"_user does not need\" \"you do not need\"" ));
		interpret( new Strings( "host \"_user\" \"you\"" ));

		audit.log( 
				applyIncoming( // general expansion
					new Strings( "I don't need anything" )
			).toString( Strings.SPACED ));
		
		audit.log(
			applyOutgoing(
				new Strings("_user needs: i do not understand, i do not need anything")
			)
		.toString( Strings.SPACED ));
		
}	}
