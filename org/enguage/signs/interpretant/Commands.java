package org.enguage.signs.interpretant;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.enguage.signs.objects.Variable;
import org.enguage.signs.vehicle.reply.Reply;
import org.enguage.signs.vehicle.reply.Response;
import org.enguage.signs.vehicle.when.When;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Context;

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
	
	private String deconceptualise( String rawAns ) {
		//audit.IN( "decon", rawAns );
		String whn = rawAns.replace( " ", "" );
		//Audit.log( ">"+ whn +"<"+ new When( whn ).toString() +">"+ When.valid( whn ));
	 	String rc = When.valid( whn ) ?				 // 88888888198888 -> 7pm
				new When( whn ).toString()
						: rawAns;					  // chs
	 	return rc; //audit.OUT( rc );
	}

	private Reply run( String cmdline ) {
		Reply r = new Reply();
		int rc = 255;
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
								
				rc = p.waitFor() == 0 ? Response.OK : Response.FAIL;
				result = deconceptualise( (rc==Response.OK? result : errTxt ));

			} catch (Exception e) {
				audit.ERROR( "exception: "+ e.toString());
				e.printStackTrace();
				throw new Exception();
			}
		} catch (Exception iox) {
			rc = Response.FAIL;
			result = "I can't run this command: "+ cmdline;
		}
		r.answer( result );
		r.format( result );
		r.response( rc );
		return r; //(Reply) audit.OUT( r );
	}

	public Reply run( Reply r ) {
		audit.in( "run", "value='"+ cmd +"', ["+ Context.valueOf() +"]" );
		String cmdline = stringToCommand( r.a.toString());
		//Audit.log( "Running: "+ cmdline );
		r = run( cmdline );
		//Audit.log( " return: "+ r.a.toString() +"(rc="+ (r.type()==Reply.YES? 0 : -1) +")" );
		return (Reply) audit.out( r );
	}
	// ---
	public static void main( String args []) {
		Response.failure( "sorry" );
		Response.success( "ok" );

		Reply r = new Reply();
		r = new Commands( "value -D selftest martin/engine/capacity 1598cc" ).run( r );
		r = new Commands( "value -D selftest martin/engine/capacity"        ).run( r );
		Audit.log( r.toString());
}	}
