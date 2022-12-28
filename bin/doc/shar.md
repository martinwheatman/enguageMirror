<h3>shar</h3>
Clone this repo:
<pre>
    git clone https://github.com/martinwheatman/enguage.git
    cd enguage/
</pre>
and  make an installable container with
<pre>
    make shar
</pre>
<p>
which creates a self extracting installable file: enguage.shar .
You can then run this file to install enauge:</p>
<pre>
    ./enguage.shar
</pre>
This should create:
<ul>
<li>'eng' in ~/bin
<li>'engauge.jar' in ~/lib
<li>config file in ~/etc
</ul>

<P>You can then type in utterances as a command line interface.
<pre>
    eng i need a coffee.
</pre>
<p>Or, without any parameters it will run as a shell
</p>
