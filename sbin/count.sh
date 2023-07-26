#!/bin/bash
# count.sh

DATA_LOCATION=etc/data
ORIENTATION="remote"
if [ "$1" = "local" -o "$1" = "remote" ]; then
    ORIENTATION="$1"; shift
fi

if [ $# -eq 0 ]; then
    echo "usage $(basename $0) [local|remote|] <file names>" >&2

else
    if [ "${ORIENTATION}" = "remote" ]; then
        COOKIES=cookies.txt
        csrfmwtoken=$(grep csrftoken $COOKIES | sed 's/^.*csrftoken\s*//')
        LS_FILES=$(curl \
            -c $COOKIES \
            -b $COOKIES \
            -d "X-CSRFToken: ${csrfmwtoken}"\
            localhost:8000/landing/list_files/ 2>/dev/null)
    else
        LS_FILES=$(ls -1 ${DATA_LOCATION})
    fi

    for NAME in $*; do
        LS_FILES=$(echo "$LS_FILES" | grep -i ${NAME})
    done
    
    # an empty string will still contain one CR/LF
    if [ -z "$LS_FILES" ]; then
    	echo 0
    else 
        echo "$LS_FILES" | wc -l
    fi
fi
