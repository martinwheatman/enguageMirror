package org.enguage.sign;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Locale;

import org.enguage.Enguage;
import org.enguage.repertoires.concepts.Autoload;
import org.enguage.repertoires.concepts.Concept;
import org.enguage.sign.interpretant.Commands;
import org.enguage.sign.object.Variable;
import org.enguage.sign.object.sofa.Perform;
import org.enguage.sign.symbol.reply.Answer;
import org.enguage.util.attr.Attribute;
import org.enguage.util.audit.Audit;
import org.enguage.util.http.Http;
import org.enguage.util.http.InfoBox;
import org.enguage.util.strings.Strings;
import org.enguage.util.strings.Terminator;
import org.enguage.util.sys.Fs;
import org.enguage.util.tag.Tag;

public class Config {

	private Config() {}
	
	private static final Audit audit = new Audit( "Config" );
	private static final String NAME = "config";
	
	private static boolean complete = false;
	public  static boolean complete() {return complete;}
	
	private static boolean setValues( String name, String value ) {
		     if (name.equals("ACCUMULATECOMMAND")) accumulateCmdStr( value );
		else if (name.equals("ANDCONJUNCTIONS")) andConjunction( value );
		else if (name.equals("ORCONJUNCTIONS")) orConjunctions(  new Strings( value ));
		else if (name.equals("PROPAGATEREPLY")) propagateReplyStr( value );
		else if (name.equals("ANDLISTFORMAT" )) andListFormat( value);
		else if (name.equals("LISTFORMATSEP" )) listSep(       value);
		else if (name.equals( "ORLISTFORMAT" )) orListFormat(  value );
		else if (name.equals( "REPEATFORMAT" )) repeatFormat(  value );
		else if (name.equals( "ATTRIBUTING" )) InfoBox.attributing(  value );
		else if (name.equals( "REFERENCERS" )) referencers( new Strings( value ));
		else if (name.equals( "CLASSPATH" )) Commands.classpath( value );
		else if (name.equals( "SUCCESS" )) okay( value );
		else if (name.equals( "FAILURE" )) notOkay( value );
		else if (name.equals(  "ANSWER" )) Answer.placeholder( value );
		else if (name.equals(   "SHELL" )) Commands.shell( value );
		else if (name.equals(   "TERMS" )) Terminator.terminators( new Strings( value ));
		else if (name.equals(    "SOFA" )) Commands.java( value );
		else if (name.equals(     "URL" )) Http.url( value );
		else if (name.equals(     "TTL" )) Autoload.ttl( value );
		else if (name.equals(     "DNU" )) dnu( value );
		else if (name.equals(     "DNK" )) dnk( value );
		else if (name.equals(     "UDU" )) udu( value );
		else if (name.equals(     "YES" )) yes( value );
		else if (name.equals(      "NO" )) no(  value );
		else
			return false;
		return true;
	}
	private static void setContext( ArrayList<Attribute> aa ) {
		ListIterator<Attribute> pi = aa.listIterator();
		while (pi.hasNext()) {
			Attribute  a = pi.next();
			String  name = a.name().toUpperCase( Locale.getDefault() );
			String value = a.value();
			if (!setValues( name, value))
				Variable.set( name,  value );
	}	}	
	private static void loadTag( Tag concepts ) {
		for (Tag t : concepts.children())
			if ( t.name().equals( "concept" ) &&
			    !t.attributes().value( "op" ).equals( "ignore" ))
				Concept.load( t.attributes().value( "id" ));
	}
	public static int load( String fname ) {
		int rc = -1;
		String content = Fs.stringFromStream(
			Assets.getStream( Enguage.RO_SPACE+ fname )
		);
		
		Tag t = new Tag( new Strings( content ).listIterator());
		if ((t = t.findByName( NAME )) != null) {
			setContext( t.attributes() );
			loadTag( t.findByName( "concepts" ));
			rc = content.length();
		}

		complete = true;
		return audit.out( rc );
	}

