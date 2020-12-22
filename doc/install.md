# Install
## As a docker image
Grab and run the docker image from
[docker hub](https://hub.docker.com/r/martinwheatman/enguage)
This runs the image listening to port 8080 as a simple webserve (where the utterance is given as the URL and the reply is displayed in the retrieved page)
<pre>
    docker pull martinwheatman/enguage
    docker run -p 8080:8080 --mount type=volume,src=eng,dst=/var/local/eng --name persona enguage
</pre>
## From a command line
Copy the commands below at a Linux or Cygwin shell, to clone this repo
and create a jar file using 'make jar', then run the test suite with 
the -t option:
<pre>
    $ git clone https://github.com/martinwheatman/enguage.git
    $ cd enguage
    $ make jar
    $ java -jar lib/enguage.jar -t
</pre>
It loads the config.xml file in the etc/ directory, which contains lots 
of options to configure Enguage.
This should give you many example utterances supported by the repertoires 
in etc/rpt/.
Great!
You can also run the jarfile as a shell to type in utterances like a 
command line interface.
<pre>
    $ java -jar lib/enguage.jar
    > i need a coffee.
    Ok , you need a coffee.
</pre>
Now you’re ready to [program Enguage](./programming.md)…!
