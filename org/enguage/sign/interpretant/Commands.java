package org.enguage.sign.interpretant;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.enguage.sign.object.Variable;
import org.enguage.sign.symbol.reply.Reply;
import org.enguage.sign.symbol.reply.Response;
import org.enguage.sign.symbol.when.When;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class Commands {
	private static Audit audit = new Audit( "commands" );
	
	public Commands (String command) { cmd = command; }

	private String cmd = "";
	
	private static String classpath = "";
	public  static String classpath() { return classpath; }
	public  static void   classpath(String cp) { classpath = cp; }
	
	private static String java = "";
	public  static String java() { return java; }
	public  static void   java(String cp) { java = cp; }
	
	private static String shell = "/bin/bash";
	public  static void   shell( String sh ) { shell = sh; }
	public  static String shell() { return shell; }
	
	private String stringToCommand( String runningAns ) {
		return Variable.deref( Strings.getStrings( cmd ))
				.replace( new Strings( "SOFA" ), new Strings( java() ))
				.replace( Strings.ellipsis, runningAns )
				.replace( "whatever", runningAns )
				.contract( "/" )
				.linuxSwitches()
				.toString();
	}
	
	private Reply runResult( int rc, String result, String errtxt ) {
		//audit.IN( "runresult", "rc="+ rc +", result="+ result +", error="+ errtxt )
		Reply r = new Reply();
		
		rc = (rc == 0) ? Response.N_OK 
				: Response.N_FAIL;
		
		String whn = result.replace( " ", "" );
		result = When.valid( whn ) ?				 // 88888888198888 -> 7pm
				new When( whn ).toString()
				: rc == Response.N_DNK ?
						Response.dnkStr()
						: result;					  // chs
	 	audit.debug( "rc="+ rc +", result="+ result +", err="+ errtxt );
		r.answer( result );
		r.format( "answer with no format" );
		r.response( rc );
		audit.debug( "run result: "+ r );
	 	return r;
	}
	
	public Reply run( String s ) {
		String cmdline = stringToCommand( s );
		Reply r = new Reply();
		Process p;
		String result;
		StringBuilder resultSb = new StringBuilder();
		StringBuilder errTxtSb = new StringBuilder();
		audit.debug( "running: "+ cmdline );
		ProcessBuilder pb = new ProcessBuilder( "bash", "-c", cmdline );
		try {
			p = pb.start();
			try (
				BufferedReader reader =
						new BufferedReader(
							new InputStreamReader(
									p.getInputStream()
						)	);
				BufferedReader  error =
						new BufferedReader(
								new InputStreamReader(
										p.getErrorStream()
						)		);
			) {
		
				String line;
				while ((line = reader.readLine()) != null)
					resultSb.append( line );
				result = resultSb.toString();
				
				if (result.equals( "" ))
					while ((line = error.readLine()) != null)
						errTxtSb.append( line );
				
				r = runResult( p.waitFor(), result, errTxtSb.toString() );
				
			} catch (Exception e) {
				r = runResult( 255, "", "Command failed: "+ cmdline );
			}
		} catch (Exception iox) {
			r = runResult( 255, "", "I can't run: "+ cmdline );
		}
		return r;
	}

	// ---
	public static void main( String[] args) {
		Response.failure( "sorry" );
		Response.success( "ok" );

		Reply r = new Reply();
		r = new Commands( "value -D selftest martin/engine/capacity 1598cc" ).run( r.answer().toString());
		r = new Commands( "value -D selftest martin/engine/capacity"        ).run( r.answer().toString());
		audit.debug( r.toString());
}	}
