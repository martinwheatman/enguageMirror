##jarFile - Command Line
<pre>
Usage: java -jar enguage.jar --http &lt;port&gt; | --port &lt;port&gt; |  --test [nnn] | --client
where: -c, --client
          runs Enguage as a shell

       -H <port>, --http <port>
          runs as a simple http server
       
       -p <port>, --port <port>
          runs as a simple server, listening on local TCP/IP port number

       -t, --test [nnn] -T <desc>
          runs a sanity check, on test number nnn (-nnn misses out test nnn)
</pre>
