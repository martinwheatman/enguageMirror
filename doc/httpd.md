
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
which interprets a hardcoded repretoire of utterances concerned with navigation and page interaction (e.g. "goto the bbc dot co dot uk" and "set the value of name to martin" and "click on the ok button")

A development of this can be found within the Enguage repo which also attempts to send the utterance to the current website if it is not understood by JusTalk. This allows a developer to interact with their repertoires locally, through the Chrome text-to-speech service.

To set this up, a developer needs to:
+ Install the [Chrome webbrowser](https://www.google.co.uk/chrome/).
+ Clone the Enguage repo locally:
  ````
  git clone https://github.com/martinwheatman/enguage.git
  ````
+ Build and run the WebServer class:
  ````
  C:\> javac org\enguage\Webserver.java
  ... or:
  $ javac org/enguage/WebServer.java

  ...and then:
  java org.enguage.WebServer
  ````
+ Run Chrome and load the JusTalk development Extension:
  
  ... &rarr; More Tools &rarr; Extensions &rarr; Load unpacked
  
  From there select the repo directory, enguage &rarr; opt &rarr; JusTalk
  This should then show the "lazy e" icon in the menu bar, next to the Extensions icon (black jigsaw puzzle piece).

The speech-to-text service of the Web Browser can now be activated by the clicking on Control-Space, or the MacControl-Space.  The microphone may need to be activated - given permission to be used - on the first operation.

Finally, a "recording" symbol is displayed in the Tab and the developer can access their repertoires by voice.


