package com.yagadi.enguage.interpretant;

import java.io.InputStream;
import java.io.PrintWriter;

import com.yagadi.enguage.object.Variable;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;
import com.yagadi.enguage.vehicle.Context;
import com.yagadi.enguage.vehicle.Reply;
import com.yagadi.enguage.vehicle.when.Moment;
import com.yagadi.enguage.vehicle.when.When;

public class Proc {
	static private Audit audit = new Audit( "run" );
	
	Proc (String command) { cmd = command; }

	private String cmd = "";
	
	static private String classpath = "";
	static public  String classpath() { return classpath; }
	static public  void   classpath(String cp) { classpath = cp; }
	
	static private String java = "";
	static public  String java() { return java; }
	static public  void   java(String cp) { java = cp; }
	
	static private String shell = "/bin/bash";
	static public  void   shell( String sh ) { shell = sh; }
	static public  String shell() { return shell; }
	
	private String conceptualise( String runningAns ) {
		return Variable.deref( Strings.getStrings( cmd ))
				.replace( new Strings( "SOFA" ), new Strings( java() ))
				.replace( Strings.ellipsis, runningAns )
				.toString();
	}
	private String deconceptualise( String rawAns ) {
	 	return Moment.valid( rawAns ) ?                 // 88888888198888 -> 7pm
			new When( rawAns ).rep( Reply.dnk() ).toString()
			: rawAns.equals( "" ) ? // silence is golden :-)
				Reply.success() // Reply.dnk() ?
				: rawAns.equals( Shell.FAIL ) ?        // FALSE
					Reply.no()                     //   --> no
					: rawAns.equals( Shell.SUCCESS ) ? // TRUE
						Reply.success()            //   --> ok
						: rawAns;                      // chs
	}
	private Process run( String cmdline ) throws Exception {
		Process p = Runtime.getRuntime().exec( shell() );
		PrintWriter stdin = new PrintWriter(p.getOutputStream());
		if (!classpath.equals( "" ))
			stdin.println( "export CLASSPATH="+ classpath );
		stdin.println( cmdline );
		stdin.close();
		return p;
	}
	private String readStream( InputStream in ) throws Exception {
		String s = "";
		byte[] buffer = new byte[1024]; // final?
		int rd;
		while ((rd = in.read(buffer)) != -1) {
			byte[] tmp = new byte[ rd ]; // Can this be done
			for (int j=0; j<rd; j++)     // more efficiently?
				tmp[ j ] = buffer[ j ];  // e.g. buffer[ 0..rd ]
			s += new String( tmp );
		}
		return s;
	}
	public Reply run( Reply r ) {
		audit.in( "run", "value='"+ cmd +"', ["+ Context.valueOf() +"]" );
		String ans = "", log;
		String cmdline = conceptualise( r.a.toString());
		int rc = 0;
		try {
			Process p = run( cmdline );
			ans = readStream( p.getInputStream());
			log = readStream( p.getErrorStream());
			if (!log.equals( "" ))
				audit.ERROR( "Proc.run(): "+ log );
			rc = p.waitFor();
		} catch (Exception e) {
			audit.ERROR( "exception: "+ e.toString());
			e.printStackTrace();
			rc = -1;
		}
		
		ans = rc == 0 ? deconceptualise( ans )
			: Reply.failure() +" , "+ cmdline +" , has failed with a return code of "+ rc;

		return (Reply) audit.out( r.answer( ans ));
	}
	public static void main( String args []) {
		Reply.failure( "sorry" );
		Proc.classpath( "/home/martin/ws/Enguage/bin" );
		Proc.java( "java com.yagadi.enguage.Enguage" );

		Reply r = new Reply();

		r = new Proc( "pwd" ).run( r );
		audit.log( r.toString());

		r = new Proc( "SOFA run" ).run( r );
		audit.log( r.toString());
}	}