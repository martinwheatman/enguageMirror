package org.enguage.objects.space;

import org.enguage.Enguage;
import org.enguage.interp.intention.Redo;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.sys.Shell;

public class Transaction {
	
	static public final String    NAME = "transaction";
	static public final int         id = 245880623; //Strings.hash( NAME );
	static private      Audit    audit = new Audit( NAME );
	static private      boolean inprog = false;
	
	static private void    create() {
		if (!inprog) {
			inprog = true;
			Enguage.o.startTxn( Redo.undoIsEnabled());
	}	}
	static private void    abort() {
		if (inprog) {
			inprog = false;
			Enguage.o.destroy();
	}	}
	static private void    commit() {
		if (inprog) {
			inprog = false;
			Enguage.o.combineUnderlays();
	}	}
	
	static public Strings interpret( Strings args ) {
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