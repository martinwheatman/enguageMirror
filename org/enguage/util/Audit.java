package org.enguage.util;

import java.util.GregorianCalendar;

public class Audit {
	private              String     name = "";
	static private       Strings   stack = new Strings( "zeroStack" );

	// global DEBUG switches...
	static       public  boolean  startupDebug = false;
	static final public  boolean  numericDebug = false;
	static       public  boolean       timings = false;
	static       public  boolean  runtimeDebug = false;
    static       public  boolean    detailedOn = false;
                 public  boolean detailedRegis = false;
	
    static       public  boolean    allTracing = false;
    static       public  void         traceAll( boolean b ) { allTracing = b; }
    
	private static int     suspended = 0; 
	public  static void    suspend() { suspended++; }
	public  static void    resume() {  if (suspended()) suspended--;  }
	public  static boolean suspended() { return suspended > 0; }
	
	private static boolean allOn = false;
	public  static void    allOff() { allOn = false; allTracing = false; indent.reset(); }
	public  static void    allOn() {  allOn = true; allTracing = true; }
	public  static boolean allAreOn() { return allOn; }
	
	private boolean auditOn = false;
	public  void    off() { auditOn = false; }
	public  void    on() {  auditOn = true; }
	public  void    on(boolean b) {  auditOn = b; }
	public  boolean isOn() { return auditOn; }
	
	private static Indent indent = new Indent();
	public  static void   incr() { indent.incr(); }
	public  static void   decr() { indent.decr(); }

	private static long then = new GregorianCalendar().getTimeInMillis();
	public  static long interval() {
		long now = new GregorianCalendar().getTimeInMillis();
		long rc = now - then;
		then = now;
		return rc;
	}
	
	private String capitalize(final String line) {
	   return Character.toUpperCase(line.charAt(0)) + line.substring(1);
	}
	
	public Audit( String nm ) { name = capitalize( nm ); }
	public Audit( String nm, boolean t ) { this( nm ); tracing = t; }
	public Audit( String nm, boolean t, boolean d ) { this( nm ); tracing = t; auditOn = d;}
	public Audit( String nm, boolean t, boolean d, boolean detail ) { this( nm ); tracing = t; auditOn = d; detailedRegis = detail;}
	
	public void   FATAL( String msg ) { LOG( "FATAL: "+ name +": "+ msg ); System.exit( 1 ); }
	public void   FATAL( String phrase, String msg ) { FATAL( phrase +": "+ msg ); }
	public void   ERROR( String info ) { System.err.println( "ERROR: " + name +"."+ stack.get( 0 ) +"(): "+ info);}
	public int    log( int    info ) { log( ""+ info ); return info; }
	public String LOG( String info ) { // ignores suspended value
		indent.print( System.out );
		System.out.println( info + (timings ? " -- "+interval()+"ms" : ""));
		return info;
	}
	public String log( String info ) {
		if (!suspended()) LOG( info );
		return info;
	}
	public  void   detail( String info ) { if (detailedOn && detailedRegis) log( info ); }
	public  void   debug( String info ) { if (auditOn || allOn) log( info ); }
	public  Object  info(  String fn, String in, Object out ) { // out may be null!
		if ((auditOn || allOn) && (out!=null && !out.equals("")))
			log( name +"."+ fn +"( "+ in +" ) => "+ out.toString() );
		return out;
	}
	
	public  boolean  tracing = Audit.allTracing;
    public  void       trace( boolean b ) { tracing = b ? true : Audit.allTracing; }

    public  void in( String fn ) { in( fn, "" );}
    public  void in( String fn, String info ) {if (tracing || allTracing) IN( fn, info );}
    public  void IN( String fn, String info ) {
		// sometimes this is tested at call time - preventing the string processing
		// in the traceIn() call being performed at runtime.
		stack.prepend( fn );
		log( "IN  "+ name +"."+ fn +"("+ (info==null?"":" "+ info +" ") +")");
		indent.incr();
	}

    public String OUT( String result ) {
    	indent.decr();
		log( "OUT "+ name +"."
				+ stack.get( 0 )
				+ (result==null ? "" : " => "+ result)
			);
		if (stack.size() > 0) stack.remove( 0 );
		return result;
    }
	public String out( String result ) {
		return (tracing || allTracing) ? OUT( result ) : result;	
	}
	public void    out() { out( (String)null ); }
	public Strings out( Strings s ) { out( s!=null?"["+s.toString(Strings.DQCSV)+"]":"<null>"); return s; }
	public boolean out( boolean b ) { out( Boolean.toString( b )); return b; }
	public int     out( int     n ) { out( Integer.toString( n )); return n; }
	public Float   out( Float   f ) { out( Float.toString(   f )); return f; }
	public long    out( long    l ) { out( Long.toString(    l )); return l; }
	public Object  out( Object  o ) { out( o==null ? "null" : o.toString()); return o;}
	
	public void title( String title ) {
		String underline = "";
		log( "\n" );
		log( title );
		for (int i = 0; i < title.length(); i++) underline += "=";
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
