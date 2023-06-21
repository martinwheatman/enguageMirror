package org.enguage.sign.symbol.reply;

import java.util.Locale;

import org.enguage.util.Strings;
import org.enguage.util.sys.Shell;

public class Response {
	public  static final int  N_DNU = -5; // DO NOT UNDERSTAND
	public  static final int  N_UDU = -4; // user does not understand
	public  static final int  N_DNK = -3; // NOT KNOWN -- init
	public  static final int N_FAIL = -2; // SORRY -- -ve
	public  static final int   N_NO = -1; // FALSE -- -ve
	public  static final int   N_OK =  0; // TRUE  -- +ve
	public  static final int  N_CHS =  1; // narrative verdict
	
	private static String  dnuStr = "DNU";
	private static Strings dnu = new Strings( dnuStr );
	public  static void    dnu( String s ) { dnu = new Strings( dnuStr = s.toLowerCase( Locale.getDefault() )); }
	public  static Strings dnu(){ return dnu; }
	public  static String  dnuStr(){ return dnuStr; }

	private static Strings dnk = new Strings( "DNK" );
	private static String  dnkStr = "DNK";
	public  static void    dnk( String s ) { dnk = new Strings( dnkStr = s.toLowerCase( Locale.getDefault() )); }
	public  static Strings dnk() { return dnk; }
	public  static String  dnkStr() { return dnkStr; }

	private static Strings no = new Strings( "no" );
	private static String  noStr = "no";
	public  static void    no(  String s ) { no = new Strings( noStr = s.toLowerCase( Locale.getDefault() )); }
	public  static Strings no() { return no; }
	public  static String  noStr() { return noStr; }
	
	private static Strings yes    = new Strings( "yes" );
	private static String  yesStr = "yes";
	public  static void    yes( String s ) { yes = new Strings( yesStr = s.toLowerCase( Locale.getDefault() )); }
	public  static Strings yes() { return yes; }
	public  static String  yesStr() { return yesStr; }

	private static Strings failure   = new Strings( Shell.FAIL );
	private static String  failureStr = Shell.FAIL;
	public  static void    failure(  String s ) { failure = new Strings( failureStr = s.toLowerCase( Locale.getDefault() )); }
	public  static Strings failure() { return failure; }
	public  static String  failureStr() { return failureStr; }
	
	private static Strings success    = new Strings( Shell.SUCCESS );
	private static String  successStr = Shell.SUCCESS;
	public  static void    success( String s ) { success = new Strings( successStr = s.toLowerCase( Locale.getDefault() )); }
	public  static Strings success() { return success; }
	public  static String  successStr() { return successStr; }
	
	/*
	 * Response
	 */
	private int  type = N_DNU;
	public  int  type() {return type;}
	public  void type( int t ) {type = t;}
	
	public  int  setType( Strings uttr ) {
		if (type != N_UDU) {
			     if (uttr.beginsIgnoreCase(    yes )) type =   N_OK;
			else if (uttr.beginsIgnoreCase( success)) type =   N_OK;
			else if (uttr.beginsIgnoreCase( failure)) type = N_FAIL;
			else if (uttr.beginsIgnoreCase(    dnu )) type =  N_DNU;
			else if (uttr.beginsIgnoreCase(     no )) type =   N_NO;
			else if (uttr.beginsIgnoreCase(    dnk )) type =  N_DNK;
			else type = N_CHS;
		}
		return type;
}	}
