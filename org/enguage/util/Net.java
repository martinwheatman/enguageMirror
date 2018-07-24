package org.enguage.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import org.enguage.Enguage;
import org.enguage.sign.context.Context;
import org.enguage.vehicle.reply.Reply;
import org.enguage.util.Audit;

public class Net {
	
	static final public int TestPort = 0;

	static private Audit audit = new Audit( "net" );
	
	static public void server( String port ) {
		ServerSocket server = null;
		try {
			server = new ServerSocket( Integer.valueOf( port ));
			audit.LOG( "Server listening on port: "+ port );
			while (true) {
				
				Socket connection = server.accept();
				
				BufferedReader in =
				   new BufferedReader( new InputStreamReader( connection.getInputStream()));
				DataOutputStream out = new DataOutputStream( connection.getOutputStream());
				
				out.writeBytes( Enguage.e.interpret( new Strings( in.readLine() )) + "\n" );

				in.close();
				out.close();
				connection.close();
			}
		} catch (IOException e) {
			audit.ERROR( "Engauge.main():IO error in TCP socket operation" );
		} finally {
			try {
				server.close();
			} catch (IOException e) {
				audit.ERROR( "Engauge.main():IO error in closing TCP socket" );
		}	}
	}
	static public String client( String addr, int port, String data ) {
		audit.in( "tcpip", "addr="+ addr +", port="+ port +"value='"+ data +"', ["+ Context.valueOf() +"]" );
		
		String rc = Reply.failure();
		
		if (port == TestPort) { // test value
			rc = Reply.success(); // assume we've stuffed the server intentionally
			
		} else if (port > 1024 && port < 65536) {
			addr = addr==null || addr.equals( "" ) ? "localhost" : addr;
			
			Socket connection = null;
			try {
				connection = new Socket( addr, port );
				DataOutputStream out = new DataOutputStream( connection.getOutputStream());
				out.writeBytes( data +"\n" );
								
				BufferedReader in =
						   new BufferedReader( new InputStreamReader( connection.getInputStream()));
				rc = in.readLine();
				
				out.close();
				connection.close();
				in.close();
				
			} catch (IOException e) {
				audit.ERROR( "error: "+ e.toString());
			} finally {
				try {
					if (null != connection) connection.close();
				} catch (IOException e){
					audit.ERROR("closing connection:"+ e.toString());
		}	}	}
		return audit.out( rc );
}	}
