package opt.api;

import java.io.IOException;
import java.net.ServerSocket;

import org.enguage.Enguage;
import org.enguage.util.audit.Audit;

import opt.api.utils.RequestHandler;

public class Server {
	
	static private int port = 8080;
	static public  int port() { return port; }	
	
	static public void server( int port ) {
		try (ServerSocket server = new ServerSocket( port, 5 )) {	
			Audit.log( String.format("Server listening on port: %s", port) );

			while (true)
				new RequestHandler( server.accept() ).run();

		} catch (IOException e) {
			e.printStackTrace();
	}	}
	
	public static void main( String arg[]) {

		Enguage.set( new Enguage() );

		server( port() );
}	}
