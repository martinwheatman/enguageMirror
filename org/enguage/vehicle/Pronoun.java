package org.enguage.vehicle;

import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Attributes;
import org.enguage.util.sys.Shell;

public class Pronoun {
	static private Audit audit = new Audit( "Pronoun" );
	
	// hard-coded three dimensions...
	static public final    int SUBJECTIVE = 0;
	static public final    int  OBJECTIVE = 1;
	static public final    int POSSESSIVE = 2;
	
	static public final    int   SINGULAR = 0;
	static public final    int     PLURAL = 1;
	
	static public final    int  MASCULINE = 0;
	static public final    int   FEMININE = 1;
	static public final    int    NEUTRAL = 2;
	static public final    int UNGENDERED = 3;
	
	static public final String   singular = "singular";
	static public final String     plural = "plural";
	static public final String  masculine = "masculine";
	static public final String   feminine = "feminine";
	static public final String    neutral = "neutral";
	static public final String subjective = "subjective";
	static public final String  objective = "objective";
	static public final String possessive = "possessive";
	
	// to switch pronouns on and off
	static private boolean using = false;
	static public  boolean using() {return using;}
	static public  void    using( boolean u ) {using = u;}
	
	// gender...
	static Strings masculines = new Strings();
	static Boolean isMasculine( String word ) { return masculines.contains( word ); }
	static void    masculineIs( String word ) {
		audit.debug("setting "+ word +" m");
		masculines.prepend( word );
	}
	static Strings feminines = new Strings();
	static Boolean isFeminine( String word ) { return feminines.contains( word ); }
	static void    feminineIs( String word ) { feminines.prepend( word );}
	static Boolean isNeutral( String word ) { // both male and female
		return masculines.contains( word ) &&
				feminines.contains( word );
	}
	static void neutralIs( String word ) {
		masculines.prepend( word );
		feminines.prepend( word );
	}
	static void ungender( String word ) {
		while (isMasculine( word ) || isFeminine( word )) {
			masculines.remove( word );
			feminines.remove( word );
	}	}
	static private int valueMfn( String s ) {
		return isNeutral(      s ) ? UNGENDERED
				: isMasculine( s ) ? MASCULINE
				: isFeminine(  s ) ? FEMININE : NEUTRAL ;
	}
	
	static private String[][][] pronouns = {
			{{"he",    "she",   "it",    "he or she"  },   // singular subjective
			 {"they",  "they",  "they",  "they"       }},  // plural
			{{"him",   "her",   "it",    "him or her" },   // singular objective
			 {"them",  "them",  "them",  "then"       }},  // plural
			{{"his",   "her",   "its",   "his or her" },   // singular possessive
			 {"their", "their", "their", "their"      }} };// plural
		
	static private String[][][] values = {
			{{"A", "B", "C", "D" },   // singular subjective
			 {"E", "F", "G", "H" }},  // plural
			{{"I", "J", "K", "L" },   // singular objective
			 {"M", "N", "O", "P" }},  // plural
			{{"Q", "R", "R", "T" },   // singular possessive
			 {"U", "V", "W", "X" }} };// plural
	
	static private boolean possessivePronoun( String s ) {
		for (int j=SINGULAR; j<=PLURAL; j++)
			for (int k=MASCULINE; k<=UNGENDERED; k++)
				if (s.equals( pronouns[POSSESSIVE][j][k])) return true;
		return false;
	}
	static private int pronounOsp( String s ) {
		for (int i=SUBJECTIVE; i<=POSSESSIVE; i++)
			for (int j=SINGULAR; j<=PLURAL; j++)
				for (int k=MASCULINE; k<=UNGENDERED; k++)
					if (s.equals( pronouns[i][j][k])) return i;
		return -1;
	}
	static private int pronounMfn( String s ) {
		for (int i=SUBJECTIVE; i<=POSSESSIVE; i++)
			for (int j=SINGULAR; j<=PLURAL; j++)
				for (int k=MASCULINE; k<=UNGENDERED; k++)
					if (s.equals( pronouns[i][j][k])) return k;
		return -1;
	}
	static private int pronounSp( String s ) {
		for (int i=SUBJECTIVE; i<=POSSESSIVE; i++)
			for (int j=SINGULAR; j<=PLURAL; j++)
				for (int k=MASCULINE; k<=UNGENDERED; k++)
					if (s.equals( pronouns[i][j][k])) return j;
		return -1;
	}
	static private boolean isPronoun( String s ) { return pronounOsp( s ) != -1;}

