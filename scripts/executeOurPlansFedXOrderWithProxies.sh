#!/bin/bash

sed -i "s,optimize=.*,optimize=false," /home/roott/federatedOptimizer/lib/fedX3.1/config2
#cp /home/roott/fedBenchFederationFuseki.ttl /home/roott/fedBenchFederation.ttl
#cp /home/roott/fedBenchFederationVirtuoso.ttl /home/roott/fedBenchFederation.ttl
cp /home/roott/fedBenchFederationProxy.ttl /home/roott/fedBenchFederation.ttl
fedBench=/home/roott/queries/fedBench
newQueries=/home/roott/queries/fedBench_1_1
#fedBench=/home/roott/queries/complexQueries
#newQueries=/home/roott/queries/complexQueries_1_1
generalPreds=/home/roott/generalPredicates
#datasets=/home/roott/datasetsFuseki
#datasets=/home/roott/datasetsVirtuoso
datasets=/home/roott/datasets
cold=true
s=`seq 1 11`
l=""
n=1
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

#s=`seq 1 10`
#l=""

#for i in ${s}; do
#    l="${l} C${i}"
#done

#l="LD7 LD11 CD6 CD7 LS5"
#l="LD3 LD7 LD11 LS3 LS5 LS7"
l="LS6"

for query in ${l}; do
    f=0
    for j in `seq 1 ${n}`; do
        cd /home/roott/federatedOptimizer/scripts
        #tmpFile=`./startProxies.sh 8891 8899 3030 "ChEBI KEGG Drugbank Geonames DBpedia Jamendo NYTimes SWDF LMDB"`
        tmpFile=`./startProxies2.sh "172.19.2.123 172.19.2.106 172.19.2.100 172.19.2.115 172.19.2.107 172.19.2.118 172.19.2.111 172.19.2.113 172.19.2.120" 3030`
        sleep 1s
        #cd /home/roott/federatedOptimizer/scripts
        #./startFederation.sh > outputStartFederation
        #sleep 1m
        cd /home/roott/federatedOptimizer/code
        if [ "$cold" = "true" ] && [ -f /home/roott/federatedOptimizer/lib/fedX3.1/cache.db ]; then
            rm /home/roott/federatedOptimizer/lib/fedX3.1/cache.db
        fi
        /usr/bin/time -f "%e %P %t %M" timeout ${w}s java -Xmx4096m -cp .:/home/roott/apache-jena-2.13.0/lib/*:/home/roott/federatedOptimizer/lib/fedX3.1/lib/* evaluateOurPlansWithFedXOrder $fedBench/$query ${datasets} /home/roott/fedBenchData 100000000 true false $newQueries/$query $generalPreds > outputFile 2> timeFile
        x=`grep "planning=" outputFile`
        y=`echo ${x##*planning=}`

        if [ -n "$y" ]; then
            d=`echo ${y%%ms*}`
        else
            d=-1
        fi

        x=`grep "NumberSelectedSources=" outputFile`
        y=`echo ${x##*NumberSelectedSources=}`

        if [ -n "$y" ]; then
            nss=`echo ${y}`
        else
            /home/roott/federatedOptimizer/scripts/processFedXPlansNSS.sh outputFile > xxx
            nss=`cat xxx`
            rm xxx
        fi

        x=`grep "NumberServices=" outputFile`
        y=`echo ${x##*NumberServices=}`

        if [ -n "$y" ]; then
            ns=`echo ${y}`
        else
            /home/roott/federatedOptimizer/scripts/processFedXPlansNSQ.sh outputFile > xxx
            ns=`cat xxx`
            rm xxx
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
        cd /home/roott/federatedOptimizer/scripts
        ./killAll.sh /home/roott/tmp/proxyFederation
        sleep 10s
        pi=`./processProxyInfo.sh ${tmpFile} 0 8`
        echo "${query} ${nss} ${ns} ${d} ${t} ${pi} ${nr}"
        
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
