# Installation

## a) As a docker image

Grab and run the docker image from
[docker hub](https://hub.docker.com/r/martinwheatman/enguage)
This runs the image listening to port 8080 as a simple webserve (where the utterance is given as the URL and the reply is displayed in the retrieved page)

<pre>
    docker pull martinwheatman/enguage
    docker run -p 8080:8080 --mount type=volume,src=eng,dst=/var/local/eng
</pre>

## b) From a DOS command line

This assumes you have git and java installed.

    Git can be obtained from: https://git-scm.com/downloads

    An open Java Development Kit can be obtained from https://openjdk.java.net/
    or a licenced verstion from https://jdk.java.net/  Any version should do.

Type the following commands:

<pre>
    C:\>git clone https://github.com/martinwheatman/enguage.git
    C:\>cd enguage
    C:\>javac opt\test\UnitTest.java
    C:\>java opt.test.UnitTest
    > i need a coffee.
    Ok , you need a coffee.
    > <Crt-Z>
    C:\>
</pre>

You can also run the java command with the '-t' option which self-tests Enguage,
with around 500 examples:

<pre>
    C:\>java opt.test.UnitTest -t
</pre>

## c) From a Linux or Cygwin shell

Copy the commands below at a Linux or Cygwin shell, to clone this repo,
create and run a jar file:

<pre>
    $ git clone https://github.com/martinwheatman/enguage.git
    $ cd enguage
    $ javac opt/test/UnitTest.java
    $ java  opt.test.UnitTest
    > i need a coffee.
    Ok , you need a coffee.
    > ^D
    $
</pre>

You can also run the jarfile as a shell to type in utterances like a
command line interface.

<pre>
    $ java  opt.test.UnitTest -t
    $ java  opt.test.UnitTest -T should
</pre>

This should give you many example utterances supported by the repertoires
in etc/rpt/.

Great!
Now you’re ready to [program Enguage](./programming.md)…!
