# Install
## As a docker image
Grab and run the docker image from
[docker hub](https://hub.docker.com/r/martinwheatman/enguage)
This runs the image listening to port 8080 as a simple webserve (where the utterance is given as the URL and the reply is displayed in the retrieved page)
<pre>
    docker pull martinwheatman/enguage
    docker run -p 8080:8080 --mount type=volume,src=eng,dst=/var/local/eng
</pre>
## From a DOS command line
This assumes you have git and java installed.
<pre>
    C:\>git clone https://github.com/martinwheatman/enguage.git
    C:\>cd enguage
    C:\>javac org\enguage\Engauge.java
    C:\>java org.enguage.Enguage
    > i need a coffee.
    Ok , you need a coffee.
    > <Crt-Z>
    C:\>
</pre>
You can also run the java command with the '-t' option which self-tests Enguage,
with around 350 examples:
<pre>
    C:\>java org.enguage.Enguage -t
</pre>

## From a Linux or Cygwin shell
Copy the commands below at a Linux or Cygwin shell, to clone this repo,
create and run a jar file:
<pre>
    $ git clone https://github.com/martinwheatman/enguage.git
    $ cd enguage
    $ make jar
    $ java -jar lib/enguage.jar
    > i need a coffee.
    Ok , you need a coffee.
    > ^D
    $
</pre>
You can also run the jarfile as a shell to type in utterances like a 
command line interface.
<pre>
    $ java -jar lib/enguage.jar -t
</pre>
This should give you many example utterances supported by the repertoires 
in etc/rpt/.

Great!
Now you’re ready to [program Enguage](./programming.md)…!
