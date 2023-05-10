#!/bin/bash

if [ $# -eq 0 ]; then
    echo "Usage: $0 <FILENAME>" >&2
    exit 1
elif [ -f $1 ]; then
    head -3 $1 | tail -1
else
    echo "$0: $1: file not found." >&2
    exit 2
fi

