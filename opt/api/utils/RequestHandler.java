package opt.api.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Map;

import org.enguage.util.audit.Audit;

import opt.api.utils.http.HttpParser;
import opt.api.utils.http.HttpResponseBuilder;
import opt.api.utils.http.HttpStringBuilder;


public class RequestHandler extends Thread {
	
	static final private String	name = "Request";
	static       private Audit  audit = new Audit( name );

	private HttpResponseBuilder responseBuilder = new HttpResponseBuilder();
    
	private Socket connection;
	
	public  RequestHandler( Socket conn ) {connection = conn;}

	public void run() {
		try (
             BufferedReader   in  = new BufferedReader( new InputStreamReader(connection.getInputStream()) );
			 DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            ) 
		{
			String request = HttpStringBuilder.build(in);

			audit.debug( String.format("PROCESSING REQUEST:\n%s", request) );
			Audit.log( String.format("PROCESSING REQUEST:\n%s", request) );
			
			Map<String, String> head = HttpParser.parseHeaders(request);
			Map<String, String> body = HttpParser.parseBody(request);

			String response = responseBuilder.build(head, body);

			audit.debug( String.format("RESPONSE:\n%s", response) );
			Audit.log( String.format("RESPONSE:\n%s", response) );
			
			out.writeBytes(response);
			connection.close();
			
		} catch (Exception e) {
			audit.error( "Error in child socket");
			e.printStackTrace();
    }	}

}	
