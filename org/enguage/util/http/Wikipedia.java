package org.enguage.util.http;

import java.io.File;
import java.io.IOException;

import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;
import org.enguage.util.sys.Fs;
import org.enguage.util.sys.Shell;

public class Wikipedia {
	
	public  static final int      ID = 277584273;
	private static final Audit audit = new Audit("Wikipedia");
	
	private static String cache = Fs.root()+"wiki"+File.separator;
	private static String url = "https://en.wikipedia.org/w/index.php?title=";
	public  static void   url( String u ) {url=u;}

	public static Strings interpret( Strings cmds ) {
		audit.in( "interprtet", "cmds="+ cmds.toString(Strings.DQCSV) );
		Strings rc = Shell.Fail;
		String cmd = cmds.remove(0);
		
		if (cmd.equals( "query" )) {
			// "nelson mandela" => "Nelson_Mandela"
			String title = cmds.normalise()      // => ["nelson", "mandela"]
					.capitalise()                // => ["Nelson", "Mandela"]
					.toString( Strings.UNDERSC );// => "Nelson_Mandela"
			
			if (new File( cache+title ).exists()) {
				rc = new Strings().append( "\""+ cache + title +"\"" );
				audit.debug( "Found: "+ rc );

			} else {
				try (Http http = new Http( url+title )) {
					
					audit.debug( "Downloading: "+ cache+title );
					if (http.responseCode() == 200) {
						Fs.stringToFile(
								cache+title,
								http.response()
						);
						rc = new Strings().append( "\""+ cache + title +"\"" );
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}
		}	}
		audit.out( rc.toString() );
		return rc;
	}
	public static void main( String[] args ) {
		Strings cmds = new Strings( "query nelson mandela" );
		Audit.log( interpret( cmds ));
}	}
