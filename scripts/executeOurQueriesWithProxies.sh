#!/bin/bash

sed -i "s,optimize=.*,optimize=false," /home/roott/federatedOptimizer/lib/fedX3.1/config2
cp /home/roott/fedBenchFederationProxy.ttl /home/roott/fedBenchFederation.ttl
fedBench=/home/roott/queries/fedBench
newQueries=/home/roott/queries/fedBench_1_1
generalPreds=/home/roott/generalPredicates
datasets=/home/roott/datasets

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

#l="LD1 LD1"

for query in ${l}; do
    for j in `seq 1 ${n}`; do
        cd /home/roott/federatedOptimizer/scripts
        tmpFile=`./startProxies.sh 8891 8899 3030 "ChEBI KEGG Drugbank Geonames DBpedia Jamendo NYTimes SWDF LMDB"`
        sleep 1s
        cd /home/roott/federatedOptimizer/code
        /usr/bin/time -f "%e %P %t %M" timeout 1800s java -cp .:/home/roott/apache-jena-2.13.0/lib/*:/home/roott/federatedOptimizer/lib/fedX3.1/lib/* evaluateSPARQLQuery $fedBench/$query ${datasets} /home/roott/fedBenchData 100000000 true false $newQueries/$query $generalPreds > outputFile 2> timeFile
        x=`grep "planning=" outputFile`
        y=`echo ${x##*planning=}`
        s=`echo ${y%%ms*}`
        x=`grep "duration=" outputFile`
        y=`echo ${x##*duration=}`
        t=`echo ${y%%ms*}`
        x=`grep "results=" outputFile`
        nr=`echo ${x##*results=}`
        cd /home/roott/federatedOptimizer/scripts
        ./killAll.sh /home/roott/tmp/proxyFederation
        sleep 10s
        pi=`./processProxyInfo.sh ${tmpFile} 0 8`
        echo "${query} ${s} ${t} ${pi} ${nr}"
        #echo "${query} ${s} ${t} ${nr}"
    done
done