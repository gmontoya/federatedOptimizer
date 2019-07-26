#!/bin/bash

. ./configFile

sed -i "s,optimize=.*,optimize=false," ${fedXConfigFile}

cold=true
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
        cd ${federatedOptimizerPath}/code
        if [ "$cold" = "true" ] && [ -f ${fedXPath}/cache.db ]; then
            rm ${fedXPath}/cache.db
        fi
        /usr/bin/time -f "%e %P %t %M" timeout ${w}s java -cp .:${JENA_HOME}/lib/*:${fedXPath}/lib/* evaluateFedXPlan ${queriesFolder}/$query ${federationFile} ${fedBenchDataPath} 100000000 true false > ${outputFile} 2> ${errorFile}
        x=`grep "decomposition=" ${outputFile}`
        y=`echo ${x##*decomposition=}`

        if [ -n "$y" ]; then
            d=`echo ${y%%ms*}`
        else
            d=-1
        fi
        x=`grep "ordering=" ${outputFile}`
        y=`echo ${x##*ordering=}`

        if [ -n "$y" ]; then
            o=`echo ${y%%ms*}`
        else
            o=-1
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
        echo "${query} ${d} ${o} ${t} ${nr}"
        if [ "$f" -ge "2" ]; then
            break
        fi
    done
done

rm -f ${outputFile}
rm -f ${errorFile}
