package opt.web;

import java.io.IOException;
import java.net.ServerSocket;

import org.enguage.Enguage;
import org.enguage.util.Audit;
import org.enguage.util.Strings;

public class Server {
	
	static private int     port = 8080;
	static public  int     port() {return port;}
	static public  boolean port( String p ) {
		try{ port = Integer.valueOf( p );
			 return true;
		} catch (NumberFormatException ignore) {
			 return false;
	}	}
	
	static private String root = "";
	static public  String root() {return root;}
	static public  void   root( String r ) {root = r;}
	
	static public void server( int port ) {

		Enguage.init( Enguage.RW_SPACE );

		try (ServerSocket server = new ServerSocket( port, 5 )) {	
			Audit.LOG( "Server listening on port: "+ port );
			while (true)
				new Request( server.accept() ).run();
		} catch (IOException e) {
			e.printStackTrace();
	}	}
	
	public static void main( String arg[]) {
		Strings args = new Strings( arg );

		for (String argc : args)
			if (!port( argc )) root( argc );

		server( port() );
}	}
