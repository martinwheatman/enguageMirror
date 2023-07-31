package org.enguage.util.http;

import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class Html {
	public  static final int      ID = 154478; // "html"
	private static final String NAME = "Html";
	private static final Audit audit = new Audit( NAME );

	public static Strings interpret( Strings args ) {
		audit.IN( "interpret", "args="+ args );
		Strings rc = new Strings( "sorry" ); // Shell.Fail;
		String cmd = args.remove( 0 );

		if (cmd.equals( "find" )) {
			String name = args.remove( 0 );
			String tag  = args.remove( 0 );
			String value = args.remove( 0 );
			
			Audit.log( "ok, cmd=find, name="+ name +", tag="+ tag +", value="+ value );
			
			rc = new Strings( "ok" );
		}
		
		audit.OUT( rc );
		return rc;
	}
}
