package org.enguage.sign.interpretant;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.enguage.sign.Assets;
import org.enguage.sign.object.Variable;
import org.enguage.sign.symbol.reply.Reply;
import org.enguage.sign.symbol.reply.Response;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class Commands {
	private static Audit audit = new Audit( "Commands" );
	
	public Commands () {}

	private String   command = "";
	public  Commands command( String c ) {
		command = Variable.deref( Strings.getStrings( c ))
				.replace( new Strings( "SOFA" ), new Strings( java() ))
				.contract( "/" ) // this gets undone!
				.linuxSwitches()
				.toString();
		return this;
	}
	
	private static String classpath = "";
	public  static String classpath() { return classpath; }
	public  static void   classpath(String cp) { classpath = cp; }
	
	private static String java = "";
	public  static String java() { return java; }
	public  static void   java(String cp) { java = cp; }
	
	private static String shell = "/bin/bash";
	public  static void   shell( String sh ) { shell = sh; }
	public  static String shell() { return shell; }
	
	private Reply runResult( int rc, Strings results ) {
		//audit.in( "runresult", "rc="+ rc +", result=["+ results +"]");
		Reply r = new Reply();
		
		boolean appending = r.answer().isAppending();
		r.answer().appendingIs( false );
	 	for (String result : results)
	 		r.answer( result );
	 	r.answer().appendingIs( appending ); 
	 	/*
	 	 * We have no control over what text the command sends back.
	 	 * A zero result is success.
	 	 * Passing back a non-zero result is a failure.
	 	 * 
	 	 */
	 	r.response( rc == 0 ? Response.N_OK : Response.N_FAIL );
	 	r.format( rc == 0 ? "ok, ..." : "sorry, ..." );
		
		//audit.out( "run result: "+ r );
	 	return r;
	}
	
	public  Commands injectParameter( String runningAns ) {
		command = new Strings( command )
				.replace( Strings.ellipsis, runningAns )
				.replace( "whatever", runningAns )
				.toString();
		return this;
	}
	
	public Reply run() {
		Reply r = new Reply();
		Strings results = new Strings();

		// somehow '/' seem to get separated!!! 
		command = new Strings( command ).contract( "/" ).toString();	
		if (Assets.context() != null && command.startsWith( "sbin/" ))
			command = Assets.path() + command;
		
		audit.debug( "running: "+ command );
		ProcessBuilder pb = new ProcessBuilder( "bash", "-c", command );
		
		try {
			Process p = pb.start();
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
					results.append( line );
				
				if (results.isEmpty())
					while ((line = error.readLine()) != null)
						results.append( line );
				
				r = runResult( p.waitFor(), results );
				
			} catch (Exception e) {
				Strings errString = new Strings();
				errString.add( "Command failed: "+ command );
				r = runResult( 1, errString  );
			}
		} catch (Exception iox) {
			Strings errString = new Strings();
			errString.add( "I can't run: "+ command );
			r = runResult( 1, errString );
		}
		return r;
	}

	// ---
	public static void main( String[] args) {
		Response.failure( "sorry" );
		Response.success( "ok" );

		Reply r = new Reply();
		Audit.log( ">>>"+ r.answer().toString());
		
		r = new Commands().command( "ls" ).injectParameter( "" ).run();
		Audit.log( ">>" + r.toString());
}	}
