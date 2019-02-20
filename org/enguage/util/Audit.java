package org.enguage.util;

import java.util.GregorianCalendar;

public class Audit {
	private              String     name = "";
	static private       Strings   stack = new Strings( "zeroStack" );

	// === global DEBUG switches...
	static       public  boolean  startupDebug = false;
	static final public  boolean  numericDebug = false;
	static       public  boolean       timings = false;
	static       public  boolean  runtimeDebug = false;
    static       public  boolean    detailedOn = false;
                 public  boolean detailedRegis = false;
	
 	// === indent
 	static private Indent indent = new Indent();
 	static public  void   incr() { indent.incr(); }
 	static public  void   decr() { indent.decr(); }

	// === timestamp
	static private long then = new GregorianCalendar().getTimeInMillis();
	static public  long interval() {
		long now = new GregorianCalendar().getTimeInMillis();
		long rc = now - then;
		then = now;
		return rc;
	}
	
	// test count

	private int  numberOfTests = 0;
	public  void passed() { numberOfTests++;}
	public  void passed( String msg ) { log( msg ); passed(); }
	public  void PASSED() {log( "+++ PASSED "+ numberOfTests +" tests in "+ interval()+"ms +++" );}

	// === debug and detail - "auditing"
	static private int     suspended = 0; 
	static public  void    suspend() { suspended++; }
	static public  void    resume() {  if (suspended()) suspended--;  }
	static public  boolean suspended() { return suspended > 0; }
	
	private boolean auditOn = false;
	public  void    off() { auditOn = false; }
	public  void    on() {  auditOn = true; }
	public  void    on(boolean b) {  auditOn = b; }
	public  boolean isOn() { return auditOn; }
	
	public Audit( String nm ) { name = Character.toUpperCase( nm.charAt(0)) + nm.substring(1); }
	public Audit( String nm, boolean t ) { this( nm ); tracing = t; }
	public Audit( String nm, boolean t, boolean d ) { this( nm ); tracing = t; auditOn = d;}
	public Audit( String nm, boolean t, boolean d, boolean detail ) { this( nm ); tracing = t; auditOn = d; detailedRegis = detail;}
	
	public void   FATAL( String msg ) { LOG( "FATAL: "+ name +": "+ msg ); System.exit( 1 ); }
	public void   FATAL( String phrase, String msg ) { FATAL( phrase +": "+ msg ); }
	public void   ERROR( String info ) {
		System.err.println(
			"ERROR: "+ name +(stack.size()>1?"."+ stack.get( 0 ) +"()" : "")+": "+ info
		);
		System.err.flush();
	}
	public int    log( int    info ) { log( ""+ info ); return info; }
	public String LOG( String info ) { // ignores suspended value
		indent.print( System.out );
		System.out.println( info + (timings ? " -- "+interval()+"ms" : ""));
		System.out.flush();
		return info;
	}
	public String log( Strings info ) { return log( info.toString()); }

	public String log( String info ) {
		if (!suspended()) LOG( info );
		return info;
	}
	public  void   detail( String info ) { if (detailedOn && detailedRegis) log( info ); }
	public  void   debug( String info ) { if (auditOn != allOn) log( info ); }
	public  Object  info(  String fn, String in, Object out ) { // out may be null!
		if ((auditOn != allOn) && (out!=null && !out.equals("")))
			log( name +"."+ fn +"( "+ in +" ) => "+ out.toString() );
		return out;
	}
	
	// === tracing
    static       public  boolean    allTracing = false;
    static       public  void         traceAll( boolean b ) { allTracing = b; }
    
	public  boolean  tracing = Audit.allTracing;
    public  void       trace( boolean b ) { tracing = b ? true : Audit.allTracing; }

    public  void in( String fn ) { in( fn, "" );}
    public  void in( String fn, String info ) {if (tracing != allTracing) IN( fn, info );}
    public  void IN( String fn, String info ) {
		// sometimes this is tested at call time - preventing the string processing
		// in the traceIn() call being performed at runtime.
		stack.prepend( fn );
		log( "IN  "+ name +"."+ fn +"("+ (info==null?"":" "+ info +" ") +")");
		indent.incr();
	}
	public void OUT() {OUT( (String)null );}
	public String OUT( String result ) {
		indent.decr();
		log( "OUT "+ name
				+ (stack.size()>1?"."+ stack.get( 0 ) +"()" : "")
				+ (result==null ? "" : " => "+ result)
			);
		if (stack.size() > 1) stack.remove( 0 );
		return result;
    }
	public String out( String result ) {
		return (tracing != allTracing) ? OUT( result ) : result;	
	}
	public void    out() { out( (String)null ); }
	public Strings out( Strings s ) { out( s!=null?"["+s.toString(Strings.DQCSV)+"]":"<null>"); return s; }
	public boolean out( boolean b ) { out( Boolean.toString( b )); return b; }
	public int     out( int     n ) { out( Integer.toString( n )); return n; }
	public Float   out( Float   f ) { out( Float.toString(   f )); return f; }
	public Float   OUT( Float   f ) { OUT( Float.toString(   f )); return f; }
	public long    out( long    l ) { out( Long.toString(    l )); return l; }
	public Object  out( Object  o ) { out( o==null ? "null" : o.toString()); return o;}
	public Object  OUT( Object  o ) { OUT( o==null ? "null" : o.toString()); return o;}
	
	// === allOn - tracing AND debug
	// allOn vs. auditOn - turning auditOn when allOn, suppresses for this level
	static private boolean allOn = false;
	static public  void    allOff() { allOn = false; allTracing = false; indent.reset(); }
	static public  void    allOn() {  allOn = true; allTracing = true; }
	static public  boolean allAreOn() { return allOn; }
	
	// === title/underline
	public void title( String title ) { log( "\n" ); underline( title, '=' );}
	public void subtl( String title ) { log( "" ); underline( title, '+' );}
	public void underline( String title ) { underline( title, '-' );}
	public void underline( String title, char ch ) {
		log( title );
		String underline = "";
		for (int i = 0; i < title.length(); i++) underline += ch;
		log( underline );
	}

	// -- test code...
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
