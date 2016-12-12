#!/bin/bash

folder=/home/roott/queries/fedBench
sed -i "s,optimize=.*,optimize=true," /home/roott/federatedOptimizer/lib/fedX3.1/config2
cp /home/roott/fedBenchFederationProxy.ttl /home/roott/fedBenchFederation.ttl
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

for query in ${l}; do
    for j in `seq 1 ${n}`; do
        cd /home/roott/federatedOptimizer/scripts
        tmpFile=`./startProxies.sh 8891 8899 3030 "ChEBI KEGG Drugbank Geonames DBpedia Jamendo NYTimes SWDF LMDB"`
        #query=LD${i}
        sleep 1s
        cd /home/roott/federatedOptimizer/lib/fedX3.1
        rm cache.db
        /usr/bin/time -f "%e %P %t %M" timeout 1800s ./cli.sh -c config2 -d /home/roott/fedBenchFederation.ttl @q ${folder}/${query} > outputFile 2> timeFile
        #cat timeFile
        #cat outputFile
        #t=`cat timeFile`
        x=`grep "duration=" outputFile`
        y=`echo ${x##*duration=}`
        t=`echo ${y%%ms*}`
        x=`grep "results=" outputFile`
        nr=`echo ${x##*results=}`
        cd /home/roott/federatedOptimizer/scripts
        ./killAll.sh /home/roott/tmp/proxyFederation
        sleep 10s
        pi=`./processProxyInfo.sh ${tmpFile} 0 8`
        echo "${query} ${t} ${pi} ${nr}"
        #echo "${query} ${t} ${nr}"
    done
done
