#!/bin/bash
## catFile.sh works as a curl emulator, echoing a local test file

if [ $# -eq 0 ]; then
	echo "usage: $0 <query>" >&2
	exit 1
fi

caps() {
	# martin wheatman => Martin Wheatman
	OUTPUT=""
	for NAME in $*; do
		OUTPUT="$OUTPUT ${NAME^}"
	done
	echo $OUTPUT
}

CACHE="selftest/wiki"
mkdir -p ${CACHE}

PARAMS="$*"
QUERY=$(echo $(caps $PARAMS) | tr ' ' '_')
CACHEFILE="$CACHE/$QUERY"

if [ ! -f "$CACHEFILE" ]; then
	URL="https://en.wikipedia.org/w/index.php?title=$QUERY"
	curl ${URL} > $CACHEFILE 2>/dev/null
fi


if [ ! -s "$CACHEFILE" ]; then
	echo "sorry, $PARAMS doesn't exist" >&2
	exit 1
fi
echo ${CACHEFILE}
# https://en.wikipedia.org/w/index.php?title=Queen_Elizabeth_The_Second&redirect=no