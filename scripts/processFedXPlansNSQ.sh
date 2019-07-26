#!/bin/bash

plans=$1

n=0
inSSQ=false
inEG=false
inES=false
numberEG=-1

while IFS="" read line; do
    currentNumber=`echo "${line}" | awk '{ match($0, /^ */); printf("%d\n", RLENGTH) }'`
    line=`echo $line`
    if [ "${currentNumber}" -eq "0" ] && [ "${line:0:1}" = "[" ]; then
        break
    fi
    if [ "${line:0:17}" = "SingleSourceQuery" ]; then
        n=1
        inSSQ=true
    elif [ "${line:0:14}" = "ExclusiveGroup" ]; then
        n=$(($n+1))
        inEG=true
        numberEG=$currentNumber
    elif [ "${line:0:22}" = "StatementSourcePattern" ]; then
        inEG=false
        inES=false
    elif [ "${line:0:18}" = "ExclusiveStatement" ] && [ "${inSSQ}" = "false" ]; then
        if [ "${inEG}" = "false" ] || [ "${currentNumber}" -le "${numberEG}" ]; then
          n=$(($n+1))
          inES=true
          inEG=false
        fi
    elif [ "${line:0:15}" = "StatementSource" ] && [ "${inES}" = "false" ] && [ "${inEG}" = "false" ] && [ "${inSSQ}" = "false" ]; then
        n=$(($n+1))
    fi
done < "${plans}"
if [ "$n" -ge "0" ]; then
    echo "$n"
fi
