#!/bin/bash

fedBench=/home/roott/queries/fedBench
splendidDescriptionFile=/home/roott/splendidFedBenchFederation.n3
SPLENDID_HOME=/home/roott/rdffederator
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
        cd ${SPLENDID_HOME}
        /usr/bin/time -f "%e %P %t %M" timeout ${w}s ./SPLENDID.sh ${splendidDescriptionFile} ${folder}/${query} > outputFile 2> timeFile

        x=`tail -n 1 timeFile`
        y=`echo ${x%% *}`
        x=`echo ${y%%.*}`
        if [ "$x" -ge "$w" ]; then
            t=`echo $y`
            t=`echo "scale=2; $t*1000" | bc`
            f=$(($f+1))
            python fixJSONFile.py outputFile
        else
            x=`grep "duration=" timeFile`
            y=`echo ${x##*duration=}`
            t=`echo ${y%%ms*}`
        fi
        x=`grep "planning=" timeFile`
        y=`echo ${x##*planning=}`

        if [ -n "$y" ]; then
            s=`echo ${y%%ms*}`
        else
            s=-1
        fi

        nr=`python formatJSONFile.py outputFile | wc -l | sed 's/^[ ^t]*//' | cut -d' ' -f1`

        ${ODYSSEY_HOME}/scripts/processPlansSplendidNSS.sh timeFile > xxx
        nss=`cat xxx`
        ${ODYSSEY_HOME}/scripts/processPlansSplendidNSQ.sh timeFile > xxx
        ns=`cat xxx`
        rm xxx
        echo "${query} ${nss} ${ns} ${s} ${t} ${nr}"

        if [ "$f" -ge "2" ]; then
            break
        fi

    done
done
