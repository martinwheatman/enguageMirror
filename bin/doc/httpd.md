
## Enguage on a Web Server

### A simple Webserver
Let’s put Enguage into a web server.
A simple webserver is available from within Enguage.
After building Engauge,
if it is called with the --http switch,
````
java org.enguage.Enguage --httpd
````
it will run, by default on port 8080, to service URLs containing utterances.
<pre>
http://localhost:8080/what do i need
</pre>
which will return, in the webpage the reply (for example):
<pre>
you don't need anything
</pre>
This currently works on a hardcoded user Id of 000...0001.
Some work is needed to add an automatic random-number-as-UID algorithm.

### JusTalk
There is a Chome Extension, called [JusTalk](https://chrome.google.com/webstore/detail/lets-justalk-to-the-web/leoimjokapbleghdnkgnomeoaaabhaco?hl=en-GB),
which interprets a hardcoded repretoire of utterances concerned with web navigation and page interaction (e.g. "goto the bbc dot co dot uk", "set the value of name to martin" and "click on the ok button")

A development of this Chrome Extension can be found within the Enguage repo which also attempts to send the utterance to the current website if it is not understood by JusTalk.
This allows a developer to interact with their repertoires locally, through the Chrome text-to-speech service, rather than in Android.
It also allows developers easy access to their repretoire files.

To set this up, a developer needs to:
+ If not already done so, clone the Enguage repo locally:
  ````
  git clone https://github.com/martinwheatman/enguage.git
  cd enguage
  ````
+ Build and run the WebServer class:
  ````
  C:\> javac org\enguage\WebServer.java
  ````
  in a Windows/DOS box, or in Linux bash shell:
  ````
  $ javac org/enguage/WebServer.java
  ````
  Then the webserver, which contains Enguage, can be started with the command:
  ````
  java org.enguage.WebServer
  Server listening on port 8080
  ````
+ Install and open the [Chrome web browser](https://www.google.co.uk/chrome/).

+ Load the development JusTalk Chrome Extension:
  
  &vellip;&rarr;More Tools&rarr;Extensions

  Toggle 'Developer mode' and 3 buttons should appear:
  + Load unpacked;
  + Pack extension; and,
  + Update.

  Click on "Load unpacked" and select the repo directory, enguage&rarr;opt&rarr;JusTalk
  This should then show the "lazy e" icon in the menu bar, next to the Extensions icon (black jigsaw puzzle piece).

+ Allow the browser to access the microphone for this site.
  + Enter the URL of the local webserver in the address bar:
    ````
    http://localhost:8080
    ````
  + Click on the (i) icon to the left of the URL and select Site settings
  + Scroll down to Microphone, and on the right-hand side click on Ask(default)&rarr;Allow
  
  In the localhost:8080 tab, the (i) popup should be displaying the Microphone option, as Allow. 

The Chrome speech-to-text service can now be activated by clicking on Control-Space, or the MacControl-Space.
If your mic is still not working:
  + Make sure you're not muted, or your mic [is blocked](https://www.youtube.com/watch?v=TiZcsd_BahU); or,
  + You might find further help here: [Windows](https://support.microsoft.com/help/4027981/windows-how-to-set-up-and-test-microphones-in-windows-10) or [Mac](https://support.apple.com/kb/PH22070)

Finally, when it is working a red "recording" symbol is displayed in the Tab and the developer can access their repertoires by voice.

Try saying "what do i need"


