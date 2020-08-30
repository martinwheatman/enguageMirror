## HTTPD - a web server example

<p>Let’s put this behind a web server. I use Ubuntu desktop which doesn’t seem to come with a webserver, so I’m setting up apache2 from scratch. I’ve also created a simple cgi-bin program to call the Enguage jarfile running on the localhost with the arguments. This can be installed:
<pre>
martin@vBox:~/enguage$ su
root@vBox:/home/martin/enguage$ apt-get install apache2
root@vBox:/home/martin/enguage$ chown www-data.www-data *
root@vBox:/home/martin/enguage$ cp cgi-enguage.c /usr/lib/cgi-bin
root@vBox:/home/martin/enguage$ cp index.html /var/www/html
root@vBox:/home/martin/enguage$ cd /var/www/html
root@vBox:/var/www/html$ more index.html
</pre>
Then we can go and look at, and build, the cgi-bin program. Apache.conf specifies the location of cgi-bin in /usr/lib. Below, I have created a simple CGI program which takes ajax enguage queries and sends them to an adjacent server on this machine. 
<pre>
martin@vBox:/var/www/html$ su
root@vBox:/var/www/html# cd /usr/lib/cgi-bin
root@vBox:/usr/lib/cgi-bin# cc -o enguage.cgi cgi-enguage.c
root@vBox:/usr/lib/cgi-bin# more cgi-enguage.c
</pre>
[cgi-example.c](src/cgi-example.c)

You will also have to enable support for CGI programs:
<pre>
root@vBox:/var/www/html# cd /etc/apache2/mods-enabled
root@vBox:/var/www/html# ln -s ../mods-available/cgi.load .
</pre>
<p>The apache webserver can now be started with:
<pre>
root@vBox:/var/www/html# systemctl start apache2
</pre>
<p>We start with typing in the utterance/sentence into the input box in the web browser and press the Send button. If the input box is updated on the Web text to speech software returning a string, the send could be performed on input box change event. The AJAX code sends the utterance to the CGI-script which places the utterance next-door onto the Enguage jar-file, running as a server.  Enguage interprets the utterance, and in the case of “show me all names” it runs the squelch script to interrogate the database. This is returned back to the web browser as plain text in the CGI program. Hopefully this example is enough to get you going. Here’s what is should look like at the end:

