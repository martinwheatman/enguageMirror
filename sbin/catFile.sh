#!/bin/sh
## catFile.sh works as a curl emulator, echoing a local test file

if [ $# -eq 0 ]; then
	echo "usage: $0 <query>" >&2
	exit 1
fi

PARAMS="$*"
QUERY=$(echo $PARAMS | tr ' ' '_')
FILE="http+en.wikipedia.org+wiki+$QUERY.html"

if [ -f "$FILE" ]; then
	cat $FILE
else
	echo "Sorry, $FILE doesn't exist" >&2
	exit 1
fi

