package org.enguage.util.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Http implements AutoCloseable {

	private URL u;
	private HttpURLConnection connection;
	private HttpURLConnection connection() {return connection;}
	

	public Http( String url ) {
		try {
			// Create a URL object with the target endpoint
			u = new URL( url );
	
			// Open a connection to the URL
			connection = (HttpURLConnection) u.openConnection();
			connection.setRequestMethod("GET");
			
		} catch (IOException e) {
			e.printStackTrace();
	}	}
	
	public void close() {
		connection().disconnect();
	}
	// ************************************************************************
	
	public int responseCode() throws IOException {
		return connection().getResponseCode();
	}
	public String response() {
		StringBuilder response = new StringBuilder();
		// Read the response
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader( connection.getInputStream() )))
		{
			String line;
			while (null != (line = reader.readLine()))
				response.append( line+"\n" );
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response.toString();
	}
	
	// ************************************************************************
	// Test code...
	//
	public static void main(String[] args) {
		// default url -- SAP graph
		String defaultUrl = "http://localhost:3004/sap.graph/";
		String defaultQuery = "SalesQuote?$top=2";
		
		String query = defaultQuery;
		if (args.length > 0)
			query = args[ 0 ];
		
		String url = defaultUrl + query;
		System.out.println( "Trying: "+ url );
		
		try (Http http = new Http( url )) {
			// Get the response code
			int responseCode = http.responseCode();
			System.out.println("Response Code: " + responseCode);

			System.out.println("Response: " + http.response() );
			
		} catch (IOException e) {
			e.printStackTrace();
		}
}	}
