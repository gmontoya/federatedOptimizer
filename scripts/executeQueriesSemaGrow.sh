#!/bin/bash

fedBench=/home/roott/queries/fedBench
SEMAGROW_HOME=/home/roott/semagrow
ODYSSEY_HOME=/home/roott/federatedOptimizer

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
        cd ${SEMAGROW_HOME}
        queryStr=`tr "\n" " " < ${folder}/${query}`
        /usr/bin/time -f "%e %P %t %M" timeout ${w}s java -Xmx4096m -cp .:http/target/semagrow-http-1.5.1/WEB-INF/lib/* eu.semagrow.cli.CliMain /etc/default/semagrow/repository.ttl "${queryStr}" output.json > outputFile 2> timeFile
        x=`tail -n 1 timeFile`
        y=`echo ${x%% *}`
        x=`echo ${y%%.*}`
        if [ "$x" -ge "$w" ]; then
            t=`echo $y`
            t=`echo "scale=2; $t*1000" | bc`
            f=$(($f+1))
            ${ODYSSEY_HOME}/scripts/fixJSONAnswer.sh output.json
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
        nr=`python ${ODYSSEY_HOME}/scripts/formatJSONFile.py output.json | wc -l | sed 's/^[ ^t]*//' | cut -d' ' -f1`

        ${ODYSSEY_HOME}/scripts/processSemaGrowPlansNSS.sh outputFile > xxx
        nss=`cat xxx`
        ${ODYSSEY_HOME}/scripts/processSemaGrowPlansNSQ.sh outputFile > xxx
        ns=`cat xxx`
        rm xxx
        echo "${query} ${nss} ${ns} ${s} ${t} ${nr}"

        if [ "$f" -ge "2" ]; then
            break
        fi
    done
done
