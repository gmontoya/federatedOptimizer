#!/bin/bash

sed -i "s,optimize=.*,optimize=false," /home/roott/federatedOptimizer/lib/fedX3.1/config2

fedBench=/home/roott/queries/fedBench
newQueries=/home/roott/queries/fedBench_1_1
generalPreds=/home/roott/generalPredicates
group=LSD
l=`seq 7 7`

for i in ${l}; do
    cd /home/roott/federatedOptimizer/scripts
    tmpFile=`./startProxies.sh 8891 8899 3030 "ChEBI KEGG Drugbank Geonames DBpedia Jamendo NYTimes SWDF LMDB"`
    query=${group}${i}
    sleep 1s
    cd /home/roott/federatedOptimizer/code
    /usr/bin/time -f "%e %P %t %M" timeout 1800s java -cp .:/home/roott/apache-jena-2.13.0/lib/*:/home/roott/federatedOptimizer/lib/fedX3.1/lib/* evaluateSPARQLQuery $fedBench/$query /home/roott/datasets /home/roott/fedBenchData 100000000 true false $newQueries/$query $generalPreds > outputFile 2> timeFile
    #cat timeFile
    #cat outputFile
    t=`cat timeFile`
    x=`grep "results=" outputFile`
    nr=`echo ${x##*results=}`
    cd /home/roott/federatedOptimizer/scripts
    ./killAll.sh /home/roott/tmp/proxyFederation
    sleep 10s
    pi=`./processProxyInfo.sh ${tmpFile} 0 8`
    echo "${query} ${t} ${pi} ${nr}"
done
