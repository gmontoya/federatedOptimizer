#!/bin/bash

sed -i "s,optimize=.*,optimize=false," /home/roott/federatedOptimizer/lib/fedX3.1/config2
cp /home/roott/fedBenchFederationVirtuoso.ttl /home/roott/fedBenchFederation.ttl
fedBench=/home/roott/queries/fedBench
newQueries=/home/roott/queries/fedBench_1_1
#fedBench=/home/roott/queries/complexQueries
#newQueries=/home/roott/queries/complexQueries_1_1
generalPreds=/home/roott/generalPredicates
datasets=/home/roott/datasetsVirtuoso

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

#s=`seq 1 10`
#l=""

#for i in ${s}; do
#    l="${l} C${i}"
#done

#l="LD8 LD11 CD4 CD5 LD3 LD5 LD6 LD9 LD10 CD6 LSD3"
#l="LSD4"

for query in ${l}; do
    #cd /home/roott/federatedOptimizer/scripts
    #tmpFile=`./startProxies.sh 8891 8899 3030 "ChEBI KEGG Drugbank Geonames DBpedia Jamendo NYTimes SWDF LMDB"`
    #sleep 1s
    for j in `seq 1 ${n}`; do
        cd /home/roott/federatedOptimizer/code
        /usr/bin/time -f "%e %P %t %M" timeout 1800s java -cp .:/home/roott/apache-jena-2.13.0/lib/*:/home/roott/federatedOptimizer/lib/fedX3.1/lib/* evaluateSPARQLQuery $fedBench/$query ${datasets} /home/roott/fedBenchData 10000000000 true false $newQueries/$query $generalPreds > outputFile 2> timeFile
        x=`grep "planning=" outputFile`
        y=`echo ${x##*planning=}`
        s=`echo ${y%%ms*}`
        x=`grep "duration=" outputFile`
        y=`echo ${x##*duration=}`
        t=`echo ${y%%ms*}`
        x=`grep "results=" outputFile`
        nr=`echo ${x##*results=}`
        #cd /home/roott/federatedOptimizer/scripts
        #./killAll.sh /home/roott/tmp/proxyFederation
        #sleep 10s
        #pi=`./processProxyInfo.sh ${tmpFile} 0 8`
        #echo "${query} ${s} ${t} ${pi} ${nr}"
        echo "${query} ${s} ${t} ${nr}"
    done
done
