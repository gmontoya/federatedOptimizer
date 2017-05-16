#!/bin/bash

plans=$1
n=-1

while read line; do
    line=`echo $line`
    if [ "${line:0:9}" = "QueryRoot" ]; then
        if [ "$n" -ge "0" ]; then
            echo "$n"
        fi
        n=0
    elif [ "${line:0:16}" = "StatementPattern" ]; then
        n=$(($n+1))
    fi
done < ${plans}
if [ "$n" -ge "0" ]; then
    echo "$n"
fi

