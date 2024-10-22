package org.enguage.sign.interpretant;

import java.util.Locale;

import org.enguage.util.strings.Strings;

public class Response {

	public enum Type {
		E_DNU, // DO NOT UNDERSTAND
		E_UDU, // user does not understand
		E_DNK, // NOT KNOWN -- init
		E_SOZ, // SORRY -- -ve
		E_NO,  // FALSE -- -ve
		E_OK,  // TRUE  -- +ve identical to 'yes'
		E_YES,  // TRUE  -- +ve identical to 'yes'
		E_CHS; // narrative verdict - meh!
	}
	
	public static final boolean isFelicitous( Response.Type type ) {
		return  Response.Type.E_YES == type ||
				Response.Type.E_OK  == type ||
				Response.Type.E_CHS == type;
	}

	public static final Type typeFromStrings( Strings uttr ) {
		     if (uttr.begins( yes()     )) return Type.E_YES;
		else if (uttr.begins( okay()    )) return Type.E_OK;
		else if (uttr.begins( notOkay() )) return Type.E_SOZ;
		else if (uttr.begins( dnu()     )) return Type.E_DNU;
		else if (uttr.begins( udu()     )) return Type.E_UDU;
		else if (uttr.begins( no()      )) return Type.E_NO;
		else if (uttr.begins( dnk()     )) return Type.E_DNK;
		else return Type.E_CHS;
	}

	public static  final String S_OKAY   = "ok";
	private static Strings okay    = new Strings( S_OKAY );
	public  static void    okay( String s ) {okay = new Strings( s.toLowerCase( Locale.getDefault() ));}
	public  static Strings okay() {return okay;}

	public static  final String S_NOT_OK = "sorry";
	private static Strings notOkay = new Strings( S_NOT_OK );
	public  static void    notOkay( String s ) {notOkay = new Strings( s.toLowerCase( Locale.getDefault() ));}
	public  static Strings notOkay() {return notOkay;}
	
	private static final String dnkStr = "DNK";
	private static Strings dnk = new Strings( dnkStr );
	public  static void    dnk( String s ) {dnk = new Strings( s.toLowerCase( Locale.getDefault() ));}
	public  static Strings dnk() {return dnk;}
	public  static String  dnkStr() {return dnkStr;}

	private static final String  noStr = "no";
	private static Strings no = new Strings( noStr );
	public  static void    no(  String s ) {no = new Strings( s.toLowerCase( Locale.getDefault() ));}
	public  static Strings no() {return no;}
	public  static String  noStr() {return noStr;}
	
	private static final String  yesStr = "yes";
	private static Strings yes    = new Strings( yesStr );
	public  static void    yes( String s ) {yes = new Strings( s.toLowerCase( Locale.getDefault() ));}
	public  static Strings yes() {return yes;}
	public  static String  yesStr() {return yesStr;}

	private static final String  dnuStr = "DNU";
	private static Strings dnu = new Strings( dnuStr );
	public  static void    dnu( String s ) {dnu = new Strings( s.toLowerCase( Locale.getDefault() ));}
	public  static Strings dnu(){return dnu;}
	public  static String  dnuStr(){return dnuStr;}

	private static final String  uduStr = "UDU";
	private static Strings udu = new Strings( uduStr );
	public  static void    udu( String s ) {udu = new Strings( s.toLowerCase( Locale.getDefault() ));}
	public  static Strings udu() {return udu;}
	public  static String  uduStr() {return uduStr;}
}
