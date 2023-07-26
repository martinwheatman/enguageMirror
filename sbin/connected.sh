#!/bin/bash
# Connected.sh - does the donkey work for simple sap graph queries

DATA_LOCATION=etc/data
ORIENTATION="remote"
if [ "$1" = "local" -o "$1" = "remote" ]; then
    ORIENTATION="$1"; shift
fi

if [ "${ORIENTATION}" = "remote" ]; then
    COOKIES=cookies.txt
    csrfmwtoken=$(grep csrftoken $COOKIES | sed 's/^.*csrftoken\s*//')    
    LS_FILES=$(curl \
        -c $COOKIES \
        -b $COOKIES \
        -d "X-CSRFToken: ${csrfmwtoken}" \
        localhost:8000/landing/list_files/ 2>/dev/null)
else
    LS_FILES=$(ls -1 ${DATA_LOCATION})
fi

uniqueFile() {
    candidate="$1"
    nfiles=$(echo "$LS_FILES" | grep \\.${candidate}.json 2>/dev/null | wc -l)
    if [ $nfiles -eq 1 ]; then
       echo "$LS_FILES" | grep \\.${candidate}.json
    else
        # validate that candidates aren't zero nor multiple files
        nfiles=$(echo "$LS_FILES" | grep ${candidate} 2>/dev/null | wc -l)
        if [ $nfiles -lt 1 ]; then
            echo "Error: ${candidate}: does not exist" >&2
            exit 1
        elif [ $nfiles -gt 1 ]; then
            echo "$candidate refers to ${nfiles} files" >&2
            echo "$LS_FILES" | grep ${candidate} | sed -e 's/^/   /g' >&2
            exit 2
        else
            echo "$LS_FILES" | grep ${candidate}
        fi
    fi
}

cmd="$1"; shift

if [ "${cmd}" = "list" ]; then

    names="$*"
    candidate=$(echo ${names} | sed -r 's/(^| )(\w)/\U\2/g')
    candidate=$(uniqueFile $candidate 2>/dev/null)
    if [ -z "$candidate" ]; then
        echo "$names doesn't exist" >&2
        exit 1
    fi
    
    # sed prog to remove first prefix (always 'sap'?)
    firstPrefix=$(echo $candidate | cut -f1 -d.)
    rem1stPrefix="s/^${firstPrefix}\.//g"

    # sed prog to remove second prefix
    secondPrefix=$(echo $candidate | cut -f2 -d.)
    rem2ndPrefix="s/^${secondPrefix}\.//g"

    if [ "${ORIENTATION}" = "remote" ]; then
        COOKIES=cookies.txt
        csrfmwtoken=$(grep csrftoken $COOKIES | sed 's/^.*csrftoken\s*//')    
        CONTENTS=$(curl \
            -c $COOKIES \
            -b $COOKIES \
            -d "X-CSRFToken: ${csrfmwtoken}" \
            localhost:8000/landing/data/${candidate} 2>/dev/null)
    else
        CONTENTS=$(cat ${DATA_LOCATION}/${candidate})
    fi
 
    echo "${CONTENTS}"     | \
        sed -e 's/}/\n}/g' | # some rought and ready \
        sed -e 's/{/{\n/g' | # ...JSON formatting    \
        grep '"$ref"'      | # Find ALL references   \
        grep https         | # ...the external ones  \
        cut -f10 -d/       | # cut out the basename  \
        grep -v "create"   | # ignore create actions \
        uniq               | # remove duplicates     \
        sed -e 's/"$//g'   | # remove trailing dquote\
        sed ${rem1stPrefix}| # remove prefix         \
        sed ${rem2ndPrefix}| # remove prefix \
        sed -e 's/\([A-Z]\)/ \L\1/g' | sed -e 's/^ //g' | sed -e 's/\.//g'


elif [ "$cmd" = "query" ]; then
    foundPlus=0
    for i in $*; do
        if [ $i = "+" ]; then
            foundPlus=1
        elif [ ${foundPlus} -eq 0 ]; then
            name1="${name1} $i"
        else
            name2="${name2} $i"
        fi
    done
    
    candidate1=$(echo ${name1} | sed -r 's/(^| )(\w)/\U\2/g')
    file1=$(uniqueFile $candidate1 2>/dev/null)
    if [ -z "$file1" ]; then
        echo "${name1} does not exist" >&2
        exit 1
    fi

    candidate2=$(echo ${name2} | sed -r 's/(^| )(\w)/\U\2/g')
    file2=$(uniqueFile $candidate2 2>/dev/null)
    if [ -z "$file2" ]; then
        echo "${name2} does not exist" >&2
        exit 2
    fi
    
    # remove file extension (not in json files)
    file2=$(echo $file2 | sed -e 's/.json//g')
    
    if [ "${ORIENTATION}" = "remote" ]; then
        COOKIES=cookies.txt
        csrfmwtoken=$(grep csrftoken $COOKIES | sed 's/^.*csrftoken\s*//')
        CONTENTS=$(curl \
            -c $COOKIES \
            -b $COOKIES \
            -d "X-CSRFToken: ${csrfmwtoken}" \
            localhost:8000/landing/data/${file1} 2>/dev/null)
    else
        CONTENTS=$(cat ${DATA_LOCATION}/${file1})
    fi
 
    echo "${CONTENTS}"     | \
        sed -e 's/}/\n}/g' | # some rought and ready \
        sed -e 's/{/{\n/g' | # ...JSON formatting    \
        grep '"$ref"'      | # Find ALL references   \
        grep https         | # ...the external ones  \
        grep -v "create"   | # ignore create actions \
        grep ${file2} >/dev/null # ignore output

    if [ $? -eq 0 ]; then  
        echo "YES"
        exit 0
    else
        echo "NO"
        exit 1
    fi

else
    echo "Usage: $0 list <{filenames}>" >&2
    echo "       $0 query <{filename1}> + <{filename2}>" >&2
    exit 1
fi
