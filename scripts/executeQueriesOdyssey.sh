#!/bin/bash

sed -i "s,optimize=.*,optimize=false," /home/roott/federatedOptimizer/lib/fedX3.1/config2
#cp /home/roott/fedBenchFederationFuseki.ttl /home/roott/fedBenchFederation.ttl
cp /home/roott/fedBenchFederationVirtuoso.ttl /home/roott/fedBenchFederation.ttl
fedBench=/home/roott/queries/fedBench
newQueries=/home/roott/queries/fedBench_1_1
#fedBench=/home/roott/queries/complexQueries
#newQueries=/home/roott/queries/complexQueries_1_1
generalPreds=/home/roott/generalPredicates
#datasets=/home/roott/datasetsFuseki
datasets=/home/roott/datasetsVirtuoso
cold=true
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

#s=`seq 1 10`
#l=""

#for i in ${s}; do
#    l="${l} C${i}"
#done

#l="LD7 LD11 CD6 CD7 LS5"
#l="LD6 LD9 LS7"

for query in ${l}; do
    f=0
    for j in `seq 1 ${n}`; do
        cd /home/roott/federatedOptimizer/code
        if [ "$cold" = "true" ] && [ -f /home/roott/federatedOptimizer/lib/fedX3.1/cache.db ]; then
            rm /home/roott/federatedOptimizer/lib/fedX3.1/cache.db
        fi
        /usr/bin/time -f "%e %P %t %M" timeout ${w}s java -cp .:/home/roott/apache-jena-2.13.0/lib/*:/home/roott/federatedOptimizer/lib/fedX3.1/lib/* evaluateSPARQLQuery $fedBench/$query ${datasets} /home/roott/fedBenchData 100000000 true false $newQueries/$query $generalPreds > outputFile 2> timeFile
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
        echo "${query} ${nss} ${ns} ${s} ${t} ${nr}"
        if [ "$f" -ge "2" ]; then
            break
        fi
        #echo "${query}" >> outputFileAccOur
        #cat outputFile >> outputFileAccOur
        #echo "${query}" >> timeFileAccOur
        #cat timeFile >> timeFileAccOur
    done
done
