package org.enguage.util.http;

import java.io.File;
import java.io.IOException;

import org.enguage.Enguage;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;
import org.enguage.util.sys.Fs;
import org.enguage.util.sys.Shell;

public class Wikipedia {
	
	public  static final int      ID = 277584273;
	private static final Audit audit = new Audit("Wikipedia");
	
	private static String cache = Enguage.RW_SPACE+"wiki"+File.separator;
	private static String qbase = "https://en.wikipedia.org/w/index.php?title=";

	public static Strings interpret( Strings cmds ) {
		audit.in( "interprtet", "cmds="+ cmds.toString(Strings.DQCSV) );
		Strings rc = Shell.Fail;
		String cmd = cmds.remove(0);
		
		if (cmd.equals( "query" )) {
			String query = cmds.normalise().capitalise().toString( Strings.UNDERSC );
			
			if (new File( cache+query ).exists()) {
				audit.debug( "file found: "+ cache+query );
				rc = new Strings().append( cache+query );

			} else {
				String url=qbase+query;
				try (Http http = new Http( url )) {
					
					audit.debug( "creating: "+ cache+query );
					if (http.responseCode() == 200) {
						Fs.stringToFile(
								cache+query,
								http.response()
						);
						rc = new Strings().append( cache+query );
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
