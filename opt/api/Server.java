package opt.api;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

import org.enguage.Enguage;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

import opt.api.utils.RequestHandler;

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
	static public  void   root( String r ) {
		if (!r.contains( ".." ))
			new File( root = r ).mkdirs();
	}
	
	static public void server( int port ) {

		Enguage.set( new Enguage() );

		try (ServerSocket server = new ServerSocket( port, 5 )) {	
			Audit.log(
				"Server listening on port: "
				+ port
				+(root.equals("") ?	"" : " (root="+root()+")")
			);
			while (true)
				new RequestHandler( server.accept() ).run();

		} catch (IOException e) {
			e.printStackTrace();
	}	}
	
	public static void main( String arg[]) {
		Strings args = new Strings( arg );

		for (String argc : args)
			if (!port( argc )) root( argc );

		server( port() );
}	}
