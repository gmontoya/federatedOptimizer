#!/bin/bash

plans=$1

inquery=false

while read line; do
    line=`echo $line`
    if [ "${line:0:8}" = "planning" ]; then
        inquery=true
        n=0
    elif [ "${line:0:7}" = "Command" ]; then #ENDPLAN" ]; then
        if [ "$n" -ge "0" ]; then
            echo "$n"
        fi
        n=0
        inquery=false
    elif [ "${line:0:15}" = "###REMOTE QUERY" ]; then
        y=`echo ${line##*@[}`
        x=`expr index "$y" ","`
        k=0
        while [ "$x" -gt "0" ]; do
            k=$(($k+1))
            y=`echo ${y:$x}`
            x=`expr index "$y" ","`
        done
        k=$(($k+1))
        #echo "k: ${k}"
    elif [ "${inquery}" = "true" ]; then
        if [ "${line:0:5}" != "UNION" ] && [ "${line:0:4}" != "JOIN" ] && [ "${line:0:8}" != "BINDJOIN" ] && [ "${line:0:8}" != "LEFTJOIN" ] && [ "${line:0:6}" != "FILTER" ]; then
            #echo "adding $k to n"
            n=$(($n+$k))
        fi
    fi
done < ${plans}
