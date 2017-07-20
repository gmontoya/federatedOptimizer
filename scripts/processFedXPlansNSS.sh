#!/bin/bash

plans=$1
n=0
inSSQ=false
inEG=false
inES=false

while read line; do
    line=`echo $line`
    #if [ "${line:0:4}" = "FedX" ]; then
    #    if [ "$n" -ge "0" ]; then
    #        echo "$n"
    #    fi
    #    n=0
    #    inSSQ=false
    #    inEG=false
    #    inES=false
    #el
    if [ "${line:0:17}" = "SingleSourceQuery" ]; then
        #n=1
        inSSQ=true
    elif [ "${line:0:14}" = "ExclusiveGroup" ]; then
        #n=$(($n+1))
        inEG=true
    elif [ "${line:0:22}" = "StatementSourcePattern" ]; then
        inEG=false
        inES=false
        inSSQ=false
    elif [ "${line:0:18}" = "ExclusiveStatement" ]; then
        n=$(($n+1))
        inES=true
    elif [ "${line:0:15}" = "StatementSource" ] && [ "${inES}" = "false" ] && [ "${inEG}" = "false" ] && [ "${inSSQ}" = "false" ]; then
        n=$(($n+1))
    fi
done < ${plans}
if [ "$n" -ge "0" ]; then
    echo "$n"
fi