	//
	static private int nameOs( String name ) {
		return name.equals(  subject   ) ? SUBJECTIVE :
		       name.equals(   object   ) ? OBJECTIVE  :
		       name.equals( possession ) ? POSSESSIVE : -1; 
	}
	static private int valuePl( String name ) {
		return Plural.isPlural( name ) ? PLURAL : SINGULAR;
	}
	static private String subject = "SUBJECT";
	static public  void   subjective( String nm ) { subject = nm; }
	static public  String subjective() { return subject; }
	
	static private String object = "OBJECT";
	static public  void   objective( String nm ) { object = nm; }
	static public  String objective() { return object; }
	
	static private String possession = "'s";
	static public  void   possession( String nm ) { possession = nm; }
	static public  String possession() { return possession; }
	static public boolean possessive( String word ) {
		String poss = get( subject, "" );
		poss = appended ? poss + possession : possession + poss;
		return word.equals( poss );
	}
	static public  boolean appended = false;
	static public  boolean appended() { return appended;}
	static public  void    appended(boolean b) { appended = b;}
	
	//
	// interaction code...
	//
	static private Strings set( Strings sa ) {
		// set([ "objective", "singular", "masculine", "him" ]);
		Strings rc = Shell.Fail;
		/* the masculine personal objective pronoun is him
		 * pronoun [obj|subj] [s|p] [m|f|n] him
		 */
		if (sa.size() == 4) {
			int so = 0, mfn = 0, sp = 0;
			rc = Shell.Success;
			while (sa.size() > 1) {
				String s = sa.remove( 0 );
				if (s.equals( singular ))
					sp = SINGULAR;
				else if (s.equals( plural ))
					sp = PLURAL;
				else if (s.equals( possessive ))
					so = POSSESSIVE;
				else if (s.equals( objective ))
					so = OBJECTIVE;
				else if (s.equals( subjective ))
					so = SUBJECTIVE;
				else if (s.equals( masculine ))
					mfn = MASCULINE;
				else if (s.equals( feminine ))
					mfn = FEMININE;
				else if (s.equals( neutral ))
					mfn = NEUTRAL;
				else
					rc = Shell.Fail;
			}
			if (rc.equals( Shell.Success ))
				pronouns [so][sp][mfn] = sa.remove( 0 );
		}
		return rc;
	}
	static private Strings add (Strings sa) {
		// e.g. add masculine martin feminine ruth
		audit.in( "add", ""+ sa );
		Strings rc = Shell.Success;
		while (rc.equals( Shell.Success ) && sa.size() > 1) {
			String gender = sa.remove( 0 );
			audit.debug( "gender:"+ gender );
			if (gender.equals( "masculine" ))
				masculineIs( sa.remove( 0 ));
			else if (gender.equals( "feminine" ))
				feminineIs( sa.remove( 0 ));
			else if (gender.equals( "unknown" ))
				neutralIs( sa.remove( 0 ));
			else
				rc = Shell.Fail;
		}
		return audit.out( rc );
	}
	static public Strings interpret( Strings sa ) {
		// e.g. (pronoun) add masculine martin
		//      (pronoun) set OBJECTIVE PLURAL MASCULINE him
		//      (pronoun) [start|stop]
		audit.in( "interpret", ""+ sa );
		Strings rc = Shell.Fail;
		int sz = sa.size();
		if (sz > 0) {
			rc = Shell.Success;
			String cmd = sa.remove( 0 );
			if (cmd.equals("set" ))
				rc = set( sa );
			else if (cmd.equals( "add" ))
				rc = add( sa );
			else if (cmd.equals("start"))
				using( true );
			else if (cmd.equals( "stop" ))
				using( false );
			else
				rc = Shell.Fail;
		}
		return audit.out( rc );
	}
	//
	// Context Control
	//
	static private Attributes ctx = new Attributes();
	
