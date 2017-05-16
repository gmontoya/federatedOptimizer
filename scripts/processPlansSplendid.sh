#!/bin/bash

plans=$1
n=-1

while read line; do
    line=`echo $line`
    if [ "${line:0:8}" = "planning" ]; then
        if [ "$n" -ge "0" ]; then
            echo "$n"
        fi
        n=0
    elif [ "${line:0:15}" = "###REMOTE QUERY" ]; then
        y=`echo ${line##*@[}`
        x=`expr index "$y" ","`
        while [ "$x" -gt "0" ]; do
            n=$(($n+1))
            y=`echo ${y:$x}`
            x=`expr index "$y" ","`
        done
        n=$(($n+1))
    fi
done < ${plans}
if [ "$n" -ge "0" ]; then
    echo "$n"
fi
