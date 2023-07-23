package opt.api.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Map;

import org.enguage.util.audit.Audit;

import opt.api.routing.Router;
import opt.api.utils.http.HttpParser;
import opt.api.utils.http.HttpStringBuilder;
import opt.api.utils.http.HttpException;


public class RequestHandler extends Thread {
	
	static final private String	name = "Request";
	static       private Audit  audit = new Audit( name );
    
	private Socket connection;
	
	public  RequestHandler( Socket conn ) {connection = conn;}

	private String response = "200 OK";
	private String response() {return response;}
	public  void   response( String s ) {response = s;}

	private Router router = new Router();

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

			String reply;
			try {
				reply = router.handle(head, body);
			} catch (HttpException e) {
				reply = e.getErrorMessage();
				response(e.getResponse());
			}
			
			String response = head.get("http") + " " + response() +"\n"
			+ "Content-Type: text/plain\n"
			+ "Content-Length: " + reply.length() + "\n"
			+ "\n"
			+ reply
			+ "\n";

			audit.debug( String.format("RESPONSE:\n%s", response) );
			Audit.log( String.format("RESPONSE:\n%s", response) );
			
			out.writeBytes(response);
			connection.close();
			
		} catch (Exception e) {
			audit.error( "Error in child socket");
			e.printStackTrace();
    }	}

}	
