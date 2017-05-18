#!/bin/bash

folder=/home/roott/queries/fedBench
#folder=/home/roott/queries/complexQueries
#cp /home/roott/fedBenchFederationVirtuoso.ttl /home/roott/fedBenchFederation.ttl
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

#l="LS6"
#l="6D7 LD11 CD6 CD7 LS5"

for query in ${l}; do
    f=0
    cold=true
    for j in `seq 1 ${n}`; do
        cd /home/roott/FedX-HiBISCus
        if [ -f /home/roott/FedX-HiBISCus/summaries*?/* ]; then
            rm /home/roott/FedX-HiBISCus/summaries*?/*
        fi
        if [ "$cold" = "true" ] && [ -f cache.db ]; then
            rm cache.db
        fi
        /usr/bin/time -f "%e %P %t %M" timeout ${w}s ./cli.sh -c config.properties @q ${folder}/${query} > outputFile 2> timeFile

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
        echo "${query} ${nss} ${ns} ${s} ${t} ${nr}" >> outputExecuteQueriesHibiscusColdCacheA

        if [ "$f" -ge "2" ]; then
            break
        fi
    done
    f=0
    cold=false
    for j in `seq 1 ${n}`; do
        cd /home/roott/FedX-HiBISCus
        if [ -f /home/roott/FedX-HiBISCus/summaries*?/* ]; then
            rm /home/roott/FedX-HiBISCus/summaries*?/*
        fi
        if [ "$cold" = "true" ] && [ -f cache.db ]; then
            rm cache.db
        fi
        /usr/bin/time -f "%e %P %t %M" timeout ${w}s ./cli.sh -c config.properties @q ${folder}/${query} > outputFile 2> timeFile
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
        echo "${query} ${nss} ${ns} ${s} ${t} ${nr}" >> outputExecuteQueriesHibiscusWarmCacheA

        if [ "$f" -ge "2" ]; then
            break
        fi
    done
done
