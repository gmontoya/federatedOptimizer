#!/bin/bash

ODYSSEY_HOME=/home/roott/federatedOptimizer
JENA_HOME=/home/roott/apache-jena-2.13.0
fedBenchDataPath=/home/roott/fedbBenchData
sed -i "s,optimize=.*,optimize=false," ${ODYSSEY_HOME}/lib/fedX3.1/config2
fedBench=/home/roott/queries/fedBench
#newQueries=/home/roott/queries/fedBench_1_1
datasets=/home/roott/datasetsVirtuoso
cold=true
#proxyFederationFile=/home/roott/tmp/proxyFederation

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
        #cd ${ODYSSEY_HOME}/scripts
        #tmpFile=`./startProxies.sh 8891 8899 3030 "ChEBI KEGG Drugbank Geonames DBpedia Jamendo NYTimes SWDF LMDB"`
        #sleep 1s
        #cd ${ODYSSEY_HOME}/scripts
        #./startFederation.sh > outputStartFederation
        #sleep 1m
        cd ${ODYSSEY_HOME}/code
        if [ "$cold" = "true" ] && [ -f ${ODYSSEY_HOME}/lib/fedX3.1/cache.db ]; then
            rm ${ODYSSEY_HOME}/lib/fedX3.1/cache.db
        fi
        /usr/bin/time -f "%e %P %t %M" timeout ${w}s java -cp .:${JENA_HOME}/lib/*:${ODYSSEY_HOME}/lib/fedX3.1/lib/* evaluateOurPlansWithFedXOrder $fedBench/$query ${datasets} ${fedBenchDataPath} 100000000 true false $newQueries/$query > outputFile 2> timeFile
        x=`grep "planning=" outputFile`
        y=`echo ${x##*planning=}`

        if [ -n "$y" ]; then
            d=`echo ${y%%ms*}`
        else
            d=-1
        fi

        x=`tail -n 1 timeFile`
        y=`echo ${x%% *}`
        x=`echo ${y%%.*}`
        if [ "$x" -ge "$w" ]; then
            t=`echo $y`
            t=`echo "scale=2; $t*1000" | bc`
            f=$(($f+1))
            nr=`grep "^\[" outputFile | grep "\]$" | wc -l | sed 's/^[ ^t]*//' | cut -d' ' -f1`
        else
            x=`grep "duration=" outputFile`
            y=`echo ${x##*duration=}`
            t=`echo ${y%%ms*}`
            x=`grep "results=" outputFile`
            nr=`echo ${x##*results=}`
        fi
        #cd ${ODYSSEY_HOME}/scripts
        #./killAll.sh ${proxyFederationFile}
        #sleep 10s
        #pi=`./processProxyInfo.sh ${tmpFile} 0 8`
        echo "${query} ${d} ${t} ${nr}"
        
        if [ "$f" -ge "2" ]; then
            break
        fi
        #killall virtuoso-t
        #sleep 10s
        #echo "${query}" >> outputFileAccOur
        #cat outputFile >> outputFileAccOur
        #echo "${query}" >> timeFileAccOur
        #cat timeFile >> timeFileAccOur
    done
done
