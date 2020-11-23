<h3>Install</h3>
    Grab the docker image from [docker hub](https://hub.docker.com/r/martinwheatman/enguage)
<pre>
    docker pull martinwheatman/enguage
</pre>
    or, by copying the commands below at a Linux or Cygwin shell, clone this repo
    and create a jar file using 'make jar', then run the test suite with the -t option:</p>
<pre>
    $ git clone https://github.com/martinwheatman/enguage.git
    $ cd enguage
    $ make jar
    $ java -jar lib/enguage.jar -t
</pre>
<p>
    It loads the config.xml file in the etc/ directory, which contains lots of options to configure Enguage.
    This should give you many example utterances supported by the repertoires in etc/rpt/.
    Great! You can also run the jarfile as a shell to type in utterances like a command line interface.
<pre>
    $ java -jar lib/enguage.jar
    > i need a coffee.
    Ok , you need a coffee.
</pre>
<p>
    Now you’re ready to program Enguage…!
</p>
