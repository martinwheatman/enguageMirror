<h3>Running with MySql</h3>
Here’s a simple example with MySql whch requires some work.
I have created a new repertoire 
in the assets directory called show_me_all.txt, containing:
<pre>
On "show me all ENTITY":
	run "squelch ENTITY";
	then, reply "ok, ENTITY include ...".
</pre>
I have also created the executable [shell script squelch](bin/squelch) which 
is on the path (I’ve got mine in ~/bin). In future this will be encapsulated 
within Enguage.

<p>In the following code, it calls the function above, and then
the sed turns field delimiting tabs into “, “
the tail removes line one (the column titles) from the output
<pre>
sqlQuery | \
	tail --lines=+2  | \
	tr '\n' ',' | sed 's/.$//' | sed 's/,/, /g'
echo # add a newline
</pre>
I have also created a simple test database using mysql. I assume you’re more familiar with mysql than I am:
<pre>
mysql> create database test;
Query OK, 1 row affected (0.00 sec)

mysql> use test;
Database changed

mysql> create table entity (names varchar(20), descriptions varchar(255));
Query OK, 0 rows affected (0.01 sec)

mysql> insert into entity (names, descriptions) values ("coffee", "americano");
Query OK, 1 row affected (0.01 sec)

mysql> insert into entity (names, descriptions) values ("milk", "dairy free");
Query OK, 1 row affected (0.01 sec)

mysql> select * from entity;
+--------+--------------+
| names  | descriptions |
+--------+--------------+
| coffee | americano    |
| milk   | dairy free   |
+--------+--------------+
2 rows in set (0.00 sec)

I can test squelch on its own from the command line:
martin@vBox:~/enguage$ cp squelch ~/bin/

martin@vBox:~/enguage$ squelch test martin secret select entity descriptions
americano, dairy free
</pre>
So when I run Enguage now:
<pre>
martin@vBox:~/enguage$ java -jar enguage.jar -d assets/ -s
Enguage (c) Martin Wheatman, 2001-4, 2011-17
Enguage main(): overlay is: [ /home/martin/yagadi.com, tmp(2) ]
.
.
.
Initialisation in: 765ms
2 clashes in a total of 117
Enguage> show me all names.
 => Ok , names include coffee , milk.
Enguage> show me all descriptions.
 => Ok , descriptions include americano , dairy free.
Enguage> 
</pre>
So, this isn’t brilliant. You can probably use MySql to separate fields with commas.  (I really have thrown this together!) And, you can still run the original example, Enguage will take longest match first:
<pre>
Enguage> show me all files.
 => Ok , assets enguage.7z enguage.jar variable.
Enguage>
</pre>
<h3>End-to-end</h3>
This is an end-to-end test from the user to the database to retrieve values from a relational database by voice. Because this involves a Java implementation on the client, it means that all traffic comes through this one piece of code. If we were to use this in real life it would mean context would leak from one user to another: not good! We need a port to JavaScript, or some context swapping server Java.
