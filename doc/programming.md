## Programming
<h3>Programming</h3>
Create a plain text file in the assets directory—a repertoire—called show_me_all_files.txt containing:
<pre>
On "show me all files":
	run "ls";
	then, reply "ok, ...".
</pre>
<p>This is a sign—a mapping between an utterance and a reply. Each repertoire consists of one or more signs. A repertoire supports [an aspect of] a concept. This representation of a sign does look a little like a function, with a ‘name’ show me all files; and, a ‘body’ consisting of a semi-colon separated list. This is intentional as Semioticians often talk of signs functioning. The ‘name’ is actually a pattern, but this example is contain string literals, it contains no VARIABLES (i.e. represented as literals in uppercase). The body is a list of further utterances. This is all meant to read, and respond, like natural language.
<p>In this case the filename is the full utterance. However, a repertoire file is made up of the constant literals in the utterance—this is the framework which defines the aspect of the concept. In more developed repertoires, this can be paired down to one or two words, such as in meeting.txt.
<p>Now this utterance can be typed in, although this would normally be collected from some speech-to-text software!
<pre>   
Enguage> show me all files.
 => Ok , assets enguage.7z enguage.jar variable.
Enguage> 
</pre>
<P>This matches the utterance with the repertoire names and loads them temporarily, so language support is both scalable and dynamic. It then loads all the signs in the repertoire, some of which may not match the filename but are ancillary to the repertoire. It then matches the utterance with the ‘name’ of the actual sign: it must match the whole utterance, this is not a keyword search like a chatbot(!)
<p>When it has found a matching sign, it interprets the sign’s body—interpretant—to see if it can produce a reply. This is a simple example, with a run command in it. This runs the content of the string which is the Linux ls command which, if you’re not familiar with Linux, produces a list of files in the current directory.
<p>The next command is the reply which says ok , … the ellipsis is replaced with whatever the output from the previous command was (i.e the file list), and it outputs this, as seen above.
If any of this hadn’t matched/run correctly, it would have go on to find a sign to match elsewhere. If no match had been made it would have replied with, “I don’t understand”.
<p>We’ll now go on to customise the above example, but for the moment it will be best if you put the commands (e.g. to interrogate your database) into a shell command. See below!

You can program repertoires/concepts by copying the examples in
./etc/reps, in a traditional written file; however, you can also 
program engaage by voice:

<pre>To the phrase hello reply hello to you too.</pre>

<p>You can introduce simple variables:</p>
<pre>To the phrase my name is variable name reply hello variable name.</pre>

<p>Simple functions can be programmed:</p>
<pre>The product of x and y is x times y.
What is the product of 6 and 7.
</pre>
..., including recursive functions:
<pre>
The factorial of n is n times the factorial of n minus 1.
What is the factorial of 4.
</pre>