	private static String  dnuStr = "DNU";
	private static Strings dnu = new Strings( dnuStr );
	public  static void    dnu( String s ) { dnu = new Strings( dnuStr = s.toLowerCase( Locale.getDefault() )); }
	public  static Strings dnu(){ return dnu; }
	public  static String  dnuStr(){ return dnuStr; }

	private static Strings udu = new Strings( "UDU" );
	private static String  uduStr = "DNK";
	public  static void    udu( String s ) { udu = new Strings( dnkStr = s.toLowerCase( Locale.getDefault() )); }
	public  static Strings udu() { return udu; }
	public  static String  uduStr() { return uduStr; }

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

	private static String  notOkayStr = Perform.S_FAIL;
	private static Strings notOkay   = new Strings( notOkayStr );
	public  static void    notOkay(  String s ) { notOkay = new Strings( notOkayStr = s.toLowerCase( Locale.getDefault() )); }
	public  static Strings notOkay() { return notOkay; }
	public  static String  notOkayStr() { return notOkayStr; }
	
	private static String  okayStr = Perform.S_SUCCESS;
	private static Strings okay    = new Strings( okayStr );
	public  static void    okay( String s ) {okay = new Strings( okayStr = s.toLowerCase( Locale.getDefault() ));}
	public  static Strings okay() {return okay;}
	public  static String  okayStr() {return okayStr;}

	private static  boolean understood = true;
	public  static  boolean understoodIs( boolean was ) { return understood = was;}
	public  static  boolean isUnderstood() { return understood; }

	private static  String  repeatFormat = "i said, ... .";
	public  static  void    repeatFormat( String s ) { repeatFormat = s.toLowerCase( Locale.getDefault() );}
	public  static  String  repeatFormat() { return repeatFormat; }

	private static  String  andConjunction = "and";
	public  static  void    andConjunction( String s ) { andConjunction = s.toLowerCase( Locale.getDefault() ); }
	public  static  String  andConjunction() { return andConjunction; }

	private static  Strings andListFormat = new Strings( ", /, and ", '/' );
	public  static  void    andListFormat( String s ) { andListFormat = new Strings( s, Config.listSep().charAt( 0 )); }
	public  static  Strings andListFormat() { return andListFormat; }

	private static  Strings orConjunctions = new Strings( ", or" );
	public  static  void    orConjunctions( Strings sa ) { orConjunctions = sa; }
	public  static  Strings orConjunctions() { return orConjunctions; }

	private static  Strings orListFormat = new Strings( ", /, or ", '/' );
	public  static  void    orListFormat( String s ) { orListFormat = new Strings( s, Config.listSep().charAt( 0 )); }
	public  static  Strings orListFormat() { return orListFormat; }

	private static  Strings referencers = new Strings( "the" );
	public  static  void    referencers( Strings sa ) { referencers = sa; }
	public  static  Strings referencers() { return referencers; }

	private static  String  listSep = "/";
	public  static  void    listSep( String s ) { listSep = s; }
	public  static  String  listSep() { return listSep; }

	private static String propagateReplyStr = "say so";
	public  static String propagateReplyStr() {return  propagateReplyStr;}
	public  static void   propagateReplyStr(String pr) {
		propagateReplyStr = pr;
		propagateReplys = new Strings( pr );
	}
	private static Strings propagateReplys = new Strings( propagateReplyStr );
	public  static Strings propagateReplys() {return  propagateReplys;}
	public  static void    propagateReplys(Strings pr) {
		propagateReplys = pr;
		propagateReplyStr = pr.toString();
	}
	
	// to interact with the 'say' list - as String or Strings
	private static String  accumulateCmdStr = "say this now";
	private static Strings accumulateCmds = new Strings( accumulateCmdStr );
	public  static String  accumulateCmdStr() {return accumulateCmdStr;}
	public  static Strings accumulateCmds() {return accumulateCmds;}
	public  static void    accumulateCmdStr(String ac) {
		accumulateCmdStr = ac;
		accumulateCmds = new Strings( ac );
	}
	public  static void    accumulateCmdStr(Strings ac) {
		accumulateCmds = ac;
		accumulateCmdStr = ac.toString();
	}

}
