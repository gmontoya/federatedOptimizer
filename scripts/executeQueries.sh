#!/bin/bash

folder=/home/roott/queries/fedBench_1_1
sed -i "s,optimize=.*,optimize=false," /home/roott/federatedOptimizer/lib/fedX3.1/config2

for i in `seq 1 11`; do
    cd /home/roott/federatedOptimizer/scripts
    tmpFile=`./startProxies.sh 8891 8899 3030 "ChEBI KEGG Drugbank Geonames DBpedia Jamendo NYTimes SWDF LMDB"`
    query=LD${i}
    cd /home/roott/federatedOptimizer/lib/fedX3.1
    /usr/bin/time -f "%e %P %t %M" ./cli.sh -c config2 -d /home/roott/fedBenchFederation.ttl -f JSON -folder results @q ${folder}/${query} > outputFile 2> timeFile
    t=`cat timeFile`
    x=`grep "results=" outputFile`
    nr=`echo ${x##*results=}`
    cd /home/roott/federatedOptimizer/scripts
    ./killAll.sh /home/roott/tmp/proxyFederation
    sleep 10s
    pi=`./processProxyInfo.sh ${tmpFile} 0 8`
    echo "${query} ${t} ${pi} ${nr}"
done
