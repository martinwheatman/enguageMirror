package org.enguage.signs.objects.space;

import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.sys.Shell;

public class Transaction {
	
	private Transaction() {}
	
	public  static final String    NAME = "transaction";
	public  static final int         ID = 245880623; //Strings.hash( NAME )
	private static      Audit    audit = new Audit( NAME );
	private static      boolean inprog = false;
	
	private static void    create() {
		if (!inprog) {
			inprog = true;
			Overlay.startTxn();
	}	}
	private static void    abort() {
		if (inprog) {
			inprog = false;
			Overlay.remove();
	}	}
	private static void    commit() {
		if (inprog) {
			inprog = false;
			Overlay.compact();
	}	}
	
	public static Strings interpret( Strings args ) {
		audit.in( "interpret", args.toString() );
		String rc = Shell.SUCCESS;
		String cmd = args.remove(0);
		if (cmd.equals( "create" ))
			create();
		else if (cmd.equals( "abort" )) {
			abort();
			rc = Shell.FAIL;// propagate the failure
		} else if (cmd.equals( "commit" ))
			commit();
		else
			rc = Shell.FAIL;
		return audit.out( new Strings( rc ));
}	}