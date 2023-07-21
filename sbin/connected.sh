#!/bin/bash
# Connected.sh - does the donkey work for simple sap graph queries

DATA_LOCATION=etc/data
cd ${DATA_LOCATION}

uniqueFile() {
    candidate="$1"
    nfiles=$(ls -1 *.*.${candidate}.json 2>/dev/null | wc -l)
    if [ $nfiles -eq 1 ]; then
       echo *.*.${candidate}.json
    else
        # validate that candidates aren't zero nor multiple files
        nfiles=$(ls -1 *${candidate}* 2>/dev/null | wc -l)
        if [ $nfiles -lt 1 ]; then
            echo "Error: ${candidate}: does not exist" >&2
            exit 1
        elif [ $nfiles -gt 1 ]; then
            echo "$candidate refers to ${nfiles} files" >&2
            echo "$(ls -1 *${candidate}*)" | sed -e 's/^/   /g' >&2
            exit 2
        else
            echo "*${candidate}*"
        fi
    fi
}

cmd="$1"; shift

if [ "${cmd}" = "list" ]; then

    names="$*"
    candidate=$(echo ${names} | sed -r 's/(^| )(\w)/\U\2/g')
    #candidate="*${candidate}*"
    
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

    cat ${candidate} | \
        sed -e 's/}/\n}/g' | # some rought and ready \
        sed -e 's/{/{\n/g' | # ...JSON formatting    \
        grep '"$ref"'      | # Find ALL references   \
        grep https         | # ...the external ones  \
        cut -f10 -d/       | # cut out the basename  \
        grep -v "create"   | # ignore create actions \
        uniq               | # remove duplicates     \
        sed -e 's/"$//g'   | # remove trailing dquote\
        sed ${rem1stPrefix}| # remove prefix         \
        sed ${rem2ndPrefix} | # remove prefix \
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
    
    cat ${file1} | \
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
