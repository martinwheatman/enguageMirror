# Programming

All utterance (apart from a few) initially map onto 'i don't understand.'
The interpretation of utterances can be progammed into Enguage in two ways, as:

+ a traditional [written program](written.md) albeit without any syntax; or,
+ a [spoken instruction](spoken.md) .

Either way the interpretation is stored in a file on disk,
in the following format:
<pre>On "utterence pattern";
	do this;
	reply "ok, this is done";
	if not, do that;
	reply "sorry, that is done";
	if not, do the other;
	reply "ok, we're done".</pre>
So this interpretation links the 'utterance pattern' to one of the replies:
'ok, this is done'; 'sorry, that is done'; or, 'ok, we're done'.

The felicity is passed back as is any answer represented by an elipsis
or the word 'whatever' is a spoken instruction, as in 'reply "my age is whatever".'
