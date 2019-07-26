#!/bin/bash

. ./configFile

federationDescriptionFile=${federationPath}/fedBenchFederation.ttl
sed -i "s,optimize=.*,optimize=true," ${fedXConfigFile}
cold=true

auxFile=`mktemp`
outputFile=`mktemp`
errorFile=`mktemp`

s=`seq 1 11`
l=""
n=${numRuns}
w=${timeoutValue}

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
    f=0
    for j in `seq 1 ${n}`; do
        cd ${fedXPath}
        if [ "$cold" = "true" ] && [ -f cache.db ]; then
            rm cache.db
        fi
        /usr/bin/time -f "%e %P %t %M" timeout ${w}s ./cli.sh -c config2 -d ${federationDescriptionFile} @q ${queriesFolder}/${query} > ${outputFile} 2> ${errorFile}

        x=`grep "planning=" ${outputFile}`
        y=`echo ${x##*planning=}`
        if [ -n "$y" ]; then
            s=`echo ${y%%ms*}`
        else 
            s=-1
        fi
        x=`tail -n 1 ${errorFile}`
        y=`echo ${x%% *}`
        x=`echo ${y%%.*}`
        if [ "$x" -ge "$w" ]; then
            t=`echo $y`
            t=`echo "scale=2; $t*1000" | bc`
            f=$(($f+1))
            nr=`grep "^\[" ${outputFile} | grep "\]$" | wc -l | sed 's/^[ ^t]*//' | cut -d' ' -f1`
        else
            x=`grep "duration=" ${outputFile}`
            y=`echo ${x##*duration=}`
            t=`echo ${y%%ms*}`
            x=`grep "results=" ${outputFile}`
            nr=`echo ${x##*results=}`
        fi
        ${federatedOptimizerPath}/scripts/processFedXPlansNSS.sh ${outputFile} > ${auxFile}
        nss=`cat ${auxFile}` 
        ${federatedOptimizerPath}/scripts/processFedXPlansNSQ.sh ${outputFile} > ${auxFile}
        ns=`cat ${auxFile}`
        rm ${auxFile}
        echo "${query} ${nss} ${ns} ${s} ${t} ${nr}"
        if [ "$f" -ge "2" ]; then
            break
        fi
    done
done

rm -f ${outputFile}
rm -f ${errorFile}
