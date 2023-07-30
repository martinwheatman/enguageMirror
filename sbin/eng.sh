#!/bin/bash

cd ~/Audio/
file=$(ls -1tr *.wav | tail -1)
whisper ${file}              \
        --output_format txt  \
        --language English   \
        --model tiny.en
#rm *.wav

file=$(ls -1tr *.txt | tail -1)
if [ ! -f ${file} ]; then
    echo "whisper failed" >&2
    exit 1
else
    words=$(cat ${file})
    if [ -n "${words}" ]; then
	    rm *.txt

	    cd /home/martin/Enguage
	    if [ ! -f org/enguage/Enguage.class ]; then
		javac org/enguage/Enguage.java
	    fi

	    java org.enguage.Enguage ${words} | speak
    fi
fi

