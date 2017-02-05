#!/bin/bash

folder=/home/roott/queries/fedBench
s=`seq 1 11`
l=""
n=10
w=1800
for i in ${s}; do
    l="${l} LD${i}"
done

s=`seq 1 7`

for i in ${s}; do
    l="${l} CD${i}"
done

for i in ${s}; do
    l="${l} LS${i}"
done

for query in ${l}; do
    f=0
    for j in `seq 1 ${n}`; do
        cd /home/roott/semagrow
        queryStr=`tr "\n" " " < ${folder}/${query}`
        /usr/bin/time -f "%e %P %t %M" timeout ${w}s java -cp .:http/target/semagrow-http-1.5.1/WEB-INF/lib/* eu.semagrow.cli.CliMain /etc/default/semagrow/repository.ttl "${queryStr}" output.json > outputFile 2> timeFile
        x=`tail -n 1 timeFile`
        y=`echo ${x%% *}`
        x=`echo ${y%%.*}`
        if [ "$x" -ge "$w" ]; then
            t=`echo $y`
            t=`echo "scale=2; $t*1000" | bc`
            f=$(($f+1))
            /home/roott/federatedOptimizer/scripts/fixJSONAnswer.sh output.json
        else
            x=`grep "duration=" outputFile`
            y=`echo ${x##*duration=}`
            t=`echo ${y%%ms*}`
        fi
        x=`grep "planning=" outputFile`
        y=`echo ${x##*planning=}`
        if [ -n "$y" ]; then
            s=`echo ${y%%ms*}`
        else
            s=-1
        fi
        nr=`python /home/roott/federatedOptimizer/scripts/formatJSONFile.py output.json | wc -l | sed 's/^[ ^t]*//' | cut -d' ' -f1`

        echo "${query} ${s} ${t} ${nr}"
        if [ "$f" -ge "2" ]; then
            break
        fi
    done
done
