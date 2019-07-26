#!/bin/bash

. ./configFile

splendidFederationFile=${federationPath}/splendidFedBenchFederation.n3

s=`seq 1 11`
l=""

auxFile=`mktemp`
outputFile=`mktemp`
errorFile=`mktemp`

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
        cd ${SPLENDID_HOME}
        /usr/bin/time -f "%e %P %t %M" timeout ${w}s ./SPLENDID.sh ${splendidFederationFile} ${queriesFolder}/${query} > ${outputFile} 2> ${errorFile}

        x=`tail -n 1 ${errorFile}`
        y=`echo ${x%% *}`
        x=`echo ${y%%.*}`
        if [ "$x" -ge "$w" ]; then
            t=`echo $y`
            t=`echo "scale=2; $t*1000" | bc`
            f=$(($f+1))
            ${federatedOptimizerPath}/fixJSONAnswer.sh ${outputFile}
        else
            x=`grep "duration=" ${errorFile}`
            y=`echo ${x##*duration=}`
            t=`echo ${y%%ms*}`
        fi
        x=`grep "planning=" ${errorFile}`
        y=`echo ${x##*planning=}`

        if [ -n "$y" ]; then
            s=`echo ${y%%ms*}`
        else
            s=-1
        fi

        nr=`python ${federatedOptimizerPath}/formatJSONFile.py ${outputFile} | wc -l | sed 's/^[ ^t]*//' | cut -d' ' -f1`

        ${federatedOptimizerPath}/processPlansSplendidNSS.sh ${errorFile} > ${auxFile}
        nss=`cat ${auxFile}`
        ${federatedOptimizerPath}/processPlansSplendidNSQ.sh ${errorFile} > ${auxFile}
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
