#!/bin/bash

folder=/home/roott/queries/fedBench
cp /home/roott/splendidFedBenchFederationProxy.n3 /home/roott/splendidFedBenchFederation.n3
s=`seq 1 11`
l=""
n=1
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

#l="LD3"
for query in ${l}; do
    for j in `seq 1 ${n}`; do

        cd /home/roott/federatedOptimizer/scripts
        tmpFile=`./startProxies.sh 8891 8899 3030 "ChEBI KEGG Drugbank Geonames DBpedia Jamendo NYTimes SWDF LMDB"`
        sleep 5s

        cd /home/roott/rdffederator
        /usr/bin/time -f "%e %P %t %M" timeout 1800s ./SPLENDID.sh /home/roott/splendidFedBenchFederation.n3 ${folder}/${query} > outputFile 2> timeFile
        x=`grep "duration=" timeFile`
        y=`echo ${x##*duration=}`
        t=`echo ${y%%ms*}`
        nr=`python formatJSONFile.py outputFile | wc -l | sed 's/^[ ^t]*//' | cut -d' ' -f1`
        cd /home/roott/federatedOptimizer/scripts
        ./killAll.sh /home/roott/tmp/proxyFederation
        sleep 10s
        pi=`./processProxyInfo.sh ${tmpFile} 0 8`
        echo "${query} ${t} ${pi} ${nr}"
    done
done
