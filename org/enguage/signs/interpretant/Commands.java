package org.enguage.signs.interpretant;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.enguage.signs.objects.Variable;
import org.enguage.signs.symbol.reply.Reply;
import org.enguage.signs.symbol.reply.Response;
import org.enguage.signs.symbol.when.When;
import org.enguage.util.Audit;
import org.enguage.util.Strings;

public class Commands {
	static private Audit audit = new Audit( "commands" );
	
	public Commands (String command) { cmd = command; }

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
		//audit.IN( "runresult", "rc="+ rc +", result="+ result +", error="+ errtxt );
		Reply r = new Reply();
		
		rc = (rc == 0) ? Response.OK 
				: Response.FAIL;
		
		String whn = result.replace( " ", "" );
		result = When.valid( whn ) ?				 // 88888888198888 -> 7pm
				new When( whn ).toString()
				: rc == Response.DNK ?
						Response.dnkStr()
						: result;					  // chs
	 	//Audit.LOG( "result="+ result +", rc="+ rc );
		r.answer( result );
		r.format( result );
		r.response( rc );
		//audit.OUT( r );
	 	return r;
	}
	
	public Reply run( String s ) {
		String cmdline = stringToCommand( s );
		Reply r = new Reply();
		Process p;
		String result = "";
		String errTxt = "";
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
					result += line;
				
				if (result.equals( "" ))
					while ((line = error.readLine()) != null)
						errTxt += line;
				
				r = runResult( p.waitFor(), result, errTxt );
				
			} catch (Exception e) {
				r = runResult( 255, "", "Command failed: "+ cmdline );
			}
		} catch (Exception iox) {
			r = runResult( 255, "", "I can't run: "+ cmdline );
		}
		return r;
	}

	// ---
	public static void main( String args []) {
		Response.failure( "sorry" );
		Response.success( "ok" );

		Reply r = new Reply();
		r = new Commands( "value -D selftest martin/engine/capacity 1598cc" ).run( r.a.toString());
		r = new Commands( "value -D selftest martin/engine/capacity"        ).run( r.a.toString());
		Audit.log( r.toString());
}	}
