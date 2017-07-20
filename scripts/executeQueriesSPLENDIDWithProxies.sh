#!/bin/bash

folder=/home/roott/queries/fedBench
cp /home/roott/splendidFedBenchFederationProxy.n3 /home/roott/splendidFedBenchFederation.n3
s=`seq 1 11`
l=""
n=10
w=1800
for i in ${s}; do
    l="${l} LD${i}"
done

l=""

s=`seq 1 7`

for i in ${s}; do
    l="${l} CD${i}"
done

for i in ${s}; do
    l="${l} LS${i}"
done

l="LS4 LS5 LS6 LS7"

for query in ${l}; do
    f=0
    for j in `seq 1 ${n}`; do

        cd /home/roott/federatedOptimizer/scripts
        #tmpFile=`./startProxies.sh 8891 8899 3030 "ChEBI KEGG Drugbank Geonames DBpedia Jamendo NYTimes SWDF LMDB"`
        tmpFile=`./startProxies2.sh "172.19.2.123 172.19.2.106 172.19.2.100 172.19.2.115 172.19.2.107 172.19.2.118 172.19.2.111 172.19.2.113 172.19.2.120" 3030`
        sleep 10s

        cd /home/roott/rdffederator
        /usr/bin/time -f "%e %P %t %M" timeout ${w}s ./SPLENDID.sh /home/roott/splendidFedBenchFederation.n3 ${folder}/${query} > outputFile 2> timeFile

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

        /home/roott/federatedOptimizer/scripts/processPlansSplendidNSS.sh timeFile > xxx
        nss=`cat xxx`
        /home/roott/federatedOptimizer/scripts/processPlansSplendidNSQ.sh timeFile > xxx
        ns=`cat xxx`
        rm xxx

        cd /home/roott/federatedOptimizer/scripts
        ./killAll.sh /home/roott/tmp/proxyFederation
        sleep 10s
        pi=`./processProxyInfo.sh ${tmpFile} 0 8`

        echo "${query} ${nss} ${ns} ${s} ${t} ${pi} ${nr}"
        if [ "$f" -ge "2" ]; then
            break
        fi
    done
done
