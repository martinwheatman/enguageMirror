<h3>Enguage Web Speech-to-Text</h3>
<p>This needs Chrome(!) It works on my Ubuntu installation.  This index.html integrates the Enguage installation, as described above, with the speech-to-text web api:
<p>index.html:
<pre>
&lt;!DOCTYPE html>
&lt;html lang="en">
&lt;head>
  &lt;title>Speech Recognition</title>
&lt;/head>
&lt;body>
	&lt;script>
window.SpeechRecognition = window.webkitSpeechRecognition || window.SpeechRecognition;

function buttonPress() {
	recognition = new SpeechRecognition();
	recognition.start();
	recognition.onresult = function(event) {	
		if (event.results[0].isFinal) {
			speechToText = event.results[0][0].transcript;
			document.getElementById( "heard" ).innerHTML = speechToText;
			interp( speechToText );
}	}	}

function interp( utterance ) {
	var xhttp = new XMLHttpRequest();
	xhttp.onreadystatechange = function() {
		if (this.readyState == 4 && this.status == 200) {
			synth = window.speechSynthesis;
			synth.speak( new SpeechSynthesisUtterance( this.responseText ));
			document.getElementById( "reply" ).innerHTML = this.responseText;
	}	}
	xhttp.open("GET", "cgi-bin/enguage.cgi?"+ utterance, true);
	xhttp.send();
}
	&lt;/script>
	&lt;button onClick="buttonPress();">Click and Speak</button>
	&lt;div id="heard">I heard...</div>
	&lt;div id="reply">my reply...</div>
&lt;/body>
&lt;/html>
</pre>

<p>Using the above HTML/JavaScript, along with the running jarfile and cgi scripts, above, we can get the below. Remember, if it says sorry, I don’t know the value of database, this can be solved by saying set the value of database to test, and set the value of table to entity.
