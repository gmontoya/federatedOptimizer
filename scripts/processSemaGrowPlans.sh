#!/bin/bash

plans=$1
n=-1
p=-1
while read line; do
    line=`echo $line`
    if [ "${line:0:9}" = "planning=" ]; then
        if [ "$n" -ge "0" ]; then
            echo "$p $n"
        fi
        y=`echo ${line##*planning=}`
        if [ -n "$y" ]; then
            p=`echo ${y%%ms*}`
        fi
        n=0
    elif [ "${line:0:21}" = "SourceQuery (source =" ]; then
        n=$(($n+1))
    fi
done < ${plans}
if [ "$n" -ge "0" ]; then
    echo "$p $n"
fi

