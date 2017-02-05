#!/bin/bash

plans=$1
n=-1
p=-1
while read line; do
    line=`echo $line`
    if [ "${line:0:4}" = "FedX" ]; then
        if [ "$n" -ge "0" ]; then
            echo "$p $n"
        fi
        n=0
        inSSQ=false
        inEG=false
    elif [ "${line:0:9}" = "planning=" ]; then
        y=`echo ${line##*planning=}`
        if [ -n "$y" ]; then
            p=`echo ${y%%ms*}`
        fi 
    elif [ "${line:0:17}" = "SingleSourceQuery" ]; then
        n=1
        inSSQ=true
    elif [ "${line:0:14}" = "ExclusiveGroup" ]; then
        n=$(($n+1))
        inEG=true
    elif [ "${line:0:22}" = "StatementSourcePattern" ]; then
        inEG=false
    elif [ "${line:0:18}" = "ExclusiveStatement" ] && [ "${inEG}" = "false" ] && [ "${inSSQ}" = "false" ]; then
        n=$(($n+1))
    elif [ "${line:0:15}" = "StatementSource" ] && [ "${inEG}" = "false" ] && [ "${inSSQ}" = "false" ]; then
        n=$(($n+1))
    fi
done < ${plans}
if [ "$n" -ge "0" ]; then
    echo "$p $n"
fi

