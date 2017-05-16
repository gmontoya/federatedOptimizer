#!/bin/bash

sed -i "s,optimize=.*,optimize=false," /home/roott/federatedOptimizer/lib/fedX3.1/config2
cp /home/roott/fedBenchFederationVirtuoso.ttl /home/roott/fedBenchFederation.ttl
fedBench=/home/roott/queries/fedBench
newQueries=/home/roott/queries/fedBench_1_1_VOID
generalPreds=/home/roott/generalPredicates
datasets=/home/roott/datasetsVirtuoso

s=`seq 1 11`
l=""
n=10
w=1800
cold=true
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

#l="LD5"
for query in ${l}; do
    #cd /home/roott/federatedOptimizer/scripts
    #tmpFile=`./startProxies.sh 8891 8899 3030 "ChEBI KEGG Drugbank Geonames DBpedia Jamendo NYTimes SWDF LMDB"`
    #sleep 1s
    f=0
    for j in `seq 1 ${n}`; do
        cd /home/roott/federatedOptimizer/code
        if [ "$cold" = "true" ] && [ -f /home/roott/federatedOptimizer/lib/fedX3.1/cache.db ]; then
            rm /home/roott/federatedOptimizer/lib/fedX3.1/cache.db
        fi
        /usr/bin/time -f "%e %P %t %M" timeout ${w}s java -cp .:/home/roott/apache-jena-2.13.0/lib/*:/home/roott/federatedOptimizer/lib/fedX3.1/lib/* evaluateSPARQLQueryVOID $fedBench/$query ${datasets} /home/roott/fedBenchData $newQueries/$query > outputFile 2>> timeFile
        x=`tail -n 1 timeFile`
        y=`echo ${x%% *}`
        x=`echo ${y%%.*}`
        if [ "$x" -ge "$w" ]; then
            t=`echo $y`
            t=`echo "scale=2; $t*1000" | bc`
            f=$(($f+1))
            nr=`wc -l outputFile | sed 's/^[ ^t]*//' | cut -d' ' -f1`
            nr=$(($nr-1))
        else
            x=`grep "duration=" outputFile`
            y=`echo ${x##*duration=}`
            t=`echo ${y%%ms*}`
            x=`grep "results=" outputFile`
            nr=`echo ${x##*results=}`
        fi
        x=`grep "planning=" outputFile`
        y=`echo ${x##*planning=}`

        if [ -n "$y" ]; then
            s=`echo ${y%%ms*}`
        else
            s=-1
        fi

        x=`grep "NumberSelectedSources=" outputFile`
        y=`echo ${x##*NumberSelectedSources=}`

        if [ -n "$y" ]; then
            nss=`echo ${y}`
        else
            nss=-1
        fi

        x=`grep "NumberServices=" outputFile`
        y=`echo ${x##*NumberServices=}`

        if [ -n "$y" ]; then
            ns=`echo ${y}`
        else
            ns=-1
        fi


        
        #ns=`grep SERVICE outputFile | wc -l | sed 's/^[ ^t]*//' | cut -d' ' -f1`
        #cd /home/roott/federatedOptimizer/scripts
        #./killAll.sh /home/roott/tmp/proxyFederation
        #sleep 10s
        #pi=`./processProxyInfo.sh ${tmpFile} 0 8`
        #echo "${query} ${s} ${t} ${pi} ${nr}"
        #echo "${query} ${s} ${t} ${nr} ${ns}"
        echo "${query} ${nss} ${ns} ${s} ${t} ${nr}"
        if [ "$f" -ge "2" ]; then
            break
        fi
    done
done
