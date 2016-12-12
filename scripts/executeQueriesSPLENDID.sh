#!/bin/bash

folder=/home/roott/queries/fedBench
cp /home/roott/splendidFedBenchFederationVirtuoso.n3 /home/roott/splendidFedBenchFederation.n3
s=`seq 1 11`
l=""
n=10
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
    for j in `seq 1 ${n}`; do
        cd /home/roott/rdffederator
        /usr/bin/time -f "%e %P %t %M" timeout 1800s ./SPLENDID.sh /home/roott/splendidFedBenchFederation.n3 ${folder}/${query} > outputFile 2> timeFile
        x=`grep "duration=" timeFile`
        y=`echo ${x##*duration=}`
        t=`echo ${y%%ms*}`
        nr=`python formatJSONFile.py outputFile | wc -l | sed 's/^[ ^t]*//' | cut -d' ' -f1`
        echo "${query} ${t} ${nr}"
    done
done
