<h3>Install</h3>
Grab the docker image from [docker hub](https://hub.docker.com/r/martinwheatman/enguage)
<pre>
    docker pull martinwheatman/enguage
</pre>
or, clone this repo:
<pre>
    git clone https://github.com/martinwheatman/enguage.git
</pre>
and create a jar file using 'make enguage', or make an installable
container 'make shar' which creates the command 'eng' in ~/bin
<p>
You can then run the test suite with the -t option:</p>
<pre>
user@ubuntu:~/enguage$ java -jar enguage.jar -t
</pre>

<p>This should give you many example utterances.
Great! You can also run the jarfile as a shell.
It loads the config.xml file in the etc/ directory,
which contains lots of options to configure Enguage.
<P>
You can also type in utterances like a command line interface.
<pre>
martin@vBox:~/enguage$ java -jar enguage.jar
> i need a coffee.
Ok , you need a coffee.
</pre>
<p>
Now you’re ready to program Enguage…!
</p>
