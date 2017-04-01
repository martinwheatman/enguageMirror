package com.yagadi.enguage.interpretant;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.vehicle.Context;
import com.yagadi.enguage.vehicle.Reply;

public class Net {
	
	static private Audit audit = new Audit( "net", true );
	
	static public Reply client( Reply r, String addr, int port, String data ) {
		audit.in( "tcpip", "value='"+ data +"', ["+ Context.valueOf() +"]" );
		//audit.log( "tcpip( value='"+ data +"', ["+ Context.valueOf() +"])" );
		
		r.answer( Reply.failure() );
		if (port == 999) { // test value
			r.answer( Reply.success() ); // assume we're stuffed the server intentionally
			
		} else if (port > 1024 && port < 65536) {
			addr = addr==null || addr.equals( "" ) ? "localhost" : addr;
			
			audit.log( "sending: "+ addr +", "+ port +", "+ data );
			
			Socket connection = null;
			try {
				connection = new Socket( addr, port );
				DataOutputStream out = new DataOutputStream( connection.getOutputStream());
				out.writeBytes( data );
				r.answer( Reply.success() );
			} catch (IOException e) {
				audit.ERROR( "Intentional error: "+ e.toString());
			} finally {
				try {
					if (null != connection) connection.close();
				} catch (IOException e){
					audit.ERROR("closing connection:"+ e.toString());
		}	}	}
		return (Reply) audit.out( r ); // assuming it is void, pass something back...
}	}
