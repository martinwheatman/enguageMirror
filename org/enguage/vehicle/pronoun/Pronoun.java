package org.enguage.vehicle.pronoun;

import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Attributes;
import org.enguage.util.sys.Shell;
import org.enguage.vehicle.Plural;

public class Pronoun {
	static private Audit audit = new Audit( "Pronoun" );
	
	// hard-coded three dimensions...
	static public final    int SUBJECTIVE = 0;
	static public final    int  OBJECTIVE = 1;
	static public final    int POSSESSIVE = 2;
	
	static public final    int   SINGULAR = 0;
	static public final    int     PLURAL = 1;
	
	static public final String   singular = "singular";
	static public final String     plural = "plural";
	
	static public final String subjective = "subjective";
	static public final String  objective = "objective";
	static public final String possessive = "possessive";
		
	static private int valuePl( String name ) {
		return !possessive( name ) && Plural.isPlural( name ) ? PLURAL : SINGULAR;
	}
	// ////////////////////////////////////////////////////////////////////////
	
	static private String[][][] pronouns = { // I, we, us
			{{"i",   "he",    "she",   "it",    "he or she"  },   // singular subjective
			 {"we",  "they",  "they",  "they",  "they"       }},  // plural
			{{"me",  "him",   "her",   "it",    "him or her" },   // singular objective
			 {"us",  "them",  "them",  "them",  "them"       }},  // plural
			{{"my",  "his",   "her",   "its",   "his or her" },   // singular possessive
			 {"our", "their", "their", "their", "their"      }} };// plural
		
	static private String[][][] values = { // initialise to names!
			{{"",   "",    "",   "",    ""  },   // singular subjective
			 {"",   "",    "",   "",    ""  }},  // plural
			{{"",   "",    "",   "",    ""  },   // singular objective
			 {"",   "",    "",   "",    ""  }},  // plural
			{{"",   "",    "",   "",    ""  },   // singular possessive
			 {"",   "",    "",   "",    ""  }} };// plural
	
	static private boolean possessivePn( String s ) {
		for (int j=SINGULAR; j<=PLURAL; j++)
			for (int k=Gendered.PERSONAL; k<=Gendered.UNGENDERED; k++)
				if (s.equals( pronouns[POSSESSIVE][j][k])) return true;
		return false;
	}
	static private int type( String s ) {
		for (int i=SUBJECTIVE; i<=POSSESSIVE; i++)
			for (int j=SINGULAR; j<=PLURAL; j++)
				for (int k=Gendered.PERSONAL; k<=Gendered.UNGENDERED; k++)
					if (s.equals( pronouns[i][j][k])) return i;
		return -1;
	}
	static private int snglPlIs( String s ) {
		for (int i=SUBJECTIVE; i<=POSSESSIVE; i++)
			for (int j=SINGULAR; j<=PLURAL; j++)
				for (int k=Gendered.PERSONAL; k<=Gendered.UNGENDERED; k++)
					if (s.equals( pronouns[i][j][k])) return j;
		return -1;
	}
	static private int gend( String s ) {
		for (int i=SUBJECTIVE; i<=POSSESSIVE; i++)
			for (int j=SINGULAR; j<=PLURAL; j++)
				for (int k=Gendered.PERSONAL; k<=Gendered.UNGENDERED; k++)
					if (s.equals( pronouns[i][j][k])) return k;
		return -1;
	}
	static private boolean isPronoun( String s ) { return type( s ) != -1;}

	// ------------------------------------------------------------------------
	static private int nameOs( String name ) {
		return name.equals(  subject   ) ? SUBJECTIVE :
		       name.equals(   object   ) ? OBJECTIVE  :
		       name.equals( possession ) ? POSSESSIVE : -1; 
	}
	static private String subject = "subject";
	static public  void   subjective( String nm ) { subject = nm; }
	static public  String subjective() { return subject; }
	
	static private String object = "OBJECT";
	static public  void   objective( String nm ) { object = nm; }
	static public  String objective() { return object; }
	
	static String possession = "'s";
	static public  void   possession( String nm ) { possession = nm; }
	static public  String possession() { return possession; }
	static public boolean possessive( String word ) {
		return appended ?
				word.endsWith( possession )
				: word.startsWith( possession );
	}
	static private boolean appended = true;
	static public  boolean appended() { return appended;}
	static public  void    appended(boolean b) { appended = b;}
	
