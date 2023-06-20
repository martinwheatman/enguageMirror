package opt.test;

import org.enguage.Enguage;
import org.enguage.util.Audit;
import org.enguage.util.Strings;

public class Example {
	public static void main( String[] args ) {
		String fsys = Enguage.RW_SPACE;
		Enguage.set( new Enguage( fsys ));
		
		Strings cmd = new Strings( args );
		if (args.length == 0)
			cmd = new Strings( "I need a coffee" );
		
		Strings reply = Enguage.get().mediate( new Strings( cmd ));
		Audit.LOG( reply.toString() );
	}
}
