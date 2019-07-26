#!/bin/bash

plans=$1
n=0

while IFS="" read line; do
    currentNumber=`echo "${line}" | awk '{ match($0, /^ */); printf("%d\n", RLENGTH) }'`
    line=`echo $line`
    if [ "${currentNumber}" -eq "0" ] && [ "${line:0:1}" = "[" ]; then
        break
    fi

    if [ "${line:0:17}" = "StatementSource (" ]; then
        n=$(($n+1))
    fi
done < ${plans}

if [ "$n" -ge "0" ]; then
    echo "$n"
fi

