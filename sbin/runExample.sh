#!/bin/bash

if [ $# -eq 0 ]; then
    echo "Usage: $0 <FILENAME> [<lineNo>]" >&2
    exit 1
elif [ -f $1 ]; then
	FILE=$1; shift
	LINE=3
	if [ -n "$1" ]; then
		LINE=$1; shift
	fi
    head -$LINE $FILE | tail -1
else
    echo "$0: $1: file not found." >&2
    exit 2
fi

