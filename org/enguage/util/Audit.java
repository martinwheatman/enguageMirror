package org.enguage.util;

import java.io.File;
import java.io.PrintStream;
import java.util.GregorianCalendar;

public class Audit {
	private              String     name = "";
	private static        Strings   stack = new Strings( "zeroStack" );

	// === global DEBUG switches...
	public  static       boolean  startupDebug = false;
	public  static final boolean  numericDebug = false;
	public  static       boolean       timings = false;
	public  static       boolean  runtimeDebug = false;
	public  static       boolean    detailedOn = false;
    public  boolean detailedRegis = false;
	
 	// === indent
 	private static  Indent indent = new Indent();
 	public  static   void   incr() { indent.incr(); }
 	public  static   void   decr() { indent.decr(); }
 	public  static   String indent() { return indent.toString();}
 	
 	// === logfile - write-only
 	private static  String fname = "." + File.separator+"audit.log";
 	public  static   void delete() {
 		File f = new File( fname );
 		if (f.exists()) f.delete();
 	}
 	public  static   void location( String l ) {
 		delete();
 		fname = l + File.separator+"audit.log";
 	}

	// === timestamp
	private static  long then = new GregorianCalendar().getTimeInMillis();
	public  static   long interval() {
		long now = new GregorianCalendar().getTimeInMillis();
		long rc = now - then;
		then = now;
		return rc;
	}
	
	// === debug and detail - "auditing"
	private static  int     suspended = 0; 
	public  static  void    suspend() { suspended++; }
	public  static  void    resume() {  if (suspended()) suspended--;  }
	public  static  boolean suspended() { return suspended > 0; }
	
	public Audit( String nm ) { name = Character.toUpperCase( nm.charAt(0)) + nm.substring(1); }
	public Audit( String nm, boolean t ) { this( nm ); tracing = t; }
	public Audit( String nm, boolean t, boolean d ) { this( nm ); tracing = t; auditOn = d;}
	public Audit( String nm, boolean t, boolean d, boolean detail ) { this( nm ); tracing = t; auditOn = d; detailedRegis = detail;}
	
	private static  String LOG( PrintStream ps, String info ) { // ignores suspended value
		indent.print( ps );
		ps.append( info + (timings ? " -- "+interval()+"ms" : "") +"\n" );
		return info;
	}
	public  static  String log( String info ) {
		if (!suspended()) {
			LOG( System.out, info );
		}
		return info;
	}
	public  static  int    log( int     info ) { log( ""+ info ); return info; }
	public  static  String LOG( String  info ) { return LOG( System.out, info );}
	public  static  String log( Strings info ) { return log( info.toString()); }
	
	// test count
	private int  numberOfTests = 0;
	public  int  numberOfTests() { return numberOfTests; }
	public  void passed() { numberOfTests++;}
	public  void passed( String msg ) { log( msg ); passed(); }
	public  void PASSED() {log( "+++ PASSED "+ numberOfTests +" tests in "+ interval()+"ms +++" );}
	
	private boolean auditOn = false;
	public  void    off() { auditOn = false; }
	public  void    on() {  auditOn = true; }
	public  void    on(boolean b) {  auditOn = b; }
	public  boolean isOn() { return auditOn; }
	
	public void   FATAL( String msg ) { LOG( "FATAL: "+ name +": "+ msg ); System.exit( 1 ); }
	public void   FATAL( String phrase, String msg ) { FATAL( phrase +": "+ msg ); }
	public void   ERROR( String info ) {
		System.err.println(
			"ERROR: "+ name +(stack.size()>1?"."+ stack.get( 0 ) +"()" : "")+": "+ info
		);
		System.err.flush();
	}
	public  void   detail( String info ) { if (detailedOn && detailedRegis) log( info ); }
	public  void   debug( String info ) { if (auditOn != allOn) log( info ); }
	public  Object  info(  String fn, String in, Object out ) { // out may be null!
		if ((auditOn != allOn) && (out!=null && !out.equals("")))
			log( name +"."+ fn +"( "+ in +" ) => "+ out.toString() );
		return out;
	}
	
	// === tracing
	public  static  boolean    allTracing = false;
	public  static  void         traceAll( boolean b ) { allTracing = b; }
    
	public  boolean  tracing = Audit.allTracing;
    public  void       trace( boolean b ) { tracing = b ? true : Audit.allTracing; }

    public  void in( String fn ) { in( fn, "" );}
    public  void in( String fn, String info ) {if (tracing != allTracing) IN( fn, info );}
    public  void IN( String fn, String info ) {
		// sometimes this is tested at call time - preventing the string processing
		// in the traceIn() call being performed at runtime.
		stack.prepend( fn );
		LOG( "IN  "+ name +"."+ fn +"("+ (info==null?"":" "+ info +" ") +")");
		indent.incr();
	}

    public String OUT() { return OUT( "" ); }
    public String OUT( String result ) {
    	indent.decr();
		LOG( "OUT "+ name
				+ (stack.size()>1?"."+ stack.get( 0 ) +"()" : "")
				+ (result==null || result.contentEquals("") ? "" : " => "+ result)
			);
		if (stack.size() > 1) stack.remove( 0 );
		return result;
    }
	public String out( String result ) {return (tracing != allTracing) ? OUT( result ) : result;}
	public void    out() { out( (String)null ); }
	public Strings out( Strings s ) { out( s!=null?"["+s.toString(Strings.DQCSV)+"]":"<null>"); return s; }
	public boolean out( boolean b ) { out( Boolean.toString( b )); return b; }
	public int     out( int     n ) { out( Integer.toString( n )); return n; }
	public Float   out( Float   f ) { out( Float.toString(   f )); return f; }
	public Float   OUT( Float   f ) { OUT( Float.toString(   f )); return f; }
	public long    out( long    l ) { out( Long.toString(    l )); return l; }
	public Object  out( Object  o ) { out( o==null ? "null" : o.toString()); return o;}
	public Object  OUT( Object  o ) { OUT( o==null ? "null" : o.toString()); return o;}
	public Strings OUT( Strings o ) { OUT( o==null ? "null" : o.toString()); return o;}
	public boolean OUT( boolean b ) { OUT( Boolean.toString( b )); return b; }
	
	// === allOn - tracing AND debug
	// allOn vs. auditOn - turning auditOn when allOn, suppresses for this level
	private static  boolean allOn = false;
	public  static  void    allOff() { allOn = false; allTracing = false; indent.reset(); }
	public  static  void    allOn() {  allOn = true; allTracing = true; }
	public  static  boolean allAreOn() { return allOn; }
	
	// === title/underline
	public void title( String title ) { log( "\n" ); underline( title, '=' );}
	public void subtl( String title ) { log( "" ); underline( title, '+' );}
	public void underline( String title ) { underline( title, '-' );}
	public void underline( String title, char ch ) {
		log( title );
		StringBuilder underline = new StringBuilder();
		for (int i = 0; i < title.length(); i++) underline.append( ch );
		log( underline.toString() );
	}

	public static void main( String[] agrs ) {
		Audit audit = new Audit( "Audit" ); // <= needs setting as $DEBUG to test
		Audit.allOn();
		audit.tracing = true;
		audit.in( "main", "this='is', a='test'" );
		audit.in( "inn", "this='is', again='aTest'" );
		audit.in( "inner", "this='is', again='aTest'" );
		audit.debug( "Hello, martin" );
		audit.out();
		audit.out();
		audit.out("passed");
}	}