	// ////////////////////////////////////////////////////////////////////////
	// Interaction code...
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
				else if (s.equals( Gendered.personal ))
					mfn = Gendered.PERSONAL;
				else if (s.equals( Gendered.masculine ))
					mfn = Gendered.MASCULINE;
				else if (s.equals( Gendered.feminine ))
					mfn = Gendered.FEMININE;
				else if (s.equals( Gendered.neutral ))
					mfn = Gendered.NEUTRAL;
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
		Strings rc = Shell.Success;
		while (rc.equals( Shell.Success ) && sa.size() > 1) {
			String gender = sa.remove( 0 );
			audit.debug( "gender:"+ gender );
			if (gender.equals( Gendered.masculine ))
				Gendered.masculineIs( sa.remove( 0 ));
			else if (gender.equals( Gendered.feminine ))
				Gendered.feminineIs( sa.remove( 0 ));
			else if (gender.equals( Gendered.personal ))
				Gendered.personalIs( sa.remove( 0 ));
			else if (gender.equals( Gendered.neutral ))
				Gendered.neutralIs( sa.remove( 0 ));
			else
				rc = Shell.Fail;
		}
		return rc;
	}
	static private Strings name (String name, String value) {
		// e.g. add masculine martin feminine ruth
		Strings rc = Shell.Success;
		if (name.equals( subjective ))
			subjective( value );
		
		else if (name.equals( objective ))
			objective( value );
		
		else if (name.equals( possessive ))
			possessive( value );
		
		else
			rc = Shell.Fail;

		return rc;
	}
	static private Strings name (String name) {
		// e.g. return possessive variable name
		audit.in( "name", "name="+ name );
		Strings rc =
				name.equals( subjective ) ?
						new Strings( subjective() )
						: name.equals( objective ) ?
								new Strings( objective() )
								: name.equals( possessive ) ?
										new Strings( possession() ) : Shell.Fail;
		return audit.out( rc );
	}
	static public Strings interpret( Strings sa ) {
		// e.g. (pronoun) add masculine martin
		//      (pronoun) set OBJECTIVE PLURAL MASCULINE him
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
			else if (cmd.equals( "name" ))
				switch (sz) { // original size!
					case 1: rc = Shell.Fail; break; // just cmd
					case 2: rc = name( sa.get( 0 )); break;
					default: name( sa.get( 0 ), sa.get( 1 ));
				}
			else
				rc = Shell.Fail;
		}
		return audit.out( rc );
	}
	// ////////////////////////////////////////////////////////////////////////
	// get/set
	//
	static private void set( String name, String value ) {
		// set SUBJECT he     -> ignore
		//     SUBJECT martin => values[s][s][m]="martin", SUBJECT="martin"
		//     UNIT    cup    => unit="cup"
		
		int os = nameOs( name );
		if (!isPronoun( value ) && os != -1)
			values[os][valuePl( value )][Gendered.valueMfn( value )] = new String( value );
	}
	static private String get( String name, String value ) {
		// N.B value is only used in get to deref pronoun table!
		//audit.in( "get", name + (isPronoun(value)?" (pronoun="+ value +")":""));
		int os = nameOs( name );
		return os != -1 && isPronoun( value ) ?
				values[os][valuePl( value )][Gendered.valueMfn( value )]
				: value;
	}
	static private String update( String name, String value ) {
		String first = value;
		set( name, value );
		String last = get( name, value );
		return last.equals( "" ) ? first : last;
	}
	static private void deref( Attribute a ) {a.value( update( a.name(), a.value() ));}
	static public Attributes deref( Attributes as ) { for (Attribute a : as) deref( a ); return as;}
	
	// ////////////////////////////////////////////////////////////////////////
	// -- test code
	//
	static private void test( String name, String value, String expected ) {
		Audit.log( "Currently "+  name +" is >"+ get( name, value ) +"<)");
		
		value = update( name, value );
		
		if (expected.equals( "" ) || value.equals( expected ))
			audit.passed();
		else
			audit.FATAL( "answer is '"+ value +"',\n\t\tbut should be '"+ expected +"'" );
	}
	static private void possessiveOutbound( String value ) {
		String subj = get( subject, value );
		if (possessive( value ))
			audit.passed( "passes: "+ value +" => "+
					pronouns[POSSESSIVE][valuePl( subj )][Gendered.valueMfn( subj )]);
		else
			Audit.log( "failed: "+ value +" != possessive"  );
	}
	static private void possessiveInbound( String pn ) {
		audit.in(  "possInternalising", pn );
		String ans = "internalising failed";
		// e.g. "his"->SUBJECT/"her"->SUBJECT/"its"->OBJECT/"their"
		if (possessivePn( pn ))
			audit.passed( "Passed: "+ pn +" => "+
					(ans = values[SUBJECTIVE][snglPlIs( pn )][gend( pn )]));
		else
			audit.passed( "Test fails: Pronoun type not possessive ("+
					type( pn ) +"!="+ POSSESSIVE +")" );
		audit.out( ans );
	}
	static private void testInterpret( String u ) { interpret( new Strings( u ));}
	
	static public void main( String args[]) {
		testInterpret( "set singular subjective neutral they" );
		Audit.log( "pronoun: "+ pronouns[SUBJECTIVE][SINGULAR][Gendered.NEUTRAL]);
		
		testInterpret( "add masculine marv" );
		testInterpret( "add feminine  ruth" );
		//testInterpret( "name subjective SUBJECT" );

		test( subject, "he",  "he"  );
		test( subject, "she", "she" );
		
		// setting and getting subject
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
		
		audit.subtl( "Outbound Test" );
		possessiveOutbound( "martin" );
		possessiveOutbound( "ruth's" );
		possessiveOutbound( "martin's" );
		
		audit.subtl( "Inbound Test" );
		possessiveInbound( "him" );
		possessiveInbound( "her" );
		possessiveInbound( "his" );
		
		Audit.log( subject+"/he => "+ update( subject, "he" ));
		Audit.log( "FRED/he => "+ update( "FRED", "he" ));
		
		Attributes a = new Attributes();
		a.add( new Attribute( subject, "martin" ));
		a.add( new Attribute( subject, "he" ));
		Audit.log( "a="+ a );
		Audit.log( "a="+ deref( a ));
		audit.PASSED();
}	}
