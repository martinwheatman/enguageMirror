package opt.web.actions;

import org.enguage.Enguage;
import org.enguage.util.Strings;

import opt.web.Request;

public class Eng {
	public static final String engPage( String username ) {
		return	"<fieldset>\n"
				+ "		<legend>Say:</legend>\n"
				+ "		<form action='/enguage'>\n"
				+ "			<label for='utterance'>Utterance:</label>\n"
				+ "			<input type='text' id='utterance' placeholder='utterance'>&nbsp;\n"
				+ "			<input type='button' id='say' onclick='i_say()' value='Say'>\n" 
				+ "		</form>\n"
				+ " 	<p id='reply'></p>\n"
				+ "	</fieldset>\n"
				+ "<script>\n"
				+ "	function i_say() {\n"
				+ "		//alert( 'hello' );\n"
				+ "		var request = '/enguage'"
				+ "					+ '?uid=" + username +"'"
				+ "					+ '&utterance='+ document.getElementById('utterance').value;\n"
				+ "		var xhttp = new XMLHttpRequest();\n"
				+ "		xhttp.onreadystatechange = function() {\n"
				+ "			if (this.readyState == 4 && this.status == 200) {\n"
				+ "				document.getElementById('utterance').value = '';\n"
				+ "				document.getElementById('reply').innerHTML = this.responseText;\n"
				+ "			}\n"
				+ "		};\n"
				+ "		xhttp.open('GET', request, true);\n"
				+ "		xhttp.send();\n"
				+ "	}\n"
				+ "</script><br>\n";
	}
	private static String form( String[] params ) {
		return "<center><strong>"
				+ Enguage.mediate(
						params[ 0 ].split( "=" )[ 1 ],
						new Strings( params[ 1 ].split( "=" )[ 1 ].split( "%20" ))
				  )
				+ "</strong></center></P>";
	}
	private static String direct( String uid, String[] params ) {
		String reply =  Enguage.mediate(
							uid,
							new Strings( params[ 0 ].split( "=" )[ 1 ].split( "%20" ))
						).toString();
		// fix incase enguage (repertoire) is not so polite!
		if (reply.equalsIgnoreCase( "i don't understand" ))
			reply = "sorry, "+ reply;
		return reply;
	}
	public static String getReply( Request r, String cmd, String[] params ) {
		String reply = "";
		if (cmd.equals( "enguage" )) {
			reply = Request.validAttrs( params, 2 ) ?
						Eng.form( params )
						: ("<center>"
							+ "(Try setting the value of utterance to something)"
							+ "</center>");
				
		} else if (cmd.equals( "Enguage" )) { // verbal interaction
			
			if (Request.validAttrs( params, 1 ) && r.uid().length() > 0)
				reply = Eng.direct( r.uid(), params );
		}
		return reply;
}	}
