package org.enguage.util;

import java.util.GregorianCalendar;

import org.enguage.signs.symbol.pattern.Frags;
import org.enguage.signs.symbol.reply.Response;

public class Audit {
	public  static final int            ID = 829030;
	private static       Strings funcNames = new Strings( "zeroStack" );

	public Audit( String nm ) {className = Character.toUpperCase( nm.charAt(0)) + nm.substring(1);}
	
	private              String   className = "";

	// === global DEBUG switches...
	public  static final boolean  numericDebug = false;
	
    // === indent
 	private static  Indentation indent = new Indentation();
 	public  static  void   incr() {indent.incr();}
 	public  static  void   decr() {indent.decr();}
 	public  static  String indent() {return indent.toString();}
 	
	// === timestamp
	private static  long then = new GregorianCalendar().getTimeInMillis();
	public  static  long interval() {
		long now = new GregorianCalendar().getTimeInMillis();
		long rc = now - then;
		then = now;
		return rc;
	}
	
	// === debug and detail - "auditing"
	private static  int     suspended = 1; // to audit we need to resume
	public  static  void    suspend() {suspended++;}
	public  static  void    resume() {if (suspended > 0) suspended--;}
	public  static  boolean suspended() {return suspended>0;}
	
	// test count
	private int  numberOfTests = 0;
	public  int  numberOfTests() {return numberOfTests;}
	public  void passed() {numberOfTests++;}
	public  void passed( String msg ) {log( msg ); passed();}
	public  void PASSED() {log( "+++ PASSED "+ numberOfTests +" tests in "+ interval()+"ms +++" );}
	
	private static  boolean auditOn = false;
	public  static  void    off() {auditOn = false;}
	public  static  void    on() { auditOn = true;}
	public  static  void    on(boolean b) { auditOn = b;}
	public  static  boolean isOn() {return auditOn;}
	
	// === allOn - tracing AND debug
	// allOn vs. auditOn - turning auditOn when allOn, suppresses for this level
	//private static  boolean allOn = false;
//	public  static  void    allOff() {auditOn = false; indent.reset();}
//	public  static  void    allOn() { auditOn = true;}
	public  static  boolean allAreOn() {return auditOn;}
	
    // LOGGING:-
	public static  String LOG( String info ) {
		indent.print( System.out );
		System.out.println( info );
		return info;
	}
	public static  String timestamp( String info ) {
		LOG( info + " -- "+interval()+"ms\n" );
		return info;
	}
	public  static  String log( String info ) {
		if (suspended==0)
			LOG( info );
		return info;
	}
	public  static  int    log( int     info ) {log( ""+ info ); return info;}
	public  static  String log( Strings info ) {return log( info.toString() );}
	
	// Ancilliary methods...
    public  void in( String fn ) {in( fn, "" );}
    public  void in( String fn, String info ) {if (auditOn) IN( fn, info );}
    public  void IN( String fn, String info ) {
		// sometimes this is tested at call time - preventing the string processing
		// in the traceIn() call being performed at runtime.
		funcNames.prepend( fn );
		LOG( "IN  "+ className +"."+ fn +"("+ (info==null?"":" "+ info +" ") +")");
		indent.incr();
	}

    public String OUT() {return OUT( "" );}
    public String OUT( String result ) {
    	indent.decr();
		LOG( "OUT "+ className
				+ (funcNames.size()>1?"."+ funcNames.remove( 0 ) +"()" : "")
				+ (result==null || result.contentEquals("") ? "" : " => "+ result)
			);
		return result;
    }
	public String  out( String result ) {return (auditOn) ? OUT( result ) : result;}
	public void    out() {out( (String)null );}
	public Strings out( Strings s ) {out( s!=null?"["+s.toString(Strings.DQCSV)+"]":"<null>"); return s;}
	public boolean out( boolean b ) {out( Boolean.toString( b )); return b;}
	public int     out( int     n ) {out( Integer.toString( n )); return n;}
	public Float   out( Float   f ) {out( Float.toString(   f )); return f;}
	public Float   OUT( Float   f ) {OUT( Float.toString(   f )); return f;}
	public long    out( long    l ) {out( Long.toString(    l )); return l;}
	public Object  out( Object  o ) {out( o==null ? "null" : o.toString()); return o;}
	public Object  OUT( Object  o ) {OUT( o==null ? "null" : o.toString()); return o;}
	public Strings OUT( Strings o ) {OUT( o==null ? "null" : o.toString()); return o;}
	public boolean OUT( boolean b ) {OUT( Boolean.toString( b )); return b;}
	
	public  void   debug( String info ) {if (auditOn) log( info );}
	public  Object  info(  String fn, String in, Object out ) {// out may be null!
		if (auditOn && (out!=null && !out.equals("")))
			log( className +"."+ fn +"( "+ in +" ) => "+ out.toString() );
		return out;
	}
	public  void   FATAL( String msg ) {LOG( "FATAL: "+ className +": "+ msg ); System.exit( 1 );}
	public  void   FATAL( String phrase, String msg ) {FATAL( phrase +": "+ msg );}
	public  void   error( String info ) {
		System.out.println(
			"ERROR: "+ className +(funcNames.size()>1?"."+ funcNames.get( 0 ) +"()" : "")+": "+ info
		);
	}
	public static Strings interpret(Strings cmds) {
		Strings rc = Response.success();
		String cmd = cmds.remove( 0 );
		Audit.log( cmd +" "+ cmds.toString());
		if (cmd.equals( "timing" ) || cmd.equals( "tracing" )) {
			if (cmds.get( 0 ).equals("off")) {
				Audit.off();
			} else {
				Audit.on();
			}
						
		} else if (cmd.equals( "detailed" )) {
			
			if (cmds.get( 0 ).equals("off")) {
				Audit.off();
			} else {
				Audit.on();
			}
		
		} else if (cmd.equals( "debug" )) {
			
			if (cmds.get( 0 ).equals( "off" )) {
				Audit.off();
				
			} else if (cmds.size() > 1 && cmds.get( 1 ).equals( "tags" )) {
				Frags.debug( !Frags.debug() );
				
			} else {
				Audit.on();
			}
		} else
			rc = Response.failure();

		return rc;
	}
	
	// === title/underline
	public void title( String title ) {log( "\n" ); underline( title, '=' );}
	public void subtl( String title ) {log( "" ); underline( title, '+' );}
	public void underline( String title ) {underline( title, '-' );}
	public void underline( String title, char ch ) {
		LOG( title );
		StringBuilder underline = new StringBuilder();
		for (int i = 0; i < title.length(); i++) underline.append( ch );
		LOG( underline.toString() );
	}

	public static void main( String[] args ) {
		Audit audit = new Audit( "Audit" ); // <= needs setting as $DEBUG to test
		Audit.on();
		audit.in( "main", "this='is', a='test'" );
		audit.in( "inn", "this='is', again='aTest'" );
		audit.in( "inner", "this='is', again='aTest'" );
		audit.debug( "Hello, martin" );
		audit.out();
		audit.out();
		audit.out( "passed" );
}	}
