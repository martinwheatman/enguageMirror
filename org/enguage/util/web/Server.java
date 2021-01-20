package org.enguage.util.web;

import java.io.IOException;
import java.net.ServerSocket;

import org.enguage.Enguage;
import org.enguage.util.Audit;

public class Server extends Thread {
	
	static final private String  name = "WebServer";
	static       private Audit  audit = new Audit( name );

	static private int  port = 8080;
	static public  int  port() {return port;}
	static public  void port( int p ) {port = p;}
	
	static public void server( String port ) {
		
		Enguage.init( Enguage.RW_SPACE );

		try (ServerSocket server =
				new ServerSocket( Integer.valueOf( port ), 5 ))
		{	
			Audit.LOG( "Server listening on port: "+ port );
			while (true)
				new Request().doRequest( server.accept() );
			
		} catch (IOException e) {
			audit.ERROR( name +":IO error in TCP socket operation" );
			e.printStackTrace();
	}	}
	public static void main( String args[]) {
		server( args.length == 1 ? args[ 0 ] : "8080" );
}	}
