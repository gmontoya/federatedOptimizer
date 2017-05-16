#!/bin/bash

folder=/home/roott/queries/fedBench
#folder=/home/roott/queries/complexQueries
sed -i "s,optimize=.*,optimize=true," /home/roott/federatedOptimizer/lib/fedX3.1/config2
cp /home/roott/fedBenchFederationVirtuoso.ttl /home/roott/fedBenchFederation.ttl
#cp /home/roott/fedBenchFederationFuseki.ttl /home/roott/fedBenchFederation.ttl
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

#for i in ${s}; do
#    l="${l} C${i}"
#done

#l="LD3 LD11 CD3 CD7 LS3 LS4 LS5"
#l="LD7 LD11 CD6 CD7 LS5"

for query in ${l}; do
    f=0
    for j in `seq 1 ${n}`; do
        cd /home/roott/federatedOptimizer/lib/fedX3.1
        if [ "$cold" = "true" ] && [ -f cache.db ]; then
            rm cache.db
        fi
        /usr/bin/time -f "%e %P %t %M" timeout ${w}s ./cli.sh -c config2 -d /home/roott/fedBenchFederation.ttl @q ${folder}/${query} > outputFile 2> timeFile

        x=`grep "planning=" outputFile`
        y=`echo ${x##*planning=}`
        if [ -n "$y" ]; then
            s=`echo ${y%%ms*}`
        else 
            s=-1
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
        /home/roott/federatedOptimizer/scripts/processFedXPlansNSS.sh outputFile > xxx
        nss=`cat xxx` 
        /home/roott/federatedOptimizer/scripts/processFedXPlansNSQ.sh outputFile > xxx
        ns=`cat xxx`
        rm xxx
        echo "${query} ${nss} ${ns} ${s} ${t} ${nr}"
        #cat outputFile >> outputFilePlans
        #cat timeFile >> timeFilePlans
        if [ "$f" -ge "2" ]; then
            break
        fi
    done
done