	static private void set( String name, String value ) {
		// set SUBJECT he     -> ignore
		//     SUBJECT martin => values[s][s][m]="martin", SUBJECT="martin"
		//     UNIT    cup    => unit="cup"
		audit.in( "set", name +" is >"+ value +"<" );
		
		int os = nameOs( name );
		if (!using) {
			ctx.remove( name );
			ctx.add( new Attribute( name, value ));
		} else if (os == -1 || !isPronoun( value )) { 
			if (os != -1)
				values[os][valuePl( value )][valueMfn( value )] = new String( value );
			// always keep a copy in ctx
			ctx.remove( name );
			ctx.add( new Attribute( name, value ));
		} // else ignore setting pronouns, e.g. he, she, it etc.

		audit.out();
	}
	static private String get( String name, String value ) {
		// N.B value is only used in get to deref pronoun table!
		audit.in( "get", name + (isPronoun(value)?" (pronoun="+ value +")":""));
		int os = nameOs( name );
		return audit.out( using && os != -1 && isPronoun( value ) ?
							values[os][valuePl( value )][pronounMfn( value )]
							: ctx.get( name ));
	}
	// -- test code
	static private void test( String name, String value, String expected ) {
		audit.IN( "test", "var="+ name +", value="+ value );
		audit.log( "Currently "+  name +" is >"+ ctx.get( name ) +"/"+ get( name, value ) +"<)");
		set( name, value );
		if (!expected.equals( "" )) {
			value = get( name, value );
			if (expected.equals( "" ) || value.equals( expected ))
				audit.passed();
			else
				audit.ERROR( "answer is '"+ value +"',\n\t\tbut should be '"+ expected +"'" );
		}
		audit.OUT( value );
	}
	static private void possessiveOutbound( String value ) {
		if (using) {
			String subj = ctx.get( subject );
			if ((subj+possession()).equals( value )) {
				int ps  = valuePl(  subj ),
				    mfn = valueMfn( subj );
				audit.passed( "passes: "+ value +" => "+pronouns[POSSESSIVE][ps][mfn] );
			} else
				audit.log( "failed: "+ ctx.get( subject )+possession() +" != "+ value );
		} else
			audit.ERROR( "Possession test, but not using!" );
	}
	static private void possessiveInbound( String pronoun ) {
		audit.in(  "possInternalising", pronoun );
		String ans = "internalising failed";
		// e.g. "his"->SUBJECT/"her"->SUBJECT/"its"->OBJECT/"their"
		if (using)
			if (possessivePronoun( pronoun ))
				audit.passed( "Passed: "+ pronoun +" => "+ (ans = values[SUBJECTIVE][pronounSp( pronoun )][pronounMfn( pronoun )]));
			else
				audit.passed( "Test fails: Pronoun type not possessive ("+ pronounOsp( pronoun ) +"!="+ POSSESSIVE +")" );
		else
			audit.passed( "Test fails: Possession test, but not using!" );
		audit.out( ans );
	}
	static private void testInterpret( String u ) { interpret( new Strings( u ));}
	
	static public void main( String args[]) {
		testInterpret( "set singular subjective neutral they" );
		audit.log( "pronoun: "+ pronouns[SUBJECTIVE][SINGULAR][NEUTRAL]);
		
		testInterpret( "add masculine marv" );
		testInterpret( "add feminine  ruth" );

		test( subject, "he",  "he"  );
		test( subject, "she", "she" );
		
		audit.title( "start using pronouns" );
		testInterpret( "start" );
		
		test( subject, "marv", "marv" );
		test( subject, "he",   "marv" );
		test( subject, "ruth", "ruth" );
		test( subject, "he",   "marv" );
		test( subject, "she",  "ruth" );
		test( subject, "yota", "yota" );
		test( subject, "it",   "yota" );
		test( subject, "she",  "ruth" );
		test( subject, "he",   "marv" );
		
		audit.title( "Possession Test" );
		testInterpret( "add masculine martin" );
		set( subject, "martin" );
		
		audit.subtitle( "Outbound Test" );
		possessiveOutbound( "martin" );
		possessiveOutbound( "ruth's" );
		possessiveOutbound( "martin's" );
		
		audit.subtitle( "Inbound Test" );
		possessiveInbound( "him" );
		possessiveInbound( "her" );
		possessiveInbound( "his" );
		audit.PASSED();
}	}